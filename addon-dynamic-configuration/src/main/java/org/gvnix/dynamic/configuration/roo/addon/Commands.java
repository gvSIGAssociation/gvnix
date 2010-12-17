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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Dynamic configuration console commands.
 * 
 * TODO Add an editor to list configuration names on commands. 
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
  
  @CliAvailabilityIndicator("configuration save")
  public boolean isSave() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value = "configuration save", help = "Store the files current properties as a configuration")
  public void save(@CliOption(key = "name", mandatory = true, help = "Configuration name to store") String name) {

    // Store the active dynamic configuration
    DynConfiguration dynConf = operations.saveActiveConfiguration(name);
    
    // Show the stored dynamic configuration
    showDynComponents(dynConf);
  }

  @CliAvailabilityIndicator("configuration activate")
  public boolean isActivate() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value="configuration activate", help="Update the files with the configuration properties")
  public void activate(@CliOption(key = "name", mandatory = true, help = "Configuration name") String name) {
    
    // Set the selected dynamic configuration as active
    DynConfiguration dynConf = operations.setActiveConfiguration(name);
    
    // If null, dynamic configuration with this name not exists
    if (dynConf == null) {
      
      logger.log(Level.WARNING, name + " configuration not exists");
      return;
    }

    // Show the activated dynamic configuration
    showDynComponents(dynConf);
  }
  
  @CliAvailabilityIndicator("configuration list")
  public boolean isList() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value = "configuration list", help = "List all saved configurations")
  public void list() {
    
    DynConfigurationList dynConfs = operations.findConfigurations();
    
    // If null, dynamic configuration with this name not exists
    if (dynConfs == null || dynConfs.size() == 0) {
      
      logger.log(Level.INFO, "There is no saved configurations");
      return;
    }
    
    // Show in console the configurations list
    for (DynConfiguration dynConf : dynConfs) {

      logger.log(Level.INFO, dynConf.toString());
    }
  }
  
  @CliAvailabilityIndicator("configuration delete")
  public boolean isDelete() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value = "configuration delete", help = "Remove a previously stored configuration")
  public void delete(@CliOption(key = "name", mandatory = true, help = "Configuration name to remove") String name) {

    // Store the active dynamic configuration
    boolean deleted = operations.deleteConfiguration(name);
    
    // Show message
    if (deleted) {
      logger.log(Level.INFO, name + " configuration deleted");
    }
    else {
      logger.log(Level.WARNING, name + " configuration not exists");
    }
  }
  
  @CliAvailabilityIndicator("configuration property list")
  public boolean isPropertyList() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value="configuration property list", help="Show the properties stored in a configuration")
  public void propertyList(@CliOption(key = "name", mandatory = true, help = "Configuration name") String name) {
    
    // Get the dynamic configuration
    DynConfiguration dynConf = operations.getConfiguration(name);
    
    // If null, dynamic configuration with this name not exists
    if (dynConf == null) {
      
      logger.log(Level.WARNING, name + " configuration not exists");
      return;
    }

    // Show the activated dynamic configuration
    showDynComponents(dynConf);
  }
  
  @CliAvailabilityIndicator("configuration property values")
  public boolean isPropertyValues() {
    
    return operations.isProjectAvailable();
  }

  @CliCommand(value="configuration property values", help="Show the property distinct values stored along configurations")
  public void propertyValues(@CliOption(key = "name", mandatory = true, help = "Property name") String name) {
    
    // Get the dynamic configuration
    DynConfigurationList dynConfs = operations.getProperties(name);
    
    // If null, property with this name not exists
    if (dynConfs == null) {
      
      logger.log(Level.WARNING, name + " property not exists");
      return;
    }

    for (DynConfiguration dynConf : dynConfs) {
      
      logger.log(Level.INFO, dynConf.toString());
      for (DynComponent dynComp : dynConf.getComponents()) {
        
        logger.log(Level.INFO, dynComp.toString());
      }
    }
  }

  /**
   * Show the components of a dynamic configuration on the console.
   * 
   * @param dynConf Dynamic configuration to show
   */
  private void showDynComponents(DynConfiguration dynConf) {

    for (DynComponent dynComp : dynConf.getComponents()) {

      logger.log(Level.INFO, dynComp.toString());
    }
  }
	
}
