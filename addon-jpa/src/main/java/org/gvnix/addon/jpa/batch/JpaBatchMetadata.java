/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.batch;

import static org.springframework.roo.model.JdkJavaType.LIST;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
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

    public static final JavaSymbolName DELETE_ALL = new JavaSymbolName(
            "deleteAll");
    public static final JavaSymbolName DELETE_NOT_IN = new JavaSymbolName(
            "deleteNoIn");
    public static final JavaSymbolName DELETE_IN = new JavaSymbolName(
            "deleteIn");
    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaBatchMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

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
    private final String entityName;

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

        // Get refereed entity
        this.entity = annotationValues.entity;

        // Roo only use one field for pk
        this.entityIdentifier = identifiers.iterator().next();

        // Store jpa ActiveRecord info
        this.activeRecordMetadata = entityActiveRecordMetadata;

        // Get entity name to use for jpql
        this.entityName = StringUtils.isBlank(this.activeRecordMetadata
                .getEntityName()) ? entity.getSimpleTypeName()
                : this.activeRecordMetadata.getEntityName();

        this.listOfIdentifiersType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(entityIdentifier.getFieldType()));

        builder.addMethod(getGetEntityMethod());
        builder.addMethod(getEntityManagerMethod());
        builder.addMethod(getDeleteAllMethod());
        builder.addMethod(getDeleteInMethod());
        builder.addMethod(getDeleteNotInMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
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
        JavaSymbolName methodName = DELETE_ALL;

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
        return getDeleteByIdsListMethod(DELETE_IN, "IN");
    }

    /**
     * Return method <code>deleteNotIn</code>
     * 
     * @return
     */
    private MethodMetadata getDeleteNotInMethod() {
        return getDeleteByIdsListMethod(DELETE_NOT_IN, "NOT IN");
    }

    /**
     * Return method to delete entity based on a list of pks of the entity
     * 
     * @param methodName for generated method
     * @param condition to use in delete-by-list operation
     * @return
     */
    private MethodMetadata getDeleteByIdsListMethod(JavaSymbolName methodName,
            String condition) {

        // Define paramters types

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
        // entityManager().createQuery("DELETE FROM Pet AS p WHERE p.id IN :idList");
        bodyBuilder
                .appendFormalLine(String
                        .format("%s query = entityManager().createQuery(\"DELETE FROM %s as %s WHERE %s.%s %s :idList\");",
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
        return DELETE_ALL;
    }

    public JavaSymbolName getDeleteInMethodName() {
        return DELETE_IN;
    }

    public JavaSymbolName getDeleteNotInMethodName() {
        return DELETE_NOT_IN;
    }

    public JavaType getListOfIdentifiersType() {
        return listOfIdentifiersType;
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

}
