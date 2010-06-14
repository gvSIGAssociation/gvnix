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
package org.gvnix.web.relation.styles.roo.addon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.ItdMetadataScanner;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.*;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;

/**
 * gvNIX relation table views service implementation.
 * 
 * 
 * @author Ricardo García Fernández( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class RelationsTableViewOperationsImpl implements RelationsTableViewOperations {

    private static Logger logger = Logger.getLogger(RelationsTableViewOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;

    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    @Reference
    private ItdMetadataScanner itdMetadataScanner;

    @Reference
    private MetadataDependencyRegistry dependencyRegistry;

    @Reference
    private PaginatedRelationTableActivationInfo paginatedRelationTableActivationInfo;
    
    private ComponentContext context;

    protected void activate(ComponentContext context) {
	this.context = context;
    }

    public boolean isProjectAvailable() {
	return paginatedRelationTableActivationInfo.isProjectAvailable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.relation.styles.roo.addon.RelationsTableViewOperations#
     * setUp()
     */
    public void setUp() {
	// Copy tagx to the project.
	copyTagx();

	// Launch search for entity manager.
	launchEntityFilter();
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.relation.styles.roo.addon.RelationsTableViewOperations#copyTagx()
     */
    public void copyTagx() {

	copyDirectoryContents("tags/relations/*.tagx", pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/relations"));
	copyDirectoryContents("tags/relations/decorators/*.tagx", pathResolver
		.getIdentifier(Path.SRC_MAIN_WEBAPP,
			"/WEB-INF/tags/relations/decorators"));
	copyDirectoryContents("tags/util/*.tagx", pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/util"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.relation.styles.roo.addon.RelationsTableViewOperations#
     * launchEntityFilter()
     */
    public void launchEntityFilter() {
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

			dependencyRegistry.notifyDownstream(em.getId());
		    }
		}
	    }
	}
	return;
    }

    /**
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath
     *            directory
     * @param target
     *            directory
     */
    private void copyDirectoryContents(String sourceAntPath,
	    String targetDirectory) {
	Assert.hasText(sourceAntPath, "Source path required");
	Assert.hasText(targetDirectory, "Target directory required");

	if (!targetDirectory.endsWith("/")) {
	    targetDirectory += "/";
	}

	if (!fileManager.exists(targetDirectory)) {
	    fileManager.createDirectory(targetDirectory);
	}

	String path = TemplateUtils.getTemplatePath(getClass(), sourceAntPath);
	Set<URL> urls = TemplateUtils.findMatchingClasspathResources(context
		.getBundleContext(), path);
	Assert.notNull(urls,
		"Could not search bundles for resources for Ant Path '" + path
			+ "'");
	for (URL url : urls) {
	    String fileName = url.getPath().substring(
		    url.getPath().lastIndexOf("/") + 1);
	    if (!fileManager.exists(targetDirectory + fileName)) {
		try {
		    FileCopyUtils.copy(url.openStream(), fileManager
			    .createFile(targetDirectory + fileName)
			    .getOutputStream());
		} catch (IOException e) {
		    new IllegalStateException(
			    "Encountered an error during copying of resources for MVC JSP addon.",
			    e);
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.relation.styles.roo.addon.RelationsTableViewOperations#
     * isActivated()
     */
    public boolean isActivated() {

	return paginatedRelationTableActivationInfo.isActivated();
    }

    public boolean isWebScaffoldGenerated() {

	return paginatedRelationTableActivationInfo.isWebScaffoldGenerated();
    }
}