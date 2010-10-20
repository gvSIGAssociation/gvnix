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
package org.gvnix.service.layer.roo.addon;

import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;

/**
 * Service to manage the Web Services.
 * 
 * <p>
 * It is required to check the configuration with install(CommunicationSense)
 * before executing any operation.
 * </p>
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerWsConfigService {
    
    /**
     * Sense of the Communication.
     * <ul>
     * <li>EXPORT: From this to external systems.</li>
     * <li>IMPORT: From external systems to this.</li>
     * </ul>
     */
    public enum CommunicationSense { EXPORT, IMPORT, IMPORT_RPC_ENCODED };

    /**
     * Install and configure Web Service library, if not already installed.
     * 
     * @param type Communication type
     */
    public void install(CommunicationSense type);

    /**
     * Publish a class as Web Service.
     * 
     * @param className
     *            to be published as Web Service.
     * @param annotationMetadata
     *            with all necessary values to define a Web Service.
     * 
     * @return true if className has changed to update annotation.
     */
    public boolean exportClass(JavaType className,
	    AnnotationMetadata annotationMetadata);

    /**
     * Converts package name to a Target Namespace.
     * 
     * @param packageName
     *            Initial String split with dots.
     * @return Initial String reverted the order.
     */
    public String convertPackageToTargetNamespace(String packageName);

    /**
     * Configure Web Service class to generate wsdl contract in jax2ws plugin in
     * pom.xml.
     * 
     * @param serviceClass
     *            Service to generate Wsdl.
     * @param serviceName
     *            Service name for wsdl file.
     * @param addressName
     *            Address to access the service.
     * @param fullyQualifiedTypeName
     *            class name location defined in annotation.
     */
    public void jaxwsBuildPlugin(JavaType serviceClass, String serviceName,
	    String addressName, String fullyQualifiedTypeName);
    
    /**
     * Add a wsdl location to import.
     * 
     * @param wsdlLocation
     *            WSDL file location
     * @param type
     *            Communication sense type
     */
    public void addImportLocation(String wsdlLocation, CommunicationSense type);

    /**
     * Imports a Web Service to class.
     * 
     * @param className
     *            class to import
     * @param wsdlLocation
     *            contract wsdl url to import
     * @param type
     *            Communication sense type
     */
    public void importService(JavaType className, String wsdlLocation,
	    CommunicationSense type);

    /**
     * Create Jax-WS plugin configuration in pom.xml.
     */
    public void installJaxwsBuildPlugin();

    /**
     * Checks if library is properly configured in a project.
     * 
     * <p>
     * Library dependencies can be different depending of communication sense.
     * </p>
     * 
     * @param type
     *            Communication type
     * @return true or false if it's configurated
     */
    public boolean isLibraryInstalled(CommunicationSense type);

    /**
     * Is this a web project ?
     * 
     * @return true if this is a web project.
     */
    boolean isProjectWebAvailable();
    
    /**
     * Is this a project ?
     * 
     * @return true if this is a project.
     */
    boolean isProjectAvailable();

}
