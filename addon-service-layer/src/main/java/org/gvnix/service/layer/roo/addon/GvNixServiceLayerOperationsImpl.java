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
package org.gvnix.service.layer.roo.addon;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class GvNixServiceLayerOperationsImpl implements
	GvNixServiceLayerOperations {

    private static Logger logger = Logger
	    .getLogger(GvNixServiceLayerOperations.class.getName());

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

    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.GvNixServiceLayerOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
	if (getPathResolver() == null) {
	    return false;
	}

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");

	if (!fileManager.exists(webXmlPath)) {
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

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks these types:
     * </p>
     * <ul>
     * <li>
     * Cxf Dependencies in pom.xml.</li>
     * <li>
     * Cxf configuration file exists.</li>
     * </ul>
     * 
     * dependencies installed </p>
     */
    public boolean isCxfInstalled() {

	boolean cxfConfigFileExists = isCxfConfigurated();

	boolean cxfDependeciesExists = isCxfDependencyInstalled();

	return cxfConfigFileExists && cxfDependeciesExists;
    }

    /**
     * <p>
     * Chekc if Cxf config file is created in the project.
     * </p>
     * 
     * @return true or false if exists Cxf configuration file.
     */
    private boolean isCxfConfigurated() {

	String prjId = ProjectMetadata.getProjectIdentifier();
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(prjId);
	String prjName = projectMetadata.getProjectName();

	String cxfFile = "WEB-INF/cxf-".concat(prjName).concat(".xml");

	// Checks for src/main/webapp/WEB-INF/cxf-PROJECT_ID.xml
	String cxfXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		cxfFile);

	boolean cxfInstalled = fileManager.exists(cxfXmlPath);

	return cxfInstalled;
    }

    /**
     * <p>
     * Check if Cxf dependencies are set in project's pom.xml.
     * </p>
     * 
     * @return true or false are Cxf dependcies set.
     */
    private boolean isCxfDependencyInstalled() {

	boolean cxfDependeciesExists;

	ProjectMetadata project = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (project == null) {
	    return false;
	}

	// Only permit installation if they don't already have the version of
	// CXF installed.
	cxfDependeciesExists = project.isDependencyRegistered(new Dependency(
		"org.apache.cxf", "cxf-rt-core", "2.2.6"));

	return cxfDependeciesExists;
    }
}