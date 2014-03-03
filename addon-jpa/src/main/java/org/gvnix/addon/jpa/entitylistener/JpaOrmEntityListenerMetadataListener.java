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
package org.gvnix.addon.jpa.entitylistener;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata listener which handles metadata reltated to jpa entity listeners
 * 
 * @author gvNIX Team
 */
@Component(immediate = true)
@Service
public class JpaOrmEntityListenerMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private MetadataService metadataService;

    @Reference
    private JpaOrmEntityListenerRegistry registry;

    @Reference
    private JpaOrmEntityListenerOperations operations;

    protected void activate(final ComponentContext context) {
        // We don't need register anything: this is done on
        // JpaOrmEntityListenerRegistry
        metadataDependencyRegistry.addNotificationListener(this);
    }

    protected void deactivate(final ComponentContext context) {
        // We don't need register anything: this is done on
        // JpaOrmEntityListenerRegistry
        metadataDependencyRegistry.removeNotificationListener(this);
    }

    public MetadataItem get(final String jpaOrmEntityListenerMetadataId) {

        // Get listener java type
        final JavaType listenerJavaType = JpaOrmEntityListenerMetadata
                .getJavaType(jpaOrmEntityListenerMetadataId);
        final LogicalPath listenerPath = JpaOrmEntityListenerMetadata
                .getPath(jpaOrmEntityListenerMetadataId);

        // Locate source metadata
        final String sourceMetadataProvider = JpaOrmEntityListenerMetadata
                .getSorceId(jpaOrmEntityListenerMetadataId);
        final String sourceMetadataKey = createProviderIdentifierKey(
                sourceMetadataProvider, listenerJavaType, listenerPath);
        final JpaOrmEntityListener sourceMetadata = (JpaOrmEntityListener) metadataService
                .get(sourceMetadataKey);

        // Check source metadata
        if (sourceMetadata == null || !sourceMetadata.isValid()) {
            // Can't get the entityListener information, so we
            // can't register it.
            return null;
        }
        if (sourceMetadata.getEntityClass() == null) {
            throw new IllegalArgumentException(
                    sourceMetadataKey
                            .concat(" doesn't provides the Entity class."));
        }
        if (sourceMetadata.getListenerClass() == null) {
            throw new IllegalArgumentException(
                    sourceMetadataKey
                            .concat(" doesn't provides the Listener class."));
        }

        // Add entity-listener to orm.xml thru operations
        operations.addEntityListener(sourceMetadata, sourceMetadataProvider);

        return new JpaOrmEntityListenerMetadata(jpaOrmEntityListenerMetadataId,
                sourceMetadata);
    }

    public String getProvidesType() {
        return JpaOrmEntityListenerMetadata.getMetadataIdentiferType();
    }

    public void notify(final String upstreamDependency,
            String downstreamDependency) {

        // Cleanup entity-listener of all entities
        if (downstreamDependency == null) {
            // Check if upstream s a Jpa ActiveRecord Metadata
            if (JpaActiveRecordMetadata.isValid(upstreamDependency)) {
                // Get entity
                JavaType entity = JpaActiveRecordMetadata
                        .getJavaType(upstreamDependency);
                // Call operations to perform clean up
                operations.cleanUpEntityListeners(entity);
            }
        }

        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been

            // Gets provider registered
            List<String> resiteredListeners = registry.getListenerOrder();

            boolean found = false;

            // For every provider
            for (String providerId : resiteredListeners) {
                // Check if is a metadata from current provider
                if (isMetadaFromProvider(providerId, upstreamDependency)) {

                    // Check if it' valid
                    String providerMetadataClass = getMetadaClass(providerId);
                    if (isMetadaValid(providerMetadataClass, upstreamDependency)) {

                        // Adjust downstream metadata
                        final JavaType javaType = getJavaType(
                                providerMetadataClass, upstreamDependency);
                        final LogicalPath path = getPath(providerMetadataClass,
                                upstreamDependency);
                        downstreamDependency = JpaOrmEntityListenerMetadata
                                .createIdentifier(javaType, path, providerId);

                        found = true;
                    }

                    // provider found: exit for
                    break;
                }
            }

            if (!found) {
                // nothing to do
                return;
            }

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                    .contains(downstreamDependency)) {
                return;
            }

            // produces metadata
            metadataService.evictAndGet(downstreamDependency);
        }
    }

    /**
     * Gets Logical path form a provider-metadata
     * 
     * @param providerMetadataClass
     * @param dependencyId
     * @return
     */
    private LogicalPath getPath(String providerMetadataClass,
            String dependencyId) {
        return PhysicalTypeIdentifierNamingUtils.getPath(providerMetadataClass,
                dependencyId);
    }

    /**
     * Gets JavaT7ype from a provider-metadata
     * 
     * @param providerMetadataClass
     * @param dependencyId
     * @return
     */
    private JavaType getJavaType(String providerMetadataClass,
            String dependencyId) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                providerMetadataClass, dependencyId);
    }

    /**
     * Gets metadata class from a provider-metadata-id
     * 
     * @param providerId
     * @return
     */
    private String getMetadaClass(String providerId) {
        return MetadataIdentificationUtils.getMetadataClass(providerId);
    }

    /**
     * If metadata is from provider-metadata-id
     * 
     * @param providerId
     * @param idToCheck
     * @return
     */
    private boolean isMetadaFromProvider(String providerId, String idToCheck) {
        return StringUtils.equals(getMetadaClass(providerId),
                getMetadaClass(idToCheck));
    }

    /**
     * If metadata is valid
     * 
     * @param providerMetadataClass
     * @param idToCheck
     * @return
     */
    private boolean isMetadaValid(String providerMetadataClass, String idToCheck) {
        return PhysicalTypeIdentifierNamingUtils.isValid(providerMetadataClass,
                idToCheck);
    }

    /**
     * Create a metadata identification string for a provider metadata
     * 
     * @param sourceMetadataProvider
     * @param listenerJavaType
     * @param listenerPath
     * @return
     */
    private String createProviderIdentifierKey(String sourceMetadataProvider,
            JavaType listenerJavaType, LogicalPath listenerPath) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                getMetadaClass(sourceMetadataProvider), listenerJavaType,
                listenerPath);
    }
}
