package org.gvnix.dynamic.configuration.roo.addon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;

@Component
@Service
@Reference(name="commands", strategy=ReferenceStrategy.LOOKUP, policy=ReferencePolicy.DYNAMIC, referenceInterface=PropertyDynamicConfiguration.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)
public class DynamicConfigurationParser implements ConfigurationParser {
  
  private static final Logger logger = HandlerUtils.getLogger(DynamicConfigurationParser.class);

  private ComponentContext context;
  @Reference private ProcessManager processManager;
  
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
  
  public Set<Class<? extends Object>> getEveryCommand() {
    
    SortedSet<Class<? extends Object>> result = new TreeSet<Class<? extends Object>>();

    for (Object o : getTargets()) {
      
      if (processManager.isDevelopmentMode()) {
        
        logger.log(Level.INFO, "Analyze dynamic configuration: " + o);
      }
      
      Class<? extends Object> c = o.getClass();
      DynamicConfiguration a = c.getAnnotation(DynamicConfiguration.class);
      
      if (a == null) {
      
        logger.log(Level.WARNING, "No annotation on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "Its dynamic configuration will not be applied");
        
        continue;
      }
      
      String file = a.file();
      Assert.hasText(file, "File name to apply dynamic configuration required on " + c.getCanonicalName());
      
      System.out.println(file);
      
      try {
        
        Method m = (Method)c.getMethod("read", new Class[0]);
        Object res = m.invoke(o, new Object[0]);
        
        result.add(c.getClass());
      }
      catch (NoSuchMethodException nsme) {
        
        logger.log(Level.WARNING, "No read method on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "No dynamic configuration will be applied to " + file);
      }
      catch (InvocationTargetException ite) {
        
        logger.log(Level.WARNING, "Cannot invoke read method on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "No dynamic configuration will be applied to " + file);
      }
      catch (IllegalAccessException iae) {
        
        logger.log(Level.WARNING, "Cannot access read method on dynamic configuration class " + c.getCanonicalName());
        logger.log(Level.WARNING, "No dynamic configuration will be applied to " + file);
      }
    }
    
    return result;
  }

}
