/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.datatables;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Web MVC JQuery datatables commands class
 * 
 * @author gvNIX Team
 */
@Component
@Service
public class DatatablesCommands implements CommandMarker {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(DatatablesCommands.class);
    /**
     * Get a reference to the DatatablesOperations
     */
    @Reference
    private DatatablesOperations operations;

    /**
     * Informs if setup commands is available
     * 
     * @return true if setup is available
     */
    @CliAvailabilityIndicator("web mvc datatables setup")
    public boolean isSetupAvailable() {
        return operations.isSetupAvailable();
    }

    /**
     * Informs if update tags command is available
     * 
     * @return true if commands is available
     */
    @CliAvailabilityIndicator("web mvc datatables update tags")
    public boolean isUpdateTagsAvailable() {
        return operations.isUpdateTagsAvailable();
    }

    /**
     * Informs if commands are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc datatables add",
            "web mvc datatables all", "web mvc datatables detail add" })
    public boolean isAddAvailable() {
        return operations.isAddAvailable();
    }

    /**
     * Use datatables component for a controller list view
     * 
     * @param type target controller
     */
    @CliCommand(value = "web mvc datatables add", help = "Add Dandelion Datatables requests support to given Controller and change the tables in the JSP pages of the related weblayer to Dandelion Datatables")
    public void add(
            @CliOption(key = "type", mandatory = true, help = "The controller to apply this component to") JavaType target,
            @CliOption(key = "ajax", mandatory = false, unspecifiedDefaultValue = "true", help = "true (default) to load data using AJAX, otherwise the data are loaded on page render time") boolean ajax,
            @CliOption(key = "inline", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Allow user to modify data in-line, that is, enable in-line editing") boolean inline,
            @CliOption(key = "mode", mandatory = false, unspecifiedDefaultValue = GvNIXDatatables.TABLE, help = "Visualization mode: if empty (default) renders a table, otherwise create one-row-per-page + one-cell-per-row datatable will be created. On each cell the content of given mode will be rendered, that is, by setting mode == show, each cell will have the show.jspx containing the data of the current entity") String mode,
            @CliOption(key = "baseFilter", mandatory = false, help = "Add a default base filter to this datatable.  Using the following format: nameEQjohnAndageGT26") JavaSymbolName baseFilter) {
        if (baseFilter != null
                && !baseFilter.toString().matches(
                        "((And)?[a-zA-Z0-9]+[A-Z][B-Z]+[a-zA-Z0-9]*)+")) {
            LOGGER.log(Level.INFO,
                    "[ERROR] BaseFilter value " + baseFilter.toString()
                            + " doesn't match required format.");
        }
        else {
            if (baseFilter != null && ajax != true)
                LOGGER.log(Level.INFO, "[INFO] BaseFilter with value "
                        + baseFilter.toString()
                        + " will not be applied if mode ajax is disabled.");
            operations.annotateController(target, ajax, mode, inline,
                    baseFilter);
        }
    }

    /**
     * Use detail datatables component for a controller list view
     * 
     * @param target controller of master datatables
     * @param property of controller entity for detail
     */
    @CliCommand(value = "web mvc datatables detail add", help = "Use detail datatable component for a controller list view")
    public void add(
            @CliOption(key = "type", mandatory = true, help = "The controller to apply this component to") JavaType target,
            @CliOption(key = "property", mandatory = true, help = "The controller entity property to show as detail") String property) {

        // TODO Validate property exists and/or auto completed parameter

        operations.annotateDetailController(target, property);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     */
    @CliCommand(value = "web mvc datatables all", help = "Use datatable component for all list view in this application")
    public void all(
            @CliOption(key = "ajax", mandatory = false, unspecifiedDefaultValue = "true", help = "Datatables will use AJAX request to get data data or not") boolean ajax) {
        operations.annotateAll(ajax);
    }

    /**
     * Setup datatables artifacts
     * 
     * @param webPackage (optional) controller package. Required if no
     *        conversionService declared on project
     */
    @CliCommand(value = "web mvc datatables setup", help = "Install the project dependencies, tags and artifacts that Dandelion Datatables needs")
    public void setup(
            @CliOption(key = "package", mandatory = false, help = "controllers base package. Required if no conversionService registered jet.") JavaPackage webPackage) {
        operations.setup(webPackage);
    }

    /**
     * Update related datatables artifacts (tags, js, images...)
     */
    @CliCommand(value = "web mvc datatables update tags", help = "Update datatables artificats (tags, images, js)")
    public void updateTags() {
        operations.updateTags();
    }
}
