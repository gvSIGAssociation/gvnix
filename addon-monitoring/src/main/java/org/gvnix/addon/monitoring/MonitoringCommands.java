/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.addon.monitoring;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * 
 * @since 1.4.0
 */
@Component
@Service
public class MonitoringCommands implements CommandMarker {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(MonitoringCommands.class);

    @Reference
    private MonitoringOperations operations;

    private TypeLocationService typeLocationService;

    private BundleContext context;

    /**
     * Update dependencies if is needed
     *
     * @param context the component context
     */
    protected void activate(ComponentContext componentContext) {
        context = componentContext.getBundleContext();
    }

    /**
     * This method checks if this command is available
     *
     * @return boolean
     */
    @CliAvailabilityIndicator({ "monitoring setup" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    @CliAvailabilityIndicator({ "monitoring all", "monitoring add package",
            "monitoring add class", "monitoring add method" })
    public boolean isAddAvailable() {
        return operations.isAddAvailable();
    }

    /**
     * This method is a command declaration to setup monitoring on gvNIX project
     */
    @CliCommand(value = "monitoring setup",
            help = "Setup Monitoring on gvNIX project")
    public void setup(
            @CliOption(key = "path",
                    mandatory = false,
                    help = "Set the storage directory for JavaMelody data files") final File path) {
        if (path != null) {
            operations.setup(path.getAbsolutePath());
        }
        else {
            operations.setup(null);
        }
    }

    /**
     * This method is a command declaration to add all files to be monitored as
     * a Spring service
     */
    @CliCommand(value = "monitoring all",
            help = "Add all files to be monitored as a Spring service")
    public void all() {
        operations.all();
    }

    /**
     * This method is a command declaration to add a path which all his child
     * methods will be monitored as a Spring service
     */
    @CliCommand(value = "monitoring add package",
            help = "Add a path which all his child methods will be monitored as a Spring service")
    public void addPackage(
            @CliOption(key = "path",
                    mandatory = true,
                    help = "Set the package path to be monitored") final JavaPackage path) {
        operations.addPackage(path);
    }

    /**
     * This method is a command declaration to add a class to be monitored as a
     * Spring service
     */
    @CliCommand(value = "monitoring add class",
            help = "Add a class to be monitored as a Spring service")
    public void addClass(@CliOption(key = "name",
            mandatory = true,
            help = "Set the class name to be monitored") final JavaType name) {

        // Getting class details
        ClassOrInterfaceTypeDetails typeDetails = getTypeLocationService()
                .getTypeDetails(name);

        // Getting class @Controller annotations
        AnnotationMetadata annotations = typeDetails
                .getAnnotation(SpringJavaType.CONTROLLER);

        // If class doesn't have @Controller annotations means that is not a
        // valid controller
        if (annotations != null) {
            operations.addClass(name);
        }
        else {
            LOGGER.log(Level.WARNING,
                    "Error. You must to specify a valid class annotated with @Controller ");
        }

    }

    /**
     * This method is a command declaration to add a method to be monitored as a
     * Spring service
     */
    @CliCommand(value = "monitoring add method",
            help = "Add a method to be monitored as a Spring service")
    public void addMethod(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Set the method name to be monitored") final JavaSymbolName methodName,
            @CliOption(key = "class",
                    mandatory = true,
                    help = "Set the class name of the method to be monitored") final JavaType className) {
        operations.addMethod(methodName, className);
    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeLocationService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on MonitoringCommands.");
                return null;
            }
        }
        else {
            return typeLocationService;
        }
    }
}
