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

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.roundtrip.XmlRoundTripFileManager;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * gvNIX implementation of standard Roo menu operation service.
 * <p>
 * This class extends and replace the services that
 * {@link org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperationsImpl}
 * offers.
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */

@Component
@Service
public class MenuOperationsImpl implements MenuOperations {

    private static Logger logger = HandlerUtils
            .getLogger(MenuOperationsImpl.class);

    /**
     * Property to identify this service in {@link FilterMenuOperationsHook} and
     * {@link MenuOperationsProxy}
     */
    @Property(boolValue = true)
    public static final String GVNIX_COMPONENT = "gvNIXComponent";

    /**
     * Use AddonOperations delegate to operations this add-on offers
     */
    @Reference
    private MenuEntryOperations operations;
    @Reference
    private XmlRoundTripFileManager xmlFileManager;

    /**
     * Waits until all required references are available
     */
    private void waitToReferences() {
        if (operations != null && xmlFileManager != null) {
            return;
        }
        while (!(operations != null && xmlFileManager != null)) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                break;
            }
        }
    }

    /** {@inheritDoc} */
    public void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String globalMessageCode, String link,
            String idPrefix, LogicalPath logicalPath) {
        waitToReferences();
        // TODO Added logicalPath param to method: related methods modification
        // required ?
        operations.addMenuItem(menuCategoryName, menuItemId, globalMessageCode,
                link, idPrefix);
    }

    /** {@inheritDoc} */
    public void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String menuItemLabel,
            String globalMessageCode, String link, String idPrefix,
            LogicalPath logicalPath) {
        waitToReferences();
        // TODO Added logicalPath param to method: related methods modification
        // required ?
        operations.addMenuItem(menuCategoryName, menuItemId, menuItemLabel,
                globalMessageCode, link, idPrefix);
    }

    public void cleanUpFinderMenuItems(JavaSymbolName menuCategoryName,
            List<String> allowedFinderMenuIds, LogicalPath logicalPath) {

        waitToReferences();
        // TODO Added logicalPath param to method: related methods modification
        // required ?
        Validate.notNull(menuCategoryName, "Menu category identifier required");
        Validate.notNull(allowedFinderMenuIds,
                "List of allowed menu items required");

        Document document = operations.getMenuDocument();

        StringBuilder categoryId = new StringBuilder(
                MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX);
        categoryId.append(menuCategoryName.getSymbolName().toLowerCase());

        // find any menu items under this category which have an id that starts
        // with the menuItemIdPrefix
        List<Element> elements = XmlUtils.findElements(
                "//menu-item[@id='".concat(categoryId.toString())
                        .concat("']//menu-item[starts-with(@id, '")
                        .concat(FINDER_MENU_ITEM_PREFIX).concat("')]"),
                document.getDocumentElement());
        if (elements.size() == 0) {
            return;
        }
        for (Element element : elements) {
            if (!allowedFinderMenuIds.contains(element.getAttribute("id"))) {
                element.getParentNode().removeChild(element);
            }
        }
        xmlFileManager.writeToDiskIfNecessary(operations.getMenuConfigFile(),
                document);
    }

    /**
     * Attempts to locate a menu item and remove it.
     * 
     * @param menuCategoryName the identifier for the menu category (required)
     * @param menuItemName the menu item identifier (required)
     * @param idPrefix the prefix to be used for this menu item (optional,
     *        MenuOperations.DEFAULT_MENU_ITEM_PREFIX is default)
     */
    public void cleanUpMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemName, String idPrefix,
            LogicalPath logicalPath) {

        waitToReferences();
        // TODO Added logicalPath param to method: related methods modification
        // required ?
        Validate.notNull(menuCategoryName, "Menu category identifier required");
        Validate.notNull(menuItemName, "Menu item id required");

        if (idPrefix == null || idPrefix.length() == 0) {
            idPrefix = DEFAULT_MENU_ITEM_PREFIX;
        }

        Document document = operations.getMenuDocument();

        StringBuilder categoryId = new StringBuilder(
                MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX);
        categoryId.append(menuCategoryName.getSymbolName().toLowerCase());

        StringBuilder itemId = new StringBuilder(idPrefix);
        itemId.append(menuCategoryName.getSymbolName().toLowerCase())
                .append("_").append(menuItemName.getSymbolName().toLowerCase());

        // find menu item under this category if exists
        Element element = XmlUtils.findFirstElement(
                "//menu-item[@id='".concat(categoryId.toString())
                        .concat("']//menu-item[@id='")
                        .concat(itemId.toString()).concat("']"),
                document.getDocumentElement());
        if (element == null) {
            return;
        }
        else {
            element.getParentNode().removeChild(element);
        }

        operations.writeXMLConfigIfNeeded(document);
    }

    /**
     * Informs if gvNIX menu is activated
     * 
     * @return
     */
    public boolean isGvNixMenuAvailable() {
        waitToReferences();
        return operations.isGvNixMenuAvailable();
    }
}
