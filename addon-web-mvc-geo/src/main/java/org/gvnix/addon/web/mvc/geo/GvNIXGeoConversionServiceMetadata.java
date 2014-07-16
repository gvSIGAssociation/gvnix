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
package org.gvnix.addon.web.mvc.geo;

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
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXGeoConversionService} annotation.
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public class GvNIXGeoConversionServiceMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaType PARSE_EXCEPTION_TYPE = new JavaType(
            "com.vividsolutions.jts.io.ParseException");

    private static final JavaType CONVERTER_TYPE = new JavaType(
            "org.springframework.core.convert.converter.Converter");

    private static final JavaType WKTREADER_TYPE = new JavaType(
            "com.vividsolutions.jts.io.WKTReader");

    private static final JavaType WKTWRITER_TYPE = new JavaType(
            "com.vividsolutions.jts.io.WKTWriter");

    private static final JavaType POINT_TYPE = new JavaType(
            "com.vividsolutions.jts.geom.Point");

    private static final JavaSymbolName POINT_TO_STRING_METHOD = new JavaSymbolName(
            "getPointToStringConverter");

    private static final JavaSymbolName STRING_TO_POINT_METHOD = new JavaSymbolName(
            "getStringToPointConverter");

    private static final JavaType CONVERTER_POINT_STRING = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(POINT_TYPE, JavaType.STRING));

    private static final JavaType CONVERTER_STRING_POINT = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, POINT_TYPE));

    /**
     * Itd builder helper
     */
    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXGeoConversionServiceMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public GvNIXGeoConversionServiceMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Creating WKTReader reader = new WKTReader(); field
        builder.addField(getField("reader", "new WKTReader()", WKTREADER_TYPE,
                Modifier.PRIVATE, null));

        // Adding methods
        builder.addMethod(getPointToStringConverterMethod());
        builder.addMethod(getStringToPointConverterMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>getPointToStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getPointToStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POINT_TO_STRING_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetPointToStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POINT_TO_STRING_METHOD,
                CONVERTER_POINT_STRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getStringToPointConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getStringToPointConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(STRING_TO_POINT_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetStringToPointConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, STRING_TO_POINT_METHOD,
                CONVERTER_STRING_POINT, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>getPointToStringConverter</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetPointToStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<%s, java.lang.String>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(POINT_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("public String convert(Point point) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format(
                "return %s.toPoint(point.getCoordinate());",
                helper.getFinalTypeName(WKTWRITER_TYPE)));

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

    }

    /**
     * Builds body method for <code>getStringToPointConverter</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetStringToPointConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<java.lang.String, %s>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(POINT_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("public Point convert(String str) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return (Point) reader.read(str);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(String.format("}catch( %s e) {",
                helper.getFinalTypeName(PARSE_EXCEPTION_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new IllegalArgumentException(");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(" \"Invalid string for point: \".concat(str), e);");

        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

        // Reset indent
        bodyBuilder.reset();

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

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
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
