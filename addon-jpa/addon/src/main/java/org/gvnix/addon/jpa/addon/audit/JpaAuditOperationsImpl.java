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
package org.gvnix.addon.jpa.addon.audit;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.addon.JpaOperations;
import org.gvnix.addon.jpa.addon.audit.providers.RevisionLogProvider;
import org.gvnix.addon.jpa.addon.audit.providers.RevisionLogProviderId;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAudit;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAuditListener;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAuditRevisionEntity;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAuditUserService;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Implementation of {@link JpaAuditOperations}
 * <p/>
 * For {@link #getUserServiceType()} this class implements a simple cache system
 * to store computed value. This is due to performance problems on
 * look-for-an-annotated-class mechanism.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
@Reference(name = "provider",
        strategy = ReferenceStrategy.EVENT,
        policy = ReferencePolicy.DYNAMIC,
        referenceInterface = RevisionLogProvider.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class JpaAuditOperationsImpl implements JpaAuditOperations,
        JpaAuditOperationsMetadata, JpaAuditOperationsSPI {

    private static final JavaType AUDIT_ANNOTATION_TYPE = new JavaType(
            GvNIXJpaAudit.class.getName());

    private static final JavaType AUDIT_USR_SERV_ANN_T = new JavaType(
            GvNIXJpaAuditUserService.class.getName());

    private static final String DEFAULT_USER_SERVICE_NAME = "AuditUserService";

    private static final Logger LOGGER = HandlerUtils
            .getLogger(JpaAuditOperationsImpl.class);

    private static final int EVICT_CACHE_MLSEC = 60 * 3 * 1000;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private MemberDetailsScanner memberDetailsScanner;

    @Reference
    private MetadataService metadataService;

    // Cache user services
    private JavaType userServiceType = null;

    // User service cache timestamp
    private Long userServiceTypeTimestamp = null;

    // Cache revisionEntityJavaType services
    private JavaType revisionEntityJavaType;

    // revisionEntityJavaType cache timestamp
    private Long revEntJTypeTimestamp = null;

    /**
     * Registered providers
     */
    private List<RevisionLogProvider> providers = new ArrayList<RevisionLogProvider>();

    /**
     * Current active provider
     */
    private RevisionLogProvider currentProvider = null;

    /**
     * Bind a provider
     * 
     * @param provider
     */
    protected void bindProvider(final RevisionLogProvider provider) {
        providers.add(provider);
    }

    /**
     * Unbind a provider
     * 
     * @param provider
     */
    protected void unbindProvider(final RevisionLogProvider provider) {
        providers.remove(provider);
        // Reset current provider
        currentProvider = null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSetupCommandAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(JpaOperations.FEATURE_NAME_GVNIX_JPA)
                && !hasUserService();
    }

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if gvNIX JPA dependencies installed
        return projectOperations
                .isFeatureInstalledInFocusedModule(JpaOperations.FEATURE_NAME_GVNIX_JPA)
                && hasUserService();
    }

    /**
     * Clean the User service Cache data
     */
    public void evictUserServiceInfoCache() {
        this.userServiceType = null;
        this.userServiceTypeTimestamp = null;
    }

    /**
     * Load userService and userType data if is needed (manage cache)
     * */
    public void loadUserServiceData() {
        // Check cache
        if (userServiceTypeTimestamp != null) {
            // Check for valid cache
            if (System.currentTimeMillis()
                    - userServiceTypeTimestamp.longValue() < EVICT_CACHE_MLSEC) {
                // return javaType cache
                return;
            }
        }
        // evict cache
        evictUserServiceInfoCache();

        // Look for user service class
        Set<ClassOrInterfaceTypeDetails> classes = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(AUDIT_USR_SERV_ANN_T);

        if (classes != null && !classes.isEmpty()) {

            // Check for multiple classes
            if (classes.size() > 1) {
                LOGGER.severe(String.format(
                        "Only one class can be annotated with %s: found %s",
                        AUDIT_USR_SERV_ANN_T, classes.size()));
            }
            else {
                // load data from locate of userService
                ClassOrInterfaceTypeDetails cid = classes.iterator().next();
                userServiceType = cid.getType();
                userServiceTypeTimestamp = System.currentTimeMillis();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * In order to solve possible performance problems when it tries to look for
     * User-service class, a very simple cache mechanism has been implemented.
     * */
    @Override
    public JavaType getUserServiceType() {
        loadUserServiceData();
        return userServiceType;
    }

    private boolean hasUserService() {
        return getUserServiceType() != null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSpringSecurityInstalled() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.SECURITY);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProvidersAvailable() {

        if (providers.isEmpty()) {
            return false;
        }
        for (RevisionLogProvider provider : providers) {
            if (provider.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates new a JavaType for entity listener class based on
     * <code>entity</code> class name.
     * 
     * @param entity
     * @param targetPackage if null uses <code>entity</code> package
     * @return
     */
    private JavaType generateListenerJavaType(JavaType entity,
            JavaPackage targetPackage) {
        if (targetPackage == null) {
            targetPackage = entity.getPackage();
        }
        return new JavaType(String.format("%s.%sAuditListener",
                targetPackage.getFullyQualifiedPackageName(),
                entity.getSimpleTypeName()));
    }

    /** {@inheritDoc} */
    public void createAll(JavaPackage targetPackage) {
        // Use the TypeLocationService to scan project for all types with a
        // specific annotation
        for (JavaType entity : typeLocationService
                .findTypesWithAnnotation(RooJavaType.ROO_JPA_ACTIVE_RECORD)) {
            JavaType finalType = null;
            if (targetPackage != null) {
                finalType = generateListenerJavaType(entity, targetPackage);
            }
            create(entity, finalType, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void create(JavaType entity, JavaType target) {
        create(entity, target, true);
    }

    /** {@inheritDoc} */
    @Override
    public void create(JavaType entity, JavaType target,
            boolean failIfAlreadySet) {
        Validate.notNull(entity, "Entity required");
        if (target == null) {
            target = generateListenerJavaType(entity, null);
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
        if (targetFile.exists()) {
            if (failIfAlreadySet) {
                Validate.isTrue(!targetFile.exists(),
                        "Type '%s' already exists", target);
            }
            else {
                LOGGER.info(String.format(
                        "Ignoring entity '%s': Type '%s' already exists",
                        entity, target));
                return;
            }
        }

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, target,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                2);

        // Add @GvNIXJpaAuditListener annotation
        AnnotationMetadataBuilder jpaAuditListenerAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXJpaAuditListener.class));
        jpaAuditListenerAnnotation.addClassAttribute("entity", entity);
        annotations.add(jpaAuditListenerAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        // Add GvNIXJpaAudit annotation to entity
        if (!annotateEntity(entity)) {
            // Already set annotation. Nothing to do
            LOGGER.info(String
                    .format("Entity %s is already annotated with %s: ignore this entity.",
                            entity.getFullyQualifiedTypeName(),
                            GvNIXJpaAudit.class.getSimpleName()));
            return;
        }

        // Create Listener class
        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }

    /**
     * Annotated entity with {@link GvNIXJpaAudit}
     * <p/>
     * 
     * @param entity
     * @return true if entity has been annotated or false if entity is already
     *         annotated
     */
    public boolean annotateEntity(final JavaType entity) {
        Validate.notNull(entity, "Java type required");

        // get class details
        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(entity);
        if (cid == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    .concat(entity.getFullyQualifiedTypeName()).concat("'"));
        }

        // Check for @GvNIXJpaAudit annotation
        if (MemberFindingUtils.getAnnotationOfType(cid.getAnnotations(),
                AUDIT_ANNOTATION_TYPE) == null) {
            // Add GvNIXJpaAudit annotation
            final AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    AUDIT_ANNOTATION_TYPE);
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    cid);
            cidBuilder.addAnnotation(annotationBuilder);
            typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
            return true;
        }
        else {
            // Already annotated
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RevisionLogProvider> getAvailableRevisionLogProviders() {
        List<RevisionLogProvider> availables = new ArrayList<RevisionLogProvider>(
                providers.size());
        if (!providers.isEmpty()) {
            for (RevisionLogProvider provider : providers) {
                if (provider.isAvailable()) {
                    availables.add(provider);
                }
            }
        }
        return Collections.unmodifiableList(availables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionLogProvider getActiveRevisionLogProvider() {
        // Try to use cached provider (if any)
        if (currentProvider != null && currentProvider.isActive()) {
            return currentProvider;
        }
        if (providers.isEmpty()) {
            return null;
        }
        RevisionLogProvider active = null;
        for (RevisionLogProvider provider : providers) {
            if (!provider.isAvailable()) {
                continue;
            }
            if (provider.isActive()) {
                if (active != null) {
                    throw new IllegalStateException(String.format(
                            "Two active providers: %s and %s",
                            active.getName(), provider.getName()));
                }
                active = provider;
            }
        }
        // Cache activated found
        currentProvider = active;
        return currentProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionLogProviderId getProviderIdByName(String value) {
        if (providers.isEmpty()) {
            return null;
        }

        for (RevisionLogProvider provider : providers) {
            if (provider.isAvailable()
                    && StringUtils.equals(value, provider.getName())) {
                return new RevisionLogProviderId(provider);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RevisionLogProviderId> getProvidersId() {
        List<RevisionLogProviderId> availables = new ArrayList<RevisionLogProviderId>(
                providers.size());
        if (!providers.isEmpty()) {
            for (RevisionLogProvider provider : providers) {
                if (provider.isAvailable()) {
                    availables.add(new RevisionLogProviderId(provider));
                }
            }
        }
        return Collections.unmodifiableList(availables);
    }

    @Override
    public void activeRevisionLog(RevisionLogProviderId provider) {

        // Check current provider installed
        RevisionLogProvider currentActive = getActiveRevisionLogProvider();
        if (currentActive != null) {
            // By now, we don't allow change provider
            throw new IllegalStateException("Provider ".concat(
                    currentActive.getName()).concat(" is alredy configured."));
        }

        // look for provider on available providers
        RevisionLogProvider toActive = null;
        List<RevisionLogProvider> availables = getAvailableRevisionLogProviders();
        for (RevisionLogProvider toCheck : availables) {
            if (provider.is(toCheck)) {
                toActive = toCheck;
                break;
            }
        }

        // Provider not found
        if (toActive == null) {
            throw new IllegalArgumentException("Provider ".concat(
                    provider.getId()).concat(
                    " is not available for this project."));
        }

        // Setup provider
        toActive.setup(this);
    }

    /**
     * Create the class for entity which will hold the revision information for
     * Hibernate Envers
     * <p/>
     * This use {@link #REVISION_LOG_ENTITY_NAME} as class name and look for
     * <em>the first package which contains a entity</em> to place it.
     * 
     */
    public void installRevisonEntity(JavaType revisionEntity) {

        PathResolver pathResolver = projectOperations.getPathResolver();

        JavaType target;
        if (revisionEntity == null) {
            target = generateRevionEntityJavaType();
        }
        else {
            target = revisionEntity;
        }

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
        AnnotationMetadataBuilder jpaAuditRevEntAnn = new AnnotationMetadataBuilder(
                new JavaType(GvNIXJpaAuditRevisionEntity.class));
        annotations.add(jpaAuditRevEntAnn);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        // Create Revision entity class
        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }

    /**
     * @return installed RevisonEntity JavaType
     */
    public JavaType getRevisionEntityJavaType() {
        if (this.revisionEntityJavaType != null) {
            if (System.currentTimeMillis() - revEntJTypeTimestamp.longValue() < EVICT_CACHE_MLSEC) {
                return this.revisionEntityJavaType;
            }
        }
        Set<ClassOrInterfaceTypeDetails> found = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(JpaAuditOperationsSPI.GVNIX_REVION_ENTITY_ANNOTATION);

        if (found.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "Class with %s annotation is missing",
                    JpaAuditOperationsSPI.GVNIX_REVION_ENTITY_ANNOTATION
                            .getFullyQualifiedTypeName()));
        }
        else if (found.size() > 1) {
            throw new IllegalStateException(String.format(
                    "More than 1 classes with %s annotation",
                    JpaAuditOperationsSPI.GVNIX_REVION_ENTITY_ANNOTATION
                            .getFullyQualifiedTypeName()));
        }
        this.revisionEntityJavaType = found.iterator().next().getType();
        this.revEntJTypeTimestamp = System.currentTimeMillis();
        return this.revisionEntityJavaType;
    }

    /**
     * Clean cached revision entity javaType
     */
    void cleanRevisionEntityJavaType() {
        this.revisionEntityJavaType = null;
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
        JavaPackage targetPackage = getBaseDomainPackage();

        if (targetPackage == null) {
            throw new IllegalStateException(
                    "No entities found on project: Can't identify package for revision entity.");
        }

        // Create JavaType with locate package
        return new JavaType(targetPackage.getFullyQualifiedPackageName()
                .concat(".")
                .concat(JpaAuditOperationsSPI.REVISION_LOG_ENTITY_NAME));
    }

    /**
     * {@inheritDoc}
     */
    public JavaPackage getBaseDomainPackage() {
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
        return targetPackage;
    }

    @Override
    public void setup(JavaType serviceClass, JavaType userType) {
        // Check parameters: get defaults
        JavaType targetServiceClass;
        if (serviceClass == null) {
            targetServiceClass = generateUserServiceJavaType();
        }
        else {
            targetServiceClass = serviceClass;
        }
        JavaType targetUserType;
        if (userType == null) {
            targetUserType = JavaType.STRING;
        }
        else {
            targetUserType = userType;
        }

        Validate.isTrue(!JdkJavaType.isPartOfJavaLang(targetServiceClass
                .getSimpleTypeName()),
                "Target service class '%s' must not be part of java.lang",
                targetServiceClass);

        int modifier = Modifier.PUBLIC;

        final String declaredByMetadataId = PhysicalTypeIdentifier
                .createIdentifier(targetServiceClass,
                        pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
        File targetUserServiceFile = new File(
                typeLocationService
                        .getPhysicalTypeCanonicalPath(declaredByMetadataId));
        if (targetUserServiceFile.exists()) {
            Validate.isTrue(!targetUserServiceFile.exists(),
                    "Type '%s' already exists", targetServiceClass);
        }

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, targetServiceClass,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                2);

        // Add @GvNIXJpaAuditUserService annotation
        AnnotationMetadataBuilder jpaAuditUserServiceAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXJpaAuditUserService.class));
        if (!JavaType.STRING.equals(targetUserType)) {
            jpaAuditUserServiceAnnotation.addClassAttribute("userType",
                    targetUserType);
        }
        annotations.add(jpaAuditUserServiceAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        // Create User Service class
        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());

        refreshAuditedEntities();

        PathResolver pathResolver = projectOperations.getPathResolver();
        LogicalPath path = pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA);
        String metadataId = JpaAuditUserServiceMetadata.createIdentifier(
                targetServiceClass, path);

        JpaAuditUserServiceMetadata metadata = (JpaAuditUserServiceMetadata) metadataService
                .get(metadataId);

        // Show warning about UserType
        if (isSpringSecurityInstalled()) {
            if (JavaType.STRING.equals(targetUserType)) {
                LOGGER.warning(String
                        .format("Generating implemention of %s.%s() method which use UserDetails.getName() as user info. Customize it if is needed.",
                                targetServiceClass,
                                JpaAuditUserServiceMetadata.GET_USER_METHOD));
            }
            else if (!(metadata.isUserTypeSpringSecUserDetails() && metadata
                    .isUserTypeEntity())) {
                LOGGER.warning(String
                        .format("You MUST customize %s.%s() method to provider user information for aduit. Addon can't identify how get %s instance.",
                                targetServiceClass,
                                JpaAuditUserServiceMetadata.GET_USER_METHOD,
                                targetUserType));
            }
        }
        else {
            LOGGER.warning(String
                    .format("You MUST implement %s.%s() method to provider user information for aduit.",
                            targetServiceClass,
                            JpaAuditUserServiceMetadata.GET_USER_METHOD));
        }

    }

    private JavaType generateUserServiceJavaType() {
        JavaPackage basePackage = getBaseDomainPackage();
        return new JavaType(basePackage.getFullyQualifiedPackageName()
                .concat(".").concat(DEFAULT_USER_SERVICE_NAME));
    }

    /**
     * Returns details of the given Java type's members
     * 
     * @param type the type for which to get the members (required)
     * @return <code>null</code> if the member details are unavailable
     */
    @SuppressWarnings("unused")
    private MemberDetails getMemberDetails(final JavaType type) {
        final String physicalTypeIdentifier = typeLocationService
                .getPhysicalTypeIdentifier(type);
        if (physicalTypeIdentifier == null) {
            return null;
        }
        // We need to lookup the metadata we depend on
        final PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(physicalTypeIdentifier);
        return getMemberDetails(physicalTypeMetadata);
    }

    /**
     * Returns details of the given physical type's members
     * 
     * @param physicalTypeMetadata the physical type for which to get the
     *        members (can be <code>null</code>)
     * @return <code>null</code> if the member details are unavailable
     */
    protected MemberDetails getMemberDetails(
            final PhysicalTypeMetadata physicalTypeMetadata) {
        // We need to abort if we couldn't find dependent metadata
        if (physicalTypeMetadata == null || !physicalTypeMetadata.isValid()) {
            return null;
        }

        final ClassOrInterfaceTypeDetails cid = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        if (cid == null) {
            // Abort if the type's class details aren't available (parse error
            // etc)
            return null;
        }
        return memberDetailsScanner.getMemberDetails(getClass().getName(), cid);
    }

    /**
     * {@inheritDoc}
     */
    public void refreshAuditedEntities() {
        // Use the TypeLocationService to scan project for all types with
        // jpaAudit and related annotation
        String metadataId;
        PathResolver pathResolver = projectOperations.getPathResolver();
        LogicalPath path = pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA);
        for (JavaType entity : typeLocationService
                .findTypesWithAnnotation(new JavaType(GvNIXJpaAudit.class))) {
            metadataId = JpaAuditMetadata.createIdentifier(entity, path);
            metadataService.evictAndGet(metadataId);
        }
        for (JavaType entity : typeLocationService
                .findTypesWithAnnotation(new JavaType(
                        GvNIXJpaAuditListener.class))) {
            metadataId = JpaAuditListenerMetadata
                    .createIdentifier(entity, path);
            metadataService.evictAndGet(metadataId);
        }

    }
}