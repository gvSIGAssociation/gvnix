package org.gvnix.addon.geo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of GEO Addon operations
 * 
 * @since 1.1
 */
@Component
@Service
public class GeoOperationsImpl implements GeoOperations {

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    private static final Logger LOGGER = Logger
            .getLogger(GeoOperationsImpl.class.getName());

    /**
     * If HIBERNATE is setted as persistence provider, the command will be
     * available
     */
    public boolean isSetupCommandAvailable() {
        return GeoUtils.isHibernateProviderPersistence(fileManager,
                pathResolver);
    }

    /** {@inheritDoc} */
    public void setup() {
        // Adding hibernate-spatial dependencies
        addHibernateSpatialDependencies();
        // Updating Persistence dialect
        updatePersistenceDialect();
    }

    /**
     * This method adds hibernate spatial dependencies and repositories to
     * pom.xml
     */
    public void addHibernateSpatialDependencies() {
        // Parse the configuration.xml file
        final Element configuration = XmlUtils.getConfiguration(getClass());
        // Add POM Repositories
        GeoUtils.updateRepositories(configuration,
                "/configuration/repositories/repository", projectOperations);
        // Add POM dependencies
        GeoUtils.updateDependencies(configuration,
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
            int totalPersistenceUnits = 0;
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
                                    // Transform current Dialect to valid
                                    // GEO dialect depens of the selected
                                    // Database
                                    String geoDialect = GeoUtils
                                            .convertToGeoDialect(propertyValue
                                                    .getNodeValue());
                                    // If geo Dialect exists, modify value with
                                    // the
                                    // valid GEO dialect
                                    if (geoDialect != null) {
                                        propertyValue.setNodeValue(geoDialect);
                                        totalModified++;
                                    }
                                    totalPersistenceUnits++;
                                }
                            }
                        }
                    }
                }
            }

            fileManager.createOrUpdateTextFileIfRequired(persistenceFile,
                    XmlUtils.nodeToString(persistenceXmlDocument), false);

            // Showing WARNING informing that if you install a different
            // persistence, you must to execute this
            // command again
            LOGGER.log(
                    Level.INFO,
                    "WARNING: If you install a new persistence, you must to execute 'geo setup' again to modify Persistence Dialects.");
        }
        else {
            LOGGER.log(Level.INFO, "Error getting persistence.xml file");
        }
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
        return FEATURE_NAME_GVNIX_GEO;
    }

    /**
     * Returns true if GEO is installed
     */
    @Override
    public boolean isInstalledInModule(String moduleName) {
        return true;
    }

}