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
package org.gvnix.web.theme.roo.addon;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * gvNIX Theme representation.
 * <p>
 * Mainly for internal use within the add-on.
 * 
 * @author Enrique Ruiz ( eruiz at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
public class Theme {

  /** Theme identifier */
  protected String id;

  /** Theme name */
  protected String name;

  /** Theme description */
  protected String description;

  /** Is the active theme */
  protected boolean active = false;

  /** Has been installed */
  protected boolean installed = false;

  /** Is available to be installed */
  protected boolean available = false;

  /** Properties the theme needs to work, i.e., labels used in custom theme tags */
  protected HashMap<String, Map<String, String>> propertyBundles = new HashMap<String, Map<String, String>>();

  /** 
   * Theme descriptor location. Usually set on parsing Theme descriptor
   * @see org.gvnix.web.theme.roo.addon.util.XmlUtils#parseTheme(URI) 
   * @see org.gvnix.web.theme.roo.addon.util.XmlUtils#parseTheme(URL) 
   */
  protected URI descriptor;

  public Theme(String id) {
    this.setId(id);
  }

  /**
   * @param themeEl
   */
  public Theme(Element themeEl) {

    // if root element isn't theme, we found invalid theme
    if(!themeEl.getNodeName().equals("gvnix-theme")) {
      throw new IllegalArgumentException("Given theme Element hasn't valid XML structure.");
    }

    this.id = themeEl.getAttribute("id");
    Assert.notNull(id, "Theme element must have id attribute");

    this.name = themeEl.getAttribute("name");

    // Description can be null in custom project themes
    Element themeDescription = XmlUtils.findFirstElementByName("description",
        themeEl);
    this.description = themeDescription != null ? themeDescription.getTextContent() : null;

    // Properties can be null
    Element properties = XmlUtils.findFirstElementByName("properties",
        themeEl);
    if(properties != null) {
      propertyBundles = parseThemeProperties(properties);
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isActive() {
    return active;
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }
  
  public boolean isInstalled() {
    return installed;
  }
  
  public void setInstalled(boolean installed) {
    this.installed = installed;
  }
  
  public boolean isAvailable() {
    return available;
  }
  
  public void setAvailable(boolean available) {
    this.available = available;
  }

  public URI getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(URI descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Get the Theme location.
   * <p>
   * The root path is resolved using the descriptor URI:
   * <ul>
   *   <li>URI syntax {@code [scheme:][//authority][path]}</li>
   *   <li>{@code [path]} ends with <em>/THEME-DIR/WEB-INF/views/theme.xml</em></li>
   *   <li>Resolving the relative URI {@code ..} will get us the directory that
   *   contains theme artefacts</li>
   * </ul>
   * 
   * @return URI for Theme directory
   */
  public URI getRootURI() {
    if (this.description == null) {
      return null;
    }
    URI themeDir = this.descriptor.resolve("../..");
    return themeDir;
  }

  /**
   * Gets the properties bundles.
   * <p>
   * For easy file properties manipulation with {@link PropFileOperations}, 
   * the key has the format PATH_ID:filename. Creating a {@link Path} with 
   * PATH_ID both filename you can update a properties file as follows:
   * <pre>
   *   int index = entry.getKey().indexOf(":");
   *   Path path = new Path(entry.getKey().substring(0, index));
   *   String filename = entry.getKey().substring(index);
   *
   *   Map<String,String> properties = entry.getValue();
   *   propFileOperations.addProperties(path, filename, properties, true, true);
   * </pre>
   * 
   * @return
   */
  public HashMap<String, Map<String, String>> getPropertyBundles() {
    return propertyBundles;
  }

  /**
   * Transform properties bundle elements into Properties objects, one for
   * each bundle Element and referenced by the destination file.
   * 
   * @param properties
   * @return
   */
  protected HashMap<String, Map<String, String>> parseThemeProperties(Element properties) {

    HashMap<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

    // iterate <bundle> elements
    NodeList bundles = properties.getChildNodes();
    for (int i = 0; i < bundles.getLength(); i++) {
      Node bundle = bundles.item(i);
      if (bundle.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      // each <bundle> contains 'path' attribute and 'file' attribute that
      // will be concatenated using : as separator in order to be used as
      // bundle identifier
      Element bundleElement = (Element) bundle;
      String key = bundleElement.getAttribute("path").concat(":")
          .concat(bundleElement.getAttribute("file"));

      // iterate bundle properties to add to 'value' Property object
      Map<String, String> value = new HashMap<String, String>();
      NodeList bundleProperties = bundleElement.getChildNodes();
      for (int j = 0; j < bundleProperties.getLength(); j++) {
        Node bundleProperty = bundleProperties.item(j);
        if (bundleProperty.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        value.put(bundleProperty.getNodeName(), bundleProperty.getNodeValue());
      }

      // finally add bundle Properties identified by path:file
      result.put(key, value);
    }

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof Theme)) {
      return false;
    }
    Theme t = (Theme) obj;

    if(getId() != null && getId().equals(t.getId())) {
      return true;
    }
    return false;
  }
}
