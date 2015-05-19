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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Abstract dynamic configuration component of property files.
 * <p>
 * Extends this class to manage new properties file values.
 * </p>
 * <ul>
 * <li>TODO When file is managed, property order is modified and comments
 * removed</li>
 * </ul>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component(componentAbstract = true)
public abstract class PropertiesDynamicConfiguration extends
        FileDynamicConfiguration {

    private static final Logger logger = HandlerUtils
            .getLogger(PropertiesDynamicConfiguration.class);

    /**
     * {@inheritDoc}
     */
    public DynPropertyList read() {

        DynPropertyList dynProps = new DynPropertyList();

        try {

            // Get the properties file path
            MutableFile file = getFile();

            // If managed file not exists, nothing to do
            if (file != null) {

                Properties props = new Properties();
                props.load(file.getInputStream());
                for (Entry<Object, Object> prop : props.entrySet()) {

                    dynProps.add(new DynProperty(prop.getKey().toString(), prop
                            .getValue().toString()));
                }
            }
        }
        catch (IOException ioe) {

            throw new IllegalStateException(ioe);
        }

        return dynProps;
    }

    /**
     * {@inheritDoc}
     */
    public void write(DynPropertyList dynProps) {
        OutputStream outputStream = null;

        try {

            // Get the properties file path
            MutableFile file = getFile();
            if (file != null) {

                Properties props = new Properties();
                props.load(file.getInputStream());
                for (DynProperty dynProp : dynProps) {

                    if (props.containsKey(dynProp.getKey())) {

                        props.put(dynProp.getKey(), dynProp.getValue());
                    }
                    else {

                        logger.log(
                                Level.WARNING,
                                "Property key "
                                        .concat(dynProp.getKey())
                                        .concat(" to put value not exists on file"));
                    }
                }
                outputStream = file.getOutputStream();
                props.store(outputStream, null);

            }
            else if (!dynProps.isEmpty()) {

                logger.log(
                        Level.WARNING,
                        "File ".concat(getFilePath())
                                .concat(" not exists and there are dynamic properties to set it"));
            }
        }
        catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            if (outputStream != null) {
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

}
