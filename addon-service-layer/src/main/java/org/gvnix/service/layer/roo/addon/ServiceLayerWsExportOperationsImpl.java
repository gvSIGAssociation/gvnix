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

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.*;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;

import com.sun.org.apache.xerces.internal.impl.XMLEntityManager.Entity;

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
        annotationsService.addJavaTypeAnnotation(serviceClass,
                GvNIXWebService.class.getName(), gvNixAnnotationAttributes,
                false);

        // Installs jax2ws plugin in project.
        serviceLayerWsConfigService.installJaxwsBuildPlugin();

        // Add GvNixAnnotations to the project.
        annotationsService.addGvNIXAnnotationsDependency();
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

        if (returnType.equals(JavaType.VOID_OBJECT)
                || returnType.equals(JavaType.VOID_PRIMITIVE)) {
            resultName = "void";
        } else if (!StringUtils.hasText(resultName)) {

            resultName = "return";
        }

        // Check if method throws an Exception.
        serviceLayerWSExportValidationService.checkMethodExceptions(
                serviceClass, methodName);

        // Checks correct namespace format.
        Assert
                .isTrue(
                        serviceLayerWSExportValidationService
                                .checkNamespaceFormat(resultNamespace),
                        "The namespace for result has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        Assert
                .isTrue(
                        serviceLayerWSExportValidationService
                                .checkNamespaceFormat(requestWrapperNamespace),
                        "The namespace for Request Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        Assert
                .isTrue(
                        serviceLayerWSExportValidationService
                                .checkNamespaceFormat(responseWrapperNamespace),
                        "The namespace for Response Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        // Create annotations to selected Method
        List<AnnotationMetadata> annotationMetadataUpdateList = getAnnotationsToExportOperation(
        serviceClass, methodName, operationName, resultName, returnType,
                resultNamespace, responseWrapperName, responseWrapperNamespace,
                requestWrapperName, requestWrapperNamespace);

        javaParserService.updateMethodAnnotations(serviceClass, methodName,
                annotationMetadataUpdateList);

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
            String requestWrapperNamespace) {

        List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();
        List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        // javax.jws.WebMethod
        operationName = StringUtils.hasText(operationName) ? operationName
                : methodName.getSymbolName();

        StringAttributeValue operationNameAttributeValue = new StringAttributeValue(
                new JavaSymbolName("operationName"), operationName);
        annotationAttributeValueList.add(operationNameAttributeValue);

        // javax.xml.ws.RequestWrapper
        requestWrapperName = StringUtils.hasText(requestWrapperName) ? requestWrapperName
                : operationName;
        StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                new JavaSymbolName("requestWrapperName"), requestWrapperName);
        annotationAttributeValueList.add(localNameAttributeValue);

        requestWrapperNamespace = StringUtils.hasText(requestWrapperNamespace) ? requestWrapperNamespace
                : serviceLayerWsConfigService
                        .convertPackageToTargetNamespace(serviceClass
                                .getPackage().getFullyQualifiedPackageName());

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


        // javax.xml.ws.ResponseWrapper
        responseWrapperName = StringUtils.hasText(responseWrapperName) ? responseWrapperName
                : operationName.concat("Response");

        localNameAttributeValue = new StringAttributeValue(new JavaSymbolName(
                "responseWrapperName"), responseWrapperName);
        annotationAttributeValueList.add(localNameAttributeValue);

        responseWrapperNamespace = StringUtils
                .hasText(responseWrapperNamespace) ? responseWrapperNamespace
                : serviceLayerWsConfigService
                        .convertPackageToTargetNamespace(serviceClass
                                .getPackage().getFullyQualifiedPackageName());

        targetNamespaceAttributeValue = new StringAttributeValue(
                new JavaSymbolName("responseWrapperNamespace"),
                responseWrapperNamespace);
        annotationAttributeValueList.add(targetNamespaceAttributeValue);

        className = serviceClass.getPackage().getFullyQualifiedPackageName()
                .concat(".")
                .concat(StringUtils.capitalize(responseWrapperName));
        classNameAttributeValue = new StringAttributeValue(new JavaSymbolName(
                "responseWrapperClassName"), className);
        annotationAttributeValueList.add(classNameAttributeValue);

        // Check result value
        if ((resultName != null && returnType != null)
                && !(returnType.equals(JavaType.VOID_PRIMITIVE) || (returnType
                        .equals(JavaType.VOID_PRIMITIVE)))) {

            localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultName"), resultName);
            annotationAttributeValueList.add(localNameAttributeValue);

            resultNamespace = StringUtils.hasText(resultNamespace) ? resultNamespace
                    : serviceLayerWsConfigService
                            .convertPackageToTargetNamespace(serviceClass
                                    .getPackage()
                                    .getFullyQualifiedPackageName());

            targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultNamespace"), resultNamespace);
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            ClassAttributeValue resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"), returnType);
            annotationAttributeValueList.add(resultTypeAttributeValue);

        } else {

            localNameAttributeValue = new StringAttributeValue(
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

}
