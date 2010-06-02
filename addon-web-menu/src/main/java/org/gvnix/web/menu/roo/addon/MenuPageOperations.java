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

    /** TODO */
    public MenuPageItem updatePage(MenuPageItem page, String symbolName,
	    String label, String messageCode, String destination, String roles,
	    String rooId, boolean hidden);

    /** TODO */
    public void hidePage(MenuPageItem page);

    /** TODO */
    public void showPage(MenuPageItem page);

    /** TODO */
    public String getFormatedList(MenuPageItem page, boolean label, boolean messageCode, boolean destination, boolean roles);

    /** TODO */
    public String getFormatedInfo(MenuPageItem page);

    /** TODO */
    public boolean isSetupAvailable();

    /** TODO */
    public MenuPageItem moveInto(MenuPageItem page, MenuPageItem into);

    /** TODO */
    public MenuPageItem moveBefore(MenuPageItem page, MenuPageItem before);

    /** TODO */
    public boolean isModelModifiable();

    /**
     * Absolute path to project configuration file
     *
     * @return
     */
    public String getConfigXMLFile();

}
