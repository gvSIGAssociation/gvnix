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
package org.gvnix.dynamic.configuration.roo.addon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Manage components and invoke it to save or activate configurations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
@Reference(name="components", strategy=ReferenceStrategy.LOOKUP, policy=ReferencePolicy.DYNAMIC, referenceInterface=DynamicConfigurationInterface.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)
public class DynamicConfigurationParser implements ConfigurationParser {
  
  private static final Logger logger = HandlerUtils.getLogger(DynamicConfigurationParser.class);

  private ComponentContext context;
  
  @Reference private PathResolver pathResolver;
  @Reference private FileManager fileManager;

  protected void activate(ComponentContext context) {
    this.context = context;
  }
  
  /**
   * Get all the dynamic configuration components.
   * 
   * @return Dynamic configuration components
   */
  private Set<Object> getComponents() {
    return getSet("components");
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
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.ConfigurationParser#save(java.lang.String)
   */
  public Set<DynConfiguration> save(String name) {
    
    // Get the properties of all dynamic properties components 
    Set<DynConfiguration> properties = getProperties();
    
    // Get the main configuration file of the dynamic configuration
    String configPath = getDynamicConfigurationFilePath();
    MutableFile file = fileManager.updateFile(configPath);

    // Parse the XML configuration file to a Document
    Document document = parse(file);
    
    // Find the configuration of document with requested name
    Element root = document.getDocumentElement();
    List<Element> dynConf = XmlUtils.findElements("/dynamic-configuration/configuration[@name='" + name + "']", root);
    if (dynConf == null || dynConf.size() == 0) {
      
      // If configuration not exists, create it
      createConfiguration(name, properties, root);
      XmlUtils.writeXml(file.getOutputStream(), document);
    }
    else {
      
      // TODO If configuration exists, update it
    }
    
    return properties;
  }

  /**
   * Get the properties of all dynamic properties components.
   * 
   * @return Set of entry with key value pairs of properties
   */
  @SuppressWarnings("unchecked")
  private Set<DynConfiguration> getProperties() {

    // Variable to store properties of all dynamic configuration components
    Set<DynConfiguration> components = new HashSet<DynConfiguration>();
    
    // Iterate all dynamic configuration components registered
    for (Object o : getComponents()) {

      // If component has not DynComponent annotation, ignore it
      Class<? extends Object> c = o.getClass();
      DynamicConfiguration a = c.getAnnotation(DynamicConfiguration.class);
      if (a == null) {

        continue;
      }

      // Relative path attribute of annotation not empty required
      String relativePath = a.relativePath();
      Assert.hasText(relativePath,
          "File name to apply dynamic configuration required on "
              + c.getCanonicalName());

      try {

        // Invoke the read method of all components to get its properties
        Method m = (Method) c.getMethod("read", new Class[0]);
        List<DynProperty> res = (List<DynProperty>) m.invoke(
            o, new Object[0]);
        
        
        
        DynConfiguration dynConfiguration = new DynConfiguration();
        DynComponent component = new DynComponent(c.getName());
        dynConfiguration.setComponent(component);
        dynConfiguration.setProperties(res);
        
        
        components.add(dynConfiguration);
      }
      catch (NoSuchMethodException nsme) {

        logger.log(Level.WARNING,
            "No read method on dynamic configuration class "
                + c.getCanonicalName());
        logger
            .log(Level.WARNING, "No dynamic configuration will be applied to "
                + relativePath, nsme);
      }
      catch (InvocationTargetException ite) {

        logger.log(Level.WARNING,
            "Cannot invoke read method on dynamic configuration class "
                + c.getCanonicalName());
        logger.log(Level.WARNING,
            "No dynamic configuration will be applied to " + relativePath, ite);
      }
      catch (IllegalAccessException iae) {

        logger.log(Level.WARNING,
            "Cannot access read method on dynamic configuration class "
                + c.getCanonicalName());
        logger.log(Level.WARNING,
            "No dynamic configuration will be applied to " + relativePath, iae);
      }
    }

    return components;
  }

  /**
   * Get the main XML configuration file path of dynamic configurations.
   * <p>
   * If XML configuration file not exists, create it with a template.
   * </p>
   * 
   * @return XML configuration file path
   */
  private String getDynamicConfigurationFilePath() {

    String path = pathResolver.getIdentifier(Path.SRC_MAIN_RESOURCES,
        "dynamic-configuration.xml");
    if (!fileManager.exists(path)) {
      try {

        FileCopyUtils.copy(TemplateUtils.getTemplate(getClass(),
            "dynamic-configuration-template.xml"), fileManager.createFile(
            path).getOutputStream());
      }
      catch (IOException ioe) {

        throw new IllegalStateException(ioe);
      }
    }
    
    return path;
  }

  /**
   * Create a new configuration element on the root element with a name.
   * <p>
   * DynConfiguration will constain the set of key and value properties.
   * </p>
   * 
   * @param name New configuration name
   * @param components Set of key and value properties
   * @param root Element to append the child configuration
   */
  private void createConfiguration(String name,
                                   Set<DynConfiguration> components,
                                   Element root) {

    Document document = root.getOwnerDocument();
    Element configuration = document.createElement("configuration");
    configuration.setAttribute("name", name);
    root.appendChild(configuration);

    for (DynConfiguration entry : components) {
      
      Element component = document.createElement("component");
      component.setAttribute("id", entry.getComponent().getName());
      configuration.appendChild(component);
      
      for (DynProperty property : entry.getProperties()) {

        Element element = document.createElement(property.getKey());
        element.setTextContent(property.getValue());
        component.appendChild(element);
      }
    }
  }

  /**
   * Parse a mutable file into a dom Document.
   * 
   * @param file Mutable file
   * @return Dom document
   */
  private Document parse(MutableFile file) {
    
    Document document;
    try {
      
      DocumentBuilder builder = XmlUtils.getDocumentBuilder();
      document = builder.parse(file.getInputStream());
      
    } catch (SAXException se) {
      throw new IllegalStateException("Unable to parse the "
          + file.getCanonicalPath() + " file", se);
    } catch (IOException ioe) {
      throw new IllegalStateException("Unable to read the "
          + file.getCanonicalPath() + " file (reason: " + ioe.getMessage()
          + ")", ioe);
    }
    
    return document;
  }

}
