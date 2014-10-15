package org.gvnix.addon.monitoring;

import java.util.*;

import org.apache.felix.scr.annotations.*;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.*;

/**
 * Implementation of operations this add-on offers.
 * 
 * @since 1.4.0
 */
@Component
@Service
public class MonitoringOperationsImpl implements MonitoringOperations {

    @Reference
    private MenuOperations menuOperations;

    @Reference
    protected FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private PropFileOperations propFileOperations;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given
     * annotation in the project
     */
    @Reference
    private TypeLocationService typeLocationService;

    /**
     * Use TypeManagementService to change types
     */
    @Reference
    private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if a project has been created
        return projectOperations.isFocusedProjectAvailable();
    }

    /** {@inheritDoc} */
    public void setup(String pathString) {
        // Adding pom.xml dependencies
        addPomDependencies();
        // Modifying web.xml
        updateWebXML(pathString);
        // Adding JavaMelody persistence
        updatePersistence();
        // Modifying ApplicationContext to enable SQL monitoring
        updateAppContextSQL();
        // Add i18n necessary messages
        addI18nControllerProperties();
        // Adding menu entry
        addMenuEntry();
    }

    /**
     * This method adds a new entry menu
     */
    public void addMenuEntry() {
        String finalPath = "monitoring";
        menuOperations.addMenuItem(new JavaSymbolName(
                "monitoring_menu_category"), new JavaSymbolName(
                "monitoring_menu_entry"), "JMelody Monitoring",
                "global_generic", "/" + finalPath, null, getWebappPath());
    }

    /**
     * This method add necessary properties to messages.properties for
     * Controller
     */
    public void addI18nControllerProperties() {

        Map<String, String> propertyList = new HashMap<String, String>();

        propertyList.put("menu_category_monitoring_menu_category_label",
                "Monitoring");

        propFileOperations.addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

    }

    /**
     * This method updates ApplicationContext.xml to enable SQL monitoring
     */
    public void updateAppContextSQL() {
        String appContextPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES,
                "META-INF/spring/applicationContext.xml");

        if (fileManager.exists(appContextPath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(appContextPath,
                    fileManager);

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Checking if exist
            NodeList beanElements = docRoot.getElementsByTagName("bean");

            for (int i = 0; i < beanElements.getLength(); i++) {
                Node bean = beanElements.item(i);
                NamedNodeMap beanAttr = bean.getAttributes();
                if (beanAttr != null) {
                    Node idAttr = beanAttr.getNamedItem("id");
                    // Checking if bean exists on current beans
                    if ("springDataSourceBeanPostProcessor".equals(idAttr
                            .getNodeValue())) {
                        return;
                    }
                }
            }

            // Creating new element (bean)
            Element beanElement = docXml.createElement("bean");
            beanElement.setAttribute("id", "springDataSourceBeanPostProcessor");
            beanElement.setAttribute("class",
                    "net.bull.javamelody.SpringDataSourceBeanPostProcessor");

            docRoot.appendChild(beanElement);

            // Saving changes
            fileManager.createOrUpdateTextFileIfRequired(appContextPath,
                    XmlUtils.nodeToString(docXml), true);
        }

    }

    /**
     * This method updates persistence.xml to add persistence which is needed by
     * JavaMelody to work
     */
    public void updatePersistence() {
        String persistencePath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");

        if (fileManager.exists(persistencePath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(persistencePath,
                    fileManager);

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Getting provider
            NodeList allProviders = docRoot.getElementsByTagName("provider");

            // Modifying or creating provider element
            if (allProviders.getLength() > 0) {
                for (int i = 0; i < allProviders.getLength(); i++) {
                    Element provider = (Element) allProviders.item(i);
                    provider.setTextContent("net.bull.javamelody.JpaPersistence");
                }
            }
            else {
                Element providerElement = docXml.createElement("provider");
                providerElement
                        .setTextContent("net.bull.javamelody.JpaPersistence");
                docRoot.appendChild(providerElement);
            }

            // Getting properties node
            NodeList allProperties = docRoot.getElementsByTagName("properties");
            Element propertiesElement = null;

            // Modifying or creating properties element
            if (allProperties.getLength() > 0) {
                for (int i = 0; i < allProperties.getLength(); i++) {
                    propertiesElement = (Element) allProperties.item(i);
                    NodeList propertyElements = propertiesElement
                            .getChildNodes();
                    for (int x = 0; x < propertyElements.getLength(); x++) {
                        Node property = propertyElements.item(x);
                        NamedNodeMap propertyAttr = property.getAttributes();
                        if (propertyAttr != null) {
                            Node nameAttr = propertyAttr.getNamedItem("name");
                            // Checking if property exists on current Properties
                            if ("net.bull.javamelody.jpa.provider"
                                    .equals(nameAttr.getNodeValue())) {
                                return;
                            }
                        }
                    }
                }
            }
            else {
                propertiesElement = docXml.createElement("properties");
                docRoot.appendChild(propertiesElement);
            }

            // Creating provider property
            Element property = docXml.createElement("property");
            property.setAttribute("name", "net.bull.javamelody.jpa.provider");
            property.setAttribute("value",
                    "org.hibernate.ejb.HibernatePersistence");

            // Adding property to properties
            propertiesElement.appendChild(property);

            // Saving result
            fileManager.createOrUpdateTextFileIfRequired(persistencePath,
                    XmlUtils.nodeToString(docXml), true);

        }
    }

    /**
     * This method updates web.xml to add filter, filter-mapping and listener
     * which are needed to proper functioning of JavaMelody
     * 
     * @param pathString
     */
    public void updateWebXML(String pathString) {
        String webPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml");

        if (fileManager.exists(webPath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(webPath,
                    fileManager);

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Checking if exists
            NodeList allFilters = docRoot.getElementsByTagName("filter-name");
            for (int i = 0; i < allFilters.getLength(); i++) {
                Element filter = (Element) allFilters.item(i);
                if ("monitoring".equals(filter.getTextContent())) {
                    return;
                }
            }

            // Creating filter elements
            Element filterElement = docXml.createElement("filter");
            // filter-name
            Element filterNameElement = docXml.createElement("filter-name");
            filterNameElement.setTextContent("monitoring");
            filterElement.appendChild(filterNameElement);
            // filter-class
            Element filterClassElement = docXml.createElement("filter-class");
            filterClassElement
                    .setTextContent("net.bull.javamelody.MonitoringFilter");
            filterElement.appendChild(filterClassElement);
            // init-param {
            Element initParamElement = docXml.createElement("init-param");
            // description
            Element descriptionElement = docXml.createElement("description");
            descriptionElement
                    .setTextContent("Enables or disables the system actions garbage collector, http sessions,heap dump, memory histogram, process list, jndi tree, opened jdbcconnections, database (true by default).");
            initParamElement.appendChild(descriptionElement);
            // param-name
            Element paramNameElement = docXml.createElement("param-name");
            paramNameElement.setTextContent("system-actions-enabled");
            initParamElement.appendChild(paramNameElement);
            // param-value
            Element paramValueElement = docXml.createElement("param-value");
            paramValueElement.setTextContent("true");
            initParamElement.appendChild(paramValueElement);
            // } init-param
            filterElement.appendChild(initParamElement);
            // init-param 2{
            Element initParam2Element = docXml.createElement("init-param");
            // description 2
            Element description2Element = docXml.createElement("description");
            description2Element
                    .setTextContent("A regular expression to exclude some urls from monitoring.");
            initParam2Element.appendChild(description2Element);
            // param-name 2
            Element paramName2Element = docXml.createElement("param-name");
            paramName2Element.setTextContent("url-exclude-pattern");
            initParam2Element.appendChild(paramName2Element);
            // param-value 2
            Element paramValue2Element = docXml.createElement("param-value");
            paramValue2Element.setTextContent("/resources/.*");
            initParam2Element.appendChild(paramValue2Element);
            // } init-param 2
            filterElement.appendChild(initParam2Element);

            // if there's path to add
            if (pathString != null) {
                // init-param 3 {
                Element initParam3Element = docXml.createElement("init-param");
                // param-name 3
                Element paramName3Element = docXml.createElement("param-name");
                paramName3Element.setTextContent("storage-directory");
                initParam3Element.appendChild(paramName3Element);
                // param-value 3
                Element paramValue3Element = docXml
                        .createElement("param-value");
                paramValue3Element.setTextContent(pathString);
                initParam3Element.appendChild(paramValue3Element);
                // } init-param 3
                filterElement.appendChild(initParam3Element);
            }

            // Creating filter-mapping element
            Element filterMappingElement = docXml
                    .createElement("filter-mapping");
            // filter-name 2
            Element filterName2Element = docXml.createElement("filter-name");
            filterName2Element.setTextContent("monitoring");
            filterMappingElement.appendChild(filterName2Element);
            // url-pattern
            Element urlPatternElement = docXml.createElement("url-pattern");
            urlPatternElement.setTextContent("/*");
            filterMappingElement.appendChild(urlPatternElement);

            // Creating listener
            Element listenerElement = docXml.createElement("listener");
            // listener-class
            Element listenerClassElement = docXml
                    .createElement("listener-class");
            listenerClassElement
                    .setTextContent("net.bull.javamelody.SessionListener");
            listenerElement.appendChild(listenerClassElement);

            // Adding elements
            docRoot.appendChild(filterElement);
            docRoot.appendChild(filterMappingElement);
            docRoot.appendChild(listenerElement);

            fileManager.createOrUpdateTextFileIfRequired(webPath,
                    XmlUtils.nodeToString(docXml), true);
        }

    }

    /**
     * This method adds pom.xml dependencies
     */
    public void addPomDependencies() {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        // Install dependencies defined in external XML file
        for (Element dependencyElement : XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency",
                XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }

        // Add all new dependencies to pom.xml
        projectOperations.addDependencies("", dependencies);

    }

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    public LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(projectOperations);
    }

}
