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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.*;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerCommands implements CommandMarker {

    private static final Logger logger = HandlerUtils.getLogger(SimpleParser.class);

    @Reference
    private ServiceLayerOperations serviceLayerOperations;
    @Reference
    private ServiceLayerWsExportOperations serviceLayerWsExportOperations;
    @Reference
    private ServiceLayerWsImportOperations serviceLayerWsImportOperations;

    @CliAvailabilityIndicator( { "service class", "service operation" })
    public boolean isCreateServiceClassAvailable() {

	return serviceLayerOperations.isProjectAvailable();
    }

    @CliCommand(value = "service class", help = "Creates a new Service class in SRC_MAIN_JAVA.")
    public void createServiceClass(
	    @CliOption(key = "class", mandatory = true, help = "Name of the service class to create") JavaType serviceClass) {

	serviceLayerOperations.createServiceClass(serviceClass);
    }

    @CliCommand(value = "service operation", help = "Adds a new method to existing Service")
    public void addServiceOperation(
	    @CliOption(key = { "", "name" }, mandatory = true, help = "The name of the operation to add") JavaSymbolName operationName,
            @CliOption(key = "service", mandatory = true, optionContext = "update,project", help = "The name of the service to receive this field") JavaType className,
	    @CliOption(key = "return", mandatory = false, unspecifiedDefaultValue = "__NULL__", optionContext = "java-all,project", help = "The Java type this operation returns") JavaType returnType,
	    @CliOption(key = "paramNames", mandatory = false, help = "The parameters of the operation. They must be introduced separated by commas without blank spaces.") String paramNames,
	    @CliOption(key = "paramTypes", mandatory = false, optionContext = "java", help = "The Java types of the given parameters. They must be introduced separated by commas without blank spaces.") JavaTypeList paramTypesList,
	    @CliOption(key = "exceptions", mandatory = false, optionContext = "exceptions", help = "The Exceptions defined for the operation. They must be introduced separated by commas without blank spaces.") JavaTypeList exceptionTypes) {

	String[] paramNameArray;
	List<String> paramNameList = new ArrayList<String>();
	List<JavaType> paramTypes = new ArrayList<JavaType>();
	List<JavaType> exceptionList = new ArrayList<JavaType>();

	boolean existsParamTypes = paramTypesList != null;

	if (existsParamTypes && paramTypesList.getJavaTypes().size() > 0) {
	    Assert.isTrue(StringUtils.hasText(paramNames),
		    "You must provide parameter names to create the method.");

	} else if (StringUtils.hasText(paramNames)) {
	    Assert.isTrue(existsParamTypes
		    && paramTypesList.getJavaTypes().size() > 0,
		    "You must provide parameter Types to create the method.");

	}

	if (StringUtils.hasText(paramNames)
		&& paramTypesList.getJavaTypes().size() > 0) {

	    paramNameArray = StringUtils
		    .commaDelimitedListToStringArray(paramNames);

	    Assert
		    .isTrue(
			    paramTypesList.getJavaTypes().size() == paramNameArray.length,
			    "The method parameter types must have the same number of parameter names to create the method.");

	    for (int i = 0; i <= paramNameArray.length - 1; i++) {

		paramNameList.add(paramNameArray[i]);
	    }

	    paramTypes = paramTypesList.getJavaTypes();

	} else {

	    paramTypes = new ArrayList<JavaType>();
	}

	// Exceptions.
	if (exceptionTypes != null
		&& !exceptionTypes.getJavaTypes().isEmpty()) {
	    exceptionList = exceptionTypes.getJavaTypes();
	}

	serviceLayerOperations.addServiceOperation(operationName, returnType,
		className, paramTypes, paramNameList, exceptionList);

    }

    @CliAvailabilityIndicator("service define ws")
    public boolean isServiceExportAvailable() {

	return serviceLayerWsExportOperations.isProjectAvailable();
    }

    @CliCommand(value = "service define ws", help = "Defines a service endpoint interface (SEI) that will be mapped to a PortType in service contract. If target class doesn't exist the add-on will create it.")
    public void serviceExport(
	    @CliOption(key = "class", mandatory = true, help = "Name of the service class to export or create") JavaType serviceClass,
	    @CliOption(key = "serviceName", mandatory = false, help = "Name to publish the Web Service.") String serviceName,
	    @CliOption(key = "portTypeName", mandatory = false, help = "Name to define the portType.") String portTypeName,
	    @CliOption(key = "addressName", mandatory = false, help = "Address to publish the Web Service in server. Default class name value.") String addressName,
	    @CliOption(key = "targetNamespace", mandatory = false, help = "Namespace name for the service. \ni.e.: 'http://services.project.layer.service.test.gvnix.org/'. It must have URI format.") String targetNamespace) {

	serviceLayerWsExportOperations.exportService(serviceClass, serviceName,
		portTypeName, targetNamespace, addressName);
        logger.warning("** IMPORTANT: Use 'service export operation' command (without the quotes) to publish service operations **".concat(System.getProperty("line.separator")));

    }

    @CliAvailabilityIndicator("service export operation")
    public boolean isServiceExportOperationAvailable() {

	return serviceLayerWsExportOperations.isProjectAvailable();
    }

    /**
     * Command to export a method as a web service operation.
     * 
     * <p>
     * Parameters:
     * </p>
     * <ul>
     * <li>
     * ``--class`` (mandatory) Class to export a method.</li>
     * <li>
     * ``--method``(mandatory) Method to export.</li>
     * <li>
     * ``--operationName`` Name of the method to be showed as a Web Service
     * operation.</li>
     * <li>
     * ``--resultName`` Method result name.</li>
     * <li>
     * ``--resultNamespace`` Namespace of the result type.</li>
     * <li>
     * ``--responseWrapperName`` Name to define the Response Wrapper Object.</li>
     * <li>
     * ``--responseWrapperNamespace``: Namespace of the Response Wrapper Object.
     * </li>
     * <li>
     * ``--requestWrapperName``: Name to define the Request Wrapper Object.</li>
     * <li>
     * ``--requestWrapperNamespace``: Namespace of the Request Wrapper Object.</li>
     * <li>
     * ``--exceptionName``: Name to define method exception if exists.</li>
     * <li>
     * ``--exceptionNamespace``: Namespace of method exception if exists.</li>
     * </ul>
     */
    @CliCommand(value = "service export operation", help = "Publish a class method as web service operation in a PortType.")
    public void serviceExportOperation(
	    @CliOption(key = "class", mandatory = true, help = "Name of the service class to export a method.") JavaType serviceClass,
	    @CliOption(key = "method", mandatory = true, help = "Method to export as Web Service Operation.") JavaSymbolName methodName,
	    @CliOption(key = "operationName", mandatory = false, help = "Name of the method to be showed as a Web Service operation.") String operationName,
	    @CliOption(key = "resultName", mandatory = false, help = "Method result name.") String resultName,
	    @CliOption(key = "resultNamespace", mandatory = false, help = "NNamespace of the result type. \ni.e.: 'http://services.project.layer.service.test.gvnix.org/'. It must have URI format.") String resultNamespace,
	    @CliOption(key = "responseWrapperName", mandatory = false, help = "Name to define the Response Wrapper Object.") String responseWrapperName,
	    @CliOption(key = "responseWrapperNamespace", mandatory = false, help = "Namespace of the Response Wrapper Object. \ni.e.: 'http://services.project.layer.service.test.gvnix.org/'. It must have URI format.") String responseWrapperNamespace,
	    @CliOption(key = "requestWrapperName", mandatory = false, help = "Name to define the Request Wrapper Object.") String requestWrapperName,
	    @CliOption(key = "requestWrapperNamespace", mandatory = false, help = "Namespace of the Request Wrapper Object. \ni.e.: 'http://services.project.layer.service.test.gvnix.org/'. It must have URI format.") String requestWrapperNamespace) {

	if (StringUtils.hasText(resultNamespace)) {
	    Assert
		    .isTrue(StringUtils.startsWithIgnoreCase(resultNamespace,
			    "http://"),
			    "Name space for WebResult is not correctly defined. It must have URI format.");
	}

	if (StringUtils.hasText(requestWrapperNamespace)) {
	    Assert
		    .isTrue(
			    StringUtils.startsWithIgnoreCase(
				    requestWrapperNamespace, "http://"),
			    "Name space for RequestWrapper is not correctly defined. It must have URI format.");
	}

	if (StringUtils.hasText(responseWrapperNamespace)) {
	    Assert
		    .isTrue(
			    StringUtils.startsWithIgnoreCase(
				    responseWrapperNamespace, "http://"),
			    "Name space for ResponsetWrapper is not correctly defined. It must have URI format.");
	}

	serviceLayerWsExportOperations.exportOperation(serviceClass,
		methodName, operationName, resultName, resultNamespace,
		responseWrapperName, responseWrapperNamespace,
		requestWrapperName, requestWrapperNamespace);
    }

    @CliAvailabilityIndicator("service import ws")
    public boolean isServiceImportAvailable() {

	return serviceLayerWsImportOperations.isProjectAvailable();
    }

    @CliCommand(value = "service import ws", help = "Imports a Web Service to Service class. If the class doesn't exists the Addon will create it.")
    public void serviceImport(
	    @CliOption(key = "class", mandatory = true, help = "Name of the service class to import or create") JavaType serviceClass,
	    @CliOption(key = "wsdl", mandatory = true, help = "Local or remote location (URL) of the web service contract") String url) {

	serviceLayerWsImportOperations.addImportAnnotation(serviceClass, url);
    }

}
