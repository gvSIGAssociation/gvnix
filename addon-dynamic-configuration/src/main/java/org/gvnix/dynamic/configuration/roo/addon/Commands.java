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
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Dynamic configuration console commands.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class Commands implements CommandMarker {
	
	private static Logger logger = Logger.getLogger(Commands.class.getName());

	@Reference private Operations operations;
	
  @CliAvailabilityIndicator("configuration list")
  public boolean isList() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value = "configuration list", help = "List all saved dynamic configurations")
  public void list() {
    
    // TODO Upgrade entity model to consider Configuration with components, name and active properties
    Set<String> configs = operations.list();
    
    // Show in console the configurations list
    for (String config : configs) {

      logger.log(Level.INFO, config);
    }
  }
  
  @CliAvailabilityIndicator("configuration save")
  public boolean isSave() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value = "configuration save", help = "Save the current property values of the dynamic configuration files on a named dynamic configuration.")
  public void save(@CliOption(key = "name", mandatory = true, help = "Name to store as the dynamic configuration") String name) {

    // Save current dynamic configuration and get it
    Set<DynConfiguration> configs = operations.save(name);
    if (configs.isEmpty() || configs.size() == 0) {

      logger.log(Level.SEVERE, "There is no dynamic configurations to save");
      return;
    }

    // Show in console the dynamic configuration components ant their properties
    for (DynConfiguration config : configs) {

      // Show the component name
      logger.log(Level.INFO, config.toString());
    }
  }

  @CliAvailabilityIndicator("configuration activate")
  public boolean isActivate() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value="configuration activate", help="Set the named dynamic configuration property values on the dynamic configuration files.")
  public void activate(@CliOption(key = "name", mandatory = true, help = "Name to store as the dynamic configuration") String name) {
    
    Set<DynConfiguration> configs = operations.activate(name);
    
    if (configs == null) {
      
      logger.log(Level.WARNING, "Dynamic configuration not exists with name " + name);
      return;
    }
    
    if (configs.isEmpty() || configs.size() == 0) {
      
      logger.log(Level.SEVERE, "Dynamic configuration is empty, not activated");
      return;
    }
    
    // Show in console the dynamic configuration components ant their properties
    for (DynConfiguration config : configs) {

      // Show the component name
      logger.log(Level.INFO, config.toString());
    }
  }
	
}
