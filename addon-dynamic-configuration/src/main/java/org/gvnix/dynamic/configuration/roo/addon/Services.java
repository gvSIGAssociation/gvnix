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

import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;

/**
 * Interface to manage components of dynamic configurations.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public interface Services {

    /**
     * Get current files properties as a dynamic configuration.
     * 
     * @return Current dynamic configuration.
     */
    public DynConfiguration getCurrentConfiguration();

    /**
     * Get the component with some property name on current configuration.
     * 
     * @param name Property name
     * @return Dynamic property or null if not exists
     */
    public DynComponent getCurrentComponent(String name);

    /**
     * Get a property name on current configuration.
     * 
     * @param name Property name
     * @return Dynamic property or null if not exists
     */
    public DynProperty getCurrentProperty(String name);

    /**
     * Update files properties from a dynamic configuration.
     * 
     * @param dynConf Dynamic configuration with properties
     */
    public void setCurrentConfiguration(DynConfiguration dynConf);

    /**
     * Get the file path related with a dynamic component.
     * 
     * @param dynComp Dynamic component to file path
     * @return File path
     */
    public String getFilePath(DynComponent dynComp);
}
