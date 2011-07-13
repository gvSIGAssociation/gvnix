/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.pattern.roo.addon;

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * Metadata for {@link GvNIXEntityBatch} annotation
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public class EntityBatchMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {
    private static final String PROVIDES_TYPE_STRING = EntityBatchMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Target entity metadata. To get persitence methods (merge, persist,
     * remove)
     */
    private final EntityMetadata entityMetadata;

    /**
     * Entity list inner class
     */
    private ClassOrInterfaceTypeDetails listInnerClass;

    /**
     * Merge Method name
     */
    private final JavaSymbolName mergeListMethodName = new JavaSymbolName(
            "merge");

    /**
     * Merge list Method definition
     */
    private MethodMetadata mergeListMethod;

    /**
     * Persist list method name
     */
    private final JavaSymbolName persisListtMethodName = new JavaSymbolName(
            "persist");

    /**
     * Persist method definition
     */
    private MethodMetadata persistListMethod;

    /**
     * Remove list method name
     */
    private final JavaSymbolName removeListMethodName = new JavaSymbolName(
            "remove");

    /**
     * Revome list method definition
     */
    private MethodMetadata removeListMethod;

    /**
     * Parameters types for all methods
     */
    private List<AnnotatedJavaType> paramTypesForMethod;

    /**
     * Parameters names for all methods
     */
    private List<JavaSymbolName> paramNamesForMethod;

    /**
     * Annotation for all methods (@Transactional)
     */
    private List<AnnotationMetadataBuilder> annotationsForMethod;

    /**
     * Metadata constructor
     * 
     * @param identifier
     * @param aspectName
     * @param governorPhysicalTypeMetadata
     * @param entityMetadata
     */
    public EntityBatchMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            EntityMetadata entityMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");
        Assert.notNull(entityMetadata, "EntityMetadata is needed.");
        this.entityMetadata = entityMetadata;

        builder.addInnerType(getListInnerClass());
        builder.addMethod(getMergeListMethod());
        builder.addMethod(getPersistListMethod());
        builder.addMethod(getRemoveListMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets Inner class to manage elements list for batch operations
     * 
     * @return
     */
    public ClassOrInterfaceTypeDetails getListInnerClass() {
        if (listInnerClass == null) {
            // Generate inner class name
            JavaType listInnerClassJavaType = new JavaType(destination
                    .getSimpleTypeName().concat("List"), 0, DataType.TYPE,
                    null, null);

            // Create class builder
            ClassOrInterfaceTypeDetailsBuilder classBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    getId(), Modifier.STATIC | Modifier.PUBLIC,
                    listInnerClassJavaType, PhysicalTypeCategory.CLASS);

            // Add fields
            FieldMetadata listField = getListInner_field("list", destination)
                    .build();
            FieldMetadata selectedField = getListInner_field("selected",
                    new JavaType("Integer")).build();
            classBuilder.addField(listField);
            classBuilder.addField(selectedField);

            // Adds getter/setter for list field
            classBuilder.addMethod(getListInner_getter(listField));
            classBuilder.addMethod(getListInner_setter(listField));

            // Adds getter/setter for selected field
            classBuilder.addMethod(getListInner_getter(selectedField));
            classBuilder.addMethod(getListInner_setter(selectedField));

            // Store generated class in a field
            listInnerClass = classBuilder.build();

        }
        return listInnerClass;

    }

    /**
     * Generate a getter for <code>field</code>
     * 
     * @param field
     *            field metadata
     * @return
     */
    private MethodMetadataBuilder getListInner_getter(FieldMetadata field) {
        // Gets filed name
        String fieldName = field.getFieldName().getSymbolName();

        // Generate method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(MessageFormat.format("return {0};",
                new Object[] { fieldName }));

        // Creates Method builder
        MethodMetadataBuilder builder = new MethodMetadataBuilder(getId(),
                Modifier.PUBLIC, new JavaSymbolName("get".concat(StringUtils
                        .capitalize(fieldName))), field.getFieldType(),
                bodyBuilder);

        return builder;
    }

    /**
     * Generate a setter for <code>field</code>
     * 
     * @param field
     *            field metadata
     * @return
     */
    private MethodMetadataBuilder getListInner_setter(FieldMetadata field) {
        // Gets filed name
        String fieldName = field.getFieldName().getSymbolName();

        // prepares method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(MessageFormat.format("this.{0} = {0};",
                new Object[] { fieldName }));

        // Create method builder
        MethodMetadataBuilder builder = new MethodMetadataBuilder(getId(),
                Modifier.PUBLIC, new JavaSymbolName("set".concat(StringUtils
                        .capitalize(fieldName))), JavaType.VOID_PRIMITIVE,
                bodyBuilder);

        // Adds setter parameter
        builder.addParameterType(new AnnotatedJavaType(field.getFieldType(),
                null));
        builder.addParameterName(field.getFieldName());

        return builder;
    }

    /**
     * Generates field (list) metadata for ListInner class using
     * <code>name</code> as field name
     * 
     * @param name
     *            to use to
     * @param listType
     *            list item type
     * 
     * @return
     */
    private FieldMetadataBuilder getListInner_field(String name,
            JavaType listType) {
        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(listType);
        JavaType fieldType = new JavaType("java.util.List", 0, DataType.TYPE,
                null, typeParams);

        FieldMetadataBuilder builder = new FieldMetadataBuilder(getId(),
                Modifier.PROTECTED, new JavaSymbolName(name), fieldType,
                MessageFormat.format("new java.util.ArrayList<{0}>()",
                        new Object[] { listType.getSimpleTypeName() }));

        return builder;
    }

    /**
     * Gets a list of types to use as parameters for methods
     * 
     * @return
     */
    private List<AnnotatedJavaType> getParamsTypesForMethods() {
        if (paramTypesForMethod == null) {
            List<AnnotatedJavaType> list = new ArrayList<AnnotatedJavaType>();

            list.add(new AnnotatedJavaType(getListInnerClass().getName(),
                    new ArrayList<AnnotationMetadata>()));
            paramTypesForMethod = Collections.unmodifiableList(list);

        }
        return paramTypesForMethod;
    }

    /**
     * Gets a list of names to use for methods parameters
     * 
     * @return
     */
    private List<JavaSymbolName> getParamsNamesForMethods() {
        if (paramNamesForMethod == null) {
            List<JavaSymbolName> list = new ArrayList<JavaSymbolName>();

            list.add(new JavaSymbolName("entities"));
            paramNamesForMethod = Collections.unmodifiableList(list);

        }
        return paramNamesForMethod;
    }

    private List<AnnotationMetadataBuilder> getAnnotationsForMethods() {
        if (annotationsForMethod == null) {
            List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                    1);
            annotations
                    .add(new AnnotationMetadataBuilder(
                            new JavaType(
                                    "org.springframework.transaction.annotation.Transactional")));
            annotationsForMethod = Collections.unmodifiableList(annotations);

        }
        return annotationsForMethod;
    }

    /**
     * <p>
     * Gets method for target operation.
     * </p>
     * <p>
     * First, try to look for it in governors. If not exists, create a new
     * method.
     * </p>
     * 
     * @param methodName
     * @param targetMethod
     * @return
     */
    private MethodMetadata getMethodFor(JavaSymbolName methodName,
            MethodMetadata targetMethod) {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = getParamsTypesForMethods();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(mergeListMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = getAnnotationsForMethods();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = getParamsNamesForMethods();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        /*
         * for (${entityName} entity : entities.list) {
         * entity.${targetMethod}(); }
         */
        bodyBuilder.appendFormalLine(MessageFormat.format(
                "for ({0} entity : entities.list) '{'",
                new Object[] { destination.getSimpleTypeName() }));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(MessageFormat.format("entity.{0}();",
                new Object[] { targetMethod.getMethodName().getSymbolName() }));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.STATIC, methodName, JavaType.VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build();

    }

    /**
     * Gets Merge list method
     * 
     * @return
     */
    public MethodMetadata getMergeListMethod() {
        if (mergeListMethod == null) {
            mergeListMethod = getMethodFor(mergeListMethodName,
                    entityMetadata.getMergeMethod());
        }
        return mergeListMethod;
    }

    /**
     * Gets Persist list method
     * 
     * @return
     */
    public MethodMetadata getPersistListMethod() {
        if (persistListMethod == null) {
            persistListMethod = getMethodFor(persisListtMethodName,
                    entityMetadata.getPersistMethod());
        }
        return persistListMethod;
    }

    /**
     * Gets Remove list method
     * 
     * @return
     */
    public MethodMetadata getRemoveListMethod() {
        if (removeListMethod == null) {
            removeListMethod = getMethodFor(removeListMethodName,
                    entityMetadata.getRemoveMethod());
        }
        return removeListMethod;
    }

    /**
     * Look for a method in governor type
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        // We have no access to method parameter information, so we scan by name
        // alone and treat any match as authoritative
        // We do not scan the superclass, as the caller is expected to know
        // we'll only scan the current class
        for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
            if (method.getMethodName().equals(methodName)
                    && method.getParameterTypes().equals(paramTypes)) {
                // Found a method of the expected name; we won't check method
                // parameters though
                return method;
            }
        }
        return null;
    }

    // Typically, no changes are required beyond this point

    @Override
    public String toString() {
        ToStringCreator tsc = new ToStringCreator(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("aspectName", aspectName);
        tsc.append("destinationType", destination);
        tsc.append("governor", governorPhysicalTypeMetadata.getId());
        tsc.append("itdTypeDetails", itdTypeDetails);
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }
}
