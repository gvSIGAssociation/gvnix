/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
package org.gvnix.service.roo.addon.ws.export;

import java.io.File;
import java.util.List;

import org.springframework.roo.model.JavaType;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSExportWsdlConfigService {

    /**
     * <ul>
     * <li>XML_ELEMENT: Associated with @GvNIXXmlElement annotation.</li>
     * <li>WEB_FAULT: Associated with @GvNIXWebFault annotation.</li>
     * <li>WEB_SERVICE: Associated with @GvNIXWebService annotation.</li>
     * </ul>
     */
    public enum GvNIXAnnotationType {

        /**
         * Web Service Xml Annotation type.
         */
        XML_ELEMENT,

        /**
         * Web Fault Web Service Annotation type.
         */
        WEB_FAULT,

        /**
         * Web Service Annotation type.
         */
        WEB_SERVICE,

        /**
         * Web Service Annotation type in interface file.
         */
        WEB_SERVICE_INTERFACE
    };

    /**
     * Generate java source code related to a WSDL with maven plugin.
     * <p>
     * Check correct WSDL format, configure plugin to generate sources and
     * generate java sources.
     * </p>
     * 
     * @param wsdlLocation contract wsdl url to export
     */
    public void generateJavaFromWsdl(String wsdlLocation);

    /**
     * <p>
     * Adds CXF Web Service generated sources directory to Roo FileSystem
     * monitor service.
     * </p>
     * <p>
     * This makes able {@link WSExportWsdlImpl} to collect java file creation
     * events from wsdl2java maven plugin
     * </p>
     * 
     * @param directoryToMonitoring directory to look up for CXF Web Service
     *            generated java files.
     */
    public void monitorFolder(String directoryToMonitoring);

    /**
     * Create gvNIX web service from wsdl2java plugin generation monitoring.
     * <p>
     * Creates java files in 'src/main/java' from result of wsdl2java plugin
     * generation monitoring and creating required gvNIX annotation to identify
     * as a Web Service.
     * </p>
     * <p>
     * This files will be monitoring by {@link WSExportWsdlImpl}.<br/>
     * Create files from each monitored {@link File} lists:
     * <ul>
     * <li>gvNIX xml element list</li>
     * <li>gvNIX web fault list</li>
     * <li>gvNIX web service list</li>
     * </ul>
     * </p>
     * 
     * @return implementation classes
     */
    public List<JavaType> createGvNixClasses();

}
