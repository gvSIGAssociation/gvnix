package org.gvnix.addon.field.geo;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
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
public class FieldGeoCommands implements CommandMarker {

    /**
     * Get a reference to the GeoOperations from the underlying OSGi container
     */
    @Reference
    private FieldGeoOperations operations;

    /**
     * This method checks if the method to add new field is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("field geo")
    public boolean isFieldCommandAvailable() {
        return operations.isFieldCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "field geo", help = "Add GEO field to selected Entity")
    public void setup() {
        operations.addField();
    }

}