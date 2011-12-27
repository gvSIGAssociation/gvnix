package com.xsoftwarelabs.spring.roo.addon.typicalsecurity;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related logic in this
 * class. You can return any objects from each method, or use the logger directly if you'd
 * like to emit messages of different severity (and therefore different colours on 
 * non-Windows systems).
 * 
 * @since 1.1
 */
@Component
@Service
public class TypicalsecurityCommands implements CommandMarker {
	private static Logger logger = Logger.getLogger(TypicalsecurityCommands.class.getName());
	@Reference private TypicalsecurityOperations operations;
	
	@CliAvailabilityIndicator({"typicalsecurity setup", "typicalsecurity add"})
	public boolean isPropertyAvailable() {
		return operations.isCommandAvailable();
	}
	
	
	
	@CliCommand(value = "typicalsecurity setup", help = "Setup typicalsecurity addon")
	public String setup(@CliOption(key = "entityPackage", mandatory = false, help = "Package where entities are placed. Default: ~.domain",specifiedDefaultValue="~.domain",unspecifiedDefaultValue="~.domain") String entityPackage,@CliOption(key = "controllerPackage", mandatory = false, help = "Package where controllers are placed. Default: ~.web",specifiedDefaultValue="~.web",unspecifiedDefaultValue="~.web") String controllerPackage) {
		return operations.setup( entityPackage,  controllerPackage);
	}
}