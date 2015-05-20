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
package org.gvnix.dynamic.configuration.roo.addon.config;

import org.apache.felix.scr.annotations.Component;

/**
 * Abstract dynamic configuration component of XML files for managing elements
 * with Xpath expressions.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component(componentAbstract = true)
public abstract class XpathDynamicConfiguration extends XmlDynamicConfiguration {

    /**
     * Xpath expression for access elements to manage.
     * 
     * @return Xpath expression
     */
    public abstract String getXpath();

    /**
     * Element name used as property key.
     * 
     * @return Element key
     */
    public abstract String getKey();

    /**
     * Element name used as property value.
     * 
     * @return Element value
     */
    public abstract String getValue();

    /**
     * String prefix for dynamic property names.
     * 
     * @return Dynamic property prefix
     */
    public abstract String getPrefix();

    /**
     * Set key value to set on a dynamic properties.
     * 
     * @param key Original key value
     * @return New key value
     */
    protected String setKeyValue(String key) {

        if (getPrefix() == null) {
            return key;
        }

        return getPrefix().concat(key);
    }

    /**
     * Get key value from a dynamic property.
     * 
     * @param key Original key value
     * @return New key value
     */
    protected String getKeyValue(String key) {

        if (getPrefix() == null) {
            return key;
        }

        return key.substring(getPrefix().length(), key.length());
    }

}
