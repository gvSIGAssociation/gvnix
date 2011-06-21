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

    @CliAvailabilityIndicator("configuration create")
    public boolean isCreate() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration create", help = "Save a new configuration with a name")
    public void create(
            @CliOption(key = "name", mandatory = true, help = "Name of the configuration to be saved") String name) {

        DynConfigurationList dynConfs = operations.findConfigurations();
        boolean isFirstDynConf = dynConfs.isEmpty();

        // Store the active dynamic configuration
        DynConfiguration dynConf = operations.saveActiveConfiguration(name);
        logger.log(Level.INFO, "configuration created");
        if (isFirstDynConf) {
            operations.setActiveConfiguration(name);
            logger.log(Level.INFO, "First created configuration set as default");
        }
        showDynComponents(dynConf);
        logger.log(
                Level.INFO,
                "(use 'configuration list' to see properties defined in the configuration or 'configuration export' to generate the configuration at project)");
    }

    @CliAvailabilityIndicator("configuration list")
    public boolean isList() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration list", help = "List all previously created configurations")
    public void list() {

        // If name specified get this configuration, else base configuration
        DynConfigurationList dynConfs = operations.findConfigurations();

        // If empty, no dynamic configuration exists
        if (dynConfs.isEmpty()) {

            logger.log(Level.WARNING, "There is not created configurations");
            logger.log(Level.WARNING,
                    "(use 'configuration create' to save a new configuration)");
            return;
        }

        // Show in console the configurations list
        showDynConfigurations(dynConfs);
        logger.log(
                Level.INFO,
                "(use 'configuration property add' to include a configuration property or 'configuration property value' to set new values to property)");
    }

    @CliAvailabilityIndicator("configuration property value")
    public boolean isPropertyValue() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration property value", help = "Set a value in a configuration property")
    public void propertyValue(
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
                    "(use 'configuration property add' to include a configuration property or 'configuration create' to save a new configuration)");
            return;
        }

        logger.log(Level.INFO, "Property value updated");
        logger.log(Level.INFO, "(use 'configuration list' to show properties)");
    }

    @CliAvailabilityIndicator("configuration property add")
    public boolean isPropertyAdd() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration property add", help = "Include a property in the configurations")
    public void propertyAdd(
            @CliOption(key = "name", mandatory = true, help = "Name of the property to add", optionContext = DynPropertyConverter.SOURCE_FILES) DynProperty name) {

        // Add the property and show a message
        Boolean added = operations.addProperty(name.getKey());
        if (added == null) {

            logger.log(Level.WARNING, "Property not exists");
            logger.log(Level.WARNING,
                    "(use 'configuration list' to see properties defined)");
        } else if (added == true) {

            logger.log(Level.INFO, "Property added");
            logger.log(
                    Level.INFO,
                    "(use 'configuration property value' to set new values to property or 'configuration create' to save a new configuration)");
        } else {

            logger.log(Level.WARNING, "Property already exists");
            logger.log(Level.WARNING,
                    "(use 'configuration property value' to set new values to property)");
        }
    }

    @CliAvailabilityIndicator("configuration export")
    public boolean isExport() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration export", help = "Write created configurations into the build tool")
    public void export() {

        // Write all dynamic configurations into the build tool
        DynConfigurationList dynConfs = operations.export();

        if (dynConfs.isEmpty()) {

            logger.log(Level.WARNING, "There is no created configurations");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration create' to create a new configuration or 'configuration list' to see properties defined)");
            return;

        } else {

            logger.log(Level.INFO, "Configuration exported");
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

    /**
     * Show the configurations of a dynamic configuration list on the console.
     * 
     * @param dynConfs
     *            Dynamic configuration to show
     */
    private void showDynConfigurations(DynConfigurationList dynConfs) {

        for (DynConfiguration dynConf : dynConfs) {

            logger.log(Level.INFO, dynConf.toString());
            for (DynComponent dynComp : dynConf.getComponents()) {

                logger.log(Level.INFO, dynComp.toString());
            }
        }
    }

}
