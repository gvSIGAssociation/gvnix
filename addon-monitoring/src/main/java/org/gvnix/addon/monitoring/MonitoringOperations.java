package org.gvnix.addon.monitoring;

/**
 * Interface of operations this add-on offers.
 * 
 * @since 1.4.0
 */
public interface MonitoringOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Setup all add-on artifacts (dependencies in this case)
     * 
     * @param pathString set the storage directory for JavaMelody data files
     *        (Default: <server_temp>/javamelody )
     */
    void setup(String pathString);
}