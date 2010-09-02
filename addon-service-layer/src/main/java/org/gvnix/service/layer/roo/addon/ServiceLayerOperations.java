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

import java.util.List;

import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerOperations {

    boolean isProjectAvailable();

    /**
     * <p>
     * Create a Service class.
     * </p>
     * 
     * @param serviceClass
     *            class to be created.
     */
    public void createServiceClass(JavaType serviceClass);

    /**
     * <p>
     * Adds an operation to a class.
     * </p>
     * 
     * @param opeName
     *            Operation Name to be created.
     * @param returnType
     *            Operation java return Type.
     * @param className
     *            Class to insert the operation.
     */
    public void addServiceOperation(JavaSymbolName opeName,
	    JavaType returnType, JavaType className);

    /**
     * <p>
     * Inserts a new operation to a class.
     * </p>
     * 
     * @param methodName
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
    public void insertMethod(JavaSymbolName methodName, JavaType returnType,
	    JavaType targetType, int modifier,
	    List<AnnotatedJavaType> paramTypes,
	    List<JavaSymbolName> paramNames, String body);
}