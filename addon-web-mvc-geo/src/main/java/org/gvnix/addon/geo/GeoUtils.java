package org.gvnix.addon.geo;

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
import org.w3c.dom.NodeList;

public class GeoUtils {

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
    public static String convertToGeoDialect(String currentDialect) {
        Map<String, String> dialects = getDialects();
        String geoDialect = dialects.get(currentDialect);
        if (geoDialect != null) {
            return geoDialect;
        }
        return null;
    }

    /**
     * This method returns a Map with Dialects and the GEO dialects associated
     * 
     * @return
     */
    public static Map<String, String> getDialects() {
        Map<String, String> dialects = new HashMap<String, String>();
        dialects.put("org.hibernate.dialect.PostgreSQLDialect",
                "org.hibernate.spatial.dialect.postgis.PostgisDialect");
        dialects.put("org.hibernate.dialect.MySQLDialect",
                "org.hibernate.spatial.dialect.mysql.MySQLSpatialDialect");
        dialects.put("org.hibernate.dialect.MySQL5Dialect",
                "org.hibernate.spatial.dialect.mysql.MySQLSpatialDialect");
        dialects.put("org.hibernate.dialect.MySQLInnoDBDialect",
                "org.hibernate.spatial.dialect.mysql.MySQLSpatialInnoDBDialect");
        dialects.put("org.hibernate.dialect.MySQL5InnoDBDialect",
                "org.hibernate.spatial.dialect.mysql.MySQLSpatialInnoDBDialect");
        dialects.put("org.hibernate.dialect.MySQL5DBDialect",
                "org.hibernate.spatial.dialect.mysql.MySQLSpatial56Dialect");
        dialects.put("org.hibernate.dialect.MySQLSpatial56Dialect",
                "org.hibernate.spatial.dialect.mysql.MySQLSpatial5InnoDBDialect");
        dialects.put("org.hibernate.dialect.Oracle10gDialect",
                "org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect");
        dialects.put("org.hibernate.dialect.OracleDialect",
                "org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect");
        return dialects;
    }
}
