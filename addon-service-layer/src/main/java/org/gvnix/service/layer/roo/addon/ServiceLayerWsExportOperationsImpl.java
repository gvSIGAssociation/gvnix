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
import org.gvnix.service.layer.roo.addon.annotations.*;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;

import com.sun.org.apache.xerces.internal.impl.XMLEntityManager.Entity;

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
public class ServiceLayerWsExportOperationsImpl implements
	ServiceLayerWsExportOperations {

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

    private static final Set<String> notAllowedCollectionTypes = new HashSet<String>();

    static {
	notAllowedCollectionTypes.add(Set.class.getName());
	notAllowedCollectionTypes.add(Map.class.getName());
	notAllowedCollectionTypes.add(HashMap.class.getName());
	notAllowedCollectionTypes.add(TreeMap.class.getName());
	notAllowedCollectionTypes.add(Vector.class.getName());
	notAllowedCollectionTypes.add(HashSet.class.getName());
    }

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

	// Checks correct namespace format.
	checkNamespaceFormat(targetNamespace);
	Assert
		.isTrue(
			checkNamespaceFormat(targetNamespace),
			"The namespace for Target Namespace has to start with 'http://'.\ni.e.: http://name.of.namespace/");

	// Namespace for the web service.
	targetNamespace = StringUtils.hasText(targetNamespace) ? targetNamespace
		: serviceLayerWsConfigService
		    .convertPackageToTargetNamespace(serviceClass.getPackage()
			    .toString());

	// Check address name not blank and set service name if not defined.
	addressName = StringUtils.hasText(addressName) ? StringUtils
		.capitalize(addressName) : serviceClass.getSimpleTypeName();

	// Define @GvNIXWebService annotation and attributes.
	// Check port type attribute name format and add attributes to a list.
	List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
	portTypeName = StringUtils.hasText(portTypeName) ? portTypeName
		: serviceName.concat("PortType");
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("name"), portTypeName));
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("targetNamespace"), targetNamespace));
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("serviceName"), serviceName));
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("address"), addressName));
	gvNixAnnotationAttributes.add(new StringAttributeValue(
		new JavaSymbolName("fullyQualifiedTypeName"), serviceClass
			.getFullyQualifiedTypeName()));
	annotationsService.addJavaTypeAnnotation(serviceClass,
		GvNIXWebService.class.getName(), gvNixAnnotationAttributes,
		false);

	// Installs jax2ws plugin in project.
	serviceLayerWsConfigService.installJaxwsBuildPlugin();

	// Add GvNixAnnotations to the project.
	annotationsService.addGvNIXAnnotationsDependency();
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void exportOperation(JavaType serviceClass,
	    JavaSymbolName methodName, String operationName, String resultName,
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
	Assert.isTrue(isMethodAvailableToExport(serviceClass, methodName,
		GvNIXWebMethod.class.getName()), "The method: '" + methodName
		+ " doesn't exists in the class '"
		+ serviceClass.getFullyQualifiedTypeName() + "'.");

	// Check authorized JavaTypes in operation.
	checkAuthorizedJavaTypesInOperation(serviceClass, methodName);

	// Check if method has return type.
	JavaType returnType = returnJavaType(serviceClass, methodName);

	Assert.isTrue(returnType != null, "The method: '" + methodName
		+ " doesn't exists in the class '"
		+ serviceClass.getFullyQualifiedTypeName() + "'.");

	if (returnType.equals(JavaType.VOID_OBJECT)
		|| returnType.equals(JavaType.VOID_PRIMITIVE)) {
	    resultName = null;
	} else if (!StringUtils.hasText(resultName)) {

	    resultName = "return";
	}

	// TODO: Check if method throws an Exception.
	// checkMethodExceptions(serviceClass, methodName);

	// Checks correct namespace format.
	Assert
		.isTrue(
			checkNamespaceFormat(resultNamespace),
			"The namespace for result has to start with 'http://'.\ni.e.: http://name.of.namespace/");
	Assert
		.isTrue(
			checkNamespaceFormat(requestWrapperNamespace),
			"The namespace for Request Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");
	Assert
		.isTrue(
			checkNamespaceFormat(responseWrapperNamespace),
			"The namespace for Response Wrapper has to start with 'http://'.\ni.e.: http://name.of.namespace/");

	// Create annotations to selected Method
	List<AnnotationMetadata> annotationMetadataUpdateList = getAnnotationsToExportOperation(
		serviceClass, methodName, operationName, resultName,
		resultNamespace, responseWrapperName, responseWrapperNamespace,
		requestWrapperName, requestWrapperNamespace);

	javaParserService.updateMethodAnnotations(serviceClass, methodName,
		annotationMetadataUpdateList);

    }

    /**
     * {@inheritDoc}
     * <p>
     * Add web services annotations to each founded exception.
     * </p>
     * <p>
     * Two types of exceptions and two ways to define annotations:
     * </p>
     * <ul>
     * <li>
     * Exceptions defined in the project.
     * <p>
     * Add @GvNIXWebFault annotation to Exception.
     * </p>
     * </li>
     * <li>
     * Exceptions imported into the project.
     * <p>
     * Add web service fault annotation using AspectJ template.
     * </p>
     * </li>
     * </ul>
     */
    public boolean checkMethodExceptions(JavaType serviceClass,
	    JavaSymbolName methodName) {

	MethodMetadata methodToCheck = javaParserService
		.getMethodByNameInClass(serviceClass, methodName);

	Assert.isTrue(methodToCheck != null, "The method: '" + methodName
		+ " doesn't exists in the class '"
		+ serviceClass.getFullyQualifiedTypeName() + "'.");

	List<JavaType> throwsTypes = methodToCheck.getThrowsTypes();

	String fileLocation;

	boolean extendsThrowable = true;

	for (JavaType throwType : throwsTypes) {

	    extendsThrowable = extendsThrowable(throwType);


	    Assert
		    .isTrue(
			    extendsThrowable,
			    "The '"
				    + throwType.getFullyQualifiedTypeName()
				    + "' class doesn't extend from 'java.lang.Throwable' in method '"
				    + methodName + "' from class '"
				    + serviceClass.getFullyQualifiedTypeName()
				    + "'.\nIt can't be used as Exception in method to be thrown.");

	    fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		    throwType.getFullyQualifiedTypeName().replace('.', '/')
			    .concat(".java"));

	    // Exception defined in the project or imported.
	    if (fileManager.exists(fileLocation)) {

		// Load class or interface details.
		// If class not found an exception will be raised.
		ClassOrInterfaceTypeDetails typeDetails = classpathOperations
			.getClassOrInterface(throwType);

		// Check and get mutable instance
		Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
			typeDetails, "Can't modify " + typeDetails.getName());
		MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) typeDetails;

		List<AnnotationAttributeValue<?>> gvNIXWebFaultAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
			new JavaSymbolName("name"), StringUtils
				.uncapitalize(throwType.getSimpleTypeName())));
		gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
			new JavaSymbolName("targetNamespace"), serviceLayerWsConfigService.convertPackageToTargetNamespace(throwType.getPackage().toString())));
		gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
			new JavaSymbolName("faultBean"), throwType.getFullyQualifiedTypeName()));

		// Define annotation.
		AnnotationMetadata gvNIXWebFaultAnnotation = new DefaultAnnotationMetadata(
			new JavaType(GvNIXWebFault.class.getName()),
			gvNIXWebFaultAnnotationAttributes);

		mutableTypeDetails.addTypeAnnotation(gvNIXWebFaultAnnotation);

	    } else {

		// TODO: If annotation is imported to project. Add to an AspectJ
		// file type annotation declaration.
	    }

	}

	return true;
    }

    /**
     * Check if throwType extends from 'java.lang.Throwable' class.
     * 
     * @param throwType
     * @return true if class extends from 'java.lang.Throwable' or false if is
     *         not a Throwable class.
     */
    private boolean extendsThrowable(JavaType throwType) {

	if (throwType.getFullyQualifiedTypeName().contentEquals(
		"java.lang.Throwable")) {
	    return true;
	}
	try {
	    Object exceptionToCheck = Class.forName(throwType
		    .getFullyQualifiedTypeName());

	    if (exceptionToCheck == null) {
		return false;
	    }

	    return extendsThrowable(new JavaType(exceptionToCheck.getClass()
		    .getSuperclass().getName()));

	} catch (ClassNotFoundException e) {
	    logger.log(Level.WARNING, "The exception class: '"
		    + throwType.getFullyQualifiedTypeName()
		    + "' doesn't exist.");
	    return false;
	}
    }

    /**
     * {@inheritDoc}
     * 
     * 
     * <p>
     * If exists any disallowed JavaType in operation:
     * </p>
     * <p>
     * Cancel all the process and show a message explaining that it's not
     * possible to publish this operation because the parameter can't be
     * Marshalled according Ws-I standards.
     * </p>
     */
    public void checkAuthorizedJavaTypesInOperation(JavaType serviceClass,
	    JavaSymbolName methodName) {

	MethodMetadata methodToCheck = javaParserService
		.getMethodByNameInClass(serviceClass, methodName);

	Assert.isTrue(methodToCheck != null, "The method: '" + methodName
		+ " doesn't exists in the class '"
		+ serviceClass.getFullyQualifiedTypeName() + "'.");

	// Check Return type
	JavaType returnType = methodToCheck.getReturnType();

	isJavaTypeAllowed(returnType, MethodParameterType.RETURN);

	// Check Input Parameters
	List<AnnotatedJavaType> inputParametersList = methodToCheck
		.getParameterTypes();

	for (AnnotatedJavaType annotatedJavaType : inputParametersList) {
	    isJavaTypeAllowed(annotatedJavaType.getJavaType(),
		    MethodParameterType.PARAMETER);
	}
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check allowed JavaType:
     * </p>
     * <ul>
     * <li>
     * Java basic types and basic objects.</li>
     * <li>
     * Project {@link Entity}. Adds @GvNIXXmlElement annotation to Entity.</li>
     * <li>
     * Collections that don't implement/extend: Map, Set, Tree.</li>
     * </ul>
     */
    public boolean isJavaTypeAllowed(JavaType javaType,
	    MethodParameterType methodParameterType) {

	// Exists
	if (javaType != null) {

	    // Check if javaType is a collection.
	    // Collection
	    List<JavaType> parameterList = javaType.getParameters();
	    if (!parameterList.isEmpty()) {

		// 1) yes - check if is allowed
		// Check if is not an allowed collection
		// 1.1) yes - recursive with its javaType.
		// 1.2) no - error.

		Assert
			.isTrue(
				isNotAllowedCollectionType(javaType) == false,
				"The '"
					+ methodParameterType
					+ "' type '"
					+ javaType.getFullyQualifiedTypeName()
					+ "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.\nThis is a disallowed collection.");

		// Check collection's parameter.
		boolean parameterAllowed = true;
		for (JavaType parameterJavaType : parameterList) {
		    parameterAllowed = parameterAllowed
			    && isJavaTypeAllowed(parameterJavaType,
				    methodParameterType);
		}
		return parameterAllowed;

	    }

	    // 2) no continue.

	    // Check if is primitive value.
	    if (javaType.isPrimitive()) {
		return true;
	    }

	    if (javaType.getFullyQualifiedTypeName().startsWith("java.lang")) {
		return true;
	    }

	    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		    .get(ProjectMetadata.getProjectIdentifier());

	    if (javaType.getFullyQualifiedTypeName().startsWith(
		    projectMetadata.getTopLevelPackage().toString())) {

		// MetadataID
		String targetId = PhysicalTypeIdentifier.createIdentifier(
			javaType, Path.SRC_MAIN_JAVA);

		// Obtain the physical type and itd mutable details
		PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
			.get(targetId);
		Assert.notNull(ptm, "Java source class doesn't exists.");

		PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
		Assert.notNull(ptd,
			"Java source code details unavailable for type "
				+ PhysicalTypeIdentifier
					.getFriendlyName(targetId));
		Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
			ptd, "Java source code is immutable for type "
				+ PhysicalTypeIdentifier
					.getFriendlyName(targetId));
		MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

		// Check if is a RooEntity
		AnnotationMetadata rooEntitynnotationMetadata = MemberFindingUtils
			.getTypeAnnotation(mutableTypeDetails, new JavaType(
				RooEntity.class.getName()));

		// TODO: Aunque no sea RooEntity añadir la anotación ?
		if (rooEntitynnotationMetadata != null) {

		    // Add @GvNIXXmlElement annotation.
		    List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

		    StringAttributeValue nameStringAttributeValue = new StringAttributeValue(
			    new JavaSymbolName("name"), StringUtils
				    .uncapitalize(javaType.getSimpleTypeName()));

		    annotationAttributeValueList.add(nameStringAttributeValue);

		    StringAttributeValue namespaceStringAttributeValue = new StringAttributeValue(
			    new JavaSymbolName("namespace"),
			    serviceLayerWsConfigService
				    .convertPackageToTargetNamespace(javaType
					    .getPackage().toString()));

		    annotationAttributeValueList
			    .add(namespaceStringAttributeValue);

		    annotationsService.addJavaTypeAnnotation(mutableTypeDetails
			    .getName(), GvNIXXmlElement.class.getName(),
			    annotationAttributeValueList, false);

		    return true;
		}
	    }

	}

	return true;
    }

    /**
     * Returns method return JavaType.
     * 
     * @param serviceClass
     *            where the method is defined.
     * @param methodName
     *            to search.
     * @return {@link JavaType}
     */
    private JavaType returnJavaType(JavaType serviceClass,
	    JavaSymbolName methodName) {

	JavaType returnType = new JavaType(JavaType.VOID_OBJECT.toString());

	MethodMetadata methodMetadata = javaParserService
		.getMethodByNameInClass(serviceClass, methodName);

	if (methodMetadata == null) {
	    return null;
	}

	if (methodMetadata.getReturnType() != null) {
	    returnType = methodMetadata.getReturnType();
	}

	return returnType;
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
	List<AnnotationAttributeValue<?>> annotationAttributeValueList;

	// org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod
	annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();
	AnnotationMetadata gvNIXWebMethod = new DefaultAnnotationMetadata(
		new JavaType(GvNIXWebMethod.class.getName()),
		annotationAttributeValueList);

	annotationMetadataList.add(gvNIXWebMethod);

	// javax.jws.WebMethod
	annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();
	operationName = StringUtils.hasText(operationName) ? operationName
		: methodName.getSymbolName();

	StringAttributeValue operationNameAttributeValue = new StringAttributeValue(
		new JavaSymbolName("operationName"), operationName);
	annotationAttributeValueList.add(operationNameAttributeValue);

	StringAttributeValue actionAttribuetValue = new StringAttributeValue(
		new JavaSymbolName("action"), "");
	annotationAttributeValueList.add(actionAttribuetValue);

	BooleanAttributeValue excludeAttribuetValue = new BooleanAttributeValue(
		new JavaSymbolName("exclude"), false);
	annotationAttributeValueList.add(excludeAttribuetValue);

	AnnotationMetadata webMethod = new DefaultAnnotationMetadata(
		new JavaType("javax.jws.WebMethod"),
		annotationAttributeValueList);

	annotationMetadataList.add(webMethod);

	// javax.xml.ws.RequestWrapper
	annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

	requestWrapperName = StringUtils.hasText(requestWrapperName) ? requestWrapperName
		: operationName;
	StringAttributeValue localNameAttributeValue = new StringAttributeValue(
		new JavaSymbolName("localName"), requestWrapperName);
	annotationAttributeValueList.add(localNameAttributeValue);

	requestWrapperNamespace = StringUtils.hasText(requestWrapperNamespace) ? requestWrapperNamespace
		: serviceLayerWsConfigService
			.convertPackageToTargetNamespace(serviceClass
				.getPackage().getFullyQualifiedPackageName());

	StringAttributeValue targetNamespaceAttributeValue = new StringAttributeValue(
		new JavaSymbolName("targetNamespace"), requestWrapperNamespace);
	annotationAttributeValueList.add(targetNamespaceAttributeValue);

	String className = serviceClass.getPackage()
		.getFullyQualifiedPackageName().concat(".").concat(
			StringUtils.capitalize(requestWrapperName).concat(
				"RequestWrapper"));
	StringAttributeValue classNameAttributeValue = new StringAttributeValue(
		new JavaSymbolName("className"), className);
	annotationAttributeValueList.add(classNameAttributeValue);

	AnnotationMetadata requestWrapper = new DefaultAnnotationMetadata(
		new JavaType("javax.xml.ws.RequestWrapper"),
		annotationAttributeValueList);

	annotationMetadataList.add(requestWrapper);

	// javax.xml.ws.ResponseWrapper
	annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

	responseWrapperName = StringUtils.hasText(responseWrapperName) ? responseWrapperName
		: operationName.concat("Response");

	localNameAttributeValue = new StringAttributeValue(new JavaSymbolName(
		"localName"), responseWrapperName);
	annotationAttributeValueList.add(localNameAttributeValue);

	responseWrapperNamespace = StringUtils
		.hasText(responseWrapperNamespace) ? responseWrapperNamespace
		: serviceLayerWsConfigService
			.convertPackageToTargetNamespace(serviceClass
				.getPackage().getFullyQualifiedPackageName());

	targetNamespaceAttributeValue = new StringAttributeValue(
		new JavaSymbolName("targetNamespace"), responseWrapperNamespace);
	annotationAttributeValueList.add(targetNamespaceAttributeValue);

	className = serviceClass.getPackage().getFullyQualifiedPackageName()
		.concat(".")
		.concat(StringUtils.capitalize(responseWrapperName));
	classNameAttributeValue = new StringAttributeValue(new JavaSymbolName(
		"className"), className);
	annotationAttributeValueList.add(classNameAttributeValue);

	AnnotationMetadata responseWrapper = new DefaultAnnotationMetadata(
		new JavaType("javax.xml.ws.ResponseWrapper"),
		annotationAttributeValueList);

	annotationMetadataList.add(responseWrapper);

	// javax.jws.WebResult
	// Check result value
	if (resutlName != null) {
	    annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

	    localNameAttributeValue = new StringAttributeValue(
		    new JavaSymbolName("name"), resutlName);
	    annotationAttributeValueList.add(localNameAttributeValue);

	    resultNamespace = StringUtils.hasText(resultNamespace) ? resultNamespace
		    : serviceLayerWsConfigService
			    .convertPackageToTargetNamespace(serviceClass
				    .getPackage()
				    .getFullyQualifiedPackageName());

	    targetNamespaceAttributeValue = new StringAttributeValue(
		    new JavaSymbolName("targetNamespace"), resultNamespace);
	    annotationAttributeValueList.add(targetNamespaceAttributeValue);

	    BooleanAttributeValue headerAttributeValue = new BooleanAttributeValue(
		    new JavaSymbolName("header"), false);
	    annotationAttributeValueList.add(headerAttributeValue);

	    StringAttributeValue partNameAttributeValue = new StringAttributeValue(
		    new JavaSymbolName("partName"), "parameters");

	    annotationAttributeValueList.add(partNameAttributeValue);

	    AnnotationMetadata webResult = new DefaultAnnotationMetadata(
		    new JavaType("javax.jws.WebResult"),
		    annotationAttributeValueList);

	    annotationMetadataList.add(webResult);
	} else {
	    // @Oneway - not require a response from the service.
	    AnnotationMetadata oneway = new DefaultAnnotationMetadata(
		    new JavaType("javax.jws.Oneway"),
		    new ArrayList<AnnotationAttributeValue<?>>());
	    annotationMetadataList.add(oneway);
	}

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
     * {@inheritDoc}
     * 
     * <p>
     * Check if method exists in the class.
     * </p>
     */
    public boolean isMethodAvailableToExport(JavaType serviceClass,
	    JavaSymbolName methodName, String annotationName) {

	boolean exists = true;
	MethodMetadata methodMetadata = javaParserService
		.getMethodByNameInClass(serviceClass, methodName);

	if (methodMetadata == null) {
	    return false;
	}

	exists = javaParserService.isAnnotationIntroducedInMethod(
		GvNIXWebMethod.class.getName(), methodMetadata);
	Assert
		.isTrue(
			exists == false,
			"The method '"
				+ methodName
				+ "' has been annotated with @"
				+ annotationName
				+ " before, you could update annotation parameters inside its class.");

	return true;
    }

    /**
     * Checks correct namespace URI format. Suffix 'http://'.
     * 
     * <p>
     * If String is blank is also correct.
     * </p>
     * 
     * @param namespace
     *            string to check as correct namespace.
     * 
     * @return true if is blank or if has correct URI format.
     */
    private boolean checkNamespaceFormat(String namespace) {
	if (StringUtils.hasText(namespace)) {
	    return StringUtils.startsWithIgnoreCase(namespace, "http://");
	}
	return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The list of not allowed collections is defined as static in this class.
     * </p>
     * <p>
     * Not allow sorted collections.
     * </p>
     * <ul>
     * <li>Set</li>
     * <li>Map</li>
     * <li>TreeMap</li>
     * <li>Vector</li>
     * <li>HashSet</li>
     * </ul>
     */
    public boolean isNotAllowedCollectionType(JavaType javaType) {
	return notAllowedCollectionTypes.contains(javaType
		.getFullyQualifiedTypeName());
    }

}
