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

import java.util.List;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 */
public interface WSExportOperations {

    /**
     * Method parameter type to check.
     * <ul>
     * <li>RETURN: output method parameter.</li>
     * <li>PARAMETER: input method parameter.</li>
     * <li>XMLENTITY: project entity/class.</li>
     * </ul>
     */
    public enum MethodParameterType {
        RETURN, PARAMETER, XMLENTITY
    };

    /**
     * <p>
     * Exports a class to Web Service.
     * </p>
     * 
     * <p>
     * If the class to export as web service doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     * 
     * @param className
     *            class to export.
     * @param serviceName
     *            Name to publish the Web Service.
     * @param portTypeName
     *            Name to define the portType.
     * @param targetNamespace
     *            Namespace name for the service.
     * @param addressName
     *            Address to access the service.
     */
    public void exportService(JavaType className, String serviceName,
            String portTypeName, String targetNamespace, String addressName);

    /**
     * Exports a class method as a web service operation.
     * 
     * <ul>
     * <li>Java class will be created if not exists.</li>
     * <li>Method searching in Java and AspectJ related files without
     * GvNIXWebService annotation already.</li>
     * <li>Check return and parameters types and add GvNIXXmlElement if
     * required.</li>
     * <li>Check method throws and update with web fault declaration if needed.</li>
     * <li>Create GvNIXWebMethod annotation for Method.</li>
     * <li>Add @GvNIXWebParam & @WebParam parameter annotations.</li>
     * </ul>
     * 
     * @param javaType
     *            Class to export a method.
     * @param methodName
     *            Method to export.
     * @param operationName
     *            Name of the method to be showed as a Web Service operation.
     * @param resultName
     *            Method result name.
     * @param resultNamespace
     *            Namespace of the result type.
     * @param responseWrapperName
     *            Name to define the Response Wrapper Object.
     * @param responseWrapperNamespace
     *            Namespace of the Response Wrapper Object.
     * @param requestWrapperName
     *            Name to define the Request Wrapper Object.
     * @param requestWrapperNamespace
     *            Namespace of the Request Wrapper Object.
     * 
     */
    public void exportOperation(JavaType javaType, JavaSymbolName methodName,
            String operationName, String resultName, String resultNamespace,
            String responseWrapperName, String responseWrapperNamespace,
            String requestWrapperName, String requestWrapperNamespace);

    /**
     * <p>
     * List methods available to export as web service operations.
     * </p>
     * <p>
     * <code>serviceClass</code> must be annotated with @GvNIXWebService.
     * </p>
     * <p>
     * Retrieves method names which aren't annotated with @GvNIXWebMethod.
     * </p>
     * 
     * @param serviceClass
     *            class to search available methods to export as web service
     *            operations.
     */
    public String getAvailableServiceOperationsToExport(JavaType serviceClass);

    /**
     * Returns names of classes which imports web services
     * 
     * @return
     */
    List<String> getServiceList();
}
