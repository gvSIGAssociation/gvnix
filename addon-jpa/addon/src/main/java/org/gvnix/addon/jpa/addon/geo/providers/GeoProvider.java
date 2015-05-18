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

package org.gvnix.addon.jpa.addon.geo.providers;

import org.gvnix.addon.jpa.addon.geo.FieldGeoTypes;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;

/**
 *
 * Interface of GeoProvider
 * 
 *  
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.4.0
 */
public interface GeoProvider {

    /**
     * Gets provider name
     *
     * @return
     */
    String getName();

    /**
     *
     */
    String getDescription();

    /**
     * This method installs the provider that implements the interface
     *
     */
    void setup();

    /**
     * This method add a new field on the selected entity
     *
     */
    void addField(JavaSymbolName fieldName, FieldGeoTypes fieldGeoType,
            JavaType entity);

    /**
     * This method add a new finder in all entities
     *
     */
    void addFinderGeoAll();

    /**
     * This method add a new finder in the selected entity
     *
     */
    void addFinderGeoAdd(JavaType entity);

    /**
     * This method checks if his provider persistence is installed
     *
     * @return true if is installed
     **/
    boolean isAvailablePersistence(FileManager fileManager,
            PathResolver pathResolver);

    /**
     * This method checks if his GEO persistence is installed
     *
     * @return true if is installed
     **/
    boolean isGeoPersistenceInstalled(FileManager fileManager,
            PathResolver pathResolver);

}
