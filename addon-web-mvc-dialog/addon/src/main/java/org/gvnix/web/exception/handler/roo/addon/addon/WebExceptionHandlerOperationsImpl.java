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
package org.gvnix.web.exception.handler.roo.addon.addon;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.WebProjectUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperationsImpl;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Exception commands that are available via the Roo shell.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component
@Service
public class WebExceptionHandlerOperationsImpl implements
        WebExceptionHandlerOperations {

    private static final String ITD_TEMPLATE = "exception.jspx";

    private static final String ENGLISH_LANGUAGE_FILENAME = "/WEB-INF/i18n/messages.properties";

    private static final String LANGUAGE_FILENAMES = "WEB-INF/i18n/messages**.properties";

    private static final String FOLDER_SEPARATOR = "/";

    private static final Logger LOGGER = HandlerUtils
            .getLogger(WebModalDialogOperationsImpl.class);

    private static final String WEB_MVC_CONFIG = "WEB-INF/spring/webmvc-config.xml";

    private static final String WEB_MVC_CONFIG_NOT_FOUND = "webmvc-config.xml not found";

    private static final String RESOLVER_BEAN_MESSAGE = "/beans/bean[@id='messageMappingExceptionResolverBean']";

    private static final String JSPX_EXTENSION = ".jspx";

    private static final String ERROR = "error.";

    private static final String TITLE = ".title";

    private static final String PROBLEM_DESCRIPTION = ".problemdescription";

    private static final String IMP_IDENTIFIER_GENERATION_EXCEPTION = "org.hibernate.id.IdentifierGenerationException";

    private static final String IDENTIFIER_GENERATION_EXCEPTION = "IdentifierGenerationException";

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private TilesOperations tilesOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private PropFileOperations propFileOperations;
    @Reference
    private I18nSupport i18nSupport;

    private WebProjectUtils webProjectUtils;
    private MessageBundleUtils messageBundleUtils;
    private OperationUtils operationUtils;

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gvnix.web.exception.handler.roo.addon.WebExceptionHandlerOperations
     * #getHandledExceptionList()
     */
    public String getHandledExceptionList() {

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                WEB_MVC_CONFIG);
        Validate.isTrue(fileManager.exists(webXmlPath),
                WEB_MVC_CONFIG_NOT_FOUND);

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        List<Element> simpleMappingException = null;
        simpleMappingException = XmlUtils.findElements(RESOLVER_BEAN_MESSAGE
                + "/property[@name='exceptionMappings']/props/prop", root);

        Validate.notNull(simpleMappingException,
                "There aren't Exceptions handled by the application.");

        StringBuilder exceptionList = new StringBuilder("Handled Exceptions:\n");

        for (Element element : simpleMappingException) {
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

        Validate.notNull(exceptionName, "You have to provide a Exception Name.");
        Validate.notNull(exceptionTitle,
                "You have to provide a Exception Title.");
        Validate.notNull(exceptionDescription,
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

        Validate.notNull(exceptionName, "You have to provide a Exception Name.");

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

        getMessageBundleUtils().installI18nMessages(
                i18nSupport.getLanguage(new Locale(exceptionLanguage)),
                projectOperations, fileManager);

        String languagePath;

        if (exceptionLanguage.compareTo("en") == 0) {
            languagePath = ENGLISH_LANGUAGE_FILENAME;
        }
        else {
            languagePath = "WEB-INF/i18n/messages_" + exceptionLanguage
                    + ".properties";
        }

        String messagesPath = pathResolver
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        languagePath);

        Validate.isTrue(
                fileManager.exists(messagesPath),
                languagePath
                        + "\t Language properties file not found.\nTry another Language: [es, de, it, nl, sv, en].");
        return languagePath;
    }

    /**
     * Checks if the Exception is mapped in the SimpleMappingExceptionResolver
     * Controller
     * 
     * @param exceptionName Exception Name to Handle.
     */
    private void existException(String exceptionName) {
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                WEB_MVC_CONFIG);
        Validate.isTrue(fileManager.exists(webXmlPath),
                WEB_MVC_CONFIG_NOT_FOUND);

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element exceptionResolver = XmlUtils
                .findFirstElement(
                        RESOLVER_BEAN_MESSAGE
                                + "/property[@name='exceptionMappings']/props/prop[@key='"
                                + exceptionName + "']", root);

        Validate.isTrue(exceptionResolver != null,
                "There isn't a Exception Handled with the name:\t"
                        + exceptionName);
    }

    /**
     * Update the webmvc-config.xml with the new Exception.
     * 
     * @param exceptionName Exception Name to Handle.
     * @return {@link String} The exceptionViewName to create the .jspx view.
     */
    protected String updateWebMvcConfig(String exceptionName) {

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                WEB_MVC_CONFIG);
        Validate.isTrue(fileManager.exists(webXmlPath),
                WEB_MVC_CONFIG_NOT_FOUND);

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element simpleMappingException = XmlUtils.findFirstElement(
                RESOLVER_BEAN_MESSAGE
                        + "/property[@name='exceptionMappings']/props", root);

        Element exceptionResolver = XmlUtils
                .findFirstElement(
                        RESOLVER_BEAN_MESSAGE
                                + "/property[@name='exceptionMappings']/props/prop[@key='"
                                + exceptionName + "']", root);

        boolean updateMappings = false;
        boolean updateController = false;

        // View name for this Exception.
        String exceptionViewName;

        if (exceptionResolver != null) {
            exceptionViewName = exceptionResolver.getTextContent();
        }
        else {
            updateMappings = true;
            exceptionViewName = getExceptionViewName(exceptionName);
        }

        Element newExceptionMapping;

        // Exception Mapping
        newExceptionMapping = webXml.createElement("prop");
        newExceptionMapping.setAttribute("key", exceptionName);

        Validate.isTrue(exceptionViewName != null,
                "Can't create the view for the:\t" + exceptionName
                        + " Exception.");

        newExceptionMapping.setTextContent(exceptionViewName);

        if (updateMappings) {
            simpleMappingException.appendChild(newExceptionMapping);
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
     * @param exceptionName to create the view.
     * @param root {@link Element} with the values off webmvc-config.xml
     * @return
     */
    private String getExceptionViewName(String exceptionName) {

        // View name for this Exception.
        int index = exceptionName.lastIndexOf('.');
        String exceptionViewName = exceptionName;

        if (index >= 0) {
            exceptionViewName = exceptionName.substring(index + 1);
        }
        exceptionViewName = StringUtils.uncapitalize(exceptionViewName);

        boolean exceptionNameExists = true;

        int exceptionCounter = 2;

        String tmpExceptionViewName = exceptionViewName;

        while (exceptionNameExists) {

            exceptionNameExists = fileManager.exists(pathResolver
                    .getIdentifier(
                            LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                            "WEB-INF/views/" + tmpExceptionViewName
                                    + JSPX_EXTENSION));

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
     * @param exceptionName Exception Name to remove.
     */
    private String removeWebMvcConfig(String exceptionName) {

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                WEB_MVC_CONFIG);
        Validate.isTrue(fileManager.exists(webXmlPath),
                WEB_MVC_CONFIG_NOT_FOUND);

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element exceptionResolver = XmlUtils
                .findFirstElement(
                        RESOLVER_BEAN_MESSAGE
                                + "/property[@name='exceptionMappings']/props/prop[@key='"
                                + exceptionName + "']", root);

        Validate.isTrue(exceptionResolver != null,
                "There isn't a Handled Exception with the name:\t"
                        + exceptionName);

        // Remove Mapping
        exceptionResolver.getParentNode().removeChild(exceptionResolver);

        String exceptionViewName = exceptionResolver.getTextContent();

        Validate.isTrue(exceptionViewName != null,
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
     * @param exceptionViewName to create the view and the definition in the xml
     */
    private void updateViewsLayout(String exceptionViewName) {

        // Exception view - create a view to show the exception message.

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/views.xml");
        Validate.isTrue(fileManager.exists(webXmlPath), "views.xml not found");

        String jspxPath = "/WEB-INF/views/" + exceptionViewName
                + JSPX_EXTENSION;

        tilesOperations.addViewDefinition("",
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                exceptionViewName, TilesOperationsImpl.PUBLIC_TEMPLATE,
                jspxPath);

    }

    /**
     * Removes the definition of the Exception view in layout.xml.
     * 
     * @param exceptionViewName Exception Name to remove.
     */
    private void removeViewsLayout(String exceptionViewName) {

        // Exception view - create a view to show the exception message.

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/views.xml");
        Validate.isTrue(fileManager.exists(webXmlPath), "views.xml not found");

        tilesOperations.removeViewDefinition(exceptionViewName, "",
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""));
    }

    /**
     * Creates a view for the new Handled Exception.
     * 
     * @param exceptionName Name of the Exception to handle.
     * @param exceptionTitle Title of the exception view.
     * @param exceptionDescription Description to show in the view.
     * @param exceptionViewName Name of the .jspx view.
     */
    private void createNewExceptionView(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String exceptionViewName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put("error.uncaughtexception.title", ERROR
                + exceptionNameUncapitalize + TITLE);
        params.put("error.uncaughtexception.problemdescription", ERROR
                + exceptionNameUncapitalize + PROBLEM_DESCRIPTION);

        String exceptionFilename = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/" + exceptionViewName + JSPX_EXTENSION);
        String template;
        try {

            InputStream templateInputStream = FileUtils.getInputStream(
                    getClass(), ITD_TEMPLATE);

            InputStreamReader readerFile = new InputStreamReader(
                    templateInputStream);

            template = IOUtils.toString(readerFile);

        }
        catch (IOException ioe) {
            throw new IllegalStateException("Unable load ITD jspx template",
                    ioe);
        }

        template = replaceParams(template, params);

        // Output the ITD if there is actual content involved
        // (if there is no content, we continue on to the deletion phase
        // at the bottom of this conditional block)

        Validate.isTrue(template.length() > 0, "The template doesn't exists.");

        MutableFile mutableFile = null;
        if (fileManager.exists(exceptionFilename)) {
            File newFile = new File(exceptionFilename);
            String existing = null;
            try {
                existing = IOUtils.toString(new FileReader(newFile));
            }
            catch (IOException ignoreAndJustOverwriteIt) {
                LOGGER.finest("Problems writting ".concat(newFile
                        .getAbsolutePath()));
            }

            if (!template.equals(existing)) {
                mutableFile = fileManager.updateFile(exceptionFilename);
            }

        }
        else {
            mutableFile = fileManager.createFile(exceptionFilename);
            Validate.notNull(mutableFile, "Could not create ITD file '"
                    + exceptionFilename + "'");
        }

        try {
            if (mutableFile != null) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(template);
                    outputStream = mutableFile.getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        }
        catch (IOException ioe) {
            throw new IllegalStateException("Could not output '"
                    + mutableFile.getCanonicalPath() + "'", ioe);
        }
    }

    /**
     * Removes Exception view .jspx.
     * 
     * @param exceptionViewName Exception Name to remove.
     */
    private void removeExceptionView(String exceptionViewName) {

        String exceptionFilename = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/" + exceptionViewName + JSPX_EXTENSION);

        fileManager.delete(exceptionFilename);

    }

    /**
     * Method to replace the parameters set in the Map into the template.
     * 
     * @param template Template to create a new jspx.
     * @param params {@link Map} with the specified parameters.
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
     * @param exceptionName Name of the Exception.
     * @param exceptionTitle Title of the Exception.
     * @param exceptionDescription Description of the Exception.
     */
    private void createMultiLanguageMessages(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String propertyFileName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        SortedSet<FileDetails> propertiesFiles = getPropertiesFiles();

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put(ERROR + exceptionNameUncapitalize + TITLE, exceptionTitle);
        params.put(ERROR + exceptionNameUncapitalize + PROBLEM_DESCRIPTION,
                exceptionDescription);

        String propertyFilePath = "/WEB-INF/i18n/";
        String canonicalPath;
        String fileName;

        String tmpProperty;
        for (Entry<String, String> entry : params.entrySet()) {

            for (FileDetails fileDetails : propertiesFiles) {

                canonicalPath = fileDetails.getCanonicalPath();

                fileName = propertyFilePath.concat(getFilename(canonicalPath));

                if (propertyFileName.compareTo(fileName.substring(1)) == 0) {

                    tmpProperty = propFileOperations.getProperty(
                            LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                            fileName, entry.getKey());
                    if (tmpProperty == null) {

                        propFileOperations.changeProperty(LogicalPath
                                .getInstance(Path.SRC_MAIN_WEBAPP, ""),
                                propertyFileName, entry.getKey(), entry
                                        .getValue());
                    }
                    else if (tmpProperty.compareTo(entry.getValue()) != 0) {
                        propFileOperations.changeProperty(LogicalPath
                                .getInstance(Path.SRC_MAIN_WEBAPP, ""),
                                propertyFileName, entry.getKey(), entry
                                        .getValue());
                    }
                }
                else {

                    // Updates the file if the property doesn't exists.
                    if (propFileOperations.getProperty(
                            LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                            fileName, entry.getKey()) == null) {
                        propFileOperations.changeProperty(LogicalPath
                                .getInstance(Path.SRC_MAIN_WEBAPP, ""),
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
     * @param exceptionName Name of the Exception.
     * @param exceptionTitle Title of the Exception.
     * @param exceptionDescription Description of the Exception.
     */
    private void updateMultiLanguageMessages(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String propertyFileName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put(ERROR + exceptionNameUncapitalize + TITLE, exceptionTitle);
        params.put(ERROR + exceptionNameUncapitalize + PROBLEM_DESCRIPTION,
                exceptionDescription);

        for (Entry<String, String> entry : params.entrySet()) {

            String tmpProperty = propFileOperations.getProperty(
                    LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                    propertyFileName, entry.getKey());
            if (tmpProperty == null) {

                propFileOperations.changeProperty(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        propertyFileName, entry.getKey(), entry.getValue());
            }
            else if (tmpProperty.compareTo(entry.getValue()) != 0) {
                propFileOperations.changeProperty(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        propertyFileName, entry.getKey(), entry.getValue());
            }

        }

    }

    /**
     * Removes the Language messages properties of the Exception.
     * 
     * @param exceptionName Exception Name to remove.
     */
    private void removeMultiLanguageMessages(String exceptionName) {

        String exceptionNameUncapitalize = StringUtils
                .uncapitalize(exceptionName);

        SortedSet<FileDetails> propertiesFiles = getPropertiesFiles();

        Map<String, String> params = new HashMap<String, String>(10);
        // Parameters
        params.put(ERROR + exceptionNameUncapitalize + TITLE, "");
        params.put(ERROR + exceptionNameUncapitalize + PROBLEM_DESCRIPTION, "");

        String propertyFilePath = "/WEB-INF/i18n/";
        String fileName;
        String canonicalPath;

        for (Entry<String, String> entry : params.entrySet()) {

            for (FileDetails fileDetails : propertiesFiles) {

                canonicalPath = fileDetails.getCanonicalPath();

                fileName = propertyFilePath.concat(getFilename(canonicalPath));

                propFileOperations.removeProperty(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
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

        // java.sql.SQLException
        addNewHandledException("java.sql.SQLException", "SQLException",
                "Se ha producido un error en el acceso a la Base de datos.",
                "es");

        languageExceptionHandled("java.sql.SQLException", "SQLException",
                "S'ha produït un error en l'accés a la Base de dades.", "ca");

        languageExceptionHandled("java.sql.SQLException", "SQLException",
                "There was an error accessing the database.", "en");

        // java.io.IOException
        addNewHandledException("java.io.IOException", "IOException",
                "Existen problemas para enviar o recibir datos.", "es");

        languageExceptionHandled("java.io.IOException", "IOException",
                "Hi ha problemes per enviar o rebre dades.", "ca");

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
                "S'ha produït un error en la transacció. No s'han guardat les dades correctament.",
                "ca");

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
                "S'ha produït un error no controlat.", "ca");

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
                "No es pot actualitzar el registre a causa de que s'ha actualitzat prèviament.",
                "ca");

        languageExceptionHandled(
                "javax.persistence.OptimisticLockException",
                "OptimisticLockException",
                "Can not update the record because it has been previously updated.",
                "en");

        // org.hibernate.NonUniqueObjectException
        addNewHandledException(
                "org.hibernate.NonUniqueObjectException",
                "NonUniqueObjectException",
                "No se puede crear el registro porque existe un objeto distinto con el mismo identificador.",
                "es");

        languageExceptionHandled(
                "org.hibernate.NonUniqueObjectException",
                "NonUniqueObjectException",
                "No es pot crear el registre perque existeix un altre objecte amb el mateix identificador.",
                "ca");

        languageExceptionHandled(
                "org.hibernate.NonUniqueObjectException",
                "NonUniqueObjectException",
                "Can not create the record because a different object with the same identifier value already exists.",
                "en");

        // org.hibernate.exception.ConstraintViolationException
        addNewHandledException(
                "org.hibernate.exception.ConstraintViolationException",
                "ConstraintViolationException",
                "No se puede crear/actualizar el registro porque no se cumple una restricción de integridad.",
                "es");

        languageExceptionHandled(
                "org.hibernate.exception.ConstraintViolationException",
                "ConstraintViolationException",
                "No es pot crear/actualiztar el registre perque no es compleix una restricció d'integritat.",
                "ca");

        languageExceptionHandled(
                "org.hibernate.exception.ConstraintViolationException",
                "ConstraintViolationException",
                "Can not create/update the record because a violation of a defined integrity constraint.",
                "en");

        // org.hibernate.id.IdentifierGenerationException
        addNewHandledException(
                IMP_IDENTIFIER_GENERATION_EXCEPTION,
                IDENTIFIER_GENERATION_EXCEPTION,
                "No se puede crear porque alguno de los campos que que conforman el identificador del registro no ha sido informado.",
                "es");

        languageExceptionHandled(
                IMP_IDENTIFIER_GENERATION_EXCEPTION,
                IDENTIFIER_GENERATION_EXCEPTION,
                "No es pot crear perque algun dels camps que conformen l'identificador del registre no ha estat informat.",
                "ca");

        languageExceptionHandled(
                IMP_IDENTIFIER_GENERATION_EXCEPTION,
                IDENTIFIER_GENERATION_EXCEPTION,
                "Can not create the record because any of the field comprising the identifier of the record has not been informed.",
                "en");

        // org.hibernate.id.IdentifierGenerationException
        addNewHandledException(
                IMP_IDENTIFIER_GENERATION_EXCEPTION,
                IDENTIFIER_GENERATION_EXCEPTION,
                "No se puede crear porque alguno de los campos que que conforman el identificador del registro no ha sido informado.",
                "es");

        languageExceptionHandled(
                IMP_IDENTIFIER_GENERATION_EXCEPTION,
                IDENTIFIER_GENERATION_EXCEPTION,
                "No es pot crear perque algun dels camps que conformen l'identificador del registre no ha estat informat.",
                "ca");

        languageExceptionHandled(
                IMP_IDENTIFIER_GENERATION_EXCEPTION,
                IDENTIFIER_GENERATION_EXCEPTION,
                "Can not create the record because any of the field comprising the identifier of the record has not been informed.",
                "en");

    }

    /**
     * Retrieves the messages properties files of the application
     * 
     * @return {@link SortedSet} with the messages properties files
     */
    private SortedSet<FileDetails> getPropertiesFiles() {

        SortedSet<FileDetails> propertiesFiles = fileManager
                .findMatchingAntPath(pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        LANGUAGE_FILENAMES));

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

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                WEB_MVC_CONFIG);

        if (!fileManager.exists(webXmlPath)) {
            return false;
        }

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element exceptionResolver = XmlUtils
                .findFirstElement(
                        "/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']"
                                + "/property[@name='exceptionMappings']/props/prop",
                        root);

        boolean isExceptionAvailable = (exceptionResolver != null) ? true
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

        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                WEB_MVC_CONFIG);

        if (!fileManager.exists(webXmlPath)) {
            return false;
        }

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element exceptionResolver = XmlUtils.findFirstElement(
                RESOLVER_BEAN_MESSAGE
                        + "/property[@name='exceptionMappings']/props/prop",
                root);

        boolean isExceptionAvailable = (exceptionResolver != null) ? true
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
        return getOperationUtils().isProjectAvailable(metadataService,
                projectOperations)
                && getWebProjectUtils().isSpringMvcProject(metadataService,
                        fileManager, projectOperations);
    }

    /**
     * Extract the filename from the given path, e.g. "mypath/myfile.txt" ->
     * "myfile.txt".
     * 
     * @param path the file path (may be <code>null</code>)
     * @return the extracted filename, or <code>null</code> if none
     */
    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1)
                : path);
    }

    public WebProjectUtils getWebProjectUtils() {
        if (webProjectUtils == null) {
            // Get all Services implement WebProjectUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebProjectUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    webProjectUtils = (WebProjectUtils) this.context
                            .getService(ref);
                    return webProjectUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WebProjectUtils on WebExceptionHandlerOperationsImpl.");
                return null;
            }
        }
        else {
            return webProjectUtils;
        }

    }

    public MessageBundleUtils getMessageBundleUtils() {
        if (messageBundleUtils == null) {
            // Get all Services implement MessageBundleUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MessageBundleUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    messageBundleUtils = (MessageBundleUtils) this.context
                            .getService(ref);
                    return messageBundleUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MessageBundleUtils on WebExceptionHandlerOperationsImpl.");
                return null;
            }
        }
        else {
            return messageBundleUtils;
        }

    }

    public OperationUtils getOperationUtils() {
        if (operationUtils == null) {
            // Get all Services implement OperationUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                OperationUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    operationUtils = (OperationUtils) this.context
                            .getService(ref);
                    return operationUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load OperationUtils on WebExceptionHandlerOperationsImpl.");
                return null;
            }
        }
        else {
            return operationUtils;
        }

    }

}
