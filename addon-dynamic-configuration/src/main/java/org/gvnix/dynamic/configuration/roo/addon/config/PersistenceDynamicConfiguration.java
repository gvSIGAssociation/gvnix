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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Dynamic configuration manager of persistence XML.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class PersistenceDynamicConfiguration extends XmlDynamicConfiguration
        implements DefaultDynamicConfiguration {

    private static final Logger logger = HandlerUtils
            .getLogger(PersistenceDynamicConfiguration.class);

    /**
     * {@inheritDoc}
     */
    public String getName() {

        return "Persistence XML";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilePath() {

        return "src/main/resources/META-INF/persistence.xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DynPropertyList read() {

        DynPropertyList dynProps = new DynPropertyList();

        // Get the XML file path
        MutableFile file = getFile();

        // If managed file not exists, nothing to do
        if (file != null) {

            // Obtain the XML file on DOM document format
            Document doc = getXmlDocument(file);

            List<Element> elems = XmlUtils.findElements(getXpath()
                    + XPATH_ARRAY_PREFIX + XPATH_ATTRIBUTE_PREFIX + getKey()
                    + XPATH_ARRAY_SUFIX, doc.getDocumentElement());
            for (Element elem : elems) {

                String key = elem.getAttribute(getKey());
                String value = elem.getAttribute(getValue());
                if (key.isEmpty() || value.isEmpty()) {

                    logger.log(Level.WARNING, "Element key or value not exists");
                    continue;
                }

                dynProps.add(new DynProperty(key, value));
            }
        }

        return dynProps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DynPropertyList dynProps) {

        // Get the XML file path
        MutableFile file = getFile();
        if (file != null) {

            // Obtain the root element of the XML file
            Document doc = getXmlDocument(file);
            Element root = doc.getDocumentElement();

            // Update the root element property values with dynamic properties
            for (DynProperty dynProp : dynProps) {

                String xpath = getXpath() + XPATH_ARRAY_PREFIX
                        + XPATH_ATTRIBUTE_PREFIX + getKey()
                        + XPATH_EQUALS_SYMBOL + XPATH_STRING_DELIMITER
                        + dynProp.getKey() + XPATH_STRING_DELIMITER
                        + XPATH_ARRAY_SUFIX;
                Element elem = XmlUtils.findFirstElement(xpath, root);
                if (elem == null) {

                    logger.log(Level.WARNING, "Element " + xpath
                            + " to set attribute value not exists on file");
                } else {

                    Attr attr = elem.getAttributeNode(getValue());
                    if (attr == null) {

                        logger.log(Level.WARNING, "Element attribute " + xpath
                                + " to set value not exists on file");
                    } else {

                        attr.setValue(dynProp.getValue());
                    }
                }
            }

            // Update the XML file
            XmlUtils.writeXml(file.getOutputStream(), doc);

        } else if (!dynProps.isEmpty()) {

            logger.log(Level.WARNING, "File " + getFilePath()
                    + " not exists and there are dynamic properties to set it");
        }
    }

    protected String getXpath() {

        return "/persistence/persistence-unit/properties/property";
    }

    protected String getKey() {

        return "name";
    }

    protected String getValue() {

        return "value";
    }

}
