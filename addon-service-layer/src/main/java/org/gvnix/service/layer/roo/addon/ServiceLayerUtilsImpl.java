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

import japa.parser.ast.body.MethodDeclaration;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.javaparser.JavaParserMutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.javaparser.details.JavaParserMethodMetadata;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerUtilsImpl implements ServiceLayerUtils {

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
     */
    public void updateMethodAnnotations() {

    }

    /**
     * {@inheritDoc}
     * <p>
     * </p>
     */
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
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, ptd,
		"Java source code is immutable for type "
			+ PhysicalTypeIdentifier.getFriendlyName(targetId));
	MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

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

		updatedMethodList.add(operationMetadata);

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

		updatedMethodList.add(operationMetadata);

	    }
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
		mutableTypeDetails.getName(), mutableTypeDetails.getModifier(),
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
