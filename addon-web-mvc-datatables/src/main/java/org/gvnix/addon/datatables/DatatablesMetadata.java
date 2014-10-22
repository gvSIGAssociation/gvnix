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
package org.gvnix.addon.datatables;

import static org.gvnix.addon.datatables.DatatablesConstants.ARRAYLIST_MAP_STRING_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.ARRAYLIST_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.ARRAY_UTILS;
import static org.gvnix.addon.datatables.DatatablesConstants.AUTOWIRED;
import static org.gvnix.addon.datatables.DatatablesConstants.BEAN_PROPERTY_BINDING_RESULT;
import static org.gvnix.addon.datatables.DatatablesConstants.BEAN_WRAPPER;
import static org.gvnix.addon.datatables.DatatablesConstants.BEAN_WRAPPER_IMP;
import static org.gvnix.addon.datatables.DatatablesConstants.BINDING_RESULT;
import static org.gvnix.addon.datatables.DatatablesConstants.CHECK_FILTERS_RETURN;
import static org.gvnix.addon.datatables.DatatablesConstants.CHECK_FILTER_EXPRESSIONS;
import static org.gvnix.addon.datatables.DatatablesConstants.COLLECTIONS;
import static org.gvnix.addon.datatables.DatatablesConstants.COLLECTION_UTILS;
import static org.gvnix.addon.datatables.DatatablesConstants.CONVERSION_SERVICE;
import static org.gvnix.addon.datatables.DatatablesConstants.CREATE_JSON_FORM_METHOD;
import static org.gvnix.addon.datatables.DatatablesConstants.CRITERIA_PARAM_NAME;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_CRITERIA_TYPE;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_CSV_EXPORT;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT_CONF;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT_EXCEPTION;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT_NAME;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT_TYPE;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT_TYPE_NAME;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_EXPORT_UTILS;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_HTML_TABLE;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_PARAMS;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_PDF_EXPORT;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_RESPONSE;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_UTILS;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_XLSX_EXPORT;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_XLS_EXPORT;
import static org.gvnix.addon.datatables.DatatablesConstants.DATATABLES_XML_EXPORT;
import static org.gvnix.addon.datatables.DatatablesConstants.DATA_SET_MAP_STRING_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.ENUMERATION_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.EXTENDED_MODEL_MAP;
import static org.gvnix.addon.datatables.DatatablesConstants.FIND_ALL_RETURN;
import static org.gvnix.addon.datatables.DatatablesConstants.GET_COLUMN_TYPE;
import static org.gvnix.addon.datatables.DatatablesConstants.GET_I18N_TEXT;
import static org.gvnix.addon.datatables.DatatablesConstants.GET_PROPERTY_MAP;
import static org.gvnix.addon.datatables.DatatablesConstants.HASHMAP_STRING_OBJECT;
import static org.gvnix.addon.datatables.DatatablesConstants.HASHMAP_STRING_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.HTTP_SERVLET_REQUEST;
import static org.gvnix.addon.datatables.DatatablesConstants.HTTP_SERVLET_RESPONSE;
import static org.gvnix.addon.datatables.DatatablesConstants.HTTP_SERVLET_RESPONSE_WRAPPER;
import static org.gvnix.addon.datatables.DatatablesConstants.IDS_PARAM_NAME;
import static org.gvnix.addon.datatables.DatatablesConstants.IO_EXCEPTION;
import static org.gvnix.addon.datatables.DatatablesConstants.LIST_DATATABLES;
import static org.gvnix.addon.datatables.DatatablesConstants.LIST_MAP_STRING_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.LIST_ROO;
import static org.gvnix.addon.datatables.DatatablesConstants.LIST_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.MAP_STRING_OBJECT;
import static org.gvnix.addon.datatables.DatatablesConstants.MAP_STRING_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.MESSAGE_SOURCE;
import static org.gvnix.addon.datatables.DatatablesConstants.POPULATE_DATATABLES_CONFIG;
import static org.gvnix.addon.datatables.DatatablesConstants.POPULATE_ITEM_FOR_RENDER;
import static org.gvnix.addon.datatables.DatatablesConstants.POPULATE_PARAMETERS_MAP;
import static org.gvnix.addon.datatables.DatatablesConstants.PRINT_WRITER;
import static org.gvnix.addon.datatables.DatatablesConstants.QDSL_BOOLEAN_BUILDER;
import static org.gvnix.addon.datatables.DatatablesConstants.QDSL_JPA_QUERY;
import static org.gvnix.addon.datatables.DatatablesConstants.QDSL_PATH_BUILDER;
import static org.gvnix.addon.datatables.DatatablesConstants.QUERYDSL_UTILS;
import static org.gvnix.addon.datatables.DatatablesConstants.REDIRECT_ATTRIBUTES;
import static org.gvnix.addon.datatables.DatatablesConstants.REDIRECT_MODEL;
import static org.gvnix.addon.datatables.DatatablesConstants.RENDER_UPDATE_FORMS_METHOD;
import static org.gvnix.addon.datatables.DatatablesConstants.REQUEST_DISPATCHER;
import static org.gvnix.addon.datatables.DatatablesConstants.REQUEST_METHOD;
import static org.gvnix.addon.datatables.DatatablesConstants.REQUEST_PARAM_NAME;
import static org.gvnix.addon.datatables.DatatablesConstants.RESPONSE_PARAM_NAME;
import static org.gvnix.addon.datatables.DatatablesConstants.SEARCH_RESULTS;
import static org.gvnix.addon.datatables.DatatablesConstants.SERVLET_EXCEPTION;
import static org.gvnix.addon.datatables.DatatablesConstants.SET_STRING;
import static org.gvnix.addon.datatables.DatatablesConstants.STRING_UTILS;
import static org.gvnix.addon.datatables.DatatablesConstants.STRING_WRITER;
import static org.gvnix.addon.datatables.DatatablesConstants.UI_MODEL;
import static org.gvnix.addon.datatables.DatatablesConstants.UPDATE_JSON_FORMS_METHOD;
import static org.gvnix.addon.datatables.DatatablesConstants.VALID;
import static org.gvnix.addon.datatables.DatatablesConstants.WEB_REQUEST;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.SpringJavaType.MODEL;
import static org.springframework.roo.model.SpringJavaType.MODEL_ATTRIBUTE;
import static org.springframework.roo.model.SpringJavaType.REQUEST_MAPPING;
import static org.springframework.roo.model.SpringJavaType.RESPONSE_BODY;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.query.JpaQueryMetadata;
import org.gvnix.addon.web.mvc.batch.WebJpaBatchMetadata;
import org.gvnix.support.WebItdBuilderHelper;
import org.springframework.roo.addon.finder.FieldToken;
import org.springframework.roo.addon.finder.ReservedToken;
import org.springframework.roo.addon.finder.Token;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
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
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * {@link GvNIXDatatables} metadata
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class DatatablesMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {
    private static final String ENDING = "\");";
    private static final String ELSE = "} else {";
    private static final String JSON_APPEND = "json.append(\",\");";
    private static final String COLTYPE_NUM = "return \"{\\\"columnType\\\": \\\"number\\\"}\";";

    private static final String DATATABLES_REDIRECT = "datatablesRedirect";

    private static final String PARAMETERS_AS_BASE_SEARCH = "// URL parameters are used as base search filters";

    private static final String APPLICATION_JSON = "application/json";

    private static final String HEADERS = "headers";

    private static final String SIZE_VAR = "size";

    private static final String SORT_FIELD_NAME = "sortFieldName";

    private static final String SORT_ORDER = "sortOrder";

    private static final String PAGE_VAR = "page";

    private static final String PARAMS_VAR = "params";

    private static final String METHOD_VAR = "method";

    private static final String ACCEPT_APPLICATION_JSON = "Accept=application/json";

    private static final JavaSymbolName SET_BASE_FILTER_METHOD = new JavaSymbolName(
            "setDatatablesBaseFilter");

    private static final JavaSymbolName EXPORT_METHOD_NAME = new JavaSymbolName(
            "export");

    private static final JavaSymbolName RETRIEVE_DATA_METHOD_NAME = new JavaSymbolName(
            "retrieveData");

    private static final Logger LOGGER = HandlerUtils
            .getLogger(DatatablesMetadata.class);

    // Constants
    private static final String PROVIDES_TYPE_STRING = DatatablesMetadata.class
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
     * Annotation values
     */
    private final DatatablesAnnotationValues annotationValues;

    /**
     * Entity property which is its identifier (as roo only use one property,
     * this includes the embedded pks)
     */
    private final FieldMetadata entityIdentifier;

    /**
     * Related entity
     */
    private final JavaType entity;

    /**
     * Related entity member details
     */
    private final MemberDetails entityMemberDetails;

    /**
     * Entity name to use in var names
     */
    private final String entityName;

    /**
     * Related entity plural
     */
    private final String entityPlural;

    /**
     * If related entity has date properties
     */
    private final Map<JavaSymbolName, DateTimeFormatDetails> entityDatePatterns;

    /**
     * Method name to get entity manager from entity class
     */
    private final JavaSymbolName entityEntityManagerMethod;

    /**
     * Batch services metadata
     */
    private final WebJpaBatchMetadata webJpaBatchMetadata;

    /**
     * Jpa Query metadata
     */
    private final JpaQueryMetadata jpaQueryMetadata;

    /**
     * Field which holds conversionService
     */
    private FieldMetadata conversionService;

    /**
     * Field which holds messageSource
     */
    private FieldMetadata messageSource;

    /**
     * Field which holds beanWrapper
     */
    private FieldMetadata beanWrapper;

    /**
     * Itd builder herlper
     */
    private final WebItdBuilderHelper helper;

    private final WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    /**
     * Information about finders
     */
    private final Map<FinderMetadataDetails, QueryHolderTokens> findersRegistered;

    /**
     * Name to use for findAll method (name includes entity plurar)
     */
    private final JavaSymbolName findAllMethodName;

    /**
     * Name to use for renderItem method (name includes entity plural)
     */
    private final JavaSymbolName renderItemsMethodName;

    /**
     * Name to use for findByParamters method (name includes entity plural)
     */
    private final JavaSymbolName findByParametersMethodName;

    /**
     * JavaType for List entity elements
     */
    private final JavaType entityListType;

    /**
     * Web metadata service to get controller information
     */
    private final WebMetadataService webMetadataService;

    private final ProjectOperations projectOperations;

    private final JavaType entityIdentifierType;

    private final JavaType entityIdArrayType;

    private final String entityList;

    public DatatablesMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            DatatablesAnnotationValues annotationValues, JavaType entity,
            MemberDetails entityMemberDetails,
            List<FieldMetadata> identifierProperties, String entityPlural,
            JavaSymbolName entityManagerMethodName,
            Map<JavaSymbolName, DateTimeFormatDetails> datePatterns,
            JavaType webScaffoldAspectName,
            WebJpaBatchMetadata webJpaBatchMetadata,
            JpaQueryMetadata jpaQueryMetadata,
            WebScaffoldAnnotationValues webScaffoldAnnotationValues,
            Map<FinderMetadataDetails, QueryHolderTokens> findersRegistered,
            WebMetadataService webMetadataService,
            ProjectOperations projectOperations) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.annotationValues = annotationValues;

        this.helper = new WebItdBuilderHelper(this,
                governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Roo only uses one property
        this.entityIdentifier = identifierProperties.get(0);
        this.entityIdentifierType = entityIdentifier.getFieldType();
        this.entity = entity;
        this.entityMemberDetails = entityMemberDetails;
        this.entityName = JavaSymbolName.getReservedWordSafeName(entity)
                .getSymbolName();
        this.entityPlural = entityPlural;
        this.entityEntityManagerMethod = entityManagerMethodName;
        this.entityDatePatterns = datePatterns;
        this.webJpaBatchMetadata = webJpaBatchMetadata;
        this.jpaQueryMetadata = jpaQueryMetadata;
        this.webScaffoldAnnotationValues = webScaffoldAnnotationValues;

        // Prepare method names which includes entity plural
        this.findAllMethodName = new JavaSymbolName(
                "findAll".concat(StringUtils.capitalize(entityPlural)));

        this.renderItemsMethodName = new JavaSymbolName(
                "render".concat(StringUtils.capitalize(entityPlural)));

        this.findByParametersMethodName = new JavaSymbolName("find".concat(
                StringUtils.capitalize(entityPlural)).concat("ByParameters"));

        this.entityListType = new JavaType(LIST.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(entity));

        this.entityList = StringUtils.uncapitalize(entityPlural);

        // this.entityIdListType = new
        // JavaType(LIST.getFullyQualifiedTypeName(),
        // 0, DataType.TYPE, null, Arrays.asList(entityIdentifierType));

        this.entityIdArrayType = new JavaType(
                entityIdentifierType.getFullyQualifiedTypeName(), 1,
                DataType.TYPE, null, null);

        // store finders information
        if (findersRegistered != null) {
            this.findersRegistered = Collections
                    .unmodifiableMap(findersRegistered);
        }
        else {
            this.findersRegistered = null;
        }

        this.webMetadataService = webMetadataService;

        this.projectOperations = projectOperations;

        // Adding precedence declaration
        // This aspect before webScaffold
        builder.setDeclarePrecedence(aspectName, webScaffoldAspectName);

        // Adding field definition
        builder.addField(getConversionServiceField());
        builder.addField(getMessageSourceField());
        builder.addField(getBeanWrapperField());

        // Adding methods
        builder.addMethod(getListDatatablesRequestMethod());
        builder.addMethod(getPopulateDatatablesConfig());
        builder.addMethod(getListRooRequestMethod());
        builder.addMethod(getPopulateParameterMapMethod());
        builder.addMethod(getGetPropertyMapMethod());
        builder.addMethod(getGetPropertyMapUrlMethod());
        builder.addMethod(getSetDatatablesBaseFilterMethod(annotationValues));
        builder.addMethod(getColumnTypeRequestMethod());
        builder.addMethod(geti18nTextRequestMethod());

        // Detail methods
        builder.addMethod(getListDatatablesDetailMethod());
        builder.addMethod(getCreateDatatablesDetailMethod());
        builder.addMethod(getUpdateDatatablesDetailMethod());
        builder.addMethod(getDeleteDatatablesDetailMethod());

        // Add AJAX mode required methods
        if (isAjax()) {
            if (isStantardMode()) {
                builder.addMethod(getFindAllMethod());
                // Adding filters methods
                builder.addMethod(getCheckFilterExpressionsMethod());
            }
            else {
                if (isInlineEditing()) {
                    // InlineEditing not supported on non-standard mode
                    throw new IllegalArgumentException(aspectName
                            .getFullyQualifiedTypeName()
                            .concat(".@GvNIXDatatables: 'mode = ")
                            .concat(annotationValues.getMode())
                            .concat("' can't use inlineEditing"));
                }
                // Render mode requires this methods
                builder.addMethod(getPopulateItemForRenderMethod());
                builder.addMethod(getRenderItemsMethod());
                builder.addMethod(getFindAllMethodRenderMode());

            }
            // add ajax request for finders
            if (this.findersRegistered != null) {
                for (Entry<FinderMetadataDetails, QueryHolderTokens> finder : this.findersRegistered
                        .entrySet()) {
                    if (finder.getValue() != null) {
                        builder.addMethod(getAjaxFinderMethod(finder.getKey(),
                                finder.getValue()));
                    }
                }
            }

            // Export via AJAX methods
            addAjaxExportMethods();
        }
        else if (!isStantardMode()) {
            // Non-Standard view mode requires AJAX data mode
            throw new IllegalArgumentException(aspectName
                    .getFullyQualifiedTypeName()
                    .concat(".@GvNIXDatatables: 'mode = ")
                    .concat(annotationValues.getMode())
                    .concat("' requires 'ajax = true'"));
        }
        else {
            // DOM standard mode requires finder by parameters
            builder.addMethod(getFindByParametersMethod());
        }
        if (isInlineEditing()) {
            if (!hasJpaBatchSupport()) {
                // InlineEditing requires batch support
                throw new IllegalArgumentException(
                        aspectName
                                .getFullyQualifiedTypeName()
                                .concat(". Inline editing requires batch support. Run 'jpa batch add' command or 'web mvc batch add' command"));
            }
            builder.addMethod(getJsonFormsMethod(true));
            builder.addMethod(getJsonFormsMethod(false));
            builder.addMethod(getRenderUpdateFormsMethod());
            builder.addMethod(getPopulateItemForRenderMethod());
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Return method to obtain JSon forms to create or update, depending if the
     * parameter {@code isCreateJsonForm} is true or false.
     * 
     * @param isCreateJsonForm true to return the method to obtain the create
     *        JSon form, false to obtain the update one.
     * @return
     */
    private MethodMetadata getJsonFormsMethod(boolean isCreateJsonForm) {

        // method name
        JavaSymbolName methodName = UPDATE_JSON_FORMS_METHOD;
        if (isCreateJsonForm) {
            methodName = CREATE_JSON_FORM_METHOD;
        }

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        if (!isCreateJsonForm) {
            parameterTypes.add(helper.createRequestParam(entityIdArrayType,
                    "id", null, null));
        }
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_REQUEST));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_RESPONSE));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(MODEL));

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName, parameterTypes);

        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        String requestMappingValue = "/datatables/updateforms";
        if (isCreateJsonForm) {
            requestMappingValue = "/datatables/createform";
        }
        AnnotationMetadataBuilder requestMappingAnnotation = helper
                .getRequestMappingAnnotation(requestMappingValue, null, null,
                        APPLICATION_JSON, null, ACCEPT_APPLICATION_JSON);
        annotations.add(requestMappingAnnotation);
        // @ResponseBody
        AnnotationMetadataBuilder responseBodyAnnotation = new AnnotationMetadataBuilder();
        responseBodyAnnotation.setAnnotationType(SpringJavaType.RESPONSE_BODY);
        annotations.add(responseBodyAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        if (!isCreateJsonForm) {
            parameterNames.add(IDS_PARAM_NAME);
        }
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);
        parameterNames.add(UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        if (isCreateJsonForm) {
            buildCreateJsonFormMethodBody(bodyBuilder);
        }
        else {
            buildUpdateJsonFormsMethodBody(bodyBuilder);
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, LIST_MAP_STRING_STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build code for the method to obtain the update JSon forms.
     * 
     * @param bodyBuilder the InvocableMemberBodyBuilder
     */
    private void buildUpdateJsonFormsMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // if (ArrayUtils.isEmpty(ids)) {
        bodyBuilder.appendFormalLine(String.format("if (%s.isEmpty(ids)) {",
                helper.getFinalTypeName(ARRAY_UTILS),
                IDS_PARAM_NAME.getSymbolName()));
        bodyBuilder.indent();

        // return new ArrayList<Map<String, String>>();
        bodyBuilder.appendFormalLine(String.format("return new %s();",
                helper.getFinalTypeName(ARRAYLIST_MAP_STRING_STRING)));

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("");

        // // Using PathBuilder, a cascading builder for Predicate expressions
        bodyBuilder
                .appendFormalLine("// Using PathBuilder, a cascading builder for Predicate expressions");

        // PathBuilder<Vet> entity = new PathBuilder<Vet>(Vet.class, "entity");
        String pathBuilder = helper.getFinalTypeName(new JavaType(
                QDSL_PATH_BUILDER.getFullyQualifiedTypeName(), entity));
        bodyBuilder.appendFormalLine(String.format(
                "%s entity = new %s(%s.class, \"entity\");", pathBuilder,
                pathBuilder, helper.getFinalTypeName(entity)));

        // // URL parameters are used as base search filters
        bodyBuilder.appendFormalLine(PARAMETERS_AS_BASE_SEARCH);

        // Set<Long> set = new HashSet<Long>();
        bodyBuilder.appendFormalLine(String.format("%s set = new %s();", helper
                .getFinalTypeName(new JavaType(JdkJavaType.SET
                        .getFullyQualifiedTypeName(), entityIdentifierType)),
                helper.getFinalTypeName(new JavaType(JdkJavaType.HASH_SET
                        .getFullyQualifiedTypeName(), entityIdentifierType))));

        // set.addAll(Arrays.asList(ids));
        bodyBuilder.appendFormalLine(String.format(
                "set.addAll(%s.asList(%s));",
                helper.getFinalTypeName(JdkJavaType.ARRAYS),
                IDS_PARAM_NAME.getSymbolName()));
        // BooleanBuilder filterBy = QuerydslUtils.createPredicateByIn(entity,
        // "id", set);
        bodyBuilder.appendFormalLine(String.format(
                "%s filterBy = %s.createPredicateByIn(entity, \"%s\", set);",
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER), helper
                        .getFinalTypeName(QUERYDSL_UTILS), entityIdentifier
                        .getFieldName().getSymbolName()));

        // // Create a query with filter
        bodyBuilder.appendFormalLine("// Create a query with filter");

        // JPAQuery query = new JPAQuery(Vet.entityManager());
        bodyBuilder.appendFormalLine(String.format(
                "%s query = new %s(%s.entityManager());",
                helper.getFinalTypeName(QDSL_JPA_QUERY),
                helper.getFinalTypeName(QDSL_JPA_QUERY),
                helper.getFinalTypeName(entity)));

        // query = query.from(entity);
        bodyBuilder.appendFormalLine("query = query.from(entity);");

        // // execute query
        bodyBuilder.appendFormalLine("// execute query");

        // List<Vet> vets = query.where(filterBy).list(entity);
        bodyBuilder.appendFormalLine(String.format(
                "%s %s = query.where(filterBy).list(entity);",
                helper.getFinalTypeName(entityListType), entityList));
        // List<Map<String, String>> udpateForms = renderUpdateForm(vets,
        // request, response);
        bodyBuilder.appendFormalLine(String.format(
                "%s udpateForms = %s(%s, request, response);",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                RENDER_UPDATE_FORMS_METHOD.getSymbolName(), entityList,
                REQUEST_PARAM_NAME.getSymbolName(),
                RESPONSE_PARAM_NAME.getSymbolName()));
        // return udpateForms;
        bodyBuilder.appendFormalLine("return udpateForms;");
    }

    /**
     * Build code for the method to obtain the create JSon form.
     * 
     * @param bodyBuilder the InvocableMemberBodyBuilder
     */
    private void buildCreateJsonFormMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // // Prepare result
        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Prepare result");

        // List<Map<String, String>> result = new ArrayList<Map<String,
        // String>>();
        bodyBuilder.appendFormalLine(String.format("%s result = new %s();",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                helper.getFinalTypeName(ARRAYLIST_MAP_STRING_STRING)));

        // String controllerPath = "vets";
        bodyBuilder.appendFormalLine(String.format(
                "String controllerPath = \"%s\";",
                webScaffoldAnnotationValues.getPath()));

        // String pageToUse = "update";
        bodyBuilder.appendFormalLine("String pageToUse = \"create\";");

        // String renderUrl = String.format("/WEB-INF/views/%s/%s.jspx",
        // controllerPath, pageToUse);
        bodyBuilder
                .appendFormalLine("String renderUrl = String.format(\"/WEB-INF/views/%s/%s.jspx\", controllerPath, pageToUse);");
        bodyBuilder.appendFormalLine("");

        // Map<String, String> item = new HashMap<String, String>();
        bodyBuilder.appendFormalLine(String.format("%s item = new %s();",
                helper.getFinalTypeName(MAP_STRING_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_STRING)));

        // final StringWriter buffer = new StringWriter();
        bodyBuilder.appendFormalLine(String.format(
                "final %s buffer = new %s();",
                helper.getFinalTypeName(STRING_WRITER),
                helper.getFinalTypeName(STRING_WRITER)));
        bodyBuilder.appendFormalLine("");

        // // Call JSP to render update form
        bodyBuilder.appendFormalLine("// Call JSP to render update form");

        // RequestDispatcher dispatcher =
        // request.getRequestDispatcher(renderUrl);
        bodyBuilder.appendFormalLine(String.format(
                "%s dispatcher = %s.getRequestDispatcher(renderUrl);",
                helper.getFinalTypeName(REQUEST_DISPATCHER),
                REQUEST_PARAM_NAME.getSymbolName()));
        bodyBuilder.appendFormalLine("");

        // // spring from:input tag uses BindingResult to locate property
        // editors
        // // for each bean property. So, we add a request attribute (required
        // key
        // // id BindingResult.MODEL_KEY_PREFIX + object name) with a correctly
        // // initialized bindingResult.
        bodyBuilder
                .appendFormalLine("// spring from:input tag uses BindingResult to locate property editors");
        bodyBuilder
                .appendFormalLine("// for each bean property. So, we add a request attribute (required key");
        bodyBuilder
                .appendFormalLine("// id BindingResult.MODEL_KEY_PREFIX + object name) with a correctly");
        bodyBuilder.appendFormalLine("// initialized bindingResult.");

        // Vet vet = new Vet();
        bodyBuilder.appendFormalLine(String.format("%s %s = new %s();",
                helper.getFinalTypeName(entity), entityName,
                helper.getFinalTypeName(entity)));

        // BeanPropertyBindingResult bindingResult = new
        // BeanPropertyBindingResult(pet, "pet");
        bodyBuilder.appendFormalLine(String.format(
                "%s bindingResult = new %s(%s, \"%s\");",
                helper.getFinalTypeName(BEAN_PROPERTY_BINDING_RESULT),
                helper.getFinalTypeName(BEAN_PROPERTY_BINDING_RESULT),
                entityName, entityName));

        // bindingResult.initConversion(conversionService_dtt);
        bodyBuilder.appendFormalLine(String.format(
                "bindingResult.initConversion(%s);",
                getConversionServiceField().getFieldName().getSymbolName()));

        // request.setAttribute(BindingResult.MODEL_KEY_PREFIX + "pet",
        // bindingResult);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s.setAttribute(%s.MODEL_KEY_PREFIX + \"%s\", bindingResult);",
                                REQUEST_PARAM_NAME.getSymbolName(),
                                helper.getFinalTypeName(BINDING_RESULT),
                                entityName));
        bodyBuilder.appendFormalLine("");

        // populateItemForRender(request, vet, true);
        bodyBuilder.appendFormalLine(String.format(
                "populateItemForRender(%s, %s, true);",
                REQUEST_PARAM_NAME.getSymbolName(), entityName));
        bodyBuilder.appendFormalLine("");

        // dispatcher.include(request, new HttpServletResponseWrapper(response)
        // {
        bodyBuilder.appendFormalLine(String.format(
                "dispatcher.include(%s, new %s(response) {",
                REQUEST_PARAM_NAME.getSymbolName(),
                helper.getFinalTypeName(HTTP_SERVLET_RESPONSE_WRAPPER)));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("");

        String printWriter = helper.getFinalTypeName(PRINT_WRITER);
        // private PrintWriter writer = new PrintWriter(buffer);
        bodyBuilder.appendFormalLine(String.format(
                "private %s writer = new PrintWriter(buffer);", printWriter,
                printWriter));
        bodyBuilder.appendFormalLine("");

        // @Override
        bodyBuilder.appendFormalLine("@Override");

        // public PrintWriter getWriter() throws IOException {
        bodyBuilder.appendFormalLine(String.format(
                "public %s getWriter() throws %s {", printWriter,
                helper.getFinalTypeName(IO_EXCEPTION)));
        bodyBuilder.indent();

        // return writer;
        bodyBuilder.appendFormalLine("return writer;");

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // });
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("});");

        // String render = buffer.toString();
        bodyBuilder.appendFormalLine("String render = buffer.toString();");
        bodyBuilder.appendFormalLine("");

        // // Put rendered content into first column
        bodyBuilder
                .appendFormalLine("// Put rendered content into first column");

        // item.put("form", render);
        bodyBuilder.appendFormalLine("item.put(\"form\", render);");

        // result.add(item);
        bodyBuilder.appendFormalLine("result.add(item);");
        bodyBuilder.appendFormalLine("");

        // return result;
        bodyBuilder.appendFormalLine("return result;");
    }

    /**
     * Return method <code>renderUpdateForms</code>
     * 
     * @return
     */
    private MethodMetadata getRenderUpdateFormsMethod() {
        // method name
        JavaSymbolName methodName = RENDER_UPDATE_FORMS_METHOD;

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(
                AnnotatedJavaType.convertFromJavaType(entityListType),
                AnnotatedJavaType.convertFromJavaType(HTTP_SERVLET_REQUEST),
                AnnotatedJavaType.convertFromJavaType(HTTP_SERVLET_RESPONSE));

        // Check if a method exist in type
        final MethodMetadata method = methodExists(methodName, parameterTypes);

        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(entityList));
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildRenderUpdateFormsMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, LIST_MAP_STRING_STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    private void buildRenderUpdateFormsMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // // Prepare result
        bodyBuilder.appendFormalLine("// Prepare result");

        // List<Map<String, String>> result = new ArrayList<Map<String,
        // String>>(vets.size());
        bodyBuilder.appendFormalLine(String.format(
                "%s result = new %s(%s.size());",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                helper.getFinalTypeName(ARRAYLIST_MAP_STRING_STRING),
                entityList));

        // String controllerPath = "vets";
        bodyBuilder.appendFormalLine(String.format(
                "String controllerPath = \"%s\";",
                webScaffoldAnnotationValues.getPath()));

        // String pageToUse = "update";
        bodyBuilder.appendFormalLine("String pageToUse = \"update\";");

        // String renderUrl = String.format("/WEB-INF/views/%s/%s.jspx",
        // controllerPath, pageToUse);
        bodyBuilder
                .appendFormalLine("String renderUrl = String.format(\"/WEB-INF/views/%s/%s.jspx\", controllerPath, pageToUse);");

        // // For every element
        bodyBuilder.appendFormalLine("// For every element");

        // for (Vet vet : vets) {
        bodyBuilder.appendFormalLine(String.format("for (%s %s : %s) {",
                helper.getFinalTypeName(entity), entityName, entityList));
        bodyBuilder.indent();

        // Map<String, String> item = new HashMap<String, String>();
        bodyBuilder.appendFormalLine(String.format("%s item = new %s();",
                helper.getFinalTypeName(MAP_STRING_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_STRING)));

        // final StringWriter buffer = new StringWriter();
        bodyBuilder.appendFormalLine(String.format(
                "final %s buffer = new %s();",
                helper.getFinalTypeName(STRING_WRITER),
                helper.getFinalTypeName(STRING_WRITER)));
        // // Call JSP to render update form
        bodyBuilder.appendFormalLine("// Call JSP to render update form");

        // RequestDispatcher dispatcher =
        // request.getRequestDispatcher(renderUrl);
        bodyBuilder.appendFormalLine(String.format(
                "%s dispatcher = %s.getRequestDispatcher(renderUrl);",
                helper.getFinalTypeName(REQUEST_DISPATCHER),
                REQUEST_PARAM_NAME.getSymbolName()));

        // populateItemForRender(request, vet, true);
        bodyBuilder.appendFormalLine(String.format(
                "populateItemForRender(%s, %s, true);",
                REQUEST_PARAM_NAME.getSymbolName(), entityName));

        // dispatcher.include(request, new HttpServletResponseWrapper(response)
        // {
        bodyBuilder.appendFormalLine(String.format(
                "dispatcher.include(%s, new %s(response) {",
                REQUEST_PARAM_NAME.getSymbolName(),
                helper.getFinalTypeName(HTTP_SERVLET_RESPONSE_WRAPPER)));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("");

        String printWriter = helper.getFinalTypeName(PRINT_WRITER);
        // private PrintWriter writer = new PrintWriter(buffer);
        bodyBuilder.appendFormalLine(String.format(
                "private %s writer = new PrintWriter(buffer);", printWriter,
                printWriter));
        bodyBuilder.appendFormalLine("");

        // @Override
        bodyBuilder.appendFormalLine("@Override");

        // public PrintWriter getWriter() throws IOException {
        bodyBuilder.appendFormalLine(String.format(
                "public %s getWriter() throws %s {", printWriter,
                helper.getFinalTypeName(IO_EXCEPTION)));
        bodyBuilder.indent();

        // return writer;
        bodyBuilder.appendFormalLine("return writer;");

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // });
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("});");

        // String render = buffer.toString();
        bodyBuilder.appendFormalLine("String render = buffer.toString();");

        // // Load item id
        bodyBuilder.appendFormalLine("// Load item id");

        String idAccessor = "get".concat(StringUtils
                .capitalize(entityIdentifier.getFieldName().getSymbolName()));
        // item.put("DT_RowId",
        // conversionService_datatables.convert(vet.getId(), String.class));
        bodyBuilder.appendFormalLine(String.format(
                "item.put(\"DT_RowId\", %s.convert(%s.%s(), String.class));",
                getConversionServiceField().getFieldName().getSymbolName(),
                entityName, idAccessor));

        // // Put rendered content into first column
        bodyBuilder
                .appendFormalLine("// Put rendered content into first column");

        // item.put("form", render);
        bodyBuilder.appendFormalLine("item.put(\"form\", render);");

        // result.add(item);
        bodyBuilder.appendFormalLine("result.add(item);");

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return result;
        bodyBuilder.appendFormalLine("return result;");
    }

    /**
     * Gets <code>renderXXX</code> method. <br>
     * This method renders the required jspx for item an store it in a List of
     * Map(String,String)
     * 
     * @return
     */
    private MethodMetadata getRenderItemsMethod() {

        JavaType entitySearchResult = new JavaType(
                SEARCH_RESULTS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(entity));

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entitySearchResult, HTTP_SERVLET_REQUEST,
                        HTTP_SERVLET_RESPONSE);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(renderItemsMethodName,
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
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("searchResult"));
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildRenderItemsMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, renderItemsMethodName,
                LIST_MAP_STRING_STRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance

    }

    /**
     * Builds the method body of <code>renderXXX</code> method. <br>
     * This method renders the required jspx for item an store it in a List of
     * Map(String,String)
     * 
     * @param bodyBuilder
     */
    private void buildRenderItemsMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        String entityTypeName = helper.getFinalTypeName(entity);
        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Prepare result");

        /*
         * List<Pets> pets = searchResult.getResults();
         */
        bodyBuilder.appendFormalLine(String.format(
                "%s %s = searchResult.getResults();",
                helper.getFinalTypeName(entityListType), entityList));
        /*
         * List<Map<String, String>> result = new ArrayList<Map<String,
         * String>>();
         */
        bodyBuilder.appendFormalLine(String.format(
                "%s result = new %s(%s.size());",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                helper.getFinalTypeName(ARRAYLIST_MAP_STRING_STRING),
                entityList));
        bodyBuilder.appendFormalLine(String.format(
                "String controllerPath = \"%s\";",
                webScaffoldAnnotationValues.getPath()));
        bodyBuilder.appendFormalLine(String.format(
                "String pageToUse = \"%s\";", annotationValues.getMode()));
        bodyBuilder
                .appendFormalLine("String renderUrl = String.format(\"/WEB-INF/views/%s/%s.jspx\", controllerPath, pageToUse);");

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// For every element");
        // for (Pet pet : owners) {
        bodyBuilder.appendFormalLine(String.format("for (%s %s: %s) {",
                entityTypeName, entityName, entityList));
        bodyBuilder.indent();

        // Map<String, String> row = new HashMap<String, String>();
        bodyBuilder.appendFormalLine(String.format("%s item = new %s();",
                helper.getFinalTypeName(MAP_STRING_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_STRING)));
        // final StringWriter buffer = new StringWriter();
        bodyBuilder.appendFormalLine(String.format(
                "final %s buffer = new %s();",
                helper.getFinalTypeName(STRING_WRITER),
                helper.getFinalTypeName(STRING_WRITER)));

        // TODO Check it can get dispatcher outside of for
        bodyBuilder.appendFormalLine("// Call JSP to render current entity");
        // RequestDispatcher dispatcher =
        // request.getRequestDispatcher("/WEB-INF/views/owners/show.jspx");
        bodyBuilder.appendFormalLine(String.format(
                "%s dispatcher = %s.getRequestDispatcher(renderUrl);",
                helper.getFinalTypeName(REQUEST_DISPATCHER),
                REQUEST_PARAM_NAME.getSymbolName()));
        bodyBuilder.appendFormalLine("");

        // populateItemForRender(request, pet, false);
        bodyBuilder.appendFormalLine(String.format(
                "populateItemForRender(%s, %s, false);",
                REQUEST_PARAM_NAME.getSymbolName(), entityName));
        // dispatcher.include(request, new HttpServletResponseWrapper(response)
        // {
        bodyBuilder.appendFormalLine(String.format(
                "dispatcher.include(%s, new %s(%s) {",
                REQUEST_PARAM_NAME.getSymbolName(),
                helper.getFinalTypeName(HTTP_SERVLET_RESPONSE_WRAPPER),
                RESPONSE_PARAM_NAME.getSymbolName()));
        bodyBuilder.indent();
        // private PrintWriter writer = new PrintWriter(buffer);
        bodyBuilder.appendFormalLine(String.format(
                "private %s writer = new %s(buffer);",
                helper.getFinalTypeName(PRINT_WRITER),
                helper.getFinalTypeName(PRINT_WRITER)));
        // @Override
        bodyBuilder.appendFormalLine("@Override");
        // public PrintWriter getWriter() throws IOException {
        bodyBuilder.appendFormalLine(String.format(
                "public %s getWriter() throws %s {",
                helper.getFinalTypeName(PRINT_WRITER),
                helper.getFinalTypeName(IO_EXCEPTION)));
        bodyBuilder.indent();
        // return writer;
        bodyBuilder.appendFormalLine("return writer;");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("});");

        bodyBuilder.appendFormalLine("");

        // String render = buffer.toString();
        bodyBuilder.appendFormalLine("String render = buffer.toString();");

        bodyBuilder.appendFormalLine("// Load item id)");
        // row.put("DT_RowId",
        // conversionService_datatables.convert(owner.getId(), String.class));
        bodyBuilder
                .appendFormalLine(String
                        .format("item.put(\"DT_RowId\", %s.convert(%s.get%s(), String.class));",
                                getConversionServiceField().getFieldName()
                                        .getSymbolName(), entityName,
                                StringUtils.capitalize(entityIdentifier
                                        .getFieldName().getSymbolName())));
        bodyBuilder
                .appendFormalLine("// Put rendered content into first column (uses column index)");
        // row.put(Integer.toString(rowIdx), showOwner);
        bodyBuilder.appendFormalLine("item.put(\"0\", render);");

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("result.add(item);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");

        bodyBuilder.appendFormalLine("return result;");

    }

    /**
     * Gets <code>getFindByParameters</code> method. <br>
     * This method generates a item List based on a parameters received in the
     * request. Used by DOM mode listDatatables.
     * 
     * @return
     */
    private MethodMetadata getFindByParametersMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entity, ENUMERATION_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(findByParametersMethodName,
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
        buildFindByParametersMapMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, findByParametersMethodName,
                entityListType, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>getFindByParameters</code> method. <br>
     * This method generates a item List based on a parameters received in the
     * request. Used by DOM mode listDatatables.
     * 
     * @param bodyBuilder
     */
    private void buildFindByParametersMapMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Gets propertyMap
        bodyBuilder.appendFormalLine("// Gets propertyMap");
        // Map<String, Object> propertyMap = getPropertyMap(visit,
        // propertyNames);
        bodyBuilder.appendFormalLine(String.format(
                "%s propertyMap = %s(%s, propertyNames);",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                GET_PROPERTY_MAP.getSymbolName(), entityName));

        //
        bodyBuilder.appendFormalLine("");
        //
        // // if there is a filter
        bodyBuilder.appendFormalLine("// if there is a filter");
        // if (!propertyMap.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!propertyMap.isEmpty()) {");
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
                .appendFormalLine("for (String key : propertyMap.keySet()) {");
        bodyBuilder.indent();
        // baseFilterPredicate.and(entity.get(key).eq(propertyMap.get(key)));
        bodyBuilder
                .appendFormalLine("baseFilterPredicate.and(entity.get(key).eq(propertyMap.get(key)));");
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
     * Gets <code>getPropertyMap</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @return
     */
    private MethodMetadata getGetPropertyMapMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entity, ENUMERATION_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_PROPERTY_MAP,
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
        buildGetPropertyMapMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_PROPERTY_MAP, MAP_STRING_OBJECT,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getPropertyMap</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @return
     */
    private MethodMetadata getGetPropertyMapUrlMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(entity, HTTP_SERVLET_REQUEST);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_PROPERTY_MAP,
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
        parameterNames.add(new JavaSymbolName("request"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetPropertyMapUrlMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_PROPERTY_MAP, MAP_STRING_OBJECT,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>setDatatablesBaseFilter</code> method. <br>
     * This method is used to set baseFilters into datatables
     * 
     * @param annotationValues
     * 
     * @return
     */
    private MethodMetadata getSetDatatablesBaseFilterMethod(
            DatatablesAnnotationValues annotationValues) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MAP_STRING_OBJECT);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SET_BASE_FILTER_METHOD,
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
        parameterNames.add(new JavaSymbolName("propertyMap"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetDatatablesBaseFilterMethodBody(bodyBuilder, annotationValues);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, SET_BASE_FILTER_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Returns <code>listDatatablesDetail</code> method <br>
     * This method is default list request handler for detail datatables
     * controllers
     * 
     * @return
     */
    private MethodMetadata getListDatatablesDetailMethod() {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MODEL, HTTP_SERVLET_REQUEST);

        // Include Item in parameters to use spring's binder to get baseFilter
        // values
        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(new JavaSymbolName(
                DatatablesConstants.LIST_DTTBLS_DET_MTHD_NAME), parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        // @RequestMapping(... produces = "text/html")
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_PROD_NAME,
                DatatablesConstants.RQST_MAP_ANN_VAL_HTML);

        // @RequestMapping(... value ="/list")
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_VAL_NAME,
                DatatablesConstants.RQST_MAP_ANN_LIST);

        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);
        parameterNames.add(new JavaSymbolName(
                DatatablesConstants.REQUEST_PARAMETER_NAME));

        // Include Item in parameters to use spring's binder to get baseFilter
        // values
        parameterNames.add(new JavaSymbolName(entityName));

        // Add method javadoc (not generated to disk because #10229)
        CommentStructure comments = new CommentStructure();
        JavadocComment javadoc = new JavadocComment(
                "Show only the list view fragment for entity as detail datatables into a master datatables.");
        comments.addComment(javadoc, CommentStructure.CommentLocation.BEGINNING);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        // [Code generated]
        bodyBuilder
                .appendFormalLine("// Do common datatables operations: get entity list filtered by request parameters");

        if (!isAjax()) {
            // listDatatables(uiModel, request, pet);
            bodyBuilder.appendFormalLine(LIST_DATATABLES.getSymbolName()
                    .concat("(uiModel, request, ")
                    .concat(entity.getSimpleTypeName().toLowerCase())
                    .concat(");"));
        }
        else {
            // listDatatables(uiModel, request);
            bodyBuilder.appendFormalLine(LIST_DATATABLES.getSymbolName()
                    .concat("(uiModel, request);"));
        }

        bodyBuilder
                .appendFormalLine("// Show only the list fragment (without footer, header, menu, etc.) ");
        // return "forward:/WEB-INF/views/pets/list.jspx";
        bodyBuilder.appendFormalLine("return \"forward:/WEB-INF/views/".concat(
                webScaffoldAnnotationValues.getPath()).concat("/list.jspx\";"));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, new JavaSymbolName(
                        DatatablesConstants.LIST_DTTBLS_DET_MTHD_NAME),
                JavaType.STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setCommentStructure(comments);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>createDatatablesDetail</code> method <br>
     * This method is default create request handler for detail datatables
     * controllers
     * 
     * @return
     */
    private MethodMetadata getCreateDatatablesDetailMethod() {

        // @RequestMapping(method = RequestMethod.POST, produces = "text/html",
        // params = "datatablesRedirect")
        // public String createDatatablesDetail(@RequestParam(value =
        // "datatablesRedirect", required = true) String redirect,
        // @Valid Pet pet, BindingResult bindingResult, Model uiModel,
        // HttpServletRequest httpServletRequest) {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(helper.createRequestParam(JavaType.STRING,
                DATATABLES_REDIRECT, true, null));
        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(VALID).build()));
        parameterTypes.addAll(AnnotatedJavaType.convertFromJavaTypes(
                new JavaType("org.springframework.validation.BindingResult"),
                MODEL, REDIRECT_ATTRIBUTES, HTTP_SERVLET_REQUEST));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(new JavaSymbolName(
                "createDatatablesDetail"), parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping(method = RequestMethod.POST, produces = "text/html",
        // params = "datatablesRedirect")
        AnnotationMetadataBuilder requestMappingAnnotation = helper
                .getRequestMappingAnnotation(null, null, null,
                        DatatablesConstants.RQST_MAP_ANN_VAL_HTML, null, null);
        requestMappingAnnotation.addEnumAttribute(METHOD_VAR, REQUEST_METHOD,
                "POST");
        requestMappingAnnotation.addStringAttribute(PARAMS_VAR,
                DATATABLES_REDIRECT);
        annotations.add(requestMappingAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName("redirect"), new JavaSymbolName(entity
                        .getSimpleTypeName().toLowerCase()),
                new JavaSymbolName("bindingResult"), UI_MODEL, REDIRECT_MODEL,
                new JavaSymbolName("httpServletRequest"));

        // Add method javadoc (not generated to disk because #10229)
        CommentStructure comments = new CommentStructure();
        JavadocComment javadoc = new JavadocComment(
                "Create an entity and redirect to given URL.");
        comments.addComment(javadoc, CommentStructure.CommentLocation.BEGINNING);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildCreateDatatablesDetailMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, new JavaSymbolName(
                        "createDatatablesDetail"), JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setCommentStructure(comments);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build method body for createDatatablesDetails
     * 
     * @param bodyBuilder
     */
    private void buildCreateDatatablesDetailMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        String entityParamName = entity.getSimpleTypeName().toLowerCase();
        bodyBuilder
                .appendFormalLine("// Do common create operations (check errors, populate, persist, ...)");
        // String view = create(pet, bindingResult, uiModel,
        // httpServletRequest);
        bodyBuilder.appendFormalLine("String view = create(".concat(
                entityParamName).concat(
                ", bindingResult, uiModel, httpServletRequest);"));
        // if (bindingResult.hasErrors() || redirect == null ||
        // redirect.trim().isEmpty()) {
        // return view;
        // }
        bodyBuilder
                .appendFormalLine("// If binding errors or no redirect, return common create error view (remain in create form)");
        bodyBuilder
                .appendFormalLine("if (bindingResult.hasErrors() || redirect == null || redirect.trim().isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return view;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // String[] paramValues =
        // httpServletRequest.getParameterValues("dtt_table_id_hash");
        bodyBuilder
                .appendFormalLine("String[] paramValues = httpServletRequest.getParameterValues(\"dtt_table_id_hash\");");

        // if(paramValues != null && paramValues.length > 0) {
        bodyBuilder
                .appendFormalLine("if(paramValues != null && paramValues.length > 0) {");
        bodyBuilder.indent();

        // redirectModel.addFlashAttribute("dtt_table_id_hash", paramValues[0]);
        bodyBuilder
                .appendFormalLine("redirectModel.addFlashAttribute(\"dtt_table_id_hash\", paramValues[0]);");
        bodyBuilder.indentRemove();

        // }else{
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();

        // redirectModel.addFlashAttribute("dtt_table_id_hash", "");
        bodyBuilder
                .appendFormalLine("redirectModel.addFlashAttribute(\"dtt_table_id_hash\", \"\");");
        bodyBuilder.indentRemove();

        // }
        bodyBuilder.appendFormalLine("}");

        // uiModel.addFlashAttribute(DatatablesUtils.ROWS_ON_TOP_IDS_PARAM,
        // pet.getId());
        bodyBuilder.appendFormalLine(String.format(
                "%s.addFlashAttribute(%s.ROWS_ON_TOP_IDS_PARAM, %s.%s());",
                REDIRECT_MODEL.getSymbolName(), helper
                        .getFinalTypeName(DATATABLES_UTILS), entityParamName,
                helper.getGetterMethodNameForField(entityIdentifier
                        .getFieldName())));

        bodyBuilder
                .appendFormalLine("// If create success, redirect to given URL: master datatables");
        // return "redirect:".concat(redirect);
        bodyBuilder.appendFormalLine("return \"redirect:\".concat(redirect);");
    }

    /**
     * Returns <code>updateDatatablesDetail</code> method <br>
     * This method is default update request handler for detail datatables
     * controllers
     * 
     * @return
     */
    private MethodMetadata getUpdateDatatablesDetailMethod() {

        // @RequestMapping(method = RequestMethod.PUT, produces = "text/html",
        // params = "datatablesRedirect")
        // public String updateDatatablesDetail(@RequestParam(value =
        // "datatablesRedirect", required = true) String redirect,
        // @Valid Pet pet, BindingResult bindingResult, Model uiModel,
        // HttpServletRequest httpServletRequest) {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(helper.createRequestParam(JavaType.STRING,
                DATATABLES_REDIRECT, true, null));
        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(VALID).build()));
        parameterTypes.addAll(AnnotatedJavaType.convertFromJavaTypes(
                BINDING_RESULT, MODEL, REDIRECT_ATTRIBUTES,
                HTTP_SERVLET_REQUEST));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(new JavaSymbolName(
                "updateDatatablesDetail"), parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping(method = RequestMethod.PUT, produces = "text/html",
        // params = "datatablesRedirect")
        AnnotationMetadataBuilder requestMappingAnnotation = helper
                .getRequestMappingAnnotation(null, null, null,
                        DatatablesConstants.RQST_MAP_ANN_VAL_HTML, null, null);
        requestMappingAnnotation.addEnumAttribute(METHOD_VAR, REQUEST_METHOD,
                "PUT");
        requestMappingAnnotation.addStringAttribute(PARAMS_VAR,
                DATATABLES_REDIRECT);
        annotations.add(requestMappingAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName("redirect"), new JavaSymbolName(entity
                        .getSimpleTypeName().toLowerCase()),
                new JavaSymbolName("bindingResult"), UI_MODEL, REDIRECT_MODEL,
                new JavaSymbolName("httpServletRequest"));

        // Add method javadoc (not generated to disk because #10229)
        CommentStructure comments = new CommentStructure();
        JavadocComment javadoc = new JavadocComment(
                "Update an entity and redirect to given URL.");
        comments.addComment(javadoc, CommentStructure.CommentLocation.BEGINNING);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        buildUpdateDatatablesDetailMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, new JavaSymbolName(
                        "updateDatatablesDetail"), JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setCommentStructure(comments);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    private void buildUpdateDatatablesDetailMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        String entityParamName = entity.getSimpleTypeName().toLowerCase();
        bodyBuilder
                .appendFormalLine("// Do common update operations (check errors, populate, merge, ...)");
        // String view = update(pet, bindingResult, uiModel,
        // httpServletRequest);
        bodyBuilder.appendFormalLine("String view = update(".concat(
                entityParamName).concat(
                ", bindingResult, uiModel, httpServletRequest);"));
        // if (bindingResult.hasErrors() || redirect == null ||
        // redirect.trim().isEmpty()) {
        // return view;
        // }
        bodyBuilder
                .appendFormalLine("// If binding errors or no redirect, return common update error view (remain in update form)");
        bodyBuilder
                .appendFormalLine("if (bindingResult.hasErrors() || redirect == null || redirect.trim().isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return view;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // String[] paramValues =
        // httpServletRequest.getParameterValues("dtt_table_id_hash");
        bodyBuilder
                .appendFormalLine("String[] paramValues = httpServletRequest.getParameterValues(\"dtt_table_id_hash\");");

        // if(paramValues != null && paramValues.length > 0) {
        bodyBuilder
                .appendFormalLine("if(paramValues != null && paramValues.length > 0) {");
        bodyBuilder.indent();

        // redirectModel.addFlashAttribute("dtt_table_id_hash", paramValues[0]);
        bodyBuilder
                .appendFormalLine("redirectModel.addFlashAttribute(\"dtt_table_id_hash\", paramValues[0]);");
        bodyBuilder.indentRemove();

        // }else{
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();

        // redirectModel.addFlashAttribute("dtt_table_id_hash", "");
        bodyBuilder
                .appendFormalLine("redirectModel.addFlashAttribute(\"dtt_table_id_hash\", \"\");");
        bodyBuilder.indentRemove();

        // }
        bodyBuilder.appendFormalLine("}");

        // uiModel.addFlashAttribute(DatatablesUtils.ROWS_ON_TOP_IDS_PARAM,
        // pet.getId());
        bodyBuilder.appendFormalLine(String.format(
                "%s.addFlashAttribute(%s.ROWS_ON_TOP_IDS_PARAM, %s.%s());",
                REDIRECT_MODEL.getSymbolName(), helper
                        .getFinalTypeName(DATATABLES_UTILS), entityParamName,
                helper.getGetterMethodNameForField(entityIdentifier
                        .getFieldName())));

        bodyBuilder
                .appendFormalLine("// If update success, redirect to given URL: master datatables");
        // return "redirect:".concat(redirect);
        bodyBuilder.appendFormalLine("return \"redirect:\".concat(redirect);");
    }

    /**
     * Returns <code>deleteDatatablesDetail</code> method <br>
     * This method is default delete request handler for detail datatables
     * controllers
     * 
     * @return
     */
    private MethodMetadata getDeleteDatatablesDetailMethod() {

        // @RequestMapping(value = "/{id}", method = RequestMethod.DELETE,
        // produces = "text/html", params = "datatablesRedirect")
        // public String deleteDatatablesDetail(@RequestParam(value =
        // "datatablesRedirect", required = true) String redirect,
        // @PathVariable("id") Long id, @RequestParam(value = "page", required =
        // false) Integer page,
        // @RequestParam(value = "size", required = false) Integer size, Model
        // uiModel) {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(helper.createRequestParam(JavaType.STRING,
                DATATABLES_REDIRECT, true, null));
        final List<AnnotationAttributeValue<?>> annotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        annotationAttributes.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "id"));
        parameterTypes.add(new AnnotatedJavaType(entityIdentifier
                .getFieldType(), new AnnotationMetadataBuilder(new JavaType(
                "org.springframework.web.bind.annotation.PathVariable"),
                annotationAttributes).build()));
        parameterTypes.add(helper.createRequestParam(JavaType.INT_OBJECT,
                PAGE_VAR, false, null));
        parameterTypes.add(helper.createRequestParam(JavaType.INT_OBJECT,
                SIZE_VAR, false, null));
        parameterTypes.addAll(AnnotatedJavaType.convertFromJavaTypes(MODEL));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(new JavaSymbolName(
                "deleteDatatablesDetail"), parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping(value = "/{id}", method = RequestMethod.DELETE,
        // produces = "text/html", params = "datatablesRedirect")
        AnnotationMetadataBuilder requestMappingAnnotation = helper
                .getRequestMappingAnnotation(null, null, null,
                        DatatablesConstants.RQST_MAP_ANN_VAL_HTML, null, null);
        requestMappingAnnotation.addEnumAttribute(METHOD_VAR, REQUEST_METHOD,
                "DELETE");
        requestMappingAnnotation.addStringAttribute(PARAMS_VAR,
                DATATABLES_REDIRECT);
        requestMappingAnnotation.addStringAttribute("value", "/{id}");
        annotations.add(requestMappingAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName("redirect"), new JavaSymbolName("id"),
                new JavaSymbolName(PAGE_VAR), new JavaSymbolName(SIZE_VAR),
                UI_MODEL);

        // Add method javadoc (not generated to disk because #10229)
        CommentStructure comments = new CommentStructure();
        JavadocComment javadoc = new JavadocComment(
                "Delete an entity and redirect to given URL.");
        comments.addComment(javadoc, CommentStructure.CommentLocation.BEGINNING);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder
                .appendFormalLine("// Do common delete operations (find, remove, add pagination attributes, ...)");
        // String view = delete(id, page, size, uiModel);
        bodyBuilder
                .appendFormalLine("String view = delete(id, page, size, uiModel);");
        // if (redirect == null || redirect.trim().isEmpty()) {
        // return view;
        // }
        bodyBuilder
                .appendFormalLine("// If no redirect, return common list view");
        bodyBuilder
                .appendFormalLine("if (redirect == null || redirect.trim().isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return view;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("// Redirect to given URL: master datatables");
        // return "redirect:".concat(redirect);
        bodyBuilder.appendFormalLine("return \"redirect:\".concat(redirect);");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, new JavaSymbolName(
                        "deleteDatatablesDetail"), JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setCommentStructure(comments);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build body method <code>getPropertyMap</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @return
     */
    private void buildGetPropertyMapMethodBody(
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
     * Build body method <code>getPropertyMap</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @return
     */
    private void buildGetPropertyMapUrlMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // URL parameters are used as base search filters
        bodyBuilder.appendFormalLine(PARAMETERS_AS_BASE_SEARCH);

        /*
         * @SuppressWarnings("unchecked") Map<String, Object> propertyValuesMap
         * = getPropertyMap(visit, request.getParameterNames());
         */
        bodyBuilder
                .appendFormalLine(String
                        .format("@SuppressWarnings(\"unchecked\") %s propertyValuesMap = getPropertyMap(%s, request.getParameterNames());",
                                helper.getFinalTypeName(MAP_STRING_OBJECT),
                                entityName));

        /*
         * // Add to the property map the parameters used as query operators
         * Map<String, Object> params = new
         * HashMap<String,Object>(populateParametersMap(request)); Set<String>
         * keySet = params.keySet();
         */
        bodyBuilder
                .appendFormalLine("// Add to the property map the parameters used as query operators");
        bodyBuilder.appendFormalLine(String.format(
                "%s params = new %s(populateParametersMap(request));",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));
        bodyBuilder.appendFormalLine(String.format(
                "%s keySet = params.keySet();",
                helper.getFinalTypeName(SET_STRING)));

        /*
         * for (String key : keySet) { if
         * (key.startsWith(QuerydslUtils.OPERATOR_PREFIX)) {
         * propertyValuesMap.put(key, params.get(key)); } else if
         * (DatatablesUtils.ROWS_ON_TOP_IDS_PARAM.equals(key)) {
         * propertyValuesMap.put(key, request.getParams().get(key)); } }
         */
        bodyBuilder.appendFormalLine("for (String key : keySet) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format(
                "if (key.startsWith(%s.OPERATOR_PREFIX)) {",
                helper.getFinalTypeName(QUERYDSL_UTILS)));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("propertyValuesMap.put(key, params.get(key));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(String.format(
                "} else if (%s.ROWS_ON_TOP_IDS_PARAM.equals(key)) {",
                helper.getFinalTypeName(DATATABLES_UTILS)));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("propertyValuesMap.put(key, request.getParameterMap().get(key));");
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if(DatatablesUtils.BOUNDING_BOX_PARAM.equals(key) || DatatablesUtils.BOUNDING_BOX_FIELDS_PARAM.equals(key)){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("propertyValuesMap.put(key, request.getParameterMap().get(key));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return propertyValuesMap;
        bodyBuilder.appendFormalLine("return propertyValuesMap;");

    }

    /**
     * Build body method <code>setDatatablesBaseFilter</code> method. <br>
     * This method returns a Map with bean properties which appears on a
     * Enumeration (usually from httpRequest.getParametersNames())
     * 
     * @param annotationValues2
     * 
     * @return
     */
    private void buildSetDatatablesBaseFilterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            DatatablesAnnotationValues annotationValues) {

        // Add here your baseFilters to propertyMap.
        bodyBuilder
                .appendFormalLine("// Add here your baseFilters to propertyMap.");

        String baseFilter = annotationValues.getBaseFilter();

        if (baseFilter != null) {

            for (String i : baseFilter.split("And")) {

                String[] values = i.split("[A-Z][A-Z]+");

                String operation = "";
                for (BaseFilterOperationTypes op : BaseFilterOperationTypes
                        .values()) {
                    if (i.contains(op.toString()))
                        operation = op.toString();
                }

                if (operation.equals("")) {
                    LOGGER.log(Level.INFO, "Invalid operation for base filter.");
                }
                else {

                    String safevalue = "";
                    if (values.length > 1)
                        safevalue = values[1];

                    bodyBuilder.appendFormalLine("propertyMap.put(\""
                            + values[0] + "\",\"" + safevalue + ENDING);
                    bodyBuilder.appendFormalLine("propertyMap.put(\"_operator_"
                            + values[0] + "\",\"" + operation + ENDING);
                }
            }
        }
    }

    /**
     * Gets <code>populateParameterMap</code> method. This method transforms a
     * HttpServlerRequest Map<String,String[]> into a Map<String,String>
     * 
     * @return
     */
    private MethodMetadata getPopulateParameterMapMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(HTTP_SERVLET_REQUEST);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_PARAMETERS_MAP,
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
        parameterNames.add(new JavaSymbolName(
                DatatablesConstants.REQUEST_PARAMETER_NAME));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildPopulateParameterMapMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_PARAMETERS_MAP,
                MAP_STRING_STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build body method of <code>populateParameterMap</code> method. This
     * method transforms a HttpServlerRequest Map<String,String[]> into a
     * Map<String,String>
     * 
     * @param bodyBuilder
     */
    private void buildPopulateParameterMapMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Map<String, Object> params;
        bodyBuilder.appendFormalLine(String.format("%s params;",
                helper.getFinalTypeName(MAP_STRING_OBJECT)));
        // if (request == null) {
        bodyBuilder.appendFormalLine("if (request == null) {");
        bodyBuilder.indent();
        // params = Collections.emptyMap();
        bodyBuilder.appendFormalLine(String.format("params = %s.emptyMap();",
                helper.getFinalTypeName(COLLECTIONS)));
        // } else {
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(ELSE);
        bodyBuilder.indent();
        // params = new HashMap<String, Object>(request.getParameterMap());
        bodyBuilder.appendFormalLine(String.format(
                "params = new %s(request.getParameterMap());",
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        //
        bodyBuilder.appendFormalLine("");

        // Map<String, String> allParams = new HashMap<String,
        // String>(params.size());
        bodyBuilder.appendFormalLine(String.format(
                "%s allParams = new %s(params.size());",
                helper.getFinalTypeName(MAP_STRING_STRING),
                helper.getFinalTypeName(HASHMAP_STRING_STRING)));
        //
        bodyBuilder.appendFormalLine("");

        // String value;
        bodyBuilder.appendFormalLine("String value;");
        // String objValue;
        bodyBuilder.appendFormalLine("Object objValue;");
        // for (String key : params.keySet()) {
        bodyBuilder.appendFormalLine("for (String key : params.keySet()) {");
        bodyBuilder.indent();

        bodyBuilder.appendFormalLine("objValue = params.get(key);");

        // if (objValue instanceof String[]) {
        bodyBuilder.appendFormalLine("if (objValue instanceof String[]) {");
        bodyBuilder.indent();
        // value = ((String[]) entry.getValue())[0];
        bodyBuilder.appendFormalLine("value = ((String[]) objValue)[0];");
        // } else {
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(ELSE);
        bodyBuilder.indent();
        // value = (String) entry.getValue();
        bodyBuilder.appendFormalLine("value = (String) objValue;");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // allParams.put(entry.getKey(), value);
        bodyBuilder.appendFormalLine("allParams.put(key, value);");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // return allParams;
        bodyBuilder.appendFormalLine("return allParams;");

    }

    /**
     * Gets a method to manage AJAX data request of a datatables which draw the
     * result of a Roo Dynamic finder.
     * 
     * @param finderMethod
     * @param queryHolder
     * @return
     */
    private MethodMetadata getAjaxFinderMethod(
            FinderMetadataDetails finderMethod, QueryHolderTokens queryHolder) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Add datatable request parameters
        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));

        // Prepares @RequestParam method parameters based on
        // finder information
        List<AnnotatedJavaType> finderParamTypes = finderMethod
                .getFinderMethodMetadata().getParameterTypes();
        List<JavaSymbolName> finderParamNames = finderMethod
                .getFinderMethodMetadata().getParameterNames();
        JavaType paramType;
        for (int i = 0; i < finderParamTypes.size(); i++) {
            paramType = finderParamTypes.get(i).getJavaType();
            if (paramType.isBoolean()) {
                // Boolean's false value is omitted on request, so must be
                // optional (by default will get false)
                parameterTypes.add(helper.createRequestParam(paramType,
                        finderParamNames.get(i).getSymbolName(), false, null));
            }
            else {
                parameterTypes.add(helper.createRequestParam(paramType,
                        finderParamNames.get(i).getSymbolName(), null, null));
            }
        }
        if (!isStantardMode()) {
            // For render mode request and response are needed to
            // perform internal request for item render
            parameterTypes.add(AnnotatedJavaType
                    .convertFromJavaType(HTTP_SERVLET_REQUEST));
            parameterTypes.add(AnnotatedJavaType
                    .convertFromJavaType(HTTP_SERVLET_RESPONSE));
        }

        JavaSymbolName methodName = new JavaSymbolName(
                finderMethod.getFinderName());

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }
        // get Finder-name value
        String finderNameValue = finderMethod.getFinderName();
        finderNameValue = finderNameValue.substring(finderNameValue
                .indexOf("By"));
        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute(HEADERS, ACCEPT_APPLICATION_JSON);
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_VAL_NAME, "/datatables/ajax");
        methodAnnotation.addStringAttribute(PARAMS_VAR,
                "ajax_find=".concat(finderNameValue));
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_PROD_NAME, APPLICATION_JSON);
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        if (!isStantardMode()) {
            // On render mode internal render request
            // can throw ServletException or IOException
            throwsTypes.add(SERVLET_EXCEPTION);
            throwsTypes.add(IO_EXCEPTION);
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        for (JavaSymbolName paramName : finderParamNames) {
            parameterNames.add(paramName);
        }
        if (!isStantardMode()) {
            // For render mode request and response are needed to
            // perform internal request for item render
            parameterNames.add(REQUEST_PARAM_NAME);
            parameterNames.add(RESPONSE_PARAM_NAME);
        }

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFinderAjaxMethodBody(bodyBuilder, finderMethod, queryHolder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, FIND_ALL_RETURN,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build body method to manage AJAX data request of a datatables which draw
     * the result of a Roo Dynamic finder.
     * 
     * @param bodyBuilder
     * @param finderMethod
     * @param queryHolder
     */
    private void buildFinderAjaxMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            FinderMetadataDetails finderMethod, QueryHolderTokens queryHolder) {

        // BooleanBuilder baseFilterPredicate = new BooleanBuilder();
        bodyBuilder.appendFormalLine(String.format("%s baseSearch = new %s();",
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER),
                helper.getFinalTypeName(QDSL_BOOLEAN_BUILDER)));
        bodyBuilder.appendFormalLine("");

        bodyBuilder
                .appendFormalLine("// Base Search. Using BooleanBuilder, a cascading builder for");
        bodyBuilder.appendFormalLine("// Predicate expressions");

        String path_builder = helper.getFinalTypeName(QDSL_PATH_BUILDER);
        String entity_class = helper.getFinalTypeName(entity);

        // PathBuilder<Pet> entity = new PathBuilder<Pet>(Pet.class, "entity");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> entity = new %s<%s>(%s.class, \"entity\");",
                path_builder, entity_class, path_builder, entity_class,
                entity_class));

        bodyBuilder.appendFormalLine("");

        // Build code to generate QueryDsl predicate to include base filter
        // on Datatables.findByCriteria
        buildFinderAjaxBaseSearchMethodBody(bodyBuilder, finderMethod,
                queryHolder);

        bodyBuilder.appendFormalLine("");

        // Build Datatables.findByCriteria call code
        buildFindByCriteriaCallMethodBody(bodyBuilder, true);

        // Build code for response based on SearchResult
        if (isStantardMode()) {
            // Build code to delegate on Datatables.populateDataSearch
            buildPopulateDataSearchCallMethodBody(bodyBuilder);
        }
        else {
            // Build code to call RenderItem method
            buildPopulateDataSearchCallRenderModeMethodBody(bodyBuilder);
        }
    }

    /**
     * Builds code to create a QueryDsl predicate which implements a Roo dynamic
     * filter condition
     * 
     * @param bodyBuilder
     * @param finderMethod
     * @param queryHolder
     */
    private void buildFinderAjaxBaseSearchMethodBody(
            InvocableMemberBodyBuilder bodyBuilder,
            FinderMetadataDetails finderMethod, QueryHolderTokens queryHolder) {

        FieldToken lastFieldToken = null;

        // flag set when a FieldTocken is located
        boolean isNewField = true;

        // Flag set when a condition is generated
        // but not included yet
        boolean isConditionApplied = false;

        // Helper which can generated the expression
        // required for generate the predicate
        FinderToDslHelper fHelper = new FinderToDslHelper(finderMethod,
                queryHolder, helper);

        // Holds current expression
        StringBuilder expBuilder = null;
        for (final Token token : queryHolder.getTokens()) {
            if (token instanceof ReservedToken) {
                // Current token isn't a field
                final String reservedToken = token.getValue();
                if (lastFieldToken == null) {
                    // Any operator must be preceded by refereed field
                    // XXX Throw a "wrong format" exception?
                    continue;
                }

                // prepare field name
                final String fieldName = lastFieldToken.getField()
                        .getFieldName().getSymbolName();

                // Add PathBuilder to expression
                expBuilder = new StringBuilder("entity");

                // XXX Currently ManyToMany and OneToMany
                // properties are not supported by Roo finders
                if (!lastFieldToken.getField().getFieldType()
                        .isCommonCollectionType()) {

                    // If previous token was a field
                    // include Path getter
                    if (isNewField) {
                        JavaType fieldType = fHelper
                                .getFieldTypeOfFinder(fieldName);
                        Validate.notNull(
                                fieldType,
                                "Field type not found for '%s' field in '%s' finder",
                                fieldName, finderMethod.getFinderName());
                        expBuilder.append(fHelper.getDslGetterFor(fieldName,
                                fieldType));
                        if (reservedToken.equalsIgnoreCase("Like")) {
                            // Add function toLower (and cast to string if it's
                            // needed)
                            expBuilder.append(fHelper
                                    .getToLowerOperatorFor(fieldType));
                        }
                        // mark as field getter is already processed
                        isNewField = false;

                        // mark as condition is no included
                        isConditionApplied = false;
                    }
                    if (reservedToken.equalsIgnoreCase("And")) {
                        if (!isConditionApplied) {
                            // use .eq() operator if condition not applied
                            expBuilder.append(fHelper
                                    .getEqualExpression(fieldName));
                        }
                        // filterCondition.and( {exp});
                        bodyBuilder.appendFormalLine(fHelper
                                .getDslAnd(expBuilder.toString()));
                        expBuilder = null;
                        isConditionApplied = true;
                    }
                    else if (reservedToken.equalsIgnoreCase("Or")) {
                        if (!isConditionApplied) {
                            // use .eq() operator if condition not applied
                            expBuilder.append(fHelper
                                    .getEqualExpression(fieldName));
                        }
                        bodyBuilder.appendFormalLine(fHelper
                                .getDslOr(expBuilder.toString()));
                        expBuilder = null;
                        isConditionApplied = true;
                    }
                    else if (reservedToken.equalsIgnoreCase("Between")) {
                        // use .between(minField,maxField) expression
                        expBuilder.append(fHelper
                                .getBetweenExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("Like")) {
                        // use .like() expression
                        expBuilder.append(fHelper.getLikeExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("IsNotNull")) {
                        // builder.append(" IS NOT NULL ");
                        // use isNotNull() expression
                        expBuilder.append(".isNotNull()");
                    }
                    else if (reservedToken.equalsIgnoreCase("IsNull")) {
                        // builder.append(" IS NULL ");
                        // use isNull() expression
                        expBuilder.append(".isNull()");
                    }
                    else if (reservedToken.equalsIgnoreCase("Not")) {
                        // builder.append(" IS NOT ");
                        // use not() expression
                        expBuilder.append(".not()");
                    }
                    else if (reservedToken.equalsIgnoreCase("NotEquals")) {
                        // builder.append(" != ");
                        expBuilder.append(fHelper
                                .getNotEqualExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("LessThan")) {
                        // builder.append(" < ");
                        expBuilder.append(fHelper
                                .getLessThanExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("LessThanEquals")) {
                        // builder.append(" <= ");
                        expBuilder.append(fHelper
                                .getLessThanEqualsExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("GreaterThan")) {
                        // builder.append(" > ");
                        expBuilder.append(fHelper
                                .getGreaterThanExpression(fieldName));
                    }
                    else if (reservedToken
                            .equalsIgnoreCase("GreaterThanEquals")) {
                        // builder.append(" >= ");
                        expBuilder.append(fHelper
                                .getGreaterThanEqualseExpression(fieldName));
                    }
                    else if (reservedToken.equalsIgnoreCase("Equals")) {
                        // builder.append(" = ");
                        expBuilder
                                .append(fHelper.getEqualExpression(fieldName));
                    }
                }
            }
            else {
                // current token is a field
                lastFieldToken = (FieldToken) token;
                isNewField = true;
            }
        }
        if (isNewField) {
            // New field is located but no operation
            // has found. generate an expression using
            // equals operator
            expBuilder = new StringBuilder("entity");
            if (lastFieldToken != null
                    && !lastFieldToken.getField().getFieldType()
                            .isCommonCollectionType()) {
                String fieldName = lastFieldToken.getField().getFieldName()
                        .getSymbolName();
                JavaType fieldType = fHelper.getFieldTypeOfFinder(fieldName);
                expBuilder
                        .append(fHelper.getDslGetterFor(fieldName, fieldType));
                expBuilder.append(fHelper.getEqualExpression(fieldName));

            }
            isConditionApplied = false;
        }
        if (!isConditionApplied) {
            // There is an expression not included in predicate yet.
            // Include it using "and" join
            if (expBuilder != null) {
                // filterCondition.and( {exp});
                bodyBuilder.appendFormalLine(fHelper.getDslAnd(expBuilder
                        .toString()));
            }
        }
    }

    /**
     * Gets <code>populateDatatablesConfig</code> method <br>
     * This method insert on Model all configuration properties which will need
     * ".tagx" to render final page. <br>
     * This properties are:
     * <ul>
     * <li><em>datatablesHasBatchSupport</em> informs if there is batch entity
     * operations support on controller (used for multi-row delete operation)</li>
     * <li><em>datatablesUseAjax</em> informs datatables data mode (
     * <em>true</em> : AJAX <em>false</em> DOM)</li>
     * <li><em>finderNameParam</em> sets the name of parameter that will contain
     * the {@code finderName} (only for AJAX mode)</li>
     * <li><em>datatablesStandardMode</em> informs render mode (<em>true</em>
     * for standard datatable view; <em>false</em> for single-item-page,
     * one-cell-per-item or render-jspx datatable modes)</li>
     * </ul>
     * 
     * @return
     */
    private MethodMetadata getPopulateDatatablesConfig() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MODEL);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_DATATABLES_CONFIG,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder annotation = new AnnotationMetadataBuilder(
                MODEL_ATTRIBUTE);
        // @ModelAttribute
        annotations.add(annotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesHasBatchSupport\", %s);",
                hasJpaBatchSupport()));
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesUseAjax\",%s);", isAjax()));
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesInlineEditing\",%s);",
                isInlineEditing()));
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesInlineCreating\",%s);",
                isInlineEditing()));

        // TODO
        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesSecurityApplied\",%s);",
                isSecurityApplied()));

        bodyBuilder.appendFormalLine(String.format(
                "uiModel.addAttribute(\"datatablesStandardMode\",%s);",
                isStantardMode()));
        if (isAjax()) {
            bodyBuilder
                    .appendFormalLine("uiModel.addAttribute(\"finderNameParam\",\"ajax_find\");");
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_DATATABLES_CONFIG,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>populateItemForRender</code> method <br>
     * This methods prepares request attributes to render a entity item in
     * non-standard render mode. User can make push-in of this method to
     * customize the parameters received on target .jspx view.
     * 
     * @return
     */
    private MethodMetadata getPopulateItemForRenderMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(HTTP_SERVLET_REQUEST, entity,
                        JavaType.BOOLEAN_PRIMITIVE);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_ITEM_FOR_RENDER,
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
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(new JavaSymbolName("editing"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildPopulateItemForRenderMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_ITEM_FOR_RENDER,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>checkFilterExpressions</code> method <br>
     * This methods checks if the current filter expression is appropieta for
     * the property type.
     * 
     * @return If is correct, return true, if not, return false,
     */
    private MethodMetadata getCheckFilterExpressionsMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(WEB_REQUEST));
        parameterTypes.add(helper.createRequestParam(JavaType.STRING,
                "property", false, null));
        parameterTypes.add(helper.createRequestParam(JavaType.STRING,
                "expression", false, null));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(CHECK_FILTER_EXPRESSIONS,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute(HEADERS, ACCEPT_APPLICATION_JSON);
        methodAnnotation.addStringAttribute(PARAMS_VAR, "checkFilters");
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(new JavaSymbolName("property"));
        parameterNames.add(new JavaSymbolName("expression"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildCheckFilterExpressionMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, CHECK_FILTER_EXPRESSIONS,
                CHECK_FILTERS_RETURN, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build standard body method for <code>populateItemForRender</code> method <br>
     * This methods prepares request attributes to render a entity item in
     * non-standard render mode. User can make push-in of this method to
     * customize the parameters received on target .jspx view.
     * 
     * @param bodyBuilder
     */
    private void buildPopulateItemForRenderMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Model uiModel = new ExtendedModelMap();
        bodyBuilder.appendFormalLine(String.format("%s uiModel = new %s();",
                MODEL, EXTENDED_MODEL_MAP));
        bodyBuilder.appendFormalLine("");

        // request.setAttribute("pet", pet);
        bodyBuilder.appendFormalLine(String.format(
                "%s.setAttribute(\"%s\", %s);",
                REQUEST_PARAM_NAME.getSymbolName(), entityName, entityName

        ));
        // request.setAttribute("itemId",
        // conversion_service.convert(pet.getId(), String.class);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s.setAttribute(\"itemId\", %s.convert(%s.get%s(),String.class));",
                                REQUEST_PARAM_NAME.getSymbolName(),
                                getConversionServiceField().getFieldName()
                                        .getSymbolName(), entityName,
                                StringUtils.capitalize(entityIdentifier
                                        .getFieldName().getSymbolName())));
        bodyBuilder.appendFormalLine("");

        // if (editing) {
        bodyBuilder.appendFormalLine("if (editing) {");
        bodyBuilder.indent();

        // // spring from:input tag uses BindingResult to locate property
        // editors for each bean
        // // property. So, we add a request attribute (required key id
        // BindingResult.MODEL_KEY_PREFIX + object name)
        // // with a correctly initialized bindingResult.
        bodyBuilder
                .appendFormalLine("// spring from:input tag uses BindingResult to locate property editors for each bean");
        bodyBuilder
                .appendFormalLine("// property. So, we add a request attribute (required key id BindingResult.MODEL_KEY_PREFIX + object name)");
        bodyBuilder
                .appendFormalLine("// with a correctly initialized bindingResult.");
        // BeanPropertyBindingResult bindindResult = new
        // BeanPropertyBindingResult(vet, "vet");
        bodyBuilder.appendFormalLine(String.format(
                "%s bindindResult = new %s(%s, \"%s\");",
                helper.getFinalTypeName(BEAN_PROPERTY_BINDING_RESULT),
                helper.getFinalTypeName(BEAN_PROPERTY_BINDING_RESULT),
                entityName, entityName));
        // bindindResult.initConversion(conversionService_datatables);
        bodyBuilder.appendFormalLine(String.format(
                "bindindResult.initConversion(%s);",
                getConversionServiceField().getFieldName().getSymbolName()));
        // request.setAttribute(BindingResult.MODEL_KEY_PREFIX +
        // "vet",bindindResult);
        bodyBuilder.appendFormalLine(String.format(
                "%s.setAttribute(%s.MODEL_KEY_PREFIX + \"%s\",bindindResult);",
                REQUEST_PARAM_NAME.getSymbolName(),
                helper.getFinalTypeName(SpringJavaType.BINDING_RESULT),
                entityName));

        // // Add date time patterns and enums to populate inputs
        bodyBuilder
                .appendFormalLine("// Add date time patterns and enums to populate inputs");
        // populateEditForm(uiModel, vet);
        bodyBuilder.appendFormalLine(String.format(
                "populateEditForm(uiModel, %s);", entityName));

        if (!entityDatePatterns.isEmpty()) {
            // } else {
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine(ELSE);
            bodyBuilder.indent();

            // // Add date time patterns
            bodyBuilder.appendFormalLine("// Add date time patterns");
            // Add date patterns (if any)
            // Delegates on Roo standard populate date patterns method
            // addDateTimeFormatPatterns(uiModel);
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
        }

        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");

        // Load uiModel attributes into request
        bodyBuilder.appendFormalLine("// Load uiModel attributes into request");
        // Map<String,Object> modelMap = uiModel.asMap();
        bodyBuilder.appendFormalLine(String.format(
                "%s modelMap = uiModel.asMap();",
                helper.getFinalTypeName(MAP_STRING_OBJECT)));
        // for (Entry<String,Object> entry : uiModel.asMap().entrySet()){
        bodyBuilder.appendFormalLine(String.format(
                "for (%s key : modelMap.keySet()){",
                helper.getFinalTypeName(JavaType.STRING)));
        bodyBuilder.indent();

        // request.setAttribute(entry.getKey(), entry.getValue());
        bodyBuilder
                .appendFormalLine("request.setAttribute(key, modelMap.get(key));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

    }

    /**
     * Build standard body method for <code>checkFilterExpression</code> method <br>
     * This methods checks if the current filter expression is appropieta for
     * the property type.
     * 
     * @param bodyBuilder
     */
    private void buildCheckFilterExpressionMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // HttpHeaders headers = new HttpHeaders();
        // headers.add("Content-Type", "application/json; charset=utf-8");

        bodyBuilder.appendFormalLine(String.format("%s headers = new %s();",
                helper.getFinalTypeName(SpringJavaType.HTTP_HEADERS),
                helper.getFinalTypeName(SpringJavaType.HTTP_HEADERS)));
        bodyBuilder
                .appendFormalLine("headers.add(\"Content-Type\", \"application/json; charset=utf-8\");");

        // if(beanWrapper == null){
        bodyBuilder.appendFormalLine("if(beanWrapper == null){");
        bodyBuilder.indent();

        // beanWrapper = new BeanWrapperImpl(XXX.class);
        bodyBuilder.appendFormalLine(String.format(
                "beanWrapper = new %s(%s.class);", helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.beans.BeanWrapperImpl")),
                helper.getFinalTypeName(entity)));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Class type = beanWrapper.getPropertyType(property);
        bodyBuilder
                .appendFormalLine("Class type = beanWrapper.getPropertyType(property);");

        // boolean response = DatatablesUtils.checkFilterExpressions(type,
        // expression, messageSource_dtt);
        bodyBuilder
                .appendFormalLine("boolean response = DatatablesUtils.checkFilterExpressions(type,expression, messageSource_dtt);");

        // return new ResponseEntity<String>(
        // String.format("{ \"response\": %s, \"property\": \"%s\"}",
        // response, property), headers, HttpStatus.OK);

        String returnStatement = "return new ResponseEntity<String>(String.format(\"{ \\\"response\\\": %s, \\\"property\\\": \\\"%s\\\"}\",response, property), headers, ";

        bodyBuilder.appendFormalLine(returnStatement.concat(String.format(
                "%s.OK);", SpringJavaType.HTTP_STATUS)));
    }

    /**
     * Returns <code>geti18nText</code> method <br>
     * This method returns column type after AJAX request
     * 
     * @return
     */
    private MethodMetadata geti18nTextRequestMethod() {
        // Define method parameter types

        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(
                new AnnotatedJavaType(MODEL), AnnotatedJavaType
                        .convertFromJavaType(HTTP_SERVLET_REQUEST), helper
                        .createRequestParam(JavaType.STRING, "_locale_", false,
                                null));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_I18N_TEXT,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        // @RequestMapping(headers = "Accept=application/json"...
        methodAnnotation.addStringAttribute(HEADERS, ACCEPT_APPLICATION_JSON);

        // @RequestMapping(params = "geti18nText"...
        methodAnnotation.addStringAttribute(PARAMS_VAR, "geti18nText");

        // @ResponseBody
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);
        parameterNames.add(new JavaSymbolName(
                DatatablesConstants.REQUEST_PARAMETER_NAME));
        parameterNames.add(new JavaSymbolName("locale"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        buildGeti18nTextMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_I18N_TEXT, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build method body for <code>Geti18nText</code> method
     * 
     * @param bodyBuilder
     */
    private void buildGeti18nTextMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // TODO Auto-generated method stub
        bodyBuilder.appendFormalLine("// Getting current locale");
        bodyBuilder.appendFormalLine(String.format(
                "%s defaultLocale = new Locale(locale);",
                helper.getFinalTypeName(new JavaType("java.util.Locale"))));

        bodyBuilder.appendFormalLine("// Building JSON response");
        bodyBuilder
                .appendFormalLine("StringBuilder json = new StringBuilder();");
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"all_isnull\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.all.isnull\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"all_notnull\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.all.notnull\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"boolean_false\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.boolean.false\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"boolean_true\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.boolean.true\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"date_between\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.date.between\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"date_date\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.date.date\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"date_day\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.date.day\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"date_month\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.date.month\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"date_year\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.date.year\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"number_between\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.number.between\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"string_contains\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.string.contains\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"string_ends\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.string.ends\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"string_isempty\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.string.isempty\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"string_isnotempty\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.string.isnotempty\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"string_starts\\\": \\\"\" + messageSource_dtt.getMessage(\"global.filters.operations.string.starts\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"button_find\\\": \\\"\" + messageSource_dtt.getMessage(\"button_find\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_all_isnull\\\": \\\"\" + messageSource_dtt.getMessage(\"help.all.isnull\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_all_notnull\\\": \\\"\" + messageSource_dtt.getMessage(\"help.all.notnull\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_boolean_false\\\": \\\"\" + messageSource_dtt.getMessage(\"help.boolean.false\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_boolean_true\\\": \\\"\" + messageSource_dtt.getMessage(\"help.boolean.true\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_date_between\\\": \\\"\" + messageSource_dtt.getMessage(\"help.date.between\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_date_date\\\": \\\"\" + messageSource_dtt.getMessage(\"help.date.date\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_date_day\\\": \\\"\" + messageSource_dtt.getMessage(\"help.date.day\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_date_month\\\": \\\"\" + messageSource_dtt.getMessage(\"help.date.month\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_date_year\\\": \\\"\" + messageSource_dtt.getMessage(\"help.date.year\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_between\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.between\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_eq\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.eq\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_neq\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.neq\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_gt\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.gt\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_lt\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.lt\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_goe\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.goe\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_number_loe\\\": \\\"\" + messageSource_dtt.getMessage(\"help.number.loe\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_string_contains\\\": \\\"\" + messageSource_dtt.getMessage(\"help.string.contains\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_string_ends\\\": \\\"\" + messageSource_dtt.getMessage(\"help.string.ends\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_string_isempty\\\": \\\"\" + messageSource_dtt.getMessage(\"help.string.isempty\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_string_isnotempty\\\": \\\"\" + messageSource_dtt.getMessage(\"help.string.isnotempty\", null, defaultLocale) + \"\\\"\");");
        bodyBuilder.appendFormalLine(JSON_APPEND);
        bodyBuilder
                .appendFormalLine("json.append(\"\\\"help_string_starts\\\": \\\"\" + messageSource_dtt.getMessage(\"help.string.starts\", null, defaultLocale) + \"\\\"\");");

        bodyBuilder.appendFormalLine("json.append(\"}\");");

        bodyBuilder.appendFormalLine("// return JSON with locale strings");
        bodyBuilder.appendFormalLine("return json.toString();");
    }

    /**
     * Returns <code>getColumnType</code> method <br>
     * This method is to return column type after AJAX request
     * 
     * @return
     */
    private MethodMetadata getColumnTypeRequestMethod() {
        // Define method parameter types

        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(
                new AnnotatedJavaType(MODEL), AnnotatedJavaType
                        .convertFromJavaType(HTTP_SERVLET_REQUEST), helper
                        .createRequestParam(JavaType.STRING, "_columnName_",
                                false, null));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_COLUMN_TYPE,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        // @RequestMapping(headers = "Accept=application/json"...
        methodAnnotation.addStringAttribute(HEADERS, ACCEPT_APPLICATION_JSON);

        // @RequestMapping(params = "getColumnType"...
        methodAnnotation.addStringAttribute(PARAMS_VAR, "getColumnType");

        // @ResponseBody
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);
        parameterNames.add(new JavaSymbolName(
                DatatablesConstants.REQUEST_PARAMETER_NAME));
        parameterNames.add(new JavaSymbolName("columnName"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        buildGetColumnTypeMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_COLUMN_TYPE, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build method body for <code>GetColumnType</code> method
     * 
     * @param bodyBuilder
     */
    private void buildGetColumnTypeMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        JavaSymbolName entity = new JavaSymbolName(entityName);

        // Getting all declared fields
        bodyBuilder.appendFormalLine("// Getting all declared fields");

        // boolean fieldExists = false;
        bodyBuilder.appendFormalLine("boolean fieldExists = false;");

        // Field attr = null;
        bodyBuilder.appendFormalLine(String.format("%s attr = null;", helper
                .getFinalTypeName(new JavaType("java.lang.reflect.Field"))));

        // for(Field field : Owner.class.getDeclaredFields()){
        bodyBuilder.appendFormalLine(String.format(
                "for(Field field : %s.class.getDeclaredFields()){",
                entity.getSymbolNameCapitalisedFirstLetter()));
        bodyBuilder.indent();

        // if(field.getName().equals(columnName)){
        bodyBuilder.appendFormalLine("if(field.getName().equals(columnName)){");
        bodyBuilder.indent();

        // attr = field;
        bodyBuilder.appendFormalLine("attr = field;");

        // fieldExists = true;
        bodyBuilder.appendFormalLine("fieldExists = true;");

        // break;
        bodyBuilder.appendFormalLine("break;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // // If current field not exists on entity, find on superclass
        bodyBuilder
                .appendFormalLine("// If current field not exists on entity, find on superclass");

        // if(!fieldExists){
        bodyBuilder.appendFormalLine("if(!fieldExists){");
        bodyBuilder.indent();

        // if(Owner.class.getSuperclass() != null){
        bodyBuilder.appendFormalLine(String.format(
                "if(%s.class.getSuperclass() != null){",
                entity.getSymbolNameCapitalisedFirstLetter()));
        bodyBuilder.indent();

        // for(Field field : Owner.class.getSuperclass().getDeclaredFields()){
        bodyBuilder
                .appendFormalLine(String
                        .format("for(Field field : %s.class.getSuperclass().getDeclaredFields()){",
                                entity.getSymbolNameCapitalisedFirstLetter()));
        bodyBuilder.indent();

        // if(field.getName().equals(columnName)){
        bodyBuilder.appendFormalLine("if(field.getName().equals(columnName)){");
        bodyBuilder.indent();

        // attr = field;
        bodyBuilder.appendFormalLine("attr = field;");

        // fieldExists = true;
        bodyBuilder.appendFormalLine("fieldExists = true;");

        // break;
        bodyBuilder.appendFormalLine("break;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if(fieldExists){
        bodyBuilder.appendFormalLine("if(fieldExists){");
        bodyBuilder.indent();

        bodyBuilder.appendFormalLine("// Getting field type");
        bodyBuilder.appendFormalLine("Object fieldType = null;");
        bodyBuilder.appendFormalLine("if (attr != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("fieldType = attr.getType();");
        bodyBuilder.appendFormalLine("String type = fieldType.toString();");
        bodyBuilder.appendFormalLine("// Returning value based on type");
        bodyBuilder.appendFormalLine("if (\"String\".equals(type)){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return \"{\\\"columnType\\\": \\\"string\\\"}\";");
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if (\"float\".equals(type) || type.contains(\"Float\")){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(COLTYPE_NUM);
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if (\"short\".equals(type) || type.contains(\"Short\")){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(COLTYPE_NUM);
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if (\"long\".equals(type) || type.contains(\"Long\")){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(COLTYPE_NUM);
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if (\"double\".equals(type) || type.contains(\"Double\")){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(COLTYPE_NUM);
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if (\"int\".equals(type) || type.contains(\"Integer\")){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(COLTYPE_NUM);
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else if (\"Date\".equals(type)){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return \"{\\\"columnType\\\": \\\"date\\\"}\";");
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} else if (\"boolean\".equals(type) || type.contains(\"Boolean\")){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return \"{\\\"columnType\\\": \\\"boolean\\\"}\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(ELSE);
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("// Returning by default");
        bodyBuilder
                .appendFormalLine("return \"{\\\"columnType\\\": \\\"undefined\\\"}\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("// Returning by default");
        bodyBuilder
                .appendFormalLine("return \"{\\\"columnType\\\": \\\"undefined\\\"}\";");
    }

    /**
     * Returns <code>listDatatables</code> method <br>
     * This method is default list request handler for datatables controllers
     * 
     * @return
     */
    private MethodMetadata getListDatatablesRequestMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MODEL, HTTP_SERVLET_REQUEST);

        if (!isAjax()) {
            // In DOM mode we include Item in parameters to use
            // spring's binder to get baseFilter values
            parameterTypes.add(new AnnotatedJavaType(entity,
                    new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        }

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(LIST_DATATABLES,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        // @RequestMapping(method = RequestMethod.GET...
        methodAnnotation.addEnumAttribute(METHOD_VAR, REQUEST_METHOD, "GET");

        // @RequestMapping(... produces = "text/html")
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_PROD_NAME,
                DatatablesConstants.RQST_MAP_ANN_VAL_HTML);

        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);
        parameterNames.add(new JavaSymbolName(
                DatatablesConstants.REQUEST_PARAMETER_NAME));

        if (!isAjax()) {
            // In DOM mode we include Item in parameters to use
            // spring's binder to get baseFilter values
            parameterNames.add(new JavaSymbolName(entityName));
        }

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        if (isAjax()) {
            buildListDatatablesRequesMethodAjaxBody(bodyBuilder);
        }
        else {
            buildListDatatablesRequesMethodDomBody(bodyBuilder);
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LIST_DATATABLES, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Build method body for <code>listDatatables</code> method for DOM mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequesMethodDomBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // // Get parentId parameter for details
        bodyBuilder.appendFormalLine("// Get parentId parameter for details");

        // if (request.getParameterMap().containsKey("_dt_parentId")){
        bodyBuilder
                .appendFormalLine("if (request.getParameterMap().containsKey(\"_dt_parentId\")){");
        bodyBuilder.indent();
        // uiModel.addAttribute("parentId",request.getParameter("_dt_parentId"));
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"parentId\",request.getParameter(\"_dt_parentId\"));");
        // }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        String listVarName = StringUtils.uncapitalize(entityPlural);

        bodyBuilder
                .appendFormalLine("// Get data (filtered by received parameters) and put it on pageContext");
        // List<Visit> visits =
        // findByParameters(visit,request.getParameterNames());
        bodyBuilder
                .appendFormalLine(String
                        .format("@SuppressWarnings(\"unchecked\") %s %s = %s(%s, request != null ? request.getParameterNames() : null);",
                                helper.getFinalTypeName(entityListType),
                                listVarName,
                                findByParametersMethodName.getSymbolName(),
                                entityName));

        // uiModel.addAttribute("pets",pets);
        bodyBuilder.appendFormalLine(String.format(
                "%s.addAttribute(\"%s\",%s);", UI_MODEL.getSymbolName(),
                entityPlural.toLowerCase(), listVarName));

        buildListDatatablesRequestMethodDetailBody(bodyBuilder);

        // return "pets/list";
        bodyBuilder.appendFormalLine(String.format("return \"%s/list\";",
                webScaffoldAnnotationValues.getPath()));

    }

    /**
     * Build method body for <code>listDatatables</code> method for AJAX mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequesMethodAjaxBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // [Code generated] Map<String, String> params =
        // populateParameterMap(request);
        bodyBuilder.appendFormalLine(String.format("%s params = %s(request);",
                helper.getFinalTypeName(MAP_STRING_STRING),
                POPULATE_PARAMETERS_MAP.getSymbolName()));

        // [Code generated] // Get parentId information for details render
        bodyBuilder
                .appendFormalLine("// Get parentId information for details render");

        // [Code generated] String parentId = params.remove("_dt_parentId");
        bodyBuilder
                .appendFormalLine("String parentId = params.remove(\"_dt_parentId\");");

        // [Code generated] if (!params.isEmpty()) {
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(parentId)) {",
                helper.getFinalTypeName(STRING_UTILS)));
        bodyBuilder.indent();

        // [Code generated] uiModel.addAttribute("parentId", parentId);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"parentId\", parentId);");

        // [Code generated] }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // [Code generated] String rowOnTopIds =
        // params.remove("dtt_row_on_top_ids");
        bodyBuilder
                .appendFormalLine("String rowOnTopIds = params.remove(\"dtt_row_on_top_ids\");");

        // [Code generated] if (StringUtils.isNotBlank(rowOnTopIds)) {
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(rowOnTopIds)) {",
                helper.getFinalTypeName(STRING_UTILS)));
        bodyBuilder.indent();

        // [Code generated] uiModel.addAttribute("dtt_row_on_top_ids",
        // rowOnTopIds);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"dtt_row_on_top_ids\", rowOnTopIds);");
        bodyBuilder.indentRemove();

        // [Code generated] }
        bodyBuilder.appendFormalLine("}");

        // [Code generated] String tableHashId =
        // params.remove("dtt_parent_table_id_hash");
        bodyBuilder
                .appendFormalLine("String tableHashId = params.remove(\"dtt_parent_table_id_hash\");");

        // [Code generated] if (StringUtils.isNotBlank(tableHashId)) {
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(tableHashId)) {",
                helper.getFinalTypeName(STRING_UTILS)));
        bodyBuilder.indent();

        // [Code generated] uiModel.addAttribute("dtt_parent_table_id_hash",
        // tableHashId);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"dtt_parent_table_id_hash\", tableHashId);");
        bodyBuilder.indentRemove();

        // [Code generated] }
        bodyBuilder.appendFormalLine("}");

        // [Code generated] if (!params.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!params.isEmpty()) {");
        bodyBuilder.indent();

        // [Code generated] uiModel.addAttribute("baseFilter", params);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"baseFilter\", params);");

        // [Code generated] }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        buildListDatatablesRequestMethodDetailBody(bodyBuilder);

        // [Code generated] return "pets/list";
        bodyBuilder.appendFormalLine(String.format("return \"%s/list\";",
                webScaffoldAnnotationValues.getPath()));
    }

    /**
     * Build method body for <code>listDatatables</code> method for AJAX mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequestMethodDetailBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        String[] fieldNames = getDetailFields();
        if (fieldNames.length > 0) {

            bodyBuilder
                    .appendFormalLine("// Add attribute available into view with information about each detail datatables ");
            bodyBuilder.appendFormalLine("Map<String, String> details;");
            bodyBuilder
                    .appendFormalLine("List<Map<String, String>> detailsInfo = new ArrayList<Map<String, String>>("
                            .concat(String.valueOf(fieldNames.length)).concat(
                                    ");"));

            List<FieldMetadata> entityFields = entityMemberDetails.getFields();

            // For each fields
            for (int i = 0; i < fieldNames.length; i++) {

                String fieldName = fieldNames[i];
                boolean found = false;

                for (FieldMetadata entityField : entityFields) {

                    if (entityField.getFieldName().getSymbolName()
                            .equals(fieldName)) {
                        found = true;

                        buildFieldDetailInfoMethodBody(bodyBuilder, entityField);
                    }
                }
                if (!found) {
                    throw new IllegalStateException(
                            String.format(
                                    "%s: Can't create datatables detail information: property %s.%s not found",
                                    getAspectName().getFullyQualifiedTypeName(),
                                    entity.getFullyQualifiedTypeName(),
                                    fieldName));

                }

            }
            bodyBuilder
                    .appendFormalLine("uiModel.addAttribute(\"detailsInfo\", detailsInfo);");
        }
    }

    /**
     * Build method code to load detailsInfo with a property information
     * 
     * @param bodyBuilder
     * @param entityField
     */
    private void buildFieldDetailInfoMethodBody(
            InvocableMemberBodyBuilder bodyBuilder, FieldMetadata entityField) {
        AnnotationMetadata entityFieldOneToManyAnnotation = entityField
                .getAnnotation(new JavaType("javax.persistence.OneToMany"));

        // Check property annotation (@OneToMany)
        if (entityFieldOneToManyAnnotation == null) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Can't create datatables detail information: property %s.%s is not OneToMany",
                            getAspectName().getFullyQualifiedTypeName(), entity
                                    .getFullyQualifiedTypeName(), entityField
                                    .getFieldName().getSymbolName()));

        }
        if (entityFieldOneToManyAnnotation.getAttribute("mappedBy") == null) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Can't create datatables detail information: property %s.%s has no mappedBy information",
                            getAspectName().getFullyQualifiedTypeName(), entity
                                    .getFullyQualifiedTypeName(), entityField
                                    .getFieldName().getSymbolName()));

        }

        String entityFieldOneToManyAnnotationMappedBy = null;

        // Check @OneToMay mappedBy value
        try {
            entityFieldOneToManyAnnotationMappedBy = entityFieldOneToManyAnnotation
                    .getAttribute("mappedBy").getValue().toString();
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Can't create datatables detail information: property %s.%s has invalid mappedBy information",
                            getAspectName().getFullyQualifiedTypeName(), entity
                                    .getFullyQualifiedTypeName(), entityField
                                    .getFieldName().getSymbolName()));

        }

        // Get type of list (entity): if not a list, do
        // nothing
        JavaType entityFieldBaseType = entityField.getFieldType().getBaseType();

        // Get target entity based on property type: ej Set<Pet> --> Pet
        if (entityFieldBaseType == null) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Can't create datatables detail information: property %s.%s can't identify target entity",
                            getAspectName().getFullyQualifiedTypeName(), entity
                                    .getFullyQualifiedTypeName(), entityField
                                    .getFieldName().getSymbolName()));
        }

        // Locate target entity metadata
        JavaTypeMetadataDetails javaTypeMetadataDetails = webMetadataService
                .getJavaTypeMetadataDetails(entityFieldBaseType,
                        entityMemberDetails,
                        entityIdentifier.getDeclaredByMetadataId());

        // Check if metadata found
        if (javaTypeMetadataDetails == null) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Can't create datatables detail information: property %s.%s can't get target entity metadata",
                            getAspectName().getFullyQualifiedTypeName(), entity
                                    .getFullyQualifiedTypeName(), entityField
                                    .getFieldName().getSymbolName()));
        }

        // Generate datailInfo code for current property
        bodyBuilder
                .appendFormalLine("details = new HashMap<String, String>();");

        bodyBuilder
                .appendFormalLine("// Base path for detail datatables entity (to get detail datatables fragment URL)");
        bodyBuilder.appendFormalLine("details.put(\"path\", \"".concat(
                javaTypeMetadataDetails.getControllerPath()).concat(ENDING));
        bodyBuilder.appendFormalLine("details.put(\"property\", \"".concat(
                entityField.getFieldName().getSymbolName()).concat(ENDING));
        bodyBuilder
                .appendFormalLine("// Property name in detail entity with the relation to master entity");
        bodyBuilder.appendFormalLine("details.put(\"mappedBy\", \"".concat(
                entityFieldOneToManyAnnotationMappedBy).concat(ENDING));

        bodyBuilder.appendFormalLine("detailsInfo.add(details);");
    }

    /**
     * Redefines {@code list} Roo webScaffod method to delegate on
     * {@link #LIST_DATATABLES}
     * 
     * @return
     */
    private MethodMetadata getListRooRequestMethod() {

        // public String OwnerController.list(
        // @RequestParam(value = "page", required = false) Integer page,
        // @RequestParam(value = "size", required = false) Integer size,

        /** Added on 1.2.6.RELEASE Roo Version */
        // @RequestParam(value = "sortFieldName", required = false) String
        // sortFieldName,
        // @RequestParam(value = "sortOrder", required = false) String sortOrder
        /** Added on 1.2.6.RELEASE Roo Version */

        // Model uiModel) {

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(
                helper.createRequestParam(JavaType.INT_OBJECT, PAGE_VAR, false,
                        null), helper.createRequestParam(JavaType.INT_OBJECT,
                        SIZE_VAR, false, null), helper.createRequestParam(
                        JavaType.STRING, SORT_FIELD_NAME, false, null), helper
                        .createRequestParam(JavaType.STRING, SORT_ORDER, false,
                                null), new AnnotatedJavaType(MODEL));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(LIST_ROO, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping(produces = "text/html")
        annotations.add(helper.getRequestMappingAnnotation(null, null, null,
                DatatablesConstants.RQST_MAP_ANN_VAL_HTML, null, null));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(PAGE_VAR), new JavaSymbolName(SIZE_VAR),
                new JavaSymbolName(SORT_FIELD_NAME), new JavaSymbolName(
                        SORT_ORDER), UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder
                .appendFormalLine("// overrides the standard Roo list method and");
        bodyBuilder.appendFormalLine("// delegates on datatables list method");
        // return listDatatables(uiModel);
        if (isAjax()) {
            bodyBuilder.appendFormalLine(String.format(
                    "return %s(uiModel, null);",
                    LIST_DATATABLES.getSymbolName()));
        }
        else {
            bodyBuilder.appendFormalLine(String.format(
                    "return %s(uiModel, null, null);",
                    LIST_DATATABLES.getSymbolName()));
        }

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LIST_ROO, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>findAllMethod</code> method <br>
     * This method handles datatables AJAX request for data which are no related
     * to a Roo Dynamic finder.
     * 
     * @return
     */
    private MethodMetadata getFindAllMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));

        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        parameterTypes.addAll(AnnotatedJavaType
                .convertFromJavaTypes(HTTP_SERVLET_REQUEST));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(findAllMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute(HEADERS, ACCEPT_APPLICATION_JSON);
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_VAL_NAME, "/datatables/ajax");
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_PROD_NAME, APPLICATION_JSON);
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(StringUtils
                .uncapitalize(entityName)));
        parameterNames.add(REQUEST_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFindAllDataMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, findAllMethodName, FIND_ALL_RETURN,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>findAll</code> method for render-a-view visualization mode. <br>
     * This method handles datatables AJAX request for data which are no related
     * to a Roo Dynamic finder.
     * 
     * @return
     */
    private MethodMetadata getFindAllMethodRenderMode() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));
        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_REQUEST));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_RESPONSE));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(findAllMethodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute(HEADERS, ACCEPT_APPLICATION_JSON);
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_VAL_NAME, "/datatables/ajax");
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_PROD_NAME, APPLICATION_JSON);
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildFindAllMethodBodyRenderMode(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, findAllMethodName, FIND_ALL_RETURN,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build method body for <code>findAll</code> method for render-a-view
     * visualization mode
     * 
     * @param bodyBuilder
     */
    private void buildFindAllMethodBodyRenderMode(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Build call to FindByCriteria
        buildFindByCriteriaCallMethodBody(bodyBuilder, false);

        buildPopulateDataSearchCallRenderModeMethodBody(bodyBuilder);
    }

    /**
     * Build method-code to call Datatables.populateDataSearch method.
     * 
     * @param bodyBuilder
     */
    private void buildPopulateDataSearchCallRenderModeMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                "%s rows = %s(searchResult, %s, %s);",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                renderItemsMethodName.getSymbolName(),
                REQUEST_PARAM_NAME.getSymbolName(),
                RESPONSE_PARAM_NAME.getSymbolName()));

        /*
         * DataSet<Map<String, String>> dataSet = new DataSet<Map<String,
         * String>>(rows, totalRecords, recordsFound);
         */
        bodyBuilder.appendFormalLine(String.format(
                "%s dataSet = new %s(rows, totalRecords, recordsFound); ",
                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING),
                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING)));

        // return DatatablesResponse.build(dataSet,criterias);
        bodyBuilder.appendFormalLine(String.format(
                "return %s.build(dataSet,%s);",
                helper.getFinalTypeName(DATATABLES_RESPONSE),
                CRITERIA_PARAM_NAME.getSymbolName()));
    }

    /**
     * Build method-code to call Datatables.findByCriteria method. <br>
     * Generated code will depend on entity has JPAQuery metadata and if there
     * is a base search.
     * 
     * @param bodyBuilder
     * @param baseSearch
     * @return
     */
    private String buildFindByCriteriaCallMethodBody(
            InvocableMemberBodyBuilder bodyBuilder, boolean baseSearch) {
        final String entityTypeName = helper.getFinalTypeName(entity);
        JavaType serachResult = new JavaType(
                SEARCH_RESULTS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(entity));

        if (jpaQueryMetadata != null) {
            String filterByInfo = String.format("%s.%s()", entityTypeName,
                    jpaQueryMetadata.getFilterByMethodName().getSymbolName());
            String orderByInfo = String.format("%s.%s()", entityTypeName,
                    jpaQueryMetadata.getOrderByMethodName().getSymbolName());

            // SearchResults<Pet> searchResult =
            // DatatablesUtils.findByCriteria(Pet.class,
            // Pet.getFilterByAssociations(), Pet.getOrderByAssociations(),
            // Pet.entityManager(), criterias);

            if (baseSearch) {
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(entity, %s.%s(), %s, baseSearch);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));

            }
            else {

                bodyBuilder.appendFormalLine(PARAMETERS_AS_BASE_SEARCH);
                // Map<String, Object> baseSearchValuesMap = getPropertyMap(pet,
                // request);
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s baseSearchValuesMap = getPropertyMap(%s, request);",
                                        helper.getFinalTypeName(MAP_STRING_OBJECT),
                                        entityName));

                // setVisitFilters(baseSearchValuesMap);
                bodyBuilder
                        .appendFormalLine("setDatatablesBaseFilter(baseSearchValuesMap);");

                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(%s.class, %s, %s, %s.%s(), %s, baseSearchValuesMap, conversionService_dtt, messageSource_dtt);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName, filterByInfo,
                                        orderByInfo, entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));
            }
        }
        else {

            // SearchResults<Pet> searchResult =
            // DatatablesUtils.findByCriteria(Pet.class,
            // Pet.getFilterByAssociations(), Pet.getOrderByAssociations(),
            // Pet.entityManager(), criterias);
            if (baseSearch) {
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(entity, %s.%s(), %s, baseSearch);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));
            }
            else {

                bodyBuilder.appendFormalLine(PARAMETERS_AS_BASE_SEARCH);
                // Map<String, Object> baseSearchValuesMap = getPropertyMap(pet,
                // request);
                bodyBuilder
                        .appendFormalLine(String
                                .format("%s baseSearchValuesMap = getPropertyMap(%s, request);",
                                        helper.getFinalTypeName(MAP_STRING_OBJECT),
                                        entityName));

                // setVisitFilters(baseSearchValuesMap);
                bodyBuilder
                        .appendFormalLine("setDatatablesBaseFilter(baseSearchValuesMap);");

                bodyBuilder
                        .appendFormalLine(String
                                .format("%s searchResult = %s.findByCriteria(%s.class, %s.%s(), %s, baseSearchValuesMap, conversionService_dtt, messageSource_dtt);",
                                        helper.getFinalTypeName(serachResult),
                                        helper.getFinalTypeName(DATATABLES_UTILS),
                                        entityTypeName, entityTypeName,
                                        entityEntityManagerMethod
                                                .getSymbolName(),
                                        CRITERIA_PARAM_NAME.getSymbolName()));
            }

        }

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Get datatables required counts");
        // long totalRecords = searchResult.getTotalCount();
        bodyBuilder
                .appendFormalLine("long totalRecords = searchResult.getTotalCount();");
        // long recordsFound = findResult.getResultsCount();
        bodyBuilder
                .appendFormalLine("long recordsFound = searchResult.getResultsCount();");
        return entityTypeName;
    }

    /**
     * Build method body for <code>findAll</code> method
     * 
     * @param bodyBuilder
     */
    private void buildFindAllDataMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Build call to FindByCriteria
        buildFindByCriteriaCallMethodBody(bodyBuilder, false);

        buildPopulateDataSearchCallMethodBody(bodyBuilder);
    }

    /**
     * Build method-code to perform a call to Datatables.populateDateSet method
     * 
     * @param bodyBuilder
     */
    private void buildPopulateDataSearchCallMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Entity pk field name");

        // String pkFieldName = "id";
        bodyBuilder.appendFormalLine(String.format(
                "String pkFieldName = \"%s\";", entityIdentifier.getFieldName()
                        .getSymbolName()));

        String dateFormatVarName = getCodeToAddDateTimeFormatPatterns(bodyBuilder);

        bodyBuilder.appendFormalLine("");
        /*
         * DataSet<Map<String, String>> dataSet =
         * DatatablesUtils.populateDataSet(searchResult.getResult(),
         * pkFieldName, totalRecords, recordsFound, criterias.getColumnDefs(),
         * datePattern, conversionService_datatables);
         */

        bodyBuilder
                .appendFormalLine(String
                        .format("%s dataSet = %s.populateDataSet(searchResult.getResults(), pkFieldName, totalRecords, recordsFound, criterias.getColumnDefs(), %s, %s); ",
                                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING),
                                helper.getFinalTypeName(DATATABLES_UTILS),
                                dateFormatVarName, getConversionServiceField()
                                        .getFieldName().getSymbolName()));

        // return DatatablesResponse.build(dataSet,criterias);
        bodyBuilder.appendFormalLine(String.format(
                "return %s.build(dataSet,%s);",
                helper.getFinalTypeName(DATATABLES_RESPONSE),
                CRITERIA_PARAM_NAME.getSymbolName()));
    }

    /**
     * Create metadata for auto-wired convertionService field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getConversionServiceField() {
        if (conversionService == null) {
            JavaSymbolName curName = new JavaSymbolName("conversionService_dtt");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null && !isConversionServiceField(currentField)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = new JavaSymbolName(
                        "conversionService_dt");
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
     * Create metadata for auto-wired messageSource field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getMessageSourceField() {
        if (messageSource == null) {
            JavaSymbolName curName = new JavaSymbolName("messageSource_dtt");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null && !isMessageSourceField(currentField)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = new JavaSymbolName("messageSource_dt");
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
                messageSource = currentField;
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
                        MESSAGE_SOURCE); // Field type
                messageSource = fieldBuilder.build(); // Build and return a
                                                      // FieldMetadata
                // instance
            }
        }
        return messageSource;
    }

    /**
     * Create metadata for beanWrapper field.
     * 
     * @return a FieldMetadata object
     */
    public FieldMetadata getBeanWrapperField() {
        if (beanWrapper == null) {
            JavaSymbolName curName = new JavaSymbolName("beanWrapper");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null && !isBeanWrapperField(currentField)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = new JavaSymbolName("beanWrapper_dtt");
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
                beanWrapper = currentField;
            }
            else {
                // create field
                List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                        1);
                // Using the FieldMetadataBuilder to create the field
                // definition.
                final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                        getId(), Modifier.PUBLIC, annotations, curName, // Field
                        BEAN_WRAPPER); // Field type
                messageSource = fieldBuilder.build(); // Build and return a
                                                      // FieldMetadata
                // instance
            }
        }
        return messageSource;
    }

    private boolean isConversionServiceField(FieldMetadata field) {
        return field != null && field.getAnnotation(AUTOWIRED) != null
                && field.getFieldType().equals(CONVERSION_SERVICE);
    }

    private boolean isMessageSourceField(FieldMetadata field) {
        return field != null && field.getAnnotation(AUTOWIRED) != null
                && field.getFieldType().equals(MESSAGE_SOURCE);
    }

    private boolean isBeanWrapperField(FieldMetadata field) {
        return field != null && field.getFieldType().equals(BEAN_WRAPPER);
    }

    /**
     * Add the methods needed to support export via AJAX.
     */
    private void addAjaxExportMethods() {
        builder.addMethod(getExportCsvMethod());
        builder.addMethod(getExportPdfMethod());
        builder.addMethod(getExportXlsMethod());
        builder.addMethod(getExportXlsxMethod());
        builder.addMethod(getExportXmlMethod());
        builder.addMethod(getExportMethod());
        builder.addMethod(getRetrieveDataMethod());
    }

    /**
     * Returns <code>exportCsv</code> method for render-a-view visualization
     * mode.
     * <p />
     * This method handles datatables AJAX request for export data to a CSV
     * file.
     * 
     * @return
     */
    private MethodMetadata getExportCsvMethod() {
        String exportType = "csv";
        JavaType exportTypeJavaType = DATATABLES_CSV_EXPORT;
        return getExportFormatMethod(exportType, exportTypeJavaType);
    }

    /**
     * Returns <code>exportPdf</code> method for render-a-view visualization
     * mode.
     * <p />
     * This method handles datatables AJAX request for export data to a PDF
     * file.
     * 
     * @return
     */
    private MethodMetadata getExportPdfMethod() {
        String exportType = "pdf";
        JavaType exportTypeJavaType = DATATABLES_PDF_EXPORT;
        return getExportFormatMethod(exportType, exportTypeJavaType);
    }

    /**
     * Returns <code>exportXls</code> method for render-a-view visualization
     * mode.
     * <p />
     * This method handles datatables AJAX request for export data to a XLS
     * file.
     * 
     * @return
     */
    private MethodMetadata getExportXlsMethod() {
        String exportType = "xls";
        JavaType exportTypeJavaType = DATATABLES_XLS_EXPORT;
        return getExportFormatMethod(exportType, exportTypeJavaType);
    }

    /**
     * Returns <code>exportXlsx</code> method for render-a-view visualization
     * mode.
     * <p />
     * This method handles datatables AJAX request for export data to a XLSX
     * file.
     * 
     * @return
     */
    private MethodMetadata getExportXlsxMethod() {
        String exportType = "xlsx";
        JavaType exportTypeJavaType = DATATABLES_XLSX_EXPORT;
        return getExportFormatMethod(exportType, exportTypeJavaType);
    }

    /**
     * Returns <code>exportXml</code> method for render-a-view visualization
     * mode.
     * <p />
     * This method handles datatables AJAX request for export data to a XML
     * file.
     * 
     * @return
     */
    private MethodMetadata getExportXmlMethod() {
        String exportType = "xml";
        JavaType exportTypeJavaType = DATATABLES_XML_EXPORT;
        return getExportFormatMethod(exportType, exportTypeJavaType);
    }

    /**
     * Returns export method for a specific format for a render-a-view
     * visualization mode.
     * <p />
     * This method handles datatables AJAX request for export data to a format
     * specified in the @{code exportType} parameter.
     * 
     * @param exportType the export type: csv, xml, pdf, etc.
     * @param exportTypeJavaType the @{code JavaType} of the export type.
     * @return
     */
    private MethodMetadata getExportFormatMethod(String exportType,
            JavaType exportTypeJavaType) {

        String exportTypeUpperCase = StringUtils.upperCase(exportType);
        String exportTypeCapitalized = StringUtils.capitalize(exportType);

        /*
         * @RequestMapping(value = "/exportcsv", produces = "text/csv") public
         * void PetController.exportCsv(
         *
         * @DatatablesParams DatatablesCriterias criterias,
         *
         * @ModelAttribute Pet pet, HttpServletRequest request,
         * HttpServletResponse response) throws ServletException, IOException,
         * ExportException { ... }
         */
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));
        parameterTypes.add(new AnnotatedJavaType(entity,
                new AnnotationMetadataBuilder(MODEL_ATTRIBUTE).build()));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_REQUEST));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_RESPONSE));

        // Building method name
        JavaSymbolName methodName = new JavaSymbolName(
                "export".concat(exportTypeCapitalized));

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
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_VAL_NAME,
                "/export".concat(exportType));
        methodAnnotation.addStringAttribute(
                DatatablesConstants.RQST_MAP_ANN_PROD_NAME,
                "text/".concat(exportType));
        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(SERVLET_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);
        throwsTypes.add(DATATABLES_EXPORT_EXCEPTION);

        // Define method parameter names
        JavaSymbolName entityNameParam = new JavaSymbolName(entityName);
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(entityNameParam);
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        /*
         * export(criterias, pet, ExportType.CSV, new CsvExport(), request,
         * response);
         */
        String format = "export(%s, %s, %s.".concat(exportTypeUpperCase)
                .concat(", new %s(), %s, %s);");
        bodyBuilder.appendFormalLine(String.format(format,
                CRITERIA_PARAM_NAME.getSymbolName(),
                entityNameParam.getSymbolName(),
                helper.getFinalTypeName(DATATABLES_EXPORT_TYPE),
                helper.getFinalTypeName(exportTypeJavaType),
                REQUEST_PARAM_NAME.getSymbolName(),
                RESPONSE_PARAM_NAME.getSymbolName()));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>export</code> method for a render-a-view visualization
     * mode.
     * <p />
     * This is a private method that handles datatables AJAX request for a
     * specific format.
     * 
     * @return
     */
    private MethodMetadata getExportMethod() {

        /*
         * private void export(DatatablesCriterias criterias, Pet pet,
         * ExportType exportType, DatatablesExport datatablesExport,
         * HttpServletRequest request, HttpServletResponse response) throws
         * ExportException { ... }
         */
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(DATATABLES_CRITERIA_TYPE));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(entity));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(DATATABLES_EXPORT_TYPE));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(DATATABLES_EXPORT));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_REQUEST));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_RESPONSE));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(EXPORT_METHOD_NAME,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(DATATABLES_EXPORT_EXCEPTION);

        // Define method parameter names
        JavaSymbolName entityNameParam = new JavaSymbolName(entityName);
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(entityNameParam);
        parameterNames.add(DATATABLES_EXPORT_TYPE_NAME);
        parameterNames.add(DATATABLES_EXPORT_NAME);
        parameterNames.add(REQUEST_PARAM_NAME);
        parameterNames.add(RESPONSE_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        /*
         * // Does the export process as is explained in
         * http://dandelion.github.
         * io/datatables/tutorials/export/controller-based-exports.html // 1.
         * Retrieve the data List<Map<String, String>> data =
         * retrieveData(criterias, pet, request);
         */
        bodyBuilder
                .appendFormalLine("// Does the export process as is explained in http://dandelion.github.io/datatables/tutorials/export/controller-based-exports.html");
        bodyBuilder.appendFormalLine("// 1. Retrieve the data");
        String format = "%s data = retrieveData(%s, %s, %s);";
        bodyBuilder.appendFormalLine(String.format(format, helper
                .getFinalTypeName(LIST_MAP_STRING_STRING), CRITERIA_PARAM_NAME
                .getSymbolName(), new JavaSymbolName(entityName)
                .getSymbolName(), REQUEST_PARAM_NAME.getSymbolName()));

        /*
         * // 2. Build an instance of "ExportConf". ExportConf exportConf = new
         * ExportConf.Builder(exportType).header(true)
         * .exportClass(datatablesExport).autoSize(true)
         * .fileName(pet.getClass().getSimpleName()).build();
         */
        bodyBuilder
                .appendFormalLine("// 2. Build an instance of \"ExportConf\"");
        format = "%s exportConf = new %s.Builder(%s).header(true).exportClass(%s).autoSize(true).fileName(%s.getClass().getSimpleName()).build();";
        bodyBuilder.appendFormalLine(String.format(format,
                helper.getFinalTypeName(DATATABLES_EXPORT_CONF),
                helper.getFinalTypeName(DATATABLES_EXPORT_CONF),
                DATATABLES_EXPORT_TYPE_NAME.getSymbolName(),
                DATATABLES_EXPORT_NAME.getSymbolName(),
                entityNameParam.getSymbolName()));

        /*
         * // 3. Build an instance of "HtmlTable" HtmlTable table =
         * DatatablesUtils.makeHtmlTable(data, criterias, exportConf, request);
         */
        bodyBuilder
                .appendFormalLine("// 3. Build an instance of \"HtmlTable\"");
        format = "%s table = %s.makeHtmlTable(data, %s, exportConf, %s);";
        bodyBuilder.appendFormalLine(String.format(format,
                helper.getFinalTypeName(DATATABLES_HTML_TABLE),
                helper.getFinalTypeName(DATATABLES_UTILS),
                CRITERIA_PARAM_NAME.getSymbolName(),
                REQUEST_PARAM_NAME.getSymbolName()));

        /*
         * // 4. Render the generated export file
         * ExportUtils.renderExport(table, exportConf, response);
         */
        bodyBuilder.appendFormalLine("// 4. Render the generated export file");
        format = "%s.renderExport(table, exportConf, %s);";
        bodyBuilder.appendFormalLine(String.format(format,
                helper.getFinalTypeName(DATATABLES_EXPORT_UTILS),
                RESPONSE_PARAM_NAME.getSymbolName()));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, EXPORT_METHOD_NAME,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns <code>retrieveData</code> method for a render-a-view
     * visualization mode.
     * 
     * @return
     */
    private MethodMetadata getRetrieveDataMethod() {

        /*
         * private List<Map<String, String>> retrieveData( DatatablesCriterias
         * criterias, Pet pet, HttpServletRequest request) { ... }
         */

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(DATATABLES_CRITERIA_TYPE));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(entity));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(HTTP_SERVLET_REQUEST));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(RETRIEVE_DATA_METHOD_NAME,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(new JavaSymbolName(entityName));
        parameterNames.add(REQUEST_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        /*
         * // Cloned criteria in order to not paginate the results
         * DatatablesCriterias noPaginationCriteria = new DatatablesCriterias(
         * criterias.getSearch(), 0, null, criterias.getColumnDefs(),
         * criterias.getSortingColumnDefs(), criterias.getInternalCounter());
         */
        bodyBuilder
                .appendFormalLine("// Cloned criteria in order to not paginate the results");
        String format = "%s noPaginationCriteria = new %s(%s.getSearch(), 0, null, %s.getColumnDefs(), %s.getSortingColumnDefs(), %s.getInternalCounter());";
        String datatablesCriteriaType = helper
                .getFinalTypeName(DATATABLES_CRITERIA_TYPE);
        String criteriaParamName = CRITERIA_PARAM_NAME.getSymbolName();
        bodyBuilder.appendFormalLine(String.format(format,
                datatablesCriteriaType, datatablesCriteriaType,
                criteriaParamName, criteriaParamName, criteriaParamName,
                criteriaParamName));

        /*
         * // Do the search to obtain the data Map<String, Object>
         * baseSearchValuesMap = getPropertyMap(XXX, request);
         * setDatatablesBaseFilter(baseSearchValuesMap);
         */
        bodyBuilder.appendFormalLine("// Do the search to obtain the data");
        bodyBuilder
                .appendFormalLine(String
                        .format("Map<String, Object> baseSearchValuesMap = getPropertyMap(%s, %s);",
                                new JavaSymbolName(entityName),
                                REQUEST_PARAM_NAME));
        bodyBuilder
                .appendFormalLine("setDatatablesBaseFilter(baseSearchValuesMap);");

        /*
         * SearchResults<Visit> searchResult = DatatablesUtils.findByCriteria(
         * Visit.class, Visit.entityManager(), noPaginationCriteria,
         * baseSearchValuesMap); org.springframework.ui.Model uiModel = new
         * org.springframework.ui.ExtendedModelMap();
         */
        format = "%s searchResult = %s.findByCriteria(%s.class, %s.entityManager(), noPaginationCriteria, baseSearchValuesMap);";
        String entityTypeName = helper.getFinalTypeName(entity);
        bodyBuilder.appendFormalLine(String.format(format, new JavaType(
                SEARCH_RESULTS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(entity)), helper
                .getFinalTypeName(DATATABLES_UTILS), entityTypeName,
                entityTypeName));

        String dateFormatVarName = getCodeToAddDateTimeFormatPatterns(bodyBuilder);

        /*
         * // Use ConversionService with the obtained data return
         * DatatablesUtils.populateDataSet(searchResult.getResults(), "id",
         * searchResult.getTotalCount(), searchResult.getResultsCount(),
         * criterias.getColumnDefs(), null, conversionService_dtt) .getRows();
         */
        bodyBuilder
                .appendFormalLine("// Use ConversionService with the obtained data");
        format = "return %s.populateDataSet(searchResult.getResults(), \"id\", searchResult.getTotalCount(), searchResult.getResultsCount(), %s.getColumnDefs(), %s, %s).getRows();";
        bodyBuilder.appendFormalLine(String.format(format,
                helper.getFinalTypeName(DATATABLES_UTILS),
                CRITERIA_PARAM_NAME.getSymbolName(), dateFormatVarName,
                getConversionServiceField().getFieldName().getSymbolName()));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, RETRIEVE_DATA_METHOD_NAME,
                LIST_MAP_STRING_STRING, parameterTypes, parameterNames,
                bodyBuilder);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Add the Java code to call tje dateformat method named
     * "addDateTimeFormatPatterns(uiModel)".
     * 
     * @param bodyBuilder the InvocableMemberBodyBuilder
     * @return the name of the date format variable.
     */
    private String getCodeToAddDateTimeFormatPatterns(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Call to dateformat method named "addDateTimeFormatPatterns(uiModel)"
        String dateFormatVarName = "null";
        if (entityDatePatterns != null && !entityDatePatterns.isEmpty()) {

            // Model uiModel = new ExtendedModelMap();
            bodyBuilder.appendFormalLine(String.format(
                    "%s uiModel = new %s();", MODEL, EXTENDED_MODEL_MAP));

            // addDateTimeFormatPatterns(uiModel);
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");

            // Map<String, Object> datePattern = uiModel.asMap();
            bodyBuilder.appendFormalLine(String.format(
                    "%s datePattern = uiModel.asMap();",
                    helper.getFinalTypeName(MAP_STRING_OBJECT)));

            dateFormatVarName = "datePattern";
        }
        return dateFormatVarName;
    }

    /**
     * Informs if a method is already defined on class
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {

        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    // Typically, no changes are required beyond this point

    public final String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("ajax", String.valueOf(annotationValues.isAjax()));
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * @return related JPA entity
     */
    public final JavaType getEntity() {
        return entity;
    }

    /**
     * @return {@link GvNIXDatatables} annotation values
     */
    public final DatatablesAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    /**
     * @return {@link RooWebScaffold} annotation values
     */
    public final WebScaffoldAnnotationValues getWebScaffoldAnnotationValues() {
        return webScaffoldAnnotationValues;
    }

    /**
     * @return controllers use AJAX data mode or not (DOM)
     */
    public final boolean isAjax() {
        return annotationValues.isAjax();
    }

    /**
     * @return controller entity properties for detail datatables
     */
    public final String[] getDetailFields() {
        return annotationValues.getDetailFields();
    }

    /**
     * @return informs if controllers has JPA-Batch-operations support available
     */
    public final boolean hasJpaBatchSupport() {
        return webJpaBatchMetadata != null;
    }

    /**
     * @return datatables shows a standard list or use render-a-view mode
     */
    public final boolean isStantardMode() {
        return annotationValues.isStandardMode();
    }

    /**
     * @return information about dynamic finder registered on the controller
     */
    public final Map<FinderMetadataDetails, QueryHolderTokens> getFindersRegistered() {
        return findersRegistered;
    }

    /**
     * @return informs if user can edit data inline (inside the table row)
     */
    public final boolean isInlineEditing() {
        return annotationValues.isInlineEditing();
    }

    /**
     * @return informs if Spring Security is applied to the project
     */
    private boolean isSecurityApplied() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String appSecurityXMLPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SPRING_CONFIG_ROOT, ""),
                "applicationContext-security.xml");
        if (new File(appSecurityXMLPath).exists()) {
            return true;
        }
        return false;
    }
}
