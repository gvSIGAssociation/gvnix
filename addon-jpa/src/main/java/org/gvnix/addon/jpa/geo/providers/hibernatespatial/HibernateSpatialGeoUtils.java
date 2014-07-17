package org.gvnix.addon.jpa.geo.providers.hibernatespatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HibernateSpatialGeoUtils {

    /**
     * This method checks if persistence is setup and if HIBERNATE was selected
     * as provider persistence
     * 
     * @param fileManager
     * @param pathResolver
     * @return
     */
    public static boolean isHibernateProviderPersistence(
            FileManager fileManager, PathResolver pathResolver) {
        // persistence.xml file
        final String persistenceFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");
        // if persistence.xml doesn't exists, return false
        if (fileManager.exists(persistenceFile)) {
            // Getting document
            final Document persistenceXmlDocument = XmlUtils
                    .readXml(fileManager.getInputStream(persistenceFile));
            final Element persistenceElement = persistenceXmlDocument
                    .getDocumentElement();
            // Getting provider tag
            final Element provider = DomUtils.findFirstElementByName(
                    "provider", persistenceElement);
            // Checking if provider is not null
            Validate.notNull(provider, "Could not find provider in %s",
                    persistenceElement);
            NodeList node = provider.getChildNodes();
            // Getting providerValue
            String providerValue = node.item(0).getNodeValue();
            // If provider selected is HIBERNATE returns true
            if (providerValue.equals("org.hibernate.ejb.HibernatePersistence")) {
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    /**
     * 
     * This method update repositories with the added to configuration.xml file
     * 
     * @param configuration
     * @param moduleName
     * @param projectOperations
     */
    public static void updateRepositories(final Element configuration,
            String path, ProjectOperations projectOperations) {
        final List<Repository> repositories = new ArrayList<Repository>();
        final List<Element> geoRepositories = XmlUtils.findElements(path,
                configuration);
        for (final Element repositoryElement : geoRepositories) {
            repositories.add(new Repository(repositoryElement));
        }
        projectOperations.addRepositories(
                projectOperations.getFocusedModuleName(), repositories);
    }

    /**
     * 
     * This method update dependencies with the added to configuration.xml file
     * 
     * @param configuration
     * @param moduleName
     * @param projectOperations
     */
    public static void updateDependencies(final Element configuration,
            String path, ProjectOperations projectOperations) {
        final List<Dependency> dependencies = new ArrayList<Dependency>();
        final List<Element> geoDependencies = XmlUtils.findElements(path,
                configuration);
        for (final Element dependencyElement : geoDependencies) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.addDependencies(
                projectOperations.getFocusedModuleName(), dependencies);
    }

    /**
     * This method transform current dialect to a valid dialect to use in
     * Hibernate Spatial
     * 
     * @param currentDialect
     * @return
     */
    public static String convertToGeoDialect(final Element configuration,
            String currentDialect) {
        Map<String, String> dialects = getDialects(configuration);
        String geoDialect = dialects.get(currentDialect);
        if (geoDialect != null) {
            return geoDialect;
        }
        return null;
    }

    /**
     * This method checks if the current dialect is a valid Geo dialect to use
     * in Hibernate Spatial
     * 
     * @param currentDialect
     * @return
     */
    public static boolean isGeoDialect(final Element configuration,
            String currentDialect) {
        Map<String, String> geoDialects = getGeoDialects(configuration);
        String geoDialect = geoDialects.get(currentDialect);
        if (geoDialect != null) {
            return true;
        }
        return false;
    }

    /**
     * This method returns a Map with Dialects and the GEO dialects associated
     * 
     * @return
     */
    public static Map<String, String> getDialects(final Element configuration) {
        Map<String, String> dialects = new HashMap<String, String>();

        final List<Element> geoDialects = XmlUtils.findElements(
                "/configuration/dialects/dialect", configuration);

        for (final Element dialectElement : geoDialects) {
            String id = dialectElement.getAttribute("id");
            String value = dialectElement.getAttribute("value");
            dialects.put(id, value);
        }
        return dialects;
    }

    /**
     * This method returns a Map with GeoDialects and the dialects associated
     * 
     * @return
     */
    public static Map<String, String> getGeoDialects(final Element configuration) {
        Map<String, String> dialects = new HashMap<String, String>();

        final List<Element> geoDialects = XmlUtils.findElements(
                "/configuration/dialects/dialect", configuration);

        for (final Element dialectElement : geoDialects) {
            String id = dialectElement.getAttribute("id");
            String value = dialectElement.getAttribute("value");
            dialects.put(value, id);
        }
        return dialects;
    }

    public static boolean isHibernateSpatialPersistenceInstalled(
            FileManager fileManager, PathResolver pathResolver,
            Class currentClass) {
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
                                            .getConfiguration(currentClass);
                                    if (HibernateSpatialGeoUtils.isGeoDialect(
                                            configuration, value)) {
                                        totalModified++;
                                    }

                                }
                            }
                        }
                    }
                }
            }

            if (totalModified != 0) {
                return true;
            }
            else {
                return false;
            }

        }
        else {
            throw new RuntimeException(
                    "ERROR: Error getting persistence.xml file");
        }
    }

}
