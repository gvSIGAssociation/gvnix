/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon;

/**
 * Interface methods required by a manager of a configuration file.
 * <p>
 * The manager class is responsible of read/write dynamic property values.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface DynamicConfigurationInterface {

  /**
   * Reads database.properties values and generates a file with given format.
   * TODO Use SomeFileFormat instead of Objet on file property.
   * 
   * @return File info getted from the original file.
   */
  Object read();
  
  /**
   * Update database.properties with values stored on the file in given format.
   * TODO Use SomeFileFormat instead of Objet on file property.
   * 
   * @param file Info to be stored on the original file.
   */
  void write(Object file);

}
