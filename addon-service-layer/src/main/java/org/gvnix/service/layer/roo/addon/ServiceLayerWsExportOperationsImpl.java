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
package org.gvnix.service.layer.roo.addon;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.*;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.*;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerWsExportOperationsImpl implements
        ServiceLayerWsExportOperations {

    private static Logger logger = Logger
            .getLogger(ServiceLayerWsExportOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private ServiceLayerWSExportValidationService serviceLayerWSExportValidationService;

    /**
     * {@inheritDoc}
     */
    public boolean isProjectAvailable() {

        if (getPathResolver() == null) {

            return false;
        }

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "/WEB-INF/web.xml");
        if (!fileManager.exists(webXmlPath)) {

            return false;
        }

        return true;
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
     * 
     * <p>
     * If the class to export as web service doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     * 
     */
    public void exportService(JavaType serviceClass, String serviceName,
            String portTypeName, String targetNamespace, String addressName) {

        // Checks if Cxf is configured in the project and installs it if it's
        // not available.
        serviceLayerWsConfigService.install(CommunicationSense.EXPORT);

        String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
                serviceClass.getFullyQualifiedTypeName().replace('.', '/')
                        .concat(".java"));

        if (!fileManager.exists(fileLocation)) {
            logger.log(Level.INFO, "Crea la nueva clase de servicio: "
                    + serviceClass.getSimpleTypeName()
                    + " para publicarla como servicio web.");
            // Create service class with Service Annotation.
            javaParserService.createServiceClass(serviceClass);

        }

        List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = exportServiceAnnotationAttributes(
                serviceClass, serviceName, portTypeName, targetNamespace,
                addressName);

        annotationsService.addJavaTypeAnnotation(serviceClass,
                GvNIXWebService.class.getName(), gvNixAnnotationAttributes,
                false);

        // Installs jax2ws plugin in project.
        serviceLayerWsConfigService.installJaxwsBuildPlugin();

        // Add GvNixAnnotations to the project.
        annotationsService.addGvNIXAnnotationsDependency();
    }

    /**
     * Creates @GvNIXWebService annotation with default attributes.
     * 
     * @param serviceClass
     * 
     * @return @GvNIXWebService default annotation.
     */
    private AnnotationMetadata getDefaultGvNIXWebServiceAnnotation(
            JavaType serviceClass) {

        // Checks serviceName parameter to publish the web service.
        String serviceName =  serviceClass.getSimpleTypeName();

        // Namespace for the web service.
        String targetNamespace = serviceLayerWsConfigService
                        .convertPackageToTargetNamespace(serviceClass
                                .getPackage().toString());

        // Check address name not blank and set service name if not defined.
        String addressName = serviceClass.getSimpleTypeName();

        String portTypeName = serviceName.concat("PortType");

        // Define @GvNIXWebService annotation and attributes.
        // Check port type attribute name format and add attributes to a list.
        List<AnnotationAttributeValue<?>> gvNixWebServiceAnnotationAttributes = exportServiceAnnotationAttributes(
                serviceClass, serviceName, portTypeName, targetNamespace,
                addressName);

        // Create @GvNIXWebService with attributes.
        AnnotationMetadata defaultAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(GvNIXWebService.class.getName()),
                gvNixWebServiceAnnotationAttributes);

        return defaultAnnotationMetadata;
    }


    /**
     */
    public List<AnnotationAttributeValue<?>> exportServiceAnnotationAttributes(
            JavaType serviceClass, String serviceName, String portTypeName,
            String targetNamespace, String addressName) {
        // Checks serviceName parameter to publish the web service.
        serviceName = StringUtils.hasText(serviceName) ? serviceName
                : serviceClass.getSimpleTypeName();

        // Checks correct namespace format.
        Assert
                .isTrue(
                        serviceLayerWSExportValidationService
                                .checkNamespaceFormat(targetNamespace),
                        "The namespace for Target Namespace has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        // Namespace for the web service.
        targetNamespace = StringUtils.hasText(targetNamespace) ? targetNamespace
                : serviceLayerWsConfigService
                        .convertPackageToTargetNamespace(serviceClass
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
        return gvNixAnnotationAttributes;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void exportOperation(JavaType serviceClass,
            JavaSymbolName methodName, String operationName, String resultName,
            String resultNamespace, String responseWrapperName,
            String responseWrapperNamespace, String requestWrapperName,
            String requestWrapperNamespace) {

        Assert.notNull(serviceClass, "Java type required");
        Assert.notNull(methodName, "Operation name required");

        // Check if serviceClass is a Web Service. If doesn't exist shows an
        // error.
        if (!isWebServiceClass(serviceClass)) {
            // Export as a service.
            exportService(serviceClass, null, null, null, null);
        }

        String webServiceTargetNamespace = serviceLayerWSExportValidationService
                .getWebServiceDefaultNamespace(serviceClass);

        // Check if method exists in the class.
        Assert.isTrue(isMethodAvailableToExport(serviceClass, methodName,
                GvNIXWebMethod.class.getName()), "The method: '" + methodName
                + " doesn't exists in the class '"
                + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Check authorized JavaTypes in operation.
        serviceLayerWSExportValidationService
                .checkAuthorizedJavaTypesInOperation(serviceClass, methodName);

        // Check if method has return type.
        JavaType returnType = returnJavaType(serviceClass, methodName);

        Assert.isTrue(returnType != null, "The method: '" + methodName
                + " doesn't exists in the class '"
                + serviceClass.getFullyQualifiedTypeName() + "'.");

        boolean isReturnTypeVoid = returnType.equals(JavaType.VOID_OBJECT)
                || returnType.equals(JavaType.VOID_PRIMITIVE);

        if (isReturnTypeVoid) {
            resultName = "void";
        } else if (!StringUtils.hasText(resultName)) {

            resultName = "return";
        }

        // Check if method throws an Exception and update.
        serviceLayerWSExportValidationService.checkMethodExceptions(
                serviceClass, methodName, webServiceTargetNamespace);

        // Checks correct namespace format.
        if (!isReturnTypeVoid) {

            Assert
                    .isTrue(
                            serviceLayerWSExportValidationService
                                    .checkNamespaceFormat(resultNamespace),
                            "The namespace for result has to start with 'http://'.\ni.e.: http://name.of.namespace/");
            Assert
                    .isTrue(
                            serviceLayerWSExportValidationService
                                    .checkNamespaceFormat(responseWrapperNamespace),
                            "The namespace for Response Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        }

        Assert
                .isTrue(
                        serviceLayerWSExportValidationService
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
                .getMethodByNameInClass(serviceClass, methodName);

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
     * 
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
     * <li>
     * webResultType</li>
     * <li>
     * resultName</li>
     * <li>
     * resultNamespace</li>
     * <li>
     * requestWrapperName</li>
     * <li>
     * requestWrapperNamespace</li>
     * <li>
     * requestWrapperClassName</li>
     * <li>
     * responseWrapperName</li>
     * <li>
     * responseWrapperNamespace</li>
     * <li>
     * responseWrapperClassName</li>
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

        // javax.jws.WebMethod
        operationName = StringUtils.hasText(operationName) ? operationName
                : methodName.getSymbolName();

        StringAttributeValue operationNameAttributeValue = new StringAttributeValue(
                new JavaSymbolName("operationName"), operationName);
        annotationAttributeValueList.add(operationNameAttributeValue);

        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInClass(serviceClass, methodName);

        // Check input parameters.
        if (!methodMetadata.getParameterTypes().isEmpty()
                && !methodMetadata.getParameterNames().isEmpty()) {

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

            StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperNamespace"),
                    requestWrapperNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            String className = serviceClass.getPackage()
                    .getFullyQualifiedPackageName().concat(".").concat(
                            StringUtils.capitalize(requestWrapperName).concat(
                                    "RequestWrapper"));
            StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperClassName"), className);
            annotationAttributeValueList.add(classNameAttributeValue);

        }

        // Check result value
        if ((resultName != null && returnType != null)
                && !(returnType.equals(JavaType.VOID_PRIMITIVE) || (returnType
                        .equals(JavaType.VOID_PRIMITIVE)))) {

            StringAttributeValue resultNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultName"), resultName);
            annotationAttributeValueList.add(resultNameAttributeValue);

            resultNamespace = StringUtils.hasText(resultNamespace) ? resultNamespace
                    : webServiceTargetNamespace;

            StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultNamespace"), resultNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            ClassAttributeValue resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"), returnType);
            annotationAttributeValueList.add(resultTypeAttributeValue);

            // javax.xml.ws.ResponseWrapper
            responseWrapperName = StringUtils.hasText(responseWrapperName) ? responseWrapperName
                    : operationName.concat("Response");

            StringAttributeValue responseWrapperNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperName"),
                    responseWrapperName);
            annotationAttributeValueList.add(responseWrapperNameAttributeValue);

            responseWrapperNamespace = StringUtils
                    .hasText(responseWrapperNamespace) ? responseWrapperNamespace
                    : webServiceTargetNamespace;

            targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperNamespace"),
                    responseWrapperNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            String className = serviceClass.getPackage()
                    .getFullyQualifiedPackageName().concat(".").concat(
                            StringUtils.capitalize(responseWrapperName));
            StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperClassName"), className);
            annotationAttributeValueList.add(classNameAttributeValue);

        } else {

            StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultName"), "void");
            annotationAttributeValueList.add(localNameAttributeValue);

            ClassAttributeValue resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"),
                    JavaType.VOID_PRIMITIVE);
            annotationAttributeValueList.add(resultTypeAttributeValue);
        }

        // Create annotation.
        // org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod
        AnnotationMetadata gvNIXWebMethod = new DefaultAnnotationMetadata(
                new JavaType(GvNIXWebMethod.class.getName()),
                annotationAttributeValueList);

        annotationMetadataList.add(gvNIXWebMethod);

        return annotationMetadataList;
    }

    /**
     * Checks if the selected class exists and contains
     * {@link ServiceLayerWSExportMetadata}.
     * 
     * @param serviceClass
     *            class to be checked.
     * @return true if the {@link JavaType} contains
     *         {@link ServiceLayerWSExportMetadata}.
     */
    private boolean isWebServiceClass(JavaType serviceClass) {
        String id = physicalTypeMetadataProvider.findIdentifier(serviceClass);

        Assert.notNull(id, "Cannot locate source for '"
                + serviceClass.getFullyQualifiedTypeName() + "'");

        // Go and get the service layer ws metadata to export selected method.
        JavaType javaType = PhysicalTypeIdentifier.getJavaType(id);
        Path path = PhysicalTypeIdentifier.getPath(id);
        String entityMid = ServiceLayerWSExportMetadata.createIdentifier(
                javaType, path);

        // Get the service layer ws metadata.
        ServiceLayerWSExportMetadata serviceLayerWSExportMetadata = (ServiceLayerWSExportMetadata) metadataService
                .get(entityMid);

        if (serviceLayerWSExportMetadata == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Check if method exists in the class.
     * </p>
     */
    public boolean isMethodAvailableToExport(JavaType serviceClass,
            JavaSymbolName methodName, String annotationName) {

        boolean exists = true;
        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInClass(serviceClass, methodName);

        if (methodMetadata == null) {
            return false;
        }

        exists = javaParserService.isAnnotationIntroducedInMethod(
                GvNIXWebMethod.class.getName(), methodMetadata);
        Assert
                .isTrue(
                        exists == false,
                        "The method '"
                                + methodName
                                + "' has been annotated with @"
                                + annotationName
                                + " before, you could update annotation parameters inside its class.");

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public List<AnnotatedJavaType> getMethodParameterAnnotations(
            JavaType serviceClass, JavaSymbolName methodName,
            String webServiceTargetNamespace) {

        List<AnnotatedJavaType> annotatedWebParameterList = new ArrayList<AnnotatedJavaType>();

        MethodMetadata methodMetadata = javaParserService
                .getMethodByNameInClass(serviceClass, methodName);

        List<AnnotatedJavaType> parameterTypesList = methodMetadata
                .getParameterTypes();

        List<JavaSymbolName> parameterNamesList = methodMetadata
                .getParameterNames();

        if (parameterTypesList.isEmpty() && parameterNamesList.isEmpty()) {
            return annotatedWebParameterList;
        }

        AnnotatedJavaType parameterWithAnnotations;
        List<AnnotationMetadata> parameterAnnotationList;
        
        // @WebParam default values.
        StringAttributeValue partNameAttributeValue = new StringAttributeValue(
                new JavaSymbolName("partName"), "parameters");

        EnumAttributeValue modeAttributeValue = new EnumAttributeValue(
                new JavaSymbolName("mode"), new EnumDetails(new JavaType(
                        "javax.jws.WebParam.Mode"), new JavaSymbolName("IN")));

        BooleanAttributeValue headerAttributeValue = new BooleanAttributeValue(
                new JavaSymbolName("header"), false);

        for (AnnotatedJavaType parameterType : parameterTypesList) {
        
            parameterAnnotationList = new ArrayList<AnnotationMetadata>();

            // @GvNIXWebParam
            List<AnnotationAttributeValue<?>> gvNIXWebParamAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            int index = parameterTypesList.indexOf(parameterType);
            
            JavaSymbolName parameterName = parameterNamesList.get(index);
            
            StringAttributeValue nameWebParamAttributeValue = new StringAttributeValue(new JavaSymbolName("name"), parameterName.getSymbolName());

            gvNIXWebParamAttributeValueList.add(nameWebParamAttributeValue);

            ClassAttributeValue typeClassAttributeValue = new ClassAttributeValue(new JavaSymbolName("type"), parameterType.getJavaType());

            gvNIXWebParamAttributeValueList.add(typeClassAttributeValue);

            AnnotationMetadata gvNixWebParamAnnotationMetadata = new DefaultAnnotationMetadata(
                    new JavaType(GvNIXWebParam.class.getName()),
                    gvNIXWebParamAttributeValueList);

            parameterAnnotationList.add(gvNixWebParamAnnotationMetadata);
            
            // @WebParam

            List<AnnotationAttributeValue<?>> webParamAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            /*
             * @WebParam(name = "fecha", partName = "parameters",
             * targetNamespace =
             * "http://services.web.project.test.gvnix.org/types", mode =
             * Mode.IN, header = false)
             */
            webParamAttributeValueList.add(nameWebParamAttributeValue);

            StringAttributeValue targetNamespace = new StringAttributeValue(
                    new JavaSymbolName("targetNamespace"),
                    webServiceTargetNamespace);

            webParamAttributeValueList.add(targetNamespace);

            webParamAttributeValueList.add(partNameAttributeValue);
            
            webParamAttributeValueList.add(modeAttributeValue);
            
            webParamAttributeValueList.add(headerAttributeValue);

            AnnotationMetadata webParamAnnotationMetadata = new DefaultAnnotationMetadata(
                    new JavaType("javax.jws.WebParam"),
                    webParamAttributeValueList);

            parameterAnnotationList.add(webParamAnnotationMetadata);

            // Add annotation list to parameter.
            parameterWithAnnotations = new AnnotatedJavaType(parameterType
                    .getJavaType(), parameterAnnotationList);
            
            annotatedWebParameterList.add(parameterWithAnnotations);

        }

        return annotatedWebParameterList;
    }
}
