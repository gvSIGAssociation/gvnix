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

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.*;
import org.springframework.roo.metadata.*;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * <p>
 * Checks if @GvNIXWebServices annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private ServiceLayerWSExportValidationService serviceLayerWSExportValidationService;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private AnnotationsService annotationsService;

    private static Logger logger = Logger
            .getLogger(ServiceLayerWSExportMetadataProvider.class.getName());

    protected void activate(ComponentContext context) {
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebService.class.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * createLocalIdentifier(org.springframework.roo.model.JavaType,
     * org.springframework.roo.project.Path)
     */
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return ServiceLayerWSExportMetadata.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = ServiceLayerWSExportMetadata
                .getJavaType(metadataIdentificationString);
        Path path = ServiceLayerWSExportMetadata
                .getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier
                .createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata
     * (java.lang.String, org.springframework.roo.model.JavaType,
     * org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        ServiceLayerWSExportMetadata serviceLayerMetadata = null;

        if (serviceLayerWsConfigService.isProjectWebAvailable()) {

            // Install configuration to export services if it's not installed.
            serviceLayerWsConfigService.install(CommunicationSense.EXPORT);
            // Installs jax2ws plugin in project.
            serviceLayerWsConfigService.installJaxwsBuildPlugin();
            // Add GvNixAnnotations to the project.
            annotationsService.addGvNIXAnnotationsDependency();

            // Check if Web Service definition is correct.
            PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                    .getPhysicalTypeDetails();

            ClassOrInterfaceTypeDetails governorTypeDetails;
            if (physicalTypeDetails == null
                    || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
                // There is a problem
                return null;
            } else {
                // We have reliable physical type details
                governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
            }

            // Get upstreamDepency Class to check.
            AnnotationMetadata gvNIXWebServiceAnnotation = MemberFindingUtils
                    .getTypeAnnotation(governorTypeDetails, new JavaType(
                            GvNIXWebService.class.getName()));

            // Check @GvNIXWebService annotation attributes.
            checkGvNIXWebServiceAnnotationAttributes(gvNIXWebServiceAnnotation,
                    governorTypeDetails);

            // Default Web Service target Namespace.
            StringAttributeValue webServiceTargetNamespaceAttributeValue = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("targetNamespace"));
            String webServiceTargetNamespace = webServiceTargetNamespaceAttributeValue
                    .getValue();

            // Show info
            logger.log(Level.FINE,
                    "Check correct format to export the web service class: '"
                            + governorTypeDetails.getName() + "'");

            // Update CXF XML
            boolean updateGvNIXWebServiceAnnotation = serviceLayerWsConfigService
                    .exportClass(governorTypeDetails.getName(),
                            gvNIXWebServiceAnnotation);

            // Define Jax-WS plugin and creates and execution build for this
            // service
            // to generate the wsdl file to check errors before deploy.
            StringAttributeValue serviceName = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("serviceName"));

            StringAttributeValue address = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("address"));

            StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

            serviceLayerWsConfigService.jaxwsBuildPlugin(governorTypeDetails
                    .getName(), serviceName.getValue(), address.getValue(),
                    fullyQualifiedTypeName.getValue());

            // Like BeanInfoMetdadaProvider to get all related metadata.
            // TODO: Update in ROO-1.1.0 with new defined service.

            // Create a list of metadata which the metadata should look for
            // accessors within
            List<MemberHoldingTypeDetails> memberHoldingTypeDetails = getMemberHoldingDetails(
                    governorTypeDetails, governorPhysicalTypeMetadata,
                    metadataIdentificationString);

            // Get public methods to check.
            List<MethodMetadata> methodMetadataList = getPublicAccessors(memberHoldingTypeDetails);

            BooleanAttributeValue exported = (BooleanAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("exported"));

            // Check method signature and annotations.
            for (MethodMetadata methodMetadata : methodMetadataList) {

                AnnotationMetadata gvNixWebMethodAnnotation = MemberFindingUtils
                        .getAnnotationOfType(methodMetadata.getAnnotations(),
                                new JavaType(GvNIXWebMethod.class.getName()));

                if (gvNixWebMethodAnnotation != null) {

                    // If the web service has been exported from WSDL, the
                    // parameters hasn't to be checked
                    if (!exported.getValue()) {

                        // Check INPUT/OUTPUT parameters
                        serviceLayerWSExportValidationService
                                .checkAuthorizedJavaTypesInOperation(
                                        governorTypeDetails.getName(),
                                        methodMetadata.getMethodName());

                        // Check and update exceptions.
                        serviceLayerWSExportValidationService
                                .checkMethodExceptions(governorTypeDetails
                                        .getName(), methodMetadata
                                        .getMethodName(),
                                        webServiceTargetNamespace);

                        // Check if attributes are defined in method.
                        Assert
                                .isTrue(
                                        !gvNixWebMethodAnnotation
                                                .getAttributeNames().isEmpty(),
                                        "The annotation @GvNIXWebMethod for '"
                                                + methodMetadata
                                                        .getMethodName()
                                                + "' method in class '"
                                                + governorTypeDetails
                                                        .getName()
                                                        .getFullyQualifiedTypeName()
                                                + "' must have all its attributes defined.");

                        // Check if @GvNIXWebMethod attributes are correct to
                        // export method to web service annotation in ITD.
                        checkGvNIXWebMethodAnnotationAttributes(
                                gvNixWebMethodAnnotation, governorTypeDetails,
                                methodMetadata);

                        // Checks @WebParam and @GvNIXWebParam attributes for
                        // each
                        // input parameter in method.
                        checkGvNIXWebParamsAnnotationAttributes(
                                governorTypeDetails, methodMetadata);
                    }

                }
            }

            // Update Annotation because Java Class or package has changed.
            if (updateGvNIXWebServiceAnnotation) {

                List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName("name")));
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName(
                                        "targetNamespace")));
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName("serviceName")));
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName("address")));
                gvNixAnnotationAttributes.add(new StringAttributeValue(
                        new JavaSymbolName("fullyQualifiedTypeName"),
                        governorTypeDetails.getName()
                                .getFullyQualifiedTypeName()));
                gvNixAnnotationAttributes
                .add((StringAttributeValue) gvNIXWebServiceAnnotation
                        .getAttribute(new JavaSymbolName("exported")));

                annotationsService.addJavaTypeAnnotation(governorTypeDetails
                        .getName(), GvNIXWebService.class.getName(),
                        gvNixAnnotationAttributes, true);
            }

            serviceLayerMetadata = new ServiceLayerWSExportMetadata(
                    metadataIdentificationString, aspectName,
                    governorPhysicalTypeMetadata, methodMetadataList);

        }

        return serviceLayerMetadata;
    }

    /**
     * Check correct values in @GvNIXWebService annotation.
     * 
     * @param gvNIXWebServiceAnnotation
     *            to check.
     * @param governorTypeDetails
     *            class where is defined @GvNIXWebService.
     */
    public void checkGvNIXWebServiceAnnotationAttributes(
            AnnotationMetadata gvNIXWebServiceAnnotation,
            ClassOrInterfaceTypeDetails governorTypeDetails) {

        // name
        StringAttributeValue name = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("name"));

        Assert
                .isTrue(
                        name != null && StringUtils.hasText(name.getValue()),
                        "Attribute 'name' in annotation @GvNIXWebService defined in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export class as Web Service.");

        // targetNamespace
        StringAttributeValue targetNamespace = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("targetNamespace"));

        Assert
                .isTrue(
                        targetNamespace != null
                                && StringUtils.hasText(targetNamespace
                                        .getValue()),
                        "Attribute 'targetNamespace' in annotation @GvNIXWebService defined in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export class as Web Service.");

        Assert
                .isTrue(
                        serviceLayerWSExportValidationService
                                .checkNamespaceFormat(targetNamespace
                                        .getValue()),
                        "The namespace for Web Service has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        // serviceName
        StringAttributeValue serviceName = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("serviceName"));

        Assert
                .isTrue(
                        serviceName != null
                                && StringUtils.hasText(serviceName.getValue()),
                        "Attribute 'serviceName' in annotation @GvNIXWebService defined in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export class as Web Service.");

        // address
        StringAttributeValue address = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("address"));

        Assert
                .isTrue(
                        address != null
                                && StringUtils.hasText(address.getValue()),
                        "Attribute 'address' in annotation @GvNIXWebService defined in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export class as Web Service.");

        // fullyQualifiedTypeName
        StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

        Assert
                .isTrue(
                        fullyQualifiedTypeName != null
                                && StringUtils.hasText(fullyQualifiedTypeName
                                        .getValue()),
                        "Attribute 'fullyQualifiedTypeName' in annotation @GvNIXWebService defined in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export class as Web Service.");

    }

    /**
     * Check correct values in @GvNIXWebMethod annotation.
     * 
     * <p>
     * Annotation attributes to check:
     * </p>
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
     * </ul>
     * 
     * @param gvNixWebMethodAnnotation
     *            to check.
     * @param governorTypeDetails
     *            class where is defined @GvNIXWebMethod.
     */
    public void checkGvNIXWebMethodAnnotationAttributes(
            AnnotationMetadata gvNixWebMethodAnnotation,
            ClassOrInterfaceTypeDetails governorTypeDetails,
            MethodMetadata methodMetadata) {

        // operationName
        StringAttributeValue operationName = (StringAttributeValue) gvNixWebMethodAnnotation
                .getAttribute(new JavaSymbolName("operationName"));

        Assert
                .isTrue(
                        operationName != null
                                && StringUtils
                                        .hasText(operationName.getValue()),
                        "Attribute 'operationName' in annotation @GvNIXWebMethod defined in method '"
                                + methodMetadata.getMethodName()
                                + "' in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export as Web Service operation.");

        // webResultType
        ClassAttributeValue webResultType = (ClassAttributeValue) gvNixWebMethodAnnotation
                .getAttribute(new JavaSymbolName("webResultType"));

        Assert
                .isTrue(
                        webResultType != null,
                        "Attribute 'webResultType' in annotation @GvNIXWebMethod has to be defined in method '"
                                + methodMetadata.getMethodName()
                                + "' in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' even if it's 'void' Java Type to export as Web Service operation.");

        // Check if webResultType has the same value than method returnType.
        Assert
                .isTrue(
                        methodMetadata.getReturnType()
                                .getFullyQualifiedTypeName().contentEquals(
                                        webResultType.getValue()
                                                .getFullyQualifiedTypeName()),
                        "Attribute 'webResultType' in annotation @GvNIXWebMethod defined in method '"
                                + methodMetadata.getMethodName()
                                + "' in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' is from a different Java Type than method return type.\nMust have the same value to export as Web Service operation.");

        // resultName
        StringAttributeValue resultName = (StringAttributeValue) gvNixWebMethodAnnotation
                .getAttribute(new JavaSymbolName("resultName"));

        // Check only if method has return type different than Void.
        if (!webResultType
                .getValue()
                .getFullyQualifiedTypeName()
                .contentEquals(JavaType.VOID_OBJECT.getFullyQualifiedTypeName())
                && !webResultType.getValue().getFullyQualifiedTypeName()
                        .contentEquals(
                                JavaType.VOID_PRIMITIVE
                                        .getFullyQualifiedTypeName())) {

            Assert
                    .isTrue(
                            resultName != null
                                    && StringUtils.hasText(resultName
                                            .getValue()),
                            "Attribute 'resultName' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

            // resultNamespace
            StringAttributeValue resultNamespace = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("resultNamespace"));

            Assert
                    .isTrue(
                            resultNamespace != null
                                    && StringUtils.hasText(resultNamespace
                                            .getValue()),
                            "Attribute 'resultNamespace' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

            Assert
                    .isTrue(
                            serviceLayerWSExportValidationService
                                    .checkNamespaceFormat(resultNamespace
                                            .getValue()),
                            "Attribute 'resultNamespace' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to start with 'http://'.\ni.e.: http://name.of.namespace/");

            // responseWrapperName
            StringAttributeValue responseWrapperName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperName"));

            Assert
                    .isTrue(
                            responseWrapperName != null
                                    && StringUtils.hasText(responseWrapperName
                                            .getValue()),
                            "Attribute 'responseWrapperName' in annotation @GvNIXWebService defined in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

            // responseWrapperNamespace
            StringAttributeValue responseWrapperNamespace = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperNamespace"));

            Assert
                    .isTrue(
                            responseWrapperNamespace != null
                                    && StringUtils
                                            .hasText(responseWrapperNamespace
                                                    .getValue()),
                            "Attribute 'responseWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

            Assert
                    .isTrue(
                            serviceLayerWSExportValidationService
                                    .checkNamespaceFormat(responseWrapperNamespace
                                            .getValue()),
                            "Attribute 'responseWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to start with 'http://'.\ni.e.: http://name.of.namespace/");
            // responseWrapperClassName
            StringAttributeValue responseWrapperClassName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperClassName"));

            Assert
                    .isTrue(
                            responseWrapperClassName != null
                                    && StringUtils
                                            .hasText(responseWrapperClassName
                                                    .getValue()),
                            "Attribute 'responseWrapperClassName' in annotation @GvNIXWebService defined in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

        }

        // Check if method has input parameters.
        if (!methodMetadata.getParameterTypes().isEmpty()
                && !methodMetadata.getParameterNames().isEmpty()) {

            // requestWrapperName
            StringAttributeValue requestWrapperName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperName"));

            Assert
                    .isTrue(
                            requestWrapperName != null
                                    && StringUtils.hasText(requestWrapperName
                                            .getValue()),
                            "Attribute 'requestWrapperName' in annotation @GvNIXWebService defined in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

            // requestWrapperNamespace
            StringAttributeValue requestWrapperNamespace = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperNamespace"));

            Assert
                    .isTrue(
                            requestWrapperNamespace != null
                                    && StringUtils
                                            .hasText(requestWrapperNamespace
                                                    .getValue()),
                            "Attribute 'requestWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");

            Assert
                    .isTrue(
                            serviceLayerWSExportValidationService
                                    .checkNamespaceFormat(requestWrapperNamespace
                                            .getValue()),
                            "Attribute 'requestWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                                    + methodMetadata.getMethodName()
                                    + "' in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to start with 'http://'.\ni.e.: http://name.of.namespace/");

            // requestWrapperClassName
            StringAttributeValue requestWrapperClassName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperClassName"));

            Assert
                    .isTrue(
                            requestWrapperClassName != null
                                    && StringUtils
                                            .hasText(requestWrapperClassName
                                                    .getValue()),
                            "Attribute 'requestWrapperClassName' in annotation @GvNIXWebService defined in class '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' has to be defined to export as Web Service operation.");
        }

    }

    /**
     * Checks @GvNIXWebParam annotation attribute values in input method
     * parameters to avoid changes in service contract.
     * 
     * @param governorTypeDetails
     *            Service Class.
     * @param methodMetadata
     *            Method to check parameters.
     */
    public void checkGvNIXWebParamsAnnotationAttributes(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            MethodMetadata methodMetadata) {

        List<AnnotatedJavaType> annotatedInputParameters = methodMetadata
                .getParameterTypes();

        if (annotatedInputParameters.isEmpty()) {
            // There aren't input parameters in method.
            return;
        }

        List<AnnotationMetadata> parameterAnnotationList;
        AnnotationMetadata gvNixWebParamAnnotation;
        AnnotationMetadata webParamAnnotation;

        StringAttributeValue gvNixWebParamNameAttributeValue;
        StringAttributeValue webParamNameAttributeValue;
        ClassAttributeValue gvNIxWebParamTypeAttributeValue;
        StringAttributeValue targetNamespaceAttribute;

        for (AnnotatedJavaType inputParameter : annotatedInputParameters) {

            parameterAnnotationList = inputParameter.getAnnotations();

            Assert.isTrue(parameterAnnotationList != null
                    && !parameterAnnotationList.isEmpty(),
                    "Must be set @GvNIXWebParam and @WebParam annotations to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + " in method: '"
                            + methodMetadata.getMethodName()
                            + " defined in class: '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be exported as web Service operation.");

            gvNixWebParamAnnotation = MemberFindingUtils.getAnnotationOfType(
                    parameterAnnotationList, new JavaType(GvNIXWebParam.class
                            .getName()));

            Assert.isTrue(gvNixWebParamAnnotation != null,
                    "Must be set @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + " in method: '"
                            + methodMetadata.getMethodName()
                            + " defined in class: '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be exported as web Service operation.");

            gvNIxWebParamTypeAttributeValue = (ClassAttributeValue) gvNixWebParamAnnotation
                    .getAttribute(new JavaSymbolName("type"));

            Assert.isTrue(gvNIxWebParamTypeAttributeValue != null
                    && gvNIxWebParamTypeAttributeValue.getValue() != null,
                    "Must be set 'type' attribute in @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + " in method: '"
                            + methodMetadata.getMethodName()
                            + " defined in class: '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be exported as web Service operation.");

            // Check if is the same class type as parameter type.
            Assert
                    .isTrue(
                            inputParameter
                                    .getJavaType()
                                    .getFullyQualifiedTypeName()
                                    .contentEquals(
                                            gvNIxWebParamTypeAttributeValue
                                                    .getValue()
                                                    .getFullyQualifiedTypeName()),
                            "The 'type' attribute in @GvNIXWebParam annotation to: "
                                    + inputParameter.getJavaType()
                                            .getFullyQualifiedTypeName()
                                    + " in method: '"
                                    + methodMetadata.getMethodName()
                                    + " defined in class: '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' is different than parameter Java type. This would change web service contract."
                                    + "\nIf you want to change the web service contract you must define the same Java type in 'type' attribute in @GvNIXWebParam annotation.");

            gvNixWebParamNameAttributeValue = (StringAttributeValue) gvNixWebParamAnnotation
                    .getAttribute(new JavaSymbolName("name"));

            Assert.isTrue(gvNixWebParamNameAttributeValue != null
                    && StringUtils.hasText(gvNixWebParamNameAttributeValue
                            .getValue()),
                    "Must be set 'name' attribute in @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + " in method: '"
                            + methodMetadata.getMethodName()
                            + " defined in class: '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be exported as web Service operation.");

            webParamAnnotation = MemberFindingUtils
                    .getAnnotationOfType(parameterAnnotationList, new JavaType(
                            "javax.jws.WebParam"));

            Assert.isTrue(webParamAnnotation != null,
                    "Must be set @WebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + " in method: '"
                            + methodMetadata.getMethodName()
                            + " defined in class: '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be exported as web Service operation.");

            webParamNameAttributeValue = (StringAttributeValue) webParamAnnotation
                    .getAttribute(new JavaSymbolName("name"));

            Assert.isTrue(webParamNameAttributeValue != null
                    && StringUtils.hasText(webParamNameAttributeValue
                            .getValue()),
                    "Must be set 'name' attribute in @WebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + " in method: '"
                            + methodMetadata.getMethodName()
                            + " defined in class: '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be exported as web Service operation.");

            Assert
                    .isTrue(
                            webParamNameAttributeValue.getValue()
                                    .contentEquals(
                                            gvNixWebParamNameAttributeValue
                                                    .getValue()),
                            "The 'name' attribute in @GvNIXWebParam and @WebParam annotation to: "
                                    + inputParameter.getJavaType()
                                            .getFullyQualifiedTypeName()
                                    + " in method: '"
                                    + methodMetadata.getMethodName()
                                    + " defined in class: '"
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + "' are different. This would change web service contract."
                                    + "\nIf you want to change the web service contract you must define the same 'name' attribute in @GvNIXWebParam and @WebParam annotation.");

            targetNamespaceAttribute = (StringAttributeValue) webParamAnnotation
                    .getAttribute(new JavaSymbolName("targetNamespace"));

            if (!inputParameter.getJavaType().isPrimitive()
                    && !inputParameter.getJavaType().isCommonCollectionType()
                    && !inputParameter.getJavaType()
                            .getFullyQualifiedTypeName()
                            .startsWith("java.lang")) {

                if (targetNamespaceAttribute != null) {

                    Assert
                            .isTrue(
                                    serviceLayerWSExportValidationService
                                            .checkNamespaceFormat(targetNamespaceAttribute
                                                    .getValue()),
                                    "Attribute 'targetNamespace' in annotation @WebParam annotation to: "
                                            + inputParameter
                                                    .getJavaType()
                                                    .getFullyQualifiedTypeName()
                                            + " in method: '"
                                            + methodMetadata.getMethodName()
                                            + " defined in class: '"
                                            + governorTypeDetails
                                                    .getName()
                                                    .getFullyQualifiedTypeName()
                                            + "' has to start with 'http://'.\ni.e.: http://name.of.namespace/");

                }

            }

        }

    }

    /**
     * Retrieve all related ITDs to {@link ClassOrInterfaceTypeDetails} with all
     * information.
     * 
     * @param governorTypeDetails
     *            Class to retrieve all related ITD information.
     * @param governorPhysicalTypeMetadata
     *            Physical Metadata.
     * @param metadataIdentificationString
     *            Identify this Metadata.
     * 
     * @return List of {@link MemberHoldingTypeDetails} information.
     */
    public List<MemberHoldingTypeDetails> getMemberHoldingDetails(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String metadataIdentificationString) {

        // Create a list of discovered members
        // BeanInfoMetadataProviderImpl.class.getName()
        List<MemberHoldingTypeDetails> memberHoldingTypeDetails = new ArrayList<MemberHoldingTypeDetails>();

        // Build a List representing the class hierarchy, where the first
        // element is the absolute superclass
        List<ClassOrInterfaceTypeDetails> cidHierarchy = new ArrayList<ClassOrInterfaceTypeDetails>();
        while (governorTypeDetails != null) {
            cidHierarchy.add(0, governorTypeDetails); // note to the top of the
            // list
            governorTypeDetails = governorTypeDetails.getSuperclass();
        }

        // Now we add this governor, plus all of its superclasses
        for (ClassOrInterfaceTypeDetails currentClass : cidHierarchy) {
            memberHoldingTypeDetails.add(currentClass);

            // Add metadata representing accessors offered by other ITDs
            for (MetadataProvider provider : metadataService
                    .getRegisteredProviders()) {

                // We're only interested in all ITD providers
                if (this.equals(provider)
                        || !(provider instanceof ItdRoleAwareMetadataProvider)) {
                    continue;
                }

                // Determine the key the ITD provider uses for this particular
                // type
                String key = ((ItdMetadataProvider) provider)
                        .getIdForPhysicalJavaType(currentClass
                                .getDeclaredByMetadataId());
                Assert
                        .isTrue(MetadataIdentificationUtils
                                .isIdentifyingInstance(key),
                                "ITD metadata provider '" + provider
                                        + "' returned an illegal key ('" + key
                                        + "'");

                // Get the metadata and ensure we have ITD type details
                // available
                MetadataItem metadataItem = metadataService.get(key);
                if (metadataItem == null || !metadataItem.isValid()) {
                    continue;
                }

                if (!(metadataItem instanceof ItdTypeDetailsProvidingMetadataItem)) {
                    continue;
                }

                ItdTypeDetailsProvidingMetadataItem itdTypeDetailsMd = (ItdTypeDetailsProvidingMetadataItem) metadataItem;
                if (itdTypeDetailsMd.getItdTypeDetails() == null) {
                    continue;
                }

                metadataDependencyRegistry.registerDependency(key,
                        metadataIdentificationString);

                // Include its accessors
                memberHoldingTypeDetails.add(itdTypeDetailsMd
                        .getItdTypeDetails());
            }
        }

        return memberHoldingTypeDetails;
    }

    /**
     * Get public methods from associated {@link MemberHoldingTypeDetails} list.
     * 
     * @param memberHoldingTypeDetails
     *            to get all public methods defined.
     * @return list of public methods {@link MethodMetadata}.
     */
    public List<MethodMetadata> getPublicAccessors(
            List<MemberHoldingTypeDetails> memberHoldingTypeDetails) {

        // We keep these in a TreeMap so the methods are output in alphabetic
        // order
        List<MethodMetadata> sortedByDetectionOrder = new ArrayList<MethodMetadata>();

        for (MemberHoldingTypeDetails holder : memberHoldingTypeDetails) {
            for (MethodMetadata method : holder.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifier())
                        && !Modifier.isStatic(method.getModifier())) {
                    sortedByDetectionOrder.add(method);
                }
            }
        }
        return sortedByDetectionOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNix_WebService";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
        return ServiceLayerWSExportMetadata.getMetadataIdentiferType();
    }

}
