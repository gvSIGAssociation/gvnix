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
package org.gvnix.addon.jpa.addon;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 * 
 * @since 1.1
 */
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Component
@Service
public class JpaOperationsImpl implements JpaOperations {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(JpaOperationsImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private ProjectOperations projectOperations;

    private MetadataService metadataService;

    protected void activate(final ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /** {@inheritDoc} */
    public boolean isSetupAvailable() {
        // Check if JPA is installed
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FeatureNames.JPA)
                && !getProjectOperations().isFeatureInstalledInFocusedModule(
                        FEATURE_NAME_GVNIX_JPA);
    }

    public void setup() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            getProjectOperations().addRepositories(
                    getProjectOperations().getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            getProjectOperations().addProperty(
                    getProjectOperations().getFocusedModuleName(),
                    new Property(property));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(
                getMetadataService(), getProjectOperations(), depens);
    }

    /**
     * Gets the feature name managed by this operations class.
     * 
     * @return feature name
     */
    public String getName() {
        return FEATURE_NAME_GVNIX_JPA;
    }

    /**
     * Returns true if the given feature is installed in current project.
     * 
     * @param moduleName feature name to check in current project
     * @return true if given feature name is installed, otherwise returns false
     */
    public boolean isInstalledInModule(final String moduleName) {
        final Pom pom = getProjectOperations().getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }

        // Look for gvnix jpa dependency
        for (final Dependency dependency : pom.getDependencies()) {
            if ("org.gvnix.addon.jpa".equals(dependency.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services ProjectOperations getMetadataService()
            // interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on JpaOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }

    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services MetadataService
            // interface
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
                LOGGER.warning("Cannot load MetadataService on JpaOperationsImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }

    }
}