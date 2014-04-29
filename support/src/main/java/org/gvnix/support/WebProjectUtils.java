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
package org.gvnix.support;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
 * @author gvNIX Team
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
     *        Dialog Bean. ie. <code>com.disid.myapp.web.dialog</code>
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

    /**
     * Create all namespaces occurrences in given {@relativePath} file with
     * namespaces occurrences contained in {@code newUriMap}.
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName view name, for example: "show", "list", "update"
     * @param uriMap where key attribute name (ex "xmlns:page") and the value
     *        the new uri (ex: "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     * @param metadataService
     */
    public static void addTagxUriInJspx(JavaType controller, String jspxName,
            Map<String, String> uriMap, ProjectOperations projectOperations,
            FileManager fileManager, MetadataService metadataService) {

        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(WebScaffoldMetadata.createIdentifier(controller,
                        getJavaPath(projectOperations)));

        Validate.notNull(webScaffoldMetadata,
                "Can't get RooWebScaffold metada for type: %s",
                controller.getFullyQualifiedTypeName());

        addTagxUriInJspx(webScaffoldMetadata.getAnnotationValues().getPath(),
                jspxName, uriMap, projectOperations, fileManager);
    }

    /**
     * Create all namespaces occurrences in given {@relativePath} file with
     * namespaces occurrences contained in {@code newUriMap}.
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName view name, for example: "show", "list", "update"
     * @param newUriMap Keys are namespace names (ex: "xmlns:page") and values
     *        are the new namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void addTagxUriInJspx(String controllerPath, String jspxName,
            Map<String, String> newUriMap, ProjectOperations projectOperations,
            FileManager fileManager) {
        addTagxUriInJspx("WEB-INF/views/".concat(controllerPath).concat("/")
                .concat(jspxName).concat(".jspx"), (Map<String, String>) null,
                newUriMap, projectOperations, fileManager);
    }

    /**
     * Create all namespaces occurrences in given {@relativePath} file with
     * namespaces occurrences contained in {@code newUriMap}.
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName view name, for example: "show", "list", "update"
     * @param oldUriMap (optional) Keys are namespace names (ex: "xmlns:page")
     *        and values are the old namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/form") that must match with the
     *        namespace URI in the XML
     * @param newUriMap Keys are namespace names (ex: "xmlns:page") and values
     *        are the new namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void addTagxUriInJspx(String controllerPath, String jspxName,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager) {
        addTagxUriInJspx("WEB-INF/views/".concat(controllerPath).concat("/")
                .concat(jspxName).concat(".jspx"), oldUriMap, newUriMap,
                projectOperations, fileManager);
    }

    /**
     * Create namespaces in given {@relativePath} file with namespaces
     * occurrences contained in {@code newUriMap}.
     * 
     * @param relativePath XML file to update. The path must be relative to
     *        {@code src/main/webapp} (cannot be null, but may be empty if
     *        referring to the path itself)
     * @param oldUriMap (optional) Keys are namespace names (ex: "xmlns:page")
     *        and values are the old namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/form") that must match with the
     *        namespace URI in the XML
     * @param newUriMap Keys are namespace names (ex: "xmlns:page") and values
     *        are the new namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void addTagxUriInJspx(String relativePath,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager) {

        // Get jspx file path
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docJspx = pathResolver.getIdentifier(
                getWebappPath(projectOperations), relativePath);

        // Parse XML document
        Document docJspXml = loadXmlDocument(docJspx, fileManager);
        if (docJspXml == null) {
            // file not found: do nothing
            return;
        }

        // Get main div
        Element docRoot = docJspXml.getDocumentElement();
        Element divMain = XmlUtils.findFirstElement("/div", docRoot);
        boolean modified = false;

        // Create namespace URIs
        for (Entry<String, String> newUriEntry : newUriMap.entrySet()) {
            String nsName = newUriEntry.getKey();
            String nsUri = newUriEntry.getValue();

            divMain.setAttribute(nsName, nsUri);
            modified = true;
        }

        // If modified, update the jspx file
        if (modified) {
            DomUtils.removeTextNodes(docJspXml);
            fileManager.createOrUpdateTextFileIfRequired(docJspx,
                    XmlUtils.nodeToString(docJspXml), true);
        }
    }

    /**
     * Replaces all namespaces occurrences in given {@relativePath} file with
     * namespaces occurrences contained in {@code newUriMap}.
     * <p/>
     * A namespace in given {@relativePath} file will be replaced by a namespace
     * in {@code newUriMap} if namespace name is in {@relativePath} file
     * <p/>
     * Only tagx namespaces found in uriMap will be updated, uriMap keys that
     * don't match any tagx namespace will be ignored.
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName view name, for example: "show", "list", "update"
     * @param uriMap where key attribute name (ex "xmlns:page") and the value
     *        the new uri (ex: "urn:jsptagdir:/WEB-INF/tags/datatables")
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
     * Replaces all namespaces occurrences in given {@relativePath} file with
     * namespaces occurrences contained in {@code newUriMap}.
     * <p/>
     * A namespace in given {@relativePath} file will be replaced by a namespace
     * in {@code newUriMap} if namespace name is in {@relativePath} file
     * <p/>
     * Only tagx namespaces found in uriMap will be updated, uriMap keys that
     * don't match any tagx namespace will be ignored.
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName view name, for example: "show", "list", "update"
     * @param newUriMap Keys are namespace names (ex: "xmlns:page") and values
     *        are the new namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void updateTagxUriInJspx(String controllerPath,
            String jspxName, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager) {
        updateTagxUriInJspx("WEB-INF/views/".concat(controllerPath).concat("/")
                .concat(jspxName).concat(".jspx"), (Map<String, String>) null,
                newUriMap, projectOperations, fileManager);
    }

    /**
     * Replaces all namespaces occurrences in given {@relativePath} file with
     * namespaces occurrences contained in {@code newUriMap}.
     * <p/>
     * A namespace in given {@relativePath} file will be replaced by a namespace
     * in {@code newUriMap} if namespace name is in {@relativePath} file
     * <p/>
     * Only tagx namespaces found in uriMap will be updated, uriMap keys that
     * don't match any tagx namespace will be ignored.
     * 
     * @param controllerPath {@link RooWebScaffold#path()} value
     * @param jspxName view name, for example: "show", "list", "update"
     * @param oldUriMap (optional) Keys are namespace names (ex: "xmlns:page")
     *        and values are the old namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/form") that must match with the
     *        namespace URI in the XML
     * @param newUriMap Keys are namespace names (ex: "xmlns:page") and values
     *        are the new namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void updateTagxUriInJspx(String controllerPath,
            String jspxName, Map<String, String> oldUriMap,
            Map<String, String> newUriMap, ProjectOperations projectOperations,
            FileManager fileManager) {
        updateTagxUriInJspx("WEB-INF/views/".concat(controllerPath).concat("/")
                .concat(jspxName).concat(".jspx"), oldUriMap, newUriMap,
                projectOperations, fileManager);
    }

    /**
     * Replaces all namespaces occurrences contained in {@code oldUriMap} in
     * given {@relativePath} file with namespaces occurrences contained in
     * {@code newUriMap}.
     * <p/>
     * A namespace in given {@relativePath} file will be replaced by a namespace
     * in {@code newUriMap} when one of the conditions below is true:
     * <p/>
     * <strong>A.</strong> Namespace name is in {@code oldUriMap}, as is in
     * {@code newUriMap} and as is in {@relativePath} file and old namespace
     * (value in {@code oldUriMap}) match with namespace in jspx.
     * <p/>
     * <strong>B.</strong> Namespace name is not in {@code oldUriMap} and
     * namespace name is in {@relativePath} file
     * 
     * @param relativePath XML file to update. The path must be relative to
     *        {@code src/main/webapp} (cannot be null, but may be empty if
     *        referring to the path itself)
     * @param oldUriMap (optional) Keys are namespace names (ex: "xmlns:page")
     *        and values are the old namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/form") that must match with the
     *        namespace URI in the XML
     * @param newUriMap Keys are namespace names (ex: "xmlns:page") and values
     *        are the new namespace URI (ex:
     *        "urn:jsptagdir:/WEB-INF/tags/datatables")
     * @param projectOperations
     * @param fileManager
     */
    public static void updateTagxUriInJspx(String relativePath,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager) {

        // If null, create default oldUriMap causing jspx will be updated with
        // all URIs in newUriMap
        if (oldUriMap == null) {
            oldUriMap = new HashMap<String, String>();
        }

        // Get jspx file path
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docJspx = pathResolver.getIdentifier(
                getWebappPath(projectOperations), relativePath);

        // Parse XML document
        Document docJspXml = loadXmlDocument(docJspx, fileManager);
        if (docJspXml == null) {
            // file not found: do nothing
            return;
        }

        // Get main div
        Element docRoot = docJspXml.getDocumentElement();
        Element divMain = XmlUtils.findFirstElement("/div", docRoot);
        boolean modified = false;

        // Update namespace URIs
        for (Entry<String, String> newUriEntry : newUriMap.entrySet()) {
            String nsName = newUriEntry.getKey();
            String nsUri = newUriEntry.getValue();

            // Namespace name is in oldUriMap, as is in and as is in given file
            // and old namespace (value in oldUriMap) match with namespace in
            // jspx
            if (oldUriMap.containsKey(nsName) && divMain.hasAttribute(nsName)) {
                String oldNsUri = oldUriMap.get(nsName);
                String currentUri = divMain.getAttribute(nsName);

                if (StringUtils.isEmpty(oldNsUri)
                        || oldNsUri.equalsIgnoreCase(currentUri)) {
                    // Compares new value with current before change
                    if (!StringUtils.equalsIgnoreCase(currentUri, nsUri)) {
                        divMain.setAttribute(nsName, nsUri);
                        modified = true;
                    }
                }
            }

            // Namespace name is not in oldUriMap and namespace name is in
            // given file
            if (!oldUriMap.containsKey(nsName) && divMain.hasAttribute(nsName)) {
                // Compares new value with current before change
                if (!StringUtils.equalsIgnoreCase(divMain.getAttribute(nsName),
                        nsUri)) {
                    divMain.setAttribute(nsName, nsUri);
                    modified = true;
                }
            }
        }

        // If modified, update the jspx file
        if (modified) {
            DomUtils.removeTextNodes(docJspXml);
            fileManager.createOrUpdateTextFileIfRequired(docJspx,
                    XmlUtils.nodeToString(docJspXml), true);
        }
    }

    /**
     * Load a XML {@link Document} from its file identifier
     * 
     * @param docFileIdentifier
     * @return document or null if file not found
     */
    public static Document loadXmlDocument(String docFileIdentifier,
            FileManager fileManager) {

        Validate.notNull(fileManager, "FileManager cannot be null");
        if (!fileManager.exists(docFileIdentifier)) {
            // document doesn't exist, so nothing to do
            return null;
        }

        // Parse document
        Document docXml;
        InputStream docIs = null;
        try {
            docIs = fileManager.getInputStream(docFileIdentifier);

            try {
                docXml = XmlUtils.getDocumentBuilder().parse(docIs);
            }
            catch (Exception ex) {
                throw new IllegalStateException(
                        "Could not open ".concat(docFileIdentifier), ex);
            }
        }
        finally {
            IOUtils.closeQuietly(docIs);
        }
        return docXml;
    }

    /**
     * Gets the {@code src/main/webapp} logicalPath
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
     * @param docTagx {@code .tagx} file document
     * @param root XML root node
     * @param varName name of variable to hold url
     * @param location URL
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
     * @param docTagx {@code .tagx} file document
     * @param root XML root node
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

        // Add css
        for (Pair<String, String> css : cssList) {
            modified = addCssToTag(docTagx, root, css.getLeft(), css.getRight())
                    || modified;
        }

        // Add js
        for (Pair<String, String> js : jsList) {
            modified = addJSToTag(docTagx, root, js.getLeft(), js.getRight())
                    || modified;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }
    }

    /**
     * Add variable to contain request Locale in string format.
     * <p/>
     * 
     * <pre>
     * {@code
     * <c:set var="VAR_NAME">
     *   <!-- Get the user local from the page context (it was set by
     *        Spring MVC's locale resolver) -->
     *   <c:set var="jqlocale">${pageContext.response.locale}</c:set>
     *   <c:if test="${fn:length(jqlocale) eq 2}">
     *     <c:out value="${jqlocale}" />
     *   </c:if>
     *   <c:if test="${fn:length(jqlocale) gt 2}">
     *     <c:out value="${fn:substringBefore(jqlocale, '_')}" default="en" />
     *   </c:if>
     *   <c:if test="${fn:length(jqlocale) lt 2}">
     *     <c:out value="en" />
     *   </c:if>
     * </c:set>
     * }
     * </pre>
     * 
     * @param docTagx {@code .tagx} file document
     * @param root XML root node
     * @param varName name of variable to create, see {@code VAR_NAME} in
     *        example above
     */
    public static boolean addLocaleVarToTag(Document docTagx, Element root,
            String varName) {

        // Add locale var
        Element varElement = XmlUtils.findFirstElement(
                String.format("c:set[@var='${%s}']", varName), root);
        if (varElement == null) {
            varElement = docTagx.createElement("c:set");
            varElement.setAttribute("var", varName);
            varElement
                    .appendChild(docTagx
                            .createComment(" Get the user local from the page context (it was set by Spring MVC's locale resolver) "));

            Element pElement = docTagx.createElement("c:set");
            pElement.setAttribute("var", "jqlocale");
            pElement.appendChild(docTagx
                    .createTextNode("${pageContext.response.locale}"));
            varElement.appendChild(pElement);

            Element ifElement = docTagx.createElement("c:if");
            ifElement.setAttribute("test", "${fn:length(jqlocale) eq 2}");

            Element outElement = docTagx.createElement("c:out");
            outElement.setAttribute("value", "${jqlocale}");
            ifElement.appendChild(outElement);
            varElement.appendChild(ifElement);

            ifElement = docTagx.createElement("c:if");
            ifElement.setAttribute("test", "${fn:length(jqlocale) gt 2}");

            outElement = docTagx.createElement("c:out");
            outElement.setAttribute("value",
                    "${fn:substringBefore(jqlocale, '_')}");
            outElement.setAttribute("default", "en");
            ifElement.appendChild(outElement);
            varElement.appendChild(ifElement);

            ifElement = docTagx.createElement("c:if");
            ifElement.setAttribute("test", "${fn:length(jqlocale) lt 2}");

            outElement = docTagx.createElement("c:out");
            outElement.setAttribute("value", "en");
            ifElement.appendChild(outElement);
            varElement.appendChild(ifElement);

            root.appendChild(varElement);

            return true;
        }
        return false;
    }
}
