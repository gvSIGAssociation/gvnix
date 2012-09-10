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
package org.gvnix.cit.security.roo.addon;

/**
 * CIT Security operation service
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public interface CitSecurityOperations {

    /**
     * Informs if setup operation is available
     *
     */
    public boolean isSetupAvailable();

    /**
     * Indicate the project has a web layer based on Spring MVC Tiles.
     *
     * @return true if the user installed an Spring MVC Tiles web layer, otherwise
     *         returns false.
     */
    boolean isSpringMvcTilesProject();

    /**
     * Install CIT Security in this projet
     *
     * @param url
     * @param login
     * @param password
     * @param appName
     */
    public void setup(String url, String login, String password, String appName);

}
