/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.service.roo.addon.addon.ws.importt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.AnnotationsService;
import org.gvnix.service.roo.addon.addon.JavaParserService;
import org.gvnix.service.roo.addon.addon.security.SecurityService;
import org.gvnix.service.roo.addon.addon.security.WSServiceSecurityMetadata;
import org.gvnix.service.roo.addon.addon.util.WsdlParserUtils;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceProxy;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceSecurity;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Addon for Handle Web Service Proxy Layer
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
@Component
@Service
public class WSImportOperationsImpl implements WSImportOperations {

    private static final String CERTIFICATION_FILE_KEY = "org.apache.ws.security.crypto.merlin.file";

    private static Logger LOGGER = Logger.getLogger(WSImportOperations.class
            .getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private SecurityService securityService;

    /**
     * {@inheritDoc}
     * <p>
     * If the class to add annotation doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     */
    public void addImportAnnotation(JavaType serviceClass, String wsdlLocation) {

        // Check URL connection and WSDL format
        securityService.getWsdl(wsdlLocation);

        // Service class path
        String fileLocation = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                        serviceClass.getFullyQualifiedTypeName()
                                .replace('.', '/').concat(".java"));

        // If class not exists, create it
        if (!fileManager.exists(fileLocation)) {

            // Create service class with Service Annotation.
            javaParserService.createServiceClass(serviceClass);
            LOGGER.log(
                    Level.FINE,
                    "New service class created: "
                            + serviceClass.getFullyQualifiedTypeName());
        }

        // Check if import annotation is already defined
        if (javaParserService.isAnnotationIntroduced(
                GvNIXWebServiceProxy.class.getName(),
                typeLocationService.getTypeDetails(serviceClass))) {

            LOGGER.log(Level.WARNING,
                    "Provided class is already importing a service");
        }
        else {

            // Add the import definition annotation and attributes to the class
            List<AnnotationAttributeValue<?>> annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
            annotationAttributeValues.add(new StringAttributeValue(
                    new JavaSymbolName("wsdlLocation"), wsdlLocation));
            annotationsService.addJavaTypeAnnotation(serviceClass,
                    GvNIXWebServiceProxy.class.getName(),
                    annotationAttributeValues, false);

            // Add GvNixAnnotations to the project.
            annotationsService.addAddonDependency();
        }
    }

    /** {@inheritDoc} **/
    public List<String> getServiceList() {
        List<String> classNames = new ArrayList<String>();
        Set<ClassOrInterfaceTypeDetails> cids = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
                        GvNIXWebServiceProxy.class.getName()));
        for (ClassOrInterfaceTypeDetails cid : cids) {
            if (Modifier.isAbstract(cid.getModifier())) {
                continue;
            }
            classNames.add(cid.getName().getFullyQualifiedTypeName());
        }
        return classNames;
    }

    /** {@inheritDoc} **/
    public void addSignatureAnnotation(JavaType importedServiceClass,
            File certificate, String password, String alias) {

        // get class
        ClassOrInterfaceTypeDetails importedServiceDetails = typeLocationService
                .getTypeDetails(importedServiceClass);

        // checks if already has security annotation
        final boolean alreadyAnnotated = javaParserService
                .isAnnotationIntroduced(
                        GvNIXWebServiceSecurity.class.getName(),
                        importedServiceDetails);

        // checks if class is really a imported service and if it's a
        // RPC-Encoded
        Document wsdl = getWSDLFromClass(importedServiceClass);
        Validate.notNull(wsdl, importedServiceDetails.getName().toString()
                .concat(" is not a imported service"));
        Validate.isTrue(
                WsdlParserUtils.isRpcEncoded(wsdl.getDocumentElement()),
                "Only RPC-Encoded services is supported");

        // Get service name from wsdl
        String serviceName = getServiceName(wsdl);

        // Check if certificate file exist
        Validate.isTrue(certificate.exists(), certificate.getAbsolutePath()
                .concat(" not found"));
        Validate.isTrue(certificate.isFile(), certificate.getAbsolutePath()
                .concat(" is not a file"));

        // Check certificate extension
        if (!certificate.getName().endsWith(".p12")) {
            // if it's not .p12 show a warning
            LOGGER.warning("Currently this action only supports pkcs12. "
                    .concat(certificate.getAbsolutePath()).concat(
                            " has no '.p12' extension"));
        }

        // Copy certificate file into resources
        File targetCertificated = copyCertificateFileIntoResources(
                importedServiceClass, certificate);

        String propertiesPath = WSServiceSecurityMetadata
                .getPropertiesPath(importedServiceClass);
        String propertiesAbsolutePath = getSecurityPropertiesAbsolutePath(importedServiceClass);

        try {
            // update client-config.wsdd
            securityService
                    .addOrUpdateAxisClientService(serviceName,
                            WSServiceSecurityMetadata
                                    .getServiceWsddConfigurationParameters(
                                            importedServiceClass, alias,
                                            propertiesPath));

            // write properties file
            createSecurityPropertiesFile(importedServiceClass, serviceName,
                    password, alias, targetCertificated.getName(),
                    propertiesAbsolutePath);

        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Error generating security configuration", e);
        }

        if (!alreadyAnnotated) {
            // Add annotation to class
            List<AnnotationAttributeValue<?>> annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
            annotationsService.addJavaTypeAnnotation(importedServiceClass,
                    GvNIXWebServiceSecurity.class.getName(),
                    annotationAttributeValues, false);
        }

        // Add GvNixAnnotations to the project.
        annotationsService.addAddonDependency();

    }

    @Override
    public String getSecurityPropertiesAbsolutePath(
            JavaType importedServiceClass) {
        // Resolve properties absolute path
        String propertiesPath = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                        WSServiceSecurityMetadata
                                .getPropertiesPath(importedServiceClass));
        return propertiesPath;
    }

    /**
     * Returns properties for service configuration
     * 
     * @return
     */
    private Properties getSecurityProperties(String password, String alias,
            String certificatePath) {
        Properties properties = new Properties();

        // security provider
        properties.put("org.apache.ws.security.crypto.provider",
                "org.apache.ws.security.components.crypto.Merlin");

        // certificate Keystore type
        properties.put("org.apache.ws.security.crypto.merlin.keystore.type",
                "pkcs12");

        // certificate keystore password
        properties.put(
                "org.apache.ws.security.crypto.merlin.keystore.password",
                password);

        // alias password
        properties.put("org.apache.ws.security.crypto.merlin.alias.password",
                password);

        // certificate keystore alias
        properties.put("org.apache.ws.security.crypto.merlin.keystore.alias",
                alias);

        // certificate keystore file
        properties.put(CERTIFICATION_FILE_KEY, certificatePath);

        return properties;
    }

    /**
     * <p>
     * Copy a certificate file into project resources
     * </p>
     * <p>
     * If a file with the same name already exists, file will be copied using a
     * new name adding an suffix to original base name (see
     * {@link #computeCertificateTargetName(File, JavaType)}).
     * </p>
     * 
     * @param importedServiceClass
     * @param certificate
     * @return final file generated
     */
    private File copyCertificateFileIntoResources(
            JavaType importedServiceClass, File certificate) {
        // Prepare target file name
        File targetCertificated = computeCertificateTargetName(certificate,
                importedServiceClass);

        // Create target folder (if not exists)
        if (!targetCertificated.getParentFile().exists()) {
            fileManager.createDirectory(targetCertificated.getParentFile()
                    .getAbsolutePath());
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(certificate);
            outputStream = fileManager.createFile(
                    targetCertificated.getAbsolutePath()).getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return targetCertificated;
    }

    /**
     * <p>
     * Generates file names for certificate file to copy into project resource
     * folder until it find a unused one.
     * </p>
     * <p>
     * The first try is <code>{target-folder}/{certificate_fileName}</code>
     * </p>
     * <p>
     * Pattern:
     * <code>{target-folder}/{certificate_name}_{counter}.{certificate_extension}</code>
     * </p>
     * 
     * @param certificate
     * @param importedServiceClass
     * @return
     */
    private File computeCertificateTargetName(File certificate,
            JavaType importedServiceClass) {

        String certificateName = certificate.getName();
        String extension = certificateName.substring(
                certificateName.lastIndexOf('.'), certificateName.length());
        certificateName = certificateName.substring(0,
                certificateName.lastIndexOf('.'));
        String targetPath = WSServiceSecurityMetadata.getCertificatePath(
                importedServiceClass, certificate.getName());
        String baseNamePath = targetPath.replace(certificate.getName(), "");

        targetPath = projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                targetPath);

        int index = 1;

        while (fileManager.exists(targetPath)) {
            targetPath = baseNamePath.concat(certificateName)
                    .concat("_" + index).concat(extension);
            targetPath = projectOperations.getPathResolver().getIdentifier(
                    LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                    targetPath);
            index++;
        }
        return new File(targetPath);
    }

    /**
     * Returns the wsdl document for a proxy class
     * 
     * @param serviceClass
     * @return wsdl or null if it's not a proxy class
     */
    public Document getWSDLFromClass(JavaType serviceClass) {
        // get class
        ClassOrInterfaceTypeDetails importedServiceDetails = typeLocationService
                .getTypeDetails(serviceClass);

        AnnotationMetadata annotation = javaParserService.getAnnotation(
                GvNIXWebServiceProxy.class.getName(), importedServiceDetails);

        if (annotation == null) {
            return null;
        }

        Document wsdl = securityService.getWsdl((String) annotation
                .getAttribute(new JavaSymbolName("wsdlLocation")).getValue());

        return wsdl;
    }

    /**
     * Creates <code>{Service_Class}Sercurity.properties</code> file in
     * <code>scr/main/resorces/{Service_Class_Package}</code>
     * 
     * @param serviceClass
     * @param serviceName
     * @param password
     * @param alias
     * @param certificatePath
     * @param propertiesAbsolutePath
     */
    private void createSecurityPropertiesFile(JavaType serviceClass,
            String serviceName, String password, String alias,
            String certificatePath, String propertiesAbsolutePath) {

        OutputStream os;

        // Gets final properties
        Properties properties = getSecurityProperties(password, alias,
                certificatePath);

        // Checks if file exists
        MutableFile mutableFile;
        if (fileManager.exists(propertiesAbsolutePath)) {
            // Load current file content
            Properties storedProperties = new Properties();
            mutableFile = fileManager.updateFile(propertiesAbsolutePath);
            InputStream is = null;
            try {
                is = mutableFile.getInputStream();
                storedProperties.load(is);
            }
            catch (IOException ioException) {
                throw new IllegalStateException(ioException);
            }
            finally {
                IOUtils.closeQuietly(is);
            }
            // Compare content
            if (propertiesEquals(properties, storedProperties)) {
                // File Content is up to date --> exit
                return;
            }
        }
        else {
            // Unable to find the file, so let's create it
            mutableFile = fileManager.createFile(propertiesAbsolutePath);
        }

        // Store properties in file
        os = null;
        try {
            os = mutableFile.getOutputStream();
            storeProperties(serviceClass, serviceName, os, properties);
        }
        catch (IOException ioException) {
            throw new IllegalStateException(ioException);
        }
        finally {
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * Compares the values of two Property instances
     * 
     * @param one
     * @param other
     * @return
     */
    private boolean propertiesEquals(Properties one, Properties other) {
        if (one.size() != other.size()) {
            return false;
        }
        Set<Entry<Object, Object>> entrySet = one.entrySet();
        Object otherValue;
        for (Entry<Object, Object> entry : entrySet) {
            otherValue = other.get(entry.getKey());
            if (otherValue == null) {
                if (entry.getValue() != null) {
                    return false;
                }
            }
            else if (!otherValue.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Store properties into a output stream
     * 
     * @param serviceClass
     * @param serviceName
     * @param os
     * @param properties
     * @throws IOException
     */
    private void storeProperties(JavaType serviceClass, String serviceName,
            OutputStream os, Properties properties) throws IOException {
        properties.store(
                os,
                "Service '".concat(serviceName).concat(
                        "' security properities. Class ".concat(serviceClass
                                .getFullyQualifiedTypeName())));
    }

    @Override
    public String getServiceName(JavaType serviceClass) {
        Document wsdl = getWSDLFromClass(serviceClass);
        return getServiceName(wsdl);
    }

    @Override
    public String getServiceName(String wsdlLocation) {
        Document wsdl = securityService.getWsdl(wsdlLocation);

        Validate.notNull(wsdl, "Can't get WSDl from ".concat(wsdlLocation));
        return getServiceName(wsdl);

    }

    @Override
    public String getServiceName(Document wsdl) {
        // loads wsdl
        Validate.isTrue(
                WsdlParserUtils.isRpcEncoded(wsdl.getDocumentElement()),
                "Only RPC-Encoded services is supported");

        // Gets first port
        Element port = WsdlParserUtils.findFirstCompatiblePort(wsdl
                .getDocumentElement());

        return port.getAttribute("name");
    }

    @Override
    public String getCertificate(JavaType serviceClass) {
        Properties properties;
        try {
            properties = loadSecurityProperties(serviceClass);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return properties.getProperty(CERTIFICATION_FILE_KEY);
    }

    /**
     * Load security properties file for <code>serviceClass</code>
     * 
     * @param serviceClass
     * @return
     * @throws IOException
     */
    private Properties loadSecurityProperties(JavaType serviceClass)
            throws IOException {
        String path = getSecurityPropertiesAbsolutePath(serviceClass);
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("Sercurity properties file for '"
                    .concat(serviceClass.getFullyQualifiedTypeName())
                    .concat("' not found: ").concat(path));
        }
        FileInputStream fileIn = null;
        Properties properties = null;
        try {
            properties = new Properties();
            fileIn = new FileInputStream(file);
            if (fileIn != null) {
                properties.load(fileIn);
            }
        }
        finally {
            IOUtils.closeQuietly(fileIn);
        }
        return properties;
    }
}
