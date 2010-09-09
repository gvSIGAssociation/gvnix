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
 * Service to manage the Web Services.
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
     * Sense of the Communication.
     * <ul>
     * <li>EXPORT: From this to external systems.</li>
     * <li>IMPORT: From external systems to this.</li>
     * </ul>
     */
    public enum CommunicationSense { EXPORT, IMPORT };

    /**
     * Install and configure Web Service library on a project.
     * 
     * @param type Communication type
     */
    public void install(CommunicationSense type);

    /**
     * Publish a class as Web Service.
     * 
     * @param serviceClass
     *            class to be configured as Web Service.
     */
    public void exportClass(JavaType serviceClass);

}
