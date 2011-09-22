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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.roo.addon.ws.export.WSExportWsdlConfigService.GvNIXAnnotationType;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.javaparser.JavaParserMutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.javaparser.JavaParserUtils;
import org.springframework.roo.classpath.javaparser.details.JavaParserMethodMetadata;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;

/**
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
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
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    protected MemberDetailsScanner memberDetailsScanner;

    /**
     * {@inheritDoc}
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
        serviceAnnotations.add(new AnnotationMetadataBuilder(new JavaType(
                "org.springframework.stereotype.Service"),
                new ArrayList<AnnotationAttributeValue<?>>()).build());

        ClassOrInterfaceTypeDetailsBuilder serviceDetails = new ClassOrInterfaceTypeDetailsBuilder(
                declaredByMetadataId, Modifier.PUBLIC, serviceClass,
                PhysicalTypeCategory.CLASS);
        for (AnnotationMetadata annotationMetadata : serviceAnnotations) {
            serviceDetails.addAnnotation(annotationMetadata);
        }

        typeManagementService.generateClassFile(serviceDetails.build());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only creates the class if not exists in project.
     * </p>
     */
    public void createGvNIXWebServiceClass(JavaType javaType,
            List<AnnotationMetadata> typeAnnotationList,
            GvNIXAnnotationType gvNIXAnnotationType,
            List<FieldMetadata> declaredFieldList,
            List<MethodMetadata> declaredMethodList,
            List<ConstructorMetadata> declaredConstructorList,
            List<JavaType> declaredClassList,
            PhysicalTypeCategory physicalTypeCategory,
            List<JavaSymbolName> enumConstantsList) {

        // Metadata Id.
        String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
                javaType, Path.SRC_MAIN_JAVA);

        // Determine the canonical filename
        String physicalLocationCanonicalPath = typeLocationService
                .getPhysicalLocationCanonicalPath(declaredByMetadataId);

        // Check the file doesn't already exist
        if (!fileManager.exists(physicalLocationCanonicalPath)) {

            if (!physicalTypeCategory.equals(PhysicalTypeCategory.ENUMERATION)) {
                enumConstantsList = null;
            }

            // Create class
            ClassOrInterfaceTypeDetailsBuilder serviceDetails = new ClassOrInterfaceTypeDetailsBuilder(
                    declaredByMetadataId, Modifier.PUBLIC, javaType,
                    physicalTypeCategory);
            for (AnnotationMetadata annotationMetadata : typeAnnotationList) {
                serviceDetails.addAnnotation(annotationMetadata);
            }

            for (FieldMetadata fieldMetadata : declaredFieldList) {

                serviceDetails.addField(fieldMetadata);
            }
            for (ConstructorMetadata constructorMetadata : declaredConstructorList) {

                serviceDetails.addConstructor(constructorMetadata);
            }
            for (MethodMetadata methodMetadata : declaredMethodList) {

                serviceDetails.addMethod(methodMetadata);
            }
            for (JavaType declaredClass : declaredClassList) {

                serviceDetails.addExtendsTypes(declaredClass);
            }
            if (enumConstantsList != null) {
                for (JavaSymbolName enumConstant : enumConstantsList) {

                    serviceDetails.addEnumConstant(enumConstant);
                }
            }

            typeManagementService.generateClassFile(serviceDetails.build());

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

        Assert.notNull(paramTypes, "Param type mustn't be null");
        Assert.notNull(paramNames, "Param name mustn't be null");

        // MetadataID
        String targetId = PhysicalTypeIdentifier.createIdentifier(targetType,
                Path.SRC_MAIN_JAVA);

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);
        Assert.notNull(ptm, "Java source class doesn't exists.");

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Assert.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

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

        mutableTypeDetails.addMethod(operationMetadata.build());

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
                Path.SRC_MAIN_JAVA);

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Assert.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Assert.isInstanceOf(JavaParserMutableClassOrInterfaceTypeDetails.class,
                ptd, "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));

        JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails = (JavaParserMutableClassOrInterfaceTypeDetails) ptd;

        List<MethodMetadata> updatedMethodList = new ArrayList<MethodMetadata>();

        // Add to list required method and all methods in Java

        // Search one method in all (Java and AspectJ)
        updatedMethodList.addAll(searchMethodInAll(className, method,
                annotationMetadataUpdateList, annotationWebParamMetadataList,
                targetId));

        // Search methods in Java, excepts one method
        updatedMethodList.addAll(searchMethodsInJava(method, targetId,
                mutableTypeDetails));

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
        // Add old class imports into new class to avoid undefined imports
        // Example: Not included HashSet import when exporting method in petclinic Owner
        classOrInterfaceTypeDetails.setRegisteredImports(mutableTypeDetails
                .getRegisteredImports());

        // Updates the class in file system.
        updateClass(classOrInterfaceTypeDetails.build());
    }

    /**
     * Search method in Java.
     * 
     * @param methodName
     *            Method name to search
     * @param targetId
     *            New method destination identifier
     * @param mutableTypeDetails
     *            Type to search methods in
     * @return Methods list
     */
    protected List<MethodMetadata> searchMethodsInJava(
            JavaSymbolName methodName, String targetId,
            JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails) {

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
     * 
     * <p>
     * Add into method and into method params required web service annotations.
     * </p>
     * 
     * @param className
     *            Class name to seach
     * @param method
     *            Method name to search
     * @param annotationMetadataUpdateList
     *            Method annotation list
     * @param annotationWebParamMetadataList
     *            Method params annotation list
     * @param targetId
     *            New method destination identifier
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
            // Example: Duplicated method when exporting toString method in petclinic Owner
            String mdClass = methodMetadata.getDeclaredByMetadataId()
                    .substring(
                            methodMetadata.getDeclaredByMetadataId()
                                    .lastIndexOf("?") + 1);
            String idClass = targetId.substring(targetId.lastIndexOf("?") + 1);

            if (methodMetadata.getMethodName().toString()
                    .compareTo(method.toString()) == 0
                    && mdClass.equals(idClass)) {

                Assert.isTrue(
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
     * <p>
     * Updates method using 'CompilationUnit'
     * </p>
     * <p>
     * TODO: Method to improve. Unused.
     * </p>
     */
    @Deprecated
    public void updateWithJavaDoc(JavaType className, JavaSymbolName method,
            String paramName, JavaType paramType) throws ParseException {

        String targetId = PhysicalTypeIdentifier.createIdentifier(className,
                Path.SRC_MAIN_JAVA);

        String javaIdentifier = physicalTypeMetadataProvider
                .findIdentifier(className);
        javaIdentifier = javaIdentifier.substring(
                javaIdentifier.indexOf("?") + 1).replace('.', '/');

        String fileIdentifier = projectOperations.getPathResolver()
                .getIdentifier(Path.SRC_MAIN_JAVA,
                        javaIdentifier.concat(".java"));

        // Retrieve class file to update.
        CompilationUnit compilationUnit;

        compilationUnit = JavaParser.parse(fileManager
                .getInputStream(fileIdentifier));

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Assert.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Assert.isInstanceOf(JavaParserMutableClassOrInterfaceTypeDetails.class,
                ptd, "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));

        JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails = (JavaParserMutableClassOrInterfaceTypeDetails) ptd;

        ClassOrInterfaceDeclaration clazz = null;

        for (TypeDeclaration classType : compilationUnit.getTypes()) {
            if (classType instanceof ClassOrInterfaceDeclaration) {
                clazz = (ClassOrInterfaceDeclaration) classType;
                break;
            }
        }

        if (clazz == null) {
            return;
        }

        List<BodyDeclaration> members = clazz.getMembers();

        MethodDeclaration methodToUpdate = null;

        for (BodyDeclaration bodyMember : members) {

            if (bodyMember instanceof MethodDeclaration) {

                methodToUpdate = (MethodDeclaration) bodyMember;

                if (methodToUpdate.getName().equals(method.getSymbolName())) {

                    List<Parameter> methodParameters = methodToUpdate
                            .getParameters();

                    // Compute the parameter type
                    Type parameterType = null;
                    if (paramType.isPrimitive()) {
                        parameterType = JavaParserUtils.getType(paramType);
                    } else {
                        NameExpr importedType = JavaParserUtils
                                .importTypeIfRequired(mutableTypeDetails
                                        .getEnclosingTypeName(),
                                        mutableTypeDetails.getImports(),
                                        paramType);

                        ClassOrInterfaceType cit = JavaParserUtils
                                .getClassOrInterfaceType(importedType);

                        parameterType = cit;
                    }

                    // TODO: Add annotations.
                    /*
                     * p.setAnnotations(parameterAnnotations);
                     */
                    List<AnnotationExpr> methodAnnotationList = new ArrayList<AnnotationExpr>();
                    methodAnnotationList
                            .addAll(methodToUpdate.getAnnotations());

                    List<MemberValuePair> memberList = new ArrayList<MemberValuePair>();
                    MemberValuePair annotationAttribute = new MemberValuePair();
                    annotationAttribute.setName("nuevo");
                    annotationAttribute.setValue(new NameExpr("nombre"));

                    memberList.add(annotationAttribute);

                    NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr(
                            new NameExpr("GvNIXWebService"), memberList);

                    methodAnnotationList.add(annotationExpr);

                    methodToUpdate.setAnnotations(methodAnnotationList);

                    methodParameters.add(new Parameter(parameterType,
                            new VariableDeclaratorId(paramName)));

                    break;
                }

            }
        }

        try {

            fileManager.delete(projectOperations.getPathResolver()
                    .getIdentifier(Path.SRC_MAIN_JAVA,
                            javaIdentifier.concat(".java")));

            JavaParserMutableClassOrInterfaceTypeDetails details = new JavaParserMutableClassOrInterfaceTypeDetails(
                    compilationUnit, clazz,
                    mutableTypeDetails.getDeclaredByMetadataId(), className,
                    metadataService, physicalTypeMetadataProvider, fileManager,
                    javaIdentifier);

            typeManagementService.generateClassFile(details);

        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
                Path.SRC_MAIN_JAVA);

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(targetId);

        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();

        Assert.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(targetId));
        Assert.isInstanceOf(JavaParserMutableClassOrInterfaceTypeDetails.class,
                ptd, "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(targetId));

        JavaParserMutableClassOrInterfaceTypeDetails mutableTypeDetails = (JavaParserMutableClassOrInterfaceTypeDetails) ptd;

        // Update method
        List<? extends MethodMetadata> methodsList = mutableTypeDetails
                .getDeclaredMethods();

        // Create param type.
        AnnotatedJavaType annotatedJavaType = new AnnotatedJavaType(paramType,
                null);

        // Create param name.
        JavaSymbolName parameterName = new JavaSymbolName(paramName);

        JavaParserMethodMetadata javaParserMethodMetadata;

        List<MethodMetadata> updatedMethodList = new ArrayList<MethodMetadata>();
        List<AnnotatedJavaType> parameterTypelist = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNamelist = new ArrayList<JavaSymbolName>();

        MethodMetadata operationMetadata;

        for (MethodMetadata methodMetadata : methodsList) {

            javaParserMethodMetadata = (JavaParserMethodMetadata) methodMetadata;

            if (methodMetadata.getMethodName().toString()
                    .compareTo(method.toString()) == 0) {

                Assert.isTrue(!javaParserMethodMetadata.getParameterNames()
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

            } else {

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

        List<ConstructorMetadata> contructorList = new ArrayList<ConstructorMetadata>();
        contructorList.addAll(mutableTypeDetails.getDeclaredConstructors());

        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
        fieldMetadataList.addAll(mutableTypeDetails.getDeclaredFields());

        List<AnnotationMetadata> annotationMetadataList = new ArrayList<AnnotationMetadata>();

        annotationMetadataList.addAll(mutableTypeDetails.getAnnotations());

        // Replicates the values from the original class.
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
     */
    public List<JavaType> getMethodExceptionList(JavaType serviceClass,
            JavaSymbolName methodName) {

        List<JavaType> throwList = new ArrayList<JavaType>();

        MethodMetadata methodMetadata = getMethodByNameInAll(serviceClass,
                methodName);

        if (methodMetadata != null) {
            throwList = methodMetadata.getThrowsTypes();
        }

        return throwList;
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
                .getClassOrInterface(serviceClass);

        // Checks if it's mutable
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                tmpServiceDetails,
                "Can't modify " + tmpServiceDetails.getName());

        MutableClassOrInterfaceTypeDetails serviceDetails = (MutableClassOrInterfaceTypeDetails) tmpServiceDetails;

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
     * 
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
     * 
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
     * Get the list of type details (Java and AJs) from a Java type.
     * 
     * @param name
     *            Java type to get details
     * @return List of type details
     */
    protected List<MemberHoldingTypeDetails> getMemberDetails(JavaType name) {

        String identifier = PhysicalTypeIdentifier.createIdentifier(name,
                Path.SRC_MAIN_JAVA);
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

        String javaIdentifier = physicalTypeMetadataProvider
                .findIdentifier(classOrInterfaceTypeDetails.getName());
        javaIdentifier = javaIdentifier.substring(
                javaIdentifier.indexOf("?") + 1).replaceAll("\\.", "/");

        fileManager.delete(projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_JAVA, javaIdentifier.concat(".java")));

        typeManagementService.generateClassFile(classOrInterfaceTypeDetails);

    }

}
