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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;

/**
 * Addon for Handle Web Service Proxy Layer
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerWsImportOperationsImpl implements ServiceLayerWsImportOperations {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWsImportOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private MetadataService metadataService;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;

    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.GvNixServiceLayerOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {

	return getPathResolver() != null;
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
     * If the class to import the web service doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     * 
     * @param serviceClass
     */
    public void importService(JavaType serviceClass, String url) {

	// Checks if Cxf is configured in the project and installs it if it's
	// not available.
	serviceLayerWsConfigService.setUp();

	String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		serviceClass.getFullyQualifiedTypeName().replace('.', '/')
			.concat(".java"));

	if (!fileManager.exists(fileLocation)) {
	    logger.log(Level.INFO, "Crea la nueva clase de servicio: "
		    + serviceClass.getSimpleTypeName()
		    + " para publicarla como servicio web.");
	    // Create service class with Service Annotation.
//	    createServiceClass(serviceClass);

	}
	
	// TODO Develop method

//	// Define Web Service Annotations.
//	updateClassAsWebService(serviceClass);
//
//	// Update CXF XML
//	updateCxfXml(serviceClass);
//
//	// Add GvNixAnnotations to the project.
//	addGvNIXAnnotationsDependecy();

    }

}
