package org.gvnix.addon.gva.security;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.gva.security.providers.SecurityProviderId;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.shell.converters.StaticFieldConverter;

@Component
@Service
public class SecurityProviderCommands implements CommandMarker {

    @Reference
    private SecurityProviderOperations operations;

    @Reference
    private StaticFieldConverter staticFieldConverter;

    /**
     * Checks if security provider add command is available
     * 
     * @return
     */
    @CliAvailabilityIndicator("security provider add")
    public boolean isSecurityProviderAddAvailable() {
        return operations.checkSecuritySetup();
    }

    /**
     * 
     * Create a security provider in the gvNIX application
     * 
     * @param securityClass
     * @param name
     */
    @CliCommand(value = "security provider add", help = "Adds a security provider")
    public void securityProviderAdd(
            @CliOption(key = "name", mandatory = true, help = "Provider's Name") SecurityProviderId name,
            @CliOption(key = "package", mandatory = true, help = "Package where you want to generete Java classes") JavaPackage targetPackage) {
        operations.installProvider(name, targetPackage);
    }
}