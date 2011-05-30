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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private static final String PROPERTY_ELEMENT_NAME = "property";
    private static final String KEY_ELEMENT_NAME = "key";
    private static final String VALUE_ELEMENT_NAME = "value";
    private static final String ACTIVE_ELEMENT_NAME = "active";
    private static final String BASE_ELEMENT_NAME = "base";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String CONFIGURATION_XPATH = "/"
            + DYNAMIC_CONFIGURATION_ELEMENT_NAME + "/"
            + CONFIGURATION_ELEMENT_NAME;
    private static final String ACTIVE_CONFIGURATION_XPATH = "/"
            + DYNAMIC_CONFIGURATION_ELEMENT_NAME + "/" + ACTIVE_ELEMENT_NAME;
    private static final String BASE_CONFIGURATION_XPATH = "/"
            + DYNAMIC_CONFIGURATION_ELEMENT_NAME + "/" + BASE_ELEMENT_NAME;

    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;

    /**
     * {@inheritDoc}
     */
    public void addConfiguration(DynConfiguration dynConf) {

        // Obtain the configuration document
        Document doc = getConfigurationDocument();

        // Add new configuration element
        Element conf = doc.createElement(CONFIGURATION_ELEMENT_NAME);
        conf.setAttribute(NAME_ATTRIBUTE_NAME, dynConf.getName());
        doc.getDocumentElement().appendChild(conf);

        // Iterate all child dynamic components of the dynamic configuration
        for (DynComponent dynComps : dynConf.getComponents()) {

            Element comp = addComponent(conf, dynComps.getId(),
                    dynComps.getName());

            // Iterate all child dynamic properties of the dynamic component
            for (DynProperty dynProp : dynComps.getProperties()) {

                // Add new property on component
                addProperty(comp, dynProp.getKey(), dynProp.getValue());
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
        List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME,
                conf);
        for (Element comp : comps) {
            conf.removeChild(comp);
        }
        conf.getParentNode().removeChild(conf);

        // If active configuration, remove the reference on configuration file
        Element activeConf = isActiveConfiguration(conf);
        if (activeConf != null) {
            activeConf.setTextContent("");
        }

        // Update the configuration file
        saveConfiguration(conf);
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration parseConfiguration(Element conf, String property) {

        DynConfiguration dynConf = new DynConfiguration();

        // Iterate all child component elements from the configuration element
        List<Element> comps = XmlUtils.findElements(COMPONENT_ELEMENT_NAME,
                conf);
        for (Element comp : comps) {

            DynComponent dynComp = parseComponent(comp, property);
            if (dynComp != null) {
                dynConf.addComponent(dynComp);
            }
        }

        // Set name property of dynamic configuration
        dynConf.setName(conf.getAttribute(NAME_ATTRIBUTE_NAME));

        // If configuration is active, mark it
        if (isActiveConfiguration(conf) != null) {
            dynConf.setActive(true);
        }

        return dynConf;
    }

    /**
     * {@inheritDoc}
     */
    public DynComponent parseComponent(Element comp, String name) {

        // If property name specified, only it be considered
        List<Element> props;
        if (name == null) {

            props = XmlUtils.findElements("*", comp);
        } else {
            props = XmlUtils.findElements(PROPERTY_ELEMENT_NAME + "/"
                    + KEY_ELEMENT_NAME + "[text()='" + name + "']/..", comp);
        }

        // Iterate all child property elements from the component element
        DynPropertyList dynProps = new DynPropertyList();
        for (Element prop : props) {

            dynProps.add(parseProperty(prop));
        }

        if (dynProps.size() == 0) {
            return null;
        }

        // Add new dynamic component
        NamedNodeMap attributes = comp.getAttributes();
        return new DynComponent(attributes.getNamedItem(ID_ATTRIBUTE_NAME)
                .getNodeValue(), attributes.getNamedItem(NAME_ATTRIBUTE_NAME)
                .getNodeValue(), dynProps);
    }

    /**
     * {@inheritDoc}
     */
    public DynProperty parseProperty(Element prop) {

        // Create a dynamic property from property element
        NodeList childs = prop.getChildNodes();
        Node key = childs.item(1);
        Node value = childs.item(3);
        return new DynProperty(key.getTextContent(), value.getTextContent());
    }

    /**
     * {@inheritDoc}
     */
    public Element findConfiguration(String name) {

        return XmlUtils.findFirstElement(CONFIGURATION_XPATH + "[@"
                + NAME_ATTRIBUTE_NAME + "='" + name + "']",
                getConfigurationDocument().getDocumentElement());
    }

    /**
     * {@inheritDoc}
     */
    public Element getBaseConfiguration() {

        return XmlUtils.findFirstElement(BASE_CONFIGURATION_XPATH,
                getConfigurationDocument().getDocumentElement());
    }

    /**
     * {@inheritDoc}
     */
    public List<Element> getAllConfigurations() {

        return XmlUtils.findElements(CONFIGURATION_XPATH,
                getConfigurationDocument().getDocumentElement());
    }

    /**
     * {@inheritDoc}
     */
    public List<Element> getAllComponents() {

        return XmlUtils.findElements("/" + DYNAMIC_CONFIGURATION_ELEMENT_NAME
                + "/" + CONFIGURATION_ELEMENT_NAME, getConfigurationDocument()
                .getDocumentElement());
    }

    /**
     * {@inheritDoc}
     */
    public Element getProperty(String configuration, String property) {

        // TODO Several properties with same name can exist at different
        // components
        return XmlUtils.findFirstElement(CONFIGURATION_XPATH + "[@"
                + NAME_ATTRIBUTE_NAME + "='" + configuration + "']" + "/"
                + COMPONENT_ELEMENT_NAME + "/" + PROPERTY_ELEMENT_NAME + "/"
                + KEY_ELEMENT_NAME + "[text()='" + property + "']/..",
                getConfigurationDocument().getDocumentElement());
    }

    /**
     * {@inheritDoc}
     */
    public void saveConfiguration(Element elem) {

        String path = getConfigurationFilePath();
        MutableFile file = fileManager.updateFile(path);
        XmlUtils.writeXml(file.getOutputStream(), elem.getOwnerDocument());
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration getActiveConfiguration() {

        DynConfiguration dynConf = null;

        Element elem = XmlUtils.findFirstElement(ACTIVE_CONFIGURATION_XPATH,
                getConfigurationDocument().getDocumentElement());
        if (elem != null) {

            Element active = findConfiguration(elem.getTextContent());
            if (active != null) {
                dynConf = parseConfiguration(active, null);
            }
        }

        return dynConf;
    }

    /**
     * {@inheritDoc}
     */
    public void setActiveConfiguration(String name) {

        // Get active configuration element
        Element elem = XmlUtils.findFirstElement(ACTIVE_CONFIGURATION_XPATH,
                getConfigurationDocument().getDocumentElement());
        elem.setTextContent(name);
        saveConfiguration(elem);
    }

    /**
     * {@inheritDoc}
     */
    public void addProperties(String name, String value, String compId,
            String compName) {

        // Add property in all stored configurations
        List<Element> confs = getAllConfigurations();
        for (Element conf : confs) {

            addProperty(name, value, compId, compName, conf);
        }

        // Add property in base configuration
        addProperty(name, "", compId, compName, getBaseConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    public void deleteProperties(String name, String component) {

        // Delete property in all stored configurations
        List<Element> confs = getAllConfigurations();
        for (Element conf : confs) {

            deleteProperty(name, component, conf);
        }

        // Delete property in base configuration
        deleteProperty(name, component, getBaseConfiguration());
    }

    /**
     * Delete a component property name on a configurations.
     * 
     * @param name
     *            Property name
     * @param component
     *            Component id
     * @param conf
     *            Configuration element
     */
    private void deleteProperty(String name, String component, Element conf) {

        // Find component and property to delete
        Element comp = getComponent(component, conf);
        Element prop = XmlUtils.findFirstElement(PROPERTY_ELEMENT_NAME + "/"
                + KEY_ELEMENT_NAME + "[text()='" + name + "']/..", comp);
        if (prop != null) {

            // Remove child property and component if no more properties
            comp.removeChild(prop);
            if (XmlUtils.findFirstElement(PROPERTY_ELEMENT_NAME, comp) == null) {

                conf.removeChild(comp);
            }

            // Save configuration
            saveConfiguration(conf);
        }
    }

    /**
     * Add a component property with some name and value on a configuration.
     * 
     * @param name
     *            Property name
     * @param value
     *            Property value
     * @param compId
     *            Component id
     * @param compId
     *            Component name
     * @param conf
     *            Configuration element
     */
    private void addProperty(String name, String value, String compId,
            String compName, Element conf) {

        // Get component or create it if not exists
        Element comp = getComponent(compId, conf);
        if (comp == null) {

            comp = addComponent(conf, compId, compName);
        }

        // Add property on component and save configuration
        addProperty(comp, name, value);
        saveConfiguration(conf);
    }

    /**
     * Get a component element with some name from a configuration element.
     * 
     * @param id
     *            Component identificador
     * @param conf
     *            Configuration element
     * @return Component element
     */
    private Element getComponent(String id, Element conf) {

        // Find required component of configuration or create if not exists
        return XmlUtils.findFirstElement(COMPONENT_ELEMENT_NAME + "[@"
                + ID_ATTRIBUTE_NAME + "='" + id + "']", conf);
    }

    /**
     * Add new property element containing key and value elements on component.
     * 
     * @param comp
     *            Component where add property
     * @param key
     *            Property key
     * @param value
     *            Property value
     */
    private void addProperty(Element comp, String key, String value) {

        // Get document and create property element
        Document doc = comp.getOwnerDocument();
        Element propElem = doc.createElement(PROPERTY_ELEMENT_NAME);

        // Create key
        Element keyElem = doc.createElement(KEY_ELEMENT_NAME);
        keyElem.setTextContent(key);
        propElem.appendChild(keyElem);

        // Create value
        Element valueElem = doc.createElement(VALUE_ELEMENT_NAME);
        valueElem.setTextContent(value);
        propElem.appendChild(valueElem);

        // Add property on component
        comp.appendChild(propElem);
    }

    /**
     * Add new component element containing id and name elements on
     * configuration.
     * 
     * @param conf
     *            Configuration where add component
     * @param id
     *            Component identificator
     * @param name
     *            Component name
     */
    private Element addComponent(Element conf, String id, String name) {

        // Add new component element
        Document doc = conf.getOwnerDocument();
        Element comp = doc.createElement(COMPONENT_ELEMENT_NAME);
        comp.setAttribute(ID_ATTRIBUTE_NAME, id);
        comp.setAttribute(NAME_ATTRIBUTE_NAME, name);
        conf.appendChild(comp);
        return comp;
    }

    /**
     * Check if a configuration is the active.
     * 
     * @param conf
     *            Configuration element
     * @return Active element or null if is not the active
     */
    private Element isActiveConfiguration(Element conf) {

        // Get active configuration element
        Element activeConf = XmlUtils.findFirstElement(
                ACTIVE_CONFIGURATION_XPATH, conf.getOwnerDocument()
                        .getDocumentElement());

        // Mark the configuration as active if same than this
        String name = conf.getAttribute(NAME_ATTRIBUTE_NAME);

        if (activeConf != null && activeConf.getTextContent().equals(name)) {

            return activeConf;
        }

        return null;
    }

    /**
     * Get the configuration file from disk in a dom document format.
     * 
     * @return Dom document instance of the XML configuration
     */
    private Document getConfigurationDocument() {

        Document doc;
        try {

            String path = getConfigurationFilePath();
            DocumentBuilder build = XmlUtils.getDocumentBuilder();
            doc = build.parse(fileManager.getInputStream(path));
        } catch (SAXException se) {

            throw new IllegalStateException(
                    "Cant parse the configuration file", se);
        } catch (IOException ioe) {

            throw new IllegalStateException("Cant read the configuration file",
                    ioe);
        }

        return doc;
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
                        DYNAMIC_CONFIGURATION_TEMPLATE_NAME), fileManager
                        .createFile(path).getOutputStream());
            } catch (IOException ioe) {

                throw new IllegalStateException(ioe);
            }
        }

        return path;
    }

}
