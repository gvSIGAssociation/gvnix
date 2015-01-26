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
package org.gvnix.addon.jpa.audit.providers.envers;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.audit.JpaAuditOperationsSPI;
import org.gvnix.addon.jpa.audit.JpaAuditRevisionEntityMetadata;
import org.gvnix.addon.jpa.audit.providers.RevisionLogMetadataBuilder;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProvider;
import org.gvnix.addon.jpa.audit.providers.RevisionLogRevisionEntityMetadataBuilder;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Revision log provider based on Hibernate envers module
 * <p/>
 * This provider is only available on project which uses Hibernate as JPA
 * implementation.
 * <p/>
 * On {@link #setup()} this module configures and install Hibernate envers an
 * generates a custom entity for revision log. This entity differs on standard
 * H-enver one in uses a long id and stores user name of every revision (if
 * Spring Security is available and there is a user logged).
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
@Component
@Service
public class EnversRevisionLogProvider implements RevisionLogProvider {

    private static final String STORE_DATA_AT_DELETE_PROP_NAME = "org.hibernate.envers.store_data_at_delete";

    @SuppressWarnings("unused")
    private static final Logger LOGGER = HandlerUtils
            .getLogger(EnversRevisionLogProvider.class);

    private static final Dependency HIBERNATE_DEPENDENCY = new Dependency(
            "org.hibernate", "hibernate-entitymanager", "4.3.6.Final");

    private static final Dependency HIBERNATE_ENVERS_DEPENDENCY = new Dependency(
            "org.hibernate", "hibernate-envers", "4.3.6.Final");

    private static final String HBER_PERS_PROV_CLS = "org.hibernate.jpa.HibernatePersistenceProvider";

    private static String PROVIDER_NAME = "H-ENVERS";
    private static String PROVIDER_DESCRIPTION = "Revision-log provider base on Hibernate envers module";

    private static final String PERSISTENCE_XML_LOCATION = "META-INF/persistence.xml";

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private FileManager fileManager;

    @Reference
    private MetadataService metadataService;

    /**
     * {@inheritDoc} Checks Hibernate dependency to show it as available
     */
    @Override
    public boolean isAvailable() {
        ProjectMetadata projectMetadata = getProjectMetadata();
        // XXX check persistence.xml ???
        return !getHibernateDependency(projectMetadata).isEmpty();
    }

    /**
     * @return
     */
    private ProjectMetadata getProjectMetadata() {
        return projectOperations.getFocusedProjectMetadata();
    }

    /**
     * Gets Hibernate dependencies declared on current project (excluding
     * version)
     * 
     * @param metadata of current project
     * @return
     */
    private Set<Dependency> getHibernateDependency(ProjectMetadata metadata) {
        return metadata.getPom().getDependenciesExcludingVersion(
                HIBERNATE_DEPENDENCY);
    }

    /**
     * Checks if Hibernate is declared as dependency on current project
     * (excluding version)
     * 
     * @param metadata of current project
     * @return
     */
    private boolean isHibernateEnversInstalled(ProjectMetadata metadata) {
        return metadata.getPom().hasDependencyExcludingVersion(
                HIBERNATE_ENVERS_DEPENDENCY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        ProjectMetadata projectMetadata = getProjectMetadata();
        if (!isHibernateEnversInstalled(projectMetadata)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return PROVIDER_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDefaultValueOfRevisionLogAttribute() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This do this operations:
     * <ul>
     * <li>Install Hibernate evenrs depencency (uses the same hibernate-core
     * version)</li>
     * </ul>
     * 
     */
    @Override
    public void setup(JpaAuditOperationsSPI operations) {

        // Installs envers dependency on pom.xml
        installEnversDependency();

        // Configure presistence.xml
        configurePersistenceXML();

        // Install revision entity class on project
        operations.installRevisonEntity(null);

        // Refresh all audited entity metadata
        operations.refreshAuditedEntities();

    }

    /**
     * Configure presistence.xml project file
     */
    private void configurePersistenceXML() {
        PathResolver pathResolver = projectOperations.getPathResolver();

        String persistencePath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                PERSISTENCE_XML_LOCATION);
        if (!fileManager.exists(persistencePath)) {
            throw new IllegalStateException(
                    "persistence.xml not found: ".concat(persistencePath));
        }

        // Load xml file
        MutableFile persistenceMutableFile = null;
        Document persistenceXml;
        try {
            // Get orm.xml content and parse it
            persistenceMutableFile = fileManager.updateFile(persistencePath);
            persistenceXml = XmlUtils.getDocumentBuilder().parse(
                    persistenceMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException("Error loading file '".concat(
                    persistencePath).concat("'"), e);
        }

        // Search for provider to assure is hibernate
        checkPersistenceProvider(persistencePath, persistenceXml);

        // Search for properties element
        Element propertiesElement = XmlUtils.findFirstElement(
                "/persistence/persistence-unit/properties", persistenceXml);

        // Check if 'org.hibernate.envers.store_data_at_delete' exists
        Element storeDataAtDeletePropElement = XmlUtils.findFirstElement(
                "/persistence/persistence-unit/properties/property[@name='"
                        .concat(STORE_DATA_AT_DELETE_PROP_NAME).concat("']"),
                persistenceXml);

        if (storeDataAtDeletePropElement != null) {
            // Property already exists: nothing to do here
            return;
        }

        // Create property element and appent to properties tag
        storeDataAtDeletePropElement = persistenceXml.createElement("property");
        storeDataAtDeletePropElement.setAttribute("name",
                STORE_DATA_AT_DELETE_PROP_NAME);
        storeDataAtDeletePropElement.setAttribute("value", "true");
        propertiesElement.appendChild(storeDataAtDeletePropElement);

        // write persistence.xml
        XmlUtils.writeXml(persistenceMutableFile.getOutputStream(),
                persistenceXml);
    }

    /**
     * Check persistence.xml registered provider to assure it's hibernate
     * 
     * @param persistencePath
     * @param persistenceXml
     */
    private void checkPersistenceProvider(String persistencePath,
            Document persistenceXml) {
        Element providerElement = XmlUtils.findFirstElement(
                "/persistence/persistence-unit/provider", persistenceXml);
        if (providerElement == null) {
            throw new IllegalStateException("Error loading file '".concat(
                    persistencePath).concat(
                    "': /persistence/persistence-unit/provider tag not found"));
        }
        String provider = providerElement.getTextContent();
        if (StringUtils.isBlank(provider)) {
            throw new IllegalStateException("Error loading file '".concat(
                    persistencePath).concat(
                    "': /persistence/persistence-unit/provider tag is empty"));
        }
        provider = provider.trim();
        if (!HBER_PERS_PROV_CLS.equals(provider)) {
            throw new IllegalStateException(
                    String.format(
                            "Error loading file '%s': unexpected /persistence/persistence-unit/provider (expected: '%s' found: '%s')",
                            persistencePath, HBER_PERS_PROV_CLS, provider));
        }
    }

    /**
     * Install envers dependency on project
     */
    private void installEnversDependency() {
        // Locate hiberante dependency
        ProjectMetadata projectMetadata = getProjectMetadata();
        Set<Dependency> hibernateDependencies = getHibernateDependency(projectMetadata);
        if (hibernateDependencies.isEmpty()) {
            throw new IllegalStateException("No Hibernate dependency found");
        }

        if (hibernateDependencies.size() > 1) {
            throw new IllegalStateException(
                    "Error on Hibernate dependency: > 1 found for "
                            .concat(HIBERNATE_DEPENDENCY.getSimpleDescription()));
        }

        // Gets hibernate-entityManager version
        String hibernateVersion = hibernateDependencies.iterator().next()
                .getVersion();

        // Install hibernate envers dependency using hibernate-entityManager
        // version
        Dependency enversDependency = new Dependency(
                HIBERNATE_ENVERS_DEPENDENCY.getGroupId(),
                HIBERNATE_ENVERS_DEPENDENCY.getArtifactId(), hibernateVersion);
        projectOperations.addDependency(
                projectOperations.getFocusedModuleName(), enversDependency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionLogMetadataBuilder getMetadataBuilder(
            JpaAuditOperationsSPI operations,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {

        String revisionEntityMetadatId = JpaAuditRevisionEntityMetadata
                .createIdentifier(operations.getRevisionEntityJavaType(),
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));
        JpaAuditRevisionEntityMetadata revisionEntityMetada = (JpaAuditRevisionEntityMetadata) metadataService
                .get(revisionEntityMetadatId);
        if (revisionEntityMetada == null) {
            throw new IllegalStateException(
                    "Can't get RevisionEntityLog metadata: "
                            .concat(revisionEntityMetadatId));
        }
        return new EnversRevisionLogMetadataBuilder(
                governorPhysicalTypeMetadata, revisionEntityMetada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionLogRevisionEntityMetadataBuilder getRevisonEntityMetadataBuilder(
            JpaAuditOperationsSPI operations,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        return new EnversRevisionLogEntityMetadataBuilder(
                governorPhysicalTypeMetadata);
    }
}
