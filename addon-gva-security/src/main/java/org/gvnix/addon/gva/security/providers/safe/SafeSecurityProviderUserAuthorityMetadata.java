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
public class SafeSecurityProviderUserAuthorityMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName EQUALS_METHOD = new JavaSymbolName(
            "equals");
    private static final JavaSymbolName HASH_CODE_METHOD = new JavaSymbolName(
            "hasCode");
    private static final JavaType JAVA_TYPE_BOOLEAN = JavaType.BOOLEAN_PRIMITIVE;
    private static final JavaType JAVA_TYPE_STRING = new JavaType(
            "java.lang.String");
    private static final JavaType GRANTED_AUTHORITY = new JavaType(
            "java.util.Set", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType(
                    "org.springframework.security.core.GrantedAuthority")));
    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderUserAuthorityMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    public SafeSecurityProviderUserAuthorityMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        /*Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");*/

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        // Adding Fields
        builder.addField(getField("serialVersionUID", "-2443806778851127910L",
                JavaType.LONG_PRIMITIVE, Modifier.PRIVATE + Modifier.STATIC
                        + Modifier.FINAL));

        builder.addField(getField("authority", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        // User Details
        builder.addField(getField("nif", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("usrtipo", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));
        builder.addField(getField("idgrupo", null, JAVA_TYPE_STRING,
                Modifier.PUBLIC));
        builder.addField(getField("idrol", null, JAVA_TYPE_STRING,
                Modifier.PUBLIC));
        builder.addField(getField("idaplicacion", null, JAVA_TYPE_STRING,
                Modifier.PUBLIC));

        // Creating getters and setters
        builder.addMethod(getGetterMethod("authority", JavaType.STRING));
        builder.addMethod(getSetterMethod("authority", JavaType.STRING));
        builder.addMethod(getGetterMethod("nif", JavaType.STRING));
        builder.addMethod(getSetterMethod("nif", JavaType.STRING));
        builder.addMethod(getGetterMethod("usrtipo", JavaType.STRING));
        builder.addMethod(getSetterMethod("usrtipo", JavaType.STRING));
        builder.addMethod(getGetterMethod("idgrupo", JavaType.STRING));
        builder.addMethod(getSetterMethod("idgrupo", JavaType.STRING));
        builder.addMethod(getGetterMethod("idrol", JavaType.STRING));
        builder.addMethod(getSetterMethod("idrol", JavaType.STRING));
        builder.addMethod(getGetterMethod("idaplicacion", JavaType.STRING));
        builder.addMethod(getSetterMethod("idaplicacion", JavaType.STRING));

        // Creating methods
        builder.addMethod(getHasCodeMethod());
        builder.addMethod(getEqualsMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>hasCode</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getHasCodeMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(HASH_CODE_METHOD,
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
        buildHashCodeMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, HASH_CODE_METHOD,
                JavaType.INT_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>equals</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getEqualsMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(JavaType.OBJECT);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(EQUALS_METHOD,
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
        parameterNames.add(new JavaSymbolName("obj"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildEqualsMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, EQUALS_METHOD,
                JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>hasCode</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildHashCodeMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("final int prime = 31;");
        bodyBuilder.appendFormalLine("int result = 1;");
        bodyBuilder.appendFormalLine("result = prime * result");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("+ ((authority == null) ? 0 : authority.hashCode());");
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("result = prime * result + ((nif == null) ? 0 : nif.hashCode());");
        bodyBuilder
                .appendFormalLine("result = prime * result + ((usrtipo == null) ? 0 : usrtipo.hashCode());");
        bodyBuilder
                .appendFormalLine("result = prime * result + ((idgrupo == null) ? 0 : idgrupo.hashCode());");
        bodyBuilder
                .appendFormalLine("result = prime * result + ((idrol == null) ? 0 : idrol.hashCode());");
        bodyBuilder
                .appendFormalLine("result = prime * result + ((idaplicacion == null) ? 0 : idaplicacion.hashCode());");
        bodyBuilder.appendFormalLine("return result;");
    }

    /**
     * Builds body method for <code>equals</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildEqualsMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("if (this == obj)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return true;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (obj == null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (getClass() != obj.getClass())");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("SafeUserAuthority other = (SafeUserAuthority) obj;");
        bodyBuilder.appendFormalLine("if (authority == null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (other.authority != null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        bodyBuilder
                .appendFormalLine("} else if (!authority.equals(other.authority))");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (nif == null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (other.nif != null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        bodyBuilder.appendFormalLine("} else if (!nif.equals(other.nif))");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (usrtipo == null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (other.usrtipo != null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        bodyBuilder
                .appendFormalLine("} else if (!usrtipo.equals(other.usrtipo))");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (idgrupo == null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (other.idgrupo != null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        bodyBuilder
                .appendFormalLine("} else if (!idgrupo.equals(other.idgrupo))");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (idrol == null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (other.idrol != null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        bodyBuilder.appendFormalLine("} else if (!idrol.equals(other.idrol))");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("if (idaplicacion == null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (other.idaplicacion != null)");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        bodyBuilder
                .appendFormalLine("} else if (!idaplicacion.equals(other.idaplicacion))");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return false;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("return true;");

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
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "get".concat(Character.toUpperCase(propertyName.charAt(0))
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
