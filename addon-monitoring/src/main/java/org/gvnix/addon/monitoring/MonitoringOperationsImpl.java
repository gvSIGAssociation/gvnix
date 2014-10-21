package org.gvnix.addon.monitoring;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.*;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.model.*;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.logging.HandlerUtils;
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

    private static final Logger LOGGER = HandlerUtils
            .getLogger(MonitoringOperationsImpl.class);

    private static final JavaType SPRING_MONITORING_ANNOTATION = new JavaType(
            "net.bull.javamelody.MonitoredWithSpring");

    private static final JavaType ROO_JAVABEAN = new JavaType(
            "org.springframework.roo.addon.javabean.RooJavaBean");

    private static final JavaType SPRING_CONTROLLER = new JavaType(
            "org.springframework.stereotype.Controller");

    private static final JavaType SPRING_SERVICE = new JavaType(
            "org.springframework.stereotype.Service");

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

    public boolean isAddAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FEATURE_NAME_GVNIX_MONITORING);
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

    /**
     * Add all files to be monitored as a Spring service
     */
    @Override
    public void all() {
        addPackage(projectOperations.getFocusedTopLevelPackage());
    }

    /**
     * Add a path which all his child methods will be monitored as a Spring
     * service
     * 
     * @param path Set the package path to be monitored
     */
    @Override
    public void addPackage(JavaPackage path) {
        // Setting up monitoring annotations
        createMonitoringConfig();

        // Creating annotation
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                SPRING_MONITORING_ANNOTATION);

        // Getting all entity, controllers and services
        Set<ClassOrInterfaceTypeDetails> entities = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_JAVABEAN);

        Set<ClassOrInterfaceTypeDetails> controllers = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(SPRING_CONTROLLER);

        Set<ClassOrInterfaceTypeDetails> services = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(SPRING_SERVICE);

        // Annotating all entities if they exists
        if (entities != null) {
            Iterator<ClassOrInterfaceTypeDetails> it = entities.iterator();

            while (it.hasNext()) {
                ClassOrInterfaceTypeDetails entity = it.next();
                if (entity.getType().getPackage().isWithin(path)) {

                    // Generating new annotation
                    ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                            entity);

                    // Add annotation to target type
                    builder.updateTypeAnnotation(annotationBuilder.build());

                    // Save changes to disk
                    typeManagementService.createOrUpdateTypeOnDisk(builder
                            .build());
                }
            }
        }

        // Annotating all controllers if they exists
        if (controllers != null) {
            Iterator<ClassOrInterfaceTypeDetails> it = controllers.iterator();

            while (it.hasNext()) {
                ClassOrInterfaceTypeDetails controller = it.next();
                if (controller.getType().getPackage().isWithin(path)) {

                    // Generating new annotation
                    ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                            controller);

                    // Add annotation to target type
                    builder.updateTypeAnnotation(annotationBuilder.build());

                    // Save changes to disk
                    typeManagementService.createOrUpdateTypeOnDisk(builder
                            .build());
                }
            }
        }

        // Annotating all services if they exists
        if (services != null) {
            Iterator<ClassOrInterfaceTypeDetails> it = services.iterator();

            while (it.hasNext()) {
                ClassOrInterfaceTypeDetails service = it.next();
                if (service.getType().getPackage().isWithin(path)) {

                    // Generating new annotation
                    ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                            service);

                    // Add annotation to target type
                    builder.updateTypeAnnotation(annotationBuilder.build());

                    // Save changes to disk
                    typeManagementService.createOrUpdateTypeOnDisk(builder
                            .build());
                }
            }
        }
    }

    /**
     * Adds config data for Spring monitoring
     * 
     * @param appContextPath
     */
    public void createMonitoringConfig() {

        // Getting Application Context path
        String appContextPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES,
                "META-INF/spring/applicationContext.xml");
        // Getting Web Config Path
        String webPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/spring/webmvc-config.xml");

        // Building configuration for annotation support in both context if
        // needed
        createMonitoringAutoProxy(appContextPath);
        createMonitoringAdvisor(appContextPath);
        if (fileManager.exists(webPath)) { // If web mvc exists
            createMonitoringAutoProxy(webPath);
            createMonitoringAdvisor(webPath);
        }

    }

    /**
     * Adds an aop:config for Spring monitoring
     * 
     * @param appContextPath
     */
    public void createMonitoringAdvisor(String appContextPath) {
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
                    if (idAttr != null
                            && "monitoringAdvisor"
                                    .equals(idAttr.getNodeValue())) {

                        // Checking if exist
                        NodeList propertyList = bean.getChildNodes();

                        for (int j = 0; j < propertyList.getLength(); j++) {
                            Node property = propertyList.item(j);
                            NamedNodeMap propertyAttr = property
                                    .getAttributes();
                            if (propertyAttr != null) {
                                Node nameAttr = propertyAttr
                                        .getNamedItem("name");
                                // Checkin if property exists on current bean
                                if (nameAttr != null
                                        && "pointcut".equals(nameAttr
                                                .getNodeValue())) {

                                    // Checking if exist
                                    NodeList bean2List = property
                                            .getChildNodes();

                                    for (int k = 0; k < bean2List.getLength(); k++) {
                                        Node bean2 = bean2List.item(k);
                                        NamedNodeMap bean2Attr = bean2
                                                .getAttributes();
                                        if (bean2Attr != null) {
                                            Node classAttr = bean2Attr
                                                    .getNamedItem("class");
                                            // Checkin if bean exists on current
                                            // property
                                            if (classAttr != null
                                                    && "net.bull.javamelody.MonitoredWithAnnotationPointcut"
                                                            .equals(classAttr
                                                                    .getNodeValue())) {
                                                return;
                                            }
                                        }
                                    }

                                    // Creating new element (bean)
                                    Element bean2Element = docXml
                                            .createElement("bean");
                                    bean2Element
                                            .setAttribute("class",
                                                    "net.bull.javamelody.MonitoredWithAnnotationPointcut");

                                    property.appendChild(bean2Element);

                                    // Saving changes and exit
                                    fileManager
                                            .createOrUpdateTextFileIfRequired(
                                                    appContextPath,
                                                    XmlUtils.nodeToString(docXml),
                                                    true);
                                }
                            }
                        }

                        // Creating new element (property)
                        Element propertyElement = docXml
                                .createElement("property");
                        propertyElement.setAttribute("name", "pointcut");

                        // Creating new element (bean)
                        Element bean2Element = docXml.createElement("bean");
                        bean2Element
                                .setAttribute("class",
                                        "net.bull.javamelody.MonitoredWithAnnotationPointcut");

                        propertyElement.appendChild(bean2Element);
                        bean.appendChild(propertyElement);

                        // Saving changes and exit
                        fileManager.createOrUpdateTextFileIfRequired(
                                appContextPath, XmlUtils.nodeToString(docXml),
                                true);
                    }
                }
            }

            // Creating new element (bean)
            Element beanElement = docXml.createElement("bean");
            beanElement.setAttribute("id", "monitoringAdvisor");
            beanElement.setAttribute("class",
                    "net.bull.javamelody.MonitoringSpringAdvisor");

            // Creating new element (property)
            Element propertyElement = docXml.createElement("property");
            propertyElement.setAttribute("name", "pointcut");

            // Creating new element (bean)
            Element bean2Element = docXml.createElement("bean");
            bean2Element.setAttribute("class",
                    "net.bull.javamelody.MonitoredWithAnnotationPointcut");

            propertyElement.appendChild(bean2Element);
            beanElement.appendChild(propertyElement);
            docRoot.appendChild(beanElement);

            // Saving changes
            fileManager.createOrUpdateTextFileIfRequired(appContextPath,
                    XmlUtils.nodeToString(docXml), true);
        }
    }

    /**
     * Adds a bean for Spring monitoring
     * 
     * @param appContextPath
     */
    public void createMonitoringAutoProxy(String appContextPath) {
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
                    Node classAttr = beanAttr.getNamedItem("class");
                    // Checking if bean exists on current beans
                    if (classAttr != null
                            && "org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"
                                    .equals(classAttr.getNodeValue())) {
                        return;
                    }
                }
            }

            // Creating new element (bean)
            Element beanElement = docXml.createElement("bean");
            beanElement
                    .setAttribute("class",
                            "org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator");

            docRoot.appendChild(beanElement);

            // Saving changes
            fileManager.createOrUpdateTextFileIfRequired(appContextPath,
                    XmlUtils.nodeToString(docXml), true);
        }
    }

    /**
     * Add a class to be monitored as a Spring service
     * 
     * @param name Set the class name to be monitored
     */
    @Override
    public void addClass(JavaType name) {
        // Setting up monitoring annotations
        createMonitoringConfig();

        // Get java type controller
        ClassOrInterfaceTypeDetails controller = getControllerDetails(name);

        // Generating new annotation
        ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                controller);
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                SPRING_MONITORING_ANNOTATION);

        // Add annotation to target type
        builder.updateTypeAnnotation(annotationBuilder.build());

        // Save changes to disk
        typeManagementService.createOrUpdateTypeOnDisk(builder.build());

    }

    /**
     * Add a method to be monitored as a Spring service
     * 
     * @param methodName Set the method name to be monitored
     * @param className Set the class name of the method to be monitored
     */
    @Override
    public void addMethod(JavaSymbolName methodName, JavaType className) {
        // Setting up monitoring annotations
        createMonitoringConfig();

        // Get java type controller
        ClassOrInterfaceTypeDetails controller = getControllerDetails(className);

        ClassOrInterfaceTypeDetailsBuilder classBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                controller);

        List<MethodMetadata> methodList = (List<MethodMetadata>) controller
                .getDeclaredMethods();

        for (int i = 0; i < methodList.size(); i++) {
            MethodMetadata method = methodList.get(i);
            if (methodName.equals(method.getMethodName())) {
                MethodMetadataBuilder builder = new MethodMetadataBuilder(
                        method);

                // Generating new annotation
                AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                        SPRING_MONITORING_ANNOTATION);

                // Add annotation to target type
                builder.updateTypeAnnotation(annotationBuilder.build());

                // Save changes to disk
                typeManagementService.createOrUpdateTypeOnDisk(classBuilder
                        .build());

            }
        }
        LOGGER.log(
                Level.INFO,
                "[ERROR] This method doesn't exist for this class or maybe it's inside an .aj file. In this case you must to push-in that method and then execute this command again");
    }

    /**
     * This method annotates a class or method
     * 
     * @param controller
     */
    private void annotateThing(ClassOrInterfaceTypeDetails controller) {

    }

    /**
     * This method gets class details
     * 
     * @param controller
     * @return
     */
    private ClassOrInterfaceTypeDetails getControllerDetails(JavaType controller) {
        ClassOrInterfaceTypeDetails existing = typeLocationService
                .getTypeDetails(controller);

        Validate.notNull(existing, "Can't get Type details");
        return existing;
    }

    /*** Feature Methods ***/

    /**
     * Gets the feature name managed by this operations class.
     * 
     * @return feature name
     */
    @Override
    public String getName() {
        return FEATURE_NAME_GVNIX_MONITORING;
    }

    /**
     * Returns true if the given feature is installed in current project.
     * 
     * @param moduleName feature name to check in current project
     * @return true if given feature name is installed, otherwise returns false
     */
    @Override
    public boolean isInstalledInModule(String moduleName) {
        // If its installed provider is net.bull.javamelody.JpaPersistence
        String persistencePath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");

        if (fileManager.exists(persistencePath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(persistencePath,
                    fileManager);

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Getting provider
            NodeList allProviders = docRoot.getElementsByTagName("provider");

            // Check providers
            if (allProviders.getLength() > 0) {
                for (int i = 0; i < allProviders.getLength(); i++) {
                    Element provider = (Element) allProviders.item(i);
                    if (provider != null
                            && "net.bull.javamelody.JpaPersistence"
                                    .equals(provider.getTextContent())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
