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
    @CliAvailabilityIndicator({ "web mvc loupefield setup" })
    public boolean isSetupAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * Check if setup is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc loupefield set" })
    public boolean isSetAvailable() {
        return operations.isSetCommandAvailable();
    }

    /**
     * Check if update is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc loupefield update" })
    public boolean isUpdatetAvailable() {
        return operations.isUpdateCommandAvailable();
    }

    /**
     * Setup Loupe Field
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupefield setup", help = "Setup necessary files to use Loupe Field")
    public void setup() {
        operations.setup();
    }

    /**
     * Set Loupe field to an entity property
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupefield set", help = "Set Loupe field to an entity property")
    public void set(
            @CliOption(key = { "class", "" }, mandatory = true, help = "The path and name of the controller object to annotate") final JavaType controller,
            @CliOption(key = "backingType", mandatory = true, optionContext = PROJECT, unspecifiedDefaultValue = "*", help = "The name of the entity") final JavaType backingType,
            @CliOption(key = "field", mandatory = true, help = "The field to apply loupe") JavaSymbolName field) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(backingType);
        if (cid == null) {
            LOGGER.warning("The specified entity can not be resolved to a type in your project");
            return;
        }

        List<? extends FieldMetadata> fieldList = cid.getDeclaredFields();
        Iterator<? extends FieldMetadata> it = fieldList.iterator();
        boolean exists = false;
        while (it.hasNext()) {
            FieldMetadata currentField = it.next();
            if (field.equals(currentField.getFieldName())) {
                exists = true;
            }
        }
        if (!exists) {
            LOGGER.warning("The field '" + field.getSymbolName()
                    + "' can not be resolved as field of your entity");
            return;
        }
        operations.setLoupeFields(controller, backingType, field);
    }

    /**
     * Update loupe fields
     * 
     * @param type
     */
    @CliCommand(value = "web mvc loupefield update", help = "Update Loupe tags")
    public void update() {
        operations.update();
    }

}