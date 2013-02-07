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
package org.gvnix.web.report.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
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
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.springframework.uaa.client.util.Assert;
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
@Component(immediate = true)
@Service
public final class ReportJspMetadataListener implements MetadataProvider,
        MetadataNotificationListener {
    // private static final Logger logger =
    // HandlerUtils.getLogger(ReportJspMetadataListener.class);

    @Reference private MetadataDependencyRegistry metadataDependencyRegistry;
    @Reference private MetadataService metadataService;
    @Reference private FileManager fileManager;
    @Reference private TilesOperations tilesOperations;
    @Reference private MenuOperations menuOperations;
    @Reference private PathResolver pathResolver;
    @Reference private PropFileOperations propFileOperations;

    private WebScaffoldMetadata webScaffoldMetadata;
    private JpaActiveRecordMetadata entityMetadata;
    private JavaType javaType;
    private JavaType formbackingObject;

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                ReportMetadata.getMetadataIdentiferType(), getProvidesType());
    }

    public MetadataItem get(String metadataIdentificationString) {
        javaType = ReportJspMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = ReportJspMetadata
                .getPath(metadataIdentificationString);
        String reportMetadataKey = ReportMetadata.createIdentifier(javaType,
                path);
        ReportMetadata reportMetadata = (ReportMetadata) metadataService
                .get(reportMetadataKey);
        if (reportMetadata == null || !reportMetadata.isValid()) {
            return null;
        }

        webScaffoldMetadata = reportMetadata.getWebScaffoldMetadata();
        Assert.notNull(webScaffoldMetadata, "Web scaffold metadata required");

        formbackingObject = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();

        entityMetadata = (JpaActiveRecordMetadata) metadataService
                .get(JpaActiveRecordMetadata.createIdentifier(
                        formbackingObject, path));
        Assert.notNull(entityMetadata,
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
        String destinationDirectory = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/" + controllerPath);
        if (!fileManager.exists(destinationDirectory)) {
            fileManager.createDirectory(destinationDirectory);
        }
        else {
            File file = new File(destinationDirectory);
            Assert.isTrue(file.isDirectory(), destinationDirectory
                    + " is a file, when a directory was expected");
        }

        Document document = getReportFormJsp(reportNameFormat[0],
                controllerPath);
        writeToDiskIfNecessary(pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views/" + controllerPath + "/" + reportNameFormat[0]
                        + ".jspx"), document);

        Map<String, String> properties = new HashMap<String, String>();

        tilesOperations.addViewDefinition(controllerPath,
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                controllerPath + "/" + reportNameFormat[0],
                TilesOperationsImpl.DEFAULT_TEMPLATE, "/WEB-INF/views/"
                        + controllerPath + "/" + reportNameFormat[0] + ".jspx");
        menuOperations.addMenuItem(
                new JavaSymbolName(formbackingObject.getSimpleTypeName()),
                new JavaSymbolName(reportNameFormat[0] + "_report"), "menu_"
                        + formbackingObject.getSimpleTypeName().toLowerCase()
                        + "_" + reportNameFormat[0] + "_report", "/"
                        + controllerPath + "/reports/" + reportNameFormat[0]
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
        propFileOperations
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

        // Add spring-message definition
        Element h3title = (Element) new XmlElementBuilder("h3", document)
                .build();
        Element titleMessage = (Element) new XmlElementBuilder(
                "spring:message", document)
                .addAttribute("code",
                        "label_report_" + controllerPath + "_" + reportName)
                .addAttribute("htmlEscape", "false").build();
        // Add the message error to the application.properties
        properties.put("label_report_" + controllerPath + "_" + reportName,
                "Report " + reportName);
        h3title.appendChild(titleMessage);

        // Add a conditional error message
        Element ciferror = (Element) new XmlElementBuilder("c:if", document)
                .addAttribute("test", "${not empty error}").build();
        Element h3error = (Element) new XmlElementBuilder("h3", document)
                .build();
        Element errorMessage = (Element) new XmlElementBuilder(
                "spring:message", document).addAttribute("code", "${error}")
                .addAttribute("htmlEscape", "false").build();
        h3error.appendChild(errorMessage);
        ciferror.appendChild(h3error);

        // Add form create element
        Element formForm = new XmlElementBuilder("form:form", document)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("fr_"
                                + formbackingObject.getFullyQualifiedTypeName()))
                .addAttribute("action", reportName)
                .addAttribute("method", "GET").build();

        // Add a drop-down select
        Element cifSelectFormat = (Element) new XmlElementBuilder("c:if",
                document).addAttribute("test", "${not empty report_formats}")
                .build();
        Element selectFormat = (Element) new XmlElementBuilder("select",
                document).addAttribute("id", "_select_format")
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
        // Two <br/> putting a blank space between drop-down select and submit
        // button
        cifSelectFormat.appendChild(new XmlElementBuilder("br", document)
                .build());
        cifSelectFormat.appendChild(new XmlElementBuilder("br", document)
                .build());

        formForm.appendChild(cifSelectFormat);

        formForm.appendChild(new XmlElementBuilder("input", document)
                .addAttribute("type", "submit").build());

        div.appendChild(h3title);
        div.appendChild(ciferror);
        div.appendChild(formForm);

        propFileOperations
                .addProperties(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);

        return document;
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Assert.isTrue(
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
            if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                    .contains(downstreamDependency)) {
                return;
            }
        }

        // We should now have an instance-specific "downstream dependency" that
        // can be processed by this class
        Assert.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        metadataService.evict(downstreamDependency);
        if (get(downstreamDependency) != null) {
            metadataDependencyRegistry.notifyDownstream(downstreamDependency);
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
        if (fileManager.exists(jspFilename)) {
            try {
                original = XmlUtils.getDocumentBuilder().parse(
                        fileManager.getInputStream(jspFilename));
            }
            catch (Exception e) {
                throw new IllegalStateException("Could not parse file: "
                        + jspFilename);
            }
            Assert.notNull(original, "Unable to parse " + jspFilename);
            if (XmlRoundTripUtils.compareDocuments(original, proposed)) {
                mutableFile = fileManager.updateFile(jspFilename);
            }
        }
        else {
            original = proposed;
            mutableFile = fileManager.createFile(jspFilename);
            Assert.notNull(mutableFile, "Could not create JSP file '"
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

}
