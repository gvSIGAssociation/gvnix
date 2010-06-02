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

package org.gvnix.web.mvc.jsp20.roo.addon;

/**
 * Operations Interface for support JSP 2.0 in Roo projects
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public interface ControllerJsp20SupportOperations {

    /**
     * Informs if activate support operation is available.
     *
     */
    public boolean isActiveSupportAvailable();

    /**
     * Informs if support is activated in this project
     *
     */
    public boolean isActivated();

    /**
     * Activate JSP 2.0 support in this project
     *
     */
    public void activeJSP20Support();
}
