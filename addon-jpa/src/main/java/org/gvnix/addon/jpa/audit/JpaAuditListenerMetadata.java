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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.entitylistener.JpaOrmEntityListener;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
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
public class JpaAuditListenerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem implements
        JpaOrmEntityListener {

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaAuditListenerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    // Method names
    private static final JavaSymbolName ON_CREATE_METHOD = new JavaSymbolName(
            "onCreate");
    private static final JavaSymbolName ON_UPDATE_METHOD = new JavaSymbolName(
            "onUpdate");
    private static final JavaSymbolName GET_USER_NAME_METHOD = new JavaSymbolName(
            "getUserName");

    // Refered types
    private static final JavaType JPA_PRE_PRESIST = new JavaType(
            "javax.persistence.PrePersist");
    private static final JavaType JPA_PRE_UPDATE = new JavaType(
            "javax.persistence.PreUpdate");
    private static final JavaType SEC_AUTHENTICATION = new JavaType(
            "org.springframework.security.core.Authentication");
    private static final JavaType SEC_SECURITY_CONTEXT_HOLDER = new JavaType(
            "org.springframework.security.core.context.SecurityContextHolder");

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
    private final ItdBuilderHelper helper;
    private final JavaType entity;
    private final JpaAuditMetadata auditMetadata;
    private final JpaAuditListenerAnnotationValues annotationValues;
    private final JavaSymbolName entityParamName;
    private final boolean isSpringSecurityInstalled;

    public JpaAuditListenerMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaAuditListenerAnnotationValues annotationValues, JavaType entity,
            JpaAuditMetadata auditMetadata, boolean isSpringSecurityInstalled) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.annotationValues = annotationValues;
        this.entity = entity;
        this.entityParamName = new JavaSymbolName(
                StringUtils.uncapitalize(entity.getSimpleTypeName()));
        this.isSpringSecurityInstalled = isSpringSecurityInstalled;

        this.auditMetadata = auditMetadata;

        // Add listener methods
        builder.addMethod(getUserNameMethod());
        builder.addMethod(getOnCreateMethod());
        builder.addMethod(getOnUpdateMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * @return the getUserName method definition
     */
    private MethodMetadata getUserNameMethod() {
        // method name
        JavaSymbolName methodName = GET_USER_NAME_METHOD;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetUserNameMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * Build getUserName method code
     * <p/>
     * Generated code is dependent on Spring Security is configured or not
     * 
     * @param bodyBuilder
     */
    private void buildGetUserNameMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        if (!isSpringSecurityInstalled) {
            bodyBuilder.appendFormalLine("return null;");
        }
        else {
            // Authentication auth =
            // SecurityContextHolder.getContext().getAuthentication();
            bodyBuilder.appendFormalLine(String.format(
                    "%s auth = %s.getContext().getAuthentication();",
                    getFinalTypeName(SEC_AUTHENTICATION),
                    getFinalTypeName(SEC_SECURITY_CONTEXT_HOLDER)));

            // if (auth == null) {
            bodyBuilder.appendFormalLine("if (auth == null) {");
            bodyBuilder.indent();

            // return null;
            bodyBuilder.appendFormalLine("return null;");

            // }
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            // return auth.getName();
            bodyBuilder.appendFormalLine("return auth.getName();");

        }

    }

    /**
     * @return the getOnUpdate method definition
     */
    private MethodMetadata getOnUpdateMethod() {
        // method name
        JavaSymbolName methodName = ON_UPDATE_METHOD;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(entity));

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(JPA_PRE_UPDATE));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(entityParamName);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildOnUpdateMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * Build onUpdate method code
     * 
     * @param bodyBuilder
     */
    private void buildOnUpdateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
        // Calendar now = Calendar.getInstance();
        bodyBuilder.appendFormalLine(String.format(
                "%s now = %s.getInstance();",
                getFinalTypeName(JdkJavaType.CALENDAR),
                getFinalTypeName(JdkJavaType.CALENDAR)));

        // String user = this.getUserName();
        bodyBuilder.appendFormalLine(String.format("String user = %s();",
                GET_USER_NAME_METHOD));

        // visit.setAuditLastUpdate(now);
        bodyBuilder.appendFormalLine(String.format("%s.%s(now);",
                entityParamName, auditMetadata.getSetLastUpdatedMethodName()));

        // visit.setAuditLastUpdatedBy(user);
        bodyBuilder.appendFormalLine(String.format("%s.%s(user);",
                entityParamName, auditMetadata.getSetLastUpdateByMethodName()));
    }

    /**
     * @return the getOnCreate method definition
     */
    private MethodMetadata getOnCreateMethod() {
        // method name
        JavaSymbolName methodName = ON_CREATE_METHOD;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(entity));

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(JPA_PRE_PRESIST));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(entityParamName);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildOnCreateMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * Build onCreate method code
     * 
     * @param bodyBuilder
     */
    private void buildOnCreateMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
        // Calendar now = Calendar.getInstance();
        bodyBuilder.appendFormalLine(String.format(
                "%s now = %s.getInstance();",
                getFinalTypeName(JdkJavaType.CALENDAR),
                getFinalTypeName(JdkJavaType.CALENDAR)));

        // String user = this.getUserName();
        bodyBuilder.appendFormalLine(String.format("String user = %s();",
                GET_USER_NAME_METHOD));

        // visit.setAuditCreation(now);
        bodyBuilder.appendFormalLine(String.format("%s.%s(now);",
                entityParamName, auditMetadata.getSetCreationMethodName()));

        // visit.setAuditCreatedBy(user);
        bodyBuilder.appendFormalLine(String.format("%s.%s(user);",
                entityParamName, auditMetadata.getSetCreatedByMethodName()));

        // visit.setAuditLastUpdate(now);
        bodyBuilder.appendFormalLine(String.format("%s.%s(now);",
                entityParamName, auditMetadata.getSetLastUpdatedMethodName()));

        // visit.setAuditLastUpdatedBy(user);
        bodyBuilder.appendFormalLine(String.format("%s.%s(user);",
                entityParamName, auditMetadata.getSetLastUpdateByMethodName()));
    }

    public JavaSymbolName getGetUserNameMethodName() {
        return GET_USER_NAME_METHOD;
    }

    public JavaSymbolName getOnCreateMethodName() {
        return ON_CREATE_METHOD;
    }

    public JavaSymbolName getOnUpdateMethodName() {
        return ON_UPDATE_METHOD;
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("endity", entity);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

    @Override
    public JavaType getEntityClass() {
        return entity;
    }

    @Override
    public JavaType getListenerClass() {
        return governorTypeDetails.getType();
    }

    public JpaAuditListenerAnnotationValues getAnnotationValues() {
        return annotationValues;
    }
}
