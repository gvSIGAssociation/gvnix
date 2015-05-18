/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.geo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.geo.annotations.GvNIXWebEntityMapLayer;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
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
 * ITD generator for {@link GvNIXWebEntityMapLayer} annotation.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.4.0
 */
public class GvNIXWebEntityMapLayerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName LIST_GEO_ENTITY_ON_MAP_VIEWER = new JavaSymbolName(
            "listGeoEntityOnMapViewer");

    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXWebEntityMapLayerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public GvNIXWebEntityMapLayerMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            TypeLocationService typeLocationService,
            TypeManagementService typeManagementService, JavaType controller,
            JavaType entity, String entityPlural) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Generate necessary methods

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding list method
        builder.addMethod(getListGeoEntityOnMapViewerMethod(entity,
                entityPlural));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Gets <code>listGeoEntityOnMapViewer</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getListGeoEntityOnMapViewerMethod(JavaType entity,
            String plural) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Adding bbox param
        AnnotationMetadataBuilder bboxParamMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_BODY);
        bboxParamMetadataBuilder.addBooleanAttribute("required", false);

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                bboxParamMetadataBuilder.build()));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(
                LIST_GEO_ENTITY_ON_MAP_VIEWER, parameterTypes);
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
                "entityMapList");
        requestMappingMetadataBuilder.addStringAttribute("headers",
                "Accept=application/json");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "application/json");
        requestMappingMetadataBuilder.addStringAttribute("consumes",
                "application/json");

        AnnotationMetadataBuilder responseBodyMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.RESPONSE_BODY);

        annotations.add(requestMappingMetadataBuilder);
        annotations.add(responseBodyMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("mapBoundingBox"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildlistGeoEntityOnMapViewerMethodBody(entity, plural, bodyBuilder);

        // Return type
        JavaType responseEntityJavaType = new JavaType(
                SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(new JavaType(
                        "java.util.List", 0, DataType.TYPE, null, Arrays
                                .asList(entity))));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LIST_GEO_ENTITY_ON_MAP_VIEWER,
                responseEntityJavaType, parameterTypes, parameterNames,
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
    private void buildlistGeoEntityOnMapViewerMethodBody(JavaType entity,
            String plural, InvocableMemberBodyBuilder bodyBuilder) {
        // HttpHeaders headers = new HttpHeaders();
        bodyBuilder.appendFormalLine(String.format(
                "%s headers = new HttpHeaders();",
                helper.getFinalTypeName(SpringJavaType.HTTP_HEADERS)));

        // headers.add("Content-Type", "application/json; charset=utf-8");
        bodyBuilder
                .appendFormalLine("headers.add(\"Content-Type\", \"application/json; charset=utf-8\");");

        // Generating empty result list
        // List<Owner> result = new ArrayList<Owner>();
        bodyBuilder.appendFormalLine("// Generating empty result list");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> result = new %s<%s>();",
                helper.getFinalTypeName(new JavaType("java.util.List")),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(new JavaType("java.util.ArrayList")),
                helper.getFinalTypeName(entity)));

        // Looking for all entries on map bounding box
        // if (StringUtils.isNotBlank(mapBoundingBox)) {
        bodyBuilder
                .appendFormalLine("// Looking for all entries on map bounding box");
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(mapBoundingBox)) {", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));

        bodyBuilder.indent();

        // result = Entity.findAllEntitiesByBoundingBox
        bodyBuilder.appendFormalLine(String.format(
                "result = %s.findAll%sByBoundingBox(mapBoundingBox);",
                helper.getFinalTypeName(entity), plural));

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else {");

        bodyBuilder.indent();

        // If bounding box is empty, find all entries
        // result = Owner.findAllOwners();

        bodyBuilder
                .appendFormalLine("// If bounding box is empty, find all entries");
        // List<Owner> result = Owner.findAllOwners();
        bodyBuilder.appendFormalLine(String.format("result = %s.findAll%s();",
                helper.getFinalTypeName(entity), plural));

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return new ResponseEntity<List<Owner>>(result, headers,
        // org.springframework.http.HttpStatus.OK);
        // Return type
        JavaType responseEntityJavaType = new JavaType(
                SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(new JavaType(
                        "java.util.List", 0, DataType.TYPE, null, Arrays
                                .asList(entity))));
        bodyBuilder
                .appendFormalLine(String
                        .format("return new %s(result, headers, org.springframework.http.HttpStatus.OK);",
                                helper.getFinalTypeName(responseEntityJavaType)));
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
}
