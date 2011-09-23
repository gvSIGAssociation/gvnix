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
package org.gvnix.web.theme.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.theme.roo.addon.util.FileUtils;
import org.gvnix.web.theme.roo.addon.util.I18nUtils;
import org.gvnix.web.theme.roo.addon.util.XmlUtils;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.JspOperationsImpl;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Execution;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathInformation;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Theme management operations for Spring MVC web layer:
 * <ul>
 * <li>Find theme repositories: bundles and local directories can act as theme
 * repository.
 * <ul>
 * <li>bundles let us to distribute themes via OBR.</li>
 * <li>local repository let us to share themes in one organization.</li>
 * </ul>
 * </li>
 * <li>Copy themes from repositories to project resources directory.</li>
 * <li>Set the project active theme</li>
 * </ul>
 * A theme is a directory that contains a set of MVC artefacts and having the
 * same directory structure than the structure created by Roo MVC add-ons
 * because a theme will overwrite default Roo MVC artefacts to set the desired
 * style.
 * <p>
 * A theme must contain the theme descriptor file,
 * [THEME_DIR]/WEB-INF/views/theme.xml. Only those directories that contains the
 * file /WEB-INF/views/theme.xml will be considered as valid themes.
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
@Component
// use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class ThemeOperationsImpl extends AbstractOperations implements
        ThemeOperations {

    /** Logger */
    private static final Logger logger = HandlerUtils
            .getLogger(ThemeOperationsImpl.class);

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadate by its MID
     */
    @Reference
    private MetadataService metadataService;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * Use PropFileOperations for property file configuration operations.
     */
    @Reference
    private PropFileOperations propFileOperations;

    /** Path identifier for project themes directory */
    public static final Path SRC_MAIN_THEMES = new Path("SRC_MAIN_THEMES");

    /** Path identifier for themes repository */
    public static final Path THEMES_REPOSITORY = new Path("THEMES_REPOSITORY");

    /** Themes installation path */
    private PathInformation themesPath = null;

    /** Themes repository path */
    private PathInformation themesRepositoryPath = null;

    // Public operations -----

    /** {@inheritDoc} */
    public boolean isProjectAvailable() {
        // Check if a project has been created
        return projectOperations.isProjectAvailable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Themes can be installed from bundles and local repository, so this method
     * checks if there are themes in OSGi bundles or in local repository
     * 
     * @return true if there are themes available in bundles or repository,
     *         otherwise returns false
     */
    public boolean isThemesAvailable() {
        return !findThemeDescriptors().isEmpty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Do not permit installation unless they have a web project with Spring MVC
     * Tiles.
     */
    public boolean isSpringMvcTilesProject() {
        return fileManager.exists(getMvcConfigFile())
                && fileManager.exists(getTilesLayoutsFile());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method will copy the given theme located in a bundle or located in a
     * local themes repository to {@code Path.SRC_MAIN_RESOURCES/themes}. It
     * creates the directory if it doesn't exist.
     * <p>
     * Note that installation will overwrite installed themes. Useful to reset
     * installed themes.
     */
    public void installThemeArtefacts(String id) {
        Assert.hasText(id, "Theme ID to match is required");

        // Iterate over available themes and install the matching theme
        List<Theme> themes = getAvailableThemes();

        for (Theme theme : themes) {
            if (theme.getId().equals(id)) {

                // destination = [PROJECT-THEMES-PATH]/THEME-ID/
                File destination = new File(getThemesPath().getLocation(), id);

                // get source URI from which get Theme artefacts
                URI sourceURI = theme.getRootURI();

                // it shouldn't occur
                Assert.notNull(sourceURI,
                        "Could not determine schema for resources for Theme '"
                                .concat(id).concat("'"));

                // copy overwriting installed themes
                copyRecursively(sourceURI, destination, true);

                // not needed to continue the iteration
                break;
            }
        }

    }

    /**
     * {@inheritDoc}
     * <p>
     * This activate method will copy the given theme from project's theme
     * folder {@link #SRC_MAIN_THEMES} to {@link Path#SRC_MAIN_WEBAPP} directory
     * and will update the theme.xml file.
     * <p>
     * {@link #updateProperties(Theme)} copy theme i18n messages to project
     * properties files.
     * <p>
     * Finally, {@link #updateSpringWebCtx(String)} updates
     * <em>webmvc-config.xml</em>.
     */
    public void setActive(String id) {

        List<Theme> themes = getInstalledThemes();

        Theme source = null;
        for (Theme theme : themes) {
            if (theme.getId().equals(id)) {
                source = theme;
            }
        }

        Assert.notNull(source, "Theme '".concat(id)
                .concat("' isn't installed."));

        // destination = [PROJECT-THEMES-PATH]/THEME-ID/
        String destinationPath = getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, "");
        File destination = new File(destinationPath);

        // before overwrite current web artifacts, load active languages. The
        // goal is to copy same languages to theme footer.jspx (if theme
        // contains
        // it)
        Set<I18n> languages = getInstalledI18n();

        // copy theme from project installation dir to src/main/webapp
        copyRecursively(source.getRootURI(), destination, true);

        // install application.css in styles folder
        installApplicationStyle();

        // modify load-scripts.tagx
        modifyLoadScriptsTagx();

        // install theme properties files
        updateProperties(source);

        // update webmvc-config.xml to set theme as default and add theme
        // message bundles locations
        updateSpringWebCtx(id);

        // when setting theme-cit modify pom.xml with required elements
        if (id.equalsIgnoreCase("cit")) {
            installApplicationVersionProperties();
            updatePomFile();
        }

        // update theme anchor to change to this theme
        updateThemeLinks(source);

        // update footer to add the active languages
        setInstalledI18n(languages);

    }

    /**
     * Install applicationversion.properties file in destination project
     * <p>
     * This file is used as resource bundle of application version number
     * message
     */
    private void installApplicationVersionProperties() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        InputStream appVersionPropIS = TemplateUtils.getTemplate(getClass(),
                "applicationversion-template.properties");
        String appVersionPropTemplate;
        try {
            appVersionPropTemplate = FileCopyUtils
                    .copyToString(new InputStreamReader(appVersionPropIS));
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable load applicationversion-template.properties", ioe);
        } finally {
            try {
                appVersionPropIS.close();
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Error creating jasperreports_extension.properties in project",
                        e);
            }
        }

        String appVersionProp = pathResolver.getIdentifier(
                Path.SRC_MAIN_RESOURCES, "applicationversion.properties");
        fileManager.createOrUpdateTextFileIfRequired(appVersionProp,
                appVersionPropTemplate, false);
    }

    /**
     * Update pom.xml adding a plugin maven-resources-plugin or, if it exists,
     * just adding executions component
     * <p>
     * TODO
     * <p>
     * This source is so because Roo doesn't support Execution with
     * Configuration. When it support the thing we could change this code with
     * something like:
     * <code>projectOperations.updateBuildPlugin(pluginWithExecutionConfigured)</code>
     * This issue is solved in 1.2.0.M1 as ROO-2658 says
     */
    private void updatePomFile() {

        Element configuration = org.springframework.roo.support.util.XmlUtils
                .getConfiguration(getClass(), "configuration.xml");
        Element pluginElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/configuration/plugin", configuration);

        Plugin plugin = new Plugin(pluginElement);

        Element executionElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("executions/execution", pluginElement);
        Execution pluginExecution = createExecutionFromElement(executionElement);

        String pom = projectOperations.getPathResolver().getIdentifier(
                Path.ROOT, "pom.xml");
        Document pomDoc = org.springframework.roo.support.util.XmlUtils
                .readXml(fileManager.getInputStream(pom));
        Element root = pomDoc.getDocumentElement();

        // Plugins section: find or create if not exists
        Element build = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/project/build", root);
        Element plugins = null;
        if (build != null) {
            plugins = org.springframework.roo.support.util.XmlUtils
                    .findFirstElement("plugins", build);
            if (plugins != null) {
                String pluginXPath = "plugin[groupId='"
                        .concat(plugin.getGroupId())
                        .concat("' and artifactId='")
                        .concat(plugin.getArtifactId())
                        .concat("' and version='").concat(plugin.getVersion())
                        .concat("']");
                Element existingPluginElement = org.springframework.roo.support.util.XmlUtils
                        .findFirstElement(pluginXPath, plugins);
                if (existingPluginElement != null) {
                    String pluginExecutionXPath = "executions/execution[id='"
                            .concat(pluginExecution.getId())
                            .concat("' and phase='")
                            .concat(pluginExecution.getPhase())
                            .concat("' and goals[goal='")
                            .concat(pluginExecution.getGoals().get(0))
                            .concat("']]");
                    Element existingPluginExecutionElement = org.springframework.roo.support.util.XmlUtils
                            .findFirstElement(pluginExecutionXPath,
                                    existingPluginElement);
                    if (existingPluginExecutionElement == null) {
                        Element pluginExecutionsElement = org.springframework.roo.support.util.XmlUtils
                                .findFirstElement("executions",
                                        existingPluginElement);
                        if (pluginExecutionsElement == null) {
                            pluginExecutionsElement = new XmlElementBuilder(
                                    "executions", pomDoc).build();
                        }
                        Node importedExecutionPlugin = pomDoc.importNode(
                                executionElement, true);

                        pluginExecutionsElement
                                .appendChild(importedExecutionPlugin);
                        existingPluginElement
                                .appendChild(pluginExecutionsElement);
                    }
                } else {
                    // create maven-resources-plugin
                    Node importedPlugin = pomDoc
                            .importNode(pluginElement, true);
                    plugins.appendChild(importedPlugin);
                }
            } else {
                // create plugins with maven-resources-plugin
                plugins = pomDoc.createElement("plugins");
                Node importedPlugin = pomDoc.importNode(pluginElement, true);
                plugins.appendChild(importedPlugin);
                build.appendChild(plugins);
            }
        } else {
            // create build and plugins with maven-resources-plugin
            build = pomDoc.createElement("build");
            plugins = pomDoc.createElement("plugins");
            Node importedPlugin = pomDoc.importNode(pluginElement, true);
            plugins.appendChild(importedPlugin);
            build.appendChild(plugins);
            root.appendChild(build);
        }

        fileManager.createOrUpdateTextFileIfRequired(pom,
                org.springframework.roo.support.util.XmlUtils
                        .nodeToString(pomDoc), true);

    }

    /**
     * Returns the @org.springframework.roo.project.Execution instance from XML
     * Element representing it
     * 
     * @param executionElement
     * @return
     */
    private Execution createExecutionFromElement(Element executionElement) {
        String executionId = org.springframework.roo.support.util.XmlUtils
                .findFirstElementByName("id", executionElement)
                .getTextContent();
        String executionPhase = org.springframework.roo.support.util.XmlUtils
                .findFirstElementByName("phase", executionElement)
                .getTextContent();
        String executionGoal = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("goals/goal", executionElement)
                .getTextContent();
        return new Execution(executionId, executionPhase, executionGoal);
    }

    /**
     * Installs application.css file in project styles folder
     */
    private void installApplicationStyle() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        copyDirectoryContents("styles/*.css",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "/styles"),
                false);
    }

    /**
     * Updates load-scripts.tagx adding in the right position some elements:
     * <ul>
     * <li><code>spring:url</code> elements for JS and CSS</li>
     * <li><code>link</code> element for CSS</li>
     * <li><code>script</code> element for JS</li>
     * </ul>
     */
    private void modifyLoadScriptsTagx() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String loadScriptsTagx = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/util/load-scripts.tagx");

        if (!fileManager.exists(loadScriptsTagx)) {
            // load-scripts.tagx doesn't exist, so nothing to do
            return;
        }

        InputStream loadScriptsIs = fileManager.getInputStream(loadScriptsTagx);

        Document loadScriptsXml;
        try {
            loadScriptsXml = org.springframework.roo.support.util.XmlUtils
                    .getDocumentBuilder().parse(loadScriptsIs);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open load-scripts.tagx file", ex);
        }

        Element lsRoot = loadScriptsXml.getDocumentElement();
        // Add new tag namesapces
        Element jspRoot = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/root", lsRoot);
        jspRoot.setAttribute("xmlns:util", "urn:jsptagdir:/WEB-INF/tags/util");

        Node nextSibiling;

        // spring:url elements
        Element testElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/root/url[@var='roo_css-ie_url']", lsRoot);
        if (testElement == null) {
            Element urlCitIECss = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/styles/cit-IE.css")
                    .addAttribute("var", "roo_css-ie_url").build();
            Element urlApplicationCss = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/styles/application.css")
                    .addAttribute("var", "application_css_url").build();
            Element urlYuiEventJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value",
                            "/resources/scripts/yui/yahoo-dom-event.js")
                    .addAttribute("var", "yui_event").build();
            Element urlYuiCoreJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value",
                            "/resources/scripts/yui/container_core-min.js")
                    .addAttribute("var", "yui_core").build();
            Element urlYoiMenuJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/scripts/yui/menu-min.js")
                    .addAttribute("var", "yui_menu").build();
            Element urlCitJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/scripts/utils.js")
                    .addAttribute("var", "cit_js_url").build();
            List<Element> springUrlElements = org.springframework.roo.support.util.XmlUtils
                    .findElements("/root/url", lsRoot);
            // Element lastSpringUrl = null;
            if (!springUrlElements.isEmpty()) {
                Element lastSpringUrl = springUrlElements.get(springUrlElements
                        .size() - 1);
                if (lastSpringUrl != null) {
                    nextSibiling = lastSpringUrl.getNextSibling()
                            .getNextSibling();
                    lsRoot.insertBefore(urlCitIECss, nextSibiling);
                    lsRoot.insertBefore(urlApplicationCss, nextSibiling);
                    lsRoot.insertBefore(urlYuiEventJs, nextSibiling);
                    lsRoot.insertBefore(urlYuiCoreJs, nextSibiling);
                    lsRoot.insertBefore(urlYoiMenuJs, nextSibiling);
                    lsRoot.insertBefore(urlCitJs, nextSibiling);
                }
            } else {
                // Add at the end of the document
                lsRoot.appendChild(urlCitIECss);
                lsRoot.appendChild(urlApplicationCss);
                lsRoot.appendChild(urlYuiEventJs);
                lsRoot.appendChild(urlYuiCoreJs);
                lsRoot.appendChild(urlYoiMenuJs);
                lsRoot.appendChild(urlCitJs);
            }
        }

        Element setUserLocale = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/root/set/out", lsRoot);
        setUserLocale.setAttribute("default", "es");

        // cit-IE.css stylesheet element
        testElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement(
                        "/root/iecondition/link[@href='${roo_css-ie_url}']",
                        lsRoot);
        if (testElement == null) {
            Element ifIE = new XmlElementBuilder("util:iecondition",
                    loadScriptsXml).build();

            Element linkCitIECss = new XmlElementBuilder("link", loadScriptsXml)
                    .addAttribute("rel", "stylesheet")
                    .addAttribute("type", "text/css")
                    .addAttribute("href", "${roo_css-ie_url}").build();
            ifIE.appendChild(linkCitIECss);
            Node linkFaviconNode = org.springframework.roo.support.util.XmlUtils
                    .findFirstElement("/root/link[@href='${favicon}']", lsRoot);
            if (linkFaviconNode != null) {
                nextSibiling = linkFaviconNode.getNextSibling()
                        .getNextSibling();
                lsRoot.insertBefore(ifIE, linkFaviconNode);
            } else {
                // Add ass last link element
                // Element lastLink = null;
                List<Element> linkElements = org.springframework.roo.support.util.XmlUtils
                        .findElements("/root/link", lsRoot);
                if (!linkElements.isEmpty()) {
                    Element lastLink = linkElements
                            .get(linkElements.size() - 1);
                    if (lastLink != null) {
                        nextSibiling = lastLink.getNextSibling()
                                .getNextSibling();
                        lsRoot.insertBefore(ifIE, nextSibiling);
                    }
                } else {
                    // Add at the end of document
                    lsRoot.appendChild(ifIE);
                }
            }
        }

        // pattern.css stylesheet element
        testElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/root/link[@href='${application_css_url}']",
                        lsRoot);
        if (testElement == null) {
            Element linkApplicationCss = new XmlElementBuilder("link",
                    loadScriptsXml).addAttribute("rel", "stylesheet")
                    .addAttribute("type", "text/css")
                    .addAttribute("media", "screen")
                    .addAttribute("href", "${application_css_url}").build();
            linkApplicationCss.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            Node linkFaviconNode = org.springframework.roo.support.util.XmlUtils
                    .findFirstElement("/root/link[@href='${favicon}']", lsRoot);
            if (linkFaviconNode != null) {
                nextSibiling = linkFaviconNode.getNextSibling()
                        .getNextSibling();
                lsRoot.insertBefore(linkApplicationCss, linkFaviconNode);
            } else {
                // Add ass last link element
                // Element lastLink = null;
                List<Element> linkElements = org.springframework.roo.support.util.XmlUtils
                        .findElements("/root/link", lsRoot);
                if (!linkElements.isEmpty()) {
                    Element lastLink = linkElements
                            .get(linkElements.size() - 1);
                    if (lastLink != null) {
                        nextSibiling = lastLink.getNextSibling()
                                .getNextSibling();
                        lsRoot.insertBefore(linkApplicationCss, nextSibiling);
                    }
                } else {
                    // Add at the end of document
                    lsRoot.appendChild(linkApplicationCss);
                }
            }
        }

        // utils.js script element
        testElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("/root/script[@src='${cit_js_url}']", lsRoot);
        if (testElement == null) {
            Element scriptYuiEventJs = new XmlElementBuilder("script",
                    loadScriptsXml).addAttribute("src", "${yui_event}")
                    .addAttribute("type", "text/javascript").build();
            scriptYuiEventJs.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            Element scriptYuiCoreJs = new XmlElementBuilder("script",
                    loadScriptsXml).addAttribute("src", "${yui_core}")
                    .addAttribute("type", "text/javascript").build();
            scriptYuiCoreJs.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            Element scriptYoiMenuJs = new XmlElementBuilder("script",
                    loadScriptsXml).addAttribute("src", "${yui_menu}")
                    .addAttribute("type", "text/javascript").build();
            scriptYoiMenuJs.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            Element scriptCitJs = new XmlElementBuilder("script",
                    loadScriptsXml).addAttribute("src", "${cit_js_url}")
                    .addAttribute("type", "text/javascript").build();
            scriptCitJs.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            List<Element> scrtiptElements = org.springframework.roo.support.util.XmlUtils
                    .findElements("/root/script", lsRoot);
            // Element lastScript = null;
            if (!scrtiptElements.isEmpty()) {
                Element lastScript = scrtiptElements
                        .get(scrtiptElements.size() - 1);
                if (lastScript != null) {
                    nextSibiling = lastScript.getNextSibling().getNextSibling();
                    lsRoot.insertBefore(scriptYuiEventJs, nextSibiling);
                    lsRoot.insertBefore(scriptYuiCoreJs, nextSibiling);
                    lsRoot.insertBefore(scriptYoiMenuJs, nextSibiling);
                    lsRoot.insertBefore(scriptCitJs, nextSibiling);
                }
            } else {
                // Add at the end of document
                lsRoot.appendChild(scriptYuiEventJs);
                lsRoot.appendChild(scriptYuiCoreJs);
                lsRoot.appendChild(scriptYoiMenuJs);
                lsRoot.appendChild(scriptCitJs);
            }
        }

        writeToDiskIfNecessary(loadScriptsTagx,
                loadScriptsXml.getDocumentElement());

    }

    /**
     * Decides if write to disk is needed (ie updated or created)<br/>
     * Used for TAGx files
     * 
     * @param filePath
     * @param body
     * @return
     */
    private boolean writeToDiskIfNecessary(String filePath, Element body) {
        // Build a string representation of the JSP
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Transformer transformer = org.springframework.roo.support.util.XmlUtils
                .createIndentingTransformer();
        org.springframework.roo.support.util.XmlUtils.writeXml(transformer,
                byteArrayOutputStream, body.getOwnerDocument());
        String viewContent = byteArrayOutputStream.toString();

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (fileManager.exists(filePath)) {
            // First verify if the file has even changed
            File f = new File(filePath);
            String existing = null;
            try {
                existing = FileCopyUtils.copyToString(new FileReader(f));
            } catch (IOException ignoreAndJustOverwriteIt) {
            }

            if (!viewContent.equals(existing)) {
                mutableFile = fileManager.updateFile(filePath);
            }
        } else {
            mutableFile = fileManager.createFile(filePath);
            Assert.notNull(mutableFile, "Could not create '" + filePath + "'");
        }

        if (mutableFile != null) {
            try {
                // We need to write the file out (it's a new file, or the
                // existing file has different contents)
                FileCopyUtils.copy(viewContent, new OutputStreamWriter(
                        mutableFile.getOutputStream()));
                // Return and indicate we wrote out the file
                return true;
            } catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }

    /** {@inheritDoc} */
    public Theme getActiveTheme() {

        String activeThemePath = getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/theme.xml");
        File descriptor = new File(activeThemePath);
        Theme active = null;

        // check if there is an active theme
        if (descriptor.exists()) {
            active = XmlUtils.parseTheme(descriptor.toURI());
            active.setActive(true);
        }

        return active;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A theme must have the same directory structure than the structure created
     * by Roo MVC add-ons because a theme will overwrite default Roo MVC
     * artefacts to set the desired style.
     * <p>
     * A theme must contain the theme descriptor file,
     * [THEME_DIR]/WEB-INF/views/theme.xml. Only those directories that contains
     * it will be considered as valid themes.
     * <p>
     * The themes in local repository will have precedence over themes in OSGi
     * bundles. The reason is local repositories could contain custom themes.
     * 
     * @return Theme list
     */
    public List<Theme> getAvailableThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        // find themes in bundles and local repository
        Set<URL> urls = findThemeDescriptors();

        for (URL url : urls) {
            // load the theme
            Theme theme = XmlUtils.parseTheme(url);
            theme.setAvailable(true);
            themes.add(theme);
        }

        return themes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method includes the <em>Active Theme</em>:
     * <ul>
     * <li>If the active theme ID match with one of installed themes no new
     * Theme object will be added, the installed theme will be set as active.</li>
     * <li>If the developer customized the active theme, including the theme ID,
     * the active Theme is a new theme and it will be included in result List.
     * Note that in that case the active theme have the
     * {@link Theme#isInstalled()} method returns false</li>
     * </ul>
     */
    public List<Theme> getInstalledThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        // 1st get the active theme
        Theme active = getActiveTheme();

        // 2nd iterate over project themes
        Set<URL> urls = findFileThemeDescriptors(getThemesPath());

        for (URL url : urls) {
            // load the theme
            Theme theme = XmlUtils.parseTheme(url);
            theme.setInstalled(true);

            // if this installed theme is the active theme, just set it as
            // active
            if (active != null && theme.equals(active)) {
                theme.setActive(true);
                active.setInstalled(true); // mark as installed to avoid to add
                                           // again
            }

            themes.add(theme);
        }

        // finally if the active theme isn't in the set of installed themes
        // add it to List. This case occurs when the developer customizes the
        // activated theme, including the theme ID
        if (active != null && !active.isInstalled()) {
            themes.add(active);
        }

        return Collections.unmodifiableList(themes);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method will iterate over installed themes checking each theme ID to
     * know if it is an available theme too. In that case, the available theme
     * won't be added to info, we will set availability flag of installed theme
     * to true.
     * <p>
     * Themes that are available and there aren't installed will be added to
     * info too.
     */
    public String getThemesInfo() {
        List<Theme> availableThemes = getAvailableThemes();
        List<Theme> installedThemes = getInstalledThemes();

        // Message builder.
        StringBuilder stringBuilder = new StringBuilder();

        // line of 72 chars
        stringBuilder
                .append("     ID          Avail  Instal  Active          Description\n");
        stringBuilder
                .append("---------------  -----  ------  ------  --------------------------------\n");

        // Iterate over installed themes
        for (Theme theme : installedThemes) {

            // remove the installed theme from available themes list and set
            // availability flag to true (it will avoid to show duplicate
            // themes)
            if (availableThemes.contains(theme)) {
                availableThemes.remove(theme);
                theme.setAvailable(true);
            }

            stringBuilder.append(getThemeInfo(theme));
        }

        // Iterate over remain available themes. Note the List doesn't have
        // the available themes that were installed
        for (Theme theme : availableThemes) {
            stringBuilder.append(getThemeInfo(theme));
        }

        return stringBuilder.toString();
    }

    // Private operations and utils -----

    /**
     * Utility method to get a formated string that shows complete Theme info.
     * This method is designed to be used from {@link #getThemesInfo()}. Theme
     * info:
     * <ul>
     * <li>Theme ID</li>
     * <li>Theme available to be installed in the project</li>
     * <li>Installed theme</li>
     * <li>Active theme</li>
     * </ul>
     * Note that the resulting String will have the same length, 72 chars.
     * 
     * @return Theme info
     */
    private String getThemeInfo(Theme theme) {
        // ID must be 15 chars length
        StringBuilder idStrBuilder = new StringBuilder("               ");

        String id = theme.getId();
        if (id.length() > 15) {
            idStrBuilder.replace(0, 15, id.substring(0, 15));
        } else {
            idStrBuilder.replace(0, id.length(), id);
        }

        // description could be null
        String description = theme.getDescription();
        if (description == null) {
            description = "";
        }

        // Message builder.
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(idStrBuilder);
        stringBuilder.append("  ");
        stringBuilder.append(theme.isAvailable() ? " Yes " : " No  ");
        stringBuilder.append("  ");
        stringBuilder.append(theme.isInstalled() ? "  Yes " : "  No  ");
        stringBuilder.append("  ");
        stringBuilder.append(theme.isActive() ? "  Yes " : "  No  ");
        stringBuilder.append("  ");

        // tail description to 30 chars length
        stringBuilder.append(description.length() > 30 ? description.substring(
                0, 30) : description);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    /**
     * Find theme descriptors in OSGi bundles and local repository.
     * <p>
     * The themes in local repository will have precedence over themes in OSGi
     * bundles because local repositories could contain custom themes. That is,
     * if there are two themes with the same ID, one in the local repository and
     * the other in a bundle, the result Set will contain the theme descriptor
     * in the local repository.
     * 
     * @return URLs to available theme descriptors "WEB-INF/views/theme.xml"
     */
    private Set<URL> findThemeDescriptors() {

        // URLs to repository theme descriptors
        Set<URL> urls = findFileThemeDescriptors(getThemesRepositoryPath());

        // URLs to theme descriptors in OSGi bundles
        urls.addAll(findBundleThemeDescriptors());

        return urls;
    }

    /**
     * Find theme descriptors in OSGi bundles.
     * 
     * @return URLs to theme descriptors "WEB-INF/views/theme.xml"
     */
    private Set<URL> findBundleThemeDescriptors() {

        // URLs to theme descriptors in OSGi bundles
        Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(
                context.getBundleContext(), "/**/WEB-INF/views/theme.xml");
        return urls;
    }

    /**
     * Find theme descriptors in the given path. Use this utility to find themes
     * in the local repository or themes installed in the project, just give the
     * search path.
     * 
     * @return URLs to theme descriptors "WEB-INF/views/theme.xml"
     */
    private Set<URL> findFileThemeDescriptors(PathInformation path) {
        Set<URL> urls = new HashSet<URL>();

        // find themes in the local theme repository (if it exists)
        if (path == null) {
            // there isn't a local theme repository, return theme descriptors in
            // bundles
            return urls;
        }

        File themesLocation = path.getLocation();
        if (themesLocation == null) {
            // if null, themes location hasn't been created yet, there isn't any
            // theme descriptor installed in the project : return empty set
            return urls;
        }

        // get the list of theme dirs in the repository
        File[] themeDirs = themesLocation.listFiles();
        if (themeDirs == null) {
            // if null there isn't any installed theme : return empty set
            return urls;
        }

        // iterate over the set of theme directories in the repository
        for (File themeDir : themeDirs) {
            File descriptor = new File(themeDir.getAbsolutePath(),
                    "WEB-INF/views/theme.xml");
            try {
                if (themeDir.isDirectory() && descriptor.exists()) {
                    urls.add(descriptor.toURI().toURL());
                }
            } catch (MalformedURLException ex) {
                // this shouldn't occur because Files were created by JDK
                logger.info("Cannot convert '".concat(descriptor.getPath())
                        .concat("' to URL. [Nothing to add]"));
            }
        }
        return urls;
    }

    /**
     * Utility to get {@link PathResolver} from {@link ProjectMetadata}.
     * <p>
     * This method will thrown if unavailable project metadata or unavailable
     * project path resolver.
     * 
     * @return PathResolver
     */
    private PathResolver getPathResolver() {
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        Assert.notNull(projectMetadata, "Unable to obtain project metadata");

        // Use PathResolver to resolve between {@link File}, {@link Path} and
        // canonical path {@link String}s.
        // See {@link MavenPathResolver} to know location values
        PathResolver pathResolver = projectMetadata.getPathResolver();
        Assert.notNull(projectMetadata, "Unable to obtain path resolver");

        return pathResolver;
    }

    /**
     * Returns the local repository that contains install themes. Search order:
     * <ol>
     * <li>System property 'theme.repository' (java -Dtheme.repository).</li>
     * <li>Environment variable 'THEME_REPOSITORY'.</li>
     * </ol>
     */
    private String getLocalRepositoryPath() {

        String path = null;

        // theme.repository
        path = System.getProperty("theme.repository");

        if (path != null) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            return path;
        }

        // THEME_REPOSITORY
        path = System.getenv("THEME_REPOSITORY");

        if (path != null) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            return path;
        }
        return path;
    }

    /**
     * This method will copy the contents of a bundle to a local directory if
     * the resource does not already exist in the target directory
     * 
     * @param sourceDirectory
     *            source directory. URI syntax:
     *            [scheme:][//authority][path][?query][#fragment]
     * @param targetDirectory
     *            target directory
     * @param overwrite
     *            if true copy to target dir overwriting destination file
     * @see JspOperationsImpl#copyDirectoryContents(String, String)
     */
    @SuppressWarnings("unchecked")
    private void copyRecursively(URI sourceDirectory, File targetDirectory,
            boolean overwrite) {
        Assert.notNull(sourceDirectory, "Source URI required");
        Assert.notNull(targetDirectory, "Target directory required");

        // if source and target are the same dir, do nothing
        if (targetDirectory.toURI().equals(sourceDirectory)) {
            return;
        }

        if (!targetDirectory.exists()) {
            fileManager.createDirectory(targetDirectory.getAbsolutePath());
        }

        // Set of resource URLs to be copied to target dir
        Set<URL> urls = new HashSet<URL>();

        // if source URI schema is file:// , source files are in a local
        // repository
        if ("file".equals(sourceDirectory.getScheme())) {
            urls = FileUtils.findFiles(new File(sourceDirectory));
        }

        // if source URI schema is bundle:// , we can access to that bundle
        // (note the authority contains the bundle ID) and copy Theme
        // artefacts. URI example
        // bundle://8.0:0/org/gvnix/web/theme/roo/addon/themes/theme-cit/
        else if ("bundle".equals(sourceDirectory.getScheme())) {
            String uriAuthority = sourceDirectory.getAuthority();
            long bundleId = Long.parseLong(uriAuthority.substring(0,
                    uriAuthority.indexOf(".")));

            // iterate over bundle entries in the given URI path and add them
            // to URLs to be copied to target dir
            Enumeration<URL> entries = context.getBundleContext()
                    .getBundle(bundleId)
                    .findEntries(sourceDirectory.getPath(), "*.*", true);
            while (entries.hasMoreElements()) {
                urls.add(entries.nextElement());
            }
        }
        // it shouldn't occur
        else {
            throw new IllegalArgumentException(
                    "Could not determine schema for resources for source dir '"
                            .concat(sourceDirectory.toString()).concat("'"));
        }

        Assert.notNull(
                urls,
                "No resources found to copy in '".concat(
                        sourceDirectory.toString()).concat("'"));

        // iterate over Theme resources and copy them with same dir layout
        for (URL url : urls) {
            String filePath = url.getPath().replaceFirst(
                    sourceDirectory.getPath(), "");
            if (isVersionControlSystemFile(filePath)) {
                // nothing to do if the URL is of a file from a Version Control
                // System
                continue;
            }
            File targetFile = new File(targetDirectory, filePath);

            try {
                // only copy files and if target file doesn't exist or overwrite
                // flag is true
                if (!targetFile.exists()) {
                    // create file using FileManager to fire creation events
                    FileCopyUtils.copy(url.openStream(), fileManager
                            .createFile(targetFile.getAbsolutePath())
                            .getOutputStream());
                }
                // if file exists and overwrite is true, update the file
                else if (overwrite) {
                    FileCopyUtils.copy(url.openStream(), fileManager
                            .updateFile(targetFile.getAbsolutePath())
                            .getOutputStream());
                }
            } catch (IOException e) {
                new IllegalStateException(
                        "Encountered an error during copying of resources for MVC Theme addon.",
                        e);
            }
        }
    }

    /**
     * Says if a file path is the path of a Version Control System control file
     * <p>
     * Currently, we only check for SVN, GIT and CVS, but we could include these
     * other VCS file patterns:<br/>
     * <code>.hg .bzr MT _MTN .cdv .arch-ids .arch-inventory _darcs RCS</code>
     * 
     * @param filePath
     * @return
     */
    private boolean isVersionControlSystemFile(String filePath) {
        return filePath.contains(".svn")
                || filePath.contains(".git")
                || filePath.contains(File.separator.concat("CVS").concat(
                        File.separator));
    }

    /**
     * Adds/Replaces the contents of the given Theme properties to the given
     * Theme properties file.
     * <p>
     * Note this method updates existing properties files only, it won't create
     * the file if it doesn't exist to avoid future addons cannot create their
     * files. Also note that {@link MessagesFileEventListener} will monitor that
     * creation in order to update properties if needed.
     * 
     * @param theme
     */
    public void updateProperties(Theme theme) {
        HashMap<String, Map<String, String>> bundles = theme
                .getPropertyBundles();

        for (Entry<String, Map<String, String>> entry : bundles.entrySet()) {

            // get the PATH_ID from key
            int index = entry.getKey().indexOf(":");
            Path path = new Path(entry.getKey().substring(0, index));

            // get filename from key
            String filename = entry.getKey().substring(index);

            // check file exists
            String filePath = getPathResolver().getIdentifier(path, filename);
            if (!fileManager.exists(filePath)) {
                continue;
            }

            // update properties file
            Map<String, String> properties = entry.getValue();
            propFileOperations.addProperties(path, filename, properties, true,
                    true);
        }
    }

    /**
     * Update WEB-INF/spring/webmvc-config.xml:
     * <ul>
     * <li>Update bean 'themeResolver' to set the default theme to given theme
     * Id.</li>
     * <li>Update bean 'messageSource' to add theme localized messages basename.
     * </li>
     * <p>
     * Note this method uses {@link FileManager} for safe update.
     * <p>
     * TODO: Comprobar THEME-ID.properties existe
     * 
     * @param id
     */
    private void updateSpringWebCtx(String themeId) {

        String webMvc = getMvcConfigFile();
        Assert.isTrue(fileManager.exists(webMvc),
                "webmvc-config.xml not found; cannot continue");

        MutableFile mutableConfigXml = null;
        Document webConfigDoc;

        try {
            if (fileManager.exists(webMvc)) {
                mutableConfigXml = fileManager.updateFile(webMvc);
                webConfigDoc = org.springframework.roo.support.util.XmlUtils
                        .getDocumentBuilder().parse(
                                mutableConfigXml.getInputStream());
            } else {
                throw new IllegalStateException(
                        "Could not acquire ".concat(webMvc));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Get themeResolver bean to change default theme
        Element resolverElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("//*[@id='themeResolver']",
                        (Element) webConfigDoc.getFirstChild());

        // throw exception if themeResolver doesn't exist
        Assert.notNull(resolverElement,
                "Could not find bean 'themeResolver' in ".concat(webMvc));

        resolverElement.setAttribute("p:defaultThemeName", themeId);

        // Get messageSource to add theme messages basename
        Element msgSourceElement = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("//*[@id='messageSource']",
                        (Element) webConfigDoc.getFirstChild());

        // throw exception if msgSourceElement doesn't exist
        Assert.notNull(msgSourceElement,
                "Could not find bean 'messageSource' in ".concat(webMvc));

        // The associated resource bundles will be checked sequentially when
        // resolving a message code.
        // We place theme basenames before the default ones to override ones in
        // a later bundle, due to the sequential lookup.
        String msgSourceBasenames = msgSourceElement
                .getAttribute("p:basenames");
        String basenames = "WEB-INF/i18n/theme/messages,WEB-INF/i18n/theme/application";
        if (!msgSourceBasenames.contains(basenames)) {
            if (themeId.equalsIgnoreCase("cit")) {
                // When setting theme-cit we want to show application version
                // number as a message
                basenames = basenames
                        .concat(",WEB-INF/i18n/theme/applicationversion");
            }
            msgSourceElement.setAttribute("p:basenames", basenames.concat(",")
                    .concat(msgSourceBasenames));
        }

        org.springframework.roo.support.util.XmlUtils.writeXml(
                mutableConfigXml.getOutputStream(), webConfigDoc);
    }

    /**
     * Add XML below to setup theme definition in theme.tagx:
     * 
     * <pre>
     *       <c:out value=" | " />
     *       <spring:url var="url_theme_gvnix" value="">
     *         <spring:param name="theme" value="THEME_ID" />
     *         <c:if test="${not empty param.page}">
     *           <spring:param name="page" value="${param.page}" />
     *         </c:if>
     *         <c:if test="${not empty param.size}">
     *           <spring:param name="size" value="${param.size}" />
     *         </c:if>
     *       </spring:url>
     *       <spring:message text="THEME_NAME" var="theme_THEME_ID" htmlEscape="false" />
     *       <a href="${url_theme_gvnix}"
     *         title="${fn:escapeXml(theme_THEME_ID)}">${fn:escapeXml(theme_THEME_ID)}</a>
     * </pre>
     */
    private void updateThemeLinks(Theme theme) {
        String tagxFileLocation = projectOperations.getPathResolver()
                .getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/util/theme.tagx");
        MutableFile tagxFile = null;

        // load theme.tagx as XML Document
        Document tagxDocument = null;
        try {
            if (fileManager.exists(tagxFileLocation)) {
                tagxFile = fileManager.updateFile(tagxFileLocation);
                tagxDocument = XmlUtils.parseFile(tagxFile.getInputStream());
            } else {
                // do nothing, some web technologies (i.e. GWT) doesn't use
                // theme.tagx
                return;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element root = tagxDocument.getDocumentElement();

        // locate Element that renders anchor to change to gvNIX theme
        Element themeEl = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("//span/spring:url[@var='url_theme_gvnix']",
                        root);

        // if not found, create the anchor for gvNIX theme
        if (null == themeEl) {
            Element span = org.springframework.roo.support.util.XmlUtils
                    .findRequiredElement("//span", root);
            span.appendChild(new XmlElementBuilder("c:out", tagxDocument)
                    .addAttribute("value", " | ").build());

            // create gvNIX spring:url and add theme parameter
            Element gvnixUrlEl = new XmlElementBuilder("spring:url",
                    tagxDocument).addAttribute("var", "url_theme_gvnix")
                    .addAttribute("value", "").build();
            gvnixUrlEl.appendChild(new XmlElementBuilder("spring:param",
                    tagxDocument).addAttribute("name", "theme")
                    .addAttribute("value", theme.getId()).build());

            // create conditional page parameter to add to gvNIX spring:url
            Element ifPageEl = new XmlElementBuilder("c:if", tagxDocument)
                    .addAttribute("test", "${not empty param.page}").build();
            Element paramPageEl = new XmlElementBuilder("spring:param",
                    tagxDocument).addAttribute("name", "page")
                    .addAttribute("value", "${param.page}").build();
            ifPageEl.appendChild(paramPageEl);

            gvnixUrlEl.appendChild(ifPageEl);

            // create conditional size parameter to add to gvNIX spring:url
            Element ifSizeEl = new XmlElementBuilder("c:if", tagxDocument)
                    .addAttribute("test", "${not empty param.size}").build();
            Element paramSizeEl = new XmlElementBuilder("spring:param",
                    tagxDocument).addAttribute("name", "size")
                    .addAttribute("value", "${param.size}").build();
            ifSizeEl.appendChild(paramSizeEl);

            gvnixUrlEl.appendChild(ifSizeEl);

            // add gvNIX spring:url to main <span> Element
            span.appendChild(gvnixUrlEl);

            // create the anchor to change to gvNIX Theme
            Element gvnixMsgEl = new XmlElementBuilder("spring:message",
                    tagxDocument).addAttribute("text", theme.getName())
                    .addAttribute("var", "theme_gvnix")
                    .addAttribute("htmlEscape", "false").build();
            span.appendChild(gvnixMsgEl);

            Element anchorEl = new XmlElementBuilder("a", tagxDocument)
                    .addAttribute("href", "${url_theme_gvnix}")
                    .addAttribute("title", "${fn:escapeXml(theme_gvnix)}")
                    .setText("${fn:escapeXml(theme_gvnix)}").build();
            span.appendChild(anchorEl);
        }
        // if found, update the parameter with given gvNIX theme ID
        else {
            themeEl.setAttribute("theme", theme.getId());

            // locate Element that sets the anchor message text
            Element gvnixMsgEl = org.springframework.roo.support.util.XmlUtils
                    .findFirstElement(
                            "//span/spring:message[@var='theme_gvnix']", root);
            gvnixMsgEl.setAttribute("text", theme.getName());
        }

        // update tagx
        org.springframework.roo.support.util.XmlUtils.writeXml(
                tagxFile.getOutputStream(), tagxDocument);
    }

    /**
     * Utility method to get project themes path.
     * <p>
     * The method initializes the {@link #themesPath} attribute if it is null.
     * 
     * @return
     */
    private PathInformation getThemesPath() {

        if (themesPath == null) {
            // resolve project installation directory for themes
            String root = getPathResolver().getRoot(Path.ROOT);

            themesPath = new PathInformation(SRC_MAIN_THEMES, true, new File(
                    root, "src/main/themes"));
        }

        return themesPath;
    }

    /**
     * Utility method to get local themes repository path.
     * 
     * @return
     */
    private PathInformation getThemesRepositoryPath() {

        if (themesRepositoryPath == null) {
            // load local themes repository path, it won't change in single
            // shell
            // execution. Note it is optional to have local repository
            String repoPath = getLocalRepositoryPath();
            if (StringUtils.hasText(repoPath)) {
                themesRepositoryPath = new PathInformation(THEMES_REPOSITORY,
                        true, new File(repoPath));
            }
        }
        return themesRepositoryPath;
    }

    /**
     * Get the absolute path for {@code webmvc-config.xml}.
     * 
     * @return the absolute path to file (never null)
     */
    private String getMvcConfigFile() {

        // resolve absolute path for menu.jspx if it hasn't been resolved yet
        return getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP,
                "/WEB-INF/spring/webmvc-config.xml");
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
        return getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP,
                "/WEB-INF/layouts/layouts.xml");
    }

    /**
     * Gets active languages in footer.jspx
     * 
     * @return
     */
    private Set<I18n> getInstalledI18n() {
        String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(Path.SRC_MAIN_WEBAPP, "");
        // Language definition
        String footerFileLocation = targetDirectory
                + "/WEB-INF/views/footer.jspx";
        Document footer = null;
        try {
            if (fileManager.exists(footerFileLocation)) {
                footer = XmlUtils.parseFile(fileManager
                        .getInputStream(footerFileLocation));
            } else {
                throw new IllegalStateException(
                        "Could not aquire the footer.jspx file");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Iterate over language elements and create a Set of I18n objects that
        // contains current info at footer.jspx
        HashSet<I18n> result = new HashSet<I18n>();

        // get <span> and not <util:language> directly because there is
        // some problem with namespaces that cause cannot locate language
        // elements
        Element span = org.springframework.roo.support.util.XmlUtils
                .findFirstElement("//span[@id='language']",
                        footer.getDocumentElement());

        // <span> contains language elements
        NodeList nodes = span.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            // if it is the element language, create the I18n instance to
            // contain
            // its info
            if (node.getNodeName().equals("util:language")) {
                Element langEl = (Element) node;
                I18n i18n = I18nUtils.createI18n(langEl.getAttribute("locale"),
                        langEl.getAttribute("label"));
                result.add(i18n);
            }
        }
        return result;
    }

    /**
     * Add language definitions in footer.jspx
     * 
     * @param languages
     */
    private void setInstalledI18n(Set<I18n> languages) {
        if (languages.isEmpty()) {
            // nothing to do
            return;
        }
        String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(Path.SRC_MAIN_WEBAPP, "");

        // Language definition
        String footerFileLocation = targetDirectory
                + "/WEB-INF/views/footer.jspx";
        MutableFile footerFile = null;
        Document footer = null;
        try {
            if (fileManager.exists(footerFileLocation)) {
                footerFile = fileManager.updateFile(footerFileLocation);
                footer = org.springframework.roo.support.util.XmlUtils
                        .getDocumentBuilder()
                        .parse(footerFile.getInputStream());
            } else {
                throw new IllegalStateException(
                        "Could not aquire the footer.jspx file");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Iterate given languages to add in footer.jspx
        for (I18n i18n : languages) {

            // if language doesn't exist in footer.jspx, add it. Otherwise do
            // nothing
            if (null == org.springframework.roo.support.util.XmlUtils
                    .findFirstElement(
                            "//span[@id='language']/language[@locale='".concat(
                                    i18n.getLocale().getLanguage())
                                    .concat("']"), footer.getDocumentElement())) {

                Element span = org.springframework.roo.support.util.XmlUtils
                        .findRequiredElement("//span[@id='language']",
                                footer.getDocumentElement());
                span.appendChild(new XmlElementBuilder("util:language", footer)
                        .addAttribute("locale", i18n.getLocale().getLanguage())
                        .addAttribute("label", i18n.getLanguage()).build());
            }
        }
        org.springframework.roo.support.util.XmlUtils.writeXml(
                footerFile.getOutputStream(), footer);
    }
}
