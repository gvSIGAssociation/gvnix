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
package org.gvnix.web.menu.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.util.Assert;

/**
 * Abstract class for gvNIX menu metada prividers
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component(componentAbstract = true)
public abstract class AbstractMenuMetadataProvider implements MetadataProvider,
	FileEventListener, MetadataNotificationListener {

    private static Logger logger = Logger
	    .getLogger(AbstractMenuMetadataProvider.class.getName());

    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;
    @Reference
    protected MetadataService metadataService;
    @Reference
    protected MetadataDependencyRegistry metadataDependencyRegistry;

    protected AbstractMenuMetadata currentMentadata;

    public MetadataItem get(String metadataIdentificationString) {
	Assert.isTrue(this.getMetadataIdentiferFinal().equals(
		metadataIdentificationString), "Unexpected metadata request '"
		+ metadataIdentificationString + "' for this provider");

	return currentMentadata;
    }

    protected boolean isActivated() {
	if (metadataService == null) {
	    logger.warning("metadataService == null !!!!");
	    return false;
	}
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return false;
	}
	return projectMetadata.getPathResolver() != null;
    }

    protected abstract String getMetadataIdentiferFinal();

    public void onFileEvent(FileEvent fileEvent) {

	if (!isActivated()) {
	    return;
	}
	// We are going to monitor MenuModel java file to maintain metadata
	if (currentMentadata == null) {
	    return;
	}

	String filePath;
	if (fileEvent.getOperation().equals(FileOperation.DELETED)) {
	    filePath = fileEvent.getFileDetails().getCanonicalPath();
	} else if (fileEvent.getOperation().equals(FileOperation.RENAMED)) {
	    filePath = fileEvent.getPreviousName().getAbsolutePath();
	} else {
	    return;
	}

	// Discards event if it's event of our java file
	if (!currentMentadata.getPhysicalTypeMetadata()
		.getPhysicalLocationCanonicalPath().equals(filePath)) {
	    return;
	}

	// if java file is deleted this will clear this.currentMentadata
	metadataService.evict(currentMentadata.getId());
	this.currentMentadata = null;

    }

    protected abstract AbstractMenuMetadata createMenuModelMetadata(
	    PhysicalTypeMetadata physicalTypeMetadata);

    protected abstract Class getAnnotationClass();

    public void notify(String upstreamDependency, String downstreamDependency) {
	// We are going to listener all java classes creation looking for
	// any that has @GvNIXMenuModel annotation
	if (!PhysicalTypeIdentifier.isValid(upstreamDependency)) {
	    return;
	}
	PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
		.get(upstreamDependency);

	if (physicalTypeMetadata == null){
	    return;
	}

	AbstractMenuMetadata newMetadata = createMenuModelMetadata(physicalTypeMetadata);

	if (newMetadata == null) {
	    return;
	}

	// We have one class with @GvNIXMenuModel annotation
	if (currentMentadata == null) {
	    currentMentadata = newMetadata;
	    return;
	}

	// Assure that there isn't any more class with annotation
	// (this.currentMetadata)
	// checking if previous exists.
	PhysicalTypeMetadata previousPhysicalTypeMetadata = (PhysicalTypeMetadata) metadataService
		.get(currentMentadata.getPhysicalTypeMetadata().getId());

	if (previousPhysicalTypeMetadata == null) {
	    currentMentadata = newMetadata;
	    return;
	}

	// Refresh previous metadata
	AbstractMenuMetadata previousMetadata = createMenuModelMetadata(previousPhysicalTypeMetadata);

	if (previousMetadata == null) {
	    currentMentadata = newMetadata;
	    return;
	}

	if (previousMetadata.getPhysicalTypeMetadata()
		.getPhysicalLocationCanonicalPath().equals(
			newMetadata.getPhysicalTypeMetadata()
				.getPhysicalLocationCanonicalPath())) {
	    currentMentadata = newMetadata;
	    return;

	}

	// If there are to classes annotates throw an exception and clear
	// this.currentMetadata
	currentMentadata = null;
	throw new IllegalStateException("Only one class can have "
		+ getAnnotationClass().getCanonicalName()
		+ " annotation: "
		+ previousPhysicalTypeMetadata.getPhysicalTypeDetails()
			.getName().getFullyQualifiedTypeName()
		+ " and "
		+ physicalTypeMetadata.getPhysicalTypeDetails().getName()
			.getFullyQualifiedTypeName());

    }

}
