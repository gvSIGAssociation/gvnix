package org.gvnix.web.mvc.binding.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * TODO
 */
@Component
@Service
public class WebBinderCommands implements CommandMarker {

    private static Logger logger = Logger.getLogger(WebBinderCommands.class
	    .getName());

    @Reference
    private WebBinderOperations webBinderOperations;

    // SETUP //

    @CliAvailabilityIndicator("web binding setup")
    public boolean isSetupAvailable() {
	return webBinderOperations.isSetupAvailable();
    }

    @CliCommand(value = "web binding setup", help = "Initializes Web default Property editors bindings")
    public void setup(
	    @CliOption(key = "class", mandatory = true, help = "Class to create for binding intialization") JavaType initializerClass,
	    @CliOption(key = "stringEmptyAsNull", mandatory = false, specifiedDefaultValue= "true", unspecifiedDefaultValue = "false", help = "Adds Editor for String that set Empty String to Null") boolean stringEmptyAsNull) {
	webBinderOperations.setup(initializerClass, stringEmptyAsNull);
    }

    // DROP //

    @CliAvailabilityIndicator("web binding drop")
    public boolean isDropAvailable() {
	return webBinderOperations.isDropAvailable();
    }

    @CliCommand(value = "web binding drop", help = "Removes the default Property editors bindings configuration")
    public void drop() {
	webBinderOperations.drop();
    }

    // Add //

//    @CliAvailabilityIndicator("web binding add")
//    public boolean isAddAvailable() {
//	return webBinderOperations.isAddAvailable();
//    }
//
//    @CliCommand(value = "web binding add", help = "Add a new Property editor to the default list")
//    public void add(
//	    @CliOption(key = "target", mandatory = true, help = "Class that manage the PropertyEditor") JavaType target,
//	    @CliOption(key = "editor", mandatory = true, help = "Editors class") JavaType editor) {
//	webBinderOperations.add(target, editor);
//    }

}
