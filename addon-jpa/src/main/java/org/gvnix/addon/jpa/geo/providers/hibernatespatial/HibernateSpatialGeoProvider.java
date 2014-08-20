package org.gvnix.addon.jpa.geo.providers.hibernatespatial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.geo.FieldGeoTypes;
import org.gvnix.addon.jpa.geo.providers.GeoProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
@Service
public class HibernateSpatialGeoProvider implements GeoProvider {

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private ProjectOperations projectOperations;

    /**
     * DECLARING CONSTANTS
     */

    public static final String NAME = "HIBERNATE_SPATIAL";

    public static final String DESCRIPTION = "Use HibernateSpatial in your project";

    private static final Logger LOGGER = Logger
            .getLogger(HibernateSpatialGeoProvider.class.getName());

    private static final JavaType HIBERNATE_TYPE_ANNOTATION = new JavaType(
            "org.hibernate.annotations.Type");

    /**
     * If HIBERNATE is setted as persistence provider, the command will be
     * available
     */
    @Override
    public boolean isAvailablePersistence(FileManager fileManager,
            PathResolver pathResolver) {
        return HibernateSpatialGeoUtils.isHibernateProviderPersistence(
                fileManager, pathResolver);
    }

    /**
     * This method checks if field geo is available to execute
     */
    @Override
    public boolean isGeoPersistenceInstalled(FileManager fileManager,
            PathResolver pathResolver) {
        return HibernateSpatialGeoUtils.isHibernateSpatialPersistenceInstalled(
                fileManager, pathResolver, getClass());
    }

    /**
     * This method configure your project to works using hibernate spatial
     */
    @Override
    public void setup() {
        // Updating Persistence dialect
        updatePersistenceDialect();
        // Adding hibernate-spatial dependencies
        addHibernateSpatialDependencies();
    }

    /**
     * This method adds new file to the specified Entity.
     * 
     * @param JavaSymbolName
     * @param fieldGeoTypes
     * @param entity
     * 
     */
    @Override
    public void addField(JavaSymbolName fieldName, FieldGeoTypes fieldGeoType,
            JavaType entity) {
        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(entity);
        final String physicalTypeIdentifier = cid.getDeclaredByMetadataId();

        // Getting fieldType and fieldDetails
        JavaType fieldType = new JavaType(fieldGeoType.toString());
        FieldDetails fieldDetails = new FieldDetails(physicalTypeIdentifier,
                fieldType, fieldName);

        // Checking not reserved words on fieldName
        ReservedWords
                .verifyReservedWordsNotPresent(fieldDetails.getFieldName());

        // Adding Annotation @Type
        List<AnnotationMetadataBuilder> fieldAnnotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder typeAnnotation = new AnnotationMetadataBuilder(
                HIBERNATE_TYPE_ANNOTATION);
        typeAnnotation.addStringAttribute("type",
                "org.hibernate.spatial.GeometryType");
        fieldAnnotations.add(typeAnnotation);
        fieldDetails.setAnnotations(fieldAnnotations);

        // Adding Modifier
        fieldDetails.setModifiers(Modifier.PRIVATE);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                fieldDetails);

        // Adding field to entity
        typeManagementService.addField(fieldBuilder.build());

    }

    /**
     * This method adds hibernate spatial dependencies and repositories to
     * pom.xml
     */
    public void addHibernateSpatialDependencies() {
        // Parse the configuration.xml file
        final Element configuration = XmlUtils.getConfiguration(getClass());
        // Add POM Repositories
        HibernateSpatialGeoUtils.updateRepositories(configuration,
                "/configuration/repositories/repository", projectOperations);
        // Add POM dependencies
        HibernateSpatialGeoUtils.updateDependencies(configuration,
                "/configuration/dependencies/dependency", projectOperations);
    }

    /**
     * This method update Pesistence Dialect to use the required one to the
     * selected database.
     * 
     */
    public void updatePersistenceDialect() {
        // persistence.xml file
        final String persistenceFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");
        // if persistence.xml doesn't exists show a WARNING
        if (fileManager.exists(persistenceFile)) {
            // Getting document
            final Document persistenceXmlDocument = XmlUtils
                    .readXml(fileManager.getInputStream(persistenceFile));
            final Element persistenceElement = persistenceXmlDocument
                    .getDocumentElement();
            // Getting all persistence-unit
            NodeList nodes = persistenceElement.getChildNodes();
            // Saving in variables, which nodes could be changed
            int totalModified = 0;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node persistenceUnit = nodes.item(i);
                // Get all items of current persistence-unit
                NodeList childNodes = persistenceUnit.getChildNodes();
                for (int x = 0; x < childNodes.getLength(); x++) {
                    Node childNode = childNodes.item(x);
                    String nodeName = childNode.getNodeName();
                    if ("properties".equals(nodeName)) {
                        // Getting all properties
                        NodeList properties = childNode.getChildNodes();
                        for (int y = 0; y < properties.getLength(); y++) {
                            Node property = properties.item(y);
                            // Getting attribute properties
                            NamedNodeMap attributes = property.getAttributes();
                            if (attributes != null) {
                                Node propertyName = attributes
                                        .getNamedItem("name");
                                String propertyNameValue = propertyName
                                        .getNodeValue();
                                if ("hibernate.dialect"
                                        .equals(propertyNameValue)) {
                                    Node propertyValue = attributes
                                            .getNamedItem("value");

                                    String value = propertyValue.getNodeValue();
                                    final Element configuration = XmlUtils
                                            .getConfiguration(getClass());
                                    if (!HibernateSpatialGeoUtils.isGeoDialect(
                                            configuration, value)) {
                                        // Transform current Dialect to valid
                                        // GEO dialect depens of the selected
                                        // Database
                                        // Parse the configuration.xml file
                                        String geoDialect = HibernateSpatialGeoUtils
                                                .convertToGeoDialect(
                                                        configuration, value);
                                        // If geo Dialect exists, modify value
                                        // with
                                        // the
                                        // valid GEO dialect
                                        if (geoDialect != null) {
                                            propertyValue
                                                    .setNodeValue(geoDialect);
                                            totalModified++;
                                        }
                                    }
                                    else {
                                        // If is geo dialect, mark as modified
                                        totalModified++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (totalModified != 0) {
                fileManager.createOrUpdateTextFileIfRequired(persistenceFile,
                        XmlUtils.nodeToString(persistenceXmlDocument), false);

                // Showing WARNING informing that if you install a different
                // persistence, you must to execute this
                // command again
                LOGGER.log(
                        Level.INFO,
                        "WARNING: If you install a new persistence, you must to execute 'jpa geo setup' again to modify Persistence Dialects.");
            }
            else {
                throw new RuntimeException(
                        "ERROR: There's not any valid database to apply GEO persistence support. GEO is only supported for POSTGRES, ORACLE, MYSQL and MSSQL databases.");
            }

        }
        else {
            throw new RuntimeException(
                    "ERROR: Error getting persistence.xml file");
        }
    }

    /**
     * This method creates types.xml file into src/main/resources/*
     * 
     * TODO: Improve <!ENTITY declaration on DOCTYPE
     * 
     * @param entity
     */
    private void addTypesXmlFile(JavaType entity) {
        // Getting current entity package
        String entityPackage = entity.getPackage()
                .getFullyQualifiedPackageName();
        String entityPackageFolder = entityPackage.replaceAll("[.]", "/");

        // Setting types.xml location using entity package
        final String typesXmlPath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES,
                String.format("/%s/types.xml", entityPackageFolder));

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(), "types.xml");
            if (!fileManager.exists(typesXmlPath)) {
                outputStream = fileManager.createFile(typesXmlPath)
                        .getOutputStream();
            }
            if (outputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            if (outputStream != null) {
                IOUtils.closeQuietly(outputStream);
            }

        }
        // Modifying created file
        if (outputStream != null) {
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(" <!DOCTYPE hibernate-mapping PUBLIC");
            writer.println("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"");
            writer.println("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\" [");
            writer.println(String.format(
                    "<!ENTITY types SYSTEM \"classpath://%s/types.xml\">",
                    entityPackageFolder));
            writer.println("]>");
            writer.println("");
            writer.println(String.format("<hibernate-mapping package=\"%s\">",
                    entityPackage));
            writer.println("<typedef name=\"geometry\" class=\"org.hibernate.spatial.GeometryType\"/>");
            writer.println("</hibernate-mapping>");
            writer.flush();
            writer.close();

        }
    }

    /**
     * PROVIDER CONFIGURATION METHODS
     */

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

}
