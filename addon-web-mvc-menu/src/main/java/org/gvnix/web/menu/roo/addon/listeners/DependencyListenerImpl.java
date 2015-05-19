/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.web.menu.roo.addon.listeners;

import java.util.logging.Logger;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.menu.roo.addon.MenuEntryOperations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Check for Spring Security dependency changes to update menu artifacts.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component
@Service
public class DependencyListenerImpl implements MenuDependencyListener {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(DependencyListenerImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private MetadataDependencyRegistry metadataDependencyRegistry;

    /**
     * Use PageOperations to execute operations
     */
    private MenuEntryOperations operations;
    private ProjectOperations projectOperations;

    private Boolean hasSpringSecurity = null;

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
            // Check if gvNIX menu is installed
            if (getMenuEntryOperations().isGvNixMenuAvailable()) {
                // if dependency changes or its first call
                if (!ObjectUtils.equals(getMenuEntryOperations()
                        .isSpringSecurityInstalled(), hasSpringSecurity)) {
                    LOGGER.finest("Spring Security changed or startup");
                    // update hasSpringSecurity variable and update artifacts
                    hasSpringSecurity = getMenuEntryOperations()
                            .isSpringSecurityInstalled();
                    getMenuEntryOperations().createWebArtefacts("~.web.menu");
                }

                // If Bootstrap is installed and gvNIX Bootstrap menu is not
                // installed
                // is neccessary to install it.
                if (getProjectOperations().isFeatureInstalledInFocusedModule(
                        "gvnix-bootstrap")
                        && !getMenuEntryOperations()
                                .isGvNixMenuBootstrapAvailable()) {
                    getMenuEntryOperations().setupBootstrapMenu();
                }

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

    public MenuEntryOperations getMenuEntryOperations() {
        if (operations == null) {
            // Get all Services implement MenuEntryOperations interface
            try {
                ServiceReference[] references = context
                        .getAllServiceReferences(
                                MenuEntryOperations.class.getName(), null);

                for (ServiceReference ref : references) {
                    operations = (MenuEntryOperations) context.getService(ref);
                    return operations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MenuEntryOperations on DependencyListenerImpl.");
                return null;
            }
        }
        else {
            return operations;
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
}
