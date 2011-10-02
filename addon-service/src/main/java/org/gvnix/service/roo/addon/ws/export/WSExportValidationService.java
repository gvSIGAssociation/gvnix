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
     * Add GvNIXXmlElement to method return and parameters types in project.
     * 
     * <p>
     * GvNIXXmlElement annotation is added to parent extend types in project and
     * type parameters in project too.
     * </p>
     * 
     * @param method
     *            Add annotation to method return and parameter types in project
     */
    public void addGvNixXmlElementToTypes(MethodMetadata method);

    /**
     * Add GvNIXWebFault to method exceptions in project.
     * 
     * <p>
     * GvNIXWebFault annotation is added to parent extend exceptions in project
     * too.
     * </p>
     * 
     * @param method
     *            Add annotation to method exceptions in project
     * @param targetNamespace
     *            Target namespace to add as annotation attribute
     */
    public void addGvNixWebFaultToExceptions(MethodMetadata method,
            String targetNamespace);

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
