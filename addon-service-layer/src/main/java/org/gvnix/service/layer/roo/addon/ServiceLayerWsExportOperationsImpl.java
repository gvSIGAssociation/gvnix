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
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
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
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    
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
     */
    public void exportService(JavaType serviceClass, String serviceName,
	    String name, String targetNamespace) {

	// Checks if Cxf is configured in the project and installs it if it's
	// not available.
	serviceLayerWsConfigService.install(CommunicationSense.EXPORT);

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

	// Checks serviceName parameter to publish the web service.
	serviceName = StringUtils.hasText(serviceName) ? StringUtils
		.capitalize(serviceName) : serviceClass.getSimpleTypeName();

	// Namespace for the web service.
	if (StringUtils.hasText(targetNamespace)) {
	    Assert
		    .isTrue(
			    StringUtils.startsWithIgnoreCase(targetNamespace,
				    "http://"),
			    "The namespace has to start with 'http://' and end with '/'.\ni.e.: http://name.of.namespace/");

	    // Adds '/' if is not defined in targetNamespace.
	    targetNamespace = targetNamespace.endsWith("/") ? targetNamespace
		    : targetNamespace.concat("/");

	} else {
	    targetNamespace = serviceLayerWsConfigService
		    .convertPackageToTargetNamespace(serviceClass.getPackage()
			    .toString());
	}

	// Define Web Service Annotations.
	updateClassAsWebService(serviceClass, serviceName, name,
		targetNamespace);

	// Update CXF XML
	serviceLayerWsConfigService.exportClass(serviceClass, serviceName);

	// Define Jax-WS plugin and creates and execution build for this service
	// to generate the wsdl file to check errors before deploy.
	serviceLayerWsConfigService.jaxwsBuildPlugin(serviceClass, serviceName);

	// Add GvNixAnnotations to the project.
	annotationsService.addGvNIXAnnotationsDependency();
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void exportOperation(JavaType serviceClass,
	    JavaSymbolName methodName, String operationName, String resutlName,
	    String resultNamespace, String responseWrapperName,
	    String responseWrapperNamespace, String requestWrapperName,
	    String requestWrapperNamespace) {

	Assert.notNull(serviceClass, "Java type required");
	Assert.notNull(methodName, "Operation name required");

	Assert.isTrue(isWebService(serviceClass));
    }

    /**
     * Checks if the selected class exists and contains
     * {@link ServiceLayerWSExportMetadata}.
     * 
     * @param serviceClass
     *            class to be checked.
     * @return true if the {@link JavaType} contains
     *         {@link ServiceLayerWSExportMetadata}.
     */
    private boolean isWebService(JavaType serviceClass) {
	String id = physicalTypeMetadataProvider.findIdentifier(serviceClass);

	Assert.notNull(id, "Cannot locate source for '"
		+ serviceClass.getFullyQualifiedTypeName() + "'");

	// Go and get the service layer ws metadata to export selected method.
	JavaType javaType = PhysicalTypeIdentifier.getJavaType(id);
	Path path = PhysicalTypeIdentifier.getPath(id);
	String entityMid = ServiceLayerWSExportMetadata.createIdentifier(
		javaType, path);

	// Get the service layer ws metadata.
	ServiceLayerWSExportMetadata serviceLayerWSExportMetadata = (ServiceLayerWSExportMetadata) metadataService
		.get(entityMid);
	Assert.notNull(serviceLayerWSExportMetadata,
		"Cannot export operation because '"
			+ serviceClass.getFullyQualifiedTypeName()
			+ "' is not a Web Service.");
	return true;
    }

    /**
     * Update an existing class to a web service.
     * 
     * <p>
     * Adds @GvNIXWebService annotation to the class.
     * </p>
     * 
     * @param serviceClass
     *            class to export.
     * @param serviceName
     *            Name to publish the Web Service.
     * @param name
     *            Name to define the portType.
     * @param targetNamespace
     *            Namespace name for the service.
     */
    private void updateClassAsWebService(JavaType serviceClass,
	    String serviceName, String name, String targetNamespace) {

	// Load class details. If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails tmpServiceDetails = classpathOperations
		.getClassOrInterface(serviceClass);

	// Checks if it's mutable
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		tmpServiceDetails, "Can't modify " + tmpServiceDetails.getName());

	MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;

	List<? extends AnnotationMetadata> serviceAnnotations = serviceDetails
		.getTypeAnnotations();
	
	// Checks if is @GvNIXWebService annotation defined.
	// TODO: The annotation can't be updated yet.
	for (AnnotationMetadata annotationMetadata : serviceAnnotations) {
	    if (annotationMetadata.getAnnotationType()
		    .getFullyQualifiedTypeName().equals(
			    GvNIXWebService.class.getName())) {
		return;
	    }
	}

	// @Service and @GvNIXWebService annotation.
	AnnotationMetadata gvNixWebServiceAnnotation = null;

	// @GvNIXWebService Annotation attributes.
	List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

	// Checks name parameter to define PortType.
	name = StringUtils.hasText(name) ? StringUtils.capitalize(name)
		: serviceName;

	// Checks if name ends with PortType to support web services
	// interoperability.
	name = StringUtils.endsWithIgnoreCase(name, "PortType") ? name : name
		.concat("PortType");

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("name"), name));

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("targetNamespace"), targetNamespace));

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("serviceName"), serviceName));

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
