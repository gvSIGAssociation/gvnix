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
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
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
     * Checks if JavaTypes input/output parameters involved in operation are
     * permitted to be published in web service and adds {@link GvNIXXmlElement}
     * annotation to any related project type which needs it.
     * 
     * @param serviceClass
     *            to check if the method is correct to be published.
     * @param methodName
     *            method to check if input/output parameters ara permitted to be
     *            publish in web service.
     */
    public void prepareAuthorizedJavaTypesInOperation(MethodMetadata method);

    /**
     * Check java type allowed to be used in a service operation.
     * 
     * <ul>
     * <li>Is an allowed java type in class loader: Java type and its parameters
     * if exists are allowed; if not empty params, implements iterable or
     * implements map</li>
     * <li>Is an allowed java type in JDK: Java type is primitive, from
     * java.lang package or from java.util package</li>
     * <li>Is an allowed java type in class loader: It's not XML entity and
     * superclass if exists is an allowed java type</li>
     * <li></li>
     * </ul>
     * 
     * <p>
     * Only in last case, GvNIXXmlElement annotation is assigned to java type
     * with attribute values from java type and elementList attribute from type
     * details allowed element fields.
     * </p>
     * 
     * @param javaType
     *            Java type (can't be null)
     * @param methodParameterType
     *            Type of the java type
     * @return Is it allowed ?
     */
    public boolean isTypeAllowed(JavaType javaType,
            MethodParameterType methodParameterType);

    /**
     * <p>
     * Check method exceptions to publish in service operation.
     * </p>
     * <p>
     * Add web services annotations to each founded exception.
     * </p>
     * <p>
     * There are two exceptions types and two ways to define annotations:
     * </p>
     * <ul>
     * <li>Exceptions defined in the project.
     * <p>
     * Add {@link GvNIXWebFault} annotation to Exception.
     * </p>
     * </li>
     * <li>Exceptions imported into the project.
     * <p>
     * Add web service fault annotation using AspectJ template.
     * </p>
     * </li>
     * </ul>
     * 
     * @param serviceClass
     *            where the method is defined.
     * @param methodName
     *            to check its exceptions.
     * @param webServiceTargetNamespace
     *            Web Service Namespace.
     * @return true if the exceptions are published correctly or false if the
     *         exceptions don't exists or are incorrect.
     */
    public boolean prepareMethodExceptions(MethodMetadata method,
            String webServiceTargetNamespace);

    /**
     * Checks correct namespace URI format. Suffix 'http://'.
     * 
     * @param namespace
     *            string to check as correct namespace.
     * @return true if is blank or if has correct URI format.
     */
    public boolean checkNamespaceFormat(String namespace);

    /**
     * Check if serviceClass has defined {@link GvNIXWebService} annotation with
     * valid 'targetNamespace' attribute and returns it if is correct.
     * 
     * @param serviceClass
     *            Web Service class to check correct target Namespace and return
     *            as result.
     * @return targetNamespace attribute from annotation {@link GvNIXWebService}
     *         .
     */
    public String getWebServiceDefaultNamespace(JavaType serviceClass);

}
