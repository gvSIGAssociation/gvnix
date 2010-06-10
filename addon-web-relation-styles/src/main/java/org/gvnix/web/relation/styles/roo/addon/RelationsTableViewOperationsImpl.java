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
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
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

    private ComponentContext context;

    protected void activate(ComponentContext context) {
	this.context = context;
    }

    public boolean isProjectAvailable() {
	return getPathResolver() != null;
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
	if (!isProjectAvailable()) {
	    return false;
	}

	if (!fileManager.exists(pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP,
		"WEB-INF/tags/util/gvnixcallfunction.tagx"))) {
	    return false;
	}
	return true;
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }
}