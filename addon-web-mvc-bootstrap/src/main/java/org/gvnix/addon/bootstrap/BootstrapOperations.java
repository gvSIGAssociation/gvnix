package org.gvnix.addon.bootstrap;

import org.springframework.roo.project.LogicalPath;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @since 1.1
 */
public interface BootstrapOperations {

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

    /**
     * Check if {@code WEB-INF/tags/jquery} and
     * {@code scripts/jquery/jquery-min.js} exist
     * 
     * @return true if is installed
     */
    boolean hasJQueryTags();

    /**
     * Check if {@code scripts/bootstrap} exist
     * 
     * @return
     */
    boolean isBootstrapInstalled();

}