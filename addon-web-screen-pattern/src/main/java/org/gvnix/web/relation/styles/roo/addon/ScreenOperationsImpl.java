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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;

/**
 * gvNIX relation table views service implementation.
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ScreenOperationsImpl implements ScreenOperations {

    private static Logger logger = Logger.getLogger(ScreenOperations.class.getName());

    @Reference private FileManager fileManager;
    @Reference private MetadataService metadataService;
    @Reference private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference private MetadataDependencyRegistry dependencyRegistry;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc
     * into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    private ComponentContext context;

    protected void activate(ComponentContext context) {
	this.context = context;
    }

    /** 
     * {@inheritDoc}
     * <p>
     * Note the project isn't available when you start a new project (emtpy
     * project dir) because the project metadata doesn't exist yet. 
     */
    public boolean isProjectAvailable() {
      // Check if a project has been created
      return projectOperations.isProjectAvailable();
    }

    /** {@inheritDoc} */
    public boolean isActivated() {
	return fileManager.exists(getPathResolver().getIdentifier(
		Path.SRC_MAIN_WEBAPP,
		"WEB-INF/tags/util/gvnixcallfunction.tagx"));
    }

    /** 
     * {@inheritDoc}
     * <p>
     * Do not permit installation unless they have a web project with
     * Spring MVC.
     */
    public boolean isSpringMvcProject() {
      return fileManager.exists(getMvcConfigFile());
    }

    /** {@inheritDoc} */
    public void setup() {

	// Copy tagx to the project.
	createWebArtefacts();

	// Launch search for entity manager.
	launchEntityFilter();
    }
 
    /** {@inheritDoc} */
    public void createWebArtefacts() {

	copyDirectoryContents("tags/relations/*.tagx", getPathResolver()
		.getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/relations"));
	copyDirectoryContents(
		"tags/relations/decorators/*.tagx",
		getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP,
			"/WEB-INF/tags/relations/decorators"));
	copyDirectoryContents("tags/util/*.tagx", getPathResolver()
		.getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/util"));
    }

    /** TBC */
    public void launchEntityFilter() {
	FileDetails srcRoot = new FileDetails(new File(getPathResolver()
		.getRoot(Path.SRC_MAIN_JAVA)), null);
	String antPath = getPathResolver().getRoot(Path.SRC_MAIN_JAVA)
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
			
			// DiSiD: Roo uses now getMemberHoldingTypeDetails instead of getPhysicalTypeDetails to get typeDetails
//			|| ptm.getPhysicalTypeDetails() == null
//			|| !(ptm.getPhysicalTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
			|| ptm.getMemberHoldingTypeDetails() == null
			|| !(ptm.getMemberHoldingTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
		    
		    continue;
		}

		// DiSiD: Roo uses now getMemberHoldingTypeDetails instead of getPhysicalTypeDetails to get typeDetails
//		ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm
//			.getPhysicalTypeDetails();
		ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm.getMemberHoldingTypeDetails();
		
		if (Modifier.isAbstract(cid.getModifier())) {
		    continue;
		}

		// DiSiD: Roo uses now metadataService instead of scan all to get metadata
		MetadataItem em = metadataService.get(id);
		dependencyRegistry.notifyDownstream(em.getId());
//		Set<MetadataItem> metadata = itdMetadataScanner.getMetadata(id);
//		for (MetadataItem item : metadata) {
//		    if (item instanceof EntityMetadata) {
//			EntityMetadata em = (EntityMetadata) item;
//
//			dependencyRegistry.notifyDownstream(em.getId());
//		    }
//		}
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

	// DiSiD: Roo uses now UrlFindingUtils instead of TemplateUtils to find urls
//	Set<URL> urls = TemplateUtils.findMatchingClasspathResources(context
//		.getBundleContext(), path);
	Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(context.getBundleContext(), path);
	
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

    /**
     * Get and initialize the absolute path for {@code webmvc-config.xml}.
     * 
     * @return the absolute path to the file (never null)
     */
    public String getMvcConfigFile() {

      // resolve absolute path for menu.jspx if it hasn't been resolved yet
      return getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP,
            "/WEB-INF/spring/webmvc-config.xml");
    }

    // Private operations and utils -----

    /**
     * Utility to get {@link PathResolver} from {@link ProjectMetadata}.
     * <p>
     * This method will thrown if unavailable project metadata or
     * unavailable project path resolver.
     *  
     * @return PathResolver
     */
    private PathResolver getPathResolver() {
      ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
      .get(ProjectMetadata.getProjectIdentifier());
      Assert.notNull(projectMetadata, "Unable to obtain project metadata");
      
      // Use PathResolver to resolve between {@link File}, {@link Path} and
      // canonical path {@link String}s.
      // See {@link MavenPathResolver} to know location values
      PathResolver pathResolver = projectMetadata.getPathResolver();
      Assert.notNull(projectMetadata, "Unable to obtain path resolver");

      return pathResolver;
    }
}