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
