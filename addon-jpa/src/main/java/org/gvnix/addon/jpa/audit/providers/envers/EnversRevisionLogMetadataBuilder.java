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
package org.gvnix.addon.jpa.audit.providers.envers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gvnix.addon.jpa.audit.GvNIXJpaAudit;
import org.gvnix.addon.jpa.audit.JpaAuditMetadata;
import org.gvnix.addon.jpa.audit.JpaAuditMetadata.Context;
import org.gvnix.addon.jpa.audit.JpaAuditRevisionEntityMetadata;
import org.gvnix.addon.jpa.audit.providers.RevisionLogMetadataBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;

/**
 * ITD builder of {@link EnversRevisionLogProvider} for audited entities
 * (annotated with {@link GvNIXJpaAudit})
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
public class EnversRevisionLogMetadataBuilder implements
        RevisionLogMetadataBuilder {

    // Defined names
    private static final JavaSymbolName AUDIT_READER_STATIC_METHOD = new JavaSymbolName(
            "auditReader");
    private static final JavaSymbolName AUDIT_READER_METHOD = new JavaSymbolName(
            "getAuditReader");
    private static final JavaSymbolName GET_PROPERTY_METHOD = new JavaSymbolName(
            "getProperty");
    private static final JavaSymbolName CREATE_ITEM_LIST_METHOD = new JavaSymbolName(
            "createList");
    private static final JavaSymbolName REV_ITEM_REVISON_ENTITY_FIELD = new JavaSymbolName(
            "revsionEntity");
    private static final JavaSymbolName REV_ITEM_REVISON_TYPE_FIELD = new JavaSymbolName(
            "revsionType");

    // Defined java types
    private static final JavaType AUDITED_ANNOTATION = new JavaType(
            "org.hibernate.envers.Audited");
    private static final JavaType AUDIT_READER = new JavaType(
            "org.hibernate.envers.AuditReader");
    private static final JavaType AUDIT_READER_FACTORY = new JavaType(
            "org.hibernate.envers.AuditReaderFactory");
    private static final JavaType REVISON_TYPE = new JavaType(
            "org.hibernate.envers.RevisionType");
    private static final JavaType AUDIT_ENTITY = new JavaType(
            "org.hibernate.envers.query.AuditEntity");
    private static final JavaType AUDIT_QUERY = new JavaType(
            "org.hibernate.envers.query.AuditQuery");
    private static final JavaType AUDIT_QUERY_CREATOR = new JavaType(
            "org.hibernate.envers.query.AuditQueryCreator");
    private static final JavaType AUDIT_CONJUNTION = new JavaType(
            "org.hibernate.envers.query.criteria.AuditConjunction");
    private static final JavaType AUDIT_PROPERTY = new JavaType(
            "org.hibernate.envers.query.criteria.AuditProperty");
    private static final JavaType AUDIT_PROPERTY_GENERIC = new JavaType(
            AUDIT_PROPERTY.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            null);
    private static final JavaType AUDIT_PROPERTY_OBJECT = new JavaType(
            AUDIT_PROPERTY.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.OBJECT));
    private static final JavaType MAP_STRING_OBJECT = new JavaType(
            JdkJavaType.MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(JavaType.STRING, JavaType.OBJECT));
    private static final JavaType ENTRY_STRING_OBJECT = new JavaType(
            JdkJavaType.MAP.getFullyQualifiedTypeName().concat(".Entry"), 0,
            DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));
    private static final JavaType HASHMAP = new JavaType(HashMap.class);
    private static final JavaType HASHMAP_STRING_OBJECT = new JavaType(
            HASHMAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));
    private static final JavaType OBJECT_ARRAY = new JavaType(
            JavaType.OBJECT.getFullyQualifiedTypeName(), 1, DataType.TYPE,
            null, null);
    private static final JavaType LIST_OBJECT_ARRAY = new JavaType(
            JdkJavaType.LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(OBJECT_ARRAY));
    private static final JavaType ARRAYLIST = new JavaType(ArrayList.class);
    private static final JavaType AUDIT_ORDER = new JavaType(
            "org.hibernate.envers.query.order.AuditOrder");
    private static final JavaType STRING_UTILS = new JavaType(
            "org.apache.commons.lang3.StringUtils");
    private static final JavaType BEAN_WRAPPER_IMPL = new JavaType(
            "org.springframework.beans.BeanWrapperImpl");
    private static final JavaType COLLECTIONS = new JavaType(Collections.class);

    // local properties
    private final PhysicalTypeMetadata governorPhysicalTypeMetadata;

    private ClassOrInterfaceTypeDetails governorTypeDetails;

    private JavaSymbolName revisionItemFielName;

    private final JpaAuditRevisionEntityMetadata revisionEntityMetadata;

    private final EnversRevisionLogEntityMetadataBuilder revisionEntityBuilder;

    private Context context;
    private ItdBuilderHelper helper;

    public EnversRevisionLogMetadataBuilder(
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaAuditRevisionEntityMetadata revisionEntityMetada) {
        this.governorPhysicalTypeMetadata = governorPhysicalTypeMetadata;
        final Object physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        if (physicalTypeDetails instanceof ClassOrInterfaceTypeDetails) {
            // We have reliable physical type details
            governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }
        else {
            throw new IllegalArgumentException(
                    "Invalid governorPhysicalTypeMetadata");
        }

        this.revisionEntityMetadata = revisionEntityMetada;
        this.revisionEntityBuilder = (EnversRevisionLogEntityMetadataBuilder) revisionEntityMetada
                .getRevisionLogBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Context context) {
        this.context = context;
        this.helper = context.getHelper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void done() {
        this.context = null;
        this.helper = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyFindAllFromDate(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        // return findAllVisit(getRevisionNumberForDate(atDate));
        body.appendFormalLine(String.format("return %s(%s(%s));",
                context.getFindAllMethodName(),
                context.getGetRevisionNumberForDate(), parameterNames.get(0)));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyFindAllFromRevision(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        // return auditReader().createQuery().forEntitiesAtRevision(Visit.class,
        // revision).getResultList();
        body.appendFormalLine(String
                .format("return %s().createQuery().forEntitiesAtRevision(%s.class, %s).getResultList();",
                        AUDIT_READER_STATIC_METHOD,
                        StringUtils.capitalize(context.getEntityName()),
                        parameterNames.get(0)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCustomArtifact(ItdTypeDetailsBuilder builder) {

        // Add @Audit if needed (includin in abstract class)
        if (governorTypeDetails.getAnnotation(AUDITED_ANNOTATION) == null) {
            builder.addAnnotation(new AnnotationMetadataBuilder(
                    AUDITED_ANNOTATION));
        }

        if (!context.isAbstractEntity()) {
            builder.addMethod(getAuditReaderStaticMethod());
            builder.addMethod(getAuditReaderMethod());
            builder.addMethod(getPropertyMethod());
            builder.addMethod(getPropertyMethodWithWrapper());
        }

    }

    /**
     * @return gets or creates getProperty(name) method
     */
    private MethodMetadata getPropertyMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper
                .toAnnotedJavaType(JavaType.STRING);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(GET_PROPERTY_METHOD,
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
        List<JavaSymbolName> parameterNames = helper.toSymbolName("name");

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        // return getProperty(name, new BeanWrapperImpl(Visit.class));
        body.appendFormalLine(String.format(
                "return %s(name, new %s(%s.class));", GET_PROPERTY_METHOD,
                helper.getFinalTypeName(BEAN_WRAPPER_IMPL),
                StringUtils.capitalize(context.getEntityName())));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC + Modifier.STATIC,
                GET_PROPERTY_METHOD, AUDIT_PROPERTY_GENERIC, parameterTypes,
                parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return gets or creates getProperty(name,beanWrapper)
     */
    private MethodMetadata getPropertyMethodWithWrapper() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                JavaType.STRING, BEAN_WRAPPER_IMPL);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(GET_PROPERTY_METHOD,
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
        List<JavaSymbolName> parameterNames = helper.toSymbolName("name",
                "beanWrapper");

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        buildGetPropertyMethodWithWrapper(body, helper, context.getEntity());

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC + Modifier.STATIC,
                GET_PROPERTY_METHOD, AUDIT_PROPERTY_GENERIC, parameterTypes,
                parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * Builds method body (implementation) of getProperty(name,beanWrapper)
     * 
     * @param body
     * @param helper
     */
    private void buildGetPropertyMethodWithWrapper(
            InvocableMemberBodyBuilder body, ItdBuilderHelper helper,
            JavaType entity) {

        // String upperCase = name.toUpperCase();
        body.appendFormalLine("String upperCase = name.toUpperCase();");

        // if ("$ID$".equals(upperCase)) {
        body.appendFormalLine("if (\"$ID$\".equals(upperCase)) {");
        body.indent();

        // return AuditEntity.id();
        body.appendFormalLine(String.format("return %s.id();",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // } else if ("$REV$".equals(upperCase)) {
        body.indentRemove();
        body.appendFormalLine("} else if (\"$REV$\".equals(upperCase)) {");
        body.indent();

        // return AuditEntity.revisionNumber();
        body.appendFormalLine(String.format("return %s.revisionNumber();",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // } else if ("$REV_TYPE$".equals(upperCase)) {
        body.indentRemove();
        body.appendFormalLine("} else if (\"$REV_TYPE$\".equals(upperCase)) {");
        body.indent();

        // return AuditEntity.revisionNumber();
        body.appendFormalLine(String.format("return %s.revisionType();",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // } else if ("$REV_USER$".equals(upperCase)) {
        body.indentRemove();
        body.appendFormalLine("} else if (\"$REV_USER$\".equals(upperCase)) {");
        body.indent();

        // return AuditEntity.revisionProperty("userName");
        body.appendFormalLine(String.format(
                "return %s.revisionProperty(\"userName\");",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // } else {
        body.indentRemove();
        body.appendFormalLine("} else {");
        body.indent();

        // if (name.contains(".")) {
        body.appendFormalLine("if (name.contains(\".\")) {");
        body.indent();

        // throw new
        // IllegalArgumentException("Related object property not supported: ".concat(name));
        body.appendFormalLine("throw new IllegalArgumentException(\"Related object property not supported: \".concat(name));");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (beanWrapper.isReadableProperty(name)) {
        body.appendFormalLine("if (beanWrapper.isReadableProperty(name)) {");
        body.indent();

        // return AuditEntity.property(name);
        body.appendFormalLine(String.format("return %s.property(name);",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // throw new
        // IllegalArgumentException("Property not found on: ".concat(name));
        body.appendFormalLine(String
                .format("throw new IllegalArgumentException(\"Property not found on %s: \".concat(name));",
                        entity));

    }

    /**
     * @return creates auditReader() method
     */
    private MethodMetadata getAuditReaderMethod() {
        return commonGetAuditReaderStaticMethod(context, AUDIT_READER_METHOD,
                Modifier.PUBLIC, "return %s.get(entityManager);");
    }

    /**
     * @return creates getAuditReader() method
     */
    private MethodMetadata getAuditReaderStaticMethod() {
        return commonGetAuditReaderStaticMethod(context,
                AUDIT_READER_STATIC_METHOD, Modifier.PUBLIC + Modifier.STATIC,
                "return %s.get(entityManager());");
    }

    /**
     * Generates a method on itd without parameters, annotation or throws
     * declarations
     * 
     * @param context
     * @param methodName
     * @param modifiers
     * @param bodyStr
     * @return
     */
    private MethodMetadata commonGetAuditReaderStaticMethod(Context context,
            JavaSymbolName methodName, int modifiers, String bodyStr) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>(
                0);

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
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();

        // return AuditReaderFactory.get(entityManager());
        body.appendFormalLine(String.format(bodyStr,
                helper.getFinalTypeName(AUDIT_READER_FACTORY)));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), modifiers, methodName, AUDIT_READER,
                parameterTypes, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyGetRevisionNumberForDate(
            InvocableMemberBodyBuilder body, List<JavaSymbolName> parameterNames) {
        // return (Long) auditReader().getRevisionNumberForDate(aDate);
        body.appendFormalLine(String.format(
                "return (Long) %s().getRevisionNumberForDate(%s);",
                AUDIT_READER_STATIC_METHOD, parameterNames.get(0)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyFindFromDate(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        // AuditReader reader = auditReader();
        body.appendFormalLine(String.format("%s reader = %s();",
                helper.getFinalTypeName(AUDIT_READER),
                AUDIT_READER_STATIC_METHOD));

        // Long revision = (Long) reader.getRevisionNumberForDate(atDate);
        body.appendFormalLine(String.format(
                "%s revision = (Long) reader.getRevisionNumberForDate(%s);",
                helper.getFinalTypeName(JavaType.LONG_OBJECT),
                parameterNames.get(1)));

        // if (revision == null) {
        body.appendFormalLine("if (revision == null) {");
        body.indent();

        // return null;
        body.appendFormalLine("return null;");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // return reader.find(Visit.class, id, revision);
        body.appendFormalLine(String.format(
                "return reader.find(%s.class, %s, revision);",
                StringUtils.capitalize(context.getEntityName()),
                parameterNames.get(0)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyFindFromRevision(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        // AuditReader reader = auditReader();
        body.appendFormalLine(String.format("%s reader = %s();",
                helper.getFinalTypeName(AUDIT_READER),
                AUDIT_READER_STATIC_METHOD));
        // return reader.find(Visit.class, id, revision);
        body.appendFormalLine(String.format(
                "return reader.find(%s.class, %s, %s);",
                StringUtils.capitalize(context.getEntityName()),
                parameterNames.get(0), parameterNames.get(1)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyGetRevisions(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {

        // Map<String, Object> criteria = new HashMap<String, Object>(1);
        body.appendFormalLine(String.format("%s criteria = new %s(1);",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));

        // criteria.put("$ID$", id);
        body.appendFormalLine(String.format("criteria.put(\"$ID$\", %s);",
                parameterNames.get(0)));

        // return findVisitRevisionByDates(fromDate, toDate, criteria, null,
        // start, limit);
        body.appendFormalLine(String.format(
                "return %s(%s, %s, criteria, null, %s, %s);",
                context.getFindRevisionsByDatesMethodName(),
                parameterNames.get(1), parameterNames.get(2),
                parameterNames.get(3), parameterNames.get(4)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyGetRevisionsInstance(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        JavaSymbolName identifierGetter = helper
                .getGetterMethodNameForField(context.getIdentifier()
                        .getFieldName());

        JavaType arrayListRevisionItems = new JavaType(
                JdkJavaType.ARRAY_LIST.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null,
                Arrays.asList(context.getRevisonItemType()));

        // if (getId() == null) {
        body.appendFormalLine(String.format("if (%s() == null) {",
                identifierGetter));
        body.indent();

        // // No persistent visit --> No history
        body.appendFormalLine("// No persistent visit --> No history");

        // return Collections.unmodifiableList(new ArrayList<VisitRevision>(0));
        body.appendFormalLine(String.format(
                "return %s.unmodifiableList(new %s(0));",
                helper.getFinalTypeName(COLLECTIONS),
                helper.getFinalTypeName(arrayListRevisionItems)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // return getVisitRevisions(getId(), fromDate, toDate, start, limit);
        body.appendFormalLine(String.format(
                "return %s(%s(), fromDate, toDate, start, limit);",
                context.getGetRevisionsMethodName(), identifierGetter,
                parameterNames.get(0), parameterNames.get(1),
                parameterNames.get(2), parameterNames.get(3),
                helper.getFinalTypeName(arrayListRevisionItems)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyFindRevisionByDates(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        // if (fromDate != null && toDate != null) {
        body.appendFormalLine(String.format("if (%s != null && %s != null) {",
                parameterNames.get(0), parameterNames.get(1)));
        body.indent();

        // if (toDate.before(fromDate)) {
        body.appendFormalLine(String.format("if (%s.before(%s)) {",
                parameterNames.get(1), parameterNames.get(0)));
        body.indent();

        // throw new
        // IllegalArgumentException("fromDate cannot be lower than toDate");
        body.appendFormalLine(String
                .format("throw new IllegalArgumentException(\"%s cannot be lower than %s\");",
                        parameterNames.get(0), parameterNames.get(1)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // Long fromRevision = null;
        body.appendFormalLine("Long fromRevision = null;");
        // Long toRevision = null;
        body.appendFormalLine("Long toRevision = null;");

        // if (fromDate != null) {
        body.appendFormalLine(String.format("if (%s != null) {",
                parameterNames.get(0)));
        body.indent();

        // fromRevision = getRevisionNumberForDate(fromDate);
        body.appendFormalLine(String.format("fromRevision = %s(%s);",
                context.getGetRevisionNumberForDate(), parameterNames.get(0)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (toDate != null) {
        body.appendFormalLine(String.format("if (%s != null) {",
                parameterNames.get(1)));
        body.indent();

        // toRevision = getRevisionNumberForDate(fromDate);
        body.appendFormalLine(String.format("toRevision = %s(%s);",
                context.getGetRevisionNumberForDate(), parameterNames.get(1)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // return
        // findVisitRevisions(fromRevision,toRevision,filterMap,order,start,limit);
        body.appendFormalLine(String.format(
                "return %s(fromRevision,toRevision,%s,%s,%s,%s);",
                context.getFindRevisionsMethodName(), parameterNames.get(2),
                parameterNames.get(3), parameterNames.get(4),
                parameterNames.get(5)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyFindRevision(InvocableMemberBodyBuilder body,
            List<JavaSymbolName> parameterNames) {
        // AuditReader reader = auditReader();
        body.appendFormalLine(String.format("%s reader = %s();",
                helper.getFinalTypeName(AUDIT_READER),
                AUDIT_READER_STATIC_METHOD));

        // BeanWrapperImpl beanWrapper = new BeanWrapperImpl(Visit.class);
        String capitalizeEntityName = StringUtils.capitalize(context
                .getEntityName());
        body.appendFormalLine(String.format(
                "%s beanWrapper = new %s(%s.class);",
                helper.getFinalTypeName(BEAN_WRAPPER_IMPL),
                helper.getFinalTypeName(BEAN_WRAPPER_IMPL),
                capitalizeEntityName));

        body.appendFormalLine("");
        body.appendFormalLine("// Get revisions list");

        // AuditQuery query =
        // reader.createQuery().forRevisionsOfEntity(Visit.class, false, true);
        body.appendFormalLine(String
                .format("%s query = reader.createQuery().forRevisionsOfEntity(%s.class, false, true);",
                        helper.getFinalTypeName(AUDIT_QUERY),
                        capitalizeEntityName));

        // if (fromRevision != null && toRevision != null) {
        body.appendFormalLine(String.format("if (%s != null && %s != null) {",
                parameterNames.get(0), parameterNames.get(1)));
        body.indent();

        // query.add(AuditEntity.revisionNumber().between(fromRevision,
        // toRevision));
        body.appendFormalLine(String.format(
                "query.add(%s.revisionNumber().between(%s, %s));",
                helper.getFinalTypeName(AUDIT_ENTITY), parameterNames.get(0),
                parameterNames.get(1)));

        // } else if (fromRevision != null) {
        body.indentRemove();
        body.appendFormalLine(String.format("} else if (%s != null) {",
                parameterNames.get(0)));
        body.indent();

        // query.add(AuditEntity.revisionNumber().ge(fromRevision));
        body.appendFormalLine(String.format(
                "query.add(%s.revisionNumber().ge(%s));",
                helper.getFinalTypeName(AUDIT_ENTITY), parameterNames.get(0)));

        // } else if (toRevision != null) {
        body.indentRemove();
        body.appendFormalLine(String.format("} else if (%s != null) {",
                parameterNames.get(1)));
        body.indent();

        // query.add(AuditEntity.revisionNumber().le(toRevision));
        body.appendFormalLine(String.format(
                "query.add(%s.revisionNumber().le(%s));",
                helper.getFinalTypeName(AUDIT_ENTITY), parameterNames.get(1)));
        // } (else if)
        body.indentRemove();
        body.appendFormalLine("}");

        body.appendFormalLine("");

        // AuditConjunction criteria = null;
        body.appendFormalLine(String.format("%s criteria = null;",
                helper.getFinalTypeName(AUDIT_CONJUNTION)));

        body.appendFormalLine("");

        // if (filterMap != null && !filterMap.isEmpty()) {
        body.appendFormalLine(String.format(
                "if (%s != null && !%s.isEmpty()) {", parameterNames.get(2),
                parameterNames.get(2)));
        body.indent();

        // criteria = AuditEntity.conjunction();
        body.appendFormalLine(String.format("criteria = %s.conjunction();",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // for (Entry<String, Object> entry : filterMap.entrySet()) {
        body.appendFormalLine(String.format("for (%s entry : %s.entrySet()) {",
                helper.getFinalTypeName(ENTRY_STRING_OBJECT),
                parameterNames.get(2)));
        body.indent();

        // criteria.add(((AuditProperty<Object>)
        // getProperty(entry.getKey(),beanWrapper)).eq(entry.getValue()));
        body.appendFormalLine(String
                .format("criteria.add(((%s) %s(entry.getKey(),beanWrapper)).eq(entry.getValue()));",
                        helper.getFinalTypeName(AUDIT_PROPERTY_OBJECT),
                        GET_PROPERTY_METHOD));
        // } (for)
        body.indentRemove();
        body.appendFormalLine("}");

        // query.add(criteria);
        body.appendFormalLine("query.add(criteria);");

        // } (if)
        body.indentRemove();
        body.appendFormalLine("}");

        body.appendFormalLine("");
        // Prepare order
        body.appendFormalLine("// Prepare order");
        // AuditProperty<?> orderProp;
        body.appendFormalLine(String.format("%s<?> orderProp;",
                helper.getFinalTypeName(AUDIT_PROPERTY)));
        // AuditOrder orderObj;
        body.appendFormalLine(String.format("%s orderObj;",
                helper.getFinalTypeName(AUDIT_ORDER)));

        // if (order != null && !order.isEmpty()) {
        body.appendFormalLine(String.format(
                "if (%s != null && !%s.isEmpty()) {", parameterNames.get(3),
                parameterNames.get(3)));
        body.indent();

        // for (String orderStr : %s) {
        body.appendFormalLine(String.format("for (String orderStr : order) {",
                parameterNames.get(3)));
        body.indent();

        // if (StringUtils.contains(orderStr, "|")) {
        body.appendFormalLine(String.format(
                "if (%s.contains(orderStr, \"|\")) {",
                helper.getFinalTypeName(STRING_UTILS)));
        body.indent();

        // orderProp = getProperty(StringUtils.substringBefore(orderStr,
        // "|"),beanWrapper);
        body.appendFormalLine(String
                .format("orderProp = %s(%s.substringBefore(orderStr, \"|\"),beanWrapper);",
                        GET_PROPERTY_METHOD,
                        helper.getFinalTypeName(STRING_UTILS)));

        // if ("ASC".equals(StringUtils.substringAfter(orderStr,
        // "|").toUpperCase())) {
        body.appendFormalLine(String
                .format("if (\"ASC\".equals(%s.substringAfter(orderStr, \"|\").toUpperCase())) {",
                        helper.getFinalTypeName(STRING_UTILS)));
        body.indent();

        // orderObj = orderProp.asc();
        body.appendFormalLine("orderObj = orderProp.asc();");

        // } else if ("DESC".equals(StringUtils.substringAfter(orderStr,
        // "|").toUpperCase())) {
        body.indentRemove();
        body.appendFormalLine(String
                .format("} else if (\"DESC\".equals(%s.substringAfter(orderStr, \"|\").toUpperCase())) {",
                        helper.getFinalTypeName(STRING_UTILS)));
        body.indent();

        // orderObj = orderProp.desc();
        body.appendFormalLine("orderObj = orderProp.desc();");

        // } else {
        body.indentRemove();
        body.appendFormalLine("} else {");
        body.indent();

        // throw new
        // IllegalArgumentException("Invalid order '".concat(orderStr).concat("'"));
        body.appendFormalLine("throw new IllegalArgumentException(\"Invalid order '\".concat(orderStr).concat(\"'\"));");

        // } (if ("ASC".equals...
        body.indentRemove();
        body.appendFormalLine("}");

        // } else { [if (SringUtils.contains...
        body.indentRemove();
        body.appendFormalLine("} else {");
        body.indent();

        // orderObj = AuditEntity.property(orderStr).asc();
        body.appendFormalLine(String.format(
                "orderObj = %s.property(orderStr).asc();",
                helper.getFinalTypeName(AUDIT_ENTITY)));

        // } (if [if (SringUtils.contains...
        body.indentRemove();
        body.appendFormalLine("}");

        // query.addOrder(orderObj);
        body.appendFormalLine("query.addOrder(orderObj);");

        // } (for
        body.indentRemove();
        body.appendFormalLine("}");

        // } (if order != null..
        body.indentRemove();
        body.appendFormalLine("}");

        body.appendFormalLine("");
        // // start modifier
        body.appendFormalLine("// start modifier");

        // if (start != null) {
        body.appendFormalLine(String.format("if (%s != null) {",
                parameterNames.get(4)));
        body.indent();

        // query.setFirstResult(start);
        body.appendFormalLine(String.format("query.setFirstResult(%s);",
                parameterNames.get(4)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (limit != null) {
        body.appendFormalLine(String.format("if (%s != null) {",
                parameterNames.get(5)));
        body.indent();

        // query.setMaxResults(limit);
        body.appendFormalLine(String.format("query.setMaxResults(%s);",
                parameterNames.get(5)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // List<Object[]> revisions = query.getResultList();
        body.appendFormalLine(String.format(
                "%s revisions = query.getResultList();",
                helper.getFinalTypeName(LIST_OBJECT_ARRAY)));

        // return VisitRevision.createList(revisions, reader);
        body.appendFormalLine(String.format("return %s.%s(revisions, reader);",
                context.getRevisonItemTypeName(), CREATE_ITEM_LIST_METHOD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCustomArtifactToRevisionItem(
            ClassOrInterfaceTypeDetailsBuilder classBuilder) {

        classBuilder.addField(createRevisionItemField());
        classBuilder.addField(createRevisionEntityRevisionField());
        classBuilder.addField(createRevisionTypeField());
        classBuilder.addConstructor(createRevisionItemConstructor());
        classBuilder.addMethod(createRevisionItemCreateList());
    }

    /**
     * @return Creates constructor for XXRevsion class
     */
    private ConstructorMetadata createRevisionItemConstructor() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>(
                2);
        parameterTypes.add(new AnnotatedJavaType(context.getEntity()));
        parameterTypes.add(new AnnotatedJavaType(revisionEntityMetadata
                .getType()));
        parameterTypes.add(new AnnotatedJavaType(REVISON_TYPE));

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>(3);
        parameterNames.add(new JavaSymbolName(context.getEntityName()));
        parameterNames.add(REV_ITEM_REVISON_ENTITY_FIELD);
        parameterNames.add(REV_ITEM_REVISON_TYPE_FIELD);

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();

        helper.buildSetterMethodBody(body, parameterNames.get(0));
        helper.buildSetterMethodBody(body, parameterNames.get(1));
        helper.buildSetterMethodBody(body, parameterNames.get(2));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        ConstructorMetadataBuilder builder = new ConstructorMetadataBuilder(
                context.getMetadataId());

        builder.setParameterTypes(parameterTypes);
        builder.setParameterNames(parameterNames);
        builder.setModifier(Modifier.PUBLIC);
        builder.setAnnotations(annotations);
        builder.setThrowsTypes(throwsTypes);
        builder.setBodyBuilder(body);

        return builder.build(); // Build and return a MethodMetadata
    }

    /**
     * @return Creates XXXRevision.createList(list,reader) static method
     */
    private MethodMetadata createRevisionItemCreateList() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper.toAnnotedJavaType(
                LIST_OBJECT_ARRAY, AUDIT_READER);

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("list",
                "reader");

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        buildRevisionItemCreateListMethodBody(body, parameterNames);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC + Modifier.STATIC,
                CREATE_ITEM_LIST_METHOD, context.getRevisonItemListType(),
                parameterTypes, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    /**
     * Builds method body (implementation) of
     * XXXRevision.createList(list,reader)
     * 
     * @param body
     * @param context
     * @param parameterNames
     */
    private void buildRevisionItemCreateListMethodBody(
            InvocableMemberBodyBuilder body, List<JavaSymbolName> parameterNames) {
        JavaType arrayListRevison = new JavaType(
                ARRAYLIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(context.getRevisonItemType()));

        // ArrayList<VisitRevision> newList = new
        // ArrayList<VisitRevision>(list.size());
        body.appendFormalLine(String.format("%s newList = new %s(%s.size());",
                helper.getFinalTypeName(arrayListRevison),
                helper.getFinalTypeName(arrayListRevison),
                parameterNames.get(0)));

        // for (Object[] item : list) {
        body.appendFormalLine(String.format("for (%s item : %s) {",
                helper.getFinalTypeName(OBJECT_ARRAY), parameterNames.get(0)));
        body.indent();

        // newList.add(new VisitRevision((Visit) item[0],
        // (HistoryRevisionEntity) item[1], (RevisionType) item[2]));
        body.appendFormalLine(String
                .format("newList.add(new %s((%s) item[0], (%s) item[1], (%s) item[2]));",
                        helper.getFinalTypeName(context.getRevisonItemType()),
                        StringUtils.capitalize(context.getEntityName()), helper
                                .getFinalTypeName(revisionEntityMetadata
                                        .getType()), helper
                                .getFinalTypeName(REVISON_TYPE)));

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // return Collections.unmodifiableList(newList);
        body.appendFormalLine(String.format(
                "return %s.unmodifiableList(newList);",
                helper.getFinalTypeName(COLLECTIONS)));

    }

    /**
     * @return creates XXXRevsion.item field
     */
    private FieldMetadata createRevisionItemField() {
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                0);
        return new FieldMetadataBuilder(context.getMetadataId(),
                Modifier.PRIVATE, annotations, getRevisionItemFieldName(),
                context.getEntity()).build();
    }

    /**
     * @return creates XXXRevision.revision field
     */
    private FieldMetadata createRevisionEntityRevisionField() {
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                0);
        return new FieldMetadataBuilder(context.getMetadataId(),
                Modifier.PRIVATE, annotations, REV_ITEM_REVISON_ENTITY_FIELD,
                revisionEntityMetadata.getType()).build();
    }

    /**
     * @return creates XXXRevsion.type field
     */
    private FieldMetadata createRevisionTypeField() {
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                0);
        return new FieldMetadataBuilder(context.getMetadataId(),
                Modifier.PRIVATE, annotations, REV_ITEM_REVISON_TYPE_FIELD,
                REVISON_TYPE).build();
    }

    /**
     * @param context
     * @return XXXRevision.item field name
     */
    private JavaSymbolName getRevisionItemFieldName() {
        if (revisionItemFielName == null) {
            revisionItemFielName = new JavaSymbolName(
                    StringUtils.uncapitalize(context.getEntityName()));
        }
        return revisionItemFielName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemGetItem(InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return %s;",
                getRevisionItemFieldName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemGetRevisionNumber(
            InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return this.%s.%s();",
                REV_ITEM_REVISON_ENTITY_FIELD,
                revisionEntityBuilder.getRevisionIdGetterName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemGetRevisionDate(
            InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return this.%s.%s();",
                REV_ITEM_REVISON_ENTITY_FIELD,
                revisionEntityBuilder.getRevisionDateGetterName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemGetRevisionUser(
            InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return this.%s.%s();",
                REV_ITEM_REVISON_ENTITY_FIELD,
                revisionEntityBuilder.getRevisonUserGetterName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemIsCreate(InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return this.%s == %s.ADD;",
                REV_ITEM_REVISON_TYPE_FIELD, context.getHelper()
                        .getFinalTypeName(REVISON_TYPE)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemIsUpdate(InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return this.%s == %s.MOD;",
                REV_ITEM_REVISON_TYPE_FIELD, context.getHelper()
                        .getFinalTypeName(REVISON_TYPE)));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemIsDelete(InvocableMemberBodyBuilder body) {
        body.appendFormalLine(String.format("return this.%s == %s.DEL;",
                REV_ITEM_REVISON_TYPE_FIELD, context.getHelper()
                        .getFinalTypeName(REVISON_TYPE)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildBodyRevisionItemGetType(InvocableMemberBodyBuilder body) {
        // if (isCreate()) {
        body.appendFormalLine(String.format("if (%s()) {",
                JpaAuditMetadata.REV_ITEM_IS_CREATE_METHOD));
        body.indent();

        // return "CREATE";
        body.appendFormalLine("return \"CREATE\";");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (isUpdate()) {
        body.appendFormalLine(String.format("if (%s()) {",
                JpaAuditMetadata.REV_ITEM_IS_UPDATE_METHOD));
        body.indent();

        // return "UPDATE";
        body.appendFormalLine("return \"UPDATE\";");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (isDelete()) {
        body.appendFormalLine(String.format("if (%s()) {",
                JpaAuditMetadata.REV_ITEM_IS_DELETE_METHOD));
        body.indent();

        // return "DELETE";
        body.appendFormalLine("return \"DELETE\";");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // return null;
        body.appendFormalLine("return null;");
    }
}
