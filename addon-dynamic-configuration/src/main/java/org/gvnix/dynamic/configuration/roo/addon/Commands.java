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
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Dynamic configuration console commands.
 * <ul>
 * <li>TODO No empty modules information should will appear on console</li>
 * </ul>
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

    @Reference
    private Operations operations;

    @CliAvailabilityIndicator("configuration save")
    public boolean isSave() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration save", help = "Create a new configuration with a name")
    public void save(
            @CliOption(key = "name", mandatory = true, help = "Name of the configuration to be created") String name) {

        // Store the active dynamic configuration
        DynConfiguration dynConf = operations.saveActiveConfiguration(name);
        logger.log(Level.INFO, "Configuration saved");
        showDynComponents(dynConf);
        logger.log(
                Level.INFO,
                "(use 'configuration property list' to see properties defined in the configuration or 'configuration activate' to update project files with configuration properties)");
    }

    @CliAvailabilityIndicator("configuration activate")
    public boolean isActivate() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration activate", help = "Update project files with the configuration property values")
    public void activate(
            @CliOption(key = "name", mandatory = true, help = "Name of the configuration to be activated") DynConfiguration name) {

        // Set the selected dynamic configuration as active
        DynConfiguration dynConf = operations.setActiveConfiguration(name
                .getName());

        // If null, dynamic configuration with this name not exists
        if (dynConf == null) {

            logger.log(Level.WARNING, "Configuration not exists");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration list' to see saved configurations or 'configuration save' to create a new configuration)");
        } else {

            if (dynConf.isActive()) {

                // Show the dynamic configuration
                logger.log(Level.INFO,
                        "Configuration activated on project files");
                showDynComponents(dynConf);
                logger.log(
                        Level.INFO,
                        "(now 'configuration property update' will modify project files too, use 'configuration unactivate' to disable)");
            } else {

                logger.log(
                        Level.SEVERE,
                        "Configuration not activated."
                                + "\nPrevious active configuration has unsaved modifications, then you can:"
                                + "\n * Undo project files modifications"
                                + "\n * Update previous active configuration properties with 'configuration property update'"
                                + "\n * Save previous active configuration with 'configuration save'"
                                + "\n * Delete previous active configuration with 'configuration delete'");
            }
        }
    }

    @CliAvailabilityIndicator("configuration unactivate")
    public boolean isUnactivate() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration unactivate", help = "Unlink the project files from the active configuration")
    public void unactivate(
            @CliOption(key = "name", mandatory = true, help = "Name of the configuration to be unactivated") DynConfiguration name) {

        // Set the selected dynamic configuration as active
        DynConfiguration dynConf = operations.setUnactiveConfiguration(name
                .getName());

        // If null, dynamic configuration with this name not exists
        if (dynConf == null) {

            logger.log(Level.WARNING, "Configuration not exists");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration list' to see saved configurations or 'configuration save' to create a new configuration)");
        } else {

            if (dynConf.isActive()) {

                logger.log(
                        Level.SEVERE,
                        "Configuration not unactivated."
                                + "\nActive configuration has unsaved modifications, then you can:"
                                + "\n * Undo project files modifications"
                                + "\n * Update active configuration properties with 'configuration property update'"
                                + "\n * Save active configuration with 'configuration save'"
                                + "\n * Delete active configuration with 'configuration delete'");
            } else {

                logger.log(Level.INFO, "Configuration unactivated");
                logger.log(
                        Level.INFO,
                        "(now 'configuration property update' will not modify project files, only configurations)");
            }
        }
    }

    @CliAvailabilityIndicator("configuration list")
    public boolean isList() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration list", help = "List all previously saved configurations")
    public void list() {

        DynConfigurationList dynConfs = operations.findConfigurations();

        // If null, dynamic configuration with this name not exists
        if (dynConfs == null || dynConfs.isEmpty()) {

            logger.log(Level.WARNING, "There is no saved configurations");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration save' to create a new configuration or 'configuration property list' to see properties defined)");
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

    @CliCommand(value = "configuration delete", help = "Remove a previously saved configuration")
    public void delete(
            @CliOption(key = "name", mandatory = true, help = "Name of the configuration to be removed") DynConfiguration name) {

        // Store the active dynamic configuration
        boolean deleted = operations.deleteConfiguration(name.getName());

        // Show message
        if (deleted) {
            logger.log(Level.INFO, "Configuration deleted");
            logger.log(
                    Level.INFO,
                    "(use 'configuration list' to see remaining configurations or 'configuration save' to create a new one)");
        } else {
            logger.log(Level.WARNING, "Configuration not exists");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration list' to see saved configurations or 'configuration save' to create a new configuration)");
        }
    }

    @CliAvailabilityIndicator("configuration property list")
    public boolean isPropertyList() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration property list", help = "Show the properties stored in a configuration")
    public void propertyList(
            @CliOption(key = "name", mandatory = false, help = "Name of the configuration to list properties") DynConfiguration name) {

        // If name specified get this configuration, else base configuration
        DynConfiguration dynConf;
        if (name != null) {

            dynConf = operations.getConfiguration(name.getName());
        } else {

            dynConf = operations.getBaseConfiguration();
        }

        // If null, dynamic configuration with this name not exists
        if (dynConf == null) {

            logger.log(Level.WARNING, "Configuration not exists");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration list' to see saved configurations or 'configuration save' to create a new configuration)");
            return;
        }

        if (dynConf.getComponents().isEmpty()) {

            logger.log(Level.WARNING, "There is no added properties");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property add' to make available a configuration property or 'configuration save' to create a new configuration)");
        }

        // Show the dynamic configuration
        showDynComponents(dynConf);
    }

    @CliAvailabilityIndicator("configuration property values")
    public boolean isPropertyValues() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration property values", help = "Show distinct property values along configurations")
    public void propertyValues(
            @CliOption(key = "name", mandatory = true, help = "Property name", optionContext = DynPropertyConverter.CONFIGURATION_FILE) DynProperty name) {

        // Get the dynamic configuration
        DynConfigurationList dynConfs = operations.getProperties(name.getKey());

        // If null, property with this name not exists
        if (dynConfs == null || dynConfs.size() == 0) {

            logger.log(Level.WARNING, "Property has no values");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property add' to make available a configuration property or 'configuration property update' to set property new values)");
            return;
        }

        for (DynConfiguration dynConf : dynConfs) {

            logger.log(Level.INFO, dynConf.toString());
            for (DynComponent dynComp : dynConf.getComponents()) {

                logger.log(Level.INFO, dynComp.toString());
            }
        }
    }

    @CliAvailabilityIndicator("configuration property update")
    public boolean isPropertyUpdate() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration property update", help = "Set a value in a configuration property")
    public void propertyUpdate(
            @CliOption(key = "configuration", mandatory = true, help = "Name of the configuration to update") DynConfiguration configuration,
            @CliOption(key = "property", mandatory = true, help = "Name of the property to update", optionContext = DynPropertyConverter.CONFIGURATION_FILE) DynProperty property,
            @CliOption(key = "value", mandatory = true, help = "Value to set") String value) {

        // Update the configuration
        DynProperty dynProperty = operations.updateProperty(
                configuration.getName(), property.getKey(), value);

        // If null, property with this name not exists
        if (dynProperty == null) {

            logger.log(Level.WARNING, "Property not exists on configuration");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property add' to make available a configuration property or 'configuration save' to create a new configuration)");
            return;
        }

        logger.log(Level.INFO, "Property updated");
        logger.log(
                Level.INFO,
                "(use 'configuration activate' to update project files with configuration properties if not active yet or 'configuration property values' to show property values)");
    }

    @CliAvailabilityIndicator("configuration property add")
    public boolean isPropertyAdd() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration property add", help = "Make available a property in the configurations")
    public void propertyAdd(
            @CliOption(key = "name", mandatory = true, help = "Name of the property to add", optionContext = DynPropertyConverter.SOURCE_FILES) DynProperty name) {

        // Add the property and show a message
        if (operations.addProperty(name.getKey())) {

            logger.log(Level.INFO, "Property added");
            logger.log(
                    Level.INFO,
                    "(use 'configuration property update' to set property new values or 'configuration save' to create a new configuration)");
        } else {

            logger.log(Level.WARNING, "Property already exists");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property update' to set property new values or 'configuration property delete' do remove it)");
        }
    }

    @CliAvailabilityIndicator("configuration property delete")
    public boolean isPropertyDelete() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration property delete", help = "Remove a property from all configurations")
    public void propertyDelete(
            @CliOption(key = "name", mandatory = true, help = "Name of the property to delete", optionContext = DynPropertyConverter.CONFIGURATION_FILE) DynProperty name) {

        // Delete the property and show a message
        if (operations.deleteProperty(name.getKey())) {

            logger.log(Level.INFO, "Property deleted");
        } else {

            logger.log(Level.WARNING, "Property not exists");
            logger.log(Level.WARNING,
                    "(use 'configuration property list' to see properties)");
        }
    }

    @CliAvailabilityIndicator("configuration export")
    public boolean isExport() {

        return operations.isProjectAvailable();
    }

    // TODO Add a parameter with the target build tool (mvn, ant, ...)
    // TODO Add a parameter with the configuration to export ?
    // XXX Currently, only mvn build tool available.
    @CliCommand(value = "configuration export", help = "Write saved configurations into the build tool")
    public void export() {

        // Write all dynamic configurations into the build tool
        // TODO Some return value ?
        DynConfigurationList dynConfs = operations.export();

        // If null, dynamic configuration with this name not exists
        if (dynConfs == null) {

            logger.log(Level.WARNING, "There is no saved configurations");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration save' to create a new configuration or 'configuration property list' to see properties defined)");
            return;

        } else if (dynConfs.isEmpty()) {

            logger.log(Level.WARNING, "There is no activated configuration");
            logger.log(Level.WARNING,
                    "(use 'configuration activate' to define the default configuration)");
            return;

        } else {

            logger.log(Level.INFO, "Configuration exported");
            for (DynConfiguration dynConf : dynConfs) {

                logger.log(Level.INFO, dynConf.toString());
                showDynComponents(dynConf);

            }
            logger.log(Level.INFO,
                    "(use '-P name' on maven commands to use the 'name' configuration)");
        }

    }

    /**
     * Show the components of a dynamic configuration on the console.
     * 
     * @param dynConf
     *            Dynamic configuration to show
     */
    private void showDynComponents(DynConfiguration dynConf) {

        for (DynComponent dynComp : dynConf.getComponents()) {

            logger.log(Level.INFO, dynComp.toString());
        }
    }

}
