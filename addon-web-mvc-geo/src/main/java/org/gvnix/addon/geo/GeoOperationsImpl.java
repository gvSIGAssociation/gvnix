package org.gvnix.addon.geo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of GEO Addon operations
 * 
 * @author gvNIX Team
 * @since 1.4
 */
@Component
@Service
public class GeoOperationsImpl extends AbstractOperations implements
        GeoOperations {

    @Reference
    private PathResolver pathResolver;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private MetadataService metadataService;

    @Reference
    private I18nSupport i18nSupport;

    @Reference
    private PropFileOperations propFileOperations;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeManagementService typeManagementService;

    private static final JavaType SCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    private static final JavaType CONVERSION_SERVICE_ANNOTATION = new JavaType(
            RooConversionService.class.getName());

    private static final JavaType GEO_CONVERSION_SERVICE_ANNOTATION = new JavaType(
            GvNIXGeoConversionService.class.getName());

    private static final JavaType MAP_VIEWER_ANNOTATION = new JavaType(
            GvNIXMapViewer.class.getName());

    /**
     * This method checks if setup command is available
     * 
     * @return true if setup command is available
     */
    @Override
    public boolean isSetupCommandAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-geo-persistence")
                && projectOperations
                        .isFeatureInstalledInFocusedModule("gvnix-jquery");
    }

    /**
     * This method checks if add map command is available
     * 
     * @return true if add map command is available
     */
    @Override
    public boolean isMapCommandAvailable() {
        return isSetupCommandAvailable();
    }

    /**
     * This method imports all necessary element to build a gvNIX GEO
     * application
     */
    @Override
    public void setup() {
        // Adding project dependencies
        updatePomDependencies();
        // Locate all ApplicationConversionServiceFactoryBean and annotate it
        annotateApplicationConversionService();
        // Installing all necessary components
        installComponents();
        // Add sources to loadScripts
        addToLoadScripts("js_leaflet_geo_js",
                "/resources/scripts/geo/leaflet.js", false);
        addToLoadScripts("js_leaflet_ext_gvnix_url",
                "/resources/scripts/geo/leaflet.ext.gvnix.map.js", false);
        addToLoadScripts("styles_leaflet_geo_css",
                "/resources/styles/geo/leaflet.css", true);
        addToLoadScripts("styles_gvnix_leaflet_geo_css",
                "/resources/styles/geo/gvnix.leaflet.css", true);
    }

    /**
     * This method adds all necessary components to display a map view
     */
    @Override
    public void addMap(JavaType controller, JavaSymbolName path) {
        String filePackage = controller.getPackage()
                .getFullyQualifiedPackageName();
        // Doing a previous setup to install necessary components and annotate
        // ApplicationConversionService
        if (!projectOperations
                .isFeatureInstalledInFocusedModule(FEATURE_NAME_GVNIX_GEO_WEB_MVC)) {
            setup();
        }
        // Adding new controller with annotated class
        addMapViewerController(controller, path);
        // Adding new show.jspx and views.xml
        createViews(filePackage, path);
        // Add new component labels to application.properties
        addI18nComponentsProperties();
        // Add new mapController view to application.properties
        addI18nControllerProperties(filePackage, path.getReadableSymbolName()
                .toLowerCase());
    }

    /**
     * This method create necessary views to visualize map
     * 
     * @param path
     */
    public void createViews(String controllerPackage, JavaSymbolName path) {
        PathResolver pathResolver = projectOperations.getPathResolver();

        String finalPath = path.getReadableSymbolName().toLowerCase();

        // Modifying views.xml to add show.jspx view
        final String viewsPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/views.xml", finalPath));
        final String showPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/show.jspx", finalPath));

        // Copying views.xml
        if (!fileManager.exists(viewsPath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "views/views.xml");
                outputStream = fileManager.createFile(viewsPath)
                        .getOutputStream();

                // Doing this to solve problems with <!DOCTYPE element
                // ////////////////////////////////////////////////
                PrintWriter writer = new PrintWriter(outputStream);
                writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                writer.println("<!DOCTYPE tiles-definitions PUBLIC \"-//Apache Software Foundation//DTD Tiles Configuration 2.1//EN\" \"http://tiles.apache.org/dtds/tiles-config_2_1.dtd\">");
                writer.println("<tiles-definitions>");
                writer.println(String.format(
                        "   <definition extends=\"default\" name=\"%s/show\">",
                        finalPath));
                writer.println(String
                        .format("      <put-attribute name=\"body\" value=\"/WEB-INF/views/%s/show.jspx\"/>",
                                finalPath));
                writer.println("   </definition>");
                writer.println("</tiles-definitions>");

                writer.flush();
                writer.close();
                // ////////////////////////////////////////////

                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        // Copying show.jspx
        if (!fileManager.exists(showPath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "views/show.jspx");
                outputStream = fileManager.createFile(showPath)
                        .getOutputStream();

                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        // If show.jspx file doesn't exists, show an error
        if (!fileManager.exists(showPath)) {
            throw new RuntimeException(String.format(
                    "ERROR. Not exists show.jspx file on 'views/%s' folder",
                    finalPath));
        }
        else {
            // Getting document and adding definition

            Document docXml = WebProjectUtils.loadXmlDocument(showPath,
                    fileManager);

            Element docRoot = docXml.getDocumentElement();

            // Creating page:map element
            String mapId = String.format("ps_%s_%s",
                    controllerPackage.replaceAll("[.]", "_"),
                    path.getSymbolNameCapitalisedFirstLetter());
            Element map = docXml.createElement("page:map");
            map.setAttribute("id", mapId);
            map.setAttribute("z", "user-managed");

            // Creating page:toc element and adding to map
            Element toc = docXml.createElement("page:toc");
            toc.setAttribute("id", String.format("%s_toc", mapId));
            toc.setAttribute("items", "${layers}");
            toc.setAttribute("z", "user-managed");
            map.appendChild(toc);

            // Adding childs to mainDiv
            docRoot.appendChild(map);

            fileManager.createOrUpdateTextFileIfRequired(showPath,
                    XmlUtils.nodeToString(docXml), true);

        }

    }

    /**
     * This method generates a new class annotated with @GvNIXMapViewer
     * 
     * @param controller
     * @param path
     */
    public void addMapViewerController(JavaType controller, JavaSymbolName path) {
        // Getting all classes with @GvNIXMapViewer annotation
        // and checking that not exists another with the specified path
        for (JavaType mapViewer : typeLocationService
                .findTypesWithAnnotation(MAP_VIEWER_ANNOTATION)) {

            Validate.notNull(mapViewer, "@GvNIXMapViewer required");

            ClassOrInterfaceTypeDetails mapViewerController = typeLocationService
                    .getTypeDetails(mapViewer);

            // Getting RequestMapping annotations
            final AnnotationMetadata requestMappingAnnotation = MemberFindingUtils
                    .getAnnotationOfType(mapViewerController.getAnnotations(),
                            SpringJavaType.REQUEST_MAPPING);

            Validate.notNull(mapViewer, String.format(
                    "Error on %s getting @RequestMapping value", mapViewer));

            String requestMappingPath = requestMappingAnnotation
                    .getAttribute("value").getValue().toString();
            // If exists some path like the selected, shows an error
            String finalPath = String.format("/%s", path.toString());
            if (finalPath.equals(requestMappingPath)) {
                throw new RuntimeException(
                        String.format(
                                "ERROR. There's other class annotated with @GvNIXMapViewer and path \"%s\"",
                                finalPath));
            }
        }

        // Create new class
        createNewController(controller, generateJavaType(controller), path);
    }

    /**
     * This method creates a controller using specified configuration
     * 
     * @param controller
     * @param target
     * @param path
     */
    public void createNewController(JavaType controller, JavaType target,
            JavaSymbolName path) {
        Validate.notNull(controller, "Entity required");
        if (target == null) {
            target = generateJavaType(controller);
        }

        Validate.isTrue(
                !JdkJavaType.isPartOfJavaLang(target.getSimpleTypeName()),
                "Target name '%s' must not be part of java.lang",
                target.getSimpleTypeName());

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(target,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                target);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, target,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                2);

        // Add @Controller annotations
        annotations
                .add(new AnnotationMetadataBuilder(SpringJavaType.CONTROLLER));

        // Add @RequestMapping annotation
        AnnotationMetadataBuilder requestMappingAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);
        requestMappingAnnotation.addStringAttribute("value",
                String.format("/%s", path.toString()));
        annotations.add(requestMappingAnnotation);

        // Add @GvNIXMapViewer annotation
        AnnotationMetadataBuilder mapViewerAnnotation = new AnnotationMetadataBuilder(
                MAP_VIEWER_ANNOTATION);
        annotations.add(mapViewerAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }

    /**
     * Generates new JavaType based on <code>controller</code> class name.
     * 
     * @param controller
     * @param targetPackage if null uses <code>controller</code> package
     * @return
     */
    private JavaType generateJavaType(JavaType controller) {
        return new JavaType(
                String.format("%s.%s", controller.getPackage()
                        .getFullyQualifiedPackageName(), controller
                        .getSimpleTypeName()));
    }

    /**
     * This method annotate ApplicationConversionServices classes to transform
     * GEO elements
     */
    public void annotateApplicationConversionService() {
        // Validate that exists web layer
        Set<JavaType> controllers = typeLocationService
                .findTypesWithAnnotation(SCAFFOLD_ANNOTATION);

        Validate.notEmpty(
                controllers,
                "There's not exists any web layer on this gvNIX application. Execute 'web mvc all --package ~.web' to create web layer.");

        // Getting all classes with @RooConversionService annotation
        for (JavaType conversorService : typeLocationService
                .findTypesWithAnnotation(CONVERSION_SERVICE_ANNOTATION)) {

            Validate.notNull(conversorService, "RooConversionService required");

            ClassOrInterfaceTypeDetails applicationConversionService = typeLocationService
                    .getTypeDetails(conversorService);

            // Only for @RooConversionService annotated controllers
            final AnnotationMetadata rooConversionServiceAnnotation = MemberFindingUtils
                    .getAnnotationOfType(
                            applicationConversionService.getAnnotations(),
                            CONVERSION_SERVICE_ANNOTATION);

            Validate.isTrue(rooConversionServiceAnnotation != null,
                    "Operation for @RooConversionService annotated classes only.");

            final boolean isGeoConversionServiceAnnotated = MemberFindingUtils
                    .getAnnotationOfType(
                            applicationConversionService.getAnnotations(),
                            GEO_CONVERSION_SERVICE_ANNOTATION) != null;

            // If annotation already exists on the target type do nothing
            if (isGeoConversionServiceAnnotated) {
                return;
            }

            ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    applicationConversionService);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    GEO_CONVERSION_SERVICE_ANNOTATION);

            // Add annotation to target type
            detailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(detailsBuilder
                    .build());
        }
    }

    /**
     * This method updates Pom dependencies and repositories
     */
    public void updatePomDependencies() {
        final Element configuration = XmlUtils.getConfiguration(getClass());
        GeoUtils.updatePom(configuration, projectOperations, metadataService);
    }

    /**
     * This method install necessary components on correct folders
     */
    public void installComponents() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        LogicalPath webappPath = getWebappPath();

        // Copy Javascript files and related resources
        copyDirectoryContents("scripts/geo/*.js",
                pathResolver.getIdentifier(webappPath, "/scripts/geo"), true);
        copyDirectoryContents("scripts/geo/images/*.png",
                pathResolver.getIdentifier(webappPath, "/scripts/geo/images"),
                true);
        copyDirectoryContents("styles/geo/*.css",
                pathResolver.getIdentifier(webappPath, "/styles/geo"), true);
        copyDirectoryContents("tags/geo/fields/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/geo/fields"), true);
        copyDirectoryContents("tags/geo/form/*.tagx",
                pathResolver
                        .getIdentifier(webappPath, "/WEB-INF/tags/geo/form"),
                true);
    }

    /**
     * This method adds reference in laod-script.tagx to use
     * jquery.loupeField.ext.gvnix.js
     */
    public void addToLoadScripts(String varName, String url, boolean isCSS) {
        // Modify Roo load-scripts.tagx
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

        if (isCSS) {
            modified = WebProjectUtils.addCssToTag(docTagx, root, varName, url)
                    || modified;
        }
        else {
            modified = WebProjectUtils.addJSToTag(docTagx, root, varName, url)
                    || modified;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }

    }

    /**
     * This method add necessary properties to messages.properties
     */
    public void addI18nComponentsProperties() {
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
     * This method add necessary properties to messages.properties for
     * Controller
     */
    public void addI18nControllerProperties(String controllerPackage,
            String path) {

        Map<String, String> propertyList = new HashMap<String, String>();
        propertyList.put(
                String.format("label_%s_%s",
                        controllerPackage.replaceAll("[.]", "_"), path),
                "Entity Map Viewer");
        propertyList.put(
                String.format("label_%s_%s_toc",
                        controllerPackage.replaceAll("[.]", "_"), path),
                "Layers");

        propFileOperations.addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

    }

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    public LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(projectOperations);
    }

    // Feature methods -----

    /**
     * Gets the feature name managed by this operations class.
     * 
     * @return feature name
     */
    @Override
    public String getName() {
        return FEATURE_NAME_GVNIX_GEO_WEB_MVC;
    }

    /**
     * Returns true if GEO is installed
     */
    @Override
    public boolean isInstalledInModule(String moduleName) {
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "scripts/geo/leaflet.js");
        return fileManager.exists(dirPath);
    }

}