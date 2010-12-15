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
import java.util.ArrayList;
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
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Manage components to save or activate dynamic configurations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
@Reference(name="components", strategy=ReferenceStrategy.LOOKUP, policy=ReferencePolicy.DYNAMIC, referenceInterface=DefaultDynamicConfiguration.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)
public class DynConfigService implements DynConfigServiceInt {
  
  private static final String DYNAMIC_CONFIGURATION_ELEMENT_NAME = "dynamic-configuration";
  private static final String CONFIGURATION_ELEMENT_NAME = "configuration";
  private static final String COMPONENT_ELEMENT_NAME = "component";
  private static final String ID_ATTRIBUTE_NAME = "id";
  private static final String NAME_ATTRIBUTE_NAME = "name";
  
  private static final String CONFIGURATION_XPATH = "/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME + "/" + CONFIGURATION_ELEMENT_NAME;

  private static final Logger logger = HandlerUtils.getLogger(DynConfigService.class);

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
   * @see org.gvnix.dynamic.configuration.roo.addon.DynConfigServiceInt#save(java.lang.String)
   */
  public Set<DynConfiguration> save(String name) {
    
    // Get all the dynamic configurations 
    Set<DynConfiguration> configs = getConfigurations();
    
    // Get the main configuration file of the dynamic configuration
    String path = getDynConfigConfigurationPath();
    MutableFile file = fileManager.updateFile(path);

    // Parse the XML configuration file to a Document
    Document document = parse(file);
    
    // Find the configuration of document with requested name
    Element root = document.getDocumentElement();
    List<Element> config = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", root);
    if (config != null && config.size() > 0) {
      
      // If configuration already exists, delete it
      removeDynConfigConfiguration(config.get(0));
    }

    // Create the configuration
    addDynConfigConfiguration(name, configs, root);
    XmlUtils.writeXml(file.getOutputStream(), document);

    return configs;
  }
  

  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.DynConfigServiceInt#activate(java.lang.String)
   */
  public Set<DynConfiguration> activate(String name) {
    
    Set<DynConfiguration> configs = new HashSet<DynConfiguration>();

    // Get the main configuration file of the dynamic configuration
    String path = getDynConfigConfigurationPath();
    MutableFile file = fileManager.updateFile(path);

    // Parse the XML configuration file to a Document
    Document document = parse(file);
    
    // Find the configuration of document with requested name
    Element root = document.getDocumentElement();
    List<Element> config = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", root);
    if (config != null && config.size() > 0) {

      configs = getDynConfigConfiguration(name, config.get(0));
      setConfigurations(configs);
      
      return configs;
    }

    return null;
  }
  
  /**
   * Get all dynamic configurations.
   * 
   * @return Set of dynamic configurations.
   */
  @SuppressWarnings("unchecked")
  private Set<DynConfiguration> getConfigurations() {

    // Variable to store all dynamic configurations
    Set<DynConfiguration> configs = new HashSet<DynConfiguration>();
    
    // Iterate all dynamic configurations components registered
    for (Object o : getComponents()) {
      try {
        
        // If component has not DynamicConfiguration annotation, ignore it
        Class<? extends Object> c = o.getClass();
        DynamicConfiguration a = c.getAnnotation(DynamicConfiguration.class);
        if (a == null) {

          continue;
        }
        
        // Invoke the read method of all components to get its properties
        Method m = (Method) c.getMethod("read", new Class[0]);
        List<DynProperty> res = (List<DynProperty>) m.invoke(
            o, new Object[0]);

        // Create a dynamic configuration object with component and properties
        DynConfiguration dynConfiguration = new DynConfiguration();
        DynComponent component = new DynComponent(c.getName(), a.name());
        dynConfiguration.setComponent(component);
        dynConfiguration.setProperties(res);
        configs.add(dynConfiguration);
      }
      catch (NoSuchMethodException nsme) {

        logger.log(Level.SEVERE,
            "No read method on dynamic configuration class", nsme);
      }
      catch (InvocationTargetException ite) {

        logger.log(Level.SEVERE,
            "Cannot invoke read method on dynamic configuration class", ite);
      }
      catch (IllegalAccessException iae) {

        logger.log(Level.SEVERE,
            "Cannot access read method on dynamic configuration class", iae);
      }
    }

    return configs;
  }

  /**
   * Set all dynamic configurations.
   * 
   * @param configs Set of dynamic configurations
   */
  @SuppressWarnings("unchecked")
  private void setConfigurations(Set<DynConfiguration> configs) {

    // Iterate all dynamic configurations components registered
    for (Object o : getComponents()) {
      try {
        
        // If component has not DynamicConfiguration annotation, ignore it
        Class<? extends Object> c = o.getClass();
        DynamicConfiguration a = c.getAnnotation(DynamicConfiguration.class);
        if (a == null) {

          continue;
        }
        
        // Invoke the read method of all components to get its properties
        for (DynConfiguration config : configs) {

          if (c.getName().equals(config.getComponent().getId())) {

            Class[] p = new Class[1];
            p[0] = List.class;
            Method m = (Method) c.getMethod("write", p);
            Object[] args = new Object[1];
            args[0] = config.getProperties();
            m.invoke(o, args);
          }
        }
      }
      catch (NoSuchMethodException nsme) {

        logger.log(Level.SEVERE,
            "No write method on dynamic configuration class", nsme);
      }
      catch (InvocationTargetException ite) {

        logger.log(Level.SEVERE,
            "Cannot invoke write method on dynamic configuration class", ite);
      }
      catch (IllegalAccessException iae) {

        logger.log(Level.SEVERE,
            "Cannot access write method on dynamic configuration class", iae);
      }
    }
  }

  /**
   * Get the main XML configuration file path of dynamic configurations.
   * <p>
   * If XML configuration file not exists, create it with a template.
   * </p>
   * 
   * @return XML configuration file path
   */
  private String getDynConfigConfigurationPath() {

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
   * DynConfiguration will contain the set of key and value properties.
   * </p>
   * 
   * @param name New configuration name
   * @param configs Set of dynamic configurations with key and value pairs
   * @param root Element to append the child configuration
   */
  private void addDynConfigConfiguration(String name,
                                   Set<DynConfiguration> configs,
                                   Element root) {

    Document document = root.getOwnerDocument();
    Element configuration = document.createElement(CONFIGURATION_ELEMENT_NAME);
    configuration.setAttribute(NAME_ATTRIBUTE_NAME, name);
    root.appendChild(configuration);

    for (DynConfiguration config : configs) {
      
      Element component = document.createElement(COMPONENT_ELEMENT_NAME);
      component.setAttribute(ID_ATTRIBUTE_NAME, config.getComponent().getId());
      component.setAttribute(NAME_ATTRIBUTE_NAME, config.getComponent().getName());
      configuration.appendChild(component);
      
      for (DynProperty property : config.getProperties()) {

        Element element = document.createElement(property.getKey());
        element.setTextContent(property.getValue());
        component.appendChild(element);
      }
    }
  }

  /**
   * Delete a existing configuration element.
   * 
   * @param conf Element to remove 
   */
  private void removeDynConfigConfiguration(Element conf) {
    
    List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME, conf);
    for (Element comp : comps) {
      conf.removeChild(comp);
    }
    conf.getParentNode().removeChild(conf);
  }

  /**
   * Get the configuration element on the root element with a name.
   * 
   * @param name Required configuration name
   * @param configs Set of dynamic configurations with key and value pairs
   * @param root Element to get the child configuration
   */
  private Set<DynConfiguration> getDynConfigConfiguration(String name,
                                   Element root) {
    
    Set<DynConfiguration> configurations = new HashSet<DynConfiguration>();

    List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME, root);
    for (Element comp : comps) {

      List<DynProperty> properties = new ArrayList<DynProperty>();

      List<Element> props = XmlUtils.findElements("*", comp);
      for (Element prop : props) {

        DynProperty property = new DynProperty(prop.getTagName(), prop
            .getTextContent());
        properties.add(property);
      }
      
      DynConfiguration configuration = new DynConfiguration();
      configuration.setProperties(properties);
      DynComponent component = new DynComponent(comp.getAttributes()
          .getNamedItem(ID_ATTRIBUTE_NAME).getNodeValue(), comp.getAttributes()
          .getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue());
      configuration.setComponent(component);
      configurations.add(configuration);
    }

    return configurations;
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
