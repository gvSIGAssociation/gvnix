/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.menu.roo.addon.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utilities related to XML management.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.6
 */
public abstract class XmlUtils {

    private static Logger logger = Logger.getLogger(XmlUtils.class.getName());

    /**
     * Parses and build Document from the given path to XML file
     * 
     * @param is InputStream to parse
     * @return XML Document. Null if file doesn't exist or there is any problem
     *         parsing file
     */
    public static Document parseFile(InputStream is) {
        Document doc = null;

        try {
            doc = org.springframework.roo.support.util.XmlUtils
                    .getDocumentBuilder().parse(is);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error parsing XML", e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
        return doc;
    }

    /**
     * Check if the given Element contains child nodes that match with the given
     * child name.
     * 
     * @param element
     * @param name The name of the child tag to match on. The special value "*"
     *        matches all tags.
     * @return
     */
    public static boolean hasChildNodes(Element element, String name) {
        NodeList elements = element.getElementsByTagName(name);
        return elements.getLength() > 0;
    }
}
