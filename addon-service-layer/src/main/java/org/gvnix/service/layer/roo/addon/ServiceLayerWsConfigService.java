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

import org.springframework.roo.model.JavaType;
import org.w3c.dom.Element;

/**
 * Utilities to manage the web services library.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerWsConfigService {

    /**
     * <p>
     * Set up library configuration to a project.
     * </p>
     */
    public void setUp();

    /**
     * <p>
     * Check if library is properly configurated in a project.
     * </p>
     * 
     * @return true or false if it's configurated.
     */
    public boolean isInstalled();

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
     * Check if Cxf dependencies are set in project's pom.xml.
     * </p>
     * 
     * @return true or false if Cxf dependcies are set.
     */
    public boolean areDependenciesInstalled();

    /**
     * <p>
     * Get Addon dependencies list to install.
     * </p>
     * 
     * @return List of dependencies as xml elements.
     */
    public List<Element> getDependencies();

    /**
     * Define Web Service class in cxf configuration file to be published.
     * 
     * @param serviceClass
     *            class to define as Web Service in Cxf configuration file.
     */
    public void updateCxfXml(JavaType serviceClass);
    
    /**
     * Adds GvNIX annotations library dependency to the current project.
     * 
     * <p>
     * TODO REMOVE FROM API
     * <p>
     */
    public void addGvNIXAnnotationsDependecy();

}
