/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
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
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.addon.datatables;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.DomUtils;
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
/**
 * @author jmvivo
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

    private static final JavaType JPA_ACTIVE_RECORD_ANNOTATION = new JavaType(
            RooJpaActiveRecord.class.getName());

    private static final String DATATABLES_CRITERIA_RESOLVER = "com.github.dandelion.datatables.extras.spring3.ajax.DatatablesCriteriasResolver";

    /**
     * Reference to ProjectOperations
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Reference to TypeLocationService
     */
    @Reference private TypeLocationService typeLocationService;

    /**
     * Reference to TypeManagementService
     */
    @Reference private TypeManagementService typeManagementService;

    /**
     * Reference to MetadataService
     */
    @Reference private MetadataService metadataService;

    /**
     * Reference to MenuOperations
     */
    @Reference private MenuOperations menuOperations;

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
                .isFeatureInstalledInFocusedModule(DatatablesFeature.NAME);
    }

    /** {@inheritDoc} */
    public boolean isSetupAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.MVC)
                && !projectOperations
                        .isFeatureInstalledInFocusedModule(DatatablesFeature.NAME);
    }

    /** {@inheritDoc} */
    public boolean isUpdateTagsAvailable() {
        return isAddAvailable();
    }

    /** {@inheritDoc} */
    public void annotateController(JavaType javaType, boolean ajax) {
        Validate.notNull(javaType, "Controller required");

        ClassOrInterfaceTypeDetails existing = getControllerDetails(javaType);

        // Get controller annotation
        final AnnotationMetadata controllerAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        SCAFFOLD_ANNOTATION);

        Validate.isTrue(controllerAnnotation != null,
                "Operation only supported for controllers");

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
                "This commando only supports JPA active record controller");

        // TODO support JPA repositories

        // Test if the annotation already exists on the target type
        if (!isDatatablesAnnotated) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    existing);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    DATATABLES_ANNOTATION);

            annotationBuilder.addBooleanAttribute("ajax", ajax);

            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder
                    .build());

            // Save changes to disk
            typeManagementService
                    .createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder
                            .build());

            doUpdateControllerListJsp(javaType, controllerAnnotation);

            doUpdateListMenuUrl(javaType, controllerAnnotation);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.roo.addon.datatables.DatatablesOperations#updateControllerListJsp
     * (org.springframework.roo.model.JavaType)
     */
    public void updateControllerListJsp(JavaType controller) {
        Validate.notNull(controller, "Controller required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = getControllerDetails(controller);

        // Get controller annotation
        final AnnotationMetadata controllerAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        SCAFFOLD_ANNOTATION);

        doUpdateControllerListJsp(controller, controllerAnnotation);
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
    private void doUpdateControllerListJsp(JavaType controller,
            AnnotationMetadata controllerAnnotation) {
        // locate list.jspx application path from @RooWebScaffold path value
        String controllerPath = getControllerPath(controllerAnnotation);
        Validate.notBlank(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");
        Validate.isTrue(controllerPath != null && !controllerPath.isEmpty(),
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        // Get list.jspx file path
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docJspx = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/views" + controllerPath + "/list.jspx");

        // Parse list.jspx document
        Document docJspXml = loadXmlDocument(docJspx);
        if (docJspXml == null) {
            // file not found: do nothing
            return;
        }

        // Get main div
        Element docRoot = docJspXml.getDocumentElement();
        Element divMain = XmlUtils.findFirstElement("/div", docRoot);

        // Update table tag namespace
        divMain.setAttribute("xmlns:table",
                "urn:jsptagdir:/WEB-INF/tags/datatables");

        // Update page tag namespace
        divMain.setAttribute("xmlns:page",
                "urn:jsptagdir:/WEB-INF/tags/datatables");

        // Update list.jspx file
        DomUtils.removeTextNodes(docJspXml);
        fileManager.createOrUpdateTextFileIfRequired(docJspx,
                XmlUtils.nodeToString(docJspXml), true);
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
    public void setup() {
        // Setup repository and dependencies
        setupProjectPom();

        // Install all artifacts
        updateTags();

        // Copy properties file
        copyPropertiesFile();

        // Add all js to
        // WEB-INF/tags/util/load-scripts.tagx
        addJSToLoadScriptsTag();

        // Update webmvc-config.xml
        updateWebMvcConfigFile();

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

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.addon.datatables.DatatablesOperations#updateTags()
     */
    @Override
    public void updateTags() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        LogicalPath webappPath = getWebappPath();

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.addon.datatables.DatatablesOperations#cleanListMenuUrl(org.
     * springframework.roo.model.JavaType)
     */
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

    private LogicalPath getWebappPath() {
        return LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP,
                projectOperations.getFocusedModuleName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.addon.datatables.DatatablesOperations#addJSToLoadScriptTag()
     */
    public void addJSToLoadScriptsTag() {

        // Parse load-script.tagx
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docTagxPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/util/load-scripts.tagx");
        Validate.isTrue(fileManager.exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = fileManager.updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        /* Add url resolutions missing */

        // Add jquery.js url resolution
        Element jqueryJsUrl = XmlUtils.findFirstElement(
                "url[@var='js_jquery_url']", root);
        if (jqueryJsUrl == null) {
            jqueryJsUrl = docTagx.createElement("spring:url");
            jqueryJsUrl.setAttribute("var", "js_jquery_url");
            jqueryJsUrl.setAttribute("value",
                    "/resources/scripts/datatables/jquery-min.js");
            root.appendChild(jqueryJsUrl);
            modified = true;
        }
        // Add jquery.datatables.js url resolution
        Element jqueryDtJsUrl = XmlUtils.findFirstElement(
                "url[@var='js_jquery_datatables_url']", root);
        if (jqueryDtJsUrl == null) {
            jqueryDtJsUrl = docTagx.createElement("spring:url");
            jqueryDtJsUrl.setAttribute("var", "js_jquery_datatables_url");
            jqueryDtJsUrl.setAttribute("value",
                    "/resources/scripts/datatables/jquery.dataTables.min.js");
            root.appendChild(jqueryDtJsUrl);
            modified = true;
        }
        // Add dataTables.custom.api.functions.js url resolution
        Element jqueryDtCustomJsUrl = XmlUtils.findFirstElement(
                "url[@var='js_jquery_datatables_custom_api_url']", root);
        if (jqueryDtCustomJsUrl == null) {
            jqueryDtCustomJsUrl = docTagx.createElement("spring:url");
            jqueryDtCustomJsUrl.setAttribute("var",
                    "js_jquery_datatables_custom_api_url");
            jqueryDtCustomJsUrl
                    .setAttribute("value",
                            "/resources/scripts/datatables/dataTables.custom.api.functions.js");
            root.appendChild(jqueryDtCustomJsUrl);
            modified = true;
        }
        // Add jquery.datatables.css url resolution
        Element jqueryDtCssUrl = XmlUtils.findFirstElement(
                "url[@var='css_jquery_datatables_url']", root);
        if (jqueryDtCssUrl == null) {
            jqueryDtCssUrl = docTagx.createElement("spring:url");
            jqueryDtCssUrl.setAttribute("var", "css_jquery_datatables_url");
            jqueryDtCssUrl.setAttribute("value",
                    "/resources/styles/datatables/jquery.dataTables.css");
            root.appendChild(jqueryDtCssUrl);
            modified = true;
        }
        // Add gvnix.dataTables.css url resolution
        Element gvnixCssUrl = XmlUtils.findFirstElement(
                "url[@var='css_gvnix_datatables_url']", root);
        if (gvnixCssUrl == null) {
            gvnixCssUrl = docTagx.createElement("spring:url");
            gvnixCssUrl.setAttribute("var", "css_gvnix_datatables_url");
            gvnixCssUrl.setAttribute("value",
                    "/resources/styles/datatables/gvnix.dataTables.css");
            root.appendChild(gvnixCssUrl);
            modified = true;
        }

        /* Declare css */
        // Add jquery.datatables.css
        Element jqueryCss = XmlUtils.findFirstElement(
                "link[@href='${css_jquery_datatables_url}']", root);
        if (jqueryCss == null) {
            jqueryCss = docTagx.createElement("link");
            jqueryCss.setAttribute("rel", "stylesheet");
            jqueryCss.setAttribute("type", "text/css");
            jqueryCss.setAttribute("media", "screen");
            jqueryCss.setAttribute("href", "${css_jquery_datatables_url}");
            root.appendChild(jqueryCss);
            modified = true;
        }

        // Add gvnix.dataTables.css
        Element gvnixCss = XmlUtils.findFirstElement(
                "link[@href='${css_gvnix_datatables_url}']", root);
        if (gvnixCss == null) {
            gvnixCss = docTagx.createElement("link");
            gvnixCss.setAttribute("rel", "stylesheet");
            gvnixCss.setAttribute("type", "text/css");
            gvnixCss.setAttribute("media", "screen");
            gvnixCss.setAttribute("href", "${css_gvnix_datatables_url}");
            root.appendChild(gvnixCss);
            modified = true;
        }

        /* Declare js */
        // Add jquery.js
        Element jqueryJs = XmlUtils.findFirstElement(
                "script[@src='${js_jquery_url}']", root);
        if (jqueryJs == null) {
            jqueryJs = docTagx.createElement("script");
            jqueryJs.setAttribute("src", "${js_jquery_url}");
            jqueryJs.setAttribute("type", "text/javascript");
            jqueryJs.appendChild(docTagx
                    .createComment("required for FF3 and Opera"));
            root.appendChild(jqueryJs);
            modified = true;
        }
        // Add jquery.datatables.js
        Element jqueryDtJs = XmlUtils.findFirstElement(
                "script[@src='${js_jquery_datatables_url}']", root);
        if (jqueryDtJs == null) {
            jqueryDtJs = docTagx.createElement("script");
            jqueryDtJs.setAttribute("src", "${js_jquery_datatables_url}");
            jqueryDtJs.setAttribute("type", "text/javascript");
            jqueryDtJs.appendChild(docTagx
                    .createComment("required for FF3 and Opera"));
            root.appendChild(jqueryDtJs);
            modified = true;
        }
        // Add dataTables.custom.api.functions.js
        Element jqueryDtCustomJs = XmlUtils.findFirstElement(
                "script[@src='${js_jquery_datatables_custom_api_url}']", root);
        if (jqueryDtCustomJs == null) {
            jqueryDtCustomJs = docTagx.createElement("script");
            jqueryDtCustomJs.setAttribute("src",
                    "${js_jquery_datatables_custom_api_url}");
            jqueryDtCustomJs.setAttribute("type", "text/javascript");
            jqueryDtCustomJs.appendChild(docTagx
                    .createComment("required for FF3 and Opera"));
            root.appendChild(jqueryDtCustomJs);
            modified = true;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }
    }

    /**
     * Load a XML {@link Document} from its file identifier
     * 
     * @param docFileIdentifier
     * @return document or null if file not found
     */
    private Document loadXmlDocument(String docFileIdentifier) {
        if (!fileManager.exists(docFileIdentifier)) {
            // document doesn't exist, so nothing to do
            return null;
        }

        // Parse document
        Document docJspXml;
        InputStream docJspxIs = null;
        try {
            docJspxIs = fileManager.getInputStream(docFileIdentifier);

            try {
                docJspXml = XmlUtils.getDocumentBuilder().parse(docJspxIs);
            }
            catch (Exception ex) {
                throw new IllegalStateException(
                        "Could not open ".concat(docFileIdentifier), ex);
            }
        }
        finally {
            IOUtils.closeQuietly(docJspxIs);
        }
        return docJspXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.addon.datatables.DatatablesOperations#updateWebMvcConfigFile()
     */
    @Override
    public void updateWebMvcConfigFile() {
        String webMvcXmlPath = projectOperations.getPathResolver()
                .getIdentifier(getWebappPath(),
                        "WEB-INF/spring/webmvc-config.xml");
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.addon.datatables.DatatablesOperations#updateWebXmlFile()
     */
    @Override
    public void updateWebXmlFile() {
        String webXmlPath = projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/web.xml");
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.addon.datatables.DatatablesOperations#copyPropertiesFile()
     */
    @Override
    public void copyPropertiesFile() {
        PathResolver pathResolver = projectOperations.getPathResolver();

        LogicalPath resouresPath = LogicalPath.getInstance(
                Path.SRC_MAIN_RESOURCES,
                projectOperations.getFocusedModuleName());

        copyDirectoryContents("resources/*.properties",
                pathResolver.getIdentifier(resouresPath, "/"), true);

    }
}