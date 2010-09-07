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

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Element;

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
public class WebServiceLayerOperationsImpl implements WebServiceLayerOperations {

    private static Logger logger = Logger
	    .getLogger(WebServiceLayerOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private ServiceLayerUtils serviceLayerUtils;
    @Reference
    private WebServiceLibraryUtils webServiceLibraryUtils;

    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.WebServiceLayerOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
	return serviceLayerUtils.isProjectAvailable();
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
	webServiceLibraryUtils.setUp();

	String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		serviceClass.getFullyQualifiedTypeName().replace('.', '/')
			.concat(".java"));

	if (!fileManager.exists(fileLocation)) {
	    logger.log(Level.INFO, "Crea la nueva clase de servicio: "
		    + serviceClass.getSimpleTypeName()
		    + " para publicarla como servicio web.");
	    // Create service class with Service Annotation.
	    createServiceClass(serviceClass);

	}

	// Define Web Service Annotations.
	updateClassAsWebService(serviceClass);

	// Update CXF XML
	webServiceLibraryUtils.updateCxfXml(serviceClass);

	// Add GvNixAnnotations to the project.
	addGvNIXAnnotationsDependecy();

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds @org.springframework.stereotype.Service annotation to the class.
     * </p>
     */
    public void createServiceClass(JavaType serviceClass) {

	// Service class
	String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
		serviceClass, Path.SRC_MAIN_JAVA);

	// Service annotations
	List<AnnotationMetadata> serviceAnnotations = new ArrayList<AnnotationMetadata>();
	serviceAnnotations.add(new DefaultAnnotationMetadata(new JavaType(
		"org.springframework.stereotype.Service"),
		new ArrayList<AnnotationAttributeValue<?>>()));

	ClassOrInterfaceTypeDetails serviceDetails = new DefaultClassOrInterfaceTypeDetails(
		declaredByMetadataId, serviceClass, Modifier.PUBLIC,
		PhysicalTypeCategory.CLASS, null, null, null, null, null,
		null, serviceAnnotations, null);

	classpathOperations.generateClassFile(serviceDetails);

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds @GvNIXWebService annotation to the class.
     * </p>
     * 
     */
    public void updateClassAsWebService(JavaType serviceClass) {

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
	return webServiceLibraryUtils.isCxfConfigurated();
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.WebServiceLayerOperations#
     * addGvNIXAnnotationsDependecy()
     */
    public void addGvNIXAnnotationsDependecy() {

	List<Element> projectProperties = XmlUtils.findElements(
		"/configuration/gvnix/properties/*", XmlUtils.getConfiguration(
			this.getClass(), "properties.xml"));
	for (Element property : projectProperties) {
	    projectOperations.addProperty(new Property(property));
	}

	List<Element> databaseDependencies = XmlUtils.findElements(
		"/configuration/gvnix/dependencies/dependency", XmlUtils
			.getConfiguration(this.getClass(),
				"gvnix-annotation-dependencies.xml"));
	for (Element dependencyElement : databaseDependencies) {
	    projectOperations
		    .dependencyUpdate(new Dependency(dependencyElement));
	}
    }

}
