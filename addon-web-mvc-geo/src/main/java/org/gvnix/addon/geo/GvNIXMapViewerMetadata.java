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
import java.util.Map;

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
 * ITD generator for {@link GvNIXGeoConversionService} annotation.
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public class GvNIXMapViewerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName UIMODEL_PARAM_NAME = new JavaSymbolName(
            "uiModel");

    private static final JavaSymbolName POPULATE_LAYERS_METHOD = new JavaSymbolName(
            "populateLayers");

    private static final JavaSymbolName POPULATE_CONFIG_METHOD = new JavaSymbolName(
            "populateConfig");

    private static final JavaSymbolName SHOW_MAP_METHOD = new JavaSymbolName(
            "showMap");

    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXMapViewerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaType LIST_MAP_STRING_STRING = new JavaType(
            new JavaType("java.util.List").getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(new JavaType(new JavaType(
                    "java.util.Map").getFullyQualifiedTypeName(), 0,
                    DataType.TYPE, null, Arrays.asList(JavaType.STRING,
                            JavaType.STRING))));

    private static final JavaType ARRAYLIST_MAP_STRING_STRING = new JavaType(
            new JavaType("java.util.ArrayList").getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(new JavaType(new JavaType(
                    "java.util.Map").getFullyQualifiedTypeName(), 0,
                    DataType.TYPE, null, Arrays.asList(JavaType.STRING,
                            JavaType.STRING))));

    public GvNIXMapViewerMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata, String path) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding Converter methods
        String finalPath = path.replaceAll("/", "");
        builder.addMethod(getShowMapMethod(finalPath));
        builder.addMethod(getPopulateLayersMethod());
        builder.addMethod(getPopulateConfigMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>showMap</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getShowMapMethod(String path) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(SpringJavaType.MODEL));
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest")));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SHOW_MAP_METHOD,
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

        requestMappingMetadataBuilder.addEnumAttribute("method",
                SpringJavaType.REQUEST_METHOD, "GET");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "text/html");
        annotations.add(requestMappingMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UIMODEL_PARAM_NAME);
        parameterNames.add(new JavaSymbolName("request"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildShowMapMethodBody(bodyBuilder, path);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, SHOW_MAP_METHOD, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>populateLayers</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getPopulateLayersMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(SpringJavaType.MODEL));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_LAYERS_METHOD,
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
        parameterNames.add(UIMODEL_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildPopulateLayersMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_LAYERS_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>populateConfig</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getPopulateConfigMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(SpringJavaType.MODEL));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(POPULATE_CONFIG_METHOD,
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
        parameterNames.add(UIMODEL_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildPopulateConfigMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, POPULATE_CONFIG_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>showMap</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildShowMapMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String path) {

        // populateLayers(uiModel);
        bodyBuilder.appendFormalLine("populateLayers(uiModel);");
        // populateConfig(uiModel);
        bodyBuilder.appendFormalLine("populateConfig(uiModel);");
        // return "path/show";
        bodyBuilder
                .appendFormalLine(String.format("return \"%s/show\";", path));

    }

    /**
     * Builds body method for <code>populateLayers</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildPopulateLayersMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // List<Map<String,String>> layerList = new
        // ArrayList<Map<String,String>>();
        bodyBuilder.appendFormalLine(String.format("%s layerList = new %s();",
                helper.getFinalTypeName(LIST_MAP_STRING_STRING),
                helper.getFinalTypeName(ARRAYLIST_MAP_STRING_STRING)));
    }

    /**
     * Builds body method for <code>populateConfig</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildPopulateConfigMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // uiModel.addAttribute("url", "URL DEFAULT VALUE");
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"url\", \"http://{s}.tile.osm.org/{z}/{x}/{y}.png?bar\");");
        // uiModel.addAttribute("maxZoom", "DEFAULT MAX ZOOM VALUE");
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"maxZoom\", \"21\");");
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
