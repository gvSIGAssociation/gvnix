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

package org.gvnix.addon.jpa.addon.geo;

import java.util.List;

import org.gvnix.addon.jpa.addon.geo.providers.GeoProviderId;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.4
 */
public interface JpaGeoOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX GEO component has been setup in this
     * project
     */
    static final String FT_NM_GVNIX_GEO_PERS = "gvnix-geo-persistence";

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupCommandAvailable();

    /**
     * 
     * @return
     */
    boolean isFieldCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isFinderGeoAllCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isFinderGeoAddCommandAvailable();

    /**
     * Installs the selected provider
     * 
     * @param provider Provider GeoProviderId
     */
    void installProvider(GeoProviderId provider);

    /**
     * Add new field depending of the selected provider.
     * 
     * @param provider Provider GeoProviderId
     */
    void addFieldByProvider(JavaSymbolName fieldName,
            FieldGeoTypes fieldGeoType, JavaType entity);

    /**
     * Add finders to all Geo Entities
     * 
     */
    void addFinderGeoAllByProvider();

    /**
     * Add finders to specific Entity
     * 
     * @param entity entity
     */
    void addFinderGeoAddByProvider(JavaType entity);

    /**
     * 
     * Get available providers on the system
     * 
     * @return A GeoProviderId List
     */
    List<GeoProviderId> getProvidersId();

    /**
     * Gets the current provider by name
     * 
     * @param name Provider Name
     * @return SecurityProviderId
     */
    GeoProviderId getProviderIdByName(String name);

}
