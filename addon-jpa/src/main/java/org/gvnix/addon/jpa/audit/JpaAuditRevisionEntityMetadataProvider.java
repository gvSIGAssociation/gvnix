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
package org.gvnix.addon.jpa.audit;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProvider;
import org.gvnix.addon.jpa.audit.providers.RevisionLogRevisionEntityMetadataBuilder;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link JpaAuditRevisionEntityMetadata}. This type is called by Roo
 * to retrieve the metadata for this add-on. Use this type to reference external
 * types and services needed by the metadata type. Register metadata triggers
 * and dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
public final class JpaAuditRevisionEntityMetadataProvider extends
        AbstractItdMetadataProvider {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(JpaAuditRevisionEntityMetadataProvider.class);

    @Reference
    private JpaAuditOperations operations;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(
                GvNIXJpaAuditRevisionEntity.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXJpaAudit.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Gets annotation values
        final JpaAuditRevisionEntityAnnotationValues annotationValues = new JpaAuditRevisionEntityAnnotationValues(
                governorPhysicalTypeMetadata);

        RevisionLogProvider logProvider = operations
                .getActiveRevisionLogProvider();

        if (logProvider == null) {
            LOGGER.severe(String
                    .format("%s is annotated with @%s but no revisionLog provider is active.",
                            governorPhysicalTypeMetadata.getType()
                                    .getFullyQualifiedTypeName(),
                            GvNIXJpaAuditRevisionEntity.class.getSimpleName()));
            return null;
        }
        RevisionLogRevisionEntityMetadataBuilder revisionLogBuilder = logProvider
                .getRevisonEntityMetadataBuilder(governorPhysicalTypeMetadata);

        // Pass dependencies required by the metadata in through its constructor
        return new JpaAuditRevisionEntityMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, annotationValues,
                revisionLogBuilder);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_Jpafilter.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXJpaAuditRevisionEntity";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = JpaAuditRevisionEntityMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = JpaAuditRevisionEntityMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return JpaAuditRevisionEntityMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return JpaAuditRevisionEntityMetadata.getMetadataIdentiferType();
    }
}