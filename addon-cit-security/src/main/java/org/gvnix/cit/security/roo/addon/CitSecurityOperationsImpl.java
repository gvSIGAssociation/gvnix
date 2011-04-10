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
package org.gvnix.cit.security.roo.addon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.security.SecurityOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Clase que implementa las operaciones del add-on <b>cit securty</b>
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class CitSecurityOperationsImpl implements CitSecurityOperations {

  static final String CLASSES_PACKAGE = "org.gvnix.security.authentication.wscit";

  static final String PROVIDER_CLASS_SHORT_NAME = "WscitAuthenticationProvider";

  static final String CLASSES_PATH = CLASSES_PACKAGE.replace(".",
      File.separator);

  private static final String PROVIDER_CLASS_NAME = CLASSES_PACKAGE
      + "WscitAuthenticationProvider";

  private static final String PROVIDER_CLASS_FILENAME = PROVIDER_CLASS_SHORT_NAME
      + ".java";

  private static final String PROVIDER_TARGET_CLASS_FILENAME = CLASSES_PATH
      + PROVIDER_CLASS_FILENAME;

  private static final String[] JAVA_CLASS_FILENAMES = new String[] {
      PROVIDER_CLASS_FILENAME, "WscitAuthenticationProvider.java",
      "WscitUserAuthority.java", "WscitUser.java", };

  private static final String[] JAVA_WS_CLASS_FILENAMES = new String[] {
      "ServerWSAuthBindingStub.java", "ServerWSAuthPort.java",
      "ServerWSAuthPortProxy.java", "ServerWSAuthService.java",
      "ServerWSAuthServiceLocator.java" };

  private static final String WSAUTH_PROPERTIES_NAME = "CITWSAuth.properties";

  private static final String SECURITY_XML_TEMPLATE = "applicationContext-security-template.xml";

  private static final Dependency AXIS_DEPENDENCY = new Dependency("axis",
      "axis", "1.4");

  private static Logger logger = Logger
      .getLogger(CitSecurityOperationsImpl.class.getName());

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

  private Boolean alreadyInstaled = null;

  private ComponentContext context;

  protected void activate(ComponentContext context) {
    this.context = context;
  }

  public boolean isSetupAvailable() {
    // Si no esta configurada la seguriad pero se puede configurar
    // ya lo haremos nosotros
    if (securityOperations.isInstallSecurityAvailable()) {
      return true;
    }

    // Si no se puede configurar la seguridad comprobamos y no esta
    // configurada
    // no estara disponible el comando
    String appSecurityXMLPath = pathResolver.getIdentifier(
        Path.SPRING_CONFIG_ROOT, "applicationContext-security.xml");
    if (!fileManager.exists(appSecurityXMLPath)) {
      return false;
    }

    // Esta configurada la seguridad, pero si ya esta ejecutado
    // este comando no estamos disponibles

    return !isAlreadyInstalled();
  }

  public boolean isAlreadyInstalled() {
    if (alreadyInstaled == null) {
      alreadyInstaled = checkIsAlredyInstalled() ? Boolean.TRUE : Boolean.FALSE;
    }
    return alreadyInstaled.booleanValue();

  }

  public void clearAlreadyInstalled() {
    alreadyInstaled = null;
  }

  public boolean checkIsAlredyInstalled() {
    // Si no existe la ruta de nuestas clases no estan instaladas: estamos
    // disponibles
    String classPath = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
        CLASSES_PATH);
    if (!fileManager.exists(classPath)) {
      return false;
    }

    // si no existe la clase provider no estamos instalados: estamos
    // disonible
    return fileManager.exists(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
        PROVIDER_TARGET_CLASS_FILENAME));
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
        Path.SRC_MAIN_WEBAPP, "WEB-INF/spring/webmvc-config.xml");
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
        Path.SRC_MAIN_WEBAPP, "/WEB-INF/layouts/layouts.xml");
  }

  public void setup(String url, String login, String password, String appName) {
    // Si no esta configurada la seguriad pero se puede configurar
    // ya lo haremos nosotros
    if (securityOperations.isInstallSecurityAvailable()) {
      securityOperations.installSecurity();
      /*
       * TODO: Hay un bug en ROO-1.1.2-RELEASE que cuando crea login.jspx el path del action
       * del formulario apunta a /resources/j_spring_security_check/ en lugar de a
       * /static/j_spring_security_check. De momento modificamos login.jspx a mano.
       * Redmine #4886. Relacionado con https://jira.springsource.org/browse/ROO-2173
       */
      String loginJspxPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/views/login.jspx");

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

  private void addDependencies() {
    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
        .get(ProjectMetadata.getProjectIdentifier());

    Set<Dependency> dependencies = projectMetadata
        .getDependenciesExcludingVersion(AXIS_DEPENDENCY);

    if (!dependencies.isEmpty()) {
      Assert.isTrue(dependencies.size() == 1,
          "Duplicate AXIS library dependecy");
      Dependency current = dependencies.iterator().next();
      String[] currentVersion = current.getVersionId().split("[.]");
      if (currentVersion.length > 1) {
        String[] requiredVersion = AXIS_DEPENDENCY.getVersionId().split("[.]");
        int cur, req;
        for (int i = 0; i < currentVersion.length && i < requiredVersion.length; i++) {
          try {
            cur = Integer.parseInt(currentVersion[i]);
            req = Integer.parseInt(requiredVersion[i]);
          }
          catch (NumberFormatException e) {
            // Actualizamos la dependencia por si acaso.
            projectOperations.addDependency(AXIS_DEPENDENCY);
            return;
          }
          if (cur > req) {
            // La dependencia actual una versión más nueva:
            // no cambiamos
            return;
          }
        }

      }
      else {
        // no sabemos la versión asumimos que es buena
        return;
      }

    }
    projectOperations.addDependency(AXIS_DEPENDENCY);

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

    String properties = pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT,
        WSAUTH_PROPERTIES_NAME);
    if (fileManager.exists(properties)) {
      return;
    }
    InputStream templateInputStream = null;
    try {
      templateInputStream = TemplateUtils.getTemplate(getClass(), "config/"
          + WSAUTH_PROPERTIES_NAME);
      String template = FileCopyUtils.copyToString(new InputStreamReader(
          templateInputStream));
      template = StringUtils.replace(template, "__URL__", url);
      template = StringUtils.replace(template, "__LOGIN__", login);
      template = StringUtils.replace(template, "__PASSWORD__", password);
      template = StringUtils.replace(template, "__APPNAME__", appName);

      FileCopyUtils.copy(template, new OutputStreamWriter(fileManager
          .createFile(properties).getOutputStream()));
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

    String secTemplate = "config/applicationContext-security-template.xml";
    String secXmlPath = pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT,
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

    InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
        secTemplate);
    try {
      secXmlDoc = XmlUtils.getDocumentBuilder().parse(templateInputStream);

      Element root = secXmlDoc.getDocumentElement();
      Element bean = XmlUtils.findFirstElement(
          "/beans/bean[@id='wscitAuthenticationProvider']", root);
      String clazz = bean.getAttribute("class");
      bean.setAttribute("class",
          clazz.replace("__TARGET_PACKAGE__", CLASSES_PACKAGE));

      bean = XmlUtils.findFirstElement(
          "/beans/bean[@id='serverWSAuthPortProxy']", root);
      clazz = bean.getAttribute("class");
      bean.setAttribute("class",
          clazz.replace("__TARGET_PACKAGE__", CLASSES_PACKAGE));

    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }

    // Write file
    XmlUtils.writeXml(mutableFile.getOutputStream(), secXmlDoc);

    fileManager.scan();
  }

  private void copyJavaFiles() {

    String prjId = ProjectMetadata.getProjectIdentifier();
    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
        .get(prjId);

    // Copiamos los ficheros del cliente del servicio WSAuth
    for (String className : JAVA_WS_CLASS_FILENAMES) {
      installTemplate("java-src-templates", className, "ServerWSAuth",
          projectMetadata, null, false);
    }

    // Copiamos los ficheros del Provider, usuarios y el cliente del
    // servicio WSAuth
    for (String className : JAVA_CLASS_FILENAMES) {
      installTemplate("java-src-templates", className, CLASSES_PACKAGE,
          projectMetadata, null, false);
    }

    // Copiamos los ficheros de los xsd del servicio WSAuth
    copyDirectoryContents("java-src/**",
        pathResolver.getRoot(Path.SRC_MAIN_JAVA));
  }

  /***
   * <p>
   * Método de utilida que genera un fichero <code>targetFilename</code> basado
   * en un <i>template</i>.
   * </p>
   * <p>
   * Los <i>template</i> se buscan en el paquete de la clase actual compuestos
   * por <code>targetFilename</code> con la extensión terminada en
   * <code>-template</code>.
   * </p>
   * <p>
   * Antes de escribir el fichero destino, se reemplazan las siguientes cadenas:
   * </p>
   * <ul>
   * <li><code>__TOP_LEVEL_PACKAGE__</code> --> Paquete raíz de la aplicación</li>
   * <li><code>__TARGET_PACKAGE__</code> --> Paquete de destino del fichero
   * especificado en <code>targetPackage</code></li>
   * <li>Por cada elemento de <code>parameters</code>: <code>__</code>
   * <i>clave</i><code>__</code> --> <i>valor<i></li>
   * </ul>
   *
   * @param sourceFolder path relativo a esta clase para buscar el template, si
   *          es null usa la la ruta de la clase actual
   * @param targetFilename nombre del fichero final
   * @param targetPackage paquete donde se generará el fichero (admite '~' como
   *          comodín del paquete base)
   * @param projectMetadata metadatos del proyecto
   * @param parameters valores adicionales a reemplazar (puede ser
   *          <code>null</code> si no se necesita)
   * @param override especifica si sobreescribir el archivo si ya existe
   */
  private void installTemplate(String sourceFolder, String targetFilename,
                               String targetPackage,
                               ProjectMetadata projectMetadata,
                               Map<String, String> parameters, boolean override) {
    // default package
    String packagePath = projectMetadata.getTopLevelPackage()
        .getFullyQualifiedPackageName().replace('.', '/');

    // setting targetPackage change default package
    String finalTargetPackage = null;
    if (targetPackage != null) {
      if (targetPackage.startsWith("~")) {
        finalTargetPackage = targetPackage.replace("~", projectMetadata
            .getTopLevelPackage().getFullyQualifiedPackageName());
      }
      else {
        finalTargetPackage = targetPackage;
      }
      packagePath = finalTargetPackage.replace('.', '/');
    }
    else {
      finalTargetPackage = getClass().getPackage().getName();
    }

    String destinationFile = projectMetadata.getPathResolver().getIdentifier(
        Path.SRC_MAIN_JAVA, packagePath + "/" + targetFilename);

    if ((!fileManager.exists(destinationFile)) || override) {
      InputStream templateInputStream;
      if (sourceFolder == null) {
        templateInputStream = TemplateUtils.getTemplate(getClass(),
            targetFilename + "-template");
      }
      else {
        templateInputStream = TemplateUtils.getTemplate(getClass(),
            sourceFolder + "/" + targetFilename + "-template");
      }
      try {
        // Read template and insert the user's package
        String input = FileCopyUtils.copyToString(new InputStreamReader(
            templateInputStream));
        input = input.replace("__TOP_LEVEL_PACKAGE__", projectMetadata
            .getTopLevelPackage().getFullyQualifiedPackageName());

        input = input.replace("__TARGET_PACKAGE__", finalTargetPackage);

        if (parameters != null) {
          for (Entry<String, String> entry : parameters.entrySet()) {
            input = input.replace("__" + entry.getKey() + "__",
                entry.getValue());
          }
        }

        // Output the file for the user
        MutableFile mutableFile = fileManager.createFile(destinationFile);
        FileCopyUtils.copy(input.getBytes(), mutableFile.getOutputStream());
      }
      catch (IOException ioe) {
        throw new IllegalStateException("Unable to create '" + targetFilename
            + "'", ioe);
      }
    }
  }

  /**
   * This method will copy the contents of a directory to another if the
   * resource does not already exist in the target directory
   *
   * @param sourceAntPath directory
   * @param target directory
   */
  private void copyDirectoryContents(String sourceAntPath,
                                     String targetDirectory) {
    Assert.hasText(sourceAntPath, "Source path required");
    Assert.hasText(targetDirectory, "Target directory required");

    if (!targetDirectory.endsWith("/")) {
      targetDirectory += "/";
    }

    if (!fileManager.exists(targetDirectory)) {
      fileManager.createDirectory(targetDirectory);
    }

    String path = TemplateUtils.getTemplatePath(getClass(), sourceAntPath);
    Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(
        context.getBundleContext(), path);

    Assert.notNull(urls,
        "Could not search bundles for resources for Ant Path '" + path + "'");
    for (URL url : urls) {
      String fileName = url.getPath().substring(
          url.getPath().lastIndexOf("java-src/") + 9);

      if (fileName.endsWith(".java")) {
        if (!fileManager.exists(targetDirectory + fileName)) {
          try {
            FileCopyUtils.copy(url.openStream(),
                fileManager.createFile(targetDirectory + fileName)
                    .getOutputStream());
          }
          catch (IOException e) {
            new IllegalStateException(
                "Encountered an error during copying of resources for MVC JSP addon.",
                e);
          }
        }
      }
    }
  }
}
