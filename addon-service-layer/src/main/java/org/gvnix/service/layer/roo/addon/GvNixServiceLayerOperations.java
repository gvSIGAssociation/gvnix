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
package org.gvnix.service.layer.roo.addon;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface GvNixServiceLayerOperations {

    boolean isProjectAvailable();

    /**
     * <p>
     * Check if Cxf is properly configurated in a project.
     * </p>
     * 
     * @return true or false if it's configurated.
     */
    public boolean isCxfInstalled();

    /**
     * <p>
     * Check if Cxf dependencies are set in project's pom.xml.
     * </p>
     * 
     * @return true or false if Cxf dependcies are set.
     */
    public boolean areCxfDependenciesInstalled();

    /**
     * <p>
     * Get Addon dependencies list to install.
     * </p>
     * 
     * @return List of dependencies as xml elements.
     */
    public List<Element> getCxfDependencies();

    /**
     * <p>
     * Check if Cxf config file is created in the project.
     * </p>
     * 
     * @return true or false if exists Cxf configuration file.
     */
    public boolean isCxfConfigurated();

    /**
     * <p>
     * Set up Cxf configuration to a project.
     * </p>
     */
    public void setUpCxf();
}