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

import java.util.List;

import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Manage configurations interface.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
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
     * @param property Property name to parse or all if null
     * @return Dynamic configuration
     */
    public DynConfiguration parseConfiguration(Element conf, String property);

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
     * Get the base configuration from configuration.
     * 
     * @return Base configuration element
     */
    public Element getBaseConfiguration();

    /**
     * Get all configuration elements.
     * 
     * @return All configuration elements
     */
    public List<Element> getAllConfigurations();

    /**
     * Get a property element from a configuration with some name.
     * 
     * @param configuration Configuration name
     * @param property Property name
     * @return Dynamic property
     */
    public Element getProperty(String configuration, String property);

    /**
     * Get current dynamic configuration from configuration file.
     * <p>
     * If no dynamic configuration active, null will be returned.
     * </p>
     * 
     * @return Active dynamic configuration or null.
     */
    public DynConfiguration getActiveConfiguration();

    /**
     * Set the active configuration on the configuration file.
     * 
     * @param name Configuration name
     */
    public void setActiveConfiguration(String name);

    /**
     * Add a component property name and value on stored and base
     * configurations.
     * 
     * @param name Property name
     * @param value Property value
     * @param compId Component id
     * @param compName Component name
     */
    public void addProperties(String name, String value, String compId,
            String compName);

    /**
     * Get the key element of a property element.
     * 
     * @param prop Property element
     * @return
     */
    public Node getKeyElement(Element prop);

    /**
     * Get the value element of a property element.
     * 
     * @param prop Property element
     * @return
     */
    public Node getValueElement(Element prop);

    /**
     * Set a value on a configuration property.
     * <p>
     * If null value, value element no written.
     * </p>
     * 
     * @param configuration Configuration name to update
     * @param property Property name to update
     * @param value Value to set
     * @return Dynamic property updated or null if not exists
     */
    public DynProperty updateProperty(String configuration, String property,
            String value);

}
