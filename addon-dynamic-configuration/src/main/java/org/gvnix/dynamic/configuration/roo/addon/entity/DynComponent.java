package org.gvnix.dynamic.configuration.roo.addon.entity;

import java.util.List;

public class DynComponent {
  
  private String id;

  private String name;
  
  private List<DynProperty> properties;
  
  
  public DynComponent() {
    super();
  }

  public DynComponent(String id, String name, List<DynProperty> properties) {
    super();
    this.id = id;
    this.name = name;
    this.properties = properties;
  }

  public DynComponent(String name) {
    super();
    this.id = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    
    if (name != null && name.length() > 0) {
    
      return name;
    }
    
    return id.substring(id.lastIndexOf(".") + 1, id.length());
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<DynProperty> getProperties() {
    return properties;
  }

  
  public void setProperties(List<DynProperty> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {

    // Show the component name
    StringBuffer buffer = new StringBuffer();
    buffer.append(getName());

    // Show properties
    for (DynProperty prop : properties) {

      // Show the property and value with format
      buffer.append("\n");
      buffer.append(" " + prop.getKey() + " = " + prop.getValue());
    }

    return buffer.toString();
  }

}
