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
package org.gvnix.service.roo.addon;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.converters.JavaTypeList;
import org.gvnix.service.roo.addon.ws.export.WSExportOperations;
import org.gvnix.service.roo.addon.ws.export.WSExportWsdlOperations;
import org.gvnix.service.roo.addon.ws.importt.WSImportOperations;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.shell.SimpleParser;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ServiceCommands implements CommandMarker {

    private static final Logger logger = HandlerUtils
            .getLogger(SimpleParser.class);

    @Reference
    private ServiceOperations serviceOperations;
    @Reference
    private WSExportOperations wSExportOperations;
    @Reference
    private WSImportOperations wSImportOperations;
    @Reference
    private WSExportWsdlOperations wSExportWsdlOperations;

    @CliAvailabilityIndicator({ "service class", "service operation" })
    public boolean isCreateServiceClassAvailable() {

        return serviceOperations.isProjectAvailable();
    }

    @CliCommand(value = "service class", help = "Creates a new Service class in SRC_MAIN_JAVA.")
    public void createServiceClass(
            @CliOption(key = "class", mandatory = true, help = "Name of the service class to create") JavaType serviceClass) {

        serviceOperations.createServiceClass(serviceClass);
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

            Assert.isTrue(
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
        if (exceptionTypes != null && !exceptionTypes.getJavaTypes().isEmpty()) {
            exceptionList = exceptionTypes.getJavaTypes();
        }

        serviceOperations.addServiceOperation(operationName, returnType,
                className, paramTypes, paramNameList, exceptionList);

    }

    @CliAvailabilityIndicator("service define ws")
    public boolean isServiceExportAvailable() {

        return wSExportOperations.isProjectAvailable();
    }

    @CliCommand(value = "service define ws", help = "Defines a service endpoint interface (SEI) that will be mapped to a PortType in service contract. If target class doesn't exist the add-on will create it.")
    public void serviceExport(
            @CliOption(key = "class", mandatory = true, help = "Name of the service class to export or create") JavaType serviceClass,
            @CliOption(key = "serviceName", mandatory = false, help = "Name to publish the Web Service.") String serviceName,
            @CliOption(key = "portTypeName", mandatory = false, help = "Name to define the portType.") String portTypeName,
            @CliOption(key = "addressName", mandatory = false, help = "Address to publish the Web Service in server. Default class name value.") String addressName,
            @CliOption(key = "targetNamespace", mandatory = false, help = "Namespace name for the service. \ni.e.: 'http://services.project.service.test.gvnix.org/'. It must have URI format.") String targetNamespace) {

        wSExportOperations.exportService(serviceClass, serviceName,
                portTypeName, targetNamespace, addressName);
        logger.warning("** IMPORTANT: Use 'service export operation' command (without the quotes) to publish service operations **"
                .concat(System.getProperty("line.separator")));

    }

    @CliAvailabilityIndicator("service export operation")
    public boolean isServiceExportOperationAvailable() {

        return wSExportOperations.isProjectAvailable();
    }

    @CliAvailabilityIndicator("service list operation")
    public boolean isServiceListOperationAvailable() {

        return wSExportOperations.isProjectAvailable();
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
     * </ul>
     */
    @CliCommand(value = "service export operation", help = "Publish a class method as web service operation in a PortType.")
    public void serviceExportOperation(
            @CliOption(key = "class", mandatory = true, help = "Name of the service class to export a method.") JavaType serviceClass,
            @CliOption(key = "method", mandatory = true, help = "Method to export as Web Service Operation.") JavaSymbolName methodName,
            @CliOption(key = "operationName", mandatory = false, help = "Name of the method to be showed as a Web Service operation.") String operationName,
            @CliOption(key = "resultName", mandatory = false, help = "Method result name.") String resultName,
            @CliOption(key = "resultNamespace", mandatory = false, help = "NNamespace of the result type. \ni.e.: 'http://services.project.service.test.gvnix.org/'. It must have URI format.") String resultNamespace,
            @CliOption(key = "responseWrapperName", mandatory = false, help = "Name to define the Response Wrapper Object.") String responseWrapperName,
            @CliOption(key = "responseWrapperNamespace", mandatory = false, help = "Namespace of the Response Wrapper Object. \ni.e.: 'http://services.project.service.test.gvnix.org/'. It must have URI format.") String responseWrapperNamespace,
            @CliOption(key = "requestWrapperName", mandatory = false, help = "Name to define the Request Wrapper Object.") String requestWrapperName,
            @CliOption(key = "requestWrapperNamespace", mandatory = false, help = "Namespace of the Request Wrapper Object. \ni.e.: 'http://services.project.service.test.gvnix.org/'. It must have URI format.") String requestWrapperNamespace) {

        if (StringUtils.hasText(resultNamespace)) {
            Assert.isTrue(StringUtils.startsWithIgnoreCase(resultNamespace,
                    "http://"),
                    "Name space for WebResult is not correctly defined. It must have URI format.");
        }

        if (StringUtils.hasText(requestWrapperNamespace)) {
            Assert.isTrue(
                    StringUtils.startsWithIgnoreCase(requestWrapperNamespace,
                            "http://"),
                    "Name space for RequestWrapper is not correctly defined. It must have URI format.");
        }

        if (StringUtils.hasText(responseWrapperNamespace)) {
            Assert.isTrue(
                    StringUtils.startsWithIgnoreCase(responseWrapperNamespace,
                            "http://"),
                    "Name space for ResponsetWrapper is not correctly defined. It must have URI format.");
        }

        wSExportOperations.exportOperation(serviceClass, methodName,
                operationName, resultName, resultNamespace,
                responseWrapperName, responseWrapperNamespace,
                requestWrapperName, requestWrapperNamespace);
    }

    @CliCommand(value = "service list operation", help = "Shows available methods to export as web service operation in selected class.")
    public String serviceExportOperationList(
            @CliOption(key = "class", mandatory = true, help = "Name of the service class to list methods available to export as web service operations.") JavaType serviceClass) {

        return wSExportOperations
                .getAvailableServiceOperationsToExport(serviceClass);
    }

    @CliAvailabilityIndicator("service import ws")
    public boolean isServiceImportAvailable() {

        return wSImportOperations.isProjectAvailable();
    }

    @CliCommand(value = "service import ws", help = "Imports a Web Service to Service class. If the class doesn't exists the Addon will create it.")
    public void serviceImport(
            @CliOption(key = "class", mandatory = true, help = "Name of the service class to import or create") JavaType serviceClass,
            @CliOption(key = "wsdl", mandatory = true, help = "Local or remote location (URL) of the web service contract") String url) {

        wSImportOperations.addImportAnnotation(serviceClass, url);
    }

    @CliAvailabilityIndicator("service export ws")
    public boolean isServiceExportWsdl() {

        return wSExportWsdlOperations.isProjectAvailable();
    }

    @CliCommand(value = "service export ws", help = "Exports a Web Service from WSDL to java code with gvNIX annotations to generate this Web Service in project with dummy methods.")
    public void serviceExportWsdl(
            @CliOption(key = "wsdl", mandatory = true, help = "Local or remote location (URL) of the web service contract") String url) {

        wSExportWsdlOperations.exportWSDL2Java(url);
    }

    @CliAvailabilityIndicator("service ws list")
    public boolean isServiceWsListAvalilable() {
        return wSImportOperations.isProjectAvailable()
                || wSExportOperations.isProjectAvailable();
    }

    public boolean isServiceSecurityWs() {
        return wSImportOperations.isProjectAvailable();
    }

    @CliCommand(value = "service security ws", help = "Adds Signature to a imported Web Service request")
    public void serviceSecurityWs(
            @CliOption(key = "class", mandatory = true, help = "Name of the imported service class") JavaType importedServiceClass,
            @CliOption(key = "certificate", mandatory = true, help = "pkcs12 file to use for sing the request") File certificate,
            @CliOption(key = "password", mandatory = true, help = "pkcs12 file password to use for sing the request") String password,
            @CliOption(key = "alias", mandatory = true, help = "alias of pkcs12 file to use for sing the request") String alias) {
        // TODO use converters to auto-complete imported service class parameter
        // (converter can use WSImportOperation.getServiceList() for
        // auto-complete)
        wSImportOperations.addSignatureAnnotation(importedServiceClass,
                certificate, password, alias);
    }

    @CliCommand(value = "service ws list", help = "Shows a class list with imported and/or exported services")
    public String serviceWsList() {

        // Gets imported services
        List<String> imported = wSImportOperations.getServiceList();

        // Gets exported services
        List<String> exported = wSExportOperations.getServiceList();

        // Format result
        return formatWsList(imported, exported);
    }

    /**
     * <p>
     * Format web service list
     * </p>
     * 
     * <p>
     * Pattern:<br/>
     * 
     * <pre>
     * Services             exported   imported
     * ------------------- --------- -----------
     * {className}             X          X
     * {className2}                       X
     * {className3}            X
     * </pre>
     * 
     * </p>
     * 
     * @param importedServices
     * @param exportedServices
     * @return
     */
    private String formatWsList(List<String> importedServices,
            List<String> exportedServices) {
        if ((importedServices == null || importedServices.isEmpty())
                && (exportedServices == null || exportedServices.isEmpty())) {
            return "No Web Services services found in application";
        }

        // Variable for max service name length
        int maxLength = 0;

        // Generate a shorted set
        Set<String> services = new TreeSet<String>();
        if (importedServices != null) {
            for (String service : importedServices) {
                services.add(service);
                if (service.length() > maxLength) {
                    maxLength = service.length();
                }
            }
        }
        if (exportedServices != null) {
            for (String service : exportedServices) {
                services.add(service);
                if (service.length() > maxLength) {
                    maxLength = service.length();
                }
            }
        }

        // Generate out
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);

        // Add header
        printer.print(fitStringTo("Services", maxLength, ' '));
        printer.print("   ");
        printer.print("exported");
        printer.print("   ");
        printer.println("imported");

        // Add header separator
        printer.print(fitStringTo("", maxLength + 1, '-'));
        printer.print(' ');
        printer.print(fitStringTo("", "exported".length() + 2, '-'));
        printer.print(' ');
        printer.print(fitStringTo("", "imported".length() + 2, '-'));
        printer.println();

        for (String service : services) {
            printer.print(fitStringTo(service, maxLength, ' '));
            printer.print("   ");

            if (exportedServices.contains(service)) {
                printer.print(fitStringTo("", 3, ' '));
                printer.print('X');
                printer.print(fitStringTo("", 4, ' '));
            } else {
                printer.print(fitStringTo("", 8, ' '));
            }

            printer.print("   ");

            if (importedServices.contains(service)) {
                printer.print(fitStringTo("", 3, ' '));
                printer.print('X');
                printer.print(fitStringTo("", 4, ' '));
            } else {
                printer.print(fitStringTo("", 8, ' '));
            }
            printer.println();
        }
        return writer.toString();
    }

    /**
     * Add <code>character</code> to a <code>string</code> until
     * <code>length</code>
     * 
     * @param string
     * @param length
     * @param character
     * @return
     */
    private String fitStringTo(String string, int length, char character) {
        StringBuilder sb = new StringBuilder(string);
        while (sb.length() < length) {
            sb.append(character);
        }
        return sb.toString();
    }
}
