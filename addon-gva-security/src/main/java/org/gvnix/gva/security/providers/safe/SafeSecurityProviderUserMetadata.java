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
package org.gvnix.gva.security.providers.safe;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXPasswordHandlerSAFE} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class SafeSecurityProviderUserMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaType JAVA_TYPE_BOOLEAN = JavaType.BOOLEAN_PRIMITIVE;
    private static final JavaType JAVA_TYPE_STRING = new JavaType(
            "java.lang.String");
    private static final JavaType GRANTED_AUTHORITY = new JavaType(
            "java.util.Set", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType(
                    "org.springframework.security.core.GrantedAuthority")));
    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderUserMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    public SafeSecurityProviderUserMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        /*Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");*/

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        // Adding Fields
        builder.addField(getField("serialVersionUID", "5767016615242591655L",
                JavaType.LONG_PRIMITIVE, Modifier.PRIVATE + Modifier.STATIC
                        + Modifier.FINAL));
        // User Details
        builder.addField(getField("username", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("password", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("accountNonExpired", null, JAVA_TYPE_BOOLEAN,
                Modifier.PRIVATE));
        builder.addField(getField("accountNonLocked", null, JAVA_TYPE_BOOLEAN,
                Modifier.PRIVATE));
        builder.addField(getField("credentialsNonExpired", null,
                JAVA_TYPE_BOOLEAN, Modifier.PRIVATE));
        builder.addField(getField("enabled", null, JAVA_TYPE_BOOLEAN,
                Modifier.PRIVATE));
        builder.addField(getField("authorities", null, GRANTED_AUTHORITY,
                Modifier.PRIVATE));
        // SAFE USER DATA
        builder.addField(getField("nombre", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("email", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("apellido1", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("apellido2", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("cif", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("habilitado", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("idHDFI", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("iusserDN", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("nif", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("oid", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("razonSocial", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("representante", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("serialNumber", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("subjectDN", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("tipoAut", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("tipoCertificado", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        // Creating methods
        builder.addMethod(getGetterMethod("username", JavaType.STRING));
        builder.addMethod(getSetterMethod("username", JavaType.STRING));
        builder.addMethod(getGetterMethod("password", JavaType.STRING));
        builder.addMethod(getSetterMethod("password", JavaType.STRING));
        builder.addMethod(getGetterMethod("accountNonExpired",
                JAVA_TYPE_BOOLEAN));
        builder.addMethod(getSetterMethod("accountNonExpired",
                JAVA_TYPE_BOOLEAN));
        builder.addMethod(getGetterMethod("accountNonLocked", JAVA_TYPE_BOOLEAN));
        builder.addMethod(getSetterMethod("accountNonLocked", JAVA_TYPE_BOOLEAN));
        builder.addMethod(getGetterMethod("credentialsNonExpired",
                JAVA_TYPE_BOOLEAN));
        builder.addMethod(getSetterMethod("credentialsNonExpired",
                JAVA_TYPE_BOOLEAN));
        builder.addMethod(getGetterMethod("enabled", JAVA_TYPE_BOOLEAN));
        builder.addMethod(getSetterMethod("enabled", JAVA_TYPE_BOOLEAN));
        builder.addMethod(getGetterMethod("authorities", GRANTED_AUTHORITY));
        builder.addMethod(getSetterMethod("authorities", GRANTED_AUTHORITY));
        builder.addMethod(getGetterMethod("nombre", JavaType.STRING));
        builder.addMethod(getSetterMethod("nombre", JavaType.STRING));
        builder.addMethod(getGetterMethod("email", JavaType.STRING));
        builder.addMethod(getSetterMethod("email", JavaType.STRING));
        builder.addMethod(getGetterMethod("apellido1", JavaType.STRING));
        builder.addMethod(getSetterMethod("apellido1", JavaType.STRING));
        builder.addMethod(getGetterMethod("apellido2", JavaType.STRING));
        builder.addMethod(getSetterMethod("apellido2", JavaType.STRING));
        builder.addMethod(getGetterMethod("cif", JavaType.STRING));
        builder.addMethod(getSetterMethod("cif", JavaType.STRING));
        builder.addMethod(getGetterMethod("habilitado", JavaType.STRING));
        builder.addMethod(getSetterMethod("habilitado", JavaType.STRING));
        builder.addMethod(getGetterMethod("idHDFI", JavaType.STRING));
        builder.addMethod(getSetterMethod("idHDFI", JavaType.STRING));
        builder.addMethod(getGetterMethod("iusserDN", JavaType.STRING));
        builder.addMethod(getSetterMethod("iusserDN", JavaType.STRING));
        builder.addMethod(getGetterMethod("nif", JavaType.STRING));
        builder.addMethod(getSetterMethod("nif", JavaType.STRING));
        builder.addMethod(getGetterMethod("oid", JavaType.STRING));
        builder.addMethod(getSetterMethod("oid", JavaType.STRING));
        builder.addMethod(getGetterMethod("razonSocial", JavaType.STRING));
        builder.addMethod(getSetterMethod("razonSocial", JavaType.STRING));
        builder.addMethod(getGetterMethod("representante", JavaType.STRING));
        builder.addMethod(getSetterMethod("representante", JavaType.STRING));
        builder.addMethod(getGetterMethod("serialNumber", JavaType.STRING));
        builder.addMethod(getSetterMethod("serialNumber", JavaType.STRING));
        builder.addMethod(getGetterMethod("subjectDN", JavaType.STRING));
        builder.addMethod(getSetterMethod("subjectDN", JavaType.STRING));
        builder.addMethod(getGetterMethod("tipoAut", JavaType.STRING));
        builder.addMethod(getSetterMethod("tipoAut", JavaType.STRING));
        builder.addMethod(getGetterMethod("tipoCertificado", JavaType.STRING));
        builder.addMethod(getSetterMethod("tipoCertificado", JavaType.STRING));

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
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

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
