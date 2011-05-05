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

import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WSExportExceptionMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String EXCEPTION_WEB_FAULT_TYPE_STRING = WSExportExceptionMetadata.class
            .getName();

    private static final String EXCEPTION_WEB_FAULT_TYPE = MetadataIdentificationUtils
            .create(EXCEPTION_WEB_FAULT_TYPE_STRING);

    public WSExportExceptionMetadata(String identifier, JavaType aspectName,
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
                        GvNIXWebFault.class.getName()));

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
     * @param annotationMetadata
     *            to retrieve attributes.
     * @return WebFault annotation to define.
     */
    public AnnotationMetadata getTypeAnnotation(
            AnnotationMetadata annotationMetadata) {

        JavaType javaType = new JavaType("javax.xml.ws.WebFault");

        if (isAnnotationIntroduced("javax.xml.ws.WebFault")) {

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

        return MemberFindingUtils.getDeclaredTypeAnnotation(
                governorTypeDetails, javaType);

    }

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation
     *            to be check if exists.
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroduced(String annotation) {
        JavaType javaType = new JavaType(annotation);
        AnnotationMetadata result = MemberFindingUtils
                .getDeclaredTypeAnnotation(governorTypeDetails, javaType);

        return result == null;
    }

    public static String getMetadataIdentiferType() {
        return EXCEPTION_WEB_FAULT_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(
                EXCEPTION_WEB_FAULT_TYPE_STRING, metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                EXCEPTION_WEB_FAULT_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(
                EXCEPTION_WEB_FAULT_TYPE_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                EXCEPTION_WEB_FAULT_TYPE_STRING, javaType, path);
    }

}
