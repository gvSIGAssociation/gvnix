/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.service.roo.addon.addon.util;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * gvNIX Wsdl parser utilities.
 * <p>
 * Compatible address should be SOAP protocol version 1.1 and 1.2.
 * </p>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public class WsdlParserUtils {

    private static final String ROOT_ELEMENT_REQUIRED = "Wsdl root element required";
    /** Compatible SOAP 1.1 and SOAP 1.2 namespaces **/
    public static final String SOAP_11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String NAMESPACE_WITHOUT_SLASH_11 = SOAP_11_NAMESPACE
            .substring(0, SOAP_11_NAMESPACE.length() - 1);
    public static final String SOAP_12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String NAMESPACE_WITHOUT_SLASH_12 = SOAP_12_NAMESPACE
            .substring(0, SOAP_12_NAMESPACE.length() - 1);

    public static final String XML_NAMESPACE_PREFIX = "xmlns:";

    /** Character separators in different strings **/
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String FILE_SEPARATOR = File.separator;
    public static final String XPATH_SEPARATOR = "/";
    public static final String PACKAGE_SEPARATOR = ".";

    /** Tokens in a namespace that are treated as package name part separators. */
    protected static final char[] pkgSeparators = { '.', ':' };

    /** Field javaPkgSeparator */
    public static final char javaPkgSeparator = pkgSeparators[0];

    /**
     * These are java keywords as specified at the following URL (sorted
     * alphabetically).
     * http://java.sun.com/docs/books/jls/second_edition/html/lexical
     * .doc.html#229308 Note that false, true, and null are not strictly
     * keywords; they are literal values, but for the purposes of this array,
     * they can be treated as literals. ****** PLEASE KEEP THIS LIST SORTED IN
     * ASCENDING ORDER ******
     */
    protected static final String keywords[] = { "abstract", "assert",
            "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private", "protected",
            "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while" };

    /** Collator for comparing the strings */
    public static final Collator englishCollator = Collator
            .getInstance(Locale.ENGLISH);

    /** Use this character as suffix */
    public static final char keywordPrefix = '_';

    /** Path to client generated sources (axis and cxf) **/
    public static final String TARGET_GENERATED_SOURCES_PATH = "."
            + FILE_SEPARATOR + "target" + FILE_SEPARATOR + "generated-sources"
            + FILE_SEPARATOR + "client";

    /** WSDL element names used **/
    public static final String DEFINITIONS_ELEMENT = "definitions";
    public static final String BINDING_ELEMENT = "binding";
    public static final String PORT_TYPE_ELEMENT = "portType";
    public static final String SERVICE_ELEMENT = "service";
    public static final String PORT_ELEMENT = "port";
    public static final String ADDRESS_ELEMENT = "address";

    /** WSDL attribute names used **/
    public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String BINDING_ATTRIBUTE = "binding";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String STYLE_ATTRIBUTE = "style";

    /** WSDL xpaths used **/
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

    /**
     * Get the namespace attribute from root wsdl
     * 
     * @param root Root element of the wsdl
     * @return Wsdl namespace
     */
    public static String getTargetNamespace(Element root) {

        // Get the namespace attribute from root wsdl
        String namespace = root.getAttribute(TARGET_NAMESPACE_ATTRIBUTE);
        StringUtils.isNotBlank(namespace);

        return namespace;
    }

    /**
     * Constructs a valid java package path from target namespace of root wsdl.
     * <p>
     * Package ends with the package separator. Related package is different
     * when web service is rpc encoded or not.
     * </p>
     * 
     * @param root Root element of the wsdl
     * @return Equivalent java package or empty
     */
    public static String getTargetNamespaceRelatedPackage(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Get the namespace attribute from root wsdl
        String namespace = getTargetNamespace(root);

        String pkg = getTargetNamespaceRelatedPackage(namespace, root)
                .toLowerCase();
        pkg = pkg.replace('_', 'u');

        return pkg.concat(".");
    }

    /**
     * Constructs a valid java package path from a namespace.
     * <p>
     * Package ends with the package separator. If target namespace has not a
     * compatible prefix, empty string will be returned.
     * </p>
     * 
     * @param namespace Name space
     * @param root Root element of the wsdl
     * @return Equivalent java package or empty
     */
    private static String getTargetNamespaceRelatedPackage(String namespace,
            Element root) {

        return normalizePackageName(makePackageName(namespace),
                javaPkgSeparator);
    }

    /**
     * Method normalizePackageName.
     * 
     * @param pkg
     * @param separator
     * @return
     */
    private static String normalizePackageName(String pkg, char separator) {

        for (int i = 0; i < pkgSeparators.length; i++) {
            pkg = pkg.replace(pkgSeparators[i], separator);
        }

        return pkg;
    }

    /**
     * Method makePackageName.
     * 
     * @param namespace
     * @return
     */
    public static String makePackageName(String namespace) {

        String hostname = null;
        String path = "";

        // get the target namespace of the document
        try {

            URL u = new URL(namespace);
            hostname = u.getHost();
            path = u.getPath();

        }
        catch (MalformedURLException e) {

            if (namespace.indexOf(':') > -1) {

                hostname = namespace.substring(namespace.indexOf(':') + 1);
                if (hostname.indexOf('/') > -1) {

                    hostname = hostname.substring(0, hostname.indexOf('/'));
                }
            }
            else {

                hostname = namespace;
            }
        }

        // if we didn't file a hostname, bail
        if (hostname == null) {
            return null;
        }

        // convert illegal java identifier
        hostname = hostname.replace('-', '_');
        path = path.replace('-', '_');

        // chomp off last forward slash in path, if necessary
        if ((path.length() > 0) && (path.charAt(path.length() - 1) == '/')) {
            path = path.substring(0, path.length() - 1);
        }

        // tokenize the hostname and reverse it
        StringTokenizer st = new StringTokenizer(hostname, ".:");
        String[] words = new String[st.countTokens()];

        for (int i = 0; i < words.length; ++i) {
            words[i] = st.nextToken();
        }

        StringBuffer sb = new StringBuffer(namespace.length());

        for (int i = words.length - 1; i >= 0; --i) {
            addWordToPackageBuffer(sb, words[i], (i == words.length - 1));
        }

        // tokenize the path
        StringTokenizer st2 = new StringTokenizer(path, "/");

        while (st2.hasMoreTokens()) {
            addWordToPackageBuffer(sb, st2.nextToken(), false);
        }

        return sb.toString();
    }

    /**
     * Massage word into a form suitable for use in a Java package name.
     * <p>
     * Append it to the target string buffer with a <tt>.</tt> delimiter if
     * <tt>word</tt> is not the first word in the package name.
     * </p>
     * 
     * @param sb the buffer to append to
     * @param word the word to append
     * @param firstWord a flag indicating whether this is the first word
     */
    private static void addWordToPackageBuffer(StringBuffer sb, String word,
            boolean firstWord) {

        if (isJavaKeyword(word)) {
            word = makeNonJavaKeyword(word);
        }

        // separate with dot after the first word
        if (!firstWord) {
            sb.append('.');
        }

        // prefix digits with underscores
        if (Character.isDigit(word.charAt(0))) {
            sb.append('_');
        }

        // replace periods with underscores
        if (word.indexOf('.') != -1) {
            char[] buf = word.toCharArray();

            for (int i = 0; i < word.length(); i++) {
                if (buf[i] == '.') {
                    buf[i] = '_';
                }
            }

            word = new String(buf);
        }

        sb.append(word);
    }

    /**
     * Checks if the input string is a valid java keyword.
     * 
     * @return boolean true/false
     */
    public static boolean isJavaKeyword(String keyword) {
        return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0);
    }

    /**
     * Turn a java keyword string into a non-Java keyword string.
     * <p>
     * Right now this simply means appending an underscore.
     * </p>
     */
    public static String makeNonJavaKeyword(String keyword) {
        return keywordPrefix + keyword;
    }

    /**
     * Find the first compatible address element of the root.
     * <p>
     * Compatible address should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl.
     * @return First compatible address element or null if no element.
     */
    public static Element findFirstCompatibleAddress(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

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
        boolean soap1_2 = false;
        boolean soap1_1 = false;
        if ((index = namespaces.indexOf(SOAP_12_NAMESPACE)) != -1
                || (index = namespaces.indexOf(NAMESPACE_WITHOUT_SLASH_12)) != -1) {

            // First preference: SOAP 1.2 protocol
            soap1_2 = true;

        }
        else if ((index = namespaces.indexOf(SOAP_11_NAMESPACE)) != -1
                || (index = namespaces.indexOf(NAMESPACE_WITHOUT_SLASH_11)) != -1) {

            // Second preference: SOAP 1.1 protocol
            soap1_1 = true;
        }
        if (!(soap1_2 || soap1_1)) {
            // Other protocols not supported
            return null;
        }

        return addresses.get(index);
    }

    /**
     * Obtain from the list the element with the reference on the root wsdl.
     * 
     * @param root Root wsdl
     * @param elements Elements list to search in
     * @param reference Reference to be searched
     * @return Element found or null if not
     */
    private static Element getReferencedElement(Element root,
            List<Element> elements, String reference) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);
        Validate.notNull(elements, "Elements list required");
        Validate.notNull(reference, "Reference required");

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
     * @param root Wsdl root element
     * @param sense Communication sense type
     * @return Path to the class
     */
    public static String getServiceClassPath(Element root, WsType sense) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Build the classpath related to the namespace
        String path = getTargetNamespaceRelatedPackage(root);

        // Find a compatible service name
        String name = findFirstCompatibleServiceClassName(root, sense);

        if (sense.equals(WsType.IMPORT_RPC_ENCODED)) {

            // Rpc generated service source ends with this string
            name = name.concat("Locator");
        }

        // Class path is the concat of path and name
        return path + capitalizeFirstChar(name);
    }

    /**
     * Get the path to the generated port type class.
     * 
     * @param root Wsdl root element
     * @param sense Communication sense type
     * @return Path to the class
     */
    public static String getPortTypeClassPath(Element root, WsType sense) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Build the classpath related to the namespace
        String path = getTargetNamespaceRelatedPackage(root);

        // Find a compatible port and port type name
        String portType = findFirstCompatiblePortTypeClassName(root, sense);
        String port = findFirstCompatiblePortClassName(root, sense);

        // RPC Encoded web services adds sufix to port type when equals to port
        if (WsType.IMPORT_RPC_ENCODED.equals(sense) && portType.equals(port)) {

            portType = portType.concat("_PortType");
        }

        // Class path is the concat of path and name
        return path + capitalizeFirstChar(portType);
    }

    /**
     * Get the port type Java file
     * 
     * @param root Wsdl root element
     * @param sense Communication sense type
     * @return Java file
     */
    public static File getPortTypeJavaFile(Element root, WsType sense) {

        return getGeneratedJavaFile(convertTypePathToJavaPath(getPortTypeClassPath(
                root, sense)));
    }

    /**
     * Convert the path to a type to the java file path.
     * 
     * @param classPath Path to the class
     * @return Path to the java file
     */
    private static String convertTypePathToJavaPath(String classPath) {

        StringUtils.isNotBlank(classPath);

        return classPath.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR).concat(
                ".java");
    }

    /**
     * Get the file on path in generated sources folder.
     * 
     * @param path Searched path
     * @return File to path
     */
    private static File getGeneratedJavaFile(String path) {

        StringUtils.isNotBlank(path);

        return new File(TARGET_GENERATED_SOURCES_PATH, path);
    }

    /**
     * Find the first compatible service related class name of the root.
     * <p>
     * Compatible service should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @param sense Communication sense type
     * @return First compatible service class name
     */
    private static String findFirstCompatibleServiceClassName(Element root,
            WsType sense) {

        String name = findFirstCompatibleServiceElementName(root);

        return convertNameToJavaFormat(name, sense);
    }

    /**
     * Find the first compatible service related element name of the root.
     * <p>
     * Compatible service should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @param sense Communication sense type
     * @return First compatible service class name
     */
    public static String findFirstCompatibleServiceElementName(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        Element port = findFirstCompatiblePort(root);

        // Get the path to the service class defined by the wsdl
        Element service = ((Element) port.getParentNode());
        String name = service.getAttribute(NAME_ATTRIBUTE);
        StringUtils.isNotBlank(name);

        return name;
    }

    /**
     * Find the first compatible port element of the root.
     * <p>
     * Compatible port should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @return First compatible port element
     */
    public static Element findFirstCompatiblePort(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Find a compatible address element
        Element address = findFirstCompatibleAddress(root);
        Validate.notNull(address, "No compatible SOAP 1.1 or 1.2 protocol");

        // Get the port element defined by the wsdl
        Element port = ((Element) address.getParentNode());

        return port;
    }

    /**
     * Check port if Supported port element of the root.
     * <p>
     * Should exists only one compatible port using SOAP protocol version 1.1 or
     * 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @return Compatible port element
     */
    public static Element checkCompatiblePort(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Find a compatible address element
        Element address = checkCompatibleAddress(root);
        Validate.notNull(address, "No compatible SOAP 1.1 or 1.2 protocol");

        // Get the port element defined by the wsdl
        Element port = ((Element) address.getParentNode());

        return port;
    }

    /**
     * Check compatible address element of the root.
     * <p>
     * Should exists only one compatible address using SOAP protocol version 1.1
     * or 1.2.
     * </p>
     * 
     * @param root Root element of wsdl.
     * @return First compatible address element or null if no element.
     */
    public static Element checkCompatibleAddress(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

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
        boolean isSoap12Compatible = false;
        boolean isSoap11Compatible = false;
        int indexSoap12 = 0;
        int indexSoap11 = 0;
        if ((indexSoap12 = namespaces.indexOf(SOAP_12_NAMESPACE)) != -1
                || (indexSoap12 = namespaces
                        .indexOf(NAMESPACE_WITHOUT_SLASH_12)) != -1) {

            // First preference: SOAP 1.2 protocol
            isSoap12Compatible = true;

        }
        if ((indexSoap11 = namespaces.indexOf(SOAP_11_NAMESPACE)) != -1
                || (indexSoap11 = namespaces
                        .indexOf(NAMESPACE_WITHOUT_SLASH_11)) != -1) {

            if (isSoap12Compatible) {
                Validate.validState(
                        false,
                        "There are defined SOAP 1.1 and 1.2 protocols.\nMust be only one protocol defined.");
            }

            isSoap11Compatible = true;
            // Second preference: SOAP 1.1 protocol

        }

        int index = 0;

        if (isSoap12Compatible && !isSoap11Compatible) {
            index = indexSoap12;
        }
        else if (isSoap11Compatible && !isSoap12Compatible) {
            index = indexSoap11;
        }
        else {
            // Other protocols not supported
            return null;
        }

        return addresses.get(index);
    }

    /**
     * Find the first compatible port related class name of the root.
     * <p>
     * Compatible port should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @param sense Communication sense type
     * @return First compatible port element class name
     */
    public static String findFirstCompatiblePortClassName(Element root,
            WsType sense) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Get the the port element name
        return convertNameToJavaFormat(findFirstCompatiblePort(root)
                .getAttribute(NAME_ATTRIBUTE), sense);
    }

    /**
     * Find the first compatible port type class name of the root.
     * <p>
     * Compatible port type should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @param sense Communication sense type
     * @return First compatible port type class name
     */
    private static String findFirstCompatiblePortTypeClassName(Element root,
            WsType sense) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        Element binding = findFirstCompatibleBinding(root);

        // Find all port types elements
        List<Element> portTypes = XmlUtils.findElements(PORT_TYPES_XPATH, root);
        Validate.notEmpty(portTypes, "No valid port type format");
        String portTypeRef = binding.getAttribute(TYPE_ATTRIBUTE);
        StringUtils.isNotEmpty(portTypeRef);
        Element portType = getReferencedElement(root, portTypes, portTypeRef);
        Validate.notNull(portType, "No valid port type reference");
        String portTypeName = portType.getAttribute(NAME_ATTRIBUTE);
        StringUtils.isNotEmpty(portTypeName);

        return convertNameToJavaFormat(portTypeName, sense);
    }

    /**
     * Find the first compatible binding name of the root.
     * <p>
     * Compatible binding should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root Root element of wsdl
     * @return First compatible binding element
     */
    private static Element findFirstCompatibleBinding(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Find all binding elements
        List<Element> bindings = XmlUtils.findElements(BINDINGS_XPATH, root);
        Validate.notEmpty(bindings, "No valid binding format");

        Element port = findFirstCompatiblePort(root);
        String bindingRef = port.getAttribute(BINDING_ATTRIBUTE);
        StringUtils.isNotEmpty(bindingRef);
        Element binding = getReferencedElement(root, bindings, bindingRef);
        Validate.notNull(binding, "No valid binding reference");

        return binding;
    }

    /**
     * URI of a wsdl namespace, or target namespace if not exists or null.
     * <p>
     * URI is defined by the value of the root attributes that starts with
     * 'xmlns' namespace. For example, for element
     * 'xmlns:tns="http://tempuri.org/"' the uri is 'http://tempuri.org/'.
     * </p>
     * 
     * @param root Wsdl root element
     * @param namespace Namespace to search it URI
     * @return Namespace URI related to the namespace
     */
    private static String getNamespaceURI(Element root, String namespace) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        String namespaceURI = null;

        if (namespace != null && namespace.length() > 0) {

            // Get the namespace related to the prefix
            namespaceURI = root.getAttribute(XML_NAMESPACE_PREFIX + namespace);
        }

        if (namespaceURI == null) {

            namespaceURI = getTargetNamespace(root);
        }

        return namespaceURI;
    }

    /**
     * Get the prefix of a name, or empty if not.
     * <p>
     * Prefix is the text before first namespace separator character. For
     * example, for name '<soap12:address>' the prefix is 'soap12'.
     * </p>
     * 
     * @param elementName An element name
     * @return Prefix of the name or empty if not
     */
    protected static String getNamespace(String elementName) {

        Validate.notNull(elementName, "Element name required");

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
     * <p>
     * Local name is the text after first namespace separator character. For
     * example, for element name soap12:address the local name is 'address'.
     * </p>
     * 
     * @param elementName An element name
     * @return Sufix of the name or name if not
     */
    protected static String getLocalName(String elementName) {

        Validate.notNull(elementName, "Element name required");

        return elementName.replaceFirst(getNamespace(elementName)
                + NAMESPACE_SEPARATOR, "");
    }

    /**
     * Converts a wsdl name to a valid Java format.
     * <p>
     * The conversion is different if RPC/Encoded communication sense.
     * </p>
     * 
     * @param name A wsdl name
     * @param sense Communication sense type
     * @return Valid java name
     */
    private static String convertNameToJavaFormat(String name, WsType sense) {

        if (WsType.IMPORT_RPC_ENCODED.equals(sense)) {

            return convertRpcNameToJavaFormat(name);
        }

        return convertDocumentNameToJavaFormat(name);
    }

    /**
     * Converts a wsdl name to a valid Java format in Document.
     * <p>
     * Valid chars are letters, numbers and $. '-', '_', ':' and '.' chars are
     * replaced with none. Other chars are replaced by unicode value with format
     * "_002f". New words in name always will be start by uppercase.
     * </p>
     * 
     * @param name A wsdl name
     * @return Valid java name
     */
    private static String convertDocumentNameToJavaFormat(String name) {

        Validate.notNull(name, "Name required");

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

                }
                else {

                    ostr.append(ch);
                }

                if ((ch >= '0') && (ch <= '9') || ch == '$') {

                    // Next character to number or $ will be uppercase
                    upper = true;

                }
                else {

                    upper = false;
                }
            }
            else {

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
     * Converts a wsdl name to a valid Java format in RPC.
     * 
     * @param name A wsdl name
     * @return Valid java name
     */
    private static String convertRpcNameToJavaFormat(String name) {

        String java = new String(name);

        if (!isJavaId(java)) {
            java = xmlNameToJavaClass(name);
        }

        return java;
    }

    /**
     * Returns true if the name is a valid java identifier.
     * 
     * @param id to check
     * @return boolean true/false
     **/
    public static boolean isJavaId(String id) {

        if (id == null || id.equals("") || isJavaKeyword(id))
            return false;
        if (!Character.isJavaIdentifierStart(id.charAt(0)))
            return false;
        for (int i = 1; i < id.length(); i++)
            if (!Character.isJavaIdentifierPart(id.charAt(i)))
                return false;

        return true;
    }

    /**
     * Map an XML name to a valid Java identifier w/ capitalized first letter.
     * 
     * @param name
     * @return
     */
    public static String xmlNameToJavaClass(String name) {

        return capitalizeFirstChar(xmlNameToJava(name));
    }

    /**
     * Capitalize the first character of the name.
     * 
     * @param name
     * @return
     */
    public static String capitalizeFirstChar(String name) {

        if ((name == null) || name.equals("")) {
            return name;
        }

        char start = name.charAt(0);

        if (Character.isLowerCase(start)) {
            start = Character.toUpperCase(start);

            return start + name.substring(1);
        }

        return name;
    }

    /**
     * Map an XML name to a Java identifier per the mapping rules of JSR 101 (in
     * version 1.0 this is "Chapter 20: Appendix: Mapping of XML Names"
     * 
     * @param name is the xml name
     * @return the java name per JSR 101 specification
     */
    public static String xmlNameToJava(String name) {

        // protect ourselves from garbage
        if (name == null || name.equals(""))
            return name;

        char[] nameArray = name.toCharArray();
        int nameLen = name.length();
        StringBuffer result = new StringBuffer(nameLen);
        boolean wordStart = false;

        // The mapping indicates to convert first character.
        int i = 0;
        while (i < nameLen
                && (isPunctuation(nameArray[i]) || !Character
                        .isJavaIdentifierStart(nameArray[i]))) {
            i++;
        }
        if (i < nameLen) {
            // Decapitalization code used to be here, but we use the
            // Introspector function now after we filter out all bad chars.

            result.append(nameArray[i]);
            // wordStart = !Character.isLetter(nameArray[i]);
            wordStart = !Character.isLetter(nameArray[i])
                    && nameArray[i] != "_".charAt(0);
        }
        else {
            // The identifier cannot be mapped strictly according to
            // JSR 101
            if (Character.isJavaIdentifierPart(nameArray[0])) {
                result.append("_" + nameArray[0]);
            }
            else {
                // The XML identifier does not contain any characters
                // we can map to Java. Using the length of the string
                // will make it somewhat unique.
                result.append("_" + nameArray.length);
            }
        }

        // The mapping indicates to skip over
        // all characters that are not letters or
        // digits. The first letter/digit
        // following a skipped character is
        // upper-cased.
        for (++i; i < nameLen; ++i) {
            char c = nameArray[i];

            // if this is a bad char, skip it and remember to capitalize next
            // good character we encounter
            if (isPunctuation(c) || !Character.isJavaIdentifierPart(c)) {
                wordStart = true;
                continue;
            }
            if (wordStart && Character.isLowerCase(c)) {
                result.append(Character.toUpperCase(c));
            }
            else {
                result.append(c);
            }
            // If c is not a character, but is a legal Java
            // identifier character, capitalize the next character.
            // For example: "22hi" becomes "22Hi"
            // wordStart = !Character.isLetter(c);
            wordStart = !Character.isLetter(c) && c != "_".charAt(0);
        }

        // covert back to a String
        String newName = result.toString();

        // Follow JavaBean rules, but we need to check if the first
        // letter is uppercase first
        if (Character.isUpperCase(newName.charAt(0)))
            newName = Introspector.decapitalize(newName);

        // check for Java keywords
        if (isJavaKeyword(newName))
            newName = makeNonJavaKeyword(newName);

        return newName;
    }

    /**
     * Is this an XML punctuation character?
     */
    private static boolean isPunctuation(char c) {

        return '-' == c || '.' == c || ':' == c || '\u00B7' == c
                || '\u0387' == c || '\u06DD' == c || '\u06DE' == c;
    }

    /**
     * Is wsdl document root element rpc encoded ?
     * 
     * @param root Wsdl document root element
     * @return is rpc endoded
     */
    public static boolean isRpcEncoded(Element root) {

        Validate.notNull(root, ROOT_ELEMENT_REQUIRED);

        // Find binding element
        Element binding = findFirstCompatibleBinding(root);

        // Find all child bindings
        List<Element> childs = XmlUtils
                .findElements(CHILD_BINDINGS_XPATH, root);
        Validate.notEmpty(childs, "No valid child bindings format");

        // Get child binding related to binding element
        for (Element child : childs) {

            // Get child parent binding element name
            Element parentBinding = ((Element) child.getParentNode());
            String name = parentBinding.getAttribute(NAME_ATTRIBUTE);
            StringUtils.isNotEmpty(name);

            // If parent binding has the same name as binding
            if (name.equals(binding.getAttribute(NAME_ATTRIBUTE))) {

                // Check RPC style
                String style = child.getAttribute(STYLE_ATTRIBUTE);
                StringUtils.isNotEmpty(name);
                if ("rpc".equalsIgnoreCase(style)) {

                    return true;
                }
            }

            /*
             * TODO To be completed like next condition for each operation:
             *
             * (bindingStyle = RPC | operationStyle == RPC) & (inputUse =
             * ENCODED | outputUse = ENCODED)
             *
             * If any operation match previous condition, then is rpc encoded
             */
        }

        return false;
    }

    /**
     * Check connection and WSDL format from the given url.<br/>
     * If you need SSL support acceding to WSLD you should use
     * {@link SecurityService#getWsdl(String)}
     * 
     * @param url URL to check
     * @return Wsdl document root element
     * @exception IllegalStateException wsdl no connection or invalid
     */
    public static Element validateWsdlUrl(String url) {

        try {

            // Parse the wsdl location to a DOM document
            Document wsdl = XmlUtils.getDocumentBuilder().parse(url);
            Element root = wsdl.getDocumentElement();
            Validate.notNull(root, "No valid document format");

            return root;

        }
        catch (SAXException e) {

            throw new IllegalStateException("The format of the wsdl has errors");

        }
        catch (IOException e) {

            throw new IllegalStateException("There is no access to the wsdl");
        }
    }

}
