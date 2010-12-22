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
package org.gvnix.dynamic.configuration.roo.addon.config;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Dynamic configuration base manager of XML files.
 * <p>
 * Extends this class adding the @DynComponent annotation to manage new xml file
 * values.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class XmlDynamicConfiguration implements
    DefaultDynamicConfiguration {
  
  @Reference private PathResolver pathResolver;
  @Reference private FileManager fileManager;

  /**
   * {@inheritDoc}
   */
  public DynPropertyList read() {
    
    MutableFile file = getXmlFile();
    Document doc = getXmlDocument(file);
    
    DynPropertyList dynProps = new DynPropertyList();
    dynProps.addAll(generateProperties("", doc.getChildNodes()));

    return dynProps;
  }

  /**
   * {@inheritDoc}
   */
  public void write(DynPropertyList dynProps) {
    
    MutableFile file = getXmlFile();
    Document doc = getXmlDocument(file);
    Element root = doc.getDocumentElement();
    for (DynProperty dynProp : dynProps) {

      String key = new String (dynProp.getKey());
      
      // Remove possible namespaces
      int end;
      while ((end = key.indexOf(":")) != -1) {
        
        int ini = key.substring(0, end + 1).lastIndexOf("/");
        int ini2 = key.substring(0, end + 1).lastIndexOf("[@");
        if (ini > ini2) {
          String ns = key.substring(ini + 1, end + 1);
          key = key.replace(ns, "");
        }
        else {
          break;
        }
      }

      int index;
      if ((index = key.indexOf("[@")) != -1) {

        // Set the attribute content
        Element elem = XmlUtils.findFirstElement(key.substring(0, index), root);

        String attrName = key.substring(index + 2, key.length() - 1);
        Attr attr = elem.getAttributeNode(attrName);
        attr.setValue(dynProp.getValue());
      }
      else {

        // Set the element content
        Element elem = XmlUtils.findFirstElement(key, root);
        elem.setTextContent(dynProp.getValue());
      }
    }

    XmlUtils.writeXml(file.getOutputStream(), doc);
  }

  /**
   * Get the XML mutable file from the annotation.
   * 
   * @return XML mutable file
   */
  private MutableFile getXmlFile() {
    
    DynamicConfiguration annotation = this.getClass().getAnnotation(
        DynamicConfiguration.class);
    String path = pathResolver.getIdentifier(
        new Path(annotation.path().name()), annotation.relativePath());

    if (fileManager.exists(path)) {

      return fileManager.updateFile(path);
    }
    else {

      throw new IllegalStateException("XML file not found");
    }
  }

  /**
   * Get a document from a file.
   * 
   * @param file Mutable file
   * @return File document
   */
  private Document getXmlDocument(MutableFile file) {

    Document doc = null;
    try {

      // Get the XML file and parse it to document
      DocumentBuilder build = XmlUtils.getDocumentBuilder();
      doc = build.parse(fileManager.getInputStream(file.getCanonicalPath()));
    }
    catch (SAXException se) {

      throw new IllegalStateException("Cant parse the XML file", se);
    }
    catch (IOException ioe) {

      throw new IllegalStateException("Cant read the XML file", ioe);
    }
    
    return doc;
  }

  /**
   * Generate a dynamic property list from a list of XML nodes.
   * <p>
   * Only TEXT_NODE and ATTRIBUTE_NODE nodes are considered. On ATTRIBUTE_NODE
   * nodes references neither are considered.
   * </p>
   * 
   * @param baseName Parent node name of node list
   * @param nodes XML node list to convert
   * @return Dynamic property list
   */
  private DynPropertyList generateProperties(String baseName, NodeList nodes) {

    DynPropertyList dynProps = new DynPropertyList();

    for (int i = 0; i < nodes.getLength(); i++) {

      Node node = nodes.item(i);

      short type = node.getNodeType();
      String content = node.getTextContent();
      String name = node.getNodeName();
      NodeList childs = node.getChildNodes();
      
      int pos = 0;
      int temp = 0;
      for (int j = 0; j < nodes.getLength(); j++) {
        
        if (name.equals(nodes.item(j).getNodeName())) {
          
          if (j > i) {
            
            pos = temp;
            break;          
          }
          else {
            temp++;
          }
        }
      }
      
      if (temp > 1) {
        pos = temp;
      }
      
      String xpath;
      if (pos == 0) {
        xpath = baseName + "/" + name;
      }
      else {
        xpath = baseName + "/" + name + "[" + pos + "]";
      }
      
      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null) {
        for (int j = 0; j < attrs.getLength(); j++) {
         
          Node attr = attrs.item(j);
          String attrName = attr.getNodeName();
          if (!attrName.equals("ref")) {
            dynProps.add(new DynProperty(xpath  + "[@" + attrName + "]" , attr.getNodeValue()));
          }
        }
      }
      
      dynProps.addAll(generateProperties(xpath, childs));
      
      if (type == Node.TEXT_NODE && content.trim().length() > 0) {

        dynProps.add(new DynProperty(baseName, content));
      }
    }

    return dynProps;
  }

}
