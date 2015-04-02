/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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
package org.gvnix.web.report.roo.addon.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.addon.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperationsImpl;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Metadata listener.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
@Component
@Service
public final class ReportJspMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(ReportJspMetadataListener.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private MetadataDependencyRegistry metadataDependencyRegistry;
    private MetadataService metadataService;
    private FileManager fileManager;
    private TilesOperations tilesOperations;
    private MenuOperations menuOperations;
    private PathResolver pathResolver;
    private PropFileOperations propFileOperations;

    private WebScaffoldMetadata webScaffoldMetadata;
    private JpaActiveRecordMetadata entityMetadata;
    private JavaType javaType;
    private JavaType formbackingObject;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                ReportMetadata.getMetadataIdentiferType(), getProvidesType());
    }

    public MetadataItem get(String metadataIdentificationString) {
        javaType = ReportJspMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = ReportJspMetadata
                .getPath(metadataIdentificationString);
        String reportMetadataKey = ReportMetadata.createIdentifier(javaType,
                path);
        ReportMetadata reportMetadata = (ReportMetadata) getMetadataService()
                .get(reportMetadataKey);
        if (reportMetadata == null || !reportMetadata.isValid()) {
            return null;
        }

        webScaffoldMetadata = reportMetadata.getWebScaffoldMetadata();
        Validate.notNull(webScaffoldMetadata, "Web scaffold metadata required");

        formbackingObject = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();

        entityMetadata = (JpaActiveRecordMetadata) getMetadataService().get(
                JpaActiveRecordMetadata.createIdentifier(formbackingObject,
                        path));
        Validate.notNull(entityMetadata,
                "Could not determine entity metadata for type: "
                        + formbackingObject.getFullyQualifiedTypeName());

        for (String installedReport : reportMetadata.getInstalledReports()) {
            installMvcArtifacts(installedReport);
        }

        return new ReportJspMetadata(metadataIdentificationString,
                reportMetadata);
    }

    /**
     * Given a reportFormat it generates/updates the JSPX showing the form with
     * the generate report submit button.
     * 
     * @param report
     */
    public void installMvcArtifacts(String report) {
        String[] reportNameFormat = ReportMetadata
                .stripGvNixReportValue(report);

        String controllerPath = webScaffoldMetadata.getAnnotationValues()
                .getPath();
        // Make the holding directory for this controller
        String destinationDirectory = getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/" + controllerPath);
        if (!getFileManager().exists(destinationDirectory)) {
            getFileManager().createDirectory(destinationDirectory);
        }
        else {
            File file = new File(destinationDirectory);
            Validate.isTrue(file.isDirectory(), destinationDirectory
                    + " is a file, when a directory was expected");
        }

        Document document = getReportFormJsp(reportNameFormat[0],
                controllerPath);
        writeToDiskIfNecessary(
                getPathResolver().getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "WEB-INF/views/" + controllerPath + "/"
                                + reportNameFormat[0] + ".jspx"), document);

        Map<String, String> properties = new HashMap<String, String>();

        getTilesOperations().addViewDefinition(
                controllerPath,
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                controllerPath + "/" + reportNameFormat[0],
                TilesOperationsImpl.DEFAULT_TEMPLATE,
                "/WEB-INF/views/" + controllerPath + "/" + reportNameFormat[0]
                        + ".jspx");
        getMenuOperations().addMenuItem(
                new JavaSymbolName(formbackingObject.getSimpleTypeName()),
                new JavaSymbolName(reportNameFormat[0] + "_report"),
                "menu_" + formbackingObject.getSimpleTypeName().toLowerCase()
                        + "_" + reportNameFormat[0] + "_report",
                "/" + controllerPath + "/reports/" + reportNameFormat[0]
                        + "?form", MenuOperations.DEFAULT_MENU_ITEM_PREFIX,
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""));
        properties.put("menu_"
                + formbackingObject.getSimpleTypeName().toLowerCase() + "_"
                + reportNameFormat[0] + "_report", new JavaSymbolName(
                formbackingObject.getSimpleTypeName()).getReadableSymbolName()
                + " " + reportNameFormat[0] + " Report");
        properties.put("menu_item_"
                + formbackingObject.getSimpleTypeName().toLowerCase() + "_"
                + reportNameFormat[0] + "_report_label", new JavaSymbolName(
                formbackingObject.getSimpleTypeName()).getReadableSymbolName()
                + " " + reportNameFormat[0] + " Report");

        // Add the message error to the application.properties
        getPropFileOperations()
                .addProperties(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);
    }

    /**
     * Generates a JSPX with a form requesting the report. The form has as many
     * radio buttons as formats has set the report.
     * 
     * @param reportName
     * @param formats
     * @param controllerPath
     * @return
     */
    private Document getReportFormJsp(String reportName, String controllerPath) {
        DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        Document document = builder.newDocument();

        Map<String, String> properties = new HashMap<String, String>();

        // Add document namespaces
        Element div = (Element) document.appendChild(new XmlElementBuilder(
                "div", document)
                .addAttribute("xmlns:c", "http://java.sun.com/jsp/jstl/core")
                .addAttribute("xmlns:fn",
                        "http://java.sun.com/jsp/jstl/functions")
                .addAttribute("xmlns:spring",
                        "http://www.springframework.org/tags")
                .addAttribute("xmlns:jsp", "http://java.sun.com/JSP/Page")
                .addAttribute("xmlns:form",
                        "http://www.springframework.org/tags/form")
                .addAttribute("version", "2.0")
                .addChild(
                        new XmlElementBuilder("jsp:directive.page", document)
                                .addAttribute("contentType",
                                        "text/html;charset=UTF-8").build())
                .addChild(
                        new XmlElementBuilder("jsp:output", document)
                                .addAttribute("omit-xml-declaration", "yes")
                                .build()).build());

        // Add div panel
        Element divPanel = (Element) new XmlElementBuilder("div", document)
                .addAttribute("class", "panel panel-default").build();

        // Add div header
        Element divPanelHeader = (Element) new XmlElementBuilder("div",
                document).addAttribute("class", "panel-heading").build();

        // Add h3 title with content
        Element h3PanelTitle = (Element) new XmlElementBuilder("h3", document)
                .addAttribute("class", "panel-title")
                .addChild(
                        new XmlElementBuilder("spring:message", document)
                                .addAttribute(
                                        "code",
                                        "label_report_" + controllerPath + "_"
                                                + reportName)
                                .addAttribute("htmlEscape", "false").build())
                .build();

        // Adding title panel to panel header
        divPanelHeader.appendChild(h3PanelTitle);

        // Adding panel header to div panel
        divPanel.appendChild(divPanelHeader);

        // Add div panel body
        Element divPanelBody = (Element) new XmlElementBuilder("div", document)
                .addAttribute("class", "panel-body").build();

        // Add if not empty error
        Element ifNotEmptyError = (Element) new XmlElementBuilder("c:if",
                document).addAttribute("test", "${not empty error}").build();

        // Add h3 title with error message
        Element h3ErrorMessage = (Element) new XmlElementBuilder("h3", document)
                .addAttribute("class", "panel-title")
                .addChild(
                        new XmlElementBuilder("spring:message", document)
                                .addAttribute("code", "${error}")
                                .addAttribute("htmlEscape", "false").build())
                .build();

        // Adding h3 message to if empty error
        ifNotEmptyError.appendChild(h3ErrorMessage);

        // Adding if not empty error to divPanelBody
        divPanelBody.appendChild(ifNotEmptyError);

        // Add form element
        Element formElement = (Element) new XmlElementBuilder("form:form",
                document)
                .addAttribute("class", "form-horizontal")
                .addAttribute("role", "form")
                .addAttribute("action", reportName)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("fr_"
                                + formbackingObject.getFullyQualifiedTypeName()))
                .addAttribute("method", "GET").build();

        // Add div control group
        Element divControlGroup = (Element) new XmlElementBuilder("div",
                document).addAttribute("class", "control-group form-group")
                .build();

        // Add div controls col
        Element divControlsCol = (Element) new XmlElementBuilder("div",
                document).addAttribute("class",
                "controls col-xs-7 col-sm-8 col-md-12 col-lg-12").build();

        // Add a drop-down select
        Element cifSelectFormat = (Element) new XmlElementBuilder("c:if",
                document).addAttribute("test", "${not empty report_formats}")
                .build();
        Element selectFormat = (Element) new XmlElementBuilder("select",
                document).addAttribute("class", "form-control input-sm")
                .addAttribute("id", "_select_format")
                .addAttribute("name", "format").build();
        Element cforEach = (Element) new XmlElementBuilder("c:forEach",
                document).addAttribute("items", "${report_formats}")
                .addAttribute("var", "format").build();
        Element optionFormat = (Element) new XmlElementBuilder("option",
                document).addAttribute("id", "option_format_${format}")
                .addAttribute("value", "${format}").build();
        Element coutFormat = (Element) new XmlElementBuilder("c:out", document)
                .addAttribute("value", "${fn:toUpperCase(format)}").build();
        optionFormat.appendChild(coutFormat);
        cforEach.appendChild(optionFormat);
        selectFormat.appendChild(cforEach);
        cifSelectFormat.appendChild(selectFormat);

        // Add input element
        Element inputElement = (Element) new XmlElementBuilder("input",
                document).addAttribute("class", "btn btn-primary btn-block")
                .addAttribute("type", "submit").build();

        // Adding elements to divControlsCol
        divControlsCol.appendChild(cifSelectFormat);
        divControlsCol.appendChild(inputElement);

        // Adding controls col to div control group
        divControlGroup.appendChild(divControlsCol);

        // Adding control group to form
        formElement.appendChild(divControlGroup);

        // Adding form to Panel Body
        divPanelBody.appendChild(formElement);

        // Adding Panel Body to Main Panel
        divPanel.appendChild(divPanelBody);

        // Adding Main Panel to general div
        div.appendChild(divPanel);

        // Add the message error to the application.properties
        properties.put("label_report_" + controllerPath + "_" + reportName,
                "Report " + reportName);

        getPropFileOperations()
                .addProperties(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);

        return document;
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Validate.isTrue(
                    MetadataIdentificationUtils.getMetadataClass(
                            upstreamDependency).equals(
                            MetadataIdentificationUtils
                                    .getMetadataClass(ReportMetadata
                                            .getMetadataIdentiferType())),
                    "Expected class-level notifications only for gvNIX Report metadata (not '"
                            + upstreamDependency + "')");

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            JavaType javaType = ReportMetadata.getJavaType(upstreamDependency);
            LogicalPath path = ReportMetadata.getPath(upstreamDependency);
            downstreamDependency = ReportJspMetadata.createIdentifier(javaType,
                    path);

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (getMetadataDependencyRegistry().getDownstream(
                    upstreamDependency).contains(downstreamDependency)) {
                return;
            }
        }

        // We should now have an instance-specific "downstream dependency" that
        // can be processed by this class
        Validate.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        getMetadataService().evict(downstreamDependency);
        if (get(downstreamDependency) != null) {
            getMetadataDependencyRegistry().notifyDownstream(
                    downstreamDependency);
        }

    }

    public String getProvidesType() {
        return ReportJspMetadata.getMetadataIdentiferType();
    }

    /** return indicates if disk was changed (ie updated or created) */
    private boolean writeToDiskIfNecessary(String jspFilename, Document proposed) {
        Document original = null;

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (getFileManager().exists(jspFilename)) {
            try {
                original = XmlUtils.getDocumentBuilder().parse(
                        getFileManager().getInputStream(jspFilename));
            }
            catch (Exception e) {
                throw new IllegalStateException("Could not parse file: "
                        + jspFilename);
            }
            Validate.notNull(original, "Unable to parse " + jspFilename);
            if (XmlRoundTripUtils.compareDocuments(original, proposed)) {
                mutableFile = getFileManager().updateFile(jspFilename);
            }
        }
        else {
            original = proposed;
            mutableFile = getFileManager().createFile(jspFilename);
            Validate.notNull(mutableFile, "Could not create JSP file '"
                    + jspFilename + "'");
        }

        if (mutableFile != null) {
            try {
                // Build a string representation of the JSP
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
                        byteArrayOutputStream, original);
                String jspContent = byteArrayOutputStream.toString();

                // We need to write the file out (it's a new file, or the
                // existing file has different contents)
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(jspContent);
                    outputStream = mutableFile.getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
                // Return and indicate we wrote out the file
                return true;
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }

    public MetadataDependencyRegistry getMetadataDependencyRegistry() {
        if (metadataDependencyRegistry == null) {
            // Get all Services implement MetadataDependencyRegistry interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataDependencyRegistry.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataDependencyRegistry) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataDependencyRegistry on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return metadataDependencyRegistry;
        }

    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return metadataService;
        }

    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return fileManager;
        }

    }

    public TilesOperations getTilesOperations() {
        if (tilesOperations == null) {
            // Get all Services implement TilesOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TilesOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TilesOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TilesOperations on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return tilesOperations;
        }

    }

    public MenuOperations getMenuOperations() {
        if (menuOperations == null) {
            // Get all Services implement MenuOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MenuOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MenuOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MenuOperations on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return menuOperations;
        }

    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (PathResolver) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return pathResolver;
        }

    }

    public PropFileOperations getPropFileOperations() {
        if (propFileOperations == null) {
            // Get all Services implement PropFileOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                PropFileOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (PropFileOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PropFileOperations on ReportJspMetadataListener.");
                return null;
            }
        }
        else {
            return propFileOperations;
        }

    }

}
