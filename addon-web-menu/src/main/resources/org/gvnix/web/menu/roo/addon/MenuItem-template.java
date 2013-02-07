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

import org.w3c.dom.Element;

/**
 * Menu model item.
 * <p>
 * This has all information about a menu's item.
 */
public class MenuItem {

  private static final String DELIMITER = "/";

  private MenuItem parent;

  private final String url;
  private final String id;
  private final String messageCode;
  private final String roles;
  private final boolean hidden;
  private List<MenuItem> children = null;

  /** If label is null, menu will use labelCode to load the label from I18N properties */
  private String labelCode;

  /**
   * Load Item values from XML Element. This doesn't load children nodes.
   * 
   * @param parent
   * @param element
   */
  MenuItem(Element element) {
    this.id = element.getAttribute("id");
    this.labelCode = element.getAttribute("labelCode");
    this.messageCode = element.getAttribute("messageCode");
    this.url = element.getAttribute("url");
    this.roles = element.getAttribute("roles");
    if (element.hasAttribute("hidden")) {
      this.hidden = Boolean.parseBoolean(element.getAttribute("hidden"));
    } else {
      this.hidden = false; 
    }
  }

  /**
   * Add a new child menu item. Child item parent is set to <code>this</code>.
   * 
   * @param child
   */
  void addChild(MenuItem child) {
    if (children == null) {
      children = new ArrayList<MenuItem>();
    }
    children.add(child);
    child.parent = this;
  }

  /**
   * Informs if this item has any children.
   * 
   * @return
   */
  public boolean hasChildren() {
    return children != null && children.size() > 0;
  }

  /**
   * Children list accessor.
   * 
   * @return List
   */
  public List<MenuItem> getChildren() {
    return this.children;
  }

  /**
   * Children list mutator
   * @param children
   */
  public void setChildren(List<MenuItem> children) {
    this.children = children;
  }

  /**
   * roles string accessor.
   * 
   * @return comma list
   */
  public String getRoles() {
    return this.roles;
  }

  /**
   * URL accessor
   * 
   * @return
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Parent accessor
   * 
   * @return
   */
  public MenuItem getParent() {
    return this.parent;
  }

  /**
   * ID accessor
   * 
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * messageCode accessor
   * 
   * @return
   */
  public String getMessageCode() {
    return messageCode;
  }

  
  /**
   * hidden accessor
   * 
   * @return
   */
  public boolean isHidden() {
    return hidden;
  }

  
  /**
   * labelCode accessor
   * 
   * @return
   */
  public String getLabelCode() {
    return labelCode;
  }

  /**
   * Convert MenuItem ID to path format.
   * <p>
   * The ID will be transformed as follows:
   * <ul>
   * <li>The id prefix, "c_" of "i_", will be removed</li>
   * <li>Append root path, "/" at the begining</li>
   * <li>Replace "_" by "/"</li>
   * </ul>
   * This method doesn't modify the current ID.
   * 
   * @return
   */
  protected String getIdAsPath() {
    StringBuilder path = new StringBuilder();

    path.append(DELIMITER);

    String idWithoutPrefix = this.id.substring(2); // current prefixes have length = 2

    path.append(idWithoutPrefix.replace("_", DELIMITER));
    
    return path.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((url == null) ? 0 : url.hashCode());
    result = prime * result + (hidden ? 1231 : 1237);
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((roles == null) ? 0 : roles.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((labelCode == null) ? 0 : labelCode.hashCode());
    result = prime * result
        + ((messageCode == null) ? 0 : messageCode.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
    	return true;
    }
    if (obj == null){
    	return false;
    }
    if (getClass() != obj.getClass()) {
    	return false;
    }
    MenuItem other = (MenuItem) obj;
    if (url == null) {
      if (other.url != null) {
    	  return false;
      }
    }
    else if (!url.equals(other.url)) {
    	return false;
    }
    if (hidden != other.hidden) {
    	return false;
    }
    if (parent == null) {
      if (other.parent != null) {
    	  return false;
      }
    }
    else if (!parent.equals(other.parent)) {
    	return false;
    }
    if (roles == null) {
      if (other.roles != null) {
    	  return false;
      }
    }
    else if (!roles.equals(other.roles)) {
    	return false;
    }
    if (id == null) {
      if (other.id != null) {
    	  return false;
      }
    }
    else if (!id.equals(other.id)) {
    	return false;
    }
    if (labelCode == null) {
      if (other.labelCode != null) {
    	  return false;
      }
    }
    else if (!labelCode.equals(other.labelCode)) {
    	return false;
    }
    if (messageCode == null) {
      if (other.messageCode != null) {
    	  return false;
      }
    }
    else if (!messageCode.equals(other.messageCode)) {
    	return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MenuItem [".concat("ID=").concat(id).concat(", link=")
      .concat(url).concat("]");
  }
}
