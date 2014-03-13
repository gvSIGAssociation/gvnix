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
package org.gvnix.addon.jpa.audit;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.JpaOperations;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProvider;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProviderId;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Implementation of {@link JpaAuditOperations}
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
@Reference(name = "provider", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = RevisionLogProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class JpaAuditOperationsImpl implements JpaAuditOperations {

    private static final JavaType AUDIT_ANNOTATION_TYPE = new JavaType(
            GvNIXJpaAudit.class.getName());

    private static final Logger LOGGER = HandlerUtils
            .getLogger(JpaAuditOperationsImpl.class);

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private PathResolver pathResolver;

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
    public boolean isCommandAvailable() {
        // Check if gvNIX JPA dependencies installed
        return projectOperations
                .isFeatureInstalledInFocusedModule(JpaOperations.FEATURE_NAME_GVNIX_JPA);
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
                    + entity.getFullyQualifiedTypeName() + "'");
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
        toActive.setup();
    }

}