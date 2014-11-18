/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.geo.listeners;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.geo.GeoOperations;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;

/**
 * Check for Bootstrap dependency changes to update Geo Components.
 * 
 * @author Juan Carlos García( jcgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class DependencyListenerImpl implements MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private ProjectOperations projectOperations;

    /**
     * Use PageOperations to execute operations
     */
    @Reference
    private GeoOperations operations;

    protected void activate(ComponentContext context) {
        this.metadataDependencyRegistry.addNotificationListener(this);
    }

    protected void deactivate(ComponentContext context) {
        this.metadataDependencyRegistry.removeNotificationListener(this);
    }

    /**
     * {@inheritDoc} When Project metadata changes look for SpringSecurity
     * dependency and update menu artifacts.
     */
    public void notify(String upstreamDependency, String downstreamDependency) {
        // Check if is project metadata
        if (ProjectMetadata.isValid(upstreamDependency)) {
            if (projectOperations
                    .isFeatureInstalledInFocusedModule("gvnix-bootstrap")
                    && operations.isInstalledInModule("gvnix-geo-component")) {
                operations.updateGeoAddonToBootstrap();
            }
        }
    }
}
