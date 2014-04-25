package org.gvnix.addon.loupefield;

import static org.springframework.roo.shell.OptionContexts.PROJECT;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.ControllerCommands;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

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
public class LoupefieldCommands implements CommandMarker { // All command types
                                                           // must implement
                                                           // the
                                                           // CommandMarker
                                                           // interface

    @Reference
    private LoupefieldOperations operations;

    @Reference
    private TypeLocationService typeLocationService;

    private static Logger LOGGER = HandlerUtils
            .getLogger(LoupefieldCommands.class);

    /**
     * Check if setup is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc loupe setup" })
    public boolean isSetupAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * Check if update is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc loupe update" })
    public boolean isUpdatetAvailable() {
        return operations.isUpdateCommandAvailable();
    }

    /**
     * Check if set is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc loupe set", "web mvc loupe field" })
    public boolean isSetAvailable() {
        return operations.isSetCommandAvailable();
    }

    /**
     * Setup Loupe Field
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupe setup", help = "Setup necessary files to use Loupe Field")
    public void setup() {
        operations.setup();
    }

    /**
     * Update loupe fields
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupe update", help = "Update Loupe tags")
    public void update() {
        operations.update();
    }

    /**
     * Set Loupe field to a Controller
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupe set", help = "Add functionalities to a Controller to use Loupe Element")
    public void set(
            @CliOption(key = { "controller", "" }, mandatory = true, help = "The path and name of the controller object to annotate") final JavaType controller) {
        operations.setLoupeController(controller);
    }

    /**
     * Convert jspx field to loupe element
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupe field", help = "Convert field into Loupefield in jspx view to use loupe element.")
    public void field(
            @CliOption(key = { "controller", "" }, mandatory = true, help = "The path and name of the controller annotated") final JavaType controller,
            @CliOption(key = { "field", "" }, mandatory = true, help = "The field to convert into loupe element") final JavaSymbolName field,
            @CliOption(key = { "additionalFields", "" }, mandatory = false, help = "Additional controller fields to use in loupe search (Separated by commas)") final String additionalFields,
            @CliOption(key = { "caption", "" }, mandatory = false, help = "Caption to show when select an item. If not set uses ConversionService") final String caption,
            @CliOption(key = { "baseFilter", "" }, mandatory = false, help = "Base Filter to default loupe filtering") final String baseFilter,
            @CliOption(key = { "listPath", "" }, mandatory = false, help = "View to use in Selector Dialog. By default uses controllerpath/list.jspx") final String listPath,
            @CliOption(key = { "max", "" }, mandatory = false, help = "Max results to show in DropDown List when search. By Default 3 elements displayed") final String max) {
        operations.setLoupeField(controller, field, additionalFields, caption,
                baseFilter, listPath, max);
    }

}