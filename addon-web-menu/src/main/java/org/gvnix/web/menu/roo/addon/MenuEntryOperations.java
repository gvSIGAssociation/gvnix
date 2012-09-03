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

import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.model.JavaSymbolName;
import org.w3c.dom.Document;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on
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
public interface MenuEntryOperations {

    static final String CATEGORY_MENU_ITEM_PREFIX = "c_";

    /**
     * Indicate project should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isProjectAvailable();

    /**
     * Indicate the project has a web layer based on Spring MVC Tiles.
     * 
     * @return true if the user installed an Spring MVC Tiles web layer,
     *         otherwise returns false.
     */
    boolean isSpringMvcTilesProject();

    /**
     * Indicate project has a gvNIX menu.
     * 
     * @return true if the user installed the gvNIX menu, otherwise returns
     *         false.
     */
    boolean isGvNixMenuAvailable();
    
    /**
     * Disables Roo MenuOperationsImpl service.
     * <p>
     * <em>IMPORTANT:</em> OSGi container will inject gvNIX MenuOperationsImpl
     * service to clients.
     */
    public void disableRooMenuOperations();

    /**
     * Checks if Spring Security 3.0.5.RELEASE is installed.
     * 
     * @return true if Spring Security 3.0.5 is installed. Otherwise returns
     *         false
     */
    boolean isSpringSecurityInstalled();

    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();

    /**
     * Create or update menu web layer artefacts.
     * 
     * @param classesPackage
     *            Web layer artefacts contains references to Java classes in
     *            this package (used to create import declarations in artefacts)
     */
    void createWebArtefacts(String classesPackage);

    /**
     * Allows for the addition of menu categories and menu items. If a category
     * or menu item with the given identifier exists then it will <b>not</b> be
     * overwritten or replaced.
     * <p>
     * Addons can determine their own category and menu item identifiers so that
     * there are no clashes with other addons.
     * <p>
     * This method will <i>not</i> write i18n message codes. This means the
     * caller will manage the properties himself - allowing for better
     * efficiency.
     * <p>
     * The recommended category identifier naming convention is
     * <i>menu_category_the-name_label</i> where intention represents a further
     * identifier to diffentiate between different categories provided by the
     * same addon. Similarly, the recommended menu item identifier naming
     * convention is <i>menu_item_the-name_the-category_label</i>.
     * 
     * @param menuCategoryName
     * @param menuItemId
     * @param globalMessageCode
     *            Code to load message from I18N properties
     * @param link
     * @param idPrefix
     * @see org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#addMenuItem(JavaSymbolName,
     *      JavaSymbolName, String, String, String)
     */
    void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String globalMessageCode, String link,
            String idPrefix);

    /**
     * Allows for the addition of menu categories and menu items. If a category
     * or menu item with the given identifier exists then it will <b>not</b> be
     * overwritten or replaced.
     * <p>
     * Addons can determine their own category and menu item identifiers so that
     * there are no clashes with other addons.
     * <p>
     * The recommended category identifier naming convention is
     * <i>addon-name_intention_category</i> where intention represents a further
     * identifier to differentiate between different categories provided by the
     * same addon. Similarly, the recommended menu item identifier naming
     * convention is <i>addon-name_intention_menu_item</i>.
     * <p>
     * This method replicates
     * {@link org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#addMenuItem(JavaSymbolName, JavaSymbolName, String, String, String, String)}
     * to provide same functionality for Roo clients when gvNIX MenuOperations
     * service setup will replace Roo MenuOperations service.
     * 
     * @param menuCategoryName
     * @param menuItemId
     * @param menuItemLabel
     *            Text to be used as argument of message
     * @param globalMessageCode
     *            Code to load message from I18N properties
     * @param link
     * @param idPrefix
     * @see org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#addMenuItem(JavaSymbolName,
     *      JavaSymbolName, String, String, String, String)
     */
    void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String menuItemLabel,
            String globalMessageCode, String link, String idPrefix);

    /**
     * Adds new item to menu.
     * <p>
     * This method provides the same functionality than
     * {@link #addMenuItem(JavaSymbolName, JavaSymbolName, String, String, String, String)}
     * plus custom menu entry info introduced in gvNIX like the list of roles
     * authorized to access to given URL and a flag that indicates if the menu
     * entry should be visible in menu.
     * <p>
     * This method won't add neither Controller nor JSPs for new menu entries,
     * so if you need them, add a new page using {@code controller class}
     * command. Menu add-on will detect the new controller all will add it to
     * menu.
     * <p>
     * A command to manage categories is not needed, just add a new item without
     * parent category and it will be added to the default category. Then change
     * the default category ID when you need ({@code menu entry update}), new
     * default category will be created automatically.
     * <p>
     * Link is not required because gvNIX menu item could act as sub-category.
     * 
     * @param menuCategoryName
     * @param menuItemId
     * @param menuItemLabel
     *            Text to be used as argument of message
     * @param globalMessageCode
     *            Code to load message from I18N properties
     * @param link
     * @param idPrefix
     * @param roles
     * @param hide
     * @param writeProps
     * @return ID assigned to new menu entry
     * @see org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#addMenuItem(JavaSymbolName,
     *      JavaSymbolName, String, String, String, String, String, boolean,
     *      boolean)
     */
    String addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String menuItemLabel,
            String globalMessageCode, String link, String idPrefix,
            String roles, boolean hide, boolean writeProps);

    /**
     * Update menu config file with given contents
     * 
     * @param doc
     *            new contents for menu.xml
     */
    void writeXMLConfigIfNeeded(Document doc);

    /**
     * Gets menu config file loaded in a Document object.
     * 
     * @return XML Document
     */
    Document getMenuDocument();

    /**
     * Return a formated string that shows complete menu tree info
     * 
     * @param pageId
     *            menu entry identifier
     * @param lang
     *            Create info in this language
     * @return
     */
    String getFormatedInfo(JavaSymbolName pageId, I18n lang);

    /**
     * Return a formated string that shows compact menu tree info.
     * <p>
     * Info about labels, roles is not shown
     * 
     * @param pageId
     * @return
     */
    String getCompactInfo(JavaSymbolName pageId);

    /**
     * Return a formated string with a list representation of a subtree.
     * <p>
     * By default shows all menu entry Ids plus target URLs.
     * 
     * @param pageId
     *            root node of subtree to be shown. If null, show complete menu
     *            tree
     * @param label
     *            show label values
     * @param message
     *            show message values
     * @param roles
     *            show roles values
     * @param lang
     *            show messages in this language
     * @return
     */
    String getFormatedInfo(JavaSymbolName pageId, boolean label,
            boolean messageCode, boolean roles, I18n lang);

    /**
     * Move the menu entry node and its children into another node.<br/>
     * 
     * The element will be place at the end of <code>into</code> children.
     * 
     * @param page
     * @param into
     * @return
     */
    void moveInto(JavaSymbolName pageId, JavaSymbolName intoId);

    /**
     * Move the menu entry node and its children before another node.
     * <p>
     * The element will be place into the same parent of <code>before</code> and
     * before it.
     * 
     * @param page
     * @param before
     * @return
     */
    void moveBefore(JavaSymbolName pageId, JavaSymbolName beforeId);

    /**
     * Update values of a menu entry.
     * 
     * @param pageId
     *            Current menu entry ID
     * @param nid
     *            New menu entry ID
     * @param label
     *            New text label
     * @param messageCode
     *            New message code
     * @param url
     *            New url
     * @param roles
     *            New rol list
     * @param hidden
     *            Set/Unset hidden
     * @param writeProps
     * @return
     */
    void updateEntry(JavaSymbolName pageId, JavaSymbolName nid, String label,
            String messageCode, String url, String roles, Boolean hidden,
            boolean writeProps);

    /**
     * Gets the absolute path for the menu config file {@code menu.xml}.
     * 
     * @return the absolute path to the file (never null)
     */
    String getMenuConfigFile();
}
