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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.menu.roo.addon.util.StringUtils;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related
 * logic in this class. You can return any objects from each method, or use the
 * logger directly if you'd like to emit messages of different severity (and
 * therefore different colours on non-Windows systems).
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
@Component
// use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class MenuEntryCommands implements CommandMarker { // all command types
                                                          // must implement the
                                                          // CommandMarker
                                                          // interface

    private static Logger logger = HandlerUtils
            .getLogger(MenuEntryCommands.class);

    /**
     * Use MenuEntryOperations to execute operations
     */
    @Reference
    private MenuEntryOperations operations;

    /**
     * Automatic {@code menu setup} command hiding in situations when the
     * command should not be visible. Command will not be made available before
     * the user has defined his Spring MVC settings in the Roo shell or directly
     * in the project.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "menu setup" })
    public boolean isSetupCommandAvailable() {
        return operations.isProjectAvailable()
                && operations.isSpringMvcTilesProject()
                && !operations.isGvNixMenuAvailable();
    }

    /**
     * Automatic {command hiding in situations when the commands should not be
     * visible. Commands will not be made available before the user has executed
     * {@code menu setup} command in the Roo shell or directly in the project.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "menu entry add", "menu entry visibility",
            "menu entry move", "menu entry update", "menu entry info",
            "menu entry roles", "menu tree" })
    public boolean areMenuCommandsAvailable() {
        return operations.isProjectAvailable()
                && operations.isGvNixMenuAvailable();
    }

    @CliCommand(value = "menu setup",
            help = "Install gvNIX web menu: multilevel menu, advanced roo-shell menu commands and context menu support.")
    public void setup() {
        operations.setup();
    }

    @CliCommand(value = "menu entry add",
            help = "Add new menu item to menu. This command won't add neither Controller nor JSPs for the new entry, if you need them use 'controller class' instead.")
    public String addEntry(
            @CliOption(key = "label",
                    mandatory = true,
                    help = "Text to show in menu if no messageCode set, otherwise label is used as message argument.") String label,
            @CliOption(key = "category",
                    mandatory = false,
                    help = "Add entry into this menu entry (category). Default add to 'Page' category. Note you don't need a command to manage categories, just change default category ID ('menu entry update') when you need, new default category will be created automatically.") MenuEntry parentEntryId,
            @CliOption(key = "messageCode",
                    mandatory = false,
                    help = "The global message code to get I18N label text (works in conjunction with label). If empty, it will be generated using entry name.") String messageCode,
            @CliOption(key = "url",
                    mandatory = false,
                    help = "The link URL to access to this page.") String url,
            @CliOption(key = "roles",
                    mandatory = false,
                    help = "User that has any of this granted roles (comma separated) will see this menu entry. If empty, the menu entry is shown for every one.") String roles) {

        // Default category is the same default category that Roo uses
        JavaSymbolName categoryName = null;
        if (parentEntryId == null) {
            categoryName = new JavaSymbolName("Page");
        }
        else {
            categoryName = parentEntryId.getJavaSymbolName();
        }

        // create new page using as menu item id the given label replacing
        // spaces by underscores
        String pageId = operations.addMenuItem(categoryName,
                new JavaSymbolName(StringUtils.underscoreAllWhitespace(label)),
                label, messageCode, url, null, roles, false, true);
        return "New page '".concat(pageId).concat("' added.");
    }

    @CliCommand(value = "menu entry visibility",
            help = "Show/Hide a menu entry. This only affects menu entry neither related artefacts nor page accessibility.")
    public void hideEntry(
            @CliOption(key = "id",
                    mandatory = true,
                    help = "Menu entry id to show/hide. Use 'menu tree' to get all pages ids.") MenuEntry menuEntryId,
            @CliOption(key = "hidden",
                    mandatory = true,
                    unspecifiedDefaultValue = "false",
                    specifiedDefaultValue = "true",
                    help = "Show/Hide menu entry. Default, show menu entry.") boolean hidden) {
        operations.updateEntry(menuEntryId.getJavaSymbolName(), null, null,
                null, null, null, hidden, false);
    }

    @CliCommand(value = "menu entry roles",
            help = "Set the user roles that will grant entry to be shown depending on user roles and target URL permissions.")
    public void secureEntry(
            @CliOption(key = "id",
                    mandatory = true,
                    help = "Menu entry ID to update.") MenuEntry menuEntryId,
            @CliOption(key = "roles",
                    mandatory = true,
                    help = "User role list (comma separated) that can access this page. If empty, the page is available for every one.") String roles) {
        operations.updateEntry(menuEntryId.getJavaSymbolName(), null, null,
                null, null, roles, null, false);
    }

    @CliCommand(value = "menu entry move",
            help = "Move a menu entry and its children to another tree node.")
    public void moveEntry(
            @CliOption(key = "id", mandatory = true, help = "Item to move") MenuEntry menuEntryId,
            @CliOption(key = "into",
                    mandatory = false,
                    optionContext = MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX,
                    help = "Insert the menu entry into this.") MenuEntry intoEntryId,
            @CliOption(key = "before", mandatory = false, help = "   .") MenuEntry beforeEntryId) {
        Validate.notNull(menuEntryId, "A page is required");
        if (intoEntryId == null && beforeEntryId == null) {
            logger.log(Level.SEVERE, "'into' or 'before' parameter must be set");
            return;
        }
        if (intoEntryId != null && beforeEntryId != null) {
            logger.log(Level.SEVERE,
                    "Only one of 'target' parameters can be set.");
            return;
        }
        if (intoEntryId != null
                && !intoEntryId
                        .getJavaSymbolName()
                        .toString()
                        .startsWith(
                                MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX)) {
            logger.log(Level.SEVERE,
                    "An item must be moved into category entry only.");
            return;
        }

        if (intoEntryId != null) {
            operations.moveInto(menuEntryId.getJavaSymbolName(),
                    intoEntryId.getJavaSymbolName());
        }
        else {
            operations.moveBefore(menuEntryId.getJavaSymbolName(),
                    beforeEntryId.getJavaSymbolName());
        }
    }

    @CliCommand(value = "menu entry update", help = "Change menu entry data.")
    public void updateEntry(
            @CliOption(key = "id",
                    mandatory = true,
                    help = "Menu entry id to update. Use 'menu tree' to get all pages ids.") MenuEntry menuEntryId,
            @CliOption(key = "nid",
                    mandatory = false,
                    help = "New ID for selected page. Use new ID to change page type: use 'c_' prefix for category pages or 'i_' prefix for item pages.") JavaSymbolName pageName,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "The label text used for related menu item. Note that related labelCode will remain the same.") String label,
            @CliOption(key = "messageCode",
                    mandatory = false,
                    help = "The global message code to get I18N label text (works in conjunction with label). If empty, it will be generated using page name.") String messageCode,
            @CliOption(key = "url",
                    mandatory = false,
                    help = "The link URL to access to this page.") String url,
            @CliOption(key = "roles",
                    mandatory = false,
                    help = "User role list (comma separated) granted to access to target URL. If empty, the page is available for every one.") String roles,
            @CliOption(key = "hidden",
                    mandatory = false,
                    unspecifiedDefaultValue = "false",
                    specifiedDefaultValue = "true",
                    help = "Menu entry visibility.") boolean hidden) {
        operations.updateEntry(menuEntryId.getJavaSymbolName(), pageName,
                label, messageCode, url, roles, hidden, true);
    }

    @CliCommand(value = "menu tree", help = "List current menu tree structure.")
    public String list(
            @CliOption(key = "id",
                    mandatory = false,
                    help = "Menu entry id to show its tree structure. Default show all entries.") MenuEntry menuEntryId) {
        return operations.getCompactInfo(menuEntryId != null ? menuEntryId
                .getJavaSymbolName() : null);
    }

    @CliCommand(value = "menu entry info",
            help = "Shows all information about a menu entry.")
    public String entryInfo(
            @CliOption(key = "id",
                    mandatory = true,
                    help = "Menu entry identifier to show info.") MenuEntry menuEntryId,
            @CliOption(key = "lang",
                    mandatory = false,
                    help = "Show messages in this language.") I18n lang) {
        return operations
                .getFormatedInfo(menuEntryId.getJavaSymbolName(), lang);
    }
}
