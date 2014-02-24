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
package org.gvnix.addon.gva.security.providers.safe;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;

/**
 * Provides {@link DatatablesMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public final class SafeSecurityProviderPasswordMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private WebMetadataService webMetadataService;

    @Reference
    protected ProjectOperations projectOperations;

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
                GvNIXPasswordHandlerSAFE.class.getName()));
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
                GvNIXPasswordHandlerSAFE.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = SafeSecurityProviderPasswordMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = SafeSecurityProviderPasswordMetadata
                .getPath(metadataIdentificationString);

        return new SafeSecurityProviderPasswordMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXPasswordHandlerSAFE.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXPasswordHandlerSAFE";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = SafeSecurityProviderPasswordMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = SafeSecurityProviderPasswordMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return SafeSecurityProviderPasswordMetadata.createIdentifier(javaType,
                path);
    }

    public String getProvidesType() {
        return SafeSecurityProviderPasswordMetadata.getMetadataIdentiferType();
    }
}