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
package org.gvnix.addon.jpa.audit;

import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.SpringJavaType.DATE_TIME_FORMAT;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.audit.providers.RevisionLogMetadataBuilder;
import org.gvnix.addon.jpa.audit.providers.RevisionLogMetadataBuilder.Context;
import org.gvnix.support.ItdBuilderHelper;
import org.gvnix.support.ItdBuilderHelper.GET_FIELD_EXISTS_ACTION;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
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
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXJpaAudit} annotation.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
public class JpaAuditMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaAuditMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    // Method and field names
    public static final JavaSymbolName CREATION_FIELD = new JavaSymbolName(
            "auditCreation");
    public static final JavaSymbolName CREATED_BY_FIELD = new JavaSymbolName(
            "auditCreatedBy");
    public static final JavaSymbolName LAST_UPDATE_FIELD = new JavaSymbolName(
            "auditLastUpdate");
    public static final JavaSymbolName LAST_UPDATED_BY_FIELD = new JavaSymbolName(
            "auditLastUpdatedBy");
    public static final JavaSymbolName GET_REVISON_NUMBER_FOR_DATE_METHOD = new JavaSymbolName(
            "getRevisionNumberForDate");
    public static final JavaSymbolName REV_ITEM_GET_ITEM_METHOD = new JavaSymbolName(
            "getItem");
    public static final JavaSymbolName REV_ITEM_GET_REVISION_NUMBER_METHOD = new JavaSymbolName(
            "getRevisionNumber");
    public static final JavaSymbolName REV_ITEM_GET_REVISION_USER_METHOD = new JavaSymbolName(
            "getRevisionUser");
    public static final JavaSymbolName REV_ITEM_GET_REVISION_DATE_METHOD = new JavaSymbolName(
            "getRevisionDate");
    public static final JavaSymbolName REV_ITEM_IS_CREATE_METHOD = new JavaSymbolName(
            "isCreate");
    public static final JavaSymbolName REV_ITEM_IS_UPDATE_METHOD = new JavaSymbolName(
            "isUpdate");
    public static final JavaSymbolName REV_ITEM_IS_DELETE_METHOD = new JavaSymbolName(
            "isDelete");
    public static final JavaSymbolName REV_ITEM_GET_TYPE_METHOD = new JavaSymbolName(
            "getType");

    // Required types used on code generation
    private static final JavaType MAP_STRING_OBJECT = new JavaType(
            JdkJavaType.MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(JavaType.STRING, JavaType.OBJECT));
    private static final JavaType LIST_STRING = new JavaType(
            JdkJavaType.LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(JavaType.STRING));

    // General metadata methods
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

    // Metadata properties

    /**
     * Itd builder herlper
     */
    private final ItdBuilderHelper helper;

    private final JpaAuditAnnotationValues annotationValues;
    /**
     * Builder created for this metadata to generate revision log artifacts.
     * <p/>
     * Is null if no provider is configured
     */
    private final RevisionLogMetadataBuilder revisionLogBuilder;
    /**
     * Entity java type
     */
    private final JavaType entity;

    /**
     * Name of entity
     */
    private final String entityName;
    /**
     * Plural configured for entity
     */
    private final String entityPlural;

    /**
     * Current entity identifier field
     */
    private final FieldMetadata identifier;
    /**
     * This entity is abstract or not
     */
    private final boolean isAbstract;
    /**
     * JavaType for List entity elements
     */
    private final JavaType entityListType;

    /**
     * fillAll (revision log) method name (based on entity name)
     */
    private JavaSymbolName findAllMethodName;

    /**
     * find (revision log) method name (based on entity name)
     */
    private JavaSymbolName findMethodName;

    /**
     * getRevision (revision log) method name (based on entity name)
     */
    private JavaSymbolName getRevisionsMethodName;

    /**
     * findRevision by date (revision log) method name (based on entity name)
     */
    private JavaSymbolName findRevisionsByDatesMethodName;

    /**
     * findRevision (revision log) method name (based on entity name)
     */
    private JavaSymbolName findRevisionsMethodName;

    /**
     * Type to use for "user" fields
     */
    private final JavaType userType;

    /**
     * Type to use for "user" is an Entity
     */
    private final boolean userTypeEntity;

    /**
     * Name of revision item (based on entity name)
     */
    private String revisonItemTypeName;

    /**
     * Java type for revision log item (based on entity name)
     */
    private JavaType revisonItemType;

    /**
     * Java type for list of revision log item (based on entity name)
     */
    private JavaType revisonItemListType;

    /**
     * Instance with all build information which can be required by revisionLog
     * builder to generate its artifacts
     */
    private Context buildContext;

    public JpaAuditMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaAuditAnnotationValues annotationValues, String entityPlural,
            RevisionLogMetadataBuilder revisionLogBuilder,
            List<FieldMetadata> identifiers, JavaType userType,
            boolean userTypeEntity) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.annotationValues = annotationValues;

        this.revisionLogBuilder = revisionLogBuilder;

        this.entity = governorPhysicalTypeMetadata.getType();
        this.entityName = entity.getSimpleTypeName();

        this.entityPlural = entityPlural;

        this.entityListType = new JavaType(LIST.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(entity));

        this.identifier = identifiers.get(0);

        this.userType = userType;

        this.userTypeEntity = userTypeEntity;

        this.isAbstract = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails().isAbstract();

        // Generate base auditing fields
        if (!isAbstractEntity()) {
            // Only add audit fields on non-abstract instances

            // add auditCreation field (and getter/setter)
            builder.addField(getFieldAuditCreation());
            builder.addMethod(getGetAuditCreationMethod());
            builder.addMethod(getSetAuditCreationMethod());

            // add auditCreatedBy field (and getter/setter)
            builder.addField(getFieldAuditCreatedBy());
            builder.addMethod(getGetAuditCreatedByMethod());
            builder.addMethod(getSetAuditCreatedByMethod());

            // add auditUpdate field (and getter/setter)
            builder.addField(getFieldAuditLastUpdate());
            builder.addMethod(getGetAuditLastUpdateMethod());
            builder.addMethod(getSetAuditLastUpdateMethod());

            // add auditUpdatedBy field (and getter/setter)
            builder.addField(getFieldAuditLastUpdatedBy());
            builder.addMethod(getGetAuditLastUpdatedByMethod());
            builder.addMethod(getSetAuditLastUpdatedByMethod());
        }

        // Prepare revisionLog declarations (if any)
        if (revisionLogBuilder != null) {

            // Prepare entity-dependent names and types
            initializeEntityDependenDefinitions();

            // Create revisionLog build context
            this.buildContext = createBuildContext();

            // initialize revision log builder
            this.revisionLogBuilder.initialize(buildContext);

            if (!isAbstract) {
                // Only add audit search method on non-abstract entities

                // Add Revision log management methods
                builder.addMethod(getFindAllFromDateMethod());
                builder.addMethod(getFindAllFromRevisionMethod());
                builder.addMethod(getFindFromDateMethod());
                builder.addMethod(getFindFromRevisionMethod());
                builder.addMethod(getGetRevisionsMethod());
                builder.addMethod(getGetRevisionsInstanceMethod());
                builder.addMethod(getGetRevisionNumberForDateMethod());
                builder.addMethod(getFindRevisionByDatesMethod());
                builder.addMethod(getFindRevisionsMethod());

                // Add Revision log item class definition
                builder.addInnerType(getRevisionClass());
            }

            // Add provider dependent artifacts
            revisionLogBuilder.addCustomArtifact(builder);

            // clean unneeded revision log references
            this.revisionLogBuilder.done();
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * @return create a build context instance required by revision log builder
     */
    private Context createBuildContext() {
        return new BuildContext(getId(), helper, this.annotationValues, entity,
                entityName, this.entityPlural, findAllMethodName,
                findMethodName, getRevisionsMethodName,
                findRevisionsByDatesMethodName, findRevisionsMethodName,
                revisonItemTypeName, entityListType, revisonItemType,
                this.identifier, revisonItemListType, isAbstract, userType);
    }

    /**
     * Initialize all entity name dependent methods-names and javaTypes
     */
    private void initializeEntityDependenDefinitions() {
        this.findAllMethodName = new JavaSymbolName(
                "findAll".concat(StringUtils.capitalize(this.entityPlural)));
        this.findMethodName = new JavaSymbolName("find".concat(StringUtils
                .capitalize(entityName)));
        this.getRevisionsMethodName = new JavaSymbolName("get".concat(
                StringUtils.capitalize(entityName)).concat("Revisions"));
        this.findRevisionsByDatesMethodName = new JavaSymbolName(
                "find".concat(StringUtils.capitalize(entityName).concat(
                        "RevisionsByDates")));
        this.findRevisionsMethodName = new JavaSymbolName(
                "find".concat(StringUtils.capitalize(entityName).concat(
                        "Revisions")));
        this.revisonItemTypeName = StringUtils.capitalize(entityName).concat(
                "Revision");
        this.revisonItemType = new JavaType(entity.getFullyQualifiedTypeName()
                .concat(".").concat(revisonItemTypeName));
        this.revisonItemListType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(revisonItemType));
    }

    /**
     * @return revision item class definition
     */
    private ClassOrInterfaceTypeDetails getRevisionClass() {

        // Check class exists
        ClassOrInterfaceTypeDetails innerClass = governorTypeDetails
                .getDeclaredInnerType(revisonItemType);

        if (innerClass != null) {
            // If class exists (already pushed-in) we can do nothing
            return innerClass;
        }

        // Create class builder for inner class
        ClassOrInterfaceTypeDetailsBuilder classBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, revisonItemType,
                PhysicalTypeCategory.CLASS);

        // Add revisionLog-provider required artifacts
        revisionLogBuilder.addCustomArtifactToRevisionItem(classBuilder);

        // Add Revision item common methods
        classBuilder.addMethod(createRevisionItemGetItemMethod());
        classBuilder.addMethod(createRevisionItemGetRevisionNumberMethod());
        classBuilder.addMethod(createRevisionItemGetRevisionUserMethod());
        classBuilder.addMethod(createRevisionItemGetRevisionDateMethod());
        classBuilder.addMethod(createRevisionItemIsCreateMethod());
        classBuilder.addMethod(createRevisionItemIsUpdateMethod());
        classBuilder.addMethod(createRevisionItemIsDeleteMethod());
        classBuilder.addMethod(createRevisionItemGetTypeMethod());

        // Build class definition from builder
        return classBuilder.build();
    }

    /**
     * Create a method for revision Item class
     * 
     * @param methodName
     * @param returnValue
     * @param body
     * @return
     */
    private MethodMetadata createRevisionItemMethod(JavaSymbolName methodName,
            JavaType returnValue, InvocableMemberBodyBuilder body) {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>(
                0);
        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                0);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>(0);

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameters = new ArrayList<JavaSymbolName>(0);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, returnValue,
                parameterTypes, parameters, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadataç

    }

    /**
     * @return creates XXRevsion.getType() method
     */
    private MethodMetadata createRevisionItemGetTypeMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemGetType(body);
        return createRevisionItemMethod(REV_ITEM_GET_TYPE_METHOD,
                JavaType.STRING, body);
    }

    /**
     * @return creates XXRevison.isDelete() method
     */
    private MethodMetadata createRevisionItemIsDeleteMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemIsDelete(body);
        return createRevisionItemMethod(REV_ITEM_IS_DELETE_METHOD,
                JavaType.BOOLEAN_PRIMITIVE, body);
    }

    /**
     * @return create XXRevsion.isUpdate() method
     */
    private MethodMetadata createRevisionItemIsUpdateMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemIsUpdate(body);
        return createRevisionItemMethod(REV_ITEM_IS_UPDATE_METHOD,
                JavaType.BOOLEAN_PRIMITIVE, body);
    }

    /**
     * @return creates XXRevsion.isCreate() method
     */
    private MethodMetadata createRevisionItemIsCreateMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemIsCreate(body);
        return createRevisionItemMethod(REV_ITEM_IS_CREATE_METHOD,
                JavaType.BOOLEAN_PRIMITIVE, body);
    }

    /**
     * @return creates XXRevsion.getRevsionDate() method
     */
    private MethodMetadata createRevisionItemGetRevisionDateMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemGetRevisionDate(body);
        return createRevisionItemMethod(REV_ITEM_GET_REVISION_DATE_METHOD,
                JdkJavaType.DATE, body);
    }

    /**
     * @return creates XXRevision.getUser() method
     */
    private MethodMetadata createRevisionItemGetRevisionUserMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemGetRevisionUser(body);
        return createRevisionItemMethod(REV_ITEM_GET_REVISION_USER_METHOD,
                userType, body);
    }

    /**
     * @return creates XXRevision.getRevisionNumber() method
     */
    private MethodMetadata createRevisionItemGetRevisionNumberMethod() {
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemGetRevisionNumber(body);
        return createRevisionItemMethod(REV_ITEM_GET_REVISION_NUMBER_METHOD,
                JavaType.LONG_OBJECT, body);
    }

    /**
     * @return creates XXRevision.getItem() method
     */
    private MethodMetadata createRevisionItemGetItemMethod() {

        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyRevisionItemGetItem(body);
        return createRevisionItemMethod(REV_ITEM_GET_ITEM_METHOD, entity, body);
    }

    /**
     * @return gets or creates
     *         findRevison(fromRevsion,toRevision,filter,order,start,limit)
     *         method
     */
    private MethodMetadata getFindRevisionsMethod() {
        // method name
        JavaSymbolName methodName = findRevisionsMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                JavaType.LONG_OBJECT, JavaType.LONG_OBJECT, MAP_STRING_OBJECT,
                LIST_STRING, JavaType.INT_OBJECT, JavaType.INT_OBJECT);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName(
                "fromRevision", "toRevision", "filterMap", "order", "start",
                "limit");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyFindRevision(bodyBuilder, parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                revisonItemListType, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates
     *         findXXXRevisonByDates(fromDate,toDate,filter,order,start,limit)
     *         method
     */
    private MethodMetadata getFindRevisionByDatesMethod() {
        // method name
        JavaSymbolName methodName = findRevisionsByDatesMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                JdkJavaType.DATE, JdkJavaType.DATE, MAP_STRING_OBJECT,
                LIST_STRING, JavaType.INT_OBJECT, JavaType.INT_OBJECT);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("fromDate",
                "toDate", "filterMap", "order", "start", "limit");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyFindRevisionByDates(bodyBuilder,
                parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                revisonItemListType, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates getXXXRevisions(fromData,toDate,start,limit)
     *         instance method
     */
    private MethodMetadata getGetRevisionsInstanceMethod() {
        // method name
        JavaSymbolName methodName = getRevisionsMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                JdkJavaType.DATE, JdkJavaType.DATE, JavaType.INT_OBJECT,
                JavaType.INT_OBJECT);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("fromDate",
                "toDate", "start", "limit");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyGetRevisionsInstance(bodyBuilder,
                parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, revisonItemListType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates getXXXRevisions(id, fromData,toDate,start,limit)
     *         method
     */
    private MethodMetadata getGetRevisionsMethod() {
        // method name
        JavaSymbolName methodName = getRevisionsMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                identifier.getFieldType(), JdkJavaType.DATE, JdkJavaType.DATE,
                JavaType.INT_OBJECT, JavaType.INT_OBJECT);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName(identifier
                .getFieldName().getSymbolName(), "fromDate", "toDate", "start",
                "limit");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyGetRevisions(bodyBuilder, parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                revisonItemListType, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates findXXX(id, revision) method
     */
    private MethodMetadata getFindFromRevisionMethod() {
        // method name
        JavaSymbolName methodName = findMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                identifier.getFieldType(), JavaType.LONG_PRIMITIVE);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName(identifier
                .getFieldName().getSymbolName(), "revision");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyFindFromRevision(bodyBuilder,
                parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName, entity,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates findXXX(id, date) method
     */
    private MethodMetadata getFindFromDateMethod() {
        // method name
        JavaSymbolName methodName = findMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                identifier.getFieldType(), JdkJavaType.DATE);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName(identifier
                .getFieldName().getSymbolName(), "atDate");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyFindFromDate(bodyBuilder, parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName, entity,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        helper.addJavaDocToMethod(
                methodBuilder,
                "Find a ".concat(entity.getSimpleTypeName()).concat(
                        "with values on specified date"), null);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates getRevisionNumberForDate(aDate) method
     */
    private MethodMetadata getGetRevisionNumberForDateMethod() {
        // method name
        JavaSymbolName methodName = GET_REVISON_NUMBER_FOR_DATE_METHOD;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper
                .toAnnotedJavaType(JdkJavaType.DATE);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("aDate");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyGetRevisionNumberForDate(bodyBuilder,
                parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                JavaType.LONG_OBJECT, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates findAllXX(revision) method
     */
    private MethodMetadata getFindAllFromRevisionMethod() {
        // method name
        JavaSymbolName methodName = findAllMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper
                .toAnnotedJavaType(JavaType.LONG_PRIMITIVE);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("revision");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder.buildBodyFindAllFromRevision(bodyBuilder,
                parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, entityListType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        helper.addJavaDocToMethod(
                methodBuilder,
                "Find all ".concat(entity.getSimpleTypeName()).concat(
                        "status on specified revision"), null);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates findAllXX(aDate) method
     */
    private MethodMetadata getFindAllFromDateMethod() {
        // method name
        JavaSymbolName methodName = findAllMethodName;

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper
                .toAnnotedJavaType(JdkJavaType.DATE);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(methodName,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("atDate");

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        revisionLogBuilder
                .buildBodyFindAllFromDate(bodyBuilder, parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, entityListType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        helper.addJavaDocToMethod(
                methodBuilder,
                "Find all ".concat(entity.getSimpleTypeName()).concat(
                        "status on specified date"), null);
        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return auditLastUpdatedBy field
     */
    private FieldMetadata getFieldAuditLastUpdatedBy() {
        List<AnnotationMetadataBuilder> annotations = null;
        if (userTypeEntity) {
            annotations = new ArrayList<AnnotationMetadataBuilder>(1);
            annotations.add(new AnnotationMetadataBuilder(
                    AnnotationMetadataBuilder.JPA_MANY_TO_ONE_ANNOTATION));

        }
        return helper.getField(LAST_UPDATED_BY_FIELD, Modifier.PRIVATE,
                userType, annotations, GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return auditLastUpdate field
     */
    private FieldMetadata getFieldAuditLastUpdate() {
        return helper.getField(LAST_UPDATE_FIELD, Modifier.PRIVATE,
                JdkJavaType.CALENDAR, getDateFormatAnnotations(),
                GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return auditCreatedBy field
     */
    private FieldMetadata getFieldAuditCreatedBy() {
        List<AnnotationMetadataBuilder> annotations = null;
        if (userTypeEntity) {
            annotations = new ArrayList<AnnotationMetadataBuilder>(1);
            annotations.add(new AnnotationMetadataBuilder(
                    AnnotationMetadataBuilder.JPA_MANY_TO_ONE_ANNOTATION));

        }
        return helper.getField(CREATED_BY_FIELD, Modifier.PRIVATE, userType,
                annotations, GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return auditCreation field
     */
    public FieldMetadata getFieldAuditCreation() {
        return helper.getField(CREATION_FIELD, Modifier.PRIVATE,
                JdkJavaType.CALENDAR, getDateFormatAnnotations(),
                GET_FIELD_EXISTS_ACTION.RETURN_EXISTING);
    }

    /**
     * @return annotation list for time-stamp fields
     */
    private List<AnnotationMetadataBuilder> getDateFormatAnnotations() {
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);
        final AnnotationMetadataBuilder dateTimeFormatBuilder = new AnnotationMetadataBuilder(
                DATE_TIME_FORMAT);
        dateTimeFormatBuilder.addStringAttribute("style", "MM");
        annotations.add(dateTimeFormatBuilder);
        return annotations;
    }

    /**
     * Gets <code>getAuditCreation</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditCreationMethod() {
        return helper.getGetterMethod(CREATION_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>setAuditCreation</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditCreationMethod() {
        return helper.getSetterMethod(CREATION_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>getAuditCreatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditCreatedByMethod() {
        return helper.getGetterMethod(CREATED_BY_FIELD, userType, null);
    }

    /**
     * Gets <code>setAuditCreatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditCreatedByMethod() {
        return helper.getSetterMethod(CREATED_BY_FIELD, userType, null);
    }

    /**
     * Gets <code>getAuditLastUpdate</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditLastUpdateMethod() {
        return helper.getGetterMethod(LAST_UPDATE_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>setAuditLastUpdate</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditLastUpdateMethod() {
        return helper.getSetterMethod(LAST_UPDATE_FIELD, JdkJavaType.CALENDAR,
                null);
    }

    /**
     * Gets <code>getAuditLastUpdatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getGetAuditLastUpdatedByMethod() {
        return helper.getGetterMethod(LAST_UPDATED_BY_FIELD, userType, null);
    }

    /**
     * Gets <code>setAuditLastUpdatedBy</code> method.
     * 
     * @return
     */
    private MethodMetadata getSetAuditLastUpdatedByMethod() {
        return helper.getSetterMethod(LAST_UPDATED_BY_FIELD, userType, null);
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("revisionLog", annotationValues.storeRevisionLog);
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * @return name of getCreation method
     */
    public JavaSymbolName getGetCreationMethodName() {
        return helper.getGetterMethodNameForField(CREATION_FIELD);
    }

    /**
     * @return name of getCreatedBy method
     */
    public JavaSymbolName getGetCreatedByMethodName() {
        return helper.getGetterMethodNameForField(CREATED_BY_FIELD);
    }

    /**
     * @return name of getLastUpdate method
     */
    public JavaSymbolName getGetLastUpdatedMethodName() {
        return helper.getGetterMethodNameForField(LAST_UPDATE_FIELD);
    }

    /**
     * @return name of getLastUpdatedBy method
     */
    public JavaSymbolName getGetLastUpdateByMethodName() {
        return helper.getGetterMethodNameForField(LAST_UPDATED_BY_FIELD);
    }

    /**
     * @return name of setCreation method
     */
    public JavaSymbolName getSetCreationMethodName() {
        return helper.getSetterMethodNameForField(CREATION_FIELD);
    }

    /**
     * @return name of setCreatedBy method
     */
    public JavaSymbolName getSetCreatedByMethodName() {
        return helper.getSetterMethodNameForField(CREATED_BY_FIELD);
    }

    /**
     * @return name of setLastUpdate method
     */
    public JavaSymbolName getSetLastUpdatedMethodName() {
        return helper.getSetterMethodNameForField(LAST_UPDATE_FIELD);
    }

    /**
     * @return name of setLastUpdate method
     */
    public JavaSymbolName getSetLastUpdateByMethodName() {
        return helper.getSetterMethodNameForField(LAST_UPDATED_BY_FIELD);
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    @SuppressWarnings("unused")
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

    /**
     * @return annotation values of metadata
     */
    public JpaAuditAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    /**
     * @return findAllXXX method name
     */
    public JavaSymbolName getFindAllMethodName() {
        return findAllMethodName;
    }

    /**
     * @return findXXX method name
     */
    public JavaSymbolName getFindMethodName() {
        return findMethodName;
    }

    /**
     * @return getXXXRevisions method name
     */
    public JavaSymbolName getGetRevisionsMethodName() {
        return getRevisionsMethodName;
    }

    /**
     * @return findXXXRevisionsByDates method name
     */
    public JavaSymbolName getFindRevisionsByDatesMethodName() {
        return findRevisionsByDatesMethodName;
    }

    /**
     * @return findXXXRevsions method name
     */
    public JavaSymbolName getFindRevisionsMethodName() {
        return findRevisionsMethodName;
    }

    /**
     * @return name of revision type name
     */
    public String getRevisonItemTypeName() {
        return revisonItemTypeName;
    }

    /**
     * @return if entity is abstract class
     */
    public boolean isAbstractEntity() {
        return isAbstract;
    }

    /**
     * @return class to use to store "user" information
     */
    public JavaType getUserType() {
        return userType;
    }

    /**
     * Class which contains generation time metadata information useful for
     * {@link RevisionLogMetadataBuilder}
     * 
     * @author gvNIX Team
     * 
     */
    private class BuildContext implements Context {

        private final ItdBuilderHelper helper;

        private final JpaAuditAnnotationValues annotationValues;
        private final JavaType entity;
        private final String entityName;
        private final String entityPlural;
        private final JavaSymbolName findAllMethodName;
        private final JavaSymbolName findMethodName;
        private final JavaSymbolName getRevisionsMethodName;
        private final JavaSymbolName findRevisionsByDatesMethodName;
        private final JavaSymbolName findRevisionsMethodName;
        private final String revisonItemTypeName;
        private final JavaType entityListType;
        private final JavaType revisonItemType;
        private final String metadataId;
        private final FieldMetadata identifier;
        private final JavaType revisonItemListType;
        private final boolean isAbstractEntity;
        private final JavaType userType;

        public BuildContext(String metadataId, ItdBuilderHelper helper,
                JpaAuditAnnotationValues annotationValues, JavaType entity,
                String entityName, String entityPlural,
                JavaSymbolName findAllMethodName,
                JavaSymbolName findMethodName,
                JavaSymbolName getRevisionsMethodName,
                JavaSymbolName findRevisionsByDatesMethodName,
                JavaSymbolName findRevisionsMethodName,
                String revisonItemTypeName, JavaType entityListType,
                JavaType revisonItemType, FieldMetadata identifier,
                JavaType revisonItemListType, boolean isAbstractEntity,
                JavaType userType) {
            super();
            this.metadataId = metadataId;
            this.helper = helper;
            this.annotationValues = annotationValues;
            this.entity = entity;
            this.entityName = entityName;
            this.entityPlural = entityPlural;
            this.findAllMethodName = findAllMethodName;
            this.findMethodName = findMethodName;
            this.getRevisionsMethodName = getRevisionsMethodName;
            this.findRevisionsByDatesMethodName = findRevisionsByDatesMethodName;
            this.findRevisionsMethodName = findRevisionsMethodName;
            this.revisonItemTypeName = revisonItemTypeName;
            this.entityListType = entityListType;
            this.revisonItemType = revisonItemType;
            this.identifier = identifier;
            this.revisonItemListType = revisonItemListType;
            this.isAbstractEntity = isAbstractEntity;
            this.userType = userType;
        }

        /** {@inheritDoc} */
        @Override
        public ItdBuilderHelper getHelper() {
            return helper;
        }

        /** {@inheritDoc} */
        @Override
        public JpaAuditAnnotationValues getAnnotationValues() {
            return annotationValues;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getEntity() {
            return entity;
        }

        /** {@inheritDoc} */
        @Override
        public String getEntityName() {
            return entityName;
        }

        /** {@inheritDoc} */
        @Override
        public String getEntityPlural() {
            return entityPlural;
        }

        /** {@inheritDoc} */
        @Override
        public JavaSymbolName getFindAllMethodName() {
            return findAllMethodName;
        }

        /** {@inheritDoc} */
        @Override
        public JavaSymbolName getFindMethodName() {
            return findMethodName;
        }

        /** {@inheritDoc} */
        @Override
        public JavaSymbolName getGetRevisionsMethodName() {
            return getRevisionsMethodName;
        }

        /** {@inheritDoc} */
        @Override
        public JavaSymbolName getFindRevisionsByDatesMethodName() {
            return findRevisionsByDatesMethodName;
        }

        /** {@inheritDoc} */
        @Override
        public JavaSymbolName getFindRevisionsMethodName() {
            return findRevisionsMethodName;
        }

        /** {@inheritDoc} */
        @Override
        public String getRevisonItemTypeName() {
            return revisonItemTypeName;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getEntityListType() {
            return entityListType;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getRevisonItemType() {
            return revisonItemType;
        }

        /** {@inheritDoc} */
        @Override
        public String getMetadataId() {
            return metadataId;
        }

        /** {@inheritDoc} */
        @Override
        public JavaSymbolName getGetRevisionNumberForDate() {
            return GET_REVISON_NUMBER_FOR_DATE_METHOD;
        }

        /** {@inheritDoc} */
        @Override
        public FieldMetadata getIdentifier() {
            return identifier;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getRevisonItemListType() {
            return revisonItemListType;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isAbstractEntity() {
            return isAbstractEntity;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getUserType() {
            return userType;
        }
    }
}
