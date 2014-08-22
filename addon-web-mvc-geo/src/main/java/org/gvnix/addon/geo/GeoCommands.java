package org.gvnix.addon.geo;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell
 * following an automatic classpath scan. You can provide simple user
 * presentation-related logic in this class. You can return any objects from
 * each method, or use the logger directly if you'd like to emit messages of
 * different severity (and therefore different colours on non-Windows systems).
 * 
 * @since 1.1
 */
@Component
@Service
public class GeoCommands implements CommandMarker {

    /**
     * Get a reference to the GeoOperations from the underlying OSGi container
     */
    @Reference
    private GeoOperations operations;

    @Reference
    private TypeLocationService typeLocationService;

    /**
     * This method checks if the setup method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo setup")
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * This method checks if the map method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo map")
    public boolean isMapCommandAvailable() {
        return operations.isMapCommandAvailable();
    }

    /**
     * This method checks if web mvc geo all method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo all")
    public boolean isAllCommandAvailable() {
        return operations.isAllCommandAvailable();
    }

    /**
     * This method checks if web mvc geo add method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo add")
    public boolean isAddCommandAvailable() {
        return operations.isAddCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo setup", help = "Setup GEO components in your project.")
    public void setup() {
        operations.setup();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo map", help = "Add new Map view to your project")
    public void addMap(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the new Map Controller") final JavaType controller,
            @CliOption(key = "path", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "Path to access to the new controller operations") final JavaSymbolName path) {
        operations.addMap(controller, path);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo all", help = "Run this method to include all GEO entities on specific map or on all available maps")
    public void all(
            @CliOption(key = "path", mandatory = false, help = "If blank, adds all GEO entities to all available maps. Indicate a list of paths separated by commas if you want to include all entities in some specific maps.") final JavaSymbolName path) {
        operations.all(path);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo add", help = "Run this method to include specific GEO entity on all maps or specific map")
    public void add(
            @CliOption(key = "controller", mandatory = true, help = "Indicates which entity controller you want to add to map") final JavaType controller,
            @CliOption(key = "path", mandatory = false, help = "If blank, adds all GEO entities to all available maps. Indicate a list of paths separated by commas if you want to include all entities in some specific maps.") final JavaSymbolName path) {
        operations.add(controller, path);
    }

}