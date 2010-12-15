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
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.Set;

import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;

/**
 * Interface to save or activate dynamic configurations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface DynConfigServiceInt {

  /**
   * Save current properties and values of dynamic files on configuration.
   * 
   * @param name Name to save it
   * @return Set of dynamic configurations with key/value pairs of properties
   */
  public Set<DynConfiguration> save(String name);

  /**
   * Write dynamic files with properties and values stored on configuration.
   * <p>
   * If dynamic configuration with name not exists, null will be returned.
   * </p>
   * 
   * @param name Configuration name to activate
   * @return Set of dynamic configurations with key/value pairs of properties
   */
  public Set<DynConfiguration> activate(String name);
  
}
