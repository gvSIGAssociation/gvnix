/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.screen.roo.addon;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;

/**
 * Provides {@link EntityBatchMetadataProvider}.
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public final class EntityBatchMetadataProvider extends
        AbstractItdMetadataProvider {

    private static final JavaType ENTITY_BATCH_ANNOTATION = new JavaType(
            GvNIXEntityBatch.class.getName());

    @Reference
    ProjectOperations projectOperations;

    @Reference
    PropFileOperations propFileOperations;

    /* For project setup */
    @Reference
    WebScreenConfigService config;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(ENTITY_BATCH_ANNOTATION);
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(ENTITY_BATCH_ANNOTATION);
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {
        JavaType entityType = EntityBatchMetadata
                .getJavaType(metadataIdentificationString);

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails classOrInterfaceDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Validate.notNull(
                classOrInterfaceDetails,
                "Governor failed to provide class type details, in violation of superclass contract");

        // We need to know the metadata of the Entity through
        LogicalPath path = EntityBatchMetadata.getPath(metadataIdentificationString);
        String entityMetadataKey = JpaActiveRecordMetadata.createIdentifier(entityType,
        		path);
        JpaActiveRecordMetadata entityMetadata = (JpaActiveRecordMetadata) metadataService
                .get(entityMetadataKey);
        if (entityMetadata == null) {
            // Metadata not available yet, do nothing on this invoke
            return null;
        }

        // Perform project setup (adds dependencies to project)
        // config.setup();

        // Pass dependencies required by the metadata in through its constructor
        return new EntityBatchMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, entityMetadata);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXPattern.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXEntityBatch";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = EntityBatchMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = EntityBatchMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return EntityBatchMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return EntityBatchMetadata.getMetadataIdentiferType();
    }
}
