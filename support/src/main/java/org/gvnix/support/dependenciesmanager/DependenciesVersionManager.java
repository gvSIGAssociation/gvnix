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
package org.gvnix.support.dependenciesmanager;

import java.util.List;
import java.util.Set;

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.w3c.dom.Element;

/**
 * Offers some utilities in order to manage dependencies and properties values
 * in the project's pom.xml
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 0.7
 */
public class DependenciesVersionManager {

    /**
     * Given a list of DOM elements representing Maven dependencies determines
     * if may add or not them to project's pom.xml
     * 
     * @param metadataService
     * @param projectOperations
     * @param dependenciesElements
     * @return true if a dependency has been added or updated, false otherwise
     */
    public static boolean manageDependencyVersion(
            MetadataService metadataService,
            ProjectOperations projectOperations,
            List<Element> dependenciesElements) {
        // Get project metadata in order to check existing properties
        ProjectMetadata md = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier(projectOperations
                        .getFocusedModuleName()));
        if (md == null) {
            return false;
        }

        boolean updateDependency = true;
        Set<Dependency> results;
        Dependency dependency = null;
        for (Element depen : dependenciesElements) {
            dependency = new Dependency(depen);
            // Get existing dependencies for check them against new dependencies
            results = projectOperations.getFocusedModule()
                    .getDependenciesExcludingVersion(dependency);

            VersionInfo existingDepVersionInfo = null;
            VersionInfo newDepVersionInfo = VersionInfo
                    .extractVersionInfoFromString(dependency.getVersion());

            for (Dependency existingDependency : results) {
                existingDepVersionInfo = VersionInfo
                        .extractVersionInfoFromString(existingDependency
                                .getVersion());
                if (existingDepVersionInfo != null) {
                    // Remove existing dependency in pom.xml just if it's older
                    // than the new one
                    if (existingDepVersionInfo.compareTo(newDepVersionInfo) < 0) {
                        projectOperations.removeDependency(
                                projectOperations.getFocusedModuleName(),
                                existingDependency);
                        updateDependency = true;
                    }
                    else {
                        updateDependency = false;
                    }
                }
            }
            // Add the new dependency just if needed
            if (updateDependency) {
                projectOperations.addDependency(
                        projectOperations.getFocusedModuleName(), dependency);
            }
        }
        return updateDependency;
    }

    /**
     * Given a list of DOM elements representing Maven properties determines if
     * may add or not them to project's pom.xml
     * 
     * @param metadataService
     * @param projectOperations
     * @param projectProperties
     * @return true if a property has been added or updated, false otherwise
     */
    public static boolean managePropertyVersion(
            MetadataService metadataService,
            ProjectOperations projectOperations, List<Element> projectProperties) {

        boolean propertiesUpdated = false;

        // Get project metadata in order to check existing properties
        ProjectMetadata md = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier(projectOperations
                        .getFocusedModuleName()));
        if (md == null) {
            return propertiesUpdated;
        }

        Set<Property> results = null;
        Property property = null;
        VersionInfo existingPropVersionInfo = null;
        VersionInfo newPropVersionInfo = null;
        for (Element elemProperty : projectProperties) {
            propertiesUpdated = true;
            // Create a new property instance for the property in add-on config
            property = new Property(elemProperty);
            newPropVersionInfo = VersionInfo
                    .extractVersionInfoFromString(property.getValue());
            // Get existing properties for check them against new properties
            results = projectOperations.getFocusedModule()
                    .getPropertiesExcludingValue(property);
            for (Property existingProperty : results) {
                existingPropVersionInfo = VersionInfo
                        .extractVersionInfoFromString(existingProperty
                                .getValue());
                if (existingPropVersionInfo != null) {
                    // Remove existing property in pom.xml just if it's older
                    // than the new one
                    if (existingPropVersionInfo.compareTo(newPropVersionInfo) < 0) {
                        // We don't need to remove the property since it's
                        // defined and addProperty will replace it, so, just
                        // mark is as updatable
                        // projectOperations.removeProperty(property);
                        propertiesUpdated = true;
                    }
                    else {
                        propertiesUpdated = false;
                    }
                }
            }
            // Add the new property just if needed
            if (propertiesUpdated) {
                projectOperations.addProperty(projectOperations
                        .getFocusedModuleName(), new Property(elemProperty));
            }
        }
        return propertiesUpdated;
    }

}
