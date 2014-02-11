package org.gvnix.gva.security.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.gva.security.providers.SecurityProviderId;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.shell.converters.StaticFieldConverter;

@Component
@Service
public class SecurityProviderCommands implements CommandMarker {

    private Logger log = Logger.getLogger(getClass().getName());

    @Reference
    private SecurityProviderOperations operations;

    @Reference
    private StaticFieldConverter staticFieldConverter;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {

    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon remove'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {

    }

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
            @CliOption(key = "name", mandatory = true, help = "Provider's Name") SecurityProviderId name) {
        operations.installProvider(name);
    }
}