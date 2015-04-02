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
package org.gvnix.addon.loupefield.addon;

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
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXPasswordHandlerSAFE} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class LoupefieldMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String REQUIRED_LABEL = "required";

    private static final String VALUE_LABEL = "value";

    // Constants
    private static final JavaType JAVA_TYPE_STRING = new JavaType(
            "java.lang.String");

    private static final String PROVIDES_TYPE_STRING = LoupefieldMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName SHOW_ONLY_METHOD_NAME = new JavaSymbolName(
            "showOnlyList");

    private static final JavaSymbolName FIND_USING_AJAX_METHOD_NAME = new JavaSymbolName(
            "findUsingAjax");

    private static final JavaType RESPONSE_LIST_MAP = new JavaType(
            "java.util.Map", 0, DataType.TYPE, null, Arrays.asList(
                    JAVA_TYPE_STRING, JAVA_TYPE_STRING));

    private static final JavaType RESPONSE_LIST = new JavaType(
            "java.util.List", 0, DataType.TYPE, null,
            Arrays.asList(RESPONSE_LIST_MAP));

    private static final JavaType RESPONSE_ENTITY = new JavaType(
            SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(RESPONSE_LIST));

    private static final JavaType ARRAY_LIST_MAP_STRING = new JavaType(
            "java.util.ArrayList", 0, DataType.TYPE, null,
            Arrays.asList(RESPONSE_LIST_MAP));

    private static final JavaType HASHMAP_STRING = new JavaType(
            "java.util.HashMap", 0, DataType.TYPE, null, Arrays.asList(
                    JAVA_TYPE_STRING, JAVA_TYPE_STRING));

    private static final JavaType MAP_STRING_OBJECT = new JavaType(
            "java.util.Map", 0, DataType.TYPE, null, Arrays.asList(
                    JAVA_TYPE_STRING, new JavaType("java.lang.Object")));

    private static final JavaType HASHMAP_STRING_OBJECT = new JavaType(
            "java.util.HashMap", 0, DataType.TYPE, null, Arrays.asList(
                    JAVA_TYPE_STRING, new JavaType("java.lang.Object")));

    private static final JavaType ITERATOR_STRING = new JavaType(
            "java.util.Iterator", 0, DataType.TYPE, null,
            Arrays.asList(JAVA_TYPE_STRING));

    private static final JavaType LIST_COLUMNDEF = new JavaType(
            "java.util.List", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType(
                    "com.github.dandelion.datatables.core.ajax.ColumnDef")));

    private static final JavaType ARRAYLIST_COLUMNDEF = new JavaType(
            "java.util.ArrayList", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType(
                    "com.github.dandelion.datatables.core.ajax.ColumnDef")));

    private static final JavaType DATASET_MAP_STRING = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DataSet", 0,
            DataType.TYPE, null, Arrays.asList(new JavaType("java.util.Map", 0,
                    DataType.TYPE, null, Arrays.asList(JAVA_TYPE_STRING,
                            JAVA_TYPE_STRING))));

    /**
     * Itd builder helper
     */
    private final ItdBuilderHelper helper;

    public LoupefieldMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaType entity) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding AUTOWIRED annotation
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);
        annotations
                .add(new AnnotationMetadataBuilder(SpringJavaType.AUTOWIRED));

        // Creating conversionService_loupe field
        builder.addField(getField("conversionService_loupe", null,
                new JavaType(
                        "org.springframework.core.convert.ConversionService"),
                Modifier.PUBLIC, annotations));

        // Adding showOnlyList method
        builder.addMethod(getShowOnlyListMethod());

        // Adding findUsingAjax method
        builder.addMethod(getFindUsingAjaxMethod(entity));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>showOnlyList</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getShowOnlyListMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(new JavaType(
                        "org.springframework.ui.Model"), new JavaType(
                        "javax.servlet.http.HttpServletRequest"));

        AnnotationMetadataBuilder pathMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        pathMetadataBuilder.addStringAttribute(VALUE_LABEL, "path");

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                pathMetadataBuilder.build()));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SHOW_ONLY_METHOD_NAME,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        requestMappingMetadataBuilder.addStringAttribute("params", "selector");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "text/html");

        annotations.add(requestMappingMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("uiModel"));
        parameterNames.add(new JavaSymbolName("request"));
        parameterNames.add(new JavaSymbolName("listPath"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildShowOnlyListMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, SHOW_ONLY_METHOD_NAME,
                JavaType.STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>findUsingAjax</code> method. <br>
     * 
     * @param entity
     * @return
     */
    private MethodMetadata getFindUsingAjaxMethod(JavaType entity) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(new JavaType(
                        "org.springframework.web.context.request.WebRequest"));

        // Adding search param
        AnnotationMetadataBuilder searchMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        searchMetadataBuilder.addStringAttribute(VALUE_LABEL, "_search_");
        searchMetadataBuilder.addBooleanAttribute(REQUIRED_LABEL, false);

        // Adding id param
        AnnotationMetadataBuilder idMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        idMetadataBuilder.addStringAttribute(VALUE_LABEL, "_id_");
        idMetadataBuilder.addBooleanAttribute(REQUIRED_LABEL, false);

        // Adding pkField param
        AnnotationMetadataBuilder pkFieldMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        pkFieldMetadataBuilder.addStringAttribute(VALUE_LABEL, "_pkField_");

        // Adding max param
        AnnotationMetadataBuilder maxMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        maxMetadataBuilder.addStringAttribute(VALUE_LABEL, "_max_");
        maxMetadataBuilder.addBooleanAttribute(REQUIRED_LABEL, false);
        maxMetadataBuilder.addStringAttribute("defaultValue", "3");

        // Adding caption param
        AnnotationMetadataBuilder captionMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        captionMetadataBuilder.addStringAttribute(VALUE_LABEL, "_caption_");
        captionMetadataBuilder.addBooleanAttribute(REQUIRED_LABEL, false);

        // Adding additionalFields param
        AnnotationMetadataBuilder additionalFieldsMB = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        additionalFieldsMB
                .addStringAttribute(VALUE_LABEL, "_additionalFields_");
        additionalFieldsMB.addBooleanAttribute(REQUIRED_LABEL, false);

        // Adding field param
        AnnotationMetadataBuilder fieldMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        fieldMetadataBuilder.addStringAttribute(VALUE_LABEL, "_field_");

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                searchMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                idMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                pkFieldMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.INT_OBJECT,
                maxMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                captionMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                additionalFieldsMB.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                fieldMetadataBuilder.build()));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(FIND_USING_AJAX_METHOD_NAME,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        requestMappingMetadataBuilder.addStringAttribute("params",
                "findUsingAjax");
        requestMappingMetadataBuilder.addStringAttribute("headers",
                "Accept=application/json");

        annotations.add(requestMappingMetadataBuilder);
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.RESPONSE_BODY));

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("request"));
        parameterNames.add(new JavaSymbolName("search"));
        parameterNames.add(new JavaSymbolName("id"));
        parameterNames.add(new JavaSymbolName("pkField"));
        parameterNames.add(new JavaSymbolName("maxResult"));
        parameterNames.add(new JavaSymbolName("caption"));
        parameterNames.add(new JavaSymbolName("additionalFields"));
        parameterNames.add(new JavaSymbolName("field"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        buildFindUsingAjaxMethodBody(bodyBuilder, entity);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, FIND_USING_AJAX_METHOD_NAME,
                RESPONSE_ENTITY, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>showOnlyList</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildShowOnlyListMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Do common datatables operations: get entity list filtered by request");
        bodyBuilder.appendFormalLine("// parameters");

        // Map<String, String> params = populateParametersMap(request);
        bodyBuilder.appendFormalLine(String.format(
                "%s<String, String> params = populateParametersMap(request);",
                helper.getFinalTypeName(new JavaType("java.util.Map"))));

        // // Get parentId information for details render
        bodyBuilder
                .appendFormalLine("// Get parentId information for details render");

        // String parentId = params.remove("_dt_parentId");
        bodyBuilder
                .appendFormalLine("String parentId = params.remove(\"_dt_parentId\");");

        // if (StringUtils.isNotBlank(parentId)) {
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(parentId)) {", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));

        bodyBuilder.indent();

        // uiModel.addAttribute("parentId", parentId);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"parentId\", parentId);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // String rowOnTopIds = params.remove("dtt_row_on_top_ids");
        bodyBuilder
                .appendFormalLine("String rowOnTopIds = params.remove(\"dtt_row_on_top_ids\");");

        // if (StringUtils.isNotBlank(rowOnTopIds)) {
        bodyBuilder
                .appendFormalLine("if (StringUtils.isNotBlank(rowOnTopIds)) {");
        bodyBuilder.indent();

        // uiModel.addAttribute("dtt_row_on_top_ids", rowOnTopIds);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"dtt_row_on_top_ids\", rowOnTopIds);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // String tableHashId = params.remove("dtt_parent_table_id_hash");
        bodyBuilder
                .appendFormalLine("String tableHashId = params.remove(\"dtt_parent_table_id_hash\");");

        // if (StringUtils.isNotBlank(tableHashId)) {
        bodyBuilder
                .appendFormalLine("if (StringUtils.isNotBlank(tableHashId)) {");
        bodyBuilder.indent();

        // uiModel.addAttribute("dtt_parent_table_id_hash", tableHashId);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"dtt_parent_table_id_hash\", tableHashId);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if (!params.isEmpty()) {
        bodyBuilder.appendFormalLine(" if (!params.isEmpty()) {");
        bodyBuilder.indent();

        // uiModel.addAttribute("baseFilter", params);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"baseFilter\", params);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Show only the list fragment (without footer, header, menu, etc.)");

        // return "forward:/WEB-INF/views/" + listPath + ".jspx";
        bodyBuilder
                .appendFormalLine("return \"forward:/WEB-INF/views/\" + listPath + \".jspx\";");

    }

    /**
     * Builds body method for <code>findUsingAjax</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildFindUsingAjaxMethodBody(
            InvocableMemberBodyBuilder bodyBuilder, JavaType entity) {

        // Adding comments
        bodyBuilder.appendFormalLine("// Declaring error utils");

        // List<Map<String, String>> errorList = new
        // ArrayList<Map<String,String>>();
        bodyBuilder.appendFormalLine(String.format("%s errorList = new %s();",
                helper.getFinalTypeName(RESPONSE_LIST),
                helper.getFinalTypeName(ARRAY_LIST_MAP_STRING)));

        // Map<String, String> error = new HashMap<String, String>();
        bodyBuilder.appendFormalLine(String.format("%s error = new %s();",
                helper.getFinalTypeName(RESPONSE_LIST_MAP),
                helper.getFinalTypeName(HASHMAP_STRING)));

        // HttpHeaders headers = new HttpHeaders();
        bodyBuilder.appendFormalLine(String.format(
                "%s headers = new HttpHeaders();", helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.http.HttpHeaders"))));
        // headers.add("Content-Type", "application/json; charset=utf-8");
        bodyBuilder
                .appendFormalLine("headers.add(\"Content-Type\",\"application/json; charset=utf-8\");");

        // Adding comments
        bodyBuilder.appendFormalLine("// Getting Entity");

        // BeanWrapper xxxxBean = new BeanWrapperImpl(XXX.class);
        bodyBuilder.appendFormalLine(String.format(
                "%s %sBean = new %s(%s.class);", helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.beans.BeanWrapper")),
                helper.getFinalTypeName(entity).toLowerCase(), helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.beans.BeanWrapperImpl")),
                helper.getFinalTypeName(entity)));

        // Adding comments
        bodyBuilder.appendFormalLine("// Getting field");

        // Class targetEntity = visitBean.getPropertyType(field);
        bodyBuilder.appendFormalLine(String.format(
                "Class targetEntity = %sBean.getPropertyType(field);", helper
                        .getFinalTypeName(entity).toLowerCase()));

        // BeanWrapper targetBean = new BeanWrapperImpl(targetEntity);
        bodyBuilder
                .appendFormalLine("BeanWrapper targetBean = new BeanWrapperImpl(targetEntity);");

        // Map<String, Object> baseSearchValuesMap = new HashMap<String,
        // Object>();
        bodyBuilder.appendFormalLine(String.format(
                "%s baseSearchValuesMap = new %s();",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));

        // String paramName;
        bodyBuilder.appendFormalLine("String paramName;");

        // Iterator<String> iter = request.getParameterNames();
        bodyBuilder.appendFormalLine(String.format(
                "%s iter = request.getParameterNames();",
                helper.getFinalTypeName(ITERATOR_STRING)));

        // while (iter.hasNext()) {
        bodyBuilder.appendFormalLine("while (iter.hasNext()) {");
        bodyBuilder.indent();

        // paramName = iter.next();
        bodyBuilder.appendFormalLine("paramName = iter.next();");

        // if (targetBean.isReadableProperty(paramName)) {
        bodyBuilder
                .appendFormalLine("if (targetBean.isReadableProperty(paramName)) {");
        bodyBuilder.indent();

        // baseSearchValuesMap.put(paramName,conversionService_loupe.convert(request.getParameter(paramName),targetBean.getPropertyType(paramName)));
        bodyBuilder
                .appendFormalLine("baseSearchValuesMap.put(paramName,conversionService_loupe.convert(request.getParameter(paramName),targetBean.getPropertyType(paramName)));");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder.appendFormalLine("// Getting Entity Manager");

        // EntityManager targetEntityManager = null;
        bodyBuilder.appendFormalLine(String.format(
                "%s targetEntityManager = null;", helper
                        .getFinalTypeName(new JavaType(
                                "javax.persistence.EntityManager"))));
        // try {
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();

        // Method entityManagerMethod = targetEntity.getMethod("entityManager");
        bodyBuilder
                .appendFormalLine(String
                        .format("%s entityManagerMethod = targetEntity.getMethod(\"entityManager\");",
                                helper.getFinalTypeName(new JavaType(
                                        "java.lang.reflect.Method"))));

        // targetEntityManager = (EntityManager)
        // entityManagerMethod.invoke(null);
        bodyBuilder
                .appendFormalLine("targetEntityManager = (EntityManager) entityManagerMethod.invoke(null);");

        bodyBuilder.indentRemove();

        // } catch (Exception e) {
        bodyBuilder.appendFormalLine("} catch (Exception e) {");
        bodyBuilder.indent();

        // return new ResponseEntity<List<Map<String, String>>>(null,
        // headers,HttpStatus.INTERNAL_SERVER_ERROR);
        bodyBuilder.appendFormalLine(String.format(
                "return new %s(null, headers,%s.INTERNAL_SERVER_ERROR);",
                helper.getFinalTypeName(RESPONSE_ENTITY), helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.http.HttpStatus"))));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder.appendFormalLine("// Creating ColumnDef - ID COLUMN");

        // List<ColumnDef> columnDefs = new ArrayList<ColumnDef>();
        bodyBuilder.appendFormalLine(String.format("%s columnDefs = new %s();",
                helper.getFinalTypeName(LIST_COLUMNDEF),
                helper.getFinalTypeName(ARRAYLIST_COLUMNDEF)));

        // ColumnDef idColumn = new ColumnDef();
        bodyBuilder.appendFormalLine("ColumnDef idColumn = new ColumnDef();");

        // idColumn.setName(pkField);
        bodyBuilder.appendFormalLine("idColumn.setName(pkField);");

        // idColumn.setFilterable(true);
        bodyBuilder.appendFormalLine("idColumn.setFilterable(true);");

        // columnDefs.add(idColumn);
        bodyBuilder.appendFormalLine("columnDefs.add(idColumn);");

        // Adding comments
        bodyBuilder.appendFormalLine("// Creating more columns to search");

        // if (StringUtils.isNotBlank(additionalFields)) {
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(additionalFields)) {", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));
        bodyBuilder.indent();

        // String[] fields = StringUtils.split(additionalFields, ",");
        bodyBuilder
                .appendFormalLine("String[] fields = StringUtils.split(additionalFields, \",\");");

        // if (fields.length > 0) {
        bodyBuilder.appendFormalLine("if (fields.length > 0) {");
        bodyBuilder.indent();

        // for (String aditionalField : fields) {
        bodyBuilder.appendFormalLine("for (String aditionalField : fields) {");
        bodyBuilder.indent();

        // ColumnDef aditionalColumn = new ColumnDef();
        bodyBuilder
                .appendFormalLine("ColumnDef aditionalColumn = new ColumnDef();");

        // aditionalColumn.setName(aditionalField);
        bodyBuilder
                .appendFormalLine("aditionalColumn.setName(aditionalField);");

        // aditionalColumn.setFilterable(true);
        bodyBuilder.appendFormalLine("aditionalColumn.setFilterable(true);");

        // columnDefs.add(aditionalColumn);
        bodyBuilder.appendFormalLine("columnDefs.add(aditionalColumn);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // SearchResults<?> searchResult = null;
        bodyBuilder.appendFormalLine(String.format(
                "%s<?> searchResult = null;",
                helper.getFinalTypeName(new JavaType(
                        "org.gvnix.web.datatables.query.SearchResults"))));

        // if (id != null) {
        bodyBuilder.appendFormalLine("if (id != null) {");
        bodyBuilder.indent();

        // if not select element, id is empty. Return item with empty values
        bodyBuilder
                .appendFormalLine("// if not select element, id is empty. Return item with empty values ");

        // if(id.isEmpty()){
        bodyBuilder.appendFormalLine("if(id.isEmpty()){");
        bodyBuilder.indent();

        // List<Map<String, String>> resultRows = new
        // ArrayList<Map<String,String>>();
        bodyBuilder
                .appendFormalLine("List<Map<String, String>> resultRows = new ArrayList<Map<String,String>>();");

        // Map<String, String> rowItem = new HashMap<String, String>();
        bodyBuilder
                .appendFormalLine("Map<String, String> rowItem = new HashMap<String, String>();");

        // Iterator<ColumnDef> iterColumns = columnDefs.iterator();
        bodyBuilder
                .appendFormalLine("Iterator<ColumnDef> iterColumns = columnDefs.iterator();");

        // while(iterColumns.hasNext()){
        bodyBuilder.appendFormalLine("while(iterColumns.hasNext()){");
        bodyBuilder.indent();

        // ColumnDef columnDef = iterColumns.next();
        bodyBuilder
                .appendFormalLine("ColumnDef columnDef = iterColumns.next();");

        // rowItem.put(columnDef.getName(),"");
        bodyBuilder.appendFormalLine("rowItem.put(columnDef.getName(),\"\");");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // rowItem.put("__caption__","");
        bodyBuilder.appendFormalLine("rowItem.put(\"__caption__\",\"\");");

        // resultRows.add(rowItem);
        bodyBuilder.appendFormalLine("resultRows.add(rowItem);");

        // return new ResponseEntity<List<Map<String,
        // String>>>(resultRows,headers, HttpStatus.OK);
        bodyBuilder
                .appendFormalLine("return new ResponseEntity<List<Map<String, String>>>(resultRows,headers, HttpStatus.OK);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // targetBean.setConversionService(conversionService_loupe);
        bodyBuilder
                .appendFormalLine("targetBean.setConversionService(conversionService_loupe);");

        // Class idType = targetBean.getPropertyType(pkField);
        bodyBuilder
                .appendFormalLine("Class idType = targetBean.getPropertyType(pkField);");

        // StringBuffer sqlBuf = new StringBuffer("");
        bodyBuilder
                .appendFormalLine(String.format(" %s sqlBuf = new %s(\"\");",
                        helper.getFinalTypeName(new JavaType(
                                "java.lang.StringBuffer")), helper
                                .getFinalTypeName(new JavaType(
                                        "java.lang.StringBuffer"))));

        // if(idType.equals(String.class)){
        bodyBuilder.appendFormalLine("if(idType.equals(String.class)){");
        bodyBuilder.indent();
        // //String with case insensitive
        bodyBuilder.appendFormalLine("//String with case insensitive");

        // sqlBuf.append("SELECT o FROM %s o WHERE UPPER(o.%s) = UPPER(:id)");
        bodyBuilder
                .appendFormalLine("sqlBuf.append(\"SELECT o FROM %s o WHERE UPPER(o.%s) = UPPER(:id)\");");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");

        bodyBuilder.indent();

        // sqlBuf.append("SELECT o FROM %s o WHERE o.%s = :id");
        bodyBuilder
                .appendFormalLine("sqlBuf.append(\"SELECT o FROM %s o WHERE o.%s = :id\");");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // // set baseSearch parameters to query
        bodyBuilder.appendFormalLine("// set baseSearch parameters to query");

        // if(baseSearchValuesMap != null && !baseSearchValuesMap.isEmpty()){
        bodyBuilder
                .appendFormalLine("if(baseSearchValuesMap != null && !baseSearchValuesMap.isEmpty()){");
        bodyBuilder.indent();

        // for (Entry<String, Object> entry : baseSearchValuesMap.entrySet()) {
        bodyBuilder
                .appendFormalLine(String
                        .format("for (%s<String, Object> entry : baseSearchValuesMap.entrySet()) {",
                                helper.getFinalTypeName(new JavaType(
                                        "java.util.Map.Entry"))));
        bodyBuilder.indent();

        // sqlBuf.append(" AND ").append(entry.getKey()).append(" = :").append(entry.getKey());
        bodyBuilder
                .appendFormalLine("sqlBuf.append(\" AND \").append(entry.getKey()).append(\" = :\").append(entry.getKey());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // String query =
        // String.format(sqlBuf.toString(),targetEntity.getSimpleName(),
        // pkField);
        bodyBuilder
                .appendFormalLine("String query = String.format(sqlBuf.toString(),targetEntity.getSimpleName(), pkField);");

        // TypedQuery<Object> q =
        // targetEntityManager.createQuery(query,targetEntity);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s<Object> q = targetEntityManager.createQuery(query,targetEntity);",
                                helper.getFinalTypeName(new JavaType(
                                        "javax.persistence.TypedQuery"))));

        // q.setParameter("id", targetBean.convertIfNecessary(id, idType));
        bodyBuilder
                .appendFormalLine("q.setParameter(\"id\", targetBean.convertIfNecessary(id, idType));");

        // set baseSearch values
        bodyBuilder.appendFormalLine("// set baseSearch values");

        // if (baseSearchValuesMap != null && !baseSearchValuesMap.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (baseSearchValuesMap != null && !baseSearchValuesMap.isEmpty()) {");
        bodyBuilder.indent();

        // for (Entry<String, Object> entry : baseSearchValuesMap.entrySet()) {
        bodyBuilder
                .appendFormalLine(String
                        .format("for (%s<String, Object> entry : baseSearchValuesMap.entrySet()) {",
                                helper.getFinalTypeName(new JavaType(
                                        "java.util.Map.Entry"))));
        bodyBuilder.indent();

        // q.setParameter(entry.getKey(), entry.getValue());
        bodyBuilder
                .appendFormalLine("q.setParameter(entry.getKey(), entry.getValue());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // searchResult = new SearchResults(q.getResultList(), 1, false, 0,
        // 1, 1);
        bodyBuilder
                .appendFormalLine("searchResult = new SearchResults(q.getResultList(), 1, false, 0, 1, 1);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();

        // DatatablesCriterias criterias = new DatatablesCriterias(search,
        // 0,maxResult, columnDefs, columnDefs, null);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s criterias = new DatatablesCriterias(search, 0,maxResult, columnDefs, columnDefs, null);",
                                helper.getFinalTypeName(new JavaType(
                                        "com.github.dandelion.datatables.core.ajax.DatatablesCriterias"))));

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Get all columns with results in id column and additional columns");

        // searchResult =
        // DatatablesUtils.findByCriteria(targetEntity,targetEntityManager,
        // criterias, baseSearchValuesMap);
        bodyBuilder
                .appendFormalLine(String
                        .format("searchResult = %s.findByCriteria(targetEntity,targetEntityManager, criterias, baseSearchValuesMap);",
                                helper.getFinalTypeName(new JavaType(
                                        "org.gvnix.web.datatables.util.DatatablesUtils"))));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Getting Result with only id column and additional columns");

        // DataSet<Map<String, String>> result =
        // DatatablesUtils.populateDataSet(searchResult.getResults(), pkField,
        // searchResult.getResultsCount(), searchResult.getResultsCount(),
        // columnDefs, null, conversionService_loupe);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s result = DatatablesUtils.populateDataSet(searchResult.getResults(), pkField, searchResult.getResultsCount(), searchResult.getResultsCount(), columnDefs, null, conversionService_loupe);",
                                helper.getFinalTypeName(DATASET_MAP_STRING)));

        // Adding comments
        bodyBuilder.appendFormalLine("// If No Data Found, return message");

        // if (result.getTotalDisplayRecords() == 0) {
        bodyBuilder
                .appendFormalLine("if (result.getTotalDisplayRecords() == 0) {");
        bodyBuilder.indent();

        // error.put("Error", "No Data Found");
        bodyBuilder
                .appendFormalLine("error.put(\"Error\", \"No Data Found\");");

        // errorList.add(error);
        bodyBuilder.appendFormalLine("errorList.add(error);");

        // return new ResponseEntity<List<Map<String, String>>>(errorList,
        // headers, HttpStatus.INTERNAL_SERVER_ERROR);
        bodyBuilder
                .appendFormalLine(String
                        .format("return new %s(errorList, headers, HttpStatus.INTERNAL_SERVER_ERROR);",
                                helper.getFinalTypeName(RESPONSE_ENTITY)));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // List<Map<String, String>> resultRows = result.getRows();
        bodyBuilder
                .appendFormalLine("List<Map<String, String>> resultRows = result.getRows();");

        // Map<String, String> captionRow = new HashMap<String, String>();
        bodyBuilder
                .appendFormalLine("Map<String, String> captionRow = new HashMap<String, String>();");

        // Adding comments
        bodyBuilder
                .appendFormalLine("// If caption is blank, use ConversionService to show item as String");

        // boolean notCaption = StringUtils.isBlank(caption);
        bodyBuilder
                .appendFormalLine("boolean notCaption = StringUtils.isBlank(caption);");

        // BeanWrapperImpl resultBean = new BeanWrapperImpl(targetEntity);
        bodyBuilder
                .appendFormalLine("BeanWrapperImpl resultBean = new BeanWrapperImpl(targetEntity);");

        // if (!notCaption && !resultBean.isReadableProperty(caption)) {
        bodyBuilder
                .appendFormalLine("if (!notCaption && !resultBean.isReadableProperty(caption)) {");
        bodyBuilder.indent();

        // error.put("Error", caption + " is not a valid field");
        bodyBuilder
                .appendFormalLine("error.put(\"Error\", caption + \" is not a valid field\");");

        // errorList.add(error);
        bodyBuilder.appendFormalLine("errorList.add(error);");

        // return new ResponseEntity<List<Map<String, String>>>(errorList,
        // headers, HttpStatus.INTERNAL_SERVER_ERROR);
        bodyBuilder
                .appendFormalLine("return new ResponseEntity<List<Map<String, String>>>(errorList, headers, HttpStatus.INTERNAL_SERVER_ERROR);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // List<?> results = searchResult.getResults();
        bodyBuilder
                .appendFormalLine("List<?> results = searchResult.getResults();");

        // Iterator<?> it = results.iterator();
        bodyBuilder.appendFormalLine("Iterator<?> it = results.iterator();");

        // Iterator<Map<String, String>> it2 = resultRows.iterator();
        bodyBuilder
                .appendFormalLine("Iterator<Map<String, String>> it2 = resultRows.iterator();");

        // Object rowCaption;
        bodyBuilder.appendFormalLine("Object rowCaption;");

        // while (it.hasNext() && it2.hasNext()) {
        bodyBuilder.appendFormalLine("while (it.hasNext() && it2.hasNext()) {");
        bodyBuilder.indent();

        // Object rowResult = it.next();
        bodyBuilder.appendFormalLine("Object rowResult = it.next();");

        // Map<String, String> rowItem = it2.next();
        bodyBuilder
                .appendFormalLine("Map<String, String> rowItem = it2.next();");

        // resultBean.setWrappedInstance(rowResult);
        bodyBuilder
                .appendFormalLine("resultBean.setWrappedInstance(rowResult);");

        // if (notCaption) {
        bodyBuilder.appendFormalLine("if (notCaption) {");
        bodyBuilder.indent();

        // rowCaption = rowResult;
        bodyBuilder.appendFormalLine("rowCaption = rowResult;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();

        // rowCaption = resultBean.getPropertyValue(caption);
        bodyBuilder
                .appendFormalLine("rowCaption = resultBean.getPropertyValue(caption);");

        // if (rowCaption == null) {
        bodyBuilder.appendFormalLine("if (rowCaption == null) {");
        bodyBuilder.indent();

        // rowCaption = rowResult;
        bodyBuilder.appendFormalLine("rowCaption = rowResult;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // rowItem.put("__caption__",
        // conversionService_loupe.convert(rowCaption, String.class));
        bodyBuilder
                .appendFormalLine("rowItem.put(\"__caption__\", conversionService_loupe.convert(rowCaption, String.class));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return new ResponseEntity<List<Map<String, String>>>(resultRows,
        // headers, HttpStatus.OK);
        bodyBuilder
                .appendFormalLine("return new ResponseEntity<List<Map<String, String>>>(resultRows, headers, HttpStatus.OK);");

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

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getField(String name, String value,
            JavaType javaType, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = new JavaSymbolName(name);
        String initializer = value;
        FieldMetadata field = getOrCreateField(curName, javaType, initializer,
                modifier, annotations);
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

}
