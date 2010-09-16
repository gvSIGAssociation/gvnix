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
import org.springframework.roo.classpath.javaparser.details.JavaParserAnnotationMetadata;
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
	    String portTypeName, String targetNamespace, String addressName) {

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
	serviceName = StringUtils.hasText(serviceName) ? serviceName
		: serviceClass.getSimpleTypeName();

	// Namespace for the web service.
	if (StringUtils.hasText(targetNamespace)) {
	    Assert
		    .isTrue(
			    StringUtils.startsWithIgnoreCase(targetNamespace,
				    "http://"),
			    "The namespace has to start with 'http://' and end with '/'.\ni.e.: http://name.of.namespace/");

	} else {
	    targetNamespace = serviceLayerWsConfigService
		    .convertPackageToTargetNamespace(serviceClass.getPackage()
			    .toString());
	}

	// Check if address name value is not blank and set service name if
	// isn't defined.
	addressName = StringUtils.hasText(addressName) ? StringUtils
		.capitalize(addressName) : serviceClass.getSimpleTypeName();

	// Define Web Service Annotations.
	updateClassAsWebService(serviceClass, serviceName, portTypeName,
		targetNamespace);

	// Update CXF XML
	serviceLayerWsConfigService.exportClass(serviceClass, serviceName,
		addressName);

	// Define Jax-WS plugin and creates and execution build for this service
	// to generate the wsdl file to check errors before deploy.
	serviceLayerWsConfigService.jaxwsBuildPlugin(serviceClass, serviceName,
		addressName);

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

	// Check if serviceClass is a Web Service. If doesn't exist shows an
	// error.
	if (!isWebServiceClass(serviceClass)) {
	    // Export as a service.
	    exportService(serviceClass, null, null, null, null);
	}

	// Check if method exists in the class.
	Assert.isTrue(isMethodAvailableToExport(serviceClass, methodName),
		"The method: '" + methodName + " doesn't exists in the class '"
			+ serviceClass.getFullyQualifiedTypeName() + "'.");

	// TODO: Create annotations to selected Method
	List<AnnotationMetadata> annotationMetadataUpdateList = getAnnotationsToExportOperation(
		serviceClass, methodName, operationName, resutlName,
		resultNamespace, responseWrapperName, responseWrapperNamespace,
		requestWrapperName, requestWrapperNamespace);

	javaParserService.updateMethodAnnotations(serviceClass, methodName,
		annotationMetadataUpdateList);

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * If the values are not set, define them using WS-i standard names.
     * </p>
     * <p>
     * Annotations to create:
     * </p>
     * <ul>
     * <li>@GvNIXWebMethod()</li>
     * <li>@WebMethod(operationName = "operationName", action = "", exclude =
     * false)</li>
     * <li>@RequestWrapper(localName = "requestWrapperName", targetNamespace =
     * "requestWrapperNamespace", className = "")</li>
     * <li>@ResponseWrapper(localName = "responseWrapperName", targetNamespace =
     * "responseWrapperNamespace", className = "")</li>
     * <li>@WebResult(name = "resutlName", targetNamespace = "resultNamespace",
     * header = false, partName = "parameters")</li>
     * </ul>
     */
    public List<AnnotationMetadata> getAnnotationsToExportOperation(
	    JavaType serviceClass, JavaSymbolName methodName,
	    String operationName, String resutlName, String resultNamespace,
	    String responseWrapperName, String responseWrapperNamespace,
	    String requestWrapperName, String requestWrapperNamespace) {

	List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();

	// org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod
	AnnotationMetadata gvNIXWebMethod = new DefaultAnnotationMetadata(
		new JavaType(
			"org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod"),
		new ArrayList<AnnotationAttributeValue<?>>());

	annotationMetadataList.add(gvNIXWebMethod);

	// // javax.jws.WebMethod
	// AnnotationMetadata webMethod = new DefaultAnnotationMetadata(
	// new JavaType("javax.jws.WebMethod"),
	// new ArrayList<AnnotationAttributeValue<?>>());
	//
	// annotationMetadataList.add(webMethod);
	//
	// // javax.xml.ws.RequestWrapper
	// AnnotationMetadata requestWrapper = new DefaultAnnotationMetadata(
	// new JavaType("javax.xml.ws.RequestWrapper"),
	// new ArrayList<AnnotationAttributeValue<?>>());
	//
	// annotationMetadataList.add(requestWrapper);
	//
	// // javax.xml.ws.ResponseWrapper
	// AnnotationMetadata responseWrapper = new DefaultAnnotationMetadata(
	// new JavaType("javax.xml.ws.ResponseWrapper"),
	// new ArrayList<AnnotationAttributeValue<?>>());
	//
	// annotationMetadataList.add(responseWrapper);
	//
	// // javax.jws.WebResult
	// AnnotationMetadata webResult = new DefaultAnnotationMetadata(
	// new JavaType("javax.jws.WebResult"),
	// new ArrayList<AnnotationAttributeValue<?>>());
	// annotationMetadataList.add(webResult);

	return annotationMetadataList;
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
    private boolean isWebServiceClass(JavaType serviceClass) {
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

	if (serviceLayerWSExportMetadata == null) {
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Check if the method methodName exists in serviceClass and is not
     * annotated before with @GvNIXWebMethod.
     * <p>
     * TODO: Check if method exists in the class.
     * </p>
     * 
     * @param serviceClass
     * @param methodName
     * @return
     */
    private boolean isMethodAvailableToExport(JavaType serviceClass,
	    JavaSymbolName methodName) {

	// Load class details. If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails tmpServiceDetails = classpathOperations
		.getClassOrInterface(serviceClass);

	// Checks if it's mutable
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		tmpServiceDetails, "Can't modify " + tmpServiceDetails.getName());

	MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;
	
	List<? extends MethodMetadata> methodList = serviceDetails.getDeclaredMethods();
	List<AnnotationMetadata> annotationMethodList;

	for (MethodMetadata methodMetadata : methodList) {
	    if (methodMetadata.getMethodName().equals(methodName)) {

		annotationMethodList = methodMetadata.getAnnotations();
		for (AnnotationMetadata annotationMetadata : annotationMethodList) {
		    Assert
			    .isTrue(
				    annotationMetadata
					    .getAnnotationType()
					    .getFullyQualifiedTypeName()
					    .compareTo(
						    "org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod") != 0,
				    "The method '"
					    + methodName
					    + "' has been annotated with @GvNIXWebMethod before, you could update annotation parameters inside its class.");
		}
		return true;
	    }
	}

	return false;
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
     * @param portTypeName
     *            Name to define the portType.
     * @param targetNamespace
     *            Namespace name for the service.
     */
    private void updateClassAsWebService(JavaType serviceClass,
	    String serviceName, String portTypeName, String targetNamespace) {

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
	Assert
		.isTrue(
			!javaParserService
				.isAnnotationIntroduced(
					"org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService",
					serviceDetails),
			"The annotation @GvNIXWebService can't be updated yet with service command.");

	// @Service and @GvNIXWebService annotation.
	AnnotationMetadata gvNixWebServiceAnnotation = null;

	// @GvNIXWebService Annotation attributes.
	List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

	// Checks name parameter to define PortType.
	portTypeName = StringUtils.hasText(portTypeName) ? portTypeName
		: serviceName.concat("PortType");

	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("name"), portTypeName));

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
