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
package org.gvnix.service.roo.addon.addon.ws.importt;

import java.io.File;
import java.util.List;

import org.springframework.roo.model.JavaType;
import org.w3c.dom.Document;

/**
 * Addon for Handle Web Service Proxy Layer
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSImportOperations {

    /**
     * Adds the gvNIX import annotation with some wsdl location in a class.
     * 
     * @param className class to add annotation.
     * @param wsdlLocation contract wsdl url to import.
     */
    public void addImportAnnotation(JavaType className, String wsdlLocation);

    /**
     * Returns names of classes which imports web services
     * 
     * @return
     */
    public List<String> getServiceList();

    /**
     * <p>
     * Adds the gvNIX annotation for add signature to request to a import
     * service.
     * </p>
     * <p>
     * <b>Note:</b> <code>certificate</code> certificate file will be copied
     * into resources project folder.<br/>
     * If file already exist, a new name (adding a numeric suffix to base name)
     * will be generated and used.
     * </p>
     * 
     * @param importedServiceClassName class with the imported service to apply
     *        signature
     * @param certificate <code>.p12</code> file to use to sign requests
     * @param password for <code>.p12</code> file
     * @param alias of the certificate from certificate file to use for sing
     *        request
     */
    public void addSignatureAnnotation(JavaType importedServiceClassName,
            File certificate, String password, String alias);

    /**
     * Returns the wsdl document for a proxy class
     * 
     * @param serviceClass
     * @return wsdl or null if it's not a proxy class
     */
    public Document getWSDLFromClass(JavaType serviceClass);

    /**
     * Returns the service name for a wsdl
     * 
     * @param wsdl
     * @return
     */
    public String getServiceName(Document wsdl);

    /**
     * Returns the service name for a wsdl
     * 
     * @param wsdlLocation path
     * @return
     */
    public String getServiceName(String wsdlLocation);

    /**
     * Returns the service name for a class based on
     * <code>GvNIXWebServiceProxy</code> information.
     * 
     * @param serviceClass
     * @return
     */
    public String getServiceName(JavaType serviceClass);

    /**
     * Returns absolute path of a security properties of a imported service
     * class
     * 
     * @param importedServiceClass
     * @return
     */
    public String getSecurityPropertiesAbsolutePath(
            JavaType importedServiceClass);

    /**
     * Returns certificate file name for given imported service class
     * 
     * @param serviceClass
     * @return
     */
    public String getCertificate(JavaType serviceClass);

}
