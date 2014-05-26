/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
import org.springframework.roo.model.JavaSymbolName;
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

    private static final Logger logger = Logger.getLogger(Commands.class
            .getName());

    @Reference
    private Operations operations;

    @CliAvailabilityIndicator("configuration create")
    public boolean isCreate() {

        return operations.isProjectAvailable();
    }

    @CliCommand(value = "configuration create", help = "Define a new configuration with a name")
    public void create(
            @CliOption(key = "name", mandatory = true, help = "Name for defined configuration") JavaSymbolName name) {

        // There is no previous dynamic configurations created ?
        DynConfigurationList dynConfs = operations.findConfigurations();
        boolean isFirstDynConf = dynConfs.isEmpty();

        // Store the active dynamic configuration
        DynConfiguration dynConf = operations.saveActiveConfiguration(name
                .getSymbolName());
        logger.log(Level.INFO,
                "Configuration created with currently available properties");

        // If first dynamic configuration, set as default
        if (isFirstDynConf) {
            operations.setActiveConfiguration(name.getSymbolName());
            logger.log(Level.INFO, "First created configuration set as default");
        }

        // Show in console the added configuration
        showDynComponents(dynConf);
        logger.log(
                Level.INFO,
                "(use 'configuration property add' to make a property available for all configurations)");
    }

    @CliAvailabilityIndicator("configuration property add")
    public boolean isPropertyAdd() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration property add", help = "Make a property available for all configurations")
    public void propertyAdd(
            @CliOption(key = "name", mandatory = true, help = "Name of property to add", optionContext = DynPropertyConverter.SOURCE_FILES) DynProperty name) {

        // Add the property and show a message
        Boolean added = operations.addProperty(name.getKey());
        if (added == null) {

            logger.log(Level.WARNING, "Property not exists");

        }
        else if (added == true) {

            logger.log(Level.INFO, "Property available for all configurations");
            logger.log(Level.INFO,
                    "(use 'configuration property value' to set property new values)");
            logger.log(Level.INFO,
                    "(use 'configuration property undefined' to set property with no values)");

        }
        else {

            logger.log(Level.WARNING,
                    "Property already available for configurations");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property value' to set property new values by configuration)");
        }
    }

    @CliAvailabilityIndicator("configuration property value")
    public boolean isPropertyValue() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration property value", help = "Set new value into a configuration property")
    public void propertyValue(
            @CliOption(key = "configuration", mandatory = true, help = "Name of configuration to update") DynConfiguration configuration,
            @CliOption(key = "property", mandatory = true, help = "Name of property to update", optionContext = DynPropertyConverter.CONFIGURATION_FILE) DynProperty property,
            @CliOption(key = "value", mandatory = true, help = "New value to set") String value) {

        // Update the configuration
        DynProperty dynProperty = operations.updateProperty(
                configuration.getName(), property.getKey(), value);

        // If null, property with this name not exists
        if (dynProperty == null) {

            logger.log(Level.WARNING,
                    "Property not available for configurations");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property add' to make a property available for configurations)");
            return;
        }

        logger.log(Level.INFO, "Property value seted");
        logger.log(Level.INFO,
                "(use 'configuration list' to show configurations and their properties)");
    }

    @CliAvailabilityIndicator("configuration property undefined")
    public boolean isPropertyUndefined() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration property undefined", help = "Set no value into a configuration property")
    public void propertyUndefined(
            @CliOption(key = "configuration", mandatory = true, help = "Name of configuration to update") DynConfiguration configuration,
            @CliOption(key = "property", mandatory = true, help = "Name of property to update", optionContext = DynPropertyConverter.CONFIGURATION_FILE) DynProperty property) {

        // Update the configuration
        DynProperty dynProperty = operations.updateProperty(
                configuration.getName(), property.getKey());

        // If null, property with this name not exists
        if (dynProperty == null) {

            logger.log(Level.WARNING,
                    "Property not available for configurations");
            logger.log(
                    Level.WARNING,
                    "(use 'configuration property add' to make a property available for configurations)");
            return;
        }

        logger.log(Level.INFO, "Property value undefined");
        logger.log(Level.INFO,
                "(use '-D propname=propvalue' on maven commands to set the property value)");
        logger.log(Level.INFO,
                "(use 'configuration list' to show configurations and their properties)");
    }

    @CliAvailabilityIndicator("configuration list")
    public boolean isList() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration list", help = "List all created configurations and their properties")
    public void list() {

        // If name specified get this configuration, else base configuration
        DynConfigurationList dynConfs = operations.findConfigurations();

        // Show in console the configurations list
        showDynConfigurations(dynConfs);
        logger.log(Level.INFO,
                "(use 'configuration export' to write configurations into the project)");
    }

    @CliAvailabilityIndicator("configuration export")
    public boolean isExport() {

        return operations.isProjectAvailable()
                && !operations.findConfigurations().isEmpty();
    }

    @CliCommand(value = "configuration export", help = "Write current configurations into project")
    public void export() {

        // Write all dynamic configurations into the build tool
        operations.export();

        logger.log(Level.INFO, "Configurations exported into project");
        logger.log(Level.INFO,
                "(use '-P configname' on maven commands to use a configuration)");
        logger.log(Level.INFO,
                "(use 'configuration create' to define a new configuration)");
    }

    /**
     * Show the configurations of a dynamic configuration list on the console.
     * 
     * @param dynConfs Dynamic configuration to show
     */
    private void showDynConfigurations(DynConfigurationList dynConfs) {

        for (DynConfiguration dynConf : dynConfs) {

            logger.log(Level.INFO, dynConf.toString());
            showDynComponents(dynConf);
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
