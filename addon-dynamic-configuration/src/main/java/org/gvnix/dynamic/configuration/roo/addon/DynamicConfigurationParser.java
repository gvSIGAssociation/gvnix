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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;

/**
 * Manage components and invoke it to read or write configurations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
@Reference(name="commands", strategy=ReferenceStrategy.LOOKUP, policy=ReferencePolicy.DYNAMIC, referenceInterface=DynamicConfigurationInterface.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)
public class DynamicConfigurationParser implements ConfigurationParser {
  
  private static final Logger logger = HandlerUtils.getLogger(DynamicConfigurationParser.class);

  private ComponentContext context;

  protected void activate(ComponentContext context) {
    this.context = context;
  }
  
  private Set<Object> getTargets() {
    return getSet("commands");
  }
  
  @SuppressWarnings("unchecked")
  private <T> Set<T> getSet(String name) {
    Set<T> result = new HashSet<T>();
    Object[] objs = context.locateServices(name);
    if (objs != null) {
      for (Object o : objs) {
        result.add((T) o);
      }
    }
    return result;
  }
  
  public Properties getProperties() {
    
    Properties result = new Properties();

    for (Object o : getTargets()) {

      Class<? extends Object> c = o.getClass();
      DynamicConfiguration a = c.getAnnotation(DynamicConfiguration.class);
      
      if (a == null) {
      
        continue;
      }
      
      String name = a.relativePath();
      Assert.hasText(name, "File name to apply dynamic configuration required on " + c.getCanonicalName());

      try {
        
        Method m = (Method)c.getMethod("read", new Class[0]);
        Properties res = (Properties)m.invoke(o, new Object[0]);
        
        result.putAll(res);
      }
      catch (NoSuchMethodException nsme) {
        
        logger.log(Level.WARNING, "No read method on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "No dynamic configuration will be applied to " + name, nsme);
      }
      catch (InvocationTargetException ite) {
        
        logger.log(Level.WARNING, "Cannot invoke read method on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "No dynamic configuration will be applied to " + name, ite);
      }
      catch (IllegalAccessException iae) {
        
        logger.log(Level.WARNING, "Cannot access read method on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "No dynamic configuration will be applied to " + name, iae);
      }
    }
    
    return result;
  }

}
