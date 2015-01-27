/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
package org.gvnix.service.roo.addon.ws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.security.SecurityService;
import org.gvnix.service.roo.addon.util.WsdlParserUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.MavenOperations;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Utilities to manage the CXF web services library.
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WSConfigServiceImpl implements WSConfigService {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(WSConfigServiceImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private static final String POM_FILE_NOT_FOUND = "pom.xml configuration file not found.";
    private static final String GOAL2 = "goal";
    private static final String GOALS2 = "goals";
    private static final String TRUE = "true";
    private static final String CONFIGURATION2 = "configuration";
    private static final String PHASE2 = "phase";
    private static final String EXECUTION = "execution";
    private static final String EXECUTIONS = "executions";
    private static final String CLASS = "class";
    private static final String HAS_UPDATED = "' has updated 'id' attribute in cxf config file.";
    private static final String THE_SERVICE = "The service '";
    private static final String IMPL = "Impl";
    private static final String ADDRESS2 = "address";
    private static final String MUST_BE_DEFINED = "' must be defined.";
    private MetadataService metadataService;
    private FileManager fileManager;
    private ProjectOperations projectOperations;
    private SecurityService securityService;
    private AnnotationsService annotationsService;
    private MavenOperations mavenOperations;

    private static final String CXF_WSDL2JAVA_EXECUTION_ID = "generate-sources-cxf-server";

    private static final Logger logger = Logger.getLogger(WSConfigService.class
            .getName());

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * {@inheritDoc}
     */
    public boolean install(WsType type) {

        // Add repository and dependency with this addon
        getAnnotationsService().addAddonDependency();

        // Installs jax2ws plugin in project
        if (type != WsType.IMPORT_RPC_ENCODED) {
            addPlugin();
        }

        // Add library dependencies, if not exists already
        addDependencies(type);

        // Install library configuration file when export (CXF)
        if (type == WsType.EXPORT || type == WsType.EXPORT_WSDL) {
            addCxfConfig();
        }

        // Add library version properties in pom.xml, if not already
        return addProperties(type);
    }

    /**
     * Install CXF configuration file if web layer exists.
     * <p>
     * In projects without web layer, configuration file no created because is
     * referenced from web.xml and not exists.
     * </p>
     * <p>
     * One servlet will be installed in '/services' URL to view the published
     * web services summary.
     * </p>
     */
    protected void addCxfConfig() {

        String webFilePath = getWebConfigFilePath();
        if (getFileManager().exists(webFilePath)) {

            // Create CXF configuration file
            createCxfConfigurationFile();

            // Update web config file: services servlet and reference cxf config
            updateWebConfigurationFile();
        }
    }

    /**
     * Returns CXF absolute configuration file path on disk.
     * <p>
     * Creates the cxf config file using project name.
     * </p>
     * 
     * @return Absolute path to the Cxf configuration file
     */
    protected String getCxfConfigAbsoluteFilePath() {

        String relativePath = getCxfConfigRelativeFilePath();

        // Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
        return getProjectOperations().getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        relativePath);
    }

    /**
     * Returns CXF relative configuration file path in the project.
     * <p>
     * Creates the cxf config file using project name.
     * </p>
     * 
     * @return Relative path to the Cxf configuration file
     */
    protected String getCxfConfigRelativeFilePath() {

        return "WEB-INF/cxf-".concat(getProjectName()).concat(".xml");
    }

    /**
     * Returns project name to set CXF configuration file.
     * 
     * @return Project Name.
     */
    private String getProjectName() {

        // Project metadata from project identifier
        ProjectMetadata projectMetadata = (ProjectMetadata) getMetadataService()
                .get(ProjectMetadata
                        .getProjectIdentifier(getProjectOperations()
                                .getFocusedModuleName()));
        Validate.isTrue(projectMetadata != null, "Project metadata required");

        return getProjectOperations().getProjectName(
                getProjectOperations().getFocusedModuleName());
    }

    /**
     * Create CXF configuration file.
     * <p>
     * Create file src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml from
     * cxf-template.xml if not exists already.
     * </p>
     */
    protected void createCxfConfigurationFile() {

        // Configuration file already exists ? Nothing to do
        String cxfFilePath = getCxfConfigAbsoluteFilePath();
        if (getFileManager().exists(cxfFilePath)) {
            return;
        }

        // Create the configuration file from a template
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    "cxf-template.xml");
            outputStream = getFileManager().createFile(cxfFilePath)
                    .getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        }
        catch (Exception e) {
            // Error writting configuration file to disk
            throw new IllegalStateException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        // TODO What is it for ?
        getFileManager().scan();
    }

    /**
     * Check if all dependencies are registered in project (pom.xml).
     * 
     * @param type Web service type
     * @return true if all dependencies are registed already
     */
    protected boolean dependenciesRegistered(WsType type) {

        // Get project to check installed dependencies
        ProjectMetadata project = (ProjectMetadata) getMetadataService().get(
                ProjectMetadata.getProjectIdentifier(getProjectOperations()
                        .getFocusedModuleName()));
        if (project == null) {
            return false;
        }

        // Iterate all dependencies
        for (Element dependency : getDependencies(type)) {

            // Some dependency not registered: all dependencies not installed
            Pom pom = project.getPom();
            if (!pom.isDependencyRegistered(new Dependency(dependency))) {
                return false;
            }
        }

        // All dependencies are installed
        return true;
    }

    /**
     * Get the file with dependencies list for certain web service type.
     * <p>
     * Different web service type has different dependencies:
     * </p>
     * <ul>
     * <li>Export and export from wsdl</li>
     * <li>Import</li>
     * <li>Import RPC encoded</li>
     * </ul>
     * <p>
     * Files are stored at src/main/resources in same package as this class
     * (required).
     * </p>
     * 
     * @param type Web service type
     * @return Dependency definition file name
     */
    protected String getDependenciesFileName(WsType type) {

        StringBuffer name = new StringBuffer("dependencies-");

        switch (type) {
        case EXPORT:
        case EXPORT_WSDL:
            name.append("export");
            break;
        case IMPORT:
            name.append("import");
            break;
        case IMPORT_RPC_ENCODED:
            name.append("import-axis");
            break;
        }

        name.append(".xml");

        return name.toString();
    }

    /**
     * Get the dependencies list for certain web service type.
     * 
     * @param type Web service type
     * @return List of dependencies as xml elements
     */
    protected List<Element> getDependencies(WsType type) {

        // Get the file with dependencies list
        InputStream dependencies = FileUtils.getInputStream(getClass(),
                getDependenciesFileName(type));
        Validate.notNull(dependencies, "Can't adquire dependencies file "
                + type);

        // Find dependencies element list into file
        return XmlUtils.findElements("/dependencies/dependency",
                (Element) getInputDocument(dependencies).getFirstChild());
    }

    /**
     * Add dependencies to project (pom.xml) if not already.
     * <p>
     * If some dependencies are not installed, will be installed.
     * </p>
     * 
     * @param type Web service type
     */
    private void addDependencies(WsType type) {

        // If all dependencies are already installed: nothing to do
        if (dependenciesRegistered(type)) {
            return;
        }

        // Get all dependencies and add them to project (pom.xml)
        for (Element dependency : getDependencies(type)) {
            getProjectOperations().addDependency(
                    getProjectOperations().getFocusedModuleName(),
                    new Dependency(dependency));
        }
    }

    /**
     * Add or update library version properties into project (pom.xml).
     * <p>
     * If newer version property, version will be updated.
     * </p>
     * <p>
     * Different web service type has different properties:
     * </p>
     * <ul>
     * <li>Import, export and export from wsdl: CXF version property</li>
     * <li>Import RPC encoded: Axis version property</li>
     * </ul>
     * 
     * @param type Web service type
     */
    protected boolean addProperties(WsType type) {

        // Add project properties, as versions
        List<Element> properties = new ArrayList<Element>();

        switch (type) {

        // Import, export and export from wsdl same properties (CXF version)
        case IMPORT:
        case EXPORT:
        case EXPORT_WSDL:

            properties = XmlUtils.findElements(
                    "/configuration/gvnix/properties/*",
                    XmlUtils.getRootElement(this.getClass(), "properties.xml"));
            break;

        // Import RPC encoded properties (Axis version)
        case IMPORT_RPC_ENCODED:

            properties = XmlUtils.findElements(
                    "/configuration/gvnix/properties/*", XmlUtils
                            .getRootElement(this.getClass(),
                                    "properties-axis.xml"));
            break;
        }

        // Add property if not exists or update if exists and newer
        return DependenciesVersionManager.managePropertyVersion(
                getMetadataService(), getProjectOperations(), properties);
    }

    /**
     * Update web configuration file (web.xml) with CXF configuration.
     * <ul>
     * <li>Add the CXF servlet declaration and mapping with '/services/*' URL to
     * access published web services. All added before forst servlet mapping</li>
     * <li>Configure Spring context to load cxf configuration file</li>
     * </ul>
     * <p>
     * If already installed cxf declaration, nothing to do.
     * </p>
     */
    protected void updateWebConfigurationFile() {

        // Get web configuration file document and root XML representation
        MutableFile file = getFileManager().updateFile(getWebConfigFilePath());
        Document web = getInputDocument(file.getInputStream());
        Element root = web.getDocumentElement();

        // If CXF servlet already installed: nothing to do
        if (XmlUtils
                .findFirstElement(
                        "/web-app/servlet[servlet-class='org.apache.cxf.transport.servlet.CXFServlet']",
                        root) != null) {
            return;
        }

        // Get first servlet mapping declaration
        Element firstMapping = XmlUtils.findRequiredElement(
                "/web-app/servlet-mapping", root);

        // Add CXF servlet definition before first mapping
        root.insertBefore(getServletDefinition(web),
                firstMapping.getPreviousSibling());

        // Add CXF servlet mapping before first mapping
        root.insertBefore(getServletMapping(web), firstMapping);

        // Add CXF configuration file path to Spring context
        Element context = XmlUtils
                .findFirstElement(
                        "/web-app/context-param[param-name='contextConfigLocation']/param-value",
                        root);
        context.setTextContent(getCxfConfigRelativeFilePath().concat(" ")
                .concat(context.getTextContent()));

        // Write modified web.xml to disk
        XmlUtils.writeXml(file.getOutputStream(), web);
    }

    /**
     * Get CXF servlet definition element.
     * 
     * @param web Document representation of web.xml
     * @return Element representation of CXF servlet definition
     */
    private Element getServletDefinition(Document web) {

        // Create servlet element
        Element servlet = web.createElement("servlet");

        // Create servlet name and add it to servlet
        Element name = web.createElement("servlet-name");
        name.setTextContent("CXFServlet");
        servlet.appendChild(name);

        // Create servlet class and add it to servlet
        Element clas = web.createElement("servlet-class");
        clas.setTextContent("org.apache.cxf.transport.servlet.CXFServlet");
        servlet.appendChild(clas);

        return servlet;
    }

    /**
     * Get CXF servlet mapping element.
     * 
     * @param web Document representation of web.xml
     * @return Element representation of CXF servlet mapping
     */
    private Element getServletMapping(Document web) {

        // Create servlet
        Element mapping = web.createElement("servlet-mapping");

        // Create servlet name and add it to servlet
        Element name = web.createElement("servlet-name");
        name.setTextContent("CXFServlet");
        mapping.appendChild(name);

        // Create servlet url pattern and add it to servlet
        Element pattern = web.createElement("url-pattern");
        pattern.setTextContent("/services/*");
        mapping.appendChild(pattern);

        return mapping;
    }

    /**
     * Get the XML document representation of a input stream.
     * <p>
     * IllegalStateException if error parsing input stream.
     * </p>
     * 
     * @param input Input stream to parse
     * @return XML document representation of input stream
     */
    protected Document getInputDocument(InputStream input) {

        try {

            return XmlUtils.getDocumentBuilder().parse(input);

        }
        catch (Exception e) {

            throw new IllegalStateException(e);
        }
    }

    /**
     * Get web configuration (web.xml) absolute file path.
     * 
     * @return web configuration (web.xml) absolute file path
     */
    protected String getWebConfigFilePath() {

        return getProjectOperations().getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/web.xml");
    }

    /**
     * {@inheritDoc}
     */
    public boolean publishClassAsWebService(JavaType className,
            AnnotationMetadata annotationMetadata) {

        Validate.isTrue(annotationMetadata != null, "Annotation '"
                + annotationMetadata.getAnnotationType()
                        .getFullyQualifiedTypeName() + "' in class '"
                + className.getFullyQualifiedTypeName()
                + "'must not be null to check cxf xml configuration file.");

        // Update web service configuration file.
        boolean updtGvNIXWServAnn = updateConfiguration(className,
                annotationMetadata);

        return updtGvNIXWServAnn;
    }

    /**
     * Updates web services configuration file.
     * 
     * @param className to export.
     * @param annotationMetadata values from web service class to set in
     *        configuration file.
     * @return true if annotation from className has to be updated because of
     *         changes in package or class name.
     */
    private boolean updateConfiguration(JavaType className,
            AnnotationMetadata annotationMetadata) {

        StringAttributeValue serviceName = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("serviceName"));

        Validate.isTrue(
                serviceName != null
                        && StringUtils.isNotBlank(serviceName.getValue()),
                "Annotation attribute 'serviceName' in "
                        + className.getFullyQualifiedTypeName()
                        + MUST_BE_DEFINED);

        StringAttributeValue address = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName(ADDRESS2));

        Validate.isTrue(
                address != null && StringUtils.isNotBlank(address.getValue()),
                "Annotation attribute 'address' in "
                        + className.getFullyQualifiedTypeName()
                        + MUST_BE_DEFINED);

        StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

        Validate.notNull(
                fullyQualifiedTypeName,
                "Annotation attribute 'fullyQualifiedTypeName' in "
                        + className.getFullyQualifiedTypeName()
                        + MUST_BE_DEFINED);
        Validate.isTrue(
                fullyQualifiedTypeName != null
                        && StringUtils.isNotBlank(fullyQualifiedTypeName
                                .getValue()),
                "Annotation attribute 'fullyQualifiedTypeName' in "
                        + className.getFullyQualifiedTypeName()
                        + MUST_BE_DEFINED);

        BooleanAttributeValue exported = (BooleanAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("exported"));

        Validate.isTrue(exported != null, "Annotation attribute 'exported' in "
                + className.getFullyQualifiedTypeName() + MUST_BE_DEFINED);

        String cxfXmlPath = getCxfConfigAbsoluteFilePath();

        boolean updateFullyQualifiedTypeName = false;
        // Check if class name and annotation class name are different.
        if (fullyQualifiedTypeName != null
                && !className.getFullyQualifiedTypeName().contentEquals(
                        fullyQualifiedTypeName.getValue())) {
            updateFullyQualifiedTypeName = true;
        }

        if (getFileManager().exists(cxfXmlPath)) {

            MutableFile cxfXmlMutableFile = getFileManager().updateFile(
                    cxfXmlPath);
            Document cxfXml = getInputDocument(cxfXmlMutableFile
                    .getInputStream());

            Element root = cxfXml.getDocumentElement();

            // Check if service exists in configuration file.
            boolean updateService = true;

            // 1) Check if class and id exists in bean.
            Element classAndIdService = XmlUtils.findFirstElement(
                    "/beans/bean[@id='" + serviceName.getValue().concat(IMPL)
                            + "' and @class='"
                            + className.getFullyQualifiedTypeName() + "']",
                    root);

            // Service is already published.
            if (classAndIdService != null) {
                logger.log(Level.FINE, THE_SERVICE + serviceName.getValue()
                        + "' is already set in cxf config file.");
                updateService = false;
            }

            if (updateService) {

                // 2) Check if class exists or it hasn't changed.
                Element classService = null;

                if (updateFullyQualifiedTypeName) {

                    // Check if exists with class name.
                    classService = XmlUtils.findFirstElement(
                            "/beans/bean[@class='"
                                    + className.getFullyQualifiedTypeName()
                                    + "']", root);

                    if (classService != null) {

                        // Update bean with new Id attribute.
                        Element updateClassService = classService;
                        String idValue = classService.getAttribute("id");

                        if (!StringUtils.isNotBlank(idValue)
                                || !idValue.contentEquals(serviceName
                                        .getValue().concat(IMPL))) {
                            updateClassService.setAttribute("id", serviceName
                                    .getValue().concat(IMPL));

                            classService.getParentNode().replaceChild(
                                    updateClassService, classService);
                            logger.log(Level.INFO,
                                    THE_SERVICE + serviceName.getValue()
                                            + HAS_UPDATED);
                        }

                    }
                    else {

                        // Check if exists with fullyQualifiedTypeName.
                        classService = XmlUtils.findFirstElement(
                                "/beans/bean[@class='"
                                        + fullyQualifiedTypeName.getValue()
                                        + "']", root);

                        if (classService != null) {

                            Element updateClassService = classService;
                            String idValue = classService.getAttribute("id");

                            updateClassService.setAttribute(CLASS,
                                    className.getFullyQualifiedTypeName());

                            if (!StringUtils.isNotBlank(idValue)
                                    || !idValue.contentEquals(serviceName
                                            .getValue().concat(IMPL))) {
                                updateClassService.setAttribute("id",
                                        serviceName.getValue().concat(IMPL));

                                logger.log(Level.INFO, THE_SERVICE
                                        + serviceName.getValue() + HAS_UPDATED);
                            }

                            classService.getParentNode().replaceChild(
                                    updateClassService, classService);
                            logger.log(
                                    Level.INFO,
                                    THE_SERVICE
                                            + serviceName.getValue()
                                            + "' has updated 'class' attribute in cxf config file.");
                        }

                    }
                }
                else {

                    // Check if exists with class name.
                    classService = XmlUtils.findFirstElement(
                            "/beans/bean[@class='"
                                    + className.getFullyQualifiedTypeName()
                                    + "']", root);

                    if (classService != null) {

                        // Update bean with new Id attribute.
                        Element updateClassService = classService;
                        String idValue = classService.getAttribute("id");

                        if (!StringUtils.isNotBlank(idValue)
                                || !idValue.contentEquals(serviceName
                                        .getValue().concat(IMPL))) {
                            updateClassService.setAttribute("id", serviceName
                                    .getValue().concat(IMPL));

                            classService.getParentNode().replaceChild(
                                    updateClassService, classService);
                            logger.log(Level.INFO,
                                    THE_SERVICE + serviceName.getValue()
                                            + HAS_UPDATED);
                        }
                    }
                }

                // 3) Check if id exists.
                Element idService = XmlUtils.findFirstElement(
                        "/beans/bean[@id='"
                                + serviceName.getValue().concat(IMPL) + "']",
                        root);

                if (idService != null) {

                    // Update bean with new class attribute.
                    Element updateIdService = idService;
                    String classNameAttribute = idService.getAttribute(CLASS);

                    if (!StringUtils.isNotBlank(classNameAttribute)
                            || !classNameAttribute.contentEquals(className
                                    .getFullyQualifiedTypeName())) {
                        updateIdService.setAttribute(CLASS,
                                className.getFullyQualifiedTypeName());
                        idService.getParentNode().replaceChild(updateIdService,
                                idService);
                        logger.log(
                                Level.INFO,
                                THE_SERVICE
                                        + serviceName.getValue()
                                        + "' has updated 'class' attribute in cxf config file.");
                    }

                }

                Element bean;
                // Check id and class values to create a new bean.
                if (classService == null && idService == null) {

                    bean = cxfXml.createElement("bean");
                    bean.setAttribute("id", serviceName.getValue().concat(IMPL));
                    bean.setAttribute(CLASS,
                            className.getFullyQualifiedTypeName());

                    root.appendChild(bean);
                }
            }

            boolean updateEndpoint = true;

            // Check if endpoint exists in the configuration file.
            Element jaxwsBean = XmlUtils.findFirstElement(
                    "/beans/endpoint[@address='/" + address.getValue()
                            + "' and @id='" + serviceName.getValue() + "']",
                    root);

            // 1) Check if exists with id and address.
            if (jaxwsBean != null) {

                logger.log(Level.FINE,
                        "The endpoint '" + serviceName.getValue()
                                + "' is already set in cxf config file.");
                updateEndpoint = false;
            }

            if (updateEndpoint) {

                // 2) Check if exists a bean with annotation address value and
                // updates id attribute with annotation serviceName value.
                Element addressEndpoint = XmlUtils.findFirstElement(
                        "/beans/endpoint[@address='/" + address.getValue()
                                + "']", root);

                if (addressEndpoint != null) {

                    // Update bean with new Id attribute.
                    Element updateAddressEndpoint = addressEndpoint;
                    String idAttribute = addressEndpoint.getAttribute("id");

                    if (!StringUtils.isNotBlank(idAttribute)
                            || !idAttribute.contentEquals(serviceName
                                    .getValue())) {

                        updateAddressEndpoint.setAttribute("id",
                                serviceName.getValue());
                        updateAddressEndpoint.setAttribute("implementor", "#"
                                .concat(serviceName.getValue()).concat(IMPL));

                        addressEndpoint.getParentNode().replaceChild(
                                updateAddressEndpoint, addressEndpoint);
                        logger.log(Level.INFO, "The endpoint bean '"
                                + serviceName.getValue() + HAS_UPDATED);

                    }

                }

                Element idEndpoint = XmlUtils
                        .findFirstElement(
                                "/beans/endpoint[@id='"
                                        + serviceName.getValue() + "']", root);

                // 3) Check if exists a bean with annotation address value in id
                // attribute and updates address attribute with annotation
                // address
                // value.
                if (idEndpoint != null) {

                    // Update bean with new Id attribute.
                    Element updateIdEndpoint = idEndpoint;

                    String addressAttribute = idEndpoint.getAttribute(ADDRESS2);

                    if (!StringUtils.isNotBlank(addressAttribute)
                            || !addressAttribute.contentEquals("/"
                                    .concat(address.getValue()))) {

                        updateIdEndpoint.setAttribute(ADDRESS2,
                                "/".concat(address.getValue()));

                        idEndpoint.getParentNode().replaceChild(
                                updateIdEndpoint, idEndpoint);
                        logger.log(
                                Level.INFO,
                                "The endpoint bean '"
                                        + serviceName.getValue()
                                        + "' has updated 'address' attribute in cxf config file.");

                    }

                }

                Element endpoint;
                // Check values to create new endpoint bean.
                if (addressEndpoint == null && idEndpoint == null) {

                    endpoint = cxfXml.createElement("jaxws:endpoint");
                    endpoint.setAttribute("id", serviceName.getValue());
                    endpoint.setAttribute("implementor",
                            "#".concat(serviceName.getValue()).concat(IMPL));
                    endpoint.setAttribute(ADDRESS2,
                            "/".concat(address.getValue()));
                    root.appendChild(endpoint);
                }

            }

            // Update configuration file.
            if (updateService || updateEndpoint) {
                XmlUtils.writeXml(cxfXmlMutableFile.getOutputStream(), cxfXml);
            }

        }

        return updateFullyQualifiedTypeName;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reverts the order of the package name split with dots.
     * </p>
     */
    public String convertPackageToTargetNamespace(String packageName) {

        // If there isn't package name in the class, return a blank String.
        if (!StringUtils.isNotBlank(packageName)) {
            return "";
        }

        String[] delimitedString = StringUtils.split(packageName, ".");
        List<String> revertedList = new ArrayList<String>();

        String revertedString;

        for (int i = delimitedString.length - 1; i >= 0; i--) {
            revertedList.add(delimitedString[i]);
        }

        revertedString = collectionToDelimitedString(revertedList, ".", "", "");

        revertedString = "http://".concat(revertedString).concat("/");

        return revertedString;

    }

    /**
     * {@inheritDoc}
     */
    public void addToJava2wsPlugin(JavaType serviceClass, String serviceName,
            String addressName, String fullyQualifiedTypeName) {

        // Get pom
        String pomPath = getPomFilePath();
        Validate.isTrue(pomPath != null,
                "Cxf configuration file not found, export again the service.");
        MutableFile pomMutableFile = getFileManager().updateFile(pomPath);
        Document pom = getInputDocument(pomMutableFile.getInputStream());
        Element root = pom.getDocumentElement();

        // Gets java2ws plugin element
        Element jaxWsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-java2ws-plugin']",
                        root);

        // Install it if it's missing
        if (jaxWsPlugin == null) {

            logger.log(Level.INFO,
                    "Jax-Ws plugin is not defined in the pom.xml. Installing in project.");
            // Installs jax2ws plugin.
            addPlugin();
        }

        // Checks if already exists the execution.
        Element serviceExecution = XmlUtils.findFirstElement(
                "/project/build/plugins/plugin/executions/execution/configuration[className='"
                        .concat(serviceClass.getFullyQualifiedTypeName())
                        .concat("']"), root);

        if (serviceExecution != null) {
            logger.log(
                    Level.FINE,
                    "A previous Wsdl generation with CXF plugin for '".concat(
                            serviceName).concat("' service has been found."));
            return;
        }

        // Checks if name of java class has been changed comparing current
        // service class and name declared in annotation
        boolean classNameChanged = false;
        if (!serviceClass.getFullyQualifiedTypeName().contentEquals(
                fullyQualifiedTypeName)) {
            classNameChanged = true;
        }

        // if class has been changed (package or name) update execution
        if (classNameChanged) {
            serviceExecution = XmlUtils.findFirstElement(
                    "/project/build/plugins/plugin/executions/execution/configuration[className='"
                            .concat(fullyQualifiedTypeName).concat("']"), root);

            // Update with serviceClass.getFullyQualifiedTypeName().
            if (serviceExecution != null && serviceExecution.hasChildNodes()) {

                Node updateServiceExecution;
                updateServiceExecution = (serviceExecution.getFirstChild() != null) ? serviceExecution
                        .getFirstChild().getNextSibling() : null;

                // Find node which contains old class name
                while (updateServiceExecution != null) {

                    if (updateServiceExecution.getNodeName().contentEquals(
                            "className")) {
                        // Update node content with new value
                        updateServiceExecution.setTextContent(serviceClass
                                .getFullyQualifiedTypeName());

                        // write pom
                        XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
                        logger.log(
                                Level.INFO,
                                "Wsdl generation with CXF plugin for '"
                                        + serviceName
                                        + " service, updated className attribute for '"
                                        + serviceClass
                                                .getFullyQualifiedTypeName()
                                        + "'.");
                        // That's all
                        return;
                    }

                    // Check next node.
                    updateServiceExecution = updateServiceExecution
                            .getNextSibling();

                }
            }
        }

        // Prepare Execution configuration
        String executionID = "${project.basedir}/src/test/resources/generated/wsdl/"
                .concat(addressName).concat(".wsdl");
        serviceExecution = createJava2wsExecutionElement(pom, serviceClass,
                addressName, executionID);

        // Checks if already exists the execution.

        // XXX ??? this is hard difficult because previously it's already
        // checked
        // using class name
        Element oldExecution = XmlUtils.findFirstElement(
                "/project/build/plugins/plugin/executions/execution[id='"
                        + executionID + "']", root);

        if (oldExecution != null) {
            logger.log(Level.FINE, "Wsdl generation with CXF plugin for '"
                    + serviceName + " service, has been configured before.");
            return;
        }

        // Checks if already exists the executions to update or create.
        Element oldExecutions = DomUtils.findFirstElementByName(EXECUTIONS,
                jaxWsPlugin);

        Element newExecutions;

        // To Update execution definitions It must be replaced in pom.xml to
        // maintain the format.
        if (oldExecutions != null) {
            newExecutions = oldExecutions;
            newExecutions.appendChild(serviceExecution);
            oldExecutions.getParentNode().replaceChild(oldExecutions,
                    newExecutions);
        }
        else {
            newExecutions = pom.createElement(EXECUTIONS);
            newExecutions.appendChild(serviceExecution);

            jaxWsPlugin.appendChild(newExecutions);
        }

        XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
    }

    /**
     * Generates Element for service wsdl generation execution
     * 
     * @param pom Pom document
     * @param serviceClass to generate
     * @param addressName of the service
     * @param executionID execution identifier
     * @return
     */
    private Element createJava2wsExecutionElement(Document pom,
            JavaType serviceClass, String addressName, String executionID) {

        Element serviceExecution = pom.createElement(EXECUTION);

        // execution.id
        Element id = pom.createElement("id");
        id.setTextContent(executionID);

        // execution.phase
        serviceExecution.appendChild(id);
        Element phase = pom.createElement(PHASE2);
        phase.setTextContent("test");
        serviceExecution.appendChild(phase);

        // Execution.Configuration
        Element configuration = pom.createElement(CONFIGURATION2);

        // Execution.configuration.className
        Element className = pom.createElement("className");
        className.setTextContent(serviceClass.getFullyQualifiedTypeName());

        // Excecution.configuration.outputFile
        Element outputFile = pom.createElement("outputFile");
        outputFile
                .setTextContent("${project.basedir}/src/test/resources/generated/wsdl/"
                        .concat(addressName).concat(".wsdl"));

        // execution.configuration.genWsdl
        Element genWsdl = pom.createElement("genWsdl");
        genWsdl.setTextContent(TRUE);

        // execution.configuration.verbose
        Element verbose = pom.createElement("verbose");
        verbose.setTextContent(TRUE);

        // execution.configuration
        configuration.appendChild(className);
        configuration.appendChild(outputFile);
        configuration.appendChild(genWsdl);
        configuration.appendChild(verbose);

        // execution
        serviceExecution.appendChild(configuration);

        // Goals
        Element goals = pom.createElement(GOALS2);
        Element goal = pom.createElement(GOAL2);
        goal.setTextContent("java2ws");
        goals.appendChild(goal);

        serviceExecution.appendChild(goals);
        return serviceExecution;
    }

    /**
     * Installs Java2ws plugin into the pom.xml from a template.
     */
    protected void addPlugin() {

        // Get the plugin from the template and write into de project (pom.xml)
        getProjectOperations().updateBuildPlugin(
                getProjectOperations().getFocusedModuleName(),
                new Plugin(XmlUtils.findFirstElement("/jaxws-plugin/plugin",
                        XmlUtils.getRootElement(this.getClass(),
                                "dependencies-export-jaxws-plugin.xml"))));

        // What is this for ?
        getFileManager().commit();
    }

    /**
     * Install wsdl2java maven plugin in pom.xml
     */
    public void installWsdl2javaPlugin() {

        // Add plugin and write this modifications to disk
        getProjectOperations().updateBuildPlugin(
                getProjectOperations().getFocusedModuleName(),
                new Plugin(XmlUtils.findFirstElement(
                        "/cxf-codegen/cxf-codegen-plugin/plugin",
                        XmlUtils.getRootElement(this.getClass(),
                                "dependencies-export-wsdl2java-plugin.xml"))));
        getFileManager().commit();
    }

    /**
     * {@inheritDoc}
     */
    public boolean addImportLocation(String wsdlLocation, WsType type) {

        // Adds Project properties to pom.xml
        // addProjectProperties(type);

        // Identifies the type of library to use
        if (WsType.IMPORT_RPC_ENCODED.equals(type)) {
            return addImportLocationRpc(wsdlLocation);
        }
        else {
            return addImportLocationDocument(wsdlLocation);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean addWsdlLocation(String wsdlLocation, Document wsdlDocument) {

        // Project properties to pom.xml
        boolean propertiesUpdated = addProperties(WsType.EXPORT_WSDL);

        // Export Wsdl2Java
        addExportWSDLLocationDocument(wsdlLocation, wsdlDocument);

        return propertiesUpdated;
    }

    /**
     * <p>
     * Adds a wsdl location to the codegen plugin configuration. If code
     * generation plugin configuration not exists, it will be created.
     * </p>
     * 
     * @param wsdlLocation WSDL file location.
     * @param wsdlDocument WSDL file.
     */
    private void addExportWSDLLocationDocument(String wsdlLocation,
            Document wsdlDocument) {

        // install Plugin
        installWsdl2javaPlugin();

        // Get pom.xml
        String pomPath = getPomFilePath();
        Validate.notNull(pomPath, POM_FILE_NOT_FOUND);

        // Get a mutable pom.xml reference to modify it
        MutableFile pomMutableFile = getFileManager().updateFile(pomPath);
        Document pom = getInputDocument(pomMutableFile.getInputStream());
        Element root = pom.getDocumentElement();

        // Get plugin element
        Element codegenWsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
                        root);

        // If plugin element not exists, error message
        Validate.notNull(codegenWsPlugin,
                "Codegen plugin is not defined in the pom.xml, relaunch again this command.");

        // Checks if already exists the execution.
        Element oldGenerateSourcesCxfServer = XmlUtils.findFirstElement(
                "/project/build/plugins/plugin/executions/execution[id='"
                        + CXF_WSDL2JAVA_EXECUTION_ID + "']", root);

        // Generate execution configuration element.
        Element newGenerateSourcesCxfServer = createWsdl2JavaExecutionElement(
                pom, wsdlDocument, wsdlLocation);

        // Checks if exists executions.
        Element oldExecutions = DomUtils.findFirstElementByName(EXECUTIONS,
                codegenWsPlugin);

        Element newExecutions;

        // To Update execution definitions It must be replaced in pom.xml to
        // maintain the format.
        if (oldGenerateSourcesCxfServer != null) {
            oldGenerateSourcesCxfServer.getParentNode().replaceChild(
                    newGenerateSourcesCxfServer, oldGenerateSourcesCxfServer);
        }
        else {

            if (oldExecutions == null) {
                newExecutions = pom.createElement(EXECUTIONS);
                newExecutions.appendChild(newGenerateSourcesCxfServer);

                codegenWsPlugin.appendChild(newExecutions);
            }
            else {
                newExecutions = oldExecutions;
                newExecutions.appendChild(newGenerateSourcesCxfServer);
                oldExecutions.getParentNode().replaceChild(newExecutions,
                        oldExecutions);
            }
        }

        // Write new XML to disk
        XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);

    }

    /**
     * Creates execution xml pom element for wsdl2java generation
     * 
     * @param pom
     * @param wsdlDocument to generate sources
     * @param wsdlLocation current wsdl location
     * @return
     */
    private Element createWsdl2JavaExecutionElement(Document pom,
            Document wsdlDocument, String wsdlLocation) {
        Element newGenerateSourcesCxfServer = pom.createElement(EXECUTION);

        // Create name for id.
        // execution.id
        Element id = pom.createElement("id");
        id.setTextContent(CXF_WSDL2JAVA_EXECUTION_ID);
        newGenerateSourcesCxfServer.appendChild(id);

        // execution.phase
        Element phase = pom.createElement(PHASE2);
        phase.setTextContent("generate-sources");
        newGenerateSourcesCxfServer.appendChild(phase);

        // execution.goals.goal
        Element goals = pom.createElement(GOALS2);
        Element goal = pom.createElement(GOAL2);
        goal.setTextContent("wsdl2java");
        goals.appendChild(goal);
        newGenerateSourcesCxfServer.appendChild(goals);

        // execution.configuration
        Element configuration = pom.createElement(CONFIGURATION2);
        newGenerateSourcesCxfServer.appendChild(configuration);

        // execution.configuration.sourceRoo
        Element sourceRoot = pom.createElement("sourceRoot");
        sourceRoot
                .setTextContent("${basedir}/target/generated-sources/cxf/server");
        configuration.appendChild(sourceRoot);

        // execution.configuration.defaultOption
        appendDefaultOptions(pom, configuration);

        // execution.configuration.wsdlOptions
        Element wsdlOptions = pom.createElement("wsdlOptions");
        configuration.appendChild(wsdlOptions);

        // execution.configuration.wsdlOptions.wsdlOption
        Element wsdlOption = pom.createElement("wsdlOption");
        wsdlOptions.appendChild(wsdlOption);

        // Check URI correct format
        wsdlLocation = removeFilePrefix(wsdlLocation);

        // execution.configuration.wsdlOptions.wsdlOption.wsdl
        Element wsdl = pom.createElement("wsdl");
        wsdlOption.appendChild(wsdl);
        wsdl.setTextContent(wsdlLocation);

        // execution.configuration.wsdlOptions.wsdlOption.extraargs.extraarg <--
        // "-impl"
        Element extraArgs = pom.createElement("extraargs");
        Element extraArg = pom.createElement("extraarg");
        extraArg.setTextContent("-impl");
        extraArgs.appendChild(extraArg);
        wsdlOption.appendChild(extraArgs);

        Element rootElement = wsdlDocument.getDocumentElement();

        // Configure the packagename to generate client sources
        // execution.configuration.wsdlOptions.wsdlOption.packagenames.packagename
        Element packagenames = pom.createElement("packagenames");
        Element packagename = pom.createElement("packagename");
        String packageName = WsdlParserUtils
                .getTargetNamespaceRelatedPackage(rootElement);

        packageName = packageName.toLowerCase();
        packagename.setTextContent(packageName.substring(0,
                packageName.length() - 1));
        packagenames.appendChild(packagename);
        wsdlOption.appendChild(packagenames);

        return newGenerateSourcesCxfServer;
    }

    /**
     * Add default options section to the configuration.
     * 
     * @param pom
     * @param configuration
     */
    protected void appendDefaultOptions(Document pom, Element configuration) {

        Element defaultOptions;
        defaultOptions = pom.createElement("defaultOptions");
        configuration.appendChild(defaultOptions);

        // Soap Headers.
        // execution.configuration.defaultOption.extendedSoapHeaders <-- true
        Element extendedSoapHeaders = pom.createElement("extendedSoapHeaders");
        extendedSoapHeaders.setTextContent(TRUE);
        defaultOptions.appendChild(extendedSoapHeaders);

        // AutoNameResolution to solve naming conflicts.
        // execution.configuration.defaultOption.autoNameResolution <-- true
        Element autoNameResolution = pom.createElement("autoNameResolution");
        autoNameResolution.setTextContent(TRUE);
        defaultOptions.appendChild(autoNameResolution);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Search the execution element using id defined in
     * CXF_WSDL2JAVA_EXECUTION_ID field.
     * </p>
     */
    public void disableWsdlLocation() {

        // Get pom.xml
        String pomPath = getPomFilePath();
        Validate.notNull(pomPath, POM_FILE_NOT_FOUND);

        // Get a mutable pom.xml reference to modify it
        MutableFile pomMutableFile = null;
        Document pom;
        try {
            pomMutableFile = getFileManager().updateFile(pomPath);
            pom = XmlUtils.getDocumentBuilder().parse(
                    pomMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element root = pom.getDocumentElement();

        // Get plugin element
        Element codegenWsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
                        root);

        // If plugin element not exists, message error
        Validate.notNull(codegenWsPlugin,
                "Codegen plugin is not defined in the pom.xml, relaunch again this command.");

        // Checks if already exists the execution.
        Element oldGenerateSourcesCxfServer = XmlUtils.findFirstElement(
                "/project/build/plugins/plugin/executions/execution[id='"
                        + CXF_WSDL2JAVA_EXECUTION_ID + "']", root);

        if (oldGenerateSourcesCxfServer != null) {

            Element executionPhase = DomUtils.findFirstElementByName(PHASE2,
                    oldGenerateSourcesCxfServer);

            if (executionPhase != null) {

                Element newPhase = pom.createElement(PHASE2);
                newPhase.setTextContent("none");

                // Remove existing wsdlOption.
                executionPhase.getParentNode().replaceChild(newPhase,
                        executionPhase);

                // Write new XML to disk.
                XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
            }

        }

    }

    /**
     * Remove the "file:" prefix from a location string.
     * 
     * @param location Location
     * @return Location withou prefix
     */
    private String removeFilePrefix(String location) {

        String prefix = "file:";
        if (StringUtils.startsWithIgnoreCase(location, prefix)) {

            return location.substring(prefix.length());
        }

        return location;
    }

    /**
     * Add a wsdl location to import of document type.
     * <p>
     * Adds a wsdl location to the codegen plugin configuration. If code
     * generation plugin configuration not exists, it will be created.
     * </p>
     * 
     * @param wsdlLocation WSDL file location
     * @return Location added to pom ?
     */
    private boolean addImportLocationDocument(String wsdlLocation) {

        // Get plugin template
        Element pluginTemplate = XmlUtils.findFirstElement(
                "/codegen-plugin/plugin", XmlUtils.getRootElement(
                        this.getClass(),
                        "dependencies-import-codegen-plugin.xml"));

        // Add plugin
        getProjectOperations().updateBuildPlugin(
                getProjectOperations().getFocusedModuleName(),
                new Plugin(pluginTemplate));
        getFileManager().commit();

        // Get pom.xml
        String pomPath = getPomFilePath();
        Validate.notNull(pomPath, POM_FILE_NOT_FOUND);

        // Get a mutable pom.xml reference to modify it
        MutableFile pomMutableFile = getFileManager().updateFile(pomPath);
        Document pom = getInputDocument(pomMutableFile.getInputStream());

        Element root = pom.getDocumentElement();

        // Get plugin element
        Element plugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
                        root);

        // If plugin element not exists, message error
        Validate.notNull(plugin,
                "Codegen plugin is not defined in the pom.xml, relaunch again this command.");

        // Check URL connection and WSDL format
        Element rootElement = getSecurityService().getWsdl(wsdlLocation)
                .getDocumentElement();

        // The wsdl location already exists on old plugin format ?
        Element wsdlOptionElement = XmlUtils.findFirstElement(
                "executions/execution/configuration/wsdlOptions/wsdlOption[wsdl='"
                        + removeFilePrefix(wsdlLocation) + "']", plugin);

        // The wsdl location already exists on new plugin format ?
        String serviceId = WsdlParserUtils
                .findFirstCompatibleServiceElementName(rootElement);
        Element execution = XmlUtils.findFirstElement(
                "executions/execution[phase='generate-sources' and id='"
                        + serviceId + "']", plugin);

        // If location already added on plugin, do nothing
        if (execution != null || wsdlOptionElement != null) {

            return false;

        }

        // Create global executions section if not exists already
        Element executions = DomUtils
                .findFirstElementByName(EXECUTIONS, plugin);
        if (executions == null) {
            executions = pom.createElement(EXECUTIONS);
            plugin.appendChild(executions);

        }

        // Create an execution section for this service
        execution = pom.createElement(EXECUTION);
        Element id = pom.createElement("id");
        id.setTextContent(serviceId);
        Element phase = pom.createElement(PHASE2);
        phase.setTextContent("generate-sources");
        execution.appendChild(id);
        execution.appendChild(phase);
        Element goals = pom.createElement(GOALS2);
        Element goal = pom.createElement(GOAL2);
        goal.setTextContent("wsdl2java");
        goals.appendChild(goal);
        execution.appendChild(goals);
        executions.appendChild(execution);

        // Access execution > configuration > sourceRoot, wsdlOptions and
        // defaultOptions.
        // Configuration, sourceRoot, wsdlOptions and defaultOptions are
        // created if not exists.
        Element configuration = DomUtils.findFirstElementByName(CONFIGURATION2,
                execution);
        if (configuration == null) {

            configuration = pom.createElement(CONFIGURATION2);
            execution.appendChild(configuration);
        }
        Element sourceRoot = DomUtils.findFirstElementByName("sourceRoot",
                configuration);
        if (sourceRoot == null) {

            sourceRoot = pom.createElement("sourceRoot");
            sourceRoot
                    .setTextContent("${basedir}/target/generated-sources/client");
            configuration.appendChild(sourceRoot);
        }
        Element defaultOptions = DomUtils.findFirstElementByName(
                "defaultOptions", configuration);
        if (defaultOptions == null) {

            appendDefaultOptions(pom, configuration);

        }
        Element wsdlOptions = DomUtils.findFirstElementByName("wsdlOptions",
                configuration);
        if (wsdlOptions == null) {

            wsdlOptions = pom.createElement("wsdlOptions");
            configuration.appendChild(wsdlOptions);
        }

        // Create new wsdl element and append it to the XML tree
        Element wsdlOption = pom.createElement("wsdlOption");
        Element wsdl = pom.createElement("wsdl");
        wsdl.setTextContent(removeFilePrefix(wsdlLocation));
        wsdlOption.appendChild(wsdl);

        // Configure the packagename to generate client sources
        Element packagenames = pom.createElement("packagenames");
        Element packagename = pom.createElement("packagename");

        // Add the package name to generate sources
        String packageName = WsdlParserUtils
                .getTargetNamespaceRelatedPackage(rootElement);
        packagename.setTextContent(packageName.substring(0,
                packageName.length() - 1));

        packagenames.appendChild(packagename);
        wsdlOption.appendChild(packagenames);
        wsdlOptions.appendChild(wsdlOption);

        // Write new XML to disk
        XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);

        return true;
    }

    /**
     * Add a wsdl location to import of document type.
     * <p>
     * Adds a wsdl location to the axistools plugin configuration. If code
     * generation plugin configuration not exists, it will be created.
     * </p>
     * 
     * @param wsdlLocation WSDL file location
     * @return Location added to pom ?
     */
    private boolean addImportLocationRpc(String wsdlLocation) {

        // Get plugin template
        Element plugin = XmlUtils.findFirstElement("/axistools-plugin/plugin",
                XmlUtils.getRootElement(this.getClass(),
                        "dependencies-import-axistools-plugin.xml"));

        // Add plugin
        getProjectOperations().updateBuildPlugin(
                getProjectOperations().getFocusedModuleName(),
                new Plugin(plugin));
        getFileManager().commit();

        // Get pom.xml
        String pomPath = getPomFilePath();
        Validate.notNull(pomPath, POM_FILE_NOT_FOUND);

        // Get a mutable pom.xml reference to modify it
        MutableFile pomMutableFile = getFileManager().updateFile(pomPath);
        Document pom = getInputDocument(pomMutableFile.getInputStream());

        Element root = pom.getDocumentElement();

        // Get plugin element
        Element axistoolsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.codehaus.mojo' and artifactId='axistools-maven-plugin']",
                        root);

        // If plugin element not exists, message error
        Validate.notNull(axistoolsPlugin,
                "Axistools plugin is not defined in the pom.xml, relaunch again this command.");

        // Check URL connection and WSDL format
        Element rootElement = getSecurityService().getWsdl(wsdlLocation)
                .getDocumentElement();

        // The wsdl location already exists on old plugin format ?
        Element wsdlLocationUrl = XmlUtils.findFirstElement(
                "executions/execution/configuration/urls[url='" + wsdlLocation
                        + "']", axistoolsPlugin);

        // The wsdl location already exists on new plugin format ?
        String serviceId = WsdlParserUtils
                .findFirstCompatibleServiceElementName(rootElement);
        Element wsdlLocationElement = XmlUtils
                .findFirstElement("executions/execution[id='" + serviceId
                        + "']", axistoolsPlugin);

        // If location already added on plugin, do nothing
        if (wsdlLocationElement != null || wsdlLocationUrl != null) {

            return false;
        }

        // Access configuration > urls element.
        // Configuration and urls are created if not exists.
        Element executions = DomUtils.findFirstElementByName(EXECUTIONS,
                axistoolsPlugin);
        if (executions == null) {

            executions = pom.createElement(EXECUTIONS);
            axistoolsPlugin.appendChild(executions);
        }

        Element execution = pom.createElement(EXECUTION);
        Element id = pom.createElement("id");
        id.setTextContent(serviceId);
        Element phase = pom.createElement(PHASE2);
        phase.setTextContent("generate-sources");
        execution.appendChild(id);
        execution.appendChild(phase);
        Element goals = pom.createElement(GOALS2);
        Element goal = pom.createElement(GOAL2);
        goal.setTextContent("wsdl2java");
        goals.appendChild(goal);
        execution.appendChild(goals);
        executions.appendChild(execution);

        Element configuration = pom.createElement(CONFIGURATION2);
        execution.appendChild(configuration);

        Element urls = pom.createElement("urls");
        configuration.appendChild(urls);

        // Configure the packagename to generate client sources
        Element packageSpace = pom.createElement("packageSpace");
        String packageName = WsdlParserUtils
                .getTargetNamespaceRelatedPackage(rootElement);
        packageSpace.setTextContent(packageName.substring(0,
                packageName.length() - 1));
        configuration.appendChild(packageSpace);

        // Create new url element and append it to the XML tree
        Element url = pom.createElement("url");
        url.setTextContent(wsdlLocation);
        urls.appendChild(url);

        // Write new XML to disk
        XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean importService(JavaType serviceClass, String wsdlLocation,
            WsType type) {

        // Install import WS configuration requirements, if not installed
        boolean propertiesUpdated = install(type);

        // Add wsdl location to pom.xml
        boolean added = addImportLocation(wsdlLocation, type);

        // Target sources folder already exists ?
        boolean sourcesExists = new File(
                WsdlParserUtils.TARGET_GENERATED_SOURCES_PATH).exists();

        // Regenerating sources required ?
        return added || !sourcesExists || propertiesUpdated;
    }

    /**
     * {@inheritDoc}
     */
    public void mvn(String parameters, String message) throws IOException {

        logger.log(Level.INFO, message + " ...");
        try {

            getMavenOperations().executeMvnCommand(parameters);
        }
        catch (IOException e) {
            logger.log(Level.WARNING, message + " error !");
            throw e;
        }
        catch (RuntimeException e) {
            logger.log(Level.WARNING, message + " error !");
            throw e;
        }
    }

    /**
     * Check if pom.xml file exists in the project and return the path.
     * <p>
     * Checks if exists pom.xml config file. If not exists, null will be
     * returned.
     * </p>
     * 
     * @return Path to the pom.xml file or null if not exists.
     */
    private String getPomFilePath() {

        // Project ID
        String prjId = ProjectMetadata
                .getProjectIdentifier(getProjectOperations()
                        .getFocusedModuleName());
        ProjectMetadata projectMetadata = (ProjectMetadata) getMetadataService()
                .get(prjId);
        Validate.isTrue(projectMetadata != null, "Project metadata required");

        String pomFileName = "pom.xml";

        // Checks for pom.xml
        String pomPath = getProjectOperations().getPathResolver()
                .getIdentifier(LogicalPath.getInstance(Path.ROOT, ""),
                        pomFileName);

        boolean pomInstalled = getFileManager().exists(pomPath);

        if (pomInstalled) {

            return pomPath;
        }
        else {

            return null;
        }
    }

    /**
     * Convenience method to return a Collection as a delimited (e.g. CSV)
     * String. E.g. useful for <code>toString()</code> implementations.
     * 
     * @param coll the Collection to display
     * @param delim the delimiter to use (probably a ",")
     * @param prefix the String to start each element with
     * @param suffix the String to end each element with
     * @return the delimited String
     */
    public static String collectionToDelimitedString(List<String> coll,
            String delim, String prefix, String suffix) {
        if (coll == null || coll.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return fileManager;
        }
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public SecurityService getSecurityService() {
        if (securityService == null) {
            // Get all Services implement SecurityService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                SecurityService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (SecurityService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load SecurityService on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return securityService;
        }
    }

    public AnnotationsService getAnnotationsService() {
        if (annotationsService == null) {
            // Get all Services implement SecurityService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                AnnotationsService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (AnnotationsService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load AnnotationsService on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return annotationsService;
        }
    }

    public MavenOperations getMavenOperations() {
        if (mavenOperations == null) {
            // Get all Services implement MavenOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MavenOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MavenOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MavenOperations on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return mavenOperations;
        }
    }

}
