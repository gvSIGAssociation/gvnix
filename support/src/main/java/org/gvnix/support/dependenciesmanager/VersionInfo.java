/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.support.dependenciesmanager;

import org.apache.commons.lang3.Validate;

/**
 * VersionInfo holder for comparisons between dependencies and properties
 * versions
 * 
 * @see org.springframework.roo.project.AutomaticProjectUpgradeService
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.7
 */
public class VersionInfo {
    private Integer major = 0;
    private Integer minor = 0;
    private Integer patch = 0;
    private Qualifiers qualifier = Qualifiers.EMPTY;

    public int compareTo(VersionInfo v) {
    	Validate.notNull(v);
        int result = major.compareTo(v.major);
        if (result != 0) {
            return result;
        }
        result = minor.compareTo(v.minor);
        if (result != 0) {
            return result;
        }
        result = patch.compareTo(v.patch);
        if (result != 0) {
            return result;
        }
        result = qualifier.newerThan(v.qualifier);
        if (result != 0) {
            return result;
        }
        return 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + "." + qualifier;
    }

    /**
     * Extracts the version information from the string. Never throws an
     * exception. <br/>
     * 
     * TODO: modify method design for make it smarter when working with several
     * version info formats
     * 
     * @param version
     *            to extract from (can be null or empty)
     * @return the version information or null if it was not in a normal form
     */
    public static VersionInfo extractVersionInfoFromString(String version) {
        if (version == null || version.length() == 0) {
            return null;
        }

        String[] ver = version.split("\\.");
        try {
            // versions as x.y.z
            if (ver.length == 3) {
                VersionInfo result = new VersionInfo();
                result.major = new Integer(ver[0]);
                result.minor = new Integer(ver[1]);
                // gvNIX versions can be x.y.z (for final versions or release
                // versions) and x.y.z-q (for snapshots versions)
                String[] patchVerQualifier = ver[2].split("-");
                result.patch = new Integer(patchVerQualifier[0]);
                if (patchVerQualifier.length == 2) {
                    String qualifier = patchVerQualifier[1];
                    if (qualifier.equalsIgnoreCase(Qualifiers.RELEASE
                            .toString())) {
                        result.qualifier = Qualifiers.RELEASE;
                    } else if (qualifier.equalsIgnoreCase(Qualifiers.SNAPSHOT
                            .toString())) {
                        result.qualifier = Qualifiers.SNAPSHOT;
                    }
                }
                return result;
            }
            // versions as x.y
            if (ver.length == 2) {
                VersionInfo result = new VersionInfo();
                result.major = new Integer(ver[0]);
                result.minor = new Integer(ver[1]);
                return result;
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }
}
