/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.roo.addon.addon.ws.export;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.AnnotationsService;
import org.gvnix.service.roo.addon.addon.JavaParserService;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.roo.addon.annotations.GvNIXWebParam;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.apache.commons.lang3.StringUtils;

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
    private WSExportValidationService wSExportValidationService;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private MemberDetailsScanner memberDetailsScanner;

    /**
     * {@inheritDoc}
     */
    public void exportService(JavaType serviceClass, String serviceName,
            String portTypeName, String targetNamespace, String addressName) {

        // Checks if Cxf is configured in the project and installs it if it's
        // not available.
        wSConfigService.install(WsType.EXPORT);

        // Localizes java file
        String fileLocation = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
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
    }

    /**
     * Creates the list of annotations attribute values to export a web service
     * class.
     * 
     * @param serviceClass to be exported.
     * @param serviceName Name of the service.
     * @param portTypeName Port type name.
     * @param targetNamespace defined.
     * @param addressName to publish the service.
     * @return List of annotation attribute values to update.
     */
    protected List<AnnotationAttributeValue<?>> exportServiceAnnotationAttributes(
            JavaType serviceClass, String serviceName, String portTypeName,
            String targetNamespace, String addressName) {
        // Checks serviceName parameter to publish the web service.
        serviceName = StringUtils.isNotBlank(serviceName) ? serviceName
                : serviceClass.getSimpleTypeName();

        // Checks correct namespace format.
        Validate.isTrue(
                wSExportValidationService.checkNamespaceFormat(targetNamespace),
                "The namespace for Target Namespace has to be defined using URI fromat.\ni.e.: http://name.of.namespace/");

        // Namespace for the web service.
        targetNamespace = StringUtils.isNotBlank(targetNamespace) ? targetNamespace
                : wSConfigService.convertPackageToTargetNamespace(serviceClass
                        .getPackage().toString());

        // Check address name not blank and set service name if not defined.
        addressName = StringUtils.isNotBlank(addressName) ? StringUtils
                .capitalize(addressName) : serviceClass.getSimpleTypeName();

        // Define @GvNIXWebService annotation and attributes.
        // Check port type attribute name format and add attributes to a list.
        List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        portTypeName = StringUtils.isNotBlank(portTypeName) ? portTypeName
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
    public void exportOperation(JavaType javaType, JavaSymbolName methodName,
            String operationName, String resultName, String resultNamespace,
            String responseWrapperName, String responseWrapperNamespace,
            String requestWrapperName, String requestWrapperNamespace) {

        // Java type and method name are required
        Validate.notNull(javaType, "Java type required");
        Validate.notNull(methodName, "Operation name required");

        // Check service class is a Web Service: If not exist exports as service
        if (!isWebServiceClass(javaType)) {
            exportService(javaType, null, null, null, null);
        }

        // Get target namespace from java type
        String targetNamespace = wSExportValidationService
                .getWebServiceDefaultNamespace(javaType);

        // Search method in class and related AJs
        MethodMetadata method = javaParserService.getMethodByNameInAll(
                javaType, methodName);

        // Check if method exists and has no gvNIX web method annotation already
        Validate.isTrue(isMethodAvailableToExport(method),
                "The method not exists or is already exported");

        // Add gvNIX xml element annotation to return and parameters in project
        wSExportValidationService.addGvNixXmlElementToTypes(method);

        // Get method return type (void type if null return type)
        JavaType returnType = returnJavaType(method);

        // Set void or return name
        boolean isVoid = returnType.equals(JavaType.VOID_OBJECT)
                || returnType.equals(JavaType.VOID_PRIMITIVE);
        if (isVoid) {
            resultName = "void";
        }
        else if (!StringUtils.isNotBlank(resultName)) {
            resultName = "return";
        }

        // Add gvNIX web fault annotation to method exceptions in project
        wSExportValidationService.addGvNixWebFaultToExceptions(method,
                targetNamespace);

        // If no void return type: check result and response wrapper namespaces
        if (!isVoid) {

            Validate.isTrue(
                    wSExportValidationService
                            .checkNamespaceFormat(resultNamespace),
                    "The namespace for result has to start with 'http://'.\ni.e.: http://name.of.namespace/");
            Validate.isTrue(
                    wSExportValidationService
                            .checkNamespaceFormat(responseWrapperNamespace),
                    "The namespace for Response Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        }

        // Check request wrapper namespace
        Validate.isTrue(
                wSExportValidationService
                        .checkNamespaceFormat(requestWrapperNamespace),
                "The namespace for Request Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        // Create GvNIXWebMethod annotation for Method
        List<AnnotationMetadata> methodAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadata methodAnnotation = getGvNIXWebMethodAnnotation(
                javaType, method, operationName, resultName, returnType,
                resultNamespace, responseWrapperName, responseWrapperNamespace,
                requestWrapperName, requestWrapperNamespace, targetNamespace);
        methodAnnotations.add(methodAnnotation);

        // Add @GvNIXWebParam & @WebParam parameter annotations
        List<AnnotatedJavaType> parametersAnnotations = getMethodParameterAnnotations(
                method, targetNamespace);

        // Add method and parameter annotations to method
        javaParserService.updateMethodAnnotations(javaType, methodName,
                methodAnnotations, parametersAnnotations);
    }

    /**
     * Returns method return java type.
     * <p>
     * If null return type, get void return type.
     * </p>
     * 
     * @param method to get return type
     * @return Method return java type
     */
    protected JavaType returnJavaType(MethodMetadata method) {

        // If no method, return null
        if (method == null) {
            return null;
        }

        // If return type not null, get it
        if (method.getReturnType() != null) {
            return method.getReturnType();
        }

        // If null return type, get void return type
        return new JavaType(JavaType.VOID_OBJECT.toString());
    }

    /**
     * Creates a gvNIX web method annotation with the values defined.
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * Attributes created into gvNIX web service annotation are used to
     * generate:
     * </p>
     * <ul>
     * <li>javax.xml.ws.WebMethod: operationName</li>
     * <li>javax.xml.ws.RequestWrapper: requestWrapperName,
     * requestWrapperNamespace, requestWrapperClassName</li>
     * <li>javax.xml.ws.WebResult: resultName, resultNamespace, webResultType</li>
     * <li>javax.xml.ws.ResponseWrapper: responseWrapperName,
     * responseWrapperNamespace, responseWrapperClassName</li>
     * <ul>
     * 
     * @param javaType Java type to export a method.
     * @param method Method to export.
     * @param operationName Name of the method to be showed as a Web Service
     *        operation.
     * @param resultName Method result name.
     * @param returnType JavaType class to return.
     * @param resultNamespace Result type Namespace.
     * @param responseWrapperName Name to define the Response Wrapper Object.
     * @param responseWrapperNamespace Response Wrapper Object Namespace.
     * @param requestWrapperName Name to define the Request Wrapper Object.
     * @param requestWrapperNamespace Request Wrapper Object Namespace.
     * @param targetNamespace Web Service Namespace.
     * @return gvNIX web method annotation for method.
     */
    protected AnnotationMetadata getGvNIXWebMethodAnnotation(JavaType javaType,
            MethodMetadata method, String operationName, String resultName,
            JavaType returnType, String resultNamespace,
            String responseWrapperName, String responseWrapperNamespace,
            String requestWrapperName, String requestWrapperNamespace,
            String targetNamespace) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // gvNIX attribute for javax.xml.ws.WebMethod attribute
        operationName = StringUtils.isNotBlank(operationName) ? operationName
                : method.getMethodName().getSymbolName();
        attrs.add(new StringAttributeValue(new JavaSymbolName("operationName"),
                operationName));

        // Creates gvNIX web method request attributes
        attrs.addAll(getRequestAnnotationAttributes(javaType, method,
                operationName, requestWrapperName, requestWrapperNamespace,
                targetNamespace));

        // Creates gvNIX web method response attributes
        attrs.addAll(getResponseAnnotationAttributes(javaType, operationName,
                resultName, returnType, resultNamespace, responseWrapperName,
                responseWrapperNamespace, targetNamespace));

        // Create gvNIX web method annotation
        return new AnnotationMetadataBuilder(new JavaType(
                GvNIXWebMethod.class.getName()), attrs).build();
    }

    /**
     * Creates gvNIX web method request attributes with the values defined.
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * Attributes created into gvNIX web service annotation are used to
     * generate:
     * </p>
     * <ul>
     * <li>javax.xml.ws.RequestWrapper: requestWrapperName,
     * requestWrapperNamespace, requestWrapperClassName</li>
     * <ul>
     * <p>
     * If parameters types or names are empty, empty list will return.
     * </p>
     * 
     * @param javaType Java type to export a method.
     * @param method Method to export.
     * @param operationName Name of the method to be showed as a Web Service
     *        operation.
     * @param requestWrapperName Name to define the Request Wrapper Object.
     * @param requestWrapperNamespace Request Wrapper Object Namespace.
     * @param targetNamespace Web Service Namespace.
     * @return gvNIX web method request attributes.
     */
    protected List<AnnotationAttributeValue<?>> getRequestAnnotationAttributes(
            JavaType javaType, MethodMetadata method, String operationName,
            String requestWrapperName, String requestWrapperNamespace,
            String targetNamespace) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // Check input parameters.
        if (!method.getParameterTypes().isEmpty()
                && !method.getParameterNames().isEmpty()) {

            // There are input parameters

            // gvNIX attributes for javax.xml.ws.RequestWrapper attributes

            requestWrapperName = StringUtils.isNotBlank(requestWrapperName) ? requestWrapperName
                    : operationName;
            attrs.add(new StringAttributeValue(new JavaSymbolName(
                    "requestWrapperName"), requestWrapperName));

            // RequestWrapper namespace
            requestWrapperNamespace = StringUtils
                    .isNotBlank(requestWrapperNamespace) ? requestWrapperNamespace
                    : targetNamespace;
            attrs.add(new StringAttributeValue(new JavaSymbolName(
                    "requestWrapperNamespace"), requestWrapperNamespace));

            // Wrapper class
            String className = javaType
                    .getPackage()
                    .getFullyQualifiedPackageName()
                    .concat(".")
                    .concat(StringUtils.capitalize(requestWrapperName).concat(
                            "RequestWrapper"));
            attrs.add(new StringAttributeValue(new JavaSymbolName(
                    "requestWrapperClassName"), className));
        }

        return attrs;
    }

    /**
     * Creates gvNIX web method response attributes with the values defined.
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * Attributes created for gvNIX web service annotation are used to generate:
     * </p>
     * <ul>
     * <li>javax.xml.ws.WebResult: resultName, resultNamespace, webResultType</li>
     * <li>javax.xml.ws.ResponseWrapper: responseWrapperName,
     * responseWrapperNamespace, responseWrapperClassName</li>
     * <ul>
     * 
     * @param javaType Java type to export a method.
     * @param operationName Name of the method to be showed as a Web Service
     *        operation.
     * @param resultName Method result name.
     * @param returnType JavaType class to return.
     * @param resultNamespace Result type Namespace.
     * @param responseWrapperName Name to define the Response Wrapper Object.
     * @param responseWrapperNamespace Response Wrapper Object Namespace.
     * @param targetNamespace Web Service Namespace.
     * @return gvNIX web method response attributes.
     */
    protected List<AnnotationAttributeValue<?>> getResponseAnnotationAttributes(
            JavaType javaType, String operationName, String resultName,
            JavaType returnType, String resultNamespace,
            String responseWrapperName, String responseWrapperNamespace,
            String targetNamespace) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // Check void return type
        if ((resultName != null && returnType != null)
                && !(returnType.equals(JavaType.VOID_PRIMITIVE) || (returnType
                        .equals(JavaType.VOID_PRIMITIVE)))) {

            // No void method
            attrs.addAll(getResponseNoVoidAnnotationAttributes(javaType,
                    operationName, resultName, returnType, resultNamespace,
                    responseWrapperName, responseWrapperNamespace,
                    targetNamespace));
        }
        else {

            // Void method
            attrs.addAll(getResponseVoidAnnotationAttributes());
        }

        return attrs;
    }

    /**
     * Creates gvNIX web method response attributes with the values defined.
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * Attributes created for gvNIX web service annotation are used to generate:
     * </p>
     * <ul>
     * <li>javax.xml.ws.WebResult: resultName, resultNamespace, webResultType</li>
     * <li>javax.xml.ws.ResponseWrapper: responseWrapperName,
     * responseWrapperNamespace, responseWrapperClassName</li>
     * <ul>
     * 
     * @param javaType Java type to export a method.
     * @param operationName Name of the method to be showed as a Web Service
     *        operation.
     * @param resultName Method result name.
     * @param returnType JavaType class to return.
     * @param resultNamespace Result type Namespace.
     * @param responseWrapperName Name to define the Response Wrapper Object.
     * @param responseWrapperNamespace Response Wrapper Object Namespace.
     * @param targetNamespace Web Service Namespace.
     * @return gvNIX web method response attributes.
     */
    protected List<AnnotationAttributeValue<?>> getResponseNoVoidAnnotationAttributes(
            JavaType javaType, String operationName, String resultName,
            JavaType returnType, String resultNamespace,
            String responseWrapperName, String responseWrapperNamespace,
            String targetNamespace) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // gvNIX attributes for javax.xml.ws.WebResult attributes

        // Result name
        attrs.add(new StringAttributeValue(new JavaSymbolName("resultName"),
                resultName));

        // Result namespace
        resultNamespace = StringUtils.isNotBlank(resultNamespace) ? resultNamespace
                : targetNamespace;
        attrs.add(new StringAttributeValue(
                new JavaSymbolName("resultNamespace"), resultNamespace));

        // Web result type
        attrs.add(new ClassAttributeValue(new JavaSymbolName("webResultType"),
                returnType));

        // gvNIX attributes for javax.xml.ws.ResponseWrapper attributes

        // Response wrapper name
        responseWrapperName = StringUtils.isNotBlank(responseWrapperName) ? responseWrapperName
                : operationName.concat("Response");
        StringAttributeValue responseWrapperNameAttr = new StringAttributeValue(
                new JavaSymbolName("responseWrapperName"), responseWrapperName);
        attrs.add(responseWrapperNameAttr);

        // Response wrapper namespace
        responseWrapperNamespace = StringUtils
                .isNotBlank(responseWrapperNamespace) ? responseWrapperNamespace
                : targetNamespace;
        attrs.add(new StringAttributeValue(new JavaSymbolName(
                "responseWrapperNamespace"), responseWrapperNamespace));

        // Response wrapper class name
        String className = javaType.getPackage().getFullyQualifiedPackageName()
                .concat(".")
                .concat(StringUtils.capitalize(responseWrapperName));
        attrs.add(new StringAttributeValue(new JavaSymbolName(
                "responseWrapperClassName"), className));

        return attrs;
    }

    /**
     * Creates gvNIX web method response attributes for void methods.
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * Attributes created for gvNIX web service annotation are used to generate:
     * </p>
     * <ul>
     * <li>javax.xml.ws.WebResult: resultName, resultNamespace, webResultType</li>
     * <ul>
     * 
     * @return gvNIX web method response attributes for void methods.
     */
    protected List<AnnotationAttributeValue<?>> getResponseVoidAnnotationAttributes() {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // gvNIX attributes for javax.xml.ws.WebResult attributes

        // Result name
        attrs.add(new StringAttributeValue(new JavaSymbolName("resultName"),
                "void"));

        // Web result type
        attrs.add(new ClassAttributeValue(new JavaSymbolName("webResultType"),
                JavaType.VOID_PRIMITIVE));

        return attrs;
    }

    /**
     * Checks if the selected class exists and contains {@link WSExportMetadata}
     * .
     * 
     * @param serviceClass class to be checked.
     * @return true if the {@link JavaType} contains {@link WSExportMetadata}.
     */
    private boolean isWebServiceClass(JavaType serviceClass) {

        // Gets PhysicalTypeIdentifier for serviceClass
        String id = typeLocationService.getPhysicalTypeIdentifier(serviceClass);

        Validate.notNull(
                id,
                "Cannot locate source for '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Prepares WSExportMetadata identifier.
        PhysicalTypeIdentifier.getJavaType(id);
        LogicalPath path = PhysicalTypeIdentifier.getPath(id);
        String entityMid = WSExportMetadata
                .createIdentifier(serviceClass, path);

        // Get the service layer ws metadata.
        WSExportMetadata wSExportMetadata = (WSExportMetadata) metadataService
                .get(entityMid);

        if (wSExportMetadata == null) {
            // it isn't an exported service
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Check if method exists and has no gvNIX web method annotation already.
     * 
     * @param method To check
     * @return true if method exists and annotation is not defined
     */
    protected boolean isMethodAvailableToExport(MethodMetadata method) {

        // Method not available, because not exists
        if (method == null) {
            return false;
        }

        // Checks if it already has @GvNIXWebMethod
        return !javaParserService.isAnnotationIntroducedInMethod(
                GvNIXWebMethod.class.getName(), method);
    }

    /**
     * Create annotations for each method parameter, if not empty.
     * <p>
     * Each parameter with not empty type and name will be related a
     * GvNIXWebParam annotation and a WebParam annotation.
     * </p>
     * 
     * @param method Method to update with annotations
     * @param targetNamespace Web Service Namespace
     * @return Annotation
     */
    protected List<AnnotatedJavaType> getMethodParameterAnnotations(
            MethodMetadata method, String targetNamespace) {

        // List to store annotations for parameters
        List<AnnotatedJavaType> annotations = new ArrayList<AnnotatedJavaType>();

        // Get method parameter types and names and return null if empty
        List<AnnotatedJavaType> paramsType = method.getParameterTypes();
        List<JavaSymbolName> paramsName = method.getParameterNames();
        if (paramsType.isEmpty() && paramsName.isEmpty()) {
            return annotations;
        }

        // For each parameters
        for (AnnotatedJavaType paramType : paramsType) {

            // annotation of this parameter
            List<AnnotationMetadata> paramsAnnotations = new ArrayList<AnnotationMetadata>();

            // Get current parameter name
            int index = paramsType.indexOf(paramType);
            JavaSymbolName paramName = paramsName.get(index);

            // Add @GvNIXWebParam to annotation list
            paramsAnnotations.add(getGvNIXWebParamAnnotation(paramType,
                    paramName));

            // Add @WebParam to annotation list
            paramsAnnotations.add(getWebParamAnnotation(targetNamespace,
                    paramType, paramName));

            // Add annotation list to parameter
            annotations.add(new AnnotatedJavaType(paramType.getJavaType(),
                    paramsAnnotations));
        }

        return annotations;
    }

    /**
     * Create gvNIX web param annotation with some java type and name.
     * 
     * @param javaType Java type
     * @param javaName Java name
     * @return gvNIX web param annotation
     */
    protected AnnotationMetadata getGvNIXWebParamAnnotation(
            AnnotatedJavaType javaType, JavaSymbolName javaName) {

        // Attributes for @GvNIXWebParam annotation
        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // @GvNIXWebParam.name
        attrs.add(new StringAttributeValue(new JavaSymbolName("name"), javaName
                .getSymbolName()));

        // @GvNIXWebParam.type
        attrs.add(new ClassAttributeValue(new JavaSymbolName("type"), javaType
                .getJavaType()));

        // Build @GvNIXWebParam annotation
        return new AnnotationMetadataBuilder(new JavaType(
                GvNIXWebParam.class.getName()), attrs).build();
    }

    /**
     * Create web param annotation with some values.
     * <p>
     * Filled atributes are:
     * </p>
     * <ul>
     * <li>name</li>
     * <li>targetNamespace: Only if not a primitive, nor common collection, nor
     * java.lang package</li>
     * <li>partName: Always 'parameters'</li>
     * <li>mode: Mode.IN</li>
     * <li>header: Always false</li>
     * </ul>
     * 
     * @param targetNamespace Target namespace
     * @param javaType Java type
     * @param javaName Java name
     * @return Web para annotation
     */
    protected AnnotationMetadata getWebParamAnnotation(String targetNamespace,
            AnnotatedJavaType javaType, JavaSymbolName javaName) {

        // values for @WebParam annotation
        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // @WebParam.name
        attrs.add(new StringAttributeValue(new JavaSymbolName("name"), javaName
                .getSymbolName()));

        // Not a primitive, nor common collection, nor java.lang package
        if (!javaType.getJavaType().isPrimitive()
                && !javaType.getJavaType().isCommonCollectionType()
                && !javaType.getJavaType().getFullyQualifiedTypeName()
                        .startsWith("java.lang")) {

            // @WebParam.targetnamespace
            attrs.add(new StringAttributeValue(new JavaSymbolName(
                    "targetNamespace"), targetNamespace));
        }

        // @WebParam.partName <-- parameters (default)
        attrs.add(new StringAttributeValue(new JavaSymbolName("partName"),
                "parameters"));

        // @WebParam.mode <-- IN (default)
        attrs.add(new EnumAttributeValue(new JavaSymbolName("mode"),
                new EnumDetails(new JavaType("javax.jws.WebParam.Mode"),
                        new JavaSymbolName("IN"))));

        // @WebParam.header <-- false (default)
        attrs.add(new BooleanAttributeValue(new JavaSymbolName("header"), false));

        return new AnnotationMetadataBuilder(
                new JavaType("javax.jws.WebParam"), attrs).build();
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

        String id = typeLocationService.getPhysicalTypeIdentifier(serviceClass);

        Validate.notNull(
                id,
                "Cannot locate source for '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Get service class details
        ClassOrInterfaceTypeDetails tmpServiceDetails = typeLocationService
                .getTypeDetails(serviceClass);
        MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(
                serviceClass.getFullyQualifiedTypeName(), tmpServiceDetails);

        // Checks if it's mutable
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class,
                tmpServiceDetails,
                "Can't modify " + tmpServiceDetails.getName());

        List<? extends MethodMetadata> methodList = memberDetails.getMethods();

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
                if (!StringUtils.isNotBlank(methodListStringBuilder.toString())) {
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
        if (!StringUtils.isNotBlank(methodListStringBuilder.toString())) {
            methodListStringBuilder
                    .append("Class '"
                            + serviceClass.getFullyQualifiedTypeName()
                            + "' hasn't got any available method to export as web service operations.");
        }

        return methodListStringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     **/
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
