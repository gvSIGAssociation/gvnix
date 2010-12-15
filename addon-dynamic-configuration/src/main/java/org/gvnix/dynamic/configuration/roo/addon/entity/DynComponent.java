package org.gvnix.dynamic.configuration.roo.addon.entity;

public class DynComponent {
  
  private String id;

  
  public DynComponent() {
    super();
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
    return id.substring(id.lastIndexOf(".") + 1, id.length());
  }

  @Override
  public String toString() {
    return "DynComponent [id=" + id + "]";
  }

}
