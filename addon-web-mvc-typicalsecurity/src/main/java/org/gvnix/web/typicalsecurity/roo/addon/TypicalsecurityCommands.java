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

package org.gvnix.web.typicalsecurity.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
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
 *
 * @since 1.1
 */
@Component
@Service
public class TypicalsecurityCommands implements CommandMarker {
    @Reference
    private TypicalsecurityOperations operations;

    @CliAvailabilityIndicator({ "typicalsecurity setup", "typicalsecurity add" })
    public boolean isPropertyAvailable() {
        return operations.isCommandAvailable();
    }

    @CliCommand(value = "typicalsecurity setup",
            help = "Setup typicalsecurity addon")
    public String setup(
            @CliOption(key = "entityPackage",
                    mandatory = false,
                    help = "Package where entities are placed. Default: ~.domain",
                    specifiedDefaultValue = "~.domain",
                    unspecifiedDefaultValue = "~.domain") String entityPackage,
            @CliOption(key = "controllerPackage",
                    mandatory = false,
                    help = "Package where controllers are placed. Default: ~.web",
                    specifiedDefaultValue = "~.web",
                    unspecifiedDefaultValue = "~.web") String controllerPackage) {
        return operations.setup(entityPackage, controllerPackage);
    }
}