package org.gvnix.addon.monitoring;

import java.io.File;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.shell.*;

/**
 * @author Jose Luis Adelantado
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

    /**
     * This method is a command declaration to setup monitoring on gvNIX project
     */
    @CliCommand(value = "monitoring setup", help = "Setup Monitoring on gvNIX project")
    public void setup(
            @CliOption(key = "path", mandatory = false, help = "Set the storage directory for JavaMelody data files") final File path) {
        // Checking that path is not null. If null, installs on default /tmp
        // folder
        if (path != null) {
            operations.setup(path.getAbsolutePath());
        }
        else {
            operations.setup(null);
        }
    }
}