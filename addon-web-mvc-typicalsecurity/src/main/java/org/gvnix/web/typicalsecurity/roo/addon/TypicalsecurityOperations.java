package org.gvnix.web.typicalsecurity.roo.addon;

/**
 * Interface of commands that are available via the Roo shell.
 * 
 * @since 1.1
 */
public interface TypicalsecurityOperations {

    boolean isCommandAvailable();

    String setup(String entityPackage, String controllerPackage);

    /**
     * This method checks if typical security is installed. If is installed
     * update views to use bootstrap
     */
    void updateTypicalSecurityAddonToBootstrap();

    boolean isTypicalSecurityInstalled();

    boolean isLoginModified();
}