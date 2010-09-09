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

import japa.parser.ParseException;

import java.util.List;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Utilities to manage java clases elements, create, update methods.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface JavaParserService {

    /**
     * Create a Service class.
     * 
     * @param serviceClass
     *            class to be created.
     */
    public void createServiceClass(JavaType serviceClass);

    /**
     * Inserts a new operation to a class.
     * 
     * @param methodName
     *            Method name.
     * @param returnType
     *            Operation java return Type.
     * @param targetType
     *            Class to insert the operation.
     * @param modifier
     *            Method modifier declaration.
     * @param throwsTypes
     *            Method exception.
     * @param annotationList
     *            Method annotations.
     * @param paramTypes
     *            Input parameters types.
     * @param paramNames
     *            Input parameters names.
     * @param body
     *            Method body.
     * 
     */
    public void createMethod(JavaSymbolName methodName, JavaType returnType,
	    JavaType targetType, int modifier, List<JavaType> throwsTypes,
	    List<AnnotationMetadata> annotationList,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body);

    /**
     * TODO:
     */
    public void updateMethodAnnotations();

    /**
     * Adds an input parameter into selected class method.
     * 
     * @param className
     *            Class to update the method with the new parameter.
     * @param method
     *            Method name.
     * @param paramName
     *            Input parameter names.
     * @param paramType
     *            Input parameter Type.
     */
    public void updateMethodParameters(JavaType className,
	    JavaSymbolName method, String paramName, JavaType paramType);

    /**
     * Adds an input parameter into selected class method.
     * 
     * @param className
     *            Class to update the method with the new parameter.
     * @param method
     *            Method name.
     * @param paramName
     *            Input parameter names.
     * @param paramType
     *            Input parameter Type.
     */
    public void updateWithJavaDoc(JavaType className, JavaSymbolName method,
	    String paramName, JavaType paramType) throws ParseException;
    /**
     * Updates the class with the new values.
     * 
     * @param classOrInterfaceTypeDetails
     *            class to update.
     */
    public void updateClass(
	    ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails);
}