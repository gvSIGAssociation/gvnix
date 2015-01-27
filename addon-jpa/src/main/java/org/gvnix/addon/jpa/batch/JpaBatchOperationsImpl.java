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
package org.gvnix.addon.jpa.batch;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.JpaOperations;
import org.gvnix.addon.jpa.annotations.batch.GvNIXJpaBatch;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of {@link JpaBatchOperations}
 * 
 * @since 1.1
 */
@Component
@Service
public class JpaBatchOperationsImpl implements JpaBatchOperations {

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private PathResolver pathResolver;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if gvNIX JPA dependencies installed
        return projectOperations
                .isFeatureInstalledInFocusedModule(JpaOperations.FEATURE_NAME_GVNIX_JPA);
    }

    /**
     * Generates new a JavaType for batch service class based on
     * <code>entity</code> class name.
     * 
     * @param entity
     * @param targetPackage if null uses <code>entity</code> package
     * @return
     */
    private JavaType generateJavaType(JavaType entity, JavaPackage targetPackage) {
        if (targetPackage == null) {
            targetPackage = entity.getPackage();
        }
        return new JavaType(String.format("%s.%sBatchService",
                targetPackage.getFullyQualifiedPackageName(),
                entity.getSimpleTypeName()));
    }

    /** {@inheritDoc} */
    public void createAll(JavaPackage targetPackage) {
        // Use the TypeLocationService to scan project for all types with a
        // specific annotation
        for (JavaType entity : typeLocationService
                .findTypesWithAnnotation(RooJavaType.ROO_JPA_ACTIVE_RECORD)) {
            ClassOrInterfaceTypeDetails entityDetails = typeLocationService
                    .getTypeDetails(entity);
            if (entityDetails.isAbstract()) {
                // ignore abstract classes
                continue;
            }
            if (targetPackage == null) {
                create(entity, null);
            }
            else {
                create(entity, generateJavaType(entity, targetPackage));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void create(JavaType entity, JavaType target) {
        Validate.notNull(entity, "Entity required");
        if (target == null) {
            target = generateJavaType(entity, null);
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
        Validate.isTrue(!targetFile.exists(), "Type '%s' already exists",
                target);

        // Prepare class builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, modifier, target,
                PhysicalTypeCategory.CLASS);

        // Prepare annotations array
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                2);

        // Add @Service annotations
        annotations.add(new AnnotationMetadataBuilder(SpringJavaType.SERVICE));

        // Add @GvNIXJpaBatch annotation
        AnnotationMetadataBuilder jpaBatchAnnotation = new AnnotationMetadataBuilder(
                new JavaType(GvNIXJpaBatch.class));
        jpaBatchAnnotation.addClassAttribute("entity", entity);
        annotations.add(jpaBatchAnnotation);

        // Set annotations
        cidBuilder.setAnnotations(annotations);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }
}