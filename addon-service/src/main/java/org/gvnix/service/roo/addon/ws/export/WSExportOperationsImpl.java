/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.roo.addon.ws.export;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.JavaParserService;
import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.roo.addon.annotations.GvNIXWebParam;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.ws.WSConfigService.CommunicationSense;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 */
@Component
@Service
public class WSExportOperationsImpl implements WSExportOperations {

    private static Logger logger = Logger.getLogger(WSExportOperations.class
            .getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private WSConfigService wSConfigService;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private WSExportValidationService wSExportValidationService;
    @Reference
    private TypeLocationService typeLocationService;

    /**
     * {@inheritDoc}
     */
    public boolean isProjectAvailable() {
        if (getPathResolver() == null) {
            return false;
        }

        // Checks if it's a web project
        String webXmlPath = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, "/WEB-INF/web.xml");
        return fileManager.exists(webXmlPath);
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        if (projectMetadata == null) {
            return null;
        }
        return projectMetadata.getPathResolver();
    }

    /**
     * {@inheritDoc}
     */
    public void exportService(JavaType serviceClass, String serviceName,
            String portTypeName, String targetNamespace, String addressName) {

        // Checks if Cxf is configured in the project and installs it if it's
        // not available.
        wSConfigService.install(CommunicationSense.EXPORT);

        // Localizes java file
        String fileLocation = projectOperations.getPathResolver()
                .getIdentifier(
                        Path.SRC_MAIN_JAVA,
                        serviceClass.getFullyQualifiedTypeName()
                                .replace('.', '/').concat(".java"));

        if (!fileManager.exists(fileLocation)) {
            logger.log(
                    Level.INFO,
                    "Creating a new class '".concat(
                            serviceClass.getSimpleTypeName()).concat(
                            " to export web service."));
            // Create service class with Service Annotation.
            javaParserService.createServiceClass(serviceClass);
        }

        // Prepares attributes for @gvNIXWebService annotation
        List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = exportServiceAnnotationAttributes(
                serviceClass, serviceName, portTypeName, targetNamespace,
                addressName);

        annotationsService.addJavaTypeAnnotation(serviceClass,
                GvNIXWebService.class.getName(), gvNixAnnotationAttributes,
                false);

        // Installs jax2ws plugin in project.
        wSConfigService.installJava2wsPlugin();

        // Add GvNixAnnotations dependency to the project.
        annotationsService.addGvNIXAnnotationsDependency();
    }

    /**
     * {@inheritDoc}
     */
    public List<AnnotationAttributeValue<?>> exportServiceAnnotationAttributes(
            JavaType serviceClass, String serviceName, String portTypeName,
            String targetNamespace, String addressName) {
        // Checks serviceName parameter to publish the web service.
        serviceName = StringUtils.hasText(serviceName) ? serviceName
                : serviceClass.getSimpleTypeName();

        // Checks correct namespace format.
        Assert.isTrue(
                wSExportValidationService.checkNamespaceFormat(targetNamespace),
                "The namespace for Target Namespace has to be defined using URI fromat.\ni.e.: http://name.of.namespace/");

        // Namespace for the web service.
        targetNamespace = StringUtils.hasText(targetNamespace) ? targetNamespace
                : wSConfigService.convertPackageToTargetNamespace(serviceClass
                        .getPackage().toString());

        // Check address name not blank and set service name if not defined.
        addressName = StringUtils.hasText(addressName) ? StringUtils
                .capitalize(addressName) : serviceClass.getSimpleTypeName();

        // Define @GvNIXWebService annotation and attributes.
        // Check port type attribute name format and add attributes to a list.
        List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        portTypeName = StringUtils.hasText(portTypeName) ? portTypeName
                : serviceName.concat("PortType");
        gvNixAnnotationAttributes.add(new StringAttributeValue(
                new JavaSymbolName("name"), portTypeName));
        gvNixAnnotationAttributes.add(new StringAttributeValue(
                new JavaSymbolName("targetNamespace"), targetNamespace));
        gvNixAnnotationAttributes.add(new StringAttributeValue(
                new JavaSymbolName("serviceName"), serviceName));
        gvNixAnnotationAttributes.add(new StringAttributeValue(
                new JavaSymbolName("address"), addressName));
        gvNixAnnotationAttributes.add(new StringAttributeValue(
                new JavaSymbolName("fullyQualifiedTypeName"), serviceClass
                        .getFullyQualifiedTypeName()));
        gvNixAnnotationAttributes.add(new BooleanAttributeValue(
                new JavaSymbolName("exported"), false));

        return gvNixAnnotationAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public void exportOperation(JavaType serviceClass,
            JavaSymbolName methodName, String operationName, String resultName,
            String resultNamespace, String responseWrapperName,
            String responseWrapperNamespace, String requestWrapperName,
            String requestWrapperNamespace) {

        Assert.notNull(serviceClass, "Java type required");
        Assert.notNull(methodName, "Operation name required");

        // Check if serviceClass is a Web Service. If doesn't exist exports as
        // service
        if (!isWebServiceClass(serviceClass)) {
            // Export as a service.
            exportService(serviceClass, null, null, null, null);
        }

        String webServiceTargetNamespace = wSExportValidationService
                .getWebServiceDefaultNamespace(serviceClass);

        // Check if method exists in the class.
        Assert.isTrue(isMethodAvailableToExport(serviceClass, methodName),
                "The method: '" + methodName + " doesn't exists in the class '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Check authorized JavaTypes in operation.
        // Also, adds @GvNIXXmlElement annotation to any related project type
        // which needs it.
        wSExportValidationService.prepareAuthorizedJavaTypesInOperation(
                serviceClass, methodName);

        // Check if method has return type.
        JavaType returnType = returnJavaType(serviceClass, methodName);

        Assert.isTrue(returnType != null,
                "The method: '" + methodName + " doesn't exists in the class '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        boolean isReturnTypeVoid = returnType.equals(JavaType.VOID_OBJECT)
                || returnType.equals(JavaType.VOID_PRIMITIVE);

        if (isReturnTypeVoid) {
            resultName = "void";
        } else if (!StringUtils.hasText(resultName)) {

            resultName = "return";
        }

        // Check if method throws an Exception and update it with annotations
        // if it's needed.
        wSExportValidationService.prepareMethodExceptions(serviceClass,
                methodName, webServiceTargetNamespace);

        // Checks correct namespace format.
        if (!isReturnTypeVoid) {

            Assert.isTrue(
                    wSExportValidationService
                            .checkNamespaceFormat(resultNamespace),
                    "The namespace for result has to start with 'http://'.\ni.e.: http://name.of.namespace/");
            Assert.isTrue(
                    wSExportValidationService
                            .checkNamespaceFormat(responseWrapperNamespace),
                    "The namespace for Response Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        }

        Assert.isTrue(
                wSExportValidationService
                        .checkNamespaceFormat(requestWrapperNamespace),
                "The namespace for Request Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        // Create annotations to selected Method
        List<AnnotationMetadata> annotationMetadataUpdateList = getAnnotationsToExportOperation(
                serviceClass, methodName, operationName, resultName,
                returnType, resultNamespace, responseWrapperName,
                responseWrapperNamespace, requestWrapperName,
                requestWrapperNamespace, webServiceTargetNamespace);

        // Add @GvNIXWebParam & @WebParam parameter annotations.
        List<AnnotatedJavaType> annotationWebParamMetadataList = getMethodParameterAnnotations(
                serviceClass, methodName, webServiceTargetNamespace);

        javaParserService.updateMethodAnnotations(serviceClass, methodName,
                annotationMetadataUpdateList, annotationWebParamMetadataList);

    }

    /**
     * Returns method return JavaType.
     * 
     * @param serviceClass
     *            where the method is defined.
     * @param methodName
     *            to search.
     * @return {@link JavaType}
     */
    private JavaType returnJavaType(JavaType serviceClass,
            JavaSymbolName methodName) {

        JavaType returnType = new JavaType(JavaType.VOID_OBJECT.toString());

        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInAll(serviceClass, methodName);

        if (methodMetadata == null) {
            return null;
        }

        if (methodMetadata.getReturnType() != null) {
            returnType = methodMetadata.getReturnType();
        }

        return returnType;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * </p>
     * <p>
     * Annotations to create:
     * </p>
     * <ul>
     * <li>@GvNIXWebMethod with params:</li>
     * <ul>
     * <li>operationName</li>
     * <li>webResultType</li>
     * <li>resultName</li>
     * <li>resultNamespace</li>
     * <li>requestWrapperName</li>
     * <li>requestWrapperNamespace</li>
     * <li>requestWrapperClassName</li>
     * <li>responseWrapperName</li>
     * <li>responseWrapperNamespace</li>
     * <li>responseWrapperClassName</li>
     * <li>
     * <ul>
     * </ul>
     */
    public List<AnnotationMetadata> getAnnotationsToExportOperation(
            JavaType serviceClass, JavaSymbolName methodName,
            String operationName, String resultName, JavaType returnType,
            String resultNamespace, String responseWrapperName,
            String responseWrapperNamespace, String requestWrapperName,
            String requestWrapperNamespace, String webServiceTargetNamespace) {

        List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();
        List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        // @WebMethod.operationName
        operationName = StringUtils.hasText(operationName) ? operationName
                : methodName.getSymbolName();
        StringAttributeValue operationNameAttributeValue = new StringAttributeValue(
                new JavaSymbolName("operationName"), operationName);
        annotationAttributeValueList.add(operationNameAttributeValue);

        // Gets target method
        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInAll(serviceClass, methodName);

        // Check input parameters.
        if (!methodMetadata.getParameterTypes().isEmpty()
                && !methodMetadata.getParameterNames().isEmpty()) {
            // no input parameters. Prepare request wrapper

            // javax.xml.ws.RequestWrapper
            requestWrapperName = StringUtils.hasText(requestWrapperName) ? requestWrapperName
                    : operationName;
            StringAttributeValue requestWrapperNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperName"),
                    requestWrapperName);
            annotationAttributeValueList.add(requestWrapperNameAttributeValue);

            requestWrapperNamespace = StringUtils
                    .hasText(requestWrapperNamespace) ? requestWrapperNamespace
                    : webServiceTargetNamespace;

            // RequestWrapper namespace
            StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperNamespace"),
                    requestWrapperNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            // Wrapper class
            String className = serviceClass
                    .getPackage()
                    .getFullyQualifiedPackageName()
                    .concat(".")
                    .concat(StringUtils.capitalize(requestWrapperName).concat(
                            "RequestWrapper"));
            StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperClassName"), className);
            annotationAttributeValueList.add(classNameAttributeValue);

        }

        // Check return value
        if ((resultName != null && returnType != null)
                && !(returnType.equals(JavaType.VOID_PRIMITIVE) || (returnType
                        .equals(JavaType.VOID_PRIMITIVE)))) {

            // Not void method

            // resultName
            StringAttributeValue resultNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultName"), resultName);
            annotationAttributeValueList.add(resultNameAttributeValue);

            // resultNamespace
            resultNamespace = StringUtils.hasText(resultNamespace) ? resultNamespace
                    : webServiceTargetNamespace;
            StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultNamespace"), resultNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            // result type
            ClassAttributeValue resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"), returnType);
            annotationAttributeValueList.add(resultTypeAttributeValue);

            // javax.xml.ws.ResponseWrapper

            // response wrapper name
            responseWrapperName = StringUtils.hasText(responseWrapperName) ? responseWrapperName
                    : operationName.concat("Response");
            StringAttributeValue responseWrapperNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperName"),
                    responseWrapperName);
            annotationAttributeValueList.add(responseWrapperNameAttributeValue);

            // response wrapper namespace
            responseWrapperNamespace = StringUtils
                    .hasText(responseWrapperNamespace) ? responseWrapperNamespace
                    : webServiceTargetNamespace;
            targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperNamespace"),
                    responseWrapperNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            // Class for response wrapper
            String className = serviceClass.getPackage()
                    .getFullyQualifiedPackageName().concat(".")
                    .concat(StringUtils.capitalize(responseWrapperName));
            StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperClassName"), className);
            annotationAttributeValueList.add(classNameAttributeValue);

        } else {

            // is a void method

            // result name
            StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultName"), "void");
            annotationAttributeValueList.add(localNameAttributeValue);

            // result type
            ClassAttributeValue resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"),
                    JavaType.VOID_PRIMITIVE);
            annotationAttributeValueList.add(resultTypeAttributeValue);
        }

        // Create annotation.
        // org.gvnix.service.roo.addon.annotations.GvNIXWebMethod
        AnnotationMetadata gvNIXWebMethod = new AnnotationMetadataBuilder(
                new JavaType(GvNIXWebMethod.class.getName()),
                annotationAttributeValueList).build();

        annotationMetadataList.add(gvNIXWebMethod);

        return annotationMetadataList;
    }

    /**
     * Checks if the selected class exists and contains {@link WSExportMetadata}
     * .
     * 
     * @param serviceClass
     *            class to be checked.
     * @return true if the {@link JavaType} contains {@link WSExportMetadata}.
     */
    private boolean isWebServiceClass(JavaType serviceClass) {
        // Gets PhysicalTypeIdentifier for serviceClass
        String id = physicalTypeMetadataProvider.findIdentifier(serviceClass);

        Assert.notNull(
                id,
                "Cannot locate source for '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Prepares WSExportMetadata identifier.
        PhysicalTypeIdentifier.getJavaType(id);
        Path path = PhysicalTypeIdentifier.getPath(id);
        String entityMid = WSExportMetadata
                .createIdentifier(serviceClass, path);

        // Get the service layer ws metadata.
        WSExportMetadata wSExportMetadata = (WSExportMetadata) metadataService
                .get(entityMid);

        if (wSExportMetadata == null) {
            // it isn't an exported service
            return false;
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMethodAvailableToExport(JavaType serviceClass,
            JavaSymbolName methodName) {

        boolean isAnnotated = true;

        // Gets method information
        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInAll(serviceClass, methodName);

        if (methodMetadata == null) {
            return false;
        }

        // Checks if it already has @GvNIXWebMethod
        isAnnotated = javaParserService.isAnnotationIntroducedInMethod(
                GvNIXWebMethod.class.getName(), methodMetadata);

        return !isAnnotated;
    }

    /**
     * {@inheritDoc}
     */
    public List<AnnotatedJavaType> getMethodParameterAnnotations(
            JavaType serviceClass, JavaSymbolName methodName,
            String webServiceTargetNamespace) {

        // List to store annotations for parameters
        List<AnnotatedJavaType> annotatedWebParameterList = new ArrayList<AnnotatedJavaType>();

        // Method information
        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInAll(serviceClass, methodName);

        // Method parameter types
        List<AnnotatedJavaType> parameterTypesList = methodMetadata
                .getParameterTypes();

        // Method parameter names
        List<JavaSymbolName> parameterNamesList = methodMetadata
                .getParameterNames();

        if (parameterTypesList.isEmpty() && parameterNamesList.isEmpty()) {
            return annotatedWebParameterList;
        }

        AnnotatedJavaType parameterWithAnnotations;
        List<AnnotationMetadata> parameterAnnotationList;

        // prepares @WebParam default values.
        StringAttributeValue partNameAttributeValue = new StringAttributeValue(
                new JavaSymbolName("partName"), "parameters");
        EnumAttributeValue modeAttributeValue = new EnumAttributeValue(
                new JavaSymbolName("mode"), new EnumDetails(new JavaType(
                        "javax.jws.WebParam.Mode"), new JavaSymbolName("IN")));
        BooleanAttributeValue headerAttributeValue = new BooleanAttributeValue(
                new JavaSymbolName("header"), false);

        // for each parameters
        for (AnnotatedJavaType parameterType : parameterTypesList) {

            // annotation of this parameter
            parameterAnnotationList = new ArrayList<AnnotationMetadata>();

            // values for @GvNIXWebParam annotation
            List<AnnotationAttributeValue<?>> gvNIXWebParamAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            // Get current parameter name
            int index = parameterTypesList.indexOf(parameterType);
            JavaSymbolName parameterName = parameterNamesList.get(index);

            // @GvNIXWebParam.name
            StringAttributeValue nameWebParamAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("name"), parameterName.getSymbolName());
            gvNIXWebParamAttributeValueList.add(nameWebParamAttributeValue);

            // @GvNIXWebParam.type
            ClassAttributeValue typeClassAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("type"), parameterType.getJavaType());
            gvNIXWebParamAttributeValueList.add(typeClassAttributeValue);

            // Build @GvNIXWebParam annotation
            AnnotationMetadata gvNixWebParamAnnotationMetadata = new AnnotationMetadataBuilder(
                    new JavaType(GvNIXWebParam.class.getName()),
                    gvNIXWebParamAttributeValueList).build();

            // Add @GvNIXWebParam to annotation list
            parameterAnnotationList.add(gvNixWebParamAnnotationMetadata);

            // values for @WebParam annotation
            List<AnnotationAttributeValue<?>> webParamAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            // @WebParam.name
            webParamAttributeValueList.add(nameWebParamAttributeValue);

            // if not a primitive or standard type
            if (!parameterType.getJavaType().isPrimitive()
                    && !parameterType.getJavaType().isCommonCollectionType()
                    && !parameterType.getJavaType().getFullyQualifiedTypeName()
                            .startsWith("java.lang")) {

                // @WebParam.targetnamespace
                StringAttributeValue targetNamespace = new StringAttributeValue(
                        new JavaSymbolName("targetNamespace"),
                        webServiceTargetNamespace);
                webParamAttributeValueList.add(targetNamespace);
            }

            // @WebParam.partName <-- parameters (default)
            webParamAttributeValueList.add(partNameAttributeValue);

            // @WebParam.mode <-- IN (default)
            webParamAttributeValueList.add(modeAttributeValue);

            // @WebParam.header <-- false (default)
            webParamAttributeValueList.add(headerAttributeValue);

            // Build annotation @WebParam
            AnnotationMetadata webParamAnnotationMetadata = new AnnotationMetadataBuilder(
                    new JavaType("javax.jws.WebParam"),
                    webParamAttributeValueList).build();
            // Add @GvNIXWebParam to annotation list
            parameterAnnotationList.add(webParamAnnotationMetadata);

            // Add annotation list to parameter.
            parameterWithAnnotations = new AnnotatedJavaType(
                    parameterType.getJavaType(), parameterAnnotationList);

            annotatedWebParameterList.add(parameterWithAnnotations);

        }

        return annotatedWebParameterList;
    }

    /**
     * {@inheritDoc}
     */
    public String getAvailableServiceOperationsToExport(JavaType serviceClass) {

        StringBuilder methodListStringBuilder = new StringBuilder();

        if (!isWebServiceClass(serviceClass)) {

            // If class is not defined as web service.
            methodListStringBuilder
                    .append("Class '"
                            + serviceClass.getFullyQualifiedTypeName()
                            + "' is not defined as Web Service.\nUse the command 'service define ws --class "
                            + serviceClass.getFullyQualifiedTypeName()
                            + "' to export as web service.");

            return methodListStringBuilder.toString();
        }

        String id = physicalTypeMetadataProvider.findIdentifier(serviceClass);

        Assert.notNull(
                id,
                "Cannot locate source for '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Get service class details
        ClassOrInterfaceTypeDetails tmpServiceDetails = typeLocationService
                .getClassOrInterface(serviceClass);

        // Checks if it's mutable
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                tmpServiceDetails,
                "Can't modify " + tmpServiceDetails.getName());

        MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;

        List<? extends MethodMetadata> methodList = serviceDetails
                .getDeclaredMethods();

        // If there aren't any methods in class.
        if (methodList == null || methodList.isEmpty()) {

            methodListStringBuilder.append("Class '"
                    + serviceClass.getFullyQualifiedTypeName()
                    + "' has not defined any method.");

            return methodListStringBuilder.toString();
        }

        boolean isAnnotationIntroduced;

        // for every method
        for (MethodMetadata methodMetadata : methodList) {

            // already has @GvNIXWebMethod
            isAnnotationIntroduced = javaParserService
                    .isAnnotationIntroducedInMethod(
                            GvNIXWebMethod.class.getName(), methodMetadata);

            // if ! has @GvNIXWebMethod
            if (!isAnnotationIntroduced) {
                if (!StringUtils.hasText(methodListStringBuilder.toString())) {
                    methodListStringBuilder
                            .append("Method list to export as web service operation in '"
                                    + serviceClass.getFullyQualifiedTypeName()
                                    + "':\n");
                }
                // add method name to list
                methodListStringBuilder
                        .append("\t* "
                                + methodMetadata.getMethodName()
                                        .getSymbolName() + "\n");
            }

        }

        // If there aren't defined any methods available to export.
        if (!StringUtils.hasText(methodListStringBuilder.toString())) {
            methodListStringBuilder
                    .append("Class '"
                            + serviceClass.getFullyQualifiedTypeName()
                            + "' hasn't got any available method to export as web service operations.");
        }

        return methodListStringBuilder.toString();
    }

    /** {@inheritDoc} **/
    public List<String> getServiceList() {
        List<String> classNames = new ArrayList<String>();

        // Gets all classes annotated with @GvNIxWebService
        Set<ClassOrInterfaceTypeDetails> cids = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
                        GvNIXWebService.class.getName()));
        for (ClassOrInterfaceTypeDetails cid : cids) {
            // Exclude abstract classes
            if (Modifier.isAbstract(cid.getModifier())) {
                continue;
            }
            classNames.add(cid.getName().getFullyQualifiedTypeName());
        }
        return classNames;
    }
}
