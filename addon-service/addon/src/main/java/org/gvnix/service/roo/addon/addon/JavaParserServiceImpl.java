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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.ws.export.WSExportWsdlConfigService.GvNIXAnnotationType;
import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.TypeParsingService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.IdentifiableAnnotatedJavaStructure;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
@Component
@Service
public class JavaParserServiceImpl implements JavaParserService {

    @Reference
    private MetadataService metadataService;
    @Reference
    private TypeManagementService typeManagementService;
    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    protected MemberDetailsScanner memberDetailsScanner;
    @Reference
    protected TypeParsingService typeParsingService;

    /**
     * {@inheritDoc}
     * <p>
     * Adds @org.springframework.stereotype.Service annotation to the class.
     * </p>
     */
    public void createServiceClass(JavaType serviceClass) {

        // Service class
        String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
                serviceClass, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

        // Service annotations
        List<AnnotationMetadata> serviceAnnotations = new ArrayList<AnnotationMetadata>();
        serviceAnnotations.add(new AnnotationMetadataBuilder(new JavaType(
                "org.springframework.stereotype.Service"),
                new ArrayList<AnnotationAttributeValue<?>>()).build());

        ClassOrInterfaceTypeDetailsBuilder serviceDetails = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, Modifier.PUBLIC, serviceClass,
                PhysicalTypeCategory.CLASS);
        for (AnnotationMetadata annotationMetadata : serviceAnnotations) {
            serviceDetails.addAnnotation(annotationMetadata);
        }

        typeManagementService.createOrUpdateTypeOnDisk(serviceDetails.build());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only creates the class if not exists in project.
     * </p>
     */
    public void createGvNixWebServiceClass(JavaType type,
            List<AnnotationMetadata> annots, GvNIXAnnotationType gvNixAnnot,
            List<FieldMetadata> fields, List<MethodMetadata> methods,
            List<ConstructorMetadata> constrs, List<JavaType> exts,
            PhysicalTypeCategory physicalType, List<JavaSymbolName> enumConsts) {

        // Metadata Id.
        String id = PhysicalTypeIdentifier.createIdentifier(type,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

        // Determine the canonical filename
        String physicalPath = typeLocationService
                .getPhysicalTypeCanonicalPath(id);

        // Check the file doesn't already exist
        if (!fileManager.exists(physicalPath)) {

            if (!physicalType.equals(PhysicalTypeCategory.ENUMERATION)) {
                enumConsts = null;
            }

            // Create class
            ClassOrInterfaceTypeDetailsBuilder typeDetails = new ClassOrInterfaceTypeDetailsBuilder(
                    id, Modifier.PUBLIC, type, physicalType);
            for (AnnotationMetadata annotationMetadata : annots) {
                typeDetails.addAnnotation(annotationMetadata);
            }
            for (FieldMetadata field : fields) {
                typeDetails.addField(field);
            }
            for (ConstructorMetadata constr : constrs) {
                typeDetails.addConstructor(constr);
            }
            for (MethodMetadata method : methods) {
                typeDetails.addMethod(method);
            }
            for (JavaType ext : exts) {
                typeDetails.addExtendsTypes(ext);
            }
            if (enumConsts != null) {
                for (JavaSymbolName enumConst : enumConsts) {
                    typeDetails.addEnumConstant(enumConst);
                }
            }

            typeManagementService.createOrUpdateTypeOnDisk(typeDetails.build());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates the class with the new method created with selected attributes.
     * </p>
     */
    public void createMethod(JavaSymbolName methodName, JavaType returnType,
            JavaType targetType, int modifier, List<JavaType> throwsTypes,
            List<AnnotationMetadata> annotationList,
            List<AnnotatedJavaType> paramTypes,
            List<JavaSymbolName> paramNames, String body) {

        Validate.notNull(paramTypes, "Param type mustn't be null");
        Validate.notNull(paramNames, "Param name mustn't be null");

        // MetadataID
        String targetId = PhysicalTypeIdentifier.createIdentifier(targetType,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);
        Validate.notNull(ptm, "Java source class doesn't exists.");

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Validate.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class, ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));
        ClassOrInterfaceTypeDetails mutableTypeDetails = (ClassOrInterfaceTypeDetails) ptd;

        // create method
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(body);
        MethodMetadataBuilder operationMetadata = new MethodMetadataBuilder(
                targetId, modifier, methodName,
                (returnType == null ? JavaType.VOID_PRIMITIVE : returnType),
                paramTypes, paramNames, bodyBuilder);
        for (AnnotationMetadata annotationMetadata : annotationList) {
            operationMetadata.addAnnotation(annotationMetadata);
        }
        for (JavaType javaType : throwsTypes) {
            operationMetadata.addThrowsType(javaType);
        }

        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                mutableTypeDetails);
        mutableTypeDetailsBuilder.addMethod(operationMetadata.build());
        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adds Web Service annotation to selected method.
     * </p>
     */
    public void updateMethodAnnotations(JavaType className,
            JavaSymbolName method,
            List<AnnotationMetadata> annotationMetadataUpdateList,
            List<AnnotatedJavaType> annotationWebParamMetadataList) {

        // MetadataID
        String targetId = PhysicalTypeIdentifier.createIdentifier(className,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Validate.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class, ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));

        ClassOrInterfaceTypeDetails mutableTypeDetails = (ClassOrInterfaceTypeDetails) ptd;

        List<MethodMetadata> updatedMethodList = new ArrayList<MethodMetadata>();

        // Add to list required method and all methods in Java

        // Search one method in all (Java and AspectJ)
        updatedMethodList.addAll(searchMethodInAll(className, method,
                annotationMetadataUpdateList, annotationWebParamMetadataList,
                targetId));

        // Search methods in Java, excepts one method
        updatedMethodList.addAll(searchMethodsInJava(method, targetId,
                mutableTypeDetails));

        ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetails = createTypeDetails(
                mutableTypeDetails, updatedMethodList);
        // Add old class imports into new class to avoid undefined imports
        // Example: Not included HashSet import when exporting method in
        // petclinic Owner
        classOrInterfaceTypeDetails.setRegisteredImports(mutableTypeDetails
                .getRegisteredImports());

        // Updates the class in file system.
        updateClass(classOrInterfaceTypeDetails.build());
    }

    /**
     * Create a class or interface from java parser and add methods.
     * 
     * @param mutableTypeDetails
     * @param updatedMethodList
     * @return
     */
    protected ClassOrInterfaceTypeDetailsBuilder createTypeDetails(
            ClassOrInterfaceTypeDetails mutableTypeDetails,
            List<MethodMetadata> updatedMethodList) {

        List<ConstructorMetadata> contructorList = new ArrayList<ConstructorMetadata>();
        contructorList.addAll(mutableTypeDetails.getDeclaredConstructors());

        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
        fieldMetadataList.addAll(mutableTypeDetails.getDeclaredFields());

        List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();

        annotationMetadataList.addAll(mutableTypeDetails.getAnnotations());

        // Replicates the values from the original class
        ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetails = new ClassOrInterfaceTypeDetailsBuilder(
                mutableTypeDetails.getDeclaredByMetadataId(),
                mutableTypeDetails.getModifier(), mutableTypeDetails.getName(),
                mutableTypeDetails.getPhysicalTypeCategory());
        for (AnnotationMetadata annotationMetadata : annotationMetadataList) {
            classOrInterfaceTypeDetails.addAnnotation(annotationMetadata);
        }
        for (FieldMetadata fieldMetadata : fieldMetadataList) {

            classOrInterfaceTypeDetails.addField(fieldMetadata);
        }
        for (ConstructorMetadata constructorMetadata : contructorList) {

            classOrInterfaceTypeDetails.addConstructor(constructorMetadata);
        }
        for (MethodMetadata methodMetadata : updatedMethodList) {

            classOrInterfaceTypeDetails.addMethod(methodMetadata);
        }
        for (JavaType declaredClass : mutableTypeDetails.getExtendsTypes()) {

            classOrInterfaceTypeDetails.addExtendsTypes(declaredClass);
        }
        for (JavaType declaredClass : mutableTypeDetails.getImplementsTypes()) {

            classOrInterfaceTypeDetails.addImplementsType(declaredClass);
        }
        if (mutableTypeDetails.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION) {
            for (JavaSymbolName enumConstant : mutableTypeDetails
                    .getEnumConstants()) {

                classOrInterfaceTypeDetails.addEnumConstant(enumConstant);
            }
        }
        return classOrInterfaceTypeDetails;
    }

    /**
     * Search method in Java.
     * 
     * @param methodName Method name to search
     * @param targetId New method destination identifier
     * @param mutableTypeDetails Type to search methods in
     * @return Methods list
     */
    protected List<MethodMetadata> searchMethodsInJava(
            JavaSymbolName methodName, String targetId,
            ClassOrInterfaceTypeDetails mutableTypeDetails) {

        List<MethodMetadata> methods = new ArrayList<MethodMetadata>();

        for (MethodMetadata method : mutableTypeDetails.getDeclaredMethods()) {

            if (method.getMethodName().toString()
                    .compareTo(methodName.toString()) != 0) {

                InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
                bodyBuilder.appendFormalLine(method.getBody());
                MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                        targetId, method.getModifier(), method.getMethodName(),
                        method.getReturnType(), method.getParameterTypes(),
                        method.getParameterNames(), bodyBuilder);
                for (AnnotationMetadata annotationMetadata : method
                        .getAnnotations()) {
                    methodMetadataBuilder.addAnnotation(annotationMetadata);
                }
                for (JavaType javaType : method.getThrowsTypes()) {
                    methodMetadataBuilder.addThrowsType(javaType);
                }
                MethodMetadata operationMetadata = methodMetadataBuilder
                        .build();

                methods.add(operationMetadata);
            }
        }

        return methods;
    }

    /**
     * Search method in all (Java and AspectJ).
     * <p>
     * Add into method and into method params required web service annotations.
     * </p>
     * 
     * @param className Class name to seach
     * @param method Method name to search
     * @param annotationMetadataUpdateList Method annotation list
     * @param annotationWebParamMetadataList Method params annotation list
     * @param targetId New method destination identifier
     * @return Methods list
     */
    protected List<MethodMetadata> searchMethodInAll(JavaType className,
            JavaSymbolName method,
            List<AnnotationMetadata> annotationMetadataUpdateList,
            List<AnnotatedJavaType> annotationWebParamMetadataList,
            String targetId) {

        List<MethodMetadata> methods = new ArrayList<MethodMetadata>();

        List<AnnotationMetadata> methodAnnotationList = new ArrayList<AnnotationMetadata>();
        MethodMetadata javaParserMethodMetadata;
        MethodMetadata operationMetadata;
        List<? extends MethodMetadata> allMethodsList = getMethodsInAll(className);
        for (MethodMetadata methodMetadata : allMethodsList) {

            javaParserMethodMetadata = methodMetadata;

            // Only export methods in this entity: no parent class methods check
            // Example: Duplicated method when export toString in Owner
            boolean equal = isMetadataId(targetId, methodMetadata);

            if (methodMetadata.getMethodName().toString()
                    .compareTo(method.toString()) == 0
                    && equal) {

                Validate.isTrue(
                        !isAnnotationIntroducedInMethod(
                                GvNIXWebMethod.class.getName(), methodMetadata),
                        "The method '" + method.toString()
                                + "' has been annotated with '@"
                                + GvNIXWebMethod.class.getName() + "' before.");

                methodAnnotationList.addAll(javaParserMethodMetadata
                        .getAnnotations());
                methodAnnotationList.addAll(annotationMetadataUpdateList);

                InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

                bodyBuilder
                        .appendFormalLine(javaParserMethodMetadata.getBody());
                MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                        targetId, javaParserMethodMetadata.getModifier(),
                        javaParserMethodMetadata.getMethodName(),
                        javaParserMethodMetadata.getReturnType(),
                        annotationWebParamMetadataList,
                        javaParserMethodMetadata.getParameterNames(),
                        bodyBuilder);
                for (AnnotationMetadata annotationMetadata : methodAnnotationList) {
                    methodMetadataBuilder.addAnnotation(annotationMetadata);
                }
                for (JavaType javaType : javaParserMethodMetadata
                        .getThrowsTypes()) {
                    methodMetadataBuilder.addThrowsType(javaType);
                }
                operationMetadata = methodMetadataBuilder.build();

                methods.add(operationMetadata);
            }
        }

        return methods;
    }

    /**
     * {@inheritDoc}
     **/
    public boolean isMetadataId(String id,
            IdentifiableAnnotatedJavaStructure metadata) {

        return metadata
                .getDeclaredByMetadataId()
                .substring(
                        metadata.getDeclaredByMetadataId().lastIndexOf("?") + 1)
                .equals(id.substring(id.lastIndexOf("?") + 1));
    }

    /**
     * {@inheritDoc}
     * <p>
     * </p>
     */
    @Deprecated
    public void updateMethodParameters(JavaType className,
            JavaSymbolName method, String paramName, JavaType paramType) {

        // TODO: Probar 'MethodDeclaration' ya que permite la creación con
        // javadoc.

        // MetadataID
        String targetId = PhysicalTypeIdentifier.createIdentifier(className,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Validate.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class, ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));

        ClassOrInterfaceTypeDetails mutableTypeDetails = (ClassOrInterfaceTypeDetails) ptd;

        // Update method
        List<? extends MethodMetadata> methodsList = mutableTypeDetails
                .getDeclaredMethods();

        // Create param type.
        AnnotatedJavaType annotatedJavaType = new AnnotatedJavaType(paramType,
                new ArrayList<AnnotationMetadata>());

        // Create param name.
        JavaSymbolName parameterName = new JavaSymbolName(paramName);

        MethodMetadata javaParserMethodMetadata;

        List<MethodMetadata> updatedMethodList = new ArrayList<MethodMetadata>();
        List<AnnotatedJavaType> parameterTypelist = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNamelist = new ArrayList<JavaSymbolName>();

        MethodMetadata operationMetadata;

        for (MethodMetadata methodMetadata : methodsList) {

            javaParserMethodMetadata = (MethodMetadata) methodMetadata;

            if (methodMetadata.getMethodName().toString()
                    .compareTo(method.toString()) == 0) {

                Validate.isTrue(!javaParserMethodMetadata.getParameterNames()
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

                InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
                bodyBuilder
                        .appendFormalLine(javaParserMethodMetadata.getBody());
                MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                        targetId, javaParserMethodMetadata.getModifier(),
                        javaParserMethodMetadata.getMethodName(),
                        javaParserMethodMetadata.getReturnType(),
                        parameterTypelist, parameterNamelist, bodyBuilder);
                for (AnnotationMetadata annotationMetadata : javaParserMethodMetadata
                        .getAnnotations()) {
                    methodMetadataBuilder.addAnnotation(annotationMetadata);
                }
                for (JavaType javaType : javaParserMethodMetadata
                        .getThrowsTypes()) {
                    methodMetadataBuilder.addThrowsType(javaType);
                }
                operationMetadata = methodMetadataBuilder.build();

            }
            else {

                InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
                bodyBuilder
                        .appendFormalLine(javaParserMethodMetadata.getBody());
                MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                        targetId, javaParserMethodMetadata.getModifier(),
                        javaParserMethodMetadata.getMethodName(),
                        javaParserMethodMetadata.getReturnType(),
                        javaParserMethodMetadata.getParameterTypes(),
                        javaParserMethodMetadata.getParameterNames(),
                        bodyBuilder);
                for (AnnotationMetadata annotationMetadata : javaParserMethodMetadata
                        .getAnnotations()) {
                    methodMetadataBuilder.addAnnotation(annotationMetadata);
                }
                for (JavaType javaType : javaParserMethodMetadata
                        .getThrowsTypes()) {
                    methodMetadataBuilder.addThrowsType(javaType);
                }
                operationMetadata = methodMetadataBuilder.build();

            }
            updatedMethodList.add(operationMetadata);

        }

        ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetails = createTypeDetails(
                mutableTypeDetails, updatedMethodList);
        classOrInterfaceTypeDetails
                .addInnerType(new ClassOrInterfaceTypeDetailsBuilder(
                        mutableTypeDetails.getSuperclass()));

        // Updates the class in file system.
        updateClass(classOrInterfaceTypeDetails.build());
    }

    /**
     * {@inheritDoc}
     * <p>
     * return true if exists an annotation with the same name in method.
     * </p>
     */
    public boolean isAnnotationIntroducedInMethod(String annotation,
            MethodMetadata methodMetadata) {

        List<AnnotationMetadata> annotationMethodList = methodMetadata
                .getAnnotations();

        for (AnnotationMetadata annotationMetadata : annotationMethodList) {
            if (annotationMetadata.getAnnotationType()
                    .getFullyQualifiedTypeName().compareTo(annotation) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if annotation is defined in as DeclaredType.
     * </p>
     */
    public boolean isAnnotationIntroduced(String annotation,
            ClassOrInterfaceTypeDetails governorTypeDetails) {
        JavaType javaType = new JavaType(annotation);
        boolean isDefined = false;
        for (AnnotationMetadata annotationMetadata : governorTypeDetails
                .getAnnotations()) {

            isDefined = annotationMetadata.getAnnotationType().equals(javaType);
            if (isDefined) {
                return isDefined;
            }
        }

        return isDefined;
    }

    public AnnotationMetadata getAnnotation(String annotation,
            ClassOrInterfaceTypeDetails governorTypeDetails) {
        JavaType javaType = new JavaType(annotation);
        for (AnnotationMetadata annotationMetadata : governorTypeDetails
                .getAnnotations()) {
            if (annotationMetadata.getAnnotationType().equals(javaType)) {
                return annotationMetadata;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Search the method by name in class.
     * </p>
     */
    public MethodMetadata getMethodByNameInClass(JavaType serviceClass,
            JavaSymbolName methodName) {
        // Load class details. If class not found an exception will be raised.
        ClassOrInterfaceTypeDetails tmpServiceDetails = typeLocationService
                .getTypeDetails(serviceClass);

        // Checks if it's mutable
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class,
                tmpServiceDetails,
                "Can't modify " + tmpServiceDetails.getName());

        ClassOrInterfaceTypeDetails serviceDetails = (ClassOrInterfaceTypeDetails) tmpServiceDetails;

        List<? extends MethodMetadata> methodList = serviceDetails
                .getDeclaredMethods();

        for (MethodMetadata methodMetadata : methodList) {
            if (methodMetadata.getMethodName().equals(methodName)) {
                return methodMetadata;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Search the method by name in class and related AJs.
     * </p>
     */
    public MethodMetadata getMethodByNameInAll(JavaType serviceClass,
            JavaSymbolName methodName) {

        MethodMetadata method = null;

        Iterator<MemberHoldingTypeDetails> members = getMemberDetails(
                serviceClass).iterator();
        while (method == null && members.hasNext()) {

            @SuppressWarnings("unchecked")
            List<MethodMetadata> methods = (List<MethodMetadata>) members
                    .next().getDeclaredMethods();
            Iterator<MethodMetadata> methodsIter = methods.iterator();
            while (method == null && methodsIter.hasNext()) {

                MethodMetadata tmpMethod = methodsIter.next();
                if (tmpMethod.getMethodName().getSymbolName()
                        .equals(methodName.getSymbolName())) {
                    method = tmpMethod;
                }
            }
        }

        return method;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Search all methods in class and related AJs.
     * </p>
     */
    public List<MethodMetadata> getMethodsInAll(JavaType name) {

        List<MethodMetadata> methods = new ArrayList<MethodMetadata>();
        for (MemberHoldingTypeDetails member : getMemberDetails(name)) {
            methods.addAll(member.getDeclaredMethods());
        }

        return methods;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Search all fields in class and related AJs.
     * </p>
     */
    public List<FieldMetadata> getFieldsInAll(JavaType name) {

        List<FieldMetadata> methods = new ArrayList<FieldMetadata>();
        for (MemberHoldingTypeDetails member : getMemberDetails(name)) {
            methods.addAll(member.getDeclaredFields());
        }

        return methods;
    }

    /**
     * {@inheritDoc}
     */
    public FieldMetadata getFieldByNameInAll(JavaType type, JavaSymbolName name) {

        FieldMetadata field = null;

        Iterator<MemberHoldingTypeDetails> members = getMemberDetails(type)
                .iterator();
        while (field == null && members.hasNext()) {

            @SuppressWarnings("unchecked")
            List<FieldMetadata> fields = (List<FieldMetadata>) members.next()
                    .getDeclaredFields();
            Iterator<FieldMetadata> fieldsIter = fields.iterator();
            while (field == null && fieldsIter.hasNext()) {

                FieldMetadata tmpField = fieldsIter.next();
                if (tmpField.getFieldName().getSymbolName()
                        .equals(name.getSymbolName())) {
                    field = tmpField;
                }
            }
        }

        return field;
    }

    /**
     * Get the list of type details (Java and AJs) from a Java type.
     * 
     * @param name Java type to get details
     * @return List of type details
     */
    protected List<MemberHoldingTypeDetails> getMemberDetails(JavaType name) {

        String identifier = PhysicalTypeIdentifier.createIdentifier(name,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));
        PhysicalTypeMetadata physicalType = (PhysicalTypeMetadata) metadataService
                .get(identifier);
        String className = getClass().getName();
        ClassOrInterfaceTypeDetails typeDetails = (ClassOrInterfaceTypeDetails) physicalType
                .getMemberHoldingTypeDetails();
        MemberDetails member = memberDetailsScanner.getMemberDetails(className,
                typeDetails);

        return member.getDetails();
    }

    /**
     * {@inheritDoc}
     * <p>
     * First, deletes the old class using its Id and then creates the new one
     * with the updated values in file system.
     * </p>
     */
    public void updateClass(
            ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails) {

        // TODO: Comprobar si ha variado el contenido para no hacer operaciones
        // innecesarias.

        // TODO: 'ClassOrInterfaceDeclaration' mantiene el javaDoc.

        String javaIdentifier = typeLocationService
                .getPhysicalTypeIdentifier(classOrInterfaceTypeDetails
                        .getName());
        javaIdentifier = javaIdentifier.substring(
                javaIdentifier.indexOf("?") + 1).replaceAll("\\.", "/");

        fileManager.delete(projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                javaIdentifier.concat(".java")));

        typeManagementService
                .createOrUpdateTypeOnDisk(classOrInterfaceTypeDetails);

    }

}
