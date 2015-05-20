/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.occ.roo.addon.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.occ.roo.addon.annotations.GvNIXEntityOCCChecksum;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.addon.JpaOperations;
import org.springframework.roo.addon.jpa.addon.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.jpa.annotations.activerecord.RooJpaActiveRecord;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * gvNIX OCCChecksum operation service implementation
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Component
@Service
public class OCCChecksumOperationsImpl implements OCCChecksumOperations {

    private static final Logger logger = HandlerUtils
            .getLogger(OCCChecksumOperationsImpl.class);

    @Reference
    private MetadataService metadataService;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private JpaOperations entityOperations;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private PersistenceMemberLocator persistenceMemberLocator;
    @Reference
    private TypeManagementService typeManagementService;

    protected void activate(ComponentContext context) {
        // TODO:
    }

    protected void deactivate(ComponentContext context) {
        // TODO:
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.occ.roo.addon.OCCChecksumOperations#isOCCChecksumAvailable()
     */
    public boolean isOCCChecksumAvailable() {
        return entityOperations.isPersistentClassAvailable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.occ.roo.addon.OCCChecksumOperations#addOccToEntity(org.
     * springframework.roo.model.JavaType, java.lang.String, java.lang.String)
     */
    public void addOccToEntity(JavaType entity, String fieldName,
            String digestMethod) {
        addGvNIXAnnotationsDependecy();
        this.doAddOccToEntity(entity, fieldName, digestMethod, true);
    }

    /**
     * Annotates given entity of its parent with GvNIXEntityOCCChecksum. If
     * <code>annotatedParent</code> is set false, if @javax.persistence.Version
     * field is declared in a parent class of the given one the method returns
     * without annotate any class. When is set true, the method will annotate
     * the class where @javax.persistence.Version field is declared or the given
     * one if field is not declared yet.
     * 
     * @param entity
     * @param fieldName
     * @param digestMethod
     * @param annotateParent
     */
    private void doAddOccToEntity(JavaType entity, String fieldName,
            String digestMethod, boolean annotateParent) {
        // Check if given entity has a @Version field declared
        // Maybe the given entity extends of a class declaring the @Version
        // field, in this case we must to annotate parent class
        String entityMetadataKey = JpaActiveRecordMetadata.createIdentifier(
                entity, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));
        JpaActiveRecordMetadata entityMetadata = (JpaActiveRecordMetadata) metadataService
                .get(entityMetadataKey, true);
        if (entityMetadata == null) {
            // entity JavaType is not a valid Entity. Nothing to do
            return;
        }

        // Check if target entity has got a @Version field and it is in the same
        // class or in a parent one. If @Version field is declared in a parent
        // class, this will be the target class for annotate with
        // @GvNIXEntityOCCChecksum
        FieldMetadata versionField = persistenceMemberLocator
                .getVersionField(entity);
        if (versionField != null) {
            String declaredByType = versionField.getDeclaredByMetadataId()
                    .substring(
                            versionField.getDeclaredByMetadataId().lastIndexOf(
                                    "?") + 1);
            if (!entityMetadataKey.endsWith(declaredByType)) {
                // Annotate parent instead of given class just if it's requested
                if (annotateParent) {
                    // @Version field is declared in a parent class, so annotate
                    // the parent instead of given entity
                    JavaType entityToAnnotate = new JavaType(declaredByType);
                    logger.info("Entity ".concat(entityToAnnotate
                            .getFullyQualifiedTypeName()
                            .concat(" will be annotated instead of: "
                                    .concat(entity.getFullyQualifiedTypeName()))));
                    // Exchange target class
                    entity = entityToAnnotate;
                }
                else {
                    // We could annotate parent class with declared @Version
                    // field, but invoker doesn't want this.
                    return;
                }
            }
        }

        // Load class details. If class not found an exception will be raised.
        ClassOrInterfaceTypeDetails tmpDetails = typeLocationService
                .getTypeDetails(entity);

        // Checks if it's mutable
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class, tmpDetails,
                "Can't modify " + tmpDetails.getName());

        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                (ClassOrInterfaceTypeDetails) tmpDetails);

        List<? extends AnnotationMetadata> entityAnnotations = mutableTypeDetailsBuilder
                .build().getAnnotations();

        // Looks for @GvNIXEntityOCCChecksumAnnotation and @RooJpaActiveRecord
        AnnotationMetadata occAnnotation = MemberFindingUtils
                .getAnnotationOfType(entityAnnotations, new JavaType(
                        GvNIXEntityOCCChecksum.class.getName()));
        AnnotationMetadata rooEntityAnnotation = MemberFindingUtils
                .getAnnotationOfType(entityAnnotations, new JavaType(
                        RooJpaActiveRecord.class.getName()));

        if (rooEntityAnnotation != null) {
            if (occAnnotation != null) {
                // Already set annotation. Nothing to do
                logger.info("Entity ".concat(entity
                        .getFullyQualifiedTypeName()
                        .concat(" is already annotated with "
                                .concat(GvNIXEntityOCCChecksum.class.getName()))));
                return;
            }

            // Prepares GvNIXEntityOCCChecksum attributes
            List<AnnotationAttributeValue<?>> occAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>(
                    2);

            if (fieldName != null) {
                occAnnotationAttributes.add(new StringAttributeValue(
                        new JavaSymbolName("fieldName"), fieldName));
            }

            if (digestMethod != null) {
                occAnnotationAttributes.add(new StringAttributeValue(
                        new JavaSymbolName("digestMethod"), digestMethod));
            }

            // Creates GvNIXEntityOCCChecksum
            occAnnotation = new AnnotationMetadataBuilder(new JavaType(
                    GvNIXEntityOCCChecksum.class.getName()),
                    occAnnotationAttributes).build();

            // Adds GvNIXEntityOCCChecksum to the entity
            mutableTypeDetailsBuilder.addAnnotation(occAnnotation);

            typeManagementService
                    .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.occ.roo.addon.OCCChecksumOperations#addOccAll(java.lang.String,
     * java.lang.String)
     */
    public void addOccAll(String fieldName, String digestMethod) {
        addGvNIXAnnotationsDependecy();
        // Look for classes annotated with @RooJpaActiveRecord
        for (JavaType type : typeLocationService
                .findTypesWithAnnotation(new JavaType(RooJpaActiveRecord.class
                        .getName()))) {
            doAddOccToEntity(type, fieldName, digestMethod, false);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.occ.roo.addon.OCCChecksumOperations#addGvNIXAnnotationsDependecy
     * ()
     */
    public void addGvNIXAnnotationsDependecy() {

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        Element conf = XmlUtils.getConfiguration(this.getClass());

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", conf);
        for (Element repo : repos) {

            projectOperations.addRepository(projectOperations
                    .getFocusedModuleName(), new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", conf);
        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }
}
