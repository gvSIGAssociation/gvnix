package org.gvnix.dynamic.configuration.roo.addon.entity;

public class DynComponent {
  
  private String id;

  private String name;
  
  
  public DynComponent() {
    super();
  }

  public DynComponent(String id, String name) {
    super();
    this.id = id;
    this.name = name;
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

  @Override
  public String toString() {
    return "DynComponent [id=" + id + "]";
  }

}
