/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.datatables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.web.mvc.jquery.JQueryOperations;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of operations this add-on offers.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public class DatatablesOperationsImpl extends AbstractOperations implements
        DatatablesOperations {

    private static final String ARGUMENT_RESOLVERS = "argument-resolvers";

    private static final JavaType SCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    private static final JavaType DATATABLES_ANNOTATION = new JavaType(
            GvNIXDatatables.class.getName());

    private static final JavaType JQUERY_ANNOTATION = new JavaType(
            "org.gvnix.addon.web.mvc.jquery.GvNIXWebJQuery");

    private static final JavaType JPA_ACTIVE_RECORD_ANNOTATION = new JavaType(
            RooJpaActiveRecord.class.getName());

    private static final String DATATABLES_CRITERIA_RESOLVER = "com.github.dandelion.datatables.extras.spring3.ajax.DatatablesCriteriasResolver";

    /**
     * Reference to ProjectOperations
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * Reference to TypeLocationService
     */
    @Reference
    private TypeLocationService typeLocationService;

    /**
     * Reference to TypeManagementService
     */
    @Reference
    private TypeManagementService typeManagementService;

    /**
     * Reference to MetadataService
     */
    @Reference
    private MetadataService metadataService;

    /**
     * Reference to MenuOperations
     */
    @Reference
    private MenuOperations menuOperations;

    /**
     * Reference to I18nSupport
     */
    @Reference
    private I18nSupport i18nSupport;

    /**
     * Reference to PropFileOperations
     */
    @Reference
    private PropFileOperations propFileOperations;

    /**
     * Reference to WebMvcOperations
     */
    @Reference
    private WebMvcOperations webMvcOperations;

    /**
     * Update dependencies if is needed
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        super.activate(context);
        // Check if setup is already executed
        if (isAddAvailable()) {
            // Update dependencies
            setupProjectPom();
        }
    }

    /** {@inheritDoc} */
    public boolean isAddAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FEATURE_NAME_GVNIX_DATATABLES);
    }

    /** {@inheritDoc} */
    public boolean isSetupAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.MVC)
                && projectOperations
                        .isFeatureInstalledInFocusedModule(JQueryOperations.FEATURE_NAME_GVNIX_JQUERY)
                && !projectOperations
                        .isFeatureInstalledInFocusedModule(FEATURE_NAME_GVNIX_DATATABLES);
    }

    /** {@inheritDoc} */
    public boolean isUpdateTagsAvailable() {
        return isAddAvailable();
    }

    /** {@inheritDoc} */
    public void annotateController(JavaType javaType, boolean ajax) {
        annotateController(javaType, ajax, "", false);
    }

    /** {@inheritDoc} */
    public void annotateController(JavaType javaType, boolean ajax,
            String mode, boolean inlineEditing) {
        Validate.notNull(javaType, "Controller required");

        ClassOrInterfaceTypeDetails existing = getControllerDetails(javaType);

        // Get controller annotation
        final AnnotationMetadata controllerAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        SCAFFOLD_ANNOTATION);

        Validate.isTrue(
                controllerAnnotation != null,
                javaType.getFullyQualifiedTypeName()
                        .concat(": Operation for @RooWebScaffold annotated controllers only."));

        final AnnotationMetadata jQueryAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        JQUERY_ANNOTATION);

        Validate.isTrue(
                jQueryAnnotation != null,
                javaType.getFullyQualifiedTypeName()
                        .concat(": Operation for @GvNIXWebJQuery annotated controllers only."));

        final boolean isDatatablesAnnotated = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        DATATABLES_ANNOTATION) != null;

        // Check is JPA active record (currently add-on only supports this
        // entities)
        JavaType entityValueType = getControllerFormBackingObject(controllerAnnotation);
        ClassOrInterfaceTypeDetails entity = typeLocationService
                .getTypeDetails(entityValueType);
        final boolean isActiveRecord = MemberFindingUtils.getAnnotationOfType(
                entity.getAnnotations(), JPA_ACTIVE_RECORD_ANNOTATION) != null;

        Validate.isTrue(isActiveRecord,
                "This command only supports JPA active record controller");

        // TODO support JPA repositories

        // Test if the annotation already exists on the target type
        if (!isDatatablesAnnotated) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    existing);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    DATATABLES_ANNOTATION);

            annotationBuilder.addBooleanAttribute("ajax", ajax);
            if (inlineEditing) {
                annotationBuilder.addBooleanAttribute("inlineEditing",
                        inlineEditing);
            }
            if (StringUtils.isNotBlank(mode)) {
                annotationBuilder.addStringAttribute("mode", mode);
            }

            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder
                    .build());

            // Save changes to disk
            typeManagementService
                    .createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder
                            .build());

            // doUpdateListMenuUrl(javaType, controllerAnnotation);
        }
    }

    /** {@inheritDoc} */
    public void annotateDetailController(JavaType javaType, String property) {
        Validate.notNull(javaType, "Controller required");
        Validate.notBlank(property, "Property required");

        // Get java type controller
        ClassOrInterfaceTypeDetails existing = getControllerDetails(javaType);

        // Get controller datatables annotation
        AnnotationMetadata datatablesAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        DATATABLES_ANNOTATION);

        // If no datatables annotation: user message and exit.
        // Detail only allowed when datatables already applied into controller.
        Validate.isTrue(
                datatablesAnnotation != null,
                "A detail datatables only can be added into an already datatables controller.\n"
                        + "Please, run 'web mvc datatables add' before or select another type.");

        // TODO Validate controller related with property already has datatables

        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArrayAttributeValue<StringAttributeValue> detailFieldsAttributesOld = (ArrayAttributeValue) datatablesAnnotation
                .getAttribute(DatatablesConstants.DATATABLES_ANNOTATION_DETAIL_FIELDS_ATTRIBUTE);

        // Get java type controller builder
        ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                existing);

        // Initialize string attributes list for detail fields
        final List<StringAttributeValue> detailFieldsAttributes = new ArrayList<StringAttributeValue>();

        // Create a string attribute for property
        final StringAttributeValue detailFieldAttribute = new StringAttributeValue(
                new JavaSymbolName("__ARRAY_ELEMENT__"), property);

        // Add old (if exists) string attributes into list
        if (detailFieldsAttributesOld != null) {
            detailFieldsAttributes.addAll(detailFieldsAttributesOld.getValue());
        }

        // Add new string attribute into list if not included already
        if (detailFieldsAttributesOld == null
                || !detailFieldsAttributesOld.getValue().contains(
                        detailFieldAttribute)) {
            detailFieldsAttributes.add(detailFieldAttribute);
        }
        else {
            throw new IllegalArgumentException(
                    "Property was already included in the controller.");
        }

        // Create "detailFields" attributes array from string attributes list
        ArrayAttributeValue<StringAttributeValue> detailFieldsArray = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName(
                        DatatablesConstants.DATATABLES_ANNOTATION_DETAIL_FIELDS_ATTRIBUTE),
                detailFieldsAttributes);

        // Get datatables annotation builder and add "detailFields"
        AnnotationMetadataBuilder datatablesAnnotationBuilder = new AnnotationMetadataBuilder(
                datatablesAnnotation);
        datatablesAnnotationBuilder.addAttribute(detailFieldsArray);
        datatablesAnnotationBuilder.build();

        // Update annotation into controller
        classOrInterfaceTypeDetailsBuilder
                .updateTypeAnnotation(datatablesAnnotationBuilder);

        // Save controller changes to disk
        typeManagementService
                .createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder
                        .build());
    }

    private ClassOrInterfaceTypeDetails getControllerDetails(JavaType controller) {
        ClassOrInterfaceTypeDetails existing = typeLocationService
                .getTypeDetails(controller);

        Validate.notNull(existing, "Can't get Type details");
        return existing;
    }

    /**
     * Updates de list.jspx page of target controller to use datatables
     * component.
     */
    public void updateControllerJspPages(JavaType controller,
            DatatablesMetadata datatablesMetadata) {
        Validate.notNull(datatablesMetadata, "Datatables metadata required");

        String controllerPath = datatablesMetadata
                .getWebScaffoldAnnotationValues().getPath();
        updateListJspx(controller, controllerPath);
        updateCreateJspx(controller, controllerPath);
        updateShowJspx(controller, controllerPath);
        updateUpdateJspx(controller, controllerPath);

        // Note there is no need to update finder jspx because this add-on
        // uses "finderNameParam" feature provided by JQuery MVC add-on
    }

    /**
     * Updates the list.jspx page of target controller to use datatables
     * component.
     * 
     * @param controller
     * @param controllerPath
     */
    private void updateListJspx(JavaType controller, String controllerPath) {

        Map<String, String> uriMap = new HashMap<String, String>(2);

        uriMap.put("xmlns:table", DatatablesConstants.URN_TAG_DATATABLES);
        uriMap.put("xmlns:page", DatatablesConstants.URN_TAG_DATATABLES);

        updateJspx(controller, controllerPath, uriMap, "list");
    }

    /**
     * Updates the create.jspx page of target controller to use detail
     * datatables component.
     * 
     * @param controller
     * @param controllerPath
     */
    private void updateCreateJspx(JavaType controller, String controllerPath) {

        Map<String, String> uriMap = new HashMap<String, String>(1);

        uriMap.put("xmlns:form", DatatablesConstants.URN_TAG_DATATABLES);

        updateJspx(controller, controllerPath, uriMap, "create");
    }

    /**
     * Updates the show.jspx page of target controller to use detail datatables
     * component.
     * 
     * @param controller
     * @param controllerPath
     */
    private void updateShowJspx(JavaType controller, String controllerPath) {

        Map<String, String> uriMap = new HashMap<String, String>(1);

        uriMap.put("xmlns:page", DatatablesConstants.URN_TAG_DATATABLES);

        updateJspx(controller, controllerPath, uriMap, "show");
    }

    /**
     * Updates the update.jspx page of target controller to use detail
     * datatables component.
     * 
     * @param controller
     * @param controllerPath
     */
    private void updateUpdateJspx(JavaType controller, String controllerPath) {

        Map<String, String> uriMap = new HashMap<String, String>(1);

        uriMap.put("xmlns:form", DatatablesConstants.URN_TAG_DATATABLES);

        updateJspx(controller, controllerPath, uriMap, "update");
    }

    /**
     * @param controller
     * @param controllerPath
     * @param uriMap
     * @param jspxName
     */
    private void updateJspx(JavaType controller, String controllerPath,
            Map<String, String> uriMap, String jspxName) {
        Validate.notBlank(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");
        Validate.isTrue(controllerPath != null && !controllerPath.isEmpty(),
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");

        if (controllerPath != null) {
            WebProjectUtils.updateTagxUriInJspx(controllerPath, jspxName,
                    uriMap, projectOperations, fileManager);
        }
    }

    /**
     * Gets attribute value {@code path} from a controller annotation
     * 
     * @param controllerAnnotation
     * @return
     */
    private String getControllerPath(AnnotationMetadata controllerAnnotation) {
        AnnotationAttributeValue<Object> controllerPathAttribute = controllerAnnotation
                .getAttribute("path");
        if (controllerPathAttribute == null) {
            return null;
        }

        String controllerPath = (String) controllerPathAttribute.getValue();
        return controllerPath;
    }

    /**
     * Gets attribute value {@code formBackingObject} from a controller
     * annotation
     * 
     * @param controllerAnnotation
     * @return
     */
    private JavaType getControllerFormBackingObject(
            AnnotationMetadata controllerAnnotation) {
        AnnotationAttributeValue<Object> controllerAttribute = controllerAnnotation
                .getAttribute("formBackingObject");
        if (controllerAttribute == null) {
            return null;
        }
        JavaType controllerPath = (JavaType) controllerAttribute.getValue();
        return controllerPath;
    }

    /** {@inheritDoc} */
    public void annotateAll(boolean ajax) {
        // Locate all controllers and annotate it
        for (JavaType type : typeLocationService
                .findTypesWithAnnotation(SCAFFOLD_ANNOTATION)) {
            annotateController(type, ajax);
        }
    }

    /** {@inheritDoc} */
    public void setup(JavaPackage webPackage) {
        // Setup repository and dependencies
        setupProjectPom();

        // Install all artifacts
        updateTags();

        // Copy properties file
        copyPropertiesFile();

        // Add required i18n keys
        addI18nKeys();

        // Add all js to
        // WEB-INF/tags/util/load-scripts.tagx
        addJSToLoadScriptsTag();

        // Update webmvc-config.xml
        updateWebMvcConfigFile(webPackage);

        // Update web.xml
        updateWebXmlFile();
    }

    /**
     * Update project pom: install repositories and dependencies
     */
    private void setupProjectPom() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepositories(
                    projectOperations.getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            projectOperations.addProperty(projectOperations
                    .getFocusedModuleName(), new Property(property));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }

    @Override
    public void updateTags() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        LogicalPath webappPath = WebProjectUtils
                .getWebappPath(projectOperations);

        // images
        OperationUtils.updateDirectoryContents("images/datatables/*.*",
                pathResolver.getIdentifier(webappPath, "/images/datatables"),
                fileManager, context, getClass());

        // install js
        copyDirectoryContents("scripts/datatables/*.js",
                pathResolver.getIdentifier(webappPath, "/scripts/datatables"),
                true);
        copyDirectoryContents("scripts/datatables/README.txt",
                pathResolver.getIdentifier(webappPath, "/scripts/datatables"),
                true);

        // install js i18n
        copyDirectoryContents("scripts/datatables/i18n/*.txt",
                pathResolver.getIdentifier(webappPath,
                        "/scripts/datatables/i18n"), true);

        // install css
        copyDirectoryContents("styles/datatables/*.css",
                pathResolver.getIdentifier(webappPath, "/styles/datatables"),
                true);

        // install tags
        copyDirectoryContents("tags/datatables/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/datatables"), true);
    }

    @Deprecated
    public void updateListMenuUrl(JavaType controller) {

        Validate.notNull(controller, "Controller required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = getControllerDetails(controller);

        // Get controller annotation
        final AnnotationMetadata controllerAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        SCAFFOLD_ANNOTATION);

        doUpdateListMenuUrl(controller, controllerAnnotation);

    }

    /**
     * Remove <code>page</code> and <code>size</code> parameters from list menu
     * link for target controller
     * 
     * @param controllerDetails
     * @param controllerAnnotation
     * @deprecated
     */
    public void doUpdateListMenuUrl(JavaType controller,
            AnnotationMetadata controllerAnnotation) {

        String controllerPath = getControllerPath(controllerAnnotation);

        JavaType formBackingType = getControllerFormBackingObject(controllerAnnotation);
        Validate.notNull(formBackingType,
                "formBackingObject is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");
        final JavaSymbolName categoryName = new JavaSymbolName(
                formBackingType.getSimpleTypeName());

        final LogicalPath webappPath = LogicalPath.getInstance(
                Path.SRC_MAIN_WEBAPP, "");

        final JavaSymbolName listMenuItemId = new JavaSymbolName("list");
        menuOperations.cleanUpMenuItem(categoryName, listMenuItemId,
                MenuOperations.DEFAULT_MENU_ITEM_PREFIX, webappPath);

        menuOperations.addMenuItem(categoryName, listMenuItemId,
                "global_menu_list", "/" + controllerPath,
                MenuOperations.DEFAULT_MENU_ITEM_PREFIX, webappPath);
    }

    public void addJSToLoadScriptsTag() {

        List<Pair<String, String>> cssList = new ArrayList<Pair<String, String>>();
        List<Pair<String, String>> jsList = new ArrayList<Pair<String, String>>();

        // Add jquery.datatables.css url resolution
        cssList.add(new ImmutablePair<String, String>(
                "css_jquery_datatables_url",
                "/resources/styles/datatables/jquery.dataTables.css"));

        // Add gvnix.dataTables.css url resolution
        cssList.add(new ImmutablePair<String, String>(
                "css_gvnix_datatables_url",
                "/resources/styles/datatables/gvnix.dataTables.css"));

        // Add jquery.datatables.js
        jsList.add(new ImmutablePair<String, String>(
                "js_jquery_datatables_url",
                "/resources/scripts/datatables/jquery.dataTables.min.js"));

        // Add jquery.dataTables.ext.gvnix.selection.js
        jsList.add(new ImmutablePair<String, String>(
                "js_jquery_datatables_selection_url",
                "/resources/scripts/datatables/jquery.dataTables.ext.gvnix.selection.js"));

        // Add jquery.dataTables.ext.gvnix.selection.js
        jsList.add(new ImmutablePair<String, String>(
                "js_jquery_datatables_edit_url",
                "/resources/scripts/datatables/jquery.dataTables.ext.gvnix.editing.js"));

        // Add jquery.dataTables.ext.gvnix.rowclick.js
        jsList.add(new ImmutablePair<String, String>(
                "js_jquery_datatables_rowclik_url",
                "/resources/scripts/datatables/jquery.dataTables.ext.gvnix.rowclick.js"));

        // Add jquery.dataTables.ext.gvnix.detail.js
        jsList.add(new ImmutablePair<String, String>(
                "js_jquery_datatables_detail_url",
                "/resources/scripts/datatables/jquery.dataTables.ext.gvnix.detail.js"));

        // Add jquery.dataTables.ext.gvnix.js
        jsList.add(new ImmutablePair<String, String>(
                "js_jquery_datatables_custom_api_url",
                "/resources/scripts/datatables/jquery.dataTables.ext.gvnix.js"));

        WebProjectUtils.addJsAndCssToLoadScriptsTag(cssList, jsList,
                projectOperations, fileManager);
    }

    @Override
    public void updateWebMvcConfigFile(JavaPackage destinationPackage) {
        LogicalPath webappPath = WebProjectUtils
                .getWebappPath(projectOperations);
        String webMvcXmlPath = projectOperations.getPathResolver()
                .getIdentifier(webappPath, "WEB-INF/spring/webmvc-config.xml");
        Validate.isTrue(fileManager.exists(webMvcXmlPath),
                "webmvc-config.xml not found");

        MutableFile webMvcXmlMutableFile = null;
        Document webMvcXml;

        try {
            webMvcXmlMutableFile = fileManager.updateFile(webMvcXmlPath);
            webMvcXml = XmlUtils.getDocumentBuilder().parse(
                    webMvcXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webMvcXml.getDocumentElement();

        // Get annotation-driven for conversion service
        List<Element> annotationDrivenFound = XmlUtils
                .findElements(
                        "annotation-driven[@conversion-service='applicationConversionService']",
                        root);

        if (annotationDrivenFound.isEmpty()) {
            // Delegate on Roo install operation
            if (destinationPackage == null) {
                throw new IllegalStateException(
                        "No conversion service found on webmvc-config.xml: package parameter is required.");
            }
            webMvcOperations.installConversionService(destinationPackage);

            // commit fileManager changes (so get final webmvc-config.xml
            // content)
            fileManager.commit();

            // reload xml file after Roo update it
            try {
                webMvcXmlMutableFile = fileManager.updateFile(webMvcXmlPath);
                webMvcXml = XmlUtils.getDocumentBuilder().parse(
                        webMvcXmlMutableFile.getInputStream());
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
            root = webMvcXml.getDocumentElement();

            // Get annotation-driven for conversion service
            annotationDrivenFound = XmlUtils
                    .findElements(
                            "annotation-driven[@conversion-service='applicationConversionService']",
                            root);
        }
        Validate.isTrue(
                !annotationDrivenFound.isEmpty(),
                "mvc:annotation-driven conversion-service=\"applicationConversionService\" tag not found in webmvc-config.xml");

        Validate.isTrue(
                annotationDrivenFound.size() == 1,
                "too much (1 expected) mvc:annotation-driven conversion-service=\"applicationConversionService\" tag found in webmvc-config.xml");

        Element annotationDriven = annotationDrivenFound.get(0);
        Element argumentResolver = null;
        Element bean = null;
        boolean addBean = false;
        // Check tag contents
        if (!annotationDriven.hasChildNodes()) {
            // No children: add bean
            addBean = true;
        }
        else {
            // Look for bean
            bean = XmlUtils.findFirstElement("argument-resolvers/bean[@class='"
                    .concat(DATATABLES_CRITERIA_RESOLVER).concat("']"),
                    annotationDriven);
            if (bean == null) {
                addBean = true;
                // get argument-resolvers tag (if any)
                argumentResolver = XmlUtils.findFirstElement(
                        ARGUMENT_RESOLVERS, annotationDriven);
            }
        }
        if (addBean) {
            if (argumentResolver == null) {
                // Add missing argument-resolvers tag to annotation driven tag
                argumentResolver = webMvcXml.createElement("mvc:"
                        + ARGUMENT_RESOLVERS);
                annotationDriven.appendChild(argumentResolver);
            }
            // add bean tag to argument-resolvers
            bean = webMvcXml.createElement("bean");
            bean.setAttribute("class", DATATABLES_CRITERIA_RESOLVER);

            argumentResolver.appendChild(bean);
            XmlUtils.writeXml(webMvcXmlMutableFile.getOutputStream(), webMvcXml);
        }
    }

    @Override
    public void updateWebXmlFile() {
        LogicalPath webappPath = WebProjectUtils
                .getWebappPath(projectOperations);
        String webXmlPath = projectOperations.getPathResolver().getIdentifier(
                webappPath, "WEB-INF/web.xml");
        Validate.isTrue(fileManager.exists(webXmlPath), "web.xml not found");

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

        boolean modified = false;

        // look for filter
        Element filter = XmlUtils.findFirstElement(
                "/filter[filter-name='datatablesFilter']", root);
        if (filter == null) {
            // Create tag
            insertXmlElement(webXml, root, "filter", "filter-name",
                    "datatablesFilter", "filter-class",
                    "com.github.dandelion.datatables.core.web.filter.DatatablesFilter");
            modified = true;
        }

        // look for filter-mapping
        Element filterMapping = XmlUtils.findFirstElement(
                "/filter-mapping[filter-name='datatablesFilter']", root);
        if (filterMapping == null) {

            // Create tag
            insertXmlElement(webXml, root, "filter-mapping", "filter-name",
                    "datatablesFilter", "url-pattern", "/*");
            modified = true;
        }

        // look for servlet
        Element servlet = XmlUtils.findFirstElement(
                "/servlet[servlet-name='datatablesController']", root);
        if (servlet == null) {

            // Create tag
            insertXmlElement(webXml, root, "servlet", "servlet-name",
                    "datatablesController", "servlet-class",
                    "com.github.dandelion.datatables.core.web.servlet.DatatablesServlet");
            modified = true;
        }

        // look for servlet-mapping
        Element servletMapping = XmlUtils.findFirstElement(
                "/servlet-mapping[servlet-name='datatablesController']", root);
        if (servletMapping == null) {
            // Create tag
            insertXmlElement(webXml, root, "servlet-mapping", "servlet-name",
                    "datatablesController", "url-pattern",
                    "/datatablesController/*");
            modified = true;
        }

        if (modified) {
            XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);
        }
    }

    /**
     * Insert a new element of type {@code nodeName} into {@code parent} with
     * definition declared in {@code subElementsAndValue}.
     * {@code subElementsAndValue} is composed in pairs of <i>nodeName</i> and
     * <i>textValue</i>.
     * 
     * @param doc
     * @param parent
     * @param nodeName
     * @param subElementsAndValue
     */
    private void insertXmlElement(Document doc, Element parent,
            String nodeName, String... subElementsAndValue) {
        Validate.isTrue(subElementsAndValue.length % 2 == 0,
                "subElementsAndValue must be even");

        Element newElement = doc.createElement(nodeName);

        Element subElement;
        for (int i = 0; i < subElementsAndValue.length - 1; i = i + 2) {
            subElement = doc.createElement(subElementsAndValue[i]);
            subElement.setTextContent(subElementsAndValue[i + 1]);
            newElement.appendChild(subElement);
        }
        // insert element as last element of the node type
        Node inserPosition = null;
        // Locate last node of this type
        List<Element> elements = XmlUtils.findElements(nodeName, parent);
        if (!elements.isEmpty()) {
            inserPosition = elements.get(elements.size() - 1).getNextSibling();
        }

        // Add node
        if (inserPosition == null) {
            parent.appendChild(newElement);
        }
        else {
            parent.insertBefore(newElement, inserPosition);
        }
    }

    @Override
    public void copyPropertiesFile() {
        PathResolver pathResolver = projectOperations.getPathResolver();

        LogicalPath resouresPath = LogicalPath.getInstance(
                Path.SRC_MAIN_RESOURCES,
                projectOperations.getFocusedModuleName());

        copyDirectoryContents("resources/*.properties",
                pathResolver.getIdentifier(resouresPath, "/"), true);

    }

    @Override
    public void addI18nKeys() {
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
     * Gets the feature name managed by this operations class.
     * 
     * @return feature name
     */
    public String getName() {
        return FEATURE_NAME_GVNIX_DATATABLES;
    }

    /**
     * Returns true if the given feature is installed in current project.
     * 
     * @param moduleName feature name to check in current project
     * @return true if given feature name is installed, otherwise returns false
     */
    public boolean isInstalledInModule(final String moduleName) {
        final Pom pom = projectOperations.getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }
        // Look for datatables taglib dependency
        for (final Dependency dependency : pom.getDependencies()) {
            if ("org.gvnix.datatables.tags".equals(dependency.getArtifactId())) {
                return true;
            }
        }
        return false;
    }
}