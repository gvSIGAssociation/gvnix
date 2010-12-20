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

import java.util.List;

import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.w3c.dom.Element;

/**
 * Manage configurations interface.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface Configurations {

  /**
   * Add a dynamic configuration on the configuration file.
   * 
   * @param configs Dynamic configuration to store at configuration file
   */
  public void addConfiguration(DynConfiguration dynConf);
  
  /**
   * Delete a configuration element from the configuration file.
   * 
   * @param conf Configuration element to delete 
   */
  public void deleteConfiguration(Element conf);

  /**
   * Parse a configuration element to a dynamic configuration.
   * <p>
   * All configuration component properties will be processed if name is null.
   * If name not null, only specified property name will be processed.
   * </p>
   * 
   * @param conf Configuration element
   * @param name Property name to parse or all if null
   * @return Dynamic configuration
   */
  public DynConfiguration parseConfiguration(Element conf, String name);

  /**
   * Parse a component element to a dynamic component.
   * <p>
   * All component properties will be processed if name is null. If name not
   * null, only specified property name will be processed.
   * </p>
   * 
   * @param comp Component element
   * @param name Property name to parse or all if null
   * @return Dynamic configuration
   */
  public DynComponent parseComponent(Element comp, String name);
  
  /**
   * Parse a property element to a dynamic property.
   * 
   * @param prop Property element
   * @return Dynamic property
   */
  public DynProperty parseProperty(Element prop);
  
  /**
   * Save the document of an element on the configuration file.
   * 
   * @param conf Element of the document to save
   */
  public void saveConfiguration(Element elem);
  
  /**
   * Find the first configuration element with given name. 
   * 
   * @param name Configuration element name
   * @return Configuration element
   */
  public Element findConfiguration(String name);
  
  /**
   * Get all configuration elements. 
   * 
   * @return All configuration elements
   */
  public List<Element> getAllConfigurations();

  /**
   * Get all component elements. 
   * 
   * @return All configuration elements
   */
  public List<Element> getAllComponents();

  /**
   * Get a property element from a configuration with some name.
   * 
   * @param configuration Configuration name
   * @param property Property name
   * @return Dynamic property
   */
  public Element getProperty(String configuration, String property);

}
