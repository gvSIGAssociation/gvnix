package org.gvnix.addon.geo;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
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
    static final String FEATURE_NAME_GVNIX_GEO_WEB_MVC = "gvnix-geo-web-mvc";

    /**
     * This method checks if setup command is available
     * 
     * @return true if setup command is available
     */
    boolean isSetupCommandAvailable();

    /**
     * This method checks if add map command is available
     * 
     * @return true if map command is available
     */
    boolean isMapCommandAvailable();

    /**
     * This method checks if web mvc geo all command is available
     * 
     * @return true if web mvc geo all command is available
     */
    boolean isAllCommandAvailable();

    /**
     * This method checks if web mvc geo add command is available
     * 
     * @return true if web mvc geo add command is available
     */
    boolean isAddCommandAvailable();

    /**
     * This method imports all necessary element to build a gvNIX GEO
     * application
     */
    void setup();

    /**
     * This method creates new components to visualize a Map component
     * 
     * @param controller
     * @param path
     */
    void addMap(JavaType controller, JavaSymbolName path);

    /**
     * This method include all GEO entities on specific map
     * 
     * @param path
     */
    void all(JavaSymbolName path);

    /**
     * This method include specific GEO entity on specific map
     * 
     * @param path
     */
    void add(JavaType controller, JavaSymbolName path);

}