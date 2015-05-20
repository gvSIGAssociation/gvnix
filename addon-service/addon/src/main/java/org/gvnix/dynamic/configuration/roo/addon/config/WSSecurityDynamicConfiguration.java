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
import org.apache.felix.scr.annotations.Service;

/**
 * Dynamic configuration manager of web services security properties.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component
@Service
public class WSSecurityDynamicConfiguration extends
        PropertiesListDynamicConfiguration implements
        DefaultDynamicConfiguration {

    /**
     * {@inheritDoc}
     */
    public String getName() {

        return "Web Services Security Properties";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilePrefix() {

        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileSufix() {

        return "Security.properties";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyTargetPrefix() {

        return "security.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertySourcePrefix() {

        return "org.apache.ws.security.crypto.";
    }

}
