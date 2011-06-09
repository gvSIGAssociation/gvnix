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
package org.gvnix.web.screen.roo.addon;

import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.model.JavaSymbolName;
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
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Abstract Listener gives support for install/create/modify MVC artifacts
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public abstract class AbstractPatternJspMetadataListener implements
        MetadataProvider, MetadataNotificationListener {

    protected FileManager _fileManager;
    protected TilesOperations _tilesOperations;
    protected MenuOperations _menuOperations;
    protected ProjectOperations _projectOperations;
    protected PropFileOperations _propFileOperations;

    protected ComponentContext context;
    protected WebScaffoldMetadata webScaffoldMetadata;
    protected JavaType formbackingType;
    protected String entityName;
    protected Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes;
    protected JavaTypePersistenceMetadataDetails formbackingTypePersistenceMetadata;
    protected JavaTypeMetadataDetails formbackingTypeMetadata;
    protected List<FieldMetadata> eligibleFields;
    protected WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    /**
     * For the given pattern it install needed MVC artifacts and generates the
     * pattern JSPx
     * 
     * @param pattern
     */
    protected void installMvcArtifacts(String pattern) {
        installPatternArtifacts();

        String[] patternNameType = pattern.split("=");

        PathResolver pathResolver = _projectOperations.getPathResolver();
        String controllerPath = webScaffoldMetadata.getAnnotationValues()
                .getPath();
        Assert.notNull(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + webScaffoldMetadata.getAnnotationValues()
                                .getGovernorTypeDetails().getName() + "'");
        if (controllerPath.startsWith("/")) {
            controllerPath = controllerPath.substring(1);
        }

        // Make the holding directory for this controller
        String destinationDirectory = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/" + controllerPath);
        if (!_fileManager.exists(destinationDirectory)) {
            _fileManager.createDirectory(destinationDirectory);
        } else {
            File file = new File(destinationDirectory);
            Assert.isTrue(file.isDirectory(), destinationDirectory
                    + " is a file, when a directory was expected");
        }

        /*
         * TODO: next test may be replaced by a test over allow or not create
         * operation of the entity
         */
        if (patternNameType[1].equalsIgnoreCase(WebPattern.tabular.name())) {
            installPatternTypeArtifact(WebPattern.tabular,
                    destinationDirectory, controllerPath, patternNameType[0]);
        } else if (patternNameType[1].equalsIgnoreCase(WebPattern.register
                .name())) {
            installPatternTypeArtifact(WebPattern.register,
                    destinationDirectory, controllerPath, patternNameType[0]);
        } else {
            // Pattern type not supported. Nothing to do
        }

        // Modify some Roo JSPx
        modifyRooJsp(RooJspx.create);
        modifyRooJsp(RooJspx.update);
        modifyRooJsp(RooJspx.show);

    }

    /**
     * Creates a JSPx of the given WebPattern type
     * 
     * @param patternType
     * @param destinationDirectory
     * @param controllerPath
     * @param patternName
     */
    private void installPatternTypeArtifact(WebPattern patternType,
            String destinationDirectory, String controllerPath,
            String patternName) {
        String patternTypeStr = patternType.name();
        String patternPath = destinationDirectory.concat("/")
                .concat(patternName).concat(".jspx");
        // Get the document for the pattern type
        Document jspDoc = patternType.equals(WebPattern.tabular) ? getUpdateTabularDocument()
                : getRegisterDocument(patternName);
        writeToDiskIfNecessary(patternPath, jspDoc);

        // add view to views.xml
        _tilesOperations.addViewDefinition(controllerPath, controllerPath + "/"
                + patternName, TilesOperations.DEFAULT_TEMPLATE,
                "/WEB-INF/views/" + controllerPath + "/" + patternName
                        + ".jspx");
        // add entry to menu.jspx
        JavaSymbolName categoryName = new JavaSymbolName(
                formbackingType.getSimpleTypeName());
        JavaSymbolName menuItemId = new JavaSymbolName("list_"
                .concat(patternTypeStr).concat("_").concat(patternName));
        String queryString = patternType.equals(WebPattern.tabular) ? "?gvnixpattern="
                .concat(patternName) : "?gvnixform&gvnixpattern=".concat(
                patternName).concat(
                "&index=${empty param.index ? 1 : param.index}");
        if (!isRelatedPattern()) {
            _menuOperations.addMenuItem(categoryName, menuItemId, "menu_list_"
                    .concat(patternTypeStr).concat("_").concat(patternName),
                    "/" + controllerPath + queryString,
                    MenuOperations.DEFAULT_MENU_ITEM_PREFIX);
        }
        // add needed properties
        Map<String, String> properties = new LinkedHashMap<String, String>();
        if (!isRelatedPattern()) {
            properties.put(
                    "menu_list_".concat(patternTypeStr).concat("_")
                            .concat(patternName),
                    new JavaSymbolName(formbackingType.getSimpleTypeName())
                            .getReadableSymbolName()
                            + " list ".concat(patternTypeStr).concat(" ")
                            + patternName);
            properties.put(
                    "menu_item_" + categoryName.getSymbolName().toLowerCase()
                            + "_" + menuItemId.getSymbolName().toLowerCase()
                            + "_label",
                    new JavaSymbolName(formbackingType.getSimpleTypeName())
                            .getReadableSymbolName()
                            + " list ".concat(patternTypeStr).concat(" ")
                            + patternName);
            _propFileOperations.addProperties(Path.SRC_MAIN_WEBAPP,
                    "/WEB-INF/i18n/application.properties", properties, true,
                    false);
        }
    }

    /**
     * Returns de XML Document with the JSPx
     * <p>
     * <strong>This method is based in:</strong>
     * {@link org.springframework.roo.addon.web.mvc.jsp.JspViewManager#getShowDocument()}
     * </p>
     * 
     * @return
     */
    private Document getRegisterDocument(String patternName) {
        String controllerPath = webScaffoldAnnotationValues.getPath();
        Assert.notNull(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + webScaffoldAnnotationValues.getGovernorTypeDetails()
                                .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        Document document = builder.newDocument();

        // Add document namespaces
        Element div = (Element) document.appendChild(new XmlElementBuilder(
                "div", document)
                .addAttribute("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/form/fields")
                .addAttribute("xmlns:page",
                        "urn:jsptagdir:/WEB-INF/tags/pattern/form")
                .addAttribute("xmlns:pattern",
                        "urn:jsptagdir:/WEB-INF/tags/pattern")
                .addAttribute("xmlns:jsp", "http://java.sun.com/JSP/Page")
                .addAttribute("version", "2.0")
                .addChild(
                        new XmlElementBuilder("jsp:directive.page", document)
                                .addAttribute("contentType",
                                        "text/html;charset=UTF-8").build())
                .addChild(
                        new XmlElementBuilder("jsp:output", document)
                                .addAttribute("omit-xml-declaration", "yes")
                                .build()).build());

        Element pageShow = new XmlElementBuilder("page:show", document)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("ps:"
                                + formbackingType.getFullyQualifiedTypeName()))
                .addAttribute("object", "${" + entityName.toLowerCase() + "}")
                .addAttribute("path", controllerPath).build();
        if (!webScaffoldAnnotationValues.isCreate()) {
            pageShow.setAttribute("create", "false");
        }
        if (!webScaffoldAnnotationValues.isUpdate()) {
            pageShow.setAttribute("update", "false");
        }
        if (!webScaffoldAnnotationValues.isDelete()) {
            pageShow.setAttribute("delete", "false");
        }
        pageShow.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(pageShow));

        Element divContentPane = new XmlElementBuilder("div", document)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("div:"
                                + formbackingType.getFullyQualifiedTypeName()
                                + "_contentPane"))
                .addAttribute("class", "patternContentPane").build();

        Element divForm = new XmlElementBuilder("div", document)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("div:"
                                + formbackingType.getFullyQualifiedTypeName()
                                + "_formNoedit"))
                .addAttribute("class", "formularios boxNoedit").build();

        divContentPane.appendChild(divForm);

        List<FieldMetadata> fieldsOfRelations = new ArrayList<FieldMetadata>();
        boolean isRelatationship = false;
        // Add field:display elements for each field
        for (FieldMetadata field : eligibleFields) {
            // Ignoring java.util.Map field types (see ROO-194)
            if (field.getFieldType().equals(new JavaType(Map.class.getName()))) {
                continue;
            }
            String fieldName = uncapitalize(field.getFieldName()
                    .getSymbolName());

            Element ul = new XmlElementBuilder("ul", document)
                    .addAttribute("class", "formInline")
                    .addAttribute(
                            "id",
                            XmlUtils.convertId("ul:"
                                    .concat(formbackingType
                                            .getFullyQualifiedTypeName())
                                    .concat(".").concat(fieldName))).build();
            Element li = new XmlElementBuilder("li", document)
                    .addAttribute("class", "size120")
                    .addAttribute(
                            "id",
                            XmlUtils.convertId("li:"
                                    .concat(formbackingType
                                            .getFullyQualifiedTypeName())
                                    .concat(".").concat(fieldName))).build();
            ul.appendChild(li);

            Element fieldDisplay = new XmlElementBuilder("field:display",
                    document)
                    .addAttribute(
                            "id",
                            XmlUtils.convertId("s:"
                                    + formbackingType
                                            .getFullyQualifiedTypeName() + "."
                                    + field.getFieldName().getSymbolName()))
                    .addAttribute("object",
                            "${" + entityName.toLowerCase() + "}")
                    .addAttribute("field", fieldName).build();
            if (field.getFieldType().equals(new JavaType(Date.class.getName()))) {
                fieldDisplay.setAttribute("date", "true");
                fieldDisplay.setAttribute("dateTimePattern", "${" + entityName
                        + "_" + fieldName.toLowerCase() + "_date_format}");
            } else if (field.getFieldType().equals(
                    new JavaType(Calendar.class.getName()))) {
                fieldDisplay.setAttribute("calendar", "true");
                fieldDisplay.setAttribute("dateTimePattern", "${" + entityName
                        + "_" + fieldName.toLowerCase() + "_date_format}");
            } else if (field.getFieldType().isCommonCollectionType()
                    && field.getCustomData().get(
                            PersistenceCustomDataKeys.ONE_TO_MANY_FIELD) != null) {
                if (isRelationVisible(patternName, field.getFieldName()
                        .getSymbolName())) {
                    fieldsOfRelations.add(field);
                    isRelatationship = true;
                }
                // continue;
            }

            if (!isRelatationship) {
                fieldDisplay.setAttribute("z",
                        XmlRoundTripUtils.calculateUniqueKeyFor(fieldDisplay));

                li.appendChild(fieldDisplay);
                divForm.appendChild(ul);
                isRelatationship = false;
            }
        }

        pageShow.appendChild(divContentPane);

        div.appendChild(pageShow);

        if (!fieldsOfRelations.isEmpty()) {
            Element patternRelations = new XmlElementBuilder(
                    "pattern:relations", document)
                    .addAttribute(
                            "id",
                            XmlUtils.convertId("pr:"
                                    + formbackingType
                                            .getFullyQualifiedTypeName() + "."
                                    + patternName))
                    .addAttribute(
                            "render",
                            "${!empty ".concat(entityName.toLowerCase())
                                    .concat("}")).build();
            patternRelations.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(patternRelations));
            for (FieldMetadata fieldMetadata : fieldsOfRelations) {
                String fieldName = uncapitalize(fieldMetadata.getFieldName()
                        .getSymbolName());
                Element patternRelation = new XmlElementBuilder(
                        "pattern:relation", document)
                        .addAttribute(
                                "id",
                                XmlUtils.convertId("pr:"
                                        + formbackingType
                                                .getFullyQualifiedTypeName()
                                        + "." + fieldName))
                        .addAttribute("object",
                                "${" + entityName.toLowerCase() + "}")
                        .addAttribute("field", fieldName)
                        .addAttribute("patternName", patternName)
                        .addAttribute("referenceName", entityName.toLowerCase())
                        .addAttribute(
                                "referenceField",
                                formbackingTypeMetadata.getPersistenceDetails()
                                        .getIdentifierField().getFieldName()
                                        .getSymbolName())
                        .addAttribute(
                                "render",
                                "${!empty ".concat(entityName.toLowerCase())
                                        .concat("}")).build();
                patternRelation.setAttribute("z", XmlRoundTripUtils
                        .calculateUniqueKeyFor(patternRelation));
                patternRelations.appendChild(patternRelation);
            }
            div.appendChild(patternRelations);
        }

        // div.appendChild(messageBox);

        return document;
    }

    /**
     * A relation is visible in a view if it's defined in
     * {@link GvNIXRelationsPattern}
     * 
     * @param patternName
     * @param symbolName
     * @return
     */
    private boolean isRelationVisible(String patternName, String symbolName) {
        ClassOrInterfaceTypeDetails cid = webScaffoldAnnotationValues
                .getGovernorTypeDetails();

        AnnotationMetadata gvNixRelatedPatternAnnotation = MemberFindingUtils
                .getAnnotationOfType(cid.getAnnotations(), new JavaType(
                        GvNIXRelationsPattern.class.getName()));
        if (gvNixRelatedPatternAnnotation != null) {
            AnnotationAttributeValue<?> thisAnnotationValue = gvNixRelatedPatternAnnotation
                    .getAttribute(new JavaSymbolName("value"));

            if (thisAnnotationValue != null) {

                ArrayAttributeValue<?> arrayVal = (ArrayAttributeValue<?>) thisAnnotationValue;

                if (arrayVal != null) {
                    @SuppressWarnings("unchecked")
                    List<StringAttributeValue> values = (List<StringAttributeValue>) arrayVal
                            .getValue();
                    String regexPattern = "(".concat(patternName).concat(")")
                            .concat(".*").concat("(").concat(symbolName)
                            .concat("=\\s*\\w*)");
                    Pattern pattern = Pattern.compile(regexPattern);
                    Matcher matcher = null;
                    for (StringAttributeValue value : values) {
                        matcher = pattern.matcher(value.getValue());
                        if (matcher.find() && matcher.groupCount() == 2) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;

    }

    public enum RooJspx {
        create, update, show;
    }

    /**
     * Modifies create.jspx or update.jspx generate by Roo based on
     * {@link RooJspx} param.
     * <p>
     * It wraps field element into ul/li elements and add a hidden param
     * <code>gvnixpattern</code> and a button
     * 
     * @param rooJspx
     */
    private void modifyRooJsp(RooJspx rooJspx) {
        String controllerPath = webScaffoldAnnotationValues.getPath();
        Assert.notNull(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + webScaffoldAnnotationValues.getGovernorTypeDetails()
                                .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        PathResolver pathResolver = _projectOperations.getPathResolver();
        String docJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/views" + controllerPath + "/" + rooJspx.name()
                        + ".jspx");

        if (!_fileManager.exists(docJspx)) {
            // create.jspx doesn't exist, so nothing to do
            return;
        }

        InputStream docJspxIs = _fileManager.getInputStream(docJspx);

        Document docJspXml;
        try {
            docJspXml = XmlUtils.getDocumentBuilder().parse(docJspxIs);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not open " + rooJspx.name()
                    + ".jspx file", ex);
        }

        Element docRoot = docJspXml.getDocumentElement();
        // Add new tag namesapces
        Element divMain = XmlUtils.findFirstElement("/div", docRoot);

        if (rooJspx.equals(RooJspx.create) || rooJspx.equals(RooJspx.update)) {
            divMain.setAttribute("xmlns:pattern",
                    "urn:jsptagdir:/WEB-INF/tags/pattern");
        }

        String idPrefix = rooJspx.equals(RooJspx.create) ? "fc:" : rooJspx
                .equals(RooJspx.update) ? "fu:" : "ps:";

        Element form = XmlUtils.findFirstElement(
                "/div/"
                        + rooJspx.name()
                        + "[@id='"
                        + XmlUtils.convertId(idPrefix
                                + formbackingType.getFullyQualifiedTypeName())
                        + "']", docRoot);

        String divContPaneId = XmlUtils.convertId("div:"
                + formbackingType.getFullyQualifiedTypeName() + "_contentPane");
        Element divContentPane = XmlUtils.findFirstElement(
                "/div/" + rooJspx.name() + "/div[@id='" + divContPaneId + "']",
                docRoot);
        if (null == divContentPane) {
            divContentPane = new XmlElementBuilder("div", docJspXml)
                    .addAttribute("id", divContPaneId)
                    .addAttribute("class", "patternContentPane").build();
        }

        String divFormId = XmlUtils.convertId("div:"
                + formbackingType.getFullyQualifiedTypeName() + "_formNoedit");
        Element divForm = XmlUtils.findFirstElement("/div/" + rooJspx.name()
                + "/div/div[@id='" + divFormId + "']", docRoot);
        if (null == divForm) {
            divForm = new XmlElementBuilder("div", docJspXml)
                    .addAttribute("id", divFormId)
                    .addAttribute("class", "formularios boxNoedit").build();
            divContentPane.appendChild(divForm);
        }

        // Wrap fields into <ul><li/></ul>
        NodeList fields = form.getChildNodes();
        if (fields.getLength() > 0) {
            Node thisField;
            for (int i = 0; i < fields.getLength(); i++) {
                thisField = fields.item(i);

                if (thisField.getNodeName().startsWith("field:")
                        && !thisField.getParentNode().getNodeName()
                                .equalsIgnoreCase("li")) {
                    if (null != thisField.getAttributes()
                    /*
                     * && null != thisField.getAttributes().getNamedItem(
                     * "type") &&
                     * !thisField.getAttributes().getNamedItem("type")
                     * .getNodeValue().equalsIgnoreCase("hidden")
                     */) {
                        Node thisNodeCpy = thisField.cloneNode(true);
                        String fieldAttValue = thisNodeCpy.getAttributes()
                                .getNamedItem("field").getNodeValue();
                        Element li = new XmlElementBuilder("li", docJspXml)
                                .addAttribute("class", "size120")
                                .addAttribute(
                                        "id",
                                        XmlUtils.convertId("li:"
                                                .concat(formbackingType
                                                        .getFullyQualifiedTypeName())
                                                .concat(".")
                                                .concat(fieldAttValue)))
                                .addChild(thisNodeCpy).build();
                        Element ul = new XmlElementBuilder("ul", docJspXml)
                                .addAttribute("class", "formInline")
                                .addAttribute(
                                        "id",
                                        XmlUtils.convertId("ul:"
                                                .concat(formbackingType
                                                        .getFullyQualifiedTypeName())
                                                .concat(".")
                                                .concat(fieldAttValue)))
                                .addChild(li).build();
                        divForm.appendChild(ul);
                        form.removeChild(thisField);
                        // form.replaceChild(ul, thisField);
                    }
                }
            }
        }

        if (rooJspx.equals(RooJspx.create) || rooJspx.equals(RooJspx.update)) {
            // Add a hidden field holding gvnixpattern parameter if exists
            String hiddenFieldId = XmlUtils.convertId("c:"
                    + formbackingType.getFullyQualifiedTypeName()
                    + "_gvnixpattern");
            Element hiddenField = XmlUtils.findFirstElement(
                    "/div/" + rooJspx.name()
                            + "/div/div/hiddengvnipattern[@id='"
                            + hiddenFieldId + "']", docRoot);
            if (null == hiddenField) {
                hiddenField = new XmlElementBuilder(
                        "pattern:hiddengvnipattern", docJspXml)
                        .addAttribute("id", hiddenFieldId)
                        .addAttribute("value", "${param.gvnixpattern}")
                        .addAttribute("render",
                                "${not empty param.gvnixpattern}").build();
                divForm.appendChild(hiddenField);
            }
            // Add a cancel button
            String cancelId = XmlUtils.convertId(idPrefix
                    + formbackingType.getFullyQualifiedTypeName() + "_cancel");
            Element cancelButton = XmlUtils.findFirstElement(
                    "/div/" + rooJspx.name() + "/div/div/cancelbutton[@id='"
                            + cancelId + "']", docRoot);
            if (null == cancelButton) {
                cancelButton = new XmlElementBuilder("pattern:cancelbutton",
                        docJspXml)
                        .addAttribute("id", cancelId)
                        .addAttribute("render",
                                "${not empty param.gvnixpattern}").build();
                divForm.appendChild(cancelButton);
            }
        }
        form.appendChild(divContentPane);

        XmlUtils.removeTextNodes(docJspXml);
        _fileManager.createOrUpdateTextFileIfRequired(docJspx,
                XmlUtils.nodeToString(docJspXml), true);
        // writeToDiskIfNecessary(docJspx, docJspXml);
    }

    /**
     * Installs static resources (JS, images, CSS) and tagx in the destination
     * project. Also, sets in application.properties the i18n properties needed
     * by tagx
     */
    private void installPatternArtifacts() {
        installStaticResource("images/pattern/enEdicion.gif");
        installStaticResource("images/pattern/pedi_off.gif");
        installStaticResource("images/pattern/pedi_on.gif");
        installStaticResource("images/pattern/pfil_off.gif");
        installStaticResource("images/pattern/pfil_on.gif");
        installStaticResource("images/pattern/plis_off.gif");
        installStaticResource("images/pattern/plis_on.gif");
        installStaticResource("scripts/quicklinks.js");
        installStaticResource("styles/pattern.css");
        PathResolver pathResolver = _projectOperations.getPathResolver();
        // copy util to tags/util
        copyDirectoryContents("tags/util/*.tagx", pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/util"));
        // copy pattern to tags/pattern
        copyDirectoryContents("tags/pattern/*.tagx",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/pattern"));
        copyDirectoryContents("tags/pattern/form/*.tagx",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/pattern/form"));
        copyDirectoryContents("tags/pattern/form/fields/*.tagx",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/pattern/form/fields"));

        // modify load-scripts.tagx
        modifyLoadScriptsTagx();

        // XXX: add properties needed by artifacts
        Map<String, String> properties = new LinkedHashMap<String, String>();
        properties.put("message_alert_title", "Alert");
        properties.put("message_info_title", "Information");
        properties.put("message_error_title", "Error");
        properties.put("message_suggest_title", "Suggestion");
        properties
                .put("message_pending_changes_problemdescription",
                        "There are changes pending to save. SAVE or CANCEL before proceed.");

        _propFileOperations
                .addProperties(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);
    }

    /**
     * Updates load-scripts.tagx adding in the right position some elements:
     * <ul>
     * <li><code>spring:url</code> elements for JS and CSS</li>
     * <li><code>link</code> element for CSS</li>
     * <li><code>script</code> element for JS</li>
     * </ul>
     */
    private void modifyLoadScriptsTagx() {
        PathResolver pathResolver = _projectOperations.getPathResolver();
        String loadScriptsTagx = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/util/load-scripts.tagx");

        if (!_fileManager.exists(loadScriptsTagx)) {
            // load-scripts.tagx doesn't exist, so nothing to do
            return;
        }

        InputStream loadScriptsIs = _fileManager
                .getInputStream(loadScriptsTagx);

        Document loadScriptsXml;
        try {
            loadScriptsXml = XmlUtils.getDocumentBuilder().parse(loadScriptsIs);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open load-scripts.tagx file", ex);
        }

        Element lsRoot = loadScriptsXml.getDocumentElement();

        Node nextSibiling;

        // spring:url elements
        Element testElement = XmlUtils.findFirstElement(
                "/root/url[@var='qljs_url']", lsRoot);
        if (testElement == null) {
            Element urlPatternCss = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/styles/pattern.css")
                    .addAttribute("var", "pattern_css_url").build();
            List<Element> springUrlElements = XmlUtils.findElements(
                    "/root/url", lsRoot);
            // Element lastSpringUrl = null;
            if (!springUrlElements.isEmpty()) {
                Element lastSpringUrl = springUrlElements.get(springUrlElements
                        .size() - 1);
                if (lastSpringUrl != null) {
                    nextSibiling = lastSpringUrl.getNextSibling()
                            .getNextSibling();
                    lsRoot.insertBefore(urlPatternCss, nextSibiling);
                }
            } else {
                // Add at the end of the document
                lsRoot.appendChild(urlPatternCss);
            }
        }

        // pattern.css stylesheet element
        testElement = XmlUtils.findFirstElement(
                "/root/link[@href='${pattern_css_url}']", lsRoot);
        if (testElement == null) {
            Element linkPatternCss = new XmlElementBuilder("link",
                    loadScriptsXml).addAttribute("rel", "stylesheet")
                    .addAttribute("type", "text/css")
                    .addAttribute("media", "screen")
                    .addAttribute("href", "${pattern_css_url}").build();
            linkPatternCss.appendChild(loadScriptsXml
                    .createCDATASection("<!-- required for FF3 and Opera -->"));
            Node linkTrundraCssNode = XmlUtils.findFirstElement(
                    "/root/link[@href='${tundra_url}']", lsRoot);
            if (linkTrundraCssNode != null) {
                nextSibiling = linkTrundraCssNode.getNextSibling()
                        .getNextSibling();
                lsRoot.insertBefore(linkPatternCss, nextSibiling);
            } else {
                // Add ass last link element
                // Element lastLink = null;
                List<Element> linkElements = XmlUtils.findElements(
                        "/root/link", lsRoot);
                if (!linkElements.isEmpty()) {
                    Element lastLink = linkElements
                            .get(linkElements.size() - 1);
                    if (lastLink != null) {
                        nextSibiling = lastLink.getNextSibling()
                                .getNextSibling();
                        lsRoot.insertBefore(linkPatternCss, nextSibiling);
                    }
                } else {
                    // Add at the end of document
                    lsRoot.appendChild(linkPatternCss);
                }
            }
        }

        // quicklinks.js script element
        testElement = XmlUtils.findFirstElement(
                "/root/script[@src='${qljs_url}']", lsRoot);
        if (testElement == null) {
            Element urlQlJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/scripts/quicklinks.js")
                    .addAttribute("var", "qljs_url").build();
            Element scriptQlJs = new XmlElementBuilder("script", loadScriptsXml)
                    .addAttribute("src", "${qljs_url}")
                    .addAttribute("type", "text/javascript").build();
            scriptQlJs.appendChild(loadScriptsXml
                    .createCDATASection("<!-- required for FF3 and Opera -->"));
            List<Element> scrtiptElements = XmlUtils.findElements(
                    "/root/script", lsRoot);
            // Element lastScript = null;
            if (!scrtiptElements.isEmpty()) {
                Element lastScript = scrtiptElements
                        .get(scrtiptElements.size() - 1);
                if (lastScript != null) {
                    nextSibiling = lastScript.getNextSibling().getNextSibling();
                    lsRoot.insertBefore(urlQlJs, nextSibiling);
                    lsRoot.insertBefore(scriptQlJs, nextSibiling);
                }
            } else {
                // Add at the end of document
                lsRoot.appendChild(urlQlJs);
                lsRoot.appendChild(scriptQlJs);
            }
        }

        writeToDiskIfNecessary(loadScriptsTagx,
                loadScriptsXml.getDocumentElement());

    }

    /**
     * Installs the resource given by parameter path into the same path inside
     * <code>src/main/webapp/</code>
     * 
     * @param path
     */
    private void installStaticResource(String path) {
        PathResolver pathResolver = _projectOperations.getPathResolver();
        String imageFile = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                path);
        if (!_fileManager.exists(imageFile)) {
            try {
                FileCopyUtils.copy(
                        TemplateUtils.getTemplate(getClass(), path),
                        _fileManager.createFile(
                                pathResolver.getIdentifier(
                                        Path.SRC_MAIN_WEBAPP, path))
                                .getOutputStream());
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of resources for MVC JSP addon.",
                        e);
            }
        }
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

        if (!_fileManager.exists(targetDirectory)) {
            _fileManager.createDirectory(targetDirectory);
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
            if (!_fileManager.exists(targetDirectory + fileName)) {
                try {
                    FileCopyUtils.copy(url.openStream(), _fileManager
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
     * Returns de XML Document with the JSPx
     * <p>
     * <strong>This method is based in:</strong>
     * {@link org.springframework.roo.addon.web.mvc.jsp.JspViewManager#getUpdateDocument()}
     * </p>
     * 
     * @return
     */
    private Document getUpdateTabularDocument() {

        String controllerPath = webScaffoldAnnotationValues.getPath();
        Assert.notNull(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + webScaffoldAnnotationValues.getGovernorTypeDetails()
                                .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        Document document = builder.newDocument();

        // Add document namespaces
        Element div = (Element) document.appendChild(new XmlElementBuilder(
                "div", document)
                .addAttribute("xmlns:form",
                        "urn:jsptagdir:/WEB-INF/tags/pattern/form")
                .addAttribute("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/pattern/form/fields")
                .addAttribute("xmlns:jsp", "http://java.sun.com/JSP/Page")
                .addAttribute("xmlns:util", "urn:jsptagdir:/WEB-INF/tags/util")
                .addAttribute("version", "2.0")
                .addChild(
                        new XmlElementBuilder("jsp:directive.page", document)
                                .addAttribute("contentType",
                                        "text/html;charset=UTF-8").build())
                .addChild(
                        new XmlElementBuilder("jsp:output", document)
                                .addAttribute("omit-xml-declaration", "yes")
                                .build()).build());

        // Add message-box element
        Element messageBox = new XmlElementBuilder("util:message-box", document)
                .addAttribute(
                        "id",
                        formbackingTypeMetadata.getPlural().toLowerCase()
                                .concat("_message_box")).build();

        // Add form update element
        Element formUpdate = new XmlElementBuilder("form:update", document)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("fu:"
                                + formbackingType.getFullyQualifiedTypeName()))
                /* Modified previous value entityName */
                .addAttribute("modelAttribute",
                        formbackingTypeMetadata.getPlural().toLowerCase())
                .build();

        if (!controllerPath.toLowerCase().equals(
                formbackingType.getSimpleTypeName().toLowerCase())) {
            formUpdate.setAttribute("path", controllerPath);
        }
        if (!"id".equals(formbackingTypePersistenceMetadata
                .getIdentifierField().getFieldName().getSymbolName())) {
            formUpdate.setAttribute("idField",
                    formbackingTypePersistenceMetadata.getIdentifierField()
                            .getFieldName().getSymbolName());
        }
        if (null == formbackingTypePersistenceMetadata
                .getVersionAccessorMethod()) {
            formUpdate.setAttribute("versionField", "none");
        } else if (!"version"
                .equals(BeanInfoUtils
                        .getPropertyNameForJavaBeanMethod(formbackingTypePersistenceMetadata
                                .getVersionAccessorMethod()))) {
            String methodName = formbackingTypePersistenceMetadata
                    .getVersionAccessorMethod().getMethodName().getSymbolName();
            formUpdate.setAttribute("versionField", methodName.substring(3));
        }

        div.appendChild(messageBox);
        createFieldsForCreateAndUpdate(entityName, relatedDomainTypes,
                eligibleFields, document, formUpdate, false);
        formUpdate.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(formUpdate));
        div.appendChild(formUpdate);

        return document;
    }

    /**
     * {@link org.springframework.roo.addon.web.mvc.jsp.JspViewManager#
     * createFieldsForCreateAndUpdate(List<FieldMetadata>, Document, Element,
     * boolean)}
     */
    private void createFieldsForCreateAndUpdate(String entityName,
            Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes,
            List<FieldMetadata> formFields, Document document, Element root,
            boolean isCreate) {
        for (FieldMetadata field : formFields) {
            String fieldName = field.getFieldName().getSymbolName();
            JavaType fieldType = field.getFieldType();
            AnnotationMetadata annotationMetadata;

            // Ignoring java.util.Map field types (see ROO-194)
            if (fieldType.equals(new JavaType(Map.class.getName()))) {
                continue;
            }
            // Fields contained in the embedded Id type have been added
            // separately to the field list
            if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.EMBEDDED_ID_FIELD)) {
                continue;
            }

            fieldType = getJavaTypeForField(field);

            JavaTypeMetadataDetails typeMetadataHolder = relatedDomainTypes
                    .get(fieldType);
            JavaTypePersistenceMetadataDetails typePersistenceMetadataHolder = null;
            if (typeMetadataHolder != null) {
                typePersistenceMetadataHolder = typeMetadataHolder
                        .getPersistenceDetails();
            }

            Element fieldElement = null;

            if (fieldType.getFullyQualifiedTypeName().equals(
                    Boolean.class.getName())
                    || fieldType.getFullyQualifiedTypeName().equals(
                            boolean.class.getName())) {
                fieldElement = document.createElement("field:checkbox");
                // Handle enum fields
            } else if (typeMetadataHolder != null
                    && typeMetadataHolder.isEnumType()) {
                fieldElement = new XmlElementBuilder("field:select", document)
                        .addAttribute(
                                "items",
                                "${"
                                        + typeMetadataHolder.getPlural()
                                                .toLowerCase() + "}")
                        .addAttribute("path", getPathForType(fieldType))
                        .build();
            } else if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.ONE_TO_MANY_FIELD)) {
                // OneToMany relationships are managed from the 'many' side of
                // the relationship, therefore we provide a link to the relevant
                // form
                // the link URL is determined as a best effort attempt following
                // Roo REST conventions, this link might be wrong if custom
                // paths are used
                // if custom paths are used the developer can adjust the path
                // attribute in the field:reference tag accordingly
                if (typePersistenceMetadataHolder != null) {
                    fieldElement = new XmlElementBuilder("field:simple",
                            document)
                            .addAttribute("messageCode",
                                    "entity_reference_not_managed")
                            .addAttribute(
                                    "messageCodeAttribute",
                                    new JavaSymbolName(fieldType
                                            .getSimpleTypeName())
                                            .getReadableSymbolName()).build();
                } else {
                    continue;
                }
            } else if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.MANY_TO_ONE_FIELD)
                    || field.getCustomData()
                            .keySet()
                            .contains(
                                    PersistenceCustomDataKeys.MANY_TO_MANY_FIELD)
                    || field.getCustomData()
                            .keySet()
                            .contains(
                                    PersistenceCustomDataKeys.ONE_TO_ONE_FIELD)) {
                JavaType referenceType = getJavaTypeForField(field);
                JavaTypeMetadataDetails referenceTypeMetadata = relatedDomainTypes
                        .get(referenceType);
                if (referenceType != null/** fix for ROO-1888 --> **/
                && referenceTypeMetadata != null
                        && referenceTypeMetadata.isApplicationType()
                        && typePersistenceMetadataHolder != null) {
                    fieldElement = new XmlElementBuilder("field:select",
                            document)
                            .addAttribute(
                                    "items",
                                    "${"
                                            + referenceTypeMetadata.getPlural()
                                                    .toLowerCase() + "}")
                            .addAttribute(
                                    "itemValue",
                                    typePersistenceMetadataHolder
                                            .getIdentifierField()
                                            .getFieldName().getSymbolName())
                            .addAttribute(
                                    "path",
                                    "/"
                                            + getPathForType(getJavaTypeForField(field)))
                            .build();

                    if (field
                            .getCustomData()
                            .keySet()
                            .contains(
                                    PersistenceCustomDataKeys.MANY_TO_MANY_FIELD)) {
                        fieldElement.setAttribute("multiple", "true");
                    }
                }
            } else if (fieldType.getFullyQualifiedTypeName().equals(
                    Date.class.getName())
                    || fieldType.getFullyQualifiedTypeName().equals(
                            Calendar.class.getName())) {
                // Only include the date picker for styles supported by Dojo
                // (SMALL & MEDIUM)
                fieldElement = new XmlElementBuilder("field:datetime", document)
                        .addAttribute(
                                "dateTimePattern",
                                "${" + entityName + "_"
                                        + fieldName.toLowerCase()
                                        + "_date_format}").build();

                if (null != MemberFindingUtils.getAnnotationOfType(field
                        .getAnnotations(), new JavaType(
                        "javax.validation.constraints.Future"))) {
                    fieldElement.setAttribute("future", "true");
                } else if (null != MemberFindingUtils.getAnnotationOfType(field
                        .getAnnotations(), new JavaType(
                        "javax.validation.constraints.Past"))) {
                    fieldElement.setAttribute("past", "true");
                }
            } else if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.LOB_FIELD)) {
                fieldElement = new XmlElementBuilder("field:textarea", document)
                        .build();
            }
            if (null != (annotationMetadata = MemberFindingUtils
                    .getAnnotationOfType(field.getAnnotations(), new JavaType(
                            "javax.validation.constraints.Size")))) {
                AnnotationAttributeValue<?> max = annotationMetadata
                        .getAttribute(new JavaSymbolName("max"));
                if (max != null) {
                    int maxValue = (Integer) max.getValue();
                    if (fieldElement == null && maxValue > 30) {
                        fieldElement = new XmlElementBuilder("field:textarea",
                                document).build();
                    }
                }
            }
            // Use a default input field if no other criteria apply
            if (fieldElement == null) {
                fieldElement = document.createElement("field:input");
            }
            addCommonAttributes(field, fieldElement);
            fieldElement.setAttribute("field", fieldName);
            fieldElement.setAttribute(
                    "id",
                    XmlUtils.convertId("c:"
                            + formbackingType.getFullyQualifiedTypeName() + "."
                            + field.getFieldName().getSymbolName()));
            fieldElement.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(fieldElement));

            root.appendChild(fieldElement);
        }
    }

    /**
     * {@link org.springframework.roo.addon.web.mvc.jsp.JspViewManager#getJavaTypeForField(FieldMetadata)}
     */
    private JavaType getJavaTypeForField(FieldMetadata field) {
        if (field.getFieldType().isCommonCollectionType()) {
            // Currently there is no scaffolding available for Maps (see
            // ROO-194)
            if (field.getFieldType().equals(new JavaType(Map.class.getName()))) {
                return null;
            }
            List<JavaType> parameters = field.getFieldType().getParameters();
            if (parameters.size() == 0) {
                throw new IllegalStateException(
                        "Unable to determine the parameter type for the "
                                + field.getFieldName().getSymbolName()
                                + " field in "
                                + formbackingType.getSimpleTypeName());
            }
            return parameters.get(0);
        }
        return field.getFieldType();
    }

    /**
     * {@link org.springframework.roo.addon.web.mvc.jsp.JspViewManager#getPathForType(JavaType)}
     */
    private String getPathForType(JavaType type) {
        JavaTypeMetadataDetails javaTypeMetadataHolder = relatedDomainTypes
                .get(type);
        Assert.notNull(
                javaTypeMetadataHolder,
                "Unable to obtain metadata for type "
                        + type.getFullyQualifiedTypeName());
        return javaTypeMetadataHolder.getControllerPath();
    }

    /** {@link org.springframework.roo.addon.web.mvc.jsp.JspViewManager#addCommonAttributes(FieldMetadata, Element)} */
    private void addCommonAttributes(FieldMetadata field, Element fieldElement) {
        AnnotationMetadata annotationMetadata;
        if (field.getFieldType().equals(new JavaType(Integer.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(int.class.getName())
                || field.getFieldType().equals(
                        new JavaType(Short.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(short.class.getName())
                || field.getFieldType().equals(
                        new JavaType(Long.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(long.class.getName())
                || field.getFieldType().equals(
                        new JavaType("java.math.BigInteger"))) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_integer");
        } else if (uncapitalize(field.getFieldName().getSymbolName()).contains(
                "email")) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_email");
        } else if (field.getFieldType().equals(
                new JavaType(Double.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(double.class.getName())
                || field.getFieldType().equals(
                        new JavaType(Float.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(float.class.getName())
                || field.getFieldType().equals(
                        new JavaType("java.math.BigDecimal"))) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_number");
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType("javax.validation.constraints.Min")))) {
            AnnotationAttributeValue<?> min = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (min != null) {
                fieldElement.setAttribute("min", min.getValue().toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType("javax.validation.constraints.Max")))
                && !"field:textarea".equals(fieldElement.getTagName())) {
            AnnotationAttributeValue<?> maxA = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (maxA != null) {
                fieldElement.setAttribute("max", maxA.getValue().toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.DecimalMin")))
                && !"field:textarea".equals(fieldElement.getTagName())) {
            AnnotationAttributeValue<?> decimalMin = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (decimalMin != null) {
                fieldElement.setAttribute("decimalMin", decimalMin.getValue()
                        .toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.DecimalMax")))) {
            AnnotationAttributeValue<?> decimalMax = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (decimalMax != null) {
                fieldElement.setAttribute("decimalMax", decimalMax.getValue()
                        .toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(field.getAnnotations(), new JavaType(
                        "javax.validation.constraints.Pattern")))) {
            AnnotationAttributeValue<?> regexp = annotationMetadata
                    .getAttribute(new JavaSymbolName("regexp"));
            if (regexp != null) {
                fieldElement.setAttribute("validationRegex", regexp.getValue()
                        .toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.Size")))) {
            AnnotationAttributeValue<?> max = annotationMetadata
                    .getAttribute(new JavaSymbolName("max"));
            if (max != null) {
                fieldElement.setAttribute("max", max.getValue().toString());
            }
            AnnotationAttributeValue<?> min = annotationMetadata
                    .getAttribute(new JavaSymbolName("min"));
            if (min != null) {
                fieldElement.setAttribute("min", min.getValue().toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(field.getAnnotations(), new JavaType(
                        "javax.validation.constraints.NotNull")))) {
            String tagName = fieldElement.getTagName();
            if (tagName.endsWith("textarea") || tagName.endsWith("input")
                    || tagName.endsWith("datetime")
                    || tagName.endsWith("textarea")
                    || tagName.endsWith("select")
                    || tagName.endsWith("reference")) {
                fieldElement.setAttribute("required", "true");
            }
        }
        if (field.getCustomData().keySet()
                .contains(PersistenceCustomDataKeys.COLUMN_FIELD)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) field
                    .getCustomData()
                    .get(PersistenceCustomDataKeys.COLUMN_FIELD);
            if (values.keySet().contains("nullable")
                    && ((Boolean) values.get("nullable")) == false) {
                fieldElement.setAttribute("required", "true");
            }
        }
        // Disable form binding for nested fields (mainly PKs)
        if (field.getFieldName().getSymbolName().contains(".")) {
            fieldElement.setAttribute("disableFormBinding", "true");
        }
    }

    /**
     * Decides if write to disk is needed (ie updated or created)<br/>
     * Used for JSPx files
     * 
     * @param jspFilename
     * @param proposed
     */
    private void writeToDiskIfNecessary(String jspFilename, Document proposed) {
        Document original = null;
        if (_fileManager.exists(jspFilename)) {
            original = XmlUtils.readXml(_fileManager
                    .getInputStream(jspFilename));
            if (XmlRoundTripUtils.compareDocuments(original, proposed)) {
                XmlUtils.removeTextNodes(original);
                _fileManager.createOrUpdateTextFileIfRequired(jspFilename,
                        XmlUtils.nodeToString(original), false);
            }
        } else {
            _fileManager.createOrUpdateTextFileIfRequired(jspFilename,
                    XmlUtils.nodeToString(proposed), false);
        }
    }

    /**
     * Decides if write to disk is needed (ie updated or created)<br/>
     * Used for TAGx files
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
        if (_fileManager.exists(filePath)) {
            // First verify if the file has even changed
            File f = new File(filePath);
            String existing = null;
            try {
                existing = FileCopyUtils.copyToString(new FileReader(f));
            } catch (IOException ignoreAndJustOverwriteIt) {
            }

            if (!viewContent.equals(existing)) {
                mutableFile = _fileManager.updateFile(filePath);
            }
        } else {
            mutableFile = _fileManager.createFile(filePath);
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

    protected String uncapitalize(String term) {
        // [ROO-1790] this is needed to adhere to the JavaBean naming
        // conventions (see JavaBean spec section 8.8)
        return Introspector.decapitalize(StringUtils.capitalize(term));
    }

    protected abstract boolean isRelatedPattern();

}
