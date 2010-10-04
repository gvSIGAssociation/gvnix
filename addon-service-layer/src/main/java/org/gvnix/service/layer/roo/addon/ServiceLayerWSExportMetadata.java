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

import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * <p>
 * gvNix Web Service Java Contract generation.
 * </p>
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class ServiceLayerWSExportMetadata extends
	AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String WEB_SERVICE_TYPE_STRING = ServiceLayerWSExportMetadata.class
	    .getName();
    private static final String WEB_SERVICE_TYPE = MetadataIdentificationUtils
	    .create(WEB_SERVICE_TYPE_STRING);

    public ServiceLayerWSExportMetadata(String identifier, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata) {
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

	if (annotationMetadata != null) {

	    // Checks correct annotation definition in class to generate or
	    // delete ITD file.

	    // Add @javax.jws.WebService and @javax.jws.soap.SOAPBinding.
	    builder
		    .addTypeAnnotation(getWebServiceAnnotation(annotationMetadata));

	    builder.addTypeAnnotation(getSoapBindingAnnotation());

	    // TODO: Add method annotations.

	    // TODO: Add @GvNIXWebFault annotation to related exceptions.

	    // TODO: Update RooEntities involved in Annotated Operations with
	    // @GvNIXWebMethod.

	    // Update methods without GvNIXWebMethod annotation with
	    // '@WebMethod(exclude = true)'
	    updateMethodWithoutGvNIXAnnotation();
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
    public AnnotationMetadata getWebServiceAnnotation(AnnotationMetadata annotationMetadata) {

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

	    return new DefaultAnnotationMetadata(javaType,
		    annotationAttributeValueList);
	}

	return MemberFindingUtils.getDeclaredTypeAnnotation(
		governorTypeDetails, javaType);
    }

    /**
     * Adds @javax.jws.soap.SOAPBinding annotation to the type, unless it
     * already exists.
     * 
     * @return the annotation is already exists or will be created, or null if
     *         it will not be created (required)
     */
    public AnnotationMetadata getSoapBindingAnnotation() {
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

	    EnumAttributeValue enumparameterStyleAttributeValue = new EnumAttributeValue(
		    new JavaSymbolName("parameterStyle"),
		    new EnumDetails(new JavaType(
			    "javax.jws.soap.SOAPBinding.ParameterStyle"),
			    new JavaSymbolName("WRAPPED")));

	    annotationAttributeValueList.add(enumparameterStyleAttributeValue);

	    return new DefaultAnnotationMetadata(javaType,
		    annotationAttributeValueList);
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
     * Update methods without @GvNIXWebMethod annotation with @WebMethod(exclude
     * = true).
     */
    public void updateMethodWithoutGvNIXAnnotation() {

	List<MethodMetadata> methodMetadataList = MemberFindingUtils
		.getMethods(governorTypeDetails);

	List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();
	attributes.add(new BooleanAttributeValue(new JavaSymbolName("exclude"),
		true));

	AnnotationMetadata methodAnnotation = new DefaultAnnotationMetadata(
		new JavaType("javax.jws.WebMethod"), attributes);

	List<AnnotationMetadata> methodAnnotationList;

	DefaultAnnotationMetadata defaultAnnotationMetadata = new DefaultAnnotationMetadata(
		new JavaType(
			"org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod"),
		new ArrayList<AnnotationAttributeValue<?>>());

	boolean exclude = true;
	for (MethodMetadata md : methodMetadataList) {

	    methodAnnotationList = md.getAnnotations();

	    if (methodAnnotationList.size() == 0) {

		builder
			.addMethodAnnotation(new DeclaredMethodAnnotationDetails(
				md, methodAnnotation));
	    } else {
		for (AnnotationMetadata annotationMetadata : methodAnnotationList) {

		    if (annotationMetadata.getAnnotationType().equals(
			    defaultAnnotationMetadata.getAnnotationType())) {
			exclude = false;
			break;
		    }

		}

		if (exclude) {
		    builder
			    .addMethodAnnotation(new DeclaredMethodAnnotationDetails(
				    md, methodAnnotation));
		}
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
