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
package org.gvnix.web.screen.roo.addon;

import java.beans.Introspector;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.apache.felix.scr.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;
import org.osgi.service.component.ComponentContext;

/**
 * This Abstract Listener gives support for install/create/modify MVC artifacts
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component(componentAbstract = true)
public abstract class AbstractPatternJspMetadataListener implements
        MetadataProvider, MetadataNotificationListener {

    // ------------ OSGi component attributes ----------------
    public BundleContext context;

    private static final String TRUE_VALUE = "true";
    private static final String FIELD_ATTRIBUTE = "field";
    private static final String FIELD_TEXTAREA_ELEMENT = "field:textarea";
    private static final String FIELD_INPUT_ELEMENT = "field:input";
    private static final String MAX_ATTRIBUTE = "max";
    private static final String RENDER_ATTRIBUTE = "render";
    private static final String DIV_ID_PREFIX = "div:";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String PATH_ATTRIBUTE = "path";
    private static final String OBJECT_ATTRIBUTE = "object";
    private static final String DIV_ELEMENT = "div";
    private static final String ID_ATTRIBUTE = "id";
    private static final String ID_RENDER_ATTRIBUTE = "idRender";
    private static final String ID_DISABLED_ATTRIBUTE = "idDisabled";
    private static final String PATH_IS_NOT_SPECIFIED = "Path is not specified in the @RooWebScaffold annotation for '";

    private static final Logger LOGGER = HandlerUtils
            .getLogger(AbstractPatternJspMetadataListener.class);

    protected FileManager _fileManager;
    protected TilesOperations _tilesOperations;
    protected MenuOperations _menuOperations;
    protected ProjectOperations _projectOperations;
    protected PropFileOperations _propFileOperations;
    protected MetadataService _metadataService;
    protected WebScreenOperations _webScreenOperations;
    protected PathResolver _pathResolver;
    protected TypeLocationService _typeLocationService;

    protected WebScaffoldMetadata webScaffoldMetadata;
    protected JavaType formbackingType;
    protected String entityName;
    protected Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes;
    protected JavaTypePersistenceMetadataDetails formbackingTypePersistenceMetadata;
    protected JavaTypeMetadataDetails formbackingTypeMetadata;
    protected List<FieldMetadata> eligibleFields;
    protected WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    protected void activate(final ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * For the given pattern it install needed MVC artifacts and generates the
     * pattern JSPx
     * 
     * @param pattern
     */
    protected void installMvcArtifacts(String pattern) {
        String[] patternNameType = pattern.split("=");

        PathResolver pathResolver = getProjectOperations().getPathResolver();
        String controllerPath = webScaffoldMetadata.getAnnotationValues()
                .getPath();
        Validate.notNull(controllerPath, PATH_IS_NOT_SPECIFIED
                + webScaffoldMetadata.getAnnotationValues()
                        .getGovernorTypeDetails().getName() + "'");
        if (controllerPath.startsWith("/")) {
            controllerPath = controllerPath.substring(1);
        }

        // Make the holding directory for this controller
        String destinationDirectory = pathResolver.getIdentifier(
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

        /*
         * TODO: next test may be replaced by a test over allow or not create
         * operation of the entity
         */
        if (patternNameType[1].equalsIgnoreCase(WebPatternType.tabular.name())) {
            installPatternTypeArtifact(WebPatternType.tabular,
                    destinationDirectory, controllerPath, patternNameType[0]);
        }
        else if (patternNameType[1].equalsIgnoreCase(WebPatternType.register
                .name())) {
            installPatternTypeArtifact(WebPatternType.register,
                    destinationDirectory, controllerPath, patternNameType[0]);
        }
        else if (patternNameType[1]
                .equalsIgnoreCase(WebPatternType.tabular_edit_register.name())) {

            installPatternTypeArtifact(WebPatternType.tabular_edit_register,
                    destinationDirectory, controllerPath, patternNameType[0]);

        }
        else {
            // Pattern type not supported. Nothing to do
            LOGGER.finer("Patter type not supported: "
                    .concat(patternNameType[1]));
        }

        // Modify some Roo JSPx
        modifyRooJsp(RooJspx.create);
        modifyRooJsp(RooJspx.update);
        modifyRooJsp(RooJspx.show);

    }

    /**
     * Creates a JSPx of the given WebPatternType type
     * 
     * @param patternType
     * @param destinationDirectory
     * @param controllerPath
     * @param patternName
     */
    private void installPatternTypeArtifact(WebPatternType patternType,
            String destinationDirectory, String controllerPath,
            String patternName) {
        String patternTypeStr = patternType.name();
        String patternPath = destinationDirectory.concat("/")
                .concat(patternName).concat(".jspx");
        // Get the document for the pattern type
        Document jspDoc = patternType.equals(WebPatternType.tabular)
                || patternType.equals(WebPatternType.tabular_edit_register) ? getUpdateTabularDocument(
                patternName, patternType) : getRegisterDocument(patternName);
        writeToDiskIfNecessary(patternPath, jspDoc);

        // add view to views.xml
        getTilesOperations().addViewDefinition(
                controllerPath,
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                controllerPath + "/" + patternName,
                TilesOperations.DEFAULT_TEMPLATE,
                "/WEB-INF/views/" + controllerPath + "/" + patternName
                        + ".jspx");
        // add entry to menu.jspx
        JavaSymbolName categoryName = new JavaSymbolName(
                formbackingType.getSimpleTypeName());
        JavaSymbolName menuItemId = new JavaSymbolName("list_"
                .concat(patternTypeStr).concat("_").concat(patternName));
        String queryString = patternType.equals(WebPatternType.tabular)
                || patternType.equals(WebPatternType.tabular_edit_register) ? "?gvnixpattern="
                .concat(patternName) : "?gvnixform&gvnixpattern=".concat(
                patternName).concat(
                "&index=${empty param.index ? 1 : param.index}");
        if (!isRelatedPattern()) {
            getMenuOperations().addMenuItem(
                    categoryName,
                    menuItemId,
                    "menu_list_".concat(patternTypeStr).concat("_")
                            .concat(patternName),
                    "/" + controllerPath + queryString,
                    MenuOperations.DEFAULT_MENU_ITEM_PREFIX,
                    getPathResolver().getFocusedPath(Path.SRC_MAIN_WEBAPP));
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
            getPropFileOperations().addProperties(
                    LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
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
        Validate.notNull(controllerPath, PATH_IS_NOT_SPECIFIED
                + webScaffoldAnnotationValues.getGovernorTypeDetails()
                        .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        Document document = builder.newDocument();

        // Add document namespaces
        Element div = (Element) document.appendChild(new XmlElementBuilder(
                DIV_ELEMENT, document)
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
                        ID_ATTRIBUTE,
                        XmlUtils.convertId("ps:"
                                + formbackingType.getFullyQualifiedTypeName()))
                .addAttribute(OBJECT_ATTRIBUTE,
                        "${" + entityName.toLowerCase() + "}")
                .addAttribute(PATH_ATTRIBUTE, controllerPath).build();
        if (!webScaffoldAnnotationValues.isCreate()) {
            pageShow.setAttribute("create", "false");
        }
        if (!webScaffoldAnnotationValues.isUpdate()) {
            pageShow.setAttribute("update", "false");
        }
        if (!webScaffoldAnnotationValues.isDelete()) {
            pageShow.setAttribute("delete", "false");
        }

        String identifierFieldName = formbackingTypeMetadata
                .getPersistenceDetails().getIdentifierField().getFieldName()
                .getSymbolName();
        if (!ID_ATTRIBUTE.equals(identifierFieldName)) {
            pageShow.setAttribute("typeIdFieldName", formbackingTypeMetadata
                    .getPersistenceDetails().getIdentifierField()
                    .getFieldName().getSymbolName());
        }

        pageShow.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(pageShow));

        Element divContentPane = new XmlElementBuilder(DIV_ELEMENT, document)
                .addAttribute(
                        ID_ATTRIBUTE,
                        XmlUtils.convertId(DIV_ID_PREFIX
                                + formbackingType.getFullyQualifiedTypeName()
                                + "_contentPane"))
                .addAttribute(CLASS_ATTRIBUTE, "patternContentPane").build();

        Element divForm = new XmlElementBuilder(DIV_ELEMENT, document)
                .addAttribute(
                        ID_ATTRIBUTE,
                        XmlUtils.convertId(DIV_ID_PREFIX
                                + formbackingType.getFullyQualifiedTypeName()
                                + "_formNoedit"))
                .addAttribute(CLASS_ATTRIBUTE, "formularios boxNoedit").build();

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
                    .addAttribute(CLASS_ATTRIBUTE, "formInline")
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("ul:"
                                    .concat(formbackingType
                                            .getFullyQualifiedTypeName())
                                    .concat(".").concat(fieldName))).build();
            Element li = new XmlElementBuilder("li", document)
                    .addAttribute(CLASS_ATTRIBUTE, "size120")
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("li:"
                                    .concat(formbackingType
                                            .getFullyQualifiedTypeName())
                                    .concat(".").concat(fieldName))).build();
            ul.appendChild(li);

            Element fieldDisplay = new XmlElementBuilder("field:display",
                    document)
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("s:"
                                    + formbackingType
                                            .getFullyQualifiedTypeName() + "."
                                    + field.getFieldName().getSymbolName()))
                    .addAttribute(OBJECT_ATTRIBUTE,
                            "${" + entityName.toLowerCase() + "}")
                    .addAttribute(FIELD_ATTRIBUTE, fieldName).build();
            if (field.getFieldType().equals(new JavaType(Date.class.getName()))) {
                fieldDisplay.setAttribute("date", TRUE_VALUE);
                fieldDisplay.setAttribute("dateTimePattern", "${" + entityName
                        + "_" + fieldName.toLowerCase() + "_date_format}");
            }
            else if (field.getFieldType().equals(
                    new JavaType(Calendar.class.getName()))) {
                fieldDisplay.setAttribute("calendar", TRUE_VALUE);
                fieldDisplay.setAttribute("dateTimePattern", "${" + entityName
                        + "_" + fieldName.toLowerCase() + "_date_format}");
            }
            else if (field.getFieldType().isCommonCollectionType()
                    && (field.getCustomData().get(
                            CustomDataKeys.ONE_TO_MANY_FIELD) != null || field
                            .getCustomData().get(
                                    CustomDataKeys.MANY_TO_MANY_FIELD) != null)) {
                if (isRelationVisible(patternName, field.getFieldName()
                        .getSymbolName())) {
                    fieldsOfRelations.add(field);
                }
                isRelatationship = true;
            }

            if (!isRelatationship) {
                fieldDisplay.setAttribute("z",
                        XmlRoundTripUtils.calculateUniqueKeyFor(fieldDisplay));

                li.appendChild(fieldDisplay);
                divForm.appendChild(ul);
                // isRelatationship = false;
            }
            isRelatationship = false;
        }

        pageShow.appendChild(divContentPane);

        div.appendChild(pageShow);

        if (!fieldsOfRelations.isEmpty()) {
            Element patternRelations = getRegisterRelationsDocument(
                    patternName, document, fieldsOfRelations);
            div.appendChild(patternRelations);
        }

        return document;
    }

    protected Element getRegisterRelationsDocument(String patternName,
            Document document, List<FieldMetadata> fieldsOfRelations) {

        Element patternRelations = new XmlElementBuilder("pattern:relations",
                document)
                .addAttribute(
                        ID_ATTRIBUTE,
                        XmlUtils.convertId("pr:"
                                + formbackingType.getFullyQualifiedTypeName()
                                + "." + patternName))
                .addAttribute(
                        RENDER_ATTRIBUTE,
                        "${!empty ".concat(entityName.toLowerCase())
                                .concat("}")).build();
        patternRelations.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(patternRelations));
        for (FieldMetadata fieldMetadata : fieldsOfRelations) {
            String fieldName = uncapitalize(fieldMetadata.getFieldName()
                    .getSymbolName());
            String webScaffoldFolder = getFieldEntityPlural(fieldMetadata);

            String referenceName = getFieldRelationMasterEntity(fieldMetadata);
            if (referenceName == null) {
                referenceName = entityName.toLowerCase();
            }

            Element patternRelation = new XmlElementBuilder("pattern:relation",
                    document)
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("pr:"
                                    + formbackingType
                                            .getFullyQualifiedTypeName() + "."
                                    + fieldName))
                    .addAttribute(OBJECT_ATTRIBUTE,
                            "${" + entityName.toLowerCase() + "}")
                    .addAttribute(FIELD_ATTRIBUTE, fieldName)
                    .addAttribute("folder", webScaffoldFolder)
                    .addAttribute("patternName", patternName)
                    .addAttribute("referenceName", referenceName)
                    .addAttribute(
                            "referenceField",
                            formbackingTypeMetadata.getPersistenceDetails()
                                    .getIdentifierField().getFieldName()
                                    .getSymbolName())
                    .addAttribute(
                            RENDER_ATTRIBUTE,
                            "${!empty ".concat(entityName.toLowerCase())
                                    .concat("}")).build();
            patternRelation.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(patternRelation));
            patternRelations.appendChild(patternRelation);
        }
        return patternRelations;
    }

    /**
     * Get field from entity related with some field.
     */
    protected String getFieldRelationMasterEntity(FieldMetadata relationField) {

        // TODO Some duplicated code with
        // RelatedPatternmetadata.getFieldRelationMasterEntity

        // Get field from master entity with OneToMany or ManyToMany annotation
        // and "mappedBy" attribute has some value
        String masterField = null;

        PhysicalTypeMetadata masterEntityDetails = (PhysicalTypeMetadata) getMetadataService()
                .get(PhysicalTypeIdentifier.createIdentifier(formbackingType,
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, "")));
        List<FieldMetadata> masterFields = masterEntityDetails
                .getMemberHoldingTypeDetails().getFieldsWithAnnotation(
                        new JavaType("javax.persistence.OneToMany"));
        masterFields.addAll(masterEntityDetails.getMemberHoldingTypeDetails()
                .getFieldsWithAnnotation(
                        new JavaType("javax.persistence.ManyToMany")));
        for (FieldMetadata tmpMasterField : masterFields) {

            List<AnnotationMetadata> masterFieldAnnotations = tmpMasterField
                    .getAnnotations();
            for (AnnotationMetadata masterFieldAnnotation : masterFieldAnnotations) {

                // TODO May be more fields on relationsField var
                AnnotationAttributeValue<?> masterFieldMappedBy = masterFieldAnnotation
                        .getAttribute(new JavaSymbolName("mappedBy"));
                if ((masterFieldAnnotation.getAnnotationType().equals(
                        new JavaType("javax.persistence.OneToMany")) || masterFieldAnnotation
                        .getAnnotationType().equals(
                                new JavaType("javax.persistence.ManyToMany")))
                        && tmpMasterField.getFieldName().equals(
                                relationField.getFieldName())) {

                    masterField = masterFieldMappedBy.getValue().toString();
                }
            }
        }

        return masterField;
    }

    protected Element getTabularRelationsDocument(String patternName,
            Document document, List<FieldMetadata> fieldsOfRelations) {

        Element patternRelations = new XmlElementBuilder("pattern:relations",
                document)
                .addAttribute(
                        ID_ATTRIBUTE,
                        XmlUtils.convertId("pr:"
                                + formbackingType.getFullyQualifiedTypeName()
                                + "." + patternName))
                .addAttribute(RENDER_ATTRIBUTE,
                        "${!empty ".concat(OBJECT_ATTRIBUTE).concat("}"))
                .build();
        patternRelations.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(patternRelations));
        for (FieldMetadata fieldMetadata : fieldsOfRelations) {
            String fieldName = uncapitalize(fieldMetadata.getFieldName()
                    .getSymbolName());
            String webScaffoldFolder = getFieldEntityPlural(fieldMetadata);

            String referenceName = getFieldRelationMasterEntity(fieldMetadata);
            if (referenceName == null) {
                referenceName = entityName.toLowerCase();
            }

            Element patternRelation = new XmlElementBuilder("pattern:relation",
                    document)
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("pr:"
                                    + formbackingType
                                            .getFullyQualifiedTypeName() + "."
                                    + fieldName))
                    .addAttribute(OBJECT_ATTRIBUTE, "${object}")
                    .addAttribute(FIELD_ATTRIBUTE, fieldName)
                    .addAttribute("folder", webScaffoldFolder)
                    .addAttribute("patternName", patternName)
                    .addAttribute("referenceName", referenceName)
                    .addAttribute(
                            "referenceField",
                            formbackingTypeMetadata.getPersistenceDetails()
                                    .getIdentifierField().getFieldName()
                                    .getSymbolName())
                    .addAttribute(RENDER_ATTRIBUTE,
                            "${!empty ".concat(OBJECT_ATTRIBUTE).concat("}"))
                    .build();
            patternRelation.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(patternRelation));
            patternRelations.appendChild(patternRelation);
        }
        return patternRelations;
    }

    /**
     * Given a FieldMetadata it returns the pluralized name of its Class in
     * plural.
     * <p>
     * If the FieldMetadata type is a Collection will return the plural of the
     * parameter in the Collection definition. That is:<br/>
     * <code>java.util.Set&lt;Foo&gt;</code> will return <code>Foos</code>
     * 
     * @param fieldMetadata
     * @return
     */
    private String getFieldEntityPlural(FieldMetadata fieldMetadata) {
        JavaType fieldEntity = fieldMetadata.getFieldType();
        if (fieldEntity.isCommonCollectionType()
                && !fieldEntity.getParameters().isEmpty()) {
            fieldEntity = fieldEntity.getParameters().get(0);
        }
        ClassOrInterfaceTypeDetails cid = getTypeLocationService()
                .getTypeDetails(fieldEntity);
        JavaType javaType = cid.getName();
        LogicalPath path = PhysicalTypeIdentifier.getPath(cid
                .getDeclaredByMetadataId());

        PluralMetadata pluralMetadata = (PluralMetadata) getMetadataService()
                .get(PluralMetadata.createIdentifier(javaType, path));

        return pluralMetadata.getPlural().toLowerCase();
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

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> values = (List<StringAttributeValue>) arrayVal
                        .getValue();
                String regexPattern = "(".concat(patternName).concat(":)")
                        .concat(".*").concat("( ").concat(symbolName)
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
        Validate.notNull(controllerPath, PATH_IS_NOT_SPECIFIED
                + webScaffoldAnnotationValues.getGovernorTypeDetails()
                        .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        PathResolver pathResolver = getProjectOperations().getPathResolver();
        String docJspx = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/views" + controllerPath + "/" + rooJspx.name()
                        + ".jspx");

        if (!getFileManager().exists(docJspx)) {
            // create.jspx doesn't exist, so nothing to do
            return;
        }

        InputStream docJspxIs = getFileManager().getInputStream(docJspx);

        Document docJspXml;
        try {
            docJspXml = XmlUtils.getDocumentBuilder().parse(docJspxIs);
        }
        catch (Exception ex) {
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

        String divContPaneId = XmlUtils.convertId(DIV_ID_PREFIX
                + formbackingType.getFullyQualifiedTypeName() + "_contentPane");
        Element divContentPane = XmlUtils.findFirstElement(
                "/div/" + rooJspx.name() + "/div[@id='" + divContPaneId + "']",
                docRoot);
        if (null == divContentPane) {
            divContentPane = new XmlElementBuilder(DIV_ELEMENT, docJspXml)
                    .addAttribute(ID_ATTRIBUTE, divContPaneId)
                    .addAttribute(CLASS_ATTRIBUTE, "patternContentPane")
                    .build();
        }

        String divFormId = XmlUtils.convertId(DIV_ID_PREFIX
                + formbackingType.getFullyQualifiedTypeName() + "_formNoedit");
        Element divForm = XmlUtils.findFirstElement("/div/" + rooJspx.name()
                + "/div/div[@id='" + divFormId + "']", docRoot);
        if (null == divForm) {
            divForm = new XmlElementBuilder(DIV_ELEMENT, docJspXml)
                    .addAttribute(ID_ATTRIBUTE, divFormId)
                    .addAttribute(CLASS_ATTRIBUTE, "formularios boxNoedit")
                    .build();
            divContentPane.appendChild(divForm);
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

        if (form != null) {
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
                                    .getNamedItem(FIELD_ATTRIBUTE)
                                    .getNodeValue();
                            Element li = new XmlElementBuilder("li", docJspXml)
                                    .addAttribute(CLASS_ATTRIBUTE, "size120")
                                    .addAttribute(
                                            ID_ATTRIBUTE,
                                            XmlUtils.convertId("li:"
                                                    .concat(formbackingType
                                                            .getFullyQualifiedTypeName())
                                                    .concat(".")
                                                    .concat(fieldAttValue)))
                                    .addChild(thisNodeCpy).build();
                            Element ul = new XmlElementBuilder("ul", docJspXml)
                                    .addAttribute(CLASS_ATTRIBUTE, "formInline")
                                    .addAttribute(
                                            ID_ATTRIBUTE,
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
            if (rooJspx.equals(RooJspx.create)
                    || rooJspx.equals(RooJspx.update)) {
                // Add a hidden field holding gvnixpattern parameter if exists
                String hiddenFieldId = XmlUtils.convertId("c:"
                        + formbackingType.getFullyQualifiedTypeName()
                        + "_gvnixpattern");
                Element hiddenField = XmlUtils.findFirstElement("/div/"
                        + rooJspx.name() + "/div/div/hiddengvnixpattern[@id='"
                        + hiddenFieldId + "']", docRoot);
                if (null == hiddenField) {
                    hiddenField = new XmlElementBuilder(
                            "pattern:hiddengvnixpattern", docJspXml)
                            .addAttribute(ID_ATTRIBUTE, hiddenFieldId)
                            .addAttribute("value", "${param.gvnixpattern}")
                            .addAttribute(RENDER_ATTRIBUTE,
                                    "${not empty param.gvnixpattern}").build();
                    divForm.appendChild(hiddenField);
                }
                // Add a cancel button
                String cancelId = XmlUtils.convertId(idPrefix
                        + formbackingType.getFullyQualifiedTypeName()
                        + "_cancel");
                Element cancelButton = XmlUtils.findFirstElement("/div/"
                        + rooJspx.name() + "/div/div/cancelbutton[@id='"
                        + cancelId + "']", docRoot);
                if (null == cancelButton) {
                    cancelButton = new XmlElementBuilder(
                            "pattern:cancelbutton", docJspXml)
                            .addAttribute(ID_ATTRIBUTE, cancelId)
                            .addAttribute(RENDER_ATTRIBUTE,
                                    "${not empty param.gvnixpattern}").build();
                    divForm.appendChild(cancelButton);
                }
            }
            form.appendChild(divContentPane);
        }
        DomUtils.removeTextNodes(docJspXml);
        getFileManager().createOrUpdateTextFileIfRequired(docJspx,
                XmlUtils.nodeToString(docJspXml), true);
        // writeToDiskIfNecessary(docJspx, docJspXml);
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
    private Document getUpdateTabularDocument(String patternName,
            WebPatternType patternType) {

        String controllerPath = webScaffoldAnnotationValues.getPath();
        Validate.notNull(controllerPath, PATH_IS_NOT_SPECIFIED
                + webScaffoldAnnotationValues.getGovernorTypeDetails()
                        .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        Document document = builder.newDocument();

        // Add document namespaces
        Element div = (Element) document.appendChild(new XmlElementBuilder(
                DIV_ELEMENT, document)
                .addAttribute("xmlns:form",
                        "urn:jsptagdir:/WEB-INF/tags/pattern/form")
                .addAttribute("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/pattern/form/fields")
                .addAttribute("xmlns:jsp", "http://java.sun.com/JSP/Page")
                .addAttribute("xmlns:pattern",
                        "urn:jsptagdir:/WEB-INF/tags/pattern")
                .addAttribute("version", "2.0")
                .addChild(
                        new XmlElementBuilder("jsp:directive.page", document)
                                .addAttribute("contentType",
                                        "text/html;charset=UTF-8").build())
                .addChild(
                        new XmlElementBuilder("jsp:output", document)
                                .addAttribute("omit-xml-declaration", "yes")
                                .build()).build());

        Element formUpdate = null;
        if (patternType.equals(WebPatternType.tabular_edit_register)) {

            // Add form update element
            formUpdate = new XmlElementBuilder("form:updateregister", document)
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("fu:"
                                    + formbackingType
                                            .getFullyQualifiedTypeName()))
                    /* Modified previous value entityName */
                    .addAttribute(
                            "modelAttribute",
                            formbackingTypeMetadata.getPlural().toLowerCase()
                                    .concat("Tab")).build();
        }
        else {
            // Add form update element
            formUpdate = new XmlElementBuilder("form:update", document)
                    .addAttribute(
                            ID_ATTRIBUTE,
                            XmlUtils.convertId("fu:".concat(formbackingType
                                    .getFullyQualifiedTypeName())))
                    /* Modified previous value entityName */
                    .addAttribute(
                            "modelAttribute",
                            formbackingTypeMetadata.getPlural().toLowerCase()
                                    .concat("Tab")).build();
        }

        if (!controllerPath.toLowerCase().equals(
                formbackingType.getSimpleTypeName().toLowerCase())) {
            formUpdate.setAttribute(PATH_ATTRIBUTE, controllerPath);
        }
        if (!ID_ATTRIBUTE.equals(formbackingTypePersistenceMetadata
                .getIdentifierField().getFieldName().getSymbolName())) {
            formUpdate.setAttribute("idField",
                    formbackingTypePersistenceMetadata.getIdentifierField()
                            .getFieldName().getSymbolName());
            formUpdate
                    .setAttribute(
                            ID_DISABLED_ATTRIBUTE,
                            String.valueOf(isIdFieldAutogenerated(formbackingTypePersistenceMetadata
                                    .getIdentifierField())));
            if (!patternType.equals(WebPatternType.tabular_edit_register)) {
                formUpdate
                        .setAttribute(
                                ID_RENDER_ATTRIBUTE,
                                String.valueOf(!isIdFieldAutogenerated(formbackingTypePersistenceMetadata
                                        .getIdentifierField())));
            }
        }
        else {
            formUpdate
                    .setAttribute(
                            ID_DISABLED_ATTRIBUTE,
                            String.valueOf(isIdFieldAutogenerated(formbackingTypePersistenceMetadata
                                    .getIdentifierField())));
            if (!patternType.equals(WebPatternType.tabular_edit_register)) {
                formUpdate
                        .setAttribute(
                                ID_RENDER_ATTRIBUTE,
                                String.valueOf(!isIdFieldAutogenerated(formbackingTypePersistenceMetadata
                                        .getIdentifierField())));
            }
        }
        if (null == formbackingTypePersistenceMetadata
                .getVersionAccessorMethod()) {
            formUpdate.setAttribute("versionField", "none");
        }
        else {
            JavaSymbolName propertyName = BeanInfoUtils
                    .getPropertyNameForJavaBeanMethod(formbackingTypePersistenceMetadata
                            .getVersionAccessorMethod());
            if (!"version".equals(propertyName.getSymbolName())) {
                String methodName = formbackingTypePersistenceMetadata
                        .getVersionAccessorMethod().getMethodName()
                        .getSymbolName();
                formUpdate
                        .setAttribute("versionField", methodName.substring(3));
            }
        }

        // Handle Roo identifiers
        List<FieldMetadata> formFields = new ArrayList<FieldMetadata>();
        if (formbackingTypePersistenceMetadata.getRooIdentifierFields().size() > 0) {
            formUpdate.setAttribute("compositePkField",
                    formbackingTypePersistenceMetadata.getIdentifierField()
                            .getFieldName().getSymbolName());
            for (FieldMetadata field : formbackingTypePersistenceMetadata
                    .getRooIdentifierFields()) {
                FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                        field);
                fieldBuilder.setFieldName(new JavaSymbolName(
                        formbackingTypePersistenceMetadata.getIdentifierField()
                                .getFieldName().getSymbolName()
                                + "." + field.getFieldName().getSymbolName()));
                formFields.add(fieldBuilder.build());
            }
        }
        formFields.addAll(eligibleFields);

        createFieldsForCreateAndUpdate(entityName, relatedDomainTypes,
                formFields, document, formUpdate, false);
        formUpdate.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(formUpdate));
        div.appendChild(formUpdate);

        List<FieldMetadata> fieldsOfRelations = new ArrayList<FieldMetadata>();
        for (FieldMetadata field : formFields) {
            if ((field.getCustomData().keySet()
                    .contains(CustomDataKeys.ONE_TO_MANY_FIELD) || field
                    .getCustomData().keySet()
                    .contains(CustomDataKeys.MANY_TO_MANY_FIELD))
                    && isRelationVisible(patternName, field.getFieldName()
                            .getSymbolName())) {
                fieldsOfRelations.add(field);
            }
        }

        if (!fieldsOfRelations.isEmpty()) {
            formUpdate.setAttribute("related", TRUE_VALUE);
            Element patternRelations = getTabularRelationsDocument(patternName,
                    document, fieldsOfRelations);
            div.appendChild(patternRelations);
        }

        return document;
    }

    /**
     * Says if given identifier field is autogenerated or not, that is, if its
     * annotated with GeneratedValue
     * 
     * @param identifierField
     * @return
     */
    private boolean isIdFieldAutogenerated(FieldMetadata identifierField) {
        List<AnnotationMetadata> annotations = identifierField.getAnnotations();
        for (AnnotationMetadata annotation : annotations) {
            if (annotation.getAnnotationType().equals(
                    new JavaType("javax.persistence.GeneratedValue"))) {
                return true;
            }
        }
        return false;
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
                    .contains(CustomDataKeys.EMBEDDED_ID_FIELD)) {
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
            }
            else if (typeMetadataHolder != null
                    && typeMetadataHolder.isEnumType()) {
                fieldElement = new XmlElementBuilder("field:select", document)
                        .addAttribute(
                                "items",
                                "${"
                                        + typeMetadataHolder.getPlural()
                                                .toLowerCase() + "}")
                        .addAttribute(PATH_ATTRIBUTE, getPathForType(fieldType))
                        .build();
            }
            else if (field.getCustomData().keySet()
                    .contains(CustomDataKeys.ONE_TO_MANY_FIELD)) {
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
                }
                else {
                    continue;
                }
            }
            else if (field.getCustomData().keySet()
                    .contains(CustomDataKeys.MANY_TO_ONE_FIELD)
                    || field.getCustomData().keySet()
                            .contains(CustomDataKeys.MANY_TO_MANY_FIELD)
                    || field.getCustomData().keySet()
                            .contains(CustomDataKeys.ONE_TO_ONE_FIELD)) {
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
                                    PATH_ATTRIBUTE,
                                    "/"
                                            + getPathForType(getJavaTypeForField(field)))
                            .build();

                    if (field.getCustomData().keySet()
                            .contains(CustomDataKeys.MANY_TO_MANY_FIELD)) {
                        fieldElement.setAttribute("multiple", TRUE_VALUE);
                    }
                }
            }
            else if (fieldType.getFullyQualifiedTypeName().equals(
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
                    fieldElement.setAttribute("future", TRUE_VALUE);
                }
                else if (null != MemberFindingUtils.getAnnotationOfType(field
                        .getAnnotations(), new JavaType(
                        "javax.validation.constraints.Past"))) {
                    fieldElement.setAttribute("past", TRUE_VALUE);
                }
            }
            else if (field.getCustomData().keySet()
                    .contains(CustomDataKeys.LOB_FIELD)) {
                fieldElement = new XmlElementBuilder(FIELD_TEXTAREA_ELEMENT,
                        document).build();
            }
            if (null != (annotationMetadata = MemberFindingUtils
                    .getAnnotationOfType(field.getAnnotations(), new JavaType(
                            "javax.validation.constraints.Size")))) {
                AnnotationAttributeValue<?> max = annotationMetadata
                        .getAttribute(new JavaSymbolName(MAX_ATTRIBUTE));
                if (max != null) {
                    int maxValue = (Integer) max.getValue();
                    if (fieldElement == null && maxValue > 30) {
                        fieldElement = new XmlElementBuilder(
                                FIELD_TEXTAREA_ELEMENT, document).build();
                    }
                }
            }
            // Use a default input field if no other criteria apply
            if (fieldElement == null) {
                fieldElement = document.createElement(FIELD_INPUT_ELEMENT);
            }
            addCommonAttributes(field, fieldElement);
            fieldElement.setAttribute(FIELD_ATTRIBUTE, fieldName);
            fieldElement.setAttribute(
                    ID_ATTRIBUTE,
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
        Validate.notNull(
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
        }
        else if (uncapitalize(field.getFieldName().getSymbolName()).contains(
                "email")) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_email");
        }
        else if (field.getFieldType().equals(
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
        if (FIELD_INPUT_ELEMENT.equals(fieldElement.getTagName())
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
        if (FIELD_INPUT_ELEMENT.equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType("javax.validation.constraints.Max")))
                && !FIELD_TEXTAREA_ELEMENT.equals(fieldElement.getTagName())) {
            AnnotationAttributeValue<?> maxA = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (maxA != null) {
                fieldElement.setAttribute(MAX_ATTRIBUTE, maxA.getValue()
                        .toString());
            }
        }
        if (FIELD_INPUT_ELEMENT.equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.DecimalMin")))
                && !FIELD_TEXTAREA_ELEMENT.equals(fieldElement.getTagName())) {
            AnnotationAttributeValue<?> decimalMin = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (decimalMin != null) {
                fieldElement.setAttribute("decimalMin", decimalMin.getValue()
                        .toString());
            }
        }
        if (FIELD_INPUT_ELEMENT.equals(fieldElement.getTagName())
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
        if (FIELD_INPUT_ELEMENT.equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.Size")))) {
            AnnotationAttributeValue<?> max = annotationMetadata
                    .getAttribute(new JavaSymbolName(MAX_ATTRIBUTE));
            if (max != null) {
                fieldElement.setAttribute(MAX_ATTRIBUTE, max.getValue()
                        .toString());
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
                fieldElement.setAttribute("required", TRUE_VALUE);
            }
        }
        if (field.getCustomData().keySet()
                .contains(CustomDataKeys.COLUMN_FIELD)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) field
                    .getCustomData().get(CustomDataKeys.COLUMN_FIELD);
            if (values.keySet().contains("nullable")
                    && ((Boolean) values.get("nullable")) == false) {
                fieldElement.setAttribute("required", TRUE_VALUE);
            }
        }
        // Disable form binding for nested fields (mainly PKs)
        if (field.getFieldName().getSymbolName().contains(".")) {
            fieldElement.setAttribute("disableFormBinding", TRUE_VALUE);
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
        if (getFileManager().exists(jspFilename)) {
            original = XmlUtils.readXml(getFileManager().getInputStream(
                    jspFilename));
            if (XmlRoundTripUtils.compareDocuments(original, proposed)) {
                DomUtils.removeTextNodes(original);
                getFileManager().createOrUpdateTextFileIfRequired(jspFilename,
                        XmlUtils.nodeToString(original), false);
            }
        }
        else {
            getFileManager().createOrUpdateTextFileIfRequired(jspFilename,
                    XmlUtils.nodeToString(proposed), false);
        }
    }

    protected String uncapitalize(String term) {
        // [ROO-1790] this is needed to adhere to the JavaBean naming
        // conventions (see JavaBean spec section 8.8)
        return Introspector.decapitalize(StringUtils.capitalize(term));
    }

    protected abstract boolean isRelatedPattern();

    public FileManager getFileManager() {
        if (_fileManager == null) {
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
                LOGGER.warning("Cannot load FileManager on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _fileManager;
        }
    }

    public ProjectOperations getProjectOperations() {
        if (_projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _projectOperations;
        }
    }

    public TilesOperations getTilesOperations() {
        if (_tilesOperations == null) {
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
                LOGGER.warning("Cannot load TilesOperations on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _tilesOperations;
        }
    }

    public MenuOperations getMenuOperations() {
        if (_menuOperations == null) {
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
                LOGGER.warning("Cannot load MenuOperations on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _menuOperations;
        }
    }

    public PropFileOperations getPropFileOperations() {
        if (_propFileOperations == null) {
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
                LOGGER.warning("Cannot load PropFileOperations on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _propFileOperations;
        }
    }

    public MetadataService getMetadataService() {
        if (_metadataService == null) {
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
                LOGGER.warning("Cannot load MetadataService on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _metadataService;
        }
    }

    public WebScreenOperations getWebScreenOperations() {
        if (_webScreenOperations == null) {
            // Get all Services implement WebScreenOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebScreenOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WebScreenOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WebScreenOperations on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _webScreenOperations;
        }
    }

    public PathResolver getPathResolver() {
        if (_pathResolver == null) {
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
                LOGGER.warning("Cannot load PathResolver on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _pathResolver;
        }
    }

    public TypeLocationService getTypeLocationService() {
        if (_typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeLocationService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on AbstractPatternJspMetadataListener.");
                return null;
            }
        }
        else {
            return _typeLocationService;
        }
    }

}
