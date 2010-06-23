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

/**
 * gvNIX menu operations service
 *
 * This class provide methos to manage the menu's pages
 *
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public interface MenuPageOperations {

    /**
     * Informs if Roo project has been created
     *
     * @return
     */
    public boolean isProjectAvailable();

    /**
     * Informs if service is doing a operation at this moment
     *
     * @return
     */
    public boolean isWorking();

    /**
     * Informs if gvNIX menu has been setup
     *
     * @return
     */
    public boolean isActivated();

    /**
     * Performs gvNIX menu setup
     *
     */
    public void setup();

    /**
     * Get a menu page from a path.<br>
     *
     * Path is like unix like file system path where root element is <code>/</code>.
     *
     * @param path to required item
     * @return the menu item
     */
    public MenuPageItem getPageItem(String path);

    /**
     * Add a page menu item.<br>
     *
     * You can't duplicate internal destiniation in two pages. There is no checks for external destination<br>
     *
     * Internal destination must be relative (example: <code>/Aentity/?form</code>)<br>
     *
     * External destination must be absolute and starts with <code>http://</code> or <code>https://</code> (example: <code>http://www.disid.com</code>)<br>
     *
     * You can't duplicate page's name in the same parent.<br>
     *
     * @param parent new page's parent, if is null parent will be root
     * @param name page's name (mandatory)
     * @param label label to use in page. You must set a label or a messageCode
     * @param messageCode code of message to use in page. It has higher priority than label
     * @param destination URL destination of item.
     * @param roles coma separated list of roles allowed to show this page. If it's <code>null</code> there will be no restrictions.
     * @param rooId Roo menu id. This is used for stardard roo menu operations
     * @param hide Hide this page
     * @return
     */
    public MenuPageItem addPage(MenuPageItem parent, String name, String label,
	    String messageCode, String destination, String roles, String rooId,
	    boolean hide);

    /**
     * Remove a page.<br>
     *
     * If <code>page</code> has children this operation will fail.<br>
     *
     * If <code>page</code> has set <code>rooId</code> property this operation will only set to hidden.<br>
     *
     * @param page to remove
     * @param force remove including its children
     * @param ignoreRooId Ignore <code>rooId</code> property
     */
    public void removePage(MenuPageItem page, boolean force, boolean ignoreRooId);

    /**
     * Update values of a page.<br/>
     *
     *
     * @param page
     * @param symbolName
     * @param label
     * @param messageCode
     * @param destination
     * @param roles
     * @param rooId
     * @param hidden
     * @return
     */
    public MenuPageItem updatePage(MenuPageItem page, String symbolName,
	    String label, String messageCode, String destination, String roles,
	    String rooId, boolean hidden);

    /**
     * Changes page's visibility to hidden
     *
     * @param page
     */
    public void hidePage(MenuPageItem page);

    /**
     * Changes page's visibility visible
     *
     * @param page
     */
    public void showPage(MenuPageItem page);

    /**
     * Return a formated string with a list representation of a subtree.
     *
     * @param page main node of subtree
     * @param label show label values
     * @param messageCode show messageCode values
     * @param destination show destination values
     * @param roles show roles values
     * @return
     */
    public String getFormatedList(MenuPageItem page, boolean label, boolean messageCode, boolean destination, boolean roles);

    /**
     * Return a string with a formated string page information
     *
     * @param page
     * @return
     */
    public String getFormatedInfo(MenuPageItem page);

    /**
     * Informs if setup command is available
     *
     * @return
     */
    public boolean isSetupAvailable();

    /**
     * Move the node page and its children into another node.<br/>
     *
     * The element will be place at the end of <code>into</code> children.
     *
     * @param page
     * @param into
     * @return
     */
    public MenuPageItem moveInto(MenuPageItem page, MenuPageItem into);

    /**
     * Move the node page and its children before another node.<br/>
     *
     * The element will be place into the same parent of <code>before</code> and
     * before it.
     *
     * @param page
     * @param before
     * @return
     */
    public MenuPageItem moveBefore(MenuPageItem page, MenuPageItem before);

    /**
     * Informs if menu model if modifiable.
     *
     * @return
     */
    public boolean isModelModifiable();

    /**
     * Absolute path to project configuration file
     *
     * @return
     */
    public String getConfigXMLFile();

}
