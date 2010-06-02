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
package org.gvnix.cxf.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Addon for CXF support
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com )
 * @author Enrique Ruiz ( eruiz at disid dot com )
 * @since 1.1
 */
@Component
@Service
public class CxfCommands implements CommandMarker {

    private static Logger logger = Logger
	    .getLogger(CxfCommands.class.getName());

    @Reference
    private CxfOperations operations;

    @CliAvailabilityIndicator("cxf setup")
    public boolean isProjectAvailable() {
	return operations.isProjectAvailable() && !operations.isCxfInstalled();
    }

    @CliAvailabilityIndicator( { "cxf service", "cxf operation",
	    "cxf operation param" })
    public boolean isCxfInstalled() {
	return operations.isCxfInstalled();
    }

    @CliCommand(value = "cxf setup", help = "Setup the support for CXF library into the project.")
    public void cxfInstall() {
	operations.setupCxf();
    }

    @CliCommand(value = "cxf service", help = "Creates a new Service in SRC_MAIN_JAVA")
    public void newService(
	    @CliOption(key = "service", optionContext = "update,project", mandatory = true, help = "Name of the service to create") JavaType ifaceName,
	    @CliOption(key = "path", mandatory = false, unspecifiedDefaultValue = "SRC_MAIN_JAVA", specifiedDefaultValue = "SRC_MAIN_JAVA", help = "Source directory to create the interface in") Path path) {
	operations.newService(ifaceName, path);
    }

    @CliCommand(value = "cxf operation", help = "Adds a new Operation to existing Service")
    public void addOperation(
	    @CliOption(key = { "", "name" }, mandatory = true, help = "The name of the operation to add") JavaSymbolName opeName,
	    @CliOption(key = "return", mandatory = false, unspecifiedDefaultValue = "__NULL__", help = "The Java type this operation returns") JavaType returnType,
	    @CliOption(key = "params", mandatory = false, help = "The parameters of the operation") String paramNames,
	    @CliOption(key = "types", mandatory = false, help = "The Java types of the given parameters") String paramTypes,
	    @CliOption(key = "service", mandatory = true, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the service to receive this field") JavaType ifaceType) {
	operations.addServiceOperation(opeName, returnType, paramNames,
		paramTypes, ifaceType);
    }

}