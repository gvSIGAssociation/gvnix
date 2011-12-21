package org.gvnix.weblayer.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Web Layer add-on command class
 * 
 * The command class is registered by the Roo shell following an automatic
 * classpath scan.
 * 
 * @since 0.9
 */
@Component
@Service
public class WebLayerCommands implements CommandMarker {

    /**
     * Get a reference to the WebLayerOperations from the underlying OSGi
     * container
     */
    @Reference
    private WebLayerOperations operations;

    /**
     * It allows automatic command hiding in situations when the command should
     * not be visible. For example the 'entity' command will not be made
     * available before the user has defined his persistence settings in the Roo
     * shell or directly in the project.
     * 
     * You can define multiple methods annotated with
     * {@link CliAvailabilityIndicator} if your commands have differing
     * visibility requirements.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web layer setup", "web layer add",
            "web layer all", "web layer show" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web layer add", help = "Some helpful description")
    public void add(
            @CliOption(key = "type", mandatory = true, help = "The java type to apply this annotation to") JavaType target) {
        operations.annotateType(target);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     * 
     */
    @CliCommand(value = "web layer all", help = "Some helpful description")
    public void all() {
        operations.annotateAll();
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     * 
     */
    @CliCommand(value = "web layer setup", help = "Setup Web Layer addon")
    public void setup() {
        operations.setup();
    }

    @CliCommand(value = "web layer show", help = "Define Web Layer View Show for the given Entity class")
    public void scaffoldshow(
            @CliOption(key = "class", mandatory = true, help = "The entity to define the web layer view show") JavaType entity,
            @CliOption(key = "package", mandatory = true, help = "The package in which the web layer definitions will be created") JavaPackage viewPackage) {
        operations.scaffoldShow(entity, viewPackage);
    }
}