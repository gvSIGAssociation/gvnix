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
package org.gvnix.web.exception.handler.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperationsImpl;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
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
 * Implementation of Exception commands that are available via the Roo shell.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebExceptionHandlerOperationsImpl implements
        WebExceptionHandlerOperations {

    private static final String ITD_TEMPLATE = "exception.jspx";

    private static final String ENGLISH_LANGUAGE_FILENAME = "/WEB-INF/i18n/messages.properties";

    private static final String LANGUAGE_FILENAMES = "WEB-INF/i18n/messages**.properties";

    @Reference
    TypeLocationService typeLocationService;
    @Reference
    ProjectOperations projectOperations;
    @Reference
    private TilesOperations tilesOperations;
    @Reference
    private I18nSupport i18nSupport;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private PropFileOperations propFileOperations;

    private ComponentContext context;

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #getHandledExceptionList()
     */
    public String getHandledExceptionList() {

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");
        Assert.isTrue(fileManager.exists(webXmlPath),
                "webmvc-config.xml not found");

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

        List<Element> simpleMappingExceptionResolverProps = null;
        simpleMappingExceptionResolverProps = XmlUtils.findElements(
                "/beans/bean[@id='messageMappingExceptionResolverBean']"
                        + "/property[@name='exceptionMappings']/props/prop",
                root);

        Assert.notNull(simpleMappingExceptionResolverProps,
                "There aren't Exceptions handled by the application.");

        StringBuilder exceptionList = new StringBuilder("Handled Exceptions:\n");

        for (Element element : simpleMappingExceptionResolverProps) {
            exceptionList.append(element.getAttribute("key") + "\n");
        }

        return exceptionList.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #addNewHandledException(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void addNewHandledException(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String exceptionLanguage) {

        Assert.notNull(exceptionName, "You have to provide a Exception Name.");
        Assert.notNull(exceptionTitle, "You have to provide a Exception Title.");
        Assert.notNull(exceptionDescription,
                "You have to provide a Exception Description.");

        // Update webmvcconfig.xml and retrieve the exception view name.
        // Returns the exceptionViewName.
        String exceptionViewName = updateWebMvcConfig(exceptionName);

        // Create .jspx Exception file.
        createNewExceptionView(exceptionName, exceptionTitle,
                exceptionDescription, exceptionViewName);

        // Update views.xml
        updateViewsLayout(exceptionViewName);

        // Add the message property to identify the new Exception.
        String exceptionFileName = getLanguagePropertiesFile(exceptionLanguage);
        createMultiLanguageMessages(exceptionName, exceptionTitle,
                exceptionDescription, exceptionFileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #removeExceptionHandled(java.lang.String)
     */
    public void removeExceptionHandled(String exceptionName) {

        Assert.notNull(exceptionName, "You have to provide a Exception Name.");

        // Remove Exception mapping
        String exceptionViewName = removeWebMvcConfig(exceptionName);

        // Remove view definition.
        removeViewsLayout(exceptionViewName);

        // Remove Exception jspx view.
        removeExceptionView(exceptionViewName);

        // Remove multiLanguage messages.
        removeMultiLanguageMessages(exceptionName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #languageExceptionHandled(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void languageExceptionHandled(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String exceptionLanguage) {

        // Checks if the Exception is handled by the
        // SimpleMappingExceptionResolver
        existException(exceptionName);

        // Retrieves the existing messages FileName in the selected Language.
        String exceptionFileName = getLanguagePropertiesFile(exceptionLanguage);

        // Updates the selected language properties fileName for the Exception.
        updateMultiLanguageMessages(exceptionName, exceptionTitle,
                exceptionDescription, exceptionFileName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #getLanguagePropertiesFile(java.lang.String)
     */
    public String getLanguagePropertiesFile(String exceptionLanguage) {

        String languagePath;

        if (exceptionLanguage.compareTo("en") == 0) {
            languagePath = ENGLISH_LANGUAGE_FILENAME;
        } else {
            languagePath = "WEB-INF/i18n/messages_" + exceptionLanguage
                    + ".properties";
        }

        String messagesPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                languagePath);
        Assert.isTrue(
                fileManager.exists(messagesPath),
                languagePath
                        + "\t Language properties file not found.\nTry another Language: [es, de, it, nl, sv, en].");
        return languagePath;
    }

    /**
     * Checks if the Exception is mapped in the SimpleMappingExceptionResolver
     * Controller
     * 
     * @param exceptionName
     *            Exception Name to Handle.
     */
    private void existException(String exceptionName) {
        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");
        Assert.isTrue(fileManager.exists(webXmlPath),
                "webmvc-config.xml not found");

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

        Element simpleMappingExceptionResolverProp = XmlUtils
                .findFirstElement(
                        "/beans/bean[@id='messageMappingExceptionResolverBean']"
                                + "/property[@name='exceptionMappings']/props/prop[@key='"
                                + exceptionName + "']", root);

        Assert.isTrue(simpleMappingExceptionResolverProp != null,
                "There isn't a Exception Handled with the name:\t"
                        + exceptionName);
    }

    /**
     * Update the webmvc-config.xml with the new Exception.
     * 
     * @param exceptionName
     *            Exception Name to Handle.
     * @return {@link String} The exceptionViewName to create the .jspx view.
     */
    protected String updateWebMvcConfig(String exceptionName) {

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");
        Assert.isTrue(fileManager.exists(webXmlPath),
                "webmvc-config.xml not found");

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

        Element simpleMappingExceptionResolverProps = XmlUtils
                .findFirstElement(
                        "/beans/bean[@id='messageMappingExceptionResolverBean']"
                                + "/property[@name='exceptionMappings']/props",
                        root);

        Element simpleMappingExceptionResolverProp = XmlUtils
                .findFirstElement(
                        "/beans/bean[@id='messageMappingExceptionResolverBean']"
                                + "/property[@name='exceptionMappings']/props/prop[@key='"
                                + exceptionName + "']", root);

        boolean updateMappings = false;
        boolean updateController = false;

        // View name for this Exception.
        String exceptionViewName;

        if (simpleMappingExceptionResolverProp != null) {
            exceptionViewName = simpleMappingExceptionResolverProp
                    .getTextContent();
        } else {
            updateMappings = true;
            exceptionViewName = getExceptionViewName(exceptionName);
        }

        Element newExceptionMapping;

        // Exception Mapping
        newExceptionMapping = webXml.createElement("prop");
        newExceptionMapping.setAttribute("key", exceptionName);

        Assert.isTrue(exceptionViewName != null,
                "Can't create the view for the:\t" + exceptionName
                        + " Exception.");

        newExceptionMapping.setTextContent(exceptionViewName);

        if (updateMappings) {
            simpleMappingExceptionResolverProps
                    .appendChild(newExceptionMapping);
        }

        // Exception Controller
        Element newExceptionView = XmlUtils.findFirstElement(
                "/beans/view-controller[@path='/" + exceptionViewName + "']",
                root);

        if (newExceptionView == null) {
            updateController = true;
        }

        newExceptionView = webXml.createElementNS(
                "http://www.springframework.org/schema/mvc", "view-controller");
        newExceptionView.setPrefix("mvc");

        newExceptionView.setAttribute("path", "/" + exceptionViewName);

        if (updateController) {
            root.appendChild(newExceptionView);
        }

        if (updateMappings || updateController) {
            XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);
        }

        return exceptionViewName;
    }

    /**
     * Returns the exception view name checking if exists in the file
     * webmvc-config.xml file.
     * 
     * @param exceptionName
     *            to create the view.
     * @param root
     *            {@link Element} with the values off webmvc-config.xml
     * @return
     */
    private String getExceptionViewName(String exceptionName) {

        // View name for this Exception.
        int index = exceptionName.lastIndexOf(".");
        String exceptionViewName = exceptionName;

        if (index >= 0) {
            exceptionViewName = exceptionName.substring(index + 1);
        }
        exceptionViewName = StringUtils.uncapitalize(exceptionViewName);

        boolean exceptionNameExists = true;

        int exceptionCounter = 2;

        String tmpExceptionViewName = exceptionViewName;

        for (int i = 1; exceptionNameExists; i++) {

            exceptionNameExists = fileManager.exists(pathResolver
                    .getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/views/"
                            + tmpExceptionViewName + ".jspx"));

            if (exceptionNameExists) {
                tmpExceptionViewName = exceptionViewName.concat(Integer
                        .toString(exceptionCounter++));
            }
        }

        return tmpExceptionViewName;
    }

    /**
     * Removes the definition of the selected Exception in the webmvc-config.xml
     * file.
     * 
     * @param exceptionName
     *            Exception Name to remove.
     */
    private String removeWebMvcConfig(String exceptionName) {

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");
        Assert.isTrue(fileManager.exists(webXmlPath),
                "webmvc-config.xml not found");

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

        Element simpleMappingExceptionResolverProp = XmlUtils
                .findFirstElement(
                        "/beans/bean[@id='messageMappingExceptionResolverBean']"
                                + "/property[@name='exceptionMappings']/props/prop[@key='"
                                + exceptionName + "']", root);

        Assert.isTrue(simpleMappingExceptionResolverProp != null,
                "There isn't a Handled Exception with the name:\t"
                        + exceptionName);

        // Remove Mapping
        simpleMappingExceptionResolverProp.getParentNode().removeChild(
                simpleMappingExceptionResolverProp);

        String exceptionViewName = simpleMappingExceptionResolverProp
                .getTextContent();

        Assert.isTrue(exceptionViewName != null,
                "Can't remove the view for the:\t" + exceptionName
                        + " Exception.");

        // Remove NameSpace bean.
        Element lastExceptionControlled = XmlUtils.findFirstElement(
                "/beans/view-controller[@path='/" + exceptionViewName + "']",
                root);

        lastExceptionControlled.getParentNode().removeChild(
                lastExceptionControlled);

        XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);

        return exceptionViewName;
    }

    /**
     * Update layout.xml to map the new Exception associated View.
     * 
     * @param exceptionViewName
     *            to create the view and the definition in the xml
     */
    private void updateViewsLayout(String exceptionViewName) {

        // Exception view - create a view to show the exception message.

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/views/views.xml");
        Assert.isTrue(fileManager.exists(webXmlPath), "views.xml not found");

        String jspxPath = "/WEB-INF/views/" + exceptionViewName + ".jspx";

        tilesOperations.addViewDefinition("", exceptionViewName,
                TilesOperationsImpl.PUBLIC_TEMPLATE, jspxPath);

    }

    /**
     * Removes the definition of the Exception view in layout.xml.
     * 
     * @param exceptionViewName
     *            Exception Name to remove.
     */
    private void removeViewsLayout(String exceptionViewName) {

        // Exception view - create a view to show the exception message.

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/views/views.xml");
        Assert.isTrue(fileManager.exists(webXmlPath), "views.xml not found");

        tilesOperations.removeViewDefinition(exceptionViewName, "");
    }

    /**
     * Creates a view for the new Handled Exception.
     * 
     * @param exceptionName
     *            Name of the Exception to handle.
     * @param exceptionTitle
     *            Title of the exception view.
     * @param exceptionDescription
     *            Description to show in the view.
     * @param exceptionViewName
     *            Name of the .jspx view.
     */
    private void createNewExceptionView(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String exceptionViewName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put("error.uncaughtexception.title", "error."
                + exceptionNameUncapitalize + ".title");
        params.put("error.uncaughtexception.problemdescription", "error."
                + exceptionNameUncapitalize + ".problemdescription");

        String exceptionFilename = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/" + exceptionViewName
                        + ".jspx");
        String template;
        try {

            InputStream templateInputStream = TemplateUtils.getTemplate(
                    getClass(), ITD_TEMPLATE);

            InputStreamReader readerFile = new InputStreamReader(
                    templateInputStream);

            template = FileCopyUtils.copyToString(readerFile);

        } catch (IOException ioe) {
            throw new IllegalStateException("Unable load ITD jspx template",
                    ioe);
        }

        template = replaceParams(template, params);

        // Output the ITD if there is actual content involved
        // (if there is no content, we continue on to the deletion phase
        // at the bottom of this conditional block)

        Assert.isTrue(template.length() > 0, "The template doesn't exists.");

        MutableFile mutableFile = null;
        if (fileManager.exists(exceptionFilename)) {
            File f = new File(exceptionFilename);
            String existing = null;
            try {
                existing = FileCopyUtils.copyToString(new FileReader(f));
            } catch (IOException ignoreAndJustOverwriteIt) {
            }

            if (!template.equals(existing)) {
                mutableFile = fileManager.updateFile(exceptionFilename);
            }

        } else {
            mutableFile = fileManager.createFile(exceptionFilename);
            Assert.notNull(mutableFile, "Could not create ITD file '"
                    + exceptionFilename + "'");
        }

        try {
            if (mutableFile != null) {
                FileCopyUtils.copy(template.getBytes(),
                        mutableFile.getOutputStream());
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not output '"
                    + mutableFile.getCanonicalPath() + "'", ioe);
        }
    }

    /**
     * Removes Exception view .jspx.
     * 
     * @param exceptionViewName
     *            Exception Name to remove.
     */
    private void removeExceptionView(String exceptionViewName) {

        String exceptionFilename = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/" + exceptionViewName
                        + ".jspx");

        fileManager.delete(exceptionFilename);

    }

    /**
     * Method to replace the parameters set in the Map into the template.
     * 
     * @param template
     *            Template to create a new jspx.
     * @param params
     *            {@link Map} with the specified parameters.
     * 
     * @return {@link String} with the updated parameters values.
     */
    private String replaceParams(String template, Map<String, String> params) {
        for (Entry<String, String> entry : params.entrySet()) {
            template = StringUtils.replace(template, "${" + entry.getKey()
                    + "}", entry.getValue());
        }
        return template;
    }

    /**
     * Updates the selected language file with the title and the description of
     * the new Exception.
     * 
     * @param exceptionName
     *            Name of the Exception.
     * @param exceptionTitle
     *            Title of the Exception.
     * @param exceptionDescription
     *            Description of the Exception.
     */
    private void createMultiLanguageMessages(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String propertyFileName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        SortedSet<FileDetails> propertiesFiles = getPropertiesFiles();

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put("error." + exceptionNameUncapitalize + ".title",
                exceptionTitle);
        params.put(
                "error." + exceptionNameUncapitalize + ".problemdescription",
                exceptionDescription);

        String propertyFilePath = "/WEB-INF/i18n/";
        String canonicalPath;
        String fileName;

        String tmpProperty;
        for (Entry<String, String> entry : params.entrySet()) {

            for (FileDetails fileDetails : propertiesFiles) {

                canonicalPath = fileDetails.getCanonicalPath();

                fileName = propertyFilePath.concat(StringUtils
                        .getFilename(canonicalPath));

                if (propertyFileName.compareTo(fileName.substring(1)) == 0) {

                    tmpProperty = propFileOperations.getProperty(
                            Path.SRC_MAIN_WEBAPP, fileName, entry.getKey());
                    if (tmpProperty == null) {

                        propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
                                propertyFileName, entry.getKey(),
                                entry.getValue());
                    } else if (tmpProperty.compareTo(entry.getValue()) != 0) {
                        propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
                                propertyFileName, entry.getKey(),
                                entry.getValue());
                    }
                } else {

                    // Updates the file if the property doesn't exists.
                    if (propFileOperations.getProperty(Path.SRC_MAIN_WEBAPP,
                            fileName, entry.getKey()) == null) {
                        propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
                                fileName, entry.getKey(), entry.getKey());
                    }
                }
            }

        }

    }

    /**
     * Updates the selected language file with the title and the description of
     * the new Exception.
     * 
     * @param exceptionName
     *            Name of the Exception.
     * @param exceptionTitle
     *            Title of the Exception.
     * @param exceptionDescription
     *            Description of the Exception.
     */
    private void updateMultiLanguageMessages(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String propertyFileName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put("error." + exceptionNameUncapitalize + ".title",
                exceptionTitle);
        params.put(
                "error." + exceptionNameUncapitalize + ".problemdescription",
                exceptionDescription);

        for (Entry<String, String> entry : params.entrySet()) {

            String tmpProperty = propFileOperations.getProperty(
                    Path.SRC_MAIN_WEBAPP, propertyFileName, entry.getKey());
            if (tmpProperty == null) {

                propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
                        propertyFileName, entry.getKey(), entry.getValue());
            } else if (tmpProperty.compareTo(entry.getValue()) != 0) {
                propFileOperations.changeProperty(Path.SRC_MAIN_WEBAPP,
                        propertyFileName, entry.getKey(), entry.getValue());
            }

        }

    }

    /**
     * Removes the Language messages properties of the Exception.
     * 
     * @param exceptionName
     *            Exception Name to remove.
     */
    private void removeMultiLanguageMessages(String exceptionName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        SortedSet<FileDetails> propertiesFiles = getPropertiesFiles();

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put("error." + exceptionNameUncapitalize + ".title", "");
        params.put(
                "error." + exceptionNameUncapitalize + ".problemdescription",
                "");

        String propertyFilePath = "/WEB-INF/i18n/";
        String fileName;
        String canonicalPath;

        for (Entry<String, String> entry : params.entrySet()) {

            for (FileDetails fileDetails : propertiesFiles) {

                canonicalPath = fileDetails.getCanonicalPath();

                fileName = propertyFilePath.concat(StringUtils
                        .getFilename(canonicalPath));

                propFileOperations.removeProperty(Path.SRC_MAIN_WEBAPP,
                        fileName, entry.getKey());
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #setUpGvNIXExceptions()
     */
    public void setUpGvNIXExceptions() {

        String exceptionResolverBeanClass = installWebServletHandlerClasses();
        updateExceptionResolverBean(exceptionResolverBeanClass);

        installMvcArtifacts();

        // java.sql.SQLException
        addNewHandledException("java.sql.SQLException", "SQLException",
                "Se ha producido un error en el acceso a la Base de datos.",
                "es");

        languageExceptionHandled("java.sql.SQLException", "SQLException",
                "There was an error accessing the database.", "en");

        // java.io.IOException
        addNewHandledException("java.io.IOException", "IOException",
                "Existen problemas para enviar o recibir datos.", "es");

        languageExceptionHandled("java.io.IOException", "IOException",
                "There are problems sending or receiving data.", "en");

        // org.springframework.transaction.TransactionException
        addNewHandledException(
                "org.springframework.transaction.TransactionException",
                "TransactionException",
                "Se ha producido un error en la transacción. No se han guardado los datos correctamente.",
                "es");

        languageExceptionHandled(
                "org.springframework.transaction.TransactionException",
                "TransactionException",
                "There was an error in the transaction. No data have been stored properly.",
                "en");

        // java.lang.UnsupportedOperationException
        addNewHandledException("java.lang.UnsupportedOperationException",
                "UnsupportedOperationException",
                "Se ha producido un error no controlado.", "es");

        languageExceptionHandled("java.lang.UnsupportedOperationException",
                "UnsupportedOperationException",
                "There was an unhandled error.", "en");

        // javax.persistence.OptimisticLockException
        addNewHandledException(
                "javax.persistence.OptimisticLockException",
                "OptimisticLockException",
                "No se puede actualizar el registro debido a que ha sido actualizado previamente.",
                "es");

        languageExceptionHandled(
                "javax.persistence.OptimisticLockException",
                "OptimisticLockException",
                "Can not update the record because it has been previously updated.",
                "en");

    }

    /**
     * Installs MVC Artifacts into current project<br/>
     * Artifacts installed:<br/>
     * <ul>
     * <li>message-box.tagx</li>
     * </ul>
     * 
     * Also adds needed i18n properties to right message_xx.properties files
     */
    private void installMvcArtifacts() {
        // copy util to tags/util
        copyDirectoryContents("tags/util/*.tagx", pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/util"));
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
     * Adds the element util:message-box in the right place in default.jspx
     * layout
     */
    private void addMessageBoxInLayout() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String defaultJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/layouts/default.jspx");

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return;
        }

        InputStream defulatJspxIs = fileManager.getInputStream(defaultJspx);

        Document defaultJspxXml;
        try {
            defaultJspxXml = XmlUtils.getDocumentBuilder().parse(defulatJspxIs);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open load-scripts.tagx file", ex);
        }

        Element lsHtml = defaultJspxXml.getDocumentElement();

        Element messageBoxElement = XmlUtils.findFirstElementByName(
                "message-box", lsHtml);
        if (messageBoxElement == null) {
            // Add utils tag lib as attribute in html element
            lsHtml.setAttribute("xmlns:util",
                    "urn:jsptagdir:/WEB-INF/tags/util");
            Element divMain = XmlUtils.findFirstElement(
                    "/html/body/div/div[@id='main']", lsHtml);
            Element insertAttributeBodyElement = XmlUtils.findFirstElement(
                    "/html/body/div/div/insertAttribute[@name='body']", lsHtml);
            Element messageBox = new XmlElementBuilder("util:message-box",
                    defaultJspxXml).build();
            divMain.insertBefore(messageBox, insertAttributeBodyElement);
        }

        writeToDiskIfNecessary(defaultJspx, defaultJspxXml.getDocumentElement());

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
     * Installs Java classes for MessageMappapingExceptionResolver support
     * 
     * @return
     */
    private String installWebServletHandlerClasses() {
        String classPackage = installWebServletHandlerClass("MessageMappingExceptionResolver");
        installWebServletHandlerClass("ModalDialog");
        return classPackage.concat(".MessageMappingExceptionResolver");
    }

    /**
     * Installs the Java class given by its className
     * 
     * @param className
     * @return
     */
    private String installWebServletHandlerClass(String className) {
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
                            "-template.java"));

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
                        .concat(className).concat("-template.java template"),
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
     * Returns the Java class full qualified name given className and based on
     * the package of App. controllers (classes annotated with @Controller)
     * 
     * @param className
     * @return
     */
    private String getClassFullQualifiedName(String className) {
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

    /**
     * Retrieves the messages properties files of the application
     * 
     * @return {@link SortedSet} with the messages properties files
     */
    private SortedSet<FileDetails> getPropertiesFiles() {

        SortedSet<FileDetails> propertiesFiles = fileManager
                .findMatchingAntPath(pathResolver.getIdentifier(
                        Path.SRC_MAIN_WEBAPP, LANGUAGE_FILENAMES));

        return propertiesFiles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #isExceptionMappingAvailable()
     */
    public boolean isExceptionMappingAvailable() {

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");

        if (!fileManager.exists(webXmlPath)) {
            return false;
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

        Element simpleMappingExceptionResolverProp = XmlUtils
                .findFirstElement(
                        "/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']"
                                + "/property[@name='exceptionMappings']/props/prop",
                        root);

        boolean isExceptionAvailable = (simpleMappingExceptionResolverProp != null) ? true
                : false;

        return isExceptionAvailable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #isExceptionMappingAvailable()
     */
    public boolean isMessageMappingAvailable() {

        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");

        if (!fileManager.exists(webXmlPath)) {
            return false;
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

        Element simpleMappingExceptionResolverProp = XmlUtils.findFirstElement(
                "/beans/bean[@id='messageMappingExceptionResolverBean']"
                        + "/property[@name='exceptionMappings']/props/prop",
                root);

        boolean isExceptionAvailable = (simpleMappingExceptionResolverProp != null) ? true
                : false;

        return isExceptionAvailable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #isProjectAvailable()
     */
    public boolean isProjectAvailable() {
        return OperationUtils.isProjectAvailable(metadataService)
                && OperationUtils.isSpringMvcProject(metadataService,
                        fileManager);
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
