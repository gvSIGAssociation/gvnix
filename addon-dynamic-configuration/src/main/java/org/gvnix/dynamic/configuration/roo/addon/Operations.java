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
   * Store files properties as a dynamic configuration with a name.
   * 
   * @param name Name for the dynamic configuration
   * @return Dynamic configuration stored
   */
  public DynConfiguration saveActiveConfiguration(String name);

  /**
   * Update files with the properties of the dynamic configuration.
   * <p>
   * If dynamic configuration with name not exists, null will be returned.
   * </p>
   * 
   * @param name Dynamic configuration name to activate
   * @return Dynamic configuration activated
   */
  public DynConfiguration setActiveConfiguration(String name);

  /**
   * Get all stored dynamic configurations.
   * 
   * @return List of stored dynamic configurations.
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
   * Is project available ?
   * 
   * @return Project avaliability
   */
  public boolean isProjectAvailable();
  
  /**
   * Get the properties stored along configurations with a name.
   * 
   * @param name Property name
   * @return Related properties along distinct configurations
   */
  public DynConfigurationList getProperties(String name);

}
