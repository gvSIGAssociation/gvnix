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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebServiceProxy;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <p>
 * gvNix Web Service Java proxy generation.
 * </p>
 * 
 * @author Mario Martínez Sánchez( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class ServiceLayerWSImportMetadata extends
	AbstractItdTypeDetailsProvidingMetadataItem {

    public static final String NAMESPACE_SOAP_11 = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String NAMESPACE_SOAP_11_WITHOUT_SLASH = NAMESPACE_SOAP_11.substring(0, NAMESPACE_SOAP_11.length() - 1);
    public static final String NAMESPACE_SOAP_12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String NAMESPACE_SOAP_12_WITHOUT_SLASH = NAMESPACE_SOAP_12.substring(0, NAMESPACE_SOAP_12.length() - 1);
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String HTTP_PROTOCOL_PREFIX = "http://";

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSImportMetadataNotificationListener.class
		    .getName());
    
    private static final String WEB_SERVICE_TYPE_STRING = ServiceLayerWSImportMetadata.class
	    .getName();    
    private static final String WEB_SERVICE_TYPE = MetadataIdentificationUtils
	    .create(WEB_SERVICE_TYPE_STRING);
    
    // From annotation
    @AutoPopulate
    private String wsdlLocation;
    
    public ServiceLayerWSImportMetadata(String identifier, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata) {

	super(identifier, aspectName, governorPhysicalTypeMetadata);

	Assert.isTrue(isValid(identifier), "Metadata identification string '"
		+ identifier + "' does not appear to be valid");

	if (!isValid()) {
	    return;
	}

	// Create the metadata.
	AnnotationMetadata annotationMetadata = MemberFindingUtils
		.getTypeAnnotation(governorTypeDetails, new JavaType(
			GvNIXWebServiceProxy.class.getName()));

	if (annotationMetadata != null) {
	    
	    // Populate wsdlLocation property class from annotation attribute
	    AutoPopulationUtils.populate(this, annotationMetadata);
	    logger.log(Level.FINE, "Wsdl location = " + wsdlLocation);

	    try {
		
		Document wsdl = XmlUtils.getDocumentBuilder().parse(
			wsdlLocation);
		Element root = wsdl.getDocumentElement();
		if (root == null) {
		    
		    throw new SAXException();
		}

		// Build the classpath related to the namespace
		String classPath = getNamespacePath(root);

		// Find a compatible address element
		Element address = findCompatibleAddressElement(root);
		if (address == null) {
		    
		    throw new SAXException();
		}
		
		// Get the path to the port class defined by the wsdl
		Element port = ((Element) address.getParentNode());
		String portName = port.getAttribute("name");
		String portPath = classPath + portName;

		// Get the path to the service class defined by the wsdl
		Element service = ((Element) port.getParentNode());
		String serviceName = service.getAttribute("name");
		String servicePath = classPath + serviceName;

		// Find all binding elements
		List<Element> bindings = XmlUtils.findElements(
			"/definitions/binding", root);
		String bindingReference = port.getAttribute("binding");
		Element binding = getReferencedElement(root, bindings,
			bindingReference);
		if (binding == null) {
		    
		    throw new SAXException();
		}
		
		// Find all port types elements
		List<Element> portTypes = XmlUtils.findElements(
			"/definitions/portType", root);
		String portTypeReference = binding.getAttribute("type");
		Element portType = getReferencedElement(root, portTypes,
			portTypeReference);
		if (portType == null) {
		    
		    throw new SAXException();
		}
		String portTypeName = portType.getAttribute("name");
		String portTypePath = classPath + portTypeName;
		
		// TODO Completar
		InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
		body.appendFormalLine("// Auto generated testing method");
		body.appendFormalLine(servicePath.replaceAll("/", ".")
			+ " tc = new " + servicePath.replaceAll("/", ".")
			+ "();");
		body
			.appendFormalLine(portTypePath.replaceAll("/", ".") + " port = tc.getTempConvertSoap12();");
		body
			.appendFormalLine("return port.celsiusToFahrenheit(\"1\");");
		MethodMetadata result = new DefaultMethodMetadata(getId(),
			Modifier.PUBLIC,
			new JavaSymbolName("unusedTestMethod"), new JavaType(
				String.class.getName()),
			new ArrayList<AnnotatedJavaType>(),
			new ArrayList<JavaSymbolName>(),
			new ArrayList<AnnotationMetadata>(), null, body
				.getOutput());
		builder.addMethod(result);

		logger.log(Level.INFO, "Web service has been imported into the class "
			+ portPath);
		
	    } catch (SAXException e) {

		logger
			.log(Level.WARNING,
				"The format of the web service to import (wsdl contract) has errors");
		throw new IllegalStateException("The format of the web service to import (wsdl contract) has errors");

	    } catch (IOException e) {

		logger.log(Level.WARNING,
			"There is no connection to the web service to import");
		throw new IllegalStateException();
	    }

	}

	// Create a representation of the desired output ITD
	itdTypeDetails = builder.build();
    }

    /**
     * Obtain from the list the element with the reference on the root wsdl.
     * 
     * @param root Root wsdl
     * @param elements Elements list to search in
     * @param reference Reference to be searched
     * @return Element found
     */
    private Element getReferencedElement(Element root, List<Element> elements,
	    String reference) {

	String prefix = getNamePrefix(reference);
	String sufix = getNameSufix(reference);
	String namespace = getPrefixNamespace(root, prefix);

	Element element = null;
	for (Element elementIter : elements) {

	    String referenceIter = elementIter.getAttribute("name");
	    String prefixIter = getNamePrefix(referenceIter);
	    String sufixIter = getNameSufix(referenceIter);
	    String namespaceIter = getPrefixNamespace(root, prefixIter);
	    if (sufixIter.equals(sufix) && namespaceIter.equals(namespace)) {

		element = elementIter;
	    }
	}
	
	return element;
    }

    /**
     * Constructs a valid path from the target namespace of the root.
     * 
     * @param root Root element of the wsdl
     * @return Equivalent namespace path 
     */
    private String getNamespacePath(Element root) {
	
	// Get the namespace attribute from root wsdl
	String namespace = root.getAttribute("targetNamespace");

	// Remove http prefix and final slash from namespace
	if (namespace.startsWith(HTTP_PROTOCOL_PREFIX)) {

	    namespace = namespace.substring(HTTP_PROTOCOL_PREFIX.length());
	}
	if (namespace.endsWith("/")) {

	    namespace = namespace.substring(0, namespace.length() - 1);
	}
	logger.log(Level.FINE, "Clean namespace = " + namespace);

	// Build package: revert the namespace and replace "." with "/"
	String classPath = new String();
	try {

	    StringTokenizer token = new StringTokenizer(namespace, ".");
	    while (true) {

		classPath = token.nextToken() + "/" + classPath;
	    }

	} catch (NoSuchElementException e) {

	    // No more string tokens
	}
	
	return classPath;
    }

    /**
     * Find a compatible address element.
     * 
     * <p>
     * Compatible address should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl.
     * @return Address element.
     */
    private Element findCompatibleAddressElement(Element root) {

	// Find all address elements
	List<Element> addresses = XmlUtils.findElements(
		"/definitions/service/port/address", root);

	// Separate on a list the addresses prefix
	List<String> prefixes = new ArrayList<String>();
	for (int i = 0; i < addresses.size(); i++) {

	    String nodeName = addresses.get(i).getNodeName();
	    prefixes.add(i, getNamePrefix(nodeName));
	}

	// Separate on a list the addresses namespace
	List<String> namespaces = new ArrayList<String>();
	for (int i = 0; i < prefixes.size(); i++) {

	    namespaces.add(i, getPrefixNamespace(root, prefixes.get(i)));
	}

	// Any namepace is a SOAP namespace with or whitout final slash ?
	int index;
	if ((index = namespaces.indexOf(NAMESPACE_SOAP_12)) != -1
		|| (index = namespaces.indexOf(NAMESPACE_SOAP_12_WITHOUT_SLASH)) != -1) {

	    logger.log(Level.INFO,
		    "Web service to import uses SOAP 1.2 protocol");

	} else if ((index = namespaces.indexOf(NAMESPACE_SOAP_11)) != -1
		|| (index = namespaces.indexOf(NAMESPACE_SOAP_11_WITHOUT_SLASH)) != -1) {

	    logger.log(Level.INFO,
		    "Web service to import uses SOAP 1.1 protocol");

	} else {

	    logger.log(Level.WARNING,
		    "Web service to import protocols are not compatible");
	    throw new IllegalStateException();
	}

	return addresses.get(index);
    }

    /**
     * Get the namespace of an wsdl prefix, or target namespace if not.
     * 
     * <p>
     * Namespace is defined by the value of the root attributes that starts with
     * 'xmlns:' prefix. For example, for element
     * 'xmlns:tns="http://tempuri.org/"' the namespace is 'http://tempuri.org/'.
     * </p>
     * 
     * @param prefix Prefix to search it namespace
     * @return Namespace related to the prefix
     */
    private String getPrefixNamespace(Element root, String prefix) {

	String namespace = null;

	if (prefix != null && prefix.length() > 0) {

	    // Get the namespace related to the prefix
	    namespace = root.getAttribute("xmlns:" + prefix);
	}

	if (namespace == null) {

	    namespace = root.getAttribute("targetNamespace");
	}
	
	return namespace;
    }

    /**
     * Get the prefix of a name, or empty if not.
     * 
     * <p>
     * Prefix is the text before first ':' character. For example, for name
     * '<soap12:address>' the prefix is 'soap12'.
     * </p>
     * 
     * @param name
     *            A name
     * @return Prefix of the name or empty if not
     */
    private String getNamePrefix(String name) {

	String prefix = "";

	// Get the index of the ':' char
	int index = name.indexOf(NAMESPACE_SEPARATOR);
	if (index != -1) {

	    // Get the prefix
	    prefix = name.substring(0, index);
	}

	return prefix;
    }

    /**
     * Get the sufix of a name, or name if not.
     * 
     * <p>
     * Sufix is the text after first ':' character. For example, for name
     * '<soap12:address>' the sufix is 'address'.
     * </p>
     * 
     * @param name
     *            A name
     * @return Sufix of the name or name if not
     */
    private String getNameSufix(String name) {
	
	return name.replaceFirst(getNamePrefix(name) + ":", "");
    }

    public static String getMetadataIdentiferType() {
	return WEB_SERVICE_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.isValid(
		WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getJavaType(
		WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getPath(
		WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
	return PhysicalTypeIdentifierNamingUtils.createIdentifier(
		WEB_SERVICE_TYPE_STRING, javaType, path);
    }

}
