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
package org.gvnix.addon.web.mvc.addon.jquery;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
import org.springframework.roo.support.logging.HandlerUtils;

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
@Component
@Service
public class JQueryJspMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(JQueryJspMetadataListener.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private MetadataDependencyRegistry metadataDependencyRegistry;

    private MetadataService metadataService;

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
    protected void activate(final ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                JQueryMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        getMetadataDependencyRegistry().registerDependency(
                WebFinderMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        getMetadataDependencyRegistry().addNotificationListener(this);
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
        getMetadataDependencyRegistry().deregisterDependency(
                JQueryMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        getMetadataDependencyRegistry().deregisterDependency(
                WebFinderMetadata.getMetadataIdentiferType(),
                JQueryJspMetadata.getMetadataIdentiferType());
        getMetadataDependencyRegistry().removeNotificationListener(this);
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

        // Get the meta-data means the given Java type has the specified
        // meta-data, otherwise the ID was valid but the metadata is
        // not currently available
        final JQueryMetadata jqueryMetadata = (JQueryMetadata) getMetadataService()
                .get(jqueryMetadataKey);

        // If we created a valid JQueryMetada, given meta-data ID refers to
        // JQueryMetadata and we must update related artifacts
        if (jqueryMetadata != null && jqueryMetadata.isValid()) {

            // Call to operations for update pages
            getOperations().updateCrudJsp(controller, jqueryMetadata);
        }
        // JQueryMetadata is required, that is, if given Java type hasn't
        // it there is nothing to do, neither in CRUD jspx nor finder jspx
        else {
            return null;
        }

        // Create a WebFinderMetadata ID for same Java type than the Java type
        // included in jqueryJspMetadataId
        final String finderMetadataKey = WebFinderMetadata.createIdentifier(
                controller, JQueryJspMetadata.getPath(jqueryJspMetadataId));

        // Get the meta-data means the given Java type has the specified
        // meta-data, otherwise the ID was valid but the metadata is
        // not currently available
        final WebFinderMetadata finderMetadata = (WebFinderMetadata) getMetadataService()
                .get(finderMetadataKey);

        // Moreover, if we created a valid WebFinderMetadata, given meta-data
        // ID refers to WebFinderMetadata and we must update the finders
        if (finderMetadata != null) {
            if (finderMetadata.isValid()) {
                // Call to operations for update pages
                getOperations().updateFindJsp(controller, finderMetadata);
            }
            else {
                // Finder meta-data is not valid
                return null;
            }
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

            // If source meta-data identification (requester/notifier) is
            // JQueryMetadata
            if (JQueryMetadata.isValid(upstreamDependency)) {
                final JavaType controller = JQueryMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = JQueryMetadata
                        .getPath(upstreamDependency);

                // Create the target meta-data identification that will
                // receive the notification
                downstreamDependency = JQueryJspMetadata.createIdentifier(
                        controller, path);
            }

            // If source meta-data identification (requester/notifier) is
            // WebFinderMetadata
            else if (WebFinderMetadata.isValid(upstreamDependency)) {
                final JavaType controller = WebFinderMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = WebFinderMetadata
                        .getPath(upstreamDependency);
                downstreamDependency = JQueryJspMetadata.createIdentifier(
                        controller, path);
            }
            else {
                // dependency not handled: nothing to do
                return;
            }

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (getMetadataDependencyRegistry().getDownstream(
                    upstreamDependency).contains(downstreamDependency)) {
                return;
            }

            // Notify to target meta-data identification, simply call
            // of the meta-data Provider. In this case the downstream is
            // JQueryJspMetadata then
            // {@link JQueryJspMetadataListener#get(String)} will be called
            // Note that evictAndGet method register downstreamDependency below
            // related to current upstreamDependency automatically, so next
            // method executions the if condition above will be true
            getMetadataService().evictAndGet(downstreamDependency);
        }
    }

    public MetadataDependencyRegistry getMetadataDependencyRegistry() {
        if (metadataDependencyRegistry == null) {
            // Get all Services implement MetadataDependencyRegistry interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataDependencyRegistry.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataDependencyRegistry) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataDependencyRegistry on JQueryJspMetadataListener.");
                return null;
            }
        }
        else {
            return metadataDependencyRegistry;
        }
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on JQueryJspMetadataListener.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public JQueryOperations getOperations() {
        if (operations == null) {
            // Get all Services implement JQueryOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                JQueryOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (JQueryOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load JQueryOperations on JQueryJspMetadataListener.");
                return null;
            }
        }
        else {
            return operations;
        }
    }

}
