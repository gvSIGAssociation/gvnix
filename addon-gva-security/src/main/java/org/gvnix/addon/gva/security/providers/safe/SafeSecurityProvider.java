package org.gvnix.addon.gva.security.providers.safe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.gva.security.providers.SecurityProvider;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.project.Resource;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <b>SAFE</b> Security Provider
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Juan Carlos García ( jcgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */

@Component
@Service
public class SafeSecurityProvider implements SecurityProvider {

    private static final String SAFE_CERTIFICATE_JS = "scripts/safe/safe_certificate.js";

    private static final String LOGIN_VIEW_PATH = "WEB-INF/views/login.jspx";

    private static final String POR_APLICACION_VALUE = "${security.SAFE.autorizacion.poraplicacion}";

    private static final String POR_APLICACION = "filtrarPorAplicacion";

    private static final String AUTORIZACION_PATH = "AutorizacionHDFI_v1_00.xml";

    private static final String AUTENTICACION_PATH = "AutenticacionArangi_v1_00.xml";

    private static final String SAFE_CLIENT_ROLES_PROPERTIES = "safe_client_roles.properties";

    private static final String ACTIVE_VALUE = "${security.SAFE.active}";

    private static final String ACTIVE = "active";

    private static final String ENDPOINT_AUTORIZA_VALUE = "${wsdl.SAFEAutorizacion.location}";

    private static final String ENDPOINT_AUTORIZA = "endpointAutoriza";

    private static final String ALIAS = "alias";

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

    private static final String SAFE_BEAN_CLASS = ".SafeProvider";

    private static final String SAFE_AUTHENTICATION_FILTER = ".SafeAuthenticationFilter";

    private static final String ID = "id";

    private static final String CLASS = "class";

    private static final String BEANS_BEAN = "beans:bean";

    private static final String REF_ATTR_VALUE = SAFE_BEAN_ID;

    private static final String REF_ATTR = REF;

    private static final String AUTH_PROVIDER_TAG = "authentication-provider";

    private static final String NAME_ATTR_VALUE = "authenticationManager";

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
    private TypeLocationService typeLocationService;

    @Reference
    private TilesOperations tilesOperations;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private MetadataService metadataService;

    @Reference
    private I18nSupport i18nSupport;

    @Reference
    private PropFileOperations propFileOperations;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void install(JavaPackage targetPackage) {
        // Adding POM dependencies
        addPomDependencies();
        // Generating java resources with annotations
        if (!fileExists("PasswordHandler", targetPackage)) {
            generatePasswordHandler(targetPackage);
        }
        if (!fileExists("SafeUser", targetPackage)) {
            generateSafeUser(targetPackage);
        }
        if (!fileExists("SafeUserAuthority", targetPackage)) {
            generateSafeUserAuthority(targetPackage);
        }
        if (!fileExists("SafeProvider", targetPackage)) {
            generateSafeProvider(targetPackage);
        }
        if (!fileExists("SafeAuthenticationFilter", targetPackage)) {
            generateSafeAuthenticationFilter(targetPackage);
        }
        if (!fileExists("SafeLoginController", targetPackage)) {
            generateSafeLoginController(targetPackage);
        }
        // Copying properties file
        copySafeClientPropertiesFile();
        // Copying wsdl files
        copySafeWSDLFiles();
        // Copying safe_certificate.js
        copySafeCertificateJS();
        // Adding safe_certificate.js to load scripts
        addToLoadScripts("safe_certificate",
                "/resources/scripts/safe/safe_certificate.js");
        // Modifying Application Context
        modifyApplicationContext();
        // Modifying webmvc-config.xml to get Context
        modifyWebMvcConfig();
        // Modifying Application Context Security
        modifyApplicationContextSecurity(targetPackage);
        // Modifying login.jspx
        modifyLoginView();
        // Adding messages
        addI18nProperties();
        // Showing next steps
        showNextSteps();

    }

    @Override
    public Boolean isInstalled() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean fileExists(String fileName, JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format("%s.".concat(fileName),
                targetPackage.getFullyQualifiedPackageName()));

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));

        return targetFile.exists();
    }

    /**
     * This method adds the necessary dependencies to the pom project file and
     * the necessary properties to run SAFE correctly
     * 
     */
    public void addPomDependencies() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());
        Element buildConfiguration = XmlUtils.getRootElement(getClass(),
                "buildconfiguration.xml");

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

        // Install Plugins
        List<Element> plugins = XmlUtils
                .findElements("/configuration/gvnix/build/plugins/plugin",
                        buildConfiguration);

        for (Element plugin : plugins) {
            projectOperations.addBuildPlugin(
                    projectOperations.getFocusedModuleName(),
                    new Plugin(plugin));
        }

        // Install Resources
        List<Element> resources = XmlUtils.findElements(
                "/configuration/gvnix/build/resources/resource",
                buildConfiguration);

        for (Element resource : resources) {
            projectOperations.addResource(projectOperations
                    .getFocusedModuleName(), new Resource(resource));
        }

    }

    /**
     * This method generates the file PasswordHandler.java with the annotation
     * <code>@GvNIXPasswordHandlerSAFE</code>
     * 
     */
    public void generatePasswordHandler(JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format("%s.PasswordHandler",
                targetPackage.getFullyQualifiedPackageName()));

        Validate.notNull(entity, "Entity required");

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                entity);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, entity,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);

        // Add @GvNIXPasswordHandlerSAFE annotation
        AnnotationMetadataBuilder gvnixPasswordHandlerAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXPasswordHandlerSAFE.class));
        annotations.add(gvnixPasswordHandlerAnnotation);

        // Add Implements Type
        cidBuilder.addImplementsType(new JavaType(
                "javax.security.auth.callback.CallbackHandler"));

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    }

    /**
     * This method generates the file SafeUser.java with the annotation
     * <code>@GvNIXUserSAFE</code>
     * 
     */
    public void generateSafeUser(JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format("%s.SafeUser",
                targetPackage.getFullyQualifiedPackageName()));

        Validate.notNull(entity, "Entity required");

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                entity);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, entity,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                2);

        // Add @GvNIXUserSAFE annotation
        AnnotationMetadataBuilder gvnixUserSafeAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXUserSAFE.class));
        annotations.add(gvnixUserSafeAnnotation);

        // Add @RooJavaBean annotation
        AnnotationMetadataBuilder javaBeanAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.roo.addon.javabean.RooJavaBean"));
        annotations.add(javaBeanAnnotation);

        // Add Implements Type
        cidBuilder.addImplementsType(new JavaType(
                "org.springframework.security.core.userdetails.UserDetails"));
        cidBuilder.addImplementsType(new JavaType("java.io.Serializable"));

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    }

    /**
     * This method generates the file SafeUserAuthority.java with the annotation
     * <code>@GvNIXUserAuthoritySAFE</code>
     * 
     */
    public void generateSafeUserAuthority(JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format("%s.SafeUserAuthority",
                targetPackage.getFullyQualifiedPackageName()));

        Validate.notNull(entity, "Entity required");

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                entity);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, entity,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                2);

        // Add @GvNIXUserAuthoritySAFE annotation
        AnnotationMetadataBuilder gvnixUserAuthoritySafeAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXUserAuthoritySAFE.class));
        annotations.add(gvnixUserAuthoritySafeAnnotation);

        // Add @RooJavaBean annotation
        AnnotationMetadataBuilder javaBeanAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.roo.addon.javabean.RooJavaBean"));
        annotations.add(javaBeanAnnotation);

        // Add Implements Type
        cidBuilder.addImplementsType(new JavaType(
                "org.springframework.security.core.GrantedAuthority"));
        cidBuilder.addImplementsType(new JavaType("java.io.Serializable"));

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    }

    /**
     * This method generates the file SafeProvider.java with the annotation
     * <code>@GvNIXProviderSAFE</code>
     * 
     */
    public void generateSafeProvider(JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format("%s.SafeProvider",
                targetPackage.getFullyQualifiedPackageName()));

        Validate.notNull(entity, "Entity required");

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                entity);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, entity,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);

        // Add @GvNIXProviderSAFE annotation
        AnnotationMetadataBuilder gvnixProviderSafeAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXProviderSAFE.class));
        annotations.add(gvnixProviderSafeAnnotation);

        // Add Extends Type
        cidBuilder
                .addExtendsTypes(new JavaType(
                        "org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider"));

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    }

    /**
     * This method generates the file SafeAuthenticationFilter.java with the
     * annotation <code>@GvNIXAuthenticationFilter</code>
     * 
     */
    public void generateSafeAuthenticationFilter(JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format(
                "%s.SafeAuthenticationFilter",
                targetPackage.getFullyQualifiedPackageName()));

        Validate.notNull(entity, "Entity required");

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                entity);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, entity,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);

        // Add @GvNIXAuthenticationFilter annotation
        AnnotationMetadataBuilder gvnixAuthenticationFilterAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXSafeAuthenticationFilter.class));
        annotations.add(gvnixAuthenticationFilterAnnotation);

        // Add Extends Type
        cidBuilder
                .addExtendsTypes(new JavaType(
                        "org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter"));

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

    }

    /**
     * This method generates the file SafeLoginController.java with the
     * annotation <code>@GvNIXSafeLoginController</code>
     * 
     */
    public void generateSafeLoginController(JavaPackage targetPackage) {
        JavaType entity = new JavaType(String.format("%s.SafeLoginController",
                targetPackage.getFullyQualifiedPackageName()));

        Validate.notNull(entity, "Entity required");

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(entity,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));

        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                entity);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, entity,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Add @GvNIXSafeLoginController annotation
        AnnotationMetadataBuilder gvnixSafeLoginControllerAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXSafeLoginController.class));
        annotations.add(gvnixSafeLoginControllerAnnotation);

        // Add @Controller annotation
        AnnotationMetadataBuilder controllerAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.CONTROLLER);
        annotations.add(controllerAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

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

        final String dynamicPropertiesFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, SAFE_CLIENT_PROPERTIES);

        final String rolesPropertiesFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, SAFE_CLIENT_ROLES_PROPERTIES);

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

        if (!fileManager.exists(dynamicPropertiesFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        SAFE_CLIENT_PROPERTIES);
                outputStream = fileManager.createFile(dynamicPropertiesFile)
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
                        SAFE_CLIENT_ROLES_PROPERTIES);
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
     * This method copy safe certificate if necessary allowing user to login
     * using SAFE certificate
     * 
     */
    public void copySafeCertificateJS() {
        final String propertiesFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, SAFE_CERTIFICATE_JS);

        if (!fileManager.exists(propertiesFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        SAFE_CERTIFICATE_JS);
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

    }

    /**
     * This method modify login if exists and copy to project if not.
     * 
     */
    public void modifyLoginView() {
        final String propertiesFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, LOGIN_VIEW_PATH);

        String loginToUse = LOGIN_VIEW_PATH;

        // If bootstrap is not installed, we need to use another login page
        if (!projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-bootstrap")) {
            loginToUse = "WEB-INF/views/login_no_bootstrap.jspx";
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(), loginToUse);
            if (!fileManager.exists(propertiesFile)) {
                outputStream = fileManager.createFile(propertiesFile)
                        .getOutputStream();
            }
            else {
                outputStream = fileManager.updateFile(propertiesFile)
                        .getOutputStream();
            }
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

    /**
     * This method copy AutenticacionArangi_v1_00.xml and
     * AutorizacionHDFI_v1_00.xml to ${basedir}/src/main/resources/wsdl/safe
     * 
     * With this files we are going to generate JAVA classes to execute
     * WebService SAFE Methods
     */
    public void copySafeWSDLFiles() {
        final String autenticacionFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES,
                "wsdl/safe/".concat(AUTENTICACION_PATH));

        final String autorizacionFile = pathResolver
                .getFocusedIdentifier(Path.SRC_MAIN_RESOURCES,
                        "wsdl/safe/".concat(AUTORIZACION_PATH));

        if (!fileManager.exists(autenticacionFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        AUTENTICACION_PATH);
                outputStream = fileManager.createFile(autenticacionFile)
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

        if (!fileManager.exists(autorizacionFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        AUTORIZACION_PATH);
                outputStream = fileManager.createFile(autorizacionFile)
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
     * This method modifies the webmvc-config.xml file to configure the
     * properties files that SAFE use.
     * 
     */
    public void modifyWebMvcConfig() {

        final String applicationContextPath = pathResolver
                .getFocusedIdentifier(Path.SRC_MAIN_RESOURCES,
                        APPLICATION_CONTEXT);
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(applicationContextPath));
        final Element config = document.getDocumentElement();
        final Element placeHolderElement = DomUtils.findFirstElementByName(
                PLACEHOLDER_ELEMENT, config);

        final String webMvcConfigPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/spring/webmvc-config.xml");
        final Document webMvcDocument = XmlUtils.readXml(fileManager
                .getInputStream(webMvcConfigPath));
        final Element mvcConfig = webMvcDocument.getDocumentElement();
        final Element placeHolderElementMvc = DomUtils.findFirstElementByName(
                PLACEHOLDER_ELEMENT, mvcConfig);

        String currentLocation = placeHolderElement.getAttribute("location");
        if (placeHolderElementMvc != null) {
            placeHolderElementMvc.setAttribute("location", currentLocation);
        }
        else {
            Element beansTag = DomUtils.getChildElementByTagName(mvcConfig,
                    "mvc:view-controller");
            Node parent = beansTag.getParentNode();
            Element newPlaceHolderElement = webMvcDocument
                    .createElement(PLACEHOLDER_ELEMENT);
            newPlaceHolderElement.setAttribute("location", currentLocation);
            parent.appendChild(newPlaceHolderElement);
        }

        fileManager.createOrUpdateTextFileIfRequired(webMvcConfigPath,
                XmlUtils.nodeToString(webMvcDocument), false);
    }

    /**
     * This method modifies the applicationContext-security.xml file to
     * configure the authentication using SAFE.
     * 
     */
    public void modifyApplicationContextSecurity(JavaPackage targetPackage) {

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

        fileManager.createOrUpdateTextFileIfRequired(applicationContextPath,
                XmlUtils.nodeToString(document), false);

        // Updating http tag
        Element httpTag = DomUtils.getChildElementByTagName(config, "http");
        httpTag.setAttribute("entry-point-ref",
                "loginUrlAuthenticationEntryPoint");
        httpTag.setAttribute("auto-config", "false");

        NodeList formLoginTag = httpTag.getElementsByTagName("form-login");
        if (formLoginTag.getLength() > 0) {
            httpTag.removeChild(formLoginTag.item(0));
        }

        Element customFilterTag = document.createElement("custom-filter");
        customFilterTag.setAttribute("position", "FORM_LOGIN_FILTER");
        customFilterTag.setAttribute(REF, "authenticationFilter");

        httpTag.appendChild(customFilterTag);

        // Creating new authenticationManager

        Element newAuthenticationManager = document
                .createElement(AUTH_MANAGER_TAG);
        newAuthenticationManager.setAttribute(ALIAS, NAME_ATTR_VALUE);

        Element newAuthenticationProvider = document
                .createElement(AUTH_PROVIDER_TAG);
        newAuthenticationProvider.setAttribute(REF_ATTR, REF_ATTR_VALUE);

        newAuthenticationManager.appendChild(newAuthenticationProvider);

        parent.appendChild(newAuthenticationManager);

        // Adding Safe Beans and childs

        Element newSafeBeans = document.createElement(BEANS_BEAN);
        newSafeBeans.setAttribute(CLASS, targetPackage
                .getFullyQualifiedPackageName().concat(SAFE_BEAN_CLASS));
        newSafeBeans.setAttribute(ID, SAFE_BEAN_ID);

        Element endPointProperty = document.createElement(BEANS_PROPERTY);
        endPointProperty.setAttribute(NAME_PROPERTY, ENDPOINT);
        endPointProperty.setAttribute(VALUE_PROPERTY, SAFE_LOCATION);

        Element endPointAutorizaProperty = document
                .createElement(BEANS_PROPERTY);
        endPointAutorizaProperty.setAttribute(NAME_PROPERTY, ENDPOINT_AUTORIZA);
        endPointAutorizaProperty.setAttribute(VALUE_PROPERTY,
                ENDPOINT_AUTORIZA_VALUE);

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

        Element activeProperty = document.createElement(BEANS_PROPERTY);
        activeProperty.setAttribute(NAME_PROPERTY, ACTIVE);
        activeProperty.setAttribute(VALUE_PROPERTY, ACTIVE_VALUE);

        Element porAplicacionProperty = document.createElement(BEANS_PROPERTY);
        porAplicacionProperty.setAttribute(NAME_PROPERTY, POR_APLICACION);
        porAplicacionProperty
                .setAttribute(VALUE_PROPERTY, POR_APLICACION_VALUE);

        newSafeBeans.appendChild(endPointProperty);
        newSafeBeans.appendChild(endPointAutorizaProperty);
        newSafeBeans.appendChild(passwordEncoderProperty);
        newSafeBeans.appendChild(saltSourceProperty);
        newSafeBeans.appendChild(applicationIdProperty);
        newSafeBeans.appendChild(environmentProperty);
        newSafeBeans.appendChild(activeProperty);
        newSafeBeans.appendChild(porAplicacionProperty);

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

        // Adding Login Entry Point
        Element loginEntryUrlBean = document.createElement(BEANS_BEAN);
        loginEntryUrlBean
                .setAttribute(
                        CLASS,
                        "org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint");
        loginEntryUrlBean.setAttribute(ID, "loginUrlAuthenticationEntryPoint");

        Element loginFormUrlProperty = document.createElement(BEANS_PROPERTY);
        loginFormUrlProperty.setAttribute(NAME_PROPERTY, "loginFormUrl");
        loginFormUrlProperty.setAttribute(VALUE_PROPERTY, "/login");

        loginEntryUrlBean.appendChild(loginFormUrlProperty);
        parent.appendChild(loginEntryUrlBean);

        // Adding authenticationFilter
        Element authenticationFilterBean = document.createElement(BEANS_BEAN);
        authenticationFilterBean.setAttribute(
                CLASS,
                targetPackage.getFullyQualifiedPackageName().concat(
                        SAFE_AUTHENTICATION_FILTER));
        authenticationFilterBean.setAttribute(ID, "authenticationFilter");

        Element authenticationManagerProperty = document
                .createElement(BEANS_PROPERTY);
        authenticationManagerProperty.setAttribute(NAME_PROPERTY,
                "authenticationManager");
        authenticationManagerProperty
                .setAttribute(REF, "authenticationManager");

        Element authenticationFailureProperty = document
                .createElement(BEANS_PROPERTY);
        authenticationFailureProperty.setAttribute(NAME_PROPERTY,
                "authenticationFailureHandler");
        authenticationFailureProperty.setAttribute(REF, "failureHandler");

        Element authenticationSuccesProperty = document
                .createElement(BEANS_PROPERTY);
        authenticationSuccesProperty.setAttribute(NAME_PROPERTY,
                "authenticationSuccessHandler");
        authenticationSuccesProperty.setAttribute(REF, "successHandler");

        authenticationFilterBean.appendChild(authenticationManagerProperty);
        authenticationFilterBean.appendChild(authenticationFailureProperty);
        authenticationFilterBean.appendChild(authenticationSuccesProperty);

        parent.appendChild(authenticationFilterBean);

        // Adding success handler
        Element successHandlerBean = document.createElement(BEANS_BEAN);
        successHandlerBean
                .setAttribute(
                        CLASS,
                        "org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler");
        successHandlerBean.setAttribute(ID, "successHandler");

        Element defaultTargetProperty = document.createElement(BEANS_PROPERTY);
        defaultTargetProperty.setAttribute(NAME_PROPERTY, "defaultTargetUrl");
        defaultTargetProperty.setAttribute(VALUE_PROPERTY, "/login");

        successHandlerBean.appendChild(defaultTargetProperty);
        parent.appendChild(successHandlerBean);

        // Adding failure handler
        Element failureHandlerBean = document.createElement(BEANS_BEAN);
        failureHandlerBean
                .setAttribute(
                        CLASS,
                        "org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler");
        failureHandlerBean.setAttribute(ID, "failureHandler");

        Element defaultFailureProperty = document.createElement(BEANS_PROPERTY);
        defaultFailureProperty.setAttribute(NAME_PROPERTY, "defaultFailureUrl");
        defaultFailureProperty.setAttribute(VALUE_PROPERTY,
                "/login?login_error=t");

        failureHandlerBean.appendChild(defaultFailureProperty);
        parent.appendChild(failureHandlerBean);

        parent.normalize();

        fileManager.createOrUpdateTextFileIfRequired(applicationContextPath,
                XmlUtils.nodeToString(document), false);
    }

    /**
     * This method adds reference in laod-script.tagx to use safe_certificate.js
     */
    public void addToLoadScripts(String varName, String url) {
        // Modify Roo load-scripts.tagx
        String docTagxPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/util/load-scripts.tagx");

        Validate.isTrue(fileManager.exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = fileManager.updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        // Add safe_certificate.js
        modified = WebProjectUtils.addJSToTag(docTagx, root, varName, url)
                || modified;

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }

    }

    /**
     * This method add necessary properties to messages.properties
     */
    public void addI18nProperties() {
        // Check if Valencian_Catalan language is supported and add properties
        // if so
        Set<I18n> supportedLanguages = i18nSupport.getSupportedLanguages();
        for (I18n i18n : supportedLanguages) {
            if (i18n.getLocale().equals(new Locale("ca"))) {
                MessageBundleUtils.installI18nMessages(
                        new ValencianCatalanLanguage(), projectOperations,
                        fileManager);
                MessageBundleUtils.addPropertiesToMessageBundle("ca",
                        getClass(), propFileOperations, projectOperations,
                        fileManager);
                break;
            }
        }
        // Add properties to Spanish messageBundle
        MessageBundleUtils.installI18nMessages(new SpanishLanguage(),
                projectOperations, fileManager);
        MessageBundleUtils.addPropertiesToMessageBundle("es", getClass(),
                propFileOperations, projectOperations, fileManager);

        // Add properties to default messageBundle
        MessageBundleUtils.addPropertiesToMessageBundle("en", getClass(),
                propFileOperations, projectOperations, fileManager);
    }

    /**
     * This method shows the next steps to configure the application correctly
     * to use this provider
     * 
     */
    public void showNextSteps() {
        log.log(Level.INFO, "");
        log.log(Level.INFO, "");
        log.log(Level.INFO,
                "*** Before execute your application you must to configure the follow"
                        + " SAFE Client Properties:");
        log.log(Level.INFO,
                "--------------------------------------------------------------------");
        log.log(Level.INFO,
                "    - security.SAFE.appId  (** Application SAFE ID )");
        log.log(Level.INFO, "    - security.SAFE.environment");
        log.log(Level.INFO, "    - security.SAFE.alias.password");
        log.log(Level.INFO,
                "    - security.SAFE.keystore.alias  (** Certificate Alias to sign SAFE request)");
        log.log(Level.INFO, "    - security.SAFE.keystore.file");
        log.log(Level.INFO, "    - security.SAFE.keystore.password");
        log.log(Level.INFO, "    - security.SAFE.keystore.type.keystore");
        log.log(Level.INFO,
                "    - security.SAFE.mapRoles  (** Map Roles from your application. Default: true)");
        log.log(Level.INFO,
                "    - security.SAFE.active  (** Active or deactivate SAFE. Default: true)");
        log.log(Level.INFO,
                "    - security.SAFE.autorizacion.poraplicacion  (** Get Authorization Roles Filtering by applicationId. Default: true)");
        log.log(Level.INFO,
                "    - security.SAFE.applet.location  (** URL del applet de SAFE para acceso con certificado (Ej: 'https://pretramitaext.gva.es/SAFE/applet'))");
        log.log(Level.INFO,
                "    - wsdl.SAFE.location  (** Authentication endpoint URL)");
        log.log(Level.INFO,
                "    - wsdl.SAFEAutorizacion.location  (** Authorization endpoint URL)");
        log.log(Level.INFO, "");
        log.log(Level.INFO,
                "*** Use the configuration commands to set this parameters (configuration property add --name XXX)");
        log.log(Level.INFO, "");
        log.log(Level.INFO, "");
    }

    private LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(projectOperations);
    }

}
