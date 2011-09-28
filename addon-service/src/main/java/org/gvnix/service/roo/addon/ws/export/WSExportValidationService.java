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
package org.gvnix.service.roo.addon.ws.export;

import java.util.List;

import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.ws.export.WSExportOperations.MethodParameterType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Utility component to Export Web services
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSExportValidationService {

    /**
     * Checks if JavaTypes input/output parameters involved in operation are
     * permitted to be published in web service and adds {@link GvNIXXmlElement}
     * annotation to any related project type which needs it.
     * 
     * @param serviceClass
     *            to check if the method is correct to be published.
     * @param methodName
     *            method to check if input/output parameters ara permitted to be
     *            publish in web service.
     */
    public void prepareAuthorizedJavaTypesInOperation(JavaType serviceClass,
            JavaSymbolName methodName);

    /**
     * <p>
     * Check if JavaType is defined in common collection type Set or extends it.
     * </p>
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
     * @return true if it's all correct.
     */
    public boolean isJavaTypeAllowed(JavaType javaType,
            MethodParameterType methodParameterType, JavaType serviceClass);

    /**
     * <p>
     * Check method exceptions to publish in service operation.
     * </p>
     * <p>
     * Add web services annotations to each founded exception.
     * </p>
     * <p>
     * There are two exceptions types and two ways to define annotations:
     * </p>
     * <ul>
     * <li>Exceptions defined in the project.
     * <p>
     * Add {@link GvNIXWebFault} annotation to Exception.
     * </p>
     * </li>
     * <li>Exceptions imported into the project.
     * <p>
     * Add web service fault annotation using AspectJ template.
     * </p>
     * </li>
     * </ul>
     * 
     * @param serviceClass
     *            where the method is defined.
     * @param methodName
     *            to check its exceptions.
     * @param webServiceTargetNamespace
     *            Web Service Namespace.
     * @return true if the exceptions are published correctly or false if the
     *         exceptions don't exists or are incorrect.
     */
    public boolean prepareMethodExceptions(JavaType serviceClass,
            JavaSymbolName methodName, String webServiceTargetNamespace);

    /**
     * Adds a declaration of <code>@WebFault</code> to exceptionClass in AspectJ
     * file.
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
     * @param namespace
     *            string to check as correct namespace.
     * @return true if is blank or if has correct URI format.
     */
    public boolean checkNamespaceFormat(String namespace);

    /**
     * Check if serviceClass has defined {@link GvNIXWebService} annotation with
     * valid 'targetNamespace' attribute and returns it if is correct.
     * 
     * @param serviceClass
     *            Web Service class to check correct target Namespace and return
     *            as result.
     * @return targetNamespace attribute from annotation {@link GvNIXWebService}
     *         .
     */
    public String getWebServiceDefaultNamespace(JavaType serviceClass);

    /**
     * Values array of allowed element fields name from governor type (Java).
     * 
     * <ul>
     * <li>Get identifier and version fields from governor related entity
     * metadata.</li>
     * <li>Get all fields from governor and remove not allowed types fields:
     * OneToMany, ManyToOne and OneToOne</li>
     * <li>Remove not allowed entity types fields for entity.</li>
     * </ul>
     * 
     * TODO Utility class. Remove from interface?
     * 
     * @param governorTypeDetails
     *            class to get fields to check.
     * @return {@link ArrayAttributeValue} with fields to be published as
     *         '@XmlElement.'
     */
    public ArrayAttributeValue<StringAttributeValue> getFields(
            ClassOrInterfaceTypeDetails governorTypeDetails);

    /**
     * Check if serviceClass is a Roo Entity.
     * 
     * @param serviceClass
     * @return true if is not a Roo Entity.
     */
    public boolean checkIsNotRooEntity(JavaType serviceClass);
}
