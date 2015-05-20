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

import org.apache.commons.lang3.Validate;
import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public class WSExportExceptionMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String EXCEP_W_FAULT_STR = WSExportExceptionMetadata.class
            .getName();

    private static final String EXCEPTION_WEB_FAULT_TYPE = MetadataIdentificationUtils
            .create(EXCEP_W_FAULT_STR);

    public WSExportExceptionMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        if (!isValid()) {
            return;
        }

        // Create the metadata.
        AnnotationMetadata annotationMetadata = governorTypeDetails
                .getTypeAnnotation(new JavaType(GvNIXWebFault.class.getName()));

        // Add @javax.jws.WebFault annotation to ITD.
        AnnotationMetadata webFaultAnnotationMetadata = getTypeAnnotation(annotationMetadata);
        if (webFaultAnnotationMetadata != null) {
            builder.addAnnotation(webFaultAnnotationMetadata);
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Create @WebFault annotation with @GvNIXWebFault attribute values.
     * 
     * @param annotationMetadata to retrieve attributes.
     * @return WebFault annotation to define.
     */
    public AnnotationMetadata getTypeAnnotation(
            AnnotationMetadata annotationMetadata) {

        JavaType javaType = new JavaType("javax.xml.ws.WebFault");

        // If annotation already exists in Java, not add it in AJ
        if (isAnnotationIntroduced("javax.xml.ws.WebFault")) {
            return null;
        }

        List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

        StringAttributeValue nameAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("name"));

        annotationAttributeValueList.add(nameAttributeValue);

        StringAttributeValue targetNamespaceAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("targetNamespace"));

        annotationAttributeValueList.add(targetNamespaceAttributeValue);

        StringAttributeValue faultBeanAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("faultBean"));

        annotationAttributeValueList.add(faultBeanAttributeValue);

        return new AnnotationMetadataBuilder(javaType,
                annotationAttributeValueList).build();
    }

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation to be check if exists.
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroduced(String annotation) {
        JavaType javaType = new JavaType(annotation);
        AnnotationMetadata result = governorTypeDetails.getAnnotation(javaType);

        return result != null;
    }

    public static String getMetadataIdentiferType() {
        return EXCEPTION_WEB_FAULT_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(EXCEP_W_FAULT_STR,
                metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(EXCEP_W_FAULT_STR,
                metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(EXCEP_W_FAULT_STR,
                metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                EXCEP_W_FAULT_STR, javaType, path);
    }

}
