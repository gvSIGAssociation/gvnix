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
package org.gvnix.web.report.roo.addon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Service offering some configuration operations
 *
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.7
 */
@Component
@Service
public class ReportConfigServiceImpl implements ReportConfigService {

    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;

    private ComponentContext context;

    /** webmvc-config.xml path */
    private String springMvcConfigFile;

    /** jasper-views.xml path */
    private String jasperViewsConfigFile;

    /** {@inheritDoc} */
    public void setup() {
        Assert.isTrue(isSpringMvcProject(),
                "Project must be Spring MVC project");
        Element configuration = XmlUtils.getConfiguration(getClass(),
                "configuration.xml");

        // Add addon repository and dependency to get annotations
        addAnnotations(configuration);

        // Add properties to pom
        updatePomProperties(configuration);

        // Add JasperReports dependencies to pom
        updateDependencies(configuration);

        // Add JasperReports plugin to pom
        // TODO: The jasperreports-maven-plugin doesn't support the current
        // version of JasperReports lib.
        // updatePlugins(configuration);

        // Add JasperReports View resolver to webmvc-config.xml and create the
        // jasper-views.xml config file.
        addJasperReportsViewResolver();

        // Install font family extension in the project
        installJasperReportsExtensionFonts();
    }

    /**
     * Add the ViewResolver config to webmvc-config.xml
     */
    public final void addJasperReportsViewResolver() {
        PathResolver pathResolver = projectOperations.getPathResolver();

        // Add config to MVC app context
        String mvcConfig = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");
        MutableFile mutableMvcConfigFile = fileManager.updateFile(mvcConfig);
        Document mvcConfigDocument;
        try {
            mvcConfigDocument = XmlUtils.getDocumentBuilder().parse(
                    mutableMvcConfigFile.getInputStream());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open Spring MVC config file '" + mvcConfig + "'",
                    ex);
        }

        Element beans = mvcConfigDocument.getDocumentElement();

        if (null == XmlUtils.findFirstElement(
                "/beans/bean[@id='jasperReportsXmlViewResolver']", beans)) {
            InputStream configTemplateInputStream = TemplateUtils.getTemplate(
                    getClass(), "jasperreports-mvc-config-template.xml");
            Assert.notNull(configTemplateInputStream,
                    "Could not acquire jasperreports-mvc-config-template.xml file");
            Document configDoc;
            try {
                configDoc = XmlUtils.getDocumentBuilder().parse(
                        configTemplateInputStream);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            Element configElement = (Element) configDoc.getFirstChild();
            Element jasperReportsBeanConfig = XmlUtils.findFirstElement(
                    "/config/bean", configElement);

            Node importedElement = mvcConfigDocument.importNode(
                    jasperReportsBeanConfig, true);
            beans.appendChild(importedElement);

            XmlUtils.writeXml(mutableMvcConfigFile.getOutputStream(),
                    mvcConfigDocument);
            try {
                configTemplateInputStream.close();
            } catch (IOException ignore) {
            }
        }

        if (fileManager.exists(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/jasper-views.xml"))) {
            fileManager.scan();
            // This file already exists, nothing to do
            return;
        }

        // Add jasper-views.xml file
        MutableFile mutableFile;
        byte[] jasperViewsConfigTemplate;
        InputStream jasperReportsTemplateInputStream;

        try {
            jasperReportsTemplateInputStream = TemplateUtils.getTemplate(
                    getClass(), "jasperreports-views-config-template.xml");
            jasperViewsConfigTemplate = FileCopyUtils
                    .copyToByteArray(jasperReportsTemplateInputStream);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to load jasperreports-views-config-template.xml",
                    ex);
        }

        String jasperViesFileDestination = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/spring/jasper-views.xml");
        if (!fileManager.exists(jasperViesFileDestination)) {
            mutableFile = fileManager.createFile(jasperViesFileDestination);
            Assert.notNull(mutableFile,
                    "Could not create JasperReports views definition file '"
                            + jasperViesFileDestination + "'");
        } else {
            mutableFile = fileManager.updateFile(jasperViesFileDestination);
        }

        try {
            FileCopyUtils.copy(jasperViewsConfigTemplate,
                    mutableFile.getOutputStream());
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not output '"
                    + mutableFile.getCanonicalPath() + "'", ioe);
        }

        try {
            jasperReportsTemplateInputStream.close();
        } catch (IOException ignore) {
        }
        fileManager.scan();
    }

    /** {@inheritDoc} */
    public boolean isJasperViewsProject() {
        return fileManager.exists(getJasperViewsFile());
    }

    /** {@inheritDoc} */
    public boolean isSpringMvcProject() {
        return fileManager.exists(getSpringMvcConfigFile());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Do not permit ad reports unless they have a web project with Spring MVC
     * Tiles.
     */
    public boolean isSpringMvcTilesProject() {
        return fileManager.exists(getSpringMvcConfigFile())
                && fileManager.exists(getTilesLayoutsFile());
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



    /** {@inheritDoc} */
    public String getJasperViewsFile() {
        // resolve path for jasper-views.xml if it hasn't been resolved yet
        if (jasperViewsConfigFile == null) {
            jasperViewsConfigFile = projectOperations.getPathResolver()
                    .getIdentifier(Path.SRC_MAIN_WEBAPP,
                            "WEB-INF/spring/jasper-views.xml");
        }
        return jasperViewsConfigFile;
    }

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    /**
     * Get the absolute path for {@code layouts.xml}.
     * <p>
     * Note that this file is required for any Tiles project.
     *
     * @return the absolute path to file (never null)
     */
    private String getTilesLayoutsFile() {

        // resolve absolute path for menu.jspx if it hasn't been resolved yet
        return projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, "/WEB-INF/layouts/layouts.xml");
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
     * Install properties defined in external XML file
     *
     * @param configuration
     */
    private void updatePomProperties(Element configuration) {
        List<Element> addonProperties = XmlUtils.findElements(
                "/configuration/gvnix/web-report/properties/*", configuration);
        for (Element property : addonProperties) {
            projectOperations.addProperty(new Property(property));
        }
    }

    /**
     * Install dependencies defined in external XML file
     *
     * @param configuration
     */
    private void updateDependencies(Element configuration) {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        List<Element> jasperReportDependencies = XmlUtils.findElements(
                "/configuration/gvnix/jasperReports/dependencies/dependency",
                configuration);
        for (Element dependencyElement : jasperReportDependencies) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.addDependencies(dependencies);
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
     * Install a font family in the project and the
     * jasperreport_extension.properties file
     *
     * This extension and the font family are needed because is the current way
     * to have bold, italic and underline fonts in the reports output. <a href=
     * "http://sites.google.com/site/xmedeko/code/misc/jasperreports-pdf-font-mapping"
     * >jasperreports-pdf-font-mapping</a>. We apply the same solution but
     * without a jar file, just creating the files in the application classpath.
     */
    private void installJasperReportsExtensionFonts() {
        PathResolver pathResolver = projectOperations.getPathResolver();

        InputStream configTemplateInputStream = TemplateUtils
                .getTemplate(getClass(),
                        "jasperfonts-extension/jasperreports_extension-template.properties");
        String jasperReportExtensionPropTemplate;
        try {
            jasperReportExtensionPropTemplate = FileCopyUtils
                    .copyToString(new InputStreamReader(
                            configTemplateInputStream));
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable load jasperreports_extension-template.properties",
                    ioe);
        } finally {
            try {
                configTemplateInputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Error creating jasperreports_extension.properties in project",
                        e);
            }
        }
        String jasperReportExtensionProp = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP,
                "WEB-INF/classes/jasperreports_extension.properties");
        fileManager.createOrUpdateTextFileIfRequired(jasperReportExtensionProp,
                jasperReportExtensionPropTemplate, false);
        String classesPathDest = projectOperations.getPathResolver()
                .getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "WEB-INF/classes/jasperfonts");

        copyDirectoryContents("jasperfonts-extension/jasperfonts/**",
                classesPathDest);

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
