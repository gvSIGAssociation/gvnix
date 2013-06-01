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
package org.gvnix.addon.web.mvc.jquery;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.finder.WebFinderMetadata;
import org.springframework.roo.metadata.DefaultMetadataService;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Listener that is invoked when implementation of {@link JQueryMetadata} or
 * {@link WebFinderMetadata} (both acts as source meta-data) identification has
 * requested to notify {@link JQueryJspMetadata} destination meta-data
 * identification of an event.
 * <p/>
 * Provides the {@link JQueryJspMetadata}, that is, this class collects from
 * OSGi services the needed info to create the {@link JQueryMetadata}.
 * <p/>
 * Both {@link MetadataProvider} and {@link MetadataNotificationListener}
 * interfaces must be implemented together because {@link JQueryJspMetadata}
 * depends on {@link JQueryMetadata} changes. In that case, we must notify both
 * the {@code upstreamDependency} and the {@code downstreamDependency} to tell
 * the listener the dependency is. To do that the
 * {@link DefaultMetadataService#notify(String, String)} requires the
 * {@link MetadataProvider} implements the {@link MetadataNotificationListener}
 * too.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
@Component(immediate = true)
@Service
public class JQueryJspMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private MetadataService metadataService;

    @Reference
    private JQueryOperations operations;

    /**
     * Registers the dependency between upstream {@link JQueryMetadata},
     * upstream {@link WebFinderMetadata} and this meta-data as downstream.
     * <p/>
     * Registers this listener to be invoked when implementation of
     * {@link JQueryMetadata} or {@link WebFinderMetadata} (both acts as source
     * meta-data) identification has requested to notify a particular
     * destination meta-data identification of an event. Note this class listens
     * for {@link JQueryJspMetadata} destination meta-data only.
     * 
     * @param context
     */
    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                JQueryMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        metadataDependencyRegistry.registerDependency(
                WebFinderMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        metadataDependencyRegistry.addNotificationListener(this);
    }

    /**
     * Removes the dependencies with {@link JQueryMetadata} and
     * {@link WebFinderMetadata}.
     * <p/>
     * De-register this instance to receive MetadataNotificationListener events.
     * 
     * @param context
     */
    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                JQueryMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        metadataDependencyRegistry.deregisterDependency(
                WebFinderMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        metadataDependencyRegistry.removeNotificationListener(this);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Updates the view related to the Controller specified in the given
     * meta-data identification string.
     * <p/>
     * Note the meta-data item neither contains nor composes details of the
     * related Java type because it doesn't create any ITD.
     */
    public MetadataItem get(final String jqueryJspMetadataId) {

        // Get the Java type (Controller) that given meta-data ID refers to
        final JavaType controller = JQueryJspMetadata
                .getJavaType(jqueryJspMetadataId);

        // Create a JQueryMetadata ID for same Java type than the Java type
        // included in jqueryJspMetadataId
        final String jqueryMetadataKey = JQueryMetadata.createIdentifier(
                controller, JQueryJspMetadata.getPath(jqueryJspMetadataId));

        // Get the metadata, or null if the ID was valid but the metadata is
        // not currently available
        final JQueryMetadata jqueryMetadata = (JQueryMetadata) metadataService
                .get(jqueryMetadataKey);

        // Create a WebFinderMetadata ID for same Java type than the Java type
        // included in jqueryJspMetadataId
        final String finderMetadataKey = WebFinderMetadata.createIdentifier(
                controller, JQueryJspMetadata.getPath(jqueryJspMetadataId));

        // Get the metadata, or null if the ID was valid but the metadata is
        // not currently available
        final WebFinderMetadata finderMetadata = (WebFinderMetadata) metadataService
                .get(finderMetadataKey);

        // If we created a valid JQueryMetada, given meta-data ID refers to
        // JQueryMetadata and we must update related artifacts
        if (jqueryMetadata != null && jqueryMetadata.isValid()) {

            // Call to operations for update pages
            operations.updateCrudJsp(controller, jqueryMetadata);
        }

        // Moreover, if we created a valid WebFinderMetadata, given meta-data
        // ID refers to WebFinderMetadata and we must update the finders
        if (finderMetadata != null && finderMetadata.isValid()) {

            // Call to operations for update pages
            operations.updateFindJsp(controller, finderMetadata);
        }

        // Otherwise, neither JQueryMetadata nor WebFinderMetadata are
        // available or valid
        if ((jqueryMetadata == null || !jqueryMetadata.isValid())
                && (finderMetadata == null || !finderMetadata.isValid())) {

            // Can't get the corresponding JQuery information, so we
            // certainly don't need to manage any JSPs at this time
            return null;
        }

        return new JQueryJspMetadata(jqueryJspMetadataId, jqueryMetadata);
    }

    public String getProvidesType() {
        return JQueryJspMetadata.getMetadataIdentiferType();
    }

    public void notify(final String upstreamDependency,
            String downstreamDependency) {

        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            if (JQueryMetadata.isValid(upstreamDependency)) {
                final JavaType controller = JQueryMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = JQueryMetadata
                        .getPath(upstreamDependency);
                // TODO Construir el identificador específico del downstream
                // para la clase indicada en el upstream
                downstreamDependency = JQueryJspMetadata.createIdentifier(
                        controller, path);
            }
            else if (WebFinderMetadata.isValid(upstreamDependency)) {
                final JavaType controller = WebFinderMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = WebFinderMetadata
                        .getPath(upstreamDependency);
                downstreamDependency = JQueryJspMetadata.createIdentifier(
                        controller, path);
            }
            else {
                // not or register dependency: nothing to do
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

            // TODO: Automáticamente registra/cache el nuevo ID generado
            // asociado al upstream, de tal forma que sucesivas invocaciones
            // la condición del if anterior será true
            metadataService.evictAndGet(downstreamDependency);
        }
    }

}
