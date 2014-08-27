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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.PhysicalTypeUtils;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link GvNIXEntityMapLayerControllerMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.4
 */
@Component
@Service
public final class GvNIXEntityMapLayerControllerMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    TypeManagementService typeManagementService;

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(
                GvNIXEntityMapLayerController.class.getName()));
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
        removeMetadataTrigger(new JavaType(
                GvNIXEntityMapLayerController.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = GvNIXEntityMapLayerControllerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXEntityMapLayerControllerMetadata
                .getPath(metadataIdentificationString);

        // Getting controller
        ClassOrInterfaceTypeDetails controller = typeLocationService
                .getTypeDetails(javaType);

        // Getting @RooWebScaffold annotation
        String webScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(
                javaType, path);

        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataId);

        WebScaffoldAnnotationValues webScaffoldAnnotationValues = webScaffoldMetadata
                .getAnnotationValues();

        // Getting entity
        JavaType entity = webScaffoldAnnotationValues.getFormBackingObject();

        LogicalPath entityPath = PhysicalTypeUtils.getPath(entity,
                typeLocationService);

        String jpaMetadataId = JpaActiveRecordMetadata.createIdentifier(entity,
                entityPath);
        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) metadataService
                .get(jpaMetadataId);
        if (jpaMetadata == null) {
            // Unsupported type (by now)
            return null;
        }

        // Getting entity plural
        String plural = jpaMetadata.getPlural();

        return new GvNIXEntityMapLayerControllerMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, typeLocationService,
                typeManagementService, javaType, entity, plural);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXEntityMapLayerController.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXEntityMapLayerController";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = GvNIXEntityMapLayerControllerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXEntityMapLayerControllerMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return GvNIXEntityMapLayerControllerMetadata.createIdentifier(javaType,
                path);
    }

    public String getProvidesType() {
        return GvNIXEntityMapLayerControllerMetadata.getMetadataIdentiferType();
    }
}