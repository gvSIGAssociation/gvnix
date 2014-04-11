package org.gvnix.addon.loupefield;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @since 1.1
 */
public interface LoupefieldOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX Bootstrap has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_LOUPEFIELDS = "gvnix-loupe";

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
    boolean isSetCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isUpdateCommandAvailable();

    /**
     * Setup all add-on artifacts
     */
    void setup();

    /**
     * Set property as Loupe Field
     */

    void setLoupeFields(JavaType controller, JavaType backingType,
            JavaSymbolName field);

    /**
     * Update all add-on artifacts
     */
    void update();

}