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

import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerWsExportOperations {
    
    /**
     * Is service layer web service export command available on Roo console ? 
     * 
     * @return Service layer web service export command available on Roo console 
     */
    boolean isProjectAvailable();

    /**
     * Exports a class to Web Service.
     * 
     * @param className
     *            class to export.
     * @param serviceName
     *            Name to publish the Web Service.
     * @param name
     *            Name to define the portType.
     * @param targetNamespace
     *            Namespace name for the service.
     */
    public void exportService(JavaType className, String serviceName,
	    String name, String targetNamespace);

}
