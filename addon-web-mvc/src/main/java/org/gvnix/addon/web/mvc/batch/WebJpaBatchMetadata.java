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
package org.gvnix.addon.web.mvc.batch;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.batch.GvNIXJpaBatch;
import org.gvnix.addon.jpa.batch.JpaBatchMetadata;
import org.gvnix.support.WebItdBuilderHelper;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
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
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXWebJpaBatch} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class WebJpaBatchMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName BATCH_SERVICE_NAME = new JavaSymbolName(
            "batchService");
    private static final JavaType LOGGER_TYPE = new JavaType("org.slf4j.Logger");
    private static final JavaType LOGGER_FACTORY_TYPE = new JavaType(
            "org.slf4j.LoggerFactory");

    private static final JavaSymbolName ID_LIST_PARAM = new JavaSymbolName(
            "idList");

    private static final JavaSymbolName DELETE_IN_PARAM = new JavaSymbolName(
            "deleteIn");

    private static final JavaSymbolName ALL_PARAM = new JavaSymbolName("all");

    private static final JavaSymbolName DELETE_METHOD = new JavaSymbolName(
            "delete");

    private static final JavaType RESPONSE_ENTITY_OBJECT = new JavaType(
            SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(JavaType.OBJECT));

    // Constants
    private static final String PROVIDES_TYPE_STRING = WebJpaBatchMetadata.class
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

    private final WebJpaBatchAnnotationValues annotationValues;
    private final JpaBatchMetadata jpaBatchMetadata;
    private final JavaType service;
    private final JavaType entity;
    private final JavaType listOfIdentifiersType;
    private final WebItdBuilderHelper helper;

    private FieldMetadata serviceFiled;

    private FieldMetadata loggerFiled;

    public WebJpaBatchMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            WebJpaBatchAnnotationValues annotationValues,
            JpaBatchMetadata jpaBatchMetadata,
            WebScaffoldAnnotationValues webScaffoldMetadataValue) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.helper = new WebItdBuilderHelper(this,
                builder.getImportRegistrationResolver());
        this.annotationValues = annotationValues;

        this.service = annotationValues.getService();

        Validate.notNull(service, String.format(
                "Missing service value required for %s in %s",
                WebJpaBatchAnnotationValues.WEB_JPA_BATCH_ANNOTATION
                        .getFullyQualifiedTypeName(),
                governorPhysicalTypeMetadata.getType()
                        .getFullyQualifiedTypeName()));

        this.jpaBatchMetadata = jpaBatchMetadata;

        listOfIdentifiersType = jpaBatchMetadata.getListOfIdentifiersType();

        this.entity = jpaBatchMetadata.getEntity();

        Validate.notNull(this.entity, String.format(
                "Missing entity value for %s in %s",
                GvNIXJpaBatch.class.getCanonicalName(),
                service.getFullyQualifiedTypeName()));

        Validate.isTrue(
                this.entity.equals(webScaffoldMetadataValue
                        .getFormBackingObject()),
                String.format(
                        "Service batch entity and Controller formBackingObject no match in %s",
                        governorPhysicalTypeMetadata.getType()
                                .getFullyQualifiedTypeName()));

        builder.addField(getLoggerField());
        builder.addField(getServiceField());
        builder.addMethod(getDeleteMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Return method <code>deleteSelection</code>
     * 
     * @return
     */
    private MethodMetadata getDeleteMethod() {
        // method name
        JavaSymbolName methodName = DELETE_METHOD;

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(helper
                .createRequestParam(JavaType.BOOLEAN_PRIMITIVE,
                        ALL_PARAM.getSymbolName(), false, null), helper
                .createRequestParam(JavaType.BOOLEAN_OBJECT,
                        DELETE_IN_PARAM.getSymbolName(), false, null), helper
                .createRequestParam(listOfIdentifiersType, ID_LIST_PARAM
                        .getSymbolName().concat("[]"), false, null));

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName, parameterTypes);

        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder requestMappingAnnotation = helper
                .getRequestMappingAnnotation("/delete", null, null,
                        "application/json", null, null);
        annotations.add(requestMappingAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(ALL_PARAM);
        parameterNames.add(DELETE_IN_PARAM);
        parameterNames.add(ID_LIST_PARAM);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildDeleteSelectionMethod(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, RESPONSE_ENTITY_OBJECT,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds deleteSelection method body
     * 
     * @param builder
     */
    private void buildDeleteSelectionMethod(InvocableMemberBodyBuilder builder) {
        // HttpHeaders headers = new HttpHeaders();
        builder.appendFormalLine(String.format(
                "%s headers = new HttpHeaders();",
                helper.getFinalTypeName(SpringJavaType.HTTP_HEADERS)));

        // headers.add("Content-Type", "application/json");
        builder.appendFormalLine("headers.add(\"Content-Type\", \"application/json\");");

        // long count = 0;
        builder.appendFormalLine("long count = 0;");

        // try {
        builder.appendFormalLine("try {");
        builder.indent(); // 1

        // if (all) {
        builder.appendFormalLine(String.format("if (%s) {",
                ALL_PARAM.getSymbolName()));
        builder.indent(); // 2

        // count = petBatchService.deleteAll();
        builder.appendFormalLine(String.format("count = %s.%s();",
                getServiceField().getFieldName().getSymbolName(),
                jpaBatchMetadata.getDeleteAllMethodName().getSymbolName()));

        // } else {
        builder.indentRemove(); // 1
        builder.appendFormalLine("} else {");
        builder.indent(); // 2

        // if (idList == null ) {
        builder.appendFormalLine(String.format("if (%s == null) {",
                ID_LIST_PARAM.getSymbolName()));

        // throw new
        // IllegalArgumentException("Missing request parameter 'idList[]'");
        builder.indent(); // 3
        builder.appendFormalLine("throw new IllegalArgumentException(\"Missing request parameter 'idList[]'\");");

        // }
        builder.indentRemove(); // 2
        builder.appendFormalLine("}");

        // if (!idList.isEmpty()) {
        builder.appendFormalLine(String.format("if (!%s.isEmpty()) {",
                ID_LIST_PARAM.getSymbolName()));
        builder.indent(); // 3

        // if (idListSelected) {
        builder.appendFormalLine(String.format("if (%s) {",
                DELETE_IN_PARAM.getSymbolName()));
        builder.indent(); // 4

        // count = petBatchService.deleteIn(idList);
        builder.appendFormalLine(String.format("count = %s.%s(%s);",
                getServiceField().getFieldName().getSymbolName(),
                jpaBatchMetadata.getDeleteInMethodName().getSymbolName(),
                ID_LIST_PARAM.getSymbolName()));

        // } else {
        builder.indentRemove(); // 3
        builder.appendFormalLine("} else {");
        builder.indent(); // 4

        // count = petBatchService.deleteNotIn(idList);
        builder.appendFormalLine(String.format("count = %s.%s(%s);",
                getServiceField().getFieldName().getSymbolName(),
                jpaBatchMetadata.getDeleteNotInMethodName().getSymbolName(),
                ID_LIST_PARAM.getSymbolName()));

        // }
        builder.indentRemove(); // 3
        builder.appendFormalLine("}");

        // }
        builder.indentRemove(); // 2
        builder.appendFormalLine("}");

        // }
        builder.indentRemove(); // 1
        builder.appendFormalLine("}");

        // } catch (RuntimeException e) {
        builder.indentRemove(); // 0
        builder.appendFormalLine("} catch (RuntimeException e) {");
        builder.indent(); // 1

        // LOGGER_BATCH.error("error deleting selection", e);
        builder.appendFormalLine(String.format(
                "%s.error(\"error deleting selection\", e);", getLoggerField()
                        .getFieldName().getSymbolName()));

        // return new ResponseEntity<Object>(e, headers,
        // HttpStatus.INTERNAL_SERVER_ERROR);
        builder.appendFormalLine(String.format(
                "return new %s<Object>(e, headers, %s.INTERNAL_SERVER_ERROR);",
                helper.getFinalTypeName(SpringJavaType.RESPONSE_ENTITY),
                helper.getFinalTypeName(SpringJavaType.HTTP_STATUS)));

        // }
        builder.indentRemove(); // 0
        builder.appendFormalLine("}");

        // LOGGER_JPA_BATCH.debug("deleted: " + count);
        builder.appendFormalLine(String.format(
                "%s.debug(\"deleted: \" + count);", getLoggerField()
                        .getFieldName().getSymbolName()));

        // return new ResponseEntity<Object>(e, headers,
        // HttpStatus.INTERNAL_SERVER_ERROR);
        builder.appendFormalLine(String.format(
                "return new %s<Object>(count, headers, %s.OK);",
                helper.getFinalTypeName(SpringJavaType.RESPONSE_ENTITY),
                helper.getFinalTypeName(SpringJavaType.HTTP_STATUS)));
    }

    /**
     * Create metadata for auto-wired jpa batch service field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getServiceField() {
        if (serviceFiled == null) {
            JavaSymbolName curName = BATCH_SERVICE_NAME;
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null) {
                if (currentField.getAnnotation(SpringJavaType.AUTOWIRED) == null
                        || !currentField.getFieldType().equals(service)) {
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
            if (currentField != null) {
                serviceFiled = currentField;
            }
            else {
                // create field
                List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                        1);
                annotations.add(new AnnotationMetadataBuilder(
                        SpringJavaType.AUTOWIRED));
                // Using the FieldMetadataBuilder to create the field
                // definition.
                final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                        getId(), Modifier.PUBLIC, annotations, curName, // Field
                        service); // Field type
                serviceFiled = fieldBuilder.build(); // Build and return a
                // FieldMetadata
                // instance
            }
        }
        return serviceFiled;
    }

    /**
     * Create metadata for logger static field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getLoggerField() {
        if (loggerFiled == null) {
            JavaSymbolName curName = new JavaSymbolName("LOGGER_BATCH");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null) {
                if (!currentField.getFieldType().equals(LOGGER_TYPE)) {
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
            if (currentField != null) {
                loggerFiled = currentField;
            }
            else {
                // Prepare initialized
                // LoggerFactory.getLogger(PetController.class);
                String initializer = String.format("%s.getLogger(%s.class);",
                        helper.getFinalTypeName(LOGGER_FACTORY_TYPE), helper
                                .getFinalTypeName(governorPhysicalTypeMetadata
                                        .getType()));

                // Using the FieldMetadataBuilder to create the field
                // definition.
                final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                        getId(), Modifier.PUBLIC + Modifier.STATIC, curName, // Field
                        LOGGER_TYPE, initializer); // Field type
                loggerFiled = fieldBuilder.build(); // Build and return a
                                                    // FieldMetadata
                                                    // instance
            }
        }
        return loggerFiled;
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
        builder.append("service", entity);
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
     * Get service which batch operation affects to
     * 
     * @return
     */
    public JavaType getService() {
        return service;
    }

    /**
     * Get {@link GvNIXWebJpaBatch} values
     * 
     * @return
     */
    public WebJpaBatchAnnotationValues getAnnotationValues() {
        return annotationValues;
    }
}
