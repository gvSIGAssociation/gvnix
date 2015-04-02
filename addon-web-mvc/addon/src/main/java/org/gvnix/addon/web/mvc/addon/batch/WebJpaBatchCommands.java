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
package org.gvnix.addon.web.mvc.addon.batch;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.web.mvc.annotations.batch.GvNIXWebJpaBatch;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * <code>web mvc batch</code> commands class
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public class WebJpaBatchCommands implements CommandMarker {

    /**
     * Get a reference to the WebJpaBatchOperations from the underlying OSGi
     * container
     */
    @Reference
    private WebJpaBatchOperations operations;

    /**
     * Informs if <code>web mvc batch setup</code> command is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc batch setup" })
    public boolean isSetupAvailable() {
        return operations.isSetupAvailable();
    }

    /**
     * Informs if <code>web mvc batch</code> commands are available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc batch add", "web mvc batch all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * Command to enable web mvc batch utilities on project
     * 
     * @param type
     */
    @CliCommand(value = "web mvc batch setup", help = "Install the project dependencies and update the webmvc config that gvNIX Web MVC Batch needs")
    public void setup() {
        operations.setup();
    }

    /**
     * Adds {@link GvNIXWebJpaBatch} annotation to a Controller
     * 
     * @param controller Target Web MVC controller to add support
     * @param service (optional) Spring service (annotated wid GvNIXJpaBatch) to
     *        use for batch operations. If no specified uses controller's
     *        formBacking object service."
     */
    @CliCommand(value = "web mvc batch add", help = "Adds support for JPA batch operations in given Controller")
    public void create(
            @CliOption(key = "controller", mandatory = true, help = "Target Web MVC controller to add support") JavaType controller,
            @CliOption(key = "service", mandatory = false, help = "Spring service (annotated wid GvNIXJpaBatch) to use for batch operations. If no specified uses controller's formBacking object service.") JavaType service) {
        operations.add(controller, service);
    }

    /**
     * Adds {@link GvNIXWebJpaBatch} annotation to a Controller
     */
    @CliCommand(value = "web mvc batch all", help = "Adds support for JPA batch operations in all Controller which related entity has a related JPA Batch Spring @Service")
    public void all() {
        operations.addAll();
    }
}
