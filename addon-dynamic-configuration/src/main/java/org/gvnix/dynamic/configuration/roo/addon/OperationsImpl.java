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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
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
public class OperationsImpl implements Operations {

  private static final String DYNAMIC_CONFIGURATION_TEMPLATE_NAME = "dynamic-configuration-template.xml";
  private static final String DYNAMIC_CONFIGURATION_FILE_NAME = "dynamic-configuration.xml";
  private static final String DYNAMIC_CONFIGURATION_ELEMENT_NAME = "dynamic-configuration";
  private static final String CONFIGURATION_ELEMENT_NAME = "configuration";
  private static final String COMPONENT_ELEMENT_NAME = "component";
  private static final String ID_ATTRIBUTE_NAME = "id";
  private static final String NAME_ATTRIBUTE_NAME = "name";
  private static final String CONFIGURATION_XPATH = "/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME + 
      "/" + CONFIGURATION_ELEMENT_NAME;

	@Reference private MetadataService metadataService;
	@Reference private Services services;
  @Reference private PathResolver pathResolver;
  @Reference private FileManager fileManager;

	public boolean isProjectAvailable() {
		return getPathResolver() != null;
	}
  
  /**
   * {@inheritDoc}
   */
  public DynConfiguration saveActiveConfiguration(String name) {
    
    // Get the active configuration 
    DynConfiguration dynConfig = services.getActiveConfiguration();
    
    // Get the XML configuration file as a dom document
    Document doc = getConfiguration();
    
    // Find the dom configuration with requested name
    Element root = doc.getDocumentElement();
    List<Element> confs = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", root);
    if (confs != null && confs.size() > 0) {
      
      // If configuration already exists, delete it
      deleteConfiguration(confs.get(0));
    }

    // Create the configuration
    dynConfig.setName(name);
    addConfiguration(dynConfig, root);
    saveConfiguration(doc);

    return dynConfig;
  }
  
  /**
   * {@inheritDoc}
   */
  public DynConfiguration setActiveConfiguration(String name) {
    
    DynConfiguration dynConfig = null;

    // Get the XML configuration file as a dom document
    Document doc = getConfiguration();
    
    // Find the dom configuration with requested name
    Element root = doc.getDocumentElement();
    List<Element> confs = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", root);
    if (confs != null && confs.size() > 0) {

      dynConfig = parseConfiguration(confs.get(0), null);
      services.setActiveConfiguration(dynConfig);
    }

    return dynConfig;
  }
  
  /**
   * {@inheritDoc}
   */
  public DynConfigurationList findConfigurations() {
    
    DynConfigurationList dynConfs = new DynConfigurationList();
    
    // Get the XML configuration file as a dom document
    Document document = getConfiguration();
    
    // Find all the dom configurations
    Element root = document.getDocumentElement();
    List<Element> confs = XmlUtils.findElements(CONFIGURATION_XPATH, root);
    for (Element conf : confs) {
      
      dynConfs.add(parseConfiguration(conf, null));
    }
    
    return dynConfs;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean deleteConfiguration(String name) {
    
    Document doc = getConfiguration();
    
    // Find the dom configuration with requested name
    Element root = doc.getDocumentElement();
    List<Element> confs = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", root);
    if (confs != null && confs.size() > 0) {
      
      // If configuration already exists, delete it
      deleteConfiguration(confs.get(0));
      saveConfiguration(doc);
      
      return true;
    }

    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public DynConfiguration getConfiguration(String name) {
    
    DynConfiguration dynConf = null;
    
    // Get the XML configuration file as a dom document
    Document document = getConfiguration();
    
    // Find the dom configuration with requested name
    Element root = document.getDocumentElement();
    List<Element> confs = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", root);
    if (confs != null && confs.size() > 0) {
      
      dynConf = parseConfiguration(confs.get(0), null);
    }
    
    return dynConf;
  }
  
  /**
   * {@inheritDoc}
   */
  public DynConfigurationList getProperties(String name) {
    
    DynConfigurationList dynConfs = new DynConfigurationList();
    
    // Get the XML configuration file as a dom document
    Document document = getConfiguration();
    
    // Find the dom configuration with requested name
    Element root = document.getDocumentElement();
    List<Element> confs = XmlUtils.findElements("/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME + "/*", root);
    if (confs != null && confs.size() > 0) {
      
      for (Element conf : confs) {
        
        DynConfiguration dynConf = parseConfiguration(conf, name);
        dynConfs.add(dynConf);
      }
    }
    
    return dynConfs;
  }
  
  /**
   * Create a new configuration dom element on the root element.
   * 
   * @param configs Dynamic configuration to store on dom configuration
   * @param root Element to store the dom configuration
   */
  private void addConfiguration(DynConfiguration dynConf, Element root) {

    Document doc = root.getOwnerDocument();
    Element conf = doc.createElement(CONFIGURATION_ELEMENT_NAME);
    conf.setAttribute(NAME_ATTRIBUTE_NAME, dynConf.getName());
    root.appendChild(conf);

    for (DynComponent dynComps : dynConf.getComponents()) {
      
      Element comp = doc.createElement(COMPONENT_ELEMENT_NAME);
      comp.setAttribute(ID_ATTRIBUTE_NAME, dynComps.getId());
      comp.setAttribute(NAME_ATTRIBUTE_NAME, dynComps.getName());
      conf.appendChild(comp);
      
      for (DynProperty dynProp : dynComps.getProperties()) {

        Element prop = doc.createElement(dynProp.getKey());
        prop.setTextContent(dynProp.getValue());
        comp.appendChild(prop);
      }
    }
  }

  /**
   * Drop an existing configuration dom element.
   * 
   * @param conf Dom element to remove 
   */
  private void deleteConfiguration(Element conf) {
    
    List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME, conf);
    for (Element comp : comps) {
      conf.removeChild(comp);
    }
    conf.getParentNode().removeChild(conf);
  }
  
  /**
   * Get the configuration from disk in a dom document.
   * 
   * @return Dom document
   */
  private Document getConfiguration() {

    Document doc;
    try {

      String path = getConfigurationFilePath();
      DocumentBuilder build = XmlUtils.getDocumentBuilder();
      doc = build.parse(fileManager.getInputStream(path));
    }
    catch (SAXException se) {

      throw new IllegalStateException("Cant parse the configuration file", se);
    }
    catch (IOException ioe) {

      throw new IllegalStateException("Cant read the configuration file", ioe);
    }

    return doc;
  }
  
  /**
   * Save the configuration to disk from a dom document .
   * 
   * @param doc Dom document
   */
  private void saveConfiguration(Document doc) {
  
    String path = getConfigurationFilePath();
    MutableFile file = fileManager.updateFile(path);
    XmlUtils.writeXml(file.getOutputStream(), doc);
  }
  
  /**
   * Get the main XML configuration file path of dynamic configurations.
   * <p>
   * If XML configuration file not exists, will be created with a template.
   * </p>
   * 
   * @return dynamic configuration XML file path
   */
  private String getConfigurationFilePath() {

    String path = pathResolver.getIdentifier(Path.SRC_MAIN_RESOURCES,
        DYNAMIC_CONFIGURATION_FILE_NAME);
    if (!fileManager.exists(path)) {
      try {

        FileCopyUtils.copy(TemplateUtils.getTemplate(getClass(),
            DYNAMIC_CONFIGURATION_TEMPLATE_NAME), fileManager.createFile(path)
            .getOutputStream());
      }
      catch (IOException ioe) {

        throw new IllegalStateException(ioe);
      }
    }

    return path;
  }
  
  /**
   * Parse a configuration dom element to a dynamic configuration.
   * 
   * @param conf Configuration dom element
   * @param name Property name to parse or all if null
   * @return Dynamic configuration
   */
  private DynConfiguration parseConfiguration(Element conf, String name) {

    DynConfiguration dynConf = new DynConfiguration();

    List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME, conf);
    for (Element comp : comps) {

      DynPropertyList dynProps = new DynPropertyList();

      List<Element> props = XmlUtils.findElements("*", comp);
      for (Element prop : props) {
        
        if (name == null || prop.getTagName().equals(name)) {

          DynProperty dynProp = new DynProperty(prop.getTagName(), prop
              .getTextContent());
          dynProps.add(dynProp);
        }
      }

      DynComponent dynComp = new DynComponent(comp.getAttributes()
          .getNamedItem(ID_ATTRIBUTE_NAME).getNodeValue(), comp.getAttributes()
          .getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue(), dynProps);
      dynConf.addComponent(dynComp);
    }

    dynConf.setName(conf.getAttribute(NAME_ATTRIBUTE_NAME));
    dynConf.setActive(dynConf.equals(services.getActiveConfiguration()));

    return dynConf;
  }
  
  /**
   * @return the path resolver or null if there is no user project
   */
  private PathResolver getPathResolver() {

    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
        .get(ProjectMetadata.getProjectIdentifier());
    if (projectMetadata == null) {

      return null;
    }

    return projectMetadata.getPathResolver();
  }

}
