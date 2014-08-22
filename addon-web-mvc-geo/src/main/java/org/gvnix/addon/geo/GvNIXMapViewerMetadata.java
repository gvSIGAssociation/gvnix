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
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

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

    private static final JavaSymbolName SHOW_MAP_METHOD = new JavaSymbolName(
            "showMap");

    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXMapViewerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public GvNIXMapViewerMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ProjectOperations projectOperations,
            TypeLocationService typeLocationService, List<JavaType> entities,
            String path) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Generating necessary methods

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding Converter methods
        String finalPath = path.replaceAll("/", "");
        builder.addMethod(getShowMapMethod(finalPath));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

        // Updating show.jspx with entities to display
        updateJSPXFiles(entities, finalPath, typeLocationService,
                projectOperations);
    }

    /**
     * This method updates necessary JSPX files to visualize correctly entities
     * and his fields
     * 
     * @param entities
     * @param path
     * @param typeLocationService
     * @param fileManager
     * 
     */
    private void updateJSPXFiles(List<JavaType> entities, String path,
            TypeLocationService typeLocationService,
            ProjectOperations projectOperations) {

        PathResolver pathResolver = projectOperations.getPathResolver();

        // Getting jspx file path
        String finalPath = new JavaSymbolName(path).getReadableSymbolName()
                .toLowerCase();
        final String showPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/show.jspx", finalPath));

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
     * Builds body method for <code>showMap</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildShowMapMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String path) {

        // return "path/show";
        bodyBuilder
                .appendFormalLine(String.format("return \"%s/show\";", path));
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
