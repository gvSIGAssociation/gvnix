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

import org.gvnix.service.roo.addon.addon.ws.export.WSExportWsdlConfigService.GvNIXAnnotationType;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.IdentifiableAnnotatedJavaStructure;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Utilities to manage java clases elements, create, update methods.
 * 
 *@author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public interface JavaParserService {

    /**
     * Create a Service class.
     * 
     * @param serviceClass class to be created.
     */
    public void createServiceClass(JavaType serviceClass);

    /**
     * Inserts a new operation to a class.
     * 
     * @param methodName Method name.
     * @param returnType Operation java return Type.
     * @param targetType Class to insert the operation.
     * @param modifier Method modifier declaration.
     * @param throwsTypes Method exception.
     * @param annotationList Method annotations.
     * @param paramTypes Input parameters types.
     * @param paramNames Input parameters names.
     * @param body Method body.
     */
    public void createMethod(JavaSymbolName methodName, JavaType returnType,
            JavaType targetType, int modifier, List<JavaType> throwsTypes,
            List<AnnotationMetadata> annotationList,
            List<AnnotatedJavaType> paramTypes,
            List<JavaSymbolName> paramNames, String body);

    /**
     * Adds annotation into selected class method.
     * 
     * @param className Class to update the method with the new parameter.
     * @param method Method name.
     * @param annotationMetadataUpdateList Annotations to set to method.
     * @param annotationWebParamMetadataList Annotations for each input
     *        parameter.
     */
    public void updateMethodAnnotations(JavaType className,
            JavaSymbolName method,
            List<AnnotationMetadata> annotationMetadataUpdateList,
            List<AnnotatedJavaType> annotationWebParamMetadataList);

    /**
     * Adds an input parameter into selected class method.
     * 
     * @param className Class to update the method with the new parameter.
     * @param method Method name.
     * @param paramName Input parameter names.
     * @param paramType Input parameter Type.
     */
    public void updateMethodParameters(JavaType className,
            JavaSymbolName method, String paramName, JavaType paramType);

    /**
     * Updates the class with the new values.
     * 
     * @param classOrInterfaceTypeDetails class to update.
     */
    public void updateClass(
            ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails);

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation to be check if exists.
     * @param methodMetadata method to check if annotation exists.
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroducedInMethod(String annotation,
            MethodMetadata methodMetadata);

    /**
     * Indicates whether the annotation will be introduced via this ITD.
     * 
     * @param annotation to be check if exists.
     * @return true if it will be introduced, false otherwise
     */
    public boolean isAnnotationIntroduced(String annotation,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    /**
     * Returns MethodMetadata in selected class.
     * 
     * @param serviceClass to search the method.
     * @param methodName to retrieve method.
     * @return Method with methodName in serviceClass. If method doesn't exists
     *         in serviceClass returns null.
     */
    public MethodMetadata getMethodByNameInClass(JavaType serviceClass,
            JavaSymbolName methodName);

    /**
     * Returns MethodMetadata in selected class and related AJs.
     * 
     * @param name Class name to search the method.
     * @param methodName to retrieve method.
     * @return Method with methodName in serviceClass and related AJs. If method
     *         doesn't exists returns null.
     */
    public MethodMetadata getMethodByNameInAll(JavaType name,
            JavaSymbolName methodName);

    /**
     * Returns MethodMetadatas in selected class and related AJs.
     * 
     * @param name Class name to search the method.
     * @return All methods in serviceClass and related AJs. If method doesn't
     *         exists returns empty list.
     */
    public List<MethodMetadata> getMethodsInAll(JavaType name);

    /**
     * Returns FieldMetadata in selected class and related AJs.
     * 
     * @param name Class name to search the field.
     * @return All fields in serviceClass and related AJs. If field doesn't
     *         exists returns empty list.
     */
    public List<FieldMetadata> getFieldsInAll(JavaType name);

    /**
     * Returns FieldMetadata in selected class and related AJs.
     * 
     * @param name Class name to search the field.
     * @param methodName to retrieve field.
     * @return Field with name in type and related AJs. If not return null
     */
    public FieldMetadata getFieldByNameInAll(JavaType type, JavaSymbolName name);

    /**
     * Create GvNIX annotated class.
     * 
     * @param javaType class.
     * @param typeAnnotationList class annotation list.
     * @param gvNIXAnnotationType to create Metadata ID.
     * @param declaredFieldList class field list
     * @param declaredMethodList class method list.
     * @param declaredConstructorList class constructor list.
     * @param declaredClassList extended classes.
     * @param physicalTypeCategory of generated class (Enum, Class..)
     * @param enumConstantsList List of enum fields defined in class.
     */
    public void createGvNixWebServiceClass(JavaType javaType,
            List<AnnotationMetadata> typeAnnotationList,
            GvNIXAnnotationType gvNIXAnnotationType,
            List<FieldMetadata> declaredFieldList,
            List<MethodMetadata> declaredMethodList,
            List<ConstructorMetadata> declaredConstructorList,
            List<JavaType> declaredClassList,
            PhysicalTypeCategory physicalTypeCategory,
            List<JavaSymbolName> enumConstantsList);

    /**
     * Returns annotation metada from a class
     * 
     * @param annotation
     * @param governorTypeDetails
     * @return
     */
    public AnnotationMetadata getAnnotation(String annotation,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    /**
     * Is a identifier metadata from a defined metadata type ?
     * 
     * @param id Metatada identifier
     * @param metadata Metadada type
     * @return Is identifier from metadata type ?
     */
    public boolean isMetadataId(String id,
            IdentifiableAnnotatedJavaStructure metadata);

}
