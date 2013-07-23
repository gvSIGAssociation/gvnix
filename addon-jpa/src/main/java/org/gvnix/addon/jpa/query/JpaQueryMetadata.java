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
package org.gvnix.addon.jpa.query;

import static org.springframework.roo.model.JdkJavaType.MAP;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata for {@link GvNIXJpaQuery} annotation.<br>
 * Generates ITD with the information collected from properties annotated by
 * {@link GvNIXJpaQuery}.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class JpaQueryMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Types constants

    private static final JavaType GVNIX_JPA_QUERY = new JavaType(
            GvNIXJpaQuery.class);

    private static final JavaType LIST = new JavaType(List.class);

    private static final JavaType LIST_STRING = new JavaType(
            LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    private static final JavaType MAP_STRING_LIST_STRING = new JavaType(
            MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, LIST_STRING));

    private static final JavaType HASHMAP = new JavaType(HashMap.class);

    private static final JavaType HASHMAP_STRING_LIST_STRING = new JavaType(
            HASHMAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, LIST_STRING));

    private static final JavaType COLLECTIONS = new JavaType(Collections.class);
    private static final JavaType ARRAYS = new JavaType(Arrays.class);

    public static final JavaSymbolName GET_FILTERBY_DEFINITION = new JavaSymbolName(
            "getFilterByAssociations");

    public static final JavaSymbolName GET_ORDERBY_DEFINITION = new JavaSymbolName(
            "getOrderByAssociations");

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaQueryMetadata.class
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

    /**
     * Field which holds filterBy definition
     */
    private FieldMetadata filterByField;

    /**
     * Field which holds orderBy definition
     */
    private FieldMetadata orderByField;

    /**
     * ITD generation helper
     */
    private final ItdBuilderHelper helper;

    public JpaQueryMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.helper = new ItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        Map<FieldMetadata, AnnotationMetadata> fieldsToProcess = new HashMap<FieldMetadata, AnnotationMetadata>();
        // Locate field with @GvNIXJpaFilterProperty
        AnnotationMetadata annotation;
        for (FieldMetadata field : governorTypeDetails.getDeclaredFields()) {
            annotation = field.getAnnotation(GVNIX_JPA_QUERY);
            if (annotation != null) {
                fieldsToProcess.put(field, annotation);
            }
        }

        // No fields to process
        if (fieldsToProcess.isEmpty()) {
            return;
        }

        // Adding static field to store filterBy definition
        builder.addField(getFilterByField());

        // Adding method to get FilterBy
        builder.addMethod(getFilterByMethod(fieldsToProcess));

        // Adding static field to store orderBy definition
        builder.addField(getOrderByField());

        // Adding method to get FilterBy
        builder.addMethod(getOrderByMethod(fieldsToProcess));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getFilterByField() {
        if (filterByField == null) {
            JavaSymbolName curName = new JavaSymbolName("filterByAssociations");
            String initializer = "null";
            int modifier = Modifier.PUBLIC + Modifier.STATIC;
            filterByField = getOrCreateField(curName, MAP_STRING_LIST_STRING,
                    initializer, modifier, null);
        }
        return filterByField;
    }

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getOrderByField() {
        if (orderByField == null) {
            JavaSymbolName curName = new JavaSymbolName("orderyByAssociations");
            String initializer = "null";
            int modifier = Modifier.PUBLIC + Modifier.STATIC;
            orderByField = getOrCreateField(curName, MAP_STRING_LIST_STRING,
                    initializer, modifier, null);
        }
        return orderByField;
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

    /**
     * Generate method to get the filterBy information
     * 
     * @param fieldsToProcess
     * @return
     */
    private MethodMetadata getFilterByMethod(
            Map<FieldMetadata, AnnotationMetadata> fieldsToProcess) {

        // public Map<String,List<String>> getJpaQueryFilterBy() {

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_FILTERBY_DEFINITION,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
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
        buildGetFilterByDefinitionMethodBody(bodyBuilder, fieldsToProcess);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC,
                GET_FILTERBY_DEFINITION, MAP_STRING_LIST_STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Generate method to get the orderBy information
     * 
     * @param fieldsToProcess
     * @return
     */
    private MethodMetadata getOrderByMethod(
            Map<FieldMetadata, AnnotationMetadata> fieldsToProcess) {

        // public Map<String,List<String>> getJpaQueryOrderBy() {

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_ORDERBY_DEFINITION,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
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
        buildGetOrderByDefinitionMethodBody(bodyBuilder, fieldsToProcess);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC,
                GET_ORDERBY_DEFINITION, MAP_STRING_LIST_STRING, parameterTypes,
                parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build body for get-filter-by information method
     * 
     * @param bodyBuilder
     * @param fieldsToProcess
     */
    private void buildGetFilterByDefinitionMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            Map<FieldMetadata, AnnotationMetadata> fieldsToProcess) {

        buildDefinitionMethodBody(bodyBuilder,
                getRelationFields(fieldsToProcess), getFilterByField(),
                "filterBy");
    }

    /**
     * Return a new map with fields from <code>fields</code> which are relation
     * properties.<br>
     * Currently just discard primitives an java.lang types.
     * 
     * @param fields
     * @return
     */
    private Map<FieldMetadata, AnnotationMetadata> getRelationFields(
            Map<FieldMetadata, AnnotationMetadata> fields) {
        Map<FieldMetadata, AnnotationMetadata> result = new HashMap<FieldMetadata, AnnotationMetadata>();
        JavaType type;
        for (Entry<FieldMetadata, AnnotationMetadata> entry : fields.entrySet()) {
            type = entry.getKey().getFieldType();
            if (JdkJavaType.isPartOfJavaLang(type)) {
                continue;
            }
            else if (type.isPrimitive()) {
                continue;
            }
            // TODO filter more properties
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Build body for get-order-by information method
     * 
     * @param bodyBuilder
     * @param fieldsToProcess
     */
    private void buildGetOrderByDefinitionMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            Map<FieldMetadata, AnnotationMetadata> fieldsToProcess) {
        buildDefinitionMethodBody(bodyBuilder,
                getRelationFields(fieldsToProcess), getOrderByField(),
                "orderBy");
    }

    /**
     * Build method to get filter by or orderBy information
     * 
     * @param bodyBuilder
     * @param fieldsToProcess
     * @param field to store result
     * @param annotationAttribute name to get values to include
     */
    private void buildDefinitionMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            Map<FieldMetadata, AnnotationMetadata> fieldsToProcess,
            FieldMetadata field, String annotationAttribute) {
        String fieldName = field.getFieldName().getSymbolName();
        // if(filterByAssociations == null) {
        bodyBuilder.appendFormalLine(String.format("if (%s == null) {",
                fieldName));
        bodyBuilder.indent();
        // Map<String, List<String>> tmp = new HashMap<String, List<String>>();
        bodyBuilder.appendFormalLine(String.format("%s tmp = new %s();",
                helper.getFinalTypeName(MAP_STRING_LIST_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_LIST_STRING)));
        for (Entry<FieldMetadata, AnnotationMetadata> entry : fieldsToProcess
                .entrySet()) {
            List<String> properties = new ArrayList<String>();
            AnnotationAttributeValue<?> curValue = entry.getValue()
                    .getAttribute(annotationAttribute);
            if (curValue == null) {
                continue;
            }
            if (curValue instanceof StringAttributeValue) {
                properties.add("\"".concat((String) curValue.getValue())
                        .concat("\""));
            }
            else {
                for (StringAttributeValue property : (List<StringAttributeValue>) curValue
                        .getValue()) {
                    properties.add("\"".concat(property.getValue())
                            .concat("\""));
                }
            }

            bodyBuilder
                    .appendFormalLine(String
                            .format("tmp.put(\"%s\", %s.unmodifiableList(%s.asList( new String[] {%s} )));",
                                    entry.getKey().getFieldName()
                                            .getSymbolName(),
                                    helper.getFinalTypeName(COLLECTIONS),
                                    helper.getFinalTypeName(ARRAYS),
                                    StringUtils.join(properties, ",")));
        }
        // filterByAssociations = Collections.unmodifiableMap(tmp);
        bodyBuilder.appendFormalLine(String.format(
                "%s = %s.unmodifiableMap(tmp);", fieldName,
                helper.getFinalTypeName(COLLECTIONS)));
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // return Collections.unmodifiableMap(filterByAssociations);
        bodyBuilder.appendFormalLine(String.format("return %s;", fieldName));

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
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Gets symbol name of method to get FilterBy definitions
     * 
     * @return
     */
    public JavaSymbolName getFilterByMethodName() {
        return GET_FILTERBY_DEFINITION;
    }

    /**
     * Gets symbol name of method to get OrderBy definitions
     * 
     * @return
     */
    public JavaSymbolName getOrderByMethodName() {
        return GET_ORDERBY_DEFINITION;
    }
}
