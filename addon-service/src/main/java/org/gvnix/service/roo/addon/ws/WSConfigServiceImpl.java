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
package org.gvnix.service.roo.addon.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

    @Reference
    private MetadataService metadataService;
    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private SecurityService securityService;
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private ProcessManager processManager;

    private static final String CXF_WSDL2JAVA_EXECUTION_ID = "generate-sources-cxf-server";

    protected static Logger logger = Logger.getLogger(WSConfigService.class
            .getName());

    /**
     * {@inheritDoc}
     * <p>
     * Check if Cxf is set in the project.
     * </p>
     * <p>
     * If is not set, then installs dependencies to the pom.xml and creates the
     * cxf configuration file.
     * </p>
     * 
     * @param type
     *            Communication type
     */
    public boolean install(CommunicationSense type) {

        // Check if properties are set in pom.xml
        boolean propertiesUpdated = addProjectProperties(type);

        // Check if it's already installed.
        if (!isLibraryInstalled(type)) {

            // Add dependencies to project
            installDependencies(type);
        }

        if (type == CommunicationSense.EXPORT) {

            // Create CXF config file src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
            installCxfConfigurationFile();

            // Update src/main/webapp/WEB-INF/web.xml :
            // - Add CXFServlet and map it to /services/*
            // - Add cxf-PROJECT_NAME.xml to Spring Context Loader
            installCxfWebConfigurationFile();
        }

        return propertiesUpdated;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks these types:
     * </p>
     * <ul>
     * <li>Cxf Dependencies in pom.xml</li>
     * <li>Cxf configuration file exists</li>
     * </ul>
     */
    public boolean isLibraryInstalled(CommunicationSense type) {

        // TODO Check Web configuration file on IMPORT ?

        boolean cxfInstalled = isDependenciesInstalled(type);

        if (type == CommunicationSense.EXPORT) {

            cxfInstalled = cxfInstalled
                    && fileManager.exists(getCxfConfigurationFilePath());
        }

        return cxfInstalled;
    }

    /**
     * Returns CXF absolute configuration file path in the project.
     * <p>
     * Creates the cxf config file using project name.
     * </p>
     * 
     * @return Path to the Cxf configuration file or null if not exists
     */
    private String getCxfConfigurationFilePath() {

        String cxfFile = "WEB-INF/cxf-".concat(getProjectName()).concat(".xml");

        // Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
        String cxfXmlPath = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, cxfFile);

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

            FileCopyUtils.copy(templateInputStream,
                    cxfXmlMutableFile.getOutputStream());
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }

        fileManager.scan();
    }

    /**
     * Check if dependencies are set in project's pom.xml.
     * <p>
     * Search if the dependencies defined in addon sense type xml file
     * (dependencies-*.xml) are set in pom.xml.
     * </p>
     * 
     * @param type
     *            Communication type
     * @return true if all dependencies are set in pom.xml
     */
    protected boolean isDependenciesInstalled(CommunicationSense type) {

        boolean cxfDependenciesExists = true;

        ProjectMetadata project = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        if (project == null) {
            return false;
        }

        // Dependencies elements are defined as:
        // <dependency org="org.apache.cxf" name="cxf-rt-bindings-soap"
        // rev="2.2.6" />
        List<Element> cxfDependenciesList = getRequiredDependencies(type);

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
     * @param type
     *            Type of required dependencies
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
        case IMPORT_RPC_ENCODED:
            name.append("import-axis");
            break;
        }

        name.append(".xml");

        return name.toString();
    }

    /**
     * Get Addon dependencies list to install.
     * <p>
     * Get addon dependencies defined in dependencies-XXXX.xml
     * </p>
     * 
     * @param type
     *            Communication type
     * @return List of addon dependencies as xml elements
     */
    protected List<Element> getRequiredDependencies(CommunicationSense type) {

        // TODO Unify distinct dependencies files in only one

        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
                getCxfRequiredDependenciesFileName(type));
        Assert.notNull(templateInputStream, "Can't adquire dependencies file "
                + type);

        Document dependencyDoc;
        try {

            dependencyDoc = XmlUtils.getDocumentBuilder().parse(
                    templateInputStream);
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }

        Element dependencies = (Element) dependencyDoc.getFirstChild();

        // TODO If only one dependencies file: /dependencies/XXXXX/dependency
        return XmlUtils.findElements("/dependencies/dependency", dependencies);
    }

    /**
     * Add addon dependencies to project dependencies if necessary.
     * 
     * @param type
     *            Communication type
     */
    private void installDependencies(CommunicationSense type) {

        // If dependencies are installed.
        boolean isInstalled = isDependenciesInstalled(type);

        // Add project properties values.
        // addProjectProperties(type);

        if (!isInstalled) {
            List<Element> cxfDependencies = getRequiredDependencies(type);
            for (Element dependency : cxfDependencies) {

                projectOperations.addDependency(new Dependency(dependency));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean addProjectProperties(CommunicationSense type) {

        // Add project properties, as versions
        List<Element> projectProperties = new ArrayList<Element>();

        switch (type) {

        case IMPORT:

            projectProperties = XmlUtils
                    .findElements("/configuration/gvnix/properties/*",
                            XmlUtils.getConfiguration(this.getClass(),
                                    "properties.xml"));
            break;

        case EXPORT:

            projectProperties = XmlUtils
                    .findElements("/configuration/gvnix/properties/*",
                            XmlUtils.getConfiguration(this.getClass(),
                                    "properties.xml"));
            break;

        case EXPORT_WSDL:

            projectProperties = XmlUtils
                    .findElements("/configuration/gvnix/properties/*",
                            XmlUtils.getConfiguration(this.getClass(),
                                    "properties.xml"));
            break;

        case IMPORT_RPC_ENCODED:

            // TODO Check cxf version property before ?
            projectProperties = XmlUtils.findElements(
                    "/configuration/gvnix/properties/*", XmlUtils
                            .getConfiguration(this.getClass(),
                                    "properties-axis.xml"));
            break;
        }

        return DependenciesVersionManager.managePropertyVersion(
                metadataService, projectOperations, projectProperties);
    }

    /**
     * Update WEB-INF/web.xml.
     * <ul>
     * <li>Create the CXF servlet declaration and mapping</li>
     * <li>Configure ContextLoader to load cxf-PROJECT_ID.xml</li>
     * </ul>
     */
    private void installCxfWebConfigurationFile() {

        String webXmlPath = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml");
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
     * {@inheritDoc}
     */
    public boolean publishClassAsWebService(JavaType className,
            AnnotationMetadata annotationMetadata) {

        Assert.isTrue(annotationMetadata != null, "Annotation '"
                + annotationMetadata.getAnnotationType()
                        .getFullyQualifiedTypeName() + "' in class '"
                + className.getFullyQualifiedTypeName()
                + "'must not be null to check cxf xml configuration file.");

        // Update web service configuration file.
        boolean updateGvNIXWebServiceAnnotation = updateConfiguration(
                className, annotationMetadata);

        return updateGvNIXWebServiceAnnotation;
    }

    /**
     * Updates web services configuration file.
     * 
     * @param className
     *            to export.
     * @param annotationMetadata
     *            values from web service class to set in configuration file.
     * @return true if annotation from className has to be updated because of
     *         changes in package or class name.
     */
    private boolean updateConfiguration(JavaType className,
            AnnotationMetadata annotationMetadata) {

        StringAttributeValue serviceName = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("serviceName"));

        Assert.isTrue(
                serviceName != null
                        && StringUtils.hasText(serviceName.getValue()),
                "Annotation attribute 'serviceName' in "
                        + className.getFullyQualifiedTypeName()
                        + "' must be defined.");

        StringAttributeValue address = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("address"));

        Assert.isTrue(
                address != null && StringUtils.hasText(address.getValue()),
                "Annotation attribute 'address' in "
                        + className.getFullyQualifiedTypeName()
                        + "' must be defined.");

        StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

        Assert.isTrue(
                fullyQualifiedTypeName != null
                        && StringUtils.hasText(fullyQualifiedTypeName
                                .getValue()),
                "Annotation attribute 'fullyQualifiedTypeName' in "
                        + className.getFullyQualifiedTypeName()
                        + "' must be defined.");

        BooleanAttributeValue exported = (BooleanAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("exported"));

        Assert.isTrue(exported != null, "Annotation attribute 'exported' in "
                + className.getFullyQualifiedTypeName() + "' must be defined.");

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

        boolean updateFullyQualifiedTypeName = false;

        // Check if class name and annotation class name are different.
        if (!className.getFullyQualifiedTypeName().contentEquals(
                fullyQualifiedTypeName.getValue())) {
            updateFullyQualifiedTypeName = true;
        }

        // Check if service exists in configuration file.
        boolean updateService = true;

        // 1) Check if class and id exists in bean.
        Element classAndIdService = XmlUtils.findFirstElement(
                "/beans/bean[@id='" + serviceName.getValue().concat("Impl")
                        + "' and @class='"
                        + className.getFullyQualifiedTypeName() + "']", root);

        // Service is already published.
        if (classAndIdService != null) {
            logger.log(Level.FINE, "The service '" + serviceName.getValue()
                    + "' is already set in cxf config file.");
            updateService = false;
        }

        if (updateService) {

            // 2) Check if class exists or it hasn't changed.
            Element classService = null;

            if (updateFullyQualifiedTypeName) {

                // Check if exists with class name.
                classService = XmlUtils.findFirstElement("/beans/bean[@class='"
                        + className.getFullyQualifiedTypeName() + "']", root);

                if (classService != null) {

                    // Update bean with new Id attribute.
                    Element updateClassService = classService;
                    String idValue = classService.getAttribute("id");

                    if (!StringUtils.hasText(idValue)
                            || !idValue.contentEquals(serviceName.getValue()
                                    .concat("Impl"))) {
                        updateClassService.setAttribute("id", serviceName
                                .getValue().concat("Impl"));

                        classService.getParentNode().replaceChild(
                                updateClassService, classService);
                        logger.log(
                                Level.INFO,
                                "The service '"
                                        + serviceName.getValue()
                                        + "' has updated 'id' attribute in cxf config file.");
                    }

                } else {

                    // Check if exists with fullyQualifiedTypeName.
                    classService = XmlUtils.findFirstElement(
                            "/beans/bean[@class='"
                                    + fullyQualifiedTypeName.getValue() + "']",
                            root);

                    if (classService != null) {

                        Element updateClassService = classService;
                        String idValue = classService.getAttribute("id");

                        updateClassService.setAttribute("class",
                                className.getFullyQualifiedTypeName());

                        if (!StringUtils.hasText(idValue)
                                || !idValue.contentEquals(serviceName
                                        .getValue().concat("Impl"))) {
                            updateClassService.setAttribute("id", serviceName
                                    .getValue().concat("Impl"));

                            logger.log(
                                    Level.INFO,
                                    "The service '"
                                            + serviceName.getValue()
                                            + "' has updated 'id' attribute in cxf config file.");
                        }

                        classService.getParentNode().replaceChild(
                                updateClassService, classService);
                        logger.log(
                                Level.INFO,
                                "The service '"
                                        + serviceName.getValue()
                                        + "' has updated 'class' attribute in cxf config file.");
                    }

                }
            } else {

                // Check if exists with class name.
                classService = XmlUtils.findFirstElement("/beans/bean[@class='"
                        + className.getFullyQualifiedTypeName() + "']", root);

                if (classService != null) {

                    // Update bean with new Id attribute.
                    Element updateClassService = classService;
                    String idValue = classService.getAttribute("id");

                    if (!StringUtils.hasText(idValue)
                            || !idValue.contentEquals(serviceName.getValue()
                                    .concat("Impl"))) {
                        updateClassService.setAttribute("id", serviceName
                                .getValue().concat("Impl"));

                        classService.getParentNode().replaceChild(
                                updateClassService, classService);
                        logger.log(
                                Level.INFO,
                                "The service '"
                                        + serviceName.getValue()
                                        + "' has updated 'id' attribute in cxf config file.");
                    }
                }
            }

            // 3) Check if id exists.
            Element idService = XmlUtils.findFirstElement("/beans/bean[@id='"
                    + serviceName.getValue().concat("Impl") + "']", root);

            if (idService != null) {

                // Update bean with new class attribute.
                Element updateIdService = idService;
                String classNameAttribute = idService.getAttribute("class");

                if (!StringUtils.hasText(classNameAttribute)
                        || !classNameAttribute.contentEquals(className
                                .getFullyQualifiedTypeName())) {
                    updateIdService.setAttribute("class",
                            className.getFullyQualifiedTypeName());
                    idService.getParentNode().replaceChild(updateIdService,
                            idService);
                    logger.log(
                            Level.INFO,
                            "The service '"
                                    + serviceName.getValue()
                                    + "' has updated 'class' attribute in cxf config file.");
                }

            }

            Element bean;
            // Check id and class values to create a new bean.
            if (classService == null && idService == null) {

                bean = cxfXml.createElement("bean");
                bean.setAttribute("id", serviceName.getValue().concat("Impl"));
                bean.setAttribute("class",
                        className.getFullyQualifiedTypeName());

                root.appendChild(bean);
            }
        }

        boolean updateEndpoint = true;

        // Check if endpoint exists in the configuration file.
        Element jaxwsBean = XmlUtils.findFirstElement(
                "/beans/endpoint[@address='/" + address.getValue()
                        + "' and @id='" + serviceName.getValue() + "']", root);

        // 1) Check if exists with id and address.
        if (jaxwsBean != null) {

            logger.log(Level.FINE, "The endpoint '" + serviceName.getValue()
                    + "' is already set in cxf config file.");
            updateEndpoint = false;
        }

        if (updateEndpoint) {

            // 2) Check if exists a bean with annotation address value and
            // updates id attribute with annotation serviceName value.
            Element addressEndpoint = XmlUtils.findFirstElement(
                    "/beans/endpoint[@address='/" + address.getValue() + "']",
                    root);

            if (addressEndpoint != null) {

                // Update bean with new Id attribute.
                Element updateAddressEndpoint = addressEndpoint;
                String idAttribute = addressEndpoint.getAttribute("id");

                if (!StringUtils.hasText(idAttribute)
                        || !idAttribute.contentEquals(serviceName.getValue())) {

                    updateAddressEndpoint.setAttribute("id",
                            serviceName.getValue());
                    updateAddressEndpoint.setAttribute("implementor", "#"
                            .concat(serviceName.getValue()).concat("Impl"));

                    addressEndpoint.getParentNode().replaceChild(
                            updateAddressEndpoint, addressEndpoint);
                    logger.log(
                            Level.INFO,
                            "The endpoint bean '"
                                    + serviceName.getValue()
                                    + "' has updated 'id' attribute in cxf config file.");

                }

            }

            Element idEndpoint = XmlUtils.findFirstElement(
                    "/beans/endpoint[@id='" + serviceName.getValue() + "']",
                    root);

            // 3) Check if exists a bean with annotation address value in id
            // attribute and updates address attribute with annotation address
            // value.
            if (idEndpoint != null) {

                // Update bean with new Id attribute.
                Element updateIdEndpoint = idEndpoint;

                String addressAttribute = idEndpoint.getAttribute("address");

                if (!StringUtils.hasText(addressAttribute)
                        || !addressAttribute.contentEquals("/".concat(address
                                .getValue()))) {

                    updateIdEndpoint.setAttribute("address",
                            "/".concat(address.getValue()));

                    idEndpoint.getParentNode().replaceChild(updateIdEndpoint,
                            idEndpoint);
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
                        "#".concat(serviceName.getValue()).concat("Impl"));
                endpoint.setAttribute("address", "/".concat(address.getValue()));
                root.appendChild(endpoint);
            }

        }

        // Update configuration file.
        if (updateService || updateEndpoint) {
            XmlUtils.writeXml(cxfXmlMutableFile.getOutputStream(), cxfXml);
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
     */
    public void addToJava2wsPlugin(JavaType serviceClass, String serviceName,
            String addressName, String fullyQualifiedTypeName) {

        // Get pom
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
            installJava2wsPlugin();
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
     * Generates Element for service wsdl generation execution
     * 
     * @param pom
     *            Pom document
     * @param serviceClass
     *            to generate
     * @param addressName
     *            of the service
     * @param executionID
     *            execution identifier
     * @return
     */
    private Element createJava2wsExecutionElement(Document pom,
            JavaType serviceClass, String addressName, String executionID) {

        Element serviceExecution = pom.createElement("execution");

        // execution.id
        Element id = pom.createElement("id");
        id.setTextContent(executionID);

        // execution.phase
        serviceExecution.appendChild(id);
        Element phase = pom.createElement("phase");
        phase.setTextContent("test");
        serviceExecution.appendChild(phase);

        // Execution.Configuration
        Element configuration = pom.createElement("configuration");

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
        genWsdl.setTextContent("true");

        // execution.configuration.verbose
        Element verbose = pom.createElement("verbose");
        verbose.setTextContent("true");

        // execution.configuration
        configuration.appendChild(className);
        configuration.appendChild(outputFile);
        configuration.appendChild(genWsdl);
        configuration.appendChild(verbose);

        // execution
        serviceExecution.appendChild(configuration);

        // Goals
        Element goals = pom.createElement("goals");
        Element goal = pom.createElement("goal");
        goal.setTextContent("java2ws");
        goals.appendChild(goal);

        serviceExecution.appendChild(goals);
        return serviceExecution;
    }

    /**
     * {@inheritDoc}
     */
    public void installJava2wsPlugin() {
        Element pluginElement = XmlUtils.findFirstElement(
                "/jaxws-plugin/plugin",
                XmlUtils.getConfiguration(this.getClass(),
                        "dependencies-export-jaxws-plugin.xml"));

        projectOperations.updateBuildPlugin(new Plugin(pluginElement));
        fileManager.commit();

    }

    /**
     * Install wsdl2java maven plugin in pom.xml
     */
    public void installWsdl2javaPlugin() {
        // Get plugin template
        Element plugin = XmlUtils.findFirstElement(
                "/cxf-codegen/cxf-codegen-plugin/plugin", XmlUtils
                        .getConfiguration(this.getClass(),
                                "dependencies-export-wsdl2java-plugin.xml"));

        // Add plugin
        projectOperations.updateBuildPlugin(new Plugin(plugin));
        fileManager.commit();
    }

    /**
     * {@inheritDoc}
     */
    public boolean addImportLocation(String wsdlLocation,
            CommunicationSense type) {

        // Adds Project properties to pom.xml
        // addProjectProperties(type);

        // Identifies the type of library to use
        if (CommunicationSense.IMPORT_RPC_ENCODED.equals(type)) {
            return addImportLocationRpc(wsdlLocation);
        } else {
            return addImportLocationDocument(wsdlLocation);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean addExportLocation(String wsdlLocation,
            Document wsdlDocument, CommunicationSense type) {

        // Project properties to pom.xml
        boolean propertiesUpdated = addProjectProperties(type);

        switch (type) {

        case EXPORT:
            // TODO: Refactor method name to use for all CommunicationSense to
            // set each plugin.
            break;

        case EXPORT_WSDL:
            // Export Wsdl2Java
            addExportWSDLLocationDocument(wsdlLocation, wsdlDocument);
            break;
        }

        return propertiesUpdated;
    }

    /**
     * <p>
     * Adds a wsdl location to the codegen plugin configuration. If code
     * generation plugin configuration not exists, it will be created.
     * </p>
     * 
     * @param wsdlLocation
     *            WSDL file location.
     * @param wsdlDocument
     *            WSDL file.
     */
    private void addExportWSDLLocationDocument(String wsdlLocation,
            Document wsdlDocument) {

        // install Plugin
        installWsdl2javaPlugin();

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
        Element codegenWsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
                        root);

        // If plugin element not exists, error message
        Assert.notNull(codegenWsPlugin,
                "Codegen plugin is not defined in the pom.xml, relaunch again this command.");

        // Checks if already exists the execution.
        Element oldGenerateSourcesCxfServer = XmlUtils.findFirstElement(
                "/project/build/plugins/plugin/executions/execution[id='"
                        + CXF_WSDL2JAVA_EXECUTION_ID + "']", root);

        // Generate execution configuration element.
        Element newGenerateSourcesCxfServer = createWsdl2JavaExecutionElement(
                pom, wsdlDocument, wsdlLocation);

        // Checks if exists executions.
        Element oldExecutions = XmlUtils.findFirstElementByName("executions",
                codegenWsPlugin);

        Element newExecutions;

        // To Update execution definitions It must be replaced in pom.xml to
        // maintain the format.
        if (oldGenerateSourcesCxfServer != null) {
            oldGenerateSourcesCxfServer.getParentNode().replaceChild(
                    newGenerateSourcesCxfServer, oldGenerateSourcesCxfServer);
        } else {

            if (oldExecutions == null) {
                newExecutions = pom.createElement("executions");
                newExecutions.appendChild(newGenerateSourcesCxfServer);

                codegenWsPlugin.appendChild(newExecutions);
            } else {
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
     * @param wsdlDocument
     *            to generate sources
     * @param wsdlLocation
     *            current wsdl location
     * @return
     */
    private Element createWsdl2JavaExecutionElement(Document pom,
            Document wsdlDocument, String wsdlLocation) {
        Element newGenerateSourcesCxfServer = pom.createElement("execution");

        // Create name for id.
        // execution.id
        Element id = pom.createElement("id");
        id.setTextContent(CXF_WSDL2JAVA_EXECUTION_ID);
        newGenerateSourcesCxfServer.appendChild(id);

        // execution.phase
        Element phase = pom.createElement("phase");
        phase.setTextContent("generate-sources");
        newGenerateSourcesCxfServer.appendChild(phase);

        // execution.goals.goal
        Element goals = pom.createElement("goals");
        Element goal = pom.createElement("goal");
        goal.setTextContent("wsdl2java");
        goals.appendChild(goal);
        newGenerateSourcesCxfServer.appendChild(goals);

        // execution.configuration
        Element configuration = pom.createElement("configuration");
        newGenerateSourcesCxfServer.appendChild(configuration);

        // execution.configuration.sourceRoo
        Element sourceRoot = pom.createElement("sourceRoot");
        sourceRoot
                .setTextContent("${basedir}/target/generated-sources/cxf/server");
        configuration.appendChild(sourceRoot);

        // execution.configuration.defaultOption
        Element defaultOptions = pom.createElement("defaultOptions");
        configuration.appendChild(defaultOptions);

        // Soap Headers.
        // execution.configuration.defaultOption.extendedSoapHeaders <-- true
        Element extendedSoapHeaders = pom.createElement("extendedSoapHeaders");
        extendedSoapHeaders.setTextContent("true");
        defaultOptions.appendChild(extendedSoapHeaders);

        // AutoNameResolution to solve naming conflicts.
        // execution.configuration.defaultOption.autoNameResolution <-- true
        Element autoNameResolution = pom.createElement("autoNameResolution");
        autoNameResolution.setTextContent("true");
        defaultOptions.appendChild(autoNameResolution);

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
     * Remove the "file:" prefix from a location string.
     * 
     * @param location
     *            Location
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
     * @param wsdlLocation
     *            WSDL file location
     * @return Location added to pom ?
     */
    private boolean addImportLocationDocument(String wsdlLocation) {

        // Get plugin template
        Element pluginTemplate = XmlUtils.findFirstElement(
                "/codegen-plugin/plugin", XmlUtils.getConfiguration(
                        this.getClass(),
                        "dependencies-import-codegen-plugin.xml"));

        // Add plugin
        projectOperations.updateBuildPlugin(new Plugin(pluginTemplate));
        fileManager.commit();

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
        Element plugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
                        root);

        // If plugin element not exists, message error
        Assert.notNull(plugin,
                "Codegen plugin is not defined in the pom.xml, relaunch again this command.");

        // Check URL connection and WSDL format
        Element rootElement = securityService.loadWsdlUrl(wsdlLocation)
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
        Element executions = XmlUtils.findFirstElementByName("executions",
                plugin);
        if (executions == null) {
            executions = pom.createElement("executions");
            plugin.appendChild(executions);

        }

        // Create an execution section for this service
        execution = pom.createElement("execution");
        Element id = pom.createElement("id");
        id.setTextContent(serviceId);
        Element phase = pom.createElement("phase");
        phase.setTextContent("generate-sources");
        execution.appendChild(id);
        execution.appendChild(phase);
        Element goals = pom.createElement("goals");
        Element goal = pom.createElement("goal");
        goal.setTextContent("wsdl2java");
        goals.appendChild(goal);
        execution.appendChild(goals);
        executions.appendChild(execution);

        // Access execution > configuration > sourceRoot, wsdlOptions and
        // defaultOptions.
        // Configuration, sourceRoot, wsdlOptions and defaultOptions are
        // created if not exists.
        Element configuration = XmlUtils.findFirstElementByName(
                "configuration", execution);
        if (configuration == null) {

            configuration = pom.createElement("configuration");
            execution.appendChild(configuration);
        }
        Element sourceRoot = XmlUtils.findFirstElementByName("sourceRoot",
                configuration);
        if (sourceRoot == null) {

            sourceRoot = pom.createElement("sourceRoot");
            sourceRoot
                    .setTextContent("${basedir}/target/generated-sources/client");
            configuration.appendChild(sourceRoot);
        }
        Element defaultOptions = XmlUtils.findFirstElementByName(
                "defaultOptions", configuration);
        if (defaultOptions == null) {

            defaultOptions = pom.createElement("defaultOptions");
            configuration.appendChild(defaultOptions);

            Element extendedSoapHeaders = pom
                    .createElement("extendedSoapHeaders");
            extendedSoapHeaders.setTextContent("true");
            defaultOptions.appendChild(extendedSoapHeaders);

            // AutoNameResolution to solve naming conflicts.
            Element autoNameResolution = pom
                    .createElement("autoNameResolution");
            autoNameResolution.setTextContent("true");
            defaultOptions.appendChild(autoNameResolution);

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
     * @param wsdlLocation
     *            WSDL file location
     * @return Location added to pom ?
     */
    private boolean addImportLocationRpc(String wsdlLocation) {

        // Get plugin template
        Element plugin = XmlUtils.findFirstElement("/axistools-plugin/plugin",
                XmlUtils.getConfiguration(this.getClass(),
                        "dependencies-import-axistools-plugin.xml"));

        // Add plugin
        projectOperations.updateBuildPlugin(new Plugin(plugin));
        fileManager.commit();

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
        Element axistoolsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.codehaus.mojo' and artifactId='axistools-maven-plugin']",
                        root);

        // If plugin element not exists, message error
        Assert.notNull(axistoolsPlugin,
                "Axistools plugin is not defined in the pom.xml, relaunch again this command.");

        // Check URL connection and WSDL format
        Element rootElement = securityService.loadWsdlUrl(wsdlLocation)
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
        Element executions = XmlUtils.findFirstElementByName("executions",
                axistoolsPlugin);
        if (executions == null) {

            executions = pom.createElement("executions");
            axistoolsPlugin.appendChild(executions);
        }

        Element execution = pom.createElement("execution");
        Element id = pom.createElement("id");
        id.setTextContent(serviceId);
        Element phase = pom.createElement("phase");
        phase.setTextContent("generate-sources");
        execution.appendChild(id);
        execution.appendChild(phase);
        Element goals = pom.createElement("goals");
        Element goal = pom.createElement("goal");
        goal.setTextContent("wsdl2java");
        goals.appendChild(goal);
        execution.appendChild(goals);
        executions.appendChild(execution);

        Element configuration = pom.createElement("configuration");
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
            CommunicationSense type) {

        // Install import WS configuration requirements, if not installed
        boolean propertiesUpdated = install(type);

        // Add wsdl location to pom.xml
        boolean added = addImportLocation(wsdlLocation, type);

        // Add GvNixAnnotations to the project.
        annotationsService.addGvNIXAnnotationsDependency();

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

        PathResolver pathResolver = projectOperations.getPathResolver();
        File root = new File(pathResolver.getRoot(Path.ROOT));

        Assert.isTrue(root.isDirectory() && root.exists(),
                "Project root does not currently exist as a directory ('"
                        + root.getCanonicalPath() + "')");

        String cmd = null;
        if (File.separatorChar == '\\') {
            cmd = "mvn.bat " + parameters;
        } else {
            cmd = "mvn " + parameters;
        }

        Process p = Runtime.getRuntime().exec(cmd, null, root);

        // Show maven command details only in development mode
        if (processManager.isDevelopmentMode()) {

            // Ensure separate threads are used for logging, as per ROO-652
            LoggingInputStream input = new LoggingInputStream(
                    p.getInputStream());
            LoggingInputStream errors = new LoggingInputStream(
                    p.getErrorStream());

            input.start();
            errors.start();
        } else {

            logger.log(Level.INFO, message + " ...");
        }

        try {

            if (p.waitFor() != 0) {

                throw new IllegalStateException(message + " error !");
            }

        } catch (InterruptedException e) {

            throw new IllegalStateException(e);
        }
    }

    private class LoggingInputStream extends Thread {

        private final BufferedReader inputStream;

        public LoggingInputStream(InputStream inputStream) {
            this.inputStream = new BufferedReader(new InputStreamReader(
                    inputStream));
        }

        @Override
        public void run() {
            String line;
            try {
                while ((line = inputStream.readLine()) != null) {
                    if (line.startsWith("[ERROR]")) {
                        logger.severe(line);
                    } else if (line.startsWith("[WARNING]")) {
                        logger.warning(line);
                    } else {
                        logger.info(line);
                    }
                }
            } catch (IOException ioe) {
                if (ioe.getMessage().contains("No such file or directory") || // for
                        // *nix/Mac
                        ioe.getMessage().contains("CreateProcess error=2")) // for
                // Windows
                {
                    logger.severe("Could not locate Maven executable; please ensure mvn command is in your path");
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignore) {
                    }
                }
            }

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
        String prjId = ProjectMetadata.getProjectIdentifier();
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(prjId);
        Assert.isTrue(projectMetadata != null, "Project metadata required");

        String pomFileName = "pom.xml";

        // Checks for pom.xml
        String pomPath = projectOperations.getPathResolver().getIdentifier(
                Path.ROOT, pomFileName);

        boolean pomInstalled = fileManager.exists(pomPath);

        if (pomInstalled) {

            return pomPath;
        } else {

            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if exists a project and if it has web.xml configuration file.
     * </p>
     */
    public boolean isProjectWebAvailable() {

        if (getPathResolver() == null) {

            return false;
        }

        String webXmlPath = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_WEBAPP, "/WEB-INF/web.xml");
        if (!fileManager.exists(webXmlPath)) {

            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if exists a project.
     * </p>
     */
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

}
