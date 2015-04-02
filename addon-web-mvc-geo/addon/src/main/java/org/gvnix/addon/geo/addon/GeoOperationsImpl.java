package org.gvnix.addon.geo.addon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.geo.addon.listeners.GeoDependencyListener;
import org.gvnix.addon.geo.annotations.GvNIXGeoConversionService;
import org.gvnix.addon.geo.annotations.GvNIXMapViewer;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.annotations.converter.RooConversionService;
import org.springframework.roo.addon.web.mvc.controller.annotations.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    private static final Logger LOGGER = HandlerUtils
            .getLogger(GeoOperationsImpl.class);

    private static final String LABEL = "label";

    private static final String INDEX_ATTR = "index";

    private static final String SHOW = "show";

    private static final String SHOW_VIEW = "WEB-INF/views/%s/show.jspx";

    private static final String VALUE = "value";

    private static final String ERR_N_TO_CREATE_NEW_MAP = "ERROR. Is necesary to create new map element using \"web mvc geo controller\" command before generate geo web layer";

    private static final JavaType GVNIX_ENTITY_MAP_LAYER_ANNOTATION = new JavaType(
            "org.gvnix.addon.jpa.annotations.geo.providers.hibernatespatial.GvNIXEntityMapLayer");

    private static final JavaType GVNIX_WEB_ENTITY_MAP_LAYER_ANNOTATION = new JavaType(
            "org.gvnix.addon.geo.annotations.GvNIXWebEntityMapLayer");

    private static final JavaType ROO_WEB_SCAFFOLD_ANNOTATION = new JavaType(
            "org.springframework.roo.addon.web.mvc.controller.annotations.scaffold.RooWebScaffold");

    private static final String WEBMCV_DATABINDER_BEAN_ID = "dataBinderRequestMappingHandlerAdapter";

    private static final String JACKSON2_RM_HANDLER_ADAPTER = "org.gvnix.web.json.Jackson2RequestMappingHandlerAdapter";

    private static final String OBJECT_MAPPER = "org.gvnix.web.json.ConversionServiceObjectMapper";

    private ComponentContext cContext;

    private PathResolver pathResolver;

    private TypeLocationService typeLocationService;

    private MetadataService metadataService;

    private I18nSupport i18nSupport;

    private PropFileOperations propFileOperations;

    private ProjectOperations projectOperations;

    private MenuOperations menuOperations;

    private TypeManagementService typeManagementService;

    private WebProjectUtils webProjectUtils;

    private OperationUtils operationUtils;

    private MessageBundleUtils messageBundleUtils;

    private static final JavaType SCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    private static final JavaType CONVERSION_SERVICE_ANNOTATION = new JavaType(
            RooConversionService.class.getName());

    private static final JavaType GEO_CONVERSION_SERVICE_ANNOTATION = new JavaType(
            GvNIXGeoConversionService.class.getName());

    private static final JavaType MAP_VIEWER_ANNOTATION = new JavaType(
            GvNIXMapViewer.class.getName());

    /**
     * Uses to ensure that dependencyListener will be loaded
     */
    @Reference
    private GeoDependencyListener dependencyListener;

    protected void activate(final ComponentContext componentContext) {
        cContext = componentContext;
        context = cContext.getBundleContext();
    }

    /**
     * This method checks if setup command is available
     * 
     * @return true if setup command is available
     */
    @Override
    public boolean isSetupCommandAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-geo-persistence")
                && getProjectOperations().isFeatureInstalledInFocusedModule(
                        "gvnix-jquery");
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
     * This method checks if web mvc geo all command is available
     * 
     * @return true if web nvc geo all command is available
     */
    @Override
    public boolean isAllCommandAvailable() {
        return isSetupCommandAvailable();
    }

    /**
     * This method checks if web mvc geo add command is available
     * 
     * @return true if web nvc geo add command is available
     */
    @Override
    public boolean isAddCommandAvailable() {
        return isSetupCommandAvailable();
    }

    @Override
    public boolean isFieldCommandAvailable() {
        return isSetupCommandAvailable();
    }

    @Override
    public boolean isLayerCommandAvailable() {
        return isSetupCommandAvailable();
    }

    @Override
    public boolean isToolCommandAvailable() {
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
        // Update web-mvc config
        updateWebMvcConfig();
        // Locate all ApplicationConversionServiceFactoryBean and annotate it
        annotateApplicationConversionService();
        // Installing all necessary components
        installComponents();
        // Install Bootstrap if necessary
        if (getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-bootstrap")) {
            updateGeoAddonToBootstrap();
        }
    }

    /**
     * This method adds all necessary components to display a map view
     */
    @Override
    public void addMap(JavaType controller, JavaSymbolName path,
            ProjectionCRSTypes projection) {
        String filePackage = controller.getPackage()
                .getFullyQualifiedPackageName();
        // Doing a previous setup to install necessary components and annotate
        // ApplicationConversionService
        if (!getProjectOperations().isFeatureInstalledInFocusedModule(
                FEATURE_NAME_GVNIX_GEO_WEB_MVC)) {
            setup();
        }
        // Adding new controller with annotated class
        addMapViewerController(controller, path, projection);
        // Adding new show.jspx and views.xml
        createViews(filePackage, path, projection);
        // Add new component labels to application.properties
        addI18nComponentsProperties();
        // Add new mapController view to application.properties
        addI18nControllerProperties(filePackage, path.getReadableSymbolName()
                .toLowerCase());
        // Add new menu entry for this new map view
        String finalPath = path.getReadableSymbolName().toLowerCase();
        getMenuOperations().addMenuItem(
                new JavaSymbolName("map_menu_category"),
                new JavaSymbolName(path.getReadableSymbolName()
                        + "_map_menu_entry"), path.getReadableSymbolName(),
                "global_generic", "/" + finalPath, null, getWebappPath());

    }

    /**
     * This method adds all GEO entities to all available maps or specific map
     */
    @Override
    public void all(JavaSymbolName path) {

        // Checking if exists map element before to generate geo web layer
        if (!checkExistsMapElement()) {
            throw new RuntimeException(ERR_N_TO_CREATE_NEW_MAP);
        }

        List<String> paths = new ArrayList<String>();
        // Checks if path is null or not. If is null, add all entities to all
        // available maps, if not, add all entities to specified map.
        if (path != null) {
            String pathList = path.toString();
            String[] pathsToAdd = pathList.split(",");

            for (String currentPath : pathsToAdd) {
                currentPath = currentPath.replaceAll("/", "").trim();

                // Getting map controller
                ClassOrInterfaceTypeDetails mapController = GeoUtils
                        .getMapControllerByPath(getTypeLocationService(),
                                currentPath);
                // If mapController is null show an error
                Validate.notNull(
                        mapController,
                        String.format(
                                "Controller annotated with @GvNIXMapViewer and with path \"%s\" doesn't found. Use \"web mvc geo controller\" to generate new map view.",
                                currentPath));
                paths.add(currentPath);
            }
        }
        else {
            // If path is null, entity will be added to all maps
            paths.add("");
        }

        // Looking for entities with GEO components and annotate his
        // controllers
        annotateAllGeoEntityControllers(paths);
    }

    /**
     * This method adds specific GEO entities to all available maps or specific
     * map
     */
    @Override
    public void add(JavaType controller, JavaSymbolName path) {

        // Checking if exists map element before to generate geo web layer
        if (!checkExistsMapElement()) {
            throw new RuntimeException(ERR_N_TO_CREATE_NEW_MAP);
        }

        Validate.notNull(controller,
                "Controller is necessary to execute this operation");

        // Checking that the specified controller is a valid controller
        ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                .getTypeDetails(controller);

        // Getting scaffold annotation
        AnnotationMetadata scaffoldAnnotation = controllerDetails
                .getAnnotation(ROO_WEB_SCAFFOLD_ANNOTATION);

        Validate.notNull(
                scaffoldAnnotation,
                String.format(
                        "%s is not a valid controller. Controller must be annotated with @RooWebScaffold",
                        controller.getFullyQualifiedTypeName()));

        // Check if is valid GEO Entity
        boolean isValidEntity = GeoUtils.isGeoEntity(scaffoldAnnotation,
                getTypeLocationService());

        Validate.isTrue(isValidEntity, String
                .format("Specified entity \"%s\" has not GEO fields",
                        scaffoldAnnotation.getAttribute("formBackingObject")
                                .getValue()));

        // Checking if entity has geo finder
        JavaType entity = (JavaType) scaffoldAnnotation.getAttribute(
                new JavaSymbolName("formBackingObject")).getValue();

        ClassOrInterfaceTypeDetails entityDetails = getTypeLocationService()
                .getTypeDetails(entity);
        AnnotationMetadata gvNIXEntityMapLayerAnnotation = entityDetails
                .getAnnotation(GVNIX_ENTITY_MAP_LAYER_ANNOTATION);

        Validate.notNull(
                gvNIXEntityMapLayerAnnotation,
                String.format(
                        "Specified entity \"%s\" is not annotated with @GvNIXEntityMapLayer. Use \"finder geo add\" command to generate necessary methods",
                        entity.getSimpleTypeName()));

        List<String> paths = new ArrayList<String>();
        // Add annotation to controller
        if (path != null) {
            String pathList = path.toString();
            String[] pathsToAdd = pathList.split(",");

            for (String currentPath : pathsToAdd) {
                currentPath = currentPath.replaceAll("/", "").trim();

                // Getting map controller
                ClassOrInterfaceTypeDetails mapController = GeoUtils
                        .getMapControllerByPath(getTypeLocationService(),
                                currentPath);
                // If mapController is null show an error
                Validate.notNull(
                        mapController,
                        String.format(
                                "Controller annotated with @GvNIXMapViewer doesn't found. Use \"web mvc geo controller\" to generate new map view.",
                                currentPath));
                paths.add(currentPath);
            }
        }
        else {
            // If path is null, entity will be added to all maps
            paths.add("");
        }

        annotateGeoEntityController(controller, paths);

    }

    /**
     * This method transform an input element to map controller on CRU views
     * 
     * @param controller
     * @param fieldName
     * @param color
     * @param weight
     * @param center
     * @param zoom
     * @param maxZoom
     */
    @Override
    public void field(JavaType controller, JavaSymbolName fieldName,
            String color, String weight, String center, String zoom,
            String maxZoom) {

        // Doing a previous setup to install necessary components and annotate
        // ApplicationConversionService
        if (!getProjectOperations().isFeatureInstalledInFocusedModule(
                FEATURE_NAME_GVNIX_GEO_WEB_MVC)) {
            setup();
        }

        Validate.notNull(controller, "Valid controller is necessary");

        // Getting controller Details
        ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                .getTypeDetails(controller);

        // Getting roo web scaffold annotation
        AnnotationMetadata rooWebScaffoldAnnotation = controllerDetails
                .getAnnotation(ROO_WEB_SCAFFOLD_ANNOTATION);

        Validate.notNull(
                rooWebScaffoldAnnotation,
                "Controller annotated with @RooWebScaffold is necessary. Generate controller for your entity using 'web mvc scaffold'");

        // Getting @RequestMapping annotation
        AnnotationMetadata requestMappingAnnotation = controllerDetails
                .getAnnotation(SpringJavaType.REQUEST_MAPPING);

        Validate.notNull(
                requestMappingAnnotation,
                "Controller annotated with @RequestMapping is necessary. Generate controller for your entity using 'web mvc scaffold'");

        // Getting path
        String path = requestMappingAnnotation.getAttribute(VALUE).getValue()
                .toString();

        // Getting entity
        JavaType relatedEntity = (JavaType) rooWebScaffoldAnnotation
                .getAttribute("formBackingObject").getValue();

        // Checking if current entity has Geo fields
        Validate.isTrue(GeoUtils.isGeoEntity(rooWebScaffoldAnnotation,
                getTypeLocationService()), String.format(
                "Current entity '%s' doesn't have GEO fields.",
                relatedEntity.getSimpleTypeName()));

        // Getting entity details
        ClassOrInterfaceTypeDetails entityDetails = getTypeLocationService()
                .getTypeDetails(relatedEntity);

        // Getting declared fields
        FieldMetadata field = entityDetails.getDeclaredField(fieldName);

        Validate.notNull(field, String.format(
                "Field '%s' not exists on entity '%s'", fieldName,
                relatedEntity.getSimpleTypeName()));

        // Getting field type to get package
        JavaType fieldType = field.getFieldType();
        JavaPackage fieldPackage = fieldType.getPackage();

        boolean isGeoField = fieldPackage.toString().equals(
                "com.vividsolutions.jts.geom");

        Validate.isTrue(isGeoField, String.format(
                "Current field '%s' is not a valid GEO field", fieldName));

        // Getting create.jspx
        final String createPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/create.jspx", path));

        // Getting update.jspx
        final String updatePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/update.jspx", path));

        // Getting show.jspx
        final String showPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, String.format(SHOW_VIEW, path));

        updateCRU(path, createPath, fieldName, fieldType, color, weight,
                center, zoom, maxZoom, "create");
        updateCRU(path, updatePath, fieldName, fieldType, color, weight,
                center, zoom, maxZoom, "update");
        updateCRU(path, showPath, fieldName, fieldType, color, weight, center,
                zoom, maxZoom, SHOW);

    }

    /**
     * This method add new base tile layers on selected map
     * 
     * @param name
     * @param url
     * @param path
     * @param index
     * @param opacity
     */
    @Override
    public void tileLayer(String name, String url, JavaSymbolName path,
            String index, String opacity) {

        // Checking if exists map element before to generate geo tile layer
        if (!checkExistsMapElement()) {
            throw new RuntimeException(ERR_N_TO_CREATE_NEW_MAP);
        }

        // Creating map with params to send
        Map<String, String> tileLayerAttr = new HashMap<String, String>();
        tileLayerAttr.put(INDEX_ATTR, index);
        tileLayerAttr.put("url", url);
        tileLayerAttr.put("opacity", opacity);

        name = name.replaceAll(" ", "_");

        createBaseLayer(name, tileLayerAttr, path, "tile");

    }

    /**
     * 
     * This method add new base wms layers on selected map
     * 
     * @param name
     * @param url
     * @param path
     * @param index
     * @param opacity
     * @param layers
     * @param format
     * @param transparent
     * @param styles
     * @param version
     * @param crs
     */
    @Override
    public void wmsLayer(String name, String url, JavaSymbolName path,
            String index, String opacity, String layers, String format,
            boolean transparent, String styles, String version, String crs) {
        // Checking if exists map element before to generate geo tile layer
        if (!checkExistsMapElement()) {
            throw new RuntimeException(ERR_N_TO_CREATE_NEW_MAP);
        }

        // Creating map with params to send
        Map<String, String> wmsLayerAttr = new HashMap<String, String>();
        wmsLayerAttr.put(INDEX_ATTR, index);
        wmsLayerAttr.put("url", url);
        wmsLayerAttr.put("opacity", opacity);
        wmsLayerAttr.put("layers", layers);
        wmsLayerAttr.put("format", format);
        wmsLayerAttr.put("transparent", transparent ? "true" : "false");
        wmsLayerAttr.put("styles", styles);
        wmsLayerAttr.put("version", version);
        wmsLayerAttr.put("crs", crs);

        name = name.replaceAll(" ", "_");

        createBaseLayer(name, wmsLayerAttr, path, "wms");

    }

    /**
     * 
     * This method add new measure tool on selected map
     * 
     * @param name
     * @param path
     * @param preventExitMessageCode
     */
    @Override
    public void addMeasureTool(String name, JavaSymbolName path,
            String preventExitMessageCode) {
        // Checking if exists map element before to generate geo tool
        if (!checkExistsMapElement()) {
            throw new RuntimeException(ERR_N_TO_CREATE_NEW_MAP);
        }

        // Creating map with params to send
        Map<String, String> toolAttr = new HashMap<String, String>();
        toolAttr.put("preventExitMessage", preventExitMessageCode);

        createTool(name, toolAttr, path, "measure");

    }

    /**
     * 
     * This method add new custom tool on selected map
     * 
     * @param name
     * @param path
     * @param preventExitMessageCode
     */
    @Override
    public void addCustomTool(String name, JavaSymbolName path,
            String preventExitMessageCode, String icon, String iconLibrary,
            boolean actionTool, String activateFunction,
            String deactivateFunction, String cursorIcon) {
        // Checking if exists map element before to generate geo tool
        if (!checkExistsMapElement()) {
            throw new RuntimeException(ERR_N_TO_CREATE_NEW_MAP);
        }

        // Creating map with params to send
        Map<String, String> toolAttr = new HashMap<String, String>();
        toolAttr.put("preventExitMessage", preventExitMessageCode);
        toolAttr.put("icon", icon);
        toolAttr.put("iconLibrary", iconLibrary);
        toolAttr.put("actionTool", actionTool ? "true" : "false");
        toolAttr.put("activateFunction", activateFunction);
        toolAttr.put("deactivateFunction", deactivateFunction);
        toolAttr.put("cursorIcon", cursorIcon);

        createTool(name, toolAttr, path, "custom");

        LOGGER.log(
                Level.INFO,
                String.format(
                        "**NOTE: Remember that you need to create %s and %s JavaScript functions with your custom tool implementation.",
                        activateFunction, deactivateFunction));

    }

    /**
     * This method generate a Map with map path and new tool id
     * 
     * @param toolAttr
     * @param path
     * @param string
     */
    private void createTool(String name, Map<String, String> toolAttr,
            JavaSymbolName path, String type) {

        String mapId = "";

        // Adding id base layer to application.properties
        Map<String, String> propertyList = new HashMap<String, String>();

        // Getting paths to add base layer
        Map<String, String> pathsMap = new HashMap<String, String>();

        Set<ClassOrInterfaceTypeDetails> controllerDetails = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        MAP_VIEWER_ANNOTATION);

        for (ClassOrInterfaceTypeDetails controller : controllerDetails) {
            String controllerPath = (String) controller
                    .getAnnotation(SpringJavaType.REQUEST_MAPPING)
                    .getAttribute(VALUE).getValue();
            if (path != null) {
                if (controllerPath.replaceAll("/", "").equals(
                        path.getSymbolName())) {
                    // Getting mapId
                    mapId = String.format("ps_%s_%s", controller.getType()
                            .getPackage().getFullyQualifiedPackageName()
                            .replaceAll("[.]", "_"), new JavaSymbolName(path
                            .getSymbolName().replaceAll("/", ""))
                            .getSymbolNameCapitalisedFirstLetter());

                    pathsMap.put(path.getSymbolName(), mapId + "_" + name);
                    // Add to application.properties
                    propertyList.put(LABEL + mapId.substring(2).toLowerCase()
                            + "_" + name, name);
                }
            }
            else {
                // Getting mapId
                mapId = String.format("ps_%s_%s", controller.getType()
                        .getPackage().getFullyQualifiedPackageName()
                        .replaceAll("[.]", "_"), new JavaSymbolName(
                        controllerPath.replaceAll("/", ""))
                        .getSymbolNameCapitalisedFirstLetter());

                pathsMap.put(controllerPath.replaceAll("/", ""), mapId + "_"
                        + name);
                // Add to application.properties
                propertyList.put(LABEL + mapId.substring(2).toLowerCase() + "_"
                        + name, name);
            }

        }

        addToolToMaps(name, pathsMap, toolAttr, type);

        getPropFileOperations().addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

    }

    /**
     * 
     * This method uses id map and id base layer to generate new base layer
     * using baseLayerAttr
     * 
     * @param name
     * @param pathsMap
     * @param toolAttr
     * @param type
     */
    private void addToolToMaps(String name, Map<String, String> pathsMap,
            Map<String, String> toolAttr, String type) {

        boolean exists = false;

        // Getting all maps
        for (Entry mapPath : pathsMap.entrySet()) {
            String viewPath = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    String.format(SHOW_VIEW, mapPath.getKey()));

            if (fileManager.exists(viewPath)) {
                Document docXml = getWebProjectUtils().loadXmlDocument(
                        viewPath, fileManager);

                // Getting current tools
                Element docRoot = docXml.getDocumentElement();
                NodeList tools = docRoot.getElementsByTagName("tool:" + type);
                // If exists tools, check if current name exists
                if (tools.getLength() > 0) {
                    for (int i = 0; i < tools.getLength(); i++) {
                        Node tool = tools.item(i);
                        NamedNodeMap currentToolAttr = tool.getAttributes();
                        if (currentToolAttr.getNamedItem("id") != null) {
                            String id = currentToolAttr.getNamedItem("id")
                                    .getNodeValue().toString();
                            String newId = (String) mapPath.getValue();

                            if (id.equals(newId)) {
                                LOGGER.log(Level.INFO, String.format(
                                        "Tool %s exists on '%s'", type,
                                        viewPath));
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                // If not exists, add tool
                if (!exists) {
                    // If not existstool, create new one
                    Element tool = docXml.createElement("tool:" + type);
                    tool.setAttribute("id", (String) mapPath.getValue());
                    for (Entry attr : toolAttr.entrySet()) {
                        String key = (String) attr.getKey();
                        String value = (String) attr.getValue();
                        if (StringUtils.isNotBlank(value)) {
                            tool.setAttribute(key, value);
                        }
                    }

                    // Generating z attr
                    tool.setAttribute("z",
                            XmlRoundTripUtils.calculateUniqueKeyFor(tool));

                    Node toolbarElement = docRoot.getElementsByTagName(
                            "geo:toolbar").item(0);
                    toolbarElement.appendChild(tool);

                    fileManager.createOrUpdateTextFileIfRequired(viewPath,
                            XmlUtils.nodeToString(docXml), true);
                }

            }
            else {
                throw new RuntimeException(String.format(
                        " Error getting 'WEB-INF/views/%s/show.jspx' view",
                        mapPath.getKey()));
            }
        }

    }

    /**
     * 
     * This method generate a Map with map path and new base layer id
     * 
     * @param name
     * @param baseLayerAttr
     * @param path
     * @param type
     */
    private void createBaseLayer(String name,
            Map<String, String> baseLayerAttr, JavaSymbolName path, String type) {

        String mapId = "";

        // Adding id base layer to application.properties
        Map<String, String> propertyList = new HashMap<String, String>();

        // Getting paths to add base layer
        Map<String, String> pathsMap = new HashMap<String, String>();

        Set<ClassOrInterfaceTypeDetails> controllerDetails = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        MAP_VIEWER_ANNOTATION);

        for (ClassOrInterfaceTypeDetails controller : controllerDetails) {
            String controllerPath = (String) controller
                    .getAnnotation(SpringJavaType.REQUEST_MAPPING)
                    .getAttribute(VALUE).getValue();
            if (path != null) {
                if (controllerPath.replaceAll("/", "").equals(
                        path.getSymbolName())) {
                    // Getting mapId
                    mapId = String.format("ps_%s_%s", controller.getType()
                            .getPackage().getFullyQualifiedPackageName()
                            .replaceAll("[.]", "_"), new JavaSymbolName(path
                            .getSymbolName().replaceAll("/", ""))
                            .getSymbolNameCapitalisedFirstLetter());

                    pathsMap.put(path.getSymbolName(), mapId + "_" + name);
                    // Add to application.properties
                    propertyList.put(LABEL + mapId.substring(2).toLowerCase()
                            + "_" + name, name);
                }
            }
            else {
                // Getting mapId
                mapId = String.format("ps_%s_%s", controller.getType()
                        .getPackage().getFullyQualifiedPackageName()
                        .replaceAll("[.]", "_"), new JavaSymbolName(
                        controllerPath.replaceAll("/", ""))
                        .getSymbolNameCapitalisedFirstLetter());

                pathsMap.put(controllerPath.replaceAll("/", ""), mapId + "_"
                        + name);
                // Add to application.properties
                propertyList.put(LABEL + mapId.substring(2).toLowerCase() + "_"
                        + name, name);
            }

        }

        addBaseLayerToMaps(name, pathsMap, baseLayerAttr, type);

        getPropFileOperations().addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

    }

    /**
     * 
     * This method uses id map and id base layer to generate new base layer
     * using baseLayerAttr
     * 
     * @param name
     * @param pathsMap
     * @param baseLayerAttr
     * @param type
     */
    private void addBaseLayerToMaps(String name, Map<String, String> pathsMap,
            Map<String, String> baseLayerAttr, String type) {

        boolean exists = false;

        // Getting all maps
        for (Entry mapPath : pathsMap.entrySet()) {
            String viewPath = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    String.format(SHOW_VIEW, mapPath.getKey()));

            if (fileManager.exists(viewPath)) {
                Document docXml = getWebProjectUtils().loadXmlDocument(
                        viewPath, fileManager);

                // Getting current base layers
                Element docRoot = docXml.getDocumentElement();
                NodeList baseLayers = docRoot.getElementsByTagName("layer:"
                        + type);
                // If exists baselayers, check if current name exists
                if (baseLayers.getLength() > 0) {
                    for (int i = 0; i < baseLayers.getLength(); i++) {
                        Node baseLayer = baseLayers.item(i);
                        NamedNodeMap currentBaseLayerAttr = baseLayer
                                .getAttributes();
                        if (currentBaseLayerAttr.getNamedItem("id") != null) {
                            String id = currentBaseLayerAttr.getNamedItem("id")
                                    .getNodeValue().toString();
                            String newId = (String) mapPath.getValue();

                            if (id.equals(newId)) {
                                LOGGER.log(Level.INFO, String.format(
                                        "Base %s layer exists on '%s'", type,
                                        viewPath));
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                // If not exists, add new base layer
                if (!exists) {
                    // If not exists base layers, create new one
                    Element baseLayer = docXml.createElement("layer:" + type);
                    baseLayer.setAttribute("id", (String) mapPath.getValue());
                    for (Entry attr : baseLayerAttr.entrySet()) {
                        String key = (String) attr.getKey();
                        String value = (String) attr.getValue();
                        if (StringUtils.isNotBlank(value)) {
                            baseLayer.setAttribute(key, value);
                        }
                    }

                    // Checks if exists index
                    if (StringUtils.isBlank(baseLayer.getAttribute(INDEX_ATTR))) {
                        baseLayer.setAttribute(INDEX_ATTR, Integer
                                .toString(getNewBaseLayerPosition(docRoot)));
                    }
                    // Generating z attr
                    baseLayer.setAttribute("z",
                            XmlRoundTripUtils.calculateUniqueKeyFor(baseLayer));

                    Node tocElement = docRoot.getElementsByTagName("geo:toc")
                            .item(0);
                    tocElement.appendChild(baseLayer);

                    fileManager.createOrUpdateTextFileIfRequired(viewPath,
                            XmlUtils.nodeToString(docXml), true);
                }

            }
            else {
                throw new RuntimeException(String.format(
                        " Error getting 'WEB-INF/views/%s/show.jspx' view",
                        mapPath.getKey()));
            }
        }
    }

    /**
     * Method to update JSPX CRU views with map controls
     * 
     * @param path
     * @param fieldName
     * @param fieldType
     * @param color
     * @param weight
     * @param center
     * @param zoom
     * @param maxZoom
     * @param type
     */
    private void updateCRU(String path, String viewPath,
            JavaSymbolName fieldName, JavaType fieldType, String color,
            String weight, String center, String zoom, String maxZoom,
            String type) {

        Map<String, String> uriMap = new HashMap<String, String>(1);
        uriMap.put("xmlns:geofield",
                "urn:jsptagdir:/WEB-INF/tags/geo/form/fields");

        getWebProjectUtils().addTagxUriInJspx(path, type, uriMap,
                getProjectOperations(), fileManager);

        // Updating file
        if (fileManager.exists(viewPath)) {
            Document createDoc = getWebProjectUtils().loadXmlDocument(viewPath,
                    fileManager);
            Element docRoot = createDoc.getDocumentElement();

            // Getting createForm element
            Node form = null;

            if (type.equals("create")) {
                form = docRoot.getElementsByTagName("form:create").item(0);
            }
            else if (type.equals("update")) {
                form = docRoot.getElementsByTagName("form:update").item(0);
            }
            else if (type.equals(SHOW)) {
                form = docRoot.getElementsByTagName("page:show").item(0);
            }

            // Getting all input elements
            NodeList inputs = null;
            if (type.equals("create") || type.equals("update")) {
                inputs = docRoot.getElementsByTagName("field:input");
            }
            else {
                inputs = docRoot.getElementsByTagName("field:display");
            }

            for (int i = 0; i < inputs.getLength(); i++) {
                Node input = inputs.item(i);
                NamedNodeMap inputAttr = input.getAttributes();
                Node attr = inputAttr.getNamedItem("field");

                // If field equals current field
                if (attr != null
                        && attr.getNodeValue().equals(fieldName.toString())) {
                    // Save id attribute
                    Node idAttr = inputAttr.getNamedItem("id");

                    // Create element depens of type
                    Element mapControl = null;
                    if (type.equals(SHOW)) {
                        mapControl = createDoc
                                .createElement("geofield:map-display");
                    }
                    else if (fieldType.equals(new JavaType(
                            "com.vividsolutions.jts.geom.Point"))) {
                        mapControl = createDoc
                                .createElement("geofield:map-point");
                    }
                    else if (fieldType.equals(new JavaType(
                            "com.vividsolutions.jts.geom.LineString"))) {
                        mapControl = createDoc
                                .createElement("geofield:map-line");
                        if (StringUtils.isNotBlank(color)) {
                            mapControl.setAttribute("color", color);
                        }

                        if (StringUtils.isNotBlank(weight)) {
                            mapControl.setAttribute("weight", weight);
                        }
                    }
                    else if (fieldType.equals(new JavaType(
                            "com.vividsolutions.jts.geom.Polygon"))
                            || fieldType.equals(new JavaType(
                                    "com.vividsolutions.jts.geom.Geometry"))) {
                        mapControl = createDoc
                                .createElement("geofield:map-polygon");
                        if (StringUtils.isNotBlank(color)) {
                            mapControl.setAttribute("color", color);
                        }
                        if (StringUtils.isNotBlank(weight)) {
                            mapControl.setAttribute("weight", weight);
                        }
                    }
                    else if (fieldType.equals(new JavaType(
                            "com.vividsolutions.jts.geom.MultiLineString"))) {
                        mapControl = createDoc
                                .createElement("geofield:map-multiline");
                        if (StringUtils.isNotBlank(color)) {
                            mapControl.setAttribute("color", color);
                        }
                        if (StringUtils.isNotBlank(weight)) {
                            mapControl.setAttribute("weight", weight);
                        }
                    }
                    else {
                        throw new RuntimeException(String.format(
                                "Cannot convert field type %s to map control",
                                fieldType));
                    }

                    // Adding general attributes
                    mapControl.setAttribute("field", fieldName.toString());

                    if (StringUtils.isNotBlank(center)) {
                        String validCenter = "[" + center + "]";
                        mapControl.setAttribute("center", validCenter);
                    }
                    if (StringUtils.isNotBlank(zoom)) {
                        mapControl.setAttribute("zoom", zoom);
                    }
                    if (StringUtils.isNotBlank(maxZoom)) {
                        mapControl.setAttribute("maxZoom", maxZoom);
                    }

                    // Adding saved attr
                    mapControl.setAttribute("id", idAttr.getNodeValue());
                    // Adding object
                    if (type.equals(SHOW)) {
                        mapControl.setAttribute("object", inputAttr
                                .getNamedItem("object").getNodeValue());
                    }
                    mapControl.setAttribute("z", "user-managed");

                    // Remove input geo field
                    form.removeChild(input);

                    // Add new element
                    form.appendChild(mapControl);

                    fileManager.createOrUpdateTextFileIfRequired(viewPath,
                            XmlUtils.nodeToString(createDoc), true);

                    break;

                }

            }

        }

    }

    /**
     * This method annotate controller with @GvNIXEntityMapLayer
     * 
     * @param controller
     * @param paths
     */
    public void annotateGeoEntityController(JavaType controller,
            List<String> paths) {

        // Obtain all map controllers
        List<JavaType> mapControllers = GeoUtils
                .getAllMapsControllers(getTypeLocationService());

        ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                .getTypeDetails(controller);

        // Generating annotation
        ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                controllerDetails);

        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                GVNIX_WEB_ENTITY_MAP_LAYER_ANNOTATION);

        // Add annotation to target type
        detailsBuilder.updateTypeAnnotation(annotationBuilder.build());

        // Save changes to disk
        getTypeManagementService().createOrUpdateTypeOnDisk(
                detailsBuilder.build());

        // / Update necessary map controllers with current entity
        // If developer specify map path add on it
        if (!(paths.size() == 1 && paths.get(0).equals(""))) {
            Iterator<String> pathIterator = paths.iterator();
            while (pathIterator.hasNext()) {
                // Getting path
                String currentPath = pathIterator.next();
                // Getting map controller for current path
                JavaType mapController = GeoUtils.getMapControllerByPath(
                        currentPath, getTypeLocationService());

                // Annotate map controllers adding current entity
                annotateMapController(mapController, getTypeLocationService(),
                        getTypeManagementService(), controllerDetails.getType());

            }
        }
        else {
            // If no path selected, annotate all mapControllers with
            // current entityController
            for (JavaType mapController : mapControllers) {
                // Annotate map controllers adding current entity
                annotateMapController(mapController, getTypeLocationService(),
                        getTypeManagementService(), controllerDetails.getType());
            }
        }

    }

    /**
     * This method annotate all controllers if has Geo Fields
     * 
     * @param path
     */
    public void annotateAllGeoEntityControllers(List<String> paths) {
        // Getting all entity controllers annotated with @RooWebScaffold
        Set<ClassOrInterfaceTypeDetails> entityControllers = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        ROO_WEB_SCAFFOLD_ANNOTATION);

        Validate.notNull(entityControllers,
                "Controllers with @RooWebScaffold annotation doesn't found");

        // Obtain all map controllers
        List<JavaType> mapControllers = GeoUtils
                .getAllMapsControllers(getTypeLocationService());

        Iterator<ClassOrInterfaceTypeDetails> it = entityControllers.iterator();
        while (it.hasNext()) {
            ClassOrInterfaceTypeDetails entityController = it.next();

            // Getting scaffold annotation
            AnnotationMetadata scaffoldAnnotation = entityController
                    .getAnnotation(ROO_WEB_SCAFFOLD_ANNOTATION);

            // Getting entity asociated
            Object entity = scaffoldAnnotation
                    .getAttribute("formBackingObject").getValue();
            ClassOrInterfaceTypeDetails entityDetails = getTypeLocationService()
                    .getTypeDetails((JavaType) entity);

            // Checking if geo finders are added to entity
            AnnotationMetadata gvNIXEntityMapLayerAnnotation = entityDetails
                    .getAnnotation(GVNIX_ENTITY_MAP_LAYER_ANNOTATION);

            if (gvNIXEntityMapLayerAnnotation != null) {
                // Getting all fields of current entity
                List<? extends FieldMetadata> entityFields = entityDetails
                        .getDeclaredFields();

                Iterator<? extends FieldMetadata> fieldsIterator = entityFields
                        .iterator();

                while (fieldsIterator.hasNext()) {
                    // Getting field
                    FieldMetadata field = fieldsIterator.next();

                    // Getting field type to get package
                    JavaType fieldType = field.getFieldType();
                    JavaPackage fieldPackage = fieldType.getPackage();

                    // If has jts field, annotate controller
                    if (fieldPackage.toString().equals(
                            "com.vividsolutions.jts.geom")) {

                        // Generating annotation
                        ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                                entityController);
                        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                                GVNIX_WEB_ENTITY_MAP_LAYER_ANNOTATION);

                        // Add annotation to target type
                        detailsBuilder.updateTypeAnnotation(annotationBuilder
                                .build());

                        // Save changes to disk
                        getTypeManagementService().createOrUpdateTypeOnDisk(
                                detailsBuilder.build());

                        // Update necessary map controllers with current entity
                        // If developer specify map path add on it
                        if (!(paths.size() == 1 && paths.get(0).equals(""))) {
                            Iterator<String> pathIterator = paths.iterator();
                            while (pathIterator.hasNext()) {
                                // Getting path
                                String currentPath = pathIterator.next();
                                // Getting map controller for current path
                                JavaType mapController = GeoUtils
                                        .getMapControllerByPath(currentPath,
                                                getTypeLocationService());

                                // Annotate map controllers adding current
                                // entity
                                annotateMapController(mapController,
                                        getTypeLocationService(),
                                        getTypeManagementService(),
                                        entityController.getType());

                            }
                        }
                        else {
                            // If no path selected, annotate all mapControllers
                            // with
                            // current entityController
                            for (JavaType mapController : mapControllers) {
                                // Annotate map controllers adding current
                                // entity
                                annotateMapController(mapController,
                                        getTypeLocationService(),
                                        getTypeManagementService(),
                                        entityController.getType());
                            }
                        }

                        break;
                    }
                }
            }
        }
    }

    /**
     * This method create necessary views to visualize map
     * 
     * @param path
     */
    public void createViews(String controllerPackage, JavaSymbolName path,
            ProjectionCRSTypes projection) {
        PathResolver pathResolver = getProjectOperations().getPathResolver();

        String finalPath = path.getReadableSymbolName().toLowerCase();

        // Modifying views.xml to add show.jspx view
        final String viewsPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/views.xml", finalPath));
        final String showPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, String.format(SHOW_VIEW, finalPath));

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

                // Depens of Bootstrap change extends layout
                if (getProjectOperations().isFeatureInstalledInFocusedModule(
                        "gvnix-bootstrap")) {
                    writer.println(String
                            .format("   <definition extends=\"default-map\" name=\"%s/show\">",
                                    finalPath));
                }
                else {
                    writer.println(String
                            .format("   <definition extends=\"default\" name=\"%s/show\">",
                                    finalPath));
                }
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

            Document docXml = getWebProjectUtils().loadXmlDocument(showPath,
                    fileManager);

            Element docRoot = docXml.getDocumentElement();

            // Creating geo:map element
            String mapId = String.format("ps_%s_%s",
                    controllerPackage.replaceAll("[.]", "_"),
                    path.getSymbolNameCapitalisedFirstLetter());

            Element map = docXml.createElement("geo:map");
            map.setAttribute("id", mapId);
            map.setAttribute("projection",
                    projection != null ? projection.toString() : "EPSG3857");
            map.setAttribute("z", XmlRoundTripUtils.calculateUniqueKeyFor(map));

            // Creating geo:toc element and adding to map
            Element toc = docXml.createElement("geo:toc");
            toc.setAttribute("id", String.format("%s_toc", mapId));
            toc.setAttribute("z", XmlRoundTripUtils.calculateUniqueKeyFor(toc));
            map.appendChild(toc);

            // Creating geo:toolbar element and adding to map
            Element toolbar = docXml.createElement("geo:toolbar");
            toolbar.setAttribute("id", String.format("%s_toolbar", mapId));
            toolbar.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(toolbar));
            map.appendChild(toolbar);

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
    public void addMapViewerController(JavaType controller,
            JavaSymbolName path, ProjectionCRSTypes projection) {
        // Getting all classes with @GvNIXMapViewer annotation
        // and checking that not exists another with the specified path
        for (JavaType mapViewer : getTypeLocationService()
                .findTypesWithAnnotation(MAP_VIEWER_ANNOTATION)) {

            Validate.notNull(mapViewer, "@GvNIXMapViewer required");

            ClassOrInterfaceTypeDetails mapViewerController = getTypeLocationService()
                    .getTypeDetails(mapViewer);

            // Getting RequestMapping annotations
            final AnnotationMetadata requestMappingAnnotation = MemberFindingUtils
                    .getAnnotationOfType(mapViewerController.getAnnotations(),
                            SpringJavaType.REQUEST_MAPPING);

            Validate.notNull(mapViewer, String.format(
                    "Error on %s getting @RequestMapping value", mapViewer));

            String requestMappingPath = requestMappingAnnotation
                    .getAttribute(VALUE).getValue().toString();
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
        createNewController(controller, generateJavaType(controller), path,
                projection);
    }

    /**
     * This method creates a controller using specified configuration
     * 
     * @param controller
     * @param target
     * @param path
     */
    public void createNewController(JavaType controller, JavaType target,
            JavaSymbolName path, ProjectionCRSTypes projection) {
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
                        getPathResolver().getFocusedPath(Path.SRC_MAIN_JAVA));
        File targetFile = new File(getTypeLocationService()
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
        requestMappingAnnotation.addStringAttribute(VALUE,
                String.format("/%s", path.toString()));
        annotations.add(requestMappingAnnotation);

        // Add @GvNIXMapViewer annotation with list of all entities annotated
        // with @GvNIXEntityMapLayer
        AnnotationMetadataBuilder mapViewerAnnotation = new AnnotationMetadataBuilder(
                MAP_VIEWER_ANNOTATION);

        // Generating empty class attribute value
        final List<ClassAttributeValue> entityAttributes = new ArrayList<ClassAttributeValue>();

        // Looking for all entities annotated with @GvNIXEntityMapLayer
        for (ClassOrInterfaceTypeDetails entity : getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        GVNIX_WEB_ENTITY_MAP_LAYER_ANNOTATION)) {

            // Getting map layer annotation
            AnnotationMetadata mapLayerAnnotation = entity
                    .getAnnotation(GVNIX_WEB_ENTITY_MAP_LAYER_ANNOTATION);

            // Getting maps where will be displayed
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ArrayAttributeValue<StringAttributeValue> mapLayerAttributes = (ArrayAttributeValue) mapLayerAnnotation
                    .getAttribute("maps");

            boolean addEntityToMap = false;

            if (mapLayerAttributes == null) {
                addEntityToMap = true;
            }
            else {

                List<StringAttributeValue> mapLayerAttributesValues = mapLayerAttributes
                        .getValue();

                for (StringAttributeValue map : mapLayerAttributesValues) {
                    if (map.getValue() == path.toString()) {
                        addEntityToMap = true;
                        break;
                    }
                }
            }

            if (addEntityToMap) {
                final ClassAttributeValue entityAttribute = new ClassAttributeValue(
                        new JavaSymbolName(VALUE), entity.getType());

                entityAttributes.add(entityAttribute);

            }

        }

        // Create "entityLayers" attributes array from string attributes
        // list
        ArrayAttributeValue<ClassAttributeValue> entityLayersArray = new ArrayAttributeValue<ClassAttributeValue>(
                new JavaSymbolName("entityLayers"), entityAttributes);
        // Adding controller class list to MapViewer Controller
        mapViewerAnnotation.addAttribute(entityLayersArray);

        // Adding projection to MapViewer annotation
        StringAttributeValue projectionAttribute = new StringAttributeValue(
                new JavaSymbolName("projection"),
                projection != null ? projection.toString() : "EPSG3857");
        mapViewerAnnotation.addAttribute(projectionAttribute);

        annotations.add(mapViewerAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        getTypeManagementService().createOrUpdateTypeOnDisk(cidBuilder.build());
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
        Set<JavaType> controllers = getTypeLocationService()
                .findTypesWithAnnotation(SCAFFOLD_ANNOTATION);

        Validate.notEmpty(
                controllers,
                "There's not exists any web layer on this gvNIX application. Execute 'web mvc all --package ~.web' to create web layer.");

        // Getting all classes with @RooConversionService annotation
        for (JavaType conversorService : getTypeLocationService()
                .findTypesWithAnnotation(CONVERSION_SERVICE_ANNOTATION)) {

            Validate.notNull(conversorService, "RooConversionService required");

            ClassOrInterfaceTypeDetails applicationConversionService = getTypeLocationService()
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
            getTypeManagementService().createOrUpdateTypeOnDisk(
                    detailsBuilder.build());
        }
    }

    /**
     * This method updates Pom dependencies and repositories
     */
    public void updatePomDependencies() {
        final Element configuration = XmlUtils.getConfiguration(getClass());
        GeoUtils.updatePom(configuration, getProjectOperations(),
                getMetadataService());
    }

    /**
     * This method install necessary components on correct folders
     */
    public void installComponents() {
        PathResolver pathResolver = getProjectOperations().getPathResolver();
        LogicalPath webappPath = getWebappPath();

        // Copy Javascript files and related resources
        getOperationUtils().updateDirectoryContents("scripts/leaflet/*.js",
                pathResolver.getIdentifier(webappPath, "/scripts/leaflet"),
                fileManager, cContext, getClass());
        getOperationUtils().updateDirectoryContents(
                "scripts/leaflet/images/*.png",
                pathResolver.getIdentifier(webappPath,
                        "/scripts/leaflet/images"), fileManager, cContext,
                getClass());
        // Copy Styles files and related resources
        getOperationUtils().updateDirectoryContents("styles/leaflet/*.css",
                pathResolver.getIdentifier(webappPath, "/styles/leaflet"),
                fileManager, cContext, getClass());
        getOperationUtils().updateDirectoryContents(
                "styles/leaflet/images/*.png",
                pathResolver
                        .getIdentifier(webappPath, "/styles/leaflet/images"),
                fileManager, cContext, getClass());
        // Copy necessary fonts
        getOperationUtils().updateDirectoryContents("styles/fonts/*.*",
                pathResolver.getIdentifier(webappPath, "/styles/fonts"),
                fileManager, cContext, getClass());
        // Copy images into images folder
        getOperationUtils().updateDirectoryContents("images/*.*",
                pathResolver.getIdentifier(webappPath, "/images"), fileManager,
                cContext, getClass());
        // Copy tags into tags folder
        getOperationUtils().updateDirectoryContents("tags/geo/*.tagx",
                pathResolver.getIdentifier(webappPath, "/WEB-INF/tags/geo"),
                fileManager, cContext, getClass());
        getOperationUtils().updateDirectoryContents(
                "tags/geo/layers/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/geo/layers"), fileManager, cContext,
                getClass());
        getOperationUtils().updateDirectoryContents(
                "tags/geo/tools/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/geo/tools"), fileManager, cContext,
                getClass());
        getOperationUtils().updateDirectoryContents(
                "tags/geo/form/fields/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/geo/form/fields"), fileManager,
                cContext, getClass());
        // Copy necessary layouts files
        getOperationUtils().updateDirectoryContents("layouts/*.jspx",
                pathResolver.getIdentifier(webappPath, "/WEB-INF/layouts"),
                fileManager, cContext, getClass());
        getOperationUtils().updateDirectoryContents("layouts/*.xml",
                pathResolver.getIdentifier(webappPath, "/WEB-INF/layouts"),
                fileManager, cContext, getClass());

        // Add JS sources to loadScripts
        if (getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-bootstrap")) {
            addToLoadScripts("js_leaflet_geo_js",
                    "/resources/scripts/leaflet/leaflet_bootstrap.js", false);
        }
        else {
            addToLoadScripts("js_leaflet_geo_js",
                    "/resources/scripts/leaflet/leaflet.js", false);
        }

        addToLoadScripts("js_leaflet_ext_gvnix_url",
                "/resources/scripts/leaflet/leaflet.ext.gvnix.map.js", false);
        addToLoadScripts(
                "js_leaflet_ext_gvnix_measure_tool_url",
                "/resources/scripts/leaflet/leaflet.ext.gvnix.map.measure.tool.js",
                false);
        addToLoadScripts(
                "js_leaflet_ext_gvnix_generic_tool_url",
                "/resources/scripts/leaflet/leaflet.ext.gvnix.map.generic.tool.js",
                false);
        addToLoadScripts("js_leaflet_geo_omnivore_js",
                "/resources/scripts/leaflet/leaflet-omnivore.min.js", false);
        addToLoadScripts("js_leaflet_geo_awesome_markers_js",
                "/resources/scripts/leaflet/leaflet.awesome-markers.min.js",
                false);
        addToLoadScripts("js_leaflet_geo_marker_cluster_js",
                "/resources/scripts/leaflet/leaflet.markercluster-src.js",
                false);
        addToLoadScripts("js_leaflet_zoom_slider_js",
                "/resources/scripts/leaflet/L.Control.Zoomslider.js", false);
        addToLoadScripts("js_leaflet_measuring_tool_js",
                "/resources/scripts/leaflet/L.MeasuringTool.js", false);
        addToLoadScripts("js_leaflet_html_layers_control",
                "/resources/scripts/leaflet/leaflet.htmllayercontrol.js", false);
        addToLoadScripts("js_leaflet_html_toolbar_control",
                "/resources/scripts/leaflet/leaflet.htmltoolbarcontrol.js",
                false);
        addToLoadScripts("js_leaflet_draw",
                "/resources/scripts/leaflet/leaflet.draw-src.js", false);

        // Add CSS Sources to Load Scripts
        if (getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-bootstrap")) {
            addToLoadScripts("styles_leaflet_geo_css",
                    "/resources/styles/leaflet/leaflet.bootstrap.css", true);
            addToLoadScripts("styles_gvnix_leaflet_geo_css",
                    "/resources/styles/leaflet/gvnix.leaflet.bootstrap.css",
                    true);
        }
        else {
            addToLoadScripts("styles_leaflet_geo_css",
                    "/resources/styles/leaflet/leaflet.css", true);
            addToLoadScripts("styles_gvnix_leaflet_geo_css",
                    "/resources/styles/leaflet/gvnix.leaflet.css", true);
        }
        addToLoadScripts("styles_leaflet_font_css",
                "/resources/styles/leaflet/font-awesome.min.css", true);
        addToLoadScripts("styles_leaflet_markers_css",
                "/resources/styles/leaflet/leaflet.awesome-markers.css", true);
        addToLoadScripts("styles_marker_cluster_css",
                "/resources/styles/leaflet/MarkerCluster.css", true);
        addToLoadScripts("styles_marker_cluster_default_css",
                "/resources/styles/leaflet/MarkerCluster.Default.css", true);
        addToLoadScripts("styles_zoom_slider_css",
                "/resources/styles/leaflet/L.Control.Zoomslider.css", true);
        addToLoadScripts("styles_leaflet_html_layers_control",
                "/resources/styles/leaflet/leaflet.htmllayercontrol.css", true);
        addToLoadScripts("styles_leaflet_html_toolbar_control",
                "/resources/styles/leaflet/leaflet.htmltoolbarcontrol.css",
                true);
        addToLoadScripts("styles_leaflet_drawl",
                "/resources/styles/leaflet/leaflet.draw.css", true);
    }

    /**
     * This method adds reference in laod-script.tagx to use
     * jquery.loupeField.ext.gvnix.js
     */
    public void addToLoadScripts(String varName, String url, boolean isCSS) {
        // Modify Roo load-scripts.tagx
        String docTagxPath = getPathResolver().getIdentifier(getWebappPath(),
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
            modified = getWebProjectUtils().addCssToTag(docTagx, root, varName,
                    url)
                    || modified;
        }
        else {
            modified = getWebProjectUtils().addJSToTag(docTagx, root, varName,
                    url)
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
        Set<I18n> supportedLanguages = getI18nSupport().getSupportedLanguages();
        for (I18n i18n : supportedLanguages) {
            if (i18n.getLocale().equals(new Locale("ca"))) {
                getMessageBundleUtils().installI18nMessages(
                        new ValencianCatalanLanguage(), getProjectOperations(),
                        fileManager);
                getMessageBundleUtils().addPropertiesToMessageBundle("ca",
                        getClass(), getPropFileOperations(),
                        getProjectOperations(), fileManager);
                break;
            }
        }
        // Add properties to Spanish messageBundle
        getMessageBundleUtils().installI18nMessages(new SpanishLanguage(),
                getProjectOperations(), fileManager);
        getMessageBundleUtils().addPropertiesToMessageBundle("es", getClass(),
                getPropFileOperations(), getProjectOperations(), fileManager);

        // Add properties to default messageBundle
        getMessageBundleUtils().addPropertiesToMessageBundle("en", getClass(),
                getPropFileOperations(), getProjectOperations(), fileManager);
    }

    /**
     * This method add necessary properties to messages.properties for
     * Controller
     */
    public void addI18nControllerProperties(String controllerPackage,
            String path) {

        Map<String, String> propertyList = new HashMap<String, String>();

        propertyList.put("menu_category_map_menu_category_label", "Maps");

        propertyList.put(
                String.format("label_%s_%s",
                        controllerPackage.replaceAll("[.]", "_"), path),
                "Entity Map Viewer");
        propertyList.put(
                String.format("label_%s_%s_toc",
                        controllerPackage.replaceAll("[.]", "_"), path),
                "Layers");

        getPropFileOperations().addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

    }

    /**
     * This method annotate MapControllers with entities to represent
     * 
     * @param mapControllersToAnnotate
     * @param typeLocationService
     * @param typeManagementService
     * @param entity
     */
    private void annotateMapController(JavaType mapController,
            TypeLocationService typeLocationService,
            TypeManagementService typeManagementService, JavaType controller) {

        ClassOrInterfaceTypeDetails mapControllerDetails = typeLocationService
                .getTypeDetails(mapController);

        // Getting @GvNIXMapViewer Annotation
        AnnotationMetadata mapViewerAnnotation = mapControllerDetails
                .getAnnotation(MAP_VIEWER_ANNOTATION);

        // Generating new annotation
        ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                mapControllerDetails);
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                MAP_VIEWER_ANNOTATION);

        // Getting current entities
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArrayAttributeValue<ClassAttributeValue> mapViewerAttributesOld = (ArrayAttributeValue) mapViewerAnnotation
                .getAttribute("entityLayers");

        // Initialize class attributes list for detail fields
        final List<ClassAttributeValue> entityAttributes = new ArrayList<ClassAttributeValue>();
        boolean notIncluded = true;

        if (mapViewerAttributesOld != null) {
            // Adding by default old entities
            entityAttributes.addAll(mapViewerAttributesOld.getValue());
            // Checking that current entity is not included yet
            List<ClassAttributeValue> mapViewerAttributesOldValues = mapViewerAttributesOld
                    .getValue();
            for (ClassAttributeValue currentEntity : mapViewerAttributesOldValues) {
                if (currentEntity.getValue().equals(controller)) {
                    notIncluded = false;
                    break;
                }
            }
        }

        // If current entity is not included in old values, include to new
        // annotation
        if (notIncluded) {
            // Create a class attribute for property
            final ClassAttributeValue entityAttribute = new ClassAttributeValue(
                    new JavaSymbolName(VALUE), controller);
            entityAttributes.add(entityAttribute);
        }

        // Create "entityLayers" attributes array from string attributes
        // list
        ArrayAttributeValue<ClassAttributeValue> entityLayersArray = new ArrayAttributeValue<ClassAttributeValue>(
                new JavaSymbolName("entityLayers"), entityAttributes);

        annotationBuilder.addAttribute(entityLayersArray);
        annotationBuilder.build();

        // Update annotation into controller
        classOrInterfaceTypeDetailsBuilder
                .updateTypeAnnotation(annotationBuilder);

        // Save controller changes to disk
        getTypeManagementService().createOrUpdateTypeOnDisk(
                classOrInterfaceTypeDetailsBuilder.build());

    }

    /**
     * Update the webmvc-config.xml file in order to register
     * Jackson2RequestMappingHandlerAdapter
     * 
     * @param targetPackage
     */
    private void updateWebMvcConfig() {
        LogicalPath webappPath = getWebProjectUtils().getWebappPath(
                getProjectOperations());
        String webMvcXmlPath = getProjectOperations().getPathResolver()
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

        Element dataBinder = XmlUtils.findFirstElement("bean[@id='"
                + WEBMCV_DATABINDER_BEAN_ID + "']", root);
        if (dataBinder != null) {
            root.removeChild(dataBinder);
        }

        // add bean tag to argument-resolvers
        Element bean = webMvcXml.createElement("bean");
        bean.setAttribute("id", WEBMCV_DATABINDER_BEAN_ID);
        bean.setAttribute("p:order", "1");
        bean.setAttribute("class", JACKSON2_RM_HANDLER_ADAPTER);

        Element property = webMvcXml.createElement("property");
        property.setAttribute("name", "objectMapper");

        Element objectMapperBean = webMvcXml.createElement("bean");
        objectMapperBean.setAttribute("class", OBJECT_MAPPER);
        property.appendChild(objectMapperBean);
        bean.appendChild(property);
        root.appendChild(bean);

        XmlUtils.writeXml(webMvcXmlMutableFile.getOutputStream(), webMvcXml);
    }

    /**
     * This method checks if exists some map element
     * 
     * @return
     */
    public boolean checkExistsMapElement() {
        // If not exists any class with @GvNIXMapViewer annotation, return false
        return !getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        MAP_VIEWER_ANNOTATION).isEmpty();
    }

    /**
     * This method returns position of new base layer
     * 
     * @param docRoot
     * @return
     */
    public int getNewBaseLayerPosition(Element docRoot) {
        NodeList tileLayers = docRoot.getElementsByTagName("layer:tile");
        NodeList wmsLayers = docRoot.getElementsByTagName("layer:wms");
        return tileLayers.getLength() + wmsLayers.getLength() + 1;
    }

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    public LogicalPath getWebappPath() {
        return getWebProjectUtils().getWebappPath(getProjectOperations());
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
        String dirPath = getPathResolver().getIdentifier(getWebappPath(),
                "scripts/leaflet/leaflet.js");
        return fileManager.exists(dirPath);
    }

    /**
     * This method updates geo addon to use Bootstrap components
     */
    @Override
    public void updateGeoAddonToBootstrap() {
        updateLoadScripts("js_leaflet_geo_js",
                "/resources/scripts/leaflet/leaflet_bootstrap.js", false);
        updateLoadScripts("styles_leaflet_geo_css",
                "/resources/styles/leaflet/leaflet.bootstrap.css", true);
        updateLoadScripts("styles_gvnix_leaflet_geo_css",
                "/resources/styles/leaflet/gvnix.leaflet.bootstrap.css", true);

        // Getting all maps and update layouts to use Bootstrap layout
        Set<ClassOrInterfaceTypeDetails> mapViews = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        new JavaType("org.gvnix.addon.geo.GvNIXMapViewer"));

        PathResolver pathResolver = getProjectOperations().getPathResolver();

        for (ClassOrInterfaceTypeDetails mapView : mapViews) {

            // Getting current map path
            AnnotationMetadata requestMapping = mapView
                    .getAnnotation(new JavaType(
                            "org.springframework.web.bind.annotation.RequestMapping"));

            String path = (String) requestMapping.getAttribute("value")
                    .getValue();

            path = path.substring(1);

            String finalPath = new JavaSymbolName(path).getReadableSymbolName()
                    .toLowerCase();

            // Modifying views.xml to add default-map
            final String viewsPath = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    String.format("WEB-INF/views/%s/views.xml", finalPath));

            // Checking if has default-map yet
            boolean isMapLayout = false;

            Document docXml = getWebProjectUtils().loadXmlDocument(viewsPath,
                    fileManager);

            // Getting current layout
            Element docRoot = docXml.getDocumentElement();

            Element definitionElement = (Element) docRoot.getElementsByTagName(
                    "definition").item(0);
            if (definitionElement != null) {
                String currentLayout = definitionElement
                        .getAttribute("extends");
                isMapLayout = "default-map".equals(currentLayout);
            }

            if (!isMapLayout) {
                // Copying views.xml
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "views/views.xml");
                    if (fileManager.exists(viewsPath)) {
                        outputStream = fileManager.updateFile(viewsPath)
                                .getOutputStream();
                    }
                    else {
                        outputStream = fileManager.createFile(viewsPath)
                                .getOutputStream();
                    }

                    // Doing this to solve problems with <!DOCTYPE element
                    // ////////////////////////////////////////////////
                    PrintWriter writer = new PrintWriter(outputStream);
                    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    writer.println("<!DOCTYPE tiles-definitions PUBLIC \"-//Apache Software Foundation//DTD Tiles Configuration 2.1//EN\" \"http://tiles.apache.org/dtds/tiles-config_2_1.dtd\">");
                    writer.println("<tiles-definitions>");

                    writer.println(String
                            .format("   <definition extends=\"default-map\" name=\"%s/show\">",
                                    finalPath));
                    writer.println(String
                            .format("      <put-attribute name=\"body\" value=\"/WEB-INF/views/%s/show.jspx\"/>",
                                    finalPath));
                    writer.println("   </definition>");
                    writer.println("</tiles-definitions>");

                    writer.flush();
                    writer.close();

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
        }
    }

    /**
     * This method adds reference in laod-script.tagx
     */
    public void updateLoadScripts(String varName, String url, boolean isCSS) {
        // Modify Roo load-scripts.tagx
        String docTagxPath = getPathResolver().getIdentifier(getWebappPath(),
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

        Element urlElement = XmlUtils.findFirstElement(
                "url[@var='".concat(varName) + "']", root);
        if (urlElement != null) {
            // Getting current url value
            String currentUrlValue = urlElement.getAttribute("value");
            if (!url.equals(currentUrlValue)) {
                urlElement.setAttribute("var", varName);
                urlElement.setAttribute(VALUE, url);
                modified = true;
            }
        }

        if (isCSS) {
            // Add link
            Element cssElement = XmlUtils.findFirstElement(
                    String.format("link[@href='${%s}']", varName), root);
            if (cssElement == null) {
                cssElement = docTagx.createElement("link");
                cssElement.setAttribute("rel", "stylesheet");
                cssElement.setAttribute("type", "text/css");
                cssElement.setAttribute("media", "screen");
                cssElement.setAttribute("href", "${".concat(varName)
                        .concat("}"));
                root.appendChild(cssElement);
                modified = true;
            }
        }
        else {
            // Add script
            Element scriptElement = XmlUtils.findFirstElement(
                    String.format("script[@src='${%s}']", varName), root);
            if (scriptElement == null) {
                scriptElement = docTagx.createElement("script");
                scriptElement.setAttribute("src",
                        "${".concat(varName).concat("}"));
                scriptElement.setAttribute("type", "text/javascript");
                scriptElement.appendChild(docTagx
                        .createComment("required for FF3 and Opera"));
                root.appendChild(scriptElement);
                modified = true;
            }
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
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
                LOGGER.warning("Cannot load PathResolver on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return pathResolver;
        }
    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
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
                LOGGER.warning("Cannot load TypeLocationService on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return typeLocationService;
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
                LOGGER.warning("Cannot load MetadataService on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public I18nSupport getI18nSupport() {
        if (i18nSupport == null) {
            // Get all Services implement I18nSupport interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(I18nSupport.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (I18nSupport) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load I18nSupport on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return i18nSupport;
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
                LOGGER.warning("Cannot load PropFileOperations on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return propFileOperations;
        }
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
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
                LOGGER.warning("Cannot load ProjectOperations on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
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
                LOGGER.warning("Cannot load MenuOperations on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return menuOperations;
        }
    }

    public TypeManagementService getTypeManagementService() {
        if (typeManagementService == null) {
            // Get all Services implement TypeManagementService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeManagementService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeManagementService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeManagementService on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
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
                LOGGER.warning("Cannot load WebProjectUtils on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return webProjectUtils;
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
                LOGGER.warning("Cannot load OperationUtils on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return operationUtils;
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
                LOGGER.warning("Cannot load MessageBundleUtils on GeoOperationsImpl.");
                return null;
            }
        }
        else {
            return messageBundleUtils;
        }

    }

}