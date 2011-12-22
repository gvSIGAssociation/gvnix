package org.gvnix.web.mvc.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.jsp.JspOperations;
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
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class WebMvcCommands implements CommandMarker { // All command types must
                                                       // implement the
                                                       // CommandMarker
                                                       // interface

    /**
     * Get a reference to the WebMvcOperations from the underlying OSGi
     * container
     */
    @Reference
    private WebMvcOperations operations;
    @Reference
    private JspOperations jspOperations;

    /**
     * Says when the commands are available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "gvnix web mvc add", "gvnix web mvc all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * Setup command is available if we are in a gvNIX project and it was not
     * setup before
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "gvnix web mvc setup" })
    public boolean isSetupAvailable() {
        return jspOperations.isSetupAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "gvnix web mvc add", help = "Some helpful description")
    public void add(
            @CliOption(key = "type", mandatory = true, help = "The java type to apply this annotation to") JavaType target) {
        operations.annotateType(target);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     * 
     */
    @CliCommand(value = "gvnix web mvc all", help = "Some helpful description")
    public void all() {
        operations.annotateAll();
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     * 
     */
    @CliCommand(value = "gvnix web mvc setup", help = "Setup a basic project structure for a Spring MVC / JSP application")
    public void setup() {
        jspOperations.installCommonViewArtefacts();
        // operations.setup();
    }
}