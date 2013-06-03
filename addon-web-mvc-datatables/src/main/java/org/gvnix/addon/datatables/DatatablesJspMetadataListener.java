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
package org.gvnix.addon.datatables;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.finder.WebFinderMetadata;
import org.springframework.roo.addon.web.mvc.jsp.JspMetadata;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * @author gvNIX Team
 */
@Component(immediate = true)
@Service
public class DatatablesJspMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private MetadataService metadataService;

    @Reference
    private DatatablesOperations operations;

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                DatatablesMetadata.getMetadataIdentiferType(),
                getProvidesType());
        metadataDependencyRegistry
                .registerDependency(
                        WebFinderMetadata.getMetadataIdentiferType(),
                        getProvidesType());
        metadataDependencyRegistry.addNotificationListener(this);
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                DatatablesMetadata.getMetadataIdentiferType(),
                getProvidesType());
        metadataDependencyRegistry
                .deregisterDependency(
                        WebFinderMetadata.getMetadataIdentiferType(),
                        getProvidesType());
        metadataDependencyRegistry.removeNotificationListener(this);
    }

    public MetadataItem get(final String datatablesJspMetadataId) {

        // Get controller
        final JavaType controller = DatatablesJspMetadata
                .getJavaType(datatablesJspMetadataId);

        // Locate datatables metadata
        final String datatablesMetadataKey = DatatablesMetadata
                .createIdentifier(controller,
                        DatatablesJspMetadata.getPath(datatablesJspMetadataId));
        final DatatablesMetadata datatablesMetadata = (DatatablesMetadata) metadataService
                .get(datatablesMetadataKey);

        // Check datatables metadata
        if (datatablesMetadata == null || !datatablesMetadata.isValid()) {
            // Can't get the corresponding datatables information, so we
            // certainly don't need
            // to manage any JSPs at this time
            return null;
        }

        // Call to operations for update pages
        operations.updateControllerJspPages(controller, datatablesMetadata);

        return new DatatablesJspMetadata(datatablesJspMetadataId,
                datatablesMetadata);
    }

    public String getProvidesType() {
        return DatatablesJspMetadata.getMetadataIdentiferType();
    }

    public void notify(final String upstreamDependency,
            String downstreamDependency) {

        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            if (DatatablesMetadata.isValid(upstreamDependency)) {
                final JavaType controller = DatatablesMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = DatatablesMetadata
                        .getPath(upstreamDependency);
                downstreamDependency = DatatablesJspMetadata.createIdentifier(
                        controller, path);

                // register dependency with JPS Metadata
                String jspMetadataId = JspMetadata.createIdentifier(controller,
                        path);
                metadataDependencyRegistry.registerDependency(jspMetadataId,
                        downstreamDependency);

            }
            else if (WebFinderMetadata.isValid(upstreamDependency)) {
                final JavaType controller = WebFinderMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = WebFinderMetadata
                        .getPath(upstreamDependency);
                downstreamDependency = DatatablesJspMetadata.createIdentifier(
                        controller, path);

                // register dependency with JPS Metadata
                String jspMetadataId = JspMetadata.createIdentifier(controller,
                        path);
                metadataDependencyRegistry.registerDependency(jspMetadataId,
                        downstreamDependency);
            }
            else if (JspMetadata.isValid(upstreamDependency)) {
                final JavaType controller = WebFinderMetadata
                        .getJavaType(upstreamDependency);
                final LogicalPath path = WebFinderMetadata
                        .getPath(upstreamDependency);
                downstreamDependency = DatatablesJspMetadata.createIdentifier(
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
            metadataService.evictAndGet(downstreamDependency);
        }
    }

}
