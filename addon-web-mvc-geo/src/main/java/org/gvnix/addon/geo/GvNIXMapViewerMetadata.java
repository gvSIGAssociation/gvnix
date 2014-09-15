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
package org.gvnix.addon.geo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * ITD generator for {@link GvNIXGeoConversionService} annotation.
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public class GvNIXMapViewerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName UIMODEL_PARAM_NAME = new JavaSymbolName(
            "uiModel");

    private static final JavaSymbolName SHOW_MAP_METHOD = new JavaSymbolName(
            "showMap");

    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXMapViewerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public GvNIXMapViewerMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ProjectOperations projectOperations,
            TypeLocationService typeLocationService, FileManager fileManager,
            List<JavaType> entities, String path, String mapId) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Generating necessary methods

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding Converter methods
        String finalPath = path.replaceAll("/", "");
        builder.addMethod(getShowMapMethod(finalPath));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

        // Updating show.jspx with entities to display
        updateJSPXFiles(entities, finalPath, typeLocationService,
                projectOperations, fileManager, mapId);
    }

    /**
     * This method updates necessary JSPX files to visualize correctly entities
     * and his fields
     * 
     * @param entities
     * @param path
     * @param typeLocationService
     * @param fileManager
     * 
     */
    private void updateJSPXFiles(List<JavaType> entities, String path,
            TypeLocationService typeLocationService,
            ProjectOperations projectOperations, FileManager fileManager,
            String mapId) {

        PathResolver pathResolver = projectOperations.getPathResolver();
        // Getting jspx file path
        String finalPath = new JavaSymbolName(path).getReadableSymbolName()
                .toLowerCase();
        final String showPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                String.format("WEB-INF/views/%s/show.jspx", finalPath));

        if (fileManager.exists(showPath)) {
            // Getting document
            Document docXml = WebProjectUtils.loadXmlDocument(showPath,
                    fileManager);
            Element docRoot = docXml.getDocumentElement();

            // Getting mapElement
            Element mapElement = getOrCreateMapElement(docRoot, docXml, mapId);
            String zValue = mapElement.getAttribute("z");

            // If map is not set as user-managed gvNIX updates map content
            if (!zValue.equals("user-managed")) {

                // Manage and Update map
                updateMapElement(mapElement, zValue, mapId);

                // Getting toc element
                Element tocElement = getOrCreateTocElement(docRoot, docXml,
                        mapElement, mapId);
                String zTocValue = tocElement.getAttribute("z");

                // If toc is not set as user-managed gvNIX updates toc content
                if (!zTocValue.equals("user-managed")) {

                    // Manage and Update toc
                    updateTocElement(tocElement, zTocValue, mapId);

                    // Add or Update entity layers and entity fields
                    createOrUpdateEntityLayersAndFields(docXml, docRoot,
                            tocElement, entities, typeLocationService,
                            projectOperations);

                }

                // Get or create toolbarElement
                Element toolbarElement = getOrCreateToolbarElement(docRoot,
                        docXml, mapElement, mapId);
                String zToolbarValue = toolbarElement.getAttribute("z");

                // If is not setted as user-managed
                if (!zToolbarValue.equals("user-managed")) {
                    // Update toolbar element if necessary
                    updateToolbarElement(toolbarElement, zToolbarValue, mapId);
                }

                // Applying all changes
                fileManager.createOrUpdateTextFileIfRequired(showPath,
                        XmlUtils.nodeToString(docXml), true);

            }

        }
        else {
            throw new RuntimeException(
                    String.format(
                            "Error getting \"WEB-INF/views/%s/show.jspx\" file to manage entity layers",
                            finalPath));
        }

    }

    /**
     * Gets <code>showMap</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getShowMapMethod(String path) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(SpringJavaType.MODEL));
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest")));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SHOW_MAP_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        requestMappingMetadataBuilder.addEnumAttribute("method",
                SpringJavaType.REQUEST_METHOD, "GET");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "text/html");
        annotations.add(requestMappingMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(UIMODEL_PARAM_NAME);
        parameterNames.add(new JavaSymbolName("request"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildShowMapMethodBody(bodyBuilder, path);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, SHOW_MAP_METHOD, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>showMap</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildShowMapMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String path) {

        // return "path/show";
        bodyBuilder
                .appendFormalLine(String.format("return \"%s/show\";", path));
    }

    /**
     * 
     * This method returns valid path from received entity controller
     * 
     * @param entity
     * @param typeLocationService
     * @return
     */
    private String generatePathFromEntity(JavaType entity,
            TypeLocationService typeLocationService) {

        ClassOrInterfaceTypeDetails entityDetails = typeLocationService
                .getTypeDetails(entity);
        AnnotationMetadata requestMappingAnnotation = entityDetails
                .getAnnotation(SpringJavaType.REQUEST_MAPPING);
        Object entityPath = requestMappingAnnotation.getAttribute("value")
                .getValue();

        return entityPath.toString();
    }

    /**
     * 
     * This method returns valid id from received entity controller
     * 
     * @param entity
     * @param typeLocationService
     * @return
     */
    private String generateIdFromEntity(JavaType controller,
            TypeLocationService typeLocationService) {

        ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                .getTypeDetails(controller);
        AnnotationMetadata scaffoldAnnotation = controllerDetails
                .getAnnotation(new JavaType(
                        "org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold"));

        JavaType entity = (JavaType) scaffoldAnnotation.getAttribute(
                "formBackingObject").getValue();

        String entityPackage = entity.getPackage()
                .getFullyQualifiedPackageName();

        String entityName = entity.getSimpleTypeName();

        String mapId = String.format("l_%s_%s", entityPackage.replaceAll("[.]",
                "_"), new JavaSymbolName(entityName)
                .getSymbolNameCapitalisedFirstLetter());

        return mapId;
    }

    /**
     * This method obtains current entity PK
     * 
     * @param controller
     * @param typeLocationService
     * @return
     */
    private String generatePKFromEntity(JavaType controller,
            TypeLocationService typeLocationService) {

        ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                .getTypeDetails(controller);
        AnnotationMetadata scaffoldAnnotation = controllerDetails
                .getAnnotation(new JavaType(
                        "org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold"));

        JavaType entity = (JavaType) scaffoldAnnotation.getAttribute(
                "formBackingObject").getValue();

        // Getting entity details
        ClassOrInterfaceTypeDetails entityDetails = typeLocationService
                .getTypeDetails(entity);

        List<FieldMetadata> idField = entityDetails
                .getFieldsWithAnnotation(new JavaType("javax.persistence.Id"));

        // If main entity not have ID field, check if exists on extended class
        if (!idField.isEmpty()) {
            return idField.get(0).getFieldName().toString();
        }
        else {
            // TODO: CHECK IF IS CORRECT
            return "id";
        }
    }

    /**
     * This method obtains all GEO fields to represent on map from entity
     * 
     * @param controller
     * @param typeLocationService
     * @return
     */
    private List<FieldMetadata> getEntityGeoFields(JavaType controller,
            TypeLocationService typeLocationService) {

        ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                .getTypeDetails(controller);
        AnnotationMetadata scaffoldAnnotation = controllerDetails
                .getAnnotation(new JavaType(
                        "org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold"));

        JavaType entity = (JavaType) scaffoldAnnotation.getAttribute(
                "formBackingObject").getValue();

        // Getting entity details
        ClassOrInterfaceTypeDetails entityDetails = typeLocationService
                .getTypeDetails(entity);

        // Getting entity fields
        List<? extends FieldMetadata> entityFields = entityDetails
                .getDeclaredFields();

        // Generating empty field list
        List<FieldMetadata> geoFields = new ArrayList<FieldMetadata>();

        for (FieldMetadata field : entityFields) {
            JavaType fieldType = field.getFieldType();
            JavaPackage fieldPackage = fieldType.getPackage();

            // If is jts field, return as geo field
            if (fieldPackage.toString().equals("com.vividsolutions.jts.geom")) {
                geoFields.add(field);
            }

        }

        return geoFields;

    }

    /**
     * This method obtains map element from document or create new one if not
     * exists
     * 
     * 
     * @param docRoot
     * @param docXml
     * @return
     */
    private Element getOrCreateMapElement(Element docRoot, Document docXml,
            String mapId) {
        // Getting geo:map element
        NodeList mapElements = docRoot.getElementsByTagName("geo:map");
        Element mapElement = (Element) mapElements.item(0);

        // If map element not exists, generate new one
        if (mapElement == null) {
            mapElement = docXml.createElement("geo:map");
            mapElement.setAttribute("id", mapId);
            mapElement.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(mapElement));

            docRoot.appendChild(mapElement);

        }

        return mapElement;
    }

    /**
     * This method updates mapElement if necessary with correct configuration
     * 
     * 
     * @param mapElement
     * @param mapId
     */
    private void updateMapElement(Element mapElement, String zValue,
            String mapId) {

        // Getting correct z value
        String validZValue = XmlRoundTripUtils
                .calculateUniqueKeyFor((Element) mapElement);

        // Regenerating zValue if is blank
        if (StringUtils.isBlank(zValue)) {
            mapElement.setAttribute("z", validZValue);
        }

        // Regenerating id if is different
        if (!mapElement.getAttribute("id").equals(mapId)) {
            mapElement.setAttribute("id", mapId);
        }

    }

    /**
     * This method obtains toc element from document or create new one if not
     * exists and append to map parent
     * 
     * @param docRoot
     * @param docXml
     * @param mapId
     * @return
     */
    private Element getOrCreateTocElement(Element docRoot, Document docXml,
            Element mapElement, String mapId) {

        NodeList tocElements = docRoot.getElementsByTagName("geo:toc");
        Element tocElement = (Element) tocElements.item(0);

        // If toc element not exists, generate new one
        if (tocElement == null) {
            tocElement = docXml.createElement("geo:toc");
            tocElement.setAttribute("id", String.format("%s_toc", mapId));
            tocElement.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(tocElement));

            mapElement.appendChild(tocElement);
        }
        return tocElement;
    }

    /**
     * This method updates tocElement if necessary with correct configuration
     * 
     * 
     * @param mapElement
     * @param mapId
     */
    private void updateTocElement(Element tocElement, String zTocValue,
            String mapId) {

        String validZTocValue = XmlRoundTripUtils
                .calculateUniqueKeyFor((Element) tocElement);

        // Regenerating id if is different
        String validId = String.format("%s_toc", mapId);
        if (!tocElement.getAttribute("id").equals(validId)) {
            tocElement.setAttribute("id", validId);
        }

        // Regenerating zValue if is blank
        if (StringUtils.isBlank(zTocValue)) {
            tocElement.setAttribute("z", validZTocValue);
        }

    }

    /**
     * This method obtains toolbar element from document or create new one if
     * not exists and append to map parent
     * 
     * @param docRoot
     * @param docXml
     * @param mapElement
     * @param mapId
     * @return
     */
    private Element getOrCreateToolbarElement(Element docRoot, Document docXml,
            Element mapElement, String mapId) {
        NodeList toolbarElements = docRoot.getElementsByTagName("geo:toolbar");
        Element toolbarElement = (Element) toolbarElements.item(0);

        // If not exists, create new toolbar element
        if (toolbarElement == null) {
            toolbarElement = docXml.createElement("geo:toolbar");
            toolbarElement.setAttribute("id",
                    String.format("%s_toolbar", mapId));
            toolbarElement.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(toolbarElement));
            mapElement.appendChild(toolbarElement);
        }

        return toolbarElement;
    }

    /**
     * 
     * This method updates toolbarElement if necessary with correct
     * configuration
     * 
     * @param toolbarElement
     * @param zToolbarValue
     * @param mapId
     */
    private void updateToolbarElement(Element toolbarElement,
            String zToolbarValue, String mapId) {

        String idToolbarValue = toolbarElement.getAttribute("id");

        // Regenerating id if necessary
        if (!idToolbarValue.equals(String.format("%s_toolbar", mapId))) {
            toolbarElement.setAttribute("id",
                    String.format("%s_toolbar", mapId));
        }

        // Regenerating z value if necessary
        if (StringUtils.isBlank(zToolbarValue)) {
            toolbarElement.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(toolbarElement));
        }

    }

    /**
     * This method create or update entity layers and entity fields if necessary
     * 
     * @param docXml
     * @param docRoot
     * @param tocElement
     * @param entities
     * @param typeLocationService
     */
    private void createOrUpdateEntityLayersAndFields(Document docXml,
            Element docRoot, Element tocElement, List<JavaType> entities,
            TypeLocationService typeLocationService,
            ProjectOperations projectOperations) {

        // Check if datatable is installed to update filterType or not
        boolean isDatatableInstalled = projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-datatables");

        // Adding necessary entities
        // STEP 1: Getting id of entities without user-managed and
        // with user-managed
        // STEP 2: Add all entities with calculate z not in current
        // user-managed entities and update them if exists

        List<String> notUserManagedEntities = new ArrayList<String>();
        List<String> userManagedEntities = new ArrayList<String>();
        List<Integer> notUserManagedEntitiesPosition = new ArrayList<Integer>();

        // STEP 1
        NodeList entityLayersElements = docRoot
                .getElementsByTagName("layer:entity");
        for (int i = 0; i < entityLayersElements.getLength(); i++) {
            Element entityLayerElement = (Element) entityLayersElements.item(i);
            String zEntityLayerValue = entityLayerElement.getAttribute("z");
            String idEntityLayerValue = entityLayerElement.getAttribute("id");

            if (!zEntityLayerValue.equals("user-managed")) {
                notUserManagedEntities.add(idEntityLayerValue);
                notUserManagedEntitiesPosition.add(i);
            }
            else {
                userManagedEntities.add(idEntityLayerValue);
            }
        }

        // STEP 2
        for (JavaType entity : entities) {

            // Obtain entityFields
            List<FieldMetadata> entityGeoFields = getEntityGeoFields(entity,
                    typeLocationService);

            String entityId = generateIdFromEntity(entity, typeLocationService);

            if (userManagedEntities.indexOf(entityId) == -1) {

                // Calculate valid entityPath, entityPK and entityID
                String entityPath = generatePathFromEntity(entity,
                        typeLocationService);

                String entityPK = generatePKFromEntity(entity,
                        typeLocationService);

                // If exists as entity layer without user-managed,
                // update
                if (notUserManagedEntities.indexOf(entityId) != -1) {
                    Integer position = notUserManagedEntitiesPosition
                            .get(notUserManagedEntities.indexOf(entityId));
                    Element entityLayerToUpdate = (Element) entityLayersElements
                            .item(position);

                    String pathEntityLayerValue = entityLayerToUpdate
                            .getAttribute("path");
                    String pkEntityLayerValue = entityLayerToUpdate
                            .getAttribute("pk");
                    String filterTypeEntityLayerValue = entityLayerToUpdate
                            .getAttribute("filterType");
                    String zEntityLayerValue = entityLayerToUpdate
                            .getAttribute("z");

                    // If path is different update
                    if (!pathEntityLayerValue.equals(entityPath)) {
                        entityLayerToUpdate.setAttribute("path", entityPath);
                    }

                    // If PK is different update
                    if (!pkEntityLayerValue.equals(entityPK)) {
                        entityLayerToUpdate.setAttribute("pk", entityPK);
                    }

                    // Filter type must to be none if datatables are not
                    // installed
                    if (!isDatatableInstalled
                            && !filterTypeEntityLayerValue.equals("none")) {
                        entityLayerToUpdate.setAttribute("filterType", "none");
                    }

                    // If z is different update
                    String validEntityLayerZValue = XmlRoundTripUtils
                            .calculateUniqueKeyFor((Element) entityLayerToUpdate);
                    if (!zEntityLayerValue.equals(validEntityLayerZValue)) {
                        entityLayerToUpdate.setAttribute("z",
                                validEntityLayerZValue);
                    }

                    // Checking layer:entity-field details if
                    // necessary
                    NodeList entityFields = entityLayerToUpdate
                            .getElementsByTagName("layer:entity-field");
                    if (entityFields != null) {

                        for (FieldMetadata field : entityGeoFields) {

                            boolean exists = false;

                            String fieldType = field.getFieldType()
                                    .getSimpleTypeName();

                            String fieldName = field.getFieldName().toString();

                            String fieldId = String.format("%s_%s", entityId,
                                    fieldName);

                            for (int i = 0; i < entityFields.getLength(); i++) {
                                Element entityField = (Element) entityFields
                                        .item(i);
                                // Getting field value
                                String fieldEntityFieldValue = entityField
                                        .getAttribute("field");
                                // Getting id value
                                String idEntityFieldValue = entityField
                                        .getAttribute("id");
                                // Getting z value
                                String zEntityFieldValue = entityField
                                        .getAttribute("z");

                                if (idEntityFieldValue.equals(fieldId)
                                        || fieldEntityFieldValue
                                                .equals(fieldName)) {

                                    exists = true;

                                    // Checking if is user-managed
                                    if (!zEntityFieldValue
                                            .equals("user-managed")) {

                                        // Update field name with
                                        // correct
                                        if (!fieldEntityFieldValue
                                                .equals(fieldName)) {
                                            entityField.setAttribute("field",
                                                    fieldName);

                                        }

                                        // Adding icon if is POINT
                                        if (fieldType.equals("Point")) {
                                            String iconAttr = entityField
                                                    .getAttribute("icon");
                                            if (StringUtils.isBlank(iconAttr)) {
                                                entityField.setAttribute(
                                                        "icon",
                                                        "glyphicon-home");
                                                entityField.setAttribute(
                                                        "iconLibrary",
                                                        "glyphicon");
                                            }
                                        }

                                        // Update field id
                                        if (!idEntityFieldValue.equals(fieldId)) {
                                            entityField.setAttribute("id",
                                                    fieldId);
                                        }
                                        // Update z value
                                        String validZEntityFieldValue = XmlRoundTripUtils
                                                .calculateUniqueKeyFor(entityField);
                                        if (!zEntityFieldValue
                                                .equals(validZEntityFieldValue)) {
                                            entityField.setAttribute("z",
                                                    validZEntityFieldValue);
                                        }

                                    }
                                }
                            }

                            // If not exists, create new
                            // layer:entity-field
                            if (!exists) {
                                Element entityFieldLayer = docXml
                                        .createElement("layer:entity-field");
                                entityFieldLayer.setAttribute("field",
                                        fieldName);
                                entityFieldLayer.setAttribute("id", fieldId);
                                entityFieldLayer.setAttribute("markerColor",
                                        getRandomMarkerColor());
                                entityFieldLayer.setAttribute("iconColor",
                                        "white");

                                // Adding icon if is POINT
                                if (fieldType.equals("Point")) {
                                    entityFieldLayer.setAttribute("icon",
                                            "glyphicon-home");
                                    entityFieldLayer.setAttribute(
                                            "iconLibrary", "glyphicon");
                                }

                                entityFieldLayer
                                        .setAttribute(
                                                "z",
                                                XmlRoundTripUtils
                                                        .calculateUniqueKeyFor(entityFieldLayer));

                                // Append to entityLayer to update
                                entityLayerToUpdate
                                        .appendChild(entityFieldLayer);
                            }
                        }
                    }

                }
                else { // Create new entity layer
                    Element entityLayer = docXml.createElement("layer:entity");
                    entityLayer.setAttribute("id", entityId);
                    entityLayer.setAttribute("filterType",
                            isDatatableInstalled ? "auto" : "none");
                    entityLayer.setAttribute("path", entityPath);
                    entityLayer.setAttribute("pk", entityPK);
                    entityLayer.setAttribute("z", XmlRoundTripUtils
                            .calculateUniqueKeyFor(entityLayer));

                    // Create new entity-field layer
                    for (FieldMetadata entityField : entityGeoFields) {

                        String fieldName = entityField.getFieldName()
                                .toString();

                        String fieldId = String.format("%s_%s", entityId,
                                fieldName);

                        Element entityFieldLayer = docXml
                                .createElement("layer:entity-field");
                        entityFieldLayer.setAttribute("field", fieldName);
                        entityFieldLayer.setAttribute("id", fieldId);
                        entityFieldLayer.setAttribute("markerColor",
                                getRandomMarkerColor());
                        entityFieldLayer.setAttribute("iconColor", "white");
                        entityFieldLayer.setAttribute("z", XmlRoundTripUtils
                                .calculateUniqueKeyFor(entityFieldLayer));

                        // Apppend to layer:entity element
                        entityLayer.appendChild(entityFieldLayer);

                    }

                    // Adding to geo:toc element
                    tocElement.appendChild(entityLayer);
                }

            }
        }

    }

    /**
     * This method returns a random marker color to use
     * 
     * @return
     */
    public String getRandomMarkerColor() {
        String[] colors = { "red", "orange", "green", "blue", "purple" };
        int idx = new Random().nextInt(colors.length);
        String randomColor = (colors[idx]);

        return randomColor;
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }
}
