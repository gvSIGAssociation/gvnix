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

import java.io.IOException;
import java.util.Properties;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

/**
 * Dynamic configuration base manager of property files.
 * <p>
 * Extends this class with adding the @DynamicConfiguration annotation to manage
 * new property files.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class PropertiesDynamicConfiguration implements
    DynamicConfigurationInterface {
  
  @Reference private PathResolver pathResolver;
  @Reference private FileManager fileManager;

  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.DynamicConfigurationInterface#read()
   */
  public Properties read() {
    
    DynamicConfiguration dynamicConfig = this.getClass().getAnnotation(DynamicConfiguration.class);

    String canonicalPath = pathResolver.getIdentifier(new Path(dynamicConfig.path().name()), dynamicConfig.relativePath());

    MutableFile mutableFile = null;
    Properties props = new Properties();
    try {
      
      if (fileManager.exists(canonicalPath)) {

        mutableFile = fileManager.updateFile(canonicalPath);
        props.load(mutableFile.getInputStream());
      }
      else {

        throw new IllegalStateException("Properties file not found");
      }
    }
    catch (IOException ioe) {

      throw new IllegalStateException(ioe);
    }

    // TODO Auto-generated method stub
    return props;
  }

  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.DynamicConfigurationInterface#write(java.lang.Object)
   */
  public void write(Object file) {
    
    System.out.println("write database.properties");
    
    // TODO Auto-generated method stub
  }

}
