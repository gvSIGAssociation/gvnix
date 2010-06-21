/*
 * Copyright (C) 2009 - CONSELLERIA D'INFRAESTRUCTURES I TRANSPORT 
 *                      GENERALITAT VALENCIANA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * You may obtain a copy of the License at http://www.gnu.org/licenses/gpl-2.0.html
 */
package org.es.gva.cit.web_binder.roo.addon;

import org.springframework.roo.model.JavaType;
import org.w3c.dom.Document;

public interface WebBinderOperations {

    /**
     * Returns <code>true</true> if setup command must be available for user
     * 
     * @return
     */
    public boolean isSetupAvailable();

    /**
     * Prepares the configuration of XML MVC config file and generates a default
     * class.
     * 
     * <b>Note:</b> currently <code>intializerCalss</code> will be override if
     * it exists.
     * 
     * @param initializerClass
     *            class to generate to use as initializer
     * @param stringEmptyAsNull
     *            Adds to the generated class the
     *            <code>StringTrimmerEditor</code> to prevent persisting empty
     *            strings.
     */
    public void setup(JavaType initializerClass, boolean stringEmptyAsNull);

    /**
     * Returns if <code>drop</code> command should be available.
     * 
     * @return
     */
    public boolean isDropAvailable();

    /**
     * Removes default binding config from XML MVC config file
     * 
     * <b>Note:</b> Currently this dosen't modify the current initializer class
     */
    public void drop();

    /**
     * Returns if <code>add</code> command must be available
     * 
     * @return
     */
    public boolean isAddAvailable();

    /**
     * Add a PropertyEditor configuration to the current initializer class
     * 
     * @param target
     *            class that will be managed by the editor
     * @param editor
     *            editor's class
     */
    public void add(JavaType target, JavaType editor);

    /**
     * Return the asbolute path to the XML MVC config file.
     * 
     * @return
     */
    public String getPathToMvcConfig();

    /**
     * Clear the current Initializer class cache
     */
    public void clearCurrentIntializer();

    /**
     * Returns the current Initializer class
     * 
     * @return
     */
    public JavaType getCurrentInitializer();

    public void renameInitializer(JavaType newType);

    /**
     * Checks if exists AnnotationMethodHandlerAdapter bean
     * 
     * @param mvcXml
     *            webmvc-config.xml document.
     * @return true if exits.
     */
    public boolean hasMvcWebInitBinderNode(Document mvcXml);
}