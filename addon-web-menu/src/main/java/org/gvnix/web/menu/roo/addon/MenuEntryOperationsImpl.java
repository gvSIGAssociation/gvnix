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
package org.gvnix.web.menu.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.shell.ShellService;
import org.gvnix.web.menu.roo.addon.util.FilenameUtils;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.logging.LoggingOutputStream;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of operations this add-on offers
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
@Component
// use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class MenuEntryOperationsImpl implements MenuEntryOperations {

    private static Logger logger = HandlerUtils
            .getLogger(MenuEntryOperationsImpl.class);

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * Use FileManager to modify the underlying disk storage.
     */
    @Reference
    private FileManager fileManager;

    /**
     * Use for property file configuration operations.
     */
    @Reference
    private PropFileOperations propFileOperations;

    /**
     * Use to to interact with Felix to have some sort of interactive shell that
     * allows you to issue commands to the OSGi framework
     */
    @Reference
    private ShellService shellService;
    
    /**
     * Use to interact with the OSGi execution context including locating
     * and disabling services by reference name.
     */
    private ComponentContext componentContext;

    /** menu.jspx file path */
    private String menuFile;

    /** menu.xml file path */
    private String menuConfigFile;

    /**
     * Is OSGI Roo menu component already disabled ?
     * In other words, is OSGI gvNIX menu component already activated ?
     */
    public static boolean isRooMenuDisabled = false;

    // Public operations -----

    /** {@inheritDoc} */
    protected void activate(ComponentContext context) {
        componentContext = context;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note the project isn't available when you start a new project (emtpy
     * project dir) because the project metadata doesn't exist yet.
     */
    public boolean isProjectAvailable() {
        // Check if a project has been created
        return projectOperations.isProjectAvailable(projectOperations.getFocusedModuleName());
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

    /** {@inheritDoc} */
    public boolean isGvNixMenuAvailable() {
        return fileManager.exists(getMenuConfigFile());
    }

    /** {@inheritDoc} */
    public boolean isSpringSecurityInstalled() {

        if (!isProjectAvailable()) {
            // no project available yet, we cannot check for SS
            return false;
        }

        // create Spring Security dependency entity
        Dependency dep = new Dependency("org.springframework.security",
                "spring-security-core", "3.0.5.RELEASE");

        // locate Spring Security dependency
        Set<Dependency> dependencies = projectOperations.getFocusedModule().getDependenciesExcludingVersion(dep);

        // if didn't find, Spring Security is not installed
        if (dependencies.isEmpty()) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public void setup() {
        // Parse the configuration.xml file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Add POM properties
        updatePomProperties(configuration);

        // Add dependencies to POM
        updateDependencies(configuration);

        // disable Roo MenuOperations to receive requests from clients
        // note we disable Roo MenuOp before start reading menu.jspx to avoid
        // clients create page whereas we are reading menu.jspx
        disableRooMenuOperations();

        // populate menu.xml from Roo menu.jspx
        createMenu();

        // create project menu entity model
        createEntityModel("~.web.menu");

        // create web layer artefacts
        createWebArtefacts("~.web.menu");
    }

    /** {@inheritDoc} */
    public void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String globalMessageCode, String link,
            String idPrefix) {
        addMenuItem(menuCategoryName, menuItemId, "", globalMessageCode, link,
                idPrefix, null, false, false);
    }

    /** {@inheritDoc} */
    public void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String menuItemLabel,
            String globalMessageCode, String link, String idPrefix) {
        addMenuItem(menuCategoryName, menuItemId, menuItemLabel,
                globalMessageCode, link, idPrefix, null, false, true);
    }

    /** {@inheritDoc} */
    public String addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String menuItemLabel,
            String globalMessageCode, String link, String idPrefix,
            String roles, boolean hide, boolean writeProps) {

    	Validate.notNull(menuCategoryName, "Menu category name required");
    	Validate.notNull(menuItemId, "Menu item name required");

        // Properties to be written
        Map<String, String> properties = new HashMap<String, String>();

        if (idPrefix == null || idPrefix.length() == 0) {
            idPrefix = MenuOperations.DEFAULT_MENU_ITEM_PREFIX;
        }

        Document document = getMenuDocument();

        // make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                (Element) document.getFirstChild());

        if (!rootElement.getNodeName().equals("gvnix-menu")) {
            throw new IllegalArgumentException(
                    "menu.xml hasn't valid XML structure.");
        }

        // build category name and Id:
        // - menuCategoryName is a name if it doesn't start with c_: create the
        // id
        // - menuCategoryName is an identifier if it starts with c_: create the
        // name
        String categoryName = menuCategoryName.getSymbolName();
        StringBuilder categoryId = new StringBuilder();

        if (!categoryName
                .startsWith(MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX)) {
            // create categoryId using the category name
            categoryId.append(MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX)
                    .append(categoryName.toLowerCase());
        } else {
            categoryId.append(categoryName.toLowerCase());
            // create category name using the category Id
            categoryName = StringUtils.capitalize(categoryName.substring(2));
        }

        // check for existence of menu category by looking for the identifier
        // provided
        Element category = XmlUtils.findFirstElement(
                "//*[@id='".concat(categoryId.toString()).concat("']"),
                rootElement);

        // if not exists, create new one
        if (category == null) {
            String categoryLabelCode = "menu_category_".concat(
                    categoryName.toLowerCase()).concat("_label");

            category = (Element) rootElement.appendChild(new XmlElementBuilder(
                    "menu-item", document)
                    .addAttribute("id", categoryId.toString())
                    .addAttribute("name", categoryName)
                    .addAttribute("labelCode", categoryLabelCode).build());

            properties.put(categoryLabelCode,
                    menuCategoryName.getReadableSymbolName());
        }

        // build menu item Id:
        // - if menu item id starts with 'i_', it is a valid ID but we remove
        // 'i_' for convenience
        // - otherwise, have to compose the ID
        StringBuilder itemId = new StringBuilder();

        if (menuItemId.getSymbolName().toLowerCase().startsWith(idPrefix)) {
            itemId.append(menuItemId.getSymbolName().toLowerCase());
            itemId.delete(0, idPrefix.length());
        } else {
            itemId.append(categoryName.toLowerCase()).append("_")
                    .append(menuItemId.getSymbolName().toLowerCase());
        }

        // check for existence of menu item by looking for the identifier
        // provided
        // Note that in view files, menu item ID has idPrefix_, but it doesn't
        // have
        // at application.properties, so we have to add idPrefix_ to look for
        // the given menu item but we have to add without idPrefix_ to
        // application.properties
        Element menuItem = XmlUtils.findFirstElement(
                "//*[@id='".concat(idPrefix).concat(itemId.toString())
                        .concat("']"), rootElement);

        String itemLabelCode = "menu_item_".concat(itemId.toString()).concat(
                "_label");

        if (menuItem == null) {
            menuItem = new XmlElementBuilder("menu-item", document)
                    .addAttribute("id", idPrefix.concat(itemId.toString()))
                    .addAttribute("labelCode", itemLabelCode)
                    .addAttribute(
                            "messageCode",
                            StringUtils.isNotBlank(globalMessageCode) ? globalMessageCode
                                    : "")
                    .addAttribute("url", StringUtils.isNotBlank(link) ? link : "")
                    .addAttribute("hidden", Boolean.toString(hide))
                    .addAttribute("roles",
                            StringUtils.isNotBlank(roles) ? roles : "").build();

            // TODO: gvnix*.tagx uses destination in spite of url, change to url
            category.appendChild(menuItem);
        }

        if (StringUtils.isNotBlank(menuItemLabel)) {
            properties.put(itemLabelCode, menuItemLabel);
        }

        if (writeProps) {
            propFileOperations.addProperties(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                    "/WEB-INF/i18n/application.properties", properties, true,
                    false);
        }

        writeXMLConfigIfNeeded(document);

        // return the ID assigned to new entry
        return idPrefix.concat(itemId.toString());
    }

    /** {@inheritDoc} */
    public Document getMenuDocument() {
        Document menuDocument = null;

        // it could be menu.xml has to be installed
        if (!fileManager.exists(getMenuConfigFile())) {
            installResourceIfNeeded("/WEB-INF/views/menu.xml", "menu.xml",
                    null, null);
        }

        InputStream menuIs = fileManager.getInputStream(getMenuConfigFile());
        menuDocument = org.gvnix.web.menu.roo.addon.util.XmlUtils
                .parseFile(menuIs);

        return menuDocument;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method uses MutableFile in combination with FileManager to take
     * advantage of Roo transactional file handling which offers automatic
     * rollback if an exception occurs.
     */
    public void writeXMLConfigIfNeeded(Document doc) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
                byteArrayOutputStream, doc);

        // new content
        String proposed = byteArrayOutputStream.toString();

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (fileManager.exists(getMenuConfigFile())) {
            String original = null;

            try {
                // Current content to rollback to if an exception occur
                original = IOUtils.toString(new FileReader(
                        getMenuConfigFile()));
            } catch (Exception e) {
                new IllegalStateException(
                        "Could not load file: ".concat(getMenuConfigFile()));
            }

            if (!proposed.equals(original)) {
                mutableFile = fileManager.updateFile(getMenuConfigFile());
            } else {
                // contents are equal, nothing to do
                return;
            }
        } else {
            mutableFile = fileManager.createFile(getMenuConfigFile());
            Validate.notNull(mutableFile,
                    "Could not create file '".concat(getMenuConfigFile())
                            .concat("'"));
        }

        try {
            if (mutableFile != null) {
                InputStream inputStream = null;
                OutputStreamWriter outputStream = null;
                try {
                	inputStream = IOUtils.toInputStream(proposed);
    	            outputStream = new OutputStreamWriter(mutableFile.getOutputStream());
    	            IOUtils.copy(inputStream, outputStream);
                }
                finally {
                	IOUtils.closeQuietly(inputStream);
                	IOUtils.closeQuietly(outputStream);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not output '".concat(
                    mutableFile.getCanonicalPath()).concat("'"), ioe);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Update the entry ID could change entry type because category entry starts
     * with 'c_' prefix, item entry starts with 'i_' prefix, so to change a
     * category entry to item entry you have to set a new ID that starts with
     * 'i_'.
     */
    public void updateEntry(JavaSymbolName pageId, JavaSymbolName nid,
            String label, String messageCode, String destination, String roles,
            Boolean hidden, boolean writeProps) {
        Document document = getMenuDocument();

        // Properties to be writen
        Map<String, String> properties = new HashMap<String, String>();

        // make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                (Element) document.getFirstChild());

        if (!rootElement.getNodeName().equals("gvnix-menu")) {
            throw new IllegalArgumentException(
                    "menu.xml hasn't valid XML structure.");
        }

        // check for existence of menu category by looking for the identifier
        // provided
        Element pageElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(pageId.getSymbolName()).concat("']"),
                rootElement);

        // exit if menu entry doesn't exist
        Validate.notNull(
                pageElement,
                "Menu entry '".concat(pageId.getSymbolName()).concat(
                        "' not found. [No changes done]"));

        if (nid != null) {
            pageElement.setAttribute("id", nid.getSymbolName());

            // TODO: if Element has children, children IDs should be
            // recalculated too
            // TODO: label code should change too (as addMenuItem does)
        }

        if (StringUtils.isNotBlank(label)) {
            String itemLabelCode = pageElement.getAttribute("labelCode");
            properties.put(itemLabelCode, label);
        }

        if (writeProps) {
            propFileOperations.addProperties(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                    "/WEB-INF/i18n/application.properties", properties, true,
                    true);
        }

        if (StringUtils.isNotBlank(messageCode)) {
            pageElement.setAttribute("messageCode", messageCode);
        }

        if (StringUtils.isNotBlank(destination)) {
            pageElement.setAttribute("url", destination);
        }

        if (StringUtils.isNotBlank(roles)) {
            pageElement.setAttribute("roles", roles);
        }

        if (hidden != null) {
            pageElement.setAttribute("hidden", hidden.toString());
        }

        writeXMLConfigIfNeeded(document);
    }

    /** {@inheritDoc} */
    public String getFormatedInfo(JavaSymbolName pageId, I18n lang) {
    	Validate.notNull(pageId, "Menu entry ID required");
        return getFormatedInfo(pageId, true, true, true, lang);
    }

    /** {@inheritDoc} */
    public String getCompactInfo(JavaSymbolName pageId) {
        Element rootElement = getMenuRootElement();

        // if no entry selected, show the info of all 1st level Elements
        if (pageId == null) {
            return getCompactInfo(rootElement.getChildNodes(), 0);
        }

        // check for existence of menu category by looking for the identifier
        // provided
        Element pageElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(pageId.getSymbolName()).concat("']"),
                rootElement);

        // if selected entry doesn't exist, error
        Validate.notNull(pageElement, "Page '".concat(pageId.getSymbolName())
                .concat("' not found [No info found]"));

        // show the info of selected menu entry
        return getCompactInfo(pageElement, 0);
    }

    /**
     * Iterates over a list of menu entry Nodes and call
     * {@link #getCompactInfo(Element, int)} to get the info of all the menu
     * entry Nodes in the given list.
     * 
     * @param nodes
     * @param tabSize
     * @return
     */
    private String getCompactInfo(NodeList nodes, int tabSize) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            // filter nodes that aren't menuItems
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = node.getNodeName();
            if (!nodeName.equals("menu-item")) {
                continue;
            }

            builder.append(getCompactInfo((Element) node, tabSize));
        }
        return builder.toString();
    }

    /**
     * Shows menu info in compact mode: <br/>
     * <code>
     * &nbsp;&nbsp;/tribunales  [c_tribunales, visible]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;/tribunales?form  [i_tribunales_new, visible]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;/tribunales?page=1&size=${empty param.size ? 10 : param.size}  [i_tribunales_list, hidden]<br/>
     * </code>
     * 
     * @param element
     * @param tabSize
     * @return
     */
    private String getCompactInfo(Element element, int tabSize) {
        StringBuilder builder = new StringBuilder();
        StringBuilder indent = new StringBuilder();

        // tab string to align children
        for (int i = 0; i < tabSize; i++) {
            indent.append(" ");
        }

        String url = element.getAttribute("url");
        if (StringUtils.isNotBlank(url)) {
            builder.append(indent).append(url).append("  ");
        }

        // string containing "[ID, visibility]: "
        StringBuilder idVisibility = new StringBuilder();
        idVisibility.append("[").append(element.getAttribute("id"));
        String hidden = element.getAttribute("hidden");
        if (!StringUtils.isNotBlank(hidden)) {
            hidden = "false"; // visible by default
        }
        if (Boolean.valueOf(hidden)) {
            idVisibility.append(", hidden");
        } else {
            idVisibility.append(", visible");
        }
        if (!StringUtils.isNotBlank(url)) {
            idVisibility.append(", no-URL");
        }
        idVisibility.append("]");

        // build Element info
        builder.append(idVisibility);

        // get children info
        if (element.hasChildNodes()) {
            builder.append("\n")
                    .append(getCompactInfo(element.getChildNodes(),
                            tabSize + 10)).append("\n");
        } else {
            builder.append("\n"); // empty line
        }
        return builder.toString();
    }

    /** {@inheritDoc} */
    public String getFormatedInfo(JavaSymbolName pageId, boolean label,
            boolean messageCode, boolean roles, I18n lang) {
        Element rootElement = getMenuRootElement();

        // if no entry selected, show the info of all 1st level Elements
        if (pageId == null) {
            return getFormatedInfo(rootElement.getChildNodes(), label,
                    messageCode, roles, lang, 0);
        }

        // check for existence of menu category by looking for the identifier
        // provided
        Element pageElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(pageId.getSymbolName()).concat("']"),
                rootElement);

        // if selected entry doesn't exist, error
        Validate.notNull(pageElement, "Page '".concat(pageId.getSymbolName())
                .concat("' not found [No info found]"));

        // show the info of selected menu entry
        return getFormatedInfo(pageElement, label, messageCode, roles, lang, 0);
    }

    /**
     * Returns the Root element of the menu.xml file
     * 
     * @return
     */
    private Element getMenuRootElement() {
        Document document = getMenuDocument();

        // make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                (Element) document.getFirstChild());

        if (!rootElement.getNodeName().equals("gvnix-menu")) {
            throw new IllegalArgumentException(
                    "menu.xml hasn't valid XML structure.");
        }

        return rootElement;
    }

    /** {@inheritDoc} */
    public void moveBefore(JavaSymbolName pageId, JavaSymbolName beforeId) {
        Document document = getMenuDocument();

        // make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                (Element) document.getFirstChild());

        if (!rootElement.getNodeName().equals("gvnix-menu")) {
            throw new IllegalArgumentException(
                    "menu.xml hasn't valid XML structure.");
        }

        // check for existence of menu category by looking for the identifier
        // provided
        Element pageElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(pageId.getSymbolName()).concat("']"),
                rootElement);

        // exit if menu entry doesn't exist
        Validate.notNull(pageElement, "Page '".concat(pageId.getSymbolName())
                .concat("' not found. [No changes done]"));

        Element beforeElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(beforeId.getSymbolName()).concat("']"),
                rootElement);

        // exit if menu entry doesn't exist
        Validate.notNull(beforeElement, "Page '".concat(beforeId.getSymbolName())
                .concat("' not found. [No changes done]"));

        // page parent element where remove menu entry element
        Element pageParentEl = (Element) pageElement.getParentNode();
        pageParentEl.removeChild(pageElement);

        // before parent element where execute insert before
        Element beforeParentEl = (Element) beforeElement.getParentNode();
        beforeParentEl.insertBefore(pageElement, beforeElement);

        writeXMLConfigIfNeeded(document);
    }

    /** {@inheritDoc} */
    public void moveInto(JavaSymbolName pageId, JavaSymbolName intoId) {
        Document document = getMenuDocument();

        // make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                (Element) document.getFirstChild());

        if (!rootElement.getNodeName().equals("gvnix-menu")) {
            throw new IllegalArgumentException(
                    "menu.xml hasn't valid XML structure.");
        }

        // check for existence of menu category by looking for the identifier
        // provided
        Element pageElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(pageId.getSymbolName()).concat("']"),
                rootElement);

        // exit if menu entry doesn't exist
        Validate.notNull(pageElement, "Page '".concat(pageId.getSymbolName())
                .concat("' not found. [No changes done]"));

        Element intoElement = XmlUtils.findFirstElement(
                "//*[@id='".concat(intoId.getSymbolName()).concat("']"),
                rootElement);

        // exit if menu entry doesn't exist
        Validate.notNull(intoElement, "Page '".concat(intoId.getSymbolName())
                .concat("' not found. [No changes done]"));

        // parent element where remove menu entry element
        Element parent = (Element) pageElement.getParentNode();
        parent.removeChild(pageElement);

        // insert
        intoElement.appendChild(pageElement);

        writeXMLConfigIfNeeded(document);
    }

    /** {@inheritDoc} */
    public String getMenuConfigFile() {

        // resolve path for menu.xml if it hasn't been resolved yet
        if (menuConfigFile == null) {
            menuConfigFile = getPathResolver().getIdentifier(
            		LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), "/WEB-INF/views/menu.xml");
        }
        return menuConfigFile;
    }

    /**
     * Get and initialize the absolute path for the {@code menu.jspx}.
     * 
     * @return the absolute path to the file (never null)
     */
    public String getMenuFile() {

        // resolve absolute path for menu.jspx if it hasn't been resolved yet
        if (menuFile == null) {
            menuFile = getPathResolver().getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                    "/WEB-INF/views/menu.jspx");
        }
        return menuFile;
    }

    // Private operations and utils -----

    /**
     * Utility to get {@link PathResolver}.
     * 
     * @return PathResolver or null if project isn't available yet
     */
    private PathResolver getPathResolver() {

        // Use PathResolver to resolve between {@link File}, {@link Path} and
        // canonical path {@link String}s.
        // See {@link MavenPathResolver} to know location values
        PathResolver pathResolver = projectOperations.getPathResolver();

        return pathResolver;
    }

    /**
     * Create Menu model that lets the project works with the underlying menu
     * structure representation, i.e, menu.xml
     * 
     * @param targetPackage
     *            Java entities will be created inside this package
     */
    protected void createEntityModel(String targetPackage) {

        installIfNeeded("MenuItem.java", targetPackage);
        installIfNeeded("Menu.java", targetPackage);
        installIfNeeded("MenuLoader.java", targetPackage);
        installIfNeeded("ContextMenuStrategy.java", targetPackage);
        installIfNeeded("BaseURLContextMenuStrategy.java", targetPackage);
        installIfNeeded("URLBrothersContextMenuStrategy.java", targetPackage);
        installIfNeeded("URLChildrenContextMenuStrategy.java", targetPackage);
    }

    /** {@inheritDoc} */
    public void createWebArtefacts(String classesPackage) {

        // parameters Map to replace variables in file templates
        Map<String, String> params = new HashMap<String, String>();

        // resolve given classes package
        String javaPackage = getFullyQualifiedPackageName(classesPackage);

        // Put variable values in parameters Map
        params.put("__TOP_LEVEL_PACKAGE__", projectOperations
                .getTopLevelPackage(projectOperations.getFocusedModuleName()).getFullyQualifiedPackageName());
        params.put("__MENU_MODEL_CLASS__", javaPackage.concat(".Menu"));

        // install tags
        installResourceIfNeeded("/WEB-INF/tags/menu/gvnixmenu.tagx",
                "gvnixmenu.tagx", params, new String[] { "<menu:gvnixitem" });

        if (isSpringSecurityInstalled()) {
            installResourceIfNeeded("/WEB-INF/tags/menu/gvnixitem.tagx",
                    "gvnixitem-sec.tagx", null,
                    new String[] { "<menu:gvnixitem", " xmlns:spring=",
                            " xmlns:sec=" });
        } else {
            installResourceIfNeeded("/WEB-INF/tags/menu/gvnixitem.tagx",
                    "gvnixitem.tagx", null, new String[] { "<menu:gvnixitem" });
        }

        // change menu.jspx to use gvnix menu tag that will render menu.xml
        installResourceIfNeeded("/WEB-INF/views/menu.jspx", "menu.jspx", null,
                new String[] { "menu:gvnixmenu" });
    }

    /**
     * Install a file template if it doesn't exist
     * <p>
     * This method has been copied from Maven addon, maybe it could be
     * refactored to utility class.
     * 
     * @param targetFilename
     *            File to create. Note this method will create the file by
     *            locating a file template with the same name as target file but
     *            prefixing "-template" to file extension. For example, given
     *            {@code Menu.java} will locate {@code Menu-template.java}
     */
    private void installIfNeeded(String targetFilename, String targetPackage) {

        String destinationFile = getAbsolutePath(targetPackage, targetFilename);

        if (!fileManager.exists(destinationFile)) {
            try {
                // Read template and insert the user's package
                String fileName = FilenameUtils.removeExtension(targetFilename);
                String fileExt = FilenameUtils.getExtension(targetFilename);

                InputStream templateInputStream = FileUtils.getInputStream(
                        getClass(), fileName.concat("-template").concat(".")
                                .concat(fileExt));
                String input = IOUtils.toString(new InputStreamReader(templateInputStream));

                // replace template variables
                input = input.replace("__TOP_LEVEL_PACKAGE__", projectOperations
                        .getTopLevelPackage(projectOperations.getFocusedModuleName()).getFullyQualifiedPackageName());

                // Output the file for the user
                // use MutableFile in combination with FileManager to take
                // advantage of
                // Roos transactional file handling which offers automatic
                // rollback if an
                // exception occurs
                MutableFile mutableFile = fileManager
                        .createFile(destinationFile);
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                	inputStream = IOUtils.toInputStream(input);
    	            outputStream = mutableFile.getOutputStream();
    	            IOUtils.copy(inputStream, outputStream);
                }
                finally {
                	IOUtils.closeQuietly(inputStream);
                	IOUtils.closeQuietly(outputStream);
                }
            } catch (IOException ioe) {
                throw new IllegalStateException("Unable to create '".concat(
                        targetFilename).concat("'"), ioe);
            }
        }
    }

    /**
     * Creates or updates the contents for one resource represented by the given
     * target file path and relative source path.
     * 
     * @param relativePath
     *            path relative to {@link Path.SRC_MAIN_WEBAPP} of target file
     * @param resourceName
     *            path relative to classpath of file to be copied (cannot be
     *            null)
     * @param toReplace
     * @param containsStrings
     */
    private void installResourceIfNeeded(String relativePath,
            String resourceName, Map<String, String> toReplace,
            String[] containsStrings) {
        PathResolver pathResolver = getPathResolver();

        String targetPath = pathResolver.getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                relativePath);

        InputStream resource = getClass().getResourceAsStream(resourceName);

        String sourceContents;
        String targetContents = null;

        // load resource to copy
        try {
            sourceContents = IOUtils.toString(new InputStreamReader(
                    resource));
            // Replace params
            if (toReplace != null) {
                for (Entry<String, String> entry : toReplace.entrySet()) {
                    sourceContents = StringUtils.replace(sourceContents,
                            entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load file to be copied '".concat(resourceName)
                            .concat("'"), e);
        } finally {
            try {
                resource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // load target contents if exists
        if (fileManager.exists(targetPath)) {
            FileReader reader = null;
            try {
                reader = new FileReader(targetPath);
                targetContents = IOUtils.toString(reader);
            } catch (Exception e) {
                throw new IllegalStateException("Error reading '".concat(
                        targetPath).concat("'"), e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // prepare mutable file
        // use MutableFile in combination with FileManager to take advantage of
        // Roos transactional file handling which offers automatic rollback if
        // an
        // exception occurs
        MutableFile target = null;
        if (targetContents == null) {
            target = fileManager.createFile(targetPath);
        } else {
            // decide if need to replace target
            if (containsStrings == null || containsStrings.length == 0) {
                // No checks to do
                target = fileManager.updateFile(targetPath);
            } else {
                for (String contains : containsStrings) {
                    if (!targetContents.contains(contains)) {
                        target = fileManager.updateFile(targetPath);
                        break;
                    }
                }
            }
        }

        if (target == null) {
            return;
        }

        try {
            InputStream inputStream = null;
            OutputStreamWriter outputStream = null;
            try {
            	inputStream = IOUtils.toInputStream(sourceContents);
	            outputStream = new OutputStreamWriter(target.getOutputStream());
	            IOUtils.copy(inputStream, outputStream);
            }
            finally {
            	IOUtils.closeQuietly(inputStream);
            	IOUtils.closeQuietly(outputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create/update '".concat(
                    targetPath).concat("'"), e);
        }
    }

    /**
     * Get the absolute path to a file name in given package name.
     * 
     * @param packageName
     *            fully qualified package name
     * @param fileName
     *            file to get its absolute path
     * @return Path.SRC_MAIN_JAVA + packagePath + fileName
     */
    private String getAbsolutePath(String packageName, String fileName) {
        String fullyQualifPackage = getFullyQualifiedPackageName(packageName);

        // default package
        String packagePath = fullyQualifPackage.replace('.', '/');

        return projectOperations.getPathResolver().getIdentifier(
        		LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""), packagePath.concat("/").concat(fileName));
    }

    /**
     * Convert a package name to a fully qualified package name with full
     * support for using "~" as denoting the user's top-level package.
     * 
     * @param packageName
     * @param prjMetadata
     * @return
     */
    private String getFullyQualifiedPackageName(String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return "";
        }

        // by default return the given packageName
        String newPackage = packageName.toLowerCase();

        // resolve "~" as denoting the user's top-level package
        if (packageName.startsWith("~")) {
            String topLevelPath = "";
            topLevelPath = projectOperations
                    .getTopLevelPackage(projectOperations.getFocusedModuleName())
                    .getFullyQualifiedPackageName();
            // analyze char after ~, if it is . do nothing, else concat .
            // between
            // topLevelPath and given package name and remove ~ at start
            if (packageName.length() > 1) {
                newPackage = (!(packageName.charAt(1) == '.') ? topLevelPath
                        .concat(".") : topLevelPath).concat(packageName
                        .substring(1));
            }
            // when given packageName is ~
            else {
                newPackage = topLevelPath;
            }
        }

        // normalize
        if (newPackage.endsWith(".")) {
            newPackage = newPackage.substring(0, newPackage.length() - 1);
        }
        return newPackage;
    }

    /**
     * Create menu.xml from menu.jspx.
     * <p>
     * Iterates over menu.jspx elements and delegates menu.xml creation to
     * {@link #addMenuItem(JavaSymbolName, JavaSymbolName, String, String, String, String)}
     * , the same method used to create menu entries from shell.
     */
    private void createMenu() {

        InputStream rooMenuInput = fileManager.getInputStream(getMenuFile());

        try {
            Document rooMenuDoc = XmlUtils.getDocumentBuilder().parse(
                    rooMenuInput);
            Element root = rooMenuDoc.getDocumentElement();
            Element menu = XmlUtils.findFirstElement("/div/menu", root);
            if (menu == null) {
            	// Roo menu exists but is still empty: there is no menu element into div element already
            	// Avoid error when execute "web mvc setup" and next "menu setup"
            	return;
            }
            
            // root categories and items
            NodeList menuElements = menu.getChildNodes();
            for (int i = 0; i < menuElements.getLength(); i++) {
                Node tmpNode = menuElements.item(i);
                if (tmpNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String nodeName = tmpNode.getNodeName();

                // process root category elements
                if (nodeName.equals("menu:category")) {
                    Element menuCategory = (Element) tmpNode;

                    // We have to recover original categoryName to success
                    // addMenuItem
                    // execution
                    JavaSymbolName categoryId = new JavaSymbolName(
                            menuCategory.getAttribute("id"));
                    JavaSymbolName categoryName = new JavaSymbolName(
                            StringUtils.capitalize(categoryId.getSymbolName()
                                    .substring(2)));

                    NodeList menuItems = menuCategory.getChildNodes();

                    // process category inner item elements
                    for (int j = 0; j < menuItems.getLength(); j++) {
                        tmpNode = menuItems.item(j);
                        if (tmpNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element menuItem = (Element) tmpNode;

                        // - At menu.jspx there isn't label, no prob because it
                        // was added to
                        // application.properties
                        JavaSymbolName menuItemId = new JavaSymbolName(
                                menuItem.getAttribute("id"));
                        String menuItemPrefix = getMenuItemPrefix(menuItemId
                                .getSymbolName());

                        addMenuItem(categoryName, menuItemId, null,
                                menuItem.getAttribute("messageCode"),
                                menuItem.getAttribute("url"), menuItemPrefix);
                    }
                }
                // item elements must be inside a category
                else if (nodeName.equals("menu:item")) {
                    Element menuItem = (Element) tmpNode;
                    JavaSymbolName menuItemId = new JavaSymbolName(
                            menuItem.getAttribute("id"));
                    logger.log(
                            Level.SEVERE,
                            "Found menu:item '"
                                    .concat(menuItemId.getSymbolName())
                                    .concat("' in menu:menu tag. It must be in "
                                            .concat("menu:category tag [Ignored]")));
                } else {
                    logger.warning("Found unknow element in menu:menu tag: '"
                            .concat(nodeName).concat("' [Ignored]"));
                }

            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disableRooMenuOperations() {
        logger.fine("Disable Roo MenuOperationsImpl");

        ServiceReference rooServiceRef = componentContext.getBundleContext()
                .getServiceReference(MenuOperations.class.getName());
        if (rooServiceRef != null) {
        	
  	        Long componentId = (Long) rooServiceRef.getProperty("component.id");
  	
  	        try {
  	            executeFelixCommand("scr disable ".concat(componentId.toString()));
  				isRooMenuDisabled = true;
  	        } catch (Exception e) {
  	            throw new IllegalStateException(
  	                    "Exception disabling Roo MenuOperationsImpl service", e);
  	        }
        }
    }

    /**
     * Execute Felix shell commands
     * 
     * @param commandLine
     * @throws Exception
     */
    private void executeFelixCommand(String commandLine) throws Exception {
        LoggingOutputStream sysOut = new LoggingOutputStream(Level.INFO);
        LoggingOutputStream sysErr = new LoggingOutputStream(Level.SEVERE);
        sysOut.setSourceClassName(MenuEntryOperationsImpl.class.getName());
        sysErr.setSourceClassName(MenuEntryOperationsImpl.class.getName());

        PrintStream printStreamOut = new PrintStream(sysOut);
        PrintStream printErrOut = new PrintStream(sysErr);
        try {
            shellService.executeCommand(commandLine, printStreamOut,
                    printErrOut);
        } finally {
            printStreamOut.close();
            printErrOut.close();
        }
    }

    /**
     * Iterates over a list of menu entry Nodes and call
     * {@link #getFormatedInfo(Element, boolean, boolean, boolean, boolean, int)}
     * to get the info of all the menu entry Nodes in the given list.
     * 
     * @param nodes
     * @param label
     * @param messageCode
     * @param roles
     * @param tabSize
     * @return
     */
    private String getFormatedInfo(NodeList nodes, boolean label,
            boolean messageCode, boolean roles, I18n lang, int tabSize) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            // filter nodes that aren't menuItems
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = node.getNodeName();
            if (!nodeName.equals("menu-item")) {
                continue;
            }

            builder.append(getFormatedInfo((Element) node, label, messageCode,
                    roles, lang, tabSize));
        }
        return builder.toString();
    }

    /**
     * Gets the selected info about the given menu entry Element.
     * <p>
     * TODO: I think compact info better. See section "List menu structure" at
     * "docs/pd-addon-web-menu.rst". Note, it means we should refactor this
     * method in 2 methods: on for list command and other for info command
     * 
     * @param element
     * @param label
     * @param messageCode
     * @param destination
     * @param roles
     * @param tabSize
     * @return
     */
    private String getFormatedInfo(Element element, boolean label,
            boolean message, boolean roles, I18n lang, int tabSize) {
        StringBuilder builder = new StringBuilder();
        StringBuilder indent = new StringBuilder();

        // tab string to align children
        for (int i = 0; i < tabSize; i++) {
            indent.append(" ");
        }

        // string containing "[ID]: "
        StringBuilder idInfo = new StringBuilder();
        idInfo.append("[").append(element.getAttribute("id")).append("]");

        // build Element info
        builder.append(indent).append(idInfo).append("\n");

        String url = element.getAttribute("url");
        if (!StringUtils.isNotBlank(url)) {
            url = "No";
        }
        builder.append(indent).append("URL          : ").append(url)
                .append("\n");

        if (label) {
            String labelCode = element.getAttribute("labelCode");
            String labelValue = null;

            // get label text from application.properties
            if (StringUtils.isNotBlank(labelCode)) {
                labelValue = propFileOperations.getProperty(
                		LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/i18n/application.properties", labelCode);
            }
            builder.append(indent).append("Label Code   : ").append(labelCode)
                    .append("\n");
            builder.append(indent).append("Label        : ")
                    .append(labelValue != null ? labelValue : "").append("\n");
        }
        if (message) {
            String messageCode = element.getAttribute("messageCode");
            String messageValue = null;

            // get message text from messages.properties
            if (StringUtils.isNotBlank(messageCode)) {

                if (lang != null) {
                    String messageBundle = "/WEB-INF/i18n/messages_".concat(
                            lang.getLocale().toString()).concat(".properties");
                    messageValue = propFileOperations.getProperty(
                    		LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), messageBundle, messageCode);
                }

                // if no value for given lang, try default lang
                if (!StringUtils.isNotBlank(messageValue)) {
                    String messageBundle = "/WEB-INF/i18n/messages.properties";
                    messageValue = propFileOperations.getProperty(
                    		LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), messageBundle, messageCode);
                }
            }
            builder.append(indent).append("Message Code : ")
                    .append(element.getAttribute("messageCode")).append("\n");
            builder.append(indent).append("Message      : ")
                    .append(messageValue != null ? messageValue : "")
                    .append("\n");
        }
        if (roles) {
            builder.append(indent).append("Roles        : ")
                    .append(element.getAttribute("roles")).append("\n");
        }

        String hidden = element.getAttribute("hidden");
        if (!StringUtils.isNotBlank(hidden)) {
            hidden = "false"; // visible by default
        }
        builder.append(indent).append("Hidden       : ").append(hidden)
                .append("\n");

        // get children info
        if (element.hasChildNodes()) {
            builder.append(indent).append("Children     : ").append("\n");
            builder.append(getFormatedInfo(element.getChildNodes(), label,
                    message, roles, lang, tabSize + 15)); // indent to the right
                                                          // of
                                                          // "Children     : "
                                                          // (length 15 chars)
        } else {
            builder.append("\n"); // empty line
        }
        return builder.toString();
    }

    /**
     * Install properties defined in external XML file
     * 
     * @param configuration
     */
    private void updatePomProperties(Element configuration) {
        List<Element> addonProperties = XmlUtils.findElements(
                "/configuration/gvnix/menu/properties/*", configuration);
        for (Element property : addonProperties) {
            projectOperations.addProperty(projectOperations.getFocusedModuleName(), new Property(property));
        }
    }

    /**
     * Install dependencies defined in external XML file
     * 
     * @param configuration
     */
    private void updateDependencies(Element configuration) {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        List<Element> securityDependencies = XmlUtils.findElements(
                "/configuration/gvnix/menu/dependencies/dependency",
                configuration);
        for (Element dependencyElement : securityDependencies) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.addDependencies(projectOperations.getFocusedModuleName(), dependencies);
    }

    /**
     * Gets menu item prefix: {@link MenuOperations#DEFAULT_MENU_ITEM_PREFIX} or
     * {@link MenuOperations#FINDER_MENU_ITEM_PREFIX}
     * 
     * @param itemId
     * @return MenuOperations.FINDER_MENU_ITEM_PREFIX if given itemId starts
     *         with "fi_", otherwise return
     *         MenuOperations.DEFAULT_MENU_ITEM_PREFIX
     */
    private String getMenuItemPrefix(String itemId) {
        if (itemId.startsWith(MenuOperations.FINDER_MENU_ITEM_PREFIX)) {
            return MenuOperations.FINDER_MENU_ITEM_PREFIX;
        } else if (itemId.startsWith("si_")) {
            return "si_";
        }
        return MenuOperations.DEFAULT_MENU_ITEM_PREFIX;
    }

    /**
     * Get the absolute path for {@code webmvc-config.xml}.
     * 
     * @return the absolute path to file (never null)
     */
    private String getMvcConfigFile() {

        // resolve absolute path for menu.jspx if it hasn't been resolved yet
        return getPathResolver().getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
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
        return getPathResolver().getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "/WEB-INF/layouts/layouts.xml");
    }
}
