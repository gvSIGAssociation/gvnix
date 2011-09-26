/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.occ.roo.addon;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.entity.EntityOperations;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.javaparser.JavaParserClassMetadata;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * gvNIX OCCChecksum operation service implementation
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class OCCChecksumOperationsImpl implements OCCChecksumOperations {

    @Reference
    private MetadataService metadataService;
    // @Reference private ClasspathOperations classpathOperations;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;
    // @Reference private ItdMetadataScanner itdMetadataScanner;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private EntityOperations entityOperations;
    @Reference
    private TypeLocationService typeLocationService;

    protected void activate(ComponentContext context) {

    }

    protected void deactivate(ComponentContext context) {

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
        this.doAddOccToEntity(entity, fieldName, digestMethod);
    }

    private void doAddOccToEntity(JavaType entity, String fieldName,
            String digestMethod) {

        // Check if given entity has a @Version field declared
        // Maybe the given entity extends of a class declaring the @Version
        // field, in this case we must to annotate parent class
        String entityMetadataKey = EntityMetadata.createIdentifier(entity,
                Path.SRC_MAIN_JAVA);
        EntityMetadata entityMetadata = (EntityMetadata) metadataService
                .get(entityMetadataKey);
        FieldMetadata versionField = entityMetadata.getVersionField();

        if (versionField != null) {
            String declaredByType = versionField.getDeclaredByMetadataId()
                    .substring(
                            versionField.getDeclaredByMetadataId().lastIndexOf(
                                    "?") + 1);
            if (!entityMetadataKey.endsWith(declaredByType)) {
                // @Version field is declared in a parent class, so annotate
                // the parent instead of given entity
                entity = new JavaType(declaredByType);
            }
        }

        // Load class details. If class not found an exception will be raised.
        ClassOrInterfaceTypeDetails tmpDetails = typeLocationService
                .getClassOrInterface(entity);

        // Checks if it's mutable
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                tmpDetails, "Can't modify " + tmpDetails.getName());

        MutableClassOrInterfaceTypeDetails entityDetails = (MutableClassOrInterfaceTypeDetails) tmpDetails;

        List<? extends AnnotationMetadata> entityAnnotations = entityDetails
                .getAnnotations();

        // Looks for @GvNIXEntityOCCChecksumAnnotation and @RooEntity
        AnnotationMetadata occAnnotation = null;
        AnnotationMetadata rooEntityAnnotation = null;
        for (AnnotationMetadata annotationItem : entityAnnotations) {
            if (annotationItem.getAnnotationType().getFullyQualifiedTypeName()
                    .equals(GvNIXEntityOCCChecksum.class.getName())) {
                occAnnotation = annotationItem;
            } else if (annotationItem.getAnnotationType()
                    .getFullyQualifiedTypeName()
                    .equals(RooEntity.class.getName())) {
                rooEntityAnnotation = annotationItem;
            }

            if (rooEntityAnnotation != null && occAnnotation != null) {
                // we found all we're looking for
                break;
            }
        }

        if (rooEntityAnnotation != null) {
            if (occAnnotation != null) {
                // Already set annotation. Nothing to do
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
            entityDetails.addTypeAnnotation(occAnnotation);
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

        FileDetails srcRoot = new FileDetails(new File(
                pathResolver.getRoot(Path.SRC_MAIN_JAVA)), null);
        String antPath = pathResolver.getRoot(Path.SRC_MAIN_JAVA)
                + File.separatorChar + "**" + File.separatorChar + "*.java";
        SortedSet<FileDetails> entries = fileManager
                .findMatchingAntPath(antPath);

        for (FileDetails file : entries) {
            String fullPath = srcRoot.getRelativeSegment(file
                    .getCanonicalPath());
            fullPath = fullPath.substring(1, fullPath.lastIndexOf(".java"))
                    .replace(File.separatorChar, '.'); // ditch the first / and
            // .java
            JavaType javaType = new JavaType(fullPath);
            String id = physicalTypeMetadataProvider.findIdentifier(javaType);
            if (id != null) {
                PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                        .get(id);
                if (ptm == null
                        || ptm.getMemberHoldingTypeDetails() == null
                        || !(ptm.getMemberHoldingTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
                    continue;
                }

                ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm
                        .getMemberHoldingTypeDetails();
                if (Modifier.isAbstract(cid.getModifier())) {
                    continue;
                }

                JavaParserClassMetadata metadata = (JavaParserClassMetadata) metadataService
                        .get(id);
                if (metadata != null) {
                    doAddOccToEntity(javaType, fieldName, digestMethod);
                }
            }
        }
        return;

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

        Element conf = XmlUtils.getConfiguration(this.getClass(),
                "configuration.xml");

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", conf);
        for (Element repo : repos) {

            projectOperations.addRepository(new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", conf);
        for (Element depen : depens) {

            projectOperations.addDependency(new Dependency(depen));
        }
    }

}
