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

import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.MAP;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.batch.GvNIXJpaBatch;
import org.gvnix.addon.jpa.batch.JpaBatchMetadata;
import org.gvnix.support.WebItdBuilderHelper;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.Jsr303JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * ITD generator for {@link GvNIXWebJpaBatch} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class WebJpaBatchMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String REQUEST_METHOD_WITHOUT_TYPE = "org.springframework.web.bind.annotation.RequestMethod.";

    private static final Logger LOGGER = HandlerUtils
            .getLogger(WebJpaBatchMetadata.class);

    private static final JavaSymbolName BINDING_RESULT_NAME = new JavaSymbolName(
            StringUtils.uncapitalize(SpringJavaType.BINDING_RESULT
                    .getSimpleTypeName()));
    private static final JavaSymbolName REQUEST_NAME = new JavaSymbolName(
            "request");

    private static final JavaType CONVERSION_SERVICE = new JavaType(
            "org.springframework.core.convert.ConversionService");

    private static final JavaType AUTOWIRED = new JavaType(
            "org.springframework.beans.factory.annotation.Autowired");

    private static final JavaType JSON_RESPONSE = new JavaType(
            "org.gvnix.web.json.JsonResponse");

    private static final JavaType HTTP_SERVLET_REQUEST = new JavaType(
            "javax.servlet.http.HttpServletRequest");
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
            "deleteBatch");

    private static final JavaSymbolName UPDATE_METHOD = new JavaSymbolName(
            "updateBatch");

    private static final JavaSymbolName CREATE_METHOD = new JavaSymbolName(
            "createBatch");

    private static final JavaSymbolName GET_OID_LIST_METHOD = new JavaSymbolName(
            "getOIDList");

    private static final JavaSymbolName GET_REQUEST_METHOD = new JavaSymbolName(
            "getRequestPropertyValues");

    private static final JavaType RESPONSE_ENTITY_OBJECT = new JavaType(
            SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(JavaType.OBJECT));

    // Constants
    private static final String PROVIDES_TYPE_STRING = WebJpaBatchMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaType MAP_STRING_OBJECT = new JavaType(
            MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));

    private static final JavaType HASHMAP = new JavaType(HashMap.class);
    private static final JavaType HASHMAP_STRING_OBJECT = new JavaType(
            HASHMAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));

    private static final JavaType ITERATOR_STRING = new JavaType(
            JdkJavaType.ITERATOR.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(JavaType.STRING));

    private static final JavaType LIST_STRING = new JavaType(
            LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    private static final JavaType ARRAYLIST_STRING = new JavaType(
            ARRAY_LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    private static final JavaType COLLECTION_UTILS = new JavaType(
            "org.apache.commons.collections.CollectionUtils");

    private static final JavaType BEAN_WRAPPER = new JavaType(
            "org.springframework.beans.BeanWrapper");
    private static final JavaType BEAN_WRAPPER_IMP = new JavaType(
            "org.springframework.beans.BeanWrapperImpl");
    private static final JavaType WEB_REQUEST = new JavaType(
            "org.springframework.web.context.request.WebRequest");

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
    private final JavaType listOfEntityType;
    private final JavaSymbolName listOfEntityName;
    private final JavaType jsonResponseList;
    private final WebItdBuilderHelper helper;
    private final FieldMetadata entityIdentifier;

    private FieldMetadata serviceFiled;

    private FieldMetadata loggerFiled;
    private final String entityName;

    private FieldMetadata conversionService;

    public WebJpaBatchMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            WebJpaBatchAnnotationValues annotationValues,
            JpaBatchMetadata jpaBatchMetadata,
            WebScaffoldAnnotationValues webScaffoldMetadataValue) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.helper = new WebItdBuilderHelper(this,
                governorPhysicalTypeMetadata,
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
        listOfEntityType = jpaBatchMetadata.getListOfEntitiesType();

        this.entity = jpaBatchMetadata.getEntity();

        this.entityName = JavaSymbolName.getReservedWordSafeName(entity)
                .getSymbolName();

        this.entityIdentifier = jpaBatchMetadata.getEntityIdentifier();

        listOfEntityName = new JavaSymbolName(
                StringUtils.uncapitalize(jpaBatchMetadata.getEntityPlural()));

        jsonResponseList = new JavaType(
                JSON_RESPONSE.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(listOfEntityType));

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
        // Adding field definition
        builder.addField(getConversionServiceField());
        builder.addField(getLoggerField());

        // Adding methods
        builder.addField(getServiceField());
        builder.addMethod(getDeleteMethod());
        builder.addMethod(getUpdateMethod());
        builder.addMethod(getCreateMethod());
        builder.addMethod(getGetOIDListMethod());
        builder.addMethod(getGetRequestPropertyValuesMethod());

        // Check if deleteBatch, createBatch or updateBatch are duplicated with
        // different name.
        checkIfExistsCUDMethods(governorTypeDetails);

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private void checkIfExistsCUDMethods(
            ClassOrInterfaceTypeDetails governorTypeDetails) {
        // If deleteBatch method is duplicated (with different name), show an
        // alert!
        final List<AnnotatedJavaType> deleteParameterTypes = Arrays.asList(
                helper.createRequestParam(JavaType.BOOLEAN_PRIMITIVE,
                        ALL_PARAM.getSymbolName(), false, null), helper
                        .createRequestParam(JavaType.BOOLEAN_OBJECT,
                                DELETE_IN_PARAM.getSymbolName(), false, null),
                helper.createRequestParam(listOfIdentifiersType, ID_LIST_PARAM
                        .getSymbolName().concat("[]"), false, null),
                new AnnotatedJavaType(entity, new AnnotationMetadataBuilder(
                        SpringJavaType.MODEL_ATTRIBUTE).build()),
                new AnnotatedJavaType(WEB_REQUEST));

        MethodMetadata deleteMethod = checkExistsDeleteBatchMethod(
                governorTypeDetails, deleteParameterTypes);
        if (deleteMethod != null) {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "WARNING: deleteBatch method was generated in %s. Remove %s method or rename it to deleteBatch",
                            aspectName.getSimpleTypeName(), deleteMethod
                                    .getMethodName().getSymbolName()));
        }

        // If updateBatch method is duplicated (with different name), show an
        // alert!
        final List<AnnotatedJavaType> updateParameterTypes = Arrays.asList(
                new AnnotatedJavaType(listOfEntityType, Arrays.asList(
                        AnnotationMetadataBuilder
                                .getInstance(SpringJavaType.REQUEST_BODY),
                        AnnotationMetadataBuilder
                                .getInstance(Jsr303JavaType.VALID))),
                AnnotatedJavaType
                        .convertFromJavaType(SpringJavaType.BINDING_RESULT),
                AnnotatedJavaType.convertFromJavaType(HTTP_SERVLET_REQUEST));

        MethodMetadata updateMethod = checkExistsCUBatchMethod(
                governorTypeDetails, updateParameterTypes, "PUT");
        if (updateMethod != null) {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "WARNING: updateBatch method was generated in %s. Remove %s method or rename it to updateBatch",
                            aspectName.getSimpleTypeName(), updateMethod
                                    .getMethodName().getSymbolName()));
        }

        // If createBatch method is duplicated (with different name), show an
        // alert!
        final List<AnnotatedJavaType> createParameterTypes = Arrays.asList(
                new AnnotatedJavaType(listOfEntityType, Arrays.asList(
                        AnnotationMetadataBuilder
                                .getInstance(SpringJavaType.REQUEST_BODY),
                        AnnotationMetadataBuilder
                                .getInstance(Jsr303JavaType.VALID))),
                AnnotatedJavaType
                        .convertFromJavaType(SpringJavaType.BINDING_RESULT),
                AnnotatedJavaType.convertFromJavaType(HTTP_SERVLET_REQUEST));

        MethodMetadata createMethod = checkExistsCUBatchMethod(
                governorTypeDetails, createParameterTypes, "POST");
        if (createMethod != null) {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "WARNING: createBatch method was generated in %s. Remove %s method or rename it to createBatch",
                            aspectName.getSimpleTypeName(), createMethod
                                    .getMethodName().getSymbolName()));
        }

    }

    /**
     * This method check if another delete method exists to the current
     * controller
     * 
     * @param governorTypeDetails
     * @param parameterTypes
     * @return
     */
    private MethodMetadata checkExistsDeleteBatchMethod(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            List<AnnotatedJavaType> parameterTypes) {

        // Getting all methods
        List<? extends MethodMetadata> methods = governorTypeDetails
                .getDeclaredMethods();
        Iterator<? extends MethodMetadata> it = methods.iterator();
        while (it.hasNext()) {
            MethodMetadata method = it.next();
            // Getting request
            AnnotationMetadata requestAnnotation = method
                    .getAnnotation(SpringJavaType.REQUEST_MAPPING);
            if (requestAnnotation != null) {
                // Getting request value
                AnnotationAttributeValue<?> request = requestAnnotation
                        .getAttribute(new JavaSymbolName("value"));
                if (request != null) {
                    String value = request.getValue().toString();
                    // Getting method parameterTypes
                    final List<JavaType> methodParameters = AnnotatedJavaType
                            .convertFromAnnotatedJavaTypes(method
                                    .getParameterTypes());
                    // If method exists with same params, return method
                    String methodName = method.getMethodName().getSymbolName();
                    if ("/delete".equals(value)
                            && AnnotatedJavaType.convertFromAnnotatedJavaTypes(
                                    parameterTypes).equals(methodParameters)
                            && !"deleteBatch".equals(methodName)) {
                        return method;
                    }
                }
            }
        }

        return null;
    }

    /**
     * This method check if another update method exists to the current
     * controller
     * 
     * @param governorTypeDetails
     * @param parameterTypes
     * @return
     */
    private MethodMetadata checkExistsCUBatchMethod(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            List<AnnotatedJavaType> parameterTypes, String requestMethodType) {

        // Getting all methods
        List<? extends MethodMetadata> methods = governorTypeDetails
                .getDeclaredMethods();
        Iterator<? extends MethodMetadata> it = methods.iterator();
        while (it.hasNext()) {
            MethodMetadata method = it.next();
            // Getting request and response body
            AnnotationMetadata requestAnnotation = method
                    .getAnnotation(SpringJavaType.REQUEST_MAPPING);
            AnnotationMetadata responseAnnotation = method
                    .getAnnotation(SpringJavaType.RESPONSE_BODY);
            if (requestAnnotation != null && responseAnnotation != null) {
                // Getting request value
                AnnotationAttributeValue<?> methodParamAnnotation = requestAnnotation
                        .getAttribute(new JavaSymbolName("method"));
                if (methodParamAnnotation != null) {
                    String value = methodParamAnnotation.getValue().toString();
                    // Getting method parameterTypes
                    final List<JavaType> methodParameters = AnnotatedJavaType
                            .convertFromAnnotatedJavaTypes(method
                                    .getParameterTypes());
                    // If method exists with same params, return method
                    String methodName = method.getMethodName().getSymbolName();
                    String validMethodName = "createBatch";
                    if (requestMethodType.equals("PUT")) {
                        validMethodName = "updateBatch";
                    }
                    if (value.equals(REQUEST_METHOD_WITHOUT_TYPE
                            .concat(requestMethodType))
                            && AnnotatedJavaType.convertFromAnnotatedJavaTypes(
                                    parameterTypes).equals(methodParameters)
                            && !methodName.equals(validMethodName)) {
                        return method;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Return method <code>delete</code>
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
                        .getSymbolName().concat("[]"), false, null),
                new AnnotatedJavaType(entity, new AnnotationMetadataBuilder(
                        SpringJavaType.MODEL_ATTRIBUTE).build()),
                new AnnotatedJavaType(WEB_REQUEST));

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
                .getRequestMappingAnnotation("/delete", "POST", null,
                        "application/json", null, null);
        annotations.add(requestMappingAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(ALL_PARAM);
        parameterNames.add(DELETE_IN_PARAM);
        parameterNames.add(ID_LIST_PARAM);
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(REQUEST_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildDeleteMethod(bodyBuilder);

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
     * Builds delete method body
     * 
     * @param builder
     */
    private void buildDeleteMethod(InvocableMemberBodyBuilder builder) {
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

        // Map<String, Object> baseFilter =
        // getRequestPropertyValues(visit,request.getParameterNames());
        builder.appendFormalLine(String.format(
                "%s baseFilter = %s(%s,%s.getParameterNames());",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                GET_REQUEST_METHOD.getSymbolName(), entityName,
                REQUEST_NAME.getSymbolName()));
        // if (baseFilter == null || baseFilter.isEmpty()){
        builder.appendFormalLine("if (baseFilter == null || baseFilter.isEmpty()) {");
        builder.indent(); // 3

        // count = batchService.deleteAll();
        builder.appendFormalLine(String.format("count = %s.%s();",
                getServiceField().getFieldName().getSymbolName(),
                jpaBatchMetadata.getDeleteAllMethodName().getSymbolName()));

        // } else {
        builder.indentRemove(); // 2
        builder.appendFormalLine("} else {");
        builder.indent(); // 3

        // count = batchService.deleteByValues(baseFilter);
        builder.appendFormalLine(String.format("count = %s.%s(baseFilter);",
                getServiceField().getFieldName().getSymbolName(),
                jpaBatchMetadata.getDeleteByValuesMethodName().getSymbolName()));

        // }
        builder.indentRemove(); // 2
        builder.appendFormalLine("}");

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
     * Return method <code>update</code>
     * 
     * @return
     */
    private MethodMetadata getUpdateMethod() {
        // method name
        JavaSymbolName methodName = UPDATE_METHOD;

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(
                new AnnotatedJavaType(listOfEntityType, Arrays.asList(
                        AnnotationMetadataBuilder
                                .getInstance(SpringJavaType.REQUEST_BODY),
                        AnnotationMetadataBuilder
                                .getInstance(Jsr303JavaType.VALID))),
                AnnotatedJavaType
                        .convertFromJavaType(SpringJavaType.BINDING_RESULT),
                AnnotatedJavaType.convertFromJavaType(HTTP_SERVLET_REQUEST));

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
                .getRequestMappingAnnotation(null, "PUT", "application/json",
                        "application/json", null, "Accept=application/json");
        annotations.add(requestMappingAnnotation);
        // @ResponseBody
        AnnotationMetadataBuilder responseBodyAnnotation = new AnnotationMetadataBuilder();
        responseBodyAnnotation.setAnnotationType(SpringJavaType.RESPONSE_BODY);
        annotations.add(responseBodyAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(listOfEntityName);
        parameterNames.add(BINDING_RESULT_NAME);
        parameterNames.add(REQUEST_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildUpdateMethod(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, jsonResponseList,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds update method body
     * 
     * @param builder
     */
    private void buildUpdateMethod(InvocableMemberBodyBuilder builder) {
        // JsonResponse<List<Vet>> jsonResponse = new
        // JsonResponse<List<Vet>>();
        builder.appendFormalLine(String.format("%s jsonResponse = new %s();",
                helper.getFinalTypeName(jsonResponseList),
                helper.getFinalTypeName(jsonResponseList)));
        // jsonResponse.setValue(vets);
        builder.appendFormalLine(String.format("jsonResponse.setValue(%s);",
                listOfEntityName.getSymbolName()));
        // if (bindingResult.hasErrors()) {
        builder.appendFormalLine(String.format("if (%s.hasErrors()) {",
                BINDING_RESULT_NAME.getSymbolName()));

        builder.indent();
        // jsonResponse.setBindingResult(bindingResult);
        builder.appendFormalLine(String.format(
                "jsonResponse.setBindingResult(%s);",
                BINDING_RESULT_NAME.getSymbolName()));

        // jsonResponse.setStatus("ERROR");
        builder.appendFormalLine("jsonResponse.setStatus(\"ERROR\");");
        // return jsonResponse
        builder.appendFormalLine("return jsonResponse;");

        // }
        builder.indentRemove();
        builder.appendFormalLine("}");

        // try {
        builder.appendFormalLine("try {");
        builder.indent();

        // vets = batchService.update(vets);
        builder.appendFormalLine(String.format("%s = %s.update(%s);",
                listOfEntityName.getSymbolName(), getServiceField()
                        .getFieldName().getSymbolName(), listOfEntityName
                        .getSymbolName()));
        // }
        builder.indentRemove();
        builder.appendFormalLine("}");

        // catch(Exception ex) {
        builder.appendFormalLine("catch(Exception ex) {");
        builder.indent();

        // jsonResponse.setStatus("ERROR");
        builder.appendFormalLine("jsonResponse.setStatus(\"ERROR\");");
        // jsonResponse.setExceptionMessage(ex.getLocalizedMessage());
        builder.appendFormalLine("jsonResponse.setExceptionMessage(ex.getLocalizedMessage());");
        // return jsonResponse
        builder.appendFormalLine("return jsonResponse;");

        // }
        builder.indentRemove();
        builder.appendFormalLine("}");

        // jsonResponse.setValue(vets);
        builder.appendFormalLine(String.format("jsonResponse.setValue(%s);",
                listOfEntityName.getSymbolName()));
        // jsonResponse.setOid(getOIDList(vets));
        builder.appendFormalLine(String.format("jsonResponse.setOid(%s(%s));",
                GET_OID_LIST_METHOD, listOfEntityName.getSymbolName()));
        // jsonResponse.setStatus("SUCCESS");
        builder.appendFormalLine("jsonResponse.setStatus(\"SUCCESS\");");
        // return jsonResponse
        builder.appendFormalLine("return jsonResponse;");
    }

    /**
     * Return method <code>create</code>
     * 
     * @return
     */
    private MethodMetadata getCreateMethod() {
        // method name
        JavaSymbolName methodName = CREATE_METHOD;

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(
                new AnnotatedJavaType(listOfEntityType, Arrays.asList(
                        AnnotationMetadataBuilder
                                .getInstance(SpringJavaType.REQUEST_BODY),
                        AnnotationMetadataBuilder
                                .getInstance(Jsr303JavaType.VALID))),
                AnnotatedJavaType
                        .convertFromJavaType(SpringJavaType.BINDING_RESULT),
                AnnotatedJavaType.convertFromJavaType(HTTP_SERVLET_REQUEST));

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
                .getRequestMappingAnnotation(null, "POST", "application/json",
                        "application/json", null, "Accept=application/json");
        annotations.add(requestMappingAnnotation);
        // @ResponseBody
        AnnotationMetadataBuilder responseBodyAnnotation = new AnnotationMetadataBuilder();
        responseBodyAnnotation.setAnnotationType(SpringJavaType.RESPONSE_BODY);
        annotations.add(responseBodyAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(listOfEntityName);
        parameterNames.add(BINDING_RESULT_NAME);
        parameterNames.add(REQUEST_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildCreateMethod(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, jsonResponseList,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds create method body
     * 
     * @param builder
     */
    private void buildCreateMethod(InvocableMemberBodyBuilder builder) {
        // JsonResponse<List<Vet>> jsonResponse = new
        // JsonResponse<List<Vet>>();
        builder.appendFormalLine(String.format("%s jsonResponse = new %s();",
                helper.getFinalTypeName(jsonResponseList),
                helper.getFinalTypeName(jsonResponseList)));
        // jsonResponse.setValue(vets);
        builder.appendFormalLine(String.format("jsonResponse.setValue(%s);",
                listOfEntityName.getSymbolName()));
        // if (bindingResult.hasErrors()) {
        builder.appendFormalLine(String.format("if (%s.hasErrors()) {",
                BINDING_RESULT_NAME.getSymbolName()));

        builder.indent();
        // jsonResponse.setBindingResult(bindingResult);
        builder.appendFormalLine(String.format(
                "jsonResponse.setBindingResult(%s);",
                BINDING_RESULT_NAME.getSymbolName()));
        // jsonResponse.setStatus("ERROR");
        builder.appendFormalLine("jsonResponse.setStatus(\"ERROR\");");
        // return jsonResponse
        builder.appendFormalLine("return jsonResponse;");

        // }
        builder.indentRemove();
        builder.appendFormalLine("}");

        // try {
        builder.appendFormalLine("try {");
        builder.indent();

        // batchService.create(vets);
        builder.appendFormalLine(String.format("%s.create(%s);",
                getServiceField().getFieldName().getSymbolName(),
                listOfEntityName.getSymbolName()));
        // }
        builder.indentRemove();
        builder.appendFormalLine("}");

        // catch(Exception ex) {
        builder.appendFormalLine("catch(Exception ex) {");
        builder.indent();

        // jsonResponse.setStatus("ERROR");
        builder.appendFormalLine("jsonResponse.setStatus(\"ERROR\");");
        // jsonResponse.setExceptionMessage(ex.getLocalizedMessage());
        builder.appendFormalLine("jsonResponse.setExceptionMessage(ex.getLocalizedMessage());");
        // return jsonResponse
        builder.appendFormalLine("return jsonResponse;");

        // }
        builder.indentRemove();
        builder.appendFormalLine("}");

        // jsonResponse.setOid(getOIDList(vets));
        builder.appendFormalLine(String.format("jsonResponse.setOid(%s(%s));",
                GET_OID_LIST_METHOD, listOfEntityName.getSymbolName()));
        // jsonResponse.setStatus("SUCCESS");
        builder.appendFormalLine("jsonResponse.setStatus(\"SUCCESS\");");
        // return jsonResponse
        builder.appendFormalLine("return jsonResponse;");
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

    /**
     * Gets <code>getRequestPropertyValues</code> method. <br>
     * This method returns a Map with bean properties which appears on a String
     * Iterator (usually from webRequest.getParametersNames())
     * 
     * @return
     */
    private MethodMetadata getGetRequestPropertyValuesMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entity, ITERATOR_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_REQUEST_METHOD,
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
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(new JavaSymbolName("propertyNames"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetRequestPropertyValuesBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_REQUEST_METHOD,
                MAP_STRING_OBJECT, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build body method <code>getRequestPropertyValues</code> method. <br>
     * This method returns a Map with bean properties which appears on a String
     * Iterator (usually from webRequest.getParametersNames())
     * 
     * @return
     */
    private void buildGetRequestPropertyValuesBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Map<String, Object> propertyValuesMap = new HashMap<String,
        // Object>();
        bodyBuilder.appendFormalLine(String.format(
                "%s propertyValuesMap = new %s();",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));
        //
        bodyBuilder.appendFormalLine("");

        // // If no entity or properties given, return empty Map
        bodyBuilder
                .appendFormalLine("// If no entity or properties given, return empty Map");

        // if(entity == null || propertyNames == null) {
        bodyBuilder.appendFormalLine(String.format(
                "if(%s == null || propertyNames == null) {", entityName));
        bodyBuilder.indent();
        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        // List<String> properties = new ArrayList<String>();
        bodyBuilder.appendFormalLine(String.format("%s properties = new %s();",
                helper.getFinalTypeName(LIST_STRING),
                helper.getFinalTypeName(ARRAYLIST_STRING)));

        // CollectionUtils.addAll(properties, propertyNames);
        bodyBuilder.appendFormalLine(String.format(
                "%s.addAll(properties, propertyNames);",
                helper.getFinalTypeName(COLLECTION_UTILS)));

        //
        bodyBuilder.appendFormalLine("");

        // // There must be at least one property name, otherwise return empty
        // Map
        bodyBuilder
                .appendFormalLine("// There must be at least one property name, otherwise return empty Map");
        // if(properties.isEmpty()) {
        bodyBuilder.appendFormalLine("if (properties.isEmpty()) {");
        bodyBuilder.indent();
        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        //
        bodyBuilder.appendFormalLine("");
        // // Iterate over given properties to get each property value
        bodyBuilder
                .appendFormalLine("// Iterate over given properties to get each property value");
        // BeanWrapper entityBean = new BeanWrapperImpl(entity);
        bodyBuilder.appendFormalLine(String.format(
                "%s entityBean = new %s(%s);",
                helper.getFinalTypeName(BEAN_WRAPPER),
                helper.getFinalTypeName(BEAN_WRAPPER_IMP), entityName));

        // for (String propertyName : properties) {
        bodyBuilder
                .appendFormalLine("for (String propertyName : properties) {");
        bodyBuilder.indent();
        // if (entityBean.isReadableProperty(propertyName)) {
        bodyBuilder
                .appendFormalLine("if (entityBean.isReadableProperty(propertyName)) {");
        bodyBuilder.indent();
        // Object propertyValue = null;
        bodyBuilder.appendFormalLine("Object propertyValue = null;");
        // try {
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        // propertyValue = entityBean.getPropertyValue(propertyName);
        bodyBuilder
                .appendFormalLine("propertyValue = entityBean.getPropertyValue(propertyName);");
        // } catch (Exception e){
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} catch (Exception e){");
        bodyBuilder.indent();
        // // TODO log warning
        bodyBuilder.appendFormalLine("// TODO log warning");
        // continue;
        bodyBuilder.appendFormalLine("continue;");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // propertyValuesMap.put(propertyName, propertyValue);
        bodyBuilder
                .appendFormalLine("propertyValuesMap.put(propertyName, propertyValue);");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");
    }

    /**
     * Gets <code>getRequestPropertyValues</code> method. <br>
     * This method returns a Map with bean properties which appears on a String
     * Iterator (usually from webRequest.getParametersNames())
     * 
     * @return
     */
    private MethodMetadata getGetOIDListMethod() {

        JavaSymbolName methodName = GET_OID_LIST_METHOD;
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(listOfEntityType);

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

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(listOfEntityName);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetOIDListBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, LIST_STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build method body for getOIDList method
     * 
     * @param bodyBuilder
     */
    private void buildGetOIDListBody(InvocableMemberBodyBuilder bodyBuilder) {
        // List<String> result = new ArrayList<String>(list.size());
        bodyBuilder.appendFormalLine(String.format(
                "%s result = new %s(%s.size());",
                helper.getFinalTypeName(LIST_STRING),
                helper.getFinalTypeName(ARRAYLIST_STRING), listOfEntityName));
        // for (Pet pet :list) {
        bodyBuilder.appendFormalLine(String.format("for (%s %s : %s) {",
                helper.getFinalTypeName(entity), entityName, listOfEntityName));
        bodyBuilder.indent();

        // result.add(conversionService_batch.convert(pet.getId(),
        // String.class));
        bodyBuilder.appendFormalLine(String.format(
                "result.add(%s.convert(%s.%s(), %s.class));",
                getConversionServiceField().getFieldName(), entityName, helper
                        .getGetterMethodNameForField(entityIdentifier
                                .getFieldName()), helper
                        .getFinalTypeName(JavaType.STRING)));

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return result;
        bodyBuilder.appendFormalLine("return result;");
    }

    /**
     * Create metadata for auto-wired convertionService field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getConversionServiceField() {
        if (conversionService == null) {
            JavaSymbolName curName = new JavaSymbolName(
                    "conversionService_batch");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null && !isConversionServiceField(currentField)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = new JavaSymbolName(
                        "conversionService_batch_");
                currentField = governorTypeDetails.getDeclaredField(newName);
                while (currentField != null
                        && !isConversionServiceField(currentField)) {
                    newName = new JavaSymbolName(newName.getSymbolName()
                            .concat("_"));
                    currentField = governorTypeDetails
                            .getDeclaredField(newName);
                }
                curName = newName;
            }
            if (currentField != null) {
                conversionService = currentField;
            }
            else {
                // create field
                List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                        1);
                annotations.add(new AnnotationMetadataBuilder(AUTOWIRED));
                // Using the FieldMetadataBuilder to create the field
                // definition.
                final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                        getId(), Modifier.PUBLIC, annotations, curName, // Field
                        CONVERSION_SERVICE); // Field type
                conversionService = fieldBuilder.build(); // Build and return a
                                                          // FieldMetadata
                // instance
            }
        }
        return conversionService;
    }

    /**
     * Check if filed is a valid conversion service
     * 
     * @param field
     * @return
     */
    private boolean isConversionServiceField(FieldMetadata field) {
        return field != null && field.getAnnotation(AUTOWIRED) != null
                && field.getFieldType().equals(CONVERSION_SERVICE);
    }

}
