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

package org.gvnix.addon.geo.addon;

/**
 * Example of an enum used for tab-completion of properties.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.4.0
 */
public enum ProjectionCRSTypes {

    EPSG3857("EPSG3857"), EPSG4326("EPSG4326"), EPSG3395("EPSG3395"), Simple(
            "Simple");

    public final String descripcion;

    ProjectionCRSTypes(String descripcion) {
        this.descripcion = descripcion;
    }

    public static ProjectionCRSTypes getCRSTypes(String crsTypes) {
        if (crsTypes != null && !"null".equals(crsTypes)) {
            try {
                return ProjectionCRSTypes.valueOf(crsTypes);
            }
            catch (java.lang.IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}