/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.roo.addon.addon;

import java.util.List;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 *@author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public interface ServiceOperations {

    /**
     * Create a Service class.
     * 
     * @param serviceClass class to be created.
     */
    public void createServiceClass(JavaType serviceClass);

    /**
     * Adds an operation to a class.
     * 
     * @param operationName Operation Name to be created.
     * @param returnType Operation java return Type.
     * @param className Class to insert the operation.
     * @param paramTypeList List of JavaType for each input parameter.
     * @param paramNameList List of names for each input parameter.
     * @param exceptionList List of exceptions that throws the operation.
     */
    public void addServiceOperation(JavaSymbolName operationName,
            JavaType returnType, JavaType className,
            List<JavaType> paramTypeList, List<String> paramNameList,
            List<JavaType> exceptionList);

}
