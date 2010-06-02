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

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;
import org.springframework.roo.support.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Roo Shell converter for gvNIX Menu path
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class MenuPagePathConverter implements Converter {

    @Reference
    private MenuPageOperations operations;

    public Object convertFromText(String value, Class<?> requiredType,
	    String optionContext) {
	return operations.getPageItem(value);
    }

    public boolean getAllPossibleValues(List<String> completions,
	    Class<?> requiredType, String existingData, String optionContext,
	    MethodTarget target) {
	List<String> paths = getAllPossibleValues(requiredType, existingData);
	if (paths == null || paths.size() < 1) {
	    return false;
	}
	completions.addAll(paths);
	return false;
    }

    private MenuPageOperationsImpl getOperationsImpl() {
	if (operations == null) {
	    return null;
	}
	if (!(operations instanceof MenuPageOperationsImpl)) {
	    return null;

	}
	return (MenuPageOperationsImpl) operations;
    }

    private List<String> getAllPossibleValues(Class<?> requiredType,
	    String existingData) {
	MenuPageOperationsImpl operationsImpl = getOperationsImpl();
	if (operationsImpl == null) {
	    return null;
	}
	List<String> result = new ArrayList<String>();

	if (!StringUtils.hasText(existingData)) {
	    result.add(MenuPageItem.ROOT.getPath());
	    return result;
	}
	String tmp = existingData;
	if (!tmp.startsWith(MenuPageItem.ROOT_PATH)) {
	    tmp = MenuPageItem.ROOT_PATH.concat(tmp);
	}
	while (tmp.endsWith(MenuPageItem.DELIMITER)
		&& tmp.length() > MenuPageItem.ROOT_PATH.length()) {
	    tmp = tmp.substring(0, tmp.length() - 1);
	}
	String filter = null;

	MenuPageItem item;
	try {
	    item = operations.getPageItem(tmp);
	} catch (Exception e) {
	    item = null;
	}
	if (item == null) {
	    int lastDelimiter = tmp.lastIndexOf(MenuPageItem.DELIMITER);
	    if (lastDelimiter < tmp.length() - 1) {
		filter = tmp.substring(lastDelimiter + 1);
	    }
	    tmp = tmp.substring(0, lastDelimiter);
	    if (tmp.length() == 0) {
		item = MenuPageItem.ROOT;
	    } else {
		item = operations.getPageItem(tmp);
	    }
	    if (item == null) {
		return null;
	    }
	}

	// Search for possible matching brothers
	if (item != MenuPageItem.ROOT) {
	    MenuPageItem parent = operationsImpl.getMenuItemParent(item);
	    List<MenuPageItem> brothers = operationsImpl
		    .getMenuItemChildren(parent);
	    for (MenuPageItem brother : brothers) {
		if (brother.getName().startsWith(item.getName())
			&& !brother.getName().equals(item.getName())) {
		    result.add(brother.getPath());
		}
	    }
	}

	// Add matching children
	List<MenuPageItem> children = operationsImpl.getMenuItemChildren(item);
	if (children == null || children.isEmpty()) {
	    result.add(item.getPath());
	    return result;
	}

	for (MenuPageItem child : children) {
	    if (filter == null || child.getName().startsWith(filter)) {
		result.add(child.getPath());
	    }
	}
	return result;

    }

    private List<String> getAllPossibleValuesOld(Class<?> requiredType,
	    String existingData) {

	Element current = getMenuDataRoot();
	if (current == null) {
	    return null;
	}
	List<String> result = new ArrayList<String>();
	existingData = existingData.trim();
	// Empty string or first char

	if (!StringUtils.hasText(existingData)) {
	    result.add(MenuPageItem.ROOT.getPath());
	    return result;
	}
	if (existingData.startsWith(MenuPageItem.DELIMITER)) {
	    existingData = existingData.substring(1);
	}
	String[] tokens = StringUtils.delimitedListToStringArray(existingData,
		MenuPageItem.DELIMITER);

	String token = null;
	NodeList children;
	Element next = null;
	Element tmp = null;
	StringBuilder currentPath = new StringBuilder(MenuPageItem.ROOT
		.getPath());

	// down by tree to before last token
	// or while token exist
	int i = 0;
	while (i < tokens.length) {
	    token = tokens[i];
	    children = current.getChildNodes();
	    for (int j = 0; j < children.getLength(); j++) {
		tmp = (Element) children.item(j);
		if (token.equals(tmp.getAttribute(MenuPageItem.XML_ATTR_NAME))) {
		    // match element, download level
		    next = tmp;
		    break;
		}
	    }

	    // no match found, don't download level and exit
	    if (next == null) {
		break;
	    }
	    if (!endWith(currentPath, MenuPageItem.DELIMITER)) {
		currentPath.append(MenuPageItem.DELIMITER);
	    }
	    current = next;
	    i++;
	}

	// Path not found
	if (i > tokens.length - 1) {
	    return null;
	}

	// Exact match
	if (i == tokens.length) {

	    // final Node
	    if (!current.hasChildNodes()) {
		result.add(currentPath.toString());
		return result;
	    }

	    // Add children to result
	    if (!endWith(currentPath, MenuPageItem.DELIMITER)) {
		currentPath.append(MenuPageItem.DELIMITER);
	    }
	    children = current.getChildNodes();
	    String tmpCurrentPath = currentPath.toString();
	    for (int j = 0; j < children.getLength(); j++) {
		tmp = (Element) children.item(j);
		result.add(tmpCurrentPath.concat(tmp
			.getAttribute(MenuPageItem.XML_ATTR_NAME)));
	    }
	    return result;
	}

	// Search for nodes that starts with current token
	if (!endWith(currentPath, MenuPageItem.DELIMITER)) {
	    currentPath.append(MenuPageItem.DELIMITER);
	}
	children = current.getChildNodes();
	String tmpCurrentPath = currentPath.toString();
	for (int j = 0; j < children.getLength(); j++) {
	    tmp = (Element) children.item(j);
	    if (tmp.getAttribute(MenuPageItem.XML_ATTR_NAME).startsWith(token))
		result.add(tmpCurrentPath.concat(tmp
			.getAttribute(MenuPageItem.XML_ATTR_NAME)));
	}
	return result;

    }

    private boolean endWith(CharSequence seq, String ending) {
	return seq.subSequence(seq.length() - ending.length(), seq.length())
		.equals(ending);
    }

    private Element getMenuDataRoot() {
	if (operations instanceof MenuPageOperationsImpl) {
	    Document document = ((MenuPageOperationsImpl) operations)
		    .getMenuDocument();
	    if (document == null) {
		return null;
	    }
	    return document.getDocumentElement();
	}
	return null;
    }

    public boolean supports(Class<?> requiredType, String optionContext) {
	return MenuPageItem.class.isAssignableFrom(requiredType);
    }

}
