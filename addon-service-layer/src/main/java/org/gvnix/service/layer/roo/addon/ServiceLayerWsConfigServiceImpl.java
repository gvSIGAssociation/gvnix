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
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
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
     * Check if library is properly configurated in a project.
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
     * @param type Communication type
     * @return true or false if it's configurated
     */
    private boolean isCxfInstalled(CommunicationSense type) {
	
	// TODO Are not checked Web and Url Rewrite configuration files, check it ?
	
	boolean cxfInstalled = isCxfDependenciesInstalled(type);
	
	if (type == CommunicationSense.EXPORT) {

	    cxfInstalled = cxfInstalled && getCxfConfigurationFilePath() != null;
	}
	
	return cxfInstalled;
    }

    /**
     * Check if Cxf config file exists in the project and return the path.
     * 
     * <p>
     * Checks if exists Cxf config file using project name.
     * If not exists, null will be returned.
     * </p>
     * 
     * @return Path to the Cxf configuration file or null if not exists
     */
    private String getCxfConfigurationFilePath() {

	// Project ID
	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	Assert.isTrue(projectMetadata != null, "Project metadata required");
	String prjName = projectMetadata.getProjectName();

	String cxfFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");

	// Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfFile);

	boolean cxfInstalled = fileManager.exists(cxfXmlPath);

	if (cxfInstalled) {
	    
	    return cxfXmlPath;
	}
	else {
	    
	    return null;
	}
    }

    /**
     * Add the file <code>src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml</code> from
     * <code>cxf-template.xml</code> if not exists.
     */
    private void installCxfConfigurationFile() {

	String cxfXmlPath = getCxfConfigurationFilePath();
	if (cxfXmlPath == null) {
	    
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
     * Search if the dependencies defined in xml Addon file dependencies-export.xml are
     * set in pom.xml.
     * </p>
     * 
     * @param type Communication type
     * @return true if all dependecies are set in pom.xml
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

	String cxfFile = getCxfConfigurationFilePath();
	Assert.isNull(cxfFile,
		"Cxf configuration file not found, can't set context loader");
	
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
     */
    public void exportClass(JavaType className) {
	
	String cxfXmlPath = getCxfConfigurationFilePath();
	Assert.isNull(cxfXmlPath,
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
	Element bean = cxfXml.createElement("bean");
	bean.setAttribute("id", className.getSimpleTypeName());
	bean.setAttribute("class", className.getFullyQualifiedTypeName());

	Element endpoint = cxfXml.createElement("jaxws:endpoint");
	endpoint.setAttribute("id", className.getSimpleTypeName());
	endpoint.setAttribute("implementor", "#".concat(className
		.getSimpleTypeName()));
	endpoint.setAttribute("address", "/".concat(className
		.getSimpleTypeName()));

	root.appendChild(bean);
	root.appendChild(endpoint);

	XmlUtils.writeXml(cxfXmlMutableFile.getOutputStream(), cxfXml);
    }

}
