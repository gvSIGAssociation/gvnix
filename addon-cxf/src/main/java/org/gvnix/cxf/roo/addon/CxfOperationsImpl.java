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
package org.gvnix.cxf.roo.addon;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
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
 * Implementation of commands that are available via the Roo shell.
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com )
 * @author Enrique Ruiz ( eruiz at disid dot com )
 * @since 1.1
 */
@Component
@Service
public class CxfOperationsImpl implements CxfOperations {

    private static final Logger logger = Logger
	    .getLogger(CxfOperationsImpl.class.getName());

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

    public boolean isProjectAvailable() {
	return getPathResolver() != null;
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

    public boolean isCxfInstalled() {
	if (!isProjectAvailable()) {
	    return false;
	}

	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	String prjName = projectMetadata.getProjectName();

	String cxfFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");

	// Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfFile);

	return fileManager.exists(cxfXmlPath);
    }

    // Setup CXF ---

    public void setupCxf() {
	Assert.isTrue(isProjectAvailable(), "Project must be created");

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

	// Add CXF config files
	// addCxfConfigFiles();

	// Add dependencies to project
	updateDependencies();

	// Setup URL rewrite to avoid to filter requests to WebServices
	updateRewriteRules();
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
     * @param ifaceName
     * @param srvName
     */
    private void updateCxfXml(JavaType ifaceName, JavaType srvName) {

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
	bean.setAttribute("id", srvName.getSimpleTypeName());
	bean.setAttribute("class", srvName.getFullyQualifiedTypeName());

	Element endpoint = cxfXml.createElement("jaxws:endpoint");
	endpoint.setAttribute("id", ifaceName.getSimpleTypeName());
	endpoint.setAttribute("implementor", "#".concat(srvName
		.getSimpleTypeName()));
	endpoint.setAttribute("address", "/".concat(ifaceName
		.getSimpleTypeName()));

	root.appendChild(bean);
	root.appendChild(endpoint);

	XmlUtils.writeXml(cxfXmlMutableFile.getOutputStream(), cxfXml);
    }

    /**
     * Update WEB-INF/web.xml
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
     * Get addon dependencies
     *
     * @return List of addon dependencies
     */
    private List<Element> getCxfDependencies() {
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
     * Add addon dependencies to project dependencies
     */
    private void updateDependencies() {
	List<Element> cxfDependencies = getCxfDependencies();
	for (Element dependency : cxfDependencies) {
	    projectOperations.dependencyUpdate(new Dependency(dependency));
	}
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
     * Add addon dependencies to project dependencies
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
	XmlUtils.writeXml(xmlMutableFile.getOutputStream(), urlXml);
    }


    // Create Service ---

    public void newService(JavaType ifaceName, Path path) {

	// Service interface
	String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
		ifaceName, path);

	// WebService annotations
	List<AnnotationMetadata> ifaceAnnotations = new ArrayList<AnnotationMetadata>();
	ifaceAnnotations.add(new DefaultAnnotationMetadata(new JavaType(
		"javax.jws.WebService"),
		new ArrayList<AnnotationAttributeValue<?>>()));

	ClassOrInterfaceTypeDetails ifaceDetails = new DefaultClassOrInterfaceTypeDetails(
		declaredByMetadataId, ifaceName, Modifier.PUBLIC,
		PhysicalTypeCategory.INTERFACE, null, null, null, null, null,
		null, ifaceAnnotations, null);
	classpathOperations.generateClassFile(ifaceDetails);

	// Service Implementation class name = full qualified name + Impl
	JavaType srvName = new JavaType(ifaceName.getFullyQualifiedTypeName()
		.concat("Impl"));

	String srvDeclaredByMetadataId = PhysicalTypeIdentifier
		.createIdentifier(srvName, Path.SRC_MAIN_JAVA);

	List<JavaType> implementsTypes = new ArrayList<JavaType>();
	implementsTypes.add(ifaceName);

	// Implementation annotations
	List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
	attrs.add(new StringAttributeValue(new JavaSymbolName(
		"endpointInterface"), ifaceName.getFullyQualifiedTypeName()));
	attrs.add(new StringAttributeValue(new JavaSymbolName("serviceName"),
		ifaceName.getSimpleTypeName()));

	List<AnnotationMetadata> entityAnnotations = new ArrayList<AnnotationMetadata>();
	entityAnnotations.add(new DefaultAnnotationMetadata(new JavaType(
		"javax.jws.WebService"), attrs));

	ClassOrInterfaceTypeDetails srvDetails = new DefaultClassOrInterfaceTypeDetails(
		srvDeclaredByMetadataId, srvName, Modifier.PUBLIC,
		PhysicalTypeCategory.CLASS, null, null, null, null, null,
		implementsTypes, entityAnnotations, null);
	classpathOperations.generateClassFile(srvDetails);

	// Update CXF XML
	updateCxfXml(ifaceName, srvName);
    }

    // Add Service Operation ---

    public void addServiceOperation(JavaSymbolName opeName,
	    JavaType returnType, JavaType ifaceType) {

	// Add method to Service Interface ---

	insertAbstractMethod(opeName, returnType, ifaceType,
		new ArrayList<AnnotatedJavaType>(),
		new ArrayList<JavaSymbolName>(), null);

	// Add method to Service Implementation ---

	JavaType implType = new JavaType(ifaceType.getFullyQualifiedTypeName()
		.concat("Impl"));

	// if return type != null we must add method body (return null);
	// create some method content to get the user started
	String returnLine = "return "
		.concat(returnType == null ? ";" : "null;");
	InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
	bodyBuilder.appendFormalLine(returnLine);

	insertMethod(opeName, returnType, implType, Modifier.PUBLIC,
		new ArrayList<AnnotatedJavaType>(),
		new ArrayList<JavaSymbolName>(), bodyBuilder.getOutput());

    }

    // Add Service Operation Param ---

    // public void addOperationParam(JavaSymbolName paramName, JavaType
    // paramType,
    // JavaSymbolName opeName, JavaType ifaceType) {
    public void addServiceOperation(JavaSymbolName opeName,
	    JavaType returnType, String paramNames, String paramTypes,
	    JavaType ifaceType) {

	// TODO: paramNames and paramTypes could be a comma-list, pending to
	// tokenize the comma-list

	// Add method to Service Interface ---

	List<AnnotatedJavaType> paramTypesList = new ArrayList<AnnotatedJavaType>();
	List<JavaSymbolName> paramNamesList = new ArrayList<JavaSymbolName>();

	List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
	attrs.add(new StringAttributeValue(new JavaSymbolName("name"),
		paramNames));

	List<AnnotationMetadata> paramAnnotations = new ArrayList<AnnotationMetadata>();
	paramAnnotations.add(new DefaultAnnotationMetadata(new JavaType(
		"javax.jws.WebParam"), attrs));

	paramNamesList.add(new JavaSymbolName(paramNames));
	paramTypesList.add(new AnnotatedJavaType(new JavaType(paramTypes),
		paramAnnotations));

	insertAbstractMethod(opeName, returnType, ifaceType, paramTypesList,
		paramNamesList, null);

	// Add method to Service Implementation ---

	JavaType implType = new JavaType(ifaceType.getFullyQualifiedTypeName()
		.concat("Impl"));

	// if return type != null we must add method body (return null);
	// create some method content to get the user started
	String returnLine = "return "
		.concat(returnType == null ? ";" : "null;");
	InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
	bodyBuilder.appendFormalLine(returnLine);

	insertMethod(opeName, returnType, implType, Modifier.PUBLIC,
		paramTypesList, paramNamesList, bodyBuilder.getOutput());

    }

    // Utilities ---

    /**
     * @param opeName
     * @param returnType
     * @param targetType
     * @param paramTypes
     * @param paramNames
     * @param body
     */
    private void insertAbstractMethod(JavaSymbolName opeName,
	    JavaType returnType, JavaType targetType,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body) {
	insertMethod(opeName, returnType, targetType, Modifier.ABSTRACT,
		paramTypes, paramNames, body);

    }

    /**
     * @param methodName
     * @param returnType
     * @param targetType
     * @param modifier
     * @param paramTypes
     * @param paramNames
     * @param body
     */
    private void insertMethod(JavaSymbolName methodName, JavaType returnType,
	    JavaType targetType, int modifier,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body) {
	Assert.notNull(paramTypes, "Param type mustn't be null");
	Assert.notNull(paramNames, "Param name mustn't be null");

	// MetadataID
	String targetId = PhysicalTypeIdentifier.createIdentifier(targetType,
		Path.SRC_MAIN_JAVA);

	// Obtain the physical type and itd mutable details
	PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
		.get(targetId);
	PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
	Assert.notNull(ptd, "Java source code details unavailable for type "
		+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, ptd,
		"Java source code is immutable for type "
			+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

	// create method
	MethodMetadata operationMetadata = new DefaultMethodMetadata(targetId,
		modifier, methodName,
		(returnType == null ? JavaType.VOID_PRIMITIVE : returnType),
		paramTypes, paramNames, new ArrayList<AnnotationMetadata>(),
		new ArrayList<JavaType>(), body);
	mutableTypeDetails.addMethod(operationMetadata);
    }

}
