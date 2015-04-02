package org.gvnix.addon.monitoring;

import java.io.File;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * @author gvNIX Team
 * @since 1.4.0
 */
@Component
@Service
public class MonitoringCommands implements CommandMarker {

    @Reference
    private MonitoringOperations operations;

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
    @CliCommand(value = "monitoring setup", help = "Setup Monitoring on gvNIX project")
    public void setup(
            @CliOption(key = "path", mandatory = false, help = "Set the storage directory for JavaMelody data files") final File path) {
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
    @CliCommand(value = "monitoring all", help = "Add all files to be monitored as a Spring service")
    public void all() {
        operations.all();
    }

    /**
     * This method is a command declaration to add a path which all his child
     * methods will be monitored as a Spring service
     */
    @CliCommand(value = "monitoring add package", help = "Add a path which all his child methods will be monitored as a Spring service")
    public void addPackage(
            @CliOption(key = "path", mandatory = true, help = "Set the package path to be monitored") final JavaPackage path) {
        operations.addPackage(path);
    }

    /**
     * This method is a command declaration to add a class to be monitored as a
     * Spring service
     */
    @CliCommand(value = "monitoring add class", help = "Add a class to be monitored as a Spring service")
    public void addClass(
            @CliOption(key = "name", mandatory = true, help = "Set the class name to be monitored") final JavaType name) {
        operations.addClass(name);
    }

    /**
     * This method is a command declaration to add a method to be monitored as a
     * Spring service
     */
    @CliCommand(value = "monitoring add method", help = "Add a method to be monitored as a Spring service")
    public void addMethod(
            @CliOption(key = "name", mandatory = true, help = "Set the method name to be monitored") final JavaSymbolName methodName,
            @CliOption(key = "class", mandatory = true, help = "Set the class name of the method to be monitored") final JavaType className) {
        operations.addMethod(methodName, className);
    }
}