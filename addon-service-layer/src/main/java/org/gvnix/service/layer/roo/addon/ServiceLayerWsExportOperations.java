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

import java.util.List;

import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerWsExportOperations {

    /**
     * Method parameter type to check.
     * <ul>
     * <li>RETURN: output method parameter.</li>
     * <li>PARAMETER: input method parameter.</li>
     * </ul>
     */
    public enum MethodParameterType {
	RETURN, PARAMETER
    };

    /**
     * Is service layer web service export command available on Roo console ? 
     * 
     * @return Service layer web service export command available on Roo console 
     */
    boolean isProjectAvailable();

    /**
     * Exports a class to Web Service.
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
     * Exports an operation to a Web Service Operation.
     * 
     * @param serviceClass
     *            Class to export a method.
     * @param methodName
     *            Method to export.
     * @param operationName
     *            Name of the method to be showed as a Web Service operation.
     * @param resutlName
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
    public void exportOperation(JavaType serviceClass,
	    JavaSymbolName methodName, String operationName, String resutlName,
	    String resultNamespace, String responseWrapperName,
	    String responseWrapperNamespace, String requestWrapperName,
	    String requestWrapperNamespace);

    /**
     * Creates the list of annotations with the values defined.
     * 
     * @param serviceClass
     *            Class to export a method.
     * @param methodName
     *            Method to export.
     * @param operationName
     *            Name of the method to be showed as a Web Service operation.
     * @param resultName
     *            Method result name.
     * @param returnType
     *            JavaType class to return.
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
    public List<AnnotationMetadata> getAnnotationsToExportOperation(
            JavaType serviceClass, JavaSymbolName methodName,
            String operationName, String resultName, JavaType returnType,
            String resultNamespace, String responseWrapperName,
            String responseWrapperNamespace, String requestWrapperName,
            String requestWrapperNamespace);

    /**
     * Check if the method methodName exists in serviceClass and is not
     * annotated before with annotationName.
     * 
     * @param serviceClass
     *            to check if method exists
     * @param methodName
     *            method to check if exists and is annotation defined.
     * @param annotationName
     *            to search in method.
     * 
     * @return true if method exists and annotation is not defined.
     */
    public boolean isMethodAvailableToExport(JavaType serviceClass,
	    JavaSymbolName methodName, String annotationName);
}
