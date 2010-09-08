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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;

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
public class ServiceLayerWsExportOperationsImpl implements ServiceLayerWsExportOperations {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWsExportOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private JavaParserService javaParserService;
    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.ServiceLayerWsExportOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {

	if (getPathResolver() == null) {

	    return false;
	}

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"/WEB-INF/web.xml");
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
     * If the class to export as web service doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     * 
     * @param serviceClass
     */
    public void exportService(JavaType serviceClass) {

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
	    javaParserService.createServiceClass(serviceClass);

	}

	// Define Web Service Annotations.
	updateClassAsWebService(serviceClass);

	// Update CXF XML
	serviceLayerWsConfigService.updateCxfXml(serviceClass);

	// Add GvNixAnnotations to the project.
	serviceLayerWsConfigService.addGvNIXAnnotationsDependecy();
    }

    /**
     * Update an existing class to a web service.
     * 
     * <p>
     * Adds @GvNIXWebService annotation to the class.
     * </p>
     * 
     * @param serviceClass
     *            class to be published as Web Service.
     */
    private void updateClassAsWebService(JavaType serviceClass) {

	// Load class details. If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails tmpServiceDetails = classpathOperations
		.getClassOrInterface(serviceClass);

	// Checks if it's mutable
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		tmpServiceDetails, "Can't modify " + tmpServiceDetails.getName());

	MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;

	List<? extends AnnotationMetadata> serviceAnnotations = serviceDetails
		.getTypeAnnotations();

	// @Service and @GvNIXWebService annotation.
	AnnotationMetadata gvNixWebServiceAnnotation = null;

	// @GvNIXWebService Annotation attributes.
	List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("name"), serviceClass.getSimpleTypeName().concat("PortType")));

	// TODO: Crear namespace a la inversa del nombre del paquete de la
	// clase.
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("targetNamespace"), "http://".concat(serviceClass.getPackage().toString()).concat("/")));

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("serviceName"), serviceClass.getSimpleTypeName()));

	for (AnnotationMetadata tmpAnnotationMetadata : serviceAnnotations) {

	    if (tmpAnnotationMetadata.getAnnotationType()
		    .getFullyQualifiedTypeName().equals(
			    GvNIXWebService.class.getName())) {

		serviceDetails.removeTypeAnnotation(new JavaType(
			GvNIXWebService.class.getName()));
	    }

	}

	// Define GvNIXWebService annotation.
	gvNixWebServiceAnnotation = new DefaultAnnotationMetadata(new JavaType(
		GvNIXWebService.class.getName()), gvNixAnnotationAttributes);

	// Adds GvNIXEntityOCCChecksum to the entity
	serviceDetails.addTypeAnnotation(gvNixWebServiceAnnotation);
    }

}
