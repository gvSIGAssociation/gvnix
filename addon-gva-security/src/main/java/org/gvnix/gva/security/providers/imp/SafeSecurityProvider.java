package org.gvnix.gva.security.providers.imp;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.gva.security.providers.SecurityProvider;
import org.springframework.roo.project.ProjectOperations;

@Component
@Service
public class SafeSecurityProvider implements SecurityProvider {

    private Logger log = Logger.getLogger(getClass().getName());

    public static final String NAME = "SAFE";
    public static final String DESCRIPTION = "Security SAFE Provider";

    @Reference
    private ProjectOperations projectOperations;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void install() {
        log.info("You're going to add security provider type SAFE");
    }

    @Override
    public Boolean isInstalled() {
        // TODO Auto-generated method stub
        return null;
    }

}
