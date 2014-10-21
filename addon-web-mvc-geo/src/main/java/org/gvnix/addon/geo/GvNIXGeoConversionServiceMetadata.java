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
package org.gvnix.addon.geo;

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

    private static final JavaType LINESTRING_TYPE = new JavaType(
            "com.vividsolutions.jts.geom.LineString");

    private static final JavaType POLYGON_TYPE = new JavaType(
            "com.vividsolutions.jts.geom.Polygon");

    private static final JavaType GEOMETRY_TYPE = new JavaType(
            "com.vividsolutions.jts.geom.Geometry");

    private static final JavaType MULTILINESTRING_TYPE = new JavaType(
            "com.vividsolutions.jts.geom.MultiLineString");

    private static final JavaSymbolName POINT_TO_STRING_METHOD = new JavaSymbolName(
            "getPointToStringConverter");

    private static final JavaSymbolName STRING_TO_POINT_METHOD = new JavaSymbolName(
            "getStringToPointConverter");

    private static final JavaSymbolName LINESTRING_TO_STRING_METHOD = new JavaSymbolName(
            "getLineStringToStringConverter");

    private static final JavaSymbolName STRING_TO_LINESTRING_METHOD = new JavaSymbolName(
            "getStringToLineStringConverter");

    private static final JavaSymbolName POLYGON_TO_STRING_METHOD = new JavaSymbolName(
            "getPolygonToStringConverter");

    private static final JavaSymbolName STRING_TO_POLYGON_METHOD = new JavaSymbolName(
            "getStringToPolygonConverter");

    private static final JavaSymbolName GEOMETRY_TO_STRING_METHOD = new JavaSymbolName(
            "getGeometryToStringConverter");

    private static final JavaSymbolName STRING_TO_GEOMETRY_METHOD = new JavaSymbolName(
            "getStringToGeometryConverter");

    private static final JavaSymbolName MULTLINESTR_TO_STR_MET = new JavaSymbolName(
            "getMultiLineStringToStringConverter");

    private static final JavaSymbolName STR_TO_MULTSTR_MET = new JavaSymbolName(
            "getStringToMultiLineStringConverter");

    private static final JavaSymbolName INSTALL_GEO_LABLES_METHOD = new JavaSymbolName(
            "installGeoLabelConverters");

    private static final JavaSymbolName AFTER_PROPERTY_SET = new JavaSymbolName(
            "afterPropertiesSet");

    private static final JavaType CONVERTER_POINT_STRING = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(POINT_TYPE, JavaType.STRING));

    private static final JavaType CONVERTER_STRING_POINT = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, POINT_TYPE));

    private static final JavaType CONVERTER_LINESTRING_STRING = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(LINESTRING_TYPE, JavaType.STRING));

    private static final JavaType CONVERTER_STRING_LINESTRING = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, LINESTRING_TYPE));

    private static final JavaType CONVERTER_POLYGON_STRING = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(POLYGON_TYPE, JavaType.STRING));

    private static final JavaType CONVERTER_STRING_POLYGON = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, POLYGON_TYPE));

    private static final JavaType CONVERTER_GEOMETRY_STRING = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(GEOMETRY_TYPE, JavaType.STRING));

    private static final JavaType CONVERTER_STRING_GEOMETRY = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, GEOMETRY_TYPE));

    private static final JavaType CONV_MULTSTR_STR = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MULTILINESTRING_TYPE, JavaType.STRING));

    private static final JavaType CONV_STR_MULTSTR = new JavaType(
            CONVERTER_TYPE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, MULTILINESTRING_TYPE));

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
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JavaType conversionServiceAspectName) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding precedence declaration
        // This aspect before Roo_ConversionService
        builder.setDeclarePrecedence(aspectName, conversionServiceAspectName);

        // Creating WKTReader reader = new WKTReader(); field
        builder.addField(getField("reader", "new WKTReader()", WKTREADER_TYPE,
                Modifier.PRIVATE, null));
        // Creating WKTWriter writer = new WKTWriter(); field
        builder.addField(getField("writer", "new WKTWriter()", WKTWRITER_TYPE,
                Modifier.PRIVATE, null));

        // Adding Converter methods
        builder.addMethod(getPointToStringConverterMethod());
        builder.addMethod(getStringToPointConverterMethod());
        builder.addMethod(getLineStringToStringConverterMethod());
        builder.addMethod(getStringToLineStringConverterMethod());
        builder.addMethod(getPolygonToStringConverterMethod());
        builder.addMethod(getStringToPolygonConverterMethod());
        builder.addMethod(getGeometryToStringConverterMethod());
        builder.addMethod(getStringToGeometryConverterMethod());
        builder.addMethod(getMultiLineStringToStringConverterMethod());
        builder.addMethod(getStringToMultiLineStringConverterMethod());

        // Adding registry methods
        builder.addMethod(getInstallGeoLabelsConverterMethod());
        builder.addMethod(getAfterPropertiesSetMethod());

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
     * Gets <code>installGeoLabelsConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getInstallGeoLabelsConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.format.FormatterRegistry")));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(INSTALL_GEO_LABLES_METHOD,
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
        parameterNames.add(new JavaSymbolName("registry"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildInstallGeoLabelsConvertersMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, INSTALL_GEO_LABLES_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>afterPropertiesSet</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getAfterPropertiesSetMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(AFTER_PROPERTY_SET,
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
        buildAfterPropertiesSetMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, AFTER_PROPERTY_SET,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getLineStringToStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getLineStringToStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(LINESTRING_TO_STRING_METHOD,
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
        buildGetLineStringToStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LINESTRING_TO_STRING_METHOD,
                CONVERTER_LINESTRING_STRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getStringToLineStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getStringToLineStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(STRING_TO_LINESTRING_METHOD,
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
        buildGetStringToLineStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, STRING_TO_LINESTRING_METHOD,
                CONVERTER_STRING_LINESTRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getPolygonToStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getPolygonToStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POLYGON_TO_STRING_METHOD,
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
        buildGetPolygonToStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POLYGON_TO_STRING_METHOD,
                CONVERTER_POLYGON_STRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getStringToPolygonConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getStringToPolygonConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(STRING_TO_POLYGON_METHOD,
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
        buildGetStringToPolygonConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, STRING_TO_POLYGON_METHOD,
                CONVERTER_STRING_POLYGON, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getGeometryToStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getGeometryToStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(GEOMETRY_TO_STRING_METHOD,
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
        buildGetGeometryToStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, GEOMETRY_TO_STRING_METHOD,
                CONVERTER_GEOMETRY_STRING, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getStringToGeometryConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getStringToGeometryConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(STRING_TO_GEOMETRY_METHOD,
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
        buildGetStringToGeometryConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, STRING_TO_GEOMETRY_METHOD,
                CONVERTER_STRING_GEOMETRY, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getMultiLineStringToStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getMultiLineStringToStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(MULTLINESTR_TO_STR_MET,
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
        buildGetMultiLineStringToStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, MULTLINESTR_TO_STR_MET,
                CONV_MULTSTR_STR, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getStringToMultiLineStringConverter</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getStringToMultiLineStringConverterMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(STR_TO_MULTSTR_MET,
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
        buildGetStringToMultiLineStringConverterMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, STR_TO_MULTSTR_MET, CONV_STR_MULTSTR,
                parameterTypes, parameterNames, bodyBuilder);
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

    /**
     * Builds body method for <code>getLineStringToStringConverter</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetLineStringToStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<%s, java.lang.String>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(LINESTRING_TYPE)));

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("public String convert(LineString lineString) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format(
                "return %s.toLineString(lineString.getCoordinateSequence());",
                helper.getFinalTypeName(WKTWRITER_TYPE)));

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

    }

    /**
     * Builds body method for <code>getStringToLineStringConverter</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetStringToLineStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<java.lang.String, %s>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(LINESTRING_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("public LineString convert(String str) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return (LineString) reader.read(str);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(String.format("}catch( %s e) {",
                helper.getFinalTypeName(PARSE_EXCEPTION_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new IllegalArgumentException(");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(" \"Invalid string for LineString: \".concat(str), e);");

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

    /**
     * Builds body method for <code>getPolygonToStringConverter</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetPolygonToStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<%s, java.lang.String>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(POLYGON_TYPE)));

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("public String convert(Polygon polygon) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return writer.writeFormatted(polygon);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

    }

    /**
     * Builds body method for <code>getStringToPolygonConverter</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetStringToPolygonConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<java.lang.String, %s>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(POLYGON_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("public Polygon convert(String str) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return (Polygon) reader.read(str);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(String.format("}catch( %s e) {",
                helper.getFinalTypeName(PARSE_EXCEPTION_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new IllegalArgumentException(");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(" \"Invalid string for Polygon: \".concat(str), e);");

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

    /**
     * Builds body method for <code>getGeometryToStringConverter</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetGeometryToStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<%s, java.lang.String>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(GEOMETRY_TYPE)));

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("public String convert(Geometry geometry) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return writer.writeFormatted(geometry);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

    }

    /**
     * Builds body method for <code>getStringToGeometryConverter</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetStringToGeometryConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<java.lang.String, %s>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(GEOMETRY_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("public Geometry convert(String str) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return reader.read(str);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(String.format("}catch( %s e) {",
                helper.getFinalTypeName(PARSE_EXCEPTION_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new IllegalArgumentException(");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(" \"Invalid string for Geometry: \".concat(str), e);");

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

    /**
     * Builds body method for <code>getMultiLineStringToStringConverter</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetMultiLineStringToStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<%s, java.lang.String>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(MULTILINESTRING_TYPE)));

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("public String convert(MultiLineString multiLineString) {");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return writer.writeFormatted(multiLineString);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("};");

    }

    /**
     * Builds body method for <code>getStringToMultiLineStringConverter</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetStringToMultiLineStringConverterMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine(String.format(
                " return new %s<java.lang.String, %s>() {",
                helper.getFinalTypeName(CONVERTER_TYPE),
                helper.getFinalTypeName(MULTILINESTRING_TYPE)));

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("public MultiLineString convert(String str) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return (MultiLineString) reader.read(str);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine(String.format("}catch( %s e) {",
                helper.getFinalTypeName(PARSE_EXCEPTION_TYPE)));

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new IllegalArgumentException(");

        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(" \"Invalid string for MultiLineString: \".concat(str), e);");

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

    /**
     * Builds body method for <code>afterPropertySet</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildAfterPropertiesSetMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("super.afterPropertiesSet();");
        bodyBuilder.appendFormalLine("installLabelConverters(getObject());");
        bodyBuilder.appendFormalLine("installGeoLabelConverters(getObject());");
    }

    /**
     * Builds body method for <code>installGeoLabelsConverters</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildInstallGeoLabelsConvertersMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Adding all methods added bellow
        bodyBuilder
                .appendFormalLine("registry.addConverter(getPointToStringConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getStringToPointConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getLineStringToStringConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getStringToLineStringConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getPolygonToStringConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getStringToPolygonConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getGeometryToStringConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getStringToGeometryConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getMultiLineStringToStringConverter());");
        bodyBuilder
                .appendFormalLine("registry.addConverter(getStringToMultiLineStringConverter());");
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
