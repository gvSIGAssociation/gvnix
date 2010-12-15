package org.gvnix.dynamic.configuration.roo.addon.entity;

import java.util.HashSet;
import java.util.Set;

public class DynConfiguration {

  private String name;
  
  private Boolean active;
  
  private Set<DynComponent> components;


  public DynConfiguration() {
    super();
    this.components = new HashSet<DynComponent>();
    active = Boolean.FALSE;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean isActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public Set<DynComponent> getComponents() {
    return components;
  }

  public void addComponent(DynComponent component) {
    components.add(component);
  }

  @Override
  public String toString() {
    
    StringBuffer buffer = new StringBuffer();
    buffer.append(getName());
    if (active) {
      buffer.append(" (Active)");
    }
    
    return buffer.toString();
  }

}
