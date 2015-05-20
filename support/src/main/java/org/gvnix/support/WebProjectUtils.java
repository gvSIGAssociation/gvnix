/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.roo.addon.web.mvc.controller.annotations.scaffold.RooWebScaffold;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utils for web projects.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public interface WebProjectUtils {

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
    public boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations);

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
    public boolean isWebProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations);

    /**
     * Installs the Dialog Java class
     * 
     * @param packageFullName fullyQualifiedName of destination package for
     *        Dialog Bean. ie. <code>com.disid.myapp.web.dialog</code>
     * @param pathResolver
     * @param fileManager
     */
    public void installWebDialogClass(String packageFullName,
            PathResolver pathResolver, FileManager fileManager);

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
    public void addTagxUriInJspx(JavaType controller, String jspxName,
            Map<String, String> uriMap, ProjectOperations projectOperations,
            FileManager fileManager, MetadataService metadataService);

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
    public void addTagxUriInJspx(String controllerPath, String jspxName,
            Map<String, String> newUriMap, ProjectOperations projectOperations,
            FileManager fileManager);

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
    public void addTagxUriInJspx(String controllerPath, String jspxName,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager);

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
    public void addTagxUriInJspx(String relativePath,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager);

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
    public void updateTagxUriInJspx(JavaType controller, String jspxName,
            Map<String, String> uriMap, ProjectOperations projectOperations,
            FileManager fileManager, MetadataService metadataService);

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
    public void updateTagxUriInJspx(String controllerPath, String jspxName,
            Map<String, String> newUriMap, ProjectOperations projectOperations,
            FileManager fileManager);

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
    public void updateTagxUriInJspx(String controllerPath, String jspxName,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager);

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
    public void updateTagxUriInJspx(String relativePath,
            Map<String, String> oldUriMap, Map<String, String> newUriMap,
            ProjectOperations projectOperations, FileManager fileManager);

    /**
     * Load a XML {@link Document} from its file identifier
     * 
     * @param docFileIdentifier
     * @return document or null if file not found
     */
    public Document loadXmlDocument(String docFileIdentifier,
            FileManager fileManager);

    /**
     * Gets the {@code src/main/webapp} logicalPath
     * 
     * @param projectOperations
     * @return
     */
    public LogicalPath getWebappPath(ProjectOperations projectOperations);

    /**
     * Gets the src/main/java logicalPath
     * 
     * @param projectOperations
     * @return
     */
    public LogicalPath getJavaPath(ProjectOperations projectOperations);

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
    public boolean addCssToTag(Document docTagx, Element root, String varName,
            String location);

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
    public boolean updateCssToTag(Document docTagx, Element root,
            String varName, String location);

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
    public boolean addUrlToTag(Document docTagx, Element root, String varName,
            String location);

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
    public boolean updateUrlToTag(Document docTagx, Element root,
            String varName, String location);

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
    public boolean addJSToTag(Document docTagx, Element root, String varName,
            String location);

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
    public boolean updateJSToTag(Document docTagx, Element root,
            String varName, String location);

    /**
     * Add css and javaScript definition to load-scripts.tagx (if not found)
     * 
     * @param cssList pairs of variable name and location
     * @param jsList pairs of variable name and location
     * @param projectOperations
     * @param fileManager
     */
    public void addJsAndCssToLoadScriptsTag(List<Pair<String, String>> cssList,
            List<Pair<String, String>> jsList,
            ProjectOperations projectOperations, FileManager fileManager);

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
    public boolean addLocaleVarToTag(Document docTagx, Element root,
            String varName);
}
