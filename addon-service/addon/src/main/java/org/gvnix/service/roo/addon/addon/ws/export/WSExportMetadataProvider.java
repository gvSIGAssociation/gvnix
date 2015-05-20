/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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

import java.util.ArrayList;
import java.util.List;
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
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * <p>
 * Checks if @GvNIXWebServices annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component
@Service
public class WSExportMetadataProvider extends AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(WSExportMetadataProvider.class);

    private static final String JSYMBOL_NAMESPACE = "targetNamespace";
    private static final String JSYMBOL_NAME = "name";
    private static final String HAS_TOBE_DEF_EXP = "' has to be defined to export class as Web Service.";
    private static final String IN_CLASS = "' in class '";
    private static final String EXPORT_WEB_OP = "' has to be defined to export as Web Service operation.";
    private static final String HAS_TO_START = "' has to start with 'http://'.\ni.e.: http://name.of.namespace/";
    private static final String IN_METHOD = " in method: '";
    private static final String DEF_IN_CLASS = " defined in class: '";
    private static final String TOBE_WEB_SERV_OP = "' to be exported as web Service operation.";

    private WSExportValidationService wSExportValidationService;
    private WSConfigService wSConfigService;
    private AnnotationsService annotationsService;
    private JavaParserService javaParserService;

    private static Logger logger = Logger
            .getLogger(WSExportMetadataProvider.class.getName());

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebService.class.getName()));
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * createLocalIdentifier(org.springframework.roo.model.JavaType,
     * org.springframework.roo.project.Path)
     */
    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return WSExportMetadata.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = WSExportMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = WSExportMetadata
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
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        WSExportMetadata serviceLayerMetadata = null;

        // Configures project
        getWSConfigService().install(WsType.EXPORT);

        // Check if Web Service definition is correct.
        PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();

        ClassOrInterfaceTypeDetails governorTypeDetails;
        if (physicalTypeDetails == null
                || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
            // There is a problem
            return null;
        }
        else {
            // We have reliable physical type details
            governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }

        // Get upstreamDepency Class to check.
        AnnotationMetadata gvNIXWebServiceAnnotation = governorTypeDetails
                .getTypeAnnotation(new JavaType(GvNIXWebService.class.getName()));

        // Check @GvNIXWebService annotation attributes.
        checkGvNIXWebServiceAnnotationAttributes(gvNIXWebServiceAnnotation,
                governorTypeDetails);

        // Default Web Service target Namespace.
        StringAttributeValue webSrvTarNmspcAttrVal = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName(JSYMBOL_NAMESPACE));
        String webServiceTargetNamespace = webSrvTarNmspcAttrVal.getValue();

        // Show info
        logger.log(Level.FINE,
                "Check correct format to export the web service class: '"
                        + governorTypeDetails.getName() + "'");

        // Update CXF XML
        boolean updtGvNIXWServAnn = getWSConfigService()
                .publishClassAsWebService(governorTypeDetails.getName(),
                        gvNIXWebServiceAnnotation);

        // Define Jax-WS plugin and creates and execution build for this
        // service to generate the wsdl file to check errors before deploy.
        StringAttributeValue serviceName = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("serviceName"));

        StringAttributeValue address = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("address"));

        StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

        getWSConfigService().addToJava2wsPlugin(governorTypeDetails.getName(),
                serviceName.getValue(), address.getValue(),
                fullyQualifiedTypeName.getValue());

        // Get methods to check.
        List<MethodMetadata> methodMetadataList = getMemberDetails(
                physicalTypeDetails.getName()).getMethods();

        BooleanAttributeValue exported = (BooleanAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("exported"));

        // For every public exported method checks its signature and
        // annotations.
        for (MethodMetadata methodMetadata : methodMetadataList) {

            AnnotationMetadata gvNixWebMethodAnnotation = MemberFindingUtils
                    .getAnnotationOfType(methodMetadata.getAnnotations(),
                            new JavaType(GvNIXWebMethod.class.getName()));

            if (gvNixWebMethodAnnotation == null) {
                // This method is not exported
                continue;
            }

            // If the web service has been exported from WSDL, the
            // parameters hasn't to be checked
            if (!exported.getValue()) {

                MethodMetadata method = getJavaParserService()
                        .getMethodByNameInAll(governorTypeDetails.getName(),
                                methodMetadata.getMethodName());

                // Add gvNIX xml element annotation to method return and
                // parameters project types
                getWSExportValidationService()
                        .addGvNixXmlElementToTypes(method);

                // Add gvNIX web fault annotation to method exceptions
                getWSExportValidationService().addGvNixWebFaultToExceptions(
                        method, webServiceTargetNamespace);

                // Checks @GvNIXWebMethod has attributes.
                Validate.isTrue(
                        !gvNixWebMethodAnnotation.getAttributeNames().isEmpty(),
                        "The annotation @GvNIXWebMethod of '"
                                .concat(methodMetadata.getMethodName()
                                        .getSymbolName())
                                .concat("' method in class '")
                                .concat(governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName())
                                .concat("' must have all its attributes defined."));

                // Check if @GvNIXWebMethod attributes are correct to
                // export method to web service annotation in ITD.
                checkGvNIXWebMethodAnnotationAttributes(
                        gvNixWebMethodAnnotation, governorTypeDetails,
                        methodMetadata);

                // Checks @WebParam and @GvNIXWebParam attributes for
                // each input parameter in method.
                checkGvNIXWebParamsAnnotationAttributes(governorTypeDetails,
                        methodMetadata);
            }

        }

        // Update Annotation because Java Class or package has changed.
        if (updtGvNIXWServAnn) {

            List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
            gvNixAnnotationAttributes.add(gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName(JSYMBOL_NAME)));
            gvNixAnnotationAttributes.add(gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName(JSYMBOL_NAMESPACE)));
            gvNixAnnotationAttributes.add(gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("serviceName")));
            gvNixAnnotationAttributes.add(gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("address")));
            gvNixAnnotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("fullyQualifiedTypeName"),
                    governorTypeDetails.getName().getFullyQualifiedTypeName()));
            gvNixAnnotationAttributes.add(gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("exported")));

            getAnnotationsService().addJavaTypeAnnotation(
                    governorTypeDetails.getName(),
                    GvNIXWebService.class.getName(), gvNixAnnotationAttributes,
                    true);
        }

        serviceLayerMetadata = new WSExportMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, methodMetadataList,
                getJavaParserService());

        return serviceLayerMetadata;
    }

    /**
     * Check correct values in @GvNIXWebService annotation.
     * 
     * @param gvNIXWebServiceAnnotation to check.
     * @param governorTypeDetails class where is defined @GvNIXWebService.
     */
    public void checkGvNIXWebServiceAnnotationAttributes(
            AnnotationMetadata gvNIXWebServiceAnnotation,
            ClassOrInterfaceTypeDetails governorTypeDetails) {

        // name
        StringAttributeValue name = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName(JSYMBOL_NAME));

        Validate.isTrue(
                name != null && StringUtils.isNotBlank(name.getValue()),
                "Attribute 'name' in annotation @GvNIXWebService defined in class '"
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + HAS_TOBE_DEF_EXP);

        // targetNamespace
        StringAttributeValue targetNamespace = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName(JSYMBOL_NAMESPACE));

        Validate.isTrue(
                targetNamespace != null
                        && StringUtils.isNotBlank(targetNamespace.getValue()),
                "Attribute 'targetNamespace' in annotation @GvNIXWebService defined in class '"
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + HAS_TOBE_DEF_EXP);

        Validate.isTrue(
                targetNamespace != null
                        && getWSExportValidationService().checkNamespaceFormat(
                                targetNamespace.getValue()),
                "The namespace for Web Service has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        // serviceName
        StringAttributeValue serviceName = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("serviceName"));

        Validate.isTrue(
                serviceName != null
                        && StringUtils.isNotBlank(serviceName.getValue()),
                "Attribute 'serviceName' in annotation @GvNIXWebService defined in class '"
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + HAS_TOBE_DEF_EXP);

        // address
        StringAttributeValue address = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("address"));

        Validate.isTrue(
                address != null && StringUtils.isNotBlank(address.getValue()),
                "Attribute 'address' in annotation @GvNIXWebService defined in class '"
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + HAS_TOBE_DEF_EXP);

        // fullyQualifiedTypeName
        StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) gvNIXWebServiceAnnotation
                .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

        Validate.isTrue(
                fullyQualifiedTypeName != null
                        && StringUtils.isNotBlank(fullyQualifiedTypeName
                                .getValue()),
                "Attribute 'fullyQualifiedTypeName' in annotation @GvNIXWebService defined in class '"
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + HAS_TOBE_DEF_EXP);

    }

    /**
     * Check correct values in @GvNIXWebMethod annotation.
     * <p>
     * Annotation attributes to check:
     * </p>
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
     * </ul>
     * 
     * @param gvNixWebMethodAnnotation to check.
     * @param governorTypeDetails class where is defined @GvNIXWebMethod.
     */
    public void checkGvNIXWebMethodAnnotationAttributes(
            AnnotationMetadata gvNixWebMethodAnnotation,
            ClassOrInterfaceTypeDetails governorTypeDetails,
            MethodMetadata methodMetadata) {

        // operationName
        StringAttributeValue operationName = (StringAttributeValue) gvNixWebMethodAnnotation
                .getAttribute(new JavaSymbolName("operationName"));

        Validate.isTrue(
                operationName != null
                        && StringUtils.isNotBlank(operationName.getValue()),
                "Attribute 'operationName' in annotation @GvNIXWebMethod defined in method '"
                        + methodMetadata.getMethodName()
                        + IN_CLASS
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + EXPORT_WEB_OP);

        // webResultType
        ClassAttributeValue webResultType = (ClassAttributeValue) gvNixWebMethodAnnotation
                .getAttribute(new JavaSymbolName("webResultType"));

        Validate.isTrue(
                webResultType != null,
                "Attribute 'webResultType' in annotation @GvNIXWebMethod has to be defined in method '"
                        + methodMetadata.getMethodName()
                        + IN_CLASS
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName()
                        + "' even if it's 'void' Java Type to export as Web Service operation.");

        // Check if webResultType has the same value than method returnType.
        Validate.isTrue(
                methodMetadata
                        .getReturnType()
                        .getFullyQualifiedTypeName()
                        .contentEquals(
                                webResultType.getValue()
                                        .getFullyQualifiedTypeName()),
                "Attribute 'webResultType' in annotation @GvNIXWebMethod defined in method '"
                        + methodMetadata.getMethodName()
                        + IN_CLASS
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
                && !webResultType
                        .getValue()
                        .getFullyQualifiedTypeName()
                        .contentEquals(
                                JavaType.VOID_PRIMITIVE
                                        .getFullyQualifiedTypeName())) {

            Validate.isTrue(
                    resultName != null
                            && StringUtils.isNotBlank(resultName.getValue()),
                    "Attribute 'resultName' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

            // resultNamespace
            StringAttributeValue resultNamespace = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("resultNamespace"));

            Validate.isTrue(
                    resultNamespace != null
                            && StringUtils.isNotBlank(resultNamespace
                                    .getValue()),
                    "Attribute 'resultNamespace' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

            Validate.isTrue(
                    resultNamespace != null
                            && getWSExportValidationService()
                                    .checkNamespaceFormat(
                                            resultNamespace.getValue()),
                    "Attribute 'resultNamespace' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName() + HAS_TO_START);

            // responseWrapperName
            StringAttributeValue responseWrapperName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperName"));

            Validate.isTrue(
                    responseWrapperName != null
                            && StringUtils.isNotBlank(responseWrapperName
                                    .getValue()),
                    "Attribute 'responseWrapperName' in annotation @GvNIXWebService defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

            // responseWrapperNamespace
            StringAttributeValue responseWrapperNamespace = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperNamespace"));

            Validate.isTrue(
                    responseWrapperNamespace != null
                            && StringUtils.isNotBlank(responseWrapperNamespace
                                    .getValue()),
                    "Attribute 'responseWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

            Validate.isTrue(
                    responseWrapperNamespace != null
                            && getWSExportValidationService()
                                    .checkNamespaceFormat(
                                            responseWrapperNamespace.getValue()),
                    "Attribute 'responseWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName() + HAS_TO_START);
            // responseWrapperClassName
            StringAttributeValue responseWrapperClassName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperClassName"));

            Validate.isTrue(
                    responseWrapperClassName != null
                            && StringUtils.isNotBlank(responseWrapperClassName
                                    .getValue()),
                    "Attribute 'responseWrapperClassName' in annotation @GvNIXWebService defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

        }

        // Check if method has input parameters.
        if (!methodMetadata.getParameterTypes().isEmpty()
                && !methodMetadata.getParameterNames().isEmpty()) {

            // requestWrapperName
            StringAttributeValue requestWrapperName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperName"));

            Validate.isTrue(
                    requestWrapperName != null
                            && StringUtils.isNotBlank(requestWrapperName
                                    .getValue()),
                    "Attribute 'requestWrapperName' in annotation @GvNIXWebService defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

            // requestWrapperNamespace
            StringAttributeValue requestWrapperNamespace = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperNamespace"));

            Validate.isTrue(
                    requestWrapperNamespace != null
                            && StringUtils.isNotBlank(requestWrapperNamespace
                                    .getValue()),
                    "Attribute 'requestWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);

            Validate.isTrue(
                    requestWrapperName != null
                            && getWSExportValidationService()
                                    .checkNamespaceFormat(
                                            requestWrapperNamespace.getValue()),
                    "Attribute 'requestWrapperNamespace' in annotation @GvNIXWebMethod defined in method '"
                            + methodMetadata.getMethodName()
                            + IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName() + HAS_TO_START);

            // requestWrapperClassName
            StringAttributeValue requestWrapperClassName = (StringAttributeValue) gvNixWebMethodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperClassName"));

            Validate.isTrue(
                    requestWrapperClassName != null
                            && StringUtils.isNotBlank(requestWrapperClassName
                                    .getValue()),
                    "Attribute 'requestWrapperClassName' in annotation @GvNIXWebService defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + EXPORT_WEB_OP);
        }

    }

    /**
     * Checks @GvNIXWebParam annotation attribute values in input method
     * parameters to avoid changes in service contract.
     * 
     * @param governorTypeDetails Service Class.
     * @param methodMetadata Method to check parameters.
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

        StringAttributeValue gvNIXWParamNmAttrVal;
        StringAttributeValue webParamNameAttributeValue;
        ClassAttributeValue gvNIXWParamTypeAttrVal;
        StringAttributeValue targetNamespaceAttribute;

        for (AnnotatedJavaType inputParameter : annotatedInputParameters) {

            parameterAnnotationList = inputParameter.getAnnotations();

            Validate.isTrue(parameterAnnotationList != null
                    && !parameterAnnotationList.isEmpty(),
                    "Must be set @GvNIXWebParam and @WebParam annotations to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + TOBE_WEB_SERV_OP);

            gvNixWebParamAnnotation = MemberFindingUtils.getAnnotationOfType(
                    parameterAnnotationList,
                    new JavaType(GvNIXWebParam.class.getName()));

            Validate.isTrue(gvNixWebParamAnnotation != null,
                    "Must be set @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + TOBE_WEB_SERV_OP);

            gvNIXWParamTypeAttrVal = (ClassAttributeValue) gvNixWebParamAnnotation
                    .getAttribute(new JavaSymbolName("type"));

            Validate.isTrue(gvNIXWParamTypeAttrVal != null
                    && gvNIXWParamTypeAttrVal.getValue() != null,
                    "Must be set 'type' attribute in @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + TOBE_WEB_SERV_OP);

            // Check if is the same class type as parameter type.
            Validate.isTrue(
                    gvNIXWParamTypeAttrVal != null
                            && inputParameter
                                    .getJavaType()
                                    .getFullyQualifiedTypeName()
                                    .contentEquals(
                                            gvNIXWParamTypeAttrVal
                                                    .getValue()
                                                    .getFullyQualifiedTypeName()),
                    "The 'type' attribute in @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' is different than parameter Java type. This would change web service contract."
                            + "\nIf you want to change the web service contract you must define the same Java type in 'type' attribute in @GvNIXWebParam annotation.");

            gvNIXWParamNmAttrVal = (StringAttributeValue) gvNixWebParamAnnotation
                    .getAttribute(new JavaSymbolName(JSYMBOL_NAME));

            Validate.isTrue(
                    gvNIXWParamNmAttrVal != null
                            && StringUtils.isNotBlank(gvNIXWParamNmAttrVal
                                    .getValue()),
                    "Must be set 'name' attribute in @GvNIXWebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + TOBE_WEB_SERV_OP);

            webParamAnnotation = MemberFindingUtils
                    .getAnnotationOfType(parameterAnnotationList, new JavaType(
                            "javax.jws.WebParam"));

            Validate.isTrue(webParamAnnotation != null,
                    "Must be set @WebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + TOBE_WEB_SERV_OP);

            webParamNameAttributeValue = (StringAttributeValue) webParamAnnotation
                    .getAttribute(new JavaSymbolName(JSYMBOL_NAME));

            Validate.isTrue(
                    webParamNameAttributeValue != null
                            && StringUtils
                                    .isNotBlank(webParamNameAttributeValue
                                            .getValue()),
                    "Must be set 'name' attribute in @WebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + TOBE_WEB_SERV_OP);

            Validate.isTrue(
                    webParamNameAttributeValue != null
                            && gvNIXWParamNmAttrVal != null
                            && webParamNameAttributeValue.getValue()
                                    .contentEquals(
                                            gvNIXWParamNmAttrVal.getValue()),
                    "The 'name' attribute in @GvNIXWebParam and @WebParam annotation to: "
                            + inputParameter.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + IN_METHOD
                            + methodMetadata.getMethodName()
                            + DEF_IN_CLASS
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' are different. This would change web service contract."
                            + "\nIf you want to change the web service contract you must define the same 'name' attribute in @GvNIXWebParam and @WebParam annotation.");

            targetNamespaceAttribute = (StringAttributeValue) webParamAnnotation
                    .getAttribute(new JavaSymbolName(JSYMBOL_NAMESPACE));

            if (!inputParameter.getJavaType().isPrimitive()
                    && !inputParameter.getJavaType().isCommonCollectionType()
                    && !inputParameter.getJavaType()
                            .getFullyQualifiedTypeName()
                            .startsWith("java.lang")) {

                if (targetNamespaceAttribute != null) {

                    Validate.isTrue(
                            getWSExportValidationService()
                                    .checkNamespaceFormat(
                                            targetNamespaceAttribute.getValue()),
                            "Attribute 'targetNamespace' in annotation @WebParam annotation to: "
                                    + inputParameter.getJavaType()
                                            .getFullyQualifiedTypeName()
                                    + IN_METHOD
                                    + methodMetadata.getMethodName()
                                    + DEF_IN_CLASS
                                    + governorTypeDetails.getName()
                                            .getFullyQualifiedTypeName()
                                    + HAS_TO_START);

                }

            }

        }

    }

    /**
     * Retrieve all related ITDs to {@link ClassOrInterfaceTypeDetails} with all
     * information.
     * 
     * @param governorTypeDetails Class to retrieve all related ITD information.
     * @param governorPhysicalTypeMetadata Physical Metadata.
     * @param metadataIdentificationString Identify this Metadata.
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

            MetadataItem metadataItem = metadataService.get(currentClass
                    .getDeclaredByMetadataId());

            if (metadataItem == null || !metadataItem.isValid()) {
                continue;
            }

            if (!(metadataItem instanceof ItdTypeDetailsProvidingMetadataItem)) {
                continue;
            }

            ItdTypeDetailsProvidingMetadataItem itdTypeDetailsMd = (ItdTypeDetailsProvidingMetadataItem) metadataItem;

            if (itdTypeDetailsMd.getMemberHoldingTypeDetails() == null) {
                continue;
            }

            getMetadataDependencyRegistry().registerDependency(
                    metadataItem.getId(), metadataIdentificationString);

            // Include its accessors
            memberHoldingTypeDetails.add(itdTypeDetailsMd
                    .getMemberHoldingTypeDetails());
        }

        return memberHoldingTypeDetails;
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
        return WSExportMetadata.getMetadataIdentiferType();
    }

    public WSConfigService getWSConfigService() {
        if (wSConfigService == null) {
            // Get all Services implement WSConfigService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WSConfigService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WSConfigService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WSConfigService on WSExportMetadataProvider.");
                return null;
            }
        }
        else {
            return wSConfigService;
        }
    }

    public JavaParserService getJavaParserService() {
        if (javaParserService == null) {
            // Get all Services implement JavaParserService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                JavaParserService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (JavaParserService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load JavaParserService on WSExportMetadataProvider.");
                return null;
            }
        }
        else {
            return javaParserService;
        }
    }

    public AnnotationsService getAnnotationsService() {
        if (annotationsService == null) {
            // Get all Services implement AnnotationsService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                AnnotationsService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (AnnotationsService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load AnnotationsService on WSExportMetadataProvider.");
                return null;
            }
        }
        else {
            return annotationsService;
        }
    }

    public WSExportValidationService getWSExportValidationService() {
        if (wSExportValidationService == null) {
            // Get all Services implement WSExportValidationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WSExportValidationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WSExportValidationService) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WSExportValidationService on WSExportMetadataProvider.");
                return null;
            }
        }
        else {
            return wSExportValidationService;
        }
    }

}
