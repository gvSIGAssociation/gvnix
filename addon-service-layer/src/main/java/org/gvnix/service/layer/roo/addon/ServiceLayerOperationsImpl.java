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
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.Assert;

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
    private JavaParserService javaParserService;

    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.ServiceLayerOperations#
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

	javaParserService.createServiceClass(serviceClass);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates the body of the new method (the return line), checks the input
     * parameters if they exists and calls the 'createMethod' method from
     * {@link JavaParserService} to add into the class.
     * </p>
     */
    public void addServiceOperation(JavaSymbolName operationName,
	    JavaType returnType, JavaType className,
	    List<JavaType> paramTypeList, List<String> paramNameList,
	    List<JavaType> exceptionList) {

	InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

	// Create some method content to get the user started.
	String todoMessage = "// TODO: You have to place the method logic here.\n";
	bodyBuilder.appendFormalLine(todoMessage);

	// If return type != null we must add method body (return null);
	String returnLine = "return "
		.concat(returnType == null ? ";" : "null;");
	bodyBuilder.appendFormalLine(returnLine);

	// Parameter names
	List<JavaSymbolName> parameterNameList = new ArrayList<JavaSymbolName>();
	// Parameter types.
	List<AnnotatedJavaType> parameterTypeList = new ArrayList<AnnotatedJavaType>();

	Assert
		.isTrue(
			paramTypeList != null
				&& paramTypeList.size() == paramNameList.size(),
			"The method parameter types must have the same number of parameter names to create the method.");

	if (paramNameList.size() > 0) {
	    for (String parameterName : paramNameList) {
		parameterNameList.add(new JavaSymbolName(parameterName));
	    }
	}

	
	if (paramTypeList != null && paramTypeList.size() > 0) {
	    for (JavaType parameterType : paramTypeList) {
		parameterTypeList
			.add(new AnnotatedJavaType(parameterType, null));
	    }
	}

	javaParserService.createMethod(operationName, returnType, className,
		Modifier.PUBLIC, exceptionList,
		new ArrayList<AnnotationMetadata>(), parameterTypeList,
		parameterNameList, bodyBuilder.getOutput());
    }

}
