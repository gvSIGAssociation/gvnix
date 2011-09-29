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
package org.gvnix.service.roo.addon.ws.export;

import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.ws.export.WSExportOperations.MethodParameterType;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaType;

/**
 * Utility component to Export Web services
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSExportValidationService {

    /**
     * Check method return and parameters types allowed.
     * 
     * <p>
     * Adds {@link GvNIXXmlElement} annotation to allowed types which needs it.
     * </p>
     * 
     * <p>
     * If exists any disallowed JavaType in operation: Cancel all the process
     * and show a message explaining that it's not possible to publish this
     * operation because the parameter can't be Marshalled according Ws-I
     * standards.
     * </p>
     * 
     * @param method
     *            Method to check valid return and type parameters.
     */
    public void prepareAllowedJavaTypes(MethodMetadata method);

    /**
     * Check java type allowed to be used in a service operation.
     * 
     * <ul>
     * <li>Is an allowed basic java type in JDK: Java type is primitive, from
     * java.lang package or from java.util package or from java.math package</li>
     * <li>Is a java type in project sources</li>
     * <li></li>
     * </ul>
     * 
     * <p>
     * Only in last case, GvNIXXmlElement annotation is assigned to java type.
     * </p>
     * 
     * @param javaType
     *            Java type (can't be null)
     * @param type
     *            Type of parameter to check
     * @return Is it allowed ?
     */
    public boolean isTypeAllowed(JavaType javaType, MethodParameterType type);

    /**
     * Set method exception throws to publish as fault in service operation.
     * 
     * <p>
     * Add fault annotations to each founded exception. There are two exceptions
     * types and two ways to define annotations:
     * </p>
     * 
     * <ul>
     * <li>Exceptions defined in the project: Add {@link GvNIXWebFault}
     * annotation to Exception.</li>
     * <li>Exceptions imported into the project: Add web service fault
     * annotation using AspectJ template in exceptions sub package.</li>
     * </ul>
     * 
     * @param method
     *            to check its exceptions
     * @param targetNamespace
     *            Web Service Namespace
     */
    public void prepareExceptions(MethodMetadata method, String targetNamespace);

    /**
     * Checks correct namespace URI format (preffix 'http://').
     * 
     * <p>
     * If String is blank is also correct.
     * </p>
     * 
     * @param namespace
     *            string to check as correct namespace.
     * @return true if is blank or if has correct URI format.
     */
    public boolean checkNamespaceFormat(String namespace);

    /**
     * Check java type defined GvNIXWebService and valid target namespace.
     * 
     * <p>
     * IllegalArgumentException when:
     * </p>
     * <ul>
     * <li>No available mutable java type</li>
     * <li>No GvNIXWebService in java type</li>
     * <li>No target namespace attribute in GvNIXWebService</li>
     * <li>Invalid target namespace value (valid is blank or URI format)</li>
     * </ul>
     * 
     * @param javaType
     *            Java type to target namespace annotation attribute
     * @return valid targetNamespace attribute from annotation GvNIXWebService
     */
    public String getWebServiceDefaultNamespace(JavaType javaType);

}
