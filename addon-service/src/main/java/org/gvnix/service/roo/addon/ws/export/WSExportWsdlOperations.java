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

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public interface WSExportWsdlOperations {

  /**
   * Is service layer command available on Roo console ?
   * 
   * @return Service layer command available on Roo console
   */
  boolean isProjectAvailable();

  /**
   * Export WSDL Web Service Contract First to Java code using GvNIX
   * annotations.
   * 
   * @param url from WSDL file to export.
   */
  public void exportWSDL2Java(String url);

  /**
   * Create new files with '@GvNIX' annotations in SRC_MAIN_JAVA.
   */
  public void updateAnnotationsToGvNIX();
}
