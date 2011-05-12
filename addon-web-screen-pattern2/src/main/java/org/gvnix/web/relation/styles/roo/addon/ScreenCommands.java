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
package org.gvnix.web.relation.styles.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related
 * logic in this class. You can return any objects from each method, or use the
 * logger directly if you'd like to emit messages of different severity (and
 * therefore different colours on non-Windows systems).
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
@Component
@Service
public class ScreenCommands implements CommandMarker {

    @Reference private ScreenOperations operations;

    /**
     * Automatic {@code web screen} command hiding in situations when the
     * command should not be visible. Command will not be made available before
     * the user has defined his Spring MVC settings in the Roo shell or directly
     * in the project.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web screen" })
    public boolean isCommandAvailable() {
	return operations.isProjectAvailable()
		&& operations.isSpringMvcProject()
		&& !operations.isActivated();
    }

    @CliCommand(value = "web screen", help = "Set the screen visualization for related entities.")
    public void setPattern(
	    @CliOption(key = "pattern", mandatory = false, help = "Screen pattern ID to apply. Default use 'MASTER-RECORD-DETAIL-TABLE' pattern.") String patId,
	    @CliOption(key = "class", mandatory = false, help = "Target entity to apply the screen pattern. Default apply to all entities.") JavaPackage entityName) {
	operations.setup();
    }

}