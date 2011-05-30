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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.apache.felix.scr.annotations.Component;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Abstract dynamic configuration component of XML files.
 * <p>
 * Extends this class to manage new XML file values.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(componentAbstract = true)
public abstract class XmlDynamicConfiguration extends FileDynamicConfiguration {

    private static final String REF_ATTRIBUTE_NAME = "ref";
    private static final String XPATH_ELEMENT_SEPARATOR = "/";
    private static final String XPATH_NAMESPACE_SUFIX = ":";
    private static final String XPATH_ATTRIBUTE_PREFIX = "@";
    private static final String XPATH_ARRAY_SUFIX = "]";
    private static final String XPATH_ARRAY_PREFIX = "[";

    private static final Logger logger = HandlerUtils
            .getLogger(XmlDynamicConfiguration.class);

    /**
     * {@inheritDoc}
     */
    public DynPropertyList read() {

        DynPropertyList dynProps = new DynPropertyList();

        // Get the XML file path
        MutableFile file = getFile();

        // If managed file not exists, nothing to do
        if (file != null) {

            // Obtain the XML file on DOM document format
            Document doc = getXmlDocument(file);

            // Create the dynamic properties list from XML document file
            dynProps.addAll(getProperties("", doc.getChildNodes()));
        }

        return dynProps;
    }

    /**
     * {@inheritDoc}
     */
    public void write(DynPropertyList dynProps) {

        // Get the XML file path
        MutableFile file = getFile();
        if (file != null) {

            // Obtain the root element of the XML file
            Document doc = getXmlDocument(file);
            Element root = doc.getDocumentElement();

            // Update the root element property values with dynamic properties
            setProperties(root, dynProps);

            // Update the XML file
            XmlUtils.writeXml(file.getOutputStream(), doc);
        } else if (!dynProps.isEmpty()) {

            logger.log(Level.WARNING, "File " + getFilePath()
                    + " not exists and there are dynamic properties to set it");
        }
    }

    /**
     * Get a document from a file.
     * 
     * @param file
     *            Mutable file
     * @return File document
     */
    private Document getXmlDocument(MutableFile file) {

        Document doc = null;
        try {

            // Get the XML file and parse it to document
            DocumentBuilder build = XmlUtils.getDocumentBuilder();
            doc = build.parse(fileManager.getInputStream(file
                    .getCanonicalPath()));
        } catch (SAXException se) {

            throw new IllegalStateException("Cant parse the XML file", se);
        } catch (IOException ioe) {

            throw new IllegalStateException("Cant read the XML file", ioe);
        }

        return doc;
    }

    /**
     * Generate a dynamic property list from a list of XML nodes.
     * <p>
     * Only TEXT_NODE, TEXT_NODE and ATTRIBUTE_NODE nodes are considered. On
     * ATTRIBUTE_NODE nodes references neither are considered.
     * </p>
     * 
     * @param baseName
     *            Parent node name of node list
     * @param nodes
     *            XML node list to convert
     * @return Dynamic property list
     */
    private DynPropertyList getProperties(String baseName, NodeList nodes) {

        DynPropertyList dynProps = new DynPropertyList();

        // Iterate all nodes on list
        for (int i = 0; i < nodes.getLength(); i++) {

            Node node = nodes.item(i);

            // Only consider element, text or attribute nodes
            if (isValidNode(node)) {

                // Generate the xpath expression that points to property
                String xpath = getPropertyXpath(baseName, nodes, i);

                // Add dynamic properties related to node attributes
                dynProps.addAll(getPropertyAttributes(node, xpath));

                // Add dynamic properties related to their childs nodes and
                // attributes
                dynProps.addAll(getProperties(xpath, node.getChildNodes()));

                // Add dynamic property related to this node
                String content = node.getTextContent();
                if (node.getNodeType() == Node.TEXT_NODE
                        && content.trim().length() > 0) {

                    dynProps.add(new DynProperty(baseName, content));
                }
            }
        }

        return dynProps;
    }

    /**
     * The node is a valid type to be generated as dynamic property ?
     * <p>
     * TEXT_NODE, TEXT_NODE and ATTRIBUTE_NODE nodes are valid.
     * </p>
     * 
     * @param node
     *            Node to check
     * @return Has valid format
     */
    private boolean isValidNode(Node node) {

        short type = node.getNodeType();
        if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE
                || type == Node.ATTRIBUTE_NODE) {

            return true;
        }

        return false;
    }

    /**
     * Create the dynamic properties related to this node attributes.
     * 
     * @param node
     *            Node to add to list
     * @param xpath
     *            Xpath expression of this node
     * @return Dynamic property list
     */
    private DynPropertyList getPropertyAttributes(Node node, String xpath) {

        DynPropertyList dynProps = new DynPropertyList();

        // Iterate all node attributes, if exists
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int j = 0; j < attrs.getLength(); j++) {

                // Get attribute and it name
                Node attr = attrs.item(j);
                String attrName = attr.getNodeName();

                // Create dynamic property, except attribute references
                if (!attrName.equals(REF_ATTRIBUTE_NAME)) {
                    dynProps.add(new DynProperty(xpath + XPATH_ARRAY_PREFIX
                            + XPATH_ATTRIBUTE_PREFIX + attrName
                            + XPATH_ARRAY_SUFIX, attr.getNodeValue()));
                }
            }
        }

        return dynProps;
    }

    /**
     * Generate the xpath expression that points to property.
     * <p>
     * The xpath expression could be an array or not.
     * </p>
     * 
     * @param baseName
     *            Base name of current node
     * @param nodes
     *            List of brother nodes
     * @param i
     *            Index of current node
     * @return Xpath expression
     */
    private String getPropertyXpath(String baseName, NodeList nodes, int i) {

        // Get current node name
        String name = nodes.item(i).getNodeName();

        // Iterate brother nodes searching other nodes with same name
        int index = 0;
        int temp = 0;
        for (int j = 0; j < nodes.getLength(); j++) {

            // If exists a brother node with same name and is valid
            Node node = nodes.item(j);
            if (name.equals(node.getNodeName()) && isValidNode(node)) {

                // Calculate this node index related to brother nodes with same
                // name
                if (j > i) {

                    index = temp;
                    break;
                } else {
                    temp++;
                }
            }
        }

        // If temp greater than 1, this node is part of an array on index
        // position
        if (temp > 1) {
            index = temp;
        }

        if (index == 0) {

            // Xpath expression of an element
            return baseName + XPATH_ELEMENT_SEPARATOR + name;
        } else {

            // Xpath expression of an element array
            return baseName + XPATH_ELEMENT_SEPARATOR + name
                    + XPATH_ARRAY_PREFIX + index + XPATH_ARRAY_SUFIX;
        }
    }

    /**
     * Remove possible namespaces from a xpath expression.
     * 
     * @param xpath
     *            Xpath expressión with optional namespaces
     * @return Xpath expresion without namespaces, if exists
     */
    private String removeNamespaces(String xpath) {

        // Find all namespace separators sufix
        int end;
        while ((end = xpath.indexOf(XPATH_NAMESPACE_SUFIX)) != -1) {

            // Namespace starts on element separator if attribute prefix not
            // before
            String substr = xpath.substring(0, end + 1);
            int ini = substr.lastIndexOf(XPATH_ELEMENT_SEPARATOR);
            int ini2 = substr.lastIndexOf(XPATH_ATTRIBUTE_PREFIX);
            if (ini > ini2) {

                // Remove namespace substring from xpath
                xpath = xpath.replace(xpath.substring(ini + 1, end + 1), "");
            } else {

                // If attribute prefix before, is an attribute not a namespace
                break;
            }
        }

        return xpath;
    }

    /**
     * Update the root element property values with dynamic properties.
     * 
     * @param root
     *            Parent element
     * @param dynProps
     *            Dynamic property list
     */
    private void setProperties(Element root, DynPropertyList dynProps) {

        // Iterate all dynamic properties to update
        for (DynProperty dynProp : dynProps) {

            // Remove possible namespaces
            String xpath = removeNamespaces(dynProp.getKey());

            // If attribute prefix present, there is an attribute else an
            // element
            int index;
            if ((index = xpath.indexOf(XPATH_ARRAY_PREFIX
                    + XPATH_ATTRIBUTE_PREFIX)) != -1) {

                // Set the new attribute value through container element
                Element elem = XmlUtils.findFirstElement(
                        xpath.substring(0, index), root);
                if (elem == null) {

                    logger.log(Level.WARNING, "Element " + xpath
                            + " to set attribute value not exists on file");
                } else {

                    String name = xpath
                            .substring(index + 2, xpath.length() - 1);
                    Attr attr = elem.getAttributeNode(name);
                    if (attr == null) {

                        logger.log(Level.WARNING, "Element attribute " + xpath
                                + " to set value not exists on file");
                    } else {

                        attr.setValue(dynProp.getValue());
                    }
                }
            } else {

                // Set the new element content
                Element elem = XmlUtils.findFirstElement(xpath, root);

                if (elem == null) {

                    logger.log(Level.WARNING, "Element " + xpath
                            + " to set text content not exists on file");
                } else {

                    elem.setTextContent(dynProp.getValue());
                }
            }
        }
    }

}
