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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Dynamic configuration operations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class OperationsImpl implements Operations{

  private static final String DYNAMIC_CONFIGURATION_ELEMENT_NAME = "dynamic-configuration";
  private static final String CONFIGURATION_ELEMENT_NAME = "configuration";
  private static final String COMPONENT_ELEMENT_NAME = "component";
  private static final String ID_ATTRIBUTE_NAME = "id";
  private static final String NAME_ATTRIBUTE_NAME = "name";
  private static final String CONFIGURATION_XPATH = "/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME + "/" + CONFIGURATION_ELEMENT_NAME;

	@Reference private MetadataService metadataService;
	@Reference private Services services;
  @Reference private PathResolver pathResolver;
  @Reference private FileManager fileManager;

	public boolean isProjectAvailable() {
		return getPathResolver() != null;
	}
  
  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.Operations#save(java.lang.String)
   */
  public Set<DynConfiguration> save(String name) {
    
    // Get all the dynamic configurations 
    Set<DynConfiguration> configs = services.getConfigurations();
    
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
   * @see org.gvnix.dynamic.configuration.roo.addon.Operations#activate(java.lang.String)
   */
  public Set<DynConfiguration> activate(String name) {
    
    Set<DynConfiguration> configs = null;

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
      services.setConfigurations(configs);
    }

    return configs;
  }

	/**
	 * @return the path resolver or null if there is no user project
	 */
	private PathResolver getPathResolver() {
		ProjectMetadata projectMetadata = (ProjectMetadata) metadataService.get(ProjectMetadata.getProjectIdentifier());
		if (projectMetadata == null) {
			return null;
		}
		return projectMetadata.getPathResolver();
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
