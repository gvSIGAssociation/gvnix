/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures     
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.menu.MenuOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * gvNIX menu operations service implementation
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class MenuPageOperationsImpl implements MenuPageOperations {

    private static Logger logger = Logger
	    .getLogger(MenuPageOperationsImpl.class.getName());

    private static final Dependency DEPENDENCY_GVNIX_WEB_ANOTATIONS = new Dependency(
	    "org.gvnix", "org.gvnix.annotations", "0.3.0-SNAPSHOT"); // FIXME
    // Version
    // must
    // be
    // load
    // by
    // anyway

    private static final String SS_VERSION = "3.0.3.CI-SNAPSHOT";

    private static final Dependency DEPENDENCY_SS_CORE = new Dependency(
	    "org.springframework.security", "spring-security-core", SS_VERSION);

    public static final Property PROPERTY_SS_VERSION = new Property(
	    "spring-security.version", SS_VERSION);

    public static final String INDENT = "    ";

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;

    private ComponentContext context;

    // Path to web.xml file
    private String webXmlFileName;

    private Document menuDocument = null;

    private MenuPageItem rooMenuRootPath = null;

    private boolean working;

    private MenuPageOperationsImplHelper helper;

    private Object lock = new Object();

    private Long originalMenuOperationsBundleId;

    protected void activate(ComponentContext context) {
	this.context = context;
	this.webXmlFileName = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/web.xml");

	helper = new MenuPageOperationsImplHelper(this, pathResolver,
		fileManager);

	if (isActivated()) {
	    activateMenuOperations();
	    makeSpringSecurityChecks();
	}
    }

    protected void deactivate(ComponentContext context) {
	if (isActivated()) {
	    this.deactivateMenuOperations();
	    this.cleanupMetadaCache();
	}
    }

    private void cleanupMetadaCache() {
	MenuItemModelMetadata item = (MenuItemModelMetadata) metadataService
		.get(MenuItemModelMetadata.getMetadataIdentiferFinal());
	MenuModelMetadata menu = (MenuModelMetadata) metadataService
		.get(MenuModelMetadata.getMetadataIdentiferFinal());
	MenuLoaderMetadata loader = (MenuLoaderMetadata) metadataService
		.get(MenuLoaderMetadata.getMetadataIdentiferFinal());

	if (item != null) {
	    metadataService.evict(item.getId());
	}

	if (menu != null) {
	    metadataService.evict(menu.getId());
	}

	if (loader != null) {
	    metadataService.evict(loader.getId());
	}

    }

    public boolean isProjectAvailable() {
	return getPathResolver() != null;
    }

    /**
     * <p>
     * Checks if this is a web project
     * </p>
     *
     * <p>
     * This method caches value to best performance
     * </p>
     *
     * @see #projectValuesChanged()
     * @return
     */
    private boolean isWebProject() {
	return fileManager.exists(webXmlFileName);
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }

    public boolean isActivated() {
	if (!isProjectAvailable()) {
	    return false;
	}
	if (isWorking()) {
	    return false;
	}

	if (!fileManager.exists(pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/menu/gvnixitem.tagx"))) {
	    return false;
	}
	if (!fileManager.exists(pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/menu/gvnixmenu.tagx"))) {
	    return false;
	}
	return true;
    }

    public boolean isCorrectlyInstalled() {
	if (!isActivated()) {
	    return false;
	}
	MenuItemModelMetadata item = (MenuItemModelMetadata) metadataService
		.get(MenuItemModelMetadata.getMetadataIdentiferFinal());
	MenuModelMetadata menu = (MenuModelMetadata) metadataService
		.get(MenuModelMetadata.getMetadataIdentiferFinal());
	MenuLoaderMetadata loader = (MenuLoaderMetadata) metadataService
		.get(MenuLoaderMetadata.getMetadataIdentiferFinal());

	if (item != null && menu != null && loader != null) {
	    return true;
	}
	if (item == null && menu == null && loader == null) {
	    return false;
	}

	/*
	 * if (item == null) {
	 * logger.warning("menu page: Missing item model class"); } if (menu ==
	 * null) { logger.warning("menu page: Missing menu model class"); } if
	 * (loader == null) {
	 * logger.warning("menu page: Missing menu loader class"); }
	 */
	return false;

    }

    public boolean isWorking() {
	return working;
    }

    public MenuPageItem getPageItem(String path) {
	return helper.getPageItem(getMenuDocument(), path);
    }

    Document getMenuDocument() {
	if (menuDocument == null) {
	    MenuLoaderMetadata loader = (MenuLoaderMetadata) metadataService
		    .get(MenuLoaderMetadata.getMetadataIdentiferFinal());
	    if (loader == null) {
		return null;
	    }
	    if (loader.getConfigXMLFile() == null) {
		return null;
	    }
	    String menuConfigPath = pathResolver.getIdentifier(
		    Path.SRC_MAIN_WEBAPP, loader.getConfigXMLFile());

	    menuDocument = helper.getDocument(menuConfigPath);
	    if (menuDocument == null) {
		if (!fileManager.exists(menuConfigPath)) {
		    helper.createOrUpdateFileFromResourses(loader
			    .getConfigXMLFile(), "gvnix-menu.xml", null, null);
		    menuDocument = helper.getDocument(menuConfigPath);
		} else {
		    throw new IllegalStateException("Bad menu config file: "
			    + menuConfigPath);
		}
	    }
	}
	return menuDocument;
    }

    List<MenuPageItem> getMenuItemChildren(MenuPageItem page) {
	return helper.getMenuItemChildren(getMenuDocument(), page);
    }

    void makeSpringSecurityChecks() {

	boolean alreadyWorking = false;
	try {
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }
	    createOrUpdateMenuTagx();
	} finally {
	    if (!alreadyWorking) {
		working = false;
	    }
	}
    }

    private void createOrUpdateMenuTagx() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());

	MenuModelMetadata menu = (MenuModelMetadata) metadataService
		.get(MenuModelMetadata.getMetadataIdentiferFinal());

	Map<String, String> params = new HashMap<String, String>();

	params.put("__TOP_LEVEL_PACKAGE__", projectMetadata
		.getTopLevelPackage().getFullyQualifiedPackageName());

	params
		.put("__MENU_MODEL_CLASS__", menu.getPhysicalTypeMetadata()
			.getPhysicalTypeDetails().getName()
			.getFullyQualifiedTypeName());

	helper.createOrUpdateFileFromResourses(
		"WEB-INF/tags/menu/gvnixmenu.tagx", "gvnixmenu.tagx", params,
		new String[] { "<menu:gvnixitem" });

	if (isSpringSecurityInstalled()) {
	    helper.createOrUpdateFileFromResourses(
		    "WEB-INF/tags/menu/gvnixitem.tagx", "gvnixitem-sec.tagx",
		    null, new String[] { "<menu:gvnixitem", " xmlns:spring=" });
	} else {
	    helper.createOrUpdateFileFromResourses(
		    "WEB-INF/tags/menu/gvnixitem.tagx", "gvnixitem.tagx", null,
		    new String[] { "<menu:gvnixitem" });
	}
    }

    /**
     * Checks if Spring Security is installed.
     *
     * If it's installed it will check and assure that it's 3.0.3CI-SNAPSHOT or
     * higher
     *
     * @return
     */
    private boolean isSpringSecurityInstalled() {

	metadataService.evict(ProjectMetadata.getProjectIdentifier());
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());

	Set<Dependency> dependencies = projectMetadata
		.getDependenciesExcludingVersion(DEPENDENCY_SS_CORE);

	if (dependencies.isEmpty()) {
	    return false;
	}

	// This is needed until roo ships Spring Security > 3.0.2
	// for http://jira.springframework.org/browse/SEC-1456
	// After that remove this code.
	Set<Property> ssProperties = projectMetadata
		.getPropertiesExcludingValue(PROPERTY_SS_VERSION);

	Assert.isTrue(ssProperties.size() == 1, "More than one properties '"
		+ PROPERTY_SS_VERSION.getName() + "'");

	Property currentVersionProperty = ssProperties.iterator().next();
	if (haveToUpdateSpringSecurity(currentVersionProperty.getValue())) {
	    projectOperations.addProperty(PROPERTY_SS_VERSION);
	}

	fileManager.scan();
	return true;
    }

    private boolean haveToUpdateSpringSecurity(String currentVersion) {
	// version split
	String[] currentVersionSplit = StringUtils.tokenizeToStringArray(
		currentVersion, ".");
	String[] targetVersionSplit = StringUtils.tokenizeToStringArray(
		SS_VERSION, ".");

	if (currentVersionSplit.length < 3) {
	    return true;
	}
	for (int i = 0; i < 3; i++) {
	    if (Integer.parseInt(currentVersionSplit[i]) < Integer
		    .parseInt(targetVersionSplit[i])) {
		return true;
	    }
	}
	return false;
    }

    private void createModel() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());

	helper.installIfNeeded("MenuItem.java", "~.web.menu", projectMetadata);
	helper.installIfNeeded("Menu.java", "~.web.menu", projectMetadata);
	helper
		.installIfNeeded("MenuLoader.java", "~.web.menu",
			projectMetadata);
	helper.installIfNeeded("ContextMenuStrategy.java", "~.web.menu",
		projectMetadata);
	helper.installIfNeeded("BaseURLContextMenuStrategy.java", "~.web.menu",
		projectMetadata);
	helper.installIfNeeded("URLBrothersContextMenuStrategy.java",
		"~.web.menu", projectMetadata);
	helper.installIfNeeded("URLChildrenContextMenuStrategy.java",
		"~.web.menu", projectMetadata);

	fileManager.scan();
    }

    private void activateMenuOperations() {
	// logger.warning("-- Activating Menu");

	// context
	// .enableComponent(org.springframework.roo.addon.web.menu.MenuOperationsImpl.class
	// .getName());

	ServiceReference serviceReferenceOperation = context.getBundleContext()
		.getServiceReference(MenuOperations.class.getName());

	Object currentService = context.getBundleContext().getService(
		serviceReferenceOperation);
	// logger.warning("-- Current Menu Service class:"
	// + currentService.getClass().getName());

	if ((currentService.getClass().getName() == MenuOperationsImpl.class
		.getName())) {
	    // logger.warning("-- Already set gvnix MenuOperations");
	    if (isCorrectlyInstalled()) {
		((MenuOperationsImpl) currentService).performDelayed(this);
	    }
	    return;
	}

	long bundleId = serviceReferenceOperation.getBundle().getBundleId();

	context.getBundleContext().ungetService(serviceReferenceOperation);

	try {
	    context.getBundleContext().getBundle(bundleId).stop();
	} catch (BundleException e) {
	    throw new IllegalStateException(
		    "Exception stopping the Default menu poperation", e);
	}

	originalMenuOperationsBundleId = bundleId;

	serviceReferenceOperation = context.getBundleContext()
		.getServiceReference(MenuOperations.class.getName());
	if (serviceReferenceOperation == null) {
	    // logger.warning("-- Service null after activation");
	    context.enableComponent(MenuOperationsImpl.class.getName());

	    serviceReferenceOperation = context.getBundleContext()
		    .getServiceReference(MenuOperations.class.getName());

	    if (serviceReferenceOperation == null) {
		logger.severe("-- Can't start service menu");
		return;
	    }

	}
	// Object service = context.getBundleContext().getService(
	// serviceReferenceOperation);

	// logger.warning("-- Activated Menu: " + service.getClass().getName());

	// context.getBundleContext().ungetService(serviceReferenceOperation);
    }

    private void deactivateMenuOperations() {

	if (originalMenuOperationsBundleId != null) {

	    // context.disableComponent(MenuOperationsImpl.class.getName());

	    try {
		context.getBundleContext().getBundle(
			originalMenuOperationsBundleId.longValue()).start();
	    } catch (BundleException e) {
		throw new IllegalStateException(
			"Error starting std menu operation", e);
	    }
	    originalMenuOperationsBundleId = null;
	}

    }

    private void loadMenuFromOldMenu() {
	// This method delegates on add menu item public method
	String rooMenuPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/views/menu.jspx");
	InputStream rooMenuInput = fileManager.getInputStream(rooMenuPath);
	helper.loadMenuFromOldMenu(rooMenuInput);
    }

    protected void addOrUpdatePageFromRooMenuItem(
	    JavaSymbolName menuCategoryName, JavaSymbolName menuItemName,
	    String globalMessageCode, String link, String idPrefix) {
	helper.addOrUpdatePageFromRooMenuItem(menuCategoryName, menuItemName,
		globalMessageCode, link, idPrefix);
    }

    protected MenuPageItem getMenuItemByRooId(String rooId) {
	Element root = getMenuDocument().getDocumentElement();
	return helper.getMenuItemByRooId(root, rooId);
    }

    public boolean isSetupAvailable() {
	return isWebProject() && !isActivated() && !isCorrectlyInstalled();
    }

    // ******************************************
    // ******************************************
    // ************** Commands ******************

    public void setup() {
	Assert.isTrue(this.isSetupAvailable(), "Operation not available");
	working = true;
	synchronized (lock) {
	    try {
		// Add annotation dependency
		addAnnotationDependecy();

		// Create model
		createModel();

		// Create gvnixmenu.tagx
		createOrUpdateMenuTagx();

		// load old menus registered in menu.jspx
		loadMenuFromOldMenu();

		// update menu.jspx
		helper.createOrUpdateFileFromResourses(
			"WEB-INF/views/menu.jspx", "menu.jspx", null,
			new String[] { "menu:gvnixmenu" });

		// activate our MenuOperationsImpl
		activateMenuOperations();

		fileManager.scan();

	    } finally {
		working = false;
	    }
	}

    }

    private void addAnnotationDependecy() {
	projectOperations.dependencyUpdate(DEPENDENCY_GVNIX_WEB_ANOTATIONS);
    }

    public MenuPageItem addPage(MenuPageItem parent, String name, String label,
	    String messageCode, String destination, String roles, String rooId,
	    boolean hide) {

	if (!(working || isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException("Unavailable command");
	}

	if (!isModelModifiable()) {
	    throw new UnsupportedOperationException(
		    "Unmodifiable menu configuration");
	}

	synchronized (lock) {
	    boolean alreadyWorking = false;
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }
	    MenuPageItem newMenuItem = null;

	    try {

		Document doc = getMenuDocument();

		Element root = doc.getDocumentElement();

		checkForDuplicateDestination(doc, destination);

		checkForDuplicateName(doc, parent, name, null);

		Element parentElement;
		if (parent == null || parent.equals(MenuPageItem.ROOT)) {
		    parent = MenuPageItem.ROOT;
		    parentElement = root;
		} else {

		    String parentXmlPath = helper.tranformToXmlPath(parent
			    .getPath());

		    parentElement = XmlUtils.findFirstElement(parentXmlPath,
			    root);
		}
		Assert.notNull(parentElement,
			"Parent not found in menu data file: '"
				+ parent.getPath() + "'");

		Element newElement = doc
			.createElement(MenuPageItem.XML_ELEMENT_NAME);

		newMenuItem = new MenuPageItem(parent, name, rooId, label,
			messageCode, destination, roles, false);
		newMenuItem.fill(newElement);

		parentElement.appendChild(newElement);

		writeXMLConfigIfNeeded(getConfigXMLFile(), doc);

		if (alreadyWorking) {
		    menuDocument = null;
		}

		helper.addToCache(newMenuItem);

	    } finally {
		if (!alreadyWorking) {
		    working = false;
		}
	    }

	    return newMenuItem;
	}
    }


    private void checkForDuplicateName(Document doc, MenuPageItem parent,
	    String name, MenuPageItem page) {
	List<MenuPageItem> children = helper.getMenuItemChildren(doc, parent);

	for (MenuPageItem child : children) {
	    if (page != null && child.equals(page)) {
		continue;
	    }
	    Assert.isTrue(!child.getName().equals(name),
		    "Target parent already contains a page with name '" + name
			    + "'");
	}

    }

    private void checkForDuplicateDestination(Document doc, String destination) {
	if (!StringUtils.hasText(destination)) {
	    return;
	}

	// Checks isn't needed for external destinations
	if (destination.startsWith("http://")
		|| destination.startsWith("https://")) {
	    return;
	}

	// Checks isn't needed for static contents
	if (destination.startsWith("/static/")) {
	    return;
	}

	String xmlPath = "//" + MenuPageItem.XML_ELEMENT_NAME + "[@"
		+ MenuPageItem.XML_ATTR_DESTINATION + "]";
	List<Element> elements = XmlUtils.findElements(xmlPath, doc
		.getDocumentElement());

	for (Element element : elements) {
	    Assert
		    .isTrue((!helper.isSameDestination(element
			    .getAttribute(MenuPageItem.XML_ATTR_DESTINATION),
			    destination)), "Duplicate destination page: "
			    + destination);
	}

    }

    public void removePage(MenuPageItem page, boolean force, boolean ignoreRooId) {

	if (!(isModelModifiable() && isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException(
		    "Setup not performed or unmodifiable menu configuration");
	}

	if (!ignoreRooId && StringUtils.hasText(page.getRooId())) {
	    hidePage(page);
	    return;
	}

	synchronized (lock) {
	    boolean alreadyWorking = false;
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }

	    try {

		Document doc = getMenuDocument();

		Element root = doc.getDocumentElement();

		String pageXmlPath = helper.tranformToXmlPath(page.getPath());

		Element pageElement = XmlUtils.findFirstElement(pageXmlPath,
			root);

		Assert.notNull(pageElement,
			"Page not found in menu data file: '" + page.getPath()
				+ "'");

		if (pageElement.hasChildNodes() && !force) {
		    throw new IllegalStateException("Page has children.");
		}

		Element parent = (Element) pageElement.getParentNode();
		parent.removeChild(pageElement);

		writeXMLConfigIfNeeded(getConfigXMLFile(), doc);

		helper.removeFromCache(page);

	    } finally {
		if (!alreadyWorking) {
		    working = false;
		}
	    }

	}
    }

    public MenuPageItem updatePage(MenuPageItem page, String name,
	    String label, String messageCode, String destination, String roles,
	    String rooId, boolean hidden) {

	if (!(working || isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException("Unavailable command");
	}

	if (!isModelModifiable()) {
	    throw new UnsupportedOperationException(
		    "Unmodifiable menu configuration");
	}

	synchronized (lock) {
	    boolean alreadyWorking = false;
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }
	    MenuPageItem newMenuItem = null;

	    try {

		Document doc = getMenuDocument();

		Element root = doc.getDocumentElement();

		String pageXmlPath = helper.tranformToXmlPath(page.getPath());

		Element pageElement = XmlUtils.findFirstElement(pageXmlPath,
			root);

		Assert.notNull(pageElement,
			"Page not found in menu data file: '" + page.getPath()
				+ "'");

		newMenuItem = new MenuPageItem(page.getPath(), name, rooId,
			label, messageCode, destination, roles, hidden);

		Element parentElement = (Element) pageElement.getParentNode();

		Element newElement = (Element) pageElement.cloneNode(true);
		// .createElement(MenuPageItem.XML_ELEMENT_NAME);
		newMenuItem.fill(newElement);

		parentElement.replaceChild(newElement, pageElement);

		writeXMLConfigIfNeeded(getConfigXMLFile(), doc);

		helper.addToCache(newMenuItem);

	    } finally {
		if (!alreadyWorking) {
		    working = false;
		}
	    }
	    return newMenuItem;

	}

    }

    public String getFormatedList() {
	if (!(isModelModifiable() && isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException(
		    "Setup not performed or unmodifiable menu configuration");
	}
	return getFormatedList(null, false, false, false, false);
    }

    public void hidePage(MenuPageItem page) {
	changePageVisibility(page, false);
    }

    public void showPage(MenuPageItem page) {
	changePageVisibility(page, true);
    }

    public void changePageVisibility(MenuPageItem page, boolean visible) {
	if (!(isModelModifiable() && isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException(
		    "Setup not performed or unmodifiable menu configuration");
	}
	boolean hidden = !visible;
	if (page.isHidden() == hidden) {
	    return;
	}

	synchronized (lock) {
	    boolean alreadyWorking = false;
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }
	    MenuPageItem newMenuItem = null;

	    try {

		Document doc = getMenuDocument();

		Element root = doc.getDocumentElement();

		String pageXmlPath = helper.tranformToXmlPath(page.getPath());

		Element pageElement = XmlUtils.findFirstElement(pageXmlPath,
			root);

		Assert.notNull(pageElement,
			"Page not found in menu data file: '" + page.getPath()
				+ "'");

		newMenuItem = new MenuPageItem(page.getPath(), page.getName(),
			page.getRooId(), page.getLabel(),
			page.getMessageCode(), page.getDestination(), page
				.getRoles(), hidden);

		Element parentElement = (Element) pageElement.getParentNode();

		Element newElement = (Element) pageElement.cloneNode(true);
		newMenuItem.fill(newElement);

		parentElement.replaceChild(newElement, pageElement);

		helper.addToCache(newMenuItem);

		writeXMLConfigIfNeeded(getConfigXMLFile(), doc);

		helper.addToCache(newMenuItem);

	    } finally {
		if (!alreadyWorking) {
		    working = false;
		}
	    }
	}
    }

    public MenuPageItem moveBefore(MenuPageItem page, MenuPageItem before) {
	if (!(isModelModifiable() && isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException(
		    "Setup not performed or unmodifiable menu configuration");
	}

	synchronized (lock) {
	    boolean alreadyWorking = false;
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }
	    MenuPageItem newMenuItem = null;

	    try {

		Document doc = getMenuDocument();

		MenuPageItem targetParent = getMenuItemParent(before);

		checkForDuplicateName(doc, targetParent, page.getName(), page);

		Element root = doc.getDocumentElement();

		String pageXmlPath = helper.tranformToXmlPath(page.getPath());

		Element pageElement = XmlUtils.findFirstElement(pageXmlPath,
			root);

		Assert.notNull(pageElement,
			"Page not found in menu data file: '" + page.getPath()
				+ "'");

		String pageXmlPathBefore = helper.tranformToXmlPath(before
			.getPath());

		Element beforeElement = XmlUtils.findFirstElement(
			pageXmlPathBefore, root);

		Assert.notNull(beforeElement,
			"Page 'before' not found in menu data file: '"
				+ before.getPath() + "'");

		int lastDel = before.getPath().lastIndexOf(
			MenuPageItem.DELIMITER);
		String beforeParentPath = before.getPath()
			.substring(0, lastDel);

		// New menu Item
		newMenuItem = new MenuPageItem(beforeParentPath, pageElement);

		Element newElement = (Element) pageElement.cloneNode(true);

		// Remove from current parent
		Element parentElement = (Element) pageElement.getParentNode();
		parentElement.removeChild(pageElement);

		// insert before 'before'
		Element intoElement = (Element) beforeElement.getParentNode();
		intoElement.insertBefore(newElement, beforeElement);

		writeXMLConfigIfNeeded(getConfigXMLFile(), doc);

		helper.addToCache(newMenuItem);

	    } finally {
		if (!alreadyWorking) {
		    working = false;
		}
	    }
	    return newMenuItem;
	}
    }

    public MenuPageItem moveInto(MenuPageItem page, MenuPageItem into) {
	if (!(isModelModifiable() && isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException(
		    "Setup not performed or unmodifiable menu configuration");
	}

	synchronized (lock) {
	    boolean alreadyWorking = false;
	    if (!working) {
		working = true;
	    } else {
		alreadyWorking = true;
	    }
	    MenuPageItem newMenuItem = null;

	    try {

		Document doc = getMenuDocument();

		checkForDuplicateName(doc, into, page.getName(), page);

		Element root = doc.getDocumentElement();

		String pageXmlPath = helper.tranformToXmlPath(page.getPath());

		Element pageElement = XmlUtils.findFirstElement(pageXmlPath,
			root);

		Assert.notNull(pageElement,
			"Page not found in menu data file: '" + page.getPath()
				+ "'");

		Element intoElement;
		if (into.equals(MenuPageItem.ROOT)) {
		    intoElement = root;
		} else {

		    String pageXmlPathInto = helper.tranformToXmlPath(into
			    .getPath());

		    intoElement = XmlUtils.findFirstElement(pageXmlPathInto,
			    root);
		}

		Assert.notNull(intoElement,
			"Page 'into' not found in menu data file: '"
				+ into.getPath() + "'");

		// New menu Item
		newMenuItem = new MenuPageItem(into, pageElement);

		// new menu element
		Element newElement = (Element) pageElement.cloneNode(true);

		// Remove from current parent
		Element parentElement = (Element) pageElement.getParentNode();
		parentElement.removeChild(pageElement);

		intoElement.appendChild(newElement);

		writeXMLConfigIfNeeded(getConfigXMLFile(), doc);

		helper.addToCache(newMenuItem);

	    } finally {
		if (!alreadyWorking) {
		    working = false;
		}
	    }
	    return newMenuItem;

	}

    }

    /**
     *
     *
     */
    public String getFormatedInfo(MenuPageItem page) {
	if (!(isModelModifiable() && isCorrectlyInstalled())) {
	    throw new UnsupportedOperationException(
		    "Setup not performed or unmodifiable menu configuration");
	}

	StringBuilder sb = new StringBuilder();
	sb.append(page.getPath() + "\n");
	if (StringUtils.hasText(page.getLabel())) {
	    sb.append(INDENT + "- label: " + page.getLabel() + "\n");
	}
	if (StringUtils.hasText(page.getMessageCode())) {
	    sb
		    .append(INDENT + "- messageCode: " + page.getMessageCode()
			    + "\n");
	}
	if (StringUtils.hasText(page.getDestination())) {
	    sb
		    .append(INDENT + "- Destination: " + page.getDestination()
			    + "\n");
	}
	if (StringUtils.hasText(page.getRoles())) {
	    sb.append(INDENT + "- Roles: " + page.getRoles() + "\n");
	}
	if (StringUtils.hasText(page.getRooId())) {
	    sb.append(INDENT + "- From Roo: " + page.getRooId() + "\n");
	}
	if (page.isHidden()) {
	    sb.append(INDENT + "- Hidden\n");
	}

	List<MenuPageItem> children = helper.getMenuItemChildren(
		getMenuDocument(), page);
	if (children != null && children.size() > 0) {
	    sb.append(INDENT + "- Children\n");
	    for (MenuPageItem child : children) {
		if (StringUtils.hasText(child.getDestination())) {
		    sb.append(INDENT + INDENT + "* " + child.getName() + "  ["
			    + child.getDestination() + "]" + "\n");
		} else {
		    sb.append(INDENT + INDENT + "* " + child.getName() + "\n");
		}

	    }

	}

	return sb.toString();
    }

    public String getFormatedList(MenuPageItem page, boolean label,
	    boolean messageCode, boolean destination, boolean roles) {
	return helper.getFormatedList(getMenuDocument(), page, label,
		messageCode, destination, roles);
    }

    // ************** Commands ******************
    // ******************************************
    // ******************************************

    protected void cleanUpRooFinderMenuItems(JavaSymbolName menuCategoryName,
	    List<String> allowedFinderMenuIds) {
	helper.cleanUpRooFinderMenuItem(getMenuDocument(), menuCategoryName,
		allowedFinderMenuIds);

    }

    protected void cleanUpRooMenuItem(JavaSymbolName menuCategoryName,
	    JavaSymbolName menuItemName, String idPrefix) {
	helper.cleanUpRooMenuItem(getMenuDocument(), menuCategoryName,
		menuItemName, idPrefix);
    }

    public String getConfigXMLFile() {
	MenuLoaderMetadata loader = (MenuLoaderMetadata) metadataService
		.get(MenuLoaderMetadata.getMetadataIdentiferFinal());
	if (loader == null || loader.getConfigXMLFile() == null) {
	    return null;
	}
	return pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, loader
		.getConfigXMLFile());
    }

    public boolean isModelModifiable() {
	return getConfigXMLFile() != null;
    }

    void dataConfigurationChanged() {
	menuDocument = null;
	rooMenuRootPath = null;
	helper.clearCache();
    }

    MenuPageItem getMenuItemParent(MenuPageItem item) {
	String[] path = StringUtils.split(item.getPath(),
		MenuPageItem.DELIMITER);
	if (path.length == 1) {
	    return MenuPageItem.ROOT;
	}
	StringBuilder sb = new StringBuilder(MenuPageItem.ROOT_PATH);
	sb.append(path[0]);
	for (int i = 1; i < path.length - 1; i++) {
	    sb.append(MenuPageItem.DELIMITER);
	    sb.append(path[i]);
	}
	return getPageItem(sb.toString());
    }

    public MenuPageItem getRooMenuRoot() {
	if (rooMenuRootPath == null) {
	    rooMenuRootPath = helper.getRooMenuRoo(getMenuDocument());
	}

	return rooMenuRootPath;
    }

    private void writeXMLConfigIfNeeded(String menuFile, Document doc) {

	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
		byteArrayOutputStream, doc);
	String proposed = byteArrayOutputStream.toString();

	// If mutableFile becomes non-null, it means we need to use it to write
	// out the contents of jspContent to the file
	MutableFile mutableFile = null;
	if (fileManager.exists(menuFile)) {
	    String original = null;

	    try {
		original = FileCopyUtils.copyToString(new FileReader(menuFile));
	    } catch (Exception e) {
		new IllegalStateException("Could not load file: " + menuFile);
	    }

	    if (!proposed.equals(original)) {
		mutableFile = fileManager.updateFile(menuFile);
	    }
	} else {
	    mutableFile = fileManager.createFile(menuFile);
	    Assert.notNull(mutableFile, "Could not create JSP file '"
		    + menuFile + "'");
	}

	try {
	    if (mutableFile != null) {

		FileCopyUtils.copy(proposed, new OutputStreamWriter(mutableFile
			.getOutputStream()));
		fileManager.scan();
	    }
	} catch (IOException ioe) {
	    throw new IllegalStateException("Could not output '"
		    + mutableFile.getCanonicalPath() + "'", ioe);
	}
    }
}
