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
package org.gvnix.web.menu.roo.addon;

import org.gvnix.web.menu.roo.addon.util.StringUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.w3c.dom.Element;

/**
 * Inmutable representation of a gvNIX Menu Entry.
 * <p>
 * Provides conversion from page ID to XML Element used for shell autocompletion
 * purpouses.
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Enrique Ruiz ( eruiz at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class MenuEntry {

    static final String XML_ROOT_ELEMENT_NAME = "gvnix-menu";
    static final String XML_MENU_ITEM_NAME = "menu-item";
    static final String XML_ATTR_ID = "id";
    static final String XML_ATTR_LABEL_CODE = "labelCode";
    static final String XML_ATTR_MESSAGE_CODE = "messageCode";
    static final String XML_ATTR_URL = "url";
    static final String XML_ATTR_ROLES = "roles";
    static final String XML_ATTR_HIDDEN = "hidden";

    private String id;
    private String labelCode;
    private String messageCode;
    private String url;
    private String roles;
    private boolean hidden;

    /**
     * Menu entry constructor
     * 
     * @param id Menu entry id. It cannot have white spaces, as utility white
     *            spaces will be changed to underscores
     */
    public MenuEntry(String id) {
        this.id = StringUtils.underscoreAllWhitespace(id);
    }

    /**
     * Menu entry constructor from XML
     * 
     * @param pageItem
     */
    public MenuEntry(Element pageItem) {
        this.id = pageItem.getAttribute(XML_ATTR_ID);
        this.labelCode = pageItem.getAttribute(XML_ATTR_LABEL_CODE);
        this.messageCode = pageItem.getAttribute(XML_ATTR_MESSAGE_CODE);
        this.url = pageItem.getAttribute(XML_ATTR_URL);
        this.roles = pageItem.getAttribute(XML_ATTR_ROLES);
        this.hidden = parseBoolean(pageItem.getAttribute(XML_ATTR_HIDDEN),
                false);
    }

    /**
     * Utility method to convert this.id to JavaSymbolName. It is useful to
     * maintain Roo representation, Roo uses JavaSymbolName to identify a page
     * menu entry.
     * 
     * @return
     */
    public JavaSymbolName getJavaSymbolName() {
        return new JavaSymbolName(this.id);
    }

    private boolean parseBoolean(String attribute, boolean defaultValue) {
        if (attribute == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(attribute);
    }

    public String getId() {
        return id;
    }

    public String getLabelCode() {
        return labelCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getUrl() {
        return url;
    }

    public String getRoles() {
        return roles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + (hidden ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((messageCode == null) ? 0 : messageCode.hashCode());
        result = prime * result + ((roles == null) ? 0 : roles.hashCode());
        result = prime * result
                + ((labelCode == null) ? 0 : labelCode.hashCode());
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
        MenuEntry other = (MenuEntry) obj;
        if (url == null) {
            if (other.url != null)
                return false;
        }
        else if (!url.equals(other.url))
            return false;
        if (hidden != other.hidden)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (roles == null) {
            if (other.roles != null)
                return false;
        }
        else if (!roles.equals(other.roles))
            return false;
        if (labelCode == null) {
            if (other.labelCode != null)
                return false;
        }
        else if (!labelCode.equals(other.labelCode))
            return false;
        if (messageCode == null) {
            if (other.messageCode != null)
                return false;
        }
        else if (!messageCode.equals(other.messageCode))
            return false;
        return true;
    }
}
