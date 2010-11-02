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

import org.gvnix.service.layer.roo.addon.ServiceLayerWsExportOperations.MethodParameterType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerWSExportValidationService {

    /**
     * Checks if JavaTypes input/output parameters involved in operation are
     * permitted to be published in web service.
     * 
     * @param serviceClass
     *            to check if the method is correct to be published.
     * @param methodName
     *            method to check if input/output parameters ara permitted to be
     *            publish in web service.
     */
    public void checkAuthorizedJavaTypesInOperation(JavaType serviceClass,
            JavaSymbolName methodName);

    /**
     * Check if JavaType is defined in common collection type Set or extends it.
     * 
     * @param javaType
     *            to check.
     * @return true if exists.
     */
    public boolean isNotAllowedCollectionType(JavaType javaType);

    /**
     * Check id JavaType is allowed type to be used in a service operation.
     * 
     * @param javaType
     *            of the parameter. Can't be null.
     * @param methodParameterType
     *            return or input parameters to check.
     * @param serviceClass
     *            where is used the element.
     * 
     * @return true if it's all correct.
     * 
     */
    public boolean isJavaTypeAllowed(JavaType javaType,
            MethodParameterType methodParameterType, JavaType serviceClass);

    /**
     * Check method exceptions to publish in service operation.
     * 
     * @param serviceClass
     *            where the method is defined.
     * @param methodName
     *            to check its exceptions.
     * @param webServiceTargetNamespace
     *            Web Service Namespace.
     * 
     * @return true if the exceptions are published correctly or false if the
     *         exceptions don't exists or are incorrect.
     */
    public boolean checkMethodExceptions(JavaType serviceClass,
            JavaSymbolName methodName, String webServiceTargetNamespace);

    /**
     * Adds a declaration of @WebFault to exceptionClass in AspectJ file.
     * 
     * @param exceptionClass
     *            to export as web service exception.
     * @param annotationAttributeValues
     *            defined for annotation.
     */
    public void exportImportedException(JavaType exceptionClass,
            List<AnnotationAttributeValue<?>> annotationAttributeValues);

    /**
     * Checks correct namespace URI format. Suffix 'http://'.
     * 
     * 
     * @param namespace
     *            string to check as correct namespace.
     * 
     * @return true if is blank or if has correct URI format.
     */
    public boolean checkNamespaceFormat(String namespace);

    /**
     * Check if serviceClass has defined @GvNIXWebService annotation with valid
     * 'targetNamespace' attribute and returns it if is correct.
     * 
     * @param serviceClass
     *            Web Service class to check correct target Namespace and return
     *            as result.
     * @return targetNamespace attribute from annotation @GvNIXWebService.
     */
    public String getWebServiceDefaultNamespace(JavaType serviceClass);

    /**
     * Retrieves all fields with aren't annotated with
     * <ul>
     * <li>@OneToMany</li>
     * <li>@ManyToOne</li>
     * <li>@OneToOne</li>
     * </ul>
     * or are not:
     * <ul>
     * <li>Unsupported Collections.</li>
     * <li>Project Objects.</li>
     * </ul>
     * 
     * @param governorTypeDetails
     *            class to get fields to check.
     * @param gvNixxmlElementAnnotationMetadata
     *            to check element values.
     * @return {@link ArrayAttributeValue} with fields to be published as
     *         '@XmlElement.'
     */
    public ArrayAttributeValue<StringAttributeValue> getElementFields(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            MethodParameterType methodParameterType);

    /**
     * Check if serviceClass is a Roo Entity.
     * 
     * @param serviceClass
     * @return true if is not a Roo Entity.
     */
    public boolean checkIsNotRooEntity(JavaType serviceClass);
}
