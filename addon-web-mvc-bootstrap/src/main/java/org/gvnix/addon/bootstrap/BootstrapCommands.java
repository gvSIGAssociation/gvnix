package org.gvnix.addon.bootstrap;

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
public class BootstrapCommands implements CommandMarker {

    /**
     * Get a reference to the BootstrapOperations from the underlying OSGi
     * container
     */
    @Reference
    private BootstrapOperations operations;

    /**
     * This method is optional. It allows automatic command hiding in situations
     * when the command should not be visible. For example the 'entity' command
     * will not be made available before the user has defined his persistence
     * settings in the Roo shell or directly in the project.
     * 
     * You can define multiple methods annotated with
     * {@link CliAvailabilityIndicator} if your commands have differing
     * visibility requirements.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc bootstrap setup" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc bootstrap setup", help = "Setup Bootstrap3 in your project")
    public void setup() {
        operations.setup();
    }

}