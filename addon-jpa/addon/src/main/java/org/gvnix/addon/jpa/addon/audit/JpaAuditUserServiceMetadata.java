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
package org.gvnix.addon.jpa.addon.audit;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXJpaAuditUserService} annotation.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
public class JpaAuditUserServiceMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaAuditUserServiceMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    // Method names
    public static final JavaSymbolName GET_USER_METHOD = new JavaSymbolName(
            "getUser");

    // Refered types
    private static final JavaType SEC_AUTHENTICATION = new JavaType(
            "org.springframework.security.core.Authentication");
    private static final JavaType SEC_SECURITY_CONTEXT_HOLDER = new JavaType(
            "org.springframework.security.core.context.SecurityContextHolder");

    private static final JavaType ILLEGAL_STATUS_EXCEPTION = new JavaType(
            IllegalStateException.class);

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
    private final JavaType userType;
    private final JpaAuditUserServiceAnnotationValues annotationValues;
    private final boolean isSpringSecurityInstalled;
    private final boolean isUserDetails;
    private final boolean isUserEntity;
    private final boolean usePatternForDate;
    private final String dateTimePattern;
    private final String dateTimeStyle;

    public JpaAuditUserServiceMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaAuditUserServiceAnnotationValues annotationValues,
            JavaType userType, boolean isSpringSecurityInstalled,
            boolean isUserDetails, boolean userTypeIsEntity,
            boolean usePattern, String dateTimePattern, String dateTimeStyle) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.annotationValues = annotationValues;
        this.userType = userType;
        this.isSpringSecurityInstalled = isSpringSecurityInstalled;
        this.isUserDetails = isUserDetails;
        this.isUserEntity = userTypeIsEntity;
        this.usePatternForDate = usePattern;
        this.dateTimePattern = dateTimePattern;
        this.dateTimeStyle = dateTimeStyle;

        // Add listener methods (only in non-abstract entities)
        builder.addMethod(getUserMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * @return the getUserName method definition
     */
    private MethodMetadata getUserMethod() {
        // method name
        JavaSymbolName methodName = GET_USER_METHOD;

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
        buildGetUserMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                userType, parameterTypes, parameterNames, bodyBuilder);
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
    private void buildGetUserMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
        if (!isSpringSecurityInstalled) {
            bodyBuilder.appendFormalLine("// TODO identify user");
            bodyBuilder.appendFormalLine("return null;");
        }
        else {
            // Authentication auth =
            // SecurityContextHolder.getContext().getAuthentication();
            bodyBuilder.appendFormalLine(String.format(
                    "%s auth = %s.getContext().getAuthentication();",
                    getFinalTypeName(SEC_AUTHENTICATION),
                    getFinalTypeName(SEC_SECURITY_CONTEXT_HOLDER)));

            // if (auth == null || !auth.isAuthenticated()) {
            bodyBuilder
                    .appendFormalLine("if (auth == null || !auth.isAuthenticated()) {");
            bodyBuilder.indent();

            // return null;
            bodyBuilder.appendFormalLine("return null;");

            // }
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            if (JavaType.STRING.equals(userType)) {

                // return auth.getName();
                bodyBuilder.appendFormalLine("return auth.getName();");
            }
            else if (isUserDetails) {
                // return (UserDetails)auth.getName();
                bodyBuilder.appendFormalLine(String.format("return (%s)auth;",
                        helper.getFinalTypeName(userType)));
            }
            else {
                bodyBuilder.appendFormalLine("// return ????;");
                bodyBuilder.appendFormalLine(String.format(
                        "throw new %s(\"Missing implementation here!!!!\");",
                        helper.getFinalTypeName(ILLEGAL_STATUS_EXCEPTION)));
            }
        }

    }

    /**
     * @return name of getUser() method
     */
    public JavaSymbolName getUserMethodName() {
        return GET_USER_METHOD;
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
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

    /**
     * @return annotation values
     */
    public JpaAuditUserServiceAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    /**
     * @return type to use for audit user fields
     */
    public JavaType userType() {
        return userType;
    }

    /**
     * @return the isUserDetails
     */
    public boolean isUserTypeSpringSecUserDetails() {
        return isUserDetails;
    }

    /**
     * @return the isUserEntity
     */
    public boolean isUserTypeEntity() {
        return isUserEntity;
    }

    /**
     * @return the usePatternForDate
     */
    public boolean usePatternForTimestamp() {
        return usePatternForDate;
    }

    /**
     * @return the dateTimePattern
     */
    public String getPatternForTimestamp() {
        return dateTimePattern;
    }

    /**
     * @return the dateTimeStyle
     */
    public String getTimestampStyle() {
        return dateTimeStyle;
    }

}
