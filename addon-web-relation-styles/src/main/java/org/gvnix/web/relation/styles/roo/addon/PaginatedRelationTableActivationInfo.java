/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
package org.gvnix.web.relation.styles.roo.addon;

public interface PaginatedRelationTableActivationInfo {

    /**
     * Check if there is a project available.
     * 
     * @return true if exists.
     */
    boolean isProjectAvailable();

    /**
     * Check if the Add-on is Activated.
     * 
     * @return true if is installed.
     */
    public boolean isActivated();

    /**
     * Check if Web Scaffold had been generated.
     * 
     * @return true if WEB-INF directory exists.
     */
    public boolean isWebScaffoldGenerated();
}
