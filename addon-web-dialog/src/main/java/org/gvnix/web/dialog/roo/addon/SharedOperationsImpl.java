/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.dialog.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Operations shared between Exception and Dialog.
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class SharedOperationsImpl implements SharedOperations {

    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private I18nSupport i18nSupport;
    @Reference
    private PropFileOperations propFileOperations;

    private ComponentContext context;

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.dialog.roo.addon.SharedOperations#
     * installWebServletClass(java.lang.String)
     */
    public String installWebServletMessageMappingExceptionResolverClass() {
        String className = "MessageMappingExceptionResolver";
        String classFullName = getClassFullQualifiedName(className);

        String classPath = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
                classFullName.replace(".", File.separator).concat(".java"));

        String classPackage = classFullName.replace(".".concat(className), "");
        MutableFile mutableClass = null;
        if (!fileManager.exists(classPath)) {
            mutableClass = fileManager.createFile(classPath);
            InputStream template = TemplateUtils.getTemplate(
                    getClass(),
                    "web/servlet/handler/".concat(className).concat(
                            ".java-template"));

            String javaTemplate;
            try {
                javaTemplate = FileCopyUtils
                        .copyToString(new InputStreamReader(template));

                // Replace package definition
                javaTemplate = StringUtils.replace(javaTemplate, "${PACKAGE}",
                        classPackage);

                // Write final java file
                FileCopyUtils.copy(javaTemplate.getBytes(),
                        mutableClass.getOutputStream());
            } catch (IOException ioe) {
                throw new IllegalStateException("Unable load "
                        .concat(className).concat(".java-template template"),
                        ioe);
            } finally {
                try {
                    template.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Error creating ".concat(
                            className).concat(".java in project"), e);
                }
            }
        }
        return classPackage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.dialog.roo.addon.SharedOperations#
     * getClassFullQualifiedName(java.lang.String)
     */
    public String getClassFullQualifiedName(String className) {
        // Search for @Controller annotated class and get its package as
        // base package for MessageMappingExceptionResolver
        Set<ClassOrInterfaceTypeDetails> webMcvControllers = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
                        "org.springframework.stereotype.Controller"));
        String classFullName = null;
        if (!webMcvControllers.isEmpty()) {
            JavaPackage controllerPackage = webMcvControllers.iterator().next()
                    .getName().getPackage();
            classFullName = controllerPackage.getFullyQualifiedPackageName()
                    .concat(".servlet.handler.").concat(className);
        }
        Assert.notNull(classFullName, "Can not get a fully qualified name for "
                .concat(className).concat(" class"));

        return classFullName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.dialog.roo.addon.SharedOperations#
     * installMvcArtifacts()
     */
    public void installMvcArtifacts() {
        // copy util to tags/util
        copyDirectoryContents("tags/dialog/modal/*.tagx",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/dialog/modal"));
        addMessageBoxInLayout();

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
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath
     *            the source path
     * @param targetDirectory
     *            the target directory
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
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            if (!fileManager.exists(targetDirectory + fileName)) {
                try {
                    FileCopyUtils.copy(url.openStream(), fileManager
                            .createFile(targetDirectory + fileName)
                            .getOutputStream());
                } catch (IOException e) {
                    new IllegalStateException(
                            "Encountered an error during copying of resources for MVC JSP addon.",
                            e);
                }
            }
        }
    }

    /**
     * Adds the element util:message-box in the right place in default.jspx
     * layout
     */
    private void addMessageBoxInLayout() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String defaultJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/layouts/default.jspx");

        // TODO: Check if it's necessary to add message-box in home-default.jspx
        // layout (when exists)

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return;
        }

        InputStream defulatJspxIs = fileManager.getInputStream(defaultJspx);

        Document defaultJspxXml;
        try {
            defaultJspxXml = XmlUtils.getDocumentBuilder().parse(defulatJspxIs);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element lsHtml = defaultJspxXml.getDocumentElement();

        // Set dialog tag lib as attribute in html element
        lsHtml.setAttribute("xmlns:dialog",
                "urn:jsptagdir:/WEB-INF/tags/dialog/modal");

        Element messageBoxElement = XmlUtils.findFirstElementByName(
                "dialog:message-box", lsHtml);
        if (messageBoxElement == null) {
            Element divMain = XmlUtils.findFirstElement(
                    "/html/body/div/div[@id='main']", lsHtml);
            Element insertAttributeBodyElement = XmlUtils.findFirstElement(
                    "/html/body/div/div/insertAttribute[@name='body']", lsHtml);
            Element messageBox = new XmlElementBuilder("dialog:message-box",
                    defaultJspxXml).build();
            divMain.insertBefore(messageBox, insertAttributeBodyElement);
        }

        writeToDiskIfNecessary(defaultJspx, defaultJspxXml.getDocumentElement());

    }

    /**
     * Decides if write to disk is needed (ie updated or created)<br/>
     * Used for TAGx files
     * 
     * TODO: candidato a ir al módulo Support
     * 
     * @param filePath
     * @param body
     * @return
     */
    private boolean writeToDiskIfNecessary(String filePath, Element body) {
        // Build a string representation of the JSP
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Transformer transformer = XmlUtils.createIndentingTransformer();
        XmlUtils.writeXml(transformer, byteArrayOutputStream,
                body.getOwnerDocument());
        String viewContent = byteArrayOutputStream.toString();

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (fileManager.exists(filePath)) {
            // First verify if the file has even changed
            File f = new File(filePath);
            String existing = null;
            try {
                existing = FileCopyUtils.copyToString(new FileReader(f));
            } catch (IOException ignoreAndJustOverwriteIt) {
            }

            if (!viewContent.equals(existing)) {
                mutableFile = fileManager.updateFile(filePath);
            }
        } else {
            mutableFile = fileManager.createFile(filePath);
            Assert.notNull(mutableFile, "Could not create '" + filePath + "'");
        }

        if (mutableFile != null) {
            try {
                // We need to write the file out (it's a new file, or the
                // existing file has different contents)
                FileCopyUtils.copy(viewContent, new OutputStreamWriter(
                        mutableFile.getOutputStream()));
                // Return and indicate we wrote out the file
                return true;
            } catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }
}
