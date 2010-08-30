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

import java.io.InputStream;
import java.util.List;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Service
@Component(immediate = true)
public class ServiceLayerActivationInfoImpl implements
	ServiceLayerActivationInfo {

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if exists Cxf config file using project name.
     * </p>
     * 
     * @return true or false if exists Cxf configuration file.
     */
    public boolean isCxfConfigurated() {

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
     * {@inheritDoc}
     * 
     * <p>
     * Search if the dependencies defined in xml Addon file dependencies.xml are
     * set in pom.xml.
     * </p>
     * 
     * @return true if all dependecies are set in pom.xml.
     */
    public boolean areCxfDependenciesInstalled() {

	boolean cxfDependenciesExists = true;

	ProjectMetadata project = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (project == null) {
	    return false;
	}

	// Dependencies elements are defined as:
	// <dependency org="org.apache.cxf" name="cxf-rt-bindings-soap"
	// rev="2.2.6" />
	List<Element> cxfDependenciesList = getCxfDependencies();

	Dependency cxfDependency;

	for (Element element : cxfDependenciesList) {

	    cxfDependency = new Dependency(element);
	    cxfDependenciesExists = cxfDependenciesExists
		    && project.isDependencyRegistered(cxfDependency);
	}

	return cxfDependenciesExists;
    }

    /* (non-Javadoc)
     * @see org.gvnix.service.layer.roo.addon.ServiceLayerActivationInfo#isProjectAvailable()
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
     * <p>
     * Get addon dependencies defined in dependencies.xml
     * </p>
     * 
     * @return List of addon dependencies as xml elements.
     */
    public List<Element> getCxfDependencies() {
	InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
		"dependencies.xml");
	Assert.notNull(templateInputStream,
		"Could not acquire dependencies.xml file");
	Document dependencyDoc;
	try {
	    dependencyDoc = XmlUtils.getDocumentBuilder().parse(
		    templateInputStream);
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}

	Element dependencies = (Element) dependencyDoc.getFirstChild();

	return XmlUtils.findElements("/dependencies/cxf/dependency",
		dependencies);

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
