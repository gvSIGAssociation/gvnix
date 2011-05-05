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

import java.util.ArrayList;
import java.util.List;

import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.DeclaredMethodAnnotationDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * <p>
 * gvNix Web Service Java Contract generation.
 * </p>
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WSExportMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String WEB_SERVICE_TYPE_STRING = WSExportMetadata.class
            .getName();
    private static final String WEB_SERVICE_TYPE = MetadataIdentificationUtils
            .create(WEB_SERVICE_TYPE_STRING);

    public WSExportMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            List<MethodMetadata> methodMetadataList) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        if (!isValid()) {
            return;
        }

        // Create the metadata.
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getTypeAnnotation(governorTypeDetails, new JavaType(
                        GvNIXWebService.class.getName()));

        // Check if Web Service has been exported from WSDL.
        BooleanAttributeValue exported = (BooleanAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("exported"));

        if (annotationMetadata != null) {

            // Add @javax.jws.WebService and @javax.jws.soap.SOAPBinding.
            AnnotationMetadata webServiceAnnotationMetadata = getWebServiceAnnotation(annotationMetadata);

            builder.addAnnotation(webServiceAnnotationMetadata);

            AnnotationMetadata soapBindingAnnotationMetadata = getSoapBindingAnnotation(annotationMetadata);

            builder.addAnnotation(soapBindingAnnotationMetadata);

            // Methods to exclude from web service.
            List<MethodMetadata> methodMetadataListToExclude = new ArrayList<MethodMetadata>();

            for (MethodMetadata methodMetadata : methodMetadataList) {

                AnnotationMetadata methodAnnotation = MemberFindingUtils
                        .getAnnotationOfType(methodMetadata.getAnnotations(),
                                new JavaType(GvNIXWebMethod.class.getName()));

                if (methodAnnotation != null) {

                    if (!exported.getValue()) {
                        // Update ITD with Web Services annotations
                        // declarations.
                        updateMethodWithGvNIXAnnotation(methodMetadata,
                                methodAnnotation);
                    } else {
                        // Update method without checking attributes.
                        updateMethodWithGvNIXAnnotationFromWsdl(methodMetadata,
                                methodAnnotation);
                    }
                } else {
                    // Exclude from Web Service.
                    methodMetadataListToExclude.add(methodMetadata);
                }

            }

            // Update methods without @GvNIXWebMethod annotation with
            // '@WebMethod(exclude = true)'
            updateMethodWithoutGvNIXAnnotation(methodMetadataListToExclude);
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Adds @javax.jws.WebService annotation to the type, unless it already
     * exists.
     * 
     * @param annotationMetadata
     *            to retrieve selected values to @javax.jws.WebService
     * 
     * @return the annotation is already exists or will be created, or null if
     *         it will not be created (required)
     */
    public AnnotationMetadata getWebServiceAnnotation(
            AnnotationMetadata annotationMetadata) {

        JavaType javaType = new JavaType("javax.jws.WebService");

        if (isAnnotationIntroduced("javax.jws.WebService")) {

            List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            StringAttributeValue nameAttributeValue = (StringAttributeValue) annotationMetadata
                    .getAttribute(new JavaSymbolName("name"));

            annotationAttributeValueList.add(nameAttributeValue);

            StringAttributeValue targetNamespaceAttributeValue = (StringAttributeValue) annotationMetadata
                    .getAttribute(new JavaSymbolName("targetNamespace"));

            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            StringAttributeValue serviceNameAttributeValue = (StringAttributeValue) annotationMetadata
                    .getAttribute(new JavaSymbolName("serviceName"));

            annotationAttributeValueList.add(serviceNameAttributeValue);

            StringAttributeValue portNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("portName"), nameAttributeValue
                            .getValue());

            annotationAttributeValueList.add(portNameAttributeValue);

            return new AnnotationMetadataBuilder(javaType,
                    annotationAttributeValueList).build();
        }

        return MemberFindingUtils.getDeclaredTypeAnnotation(
                governorTypeDetails, javaType);
    }

    /**
     * Adds @javax.jws.soap.SOAPBinding annotation to the type, unless it
     * already exists.
     * 
     * @param annotationMetadata
     *            to check if exists SOAPBinding parameter type value defined.
     * 
     * @return the annotation is already exists or will be created, or null if
     *         it will not be created (required)
     */
    public AnnotationMetadata getSoapBindingAnnotation(
            AnnotationMetadata annotationMetadata) {
        JavaType javaType = new JavaType("javax.jws.soap.SOAPBinding");

        if (isAnnotationIntroduced("javax.jws.soap.SOAPBinding")) {

            List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            EnumAttributeValue enumStyleAttributeValue = new EnumAttributeValue(
                    new JavaSymbolName("style"), new EnumDetails(new JavaType(
                            "javax.jws.soap.SOAPBinding.Style"),
                            new JavaSymbolName("DOCUMENT")));

            annotationAttributeValueList.add(enumStyleAttributeValue);

            EnumAttributeValue enumUseAttributeValue = new EnumAttributeValue(
                    new JavaSymbolName("use"), new EnumDetails(new JavaType(
                            "javax.jws.soap.SOAPBinding.Use"),
                            new JavaSymbolName("LITERAL")));

            annotationAttributeValueList.add(enumUseAttributeValue);

            EnumAttributeValue enumparameterStyleAttributeValue = (EnumAttributeValue) annotationMetadata
                    .getAttribute(new JavaSymbolName("parameterStyle"));

            if (enumparameterStyleAttributeValue != null) {

                enumparameterStyleAttributeValue = new EnumAttributeValue(
                        new JavaSymbolName("parameterStyle"),
                        new EnumDetails(new JavaType(
                                "javax.jws.soap.SOAPBinding.ParameterStyle"),
                                enumparameterStyleAttributeValue.getValue()
                                        .getField()));

            } else {
                enumparameterStyleAttributeValue = new EnumAttributeValue(
                        new JavaSymbolName("parameterStyle"),
                        new EnumDetails(new JavaType(
                                "javax.jws.soap.SOAPBinding.ParameterStyle"),
                                new JavaSymbolName("WRAPPED")));
            }

            annotationAttributeValueList.add(enumparameterStyleAttributeValue);

            return new AnnotationMetadataBuilder(javaType,
                    annotationAttributeValueList).build();
        }

        return MemberFindingUtils.getDeclaredTypeAnnotation(
                governorTypeDetails, javaType);
    }

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation
     *            to be check if exists.
     * 
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroduced(String annotation) {
        JavaType javaType = new JavaType(annotation);
        AnnotationMetadata result = MemberFindingUtils
                .getDeclaredTypeAnnotation(governorTypeDetails, javaType);

        return result == null;
    }

    /**
     * Update methods with @GvNIXWebMethod annotation to ITD.
     * 
     * @param methodMetadata
     *            method to assign ITD declarations.
     * @param methodAnnotation
     *            Annotations to generate ITD declaration.
     */
    public void updateMethodWithGvNIXAnnotation(MethodMetadata methodMetadata,
            AnnotationMetadata methodAnnotation) {

        List<AnnotationAttributeValue<?>> annotationAttributeValueList;

        AnnotationAttributeValue<?> tmpAnnotationAttributeValue;

        // javax.jws.WebMethod
        annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        StringAttributeValue operationNameAttributeValue = (StringAttributeValue) methodAnnotation
                .getAttribute(new JavaSymbolName("operationName"));

        annotationAttributeValueList.add(operationNameAttributeValue);

        // Check if exists action attribute defined.
        tmpAnnotationAttributeValue = methodAnnotation
                .getAttribute(new JavaSymbolName("action"));

        StringAttributeValue actionAttribuetValue;
        if (tmpAnnotationAttributeValue != null) {
            actionAttribuetValue = new StringAttributeValue(new JavaSymbolName(
                    "action"),
                    ((StringAttributeValue) tmpAnnotationAttributeValue)
                            .getValue());
        } else {
            actionAttribuetValue = new StringAttributeValue(new JavaSymbolName(
                    "action"), "");
        }

        annotationAttributeValueList.add(actionAttribuetValue);

        BooleanAttributeValue excludeAttribuetValue = new BooleanAttributeValue(
                new JavaSymbolName("exclude"), false);
        annotationAttributeValueList.add(excludeAttribuetValue);

        AnnotationMetadata webMethod = new AnnotationMetadataBuilder(
                new JavaType("javax.jws.WebMethod"),
                annotationAttributeValueList).build();

        // Add to AspectJ.
        builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                methodMetadata, webMethod));

        if (!methodMetadata.getParameterTypes().isEmpty()
                && !methodMetadata.getParameterNames().isEmpty()) {

            // javax.xml.ws.RequestWrapper
            annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("localName"),
                    ((StringAttributeValue) methodAnnotation
                            .getAttribute(new JavaSymbolName(
                                    "requestWrapperName"))).getValue());
            annotationAttributeValueList.add(localNameAttributeValue);

            StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("targetNamespace"),
                    ((StringAttributeValue) methodAnnotation
                            .getAttribute(new JavaSymbolName(
                                    "requestWrapperNamespace"))).getValue());
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("className"),
                    ((StringAttributeValue) methodAnnotation
                            .getAttribute(new JavaSymbolName(
                                    "requestWrapperClassName"))).getValue());
            annotationAttributeValueList.add(classNameAttributeValue);

            AnnotationMetadata requestWrapper = new AnnotationMetadataBuilder(
                    new JavaType("javax.xml.ws.RequestWrapper"),
                    annotationAttributeValueList).build();

            // Add to AspectJ.
            builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                    methodMetadata, requestWrapper));

        }

        // javax.jws.WebResult
        // Check result value
        StringAttributeValue resutlNameAttributeValue = (StringAttributeValue) methodAnnotation
                .getAttribute(new JavaSymbolName("resultName"));

        ClassAttributeValue resultTypeAttributeValue = (ClassAttributeValue) methodAnnotation
                .getAttribute(new JavaSymbolName("webResultType"));

        if ((resutlNameAttributeValue != null && !resutlNameAttributeValue
                .getValue().contains("void"))
                && (resultTypeAttributeValue != null && !resultTypeAttributeValue
                        .getValue().getFullyQualifiedTypeName().contains(
                                JavaType.VOID_PRIMITIVE
                                        .getFullyQualifiedTypeName()))) {

            annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("name"), resutlNameAttributeValue
                            .getValue());
            annotationAttributeValueList.add(localNameAttributeValue);

            StringAttributeValue gvNIxWebResultTargetNamespace = (StringAttributeValue) methodAnnotation
                    .getAttribute(new JavaSymbolName("resultNamespace"));

            StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("targetNamespace"),
                    gvNIxWebResultTargetNamespace.getValue());

            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            // Check parameters header, partName from WebResult.
            // Parameter webResultHeader.
            BooleanAttributeValue headerAttributeValue = (BooleanAttributeValue) methodAnnotation
                    .getAttribute(new JavaSymbolName("webResultHeader"));

            if (headerAttributeValue == null) {
                headerAttributeValue = new BooleanAttributeValue(
                        new JavaSymbolName("header"), false);
            } else {
                headerAttributeValue = new BooleanAttributeValue(
                        new JavaSymbolName("header"), headerAttributeValue
                                .getValue());
            }
            annotationAttributeValueList.add(headerAttributeValue);

            // Parameter webResultPartName.
            StringAttributeValue partNameAttributeValue = (StringAttributeValue) methodAnnotation
                    .getAttribute(new JavaSymbolName("webResultPartName"));

            if (partNameAttributeValue == null) {
                partNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("partName"), "parameters");
            } else {
                partNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("partName"), partNameAttributeValue
                                .getValue());
            }
            annotationAttributeValueList.add(partNameAttributeValue);

            AnnotationMetadata webResult = new AnnotationMetadataBuilder(
                    new JavaType("javax.jws.WebResult"),
                    annotationAttributeValueList).build();

            // Add to AspectJ.
            builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                    methodMetadata, webResult));

            // javax.xml.ws.ResponseWrapper
            annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("localName"),
                    ((StringAttributeValue) methodAnnotation
                            .getAttribute(new JavaSymbolName(
                                    "responseWrapperName"))).getValue());
            annotationAttributeValueList.add(localNameAttributeValue);

            targetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("targetNamespace"),
                    ((StringAttributeValue) methodAnnotation
                            .getAttribute(new JavaSymbolName(
                                    "responseWrapperNamespace"))).getValue());
            annotationAttributeValueList.add(targetNamespaceAttributeValue);

            StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("className"),
                    ((StringAttributeValue) methodAnnotation
                            .getAttribute(new JavaSymbolName(
                                    "responseWrapperClassName"))).getValue());
            annotationAttributeValueList.add(classNameAttributeValue);

            AnnotationMetadata responseWrapper = new AnnotationMetadataBuilder(
                    new JavaType("javax.xml.ws.ResponseWrapper"),
                    annotationAttributeValueList).build();

            // Add to AspectJ.
            builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                    methodMetadata, responseWrapper));

        } else {
            // @Oneway - not require a response from the service.
            AnnotationMetadata oneway = new AnnotationMetadataBuilder(
                    new JavaType("javax.jws.Oneway"),
                    new ArrayList<AnnotationAttributeValue<?>>()).build();

            // Add to AspectJ.
            builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                    methodMetadata, oneway));
        }

    }

    /**
     * Update methods with @GvNIXWebMethod annotation when Web Service has been
     * exported from WSDL file.
     * 
     * @param methodMetadata
     *            method to assign ITD declarations.
     * @param methodAnnotation
     *            Annotations to generate ITD declaration.
     */
    public void updateMethodWithGvNIXAnnotationFromWsdl(
            MethodMetadata methodMetadata, AnnotationMetadata methodAnnotation) {

        List<AnnotationAttributeValue<?>> annotationAttributeValueList;

        AnnotationAttributeValue<?> tmpAnnotationAttributeValue;

        // javax.jws.WebMethod
        annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        StringAttributeValue operationNameAttributeValue = (StringAttributeValue) methodAnnotation
                .getAttribute(new JavaSymbolName("operationName"));

        annotationAttributeValueList.add(operationNameAttributeValue);

        // Check if exists action attribute defined.
        tmpAnnotationAttributeValue = methodAnnotation
                .getAttribute(new JavaSymbolName("action"));

        StringAttributeValue actionAttribuetValue;
        if (tmpAnnotationAttributeValue != null) {
            actionAttribuetValue = new StringAttributeValue(new JavaSymbolName(
                    "action"),
                    ((StringAttributeValue) tmpAnnotationAttributeValue)
                            .getValue());
        } else {
            actionAttribuetValue = new StringAttributeValue(new JavaSymbolName(
                    "action"), "");
        }

        annotationAttributeValueList.add(actionAttribuetValue);

        BooleanAttributeValue excludeAttribuetValue = new BooleanAttributeValue(
                new JavaSymbolName("exclude"), false);
        annotationAttributeValueList.add(excludeAttribuetValue);

        AnnotationMetadata webMethod = new AnnotationMetadataBuilder(
                new JavaType("javax.jws.WebMethod"),
                annotationAttributeValueList).build();

        // Add to AspectJ.
        builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                methodMetadata, webMethod));

        if (!methodMetadata.getParameterTypes().isEmpty()
                && !methodMetadata.getParameterNames().isEmpty()) {

            // javax.xml.ws.RequestWrapper
            annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperName"));

            if (tmpAnnotationAttributeValue != null) {

                StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("localName"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());
                annotationAttributeValueList.add(localNameAttributeValue);

            }

            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperNamespace"));

            if (tmpAnnotationAttributeValue != null) {

                StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("targetNamespace"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());
                annotationAttributeValueList.add(targetNamespaceAttributeValue);
            }

            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("requestWrapperClassName"));

            if (tmpAnnotationAttributeValue != null) {

                StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("className"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());
                annotationAttributeValueList.add(classNameAttributeValue);

            }

            if (!annotationAttributeValueList.isEmpty()) {

                AnnotationMetadata requestWrapper = new AnnotationMetadataBuilder(
                        new JavaType("javax.xml.ws.RequestWrapper"),
                        annotationAttributeValueList).build();

                // Add to AspectJ.
                builder
                        .addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                                methodMetadata, requestWrapper));
            }

        }

        // javax.jws.WebResult
        // Check result value
        StringAttributeValue resutlNameAttributeValue = (StringAttributeValue) methodAnnotation
                .getAttribute(new JavaSymbolName("resultName"));

        ClassAttributeValue resultTypeAttributeValue = (ClassAttributeValue) methodAnnotation
                .getAttribute(new JavaSymbolName("webResultType"));

        if ((resutlNameAttributeValue != null && !resutlNameAttributeValue
                .getValue().contains("void"))
                && (resultTypeAttributeValue != null && !resultTypeAttributeValue
                        .getValue().getFullyQualifiedTypeName().contains(
                                JavaType.VOID_PRIMITIVE
                                        .getFullyQualifiedTypeName()))) {

            annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            StringAttributeValue localNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("name"), resutlNameAttributeValue
                            .getValue());
            annotationAttributeValueList.add(localNameAttributeValue);

            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("resultNamespace"));

            if (tmpAnnotationAttributeValue != null) {

                StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("targetNamespace"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());

                annotationAttributeValueList.add(targetNamespaceAttributeValue);
            }

            // Add these attributes to @GvNIXWebMethod.
            // Parameter webResultHeader.
            BooleanAttributeValue headerAttributeValue = (BooleanAttributeValue) methodAnnotation
                    .getAttribute(new JavaSymbolName("webResultHeader"));

            if (headerAttributeValue == null) {
                headerAttributeValue = new BooleanAttributeValue(
                        new JavaSymbolName("header"), false);
            } else {
                headerAttributeValue = new BooleanAttributeValue(
                        new JavaSymbolName("header"), headerAttributeValue
                                .getValue());
            }
            annotationAttributeValueList.add(headerAttributeValue);

            // Parameter webResultPartName.
            StringAttributeValue partNameAttributeValue = (StringAttributeValue) methodAnnotation
                    .getAttribute(new JavaSymbolName("webResultPartName"));

            if (partNameAttributeValue == null) {
                partNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("partName"), "parameters");
            } else {
                partNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("partName"), partNameAttributeValue
                                .getValue());
            }
            annotationAttributeValueList.add(partNameAttributeValue);

            AnnotationMetadata webResult = new AnnotationMetadataBuilder(
                    new JavaType("javax.jws.WebResult"),
                    annotationAttributeValueList).build();

            // Add to AspectJ.
            builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                    methodMetadata, webResult));

            // javax.xml.ws.ResponseWrapper
            annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperName"));

            if (tmpAnnotationAttributeValue != null) {

                localNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("localName"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());
                annotationAttributeValueList.add(localNameAttributeValue);

            }
            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperNamespace"));

            if (tmpAnnotationAttributeValue != null) {

                StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("targetNamespace"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());
                annotationAttributeValueList.add(targetNamespaceAttributeValue);

            }

            tmpAnnotationAttributeValue = methodAnnotation
                    .getAttribute(new JavaSymbolName("responseWrapperClassName"));

            if (tmpAnnotationAttributeValue != null) {

                StringAttributeValue classNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("className"),
                        ((StringAttributeValue) tmpAnnotationAttributeValue)
                                .getValue());
                annotationAttributeValueList.add(classNameAttributeValue);

            }

            if (!annotationAttributeValueList.isEmpty()) {

                AnnotationMetadata responseWrapper = new AnnotationMetadataBuilder(
                        new JavaType("javax.xml.ws.ResponseWrapper"),
                        annotationAttributeValueList).build();

                // Add to AspectJ.
                builder
                        .addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                                methodMetadata, responseWrapper));
            }

        } else {

            // @Oneway - not require a response from the service.
            AnnotationMetadata oneway = new AnnotationMetadataBuilder(
                    new JavaType("javax.jws.Oneway"),
                    new ArrayList<AnnotationAttributeValue<?>>()).build();

            // Add to AspectJ.
            builder.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                    methodMetadata, oneway));
        }

    }

    /**
     * Update methods without @GvNIXWebMethod annotation with @WebMethod(exclude
     * = true).
     * 
     * @param methodMetadataListToExclude
     *            methods to exclude from Web Service.
     */
    public void updateMethodWithoutGvNIXAnnotation(
            List<MethodMetadata> methodMetadataListToExclude) {

        List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();
        attributes.add(new BooleanAttributeValue(new JavaSymbolName("exclude"),
                true));

        AnnotationMetadata methodAnnotation = new AnnotationMetadataBuilder(
                new JavaType("javax.jws.WebMethod"), attributes).build();

        for (MethodMetadata md : methodMetadataListToExclude) {

            AnnotationMetadata gvNIXWebMethodMethodAnnotation = MemberFindingUtils
                    .getAnnotationOfType(md.getAnnotations(), new JavaType(
                            GvNIXWebMethod.class.getName()));

            if (gvNIXWebMethodMethodAnnotation == null) {
                builder
                        .addMethodAnnotation(new DeclaredMethodAnnotationDetails(
                                md, methodAnnotation));
            }
        }

    }

    public static String getMetadataIdentiferType() {
        return WEB_SERVICE_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(
                WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(
                WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                WEB_SERVICE_TYPE_STRING, javaType, path);
    }

}
