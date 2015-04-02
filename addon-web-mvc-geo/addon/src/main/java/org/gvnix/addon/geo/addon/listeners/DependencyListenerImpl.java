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
package org.gvnix.addon.geo.addon.listeners;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.geo.addon.GeoOperations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

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
public class DependencyListenerImpl implements GeoDependencyListener {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(DependencyListenerImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private MetadataDependencyRegistry metadataDependencyRegistry;

    private ProjectOperations projectOperations;

    /**
     * Use PageOperations to execute operations
     */
    private GeoOperations operations;

    protected void activate(final ComponentContext context) {
        this.context = context.getBundleContext();
        getMetadataDependencyRegistry().addNotificationListener(this);
    }

    protected void deactivate(ComponentContext context) {
        getMetadataDependencyRegistry().removeNotificationListener(this);
    }

    /**
     * {@inheritDoc} When Project metadata changes look for SpringSecurity
     * dependency and update menu artifacts.
     */
    public void notify(String upstreamDependency, String downstreamDependency) {
        // Check if is project metadata
        if (ProjectMetadata.isValid(upstreamDependency)) {
            if (getProjectOperations().isFeatureInstalledInFocusedModule(
                    "gvnix-bootstrap")
                    && getGeoOperations().isInstalledInModule(
                            "gvnix-geo-component")) {
                getGeoOperations().updateGeoAddonToBootstrap();
            }
        }
    }

    public MetadataDependencyRegistry getMetadataDependencyRegistry() {
        if (metadataDependencyRegistry == null) {
            // Get all Services implement MetadataDependencyRegistry interface
            try {
                ServiceReference[] references = context
                        .getAllServiceReferences(
                                MetadataDependencyRegistry.class.getName(),
                                null);

                for (ServiceReference ref : references) {
                    metadataDependencyRegistry = (MetadataDependencyRegistry) context
                            .getService(ref);
                    return metadataDependencyRegistry;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataDependencyRegistry on DependencyListenerImpl.");
                return null;
            }
        }
        else {
            return metadataDependencyRegistry;
        }
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference[] references = context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference ref : references) {
                    projectOperations = (ProjectOperations) context
                            .getService(ref);
                    return projectOperations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on DependencyListenerImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public GeoOperations getGeoOperations() {
        if (operations == null) {
            // Get all Services implement GeoOperations interface
            try {
                ServiceReference[] references = context
                        .getAllServiceReferences(GeoOperations.class.getName(),
                                null);

                for (ServiceReference ref : references) {
                    operations = (GeoOperations) context.getService(ref);
                    return operations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load GeoOperations on DependencyListenerImpl.");
                return null;
            }
        }
        else {
            return operations;
        }
    }
}
