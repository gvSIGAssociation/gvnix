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
package org.gvnix.service.roo.addon.ws.importt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.JavaParserService;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceProxy;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceSecurity;
import org.gvnix.service.roo.addon.security.SecurityService;
import org.gvnix.service.roo.addon.security.WSServiceSecurityMetadata;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;

/**
 * Addon for Handle Web Service Proxy Layer
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WSImportOperationsImpl implements WSImportOperations {

    private static Logger logger = Logger.getLogger(WSImportOperations.class
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
    private WSConfigService wSConfigService;
    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private SecurityService securityService;

    // @Reference
    // private ClasspathOperations classpathOperations;

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.service.roo.addon.ws.importt.WSImportOperations
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
        return wSConfigService.isProjectAvailable();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the class to add annotation doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     */
    public void addImportAnnotation(JavaType serviceClass, String wsdlLocation) {

        // Check URL connection and WSDL format
        securityService.loadWsdlUrl(wsdlLocation);

        // Service class path
        String fileLocation = projectOperations.getPathResolver()
                .getIdentifier(
                        Path.SRC_MAIN_JAVA,
                        serviceClass.getFullyQualifiedTypeName()
                                .replace('.', '/').concat(".java"));

        // If class not exists, create it
        if (!fileManager.exists(fileLocation)) {

            // Create service class with Service Annotation.
            javaParserService.createServiceClass(serviceClass);
            logger.log(
                    Level.FINE,
                    "New service class created: "
                            + serviceClass.getFullyQualifiedTypeName());
        }

        // Check if import annotation is already defined
        // DiSiD: Use typeLocationService instead of classpathOperations
        // if
        // (javaParserService.isAnnotationIntroduced(GvNIXWebServiceProxy.class
        // .getName(), classpathOperations
        // .getClassOrInterface(serviceClass))) {
        if (javaParserService.isAnnotationIntroduced(
                GvNIXWebServiceProxy.class.getName(),
                typeLocationService.getClassOrInterface(serviceClass))) {

            logger.log(Level.WARNING,
                    "Provided class is already importing a service");
        } else {

            // Add the import definition annotation and attributes to the class
            List<AnnotationAttributeValue<?>> annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
            annotationAttributeValues.add(new StringAttributeValue(
                    new JavaSymbolName("wsdlLocation"), wsdlLocation));
            annotationsService.addJavaTypeAnnotation(serviceClass,
                    GvNIXWebServiceProxy.class.getName(),
                    annotationAttributeValues, false);

            // Add GvNixAnnotations to the project.
            annotationsService.addGvNIXAnnotationsDependency();
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
                .getClassOrInterface(importedServiceClass);

        // checks if class is really a imported service
        boolean hasImportAnnotation = javaParserService.isAnnotationIntroduced(
                GvNIXWebServiceProxy.class.getName(), importedServiceDetails);
        Assert.isTrue(hasImportAnnotation, importedServiceDetails.toString()
                .concat(" is not a imported service"));

        // Check if certificate file exist
        Assert.isTrue(certificate.exists(), certificate.getAbsolutePath()
                .concat(" not found"));
        Assert.isTrue(certificate.isFile(), certificate.getAbsolutePath()
                .concat(" is not a file"));

        // Check certificate extension
        if (!certificate.getName().endsWith(".p12")) {
            // if it's not .p12 show a warning
            logger.warning("Currently this action only supports pkcs12. "
                    .concat(certificate.getAbsolutePath()).concat(
                            " has no '.p12' extension"));
        }

        // Copy certificate file into resources
        File targetCertificated = copyCertificateFileIntoResources(
                importedServiceClass, certificate);

        // Add annotation to class
        List<AnnotationAttributeValue<?>> annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
        annotationAttributeValues
                .add(new StringAttributeValue(
                        new JavaSymbolName("certificate"), targetCertificated
                                .getName()));
        annotationAttributeValues.add(new StringAttributeValue(
                new JavaSymbolName("password"), password));
        annotationAttributeValues.add(new StringAttributeValue(
                new JavaSymbolName("alias"), alias));
        annotationsService.addJavaTypeAnnotation(importedServiceClass,
                GvNIXWebServiceSecurity.class.getName(),
                annotationAttributeValues, false);

        // Add GvNixAnnotations to the project.
        annotationsService.addGvNIXAnnotationsDependency();

    }

    /**
     * <p>
     * Copy a certificate file into project resources
     * </p>
     * 
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

        // Copy certificate file to project resource folder
        MutableFile targetCertificatedMutableFile = fileManager
                .createFile(targetCertificated.getAbsolutePath());
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(certificate);
            os = targetCertificatedMutableFile.getOutputStream();

            FileCopyUtils.copy(is, os);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Noting to do
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    // Noting to do
                }
            }
        }
        return targetCertificated;
    }

    /**
     * <p>
     * Generates file names for certificate file to copy into project resource
     * folder until it find a unused one.
     * </p>
     * 
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
                Path.SRC_MAIN_RESOURCES, targetPath);

        int index = 1;

        while (fileManager.exists(targetPath)) {
            targetPath = baseNamePath.concat(certificateName)
                    .concat("_" + index).concat(extension);
            targetPath = projectOperations.getPathResolver().getIdentifier(
                    Path.SRC_MAIN_RESOURCES, targetPath);
            index++;
        }
        return new File(targetPath);
    }
}
