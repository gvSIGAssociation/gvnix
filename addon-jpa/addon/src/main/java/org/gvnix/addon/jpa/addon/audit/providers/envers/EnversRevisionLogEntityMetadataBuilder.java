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
package org.gvnix.addon.jpa.addon.audit.providers.envers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gvnix.addon.jpa.addon.audit.GvNIXJpaAuditRevisionEntity;
import org.gvnix.addon.jpa.addon.audit.JpaAuditUserServiceMetadata;
import org.gvnix.addon.jpa.addon.audit.providers.RevisionLogRevisionEntityMetadataBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.gvnix.support.ItdBuilderHelper.GET_FIELD_EXISTS_ACTION;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.JpaJavaType;

/**
 * ITD builder of {@link EnversRevisionLogEntityMetadataBuilder} for the entity
 * with stores revision identifiers (annotated
 * {@link GvNIXJpaAuditRevisionEntity})
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
public class EnversRevisionLogEntityMetadataBuilder implements
        RevisionLogRevisionEntityMetadataBuilder {

    private static final JavaSymbolName ID_FIELD = new JavaSymbolName("id");
    private static final JavaSymbolName TIMESTAMP_FIELD = new JavaSymbolName(
            "timestamp");
    private static final JavaSymbolName USER_FIELD = new JavaSymbolName(
            "revisonUser");
    private static final JavaSymbolName TO_STRING_METHOD = new JavaSymbolName(
            "toString");

    private static final JavaSymbolName REVISION_DATE_TRANSIENT_FIELD = new JavaSymbolName(
            "revisionDate");

    private static final JavaSymbolName EQUALS_METHOD = new JavaSymbolName(
            "equals");
    private static final JavaSymbolName HASH_CODE_METHOD = new JavaSymbolName(
            "hashCode");
    private static final JavaSymbolName NEW_REVISION_METHOD = new JavaSymbolName(
            "newRevision");

    private static final JavaType TO_STRING_BUILDER = new JavaType(
            "org.apache.commons.lang3.builder.ToStringBuilder");

    private static final JavaType HASH_CODE_BUILDER = new JavaType(
            "org.apache.commons.lang3.builder.HashCodeBuilder");

    private static final JavaType REVISION_ENTITY = new JavaType(
            "org.hibernate.envers.RevisionEntity");

    private static final JavaType REVISION_NUMBER = new JavaType(
            "org.hibernate.envers.RevisionNumber");

    private static final JavaType REVISION_TIMESTAMP = new JavaType(
            "org.hibernate.envers.RevisionTimestamp");

    private static final JavaType REVISION_LISTENER = new JavaType(
            "org.hibernate.envers.RevisionListener");

    private final PhysicalTypeMetadata governorPhysicalTypeMetadata;
    private final ClassOrInterfaceTypeDetails governorTypeDetails;

    private JavaType revisionListenerType;
    private FieldMetadata idField;
    private FieldMetadata timestampField;
    private FieldMetadata userField;
    private MethodMetadata idFieldGetter;
    private MethodMetadata idFieldSetter;
    private MethodMetadata timestampFieldGetter;
    private MethodMetadata timestampFieldSetter;
    private MethodMetadata userNameFieldGetter;
    private MethodMetadata userNameFieldSetter;
    private MethodMetadata revsionDateGetter;

    private ItdTypeDetailsBuilder builder;
    private Context context;
    private ItdBuilderHelper helper;

    public EnversRevisionLogEntityMetadataBuilder(
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
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
    }

    @Override
    public void initialize(ItdTypeDetailsBuilder builder, Context context) {
        if (this.builder != null || idField != null) {
            throw new IllegalStateException();
        }
        this.builder = builder;
        this.context = context;
        this.helper = context.getHelper();
    }

    @Override
    public void done() {
        this.builder = null;
        this.context = null;
        this.helper = null;
    }

    @Override
    public void fillEntity() {

        this.revisionListenerType = new JavaType(governorPhysicalTypeMetadata
                .getType().getFullyQualifiedTypeName()
                .concat(".RevisionLogEntityListener"));

        // Add @Entity if needed
        if (governorTypeDetails.getAnnotation(JpaJavaType.ENTITY) == null) {
            builder.addAnnotation(new AnnotationMetadataBuilder(
                    JpaJavaType.ENTITY));
        }

        // Add @RevisionEntity if needed
        if (governorTypeDetails.getAnnotation(JpaJavaType.ENTITY) == null) {
            Collection<AnnotationAttributeValue<?>> values = new ArrayList<AnnotationAttributeValue<?>>();
            values.add(new ClassAttributeValue(new JavaSymbolName("value"),
                    revisionListenerType));
            builder.addAnnotation(AnnotationMetadataBuilder.getInstance(
                    REVISION_ENTITY, values));
        }

        builder.addField(getIdField());
        builder.addMethod(getIdFieldGetter());
        builder.addMethod(getIdFieldSetter());
        builder.addField(getTimestampField());
        builder.addMethod(getTimestampGetter());
        builder.addMethod(getTimestampSetter());
        builder.addField(getUserField());
        builder.addMethod(getUserGetter());
        builder.addMethod(getUserNameSetter());
        builder.addMethod(getRevisionDate());

        builder.addMethod(getToStringMethod());
        builder.addMethod(getEqualsMethod());
        builder.addMethod(getHashCodeMethod());

        builder.addInnerType(getRevisionListener());

    }

    private MethodMetadata getRevisionDate() {
        if (revsionDateGetter == null) {
            JavaSymbolName methodName = helper
                    .getGetterMethodNameForField(REVISION_DATE_TRANSIENT_FIELD);
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
            List<AnnotationMetadataBuilder> annotations = helper
                    .toAnnotationMetadata(JpaJavaType.TRANSIENT);

            // Define method throws types (none in this case)
            List<JavaType> throwsTypes = new ArrayList<JavaType>();

            // Define method parameter names (none in this case)
            List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>(
                    0);

            // Create the method body
            InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
            // return new Date(this.timestamp);
            body.appendFormalLine(String.format("return new %s(this.%s);",
                    helper.getFinalTypeName(JdkJavaType.DATE), TIMESTAMP_FIELD));

            // Use the MethodMetadataBuilder for easy creation of MethodMetadata
            MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                    context.getMetadataId(), Modifier.PUBLIC, methodName,
                    JdkJavaType.DATE, parameterTypes, parameterNames, body);
            methodBuilder.setAnnotations(annotations);
            methodBuilder.setThrowsTypes(throwsTypes);

            revsionDateGetter = methodBuilder.build(); // Build and return a
                                                       // MethodMetadata

        }
        return revsionDateGetter;
    }

    private MethodMetadata getToStringMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>(
                0);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(TO_STRING_METHOD,
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
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>(0);

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        // return new ToStringBuilder(this).append("id",
        // id).append("revisonDate", getRevisionDate()).append("userName",
        // userName).toString();
        body.appendFormalLine(String
                .format("return new %s(this).append(\"%s\", this.%s).append(\"%s\", this.%s()).append(\"%s\", this.%s).toString();",
                        helper.getFinalTypeName(TO_STRING_BUILDER), ID_FIELD,
                        ID_FIELD, REVISION_DATE_TRANSIENT_FIELD,
                        getRevisionDate().getMethodName(), USER_FIELD,
                        USER_FIELD));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC, TO_STRING_METHOD,
                JavaType.STRING, parameterTypes, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    private ClassOrInterfaceTypeDetails getRevisionListener() {

        // Check class exists
        ClassOrInterfaceTypeDetails innerClass = governorTypeDetails
                .getDeclaredInnerType(revisionListenerType);

        if (innerClass != null) {
            // If class exists (already push-in) we can do nothing
            return innerClass;
        }

        // Create inner class

        ClassOrInterfaceTypeDetailsBuilder classBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                context.getMetadataId(), Modifier.PUBLIC + Modifier.STATIC,
                revisionListenerType, PhysicalTypeCategory.CLASS);
        classBuilder.addImplementsType(REVISION_LISTENER);

        classBuilder.addMethod(getNewRevisionMethod());

        return classBuilder.build();
    }

    private MethodMetadata getNewRevisionMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper
                .toAnnotedJavaType(JavaType.OBJECT);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(TO_STRING_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                0);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper
                .toSymbolName("revisionEntity");

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        buildNewRevisionMethodBody(body);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC, NEW_REVISION_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    private void buildNewRevisionMethodBody(InvocableMemberBodyBuilder body) {
        // HistoryRevisionEntity revision = (HistoryRevisionEntity)
        // revisionEntity;
        body.appendFormalLine(String.format(
                "%s revision = (%s) revisionEntity;", context.getEntity()
                        .getSimpleTypeName(), context.getEntity()
                        .getSimpleTypeName()));

        // revison.setUserNsame(AuditUserService.getUser());
        body.appendFormalLine(String.format("revision.%s(%s.%s());",
                helper.getSetterMethodNameForField(USER_FIELD),
                helper.getFinalTypeName(context.getUserService()),
                JpaAuditUserServiceMetadata.GET_USER_METHOD));

    }

    private MethodMetadata getHashCodeMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>(
                0);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(TO_STRING_METHOD,
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
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>(0);

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        body.appendFormalLine(String.format(
                "return new %s(17, 31).append(%s).append(%s).toHashCode();",
                helper.getFinalTypeName(HASH_CODE_BUILDER), ID_FIELD,
                TIMESTAMP_FIELD));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC, HASH_CODE_METHOD,
                JavaType.INT_PRIMITIVE, parameterTypes, parameterNames, body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    private MethodMetadata getEqualsMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = helper
                .toAnnotedJavaType(JavaType.OBJECT);

        // Check if a method exist in type
        final MethodMetadata method = helper.methodExists(TO_STRING_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                0);

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = helper.toSymbolName("object");

        // Create the method body
        InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
        buildEqualsMethodBody(body);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                context.getMetadataId(), Modifier.PUBLIC, EQUALS_METHOD,
                JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
                body);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
    }

    private void buildEqualsMethodBody(InvocableMemberBodyBuilder body) {
        // if (this == object) {
        body.appendFormalLine("if (this == object) {");
        body.indent();

        // return true;
        body.appendFormalLine("return true;");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (!(object instanceof RevisionEntity)) {
        body.appendFormalLine(String.format("if (!(object instanceof %s)) {",
                context.getEntity()));
        body.indent();

        // return false;
        body.appendFormalLine("return false;");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // HistoryRevisionEntity that = (HistoryRevisionEntity) object;
        body.appendFormalLine(String.format("%s that = (%s) object;",
                context.getEntity(), context.getEntity()));

        // if (id != that.id) {
        body.appendFormalLine(String.format("if (this.%s == that.%s) {",
                ID_FIELD, ID_FIELD));
        body.indent();

        // return false;
        body.appendFormalLine("return false;");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // if (timestamp != that.timestamp) {
        body.appendFormalLine(String.format("if (this.%s == that.%s) {",
                TIMESTAMP_FIELD, TIMESTAMP_FIELD));
        body.indent();

        // return false;
        body.appendFormalLine("return false;");

        // }
        body.indentRemove();
        body.appendFormalLine("}");

        // return true;
        body.appendFormalLine("return true;");
    }

    private MethodMetadata getUserGetter() {
        if (userNameFieldGetter == null) {
            userNameFieldGetter = helper.getGetterMethod(getUserField(), null);
        }
        return userNameFieldGetter;
    }

    private MethodMetadata getUserNameSetter() {
        if (userNameFieldSetter == null) {
            userNameFieldSetter = helper.getSetterMethod(getUserField(), null);
        }
        return userNameFieldSetter;
    }

    private MethodMetadata getTimestampGetter() {
        if (timestampFieldGetter == null) {
            timestampFieldGetter = helper.getGetterMethod(getTimestampField(),
                    null);
        }
        return timestampFieldGetter;
    }

    private MethodMetadata getTimestampSetter() {
        if (timestampFieldSetter == null) {
            timestampFieldSetter = helper.getSetterMethod(getTimestampField(),
                    null);
        }
        return timestampFieldSetter;
    }

    private MethodMetadata getIdFieldGetter() {
        if (idFieldGetter == null) {
            idFieldGetter = helper.getGetterMethod(getIdField(), null);
        }
        return idFieldGetter;
    }

    private MethodMetadata getIdFieldSetter() {
        if (idFieldSetter == null) {
            idFieldSetter = helper.getSetterMethod(getIdField(), null);
        }
        return idFieldSetter;
    }

    private FieldMetadata getUserField() {
        if (userField == null) {
            List<AnnotationMetadataBuilder> annotations = null;
            if (context.getUserTypeIsEntity()) {
                annotations = new ArrayList<AnnotationMetadataBuilder>(1);
                annotations.add(new AnnotationMetadataBuilder(
                        AnnotationMetadataBuilder.JPA_MANY_TO_ONE_ANNOTATION));

            }
            userField = helper
                    .getField(
                            USER_FIELD,
                            Modifier.PRIVATE,
                            context.getUserType(),
                            annotations,
                            GET_FIELD_EXISTS_ACTION.RETURN_EXISTING_IF_ANNOTATION_MATCH);
        }
        return userField;
    }

    private FieldMetadata getTimestampField() {
        if (timestampField == null) {
            List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
            annotations.add(new AnnotationMetadataBuilder(REVISION_TIMESTAMP));
            timestampField = helper
                    .getField(
                            TIMESTAMP_FIELD,
                            Modifier.PRIVATE,
                            JavaType.LONG_OBJECT,
                            annotations,
                            GET_FIELD_EXISTS_ACTION.RETURN_EXISTING_IF_ANNOTATION_MATCH);
        }
        return timestampField;
    }

    /**
     * @param helper
     * @return
     */
    private FieldMetadata getIdField() {
        if (idField == null) {
            List<AnnotationMetadataBuilder> annotations = helper
                    .toAnnotationMetadata(JpaJavaType.ID,
                            JpaJavaType.GENERATED_VALUE, REVISION_NUMBER);
            idField = helper
                    .getField(
                            ID_FIELD,
                            Modifier.PRIVATE,
                            JavaType.LONG_OBJECT,
                            annotations,
                            GET_FIELD_EXISTS_ACTION.RETURN_EXISTING_IF_ANNOTATION_MATCH);
        }
        return idField;
    }

    public JavaSymbolName getRevisionIdGetterName() {
        return getIdFieldGetter().getMethodName();
    }

    public JavaSymbolName getRevisionDateGetterName() {
        return getRevisionDate().getMethodName();
    }

    public JavaSymbolName getRevisonUserGetterName() {
        return getUserGetter().getMethodName();
    }
}
