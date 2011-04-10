/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures     
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.theme.roo.addon.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.gvnix.web.theme.roo.addon.Theme;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utilities related to XML mangement.
 * 
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
public abstract class XmlUtils {

  private static Logger logger = Logger.getLogger(XmlUtils.class
      .getName());

  /**
   * Parses and build Document from the given path to XML file
   * 
   * @param is InputStream to parse
   * @return XML Document. Null if file doesn't exist or there is any problem
   * parsing file
   */
  public static Document parseFile(InputStream is) {
    Document doc = null;

    try {
      doc = org.springframework.roo.support.util.XmlUtils.getDocumentBuilder().parse(is);
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
   * Parses and build Document from the given theme descriptor URL to Theme 
   * object.
   * 
   * @param url URL to theme descriptor to parse
   * @return Theme object. Null if there is any problem parsing the stream or
   *         the stream doesn't contain a valid XML.
   */
  public static Theme parseTheme(URL url) {
    try {
      Theme theme = parseTheme(url.openStream());
      theme.setDescriptor(url.toURI());
      return theme;
    }
    catch (IOException e) {
      throw new IllegalStateException("I/O exception.", e);
    }
    catch (URISyntaxException e) {
      throw new IllegalStateException("Error parsing URL to URI.", e);
    }
  }

  /**
   * Parses and build Document from the given theme descriptor URI to Theme 
   * object.
   * 
   * @param uri URI to theme descriptor to parse
   * @return Theme object. Null if there is any problem parsing the stream or
   *         the stream doesn't contain a valid XML.
   */
  public static Theme parseTheme(URI uri) {
    try {
      Theme theme = parseTheme(uri.toURL().openStream());
      theme.setDescriptor(uri);
      return theme;
    }
    catch (IOException e) {
      throw new IllegalStateException("I/O exception.", e);
    }
  }

  /**
   * Parses and build Document from the stream to Theme object.
   * <p>
   * This is an internal utility method, use {@link #parseTheme(URI)} and
   * {@link #parseTheme(URL)} to parse Theme descriptors because the 
   * URI to the theme descriptor is set in the new Theme objet.
   * 
   * @param is InputStream to parse
   * @return Theme object. Null if there is any problem parsing the stream or
   *         the stream doesn't contain a valid XML.
   */
  protected static Theme parseTheme(InputStream is) {
    try {
      
      // load the theme
      Document themeDoc = org.springframework.roo.support.util.XmlUtils.getDocumentBuilder().parse(is);
      Element root = (Element) themeDoc.getDocumentElement();

      // if root element isn't theme, we found invalid theme
      if(!root.getNodeName().equals("gvnix-theme")) {
        throw new IllegalStateException("XML doesn't contain valid Theme.");
      }

      Theme theme = new Theme(root);
      return theme;
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
  }

}
