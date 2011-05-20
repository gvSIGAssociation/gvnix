/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.screen.roo.addon;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Service offering some configuration operations
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public class WebScreenConfigServiceImpl implements WebScreenConfigService {

    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;

    private ComponentContext context;

    /** webmvc-config.xml path */
    private String springMvcConfigFile;

    /** {@inheritDoc} */
    public void setup() {
        Assert.isTrue(isSpringMvcProject(),
                "Project must be Spring MVC project");
        Element configuration = XmlUtils.getConfiguration(getClass(),
                "configuration.xml");

        // Add addon repository and dependency to get annotations
        addAnnotations(configuration);

        // Add resources
        addResources();

    }

    /**
     * Adds resource to project
     */
    private void addResources() {
        // TODO

    }

    /** {@inheritDoc} */
    public boolean isSpringMvcProject() {
        return fileManager.exists(getSpringMvcConfigFile());
    }

    /** {@inheritDoc} */
    public String getSpringMvcConfigFile() {
        // resolve path for spring-mvc.xml if it hasn't been resolved yet
        if (springMvcConfigFile == null) {
            springMvcConfigFile = projectOperations.getPathResolver()
                    .getIdentifier(Path.SRC_MAIN_WEBAPP,
                            "WEB-INF/spring/webmvc-config.xml");
        }
        return springMvcConfigFile;
    }

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    /**
     * Add addon repository and dependency to get annotations.
     * 
     * @param configuration
     *            Configuration element
     */
    private void addAnnotations(Element configuration) {

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {

            projectOperations.addRepository(new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);
        for (Element depen : depens) {

            projectOperations.addDependency(new Dependency(depen));
        }
    }

    /**
     * Install plugins defined in external XML file
     * 
     * @param configuration
     */
    @SuppressWarnings("unused")
    private void updatePlugins(Element configuration) {
        List<Element> jasperReportsPlugins = XmlUtils.findElements(
                "/configuration/gvnix/jasperReports/plugins/plugin",
                configuration);
        for (Element pluginElement : jasperReportsPlugins) {
            projectOperations.addBuildPlugin(new Plugin(pluginElement));
        }
    }

    /**
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath
     *            the source path
     * @param targetDirectory
     *            the target directory
     */
    @SuppressWarnings("unused")
    private void copyDirectoryContents(String sourceAntPath,
            String targetDirectory) {
        Assert.hasText(sourceAntPath, "Source path required");
        Assert.hasText(targetDirectory, "Target directory required");

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        String path = TemplateUtils.getTemplatePath(getClass(), sourceAntPath);
        Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(
                context.getBundleContext(), path);
        Assert.notNull(urls,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            if (!fileManager.exists(targetDirectory + fileName)) {
                try {
                    FileCopyUtils.copy(url.openStream(), fileManager
                            .createFile(targetDirectory + fileName)
                            .getOutputStream());
                } catch (IOException e) {
                    new IllegalStateException(
                            "Encountered an error during copying of resources for MVC JSP addon.",
                            e);
                }
            }
        }
    }
}
