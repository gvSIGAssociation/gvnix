/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon;

import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;

/**
 * Dynamic configuration operations interface.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
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
   * <li>If dynamic configuration with name not exists, null will be returned.</li>
   * <li>If active property is false, there are pending local changes</li>
   * </ul>
   * 
   * @param name Dynamic configuration name to activate
   * @return Dynamic configuration activated
   */
  public DynConfiguration setActiveConfiguration(String name);

  /**
   * Unlink files with the properties of the dynamic configuration.
   * <ul>
   * <li>If dynamic configuration with name not exists, null will be returned.</li>
   * <li>If active property is false, there are pending local changes</li>
   * </ul>
   * 
   * @param name Dynamic configuration name to activate
   * @return Dynamic configuration activated
   */
  public DynConfiguration setUnactiveConfiguration(String name);

  /**
   * Get all stored dynamic configurations.
   * 
   * @return List of stored dynamic configurations or empty if not.
   */
  public DynConfigurationList findConfigurations();

  /**
   * Remove a previously stored configuration.
   * 
   * @param name Name of the dynamic configuration
   * @return Dynamic configuration to remove
   */
  public boolean deleteConfiguration(String name);
  
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
   * Get the properties stored along configurations with a name or empty.
   * 
   * @param name Property name
   * @return Related properties along distinct configurations or empty
   */
  public DynConfigurationList getProperties(String name);

  /**
   * Get the base dynamic configuration if contains a property name, else empty.
   * 
   * @param name Property name
   * @return Dynamic configuration or empty
   */
  public DynConfiguration getBaseProperty(String name);

  /**
   * Set a value on a configuration property.
   * <p>
   * If configuration is active, the value is setted on disk too.
   * </p>
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
   * @return false if already exists
   */
  public boolean addProperty(String name);
  
  /**
   * Delete a property with some name on all configurations.
   * 
   * @param name Property name
   * @return false if not exists
   */
  public boolean deleteProperty(String name);
  
}
