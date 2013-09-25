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
package org.gvnix.addon.jpa.batch;

import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.MAP;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
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
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXJpaBatch} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class JpaBatchMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName FIND_BY_VALUES_METHOD = new JavaSymbolName(
            "findByValues");
    private static final JavaSymbolName DELETE_BY_VALUES_METHOD = new JavaSymbolName(
            "deleteByValues");
    public static final JavaSymbolName DELETE_ALL_METHOD = new JavaSymbolName(
            "deleteAll");
    public static final JavaSymbolName DELETE_NOT_IN_METHOD = new JavaSymbolName(
            "deleteNotIn");
    public static final JavaSymbolName DELETE_IN_METHOD = new JavaSymbolName(
            "deleteIn");
    public static final JavaSymbolName UPDATE_METHOD = new JavaSymbolName(
            "update");
    public static final JavaSymbolName CREATE_METHOD = new JavaSymbolName(
            "create");

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaBatchMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    static final JavaType MAP_STRING_STRING = new JavaType(
            MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.STRING));

    static final JavaType MAP_STRING_OBJECT = new JavaType(
            MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));

    static final JavaType QDSL_BOOLEAN_BUILDER = new JavaType(
            "com.mysema.query.BooleanBuilder");
    static final JavaType QDSL_PATH_BUILDER = new JavaType(
            "com.mysema.query.types.path.PathBuilder");
    static final JavaType QDSL_JPA_QUERY = new JavaType(
            "com.mysema.query.jpa.impl.JPAQuery");
    static final JavaType QDSL_JPA_DELETE_CLAUSE = new JavaType(
            "com.mysema.query.jpa.impl.JPADeleteClause");

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

    private final JpaBatchAnnotationValues annotationValues;
    private final JpaActiveRecordMetadata activeRecordMetadata;
    private final JavaType entity;
    private final FieldMetadata entityIdentifier;
    private final JavaType listOfIdentifiersType;
    private final JavaType listOfEntitiesType;
    private final String entityName;

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    /** Related entity plural */
    private final String entityPlural;

    public JpaBatchMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaBatchAnnotationValues annotationValues,
            List<FieldMetadata> identifiers,
            JpaActiveRecordMetadata entityActiveRecordMetadata,
            JpaCrudAnnotationValues crudAnnotationValues) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.annotationValues = annotationValues;

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        // Get refereed entity
        this.entity = annotationValues.entity;

        // Roo only use one field for pk
        this.entityIdentifier = identifiers.iterator().next();

        // Store jpa ActiveRecord info
        this.activeRecordMetadata = entityActiveRecordMetadata;

        // Get entity name
        this.entityName = StringUtils.isBlank(this.activeRecordMetadata
                .getEntityName()) ? entity.getSimpleTypeName()
                : this.activeRecordMetadata.getEntityName();
        this.entityPlural = entityActiveRecordMetadata.getPlural();

        this.listOfIdentifiersType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(entityIdentifier.getFieldType()));
        this.listOfEntitiesType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(entity.getBaseType()));

        builder.addMethod(getGetEntityMethod());
        builder.addMethod(getEntityManagerMethod());
        builder.addMethod(getDeleteAllMethod());
        builder.addMethod(getDeleteInMethod());
        builder.addMethod(getDeleteNotInMethod());
        builder.addMethod(getCreateMethod());
        builder.addMethod(getUpdateMethod());
        builder.addMethod(getFindByValuesMethod());
        builder.addMethod(getDeleteByValuesMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>deleteByValuesMethod</code> method. <br>
     * This method performs a delete base on property values
     * condition (value equal and concatenates conditions using "and" operator)
     * 
     * @return
     */
    private MethodMetadata getDeleteByValuesMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MAP_STRING_OBJECT);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(DELETE_BY_VALUES_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.TRANSACTIONAL));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("propertyValues"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildDeleteByValuesMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, DELETE_BY_VALUES_METHOD,
                JavaType.LONG_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>deleteByValues</code> method. <br>
     * This method performs a delete base on property values
     * condition (value equal and concatenates conditions using "and" operator)
     * 
     * @param bodyBuilder
     */
    private void buildDeleteByValuesMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        //
        bodyBuilder.appendFormalLine("");
        //
        // // if there no is a filter
        bodyBuilder.appendFormalLine("// if there no is a filter");
        // if (propertyValues == null || propertyValues.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (propertyValues == null || propertyValues.isEmpty()) {");
        bodyBuilder.indent();

        // throw new IllegalArgumentException("Missing property values");
        bodyBuilder
                .appendFormalLine("throw new IllegalArgumentException(\"Missing property values\");");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // // Prepare a predicate
        bodyBuilder.appendFormalLine("// Prepare a predicate");
        // BooleanBuilder baseFilterPredicate = new BooleanBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "%s baseFilterPredicate = new %s();",
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER),
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER)));
        bodyBuilder.appendFormalLine("");
        // // Base filter. Using BooleanBuilder, a cascading builder for
        bodyBuilder
                .appendFormalLine("// Base filter. Using BooleanBuilder, a cascading builder for");
        // // Predicate expressions
        bodyBuilder.appendFormalLine("// Predicate expressions");
        // PathBuilder<Visit> entity = new PathBuilder<Visit>(Visit.class,
        // "entity");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> entity = new %s<%s>(%s.class, \"entity\");",
                helper.getFinalTypeName(QDSL_PATH_BUILDER),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(QDSL_PATH_BUILDER),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(entity)));

        bodyBuilder.appendFormalLine("");
        //
        // // Build base filter
        bodyBuilder.appendFormalLine("// Build base filter");
        // for (String key : propertyMap.keySet()) {
        bodyBuilder
                .appendFormalLine("for (String key : propertyValues.keySet()) {");
        bodyBuilder.indent();
        // baseFilterPredicate.and(entity.get(key).eq(propertyMap.get(key)));
        bodyBuilder
                .appendFormalLine("baseFilterPredicate.and(entity.get(key).eq(propertyValues.get(key)));");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        //
        // // Create a query with filter
        bodyBuilder.appendFormalLine("// Create a query with filter");
        // JPADeleteClause delete = new
        // JPADeleteClause(Visit.entityManager(),entity);
        bodyBuilder.appendFormalLine(String.format(
                "%s delete = new %s(%s.entityManager(),entity);",
                helper.getFinalTypeName(QDSL_JPA_DELETE_CLAUSE),
                helper.getFinalTypeName(QDSL_JPA_DELETE_CLAUSE),
                helper.getFinalTypeName(entity)));
        //
        bodyBuilder.appendFormalLine("");
        // // execute delete
        bodyBuilder.appendFormalLine("// execute delete");
        // return delete.where(baseFilterPredicate).execute();
        bodyBuilder
                .appendFormalLine("return delete.where(baseFilterPredicate).execute();");

    }

    /**
     * Gets <code>findByParameters</code> method. <br>
     * This method generates a item List based on a list of entity values.
     * 
     * @return
     */
    private MethodMetadata getFindByValuesMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MAP_STRING_OBJECT);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(FIND_BY_VALUES_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("propertyValues"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFindByValuesMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, FIND_BY_VALUES_METHOD,
                listOfEntitiesType, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>findByValues</code> method. <br>
     * This method generates a item List based on a list of entity values.
     * 
     * @param bodyBuilder
     */
    private void buildFindByValuesMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        //
        bodyBuilder.appendFormalLine("");
        //
        // // if there is a filter
        bodyBuilder.appendFormalLine("// if there is a filter");
        // if (propertyValues != null && !propertyValues.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (propertyValues != null && !propertyValues.isEmpty()) {");
        bodyBuilder.indent();

        // // Prepare a predicate
        bodyBuilder.appendFormalLine("// Prepare a predicate");
        // BooleanBuilder baseFilterPredicate = new BooleanBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "%s baseFilterPredicate = new %s();",
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER),
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER)));
        bodyBuilder.appendFormalLine("");
        // // Base filter. Using BooleanBuilder, a cascading builder for
        bodyBuilder
                .appendFormalLine("// Base filter. Using BooleanBuilder, a cascading builder for");
        // // Predicate expressions
        bodyBuilder.appendFormalLine("// Predicate expressions");
        // PathBuilder<Visit> entity = new PathBuilder<Visit>(Visit.class,
        // "entity");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> entity = new %s<%s>(%s.class, \"entity\");",
                helper.getFinalTypeName(QDSL_PATH_BUILDER),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(QDSL_PATH_BUILDER),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(entity)));

        bodyBuilder.appendFormalLine("");
        //
        // // Build base filter
        bodyBuilder.appendFormalLine("// Build base filter");
        // for (String key : propertyMap.keySet()) {
        bodyBuilder
                .appendFormalLine("for (String key : propertyValues.keySet()) {");
        bodyBuilder.indent();
        // baseFilterPredicate.and(entity.get(key).eq(propertyMap.get(key)));
        bodyBuilder
                .appendFormalLine("baseFilterPredicate.and(entity.get(key).eq(propertyValues.get(key)));");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        //
        // // Create a query with filter
        bodyBuilder.appendFormalLine("// Create a query with filter");
        // JPAQuery query = new JPAQuery(Visit.entityManager());
        bodyBuilder.appendFormalLine(String.format(
                "%s query = new %s(%s.entityManager());",
                helper.getFinalTypeName(QDSL_JPA_QUERY),
                helper.getFinalTypeName(QDSL_JPA_QUERY),
                helper.getFinalTypeName(entity)));
        // query = query.from(entity);
        bodyBuilder.appendFormalLine("query = query.from(entity);");
        //
        bodyBuilder.appendFormalLine("");
        // // execute query
        bodyBuilder.appendFormalLine("// execute query");
        // return query.where(baseFilterPredicate).list(entity);
        bodyBuilder
                .appendFormalLine("return query.where(baseFilterPredicate).list(entity);");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        //
        // // no filter: return all elements
        bodyBuilder.appendFormalLine("// no filter: return all elements");
        // return Visit.findAllVisits();
        bodyBuilder.appendFormalLine(String.format("return %s.findAll%s();",
                helper.getFinalTypeName(entity),
                StringUtils.capitalize(entityPlural)));
    }

    /**
     * Return method <code>getEntity</code>
     * 
     * @return
     */
    private MethodMetadata getGetEntityMethod() {
        // method name
        JavaSymbolName methodName = new JavaSymbolName("getEntity");

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types (none in this case)
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(String.format("return %s.class;",
                getFinalTypeName(entity)));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.CLASS,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Return method <code>entityManager</code>
     * 
     * @return
     */
    private MethodMetadata getEntityManagerMethod() {
        // method name
        JavaSymbolName methodName = new JavaSymbolName("entityManager");

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types (none in this case)
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "return %s.entityManager();", getFinalTypeName(entity)));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName,
                JpaJavaType.ENTITY_MANAGER, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Return method <code>deleteAll</code>
     * 
     * @return
     */
    private MethodMetadata getDeleteAllMethod() {
        // method name
        JavaSymbolName methodName = DELETE_ALL_METHOD;

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.TRANSACTIONAL));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types (none in this case)
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder
                .appendFormalLine(String
                        .format("return entityManager().createQuery(\"DELETE FROM %s\").executeUpdate();",
                                entityName));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.INT_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Return method <code>deleteIn</code>
     * 
     * @return
     */
    private MethodMetadata getDeleteInMethod() {
        return getDeleteByIdsListMethod(DELETE_IN_METHOD, "IN");
    }

    /**
     * Return method <code>deleteNotIn</code>
     * 
     * @return
     */
    private MethodMetadata getDeleteNotInMethod() {
        return getDeleteByIdsListMethod(DELETE_NOT_IN_METHOD, "NOT IN");
    }

    /**
     * Return method to delete entity based on a list of pks of the entity
     * 
     * @param methodName for generated method
     * @param condition use IN or NOT IN delete-by-list operation
     * @return
     */
    private MethodMetadata getDeleteByIdsListMethod(JavaSymbolName methodName,
            String condition) {

        // Define parameters types

        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(listOfIdentifiersType);

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.TRANSACTIONAL));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        JavaSymbolName parameterName = new JavaSymbolName("ids");
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(parameterName);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String aliasName = entity.getSimpleTypeName().toLowerCase()
                .substring(0, 1);

        // Query query =
        // entityManager().createQuery("DELETE FROM Pet AS p WHERE p.id IN (:idList)");
        bodyBuilder
                .appendFormalLine(String
                        .format("%s query = entityManager().createQuery(\"DELETE FROM %s as %s WHERE %s.%s %s (:idList)\");",
                                getFinalTypeName(JpaJavaType.QUERY),
                                entityName,
                                aliasName,
                                aliasName,
                                entityIdentifier.getFieldName().getSymbolName(),
                                condition));

        // query.setParameter("list", ids);
        bodyBuilder.appendFormalLine(String.format(
                "query.setParameter(\"idList\", %s);",
                parameterName.getSymbolName()));

        // return query.executeUpdate();
        bodyBuilder.appendFormalLine("return query.executeUpdate();");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.INT_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    /**
     * Return method to create a list of entities
     * 
     * @return
     */
    private MethodMetadata getCreateMethod() {

        // Define parameters types

        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(listOfEntitiesType);

        // Check if a method exist in type
        final MethodMetadata method = methodExists(CREATE_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.TRANSACTIONAL));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        JavaSymbolName parameterName = new JavaSymbolName(
                entityPlural.toLowerCase());
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(parameterName);

        // --- Create the method body ---

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        // for(Vet vet : vets) {
        // vet.persist();
        // }
        bodyBuilder.appendFormalLine(String.format("for( %s %s : %s) {",
                entityName, entityName.toLowerCase(), parameterName));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format("%s.persist();",
                entityName.toLowerCase()));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        JavaType returnType = JavaType.VOID_PRIMITIVE;
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, CREATE_METHOD, returnType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Return method to update a list of entities
     * 
     * @return
     */
    private MethodMetadata getUpdateMethod() {

        // Define parameters types

        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(listOfEntitiesType);

        // Check if a method exist in type
        final MethodMetadata method = methodExists(UPDATE_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.TRANSACTIONAL));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        JavaSymbolName parameterName = new JavaSymbolName(
                entityPlural.toLowerCase());
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(parameterName);

        // --- Create the method body ---

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(entity);

        JavaType list = new JavaType("java.util.List", 0, DataType.TYPE, null,
                typeParams);
        JavaType arrayList = new JavaType("java.util.ArrayList", 0,
                DataType.TYPE, null, typeParams);

        // List<Vet> merged = new ArrayList<Vet>();
        bodyBuilder.appendFormalLine(String.format("%s merged = new %s();",
                getFinalTypeName(list), getFinalTypeName(arrayList)));

        // for(Vet vet : vets) {
        // merged.add( vet.merge() );
        // }
        // return merged;
        bodyBuilder.appendFormalLine(String.format("for( %s %s : %s) {",
                entityName, entityName.toLowerCase(), parameterName));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format("merged.add( %s.merge() );",
                entityName.toLowerCase()));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return merged;");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        JavaType returnType = list;
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, UPDATE_METHOD, returnType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("entity", entity);
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Get entity which batch operation affects to
     * 
     * @return
     */
    public JavaType getEntity() {
        return entity;
    }

    /**
     * Get {@link GvNIXJpaBatch} values
     * 
     * @return
     */
    public JpaBatchAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    public FieldMetadata getEntityIdentifier() {
        return entityIdentifier;
    }

    public JavaSymbolName getDeleteAllMethodName() {
        return DELETE_ALL_METHOD;
    }

    public JavaSymbolName getDeleteInMethodName() {
        return DELETE_IN_METHOD;
    }

    public JavaSymbolName getDeleteNotInMethodName() {
        return DELETE_NOT_IN_METHOD;
    }

    public JavaType getListOfIdentifiersType() {
        return listOfIdentifiersType;
    }

    public JavaType getListOfEntitiesType() {
        return listOfEntitiesType;
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
     * @return Entity name in plural
     */
    public String getEntityPlural() {
        return entityPlural;
    }

    /**
     * @return deleteByValue method Name
     */
    public JavaSymbolName getDeleteByValuesMethodName() {
        return DELETE_BY_VALUES_METHOD;
    }

}
