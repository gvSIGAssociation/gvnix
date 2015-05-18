/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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

package org.gvnix.addon.bootstrap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell
 * following an automatic classpath scan. You can provide simple user
 * presentation-related logic in this class. You can return any objects from
 * each method, or use the logger directly if you'd like to emit messages of
 * different severity (and therefore different colours on non-Windows systems).
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.1
 */
@Component
@Service
public class BootstrapCommands implements CommandMarker {

    /**
     * Get a reference to the BootstrapOperations from the underlying OSGi
     * container
     */
    @Reference
    private BootstrapOperations operations;

    /**
     * This method checks if the setup method is available
     *
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc bootstrap setup")
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * This method checks if the update method is available
     *
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc bootstrap update")
    public boolean isUpdateAvailable() {
        return operations.isUpdateCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     *
     * @param type
     */
    @CliCommand(value = "web mvc bootstrap setup",
            help = "Setup Bootstrap 3 in your project.")
    public void setup() {
        operations.setup();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     *
     * @param type
     */
    @CliCommand(value = "web mvc bootstrap update",
            help = "Update Bootstrap 3 tags in your project. Use this if you installed menu, datatables or security after bootstrap setup.")
    public void update() {
        operations.updateTags();
    }

}