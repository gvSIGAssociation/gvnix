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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * gvNix Wsdl parser utilities.
 * 
 * <p>
 * Compatible address should be SOAP protocol version 1.1 and 1.2.
 * </p>
 * 
 * <p>
 * Compatible namespace protocol should be nothing, http or urn.
 * </p>
 * 
 * @author Mario Martínez Sánchez( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WsdlParserUtils {

    public static final String SOAP_11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP_11_NAMESPACE_WITHOUT_SLASH = SOAP_11_NAMESPACE
	    .substring(0, SOAP_11_NAMESPACE.length() - 1);
    public static final String SOAP_12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String SOAP_12_NAMESPACE_WITHOUT_SLASH = SOAP_12_NAMESPACE
	    .substring(0, SOAP_12_NAMESPACE.length() - 1);

    public static final String HTTP_PROTOCOL_PREFIX = "http://";
    public static final String WWW_PROTOCOL_PREFIX = "www.";
    public static final String URN_PROTOCOL_PREFIX = "urn:";
    public static final String XML_NAMESPACE_PREFIX = "xmlns:";
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String URL_SEPARATOR = "/";
    public static final String FILE_SEPARATOR = File.separator;
    public static final String DOMAIN_SEPARATOR = ".";
    public static final String XPATH_SEPARATOR = "/";
    public static final String PACKAGE_SEPARATOR = ".";
    public static final String PORT_SEPARATOR = ":";
    
    public static final String TARGET_GENERATED_SOURCES_PATH = "."
	    + FILE_SEPARATOR + "target" + FILE_SEPARATOR + "generated-sources"
	    + FILE_SEPARATOR + "cxf";

    public static final String DEFINITIONS_ELEMENT = "definitions";
    public static final String BINDING_ELEMENT = "binding";
    public static final String PORT_TYPE_ELEMENT = "portType";
    public static final String SERVICE_ELEMENT = "service";
    public static final String PORT_ELEMENT = "port";
    public static final String ADDRESS_ELEMENT = "address";

    public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String BINDING_ATTRIBUTE = "binding";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String STYLE_ATTRIBUTE = "style";

    public static final String BINDINGS_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + BINDING_ELEMENT;
    public static final String PORT_TYPES_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + PORT_TYPE_ELEMENT;
    public static final String ADDRESSES_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + SERVICE_ELEMENT
	    + XPATH_SEPARATOR + PORT_ELEMENT + XPATH_SEPARATOR
	    + ADDRESS_ELEMENT;
    public static final String CHILD_BINDINGS_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + BINDING_ELEMENT
	    + XPATH_SEPARATOR + BINDING_ELEMENT;

    private static Logger logger = Logger.getLogger(WsdlParserUtils.class
	    .getName());

    /**
     * Constructs a valid java package path from target namespace of root wsdl.
     * 
     * <p>
     * Package ends with the package separator. If target namespace has not a
     * compatible prefix, empty string will be returned.
     * </p>
     * 
     * @param root
     *            Root element of the wsdl
     * @return Equivalent java package or empty
     */
    public static String getTargetNamespaceRelatedPackage(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Get the namespace attribute from root wsdl in lower case
	String namespace = root.getAttribute(TARGET_NAMESPACE_ATTRIBUTE).toLowerCase();

	// Namespace separators
	String separator1 = null;
	String separator2 = null;
	String separator3 = null;

	if (namespace.startsWith(HTTP_PROTOCOL_PREFIX)) {

	    // Remove http prefix and final url separator from namespace
	    namespace = namespace.substring(HTTP_PROTOCOL_PREFIX.length());
	    if (namespace.endsWith(URL_SEPARATOR)) {

		namespace = namespace.substring(0, namespace.length() - 1);
	    }
	    
	    // Remove www prefix
	    if (namespace.startsWith(WWW_PROTOCOL_PREFIX)) {
		
		namespace = namespace.substring(WWW_PROTOCOL_PREFIX.length());
	    }

	    separator1 = URL_SEPARATOR;
	    separator2 = DOMAIN_SEPARATOR;
	    separator3 = PORT_SEPARATOR;
	    
	} else if (namespace.startsWith(URN_PROTOCOL_PREFIX)) {

	    // Remove urn prefix from namespace
	    namespace = namespace.substring(URN_PROTOCOL_PREFIX.length());

	    separator1 = ":";
	    separator2 = "-";
	}

	// Revert namespace and replace url and domain with package separator
	String path = "";

	// Url tokens
	StringTokenizer urlTokens = new StringTokenizer(namespace, separator1);
	if (urlTokens.hasMoreTokens()) {
	    
	    String urlToken = urlTokens.nextToken();
	    
	    // Port tokens
	    StringTokenizer portTokens = null;
	    if (separator3 != null) {

		// First token is domain and second one the port
		portTokens = new StringTokenizer(urlToken,
			separator3);
		urlToken = portTokens.nextToken();
	    }

	    // Domain is the first token of the Url
	    StringTokenizer domainTokens = new StringTokenizer(urlToken,
		    separator2);
	    while (domainTokens.hasMoreTokens()) {

		path = domainTokens.nextToken().replaceAll("[^a-zA-Z0-9$]", "_") + PACKAGE_SEPARATOR + path;
	    }

	    // Port token
	    if (separator3 != null) {
		while (portTokens.hasMoreTokens()) {
		    
		    path = path + "_"
			    + portTokens.nextToken().replaceAll(
				    "[^a-zA-Z0-9$]", "_") + PACKAGE_SEPARATOR;
		}
	    }

	    // Url tokens
	    while (urlTokens.hasMoreTokens()) {

		path = path + urlTokens.nextToken().replaceAll("[^a-zA-Z0-9$]", "_") + PACKAGE_SEPARATOR;
	    }
	}

	return path;
    }

    /**
     * Find the first compatible address element of the root.
     * 
     * <p>
     * Compatible address should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl.
     * @return First compatible address element or null if no element.
     */
    public static Element findFirstCompatibleAddress(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Find all address elements
	List<Element> addresses = XmlUtils.findElements(ADDRESSES_XPATH, root);

	// Separate on a list the addresses prefix
	List<String> prefixes = new ArrayList<String>();
	for (int i = 0; i < addresses.size(); i++) {

	    String nodeName = addresses.get(i).getNodeName();
	    prefixes.add(i, getNamespace(nodeName));
	}

	// Separate on a list the addresses namespace
	List<String> namespaces = new ArrayList<String>();
	for (int i = 0; i < prefixes.size(); i++) {

	    namespaces.add(i, getNamespaceURI(root, prefixes.get(i)));
	}

	// Any namepace is a SOAP namespace with or whitout final slash ?
	int index;
	if ((index = namespaces.indexOf(SOAP_12_NAMESPACE)) != -1
		|| (index = namespaces.indexOf(SOAP_12_NAMESPACE_WITHOUT_SLASH)) != -1) {

	    // First preference: SOAP 1.2 protocol

	} else if ((index = namespaces.indexOf(SOAP_11_NAMESPACE)) != -1
		|| (index = namespaces.indexOf(SOAP_11_NAMESPACE_WITHOUT_SLASH)) != -1) {

	    // Second preference: SOAP 1.1 protocol

	} else {

	    // Other protocols not supported
	    return null;
	}

	return addresses.get(index);
    }

    /**
     * Obtain from the list the element with the reference on the root wsdl.
     * 
     * @param root
     *            Root wsdl
     * @param elements
     *            Elements list to search in
     * @param reference
     *            Reference to be searched
     * @return Element found or null if not
     */
    private static Element getReferencedElement(Element root,
	    List<Element> elements, String reference) {

	Assert.notNull(root, "Wsdl root element required");
	Assert.notNull(elements, "Elements list required");
	Assert.notNull(reference, "Reference required");

	String prefix = getNamespace(reference);
	String sufix = getLocalName(reference);
	String namespace = getNamespaceURI(root, prefix);

	Element element = null;
	for (Element elementIter : elements) {

	    String referenceIter = elementIter.getAttribute(NAME_ATTRIBUTE);
	    String prefixIter = getNamespace(referenceIter);
	    String sufixIter = getLocalName(referenceIter);
	    String namespaceIter = getNamespaceURI(root, prefixIter);
	    if (sufixIter.equals(sufix) && namespaceIter.equals(namespace)) {

		element = elementIter;
	    }
	}

	return element;
    }

    /**
     * Get the path to the generated service class.
     * 
     * @param root
     *            Wsdl root element
     * @return Path to the class
     */
    public static String getServiceClassPath(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Build the classpath related to the namespace
	String path = getTargetNamespaceRelatedPackage(root);

	// Find a compatible service name
	String name = findFirstCompatibleServiceClassName(root);

	// Class path is the concat of path and name
	return path + name;
    }

    /**
     * Get the path to the generated port type class.
     * 
     * @param root
     *            Wsdl root element
     * @return Path to the class
     */
    public static String getPortTypeClassPath(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Build the classpath related to the namespace
	String path = getTargetNamespaceRelatedPackage(root);

	// Find a compatible port type name
	String name = findFirstCompatiblePortTypeClassName(root);

	// Class path is the concat of path and name
	return path + name;
    }
    
    /**
     * Get the port type Java file
     * 
     * @param root
     *            Wsdl root element
     * @return Java file
     */
    public static File getPortTypeJavaFile(Element root) {

	return getGeneratedJavaFile(convertTypePathToJavaPath(getPortTypeClassPath(root)));
    }

    /**
     * Convert the path to a type to the java file path.
     * 
     * @param classPath
     *            Path to the class
     * @return Path to the java file
     */
    private static String convertTypePathToJavaPath(String classPath) {
	
	Assert.hasText(classPath, "Text in class path required");

	return classPath.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR).concat(".java");
    }
    
    /**
     * Get the file on path in generated sources folder.
     * 
     * @param path Searched path
     * @return File to path
     */
    private static File getGeneratedJavaFile(String path) {
	
	Assert.hasText(path, "Text in path required");
	
	return new File(TARGET_GENERATED_SOURCES_PATH, path);
    }

    /**
     * Find the first compatible service related class name of the root.
     * 
     * <p>
     * Compatible service should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible service class name
     */
    private static String findFirstCompatibleServiceClassName(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	Element port = findFirstCompatiblePort(root);

	// Get the path to the service class defined by the wsdl
	Element service = ((Element) port.getParentNode());
	String name = service.getAttribute(NAME_ATTRIBUTE);
	Assert.hasText(name, "No name attribute in service element");

	return convertNameToJavaFormat(name);
    }

    /**
     * Find the first compatible port element of the root.
     * 
     * <p>
     * Compatible port should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible port element
     */
    public static Element findFirstCompatiblePort(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Find a compatible address element
	Element address = findFirstCompatibleAddress(root);
	Assert.notNull(address, "No compatible SOAP 1.1 or 1.2 protocol");

	// Get the port element defined by the wsdl
	Element port = ((Element) address.getParentNode());

	return port;
    }

    /**
     * Find the first compatible port related class name of the root.
     * 
     * <p>
     * Compatible port should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible port element class name
     */
    public static String findFirstCompatiblePortClassName(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Get the the port element name
	return convertNameToJavaFormat(findFirstCompatiblePort(root).getAttribute(NAME_ATTRIBUTE));
    }

    /**
     * Find the first compatible port type class name of the root.
     * 
     * <p>
     * Compatible port type should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible port type class name
     */
    private static String findFirstCompatiblePortTypeClassName(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	Element binding = findFirstCompatibleBinding(root);

	// Find all port types elements
	List<Element> portTypes = XmlUtils.findElements(PORT_TYPES_XPATH, root);
	Assert.notEmpty(portTypes, "No valid port type format");
	String portTypeRef = binding.getAttribute(TYPE_ATTRIBUTE);
	Assert.hasText(portTypeRef, "No type attribute in binding element");
	Element portType = getReferencedElement(root, portTypes, portTypeRef);
	Assert.notNull(portType, "No valid port type reference");
	String portTypeName = portType.getAttribute(NAME_ATTRIBUTE);
	Assert.hasText(portTypeName, "No name attribute in port type element");

	return convertNameToJavaFormat(portTypeName);
    }

    /**
     * Find the first compatible binding name of the root.
     * 
     * <p>
     * Compatible binding should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible binding element
     */
    private static Element findFirstCompatibleBinding(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Find all binding elements
	List<Element> bindings = XmlUtils.findElements(BINDINGS_XPATH, root);
	Assert.notEmpty(bindings, "No valid binding format");

	Element port = findFirstCompatiblePort(root);
	String bindingRef = port.getAttribute(BINDING_ATTRIBUTE);
	Assert.hasText(bindingRef, "No binding attribute in port element");
	Element binding = getReferencedElement(root, bindings, bindingRef);
	Assert.notNull(binding, "No valid binding reference");

	return binding;
    }

    /**
     * URI of a wsdl namespace, or target namespace if not exists or null.
     * 
     * <p>
     * URI is defined by the value of the root attributes that starts with
     * 'xmlns' namespace. For example, for element
     * 'xmlns:tns="http://tempuri.org/"' the uri is 'http://tempuri.org/'.
     * </p>
     * 
     * @param root
     *            Wsdl root element
     * @param namespace
     *            Namespace to search it URI
     * @return Namespace URI related to the namespace
     */
    private static String getNamespaceURI(Element root, String namespace) {

	Assert.notNull(root, "Wsdl root element required");

	String namespaceURI = null;

	if (namespace != null && namespace.length() > 0) {

	    // Get the namespace related to the prefix
	    namespaceURI = root.getAttribute(XML_NAMESPACE_PREFIX + namespace);
	}

	if (namespaceURI == null) {

	    namespaceURI = root.getAttribute(TARGET_NAMESPACE_ATTRIBUTE);
	}

	return namespaceURI;
    }

    /**
     * Get the prefix of a name, or empty if not.
     * 
     * <p>
     * Prefix is the text before first namespace separator character. For
     * example, for name '<soap12:address>' the prefix is 'soap12'.
     * </p>
     * 
     * @param elementName
     *            An element name
     * @return Prefix of the name or empty if not
     */
    protected static String getNamespace(String elementName) {

	Assert.notNull(elementName, "Element name required");

	String prefix = "";

	// Get the index of the namespace separator char
	int index = elementName.indexOf(NAMESPACE_SEPARATOR);
	if (index != -1) {

	    // Get the prefix
	    prefix = elementName.substring(0, index);
	}

	return prefix;
    }

    /**
     * Get the local name of an element.
     * 
     * <p>
     * Local name is the text after first namespace separator character. For
     * example, for element name soap12:address the local name is 'address'.
     * </p>
     * 
     * @param elementName
     *            An element name
     * @return Sufix of the name or name if not
     */
    protected static String getLocalName(String elementName) {

	Assert.notNull(elementName, "Element name required");

	return elementName.replaceFirst(getNamespace(elementName)
		+ NAMESPACE_SEPARATOR, "");
    }

    /**
     * Converts a wsdl name to a valid Java format.
     * 
     * <p>
     * Valid chars are letters, numbers and $. '-', '_', ':' and '.' chars are
     * replaced with none. Other chars are replaced by unicode value with format
     * "_002f". New words in name always will be start by uppercase. 
     * </p>
     * 
     * @param name
     *            A wsdl name
     * @return Valid java name
     */
    private static String convertNameToJavaFormat(String name) {

	Assert.notNull(name, "Name required");

	StringBuffer ostr = new StringBuffer();

	// First character, uppercase
	boolean upper = true;
	for (int i = 0; i < name.length(); i++) {

	    char ch = name.charAt(i);

	    // Letter, number or $
	    if ((ch >= 'a') && (ch <= 'z') || (ch >= 'A') && (ch <= 'Z')
		    || (ch >= '0') && (ch <= '9') || ch == '$') {

		if (upper) {

		    ostr.append(Character.toUpperCase(ch));

		} else {

		    ostr.append(ch);
		}

		if ((ch >= '0') && (ch <= '9') || ch == '$') {

		    // Next character to number or $ will be uppercase
		    upper = true;

		} else {

		    upper = false;
		}
	    } else {

		// Next characters will be replace by none, others to Unicode
		if (ch != '-' && ch != '_' && ch != ':' && ch != '.') {

		    // Unicode prefix
		    ostr.append("_");

		    // Unicode value
		    String hex = Integer.toHexString(name.charAt(i) & 0xFFFF);
		    for (int j = 0; j < 4 - hex.length(); j++) {

			// Prepend zeros because unicode requires 4 digits
			ostr.append("0");
		    }

		    // Standard unicode format
		    ostr.append(hex.toLowerCase());
		}

		// Next character will be uppercase
		upper = true;
	    }
	}

	return (new String(ostr));
    }
    
    /**
     * Is wsdl document root element rpc encoded ?  
     * 
     * @param root Wsdl document root element
     * @return is rpc endoded
     */
    public static boolean isRpcEncoded(Element root) {

	Assert.notNull(root, "Wsdl root element required");
	
	// Find binding element
	Element binding = findFirstCompatibleBinding(root);

	// Find all child bindings
	List<Element> childs = XmlUtils.findElements(CHILD_BINDINGS_XPATH, root);
	Assert.notEmpty(childs, "No valid child bindings format");
	
	// Get child binding related to binding element
	for (Element child : childs) {
	    
	    // Get child parent binding element name
	    Element parentBinding = ((Element)child.getParentNode());
	    String name = parentBinding.getAttribute(NAME_ATTRIBUTE);
	    Assert.hasText(name, "No name attribute in child binding element");
	    
	    // If parent binding has the same name as binding
	    if (name.equals(binding.getAttribute(NAME_ATTRIBUTE))) {
		
		// Check RPC style
		String style = child.getAttribute(STYLE_ATTRIBUTE);
		Assert.hasText(name, "No style attribute in child binding element");
		if ("rpc".equalsIgnoreCase(style)) {
		    
		    return true;
		}
	    }
	    
	    /*
	     * TODO To be completed like next condition for each operation:
	     * 
	     *  (bindingStyle = RPC | operationStyle == RPC) & (inputUse = ENCODED | outputUse = ENCODED)
	     * 
	     * If any operation match previous condition, then is rpc encoded
	     */
	}
	
	return false;
    }
    
}
