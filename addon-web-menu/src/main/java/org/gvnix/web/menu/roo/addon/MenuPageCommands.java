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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.util.Assert;

/**
 * Roo Commands for gvNIX menu page
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class MenuPageCommands implements CommandMarker {

    // private static Logger logger = Logger.getLogger(MenuPageCommands.class
    // .getName());

    static final String SET_TO_NULL = "#null-value#";

    @Reference
    private MenuPageOperations operations;

    protected void activate(ComponentContext context) {

    }

    protected void deactivate(ComponentContext context) {

    }

    @CliAvailabilityIndicator("page menu setup")
    public boolean isInstallAvailable() {
	return operations.isSetupAvailable();
    }

    @CliCommand(value = "page menu setup", help = "Install gvNIX web menu. Adds multilevel menu, advanced roo-shell menu commands (page add, page move, page list...) and context menu support.")
    public void setup() {
	operations.setup();
    }

    @CliAvailabilityIndicator( { "page add", "page hide", "page remove",
	    "page move", "page update", "page info", "page list", "page show" })
    public boolean isMenuCommandsAvailable() {
	return operations.isActivated() && operations.isModelModifiable();
    }

    @CliCommand(value = "page add", help = "Add new page to menu")
    public void addPage(
	    @CliOption(key = "parent", mandatory = false, unspecifiedDefaultValue = MenuPageItem.ROOT_PATH, help = "Node parent where add new page item (default root)") MenuPageItem parent,
	    @CliOption(key = "name", mandatory = true, help = "Name to identify the page item") JavaSymbolName name,
	    @CliOption(key = "label", mandatory = false, help = "String or message code to menu label") String label,
	    @CliOption(key = "messageCode", mandatory = false, help = "Message code for menu label") String messageCode,
	    @CliOption(key = "destination", mandatory = false, help = "Item's destination") String destination,
	    @CliOption(key = "roles", mandatory = false, help = "User Role list (comma separated) that can use this page item. If not set is available for every one.") String roles) {
	Assert.isTrue(label != null || messageCode != null,
		"Must set a label or a messageCode");
	operations.addPage(parent, name.getSymbolName(), label, messageCode,
		destination, roles, null, false);
    }

    @CliCommand(value = "page remove", help = "Remove a page. This only remove menu item no item's destination. If it has sub-items operation will be canceled. You can use --force to force operation.")
    public void removePage(
	    @CliOption(key = "page", mandatory = true, help = "Item to remove") MenuPageItem page,
	    @CliOption(key = "force", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Force to perform operation when a page has children.") boolean force) {
	Assert.notNull(page, "A page is required");
	operations.removePage(page, force, false);
    }

    @CliCommand(value = "page hide", help = "Hide a page. This only affects menu item no item's destination.")
    public void hidePage(
	    @CliOption(key = "page", mandatory = true, help = "Item to hide") MenuPageItem page) {
	Assert.notNull(page, "A page is required");
	operations.hidePage(page);
    }

    @CliCommand(value = "page show", help = "Make visible a hidden a page. This only affects menu item no item's destination.")
    public void showPage(
	    @CliOption(key = "page", mandatory = true, help = "Item to show") MenuPageItem page) {
	Assert.notNull(page, "A page is required");
	operations.showPage(page);
    }

    @CliCommand(value = "page move", help = "Move a page and its children to another plase.")
    public void movePage(
	    @CliOption(key = "page", mandatory = true, help = "Item to move") MenuPageItem page,
	    @CliOption(key = "into", mandatory = false, help = "Insert the page into this.") MenuPageItem into,
	    @CliOption(key = "before", mandatory = false, help = "Locate the page before this (in the same level).") MenuPageItem before) {
	Assert.notNull(page, "A page is required");
	Assert
		.isTrue(!(into == null && before == null),
			"One (and only one) of 'target' parameters ('into' or 'before') must be set");
	Assert.isTrue(!(into != null && before != null),
		"Only one of 'target' paremters can be set.");
	if (into != null) {
	    operations.moveInto(page, into);
	} else {
	    operations.moveBefore(page, before);
	}
    }

    @CliCommand(value = "page update", help = "Change values of a menu item")
    public void updatePage(
	    @CliOption(key = "page", mandatory = true, help = "Node to modify") MenuPageItem page,
	    @CliOption(key = "name", mandatory = false, help = "Name to identify the page item", specifiedDefaultValue = SET_TO_NULL) JavaSymbolName name,
	    @CliOption(key = "label", mandatory = false, help = "String to use as menu label", specifiedDefaultValue = SET_TO_NULL) String label,
	    @CliOption(key = "messageCode", mandatory = false, help = "Message code for menu label", specifiedDefaultValue = SET_TO_NULL) String messageCode,
	    @CliOption(key = "destination", mandatory = false, help = "Item's destination", specifiedDefaultValue = SET_TO_NULL) String destination,
	    @CliOption(key = "roles", mandatory = false, help = "User Role list (comma separated) that can use this page item. If not set is available for every one.", specifiedDefaultValue = SET_TO_NULL) String roles,
	    @CliOption(key = "hidden", mandatory = false, help = "It's hidden.") Boolean hidden) {

	Assert.notNull(page, "A page is required");

	if (name == null) {
	    name = new JavaSymbolName(page.getName());
	} else if (name.equals(SET_TO_NULL)) {
	    name = null;
	}

	if (label == null) {
	    label = page.getLabel();
	} else if (label.equals(SET_TO_NULL)) {
	    label = null;
	}

	if (messageCode == null) {
	    messageCode = page.getMessageCode();
	} else if (messageCode.equals(SET_TO_NULL)) {
	    messageCode = null;
	}

	if (destination == null) {
	    destination = page.getDestination();
	} else if (destination.equals(SET_TO_NULL)) {
	    destination = null;
	}

	if (roles == null) {
	    roles = page.getRoles();
	} else if (roles.equals(SET_TO_NULL)) {
	    roles = null;
	}

	if (hidden == null) {
	    hidden = page.isHidden();
	}

	operations.updatePage(page, name.getSymbolName(), label, messageCode,
		destination, roles, null, hidden);
    }

    @CliCommand(value = "page list", help = "List current menu tree structure.")
    public String list(
	    @CliOption(key = "page", mandatory = false, help = "Item to list") MenuPageItem page,
	    @CliOption(key = "label", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show titles") boolean label,
	    @CliOption(key = "messageCode", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show titles") boolean messageCode,
	    @CliOption(key = "destination", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show destinations") boolean destination,
	    @CliOption(key = "roles", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show Roles") boolean roles) {
	return operations.getFormatedList(page, label, messageCode, destination,
		roles);
    }

    @CliCommand(value = "page info", help = "Shows all information about a page.")
    public String getMenuInfoFormated(
	    @CliOption(key = "page", mandatory = true, help = "Item to show info") MenuPageItem page) {
	Assert.notNull(page, "A page is required");
	return operations.getFormatedInfo(page);
    }

}
