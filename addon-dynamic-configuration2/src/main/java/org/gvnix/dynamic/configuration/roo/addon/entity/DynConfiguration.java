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
package org.gvnix.dynamic.configuration.roo.addon.entity;

/**
 * Dynamic configuration entity.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class DynConfiguration {

  private String name;
  
  private Boolean active;
  
  private DynComponentList components;


  public DynConfiguration() {
    super();
    this.components = new DynComponentList();
    active = Boolean.FALSE;
  }
  
  public DynConfiguration(String name) {
    super();
    this.components = new DynComponentList();
    active = Boolean.FALSE;
    this.name = name;
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

  public DynComponentList getComponents() {
    return components;
  }

  public void addComponent(DynComponent component) {
    components.add(component);
  }

  /**
   * {@inheritDoc}
   * 
   * Name and active message if configuration active.  
   */
  @Override
  public String toString() {
    
    StringBuffer buffer = new StringBuffer();
    
    if (active) {
      buffer.append("      (Active)      ");
    }
    else {
      buffer.append("                    ");
    }
    buffer.append(getName());
    buffer.append("\n----------------------------------------");
    
    return buffer.toString();
  }
  
  /**
   * Two configurations are equal if their components are equal.
   * 
   * @param obj Configuration to compare to
   * @return Configuration equals
   */
  public boolean equals(DynConfiguration obj) {

    for (DynComponent component : components) {
      
      boolean exist = false;
      for (DynComponent component2 : obj.getComponents()) {
        if (component.equals(component2)) {
          
          exist = true;
          break;
        }
      }
      
      if (!exist) {
        return false;
      }
    }
    
    return true;
  }

}
