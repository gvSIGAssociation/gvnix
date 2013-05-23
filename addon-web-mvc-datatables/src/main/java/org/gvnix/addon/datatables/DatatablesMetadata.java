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
package org.gvnix.addon.datatables;

import static org.gvnix.addon.datatables.DatatablesConstants.*;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.SpringJavaType.MODEL;
import static org.springframework.roo.model.SpringJavaType.MODEL_ATTRIBUTE;
import static org.springframework.roo.model.SpringJavaType.REQUEST_MAPPING;
import static org.springframework.roo.model.SpringJavaType.RESPONSE_BODY;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.query.JpaQueryMetadata;
import org.gvnix.addon.web.mvc.batch.WebJpaBatchMetadata;
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
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * {@link GvNIXDatatables} metadata
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class DatatablesMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

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
     * Related entity plural
     */
    private final String entityPlural;

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
     * Itd builder herlper
     */
    private WebItdBuilderHelper helper;

    private final WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    public DatatablesMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            DatatablesAnnotationValues annotationValues, JavaType entity,
            List<FieldMetadata> identifierProperties, String entityPlural,
            JavaSymbolName entityManagerMethodName, boolean hasDateTypes,
            JavaType webScaffoldAspectName,
            WebJpaBatchMetadata webJpaBatchMetadata,
            JpaQueryMetadata jpaQueryMetadata,
            WebScaffoldAnnotationValues webScaffoldAnnotationValues) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.annotationValues = annotationValues;

        this.helper = new WebItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        // Roo only uses one property
        this.entityIdentifier = identifierProperties.get(0);
        this.entity = entity;
        this.entityPlural = entityPlural;
        this.entityEntityManagerMethod = entityManagerMethodName;
        this.webJpaBatchMetadata = webJpaBatchMetadata;
        this.jpaQueryMetadata = jpaQueryMetadata;
        this.webScaffoldAnnotationValues = webScaffoldAnnotationValues;

        // Adding precedence declaration
        // This aspect before webScaffold
        builder.setDeclarePrecedence(aspectName, webScaffoldAspectName);

        // Adding field definition
        builder.addField(getConversionServiceField());

        // Adding methods definition
        builder.addMethod(getListDatatablesRequestMethod());
        builder.addMethod(getPopulateDatatablesConfig());
        builder.addMethod(getListRooRequestMethod());

        // Add AJAX mode required methods
        if (isAjax()) {
            builder.addMethod(getGetDatatablesDataMethod());
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>populateDatatablesHasBatchSupport</code> method
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
     * Returns <code>listDatatablesRequest</code> method
     * 
     * @return
     */
    private MethodMetadata getListDatatablesRequestMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(MODEL);

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
        methodAnnotation.addEnumAttribute("method", REQUEST_METHOD, "GET");

        // @RequestMapping(... produces = "text/html")
        methodAnnotation.addStringAttribute("produces", "text/html");

        annotations.add(methodAnnotation);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UI_MODEL);

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
     * Build method body for <code>listDatatablesRequest</code> method for DOM
     * mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequesMethodDomBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        JavaType objectList = new JavaType(LIST.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(entity));
        String listVarName = StringUtils.uncapitalize(entityPlural);

        bodyBuilder
                .appendFormalLine("// Get all data to put it on pageContext");
        bodyBuilder.appendFormalLine(String.format("%s %s = %s.findAll%s();",
                helper.getFinalTypeName(objectList), listVarName,
                helper.getFinalTypeName(entity), entityPlural));

        // uiModel.addAttribute("pets",pets);
        bodyBuilder.appendFormalLine(String.format(
                "%s.addAttribute(\"%s\",%s);", UI_MODEL.getSymbolName(),
                entityPlural.toLowerCase(), listVarName));

        // return "pets/list";
        bodyBuilder.appendFormalLine(String.format("return \"%s/list\";",
                entityPlural.toLowerCase()));

    }

    /**
     * Build method body for <code>listDatatablesRequest</code> method for AJAX
     * mode
     * 
     * @param bodyBuilder
     */
    private void buildListDatatablesRequesMethodAjaxBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // return "pets/list";
        bodyBuilder.appendFormalLine(String.format("return \"%s/list\";",
                entityPlural.toLowerCase()));

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
        // Model uiModel) {

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = Arrays.asList(helper
                .createRequestParam(JavaType.INT_OBJECT, "page", false, null),
                helper.createRequestParam(JavaType.INT_OBJECT, "size", false,
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
                "text/html", null, null));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName("page"), new JavaSymbolName("size"),
                UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildListRooRequestMethodBody(bodyBuilder);

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
     * Build method body for <code>listRooRequest</code> method
     * 
     * @param bodyBuilder
     */
    private void buildListRooRequestMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        bodyBuilder
                .appendFormalLine("// overrides the standar Roo list method and");
        bodyBuilder.appendFormalLine("// delegates on datatables list method");
        // return listDatatables(uiModel);
        bodyBuilder.appendFormalLine(String.format("return %s(%s);",
                LIST_DATATABLES.getSymbolName(), UI_MODEL.getSymbolName()));
    }

    /**
     * Returns <code>getDatatablesData</code> method
     * 
     * @return
     */
    private MethodMetadata getGetDatatablesDataMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(new AnnotatedJavaType(DATATABLES_CRITERIA_TYPE,
                new AnnotationMetadataBuilder(DATATABLES_PARAMS).build()));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(MODEL));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GET_DATATABLES_DATA,
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
        methodAnnotation.addStringAttribute("headers",
                "Accept=application/json");
        methodAnnotation.addStringAttribute("value", "/datatables/ajax");
        methodAnnotation.addStringAttribute("produces", "application/json");
        annotations.add(methodAnnotation);
        annotations.add(new AnnotationMetadataBuilder(RESPONSE_BODY));

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CRITERIA_PARAM_NAME);
        parameterNames.add(UI_MODEL);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetDatatablesDataMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GET_DATATABLES_DATA,
                GET_DATATABLES_DATA_RETURN, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Build method body for <code>getDatatablesData</code> method
     * 
     * @param bodyBuilder
     */
    private void buildGetDatatablesDataMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Prepares associations information
        String filterByInfo = "null";
        String orderByInfo = "null";
        final String entityFinalName = helper.getFinalTypeName(entity);
        if (jpaQueryMetadata != null) {
            filterByInfo = String.format("%s.%s()", entityFinalName,
                    jpaQueryMetadata.getFilterByMethodName().getSymbolName());
            orderByInfo = String.format("%s.%s()", entityFinalName,
                    jpaQueryMetadata.getOrderByMethodName().getSymbolName());
        }

        JavaType serachResult = new JavaType(
                SEARCH_RESULTS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(entity));

        // SearchResults<Pet> searchResult =
        // DatatablesUtils.findByCriteria(Pet.class,
        // Pet.getFilterByAssociations(), Pet.getOrderByAssociations(),
        // Pet.entityManager(), criterias);

        bodyBuilder
                .appendFormalLine(String
                        .format("%s searchResult = %s.findByCriteria(%s.class, %s, %s, %s.%s(), %s);",
                                helper.getFinalTypeName(serachResult),
                                helper.getFinalTypeName(DATATABLES_UTILS),
                                entityFinalName, filterByInfo, orderByInfo,
                                entityFinalName,
                                entityEntityManagerMethod.getSymbolName(),
                                CRITERIA_PARAM_NAME.getSymbolName()));

        bodyBuilder.appendFormalLine("");
        // long totalRecords = Pet.countPets();
        bodyBuilder.appendFormalLine(String.format(
                "long totalRecords = %s.count%s();", entityFinalName,
                entityPlural));

        // long recordsFound = findResult.getTotalResultCount();
        bodyBuilder.appendFormalLine(String.format(
                "long recordsFound = searchResult.getTotalResultCount();",
                entityFinalName, entityPlural));

        bodyBuilder.appendFormalLine("");
        bodyBuilder.appendFormalLine("// Entity pk field name");

        // String pkFieldName = "id";
        bodyBuilder.appendFormalLine(String.format(
                "String pkFieldName = \"%s\";", entityIdentifier.getFieldName()
                        .getSymbolName()));

        bodyBuilder.appendFormalLine("");
        /*
         * DataSet<Map<String, String>> dataSet =
         * DatatablesUtils.populateDataSet(searchResult.getResult(),
         * pkFieldName, totalRecords, recordsFound, criterias.getColumnDefs(),
         * null, conversionService_datatables);
         */

        bodyBuilder
                .appendFormalLine(String
                        .format("%s dataSet = %s.populateDataSet(searchResult.getResults(), pkFieldName, totalRecords, recordsFound, criterias.getColumnDefs(), null, %s); ",
                                helper.getFinalTypeName(DATA_SET_MAP_STRING_STRING),
                                helper.getFinalTypeName(DATATABLES_UTILS),
                                getConversionServiceField().getFieldName()
                                        .getSymbolName()));

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
            JavaSymbolName curName = new JavaSymbolName(
                    "conversionService_datatables");
            // Check if field exist
            FieldMetadata currentField = governorTypeDetails
                    .getDeclaredField(curName);
            if (currentField != null) {
                if (currentField.getAnnotation(AUTOWIRED) == null
                        || !currentField.getFieldType().equals(
                                CONVERSION_SERVICE)) {
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

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {

        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    // Typically, no changes are required beyond this point

    public String toString() {
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

    public JavaType getEntity() {
        return entity;
    }

    public DatatablesAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    public WebScaffoldAnnotationValues getWebScaffoldAnnotationValues() {
        return webScaffoldAnnotationValues;
    }

    public boolean isAjax() {
        return annotationValues.isAjax();
    }

    public boolean hasJpaBatchSupport() {
        return webJpaBatchMetadata != null;
    }
}
