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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utilities to manage the CXF web services library.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWsConfigServiceImpl implements ServiceLayerWsConfigService {

    @Reference
    private MetadataService metadataService;
    @Reference
    private FileManager fileManager;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;

    private static final String DOCTYPE_PUBLIC = "-//tuckey.org//DTD UrlRewrite 3.0//EN";
    private static final String DOCTYPE_SYSTEM = "http://tuckey.org/res/dtds/urlrewrite3.0.dtd";
    
    private static Logger logger = Logger
	    .getLogger(ServiceLayerWsConfigService.class.getName());

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Check if Cxf is set in the project.
     * </p>
     * <p>
     * If is not set, then installs dependencies to the pom.xml and creates the
     * cxf configuration file.
     * </p>
     * 
     * @param type Communication type
     */
    public void install(CommunicationSense type) {

	// Check if it's already installed.
	if (isCxfInstalled(type)) {
	    // Nothing to do
	    return;
	}

	// Add dependencies to project
	installCxfDependencies(type);
	
	if (type == CommunicationSense.EXPORT) {

	    // Create CXF config file src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	    installCxfConfigurationFile();

	    // Update src/main/webapp/WEB-INF/web.xml :
	    // - Add CXFServlet and map it to /services/*
	    // - Add cxf-PROJECT_NAME.xml to Spring Context Loader
	    installCxfWebConfigurationFile();

	    // TODO: comprobar si ya se ha actualizado el fichero urlrewrite.
	    // Setup URL rewrite to avoid to filter requests to WebServices
	    installCxfUrlRewriteConfigurationFile();
	}
    }

    /**
     * Checks if library is properly configured in a project.
     * 
     * <p>
     * Checks these types:
     * </p>
     * <ul>
     * <li>
     * Cxf Dependencies in pom.xml</li>
     * <li>
     * Cxf configuration file exists</li>
     * </ul>
     * 
     * @param type
     *            Communication type
     * @return true or false if it's configurated
     */
    private boolean isCxfInstalled(CommunicationSense type) {
	
	// TODO Are not checked Web and Url Rewrite configuration files, check it ?
	
	boolean cxfInstalled = isCxfDependenciesInstalled(type);
	
	if (type == CommunicationSense.EXPORT) {

	    cxfInstalled = cxfInstalled
		    && fileManager.exists(getCxfConfigurationFilePath());
	}
	
	return cxfInstalled;
    }

    /**
     * Returns CXF absolute configuration file path in the project.
     * 
     * <p>
     * Creates the cxf config file using project name.
     * </p>
     * 
     * @return Path to the Cxf configuration file or null if not exists
     */
    private String getCxfConfigurationFilePath() {

	String cxfFile = "WEB-INF/cxf-".concat(getProjectName()).concat(".xml");

	// Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfFile);

	return cxfXmlPath;
    }

    /**
     * Returns project name to set CXF configuration file.
     * 
     * @return Project Name.
     */
    private String getProjectName() {
	// Project ID
	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	Assert.isTrue(projectMetadata != null, "Project metadata required");

	String projectName = projectMetadata.getProjectName();

	return projectName;
    }

    /**
     * Add the file <code>src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml</code> from
     * <code>cxf-template.xml</code> if not exists.
     */
    private void installCxfConfigurationFile() {

	String cxfXmlPath = getCxfConfigurationFilePath();

	if (fileManager.exists(cxfXmlPath)) {
	    
	    // File exists, nothing to do
	    return;
	}

	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		"cxf-template.xml");
	MutableFile cxfXmlMutableFile = fileManager.createFile(cxfXmlPath);
	
	try {
	    
	    FileCopyUtils.copy(templateInputStream, cxfXmlMutableFile
		    .getOutputStream());
	} 
	catch (Exception e) {
	    
	    throw new IllegalStateException(e);
	}

	fileManager.scan();
    }

    /**
     * Check if Cxf dependencies are set in project's pom.xml.
     * 
     * <p>
     * Search if the dependencies defined in xml Addon file
     * dependencies-export.xml are set in pom.xml.
     * </p>
     * 
     * @param type
     *            Communication type
     * @return true if all dependencies are set in pom.xml
     */
    protected boolean isCxfDependenciesInstalled(CommunicationSense type) {

	boolean cxfDependenciesExists = true;

	ProjectMetadata project = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (project == null) {
	    return false;
	}

	// Dependencies elements are defined as:
	// <dependency org="org.apache.cxf" name="cxf-rt-bindings-soap"
	// rev="2.2.6" />
	List<Element> cxfDependenciesList = getCxfRequiredDependencies(type);

	Dependency cxfDependency;

	for (Element element : cxfDependenciesList) {

	    cxfDependency = new Dependency(element);
	    cxfDependenciesExists = cxfDependenciesExists
		    && project.isDependencyRegistered(cxfDependency);
	}

	return cxfDependenciesExists;
    }
    
    /**
     * Get the file name of the Cxf required dependencies of certain type. 
     * 
     * @param type Type of required dependencies
     * @return File name
     */
    private String getCxfRequiredDependenciesFileName(CommunicationSense type) {
	
	StringBuffer name = new StringBuffer("dependencies-");
	
	switch (type) {
	    case EXPORT:
		name.append("export");
		break;
	    case IMPORT:
		name.append("import");
		break;
	}
	
	name.append(".xml");
	
	return name.toString();
    }

    /**
     * Get Addon dependencies list to install.
     * 
     * <p>
     * Get addon dependencies defined in dependencies-export.xml
     * </p>
     * 
     * @param type Communication type
     * @return List of addon dependencies as xml elements
     */
    protected List<Element> getCxfRequiredDependencies(CommunicationSense type) {

	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		getCxfRequiredDependenciesFileName(type));
	Assert.notNull(templateInputStream,
		"Can't adquire dependencies file " + type);

	Document dependencyDoc;
	try {

	    dependencyDoc = XmlUtils.getDocumentBuilder().parse(
		    templateInputStream);
	} catch (Exception e) {

	    throw new IllegalStateException(e);
	}

	Element dependencies = (Element) dependencyDoc.getFirstChild();

	return XmlUtils.findElements("/dependencies/cxf/dependency",
		dependencies);
    }

    /**
     * Add addon dependencies to project dependencies if necessary.
     * 
     * @param type Communication type
     */
    private void installCxfDependencies(CommunicationSense type) {

	// If dependencies are installed continue.
	if (isCxfDependenciesInstalled(type)) {
	    
	    return;
	}
	
	// Add project properties as cxf version
	// TODO Check cxf version property before ?
	List<Element> projectProperties = XmlUtils.findElements(
		"/configuration/gvnix/properties/*", XmlUtils.getConfiguration(
			this.getClass(), "properties.xml"));
	for (Element property : projectProperties) {
	    projectOperations.addProperty(new Property(property));
	}

	List<Element> cxfDependencies = getCxfRequiredDependencies(type);
	for (Element dependency : cxfDependencies) {
	    projectOperations.dependencyUpdate(new Dependency(dependency));
	}
    }

    /**
     * Update WEB-INF/web.xml.
     * 
     * <ul>
     * <li>Create the CXF servlet declaration and mapping</li>
     * <li>Configure ContextLoader to load cxf-PROJECT_ID.xml</li>
     * </ul>
     */
    private void installCxfWebConfigurationFile() {
	
	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/web.xml");
	Assert.isTrue(fileManager.exists(webXmlPath), "web.xml not found");

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

	if (null != XmlUtils
		.findFirstElement(
			"/web-app/servlet[servlet-class='org.apache.cxf.transport.servlet.CXFServlet']",
			root)) {
	    // cxf servlet already installed, nothing to do
	    return;
	}

	// Insert servlet def
	Element firstServletMapping = XmlUtils.findRequiredElement(
		"/web-app/servlet-mapping", root);

	Element servlet = webXml.createElement("servlet");
	Element servletName = webXml.createElement("servlet-name");

	// TODO: Create command parameter to set the servlet name
	servletName.setTextContent("CXFServlet");
	servlet.appendChild(servletName);
	Element servletClass = webXml.createElement("servlet-class");
	servletClass
		.setTextContent("org.apache.cxf.transport.servlet.CXFServlet");
	servlet.appendChild(servletClass);
	root.insertBefore(servlet, firstServletMapping.getPreviousSibling());

	// Insert servlet mapping
	Element servletMapping = webXml.createElement("servlet-mapping");
	Element servletName2 = webXml.createElement("servlet-name");
	servletName2.setTextContent("CXFServlet");
	servletMapping.appendChild(servletName2);

	// TODO: Create command parameter to set the servlet mapping
	Element urlMapping = webXml.createElement("url-pattern");
	urlMapping.setTextContent("/services/*");
	servletMapping.appendChild(urlMapping);
	root.insertBefore(servletMapping, firstServletMapping);

	// Project Name
	String prjName = getProjectName();

	String cxfFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");
	
	Element contextConfigLocation = XmlUtils
		.findFirstElement(
			"/web-app/context-param[param-name='contextConfigLocation']/param-value",
			root);
	String paramValueContent = contextConfigLocation.getTextContent();
	contextConfigLocation.setTextContent(cxfFile.concat(" ").concat(
		paramValueContent));

	XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);
    }

    /**
     * Update url rewrite rules.
     */
    private void installCxfUrlRewriteConfigurationFile() {
	List<Element> rules = getCxfUrlRewriteRequiredRules();

	// Open file and append rules before the first element
	String xmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/urlrewrite.xml");
	Assert.isTrue(fileManager.exists(xmlPath), "urlrewrite.xml not found");

	MutableFile xmlMutableFile = null;
	Document urlXml;

	try {
	    xmlMutableFile = fileManager.updateFile(xmlPath);
	    urlXml = XmlUtils.getDocumentBuilder().parse(
		    xmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = urlXml.getDocumentElement();

	for (Element rule : rules) {

	    // Create rule in dest doc
	    Element rewRule = (Element) urlXml.adoptNode(rule);

	    root.insertBefore(rewRule, root.getFirstChild());

	}

	// Define DTD
	Transformer xformer;
	try {
	    xformer = XmlUtils.createIndentingTransformer();
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, DOCTYPE_PUBLIC);
	xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DOCTYPE_SYSTEM);

	XmlUtils.writeXml(xformer, xmlMutableFile.getOutputStream(), urlXml);
    }

    /**
     * Get addon rewrite rules.
     * 
     * @return List of addon rewrite rules
     */
    private List<Element> getCxfUrlRewriteRequiredRules() {
	
	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		"urlrewrite-rules.xml");
	Assert.notNull(templateInputStream,
		"Could not adquire urlrewrite-rules.xml file");
	
	Document dependencyDoc;
	try {
	    dependencyDoc = XmlUtils.getDocumentBuilder().parse(
		    templateInputStream);
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	Element root = (Element) dependencyDoc.getFirstChild();

	return XmlUtils.findElements("/urlrewrite-rules/cxf/rule", root);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Define a Web Service class in cxf configuration file to be published.
     * <p>
     * <p>
     * Update cxf file if its necessary to avoid changes in WSDL contract.
     * </p>
     */
    public void exportClass(JavaType className,
	    AnnotationMetadata annotationMetadata) {

	Assert.isTrue(annotationMetadata != null, "Annotation '"
		+ annotationMetadata.getAnnotationType()
			.getFullyQualifiedTypeName() + "' in class '"
		+ className.getFullyQualifiedTypeName()
		+ "'must not be null to check cxf xml configuration file.");

	StringAttributeValue serviceName = (StringAttributeValue) annotationMetadata
		.getAttribute(new JavaSymbolName("serviceName"));

	Assert.isTrue(serviceName != null
		&& StringUtils.hasText(serviceName.getValue()),
		"Annotation attribute 'serviceName.getValue()' in "
			+ className.getFullyQualifiedTypeName()
			+ "' must be defined.");

	StringAttributeValue address = (StringAttributeValue) annotationMetadata
		.getAttribute(new JavaSymbolName("address"));

	Assert.isTrue(address != null
		&& StringUtils.hasText(address.getValue()),
		"Annotation attribute 'address.getValue()' in "
			+ className.getFullyQualifiedTypeName()
			+ "' must be defined.");

	StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) annotationMetadata
		.getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

	Assert.isTrue(fullyQualifiedTypeName != null
		&& StringUtils.hasText(fullyQualifiedTypeName.getValue()),
		"Annotation attribute 'fullyQualifiedTypeName.getValue()' in "
			+ className.getFullyQualifiedTypeName()
			+ "' must be defined.");

	String cxfXmlPath = getCxfConfigurationFilePath();
	Assert.isTrue(fileManager.exists(cxfXmlPath),
		"Cxf configuration file not found, export again the service.");

	MutableFile cxfXmlMutableFile = null;
	Document cxfXml;
	try {
	    cxfXmlMutableFile = fileManager.updateFile(cxfXmlPath);
	    cxfXml = XmlUtils.getDocumentBuilder().parse(
		    cxfXmlMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	Element root = cxfXml.getDocumentElement();

	// Check if service exists in configuration file.

	// 1) Check if class and id exists.
	Element classAndIdService = XmlUtils.findFirstElement("/beans/bean[@id='"
		+ serviceName.getValue().concat("Impl") + "' and @class='"
			+ fullyQualifiedTypeName.getValue() + "']", root);

	// Service is already published.
	if (classAndIdService != null) {
	    logger.log(Level.INFO, "The service '" + serviceName.getValue()
		    + "' is already set in cxf config file.");
	    return;
	}

	// 2) Check if class exists.
	Element classService = XmlUtils.findFirstElement("/beans/bean[@class='"
		+ fullyQualifiedTypeName.getValue() + "']", root);

	if (classService != null) {

	    // Update bean with new Id attribute.
	    Element updateClassService = classService;
	    updateClassService.setAttribute("id", serviceName.getValue().concat(
		    "Impl"));
	    
	    classService.getParentNode().replaceChild(updateClassService,
		    classService);
	    logger.log(Level.INFO, "The service '" + serviceName.getValue()
		    + "' has been updated its 'id' in cxf config file.");
	}

	// 3) Check if id exists.
	Element idService = XmlUtils.findFirstElement("/beans/bean[@id='"
		+ serviceName.getValue().concat("Impl") + "']", root);

	if (idService != null) {
	    
	    // Update bean with new class attribute.

	    Element updateIdService = idService;
	    updateIdService.setAttribute("class", fullyQualifiedTypeName
		    .getValue());

	    idService.getParentNode().replaceChild(updateIdService, idService);
	    logger.log(Level.INFO, "The service '" + serviceName.getValue()
		    + "' has been updated its 'class' in cxf config file.");
	}

	// Check id and class values to update.
	if (classService == null && idService == null) {

	    Element bean = cxfXml.createElement("bean");
	    bean.setAttribute("id", serviceName.getValue().concat("Impl"));
	    bean.setAttribute("class", fullyQualifiedTypeName.getValue());

	    Element endpoint = cxfXml.createElement("jaxws:endpoint");
	    endpoint.setAttribute("id", serviceName.getValue());
	    endpoint.setAttribute("implementor", "#".concat(
		    serviceName.getValue()).concat("Impl"));
	    endpoint.setAttribute("address", "/".concat(address.getValue()));

	    root.appendChild(bean);
	    root.appendChild(endpoint);
	}

	XmlUtils.writeXml(cxfXmlMutableFile.getOutputStream(), cxfXml);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Reverts the order of the package name split with dots.
     * </p>
     * 
     */
    public String convertPackageToTargetNamespace(String packageName) {

	// If there isn't package name in the class, return a blank String.
	if (!StringUtils.hasText(packageName)) {
	    return "";
	}

	String[] delimitedString = StringUtils.delimitedListToStringArray(
		packageName, ".");
	List<String> revertedList = new ArrayList<String>();

	String revertedString;

	for (int i = delimitedString.length - 1; i >= 0; i--) {
	    revertedList.add(delimitedString[i]);
	}

	revertedString = StringUtils.collectionToDelimitedString(revertedList,
		".");

	revertedString = "http://".concat(revertedString).concat("/");

	return revertedString;

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds the plugin configuration from a file.
     * </p>
     * <p>
     * Defines an execution for the serviceClass with the serviceName to
     * generate in maven compile goal.
     * </p>
     */
    public void jaxwsBuildPlugin(JavaType serviceClass, String serviceName,
	    String addressName) {

	Element pluginElement = XmlUtils.findFirstElement(
		"/jaxws-plugin/plugin", XmlUtils.getConfiguration(this
			.getClass(), "dependencies-export-jaxws-plugin.xml"));

	projectOperations.buildPluginUpdate(new Plugin(pluginElement));

	// Update plugin with execution configuration.
	String pomPath = getPomFilePath();
	Assert.isTrue(pomPath != null,
		"Cxf configuration file not found, export again the service.");

	MutableFile pomMutableFile = null;
	Document pom;
	try {
	    pomMutableFile = fileManager.updateFile(pomPath);
	    pom = XmlUtils.getDocumentBuilder().parse(
		    pomMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	Element root = pom.getDocumentElement();

	Element jaxWsPlugin = XmlUtils.findFirstElement(
			"/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-java2ws-plugin']",
		root);

	Assert
		.notNull(jaxWsPlugin,
			"Jax-Ws plugin is not defined in the pom.xml, relaunch again this command.");

	// Checks if already exists the execution.
	Element serviceExecution = XmlUtils
		.findFirstElement(
			"/project/build/plugins/plugin/executions/execution/configuration[className='"
				+ serviceClass.getFullyQualifiedTypeName()
				+ "']", root);

	if (serviceExecution != null) {
	    logger.log(Level.INFO, "Wsdl generation with CXF plugin for '"
		    + serviceName + " service, it's already configured.");
	    return;
	}

	// Execution
	serviceExecution = pom.createElement("execution");

	String gerenateServiceName = StringUtils.uncapitalize(serviceName);

	Element id = pom.createElement("id");
	id.setTextContent("generate-gvnix-service-".concat(gerenateServiceName)
		.concat("-wsdl"));

	serviceExecution.appendChild(id);
	Element phase = pom.createElement("phase");
	phase.setTextContent("compile");

	serviceExecution.appendChild(phase);

	// Configuration
	Element configuration = pom.createElement("configuration");
	Element className = pom.createElement("className");
	className.setTextContent(serviceClass.getFullyQualifiedTypeName());
	Element outputFile = pom.createElement("outputFile");
	outputFile
		.setTextContent("${project.basedir}/src/test/resources/generated/wsdl/"
			.concat(addressName).concat(".wsdl"));
	Element genWsdl = pom.createElement("genWsdl");
	genWsdl.setTextContent("true");
	Element verbose = pom.createElement("verbose");
	verbose.setTextContent("true");

	configuration.appendChild(className);
	configuration.appendChild(outputFile);
	configuration.appendChild(genWsdl);
	configuration.appendChild(verbose);

	serviceExecution.appendChild(configuration);

	// Goals
	Element goals = pom.createElement("goals");
	Element goal = pom.createElement("goal");
	goal.setTextContent("java2ws");
	goals.appendChild(goal);

	serviceExecution.appendChild(goals);

	// Checks if already exists the execution.
	Element oldExecutions = XmlUtils.findFirstElementByName("executions",
		jaxWsPlugin);

	Element newExecutions;

	// To Update execution definitions It must be replaced in pom.xml to
	// maintain the format.
	if (oldExecutions != null) {
	    newExecutions = oldExecutions;
	    newExecutions.appendChild(serviceExecution);
	    oldExecutions.getParentNode().replaceChild(oldExecutions,
		    newExecutions);
	} else {
	    newExecutions = pom.createElement("executions");
	    newExecutions.appendChild(serviceExecution);

	    jaxWsPlugin.appendChild(newExecutions);
	}


	XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds a wsdl location to the codegen plugin configuration. If code
     * generation plugin configuration not exists, it will be created.
     * </p>
     */
    public void addImportLocation(String wsdlLocation) {

	// Get plugin template
	Element pluginElement = XmlUtils.findFirstElement(
		"/codegen-plugin/plugin", XmlUtils.getConfiguration(this
			.getClass(), "dependencies-import-codegen-plugin.xml"));

	// Add plugin
	projectOperations.buildPluginUpdate(new Plugin(pluginElement));

	// Get pom.xml
	String pomPath = getPomFilePath();
	Assert.notNull(pomPath, "pom.xml configuration file not found.");

	// Get a mutable pom.xml reference to modify it
	MutableFile pomMutableFile = null;
	Document pom;
	try {
	    pomMutableFile = fileManager.updateFile(pomPath);
	    pom = XmlUtils.getDocumentBuilder().parse(
		    pomMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	Element root = pom.getDocumentElement();

	// Get plugin element
	Element codegenWsPlugin = XmlUtils.findFirstElement(
		"/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
		root);

	// If plugin element not exists, message error
	Assert
		.notNull(codegenWsPlugin,
			"Codegen plugin is not defined in the pom.xml, relaunch again this command.");

	// Access executions > execution > configuration > wsdlOptions element.
	// Configuration and wsdlOptions are created if not exists.
	Element executions = XmlUtils.findFirstElementByName("executions",
		codegenWsPlugin);
	Element execution = XmlUtils.findFirstElementByName("execution",
		executions);
	Element configuration = XmlUtils.findFirstElementByName("configuration",
		execution);
	if (configuration == null) {
	
	    configuration = pom.createElement("configuration");
	    execution.appendChild(configuration);
	}
	Element wsdlOptions = XmlUtils.findFirstElementByName("wsdlOptions",
		configuration);
	if (wsdlOptions == null) {
		
	    wsdlOptions = pom.createElement("wsdlOptions");
	    configuration.appendChild(wsdlOptions);
	}
	
	// Create new wsdl element and append it to the XML tree
	Element wsdlOption = pom.createElement("wsdlOption");
	Element wsdl = pom.createElement("wsdl");
	wsdl.setTextContent(wsdlLocation);
	wsdlOption.appendChild(wsdl);
	wsdlOptions.appendChild(wsdlOption);
	
	// Write new XML to disk
	XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
    }

    /**
     * Check if pom.xml file exists in the project and return the path.
     * 
     * <p>
     * Checks if exists pom.xml config file. If not exists, null will be
     * returned.
     * </p>
     * 
     * @return Path to the pom.xml file or null if not exists.
     */
    private String getPomFilePath() {

	// Project ID
	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	Assert.isTrue(projectMetadata != null, "Project metadata required");

	String pomFileName = "pom.xml";

	// Checks for pom.xml
	String pomPath = pathResolver.getIdentifier(Path.ROOT,
		pomFileName);

	boolean pomInstalled = fileManager.exists(pomPath);

	if (pomInstalled) {

	    return pomPath;
	} else {

	    return null;
	}
    }

}
