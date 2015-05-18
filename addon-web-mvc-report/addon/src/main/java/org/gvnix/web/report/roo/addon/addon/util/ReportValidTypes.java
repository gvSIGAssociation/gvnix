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
package org.gvnix.web.report.roo.addon.addon.util;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

/**
 * Enum con los tipos de objeto soportados por los field de JasperReports
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.6
 */
public enum ReportValidTypes {
    BOOLEAN("java.lang.Boolean"), BYTE("java.lang.Byte"), DATE("java.util.Date"), TIMESTAMP(
            "java.sql.Timestamp"), TIME("java.sql.Time"), DOUBLE(
            "java.lang.Double"), FLOAT("java.lang.Float"), INTEGER(
            "java.lang.Integer"), LONG("java.lang.Long"), SHORT(
            "java.lang.Short"), BIGDECIMAL("java.math.BigDecimal"), NUMBER(
            "java.lang.Number"), STRING("java.lang.String");

    private String type;

    private static Collection<ReportValidTypes> validTypesSet = EnumSet.of(
            BOOLEAN, BYTE, DATE, TIMESTAMP, TIME, DOUBLE, FLOAT, INTEGER, LONG,
            SHORT, BIGDECIMAL, NUMBER, STRING);

    public static final Collection<String> VALID_TYPES = getValidTypes();

    private ReportValidTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private static Collection<String> getValidTypes() {
        if (null != VALID_TYPES && !VALID_TYPES.isEmpty()) {
            return VALID_TYPES;
        }
        HashSet<String> validTypes = new HashSet<String>(validTypesSet.size());
        for (ReportValidTypes validType : validTypesSet) {
            validTypes.add(validType.getType());
        }
        return validTypes;
    }

}
