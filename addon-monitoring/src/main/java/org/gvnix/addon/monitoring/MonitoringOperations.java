package org.gvnix.addon.monitoring;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers.
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public interface MonitoringOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX MONITORING has been setup in this
     * project
     */
    public static final String FEATURE_NAME_GVNIX_MONITORING = "gvnix-monitoring";

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    boolean isAddAvailable();

    /**
     * Setup all add-on artifacts (dependencies in this case)
     * 
     * @param pathString set the storage directory for JavaMelody data files
     *        (Default: <server_temp>/javamelody )
     */
    void setup(String pathString);

    /**
     * Add all files to be monitored as a Spring service
     */
    void all();

    /**
     * Add a path which all his child methods will be monitored as a Spring
     * service
     * 
     * @param path Set the package path to be monitored
     */
    void addPackage(JavaPackage path);

    /**
     * Add a name class to be monitored as a Spring service
     * 
     * @param name Set the class name to be monitored
     */
    void addClass(JavaType name);

    /**
     * Add a method to be monitored as a Spring service
     * 
     * @param methodName Set the method name to be monitored
     * @param className Set the class name of the method to be monitored
     */
    void addMethod(JavaSymbolName methodName, JavaType className);

}