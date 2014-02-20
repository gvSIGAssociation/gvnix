package org.gvnix.addon.gva.security.providers.cit;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.gva.security.providers.SecurityProvider;
import org.gvnix.cit.security.roo.addon.CitSecurityOperations;
import org.springframework.roo.model.JavaPackage;

@Component
@Service
public class CitSecurityProvider implements SecurityProvider {

    @Reference
    private CitSecurityOperations citSecurityOperations;

    private static final String PROVIDER_DESCRIPTION = "Security CIT Provider";
    private static final String NAME = "CIT";

    private Logger log = Logger.getLogger(getClass().getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return PROVIDER_DESCRIPTION;
    }

    @Override
    public void install(JavaPackage targetPackage) {
        citSecurityOperations.setup("", "", "", "");
        // Showing Next Steps
        showNextSteps();
    }

    @Override
    public Boolean isInstalled() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This method shows the next steps to configure the application correctly
     * to use this provider
     * 
     */
    public void showNextSteps() {
        log.log(Level.INFO, "");
        log.log(Level.INFO, "");
        log.log(Level.INFO,
                "*** Before execute your application you must to configure the follow"
                        + " CIT Client Properties:");
        log.log(Level.INFO,
                "--------------------------------------------------------------------");
        log.log(Level.INFO, "    - wsauth.appName");
        log.log(Level.INFO, "    - wsauth.loggin");
        log.log(Level.INFO, "    - wsauth.password");
        log.log(Level.INFO, "    - wsauth.url");
        log.log(Level.INFO, "");
        log.log(Level.INFO,
                "*** Use the configuration commands to set this parameters");
        log.log(Level.INFO, "");
        log.log(Level.INFO, "");
    }

}
