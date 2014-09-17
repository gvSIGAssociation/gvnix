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

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;

/**
 * Provides {@link GvNIXMapViewerMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.4
 */
@Component
@Service
public final class GvNIXMapViewerMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private ProjectOperations projectOperations;

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXMapViewer.class.getName()));
    }

    /**
     * Unregister this provider
     * 
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXMapViewer.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = GvNIXMapViewerMetadata
                .getJavaType(metadataIdentificationString);

        ClassOrInterfaceTypeDetails controller = typeLocationService
                .getTypeDetails(javaType);

        // Getting @RequestMapping annotation
        AnnotationMetadata requestMappingAnnotation = controller
                .getAnnotation(SpringJavaType.REQUEST_MAPPING);

        // Getting @GvNIXMapViewer annotation
        AnnotationMetadata mapViewerAnnotation = controller
                .getAnnotation(new JavaType(GvNIXMapViewer.class.getName()));

        // Getting path value
        AnnotationAttributeValue<Object> value = requestMappingAnnotation
                .getAttribute("value");

        String path = value.getValue().toString();

        // Getting mapId
        String mapId = String.format("ps_%s_%s", javaType.getPackage()
                .getFullyQualifiedPackageName().replaceAll("[.]", "_"),
                new JavaSymbolName(path.replaceAll("/", ""))
                        .getSymbolNameCapitalisedFirstLetter());

        // Getting entityLayers
        List<JavaType> entitiesToVisualize = new ArrayList<JavaType>();

        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArrayAttributeValue<ClassAttributeValue> mapViewerAttributes = (ArrayAttributeValue) mapViewerAnnotation
                .getAttribute("entityLayers");
        if (mapViewerAttributes != null) {
            List<ClassAttributeValue> entityLayers = mapViewerAttributes
                    .getValue();
            for (ClassAttributeValue entity : entityLayers) {
                entitiesToVisualize.add(entity.getValue());
            }
        }

        // Getting projection
        String projection = "";
        AnnotationAttributeValue<Object> projectionAttr = mapViewerAnnotation
                .getAttribute("projection");
        if (projectionAttr != null) {
            projection = projectionAttr.getValue().toString();
        }

        return new GvNIXMapViewerMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, projectOperations,
                typeLocationService, this.fileManager, entitiesToVisualize,
                path, mapId, projection);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXMapViewer.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXMapViewer";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = GvNIXMapViewerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXMapViewerMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return GvNIXMapViewerMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return GvNIXMapViewerMetadata.getMetadataIdentiferType();
    }
}