/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.support;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.CommentStructure.CommentLocation;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Helper which provides utilities for a ITD generator (Metadata)
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public class ItdBuilderHelper {

    private final AbstractItdTypeDetailsProvidingMetadataItem metadata;
    private final ImportRegistrationResolver importResolver;
    private final ClassOrInterfaceTypeDetails governorTypeDetails;
    private final MemberDetails memberDetails;

    public ItdBuilderHelper(
            AbstractItdTypeDetailsProvidingMetadataItem metadata,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ImportRegistrationResolver importResolver) {
        this(metadata, governorPhysicalTypeMetadata, importResolver, null);
    }

    /**
     * Constructor
     * 
     * @param metadata
     * @param governorPhysicalTypeMetadata
     * @param importResolver (usually to get use
     *        <code>builder.getImportRegistrationResolver()</code>)
     * @param memberDetails (optional)
     */
    public ItdBuilderHelper(
            AbstractItdTypeDetailsProvidingMetadataItem metadata,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ImportRegistrationResolver importResolver,
            MemberDetails memberDetails) {
        this.metadata = metadata;
        final Object physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        if (physicalTypeDetails instanceof ClassOrInterfaceTypeDetails) {
            // We have reliable physical type details
            this.governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }
        else {
            // This metadata is invalid
            this.governorTypeDetails = null;
        }
        this.importResolver = importResolver;
        this.memberDetails = memberDetails;
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    public String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false, importResolver);
    }

    /**
     * Create an annotation value from string array
     * 
     * @param name
     * @param stringValues
     * @return
     */
    public ArrayAttributeValue<StringAttributeValue> toAttributeValue(
            String name, Iterable<String> stringValues) {
        List<StringAttributeValue> stringAttributeValues = new ArrayList<StringAttributeValue>();
        JavaSymbolName ignored = new JavaSymbolName("ignored");

        for (String str : stringValues) {
            stringAttributeValues.add(new StringAttributeValue(ignored, str));
        }

        return new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName(name), stringAttributeValues);
    }

    /**
     * Create an annotation value from Enum array
     * 
     * @param name
     * @param Sy
     * @return
     */
    public ArrayAttributeValue<EnumAttributeValue> toEnumAttributeValue(
            String name, JavaType enumType, Iterable<String> enumValueNames) {
        List<EnumAttributeValue> enumAttributeValues = new ArrayList<EnumAttributeValue>();
        JavaSymbolName ignored = new JavaSymbolName("ignored");

        for (String enumName : enumValueNames) {
            enumAttributeValues.add(new EnumAttributeValue(ignored,
                    new EnumDetails(enumType, new JavaSymbolName(enumName))));
        }

        return new ArrayAttributeValue<EnumAttributeValue>(new JavaSymbolName(
                name), enumAttributeValues);
    }

    /**
     * Try to locate a method in governor
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    public MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    /**
     * Builds body method for a field getter method.
     * 
     * @param bodyBuilder
     * @param fieldName
     */
    public void buildGetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            JavaSymbolName fieldName) {
        bodyBuilder.appendFormalLine(String.format("return this.%s;",
                fieldName.getSymbolName()));
    }

    /**
     * Builds body method for a field setter method.
     * 
     * @param bodyBuilder
     * @param fieldName
     */
    public void buildSetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            JavaSymbolName fieldName) {
        bodyBuilder.appendFormalLine(String.format("this.%s = %s;",
                fieldName.getSymbolName(), fieldName.getSymbolName()));
    }

    /**
     * Prepares a field-getter method.
     * <p/>
     * First, try to locate it on governor. Otherwise create it.
     * 
     * @param filedMetadata
     * @param aAnnotations (optional) annotation list
     * @return
     */
    public MethodMetadata getGetterMethod(FieldMetadata fieldMetadata,
            List<AnnotationMetadataBuilder> aAnnotations) {

        JavaSymbolName methodName = getGetterMethodNameForField(fieldMetadata
                .getFieldName());
        return getGetterMethod(fieldMetadata.getFieldName(), methodName,
                fieldMetadata.getFieldType(), aAnnotations);
    }

    /**
     * Prepares a field-getter method.
     * <p/>
     * First, try to locate it on governor. Otherwise create it.
     * 
     * @param filedName
     * @param fieldType
     * @param aAnnotations (optional) annotation list
     * @return
     */
    public MethodMetadata getGetterMethod(JavaSymbolName filedName,
            JavaType fieldType, List<AnnotationMetadataBuilder> aAnnotations) {

        JavaSymbolName methodName = getGetterMethodNameForField(filedName);
        return getGetterMethod(filedName, methodName, fieldType, aAnnotations);
    }

    /**
     * @param filedName
     * @return default getter name for received fieldName
     */
    public JavaSymbolName getGetterMethodNameForField(JavaSymbolName filedName) {
        JavaSymbolName methodName = new JavaSymbolName("get".concat(filedName
                .getSymbolNameCapitalisedFirstLetter()));
        return methodName;
    }

    /**
     * Prepares a field-getter method.
     * <p/>
     * First, try to locate it on governor. Otherwise create it.
     * 
     * @param filedName
     * @param methodName
     * @param fieldType
     * @param aAnnotations (optional) annotation list
     * @return
     */
    public MethodMetadata getGetterMethod(JavaSymbolName filedName,
            JavaSymbolName methodName, JavaType fieldType,
            List<AnnotationMetadataBuilder> aAnnotations) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        if (aAnnotations != null) {
            annotations.addAll(aAnnotations);
        }

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetterMethodBody(bodyBuilder, filedName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                metadata.getId(), Modifier.PUBLIC, methodName, fieldType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Prepares a field-setter method.
     * <p/>
     * First, try to locate it on governor. Otherwise create it.
     * 
     * @param filedMetadata
     * @param aAnnotations (optional) annotation list
     * @return
     */
    public MethodMetadata getSetterMethod(FieldMetadata fieldMetadata,
            List<AnnotationMetadataBuilder> aAnnotations) {

        JavaSymbolName methodName = getSetterMethodNameForField(fieldMetadata
                .getFieldName());
        return getSetterMethod(fieldMetadata.getFieldName(), methodName,
                fieldMetadata.getFieldType(), aAnnotations);
    }

    /**
     * Prepares a field-setter method.
     * <p/>
     * First, try to locate it on governor. Otherwise create it.
     * 
     * @param filedName
     * @param fieldType
     * @param aAnnotations (optional) annotation list
     * @return
     */
    public MethodMetadata getSetterMethod(JavaSymbolName filedName,
            JavaType fieldType, List<AnnotationMetadataBuilder> aAnnotations) {
        JavaSymbolName methodName = getSetterMethodNameForField(filedName);
        return getSetterMethod(filedName, methodName, fieldType, aAnnotations);
    }

    /**
     * @param filedName
     * @return default setter name for received fieldName
     */
    public JavaSymbolName getSetterMethodNameForField(JavaSymbolName filedName) {
        JavaSymbolName methodName = new JavaSymbolName("set".concat(filedName
                .getSymbolNameCapitalisedFirstLetter()));
        return methodName;
    }

    /**
     * Prepares a field-setter method.
     * <p/>
     * First, try to locate it on governor. Otherwise create it.
     * 
     * @param filedName
     * @param methodName
     * @param fieldType
     * @param aAnnotations (optional) annotation list
     * @return
     */
    public MethodMetadata getSetterMethod(JavaSymbolName filedName,
            JavaSymbolName methodName, JavaType fieldType,
            List<AnnotationMetadataBuilder> aAnnotations) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(fieldType));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        if (aAnnotations != null) {
            annotations.addAll(aAnnotations);
        }

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(filedName);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetterMethodBody(bodyBuilder, filedName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                metadata.getId(), Modifier.PUBLIC, methodName,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Action to do in
     * {@link ItdBuilderHelper#getField(JavaSymbolName, int, JavaType, List, GET_FIELD_EXISTS_ACTION)}
     * when required field is already defined in target class
     * 
     * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
     * 
     */
    public static enum GET_FIELD_EXISTS_ACTION {
        RETURN_EXISTING, RETURN_EXISTING_IF_ANNOTATION_MATCH, CREATE_NEW_ALWAYS, THROW_ERROR
    }

    /**
     * Create metadata for auto-wired convertionService field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getField(JavaSymbolName fieldName, int modifiers,
            JavaType fieldType,
            List<AnnotationMetadataBuilder> annotationsRequired,
            GET_FIELD_EXISTS_ACTION whenExists) {
        JavaSymbolName curName = fieldName;
        // Check if field exist
        FieldMetadata currentField = getDeclaredField(curName);
        if (currentField != null) {
            if (whenExists == GET_FIELD_EXISTS_ACTION.RETURN_EXISTING) {
                return currentField;
            }
            else if (whenExists == GET_FIELD_EXISTS_ACTION.RETURN_EXISTING_IF_ANNOTATION_MATCH) {
                if (annotationsRequired == null
                        || annotationsRequired.isEmpty()) {
                    return currentField;
                }
                if (hasFieldAllAnnotation(currentField, annotationsRequired)) {
                    return currentField;
                }
            }
            else if (whenExists == GET_FIELD_EXISTS_ACTION.THROW_ERROR) {
                throw new IllegalStateException(String.format(
                        "Field %s already exist", fieldName));
            }
        }
        if (currentField != null) {
            // No compatible field: look for new name
            JavaSymbolName newName = new JavaSymbolName(fieldName
                    .getSymbolName().concat("_"));
            return getField(newName, modifiers, fieldType, annotationsRequired,
                    whenExists);
        }
        // create field
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);
        if (annotationsRequired != null && !annotationsRequired.isEmpty()) {
            annotations.addAll(annotationsRequired);
        }
        // Using the FieldMetadataBuilder to create the field
        // definition.
        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                metadata.getId(), modifiers, annotations, fieldName, // Field
                fieldType); // Field type
        return fieldBuilder.build(); // Build and return a
                                     // FieldMetadata
        // instance
    }

    /**
     * Look in current entity definitions for a field
     * <p/>
     * If {@link MemberDetails} is initialized look on it.
     * 
     * @param curName
     * @return
     */
    private FieldMetadata getDeclaredField(JavaSymbolName name) {
        if (memberDetails == null) {
            return governorTypeDetails.getDeclaredField(name);
        }
        for (FieldMetadata field : memberDetails.getFields()) {
            if (field.getFieldName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Check if a currentField has all annotations
     * <p/>
     * TODO check annotation values
     * 
     * @param currentField
     * @param annotationsRequired
     * @return
     */
    private boolean hasFieldAllAnnotation(FieldMetadata currentField,
            List<AnnotationMetadataBuilder> annotationsRequired) {

        for (AnnotationMetadataBuilder annotation : annotationsRequired) {
            if (currentField.getAnnotation(annotation.getAnnotationType()) == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds JavaDoc metadata to a method builder
     * 
     * @param methodBuilder
     * @param description (optional) main method javaDoc
     * @param returnInfo (optional) return description
     * @param parametersInfo (optional) description of every parameters (if it's
     *        set must match with method parameters )
     */
    public void addJavaDocToMethod(MethodMetadataBuilder methodBuilder,
            String description, String returnInfo, String... parametersInfo) {

        if (parametersInfo != null
                && parametersInfo.length > 0
                && parametersInfo.length != methodBuilder.getParameterNames()
                        .size()) {
            throw new IllegalArgumentException(
                    "Parameter names size doesn't match with parameters info.");
        }

        CommentStructure comments = methodBuilder.getCommentStructure();

        if (comments == null) {
            comments = new CommentStructure();
            methodBuilder.setCommentStructure(comments);
        }

        if (StringUtils.isNotBlank(description)) {
            comments.addComment(new JavadocComment(), CommentLocation.BEGINNING);
            comments.addComment(
                    new JavadocComment(StringUtils.replace(description, "\n",
                            "\n<p/>\n")), CommentLocation.BEGINNING);
        }
        addJavaDocParams(methodBuilder, comments, parametersInfo);

        if (StringUtils.isNotBlank(returnInfo)) {
            comments.addComment(new JavadocComment(), CommentLocation.BEGINNING);
            comments.addComment(
                    new JavadocComment("@returns ".concat(returnInfo)),
                    CommentLocation.BEGINNING);
        }

        addJavaDocThrows(methodBuilder, comments);
    }

    /**
     * Adds method throws to JavaDoc
     * 
     * @param methodBuilder
     * @param comments
     */
    private void addJavaDocThrows(MethodMetadataBuilder methodBuilder,
            CommentStructure comments) {
        if (!methodBuilder.getThrowsTypes().isEmpty()) {
            comments.addComment(new JavadocComment(), CommentLocation.BEGINNING);
            for (JavaType throwType : methodBuilder.getThrowsTypes()) {
                comments.addComment(
                        new JavadocComment("@throws ".concat(throwType
                                .getSimpleTypeName())),
                        CommentLocation.BEGINNING);
            }
        }
    }

    /**
     * Adds method params to JavaDoc
     * 
     * @param methodBuilder
     * @param comments
     * @param parametersInfo
     */
    private void addJavaDocParams(MethodMetadataBuilder methodBuilder,
            CommentStructure comments, String... parametersInfo) {
        StringBuilder sBuilder;
        if (!methodBuilder.getParameterNames().isEmpty()) {
            comments.addComment(new JavadocComment(), CommentLocation.BEGINNING);
            int paramIndex = 0;
            for (JavaSymbolName paramName : methodBuilder.getParameterNames()) {
                sBuilder = new StringBuilder("@param ");
                sBuilder.append(paramName.getSymbolName());
                if (parametersInfo != null
                        && paramIndex < parametersInfo.length) {
                    sBuilder.append(parametersInfo[paramIndex]);
                }
                comments.addComment(new JavadocComment(sBuilder.toString()),
                        CommentLocation.BEGINNING);
                paramIndex++;
            }
        }
    }

    /**
     * Transform a list of {@link JavaType} to a list of
     * {@link AnnotationMetadataBuilder}
     * 
     * @param annotations
     * @return
     */
    public List<AnnotationMetadataBuilder> toAnnotationMetadata(
            JavaType... annotations) {
        return toAnnotationMetadata(Arrays.asList(annotations));
    }

    /**
     * Transform a list of {@link JavaType} to a list of
     * {@link AnnotationMetadataBuilder}
     * 
     * @param annotations
     * @return
     */
    public List<AnnotationMetadataBuilder> toAnnotationMetadata(
            List<JavaType> annotations) {
        List<AnnotationMetadataBuilder> result = new ArrayList<AnnotationMetadataBuilder>();
        if (annotations != null && !annotations.isEmpty()) {
            for (JavaType annotation : annotations) {
                result.add(new AnnotationMetadataBuilder(annotation));
            }
        }
        return result;
    }

    /**
     * Transform a list of {@link JavaType} to a list of
     * {@link AnnotatatedJavaType}
     * 
     * @param types
     * @return
     */
    public List<AnnotatedJavaType> toAnnotedJavaType(JavaType... types) {
        return toAnnotedJavaType(Arrays.asList(types));
    }

    /**
     * Transform a list of {@link JavaType} to a list of
     * {@link AnnotationMetadataBuilder}
     * 
     * @param types
     * @return
     */
    public List<AnnotatedJavaType> toAnnotedJavaType(List<JavaType> types) {
        List<AnnotatedJavaType> result = new ArrayList<AnnotatedJavaType>();
        if (types != null && !types.isEmpty()) {
            for (JavaType type : types) {
                result.add(new AnnotatedJavaType(type));
            }
        }
        return result;
    }

    /**
     * Transform a list of Strings to a list of {@link JavaSymbolName}
     * 
     * @param annotations
     * @return
     */
    public List<JavaSymbolName> toSymbolName(String... names) {
        return toSymbolName(Arrays.asList(names));
    }

    /**
     * Transform a list of Strings to a list of {@link JavaSymbolName}
     * 
     * @param annotations
     * @return
     */
    public List<JavaSymbolName> toSymbolName(List<String> names) {
        List<JavaSymbolName> result = new ArrayList<JavaSymbolName>();
        if (names != null && !names.isEmpty()) {
            for (String name : names) {
                result.add(new JavaSymbolName(name));
            }
        }
        return result;
    }
}
