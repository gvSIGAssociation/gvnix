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
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Manage configurations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ConfigurationsImpl implements Configurations {

  private static final String DYNAMIC_CONFIGURATION_TEMPLATE_NAME = "dynamic-configuration-template.xml";
  private static final String DYNAMIC_CONFIGURATION_FILE_NAME = "dynamic-configuration.xml";
  private static final String DYNAMIC_CONFIGURATION_ELEMENT_NAME = "dynamic-configuration";
  private static final String CONFIGURATION_ELEMENT_NAME = "configuration";
  private static final String COMPONENT_ELEMENT_NAME = "component";
  private static final String ID_ATTRIBUTE_NAME = "id";
  private static final String NAME_ATTRIBUTE_NAME = "name";
  private static final String CONFIGURATION_XPATH = "/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME + 
      "/" + CONFIGURATION_ELEMENT_NAME;

	@Reference private Services services;
  @Reference private PathResolver pathResolver;
  @Reference private FileManager fileManager;
  
  /**
   * {@inheritDoc}
   */
  public void addConfiguration(DynConfiguration dynConf) {
    
    // Obtain the configuration document
    Document doc = getConfiguration();
    
    // Add new configuration element
    Element conf = doc.createElement(CONFIGURATION_ELEMENT_NAME);
    conf.setAttribute(NAME_ATTRIBUTE_NAME, dynConf.getName());
    doc.getDocumentElement().appendChild(conf);

    // Iterate all child dynamic components of the dynamic configuration
    for (DynComponent dynComps : dynConf.getComponents()) {
      
      // Add new component element
      Element comp = doc.createElement(COMPONENT_ELEMENT_NAME);
      comp.setAttribute(ID_ATTRIBUTE_NAME, dynComps.getId());
      comp.setAttribute(NAME_ATTRIBUTE_NAME, dynComps.getName());
      conf.appendChild(comp);
      
      // Iterate all child dynamic properties of the dynamic component
      for (DynProperty dynProp : dynComps.getProperties()) {

        // Add new property element
        Element prop = doc.createElement(dynProp.getKey());
        prop.setTextContent(dynProp.getValue());
        comp.appendChild(prop);
      }
    }
    
    // Update the configuration file
    saveConfiguration(conf);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteConfiguration(Element conf) {

    // Remove configuration element and their child component elements
    List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME, conf);
    for (Element comp : comps) {
      conf.removeChild(comp);
    }
    conf.getParentNode().removeChild(conf);
    
    // Update the configuration file
    saveConfiguration(conf);
  }
  
  /**
   * {@inheritDoc}
   */
  public DynConfiguration parseConfiguration(Element conf, String name) {
    
    DynConfiguration dynConf = new DynConfiguration();

    // Iterate all child component elements from the configuration element
    List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME, conf);
    for (Element comp : comps) {

      DynPropertyList dynProps = new DynPropertyList();

      // Iterate all child property elements from the component element
      List<Element> props = XmlUtils.findElements("*", comp);
      for (Element prop : props) {

        // If property name specified, only it be considered
        if (name == null || prop.getTagName().equals(name)) {

          // Add new dynamic property
          DynProperty dynProp = new DynProperty(prop.getTagName(), prop
              .getTextContent());
          dynProps.add(dynProp);
        }
      }

      // Add new dynamic component
      DynComponent dynComp = new DynComponent(comp.getAttributes()
          .getNamedItem(ID_ATTRIBUTE_NAME).getNodeValue(), comp.getAttributes()
          .getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue(), dynProps);
      dynConf.addComponent(dynComp);
    }

    // Set name and active properties on the dynamic configuration
    dynConf.setName(conf.getAttribute(NAME_ATTRIBUTE_NAME));
    dynConf.setActive(dynConf.equals(services.getActiveConfiguration()));

    return dynConf;
  }
  
  /**
   * {@inheritDoc}
   */
  public Element findConfiguration(String name) {

    List<Element> confs = XmlUtils.findElements(CONFIGURATION_XPATH + "[@"
        + NAME_ATTRIBUTE_NAME + "='" + name + "']", getConfiguration()
        .getDocumentElement());
    if (confs == null || confs.size() == 0) {

      return null;
    }

    return confs.get(0);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Element> getAllConfigurations() {
    
    return XmlUtils.findElements(CONFIGURATION_XPATH, getConfiguration()
        .getDocumentElement());
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Element> getAllComponents() {

    return XmlUtils.findElements("/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME
        + "/*", getConfiguration().getDocumentElement());
  }

  /**
   * Get the configuration file from disk in a dom document format.
   * 
   * @return Dom document instance of the XML configuration
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
   * Save a configuration element on the configuration file.
   * 
   * @param conf Configuration element to save
   */
  private void saveConfiguration(Element conf) {
    
    String path = getConfigurationFilePath();
    MutableFile file = fileManager.updateFile(path);
    XmlUtils.writeXml(file.getOutputStream(), conf.getOwnerDocument());
  }

  /**
   * Get the configuration file path.
   * <p>
   * If XML configuration file not exists, will be created with a template.
   * </p>
   * 
   * @return Configuration file path
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
  
}
