package org.gvnix.addon.geo;

import java.util.Collections;
import java.util.List;

import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

public class GeoUtils {

    /**
     * 
     * This method updates dependencies and repositories with the added to
     * configuration.xml file
     * 
     * @param configuration
     * @param moduleName
     * @param projectOperations
     */
    public static void updatePom(final Element configuration,
            ProjectOperations projectOperations, MetadataService metadataService) {

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepositories(
                    projectOperations.getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }

}
