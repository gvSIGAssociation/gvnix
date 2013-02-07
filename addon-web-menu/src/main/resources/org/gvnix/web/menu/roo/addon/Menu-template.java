/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package __TOP_LEVEL_PACKAGE__.web.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu Model Root class
 */
public class Menu {

  /**
   * Menu identifier.
   */
  private String id;

  /***
   * First level menu items.
   */
  private List<MenuItem> children = new ArrayList<MenuItem>();

  protected Menu(String id) {
    if (id == null) {
    	this.id = "_menu";
    }
    else {
    	this.id = id;
    }
  }
  
  /**
   * Gets menu id
   * @return
   */
  public String getId(){
	  return this.id;
  }

  /**
   * id mutator
   * 
   * @param id
   */
  void setId(String id) {
    this.id = id;
  }

  /**
   * add a child
   * 
   * @param child
   */
  void addChild(MenuItem child) {
    this.children.add(child);
  }

  /**
   * Gets children.
   * 
   * @return List
   */
  public List<MenuItem> getChildren() {
    return this.children;
  }

  /**
   * Set root menu items.
   * 
   * @param children
   */
  public void setChildren(List<MenuItem> children) {
    this.children = children;
  }
}
