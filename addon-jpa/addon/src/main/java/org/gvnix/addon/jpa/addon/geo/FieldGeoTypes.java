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

/**
 * Example of an enum used for tab-completion of properties.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1.1
 */
public enum FieldGeoTypes {

    POINT("com.vividsolutions.jts.geom.Point"), LINESTRING(
            "com.vividsolutions.jts.geom.LineString"), POLYGON(
            "com.vividsolutions.jts.geom.Polygon"), GEOMETRY(
            "com.vividsolutions.jts.geom.Geometry"), MULTILINESTRING(
            "com.vividsolutions.jts.geom.MultiLineString");

    public final String descripcion;

    FieldGeoTypes(String descripcion) {
        this.descripcion = descripcion;
    }

    public static FieldGeoTypes getFieldGeoTypes(String geoTypes) {
        if (geoTypes != null && !"null".equals(geoTypes)) {
            try {
                return FieldGeoTypes.valueOf(geoTypes);
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