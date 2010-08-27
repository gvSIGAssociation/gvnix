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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.mvc.jsp.TilesOperations;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class GvNixServiceLayerOperationsImpl implements
	GvNixServiceLayerOperations {

    private static Logger logger = Logger
	    .getLogger(GvNixServiceLayerOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private TilesOperations tilesOperations;

    private ComponentContext context;

    private static final String DOCTYPE_PUBLIC = "-//tuckey.org//DTD UrlRewrite 3.0//EN";
    private static final String DOCTYPE_SYSTEM = "http://tuckey.org/res/dtds/urlrewrite3.0.dtd";
    
    protected void activate(ComponentContext context) {
	this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.GvNixServiceLayerOperations#
     * isProjectAvailable()
     */
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
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * If the class to export as web service doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     * 
     * @param serviceClass
     */
    public void exportService(JavaType serviceClass) {

	// Checks if Cxf is configured in the project and installs it if it's
	// not available.
	setUpCxf();

	String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		serviceClass.getFullyQualifiedTypeName().replace('.', '/')
			.concat(".java"));

	if (!fileManager.exists(fileLocation)) {
	    logger
		    .log(Level.INFO,
			    "Crea la nueva clase de servicio para publicarla como servicio web.");
	    // Create service class with Service Annotation.
	    createServiceClass(serviceClass);

	}

	// Define Web Service Annotations.
	updateClassAsWebService(serviceClass);

	// Update CXF XML
	updateCxfXml(serviceClass);

	// Add GvNixAnnotations to the project.
	addGvNIXAnnotationsDependecy();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds @org.springframework.stereotype.Service annotation to the class.
     * </p>
     */
    public void createServiceClass(JavaType serviceClass) {

	// Service class
	String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
		serviceClass, Path.SRC_MAIN_JAVA);

	// WebService annotations
	List<AnnotationMetadata> serviceAnnotations = new ArrayList<AnnotationMetadata>();
	serviceAnnotations.add(new DefaultAnnotationMetadata(new JavaType(
		"org.springframework.stereotype.Service"),
		new ArrayList<AnnotationAttributeValue<?>>()));

	ClassOrInterfaceTypeDetails serviceDetails = new DefaultClassOrInterfaceTypeDetails(
		declaredByMetadataId, serviceClass, Modifier.PUBLIC,
		PhysicalTypeCategory.CLASS, null, null, null, null, null,
		null, serviceAnnotations, null);

	classpathOperations.generateClassFile(serviceDetails);

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds @GvNixWebService annotation to the class.
     * </p>
     * 
     */
    public void updateClassAsWebService(JavaType serviceClass) {

	// Load class details. If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails tmpServiceDetails = classpathOperations
		.getClassOrInterface(serviceClass);

	// Checks if it's mutable
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		tmpServiceDetails, "Can't modify " + tmpServiceDetails.getName());

	MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;

	List<? extends AnnotationMetadata> serviceAnnotations = serviceDetails
		.getTypeAnnotations();

	// @Service and @GvNixWebService annotation.
	AnnotationMetadata gvNixWebServiceAnnotation = null;

	// @GvNixWebService Annotation attributes.
	List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("name"), serviceClass.getSimpleTypeName().concat("PortType")));

	// TODO: Crear namespace a la inversa del nombre del paquete de la
	// clase.
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("targetNamespace"), "http://".concat(serviceClass.getPackage().toString()).concat("/")));

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("serviceName"), serviceClass.getSimpleTypeName()));

	for (AnnotationMetadata tmpAnnotationMetadata : serviceAnnotations) {

	    if (tmpAnnotationMetadata.getAnnotationType()
		    .getFullyQualifiedTypeName().equals(
			    GvNixWebService.class.getName())) {

		serviceDetails.removeTypeAnnotation(new JavaType(
			GvNixWebService.class.getName()));
	    }

	}

	// Define GvNixWebService annotation.
	gvNixWebServiceAnnotation = new DefaultAnnotationMetadata(new JavaType(
		GvNixWebService.class.getName()), gvNixAnnotationAttributes);

	// Adds GvNIXEntityOCCChecksum to the entity
	serviceDetails.addTypeAnnotation(gvNixWebServiceAnnotation);

    }

    /**
     * {@inheritDoc}
     * 
     */
    public void updateCxfXml(JavaType className) {

	// Project ID
	String prjId = ProjectMetadata.getProjectIdentifier();

	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);

	Assert.isTrue(projectMetadata != null, "Project metadata required");

	// Project Name
	String prjName = projectMetadata.getProjectName();

	String cxfFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfFile);

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
     */
    public void setUpCxf() {

	// Check if it's already installed.
	if (isCxfInstalled()) {
	    // Nothing to do
	    return;
	}

	// Create CXF config file src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	addCxfXml();

	// Update src/main/webapp/WEB-INF/web.xml :
	// - Add CXFServlet and map it to /services/*
	// - Add cxf-PROJECT_NAME.xml to Spring Context Loader
	updateWebConfig();

	// Add dependencies to project
	updateDependencies();

	// TODO: comprobar si ya se ha actualizado el fichero urlrewrite.
	// Setup URL rewrite to avoid to filter requests to WebServices
	updateRewriteRules();

    }

    /**
     * Update url rewrite rules
     */
    private void updateRewriteRules() {
	List<Element> rules = getRewriteRules();

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
     * Get addon rewrite rules
     * 
     * @return List of addon rewrite rules
     */
    private List<Element> getRewriteRules() {
	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		"urlrewrite-rules.xml");
	Assert.notNull(templateInputStream,
		"Could not acquire urlrewrite-rules.xml file");
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
     * <p>
     * Get addon dependencies defined in dependencies.xml
     * </p>
     * 
     * @return List of addon dependencies as xml elements.
     */
    public List<Element> getCxfDependencies() {
	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		"dependencies.xml");
	Assert.notNull(templateInputStream,
		"Could not acquire dependencies.xml file");
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
     * Add addon dependencies to project dependencies if necessary
     */
    private void updateDependencies() {

	// If dependencies are installed continue.
	if (areCxfDependenciesInstalled()) {
	    return;
	}

	List<Element> cxfDependencies = getCxfDependencies();
	for (Element dependency : cxfDependencies) {
	    projectOperations.dependencyUpdate(new Dependency(dependency));
	}
    }

    /**
     * <p>
     * Update WEB-INF/web.xml
     * </p>
     * <ul>
     * <li>Create the CXF servlet declaration and mapping</li>
     * <li>Configure ContextLoader to load cxf-PROJECT_ID.xml</li>
     * </ul>
     */
    private void updateWebConfig() {
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

	// Configure ContextLoader
	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	String prjName = projectMetadata.getProjectName();
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
     * Add the file <code>src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml</code> from
     * <code>cxf-template.xml</code> if not exists.
     */
    private void addCxfXml() {

	// Project ID
	String prjId = ProjectMetadata.getProjectIdentifier();

	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	Assert.isTrue(projectMetadata != null, "Project metadata required");

	// Project Name
	String prjName = projectMetadata.getProjectName();

	String cxfDestFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfDestFile);

	// Document cxfXmlDoc;
	// MutableFile mutableFile;
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
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	fileManager.scan();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks these types:
     * </p>
     * <ul>
     * <li>
     * Cxf Dependencies in pom.xml.</li>
     * <li>
     * Cxf configuration file exists.</li>
     * </ul>
     * 
     * dependencies installed </p>
     */
    public boolean isCxfInstalled() {

	boolean cxfConfigFileExists = isCxfConfigurated();

	boolean cxfDependeciesExists = areCxfDependenciesInstalled();

	return cxfConfigFileExists && cxfDependeciesExists;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if exists Cxf config file using project name.
     * </p>
     * 
     * @return true or false if exists Cxf configuration file.
     */
    public boolean isCxfConfigurated() {

	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	String prjName = projectMetadata.getProjectName();

	String cxfFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");

	// Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfFile);

	boolean cxfInstalled = fileManager.exists(cxfXmlPath);

	return cxfInstalled;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Search if the dependencies defined in xml Addon file dependencies.xml are
     * set in pom.xml.
     * </p>
     * 
     * @return true if all dependecies are set in pom.xml.
     */
    public boolean areCxfDependenciesInstalled() {

	boolean cxfDependenciesExists = true;

	ProjectMetadata project = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (project == null) {
	    return false;
	}

	// Dependencies elements are defined as:
	// <dependency org="org.apache.cxf" name="cxf-rt-bindings-soap"
	// rev="2.2.6" />
	List<Element> cxfDependenciesList = getCxfDependencies();

	Dependency cxfDependency;

	for (Element element : cxfDependenciesList) {

	    cxfDependency = new Dependency(element);
	    cxfDependenciesExists = cxfDependenciesExists
		    && project.isDependencyRegistered(cxfDependency);
	}

	return cxfDependenciesExists;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.GvNixServiceLayerOperations#
     * addGvNIXAnnotationsDependecy()
     */
    public void addGvNIXAnnotationsDependecy() {

	List<Element> projectProperties = XmlUtils.findElements(
		"/configuration/gvnix/properties/*", XmlUtils.getConfiguration(
			this.getClass(), "properties.xml"));
	for (Element property : projectProperties) {
	    projectOperations.addProperty(new Property(property));
	}

	List<Element> databaseDependencies = XmlUtils.findElements(
		"/configuration/gvnix/dependencies/dependency", XmlUtils
			.getConfiguration(this.getClass(),
				"gvnix-annotation-dependencies.xml"));
	for (Element dependencyElement : databaseDependencies) {
	    projectOperations
		    .dependencyUpdate(new Dependency(dependencyElement));
	}
    }

}