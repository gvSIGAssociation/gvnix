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

import java.util.ArrayList;
import java.util.List;

import org.gvnix.service.layer.roo.addon.annotations.GvNIXXmlElement;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

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

    private static final String XML_ELEMENT_STRING = ServiceLayerWSExportXmlElementMetadata.class
	    .getName();
    private static final String XML_ELEMENT_TYPE = MetadataIdentificationUtils
	    .create(XML_ELEMENT_STRING);

    public ServiceLayerWSExportXmlElementMetadata(String identifier, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata) {
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

	    String[] propOrder = { "id", "version" };
	    
	    // TODO Comprobar el uso de AutoPopulationUtils.populate(this,
	    // annotation) para añadir las anotaciones
	    
	    // Type annotations
	    List<AnnotationMetadata> annotationTypeList = getXmlElementTypeAnnotation(
		    gvNIXXmlElementAnnotationMetadata, propOrder);
	    
	    for (AnnotationMetadata annotationMetadata : annotationTypeList) {
		    builder.addTypeAnnotation(annotationMetadata);
	    }
	    // Field Annotations

	}

	// Create a representation of the desired output ITD
	itdTypeDetails = builder.build();

    }

    /**
     * Method to create Type Annotations.
     * 
     * @param gvNIXXmlElementAnnotationMetadata
     *            with info to build AspectJ annotations.
     * @param propOrder
     *            properties to be annotated.
     * @return {@link List} of {@link AnnotationMetadata} to build the ITD.
     */
    public List<AnnotationMetadata> getXmlElementTypeAnnotation(
	    AnnotationMetadata gvNIXXmlElementAnnotationMetadata,
	    String[] propOrder) {

	// Annotation list.
	List<AnnotationMetadata> annotationTypeList = new ArrayList<AnnotationMetadata>();

	// @XmlRootElement
	List<AnnotationAttributeValue<?>> xmlRootElementAnnotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

	JavaType xmlRootElement = new JavaType(
		"javax.xml.bind.annotation.XmlRootElement");
	
	StringAttributeValue nameAttributeValue = (StringAttributeValue) gvNIXXmlElementAnnotationMetadata
		.getAttribute(new JavaSymbolName("name"));
	xmlRootElementAnnotationAttributeValueList.add(nameAttributeValue);

	StringAttributeValue namespaceAttributeValue = (StringAttributeValue) gvNIXXmlElementAnnotationMetadata
		.getAttribute(new JavaSymbolName("namespace"));
	xmlRootElementAnnotationAttributeValueList.add(namespaceAttributeValue);

	AnnotationMetadata xmlRootElementAnnotation = new DefaultAnnotationMetadata(
		xmlRootElement, xmlRootElementAnnotationAttributeValueList);

	annotationTypeList.add(xmlRootElementAnnotation);

	// @XmlType
	List<AnnotationAttributeValue<?>> xmlTypeAnnotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

	JavaType xmlType = new JavaType("javax.xml.bind.annotation.XmlType");

	List<StringAttributeValue> propOrderList = new ArrayList<StringAttributeValue>();

	StringAttributeValue propOrderAttributeValue;
	
	for (int i=0; i<propOrder.length ; i++) {
	    propOrderAttributeValue = new StringAttributeValue(
		    new JavaSymbolName("ignored"), propOrder[i]);
	    propOrderList.add(propOrderAttributeValue);
	}

	ArrayAttributeValue<StringAttributeValue> propOrderAttributeList = new ArrayAttributeValue<StringAttributeValue>(
		new JavaSymbolName(
		"propOrder"), propOrderList);

	xmlTypeAnnotationAttributeValueList.add(propOrderAttributeList);

	StringAttributeValue xmlTypeNameAttributeValue = (StringAttributeValue) gvNIXXmlElementAnnotationMetadata
		.getAttribute(new JavaSymbolName("name"));
	xmlTypeAnnotationAttributeValueList.add(xmlTypeNameAttributeValue);

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
		new JavaSymbolName("value"),
		xmlAccessTypeEnumDetails);

	xmlAccessorTypeAnnotationAttributeValueList
		.add(xmlAccessTypeAttributeValue);
	
	AnnotationMetadata xmlAccessorTypeAnnotation = new DefaultAnnotationMetadata(xmlAccessorType, xmlAccessorTypeAnnotationAttributeValueList);

	annotationTypeList.add(xmlAccessorTypeAnnotation);

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
	return PhysicalTypeIdentifierNamingUtils.isValid(
		XML_ELEMENT_STRING, metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getJavaType(
		XML_ELEMENT_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getPath(
		XML_ELEMENT_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
	return PhysicalTypeIdentifierNamingUtils.createIdentifier(
		XML_ELEMENT_STRING, javaType, path);
    }

}
