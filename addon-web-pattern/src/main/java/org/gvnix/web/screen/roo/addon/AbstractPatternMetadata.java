/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
 * Valenciana
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.screen.roo.addon;

import java.beans.Introspector;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public abstract class AbstractPatternMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName PAGE_PARAM_NAME = new JavaSymbolName(
            "page");
    private static final JavaSymbolName SIZE_PARAM_NAME = new JavaSymbolName(
            "size");
    private static final JavaSymbolName PARAMS_ATTRIBUTE_NAME = new JavaSymbolName(
            "params");
    private static final JavaSymbolName VALUE_ATTRIBUTE_NAME = new JavaSymbolName(
            "value");
    private static final JavaType REQUEST_MAPPING_ANNOTATION_TYPE = new JavaType(
            "org.springframework.web.bind.annotation.RequestMapping");
    protected WebScaffoldMetadata webScaffoldMetadata;
    private SortedMap<JavaType, JavaTypeMetadataDetails> relatedEntities;
    protected JavaType entity;
    protected JavaTypeMetadataDetails entityTypeDetails;
    protected List<String> patterns;
    private SortedMap<JavaType, JavaTypeMetadataDetails> relatedFields;
    private Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedDates;
    protected List<MethodMetadata> controllerMethods;
    private List<FieldMetadata> controllerFields;
    protected Map<JavaSymbolName, DateTimeFormatDetails> entityDateTypes;
    private String aspectPackage;

    public AbstractPatternMetadata(
            String mid,
            JavaType aspect,
            PhysicalTypeMetadata controllerMetadata,
            MemberDetails controllerDetails,
            WebScaffoldMetadata webScaffoldMetadata,
            List<StringAttributeValue> patterns,
            PhysicalTypeMetadata entityMetadata,
            SortedMap<JavaType, JavaTypeMetadataDetails> relatedEntities,
            SortedMap<JavaType, JavaTypeMetadataDetails> relatedFields,
            Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedDates,
            Map<JavaSymbolName, DateTimeFormatDetails> entityDateTypes) {

        super(mid, aspect, controllerMetadata);

        // Required parameters: Web scaffold information and relatedEntities
        // details (entity and optional master entity)
        Validate.notNull(webScaffoldMetadata, "Web scaffold metadata required");
        Validate.notNull(relatedEntities,
                "Related relatedEntities type metadata details required");

        if (!isValid()) {

            // This metadata instance not be already produced at the time of
            // instantiation (will retry)
            return;
        }

        this.webScaffoldMetadata = webScaffoldMetadata;
        this.entity = entityMetadata.getMemberHoldingTypeDetails().getName();
        this.relatedEntities = relatedEntities;
        this.entityTypeDetails = relatedEntities.get(entity);
        this.entityDateTypes = entityDateTypes;

        this.controllerMethods = controllerDetails.getMethods();
        this.controllerFields = controllerDetails.getFields();

        Validate.notNull(entityTypeDetails,
                "Metadata holder required for form backing type: " + entity);
        Validate.notNull(entityTypeDetails.getPersistenceDetails(),
                "PersistenceMetadata details required for form backing type: "
                        + entity);

        WebScaffoldAnnotationValues webScaffoldValues = new WebScaffoldAnnotationValues(
                controllerMetadata);
        if (webScaffoldValues.isPopulateMethods()) {

            filterAlreadyPopulatedTypes(relatedFields);
        }

        this.relatedFields = relatedFields;
        this.relatedDates = relatedDates;

        // TODO: Take care of attributes "create, update, delete" in
        // RooWebScaffold annotation
        List<String> definedPatternsList = new ArrayList<String>();
        for (StringAttributeValue definedPattern : patterns) {

            definedPatternsList.add(definedPattern.getValue());
        }

        this.aspectPackage = aspect.getPackage().getFullyQualifiedPackageName();

        this.patterns = Collections.unmodifiableList(definedPatternsList);

        builder.addField(getDefinedPatternField());

        List<String> tabularPatterns = getPatternTypeDefined(
                WebPatternType.tabular, this.patterns);
        if (!tabularPatterns.isEmpty()) {

            // TODO findAll method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails().getFindAllMethod() == null) {

                // TODO: If no find all method, all other patterns are not
                // generated ?
                return;
            }

            for (String tabularPattern : tabularPatterns) {

                builder.addMethod(getTabularMethod(tabularPattern));
            }

            builder.addMethod(getCreateListMethod());
            builder.addMethod(getUpdateListMethod());
            builder.addMethod(getDeleteListMethod());
            builder.addMethod(getFilterListMethod());
            builder.addMethod(getRefererRedirectMethod());
        }

        List<String> registerPatterns = getPatternTypeDefined(
                WebPatternType.register, this.patterns);
        if (!registerPatterns.isEmpty()) {

            // TODO findEntries method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails()
                    .getFindEntriesMethod() == null) {

                // TODO: If no find entries method, all other patterns are not
                // generated ?
                return;
            }

            for (String registerPattern : registerPatterns) {

                builder.addMethod(getRegisterMethod(registerPattern));
                builder.addMethod(getDeleteMethod(registerPattern));
            }

            builder.addMethod(getRefererQueryMethod());
        }

        List<String> tabularEditPatterns = getPatternTypeDefined(
                WebPatternType.tabular_edit_register, this.patterns);
        if (!tabularEditPatterns.isEmpty()) {

            // TODO findAll method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails().getFindAllMethod() == null) {

                // TODO: If no find all method, all other patterns are not
                // generated ?
                return;
            }

            for (String tabularEditPattern : tabularEditPatterns) {

                builder.addMethod(getTabularMethod(tabularEditPattern));
            }

            builder.addMethod(getDeleteListMethod());
            builder.addMethod(getFilterListMethod());
            builder.addMethod(getRefererRedirectMethod());
            builder.addMethod(getRefererQueryMethod());
        }
    }

    protected MethodMetadata getDeleteMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("deletePattern"
                + patternName);

        FieldMetadata formBackingObjectIdField = entityTypeDetails
                .getPersistenceDetails().getIdentifierField();
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrPathVar = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPathVar.add(new StringAttributeValue(VALUE_ATTRIBUTE_NAME,
                formBackingObjectIdField.getFieldName().getSymbolName()));
        List<AnnotationMetadata> methodAttrPathVarAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttPathVarAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.PathVariable"),
                reqParamAttrPathVar);
        methodAttrPathVarAnnotations.add(methodAttPathVarAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(formBackingObjectIdField
                .getFieldType(), methodAttrPathVarAnnotations));

        methodParamTypes.add(getPatternRequestParam(false).getValue());

        List<AnnotationAttributeValue<?>> reqParamAttrPage = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPage.add(new StringAttributeValue(VALUE_ATTRIBUTE_NAME,
                "page"));
        reqParamAttrPage.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), false));
        List<AnnotationMetadata> methodAttrPageAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrPageAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrPage);
        methodAttrPageAnnotations.add(methodAttrPageAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(JavaType.INT_OBJECT,
                methodAttrPageAnnotations));

        List<AnnotationAttributeValue<?>> reqParamAttrSize = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrSize.add(new StringAttributeValue(VALUE_ATTRIBUTE_NAME,
                "size"));
        reqParamAttrSize.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), false));
        List<AnnotationMetadata> methodAttrSizeAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrSizeAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrSize);
        methodAttrSizeAnnotations.add(methodAttrSizeAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(JavaType.INT_OBJECT,
                methodAttrSizeAnnotations));

        Entry<JavaSymbolName, AnnotatedJavaType> modelRequestParam = getModelRequestParam();
        methodParamTypes.add(modelRequestParam.getValue());
        Entry<JavaSymbolName, AnnotatedJavaType> httpServletRequest = getHttpServletRequest();
        methodParamTypes.add(httpServletRequest.getValue());

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        // id, pattern, page, size, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(formBackingObjectIdField.getFieldName());
        Entry<JavaSymbolName, AnnotatedJavaType> patternRequestParam = getPatternRequestParam(false);
        methodParamNames.add(patternRequestParam.getKey());
        methodParamNames.add(PAGE_PARAM_NAME);
        methodParamNames.add(SIZE_PARAM_NAME);
        methodParamNames.add(modelRequestParam.getKey());
        methodParamNames.add(httpServletRequest.getKey());

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityTypeDetails.getPlural();

        bodyBuilder.appendFormalLine("delete(".concat(
                formBackingObjectIdField.getFieldName().getSymbolName())
                .concat(", page, size, uiModel);"));
        bodyBuilder.appendFormalLine("return \""
                .concat("redirect:/")
                .concat(entityNamePlural.toLowerCase())
                .concat("?gvnixform&\" + refererQuery("
                        + httpServletRequest.getKey() + ");"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                VALUE_ATTRIBUTE_NAME, "/{"
                        + formBackingObjectIdField.getFieldName()
                                .getSymbolName() + "}"));
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(getPatternParamRequestMapping(patternName));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        PARAMS_ATTRIBUTE_NAME, paramValues));
        requestMappingAttributes
                .add(getMethodRequestMapping(RequestMethod.DELETE));
        requestMappingAttributes.add(getProducesParamRequestMapping());
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);

        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Get refererQuery method.
     * <p>
     * Get referer url from request header and return query part without "form"
     * parameter. Remove "form" parameter is required to avoid mapping with
     * create and update Roo patterns.
     * </p>
     * 
     * @return Referer url query part without "form" param
     */
    protected MethodMetadata getRefererQueryMethod() {

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("refererQuery");

        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        Entry<JavaSymbolName, AnnotatedJavaType> httpServletRequest = getHttpServletRequest();
        methodParamTypes.add(httpServletRequest.getValue());

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(httpServletRequest.getKey());

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("String url = \"\";");
        bodyBuilder.appendFormalLine("String referer = "
                + httpServletRequest.getKey() + ".getHeader(\"Referer\");");

        JavaType stringUtils = new JavaType(
                "org.springframework.util.StringUtils");
        bodyBuilder.appendFormalLine("if (".concat(
                stringUtils.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                ".hasText(referer)) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        JavaType netURL = new JavaType("java.net.URL");
        bodyBuilder.appendFormalLine("String[] params = new ".concat(
                netURL.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                "(referer).getQuery().split(\"&\");"));
        bodyBuilder.appendFormalLine("for (String param : params) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (!param.equals(\""
                + getFormParamRequestMapping(true).getValue() + "\")) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("url = url.concat(param).concat(\"&\");");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("if (url.endsWith(\"&\")) { url = url.substring(0, url.length() - 1); }");
        bodyBuilder.indentRemove();
        JavaType exception = new JavaType("java.lang.Exception");
        bodyBuilder.appendFormalLine("} catch ( ".concat(
                exception.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                " e ) {"));
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return url;");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), 0, methodName, JavaType.STRING, methodParamTypes,
                methodParamNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    protected List<AnnotationMetadataBuilder> getRequestMappingAnnotationCreateUpdate(
            RequestMethod requestMethod, String patternName) {
        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(getPatternParamRequestMapping(patternName));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        PARAMS_ATTRIBUTE_NAME, paramValues));
        requestMappingAttributes.add(getMethodRequestMapping(requestMethod));
        requestMappingAttributes.add(getProducesParamRequestMapping());
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        return annotations;
    }

    /**
     * Enumeration of HTTP Request method types
     */
    public enum RequestMethod {
        DELETE, PUT, POST, GET;
    }

    /**
     * Enumeration of some persistence method names
     */
    public enum PersistenceMethod {
        REMOVE("remove"), PERSIST("persist"), MERGE("merge");

        private String name;

        private PersistenceMethod(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * If there is patterns of given WebPatternType defined in GvNIXPattern it
     * returns its names, empty list otherwise
     * 
     * @param patternType
     * @param definedPatternsList
     * @return Some type pattern names list
     */
    protected List<String> getPatternTypeDefined(WebPatternType patternType,
            List<String> definedPatternsList) {
        List<String> patternList = new ArrayList<String>();
        for (String definedPattern : definedPatternsList) {
            if (definedPattern.split("=")[1].equalsIgnoreCase(patternType
                    .name())) {
                patternList.add(definedPattern.split("=")[0]);
            }
        }
        return patternList;
    }

    protected FieldMetadataBuilder getGvNIXPatternField() {
        return new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "PATTERN_PARAM_NAME"), JavaType.STRING, "\"" + getPattern()
                + "\"");
    }

    protected FieldMetadata getDefinedPatternField() {
        String definedPatternIds = "";
        for (String definedPattern : patterns) {
            if (definedPatternIds.length() > 0) {
                definedPatternIds = definedPatternIds.concat(", ");
            }
            definedPatternIds = definedPatternIds.concat("\"")
                    .concat(definedPattern.split("=")[0]).concat("\"");
        }

        FieldMetadataBuilder fmb = null;

        JavaSymbolName fieldName = new JavaSymbolName("definedPatterns");
        JavaType stringArray = new JavaType(
                JavaType.STRING.getFullyQualifiedTypeName(), 1, DataType.TYPE,
                null, null);

        FieldMetadata field = fieldExists(fieldName, stringArray);
        if (field != null) {
            String initializer = field.getFieldInitializer();
            initializer = initializer.replace("{", "").replace("}", "")
                    .replace(" ", "").replace("\"", "");
            for (String pattern : initializer.split(",")) {
                if (!definedPatternIds.contains(pattern)) {
                    if (definedPatternIds.length() > 0) {
                        definedPatternIds = definedPatternIds.concat(", ");
                    }
                    definedPatternIds = definedPatternIds.concat("\"")
                            .concat(pattern).concat("\"");
                }
            }
            fmb = new FieldMetadataBuilder(field);
            fmb.setFieldInitializer("{ ".concat(definedPatternIds).concat(" }"));
        }

        if (fmb == null) {
            fmb = new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                    | Modifier.STATIC | Modifier.FINAL, fieldName, stringArray,
                    "{ ".concat(definedPatternIds).concat(" }"));
        }

        field = fmb.build();
        controllerFields.add(field);
        return field;
    }

    private FieldMetadata fieldExists(JavaSymbolName fieldName,
            JavaType fieldType) {
        for (FieldMetadata fieldMetadata : controllerFields) {
            if (fieldMetadata.getFieldName().equals(fieldName)
                    && fieldType.equals(fieldMetadata.getFieldType())) {
                return fieldMetadata;
            }
        }
        return null;
    }

    protected void addStaticFields() {
        FieldMetadataBuilder field = new FieldMetadataBuilder(getId(),
                Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
                new JavaSymbolName("INDEX_PARAM_NAME"), JavaType.STRING,
                "\"index\"");
        builder.addField(field);

        field = new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "FORM_PARAM_NAME"), JavaType.STRING, "\"gvnixform\"");
        builder.addField(field);

        field = new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "REFERER_PARAM_NAME"), JavaType.STRING, "\"Referer\"");
        builder.addField(field);
    }

    /**
     * Returns the method handling requests of a given gvnixpattern of type
     * tabular
     * 
     * @return
     */
    protected MethodMetadata getTabularMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("tabular" + patternName);

        // Define method parameter types
        // @RequestParam(value = "gvnixpattern", required = true) String
        // pattern, HttpServletRequest req, Model uiModel)
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        methodParamTypes.add(getPatternRequestParam(true).getValue());
        Entry<JavaSymbolName, AnnotatedJavaType> modelRequestParam = getModelRequestParam();
        methodParamTypes.add(modelRequestParam.getValue());
        Entry<JavaSymbolName, AnnotatedJavaType> httpServletRequest = getHttpServletRequest();
        methodParamTypes.add(httpServletRequest.getValue());

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        // pattern, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        Entry<JavaSymbolName, AnnotatedJavaType> patternRequestParam = getPatternRequestParam(false);
        methodParamNames.add(patternRequestParam.getKey());
        methodParamNames.add(modelRequestParam.getKey());
        methodParamNames.add(httpServletRequest.getKey());

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityTypeDetails.getPlural();

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(entity);

        // Add date validation pattern to model if some date type field exists
        if (!entityDateTypes.isEmpty()) {
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
        }

        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        bodyBuilder.appendFormalLine(javaUtilList
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" ")
                .concat(entityNamePlural.toLowerCase())
                .concat(" = ")
                .concat(entity.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()))
                .concat(".")
                .concat(entityTypeDetails.getPersistenceDetails()
                        .getFindAllMethod().getMethodName().concat("();")));

        bodyBuilder
                .appendFormalLine("if ("
                        .concat(entityNamePlural.toLowerCase())
                        .concat(".isEmpty() && httpServletRequest.getSession().getAttribute(\"dialogMessage\") == null) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                entityNamePlural.toLowerCase()).concat("Tab\", null);"));

        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Info,
                "message_entitynotfound_problemdescription");

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat(
                "/\".concat(" + getPattern() + ");"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // May we need to populate some Model Attributes with the data of
        // related entities
        addBodyLinesPopulatingRelatedEntitiesData(bodyBuilder);

        bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                .concat(entityNamePlural.toLowerCase()).concat("Tab\", ")
                .concat(entityNamePlural.toLowerCase()).concat(");"));
        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat(
                "/\".concat(" + getPattern() + ");"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(getPatternParamRequestMapping(patternName));
        paramValues.add(getFormParamRequestMapping(false));
        paramValues
                .add(new StringAttributeValue(VALUE_ATTRIBUTE_NAME, "!find"));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        PARAMS_ATTRIBUTE_NAME, paramValues));
        requestMappingAttributes
                .add(getMethodRequestMapping(RequestMethod.GET));
        requestMappingAttributes.add(getProducesParamRequestMapping());
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    protected MethodMetadata getRegisterMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("register" + patternName);

        // Define method parameter types
        // @RequestParam(value = "gvnixpattern", required = true) String
        // pattern, HttpServletRequest req, Model uiModel)
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrIndex = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrIndex.add(new StringAttributeValue(VALUE_ATTRIBUTE_NAME,
                "index"));
        reqParamAttrIndex.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), true));
        List<AnnotationMetadata> methodAttrIndexAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrIndexAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrIndex);
        methodAttrIndexAnnotations.add(methodAttrIndexAnnotation.build());

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(Integer.class
                .getName()), methodAttrIndexAnnotations));
        methodParamTypes.add(getPatternRequestParam(true).getValue());
        Entry<JavaSymbolName, AnnotatedJavaType> httpServletRequest = getHttpServletRequest();
        methodParamTypes.add(httpServletRequest.getValue());
        Entry<JavaSymbolName, AnnotatedJavaType> modelRequestParam = getModelRequestParam();
        methodParamTypes.add(modelRequestParam.getValue());

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        // pattern, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("index"));
        Entry<JavaSymbolName, AnnotatedJavaType> patternRequestParam = getPatternRequestParam(false);
        methodParamNames.add(patternRequestParam.getKey());
        methodParamNames.add(httpServletRequest.getKey());
        methodParamNames.add(modelRequestParam.getKey());

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityTypeDetails.getPlural();
        String entityName = uncapitalize(entity.getSimpleTypeName());

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(entity);

        // Add date validation pattern to model if some date type field exists
        if (!entityDateTypes.isEmpty()) {
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
        }

        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        bodyBuilder
                .appendFormalLine(javaUtilList
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(" ")
                        .concat(entityNamePlural.toLowerCase())
                        .concat(" = ")
                        .concat(entity.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".")
                        .concat(entityTypeDetails
                                .getPersistenceDetails()
                                .getFindEntriesMethod()
                                .getMethodName()
                                .concat("(index == null ? 0 : (index.intValue() - 1), 1);")));

        bodyBuilder
                .appendFormalLine("if ("
                        .concat(entityNamePlural.toLowerCase())
                        .concat(".isEmpty() && httpServletRequest.getSession().getAttribute(\"dialogMessage\") == null) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                entityName.toLowerCase()).concat("\", null);"));

        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Info,
                "message_entitynotfound_problemdescription");

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat(
                "/\".concat(" + getPattern() + ");"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine(entity.getSimpleTypeName().concat(" ")
                .concat(entityName.toLowerCase()).concat(" = ")
                .concat(entityNamePlural.toLowerCase()).concat(".get(0);"));

        bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                .concat(entityName.toLowerCase()).concat("\", ")
                .concat(entityName.toLowerCase()).concat(");"));

        bodyBuilder.appendFormalLine(JavaType.LONG_PRIMITIVE
                .getNameIncludingTypeParameters()
                .concat(" count = ")
                .concat(entity.getSimpleTypeName())
                .concat(".")
                .concat(entityTypeDetails.getPersistenceDetails()
                        .getCountMethod().getMethodName()).concat("();"));
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"maxEntities"
                .concat("\", count == 0 ? 1 : count);"));

        // May we need to populate some Model Attributes with the data of
        // related entities
        addBodyLinesPopulatingRelatedEntitiesData(bodyBuilder);

        // Add date validation pattern to model if some date type field exists
        addBodyLinesRegisteringRelatedEntitiesDateTypesFormat(bodyBuilder);

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat(
                "/\".concat(" + getPattern() + ");"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(getPatternParamRequestMapping(patternName));
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixform"));
        paramValues
                .add(new StringAttributeValue(VALUE_ATTRIBUTE_NAME, "!find"));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        PARAMS_ATTRIBUTE_NAME, paramValues));
        requestMappingAttributes
                .add(getMethodRequestMapping(RequestMethod.GET));
        requestMappingAttributes.add(getProducesParamRequestMapping());
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Adds body lines which populates required data for the related entities.
     * <p>
     * This lines are like Roo's ModelAttribute methods but registering the
     * model attributes directly in the method. The model attributes are set
     * with data (usually Sets) needed by the entity related entities.
     * 
     * @param bodyBuilder
     */
    protected void addBodyLinesPopulatingRelatedEntitiesData(
            InvocableMemberBodyBuilder bodyBuilder) {
        for (JavaType type : relatedFields.keySet()) {
            JavaTypeMetadataDetails javaTypeMd = relatedFields.get(type);
            JavaTypePersistenceMetadataDetails javaTypePersistenceMd = javaTypeMd
                    .getPersistenceDetails();
            if (javaTypePersistenceMd != null
                    && javaTypePersistenceMd.getFindAllMethod() != null) {
                bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                        .concat(javaTypeMd.getPlural().toLowerCase())
                        .concat("\", ")
                        .concat(type.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".")
                        .concat(javaTypePersistenceMd.getFindAllMethod()
                                .getMethodName()).concat("());"));
            }
            else if (javaTypeMd.isEnumType()) {
                JavaType arrays = new JavaType("java.util.Arrays");
                bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                        .concat(javaTypeMd.getPlural().toLowerCase())
                        .concat("\", ")
                        .concat(arrays.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".asList(")
                        .concat(type.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".class.getEnumConstants()));"));
            }
        }
    }

    /**
     * Adds body lines registering DateTime formats of the related entities.
     * <p>
     * If related entity has DateTime fields, we need to register, as model
     * attribute, the DateTime format in order to render, validate and send
     * those fields in the right format. This is similar to the Roo's
     * DateTimeFormatPatterns helper method.
     * 
     * @param bodyBuilder
     */
    private void addBodyLinesRegisteringRelatedEntitiesDateTypesFormat(
            InvocableMemberBodyBuilder bodyBuilder) {
        for (Entry<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> javaTypeDateTimeFormatDetailsEntry : relatedDates
                .entrySet()) {

            String relatedEntityName = uncapitalize(javaTypeDateTimeFormatDetailsEntry
                    .getKey().getSimpleTypeName());
            for (Entry<JavaSymbolName, DateTimeFormatDetails> javaSymbolNameDateTimeFormatDetailsEntry : javaTypeDateTimeFormatDetailsEntry
                    .getValue().entrySet()) {

                String pattern;
                if (javaSymbolNameDateTimeFormatDetailsEntry.getValue().pattern != null) {
                    pattern = "\""
                            + javaSymbolNameDateTimeFormatDetailsEntry
                                    .getValue().pattern + "\"";
                }
                else {
                    JavaType dateTimeFormat = new JavaType(
                            "org.joda.time.format.DateTimeFormat");
                    String dateTimeFormatSimple = dateTimeFormat
                            .getNameIncludingTypeParameters(false,
                                    builder.getImportRegistrationResolver());
                    JavaType localeContextHolder = new JavaType(
                            "org.springframework.context.i18n.LocaleContextHolder");
                    String localeContextHolderSimple = localeContextHolder
                            .getNameIncludingTypeParameters(false,
                                    builder.getImportRegistrationResolver());
                    pattern = dateTimeFormatSimple
                            + ".patternForStyle(\""
                            + javaSymbolNameDateTimeFormatDetailsEntry
                                    .getValue().style + "\", "
                            + localeContextHolderSimple + ".getLocale())";
                }
                bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                        + relatedEntityName
                        + "_"
                        + javaSymbolNameDateTimeFormatDetailsEntry.getKey()
                                .getSymbolName().toLowerCase()
                        + "_date_format\", " + pattern + ");");
            }
        }
    }

    enum DialogType {
        Error, Info, Alert, Suggest;
    }

    /**
     * Using the given bodyBuilder adds code lines for set a Session Attribute
     * with an instance of Dialog bean
     * 
     * @param bodyBuilder
     * @param dialogType
     * @param messageDescriptionCode
     */
    protected void addBodyLinesForDialogMessage(
            InvocableMemberBodyBuilder bodyBuilder, DialogType dialogType,
            String messageDescriptionCode) {
    	
    	// TODO Código duplicado con método contiguo
    	
        JavaType httpSession = new JavaType("javax.servlet.http.HttpSession");
        bodyBuilder.appendFormalLine(httpSession
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(
                        " session = " + getHttpServletRequest().getKey()
                                + ".getSession();"));
        JavaType dialogJavaType = new JavaType(
                this.aspectPackage.concat(".dialog.Dialog"));
        JavaType dialogTypeJavaType = new JavaType(dialogJavaType
                .getFullyQualifiedTypeName().concat(".DialogType"));
        bodyBuilder.appendFormalLine(dialogJavaType
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" dialog = new Dialog(")
                .concat(dialogTypeJavaType
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(".").concat(dialogType.name())
                        .concat(", \"message_")
                        .concat(dialogType.name().toLowerCase())
                        .concat("_title\", \"").concat(messageDescriptionCode)
                        .concat("\");")));
        bodyBuilder
                .appendFormalLine("session.setAttribute(\"dialogMessage\", dialog);");
    }

    /**
     * Using the given bodyBuilder adds code lines for set a Session Attribute
     * with an instance of Dialog bean
     * 
     * @param bodyBuilder
     * @param dialogType
     * @param messageDescriptionCode
     */
    protected void addBodyLinesForDialogBinding(
            InvocableMemberBodyBuilder bodyBuilder, DialogType dialogType,
            String messageDescriptionCode) {

    	// TODO Código duplicado con método contiguo

        JavaType httpSession = new JavaType("javax.servlet.http.HttpSession");
        bodyBuilder.appendFormalLine(httpSession
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(
                        " session = " + getHttpServletRequest().getKey()
                                + ".getSession();"));
        JavaType dialogJavaType = new JavaType(
                this.aspectPackage.concat(".dialog.Dialog"));
        JavaType dialogTypeJavaType = new JavaType(dialogJavaType
                .getFullyQualifiedTypeName().concat(".DialogType"));
        bodyBuilder.appendFormalLine(dialogJavaType
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" dialog = new Dialog(")
                .concat(dialogTypeJavaType
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(".").concat(dialogType.name())
                        .concat(", \"message_")
                        .concat(dialogType.name().toLowerCase())
                        .concat("_title\", \"").concat(messageDescriptionCode)
                        .concat("\", bindingResult.getFieldErrors());")));
        bodyBuilder
                .appendFormalLine("session.setAttribute(\"dialogMessage\", dialog);");
    }

    /**
     * Generates the MethodMedata of createList() method for ITD
     * 
     * @return
     */
    protected MethodMetadata getCreateListMethod() {
        // Here we're sure that Entity.createList() method exists

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("createList");

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = getRequestMappingAttributes(RequestMethod.POST);
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypes();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        List<JavaSymbolName> methodParamNames = getMethodParameterNames();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = getMethodBodyBuilderBinding(PersistenceMethod.PERSIST);
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of updateList() method for ITD
     * 
     * @return
     */
    protected MethodMetadata getUpdateListMethod() {
        // Here we're sure that Entity.updateList() method exists

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("updateList");

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = getRequestMappingAttributes(RequestMethod.PUT);
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypes();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        List<JavaSymbolName> methodParamNames = getMethodParameterNames();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = getMethodBodyBuilderBinding(PersistenceMethod.MERGE);
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of deleteList() method for ITD
     * 
     * @return
     */
    protected MethodMetadata getDeleteListMethod() {
        // Here we're sure that Entity.deleteList() method exists

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("deleteList");

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = getRequestMappingAttributes(RequestMethod.DELETE);
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypes();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        List<JavaSymbolName> methodParamNames = getMethodParameterNames();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = getMethodBodyBuilder(PersistenceMethod.REMOVE);
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_ANNOTATION_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of filterList(EntityList) method for ITD
     * <p>
     * The generated method removes from the passed list the objects not
     * involved in the operation.
     * </p>
     * 
     * @return
     */
    protected MethodMetadata getFilterListMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("filterList");

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(entity
                .getFullyQualifiedTypeName().concat(".")
                .concat(entity.getSimpleTypeName()).concat("List")),
                annotations));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("entities"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(entity.getSimpleTypeName()
                .concat("List list = new ").concat(entity.getSimpleTypeName())
                .concat("List();"));
        bodyBuilder
                .appendFormalLine("for ( Integer select : entities.getSelected() ) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if ( select != null ) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("list.getList().add(entities.getList().get(select));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();

        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return list;");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, new JavaType(entity
                        .getFullyQualifiedTypeName().concat(".")
                        .concat(entity.getSimpleTypeName()).concat("List")),
                parameterTypes, parameterNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of
     * getRefererRedirectViewName(HttpServletRequest) method for ITD
     * <p>
     * The generated method redirects to the Referer URL
     * </p>
     * 
     * @return
     */
    protected MethodMetadata getRefererRedirectMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName(
                "getRefererRedirectViewName");

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        Entry<JavaSymbolName, AnnotatedJavaType> httpServletRequest = getHttpServletRequest();
        parameterTypes.add(httpServletRequest.getValue());

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(httpServletRequest.getKey());

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("String referer = "
                + httpServletRequest.getKey() + ".getHeader(\"Referer\");");

        JavaType stringUtils = new JavaType(
                "org.springframework.util.StringUtils");
        bodyBuilder.appendFormalLine("if (!".concat(
                stringUtils.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                ".hasText(referer)) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return null;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("return \"redirect:\".concat(referer);");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Returns the Attributes of the {@link RequestMapping} annotation for the
     * methods
     * <p>
     * Returns:<br>
     * <code>value="/list", method = RequestMethod.DELETE</code> as attributes
     * of RequestMapping annotation
     * </p>
     * 
     * @param requestMethod
     * @return
     */
    private List<AnnotationAttributeValue<?>> getRequestMappingAttributes(
            RequestMethod requestMethod) {

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                VALUE_ATTRIBUTE_NAME, "/list"));
        requestMappingAttributes.add(getMethodRequestMapping(requestMethod));
        requestMappingAttributes.add(getProducesParamRequestMapping());

        return requestMappingAttributes;
    }

    /**
     * Returns the list of Parameter Types for the methods
     * <p>
     * Returns a list containing:<br/>
     * <code>@Valid formBackingObjectList, BindingResult, HttpServletRequest</cod>
     * </p>
     * 
     * @return
     */
    private List<AnnotatedJavaType> getMethodParameterTypes() {
        /*
         * Define method parameter types. (@Valid formBackingObjectList,
         * BindingResult, HttpServletRequest)
         */
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(getEntity(
                new JavaType(entity.getFullyQualifiedTypeName().concat(".")
                        .concat(entity.getSimpleTypeName()).concat("List")))
                .getValue());
        parameterTypes.add(getBindingResult().getValue());
        parameterTypes.add(getHttpServletRequest().getValue());

        return parameterTypes;
    }

    /**
     * Returns the list of parameter names for the methods
     * <p>
     * Returns:<br/>
     * <code>entities, bindingResult, httpServletRequest</code>
     * </p>
     * 
     * @return
     */
    private List<JavaSymbolName> getMethodParameterNames() {

        // Define method parameter names (entities, bindingResult,
        // httpServletRequest)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("entities"));
        parameterNames.add(getBindingResult().getKey());
        parameterNames.add(getHttpServletRequest().getKey());

        return parameterNames;
    }

    /**
     * Get the RequestParam annotation for gvnixpattern.
     * <p>
     * Key has the param name and value has the annotation.
     * </p>
     * <code>@RequestParam(value = "gvnixpattern", required = true) String gvnixpattern</code>
     * 
     * @param required Is this request param required ?
     * @return Request param name and annotation
     */
    protected Entry<JavaSymbolName, AnnotatedJavaType> getPatternRequestParam(
            boolean required) {

        // TODO Required can be always same value ?

        AnnotationMetadataBuilder gvnixpatternParamBuilder = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"));
        gvnixpatternParamBuilder.addStringAttribute("value", getPattern());
        gvnixpatternParamBuilder.addBooleanAttribute("required", required);
        List<AnnotationMetadata> gvnixpatternParam = new ArrayList<AnnotationMetadata>();
        gvnixpatternParam.add(gvnixpatternParamBuilder.build());

        return new SimpleEntry<JavaSymbolName, AnnotatedJavaType>(
                new JavaSymbolName(getPattern()), new AnnotatedJavaType(
                        new JavaType("java.lang.String"), gvnixpatternParam));
    }

    /**
     * Get the RequestParam annotation for uiModel.
     * <p>
     * Key has the param name and value has the annotation.
     * </p>
     * <code>Model uiModel</code>
     * 
     * @return Request param name and annotation
     */
    protected Entry<JavaSymbolName, AnnotatedJavaType> getModelRequestParam() {

        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        return new SimpleEntry<JavaSymbolName, AnnotatedJavaType>(
                new JavaSymbolName("uiModel"), new AnnotatedJavaType(
                        new JavaType("org.springframework.ui.Model"),
                        annotations));
    }

    /**
     * Get request mapping annotation for a method.
     * <code>@RequestMapping(params = { "form", "gvnixpattern=AplicacionListados2", "gvnixreference" }, method = RequestMethod.GET)</code>
     * 
     * @param patternName Pattern name to match on request mapping with
     *            "gvnixpattern" attribute name.
     * @return
     */
    protected AnnotationMetadataBuilder getRequestMapping(String patternName,
            RequestMethod method) {

        List<AnnotationAttributeValue<?>> requestMappingAnnotation = getParamsRequestMapping(patternName);
        requestMappingAnnotation.add(getMethodRequestMapping(method));
        requestMappingAnnotation.add(getProducesParamRequestMapping());
        return new AnnotationMetadataBuilder(REQUEST_MAPPING_ANNOTATION_TYPE,
                requestMappingAnnotation);
    }

    protected List<AnnotationAttributeValue<?>> getParamsRequestMapping(
            String patternName) {

        List<StringAttributeValue> requestMapingAnnotationParams = new ArrayList<StringAttributeValue>();
        requestMapingAnnotationParams.add(getFormParamRequestMapping(true));
        requestMapingAnnotationParams
                .add(getPatternParamRequestMapping(patternName));
        requestMapingAnnotationParams.add(getReferenceParamRequestMapping());
        List<AnnotationAttributeValue<?>> requestMappingAnnotation = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAnnotation
                .add(new ArrayAttributeValue<StringAttributeValue>(
                        PARAMS_ATTRIBUTE_NAME, requestMapingAnnotationParams));

        return requestMappingAnnotation;
    }

    protected EnumAttributeValue getMethodRequestMapping(RequestMethod method) {

        return new EnumAttributeValue(
                new JavaSymbolName("method"),
                new EnumDetails(
                        new JavaType(
                                "org.springframework.web.bind.annotation.RequestMethod"),
                        new JavaSymbolName(method.name())));
    }

    protected StringAttributeValue getReferenceParamRequestMapping() {

        // TODO Refactor
        return new StringAttributeValue(VALUE_ATTRIBUTE_NAME, "gvnixreference");
    }

    protected StringAttributeValue getPatternParamRequestMapping(
            String patternName) {

        // TODO Refactor
        return new StringAttributeValue(VALUE_ATTRIBUTE_NAME, getPattern()
                + "=" + patternName);
    }

    protected StringAttributeValue getProducesParamRequestMapping() {

        // TODO Refactor
        return new StringAttributeValue(new JavaSymbolName("produces"),
                "text/html");
    }

    protected String getPattern() {

        // TODO Refactor
        return "gvnixpattern";
    }

    protected StringAttributeValue getFormParamRequestMapping(boolean exists) {

        // TODO Refactor
        if (exists) {
            return new StringAttributeValue(VALUE_ATTRIBUTE_NAME, "form");
        }
        else {
            return new StringAttributeValue(VALUE_ATTRIBUTE_NAME, "!form");
        }
    }

    // TODO Refactor method name
    protected AnnotationMetadata getValid() {

        return new AnnotationMetadataBuilder(new JavaType(
                "javax.validation.Valid")).build();
    }

    protected Entry<JavaSymbolName, AnnotatedJavaType> getHttpServletRequest() {

        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        return new SimpleEntry<JavaSymbolName, AnnotatedJavaType>(
                new JavaSymbolName("httpServletRequest"),
                new AnnotatedJavaType(new JavaType(
                        "javax.servlet.http.HttpServletRequest"), annotations));
    }

    protected Entry<JavaSymbolName, AnnotatedJavaType> getBindingResult() {

        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        return new SimpleEntry<JavaSymbolName, AnnotatedJavaType>(
                new JavaSymbolName("bindingResult"),
                new AnnotatedJavaType(new JavaType(
                        "org.springframework.validation.BindingResult"),
                        annotations));
    }

    // TODO Refactor method name
    protected Entry<JavaSymbolName, AnnotatedJavaType> getEntity(JavaType name) {

        List<AnnotationMetadata> methodAttrValidAnnotations = new ArrayList<AnnotationMetadata>();
        methodAttrValidAnnotations.add(getValid());

        return new SimpleEntry<JavaSymbolName, AnnotatedJavaType>(
                new JavaSymbolName(name.getSimpleTypeName().toLowerCase()),
                new AnnotatedJavaType(new JavaType(
                        name.getFullyQualifiedTypeName()),
                        methodAttrValidAnnotations));
    }

    protected void getRequestParam(List<JavaSymbolName> methodParamNames,
            List<AnnotatedJavaType> methodParamTypes) {

        Entry<JavaSymbolName, AnnotatedJavaType> patternRequestParam = getPatternRequestParam(false);
        methodParamTypes.add(patternRequestParam.getValue());
        methodParamNames.add(patternRequestParam.getKey());

        Entry<JavaSymbolName, AnnotatedJavaType> validEntity = getEntity(entity);
        methodParamTypes.add(validEntity.getValue());
        methodParamNames.add(validEntity.getKey());

        Entry<JavaSymbolName, AnnotatedJavaType> bindingResult = getBindingResult();
        methodParamTypes.add(bindingResult.getValue());
        methodParamNames.add(bindingResult.getKey());

        Entry<JavaSymbolName, AnnotatedJavaType> modelRequestParam = getModelRequestParam();
        methodParamTypes.add(modelRequestParam.getValue());
        methodParamNames.add(modelRequestParam.getKey());

        Entry<JavaSymbolName, AnnotatedJavaType> httpServletRequest = getHttpServletRequest();
        methodParamTypes.add(httpServletRequest.getValue());
        methodParamNames.add(httpServletRequest.getKey());
    }

    /**
     * Returns the method body of the methods given a {@link PersistenceMethod}
     * <p>
     * Example:<br/>
     * <code>
     * if ( !bindingResult.hasErrors() ) {<br/>
     * &nbsp;&nbsp;Car.persist(filterList(entities));<br/>
     * }<br/>
     * return getRefererRedirectViewName(httpServletRequest);
     * </code>
     * 
     * @param persistenceMethod
     * @return
     */
    private InvocableMemberBodyBuilder getMethodBodyBuilder(
            PersistenceMethod persistenceMethod) {

    	// TODO Código duplicado con método contiguo

    	// Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        // test if form has errors
        bodyBuilder.appendFormalLine("if ( !" + getBindingResult().getKey()
                + ".hasErrors() ) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(entity
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(".")
                .concat(persistenceMethod.getName())
                .concat("(filterList(entities));"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return getRefererRedirectViewName("
                + getHttpServletRequest().getKey() + ");");

        return bodyBuilder;
    }

    /**
     * Returns the method body of the methods given a {@link PersistenceMethod}
     * <p>
     * Example:<br/>
     * <code>
     * if ( !bindingResult.hasErrors() ) {<br/>
     * &nbsp;&nbsp;Car.persist(filterList(entities));<br/>
     * }<br/>
     * return getRefererRedirectViewName(httpServletRequest);
     * </code>
     * 
     * @param persistenceMethod
     * @return
     */
    private InvocableMemberBodyBuilder getMethodBodyBuilderBinding(
            PersistenceMethod persistenceMethod) {
    	
    	// TODO Código duplicado con método contiguo
    	
        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        // test if form has errors
        bodyBuilder.appendFormalLine("if ( !" + getBindingResult().getKey()
                + ".hasErrors() ) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(entity
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(".")
                .concat(persistenceMethod.getName())
                .concat("(filterList(entities));"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        addBodyLinesForDialogBinding(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return getRefererRedirectViewName("
                + getHttpServletRequest().getKey() + ");");

        return bodyBuilder;
    }

    /**
     * Remove from <code>relatedFields</code> those types that are being
     * returned in any other populate method (these methods have ModelAttribute
     * method annotation)
     * 
     * @param relatedFields
     */
    private void filterAlreadyPopulatedTypes(
            SortedMap<JavaType, JavaTypeMetadataDetails> typesForPopulate) {

        Set<JavaType> keyTypesForPopulate = typesForPopulate.keySet();
        if (keyTypesForPopulate.isEmpty()) {
            return;
        }

        for (MethodMetadata method : controllerMethods) {
            JavaType returnType = method.getReturnType();
            if (returnType.isCommonCollectionType()) {
                for (JavaType genericType : returnType.getParameters()) {
                    if (typesForPopulate.keySet().contains(genericType)) {
                        typesForPopulate.remove(genericType);
                    }
                }
            }
            else if (typesForPopulate.keySet().contains(returnType)) {
                typesForPopulate.remove(returnType);
            }
        }
    }

    /**
     * Returns the method if exists or null otherwise. With this we assure that
     * a method is defined once in the Class
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    protected MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        List<JavaType> nonAnnotatedJavaTypes = AnnotatedJavaType
                .convertFromAnnotatedJavaTypes(paramTypes);
        for (MethodMetadata methodMetadata : controllerMethods) {
            if (methodMetadata.getMethodName().equals(methodName)
                    && nonAnnotatedJavaTypes.equals(AnnotatedJavaType
                            .convertFromAnnotatedJavaTypes(methodMetadata
                                    .getParameterTypes()))) {
                return methodMetadata;
            }
        }
        return null;
    }

    protected String uncapitalize(String term) {
        // [ROO-1790] this is needed to adhere to the JavaBean naming
        // conventions (see JavaBean spec section 8.8)
        return Introspector.decapitalize(StringUtils.capitalize(term));
    }

    public WebScaffoldMetadata getWebScaffoldMetadata() {
        return this.webScaffoldMetadata;
    }

    public SortedMap<JavaType, JavaTypeMetadataDetails> getRelatedApplicationTypeMetadata() {
        return this.relatedEntities;
    }

    public List<String> getDefinedPatterns() {
        return patterns;
    }

    // Typically, no changes are required beyond this point

    @Override
    public String toString() {
        ToStringBuilder tsc = new ToStringBuilder(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("aspectName", aspectName);
        tsc.append("destinationType", destination);
        tsc.append("governor", governorPhysicalTypeMetadata.getId());
        tsc.append("itdTypeDetails", itdTypeDetails);
        return tsc.toString();
    }
}
