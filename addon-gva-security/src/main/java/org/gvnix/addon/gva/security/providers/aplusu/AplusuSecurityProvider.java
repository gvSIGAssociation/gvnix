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
package org.gvnix.addon.gva.security.providers.aplusu;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.gva.security.providers.SecurityProvider;
import org.springframework.roo.addon.security.SecurityOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <b>APLUSU</b> Security Provider
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
public class AplusuSecurityProvider implements SecurityProvider {

    private static final String DESCRIPTION = "Security APLUSU Provider";

    private static final String NAME = "APLUSU";

    /**
     * Installed classes package
     */

    public String aplusuPackage = ".security.authentication.aplusu";

    static final Integer DEFAUL_SESSION_TIMEOUT = 45;

    /**
     * Spring Security authentication provider to use
     */
    private static final String PROVIDER_CLASS_FILENAME = "AplusuSecurityProvider.java";

    /**
     * Classes with Spring Security integration
     */
    private static final String[] JAVA_CLASS_FILENAMES = new String[] {
            PROVIDER_CLASS_FILENAME, "AplusuSecurityProvider.java",
            "AplusuUserAuthority.java", "AplusuUser.java", };

    /**
     * Classes with Web-service client
     */
    private static final String[] JAVA_WS_CLASS_FILENAMES = new String[] {
            "ServerWSAuthBindingStub.java", "ServerWSAuthPort.java",
            "ServerWSAuthPortProxy.java", "ServerWSAuthService.java",
            "ServerWSAuthServiceLocator.java" };

    /**
     * Classes with service data structures
     */
    private static final String[] JAVA_XSD_CLASS_FILENAMES = new String[] {
            "CredencialAplusu.java", "ModuloStruct.java", "ValidaStruct.java",
            "StructUtil.java" };

    /**
     * Test classes
     */
    private static final String[] JAVA_TEST_CLASS_FILENAMES = new String[] {
            "ServerWSAuthBindingStubTest.java",
            "AplusuSecurityProviderTest.java" };

    private static final String WSAUTH_PROPERTIES_NAME = "aplusu.properties";

    private static final Dependency AXIS_DEPENDENCY = new Dependency("axis",
            "axis", "1.4");

    private static final Dependency MOCK_DEPENDENCY = new Dependency(
            "org.mockito", "mockito-all", "1.9.5", DependencyType.JAR,
            DependencyScope.TEST);

    private static Logger logger = Logger
            .getLogger(AplusuSecurityProvider.class.getName());

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private MetadataService metadataService;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private SecurityOperations securityOperations;

    /**
     * Get java package of APLUSU security classes into destination project.
     * 
     * @return APLUSU security classes java package
     */
    protected String getClassesPackage() {
        return aplusuPackage;
    }

    /**
     * Get folder path of APLUSU security files into destination project.
     * 
     * @return APLUSU security files folder path
     */
    protected String getClassesPath() {

        return getClassesPackage().replace(".", File.separator);
    }

    /**
     * Get file path of APLUSU security provider file into destination project.
     * 
     * @return APLUSU security provider file path
     */
    protected String getProviderTargetClassFileName() {
        return getClassesPath() + File.separator + PROVIDER_CLASS_FILENAME;
    }

    public boolean isSetupAvailable() {
        // Si no esta configurada la seguriad pero se puede configurar
        // ya lo haremos nosotros
        if (securityOperations.isSecurityInstallationPossible()) {
            return true;
        }

        // Si no se puede configurar la seguridad comprobamos y no esta
        // configurada
        // no estara disponible el comando
        String appSecurityXMLPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SPRING_CONFIG_ROOT, ""),
                "applicationContext-security.xml");
        if (!fileManager.exists(appSecurityXMLPath)) {
            return false;
        }

        // Esta configurada la seguridad, pero si ya esta ejecutado
        // este comando no estamos disponibles

        return !checkIsAlredyInstalled();
    }

    /**
     * Check if services is already installed on target project
     * 
     * @return
     */
    public boolean checkIsAlredyInstalled() {
        // Si no existe la ruta de nuestas clases no estan instaladas: estamos
        // disponibles
        String folderPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                getClassesPath());
        if (!fileManager.exists(folderPath)) {
            return false;
        }

        // si no existe la clase provider no estamos instalados: estamos
        // disonible
        String filePath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                getProviderTargetClassFileName());
        return fileManager.exists(filePath);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Do not permit ad reports unless they have a web project with Spring MVC
     * Tiles.
     */
    public boolean isSpringMvcTilesProject() {
        return fileManager.exists(getSpringMvcConfigFile())
                && fileManager.exists(getTilesLayoutsFile());
    }

    /**
     * Get the absolute path for {@code webmvc-config.xml}.
     * 
     * @return the absolute path to file (never null)
     */
    private String getSpringMvcConfigFile() {
        // resolve path for webmvc-config.xml if it hasn't been resolved yet
        return projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/spring/webmvc-config.xml");
    }

    /**
     * Get the absolute path for {@code layouts.xml}.
     * <p>
     * Note that this file is required for any Tiles project.
     * 
     * @return the absolute path to file (never null)
     */
    private String getTilesLayoutsFile() {
        // resolve absolute path for layouts.xml if it hasn't been resolved yet
        return projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "/WEB-INF/layouts/layouts.xml");
    }

    public void setup(String url, String login, String password, String appName) {
        // Si no esta configurada la seguriad pero se puede configurar
        // ya lo haremos nosotros
        if (securityOperations.isSecurityInstallationPossible()) {
            securityOperations.installSecurity();
            // Forced write of web.xml modified by previous command because we
            // need to modify it too in following lines
            fileManager.commit();

            /*
             * TODO: Hay un bug en ROO-1.1.2-RELEASE que cuando crea login.jspx
             * el path del action del formulario apunta a
             * /resources/j_spring_security_check/ en lugar de a
             * /static/j_spring_security_check. De momento modificamos
             * login.jspx a mano. Redmine #4886. Relacionado con
             * https://jira.springsource.org/browse/ROO-2173
             */
        }

        // Copiamos las clases necesarias para el servicio
        copyJavaFiles();

        // Copiar los archivos de configuracion que necesitamos
        copyConfigFiles(url, login, password, appName);

        // Actualizamos la configuracion de seguriada
        updateSecurityConfig();

        // Añadimos dependencias
        addDependencies();

    }

    /**
     * Add all dependencies needed on target project (if they aren't installed
     * yet)
     */
    private void addDependencies() {
        if (!isInstalled(AXIS_DEPENDENCY)) {
            projectOperations.addDependency(
                    projectOperations.getFocusedModuleName(), AXIS_DEPENDENCY);
        }
        if (!isInstalled(MOCK_DEPENDENCY)) {
            projectOperations.addDependency(
                    projectOperations.getFocusedModuleName(), MOCK_DEPENDENCY);
        }
    }

    /**
     * Check if a dependency is already installed on tarject project
     * 
     * @param dependency
     * @return
     */
    private boolean isInstalled(Dependency dependency) {
        Set<Dependency> dependencies = projectOperations.getFocusedModule()
                .getDependenciesExcludingVersion(dependency);

        if (dependencies.isEmpty()) {
            return false;
        }
        Validate.isTrue(dependencies.size() == 1,
                "Library dependecy: ".concat(dependency.toString()));
        Dependency current = dependencies.iterator().next();
        String[] currentVersion = current.getVersion().split("[.]");
        if (currentVersion.length > 1) {
            String[] requiredVersion = dependency.getVersion().split("[.]");
            int cur, req;
            for (int i = 0; i < currentVersion.length
                    && i < requiredVersion.length; i++) {
                try {
                    cur = Integer.parseInt(currentVersion[i]);
                    req = Integer.parseInt(requiredVersion[i]);
                }
                catch (NumberFormatException e) {
                    return false;
                }
                if (cur > req) {
                    // La dependencia actual una versión más nueva:
                    // no cambiamos
                    return true;
                }
            }
            // la version es inferior instalamos
            return false;
        }
        else {
            // no sabemos la versión asumimos que es buena
            return true;
        }

    }

    /**
     * Copia los archivos de configuración própios de nuestra herramienta
     * 
     * @param url
     * @param login
     * @param password
     * @param appName
     */
    private void copyConfigFiles(String url, String login, String password,
            String appName) {

        String properties = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SPRING_CONFIG_ROOT, ""),
                WSAUTH_PROPERTIES_NAME);
        if (fileManager.exists(properties)) {
            return;
        }
        InputStream templateInputStream = null;
        try {
            templateInputStream = FileUtils.getInputStream(getClass(),
                    WSAUTH_PROPERTIES_NAME);
            String template = IOUtils.toString(new InputStreamReader(
                    templateInputStream));
            template = StringUtils.replace(template, "__URL__", url);
            template = StringUtils.replace(template, "__LOGIN__", login);
            template = StringUtils.replace(template, "__PASSWORD__", password);
            template = StringUtils.replace(template, "__APPNAME__", appName);

            InputStream inputStream = null;
            OutputStreamWriter outputStream = null;
            try {
                inputStream = IOUtils.toInputStream(template);
                outputStream = new OutputStreamWriter(fileManager.createFile(
                        properties).getOutputStream());
                IOUtils.copy(inputStream, outputStream);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }

        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to create '"
                    + WSAUTH_PROPERTIES_NAME + "'", e);
        }
        finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                }
                catch (IOException e) {
                    logger.throwing(getClass().getName(), "copyConfigFiles", e);
                }
            }
        }

    }

    private void updateSecurityConfig() {

        String secTemplate = "applicationContext-security-template.xml";
        String secXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SPRING_CONFIG_ROOT, ""),
                "applicationContext-security.xml");

        Document secXmlDoc;
        MutableFile mutableFile;
        if (fileManager.exists(secXmlPath)) {
            // File exists, update file
            mutableFile = fileManager.updateFile(secXmlPath);
        }
        else {
            // Create file
            mutableFile = fileManager.createFile(secXmlPath);
        }

        InputStream templateInputStream = FileUtils.getInputStream(getClass(),
                secTemplate);
        try {
            secXmlDoc = XmlUtils.getDocumentBuilder()
                    .parse(templateInputStream);

            Element root = secXmlDoc.getDocumentElement();
            Element bean = XmlUtils.findFirstElement(
                    "/beans/bean[@id='aplusuSecurityProvider']", root);
            String clazz = bean.getAttribute("class");
            bean.setAttribute("class",
                    clazz.replace("__TARGET_PACKAGE__", getClassesPackage()));

            bean = XmlUtils.findFirstElement(
                    "/beans/bean[@id='serverWSAuthPortProxy']", root);
            clazz = bean.getAttribute("class");
            bean.setAttribute("class",
                    clazz.replace("__TARGET_PACKAGE__", getClassesPackage()));

        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        // Write file
        XmlUtils.writeXml(mutableFile.getOutputStream(), secXmlDoc);

        updateSessionTimeout(DEFAUL_SESSION_TIMEOUT);

        fileManager.scan();
    }

    /**
     * Set session-timeout with value given as parameter
     * 
     * @param sessionTimeout
     */
    private void updateSessionTimeout(Integer sessionTimeout) {
        Validate.notNull(sessionTimeout, "Session timeout must not be null");

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/web.xml");

        Document webXmlDoc;
        MutableFile mutableFile;
        try {
            if (fileManager.exists(webXmlPath)) {
                // File exists, update file
                mutableFile = fileManager.updateFile(webXmlPath);
                webXmlDoc = XmlUtils.getDocumentBuilder().parse(
                        mutableFile.getInputStream());
            }
            else {
                throw new IllegalStateException(
                        "Could not acquire ".concat(webXmlPath));
            }
            Element root = webXmlDoc.getDocumentElement();
            Element sessionTimeoutElement = XmlUtils.findFirstElement(
                    "/web-app/session-config/session-timeout", root);
            sessionTimeoutElement
                    .setTextContent(String.valueOf(sessionTimeout));
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Write file
        XmlUtils.writeXml(mutableFile.getOutputStream(), webXmlDoc);

    }

    private void copyJavaFiles() {

        String prjId = ProjectMetadata.getProjectIdentifier(projectOperations
                .getFocusedModuleName());
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(prjId);

        // Copiamos los ficheros del cliente del servicio WSAuth
        for (String className : JAVA_WS_CLASS_FILENAMES) {
            installTemplate("java-src-templates", className,
                    getClassesPackage(), projectMetadata, null, false, false);
        }

        // Copiamos los ficheros del Provider, usuarios y el cliente del
        // servicio WSAuth
        for (String className : JAVA_CLASS_FILENAMES) {
            installTemplate("java-src-templates", className,
                    getClassesPackage(), projectMetadata, null, false, false);
        }

        // Copiamos los ficheros de los xsd del servicio WSAuth
        for (String className : JAVA_XSD_CLASS_FILENAMES) {
            installTemplate("java-src-templates", className,
                    getClassesPackage(), projectMetadata, null, false, false);
        }

        // Copiamos los ficheros de los test
        for (String className : JAVA_TEST_CLASS_FILENAMES) {
            installTemplate("java-src-templates", className,
                    getClassesPackage(), projectMetadata, null, false, true);
        }
    }

    /***
     * <p>
     * Método de utilida que genera un fichero <code>targetFilename</code>
     * basado en un <i>template</i>.
     * </p>
     * <p>
     * Los <i>template</i> se buscan en el paquete de la clase actual compuestos
     * por <code>targetFilename</code> con la extensión terminada en
     * <code>-template</code>.
     * </p>
     * <p>
     * Antes de escribir el fichero destino, se reemplazan las siguientes
     * cadenas:
     * </p>
     * <ul>
     * <li><code>__TOP_LEVEL_PACKAGE__</code> --> Paquete raíz de la aplicación</li>
     * <li><code>__TARGET_PACKAGE__</code> --> Paquete de destino del fichero
     * especificado en <code>targetPackage</code></li>
     * <li>Por cada elemento de <code>parameters</code>: <code>__</code>
     * <i>clave</i><code>__</code> --> <i>valor<i></li>
     * </ul>
     * 
     * @param sourceFolder path relativo a esta clase para buscar el template,
     *        si es null usa la la ruta de la clase actual
     * @param targetFilename nombre del fichero final
     * @param targetPackage paquete donde se generará el fichero (admite '~'
     *        como comodín del paquete base)
     * @param projectMetadata metadatos del proyecto
     * @param parameters valores adicionales a reemplazar (puede ser
     *        <code>null</code> si no se necesita)
     * @param override especifica si sobreescribir el archivo si ya existe
     * @param isTest especifica si la clase es de test o del fuente
     */
    private void installTemplate(String sourceFolder, String targetFilename,
            String targetPackage, ProjectMetadata projectMetadata,
            Map<String, String> parameters, boolean override, boolean isTest) {
        // default package
        String packagePath = projectOperations
                .getTopLevelPackage(projectOperations.getFocusedModuleName())
                .getFullyQualifiedPackageName().replace('.', '/');

        // setting targetPackage change default package
        String finalTargetPackage = null;
        if (targetPackage == null) {
            finalTargetPackage = getClass().getPackage().getName();
        }
        else {
            if (targetPackage.startsWith("~")) {
                finalTargetPackage = targetPackage.replace(
                        "~",
                        projectOperations.getTopLevelPackage(
                                projectOperations.getFocusedModuleName())
                                .getFullyQualifiedPackageName());
            }
            else {
                finalTargetPackage = targetPackage;
            }
        }
        packagePath = finalTargetPackage.replace('.', '/');

        Path basePath = Path.SRC_MAIN_JAVA;
        if (isTest) {
            basePath = Path.SRC_TEST_JAVA;
        }

        String destinationFile = projectOperations.getPathResolver()
                .getIdentifier(LogicalPath.getInstance(basePath, ""),
                        packagePath + "/" + targetFilename);

        if ((!fileManager.exists(destinationFile)) || override) {
            InputStream templateInputStream;
            if (sourceFolder == null) {
                templateInputStream = FileUtils.getInputStream(getClass(),
                        targetFilename + "-template");
            }
            else {
                templateInputStream = FileUtils.getInputStream(getClass(),
                        sourceFolder + "/" + targetFilename + "-template");
            }
            try {
                // Read template and insert the user's package
                String input = IOUtils.toString(new InputStreamReader(
                        templateInputStream));
                input = input.replace("__TOP_LEVEL_PACKAGE__", aplusuPackage);

                input = input.replace("__TARGET_PACKAGE__", aplusuPackage);

                if (parameters != null) {
                    for (Entry<String, String> entry : parameters.entrySet()) {
                        input = input.replace("__" + entry.getKey() + "__",
                                entry.getValue());
                    }
                }

                // Output the file for the user
                MutableFile mutableFile = fileManager
                        .createFile(destinationFile);

                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(input);
                    outputStream = mutableFile.getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }

            }
            catch (IOException ioe) {
                throw new IllegalStateException("Unable to create '"
                        + targetFilename + "'", ioe);
            }
        }
    }

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
        // Changing default package with the developer package
        aplusuPackage = targetPackage.getFullyQualifiedPackageName();
        setup("", "", "", "");
        // Showing next steps to do
        showNextSteps();
    }

    @Override
    public Boolean isInstalled() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This method shows the next steps to configure the application correctly
     * to use this provider
     * 
     */
    public void showNextSteps() {
        logger.log(Level.INFO, "");
        logger.log(Level.INFO, "");
        logger.log(Level.INFO,
                "*** Before execute your application you must to configure the follow"
                        + " APLUSU Client Properties:");
        logger.log(Level.INFO,
                "--------------------------------------------------------------------");
        logger.log(Level.INFO, "    - wsauth.loggin");
        logger.log(Level.INFO, "    - wsauth.password");
        logger.log(Level.INFO, "    - wsauth.appName");
        logger.log(Level.INFO, "    - wsauth.url");
        logger.log(Level.INFO, "");
        logger.log(Level.INFO,
                "*** Use the configuration commands to set this parameters");
        logger.log(Level.INFO, "");
        logger.log(Level.INFO, "");
    }

}
