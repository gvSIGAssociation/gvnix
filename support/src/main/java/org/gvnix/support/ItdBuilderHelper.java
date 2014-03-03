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
package org.gvnix.support;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Helper which provides utilities for a ITD generator (Metadata)
 * 
 * @author gvNIX Team
 */
public class ItdBuilderHelper {

    protected final AbstractItdTypeDetailsProvidingMetadataItem metadata;
    protected final ImportRegistrationResolver importResolver;
    protected final ClassOrInterfaceTypeDetails governorTypeDetails;

    /**
     * Constructor
     * 
     * @param metadata
     * @param importResolver (usually to get use
     *        <code>builder.getImportRegistrationResolver()</code>)
     */
    public ItdBuilderHelper(
            AbstractItdTypeDetailsProvidingMetadataItem metadata,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ImportRegistrationResolver importResolver) {
        this.metadata = metadata;
        final Object physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        if (physicalTypeDetails instanceof ClassOrInterfaceTypeDetails) {
            // We have reliable physical type details
            this.governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }
        else {
            this.governorTypeDetails = null;
        }
        this.importResolver = importResolver;
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
        // return this.auditCreation;
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
        // this.auditCreation = auditCreation;
        bodyBuilder.appendFormalLine(String.format("this.%s = %s;",
                fieldName.getSymbolName(), fieldName.getSymbolName()));
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
     * @return
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
     * @return
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
        FieldMetadata currentField = governorTypeDetails
                .getDeclaredField(curName);
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
            currentField = null;
            JavaSymbolName newName = new JavaSymbolName(fieldName
                    .getSymbolName().concat("_"));
            currentField = governorTypeDetails.getDeclaredField(newName);
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

}
