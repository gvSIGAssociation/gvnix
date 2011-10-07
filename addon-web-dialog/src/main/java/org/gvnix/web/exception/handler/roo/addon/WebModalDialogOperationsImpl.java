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
package org.gvnix.web.exception.handler.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.MetadataUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
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
 * Implementation of Operations of Dialogs .
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebModalDialogOperationsImpl implements WebModalDialogOperations {

    private static final JavaSymbolName VALUE = new JavaSymbolName("value");
    private static final JavaSymbolName ARRAY_ELEMENT = new JavaSymbolName(
            "__ARRAY_ELEMENT__");

    private static final JavaType MODAL_DIALOGS = new JavaType(
            GvNIXModalDialogs.class.getName());

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private WebExceptionHandlerOperations exceptionOperations;
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
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
        return OperationUtils.isProjectAvailable(metadataService)
                && OperationUtils.isSpringMvcProject(metadataService,
                        fileManager);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * addModalDialogAnnotation(org.springframework.roo.model.JavaType,
     * org.springframework.roo.model.JavaSymbolName)
     */
    public void addModalDialogAnnotation(JavaType controllerClass,
            JavaSymbolName name) {
        Assert.notNull(controllerClass, "controller is required");
        Assert.notNull(name, "name is required");
        // setup maven dependency
        setupMavenDependency();

        annotateWithModalDialog(controllerClass, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * addDefaultModalDialogAnnotation(org.springframework.roo.model.JavaType)
     */
    public void addDefaultModalDialogAnnotation(JavaType controllerClass) {
        Assert.notNull(controllerClass, "controller is required");

        annotateWithModalDialog(controllerClass, null);
    }

    /**
     * Annotates given controller class with {@link GvNIXModalDialogs} with
     * value name if informed
     * 
     * @param controllerClass
     * @param name
     *            name of the modal dialog. May be null.
     */
    private void annotateWithModalDialog(JavaType controllerClass,
            JavaSymbolName name) {
        Assert.notNull(controllerClass, "controller is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        MutableClassOrInterfaceTypeDetails controllerDetails = MetadataUtils
                .getPhysicalTypeDetails(controllerClass, metadataService,
                        physicalTypeMetadataProvider);
        // Test if has the @Controller
        Assert.notNull(MemberFindingUtils.getAnnotationOfType(controllerDetails
                .getAnnotations(), new JavaType(
                "org.springframework.stereotype.Controller")), controllerClass
                .getSimpleTypeName().concat(" has not @Controller annotation"));

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        MODAL_DIALOGS);

        // List of pattern to use
        List<StringAttributeValue> modalDialogsList = new ArrayList<StringAttributeValue>();

        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXModalDialog already exists

            // Loads previously registered modal dialog into modalDialogsList
            // Also checks if name is used previously
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(VALUE);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();

                if (previousValues != null && !previousValues.isEmpty()) {
                    for (StringAttributeValue value : previousValues) {
                        if (!modalDialogsList.contains(value)) {
                            modalDialogsList.add(value);
                        }
                    }
                }
            }
            isAlreadyAnnotated = true;
        }

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                MODAL_DIALOGS);

        if (name != null) {
            StringAttributeValue newModalDialogValue = new StringAttributeValue(
                    ARRAY_ELEMENT, name.getSymbolName());
            if (!modalDialogsList.contains(newModalDialogValue)) {
                modalDialogsList.add(newModalDialogValue);
            }

            // Add attribute values
            annotationBuilder
                    .addAttribute(new ArrayAttributeValue<StringAttributeValue>(
                            VALUE, modalDialogsList));
        }

        if (isAlreadyAnnotated) {
            controllerDetails.updateTypeAnnotation(annotationBuilder.build(),
                    new HashSet<JavaSymbolName>());
        } else {
            controllerDetails.addTypeAnnotation(annotationBuilder.build());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * setupModalDialogsSupport()
     */
    public void setupModalDialogsSupport() {
        // setup maven dependency
        setupMavenDependency();

        // install Dialog Bean
        OperationUtils.installWebDialogClass(
                getControllerFullyQualifiedPackage().concat(".dialog"),
                pathResolver, fileManager);

        // install MessageMappingBeanResolver
        String messageMappingResolverClass = installWebServletMessageMappingExceptionResolverClass();
        updateExceptionResolverBean(messageMappingResolverClass);

        // install gvNIX excpections
        exceptionOperations.setUpGvNIXExceptions();

        // install MVC artifacts
        installMvcArtifacts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * setupMavenDependency()
     */
    public void setupMavenDependency() {
        Element configuration = XmlUtils.getConfiguration(getClass(),
                "configuration.xml");

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepository(new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);
        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);

        depens = XmlUtils.findElements(
                "/configuration/dependencies/dependency", configuration);
        for (Element depen : depens) {
            projectOperations.addDependency(new Dependency(depen));
        }
    }

    /**
     * Installs Java classes for MessageMappapingExceptionResolver support
     * 
     * @return string as fully qualified name of MessageMappingExceptionResolver
     *         Java class installed
     */
    private String installWebServletMessageMappingExceptionResolverClass() {
        String className = "MessageMappingExceptionResolver";
        String classPackage = getControllerFullyQualifiedPackage().concat(
                ".servlet.handler");

        String classPath = pathResolver.getIdentifier(
                Path.SRC_MAIN_JAVA,
                classPackage.concat(".").concat(className)
                        .replace(".", File.separator).concat(".java"));

        MutableFile mutableClass = null;
        if (!fileManager.exists(classPath)) {
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
                javaTemplate = StringUtils.replace(javaTemplate,
                        "${PACKAGE_DIALOG}",
                        getControllerFullyQualifiedPackage().concat(".dialog"));

                // Write final java file
                mutableClass = fileManager.createFile(classPath);
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
        return classPackage.concat(".").concat(className);
    }

    /**
     * Returns the fully qualified name of the Controllers package of the
     * App.(classes annotated with @Controller)
     * 
     * @param className
     * @return
     */
    private String getControllerFullyQualifiedPackage() {
        // Search for @Controller annotated class and get its package
        Set<ClassOrInterfaceTypeDetails> webMcvControllers = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
                        "org.springframework.stereotype.Controller"));
        String controllerPackageName = null;
        if (!webMcvControllers.isEmpty()) {
            JavaPackage controllerPackage = webMcvControllers.iterator().next()
                    .getName().getPackage();
            controllerPackageName = controllerPackage
                    .getFullyQualifiedPackageName();
        }
        Assert.notNull(controllerPackageName,
                "Can not get a fully qualified name for Controllers package");

        return controllerPackageName;
    }

    /**
     * Change the class of the bean MappingExceptionResolver by gvNIX's resolver
     * class. The gvNIX resolver class supports redirect calls and messages in a
     * modal dialog.
     * 
     * @param beanClassName
     *            the name of the new ExceptionResolver Bean
     */
    private void updateExceptionResolverBean(String beanClassName) {
        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");

        if (!fileManager.exists(webXmlPath)) {
            return;
        }

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element simpleMappingExceptionResolverBean = XmlUtils
                .findFirstElement(
                        "/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']",
                        root);

        // We'll replace the class just if SimpleMappingExceptionResolver is set
        if (simpleMappingExceptionResolverBean != null) {
            simpleMappingExceptionResolverBean.setAttribute("class",
                    beanClassName);
            simpleMappingExceptionResolverBean.setAttribute("id",
                    "messageMappingExceptionResolverBean");
            XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);
        }

        // Here we need MessageMappingExceptionResolver set as ExceptionResolver
        Element messageMappingExceptionResolverBean = XmlUtils
                .findFirstElement("/beans/bean[@class='".concat(beanClassName)
                        .concat("']"), root);
        Assert.notNull(messageMappingExceptionResolverBean,
                "MessageMappingExceptionResolver is not configured. Check webmvc-config.xml");

    }

    /**
     * Installs MVC Artifacts into current project<br/>
     * Artifacts installed:<br/>
     * <ul>
     * <li>message-box.tagx</li>
     * </ul>
     * Modify default.jspx layout adding in the right position the element
     * &lt;util:message-box /&gt;
     * <p>
     * Also adds needed i18n properties to right message_xx.properties files
     */
    public void installMvcArtifacts() {
        // copy util to tags/util
        copyDirectoryContents("tags/dialog/modal/*.tagx",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/dialog/modal"));
        addMessageBoxInLayout();

        modifyChangesControlDialogNs();

        addI18nProperties();
    }

    /**
     * Takes properties files (messages_xx.properties) and adds their content to
     * i18n message bundle file in current project
     */
    private void addI18nProperties() {
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

    private void modifyChangesControlDialogNs() {
        String changesControlTagx = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/util/changes-control.tagx");

        if (!fileManager.exists(changesControlTagx)) {
            // tags/util/changes-control.tagx doesn't exist, so nothing to do
            return;
        }

        InputStream changesControlTagxIs = fileManager
                .getInputStream(changesControlTagx);

        Document changesControlTagxXml;
        try {
            changesControlTagxXml = XmlUtils.getDocumentBuilder().parse(
                    changesControlTagxIs);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element jspRoot = changesControlTagxXml.getDocumentElement();

        // Set dialog tag lib as attribute in html element
        jspRoot.setAttribute("xmlns:dialog",
                "urn:jsptagdir:/WEB-INF/tags/dialog/modal");

        writeToDiskIfNecessary(changesControlTagx,
                changesControlTagxXml.getDocumentElement());

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

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * isMessageBoxOfTypeModal()
     */
    public boolean isMessageBoxOfTypeModal() {
        String defaultJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/layouts/default.jspx");

        // TODO: Check if it's necessary to add message-box in home-default.jspx
        // layout (when exists)

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return false;
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

        // Check if dialog:message-box is of type modal
        String dialogNS = lsHtml.getAttribute("xmlns:dialog");
        if (dialogNS.equals("urn:jsptagdir:/WEB-INF/tags/dialog/modal")) {
            return true;
        }

        return false;
    }

}
