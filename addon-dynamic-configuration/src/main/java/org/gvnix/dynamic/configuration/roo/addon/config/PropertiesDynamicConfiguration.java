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
package org.gvnix.dynamic.configuration.roo.addon.config;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.felix.scr.annotations.Component;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.MutableFile;

/**
 * Abstract dynamic configuration component of property files.
 * <p>
 * Extends this class to manage new properties file values.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(componentAbstract = true)
public abstract class PropertiesDynamicConfiguration extends FileDynamicConfiguration {
  
  /**
   * {@inheritDoc}
   */
  public DynPropertyList read() {

    // Get the properties file path
    MutableFile file = getFile();

    DynPropertyList dynProps = new DynPropertyList();

    try {

      Properties props = new Properties();
      props.load(file.getInputStream());
      for (Entry<Object, Object> prop : props.entrySet()) {

        dynProps.add(new DynProperty(prop.getKey().toString(), prop
            .getValue().toString()));
      }
    }
    catch (IOException ioe) {

      throw new IllegalStateException(ioe);
    }

    return dynProps;
  }

  /**
   * {@inheritDoc}
   */
  public void write(DynPropertyList dynProps) {

    try {

      Properties props = new Properties();
      for (DynProperty dynProp : dynProps) {

        props.put(dynProp.getKey(), dynProp.getValue());
      }

      // Get the properties file path
      MutableFile file = getFile();
      props.store(file.getOutputStream(), null);
    }
    catch (IOException ioe) {

      throw new IllegalStateException(ioe);
    }
  }

}
