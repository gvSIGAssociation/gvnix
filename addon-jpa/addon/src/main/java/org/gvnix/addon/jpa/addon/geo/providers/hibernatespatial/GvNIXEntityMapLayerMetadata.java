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
package org.gvnix.addon.jpa.addon.geo.providers.hibernatespatial;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
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
 * ITD generator for {@link GvNIXEntityMapLayerController} annotation.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.4.0
 */
public class GvNIXEntityMapLayerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXEntityMapLayerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public GvNIXEntityMapLayerMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            TypeLocationService typeLocationService,
            TypeManagementService typeManagementService, JavaType entity,
            String entityPlural, List<JavaSymbolName> geoFieldNames) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Generate necessary methods

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding findAllEntitiesByBoundingBox method
        builder.addMethod(getFindAllEntitiesByBoundingBoxMethod(entity,
                entityPlural, geoFieldNames));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Gets <code>findAllEntitiesByBoundingBox</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getFindAllEntitiesByBoundingBoxMethod(
            JavaType entity, String plural, List<JavaSymbolName> geoFieldNames) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Adding String param
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(JavaType.STRING));

        // Getting method name
        JavaSymbolName methodName = new JavaSymbolName(String.format(
                "findAll%sByBoundingBox", plural));

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

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        // bbox parameter
        parameterNames.add(new JavaSymbolName("bbox"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildfindAllEntitiesByBoundingBoxMethodBody(entity, plural,
                bodyBuilder, geoFieldNames);

        // Return type
        JavaType responseEntityJavaType = new JavaType("java.util.List", 0,
                DataType.TYPE, null, Arrays.asList(entity));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
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
    private void buildfindAllEntitiesByBoundingBoxMethodBody(JavaType entity,
            String plural, InvocableMemberBodyBuilder bodyBuilder,
            List<JavaSymbolName> geoFieldNames) {

        // Generating query
        StringBuilder query = new StringBuilder().append(String.format(
                "SELECT o FROM %s o", helper.getFinalTypeName(entity)));
        String finalQuery = query.toString();
        if (!geoFieldNames.isEmpty()) {
            query.append(" WHERE ");
            // Adding all fields to intersect
            for (JavaSymbolName field : geoFieldNames) {
                query.append(String.format(
                        " intersects(o.%s, :bbox) = true OR ", field.toString()));
            }

            // Removing last OR
            finalQuery = query.substring(0, query.length() - 3);

        }

        // TypedQuery<Entity> q = em.createQuery(query, Entity.class);
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> q = entityManager().createQuery(\"%s\", %s.class);",
                helper.getFinalTypeName(new JavaType(
                        "javax.persistence.TypedQuery")), helper
                        .getFinalTypeName(entity), finalQuery, helper
                        .getFinalTypeName(entity)));

        if (!geoFieldNames.isEmpty()) {
            // q.setParameter("bbox", String.format("POLYGON((%s))", bbox));
            bodyBuilder
                    .appendFormalLine("q.setParameter(\"bbox\", String.format(\"POLYGON((%s))\", bbox));");
        }
        // return q.getResultList();
        bodyBuilder.appendFormalLine("return q.getResultList();");

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
