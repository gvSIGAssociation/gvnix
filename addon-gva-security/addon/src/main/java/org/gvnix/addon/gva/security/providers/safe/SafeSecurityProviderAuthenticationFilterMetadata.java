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
package org.gvnix.addon.gva.security.providers.safe;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
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
 * ITD generator for {@link GvNIXSafeAuthenticationFilter} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class SafeSecurityProviderAuthenticationFilterMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderAuthenticationFilterMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName ATTEMPT_AUTHENTICATION_METHOD = new JavaSymbolName(
            "attemptAuthentication");

    private static final JavaSymbolName OBTAIN_TOKEN_METHOD = new JavaSymbolName(
            "obtainToken");

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    public SafeSecurityProviderAuthenticationFilterMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding Fields
        builder.addField(getField("tokenParameter", "\"j_token\"",
                JavaType.STRING, Modifier.PRIVATE));

        builder.addField(getField("postOnly", "true",
                JavaType.BOOLEAN_PRIMITIVE, Modifier.PRIVATE));

        // Creating getters and setters
        builder.addMethod(getGetterMethod("tokenParameter", JavaType.STRING));
        builder.addMethod(getSetterMethod("tokenParameter", JavaType.STRING));

        // Adding necessary methods
        builder.addMethod(getAttemptAuthentication());
        builder.addMethod(getObtainToken());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets all getters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getGetterMethod(String propertyName,
            JavaType returnType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        String prefix = returnType == JavaType.BOOLEAN_PRIMITIVE ? "is" : "get";
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                prefix.concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName, returnType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets all setters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getSetterMethod(String propertyName,
            JavaType parameterType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(parameterType);

        // Check if a method with the same signature already exists in the
        // target type
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "set".concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(propertyName));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for all getters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("return this.".concat(propertyName)
                .concat(";"));

    }

    /**
     * Builds body method for all setters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildSetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("this.".concat(propertyName).concat(" = ")
                .concat(propertyName).concat(";"));

    }

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getField(String name, String value,
            JavaType javaType, int modifier) {
        JavaSymbolName curName = new JavaSymbolName(name);
        String initializer = value;
        FieldMetadata field = getOrCreateField(curName, javaType, initializer,
                modifier, null);
        return field;
    }

    /**
     * Gets or creates a field based on parameters.<br>
     * First try to get a suitable field (by name and type). If not found create
     * a new one (adding a counter to name if it's needed)
     * 
     * @param fielName
     * @param fieldType
     * @param initializer (String representation)
     * @param modifier See {@link Modifier}
     * @param annotations optional (can be null)
     * @return
     */
    private FieldMetadata getOrCreateField(JavaSymbolName fielName,
            JavaType fieldType, String initializer, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = fielName;

        // Check if field exist
        FieldMetadata currentField = governorTypeDetails
                .getDeclaredField(curName);
        if (currentField != null) {
            if (!currentField.getFieldType().equals(fieldType)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = curName;
                int i = 1;
                while (governorTypeDetails.getDeclaredField(newName) != null) {
                    newName = new JavaSymbolName(curName.getSymbolName()
                            .concat(StringUtils.repeat('_', i)));
                    i++;
                }
                curName = newName;
            }
        }
        if (currentField == null) {
            // create field
            if (annotations == null) {
                annotations = new ArrayList<AnnotationMetadataBuilder>(0);
            }
            // Using the FieldMetadataBuilder to create the field
            // definition.
            final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                    getId(), modifier, annotations, curName, // Field
                    fieldType); // Field type
            fieldBuilder.setFieldInitializer(initializer);
            currentField = fieldBuilder.build(); // Build and return a
            // FieldMetadata
            // instance
        }
        return currentField;

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
     * Gets <code>getAttemptAuthentication</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getAttemptAuthentication() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest")));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "javax.servlet.http.HttpServletResponse")));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(
                ATTEMPT_AUTHENTICATION_METHOD, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder overrideAnnotation = new AnnotationMetadataBuilder();
        overrideAnnotation
                .setAnnotationType(new JavaType("java.lang.Override"));

        annotations.add(overrideAnnotation);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(new JavaType(
                "org.springframework.security.core.AuthenticationException"));

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("request"));
        parameterNames.add(new JavaSymbolName("response"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildAttemptAuthenticationMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(),
                Modifier.PUBLIC,
                ATTEMPT_AUTHENTICATION_METHOD,
                new JavaType("org.springframework.security.core.Authentication"),
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    public void buildAttemptAuthenticationMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // if (postOnly && !request.getMethod().equals("POST")) {
        bodyBuilder
                .appendFormalLine("if (postOnly && !request.getMethod().equals(\"POST\")) {");
        bodyBuilder.indent();

        // throw new
        // AuthenticationServiceException("Authentication method not supported: "
        // + request.getMethod());
        bodyBuilder
                .appendFormalLine(String
                        .format("throw new %s(\"Authentication method not supported: \" + request.getMethod());",
                                helper.getFinalTypeName(new JavaType(
                                        "org.springframework.security.authentication.AuthenticationServiceException"))));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // String username = obtainUsername(request);
        bodyBuilder
                .appendFormalLine("String username = obtainUsername(request);");
        // String password = obtainPassword(request);
        bodyBuilder
                .appendFormalLine("String password = obtainPassword(request);");
        // String token = obtainToken(request);
        bodyBuilder.appendFormalLine("String token = obtainToken(request);");

        // if (username == null) {
        bodyBuilder.appendFormalLine("if (username == null) {");
        bodyBuilder.indent();

        // username = "";
        bodyBuilder.appendFormalLine("username = \"\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if (password == null) {
        bodyBuilder.appendFormalLine(" if (password == null) {");
        bodyBuilder.indent();

        // password = "";
        bodyBuilder.appendFormalLine("password = \"\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if (token == null){
        bodyBuilder.appendFormalLine(" if (token == null){");
        bodyBuilder.indent();

        // token = "";
        bodyBuilder.appendFormalLine("token = \"\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // username = username.trim();
        bodyBuilder.appendFormalLine("username = username.trim();");
        // String userToken = username.concat("::").concat(token);
        bodyBuilder
                .appendFormalLine("String userToken = username.concat(\"::\").concat(token);");

        // UsernamePasswordAuthenticationToken authRequest = new
        // UsernamePasswordAuthenticationToken(userToken, password);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s authRequest = new UsernamePasswordAuthenticationToken(userToken, password);",
                                helper.getFinalTypeName(new JavaType(
                                        "org.springframework.security.authentication.UsernamePasswordAuthenticationToken"))));
        bodyBuilder
                .appendFormalLine("// Allow subclasses to set the \"details\" property");

        // setDetails(request, authRequest);
        bodyBuilder.appendFormalLine("setDetails(request, authRequest);");

        // return this.getAuthenticationManager().authenticate(authRequest);
        bodyBuilder
                .appendFormalLine("return this.getAuthenticationManager().authenticate(authRequest);");

    }

    /**
     * Gets <code>getObtainToken</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getObtainToken() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest")));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(OBTAIN_TOKEN_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("request"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildObtainTokenMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, OBTAIN_TOKEN_METHOD, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    public void buildObtainTokenMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // return request.getParameter(tokenParameter);
        bodyBuilder
                .appendFormalLine("return request.getParameter(tokenParameter);");
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
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

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }
}
