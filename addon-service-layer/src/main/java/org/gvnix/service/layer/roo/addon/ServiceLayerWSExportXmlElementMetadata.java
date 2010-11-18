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
import java.util.logging.Logger;

import org.gvnix.service.layer.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXXmlElementField;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * <p>
 * gvNix Xml Element Marsharlling generation.
 * </p>
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class ServiceLayerWSExportXmlElementMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final Set<String> notAllowedIntefaceCollectionTypes = new HashSet<String>();

    static {
        notAllowedIntefaceCollectionTypes.add(Map.class.getName());
    }

    private static final String XML_ELEMENT_STRING = ServiceLayerWSExportXmlElementMetadata.class
            .getName();
    private static final String XML_ELEMENT_TYPE = MetadataIdentificationUtils
            .create(XML_ELEMENT_STRING);

    private static Logger logger = Logger
            .getLogger(ServiceLayerWSExportXmlElementMetadata.class.getName());

    public ServiceLayerWSExportXmlElementMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            EntityMetadata entityMetadata,
            List<FieldMetadata> fieldMetadataElementList,
            List<FieldMetadata> fieldmetadataTransientList) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        if (!isValid()) {
            return;
        }

        // Create the metadata.
        AnnotationMetadata gvNIXXmlElementAnnotationMetadata = MemberFindingUtils
                .getTypeAnnotation(governorTypeDetails, new JavaType(
                        GvNIXXmlElement.class.getName()));

        if (gvNIXXmlElementAnnotationMetadata != null) {

            // Type annotations.
            List<AnnotationMetadata> annotationTypeList = getXmlElementTypeAnnotation(
                    gvNIXXmlElementAnnotationMetadata, fieldMetadataElementList);

            for (AnnotationMetadata annotationMetadata : annotationTypeList) {
                builder.addTypeAnnotation(annotationMetadata);
            }

            // If is Java Enum type
            if (!governorTypeDetails.getPhysicalTypeCategory().equals(
                    PhysicalTypeCategory.ENUMERATION)) {

                // Declared XmlTransient field annotations.
                List<DeclaredFieldAnnotationDetails> declaredFieldXmlTransientFieldList = getXmlTransientFieldAnnotations(fieldmetadataTransientList);

                for (DeclaredFieldAnnotationDetails declaredFieldAnnotationDetails : declaredFieldXmlTransientFieldList) {
                    builder.addFieldAnnotation(declaredFieldAnnotationDetails);
                }

                // Declared XmlElement field annotations
                List<DeclaredFieldAnnotationDetails> declaredFieldXmlElementFieldList = getXmlElementFieldAnnotations(fieldMetadataElementList);
                for (DeclaredFieldAnnotationDetails declaredFieldAnnotationDetails : declaredFieldXmlElementFieldList) {
                    builder.addFieldAnnotation(declaredFieldAnnotationDetails);
                }

            }
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Converts {@link FieldMetadata} {@link List} to
     * {@link DeclaredFieldAnnotationDetails} {@link List} using @XmlTransient
     * annotation.
     * 
     * @param fieldTransientList
     *            list to convert.
     * @return All the annotated @XmlTransient fields (never null, but may be
     *         empty).
     */
    public List<DeclaredFieldAnnotationDetails> getXmlTransientFieldAnnotations(
            List<FieldMetadata> fieldTransientList) {

        List<DeclaredFieldAnnotationDetails> annotationXmlTransientFieldList = new ArrayList<DeclaredFieldAnnotationDetails>();

        // Void list, annotation doesn't need attribute values.
        List<AnnotationAttributeValue<?>> attributeValueList = new ArrayList<AnnotationAttributeValue<?>>();
        // Creates the annotation.
        AnnotationMetadata xmlTransientAnnotation = new DefaultAnnotationMetadata(
                new JavaType("javax.xml.bind.annotation.XmlTransient"),
                attributeValueList);

        DeclaredFieldAnnotationDetails declaredFieldAnnotationDetails;

        for (FieldMetadata fieldMetadata : fieldTransientList) {

            declaredFieldAnnotationDetails = new DeclaredFieldAnnotationDetails(
                    fieldMetadata, xmlTransientAnnotation);
            annotationXmlTransientFieldList.add(declaredFieldAnnotationDetails);
        }

        return annotationXmlTransientFieldList;
    }

    /**
     * Converts {@link FieldMetadata} {@link List} to
     * {@link DeclaredFieldAnnotationDetails} {@link List} using @XmlElement
     * annotation.
     * 
     * @param fieldMetadataElementList
     *            list to convert.
     * @return All the annotated @XmlElement fields (never null, but may be
     *         empty).
     */
    public List<DeclaredFieldAnnotationDetails> getXmlElementFieldAnnotations(
            List<FieldMetadata> fieldMetadataElementList) {

        List<DeclaredFieldAnnotationDetails> annotationXmlElementFieldList = new ArrayList<DeclaredFieldAnnotationDetails>();

        // Void list, annotation doesn't need attribute values.
        List<AnnotationAttributeValue<?>> attributeValueList;
        AnnotationMetadata xmlTransientAnnotation;
        StringAttributeValue nameStringAttributeValue;

        DeclaredFieldAnnotationDetails declaredFieldAnnotationDetails;

        AnnotationMetadata gVNIXxmlElementFieldAnnotation;
        for (FieldMetadata fieldMetadata : fieldMetadataElementList) {

            gVNIXxmlElementFieldAnnotation = MemberFindingUtils
            .getAnnotationOfType(fieldMetadata.getAnnotations(), new JavaType(
                    GvNIXXmlElementField.class.getName()));

            attributeValueList = new ArrayList<AnnotationAttributeValue<?>>();
            
            if (gVNIXxmlElementFieldAnnotation != null) {

                List<JavaSymbolName> annotationAttributeNames = gVNIXxmlElementFieldAnnotation.getAttributeNames();
                
                AnnotationAttributeValue<?> tmpAnnotationAttributeValue;

                for (JavaSymbolName javaSymbolName : annotationAttributeNames) {
                    tmpAnnotationAttributeValue = gVNIXxmlElementFieldAnnotation.getAttribute(javaSymbolName);
                    attributeValueList.add(tmpAnnotationAttributeValue);
                }
                
            } else {

                nameStringAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("name"), fieldMetadata
                                .getFieldName().getSymbolName());
                attributeValueList.add(nameStringAttributeValue);

            }

            // Creates the annotation.
            xmlTransientAnnotation = new DefaultAnnotationMetadata(
                    new JavaType("javax.xml.bind.annotation.XmlElement"),
                    attributeValueList);
            declaredFieldAnnotationDetails = new DeclaredFieldAnnotationDetails(
                    fieldMetadata, xmlTransientAnnotation);
            annotationXmlElementFieldList.add(declaredFieldAnnotationDetails);
        }

        return annotationXmlElementFieldList;
    }

    /**
     * Method to create Type Annotations.
     * 
     * @param gvNIXXmlElementAnnotationMetadata
     *            with info to build AspectJ annotations.
     * @param fieldElementList
     *            fields order to be published.
     * @return {@link List} of {@link AnnotationMetadata} to build the ITD.
     */
    public List<AnnotationMetadata> getXmlElementTypeAnnotation(
            AnnotationMetadata gvNIXXmlElementAnnotationMetadata,
            List<FieldMetadata> fieldElementList) {

        boolean enumerationClass = governorTypeDetails.getPhysicalTypeCategory().equals(PhysicalTypeCategory.ENUMERATION);

        AnnotationAttributeValue<?> tmpAttribute;

        // Boolean exported attribute.
        BooleanAttributeValue exportedBooleanAttributeValue = (BooleanAttributeValue) gvNIXXmlElementAnnotationMetadata
                .getAttribute(new JavaSymbolName("exported"));

        // Annotation list.
        List<AnnotationMetadata> annotationTypeList = new ArrayList<AnnotationMetadata>();

        // @XmlRootElement
        List<AnnotationAttributeValue<?>> xmlRootElementAnnotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        JavaType xmlRootElement = new JavaType(
                "javax.xml.bind.annotation.XmlRootElement");

        tmpAttribute = gvNIXXmlElementAnnotationMetadata
                .getAttribute(new JavaSymbolName("name"));
        
        if (tmpAttribute != null) {
            StringAttributeValue nameAttributeValue = (StringAttributeValue) tmpAttribute;

            xmlRootElementAnnotationAttributeValueList.add(nameAttributeValue);

            StringAttributeValue namespaceAttributeValue = (StringAttributeValue) gvNIXXmlElementAnnotationMetadata
                    .getAttribute(new JavaSymbolName("namespace"));
            xmlRootElementAnnotationAttributeValueList
                    .add(namespaceAttributeValue);

            AnnotationMetadata xmlRootElementAnnotation = new DefaultAnnotationMetadata(
                    xmlRootElement, xmlRootElementAnnotationAttributeValueList);

            annotationTypeList.add(xmlRootElementAnnotation);
        }

        // @XmlType
        List<AnnotationAttributeValue<?>> xmlTypeAnnotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        JavaType xmlType = new JavaType("javax.xml.bind.annotation.XmlType");

        List<StringAttributeValue> propOrderList = new ArrayList<StringAttributeValue>();

        StringAttributeValue propOrderAttributeValue;

        for (FieldMetadata fieldMetadata : fieldElementList) {
            propOrderAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("ignored"), fieldMetadata.getFieldName()
                            .getSymbolName());
            propOrderList.add(propOrderAttributeValue);
        }

        tmpAttribute = gvNIXXmlElementAnnotationMetadata
                .getAttribute(new JavaSymbolName("xmlTypeName"));

        StringAttributeValue xmlTypeNameAttributeValue;
        if (tmpAttribute != null) {
            xmlTypeNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("name"), ((StringAttributeValue) tmpAttribute).getValue());
            xmlTypeAnnotationAttributeValueList.add(xmlTypeNameAttributeValue);
        } else {
            xmlTypeNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("name"), "");
            xmlTypeAnnotationAttributeValueList.add(xmlTypeNameAttributeValue);
        }

        ArrayAttributeValue<StringAttributeValue> propOrderAttributeList = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("propOrder"), propOrderList);

        xmlTypeAnnotationAttributeValueList.add(propOrderAttributeList);

        StringAttributeValue xmlTypeNamespaceAttributeValue = (StringAttributeValue) gvNIXXmlElementAnnotationMetadata
                .getAttribute(new JavaSymbolName("namespace"));
        xmlTypeAnnotationAttributeValueList.add(xmlTypeNamespaceAttributeValue);

        AnnotationMetadata xmlTypeRootElementAnnotation = new DefaultAnnotationMetadata(
                xmlType, xmlTypeAnnotationAttributeValueList);

        annotationTypeList.add(xmlTypeRootElementAnnotation);

        // @XmlAccessorType
        List<AnnotationAttributeValue<?>> xmlAccessorTypeAnnotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        JavaType xmlAccessorType = new JavaType(
                "javax.xml.bind.annotation.XmlAccessorType");

        EnumDetails xmlAccessTypeEnumDetails = new EnumDetails(new JavaType(
                "javax.xml.bind.annotation.XmlAccessType"), new JavaSymbolName(
                "FIELD"));

        EnumAttributeValue xmlAccessTypeAttributeValue = new EnumAttributeValue(
                new JavaSymbolName("value"), xmlAccessTypeEnumDetails);

        xmlAccessorTypeAnnotationAttributeValueList
                .add(xmlAccessTypeAttributeValue);

        if (!enumerationClass) {
            AnnotationMetadata xmlAccessorTypeAnnotation = new DefaultAnnotationMetadata(
                    xmlAccessorType,
                    xmlAccessorTypeAnnotationAttributeValueList);

            annotationTypeList.add(xmlAccessorTypeAnnotation);
        }
        
        if (enumerationClass) {

            // @XmlEnum
            List<AnnotationAttributeValue<?>> xmlEnumAnnotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            JavaType xmlEnum = new JavaType("javax.xml.bind.annotation.XmlEnum");

            AnnotationMetadata xmlEnumAnnotation = new DefaultAnnotationMetadata(
                    xmlEnum, xmlEnumAnnotationAttributeValueList);

            annotationTypeList.add(xmlEnumAnnotation);

        }

        return annotationTypeList;

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

    public static String getMetadataIdentiferType() {
        return XML_ELEMENT_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(XML_ELEMENT_STRING,
                metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                XML_ELEMENT_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(XML_ELEMENT_STRING,
                metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                XML_ELEMENT_STRING, javaType, path);
    }

}
