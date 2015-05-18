/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.addon.monitoring;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
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
