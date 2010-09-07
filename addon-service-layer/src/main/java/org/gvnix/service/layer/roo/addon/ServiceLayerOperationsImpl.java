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
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.*;

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
public class ServiceLayerOperationsImpl implements ServiceLayerOperations {

    @Reference
    private MetadataService metadataService;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private ServiceLayerUtils serviceLayerUtils;

    
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
     * Creates the body of the new method (the return line) and calls the
     * 'insertMethod' method to add into the class.
     * </p>
     */
    public void addServiceOperation(JavaSymbolName operationName,
	    JavaType returnType, JavaType className) {

	InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

	// Create some method content to get the user started.
	String todoMessage = "// TODO: You have to place the method logic here.\n";
	bodyBuilder.appendFormalLine(todoMessage);

	// If return type != null we must add method body (return null);
	String returnLine = "return "
		.concat(returnType == null ? ";" : "null;");
	bodyBuilder.appendFormalLine(returnLine);

	insertMethod(operationName, returnType, className, Modifier.PUBLIC,
		new ArrayList<AnnotatedJavaType>(),
		new ArrayList<JavaSymbolName>(), bodyBuilder.getOutput());
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Updates the class with the new method.
     * </p>
     */
    public void insertMethod(JavaSymbolName methodName, JavaType returnType,
	    JavaType targetType, int modifier,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body) {
	
	serviceLayerUtils.createMethod(methodName, returnType, targetType,
		modifier, new ArrayList<JavaType>(),
		new ArrayList<AnnotationMetadata>(), paramTypes, paramNames,
		body);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void addServiceOperationParameter(JavaType className,
	    JavaSymbolName method, String paramName, JavaType paramType) {

	serviceLayerUtils.updateMethodParameters(className, method, paramName,
		paramType);

    }

    /*
     * Utilities
     */

    /**
     * <p>
     * Inserts the method as abstract in the selected class.
     * </p>
     * 
     * @param opeName
     *            Method name.
     * @param returnType
     *            Operation java return Type.
     * @param targetType
     *            Class to insert the operation.
     * @param modifier
     *            Method modifier declaration.
     * @param paramTypes
     *            Input parameters types.
     * @param paramNames
     *            Input parameters names.
     * @param body
     *            Method body.
     */
    private void insertAbstractMethod(JavaSymbolName opeName,
	    JavaType returnType, JavaType targetType,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body) {
	insertMethod(opeName, returnType, targetType, Modifier.ABSTRACT,
		paramTypes, paramNames, body);

    }

}