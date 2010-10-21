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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;


import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.javaparser.JavaParserMutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.javaparser.JavaParserUtils;
import org.springframework.roo.classpath.javaparser.details.JavaParserMethodMetadata;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class JavaParserServiceImpl implements JavaParserService {

    private static Logger logger = Logger
    .getLogger(JavaParserServiceImpl.class.getName());

    @Reference
    private MetadataService metadataService;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private PathResolver pathResolver;

    
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
     * Updates the class with the new method created with selected attributes.
     * </p>
     */
    public void createMethod(JavaSymbolName methodName, JavaType returnType,
	    JavaType targetType, int modifier, List<JavaType> throwsTypes,
	    List<AnnotationMetadata> annotationList,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body) {

	Assert.notNull(paramTypes, "Param type mustn't be null");
	Assert.notNull(paramNames, "Param name mustn't be null");

	// MetadataID
	String targetId = PhysicalTypeIdentifier.createIdentifier(targetType,
		Path.SRC_MAIN_JAVA);

	// Obtain the physical type and itd mutable details
	PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
		.get(targetId);
	Assert.notNull(ptm, "Java source class doesn't exists.");

	PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
	Assert.notNull(ptd, "Java source code details unavailable for type "
		+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, ptd,
		"Java source code is immutable for type "
			+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

	// create method
	MethodMetadata operationMetadata = new DefaultMethodMetadata(targetId,
		modifier, methodName,
		(returnType == null ? JavaType.VOID_PRIMITIVE : returnType),
		paramTypes, paramNames, annotationList, throwsTypes, body);
	mutableTypeDetails.addMethod(operationMetadata);

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds Web Service annotation to selected method.
     * </p>
     */
    public void updateMethodAnnotations(JavaType className,
            JavaSymbolName method,
            List<AnnotationMetadata> annotationMetadataUpdateList,
            List<AnnotatedJavaType> annotationWebParamMetadataList) {

	// MetadataID
	String targetId = PhysicalTypeIdentifier.createIdentifier(className,
		Path.SRC_MAIN_JAVA);

	// Obtain the physical type and itd mutable details
	PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
		.get(targetId);
	PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
	Assert.notNull(ptd, "Java source code details unavailable for type "
		+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	Assert.isInstanceOf(JavaParserMutableClassOrInterfaceTypeDetails.class,
		ptd, "Java source code is immutable for type "
			+ PhysicalTypeIdentifier.getFriendlyName(targetId));

	JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails = (JavaParserMutableClassOrInterfaceTypeDetails) ptd;

	// Update method
	List<? extends MethodMetadata> methodsList = mutableTypeDetails
		.getDeclaredMethods();

	JavaParserMethodMetadata javaParserMethodMetadata;

	List<MethodMetadata> updatedMethodList = new ArrayList<MethodMetadata>();
	List<AnnotationMetadata> methodAnnotationList = new ArrayList<AnnotationMetadata>();
	
	MethodMetadata operationMetadata;

	for (MethodMetadata methodMetadata : methodsList) {

	    javaParserMethodMetadata = (JavaParserMethodMetadata) methodMetadata;

	    if (methodMetadata.getMethodName().toString().compareTo(
		    method.toString()) == 0) {

		Assert.isTrue(!isAnnotationIntroducedInMethod(
			GvNIXWebMethod.class.getName(), methodMetadata),
			"The method '" + method.toString()
				+ "' has been annotated with '@"
				+ GvNIXWebMethod.class.getName() + "' before.");

		methodAnnotationList.addAll(javaParserMethodMetadata
			.getAnnotations());
		methodAnnotationList.addAll(annotationMetadataUpdateList);

		operationMetadata = new DefaultMethodMetadata(targetId,
			javaParserMethodMetadata.getModifier(),
			javaParserMethodMetadata.getMethodName(),
			javaParserMethodMetadata.getReturnType(),
                        annotationWebParamMetadataList,
			javaParserMethodMetadata.getParameterNames(),
			methodAnnotationList,
			javaParserMethodMetadata.getThrowsTypes(),
			javaParserMethodMetadata.getBody()
				.substring(
					javaParserMethodMetadata.getBody()
						.indexOf("{") + 1,
					javaParserMethodMetadata.getBody()
						.indexOf("}")));

	    } else {
		operationMetadata = new DefaultMethodMetadata(targetId,
			javaParserMethodMetadata.getModifier(),
			javaParserMethodMetadata.getMethodName(),
			javaParserMethodMetadata.getReturnType(),
			javaParserMethodMetadata.getParameterTypes(),
			javaParserMethodMetadata.getParameterNames(),
			javaParserMethodMetadata.getAnnotations(),
			javaParserMethodMetadata.getThrowsTypes(),
			javaParserMethodMetadata.getBody()
				.substring(
					javaParserMethodMetadata.getBody()
						.indexOf("{") + 1,
					javaParserMethodMetadata.getBody()
						.indexOf("}")));

	    }
	    updatedMethodList.add(operationMetadata);

	}

	List<ConstructorMetadata> contructorList = new ArrayList<ConstructorMetadata>();
	contructorList.addAll(mutableTypeDetails.getDeclaredConstructors());

	List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
	fieldMetadataList.addAll(mutableTypeDetails.getDeclaredFields());

	List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();
	annotationMetadataList.addAll(mutableTypeDetails.getTypeAnnotations());

	// Replicates the values from the original class.
	ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = new DefaultClassOrInterfaceTypeDetails(
		mutableTypeDetails.getDeclaredByMetadataId(),
		mutableTypeDetails.getName(),
		mutableTypeDetails.getModifier(),
		mutableTypeDetails.getPhysicalTypeCategory(),
		contructorList,
		fieldMetadataList,
		updatedMethodList,
		mutableTypeDetails.getSuperclass(),
		mutableTypeDetails.getExtendsTypes(),
		mutableTypeDetails.getImplementsTypes(),
		annotationMetadataList,
		(mutableTypeDetails.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION) ? mutableTypeDetails
			.getEnumConstants()
			: null);

	// Updates the class in file system.
	updateClass(classOrInterfaceTypeDetails);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Updates method using 'CompilationUnit'
     * </p>
     * 
     * <p>
     * TODO: Method to improve. Unused.
     * </p>
     */
    @Deprecated
    public void updateWithJavaDoc(JavaType className, JavaSymbolName method,
	    String paramName, JavaType paramType) throws ParseException {

	String targetId = PhysicalTypeIdentifier.createIdentifier(className,
		Path.SRC_MAIN_JAVA);

	String javaIdentifier = physicalTypeMetadataProvider
		.findIdentifier(className);
	javaIdentifier = javaIdentifier.substring(
		javaIdentifier.indexOf("?") + 1).replace('.', '/');

	String fileIdentifier = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		javaIdentifier.concat(".java"));

	// Retrieve class file to update.
	CompilationUnit compilationUnit;

	compilationUnit = JavaParser.parse(fileManager
		.getInputStream(fileIdentifier));

	// Obtain the physical type and itd mutable details
	PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
		.get(targetId);
	PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
	Assert.notNull(ptd, "Java source code details unavailable for type "
		+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	Assert.isInstanceOf(JavaParserMutableClassOrInterfaceTypeDetails.class,
		ptd, "Java source code is immutable for type "
			+ PhysicalTypeIdentifier.getFriendlyName(targetId));

	JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails = (JavaParserMutableClassOrInterfaceTypeDetails) ptd;

	ClassOrInterfaceDeclaration clazz = null;

	for (TypeDeclaration classType : compilationUnit.getTypes()) {
	    if (classType instanceof ClassOrInterfaceDeclaration) {
		clazz = (ClassOrInterfaceDeclaration) classType;
		break;
	    }
	}

	if (clazz == null) {
	    return;
	}

	List<BodyDeclaration> members = clazz.getMembers();

	MethodDeclaration methodToUpdate = null;

	for (BodyDeclaration bodyMember : members) {

	    if (bodyMember instanceof MethodDeclaration) {

		methodToUpdate = (MethodDeclaration) bodyMember;

		if (methodToUpdate.getName().equals(method.getSymbolName())) {

		    List<Parameter> methodParameters = methodToUpdate
			    .getParameters();

		    // Compute the parameter type
		    Type parameterType = null;
		    if (paramType.isPrimitive()) {
			parameterType = JavaParserUtils.getType(paramType);
		    } else {
			NameExpr importedType = JavaParserUtils
				.importTypeIfRequired(mutableTypeDetails
					.getEnclosingTypeName(),
					mutableTypeDetails.getImports(),
					paramType);

			ClassOrInterfaceType cit = JavaParserUtils
				.getClassOrInterfaceType(importedType);

			parameterType = cit;
		    }

		    // TODO: Añadir anotaciones
		    /*
		     * p.setAnnotations(parameterAnnotations);
		     */
		    List<AnnotationExpr> methodAnnotationList = new ArrayList<AnnotationExpr>();
		    methodAnnotationList.addAll(methodToUpdate.getAnnotations());
		    

		    List<MemberValuePair> memberList = new ArrayList<MemberValuePair>();
		    MemberValuePair annotationAttribute = new MemberValuePair();
		    annotationAttribute.setName("nuevo");
		    annotationAttribute.setValue(new NameExpr("nombre"));

		    memberList.add(annotationAttribute);
		    
		    NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr(
			    new NameExpr("GvNIXWebService"), memberList);
		    
		    methodAnnotationList.add(annotationExpr);

		    methodToUpdate.setAnnotations(methodAnnotationList);

		    methodParameters.add(new Parameter(parameterType,
			    new VariableDeclaratorId(paramName)));

		    break;
		}

	    }
	}

	try {

	    fileManager.delete(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		    javaIdentifier.concat(".java")));

	    JavaParserMutableClassOrInterfaceTypeDetails details = new JavaParserMutableClassOrInterfaceTypeDetails(
		    compilationUnit, clazz, fileManager, mutableTypeDetails
			    .getDeclaredByMetadataId(), javaIdentifier,
		    className, metadataService, physicalTypeMetadataProvider);

	    classpathOperations.generateClassFile(details);

	} catch (CloneNotSupportedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * {@inheritDoc}
     * <p>
     * </p>
     */
    @Deprecated
    public void updateMethodParameters(JavaType className,
	    JavaSymbolName method, String paramName, JavaType paramType) {

	// TODO: Probar 'MethodDeclaration' ya que permite la creación con
	// javadoc.

	// MetadataID
	String targetId = PhysicalTypeIdentifier.createIdentifier(className,
		Path.SRC_MAIN_JAVA);

	// Obtain the physical type and itd mutable details
	PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
		.get(targetId);
	PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
	Assert.notNull(ptd, "Java source code details unavailable for type "
		+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	Assert.isInstanceOf(JavaParserMutableClassOrInterfaceTypeDetails.class,
		ptd, "Java source code is immutable for type "
			+ PhysicalTypeIdentifier.getFriendlyName(targetId));

	JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails = (JavaParserMutableClassOrInterfaceTypeDetails) ptd;

	// Update method
	List<? extends MethodMetadata> methodsList = mutableTypeDetails
		.getDeclaredMethods();

	// Create param type.
	AnnotatedJavaType annotatedJavaType = new AnnotatedJavaType(paramType,
		null);

	// Create param name.
	JavaSymbolName parameterName = new JavaSymbolName(paramName);

	JavaParserMethodMetadata javaParserMethodMetadata;

	List<MethodMetadata> updatedMethodList = new ArrayList<MethodMetadata>();
	List<AnnotatedJavaType> parameterTypelist = new ArrayList<AnnotatedJavaType>();
	List<JavaSymbolName> parameterNamelist = new ArrayList<JavaSymbolName>();

	MethodMetadata operationMetadata;

	for (MethodMetadata methodMetadata : methodsList) {

	    javaParserMethodMetadata = (JavaParserMethodMetadata) methodMetadata;

	    if (methodMetadata.getMethodName().toString().compareTo(
		    method.toString()) == 0) {

		Assert.isTrue(!javaParserMethodMetadata.getParameterNames()
			.contains(parameterName),
			"There couldn't be two parameters with same name: '"
				+ parameterName + "' in the method "
				+ methodMetadata.getMethodName());

		for (JavaSymbolName tmpParameterName : javaParserMethodMetadata
			.getParameterNames()) {
		    parameterNamelist.add(tmpParameterName);
		}
		parameterNamelist.add(parameterName);

		for (AnnotatedJavaType tmpParameterType : javaParserMethodMetadata
			.getParameterTypes()) {
		    parameterTypelist.add(tmpParameterType);
		}
		parameterTypelist.add(annotatedJavaType);

		operationMetadata = new DefaultMethodMetadata(targetId,
			javaParserMethodMetadata.getModifier(),
			javaParserMethodMetadata.getMethodName(),
			javaParserMethodMetadata.getReturnType(),
			parameterTypelist, parameterNamelist,
			javaParserMethodMetadata.getAnnotations(),
			javaParserMethodMetadata.getThrowsTypes(),
			javaParserMethodMetadata.getBody()
				.substring(
					javaParserMethodMetadata.getBody()
						.indexOf("{") + 1,
					javaParserMethodMetadata.getBody()
						.indexOf("}")));

	    } else {
		operationMetadata = new DefaultMethodMetadata(targetId,
			javaParserMethodMetadata.getModifier(),
			javaParserMethodMetadata.getMethodName(),
			javaParserMethodMetadata.getReturnType(),
			javaParserMethodMetadata.getParameterTypes(),
			javaParserMethodMetadata.getParameterNames(),
			javaParserMethodMetadata.getAnnotations(),
			javaParserMethodMetadata.getThrowsTypes(),
			javaParserMethodMetadata.getBody()
				.substring(
					javaParserMethodMetadata.getBody()
						.indexOf("{") + 1,
					javaParserMethodMetadata.getBody()
						.indexOf("}")));

	    }
	    updatedMethodList.add(operationMetadata);

	}

	List<ConstructorMetadata> contructorList = new ArrayList<ConstructorMetadata>();
	contructorList.addAll(mutableTypeDetails.getDeclaredConstructors());

	List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
	fieldMetadataList.addAll(mutableTypeDetails.getDeclaredFields());

	List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();
	annotationMetadataList.addAll(mutableTypeDetails.getTypeAnnotations());

	// Replicates the values from the original class.
	ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = new DefaultClassOrInterfaceTypeDetails(
		mutableTypeDetails.getDeclaredByMetadataId(),
		mutableTypeDetails.getName(),
		mutableTypeDetails.getModifier(),
		mutableTypeDetails.getPhysicalTypeCategory(),
		contructorList,
		fieldMetadataList,
		updatedMethodList,
		mutableTypeDetails.getSuperclass(),
		mutableTypeDetails.getExtendsTypes(),
		mutableTypeDetails.getImplementsTypes(),
		annotationMetadataList,
		(mutableTypeDetails.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION) ? mutableTypeDetails
			.getEnumConstants()
			: null);

	// Updates the class in file system.
	updateClass(classOrInterfaceTypeDetails);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * return true if exists an annotation with the same name in method.
     * </p>
     */
    public boolean isAnnotationIntroducedInMethod(String annotation,
	    MethodMetadata methodMetadata) {

	List<AnnotationMetadata> annotationMethodList = methodMetadata
		.getAnnotations();

	for (AnnotationMetadata annotationMetadata : annotationMethodList) {
	    if (annotationMetadata.getAnnotationType()
		    .getFullyQualifiedTypeName().compareTo(annotation) == 0) {
		return true;
	    }
	}
	return false;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public List<JavaType> getMethodExceptionList(JavaType serviceClass,
	    JavaSymbolName methodName) {

	List<JavaType> throwList = new ArrayList<JavaType>();

	MethodMetadata methodMetadata = getMethodByNameInClass(serviceClass,
		methodName);

	if (methodMetadata != null) {
	    throwList = methodMetadata.getThrowsTypes();
	}
	
	return throwList;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if annotation is defined in as DeclaredType.
     * </p>
     */
    public boolean isAnnotationIntroduced(String annotation,
            ClassOrInterfaceTypeDetails governorTypeDetails) {
        JavaType javaType = new JavaType(annotation);
        boolean isDefined = false;
        for (AnnotationMetadata annotationMetadata : governorTypeDetails
                .getTypeAnnotations()) {

            isDefined = annotationMetadata.getAnnotationType().equals(javaType);
            if (isDefined) {
                return isDefined;
            }
        }

        return isDefined;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Search the method by name.
     * </p>
     */
    public MethodMetadata getMethodByNameInClass(JavaType serviceClass,
	    JavaSymbolName methodName) {
	// Load class details. If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails tmpServiceDetails = classpathOperations
		.getClassOrInterface(serviceClass);

	// Checks if it's mutable
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		tmpServiceDetails, "Can't modify "
			+ tmpServiceDetails.getName());

	MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;

	List<? extends MethodMetadata> methodList = serviceDetails
		.getDeclaredMethods();

	for (MethodMetadata methodMetadata : methodList) {
	    if (methodMetadata.getMethodName().equals(methodName)) {
		return methodMetadata;
	    }
	}

	return null;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * First, deletes the old class using its Id and then creates the new one
     * with the updated values in file system.
     * </p>
     * 
     */
    public void updateClass(
	    ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails) {

	// TODO: Comprobar si ha variado el contenido para no hacer operaciones
	// innecesarias.

	// TODO: 'ClassOrInterfaceDeclaration' mantiene el javaDoc.

	String javaIdentifier = physicalTypeMetadataProvider
		.findIdentifier(classOrInterfaceTypeDetails.getName());
	javaIdentifier = javaIdentifier.substring(
		javaIdentifier.indexOf("?") + 1).replaceAll("\\.", "/");

	fileManager.delete(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		javaIdentifier.concat(".java")));

	classpathOperations.generateClassFile(classOrInterfaceTypeDetails);

    }
}
