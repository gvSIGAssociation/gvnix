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
     * This method adds the bootstrap libraries to the gvNIX project
     */
    void addBootstrapScriptsLibraries();

    /**
     * This method adds the bootstrap styles to the gvNIX project
     */
    void addBootstrapStyles();

    /**
     * This method replaces the old standard.css with a new one adapted to
     * Bootstrap3
     */
    void replaceOldStyles();

    /**
     * This method adds the bootstrap tags to the correct folder
     */
    void addBootstrapTags();

    /**
     * This method updates HTML structure in default gvNIX layouts to use
     * bootsrap
     */
    void updateGvNIXLayouts();

    /**
     * This method updates HTML structure in default gvNIX views to use
     * bootstrap
     */
    void updateViews();

    /**
     * This method copies images resources to gvNIX application
     */
    void addImageResources();

    /**
     * This method checks if Datatables is installed. If is installed install
     * necessary scripts and styles to use datatables in bootstrap
     */
    void checkAndUpdateDatatables();

    /**
     * This method checks if typical security is installed. If is installed
     * update views to use bootstrap
     */
    void checkAndUpdateSecurity();

    /**
     * Check if {@code WEB-INF/tags/jquery} and
     * {@code scripts/jquery/jquery-min.js} exist
     * 
     * @return true if is installed
     */
    boolean hasJQueryTags();

    /**
     * Check if {@code WEB-INF/tags/menu} exist
     * 
     * @return true if is installed
     */
    boolean isMenuInstalled();

    /**
     * Check if {@code scripts/bootstrap} exist
     * 
     * @return
     */
    boolean isBootstrapInstalled();

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    LogicalPath getWebappPath();
}