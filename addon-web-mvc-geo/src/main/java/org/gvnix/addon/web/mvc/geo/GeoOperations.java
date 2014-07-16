package org.gvnix.addon.web.mvc.geo;

import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @since 1.1
 */
public interface GeoOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX GEO component has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_GEO = "gvnix-geo-web-mvc";

    /**
     * This method checks if setup command is available
     * 
     * @return true if setup command is available
     */
    boolean isSetupCommandAvailable();

    /**
     * This method imports all necessary element to build a gvNIX GEO
     * application
     */
    void setup();

}