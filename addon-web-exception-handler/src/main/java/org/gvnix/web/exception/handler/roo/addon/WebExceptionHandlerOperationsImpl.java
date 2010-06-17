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
package org.gvnix.web.exception.handler.roo.addon;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.transform.*;

import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;
import org.w3c.dom.*;
import org.apache.felix.scr.annotations.*;

/**
 * Implementation of Exception commands that are available via the Roo shell.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebExceptionHandlerOperationsImpl implements
	WebExceptionHandlerOperations {

    private static Logger logger = Logger
	    .getLogger(WebExceptionHandlerOperationsImpl.class.getName());

    private static final String ITD_TEMPLATE = "exception.jspx";

    private static final String ENGLISH_LANGUAGE_FILENAME = "/WEB-INF/i18n/messages.properties";

    private static final String LANGUAGE_FILENAMES = "WEB-INF/i18n/messages**.properties";

    private static final String DOCTYPE_PUBLIC = "-//Apache Software Foundation//DTD Tiles Configuration 2.1//EN";
    private static final String DOCTYPE_SYSTEM = "http://tiles.apache.org/dtds/tiles-config_2_1.dtd";

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private PropFileOperations propFileOperations;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #getHandledExceptionList()
     */
    public String getHandledExceptionList() {

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");
	Assert.isTrue(fileManager.exists(webXmlPath),
		"webmvc-config.xml not found");

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	List<Element> simpleMappingExceptionResolverProps = XmlUtils
		.findElements(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']/property[@name='exceptionMappings']/props/prop",
			root);

	Assert.notNull(simpleMappingExceptionResolverProps,
		"There aren't Exceptions handled by the application.");

	StringBuilder exceptionList = new StringBuilder("Handled Exceptions:\n");

	for (Element element : simpleMappingExceptionResolverProps) {
	    exceptionList.append(element.getAttribute("key") + "\n");
	}

	return exceptionList.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #addNewHandledException(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void addNewHandledException(String exceptionName,
	    String exceptionTitle, String exceptionDescription,
	    String exceptionLanguage) {

	Assert.notNull(exceptionName, "You have to provide a Exception Name.");
	Assert
		.notNull(exceptionTitle,
			"You have to provide a Exception Title.");
	Assert.notNull(exceptionDescription,
		"You have to provide a Exception Description.");

	// Update webmvcconfig.xml and retrieve the exception view name.
	// Returns the exceptionViewName.
	String exceptionViewName = updateWebMvcConfig(exceptionName);

	// Create .jspx Exception file.
	createNewExceptionView(exceptionName, exceptionTitle,
		exceptionDescription, exceptionViewName);

	// Update views.xml
	updateViewsLayout(exceptionViewName);

	// Add the message property to identify the new Exception.
	String exceptionFileName = getLanguagePropertiesFile(exceptionLanguage);
	createMultiLanguageMessages(exceptionName, exceptionTitle,
		exceptionDescription, exceptionFileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #removeExceptionHandled(java.lang.String)
     */
    public void removeExceptionHandled(String exceptionName) {

	Assert.notNull(exceptionName, "You have to provide a Exception Name.");

	// Remove Exception mapping
	String exceptionViewName = removeWebMvcConfig(exceptionName);

	// Remove view definition.
	removeViewsLayout(exceptionViewName);

	// Remove Exception jspx view.
	removeExceptionView(exceptionViewName);

	// Remove multiLanguage messages.
	removeMultiLanguageMessages(exceptionName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #languageExceptionHandled(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void languageExceptionHandled(String exceptionName,
	    String exceptionTitle, String exceptionDescription,
	    String exceptionLanguage) {

	// Checks if the Exception is handled by the
	// SimpleMappingExceptionResolver
	existException(exceptionName);

	// Retrieves the existing messages FileName in the selected Language.
	String exceptionFileName = getLanguagePropertiesFile(exceptionLanguage);

	// Updates the selected language properties fileName for the Exception.
	updateMultiLanguageMessages(exceptionName, exceptionTitle,
		exceptionDescription, exceptionFileName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #getLanguagePropertiesFile(java.lang.String)
     */
    public String getLanguagePropertiesFile(String exceptionLanguage) {

	String languagePath;

	if (exceptionLanguage.compareTo("en") == 0) {
	    languagePath = ENGLISH_LANGUAGE_FILENAME;
	} else {
	    languagePath = "WEB-INF/i18n/messages_" + exceptionLanguage
		    + ".properties";
	}

	String messagesPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		languagePath);
	Assert
		.isTrue(
			fileManager.exists(messagesPath),
			languagePath
				+ "\t Language properties file not found.\nTry another Language: [es, de, it, nl, sv, en].");
	return languagePath;
    }

    /**
     * Checks if the Exception is mapped in the SimpleMappingExceptionResolver
     * Controller
     * 
     * @param exceptionName
     *            Exception Name to Handle.
     */
    private void existException(String exceptionName) {
	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");
	Assert.isTrue(fileManager.exists(webXmlPath),
		"webmvc-config.xml not found");

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	Element simpleMappingExceptionResolverProp = XmlUtils
		.findFirstElement(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']"
				+ "/property[@name='exceptionMappings']/props/prop[@key='"
				+ exceptionName + "']", root);

	Assert.isTrue(simpleMappingExceptionResolverProp != null,
		"There isn't a Exception Handled with the name:\t"
			+ exceptionName);
    }

    /**
     * Update the webmvc-config.xml with the new Exception.
     * 
     * @param exceptionName
     *            Exception Name to Handle.
     * @return {@link String} The exceptionViewName to create the .jspx view.
     */
    private String updateWebMvcConfig(String exceptionName) {

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");
	Assert.isTrue(fileManager.exists(webXmlPath),
		"webmvc-config.xml not found");

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	Element simpleMappingExceptionResolverProps = XmlUtils
		.findFirstElement(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']"
				+ "/property[@name='exceptionMappings']/props",
			root);

	Element simpleMappingExceptionResolverProp = XmlUtils
		.findFirstElement(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']"
				+ "/property[@name='exceptionMappings']/props/prop[@key='"
				+ exceptionName + "']", root);


	if (simpleMappingExceptionResolverProp != null) {
	    return simpleMappingExceptionResolverProp.getTextContent();
	}

	// View name for this Exception.
	String exceptionViewName = getExceptionViewName(exceptionName);

	// Exception Mapping
	Element newExceptionMapping = webXml.createElement("prop");
	newExceptionMapping.setAttribute("key", exceptionName);

	Assert.isTrue(exceptionViewName != null,
		"Can't create the view for the:\t" + exceptionName
			+ " Exception.");

	newExceptionMapping.setTextContent(exceptionViewName);

	simpleMappingExceptionResolverProps.appendChild(newExceptionMapping);

	// Exception Controller
	Element newExceptionView = webXml.createElementNS(
		"http://www.springframework.org/schema/mvc", "view-controller");
	newExceptionView.setPrefix("mvc");

	newExceptionView.setAttribute("path", "/" + exceptionViewName);

	root.appendChild(newExceptionView);

	XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);

	return exceptionViewName;
    }

    /**
     * Returns the exception view name checking if exists in the file
     * webmvc-config.xml file.
     * 
     * @param exceptionName
     *            to create the view.
     * @param root
     *            {@link Element} with the values off webmvc-config.xml
     * @return
     */
    private String getExceptionViewName(String exceptionName) {

	// View name for this Exception.
	int index = exceptionName.lastIndexOf(".");
	String exceptionViewName = exceptionName;

	if (index >= 0) {
	    exceptionViewName = exceptionName.substring(index + 1);
	}
	exceptionViewName = StringUtils.uncapitalize(exceptionViewName);

	boolean exceptionNameExists = true;

	int exceptionCounter = 2;

	String tmpExceptionViewName = exceptionViewName;

	for (int i = 1; exceptionNameExists; i++) {

	    exceptionNameExists = fileManager.exists(pathResolver
		    .getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/views/"
			    + tmpExceptionViewName + ".jspx"));

	    if (exceptionNameExists) {
		tmpExceptionViewName = exceptionViewName.concat(Integer
			.toString(exceptionCounter++));
	    }
	}

	return tmpExceptionViewName;
    }

    /**
     * Removes the definition of the selected Exception in the webmvc-config.xml
     * file.
     * 
     * @param exceptionName
     *            Exception Name to remove.
     */
    private String removeWebMvcConfig(String exceptionName) {

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");
	Assert.isTrue(fileManager.exists(webXmlPath),
		"webmvc-config.xml not found");

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	Element simpleMappingExceptionResolverProp = XmlUtils
		.findFirstElement(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']/property[@name='exceptionMappings']/props/prop[@key='"
				+ exceptionName + "']", root);

	Assert.isTrue(simpleMappingExceptionResolverProp != null,
		"There isn't a Handled Exception with the name:\t"
			+ exceptionName);

	// Remove Mapping
	simpleMappingExceptionResolverProp.getParentNode().removeChild(
		simpleMappingExceptionResolverProp);

	String exceptionViewName = simpleMappingExceptionResolverProp
		.getTextContent();

	Assert.isTrue(exceptionViewName != null,
		"Can't remove the view for the:\t" + exceptionName
			+ " Exception.");

	// Remove NameSpace bean.
	Element lastExceptionControlled = XmlUtils.findFirstElement(
		"/beans/view-controller[@path='/" + exceptionViewName + "']",
		root);

	lastExceptionControlled.getParentNode().removeChild(
		lastExceptionControlled);

	XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);

	return exceptionViewName;
    }

    /**
     * Update layout.xml to map the new Exception associated View.
     * 
     * @param exceptionViewName
     *            to create the view and the definition in the xml
     */
    private void updateViewsLayout(String exceptionViewName) {

	// Exception view - create a view to show the exception message.

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/views/views.xml");
	Assert.isTrue(fileManager.exists(webXmlPath), "views.xml not found");

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	// Compare views bean.
	Element viewToCheck = XmlUtils.findFirstElement(
		"/tiles-definitions/definition[@name='" + exceptionViewName
			+ "']", root);

	// Exists the ExceptionView.
	if (viewToCheck != null) {
	    return;
	}

	// New Exception Mapping to the jspx.
	Element viewJspxException = webXml.createElement("definition");
	viewJspxException.setAttribute("name", exceptionViewName);
	viewJspxException.setAttribute("extends", "public");

	String jspxPath = "/WEB-INF/views/" + exceptionViewName + ".jspx";

	Element putAttribute = webXml.createElement("put-attribute");
	putAttribute.setAttribute("name", "body");
	putAttribute.setAttribute("value", jspxPath);

	viewJspxException.appendChild(putAttribute);

	root.appendChild(viewJspxException);

	// Define DTD
	Transformer xformer;
	try {
	    xformer = XmlUtils.createIndentingTransformer();
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, DOCTYPE_PUBLIC);
	xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DOCTYPE_SYSTEM);

	XmlUtils.writeXml(xformer, webXmlMutableFile.getOutputStream(), webXml);

    }

    /**
     * Removes the definition of the Exception view in layout.xml.
     * 
     * @param exceptionViewName
     *            Exception Name to remove.
     */
    private void removeViewsLayout(String exceptionViewName) {

	// Exception view - create a view to show the exception message.

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/views/views.xml");
	Assert.isTrue(fileManager.exists(webXmlPath), "views.xml not found");

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	Element viewToRemove = XmlUtils.findFirstElement(
		"/tiles-definitions/definition[@name='" + exceptionViewName
			+ "']", root);

	viewToRemove.getParentNode().removeChild(viewToRemove);

	// Define DTD
	Transformer xformer;
	try {
	    xformer = XmlUtils.createIndentingTransformer();
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, DOCTYPE_PUBLIC);
	xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DOCTYPE_SYSTEM);

	XmlUtils.writeXml(xformer, webXmlMutableFile.getOutputStream(), webXml);

    }

    /**
     * Creates a view for the new Handled Exception.
     * 
     * @param exceptionName
     *            Name of the Exception to handle.
     * @param exceptionTitle
     *            Title of the exception view.
     * @param exceptionDescription
     *            Description to show in the view.
     * @param exceptionViewName
     *            Name of the .jspx view.
     */
    private void createNewExceptionView(String exceptionName,
	    String exceptionTitle, String exceptionDescription,
	    String exceptionViewName) {

	String exceptionNameUncapitalize = StringUtils
		.uncapitalize(exceptionName);

	Map<String, String> params = new HashMap<String, String>(10);
	// Parameters
	params.put("error.uncaughtexception.title", "error."
		+ exceptionNameUncapitalize + ".title");
	params.put("error.uncaughtexception.problemdescription", "error."
		+ exceptionNameUncapitalize + ".problemdescription");

	String exceptionFilename = pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, "WEB-INF/views/" + exceptionViewName
			+ ".jspx");
	String template;
	try {

	    InputStream templateInputStream = TemplateUtils.getTemplate(
		    getClass(), ITD_TEMPLATE);

	    InputStreamReader readerFile = new InputStreamReader(
		    templateInputStream);

	    template = FileCopyUtils.copyToString(readerFile);

	} catch (IOException ioe) {
	    throw new IllegalStateException("Unable load ITD jspx template",
		    ioe);
	}

	template = replaceParams(template, params);

	// Output the ITD if there is actual content involved
	// (if there is no content, we continue on to the deletion phase
	// at the bottom of this conditional block)

	Assert.isTrue(template.length() > 0, "The template doesn't exists.");

	MutableFile mutableFile = null;
	if (fileManager.exists(exceptionFilename)) {
	    File f = new File(exceptionFilename);
	    String existing = null;
	    try {
		existing = FileCopyUtils.copyToString(new FileReader(f));
	    } catch (IOException ignoreAndJustOverwriteIt) {
	    }

	    if (!template.equals(existing)) {
		mutableFile = fileManager.updateFile(exceptionFilename);
	    }

	} else {
	    mutableFile = fileManager.createFile(exceptionFilename);
	    Assert.notNull(mutableFile, "Could not create ITD file '"
		    + exceptionFilename + "'");
	}

	try {
	    if (mutableFile != null) {
		FileCopyUtils.copy(template.getBytes(), mutableFile
			.getOutputStream());
	    }
	} catch (IOException ioe) {
	    throw new IllegalStateException("Could not output '"
		    + mutableFile.getCanonicalPath() + "'", ioe);
	}
    }

    /**
     * Removes Exception view .jspx.
     * 
     * @param exceptionViewName
     *            Exception Name to remove.
     */
    private void removeExceptionView(String exceptionViewName) {

	String exceptionFilename = pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, "WEB-INF/views/" + exceptionViewName
			+ ".jspx");

	fileManager.delete(exceptionFilename);

    }

    /**
     * Method to replace the parameters set in the Map into the template.
     * 
     * @param template
     *            Template to create a new jspx.
     * @param params
     *            {@link Map} with the specified parameters.
     * 
     * @return {@link String} with the updated parameters values.
     */
    private String replaceParams(String template, Map<String, String> params) {
	for (Entry<String, String> entry : params.entrySet()) {
	    template = StringUtils.replace(template, "${" + entry.getKey()
		    + "}", entry.getValue());
	}
	return template;
    }

    /**
     * Updates the selected language file with the title and the description of
     * the new Exception.
     * 
     * @param exceptionName
     *            Name of the Exception.
     * @param exceptionTitle
     *            Title of the Exception.
     * @param exceptionDescription
     *            Description of the Exception.
     */
    private void createMultiLanguageMessages(String exceptionName,
	    String exceptionTitle, String exceptionDescription,
	    String propertyFileName) {

	String exceptionNameUncapitalize = StringUtils
		.uncapitalize(exceptionName);

	SortedSet<FileDetails> propertiesFiles = getPropertiesFiles();

	Map<String, String> params = new HashMap<String, String>(10);
	// Parameters
	params.put("error." + exceptionNameUncapitalize + ".title",
		exceptionTitle);
	params.put(
		"error." + exceptionNameUncapitalize + ".problemdescription",
		exceptionDescription);

	String propertyFilePath = "/WEB-INF/i18n/";
	String canonicalPath;
	String fileName;

	String tmpProperty;
	for (Entry<String, String> entry : params.entrySet()) {

	    for (FileDetails fileDetails : propertiesFiles) {

		canonicalPath = fileDetails.getCanonicalPath();

		fileName = propertyFilePath.concat(StringUtils
			.getFilename(canonicalPath));

		if (propertyFileName.compareTo(fileName.substring(1)) == 0) {

		    tmpProperty = propFileOperations.getProperty(
			    Path.SRC_MAIN_WEBAPP, fileName, entry.getKey());
		    if (tmpProperty == null) {

			propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
				propertyFileName, entry.getKey(), entry
					.getValue());
		    } else if (tmpProperty.compareTo(entry.getValue()) != 0) {
			propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
				propertyFileName, entry.getKey(), entry
					.getValue());
		    }
		} else {

		    // Updates the file if the property doesn't exists.
		    if (propFileOperations.getProperty(Path.SRC_MAIN_WEBAPP,
			    fileName, entry.getKey()) == null) {
			propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
				fileName, entry.getKey(), entry.getKey());
		    }
		}
	    }

	}

    }

    /**
     * Updates the selected language file with the title and the description of
     * the new Exception.
     * 
     * @param exceptionName
     *            Name of the Exception.
     * @param exceptionTitle
     *            Title of the Exception.
     * @param exceptionDescription
     *            Description of the Exception.
     */
    private void updateMultiLanguageMessages(String exceptionName,
	    String exceptionTitle, String exceptionDescription,
	    String propertyFileName) {

	String exceptionNameUncapitalize = StringUtils
		.uncapitalize(exceptionName);

	Map<String, String> params = new HashMap<String, String>(10);
	// Parameters
	params.put("error." + exceptionNameUncapitalize + ".title",
		exceptionTitle);
	params.put(
		"error." + exceptionNameUncapitalize + ".problemdescription",
		exceptionDescription);

	for (Entry<String, String> entry : params.entrySet()) {

	    String tmpProperty = propFileOperations.getProperty(
		    Path.SRC_MAIN_WEBAPP,
		    propertyFileName, entry.getKey());
	    if (tmpProperty == null) {

		propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
			propertyFileName, entry.getKey(), entry.getValue());
	    } else if (tmpProperty.compareTo(entry.getValue()) != 0) {
		propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
			propertyFileName, entry.getKey(), entry.getValue());
	    }

	}

    }

    /**
     * Removes the Language messages properties of the Exception.
     * 
     * @param exceptionName
     *            Exception Name to remove.
     */
    private void removeMultiLanguageMessages(String exceptionName) {

	String exceptionNameUncapitalize = StringUtils
		.uncapitalize(exceptionName);

	SortedSet<FileDetails> propertiesFiles = getPropertiesFiles();

	Map<String, String> params = new HashMap<String, String>(10);
	// Parameters
	params.put("error." + exceptionNameUncapitalize + ".title", "");
	params.put(
		"error." + exceptionNameUncapitalize + ".problemdescription",
		"");

	String propertyFilePath = "/WEB-INF/i18n/";
	String fileName;
	String canonicalPath;

	for (Entry<String, String> entry : params.entrySet()) {

	    for (FileDetails fileDetails : propertiesFiles) {

		canonicalPath = fileDetails.getCanonicalPath();

		fileName = propertyFilePath.concat(StringUtils
			.getFilename(canonicalPath));

		propFileOperations.removeProperty(Path.SRC_MAIN_WEBAPP,
			fileName, entry.getKey());
	    }

	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #setUpGvNIXExceptions()
     */
    public void setUpGvNIXExceptions() {

	// java.sql.SQLException
	addNewHandledException("java.sql.SQLException", "SQLException",
		"Se ha producido un error en el acceso a la Base de datos.",
		"es");

	languageExceptionHandled("java.sql.SQLException", "SQLException",
		"There was an error accessing the database.", "en");

	// java.io.IOException
	addNewHandledException("java.io.IOException", "IOException",
		"Existen problemas para enviar o recibir datos.", "es");

	languageExceptionHandled("java.io.IOException", "IOException",
		"There are problems sending or receiving data.", "en");

	// org.springframework.transaction.TransactionException
	addNewHandledException(
		"org.springframework.transaction.TransactionException",
		"TransactionException",
		"Se ha producido un error en la transacción. No se han guardado los datos correctamente.",
		"es");

	languageExceptionHandled(
		"org.springframework.transaction.TransactionException",
		"TransactionException",
		"There was an error in the transaction. No data have been stored properly.",
		"en");

	// java.lang.UnsupportedOperationException
	addNewHandledException("java.lang.UnsupportedOperationException",
		"UnsupportedOperationException",
		"Se ha producido un error no controlado.", "es");

	languageExceptionHandled("java.lang.UnsupportedOperationException",
		"UnsupportedOperationException",
		"There was an unhandled error.", "en");
    }

    private boolean isGvNixExceptionActivated(String exceptionName) {
	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");
	Assert.isTrue(fileManager.exists(webXmlPath),
		"webmvc-config.xml not found");

	Document webXml;

	try {
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    fileManager.getInputStream(webXmlPath));
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	Element root = webXml.getDocumentElement();

	Element simpleMappingExceptionResolverProp = XmlUtils
		.findFirstElement(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']/property[@name='exceptionMappings']/props/prop[@key='"
				+ exceptionName + "']", root);

	if (simpleMappingExceptionResolverProp == null) {
	    return false;
	}

	return true;
    }

    /**
     * Retrieves the messages properties files of the application
     * 
     * @return {@link SortedSet} with the messages properties files
     */
    private SortedSet<FileDetails> getPropertiesFiles() {

	SortedSet<FileDetails> propertiesFiles = fileManager
		.findMatchingAntPath(pathResolver.getIdentifier(
			Path.SRC_MAIN_WEBAPP, LANGUAGE_FILENAMES));

	return propertiesFiles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #isExceptionMappingAvailable()
     */
    public boolean isExceptionMappingAvailable() {

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");

	if (!fileManager.exists(webXmlPath)) {
	    return false;
	}

	MutableFile webXmlMutableFile = null;
	Document webXml;

	try {
	    webXmlMutableFile = fileManager.updateFile(webXmlPath);
	    webXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = webXml.getDocumentElement();

	Element simpleMappingExceptionResolverProp = XmlUtils
		.findFirstElement(
			"/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']"
				+ "/property[@name='exceptionMappings']/props/prop",
			root);

	boolean isExceptionAvailable = (simpleMappingExceptionResolverProp != null) ? true
		: false;

	return isExceptionAvailable;
    }

    public boolean isProjectAvailable() {

	if (getPathResolver() == null) {
	    return false;
	}

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");

	if (!fileManager.exists(webXmlPath)) {
	    return false;
	}
	return true;
    }

    /**
     * @return the path resolver or null if there is no user project.
     */
    private PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }

}
