package org.gvnix.gva.security.providers.safe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.gva.security.providers.SecurityProvider;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Component
@Service
public class SafeSecurityProvider implements SecurityProvider {

    private static final String LOCATION_VALUE = "classpath*:META-INF/spring/*.properties,classpath*:safe_client.properties";

    private static final String LOCATION = "location";

    private static final String PLACEHOLDER_ELEMENT = "context:property-placeholder";

    private static final String APPLICATION_CONTEXT = "META-INF/spring/applicationContext.xml";

    private static final String SAFE_CLIENT_PROPERTIES = "safe_client.properties";

    private static final String ENVIRONMENT_VALUE = "${security.SAFE.environment}";

    private static final String ENVIRONMENT = "environment";

    private static final String APPLICATION_ID_VALUE = "${security.SAFE.appId}";

    private static final String APPLICATION_ID = "applicationId";

    private static final String HTTP_TAG = "http";

    private static final String REF = "ref";

    private static final String USER_PROPERTY = "userPropertyToUse";

    private static final String NIF = "nif";

    private static final String SALT_SOURCE_CLASS = "org.springframework.security.authentication.dao.ReflectionSaltSource";

    private static final String PASS_ENCODER_CLASS = "org.springframework.security.authentication.encoding.PlaintextPasswordEncoder";

    private static final String SALT_SOURCE = "saltSource";

    private static final String PASSWORD_ENCODER = "passwordEncoder";

    private static final String SAFE_LOCATION = "${wsdl.SAFE.location}";

    private static final String ENDPOINT = "endpoint";

    private static final String VALUE_PROPERTY = "value";

    private static final String NAME_PROPERTY = "name";

    private static final String BEANS_PROPERTY = "beans:property";

    private static final String SAFE_BEAN_ID = "wsSafeProvider";

    private static final String SAFE_BEAN_CLASS = "~.security.authentication.wssafe.WsSafeProvider";

    private static final String ID = "id";

    private static final String CLASS = "class";

    private static final String BEANS_BEAN = "beans:bean";

    private static final String REF_ATTR_VALUE = SAFE_BEAN_ID;

    private static final String REF_ATTR = REF;

    private static final String AUTH_PROVIDER_TAG = "authentication-provider";

    private static final String NAME_ATTR_VALUE = "authenticationManager";

    private static final String NAME_ATTR = NAME_PROPERTY;

    private static final String AUTH_MANAGER_TAG = "authentication-manager";

    private static final String SECURITY_CONTEXT_PATH = "META-INF/spring/applicationContext-security.xml";

    private static final String SAFE_PROPERTIES_PATH = "safe_client_sign.properties";

    private Logger log = Logger.getLogger(getClass().getName());

    public static final String NAME = "SAFE";
    public static final String DESCRIPTION = "Security SAFE Provider";

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TilesOperations tilesOperations;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private MetadataService metadataService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void install() {
        // Adding POM dependencies
        addPomDependencies();
        // Copying properties file
        copySafeClientPropertiesFile();
        // Modifying Application Context
        modifyApplicationContext();
        // Modifying Application Context Security
        modifyApplicationContextSecurity();

    }

    @Override
    public Boolean isInstalled() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This method adds the necessary dependencies to the pom project file and
     * the necessary properties to run SAFE correctly
     * 
     */
    public void addPomDependencies() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepositories(
                    projectOperations.getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            projectOperations.addProperty(projectOperations
                    .getFocusedModuleName(), new Property(property));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);

       /* // Install Plugins
        List<Element> plugins = XmlUtils.findElements(
                "/configuration/gvnix/build/plugins", configuration);

        for (Element plugin : plugins) {
            projectOperations.addProperty(projectOperations
                    .getFocusedModuleName(), new Property(plugin));
        }

        // Install Resources
        List<Resource> resources = XmlUtils.findElements(
                "/configuration/gvnix/build/resources", configuration);

        for (Resource resource : resources) {
            projectOperations.addResource("", resource);
        }*/

    }

    /**
     * This method copy the file safe_client_sign.properties to the correct
     * location of the project.
     * 
     * This file contains, all the SAFE configuration and SAFE variables that
     * the system needs
     * 
     */
    public void copySafeClientPropertiesFile() {
        final String propertiesFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, SAFE_PROPERTIES_PATH);

        final String rolesPropertiesFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, SAFE_CLIENT_PROPERTIES);

        if (!fileManager.exists(propertiesFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        SAFE_PROPERTIES_PATH);
                outputStream = fileManager.createFile(propertiesFile)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        if (!fileManager.exists(rolesPropertiesFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        SAFE_CLIENT_PROPERTIES);
                outputStream = fileManager.createFile(rolesPropertiesFile)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    /**
     * This method modifies the applicationContext.xml file to configure the
     * properties files that SAFE use.
     * 
     */
    public void modifyApplicationContext() {

        final String applicationContextPath = pathResolver
                .getFocusedIdentifier(Path.SRC_MAIN_RESOURCES,
                        APPLICATION_CONTEXT);
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(applicationContextPath));
        final Element config = document.getDocumentElement();
        final Element placeHolderElement = DomUtils.findFirstElementByName(
                PLACEHOLDER_ELEMENT, config);
        final Node parent = placeHolderElement.getParentNode();

        placeHolderElement.setAttribute(LOCATION, LOCATION_VALUE);

        parent.normalize();

        fileManager.createOrUpdateTextFileIfRequired(applicationContextPath,
                XmlUtils.nodeToString(document), false);
    }

    /**
     * This method modifies the applicationContext-security.xml file to
     * configure the authentication using SAFE.
     * 
     */
    public void modifyApplicationContextSecurity() {

        final String applicationContextPath = pathResolver
                .getFocusedIdentifier(Path.SRC_MAIN_RESOURCES,
                        SECURITY_CONTEXT_PATH);
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(applicationContextPath));
        final Element config = document.getDocumentElement();
        final Element httpElement = DomUtils.findFirstElementByName(HTTP_TAG,
                config);
        final Node parent = httpElement.getParentNode();

        final Element authenticationManager = DomUtils.findFirstElementByName(
                AUTH_MANAGER_TAG, config);
        if (authenticationManager != null) {
            parent.removeChild(authenticationManager);
        }

        // Creating new authenticationManager

        Element newAuthenticationManager = document
                .createElement(AUTH_MANAGER_TAG);
        newAuthenticationManager.setAttribute(NAME_ATTR, NAME_ATTR_VALUE);

        Element newAuthenticationProvider = document
                .createElement(AUTH_PROVIDER_TAG);
        newAuthenticationProvider.setAttribute(REF_ATTR, REF_ATTR_VALUE);

        newAuthenticationManager.appendChild(newAuthenticationProvider);

        parent.appendChild(newAuthenticationManager);

        List<Element> beans = DomUtils.getChildElementsByTagName(config,
                BEANS_BEAN);

        Iterator<Element> it = beans.iterator();
        while (it.hasNext()) {
            Element child = it.next();
            if (SAFE_BEAN_ID.equals(child.getAttribute("id"))
                    || PASSWORD_ENCODER.equals(child.getAttribute("id"))
                    || SALT_SOURCE.equals(child.getAttribute("id"))) {
                parent.removeChild(child);
            }
        }

        // Adding Safe Beans and childs

        Element newSafeBeans = document.createElement(BEANS_BEAN);
        newSafeBeans.setAttribute(CLASS, SAFE_BEAN_CLASS);
        newSafeBeans.setAttribute(ID, SAFE_BEAN_ID);

        Element endPointProperty = document.createElement(BEANS_PROPERTY);
        endPointProperty.setAttribute(NAME_PROPERTY, ENDPOINT);
        endPointProperty.setAttribute(VALUE_PROPERTY, SAFE_LOCATION);

        Element passwordEncoderProperty = document
                .createElement(BEANS_PROPERTY);
        passwordEncoderProperty.setAttribute(NAME_PROPERTY, PASSWORD_ENCODER);
        passwordEncoderProperty.setAttribute(REF, PASSWORD_ENCODER);

        Element saltSourceProperty = document.createElement(BEANS_PROPERTY);
        saltSourceProperty.setAttribute(NAME_PROPERTY, SALT_SOURCE);
        saltSourceProperty.setAttribute(REF, SALT_SOURCE);

        Element applicationIdProperty = document.createElement(BEANS_PROPERTY);
        applicationIdProperty.setAttribute(NAME_PROPERTY, APPLICATION_ID);
        applicationIdProperty
                .setAttribute(VALUE_PROPERTY, APPLICATION_ID_VALUE);

        Element environmentProperty = document.createElement(BEANS_PROPERTY);
        environmentProperty.setAttribute(NAME_PROPERTY, ENVIRONMENT);
        environmentProperty.setAttribute(VALUE_PROPERTY, ENVIRONMENT_VALUE);

        newSafeBeans.appendChild(endPointProperty);
        newSafeBeans.appendChild(passwordEncoderProperty);
        newSafeBeans.appendChild(saltSourceProperty);
        newSafeBeans.appendChild(applicationIdProperty);
        newSafeBeans.appendChild(environmentProperty);

        parent.appendChild(newSafeBeans);

        // Adding Password encoder Bean

        Element newPasswordEncoderBean = document.createElement(BEANS_BEAN);
        newPasswordEncoderBean.setAttribute(CLASS, PASS_ENCODER_CLASS);
        newPasswordEncoderBean.setAttribute(ID, PASSWORD_ENCODER);

        parent.appendChild(newPasswordEncoderBean);

        // Adding SaltSource Bean

        Element newSaltSourceBean = document.createElement(BEANS_BEAN);
        newSaltSourceBean.setAttribute(CLASS, SALT_SOURCE_CLASS);
        newSaltSourceBean.setAttribute(ID, SALT_SOURCE);

        Element userProperty = document.createElement(BEANS_PROPERTY);
        userProperty.setAttribute(NAME_PROPERTY, USER_PROPERTY);
        userProperty.setAttribute(VALUE_PROPERTY, NIF);

        newSaltSourceBean.appendChild(userProperty);

        parent.appendChild(newSaltSourceBean);

        parent.normalize();

        fileManager.createOrUpdateTextFileIfRequired(applicationContextPath,
                XmlUtils.nodeToString(document), false);
    }

}
