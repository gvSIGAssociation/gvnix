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

/**
 * Enum for libraries version qualifiers
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.7
 */
public enum Qualifiers {

    RELEASE("RELEASE"), EMPTY(""), SNAPSHOT("SNAPSHOT");

    private String qualifier;

    private Qualifiers(String qualifier) {
        this.qualifier = qualifier;
    }

    public boolean isRelease() {
        return this.equals(RELEASE) || this.equals(EMPTY);
    }

    public boolean isSnapshot() {
        return this.equals(SNAPSHOT);
    }

    /**
     * Says if the qualifier is newer than passed qualifier
     * <ul>
     * <li>RELEASE equals to EMPTY</li>
     * <li>RELEASE newer than SNAPSHOT</li>
     * </ul>
     * 
     * @param q
     * @return <ul>
     *         <li>0 if this equals to q</li>
     *         <li>1 if this newer than q</li>
     *         <li>-1 otherwise</li>
     *         </ul>
     */
    public int newerThan(Qualifiers q) {
        if (this.equals(q)) {
            return 0;
        }

        if (this.isRelease() && q.isSnapshot()) {
            return 1;
        }

        return -1;
    }

}
