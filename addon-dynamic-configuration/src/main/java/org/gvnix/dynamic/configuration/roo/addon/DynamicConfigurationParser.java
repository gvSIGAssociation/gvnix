package org.gvnix.dynamic.configuration.roo.addon;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

@Component
@Service
@Reference(name="commands", strategy=ReferenceStrategy.LOOKUP, policy=ReferencePolicy.DYNAMIC, referenceInterface=CommandMarker.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)
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
    if ("commands".equals(name)) {
      result.add((T) this);
    }
    return result;
  }
  
  public Set<String> getEveryCommand() {
    SortedSet<String> result = new TreeSet<String>();
    for (Object o : getTargets()) {
      Method[] methods = o.getClass().getMethods();
      for (Method m : methods) {
        CliCommand cmd = m.getAnnotation(CliCommand.class);
        if (cmd != null) {
          for (String value : cmd.value()) {
            result.add(value);
          }
        }
      }
    }
    return result;
  }

}
