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
package org.gvnix.service.roo.addon.ws.export;

import java.util.List;

import org.springframework.roo.model.JavaType;

/**
 * Export web services operation from WSDL (Contract First).
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 * @See {@link WSExportWsdlListener}
 * @see {@link WSExportWsdlConfigService}
 */
public interface WSExportWsdlOperations {

    /**
     * Generate gvNIX server annotated java code from WSDL.
     * 
     * <p>
     * Generate WSDL javas with wsdl2java maven plugin at target folder and
     * monitoring this folder to create gvNIX javas into src folder from
     * generated ones.
     * </p>
     * 
     * @param url
     *            from WSDL file to export.
     * @return implementation classes
     */
    public List<JavaType> exportWsdl(String url);

}
