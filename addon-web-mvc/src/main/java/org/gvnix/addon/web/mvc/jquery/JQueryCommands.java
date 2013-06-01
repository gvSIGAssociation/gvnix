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
package org.gvnix.addon.web.mvc.jquery;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Web MVC JQuery commands class
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public class JQueryCommands implements CommandMarker {

    /**
     * Get a reference to the JQueryOperations
     */
    @Reference
    private JQueryOperations operations;

    /**
     * Informs if setup command is available
     * 
     * @return true if setup is available
     */
    @CliAvailabilityIndicator("web mvc jquery setup")
    public boolean isSetupAvailable() {
        return operations.isSetupAvailable();
    }

    /**
     * Informs if update tags command is available
     * 
     * @return true if commands is available
     */
    @CliAvailabilityIndicator("web mvc jquery update tags")
    public boolean isUpdateTagsAvailable() {
        return operations.isUpdateTagsAvailable();
    }

    /**
     * Informs if add commands are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc jquery add", "web mvc jquery all" })
    public boolean isAddAvailable() {
        return operations.isAddAvailable();
    }

    /**
     * Use JQuery for a controller list view
     * 
     * @param type target controller
     */
    @CliCommand(value = "web mvc jquery add", help = "Use JQuery for a controller list view")
    public void add(
            @CliOption(key = "type", mandatory = true, help = "The controller to apply JQuery to") JavaType target) {
        operations.annotateController(target);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     */
    @CliCommand(value = "web mvc jquery all", help = "Use JQuery for all list view in this application")
    public void all() {
        operations.annotateAll();
    }

    /**
     * Setup JQuery artifacts
     */
    @CliCommand(value = "web mvc jquery setup", help = "Setup JQuery support")
    public void setup() {
        operations.setup();
    }

    /**
     * Update related JQuery artifacts (tags, js, images...)
     */
    @CliCommand(value = "web mvc jquery update tags", help = "Update jquery artificats (tags, images, js)")
    public void updateTags() {
        operations.updateTags();
    }
}