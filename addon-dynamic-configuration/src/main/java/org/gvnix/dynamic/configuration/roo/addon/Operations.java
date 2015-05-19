/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon;

import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;

/**
 * Dynamic configuration operations interface.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public interface Operations {

    /**
     * Store new dynamic configuration with base properties.
     * <p>
     * If configuration already exists, previous configuration will be deleted.
     * </p>
     * 
     * @param name Name for the dynamic configuration
     * @return Dynamic configuration stored
     */
    public DynConfiguration saveActiveConfiguration(String name);

    /**
     * Link files with the properties of the dynamic configuration.
     * <ul>
     * <li>If dynamic configuration with name not exists, null will be returned.
     * </li>
     * <li>If active property is false, there are pending local changes</li>
     * </ul>
     * 
     * @param name Dynamic configuration name to activate
     * @return Dynamic configuration activated
     */
    public DynConfiguration setActiveConfiguration(String name);

    /**
     * Get all stored dynamic configurations.
     * 
     * @return List of stored dynamic configurations or empty if not.
     */
    public DynConfigurationList findConfigurations();

    /**
     * Get stored dynamic configuration with a name.
     * <p>
     * If dynamic configuration with name not exists, null will be returned.
     * </p>
     * 
     * @param name Name of the dynamic configuration
     * @return Stored dynamic configuration.
     */
    public DynConfiguration getConfiguration(String name);

    /**
     * Get base dynamic configuration.
     * 
     * @return Base dynamic configuration.
     */
    public DynConfiguration getBaseConfiguration();

    /**
     * Get the base dynamic configuration if contains a property name, else
     * empty.
     * 
     * @param name Property name
     * @return Dynamic configuration or empty
     */
    public DynConfiguration getBaseProperty(String name);

    /**
     * Set a configuration property with no value.
     * 
     * @param configuration Configuration name to update
     * @param property Property name to update
     * @return Dynamic property updated or null if not exists
     */
    public DynProperty updateProperty(String configuration, String property);

    /**
     * Set a value on a configuration property.
     * 
     * @param configuration Configuration name to update
     * @param property Property name to update
     * @param value Value to set
     * @return Dynamic property updated or null if not exists
     */
    public DynProperty updateProperty(String configuration, String property,
            String value);

    /**
     * Is project available ?
     * 
     * @return Project avaliability
     */
    public boolean isProjectAvailable();

    /**
     * Add property with name and value on all configurations.
     * 
     * @param name Property name
     * @return false if already exists or null if not exists
     */
    public Boolean addProperty(String name);

    /**
     * Write all stored dynamic configurations into the build tool.
     * <p>
     * If no dynamic configuration active returns null. If no dynamic
     * configurations returns empty List.
     * </p>
     * 
     * @return Exported dynamic configuration
     */
    public DynConfigurationList export();

}
