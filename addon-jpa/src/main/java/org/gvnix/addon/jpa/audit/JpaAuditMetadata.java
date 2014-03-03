/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.audit;

import static org.springframework.roo.model.SpringJavaType.DATE_TIME_FORMAT;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.gvnix.support.ItdBuilderHelper.GET_FIELD_EXISTS_ACTION;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXJpaAudit} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class JpaAuditMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaAuditMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName CREATION_FIELD = new JavaSymbolName(
            "auditCreation");
    private static final JavaSymbolName CREATED_BY_FIELD = new JavaSymbolName(
            "auditCreatedBy");
    private static final JavaSymbolName LAST_UPDATE_FIELD = new JavaSymbolName(
            "auditLastUpdate");
    private static final JavaSymbolName LAST_UPDATED_BY_FIELD = new JavaSymbolName(
            "auditLastUpdatedBy");

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    public JpaAuditMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        builder.addField(getFieldAuditCreation());
        builder.addMethod(getGetAuditCreationMethod());
        builder.addMethod(getSetAuditCreationMethod());

        builder.addField(getFieldAuditCreatedBy());
        builder.addMethod(getGetAuditCreatedByMethod());
        builder.addMethod(getSetAuditCreatedByMethod());

        builder.addField(getFieldAuditLastUpdate());
        builder.addMethod(getGetAuditLastUpdateMethod());
        builder.addMethod(getSetAuditLastUpdateMethod());

        builder.addField(getFieldAuditLastUpdatedBy());
        builder.addMethod(getGetAuditLastUpdatedByMethod());
        builder.addMethod(getSetAuditLastUpdatedByMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * @return auditLastUpdatedBy field
     */
    private FieldMetadata getFieldAuditLastUpdatedBy() {
        return helper.getField(LAST_UPDATED_BY_FIELD, Modifier.PRIVATE,
                JavaType.STRING, null, GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return auditLastUpdate field
     */
    private FieldMetadata getFieldAuditLastUpdate() {
        return helper.getField(LAST_UPDATE_FIELD, Modifier.PRIVATE,
                JdkJavaType.CALENDAR, getDateFormatAnnotations(),
                GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return auditCreatedBy field
     */
    private FieldMetadata getFieldAuditCreatedBy() {
        return helper.getField(CREATED_BY_FIELD, Modifier.PRIVATE,
                JavaType.STRING, null, GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return auditCreation field
     */
    public FieldMetadata getFieldAuditCreation() {
        return helper.getField(CREATION_FIELD, Modifier.PRIVATE,
                JdkJavaType.CALENDAR, getDateFormatAnnotations(),
                GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return annotation list for time-stamp fields
     */
    private List<AnnotationMetadataBuilder> getDateFormatAnnotations() {
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);
        final AnnotationMetadataBuilder dateTimeFormatBuilder = new AnnotationMetadataBuilder(
                DATE_TIME_FORMAT);
        dateTimeFormatBuilder.addStringAttribute("style", "MM");
        annotations.add(dateTimeFormatBuilder);
        return annotations;
    }

    /**
     * Gets <code>getAuditCreation</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditCreationMethod() {
        return helper.getGetterMethod(CREATION_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>setAuditCreation</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditCreationMethod() {
        return helper.getSetterMethod(CREATION_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>getAuditCreatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditCreatedByMethod() {
        return helper.getGetterMethod(CREATED_BY_FIELD, JavaType.STRING, null);
    }

    /**
     * Gets <code>setAuditCreatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditCreatedByMethod() {
        return helper.getSetterMethod(CREATED_BY_FIELD, JavaType.STRING, null);
    }

    /**
     * Gets <code>getAuditLastUpdate</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditLastUpdateMethod() {
        return helper.getGetterMethod(LAST_UPDATE_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>setAuditLastUpdate</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditLastUpdateMethod() {
        return helper.getSetterMethod(LAST_UPDATE_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>getAuditLastUpdatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditLastUpdatedByMethod() {
        return helper.getGetterMethod(LAST_UPDATED_BY_FIELD, JavaType.STRING,
                null);
    }

    /**
     * Gets <code>setAuditLastUpdatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditLastUpdatedByMethod() {
        return helper.getSetterMethod(LAST_UPDATED_BY_FIELD, JavaType.STRING,
                null);
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * @return name of getCreation method
     */
    public JavaSymbolName getGetCreationMethodName() {
        return helper.getGetterMethodNameForField(CREATION_FIELD);
    }

    /**
     * @return name of getCreatedBy method
     */
    public JavaSymbolName getGetCreatedByMethodName() {
        return helper.getGetterMethodNameForField(CREATED_BY_FIELD);
    }

    /**
     * @return name of getLastUpdate method
     */
    public JavaSymbolName getGetLastUpdatedMethodName() {
        return helper.getGetterMethodNameForField(LAST_UPDATE_FIELD);
    }

    /**
     * @return name of getLastUpdatedBy method
     */
    public JavaSymbolName getGetLastUpdateByMethodName() {
        return helper.getGetterMethodNameForField(LAST_UPDATED_BY_FIELD);
    }

    /**
     * @return name of setCreation method
     */
    public JavaSymbolName getSetCreationMethodName() {
        return helper.getSetterMethodNameForField(CREATION_FIELD);
    }

    /**
     * @return name of setCreatedBy method
     */
    public JavaSymbolName getSetCreatedByMethodName() {
        return helper.getSetterMethodNameForField(CREATED_BY_FIELD);
    }

    /**
     * @return name of setLastUpdate method
     */
    public JavaSymbolName getSetLastUpdatedMethodName() {
        return helper.getSetterMethodNameForField(LAST_UPDATE_FIELD);
    }

    /**
     * @return name of setLastUpdate method
     */
    public JavaSymbolName getSetLastUpdateByMethodName() {
        return helper.getSetterMethodNameForField(LAST_UPDATED_BY_FIELD);
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    @SuppressWarnings("unused")
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }
}
