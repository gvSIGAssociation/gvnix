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
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.converter.ConversionServiceMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link GeoConversionServiceMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.4
 */
@Component
@Service
public final class GvNIXGeoConversionServiceMetadataProvider extends
        AbstractItdMetadataProvider {

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
                GvNIXGeoConversionService.class.getName()));
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
                GvNIXGeoConversionService.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = GvNIXGeoConversionServiceMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXGeoConversionServiceMetadata
                .getPath(metadataIdentificationString);

        // Get RooConversionServiceMetadata
        String conversionServiceMetadataId = PhysicalTypeIdentifierNamingUtils
                .createIdentifier(ConversionServiceMetadata.class.getName(),
                        javaType, path);
        ConversionServiceMetadata conversionServiceMetadata = (ConversionServiceMetadata) metadataService
                .get(conversionServiceMetadataId);

        // Getting ConversionServiceMetadata Aspect Name
        JavaType conversionServiceAspectName = conversionServiceMetadata
                .getAspectName();

        return new GvNIXGeoConversionServiceMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, conversionServiceAspectName);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXGeoConversionService.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXGeoConversionService";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = GvNIXGeoConversionServiceMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXGeoConversionServiceMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return GvNIXGeoConversionServiceMetadata.createIdentifier(javaType,
                path);
    }

    public String getProvidesType() {
        return GvNIXGeoConversionServiceMetadata.getMetadataIdentiferType();
    }
}