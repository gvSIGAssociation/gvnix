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

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.audit.GvNIXJpaAudit.StoreRevisionLog;
import org.gvnix.addon.jpa.audit.providers.RevisionLogMetadataBuilder;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProvider;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link JpaAuditMetadata}. This type is called by Roo to retrieve the
 * metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
public final class JpaAuditMetadataProvider extends AbstractItdMetadataProvider {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(JpaAuditMetadataProvider.class);

    @Reference
    private JpaAuditOperationsMetadata operations;

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
        addMetadataTrigger(new JavaType(GvNIXJpaAudit.class.getName()));
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
        final JpaAuditAnnotationValues annotationValues = new JpaAuditAnnotationValues(
                governorPhysicalTypeMetadata);

        LogicalPath path = JpaAuditMetadata
                .getPath(metadataIdentificationString);

        // Gets active revisionLog provider
        RevisionLogProvider logProvider = operations
                .getActiveRevisionLogProvider();

        JavaType userService = operations.getUserServiceType();

        if (userService == null) {
            // No user type defined
            return null;
        }

        String userServiceId = JpaAuditUserServiceMetadata.createIdentifier(
                userService, path);

        JpaAuditUserServiceMetadata userServiceMetadata = (JpaAuditUserServiceMetadata) metadataService
                .get(userServiceId);

        if (userServiceMetadata == null) {
            // No user type: do nothing
            return null;
        }

        // prepares entity information
        JavaType entity = JpaAuditMetadata
                .getJavaType(metadataIdentificationString);

        // Gets entity JPA metadata
        String jpaMetadataId = JpaActiveRecordMetadata.createIdentifier(entity,
                path);

        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) metadataService
                .get(jpaMetadataId);

        if (jpaMetadata == null) {
            // There is no JpaMetadata yet: return null
            return null;
        }

        // Gets entity identifier field definition
        List<FieldMetadata> identifiers = persistenceMemberLocator
                .getIdentifierFields(entity);

        if (identifiers == null || identifiers.isEmpty()) {
            // There is no JpaMetadata yet: return null
            return null;
        }

        // Add dependency with JPA metadata
        metadataDependencyRegistry.registerDependency(jpaMetadataId,
                metadataIdentificationString);

        // Makes all downstrean dependents of class dependents of it
        Set<String> dependentsOfJpa = metadataDependencyRegistry
                .getDownstream(getGovernorPhysicalTypeIdentifier(metadataIdentificationString));
        for (String downstreamId : dependentsOfJpa) {
            if (!metadataIdentificationString.equals(downstreamId)
                    && !jpaMetadataId.equals(downstreamId)) {
                metadataDependencyRegistry.registerDependency(
                        metadataIdentificationString, downstreamId);
                // Cleans metadata already generated from governor
                // so it could generate taking account of JpaAudit
                // ITD generation
                metadataService.evict(downstreamId);
            }
        }

        // Add dependency with UserService metadata
        metadataDependencyRegistry.registerDependency(userServiceId,
                metadataIdentificationString);

        // Gets the plural of current entity
        String plural = jpaMetadata.getPlural();

        // Prepares log provider builder (if any)
        RevisionLogMetadataBuilder revisionLogBuilder = null;
        if (logProvider == null) {
            // No log provider
            if (annotationValues.getStoreRevisionLog() == StoreRevisionLog.YES) {
                // entity is annotated to log revision but not provider set:
                // Show warning to user
                LOGGER.warning(governorPhysicalTypeMetadata
                        .getType()
                        .getSimpleTypeName()
                        .concat(" configured to use Revision Log but no provider set: try to use 'jpa audit revisonLog' command"));
            }
            // pass revisionLogBuilder as null: don't use revision log
        }
        else {
            // log provider configured
            boolean useRevisionLog = false;
            if (annotationValues.getStoreRevisionLog() == null
                    || annotationValues.getStoreRevisionLog() == StoreRevisionLog.PROVIDER_DEFAULT) {
                // revisionLog annotation value not set or use provider
                // configuration: get from provider
                useRevisionLog = logProvider
                        .getDefaultValueOfRevisionLogAttribute();
            }
            else if (annotationValues.getStoreRevisionLog() == StoreRevisionLog.YES) {
                // revisionLog required by annotation value
                useRevisionLog = true;
            }
            if (useRevisionLog) {
                // Use revision log: require builder to provider
                revisionLogBuilder = logProvider.getMetadataBuilder(operations,
                        governorPhysicalTypeMetadata);
            }
            // otherwise: pass revisionLogBuilder as null: don't use revision
            // log
        }

        // Pass dependencies required by the metadata in through its constructor
        return new JpaAuditMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, annotationValues, plural,
                revisionLogBuilder, identifiers,
                userServiceMetadata.userType(),
                userServiceMetadata.isUserTypeEntity(),
                userServiceMetadata.isUserTypeSpringSecUserDetails(),
                userServiceMetadata.usePatternForTimestamp(),
                userServiceMetadata.getPatternForTimestamp(),
                userServiceMetadata.getTimestampStyle());
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXJpaAudit.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXJpaAudit";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = JpaAuditMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = JpaAuditMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return JpaAuditMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return JpaAuditMetadata.getMetadataIdentiferType();
    }
}