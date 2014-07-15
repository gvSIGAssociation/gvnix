package org.gvnix.addon.jpa.geo;

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
public class JpaGeoOperationsImpl implements JpaGeoOperations {

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
            .getLogger(JpaGeoOperationsImpl.class.getName());

    /**
     * If HIBERNATE is setted as persistence provider, the command will be
     * available
     */
    public boolean isSetupCommandAvailable() {
        return JpaGeoUtils.isHibernateProviderPersistence(fileManager,
                pathResolver);
    }

    /** {@inheritDoc} */
    public void setup() {
        // Updating Persistence dialect
        updatePersistenceDialect();
        // Adding hibernate-spatial dependencies
        addHibernateSpatialDependencies();
    }

    /**
     * This method adds hibernate spatial dependencies and repositories to
     * pom.xml
     */
    public void addHibernateSpatialDependencies() {
        // Parse the configuration.xml file
        final Element configuration = XmlUtils.getConfiguration(getClass());
        // Add POM Repositories
        JpaGeoUtils.updateRepositories(configuration,
                "/configuration/repositories/repository", projectOperations);
        // Add POM dependencies
        JpaGeoUtils.updateDependencies(configuration,
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
                                    if (!JpaGeoUtils.isGeoDialect(
                                            configuration, value)) {
                                        // Transform current Dialect to valid
                                        // GEO dialect depens of the selected
                                        // Database
                                        // Parse the configuration.xml file
                                        String geoDialect = JpaGeoUtils
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