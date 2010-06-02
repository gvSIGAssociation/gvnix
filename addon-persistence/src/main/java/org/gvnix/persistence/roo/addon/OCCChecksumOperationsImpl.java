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
package org.gvnix.persistence.roo.addon;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.ItdMetadataScanner;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;

/**
 * gvNIX OCCChecksum operation service implementation
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class OCCChecksumOperationsImpl implements OCCChecksumOperations {

    private static final Dependency GVNIX_CORE_ANOTATIONS = new Dependency(
	    "org.gvnix", "org.gvnix.annotations", "0.3.0-SNAPSHOT"); // FIXME
									       // Version
									       // must
									       // be
									       // load
									       // by
									       // anyway

    private static Logger logger = Logger.getLogger(OCCChecksumOperationsImpl.class
	    .getName());

    @Reference private MetadataService metadataService;
    @Reference private ClasspathOperations classpathOperations;
    @Reference private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference private PathResolver pathResolver;
    @Reference private FileManager fileManager;
    @Reference private ItdMetadataScanner itdMetadataScanner;
    @Reference private ProjectOperations projectOperations;

    protected void activate(ComponentContext context) {

    }

    protected void deactivate(ComponentContext context) {

    }

    /* (non-Javadoc)
     * @see org.gvnix.persistence.roo.addon.OCCChecksumOperations#isOCCChecksumAvailable()
     */
    public boolean isOCCChecksumAvailable() {
	return classpathOperations.isPersistentClassAvailable();
    }

    /* (non-Javadoc)
     * @see org.gvnix.persistence.roo.addon.OCCChecksumOperations#addOccToEntity(org.springframework.roo.model.JavaType, java.lang.String, java.lang.String)
     */
    public void addOccToEntity(JavaType entity, String fieldName,
	    String digestMethod) {
	addGvNIXAnnotationsDependecy();
	this.doAddOccToEntity(entity, fieldName, digestMethod);
    }

    private void doAddOccToEntity(JavaType entity, String fieldName,
	    String digestMethod) {

	// Load class details. If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails tmpDetails = classpathOperations
		.getClassOrInterface(entity);

	// Checks if it's mutable
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		tmpDetails, "Can't modify " + tmpDetails.getName());

	MutableClassOrInterfaceTypeDetails entityDetails = (MutableClassOrInterfaceTypeDetails) tmpDetails;

	List<? extends AnnotationMetadata> entityAnnotations = entityDetails
		.getTypeAnnotations();

	// Looks for @GvNIXEntityOCCChecksumAnnotation and @RooEntity
	AnnotationMetadata occAnnotation = null;
	AnnotationMetadata rooEntityAnnotation = null;
	for (AnnotationMetadata annotationItem : entityAnnotations) {
	    if (annotationItem.getAnnotationType().getFullyQualifiedTypeName()
		    .equals(GvNIXEntityOCCChecksum.class.getName())) {
		occAnnotation = annotationItem;
	    } else if (annotationItem.getAnnotationType()
		    .getFullyQualifiedTypeName().equals(
			    RooEntity.class.getName())) {
		rooEntityAnnotation = annotationItem;
	    }

	    if (rooEntityAnnotation != null && occAnnotation != null) {
		// we found all we're looking for
		break;
	    }
	}

	// Checks it's a RooEntity
	Assert.notNull(rooEntityAnnotation, entityDetails.getName()
		+ " isn't a RooEntity");

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
	occAnnotation = new DefaultAnnotationMetadata(new JavaType(
		GvNIXEntityOCCChecksum.class.getName()),
		occAnnotationAttributes);

	// Adds GvNIXEntityOCCChecksum to the entity
	entityDetails.addTypeAnnotation(occAnnotation);

	// TODO Does it need to do anything more to make persistent this
	// changes?

    }

    /* (non-Javadoc)
     * @see org.gvnix.persistence.roo.addon.OCCChecksumOperations#addOccAll(java.lang.String, java.lang.String)
     */
    public void addOccAll(String fieldName, String digestMethod) {
	addGvNIXAnnotationsDependecy();

	FileDetails srcRoot = new FileDetails(new File(pathResolver
		.getRoot(Path.SRC_MAIN_JAVA)), null);
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
			|| ptm.getPhysicalTypeDetails() == null
			|| !(ptm.getPhysicalTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
		    continue;
		}

		ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm
			.getPhysicalTypeDetails();
		if (Modifier.isAbstract(cid.getModifier())) {
		    continue;
		}

		Set<MetadataItem> metadata = itdMetadataScanner.getMetadata(id);
		for (MetadataItem item : metadata) {
		    if (item instanceof EntityMetadata) {
			EntityMetadata em = (EntityMetadata) item;
			doAddOccToEntity(javaType, fieldName, digestMethod);
			break;
		    }
		}

	    }
	}
	return;

    }

    /* (non-Javadoc)
     * @see org.gvnix.persistence.roo.addon.OCCChecksumOperations#addGvNIXAnnotationsDependecy()
     */
    public void addGvNIXAnnotationsDependecy() {
	ProjectMetadata project = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (project == null) {
	    return;
	}

	if (project.getDependencies().contains(GVNIX_CORE_ANOTATIONS)) {
	    return;
	} else if (project.getDependenciesExcludingVersion(
		GVNIX_CORE_ANOTATIONS).size() > 0) {
	    // TODO what we should do?

	}
	projectOperations.dependencyUpdate(GVNIX_CORE_ANOTATIONS);
    }

}
