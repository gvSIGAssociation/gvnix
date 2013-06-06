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
package org.gvnix.service.roo.addon.ws;

import java.io.IOException;

import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.w3c.dom.Document;

/**
 * Service to manage the Web Services.
 * <p>
 * It is required to check the configuration with install(CommunicationSense)
 * before executing any operation.
 * </p>
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSConfigService {

    /**
     * Sense of the Communication.
     * <ul>
     * <li>EXPORT: From this to external systems.</li>
     * <li>EXPORT_WSDL: From external systems to this WSDL2JAVA.</li>
     * <li>IMPORT: From external systems to this.</li>
     * <li>IMPORT_RPC_ENCODED: From external systems to this RPC Encoded.</li>
     * </ul>
     */
    public enum WsType {
        EXPORT, EXPORT_WSDL, IMPORT, IMPORT_RPC_ENCODED
    };

    /**
     * Maven command for sources generation
     */
    static final String GENERATE_SOURCES = "clean generate-sources";

    /**
     * User message when maven is generating sources
     */
    static final String GENERATE_SOURCES_INFO = "Generating sources";

    /**
     * Install web service library, if not already installed.
     * <ul>
     * <li>Add repository and dependency with this addon (annotations from this
     * addon are used along project)</li>
     * <li>Installs jax2ws plugin</li>
     * <li>Add library dependencies</li>
     * <li>Install library configuration file when export (CXF)</li>
     * <li>Add library version properties</li>
     * </ul>
     * <p>
     * When true returned (properties changes) sources regeneration is required
     * to avoid errors compilation errors.
     * </p>
     * 
     * @param type Web service type to install library
     * @return Project properties changed ? If true source regeneration required
     */
    public boolean install(WsType type);

    /**
     * Publish a class as Web Service.
     * <p>
     * Define a Web Service class in cxf configuration file to be published.
     * <p>
     * <p>
     * Update cxf file if its necessary to avoid changes in WSDL contract
     * checking type annotation values from service class.
     * </p>
     * 
     * @param className to be published as Web Service.
     * @param annotationMetadata with all necessary values to define a Web
     *        Service.
     * @return true if class annotation must be updated (Class name or package
     *         has been changed).
     */
    public boolean publishClassAsWebService(JavaType className,
            AnnotationMetadata annotationMetadata);

    /**
     * Converts package name to a Target Namespace.
     * 
     * @param packageName Initial String split with dots.
     * @return Initial String reverted the order.
     */
    public String convertPackageToTargetNamespace(String packageName);

    /**
     * <p>
     * Adds wsdl generation configuration in pom.xml using java2ws maven plugin
     * for a Web Service class
     * </p>
     * <p>
     * Installs java2ws plugin if it's needed
     * </p>
     * 
     * @param serviceClass Service to generate Wsdl.
     * @param serviceName Service name for wsdl file.
     * @param addressName Address to access the service.
     * @param fullyQualifiedTypeName class name location defined in annotation.
     */
    public void addToJava2wsPlugin(JavaType serviceClass, String serviceName,
            String addressName, String fullyQualifiedTypeName);

    /**
     * Add a wsdl location to import.
     * <p>
     * Adds a wsdl location to the plugin configuration. If code generation
     * plugin configuration not exists, it will be created.
     * </p>
     * 
     * @param wsdlLocation WSDL file location
     * @param type Communication sense type
     * @return wsdl location added, or false if already exists
     */
    public boolean addImportLocation(String wsdlLocation, WsType type);

    /**
     * <p>
     * Add a wsdl location to export.
     * </p>
     * <p>
     * Adds a wsdl location to the plugin configuration. If code generation
     * plugin configuration not exists, it will be created.
     * </p>
     * 
     * @param wsdlLocation WSDL file location
     * @param wsdlDocument WSDL file
     * @return
     */
    public boolean addWsdlLocation(String wsdlLocation, Document wsdlDocument);

    /**
     * Imports a Web Service to class.
     * 
     * @param className class to import
     * @param wsdlLocation contract wsdl url to import
     * @param type Communication sense type
     * @return Generate sources required ?
     */
    public boolean importService(JavaType className, String wsdlLocation,
            WsType type);

    /**
     * Run maven command with parameters.
     * <p>
     * On development mode maven details will be showed, else input message.
     * </p>
     * 
     * @param parameters to run with maven.
     * @param message Information showed if no development mode.
     * @throws IOException
     */
    public void mvn(String parameters, String message) throws IOException;

    /**
     * Remove Cxf wsdl2java plugin execution from pom.xml.
     */
    public void disableWsdlLocation();

}
