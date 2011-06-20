/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.screen.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Command Class for <code>web mvc screen *</code> commands
 * 
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 * @since 0.8
 */
@Component
@Service
public class WebScreenCommands implements CommandMarker {
    /**
     * Get a reference to the WebScreenOperations from the underlying OSGi
     * container
     */
    @Reference
    private WebScreenOperations operations;

    /**
     * Informs if <code>web mvc pattern *</code> command are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc pattern" })
    public boolean isWebPatternAvaliable() {
        return operations.isPatternCommandAvailable();
    }

    /**
     * Adds a pattern to a web MVC controller
     * 
     * @param controllerClass
     *            The controller to apply the pattern to
     * @param name
     *            Identification to use for this pattern
     * @param type
     *            The pattern to apply
     */
    @CliCommand(value = "web mvc pattern", help = "Add a screen pattern to a controller")
    public void webScreenAdd(
            @CliOption(key = "class", mandatory = true, help = "The controller to apply the pattern to") JavaType controllerClass,
            @CliOption(key = "name", mandatory = true, help = "Identificication to use for this pattern") JavaSymbolName name,
            @CliOption(key = "type", mandatory = true, help = "The pattern to apply") WebPattern type) {
        operations.addPattern(controllerClass, name, type);
    }

    /**
     * Informs if <code>web mvc relation pattern</code> command are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc relation pattern" })
    public boolean isWebRelationPatternAvaliable() {
        return operations.isRelationPatternCommandAvailable();
    }

    /**
     * Adds a pattern to a web MVC controller filed
     * 
     * @param controllerClass
     *            The controller to apply the pattern to
     * @param name
     *            Identification to use for this pattern
     * @param field
     *            The one-to-may field
     * @param type
     *            The pattern to apply
     */
    @CliCommand(value = "web mvc relation pattern", help = "Add a screen pattern to a controller")
    public void webRelationPattern(
            @CliOption(key = "class", mandatory = true, help = "The controller to apply the pattern to") JavaType controllerClass,
            @CliOption(key = "name", mandatory = true, help = "Identificication to use for this pattern") JavaSymbolName name,
            @CliOption(key = "field", mandatory = true, help = "The one-to-many field to apply the pattern to") JavaSymbolName field,
            @CliOption(key = "type", mandatory = true, help = "The pattern to apply") WebPattern type) {
        operations.addRelationPattern(controllerClass, name, field, type);
    }
}
