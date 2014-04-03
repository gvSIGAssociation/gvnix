package org.gvnix.addon.bootstrap;

import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @since 1.1
 */
public interface BootstrapOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX Bootstrap has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_BOOTSTRAP = "gvnix-bootstrap";

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isUpdateCommandAvailable();

    /**
     * Setup all Bootstrap artifacts (css, js, etc.. )
     */
    void setup();

    /**
     * Update all Bootstrap tags (css, js, etc.. )
     */
    void updateTags();

}