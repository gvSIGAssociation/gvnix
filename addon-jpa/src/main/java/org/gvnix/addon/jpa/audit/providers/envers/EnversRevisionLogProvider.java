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

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.audit.GvNIXJpaAudit;
import org.gvnix.addon.jpa.audit.GvNIXJpaAuditRevisionEntity;
import org.gvnix.addon.jpa.audit.JpaAuditMetadata;
import org.gvnix.addon.jpa.audit.JpaAuditRevisionEntityMetadata;
import org.gvnix.addon.jpa.audit.providers.RevisionLogMetadataBuilder;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProvider;
import org.gvnix.addon.jpa.audit.providers.RevisionLogRevisionEntityMetadataBuilder;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
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

    private static final String REVISION_LOG_ENTITY_NAME = "RevisionLogEntity";

    private static final String STORE_DATA_AT_DELETE_PROP_NAME = "org.hibernate.envers.store_data_at_delete";

    @SuppressWarnings("unused")
    private static final Logger LOGGER = HandlerUtils
            .getLogger(EnversRevisionLogProvider.class);

    private static final Dependency SPRING_SEC_DEPENDENCY = new Dependency(
            "org.springframework.security", "spring-security-core",
            "3.1.0.RELEASE");

    private static final Dependency HIBERNATE_DEPENDENCY = new Dependency(
            "org.hibernate", "hibernate-entitymanager", "4.2.2.Final");

    private static final Dependency HIBERNATE_ENVERS_DEPENDENCY = new Dependency(
            "org.hibernate", "hibernate-envers", "4.2.2.Final");

    private static final String HIBERNATE_PERSISTENCE_PROVIDER_CLASS = "org.hibernate.ejb.HibernatePersistence";

    private static String PROVIDER_NAME = "H-ENVERS";
    private static String PROVIDER_DESCRIPTION = "Revision-log provider base on Hibernate envers module";

    private static final JavaType REVISON_ENTITY_ANNOTATION = new JavaType(
            "org.hibernate.envers.RevisionEntity");

    private static final String PERSISTENCE_XML_LOCATION = "META-INF/persistence.xml";

    private static final JavaType GVNIX_REVION_ENTITY_ANNOTATION = new JavaType(
            GvNIXJpaAuditRevisionEntity.class);

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private FileManager fileManager;

    @Reference
    private MetadataService metadataService;

    private JavaType revisionEntityJavaType;

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

    public boolean isSpringSecInstalled() {
        return getProjectMetadata().getPom().hasDependencyExcludingVersion(
                SPRING_SEC_DEPENDENCY);
    }

    private Set<Dependency> getHibernateDependency(ProjectMetadata metadata) {
        // TODO check persistence.xml???
        return metadata.getPom().getDependenciesExcludingVersion(
                HIBERNATE_DEPENDENCY);
    }

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
        if (getHibernateDependency(projectMetadata).isEmpty()) {
            return false;
        }
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
    public void setup() {

        // Installs envers dependency on pom.xml
        installEnversDependency();

        // Configure presistence.xml
        configurePersistenceXML();

        // Install revision entity class on project
        installRevisonEntity();

        // Refresh all audited entity metadata
        refreshAuditedEntities();

    }

    /**
     * Performs a {@link MetadataService#evictAndGet(String)} of all entities
     * annotated with {@link GvNIXJpaAudit}. This regenerates related
     * <em>.aj</em> files.
     */
    public void refreshAuditedEntities() {
        // Use the TypeLocationService to scan project for all types with
        // jpaAudit
        // annotation
        String metadataId;
        PathResolver pathResolver = projectOperations.getPathResolver();
        LogicalPath path = pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA);
        for (JavaType entity : typeLocationService
                .findTypesWithAnnotation(new JavaType(GvNIXJpaAudit.class))) {
            metadataId = JpaAuditMetadata.createIdentifier(entity, path);
            metadataService.evictAndGet(metadataId);
        }
    }

    /**
     * Create the class for entity which will hold the revision information for
     * Hibernate Envers
     * <p/>
     * This use {@link #REVISION_LOG_ENTITY_NAME} as class name and look for
     * <em>the first package which contains a entity</em> to place it.
     * 
     */
    private void installRevisonEntity() {

        PathResolver pathResolver = projectOperations.getPathResolver();

        JavaType target = generateRevionEntityJavaType();

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(target,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        File targetFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        if (targetFile.exists()) {
            Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                    target);
        }

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, target,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);

        // Add @GvNIXJpaAuditListener annotation
        AnnotationMetadataBuilder jpaAuditRevisionEntityAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXJpaAuditRevisionEntity.class));
        annotations.add(jpaAuditRevisionEntityAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        // Create Revision entity class
        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }

    /**
     * Generates new a JavaType for revision log entity.
     * <p/>
     * Locates the early package which contains a entity and use it as domain
     * package.
     * 
     * @return
     */
    private JavaType generateRevionEntityJavaType() {
        // Use the TypeLocationService to scan project for all types with entity
        // annotation
        Set<JavaPackage> packages = new HashSet<JavaPackage>();
        for (JavaType entity : typeLocationService
                .findTypesWithAnnotation(RooJavaType.ROO_JPA_ACTIVE_RECORD)) {
            packages.add(entity.getPackage());
        }

        // Get the shorter (lowest deep level) package which contains an entity
        JavaPackage targetPackage = null;
        for (JavaPackage cur : packages) {
            if (targetPackage == null
                    || cur.getElements().size() < targetPackage.getElements()
                            .size()) {
                targetPackage = cur;
            }
        }

        if (targetPackage == null) {
            throw new IllegalStateException(
                    "No entities found on project: Can't identify package for revision entity.");
        }

        // Create JavaType with locate package
        return new JavaType(targetPackage.getFullyQualifiedPackageName()
                .concat(".").concat(REVISION_LOG_ENTITY_NAME));
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
        if (!HIBERNATE_PERSISTENCE_PROVIDER_CLASS.equals(provider)) {
            throw new IllegalStateException(
                    String.format(
                            "Error loading file '%s': unexpected /persistence/persistence-unit/provider (expected: '%s' found: '%s')",
                            persistencePath,
                            HIBERNATE_PERSISTENCE_PROVIDER_CLASS, provider));
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
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {

        String revisionEntityMetadatId = JpaAuditRevisionEntityMetadata
                .createIdentifier(getRevisionEntityJavaType(),
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
     * @return installed RevisonEntity JavaType
     */
    public JavaType getRevisionEntityJavaType() {
        if (this.revisionEntityJavaType == null) {
            Set<ClassOrInterfaceTypeDetails> found = typeLocationService
                    .findClassesOrInterfaceDetailsWithAnnotation(GVNIX_REVION_ENTITY_ANNOTATION);

            if (found.isEmpty()) {
                throw new IllegalStateException(String.format(
                        "Class with %s annotation is missing",
                        GVNIX_REVION_ENTITY_ANNOTATION
                                .getFullyQualifiedTypeName()));
            }
            else if (found.size() > 1) {
                throw new IllegalStateException(String.format(
                        "More than 1 classes with %s annotation",
                        GVNIX_REVION_ENTITY_ANNOTATION
                                .getFullyQualifiedTypeName()));
            }
            this.revisionEntityJavaType = found.iterator().next().getType();
        }
        return this.revisionEntityJavaType;
    }

    /**
     * Clean cached revision entity javaType
     */
    void cleanRevisionEntityJavaType() {
        this.revisionEntityJavaType = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionLogRevisionEntityMetadataBuilder getRevisonEntityMetadataBuilder(
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        return new EnversRevisionLogEntityMetadataBuilder(
                governorPhysicalTypeMetadata, isSpringSecInstalled());
    }
}
