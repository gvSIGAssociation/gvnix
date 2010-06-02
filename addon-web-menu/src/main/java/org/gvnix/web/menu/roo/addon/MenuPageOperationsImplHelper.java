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

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.roo.addon.web.menu.MenuOperations;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.ObjectUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class of MenuPageOperationsImpl
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class MenuPageOperationsImplHelper {

    private static Logger logger = Logger
	    .getLogger(MenuPageOperationsImplHelper.class.getName());

    private FileManager fileManager;

    private PathResolver pathResolver;

    private MenuPageOperationsImpl operations;

    private Map<String, MenuPageItem> cacheByPath = new HashMap<String, MenuPageItem>();
    private Map<String, MenuPageItem> cacheByRooId = new HashMap<String, MenuPageItem>();

    MenuPageOperationsImplHelper(MenuPageOperationsImpl operations,
	    PathResolver pathResolver, FileManager fileManager) {
	this.operations = operations;
	this.fileManager = fileManager;
	this.pathResolver = pathResolver;
    }

    protected void addOrUpdatePageFromRooMenuItem(
	    JavaSymbolName menuCategoryName, JavaSymbolName menuItemName,
	    String globalMessageCode, String link, String idPrefix) {

	if (idPrefix == null || idPrefix.length() == 0) {
	    idPrefix = MenuOperations.DEFAULT_MENU_ITEM_PREFIX;
	}
	// looks in menu items if there is something that already match with
	// required item

	String rooId = idPrefix
		+ menuCategoryName.getSymbolName().toLowerCase() + "_"
		+ menuItemName.getSymbolName().toLowerCase();

	MenuPageItem item = operations.getMenuItemByRooId(rooId);

	// if any element match create a new one
	if (item == null) {
	    String categoryRooId = "c:"
		    + menuCategoryName.getSymbolName().toLowerCase();
	    MenuPageItem category = operations
		    .getMenuItemByRooId(categoryRooId);
	    // if menuCategoryName is set look at menu for it or create a new
	    // one
	    if (category == null) {

		category = operations.addPage(operations.getRooMenuRoot(),
			menuCategoryName.getSymbolName(), null,
			"menu.category."
				+ menuCategoryName.getSymbolName()
					.toLowerCase() + ".label", null, null,
			categoryRooId, false);
		setProperty(Path.SRC_MAIN_WEBAPP,
			"/WEB-INF/i18n/application.properties",
			"menu.category."
				+ menuCategoryName.getSymbolName()
					.toLowerCase() + ".label",
			menuCategoryName.getReadableSymbolName());
	    }
	    Assert.notNull(category, "Can't create category: "
		    + menuCategoryName);
	    operations.addPage(category, menuItemName.getSymbolName(), null,
		    globalMessageCode, link, null, rooId, false);
	    setProperty(Path.SRC_MAIN_WEBAPP,
		    "/WEB-INF/i18n/application.properties", "menu.item."
			    + menuItemName.getSymbolName().toLowerCase()
			    + ".label", menuItemName.getReadableSymbolName());
	} else {

	    // Update menu Item values
	    operations.updatePage(item, item.getName(), null,
		    globalMessageCode, link, item.getRoles(), rooId, item
			    .isHidden());
	    setProperty(Path.SRC_MAIN_WEBAPP,
		    "/WEB-INF/i18n/application.properties", "menu.item."
			    + menuItemName.getSymbolName().toLowerCase()
			    + ".label", menuItemName.getReadableSymbolName());
	}

    }

    protected MenuPageItem getMenuItemByRooId(Element root, String rooId) {
	MenuPageItem menuItem = getFromCacheByRooId(rooId);
	if (menuItem != null) {
	    return menuItem;
	}
	Element element = XmlUtils.findFirstElement("//"
		+ MenuPageItem.XML_ELEMENT_NAME + "[@"
		+ MenuPageItem.XML_ATTR_ROOID + "='" + rooId + "']", root);
	if (element == null) {
	    return null;
	}

	return getMenuItemByXMLElement(root, element);
    }

    protected MenuPageItem getMenuItemByXMLElement(Element root, Element element) {
	Node current = element.getParentNode();
	Stack<String> pathStack = new Stack<String>();
	while (!current.isEqualNode(root)) {
	    pathStack.push(((Element) current)
		    .getAttribute(MenuPageItem.XML_ATTR_NAME));
	    current = current.getParentNode();
	}
	StringBuilder stb = new StringBuilder(MenuPageItem.ROOT_PATH);
	while (!pathStack.isEmpty()) {
	    stb.append(pathStack.pop());
	    stb.append(MenuPageItem.DELIMITER);
	}
	stb.append(element.getAttribute(MenuPageItem.XML_ATTR_NAME));

	return new MenuPageItem(stb.toString(), element);
    }

    protected void loadMenuFromOldMenu(InputStream rooMenuInput) {
	Document rooMenuDoc;

	try {
	    rooMenuDoc = XmlUtils.getDocumentBuilder().parse(rooMenuInput);

	    Element root = rooMenuDoc.getDocumentElement();

	    Element menu = XmlUtils.findFirstElement("/div/menu", root);

	    Element menuElement;
	    NodeList menuElements = menu.getChildNodes();
	    NodeList menuItems;
	    Element menuItem;

	    JavaSymbolName category;
	    Node tmpNode;

	    for (int i = 0; i < menuElements.getLength(); i++) {
		tmpNode = menuElements.item(i);
		if (tmpNode.getNodeType() != Node.ELEMENT_NODE) {
		    continue;
		}
		menuElement = (Element) menuElements.item(i);
		if (menuElement.getTagName() == "menu:category") {
		    category = new JavaSymbolName(menuElement
			    .getAttribute("name"));
		    menuItems = menuElement.getChildNodes();
		    for (int j = 0; j < menuItems.getLength(); j++) {
			tmpNode = menuItems.item(j);
			if (tmpNode.getNodeType() != Node.ELEMENT_NODE) {
			    continue;
			}
			menuItem = (Element) tmpNode;
			addOrUpdatePageFromRooMenuItem(category,
				new JavaSymbolName(menuItem
					.getAttribute("name")), menuItem
					.getAttribute("messageCode"), menuItem
					.getAttribute("url"),
				getIdPrefixFromRooMenuItem(menuItem
					.getAttribute("id")));
		    }

		} else if (menuElement.getTagName() == "menu:item") {
		    addOrUpdatePageFromRooMenuItem(null, new JavaSymbolName(
			    menuElement.getAttribute("name")), menuElement
			    .getAttribute("messageCode"), menuElement
			    .getAttribute("url"),
			    getIdPrefixFromRooMenuItem(menuElement
				    .getAttribute("id")));
		} else {
		    logger.warning("Found unknow element into menu tag: '"
			    + menuElement.getTagName() + "' [Ignored]");
		}

	    }

	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

    }

    protected String getIdPrefixFromRooMenuItem(String id) {
	int index = id.indexOf(":");
	if (index < 0) {
	    return null;
	} else if (index == 0) {
	    return "";
	} else {
	    return id.substring(0, index + 1);
	}

    }

    /**
     * Install a file template if it doesn't exist
     * <p>
     * This method has been copied from Maven addon, maybe it could be
     * refactored to utility class.
     *
     * @param targetFilename
     * @param projectMetadata
     */
    protected void installIfNeeded(String targetFilename, String targetPackage,
	    ProjectMetadata projectMetadata) {
	// default package
	String packagePath = projectMetadata.getTopLevelPackage()
		.getFullyQualifiedPackageName().replace('.', '/');

	// setting targetPackage change default package
	if (targetPackage != null) {
	    if (targetPackage.startsWith("~")) {
		packagePath = targetPackage.replace(
			"~",
			projectMetadata.getTopLevelPackage()
				.getFullyQualifiedPackageName()).replace('.',
			'/');
	    } else {
		packagePath = targetPackage.replace('.', '/');
	    }
	}

	String destinationFile = projectMetadata.getPathResolver()
		.getIdentifier(Path.SRC_MAIN_JAVA,
			packagePath + "/" + targetFilename);

	if (!fileManager.exists(destinationFile)) {
	    InputStream templateInputStream = TemplateUtils.getTemplate(
		    getClass(), targetFilename + "-template");
	    try {
		// Read template and insert the user's package
		String input = FileCopyUtils
			.copyToString(new InputStreamReader(templateInputStream));
		input = input.replace("__TOP_LEVEL_PACKAGE__", projectMetadata
			.getTopLevelPackage().getFullyQualifiedPackageName());

		// Output the file for the user
		MutableFile mutableFile = fileManager
			.createFile(destinationFile);
		FileCopyUtils.copy(input.getBytes(), mutableFile
			.getOutputStream());
	    } catch (IOException ioe) {
		throw new IllegalStateException("Unable to create '"
			+ targetFilename + "'", ioe);
	    }
	}
    }

    protected void createOrUpdateFileFromResourses(String targetWebappPath,
	    String resourceName, Map<String, String> toReplace,
	    String[] containsStrings) {
	String targetPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		targetWebappPath);

	InputStream resource = getClass().getResourceAsStream(resourceName);

	String sourceContents;
	String targetContents = null;

	// load source
	try {
	    sourceContents = FileCopyUtils.copyToString(new InputStreamReader(
		    resource));
	    // Replace params
	    if (toReplace != null) {
		for (Entry<String, String> entry : toReplace.entrySet()) {
		    sourceContents = StringUtils.replace(sourceContents, entry
			    .getKey(), entry.getValue());
		}
	    }

	} catch (IOException e) {
	    throw new IllegalStateException("Unable to load template '"
		    + resourceName + "'", e);
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
		targetContents = FileCopyUtils.copyToString(reader);
	    } catch (Exception e) {
		throw new IllegalStateException(
			"Error reading current menu.jspx: " + targetPath, e);
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
	MutableFile target = null;
	if (targetContents == null) {
	    target = fileManager.createFile(targetPath);
	} else {
	    // decide if need to replace target

	    if (ObjectUtils.isEmpty(containsStrings)) {
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

	    FileCopyUtils.copy(sourceContents, new OutputStreamWriter(target
		    .getOutputStream()));
	} catch (IOException e) {
	    throw new IllegalStateException("Unable to create/update '"
		    + targetPath + "'", e);
	}

    }

    protected void cleanUpRooFinderMenuItem(Document document,
	    JavaSymbolName menuCategoryName, List<String> allowedFinderMenuIds) {

	Element root = document.getDocumentElement();

	List<Element> elements = XmlUtils
		.findElements("//" + MenuPageItem.XML_ELEMENT_NAME
			+ "[starts-with(@" + MenuPageItem.XML_ATTR_ROOID
			+ ", '" + MenuOperations.FINDER_MENU_ITEM_PREFIX
			+ menuCategoryName.getSymbolName().toLowerCase()
			+ "_')]", root);
	if (elements == null || elements.size() < 1) {
	    return;
	}
	for (Element element : elements) {
	    if (!allowedFinderMenuIds.contains(element
		    .getAttribute(MenuPageItem.XML_ATTR_ROOID))) {
		operations.removePage(getMenuItemByXMLElement(root, element),
			true, true);
	    }
	}
    }

    protected void cleanUpRooMenuItem(Document document,
	    JavaSymbolName menuCategoryName, JavaSymbolName menuItemName,
	    String idPrefix) {

	if (idPrefix == null || idPrefix.length() == 0) {
	    idPrefix = MenuOperations.DEFAULT_MENU_ITEM_PREFIX;
	}

	String rooId = idPrefix
		+ menuCategoryName.getSymbolName().toLowerCase() + "_"
		+ menuItemName.getSymbolName().toLowerCase();
	MenuPageItem menuItem = getMenuItemByRooId(document
		.getDocumentElement(), rooId);

	if (menuItem != null) {
	    operations.removePage(menuItem, true, true);
	}
    }

    protected Document getDocument(String pathToFile) {
	if (!fileManager.exists(pathToFile)) {
	    return null;
	}

	InputStream rooMenuInput = fileManager.getInputStream(pathToFile);

	Document menuDoc = null;

	try {
	    menuDoc = XmlUtils.getDocumentBuilder().parse(rooMenuInput);

	} catch (Exception e) {
	    throw new IllegalStateException(
		    "Error loading menu configuration: " + pathToFile, e);
	} finally {
	    if (rooMenuInput != null) {
		try {
		    rooMenuInput.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
	return menuDoc;
    }

    protected MenuPageItem getPageItem(Document doc, String path) {
	if (doc == null) {
	    return null;
	}
	Assert.notNull(path.startsWith(MenuPageItem.DELIMITER),
		"Invalid path: null");
	Assert.isTrue(path.startsWith(MenuPageItem.DELIMITER),
		"Invalid path: '" + path + "'");
	path = path.trim();
	if (path.length() == 1) {
	    return MenuPageItem.ROOT;
	}

	while (path.endsWith(MenuPageItem.DELIMITER)) {
	    path = path.substring(0, path.length() - 1);
	}

	MenuPageItem current = getFromCacheByPath(path);
	if (current != null) {
	    return current;
	}

	String xPath = tranformToXmlPath(path);

	Element element = XmlUtils.findFirstElement(xPath, doc
		.getDocumentElement());

	if (element == null) {
	    return null;
	}

	current = new MenuPageItem(path, element);
	addToCache(current);
	return current;
    }

    // **************************************************
    // **************************************************
    // ******************** CACHE ***********************

    protected void addToCache(MenuPageItem item) {
	cacheByPath.put(item.getPath(), item);
	if (StringUtils.hasText(item.getRooId())) {
	    cacheByRooId.put(item.getRooId(), item);
	}
    }

    protected void removeFromCache(MenuPageItem item) {
	cacheByPath.remove(item.getPath());
	if (StringUtils.hasText(item.getRooId())) {
	    cacheByRooId.remove(item.getRooId());
	}
    }

    protected void clearCache() {
	cacheByPath.clear();
	cacheByRooId.clear();
    }

    private MenuPageItem getFromCacheByPath(String path) {
	return cacheByPath.get(path);
    }

    private MenuPageItem getFromCacheByRooId(String rooId) {
	return cacheByRooId.get(rooId);
    }

    // ******************** CACHE ***********************
    // **************************************************
    // **************************************************

    protected String tranformToXmlPath(String path) {
	Assert.isTrue(path.startsWith(MenuPageItem.DELIMITER), "Invalid path: "
		+ path);

	String pathToProcess = path.trim().substring(1, path.length());

	String[] pathArray = StringUtils.tokenizeToStringArray(pathToProcess,
		MenuPageItem.DELIMITER);

	StringBuilder xmlPath = new StringBuilder("/"
		+ MenuPageItem.XML_ROOT_ELEMENT_NAME);
	final String baseStr = "/" + MenuPageItem.XML_ELEMENT_NAME + "[@"
		+ MenuPageItem.XML_ATTR_NAME + "='";
	for (String name : pathArray) {
	    xmlPath.append(baseStr);
	    xmlPath.append(name);
	    xmlPath.append("'");
	    xmlPath.append("]");
	}
	return xmlPath.toString();
    }

    List<MenuPageItem> getMenuItemChildren(Document doc, MenuPageItem page) {
	List<MenuPageItem> result = new ArrayList<MenuPageItem>();

	Element root = doc.getDocumentElement();

	Element pageElement;
	if (page == null || page.equals(MenuPageItem.ROOT)) {
	    pageElement = root;
	} else {
	    String pageXmlPath = tranformToXmlPath(page.getPath());

	    pageElement = XmlUtils.findFirstElement(pageXmlPath, root);
	}
	if (pageElement.hasChildNodes()) {

	    Element child;
	    NodeList children = pageElement.getChildNodes();
	    Node tmpNode;
	    MenuPageItem current;

	    for (int i = 0; i < children.getLength(); i++) {
		// Next Element and create MenuItem
		tmpNode = children.item(i);
		if (tmpNode.getNodeType() != Node.ELEMENT_NODE
			|| !MenuPageItem.XML_ELEMENT_NAME.equals(tmpNode
				.getNodeName())) {
		    continue;
		}
		child = (Element) tmpNode;
		current = new MenuPageItem(page, child);
		addToCache(current);
		result.add(current);
	    }
	}

	return result;
    }

    protected String getFormatedList(Document doc, MenuPageItem page,
	    boolean label, boolean messageCode, boolean destination,
	    boolean roles) {

	if (doc == null) {
	    return null;
	}

	// This class is used by tree iterator to add a detail's line
	// every single element will be a information text line
	class Detail {
	    String path;
	    String label;
	    String destination;
	    String roles;
	    String messageCode;

	    public Detail(String path, String label, String messageCode,
		    String destination, String roles) {
		super();
		this.path = path;
		this.label = label;
		this.messageCode = messageCode;
		this.destination = destination;
		this.roles = roles;
	    }
	}

	// Details list.
	List<Detail> detailList = new ArrayList<Detail>();

	// variables to store max length of strings for make a good indentation
	int maxPath = 0;
	int maxLabel = 0;
	int maxMessageCode = 0;
	int maxDestination = 0;
	int maxRoles = 0;

	// Stack element
	class StackItem {
	    MenuPageItem item;
	    int index;
	    NodeList nodeList;

	    public StackItem(MenuPageItem item, Element element, int index,
		    NodeList nodeList) {
		this.item = item;
		this.index = index;
		this.nodeList = nodeList;
	    }

	    public String getPathOfChild(Element element) {
		if (item == null) {
		    return MenuPageItem.ROOT_PATH
			    + element.getAttribute(MenuPageItem.XML_ATTR_NAME);
		}
		return item.getPath() + MenuPageItem.DELIMITER
			+ element.getAttribute(MenuPageItem.XML_ATTR_NAME);
	    }
	}

	Element root = doc.getDocumentElement();

	// Stack to store status before download a tree level
	Stack<StackItem> stack = new Stack<StackItem>();

	if (page == null || page.equals(MenuPageItem.ROOT)) {
	    // Push root
	    stack.add(new StackItem(null, null, 0, root.getChildNodes()));
	} else {
	    String xPathExp = tranformToXmlPath(page.getPath());
	    Element element = (Element) XmlUtils.findFirstElement(xPathExp,
		    root);
	    Assert.notNull(element, "Element not found: " + page.getPath());
	    if (element.hasChildNodes()) {
		stack.add(new StackItem(page, element, 0, element
			.getChildNodes()));
	    } else {
		// Create a new detail information
		detailList.add(new Detail(page.getPath(), page.getLabel(), page
			.getMessageCode(), page.getDestination(), page
			.getRoles()));

	    }
	}
	StackItem current;
	Element currentElement;
	Node tmpNode;
	MenuPageItem menuItem;
	Detail curDetail;

	while (!stack.isEmpty()) {
	    // Pop element to evaluate
	    current = stack.pop();
	    for (int i = current.index; i < current.nodeList.getLength(); i++) {
		// Next Element and create MenuItem
		tmpNode = current.nodeList.item(i);
		if (tmpNode.getNodeType() != Node.ELEMENT_NODE
			|| !MenuPageItem.XML_ELEMENT_NAME.equals(tmpNode
				.getNodeName())) {
		    continue;
		}

		currentElement = (Element) tmpNode;

		menuItem = new MenuPageItem(current
			.getPathOfChild(currentElement), currentElement);
		addToCache(menuItem);

		// Create a new detail information
		curDetail = new Detail(menuItem.getPath(), menuItem.getLabel(),
			menuItem.getMessageCode(), menuItem.getDestination(),
			menuItem.getRoles());

		// Compute String max length
		if (curDetail.path.length() > maxPath) {
		    maxPath = curDetail.path.length();
		}

		if (label && curDetail.label != null
			&& curDetail.label.length() > maxLabel) {
		    maxLabel = curDetail.label.length();
		}

		if (messageCode && curDetail.messageCode != null
			&& curDetail.messageCode.length() > maxMessageCode) {
		    maxMessageCode = curDetail.messageCode.length();
		}

		if (destination && curDetail.destination != null
			&& curDetail.destination.length() > maxDestination) {
		    maxDestination = curDetail.destination.length();
		}

		if (roles && curDetail.roles != null
			&& curDetail.roles.length() > maxRoles) {
		    maxRoles = curDetail.roles.length();
		}

		// Adds detail to list
		detailList.add(curDetail);

		// If element has children push current Stack Item
		// and push new Stack Item to add these children
		if (currentElement.hasChildNodes()) {
		    // Set index to next
		    current.index = i + 1;
		    // Push current
		    stack.push(current);
		    // Push new element
		    stack.push(new StackItem(menuItem, currentElement, 0,
			    currentElement.getChildNodes()));
		    break;
		}
	    }
	}

	StringBuilder sb = new StringBuilder();

	// For every details
	for (Detail detail : detailList) {
	    // Append information i
	    appendToStrBuilderFixedSize(sb, detail.path, maxPath);
	    if (label) {
		sb.append("  ");
		appendToStrBuilderFixedSize(sb, detail.label, maxLabel);
	    }

	    if (messageCode) {
		sb.append("  ");
		appendToStrBuilderFixedSize(sb, detail.messageCode,
			maxMessageCode);
	    }

	    if (destination) {
		sb.append("  ");
		appendToStrBuilderFixedSize(sb, detail.destination,
			maxDestination);
	    }

	    if (roles) {
		sb.append("  ");
		appendToStrBuilderFixedSize(sb, detail.roles, maxRoles);
	    }
	    sb.append("\n");
	}

	return sb.toString();

    }

    private void appendToStrBuilderFixedSize(StringBuilder sb, String data,
	    int size) {
	// append data
	sb.append(data);

	// Compute remainder spaces to insert
	int remainder = size - data.length();
	if (remainder <= 0) {
	    return;
	}

	// Append remainder spaces
	for (int i = 0; i < remainder; i++) {
	    sb.append(' ');
	}
    }

    boolean isSameDestination(String destination1, String destination2) {
	int questionCharIndes1 = destination1.indexOf("?");
	int questionCharIndes2 = destination2.indexOf("?");
	if (questionCharIndes1 != questionCharIndes2) {
	    // Checks for useless question char difference
	    if (questionCharIndes1 < 0) {
		if (!destination2.endsWith("?")) {
		    return false;
		}
	    } else if (questionCharIndes2 < 0) {
		if (!destination1.endsWith("?")) {
		    return false;
		}
	    }
	}

	// Url's files have the same size.
	String file1, file2;
	String query1, query2;
	if (questionCharIndes1 < 0) {
	    file1 = destination1;
	    query1 = null;
	} else {
	    file1 = destination1.substring(0, questionCharIndes1);
	    query1 = destination1.substring(questionCharIndes1 + 1);
	}
	if (questionCharIndes2 < 0) {
	    file2 = destination2;
	    query2 = null;
	} else {
	    file2 = destination2.substring(0, questionCharIndes2);
	    query2 = destination2.substring(questionCharIndes2 + 1);
	}

	if (file1.length() != file2.length()) {
	    return false;
	}

	if (!file1.equalsIgnoreCase(file2)) {
	    // Url's files are different
	    return false;
	}

	// Compare parameters without values
	if (query1 == null && query2 != null) {
	    return false;
	} else if (query2 == null && query1 != null) {
	    return false;
	}

	String[] paramTokens1 = StringUtils.tokenizeToStringArray(query1, "&");
	String[] paramTokens2 = StringUtils.tokenizeToStringArray(query2, "&");

	if (paramTokens1.length != paramTokens2.length) {
	    return false;
	}

	// Add params names to a sorted set
	TreeSet<String> paramNames1 = new TreeSet<String>();
	Map<String, String> paramValues1 = new HashMap<String, String>();
	TreeSet<String> paramNames2 = new TreeSet<String>();
	Map<String, String> paramValues2 = new HashMap<String, String>();

	String paramName;
	String paramValue;
	int equalCharIndex;
	for (String paramToken : paramTokens1) {
	    equalCharIndex = paramToken.indexOf("=");
	    if (equalCharIndex < 0) {
		paramName = paramToken;
		paramValue = null;
	    } else {
		paramName = paramToken.substring(0, equalCharIndex);
		paramValue = paramToken.substring(equalCharIndex + 1,
			paramToken.length());
		;
	    }
	    // TODO case insensitive compare: check servlet specification
	    paramName = paramName.toLowerCase();
	    paramNames1.add(paramName);
	    paramValues1.put(paramName, paramValue);
	}

	for (String paramToken : paramTokens2) {
	    equalCharIndex = paramToken.indexOf("=");
	    if (equalCharIndex < 0) {
		paramName = paramToken;
		paramValue = null;
	    } else {
		paramName = paramToken.substring(0, equalCharIndex);
		paramValue = paramToken.substring(equalCharIndex + 1,
			paramToken.length());
		;
	    }
	    // TODO case insensitive compare: check servlet specification
	    paramName = paramName.toLowerCase();
	    paramNames2.add(paramName);
	    paramValues2.put(paramName, paramValue);
	}

	// check for duplicated parameters
	if (paramNames1.size() != paramNames2.size()) {
	    return false;
	}

	Iterator<String> iterParamNames1 = paramNames1.iterator();
	Iterator<String> iterParamNames2 = paramNames2.iterator();

	String name1;
	String name2;
	String value1;
	String value2;
	while (iterParamNames1.hasNext()) {
	    name1 = iterParamNames1.next();
	    name2 = iterParamNames2.next();
	    if (!name1.equals(name2)) {
		return false;
	    }
	    value1 = paramValues1.get(name1);
	    value2 = paramValues2.get(name2);
	    if (!ObjectUtils.nullSafeEquals(value1, value2)) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Changes the specified property, throwing an exception if the file does
     * not exist.
     *
     * @param propertyFilePath
     *            the location of the property file (required)
     * @param propertyFilename
     *            the name of the property file within the specified path
     *            (required)
     * @param key
     *            the property key to update (required)
     * @param value
     *            the property value to set into the property key (required)
     */
    private void setProperty(Path propertyFilePath, String propertyFilename,
	    String key, String value) {
	Assert.notNull(propertyFilePath, "Property file path required");
	Assert.hasText(propertyFilename, "Property filename required");
	Assert.hasText(key, "Key required");
	Assert.hasText(value, "Value required");

	String filePath = pathResolver.getIdentifier(propertyFilePath,
		propertyFilename);

	Properties readProps = new Properties();
	try {
	    if (fileManager.exists(filePath)) {

		readProps.load(fileManager.getInputStream(filePath));
	    } else {
		throw new IllegalStateException("Properties file not found");
	    }
	} catch (IOException ioe) {
	    throw new IllegalStateException(ioe);
	}
	if (null == readProps.getProperty(key)) {
	    MutableFile mutableFile = fileManager.updateFile(filePath);
	    Properties props = new Properties() {
		// override the keys() method to order the keys alphabetically
		@Override
		@SuppressWarnings("unchecked")
		public synchronized Enumeration keys() {
		    final Object[] keys = keySet().toArray();
		    Arrays.sort(keys);
		    return new Enumeration() {
			int i = 0;

			public boolean hasMoreElements() {
			    return i < keys.length;
			}

			public Object nextElement() {
			    return keys[i++];
			}
		    };
		}
	    };
	    try {
		props.load(mutableFile.getInputStream());
		props.setProperty(key, value);
		props.store(mutableFile.getOutputStream(), "Updated "
			+ new Date());
	    } catch (IOException ioe) {
		throw new IllegalStateException(ioe);
	    }
	}
    }

    public void cleanUpXMLSpaces(Document doc) {

	Element root = doc.getDocumentElement();

	class StackItem {
	    Node current;
	    NodeList children;
	    int index;

	    public StackItem(Node current, NodeList children, int index) {
		super();
		this.current = current;
		this.children = children;
		this.index = index;
	    }
	}

	Stack<StackItem> stack = new Stack<StackItem>();
	stack.push(new StackItem(root, root.getChildNodes(), 0));
	StackItem current;
	Node curNode;
	String nodeValue;
	String[] spaces;
	StringBuilder sb;

	while (!stack.isEmpty()) {
	    current = stack.pop();
	    for (int i = current.index; i < current.children.getLength(); i++) {
		curNode = current.children.item(i);
		nodeValue = curNode.getNodeValue();
		if (curNode != null
			&& curNode.getNodeType() == Node.TEXT_NODE
			&& !StringUtils.hasText(nodeValue.replace("\n", "")
				.trim())) {
		    spaces = nodeValue.split("\n");
		    if (spaces.length > 3) {
			sb = new StringBuilder();
			sb.append("\n");
			sb.append(spaces[spaces.length - 2]);
			sb.append("\n");
			sb.append(spaces[spaces.length - 1]);
			curNode.setTextContent(sb.toString());
			continue;
		    }
		}

		if (curNode.hasChildNodes()) {
		    current.index++;
		    stack.push(current);
		    stack.push(new StackItem(curNode, curNode.getChildNodes(),
			    0));
		    break;
		}
	    }
	}
    }

    public MenuPageItem getRooMenuRoo(Document menuDocument) {
	Element root = menuDocument.getDocumentElement();
	MenuPageItem item = MenuPageItem.ROOT;
	if (root.hasAttribute(MenuPageItem.XML_ROOT_ATTR_ROO_MENU_BASE)) {
	    String basePath = root
		    .getAttribute(MenuPageItem.XML_ROOT_ATTR_ROO_MENU_BASE);
	    try {
		item = operations.getPageItem(basePath);
		if (item == null) {
		    item = MenuPageItem.ROOT;
		}
	    } catch (Exception e) {
		// Nothing to do
	    }
	}
	return item;
    }
}
