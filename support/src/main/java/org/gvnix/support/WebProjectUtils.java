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
package org.gvnix.support;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utils for web projects.
 * 
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WebProjectUtils {

    /**
     * Check if current project is a Spring MVC one
     * <p/>
     * Search webmvc-config.xml file exists.
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a Spring MVC project ?
     */
    public static boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations) {

        PathResolver pathResolver = OperationUtils.getPathResolver(
                metadataService, projectOperations);
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/spring/webmvc-config.xml");

        return fileManager.exists(webXmlPath);
    }

    /**
     * Check if current project is a web project
     * <p/>
     * Search web.xml file exists.
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a web project ?
     */
    public static boolean isWebProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations) {

        PathResolver pathResolver = OperationUtils.getPathResolver(
                metadataService, projectOperations);
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/web.xml");

        return fileManager.exists(webXmlPath);
    }

    /**
     * Installs the Dialog Java class
     * 
     * @param packageFullName fullyQualifiedName of destination package for
     *            Dialog Bean. ie. <code>com.disid.myapp.web.dialog</code>
     * @param pathResolver
     * @param fileManager
     */
    public static void installWebDialogClass(String packageFullName,
            PathResolver pathResolver, FileManager fileManager) {

        String classFullName = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                packageFullName.concat(".Dialog").replace(".", File.separator)
                        .concat(".java"));

        OperationUtils.installJavaClassFromTemplate(packageFullName,
                classFullName, "Dialog.java-template", pathResolver,
                fileManager);
    }

    // TODO: hasServlet method
    // public static boolean hasServlet(final Document webXml, final String
    // className) {
    // if (XmlUtils.findFirstElement(
    // "/web-app/servlet[servlet-class='".concat(className).concat("']"),
    // webXml.getElementRoot()) != null) {
    // return true;
    // }
    // }

    /**
     * Update tagx namespaces on a jspx
     * 
     * @param controller
     * @param jspxName (by example: "show", "list", "update")
     * @param uriMap where key attribute name (ex "xmlns:page") and the value
     *            the new uri (ex: "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     * @param metadataService
     */
    public static void updateTagxUriInJspx(JavaType controller,
            String jspxName, Map<String, String> uriMap,
            ProjectOperations projectOperations, FileManager fileManager,
            MetadataService metadataService) {

        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(WebScaffoldMetadata.createIdentifier(controller,
                        getJavaPath(projectOperations)));

        Validate.notNull(webScaffoldMetadata,
                "Can't get RooWebScaffold metada for type: %s",
                controller.getFullyQualifiedTypeName());

        updateTagxUriInJspx(
                webScaffoldMetadata.getAnnotationValues().getPath(), jspxName,
                uriMap, projectOperations, fileManager);
    }

    /**
     * Update tagx namespaces on a jspx
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName (by example: "show", "list", "update")
     * @param uriMap where key attribute name (ex "xmlns:page") and the value
     *            the new uri (ex: "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void updateTagxUriInJspx(String controllerPath,
            String jspxName, Map<String, String> uriMap,
            ProjectOperations projectOperations, FileManager fileManager) {
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        // Get list.jspx file path
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docJspx = pathResolver.getIdentifier(
                getWebappPath(projectOperations), "WEB-INF/views"
                        + controllerPath + "/" + jspxName + ".jspx");

        // Parse list.jspx document
        Document docJspXml = loadXmlDocument(docJspx, fileManager);
        if (docJspXml == null) {
            // file not found: do nothing
            return;
        }

        // Get main div
        Element docRoot = docJspXml.getDocumentElement();
        Element divMain = XmlUtils.findFirstElement("/div", docRoot);

        // Update namespace
        for (Entry<String, String> entry : uriMap.entrySet()) {
            divMain.setAttribute(entry.getKey(), entry.getValue());
        }

        // Update list.jspx file
        DomUtils.removeTextNodes(docJspXml);
        fileManager.createOrUpdateTextFileIfRequired(docJspx,
                XmlUtils.nodeToString(docJspXml), true);
    }

    /**
     * Load a XML {@link Document} from its file identifier
     * 
     * @param docFileIdentifier
     * @return document or null if file not found
     */
    public static Document loadXmlDocument(String docFileIdentifier,
            FileManager fileManager) {
        if (!fileManager.exists(docFileIdentifier)) {
            // document doesn't exist, so nothing to do
            return null;
        }

        // Parse document
        Document docJspXml;
        InputStream docJspxIs = null;
        try {
            docJspxIs = fileManager.getInputStream(docFileIdentifier);

            try {
                docJspXml = XmlUtils.getDocumentBuilder().parse(docJspxIs);
            }
            catch (Exception ex) {
                throw new IllegalStateException(
                        "Could not open ".concat(docFileIdentifier), ex);
            }
        }
        finally {
            IOUtils.closeQuietly(docJspxIs);
        }
        return docJspXml;
    }

    /**
     * Gets the src/mian/webapp logicalPath
     * 
     * @param projectOperations
     * @return
     */
    public static LogicalPath getWebappPath(ProjectOperations projectOperations) {
        return LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP,
                projectOperations.getFocusedModuleName());
    }

    /**
     * Gets the src/main/java logicalPath
     * 
     * @param projectOperations
     * @return
     */
    public static LogicalPath getJavaPath(ProjectOperations projectOperations) {
        return LogicalPath.getInstance(Path.SRC_MAIN_JAVA,
                projectOperations.getFocusedModuleName());
    }

    /**
     * Append a css definition in loadScript.tagx
     * <p/>
     * This first append a "spring:url" (if not exists) and then add the "link"
     * tag (if not exists)
     * 
     * @param docTagx loadScript.tagx document
     * @param root root node
     * @param varName name of variable to hold css url
     * @param location css location
     * @return document has changed
     */
    public static boolean addCssToTag(Document docTagx, Element root,
            String varName, String location) {
        boolean modified = false;

        // add url resolution
        modified = addUrlToTag(docTagx, root, varName, location);

        // Add link
        Element cssElement = XmlUtils.findFirstElement(
                String.format("link[@href='${%s}']", varName), root);
        if (cssElement == null) {
            cssElement = docTagx.createElement("link");
            cssElement.setAttribute("rel", "stylesheet");
            cssElement.setAttribute("type", "text/css");
            cssElement.setAttribute("media", "screen");
            cssElement.setAttribute("href", "${".concat(varName).concat("}"));
            root.appendChild(cssElement);
            modified = true;
        }
        return modified;
    }

    /**
     * Append a '<spring:url var="varName" value='location'/>' tag inside root
     * element if not exist.
     * <p/>
     * First try to locate element using expresion "url[@var='varName']. If not
     * found append this tag.
     * <p/>
     * 
     * @param docTagx
     * @param root
     * @param varName
     * @param location
     * @return if document has changed
     */
    public static boolean addUrlToTag(Document docTagx, Element root,
            String varName, String location) {
        Element urlElement = XmlUtils.findFirstElement(
                "url[@var='".concat(varName) + "']", root);
        if (urlElement == null) {
            urlElement = docTagx.createElement("spring:url");
            urlElement.setAttribute("var", varName);
            urlElement.setAttribute("value", location);
            root.appendChild(urlElement);
            return true;
        }
        return false;
    }

    /**
     * Append a js definition in loadScript.tagx
     * <p/>
     * This first append a "spring:url" (if not exists) and then add the "link"
     * tag (if not exists)
     * 
     * @param docTagx loadScript.tagx document
     * @param root root node
     * @param varName name of variable to hold js url
     * @param location js location
     * @return document has changed
     */
    public static boolean addJSToTag(Document docTagx, Element root,
            String varName, String location) {
        boolean modified = false;

        // add url resolution
        modified = addUrlToTag(docTagx, root, varName, location);

        // Add script
        Element scriptElement = XmlUtils.findFirstElement(
                String.format("script[@src='${%s}']", varName), root);
        if (scriptElement == null) {
            scriptElement = docTagx.createElement("script");
            scriptElement.setAttribute("src", "${".concat(varName).concat("}"));
            scriptElement.setAttribute("type", "text/javascript");
            scriptElement.appendChild(docTagx
                    .createComment("required for FF3 and Opera"));
            root.appendChild(scriptElement);
            modified = true;
        }
        return modified;
    }

    /**
     * Add css and javaScript definition to load-scripts.tagx (if not found)
     * 
     * @param cssList pairs of variable name and location
     * @param jsList pairs of variable name and location
     * @param projectOperations
     * @param fileManager
     */
    public static void addJsAndCssToLoadScriptsTag(
            List<Pair<String, String>> cssList,
            List<Pair<String, String>> jsList,
            ProjectOperations projectOperations, FileManager fileManager) {

        // Parse load-script.tagx
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docTagxPath = pathResolver.getIdentifier(
                getWebappPath(projectOperations),
                "WEB-INF/tags/util/load-scripts.tagx");
        Validate.isTrue(fileManager.exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = fileManager.updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        /* Add css */
        for (Pair<String, String> css : cssList) {
            modified = addCssToTag(docTagx, root, css.getLeft(), css.getRight())
                    || modified;
        }
        /* Add js */
        for (Pair<String, String> js : jsList) {
            modified = addJSToTag(docTagx, root, js.getLeft(), js.getRight())
                    || modified;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }
    }
}
