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
package org.gvnix.service.roo.addon;

import japa.parser.ParseException;

import java.util.List;

import org.gvnix.service.roo.addon.ws.export.WSExportWsdlConfigService.GvNIXAnnotationType;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Utilities to manage java clases elements, create, update methods.
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
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
     */
    public void createMethod(JavaSymbolName methodName, JavaType returnType,
            JavaType targetType, int modifier, List<JavaType> throwsTypes,
            List<AnnotationMetadata> annotationList,
            List<AnnotatedJavaType> paramTypes,
            List<JavaSymbolName> paramNames, String body);

    /**
     * Adds annotation into selected class method.
     * 
     * @param className
     *            Class to update the method with the new parameter.
     * @param method
     *            Method name.
     * @param annotationMetadataUpdateList
     *            Annotations to set to method.
     * @param annotationWebParamMetadataList
     *            Annotations for each input parameter.
     */
    public void updateMethodAnnotations(JavaType className,
            JavaSymbolName method,
            List<AnnotationMetadata> annotationMetadataUpdateList,
            List<AnnotatedJavaType> annotationWebParamMetadataList);

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

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation
     *            to be check if exists.
     * @param methodMetadata
     *            method to check if annotation exists.
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroducedInMethod(String annotation,
            MethodMetadata methodMetadata);

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation
     *            to be check if exists.
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroduced(String annotation,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    /**
     * Returns method exceptions throw list.
     * 
     * @param serviceClass
     *            where the method is defined.
     * @param methodName
     *            to search.
     * @return {@link List} of {@link JavaType} Exception that are defined in
     *         method. Empty list if there is no exception defined.
     */
    public List<JavaType> getMethodExceptionList(JavaType serviceClass,
            JavaSymbolName methodName);

    /**
     * Returns MethodMetadata in selected class.
     * 
     * @param serviceClass
     *            to search the method.
     * @param methodName
     *            to retrieve method.
     * @return Method with methodName in serviceClass. If method doesn't exists
     *         in serviceClass returns null.
     */
    public MethodMetadata getMethodByNameInClass(JavaType serviceClass,
            JavaSymbolName methodName);

    /**
     * Create GvNIX annotated class.
     * 
     * @param javaType
     *            class.
     * @param typeAnnotationList
     *            class annotation list.
     * @param gvNIXAnnotationType
     *            to create Metadata ID.
     * @param declaredFieldList
     *            class field list
     * @param declaredMethodList
     *            class method list.
     * @param declaredConstructorList
     *            class constructor list.
     * @param declaredClassList
     *            extended classes.
     * @param physicalTypeCategory
     *            of generated class (Enum, Class..)
     * @param enumConstantsList
     *            List of enum fields defined in class.
     */
    public void createGvNIXWebServiceClass(JavaType javaType,
            List<AnnotationMetadata> typeAnnotationList,
            GvNIXAnnotationType gvNIXAnnotationType,
            List<FieldMetadata> declaredFieldList,
            List<MethodMetadata> declaredMethodList,
            List<ConstructorMetadata> declaredConstructorList,
            List<JavaType> declaredClassList,
            PhysicalTypeCategory physicalTypeCategory,
            List<JavaSymbolName> enumConstantsList);
}