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

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.addon.security.SecurityOperations;
import org.springframework.roo.file.monitor.FileMonitorService;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Clase que implementa las operaciones del add-on <b>cit securty</b>
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
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
	    PROVIDER_TARGET_CLASS_FILENAME, "ServerWSAuthBindingStub.java",
	    "ServerWSAuthPort.java", "ServerWSAuthPortProxy.java",
	    "ServerWSAuthService.java", "ServerWSAuthServiceLocator.java",
	    "WscitAuthenticationProvider.java", "WscitUserAuthority.java",
	    "WscitUser.java", };

    private static final String WSAUTH_PROPERTIES_NAME = "CITWSAuth.properties";
    private static final String SECURITY_XML_TEMPLATE = "applicationContext-security-template.xml";

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
    @Reference
    private FileMonitorService fileMonitorService;

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
	    alreadyInstaled = checkIsAlredyInstalled() ? Boolean.TRUE
		    : Boolean.FALSE;
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
	return fileManager.exists(pathResolver.getIdentifier(
		Path.SRC_MAIN_JAVA, PROVIDER_TARGET_CLASS_FILENAME));
    }

    public void setup() {
	// Si no esta configurada la seguriad pero se puede configurar
	// ya lo haremos nosotros
	if (!securityOperations.isInstallSecurityAvailable()) {
	    securityOperations.installSecurity();
	}

	// Copiamos las clases necesarias para el servicio
	copyJavaFiles();

	// Copiar los archivos de configuracion que necesitamos
	copyConfigFiles();

	// Actualizamos la configuracion de seguriada
	updateSecurityConfig();

    }

    /**
     * Copia los archivos de configuración própios de nuestra herramienta
     */
    private void copyConfigFiles() {

	String properties = pathResolver.getIdentifier(Path.SPRING_CONFIG_ROOT,
		WSAUTH_PROPERTIES_NAME);
	String source = getClass().getResource(
		"config/" + WSAUTH_PROPERTIES_NAME).getFile();

	try {
	    FileCopyUtils.copy(new File(source), new File(properties));
	} catch (IOException e) {
	    throw new IllegalStateException("Unable to create '"
		    + WSAUTH_PROPERTIES_NAME + "'", e);
	}

    }

    private void updateSecurityConfig() {

	String cxfDestFile = "applicationContext-security.xml";
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfDestFile);

	Document cxfXmlDoc;
	MutableFile mutableFile;
	if (fileManager.exists(cxfXmlPath)) {
	    // File exists, nothing to do
	    return;
	}

	// Create file
	mutableFile = fileManager.createFile(cxfXmlPath);
	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		"cxf-template.xml");
	try {
	    cxfXmlDoc = XmlUtils.getDocumentBuilder()
		    .parse(templateInputStream);

	    Element root = cxfXmlDoc.getDocumentElement();
	    Element bean = XmlUtils.findFirstElement(
		    "/beans/bean[id='wscitAuthenticationProvider']", root);
	    String clazz = bean.getAttribute("class");
	    bean.setAttribute("class", clazz.replace("__TARGET_PACKAGE__",
		    CLASSES_PACKAGE));

	    bean = XmlUtils.findFirstElement(
		    "/beans/bean[id='serverWSAuthPortProxy']", root);
	    clazz = bean.getAttribute("class");
	    bean.setAttribute("class", clazz.replace("__TARGET_PACKAGE__",
		    CLASSES_PACKAGE));

	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	// Write file
	XmlUtils.writeXml(mutableFile.getOutputStream(), cxfXmlDoc);

	fileManager.scan();
    }

    private void copyJavaFiles() {

	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);

	// Copiamos los ficheros del Provider, usuarios y el cliente del
	// servicio WSAuth
	for (String className : JAVA_CLASS_FILENAMES) {
	    installTemplate("java-src-templates", className, CLASSES_PACKAGE,
		    projectMetadata, null, false);
	}

	// Copiamos los ficheros de los xsd del servicio WSAuth
	String xsdTargetFolder = pathResolver.getRoot(Path.SRC_MAIN_JAVA);
	String source = getClass().getResource("java-src").getFile();

	FileUtils.copyRecursively(new File(source), new File(xsdTargetFolder),
		false);
    }

    /***
     * <p>
     * Método de utilida que genera un fichero <code>targetFilename</code>
     * basado en un <i>template</i>.
     * </p>
     * 
     * <p>
     * Los <i>template</i> se buscan en el paquete de la clase actual compuestos
     * por <code>targetFilename</code> con la extensión terminada en
     * <code>-template</code>.
     * </p>
     * 
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
     * @param sourceFolder
     *            path relativo a esta clase para buscar el template, si es null
     *            usa la la ruta de la clase actual
     * 
     * @param targetFilename
     *            nombre del fichero final
     * @param targetPackage
     *            paquete donde se generará el fichero (admite '~' como comodín
     *            del paquete base)
     * @param projectMetadata
     *            metadatos del proyecto
     * @param parameters
     *            valores adicionales a reemplazar (puede ser <code>null</code>
     *            si no se necesita)
     * @param override
     *            especifica si sobreescribir el archivo si ya existe
     * 
     */
    private void installTemplate(String sourceFolder, String targetFilename,
	    String targetPackage, ProjectMetadata projectMetadata,
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
	    } else {
		finalTargetPackage = targetPackage;
	    }
	    packagePath = finalTargetPackage.replace('.', '/');
	} else {
	    finalTargetPackage = getClass().getPackage().getName();
	}

	String destinationFile = projectMetadata.getPathResolver()
		.getIdentifier(Path.SRC_MAIN_JAVA,
			packagePath + "/" + targetFilename);

	if ((!fileManager.exists(destinationFile)) || override) {
	    InputStream templateInputStream;
	    if (sourceFolder == null) {
		templateInputStream = TemplateUtils.getTemplate(getClass(),
			targetFilename + "-template");
	    } else {
		templateInputStream = TemplateUtils.getTemplate(getClass(),
			sourceFolder + "/" + targetFilename + "-template");
	    }
	    try {
		// Read template and insert the user's package
		String input = FileCopyUtils
			.copyToString(new InputStreamReader(templateInputStream));
		input = input.replace("__TOP_LEVEL_PACKAGE__", projectMetadata
			.getTopLevelPackage().getFullyQualifiedPackageName());

		input = input.replace("__TARGET_PACKAGE__", projectMetadata
			.getTopLevelPackage().getFullyQualifiedPackageName());

		if (parameters != null) {
		    for (Entry<String, String> entry : parameters.entrySet()) {
			input = input.replace("__" + entry.getKey() + "__",
				entry.getValue());
		    }
		}

		// Output the file for the user
		MutableFile mutableFile = fileManager
			.createFile(destinationFile);
		FileCopyUtils.copy(input.getBytes(), mutableFile
			.getOutputStream());
	    } catch (IOException ioe) {
		throw new IllegalStateException("Unable to create '"
			+ targetFilename + "'", ioe);
	    }
	}
    }
}
