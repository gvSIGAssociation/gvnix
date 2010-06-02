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

import org.springframework.roo.support.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Inmutable representation of a gvNIX Menu item
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
public class MenuPageItem {

    public static final String DELIMITER = "/";
    public static final String ROOT_PATH = DELIMITER;
    public static final MenuPageItem ROOT = new MenuPageItem();

    static final String XML_ROOT_ELEMENT_NAME = "gvNIX_menu";
    static final String XML_ELEMENT_NAME = "menuItem";

    static final String XML_ATTR_NAME = "name";
    static final String XML_ATTR_ROOID = "rooId";
    static final String XML_ATTR_LABEL = "label";
    static final String XML_ATTR_MESSAGECODE = "messageCode";
    static final String XML_ATTR_DESTINATION = "destination";
    static final String XML_ATTR_ROLES = "roles";
    static final String XML_ATTR_HIDDEN = "hidden";
    static final String XML_ROOT_ATTR_ROO_MENU_BASE = "rooMenuBasePath";

    private String path;
    private String name;
    private String rooId;
    private String label;
    private String messageCode;
    private String destination;
    private String roles;
    private boolean hidden;

    /**
     * Root constructor
     */
    private MenuPageItem() {
	path = DELIMITER;
    }

    /**
     * Constructor form XML
     *
     * @param path
     *            of element
     * @param pageItem
     */
    public MenuPageItem(String path, Element pageItem) {
	this.path = path;
	this.name = pageItem.getAttribute(XML_ATTR_NAME);
	this.label = pageItem.getAttribute(XML_ATTR_LABEL);
	this.messageCode = pageItem.getAttribute(XML_ATTR_MESSAGECODE);
	this.destination = pageItem.getAttribute(XML_ATTR_DESTINATION);
	this.roles = pageItem.getAttribute(XML_ATTR_ROLES);
	this.hidden = getStringToBoolean(
		pageItem.getAttribute(XML_ATTR_HIDDEN), false);
	this.rooId = pageItem.getAttribute(XML_ATTR_ROOID);

    }

    public void fill(Element pageItem) {
	pageItem.setAttribute(XML_ATTR_NAME, name);
	if (StringUtils.hasText(label)) {
	    pageItem.setAttribute(XML_ATTR_LABEL, label);
	} else if (pageItem.hasAttribute(XML_ATTR_LABEL)){
	    pageItem.removeAttribute(XML_ATTR_LABEL);
	}

	if (StringUtils.hasText(messageCode)) {
	    pageItem.setAttribute(XML_ATTR_MESSAGECODE, messageCode);
	} else if (pageItem.hasAttribute(XML_ATTR_MESSAGECODE)){
	    pageItem.removeAttribute(XML_ATTR_MESSAGECODE);
	}

	if (StringUtils.hasText(destination)) {
	    pageItem.setAttribute(XML_ATTR_DESTINATION, destination);
	} else if (pageItem.hasAttribute(XML_ATTR_DESTINATION)){
	    pageItem.removeAttribute(XML_ATTR_DESTINATION);
	}
	if (StringUtils.hasText(roles)) {
	    pageItem.setAttribute(XML_ATTR_ROLES, roles);
	} else if (pageItem.hasAttribute(XML_ATTR_ROLES)){
	    pageItem.removeAttribute(XML_ATTR_ROLES);
	}
	if (StringUtils.hasText(rooId)) {
	    pageItem.setAttribute(XML_ATTR_ROOID, rooId);
	} else if (pageItem.hasAttribute(XML_ATTR_ROOID)){
	    pageItem.removeAttribute(XML_ATTR_ROOID);
	}
	if (hidden) {
	    pageItem.setAttribute(XML_ATTR_HIDDEN, Boolean.TRUE.toString());
	} else {
	    if (pageItem.hasAttribute(XML_ATTR_HIDDEN)) {
		pageItem.removeAttribute(XML_ATTR_HIDDEN);
	    }
	}
    }

    public MenuPageItem(String path, String name, String rooId, String label,
	    String messageCode, String destination, String roles, boolean hidden) {
	this.path = path;
	this.name = name;
	this.rooId = rooId;
	this.label = label;
	this.messageCode = messageCode;
	this.destination = destination;
	this.roles = roles;
	this.hidden = hidden;
    }

    public MenuPageItem(MenuPageItem parent, String name, String rooId,
	    String label, String messageCode, String destination, String roles,
	    boolean hidden) {
	if (parent == ROOT) {
	    this.path = DELIMITER + name;
	} else {
	    this.path = parent.getPath() + DELIMITER + name;
	}

	this.name = name;
	this.rooId = rooId;
	this.label = label;
	this.messageCode = messageCode;
	this.destination = destination;
	this.roles = roles;
	this.hidden = hidden;
    }

    public MenuPageItem(MenuPageItem parent, Element pageItem) {
	this.name = pageItem.getAttribute(XML_ATTR_NAME);
	if (parent == ROOT) {
	    this.path = DELIMITER + name;
	} else {
	    this.path = parent.getPath() + DELIMITER + name;
	}


	this.label = pageItem.getAttribute(XML_ATTR_LABEL);
	this.destination = pageItem.getAttribute(XML_ATTR_DESTINATION);
	this.roles = pageItem.getAttribute(XML_ATTR_ROLES);
	this.hidden = getStringToBoolean(
		pageItem.getAttribute(XML_ATTR_HIDDEN), false);
	this.rooId = pageItem.getAttribute(XML_ATTR_ROOID);

    }

    private boolean getStringToBoolean(String attribute, boolean defaultValue) {
	if (attribute == null) {
	    return defaultValue;
	}
	return Boolean.parseBoolean(attribute);
    }

    public String getPath() {
	return path;
    }

    public String getName() {
	return name;
    }

    public String getLabel() {
	return label;
    }

    public String getMessageCode() {
	return messageCode;
    }

    public String getRooId() {
	return rooId;
    }

    public boolean isHidden() {
	return hidden;
    }

    public String getDestination() {
	return destination;
    }

    public String getRoles() {
	return roles;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((destination == null) ? 0 : destination.hashCode());
	result = prime * result + (hidden ? 1231 : 1237);
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((path == null) ? 0 : path.hashCode());
	result = prime * result + ((roles == null) ? 0 : roles.hashCode());
	result = prime * result + ((rooId == null) ? 0 : rooId.hashCode());
	result = prime * result + ((label == null) ? 0 : label.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	MenuPageItem other = (MenuPageItem) obj;
	if (destination == null) {
	    if (other.destination != null)
		return false;
	} else if (!destination.equals(other.destination))
	    return false;
	if (hidden != other.hidden)
	    return false;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (path == null) {
	    if (other.path != null)
		return false;
	} else if (!path.equals(other.path))
	    return false;
	if (roles == null) {
	    if (other.roles != null)
		return false;
	} else if (!roles.equals(other.roles))
	    return false;
	if (rooId == null) {
	    if (other.rooId != null)
		return false;
	} else if (!rooId.equals(other.rooId))
	    return false;
	if (label == null) {
	    if (other.label != null)
		return false;
	} else if (!label.equals(other.label))
	    return false;
	return true;
    }

}
