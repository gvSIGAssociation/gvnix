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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.JavaParserService;
import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.roo.addon.annotations.GvNIXWebParam;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElementField;
import org.gvnix.service.roo.addon.security.SecurityService;
import org.gvnix.service.roo.addon.util.WsdlParserUtils;
import org.gvnix.service.roo.addon.ws.WSCompilationUnit;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.javaparser.details.JavaParserConstructorMetadataBuilder;
import org.springframework.roo.classpath.javaparser.details.JavaParserFieldMetadataBuilder;
import org.springframework.roo.classpath.javaparser.details.JavaParserMethodMetadataBuilder;
import org.springframework.roo.file.monitor.DirectoryMonitoringRequest;
import org.springframework.roo.file.monitor.NotifiableFileMonitorService;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WSExportWsdlConfigServiceImpl implements WSExportWsdlConfigService {

    @Reference
    private WSConfigService wSConfigService;
    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private NotifiableFileMonitorService fileMonitorService;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private SecurityService securityService;

    private File schemaPackage = new File("");
    
	private static final String FOLDER_SEPARATOR = "/";

    /*
     * Variables to store web files of web service generated by maven wsdl2java
     * plugin. TODO THIS IS NO THREAD-SAFE. EXTRACT IT to a process class and
     * identify in every WsExportWsdlListener call what process refers the file
     */
    private List<File> xmlElements = new ArrayList<File>();
    private List<File> webFaults = new ArrayList<File>();
    private List<File> webServices = new ArrayList<File>();
    private List<File> webServiceInterfaces = new ArrayList<File>();

    protected static Logger logger = Logger
            .getLogger(WSExportWsdlConfigService.class.getName());

    /**
     * {@inheritDoc}
     */
    public void generateJavaFromWsdl(String wsdlLocation) {

        // Check if WSDL is RPC enconded and copy file to project
        Document wsdlDocument = checkWSDLFile(wsdlLocation);

        // Configure plugin cxf to generate java code using WSDL
        wSConfigService.addWsdlLocation(wsdlLocation, wsdlDocument);

        // Reset to generate file list
        resetGeneratedFilesList();

        try {

            // Run maven generate-sources command
            wSConfigService.mvn(WSConfigService.GENERATE_SOURCES,
                    WSConfigService.GENERATE_SOURCES_INFO);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "There is an error generating java sources with '"
                            + wsdlLocation + "'.\n" + e.getMessage());
        }

        // Remove plugin execution
        wSConfigService.disableWsdlLocation();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Monitoring file creation only.
     * </p>
     */
    public void monitorFolder(String folder) {

        // Get path to monitoring folder
        String path = projectOperations.getPathResolver().getIdentifier(
        		LogicalPath.getInstance(Path.ROOT, ""), folder);

        // Monitoring only created files
        Set<FileOperation> notify = new HashSet<FileOperation>();
        notify.add(FileOperation.CREATED);
        DirectoryMonitoringRequest dirMonitor = new DirectoryMonitoringRequest(
                new File(path), true, notify);

        // Add monitor, scan and remove it
        fileMonitorService.add(dirMonitor);
        fileMonitorService.scanAll();
        fileMonitorService.remove(dirMonitor);
    }

    /**
     * {@inheritDoc}
     */
    public List<JavaType> createGvNixClasses() {

        // Create java classes with @GvNIXXmlElement annotation
        createGvNixXmlElementsClasses();

        // Create java classes with @GvNIXWebFault annotation
        createGvNixWebFaultClasses();

        // Create java classes with @GvNIXWebService annotation
        return createGvNixWebServiceClasses();
    }

    /**
     * Create in 'src/main/java' gvNIX xml elements from wsdl2java classes list.
     * 
     * <p>
     * The wsdl2java classes list are registered by {@link WSExportWsdlListener}
     * </p>
     */
    protected void createGvNixXmlElementsClasses() {

        for (File file : xmlElements) {

            // File directory
            String path = file.getAbsolutePath();
            path = path.substring(0, path.lastIndexOf(File.separator) + 1);

            try {

                // Get first type from java file
                CompilationUnit compUnit = JavaParser.parse(file);
                List<TypeDeclaration> types = compUnit.getTypes();
                if (types != null) {

                    createGvNixXmlElementClass(types.get(0), compUnit, path);
                }
            } catch (ParseException e) {
                throw new IllegalStateException(
                        "Generated Xml Element service java file '"
                                + file.getAbsolutePath() + "' has errors:\n"
                                + e.getMessage());

            } catch (IOException e) {
                throw new IllegalStateException(
                        "Generated Xml Element service java file '"
                                + file.getAbsolutePath() + "' has errors:\n"
                                + e.getMessage());
            }

        }

        // Copy 'package-info.java' from generated to sources folder
        createPackageInfoClass();
    }

    /**
     * Copy package info file from generated folder to sources folder.
     */
    protected void createPackageInfoClass() {

        // If schema package file exists
        if (schemaPackage.exists()) {
            try {

                // Schema package file absolute path
                String path = schemaPackage.getAbsolutePath();

                // Remove sources dir folder prefix
                path = StringUtils
                        .remove(path,
                                projectOperations
                                        .getPathResolver()
                                        .getIdentifier(
                                        		LogicalPath.getInstance(Path.ROOT, ""),
                                                WSExportWsdlListener.GENERATED_CXF_SOURCES_DIR));

                // Create file into sources folder at same location
                fileManager.createOrUpdateTextFileIfRequired(
                        projectOperations.getPathResolver().getIdentifier(
                        		LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""), path), IOUtils.toString(new InputStreamReader(
                                        new FileInputStream(schemaPackage))),
                        true);

            } catch (FileNotFoundException e) {
                throw new IllegalStateException(
                        "Generated 'package-info.java' file '"
                                + schemaPackage.getAbsolutePath()
                                + "' doesn't exist:\n" + e.getMessage());
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Generated 'package-info.java' file '"
                                + schemaPackage.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());
            }
        }
    }

    /**
     * Generate declared java type classes and its inner types from declaration.
     * 
     * @param typeDecl
     *            class to convert to JavaType
     * @param compUnit
     *            Values from class to check
     * @param fileDir
     *            to check 'package-info.java' annotation values
     */
    protected void createGvNixXmlElementClass(TypeDeclaration typeDecl,
            CompilationUnit compUnit, String fileDir) {

        // Compilation unit package
        String pkg = compUnit.getPackage().getName().toString();

        // Java type to generate from
        JavaType type = new JavaType(pkg.concat(".").concat(typeDecl.getName()));

        // Initial values for gvNIX xml element annotation creation
        PhysicalTypeCategory physicalTypeCategory = PhysicalTypeCategory.CLASS;
        List<JavaSymbolName> enumConstants = new ArrayList<JavaSymbolName>();
        boolean isEnum = typeDecl instanceof EnumDeclaration;
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();

        if (isEnum) {

            // Type is an enumeration

            // Define physical type as enumeration
            physicalTypeCategory = PhysicalTypeCategory.ENUMERATION;

            // Add all enumeration constant entries names to a list
            for (EnumConstantDeclaration enumDecl : ((EnumDeclaration) typeDecl)
                    .getEntries()) {

                enumConstants.add(new JavaSymbolName(enumDecl.getName()));
            }
        } else if (typeDecl instanceof ClassOrInterfaceDeclaration) {

            // Type is a class or interface

            // For each type member
            for (BodyDeclaration body : typeDecl.getMembers()) {

                if (body instanceof FieldDeclaration) {

                    // Member is a field: add gvNIX xml element field annotation
                    loadFieldFromType(PhysicalTypeIdentifier.createIdentifier(
                            type, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, "")), new WSCompilationUnit(
                            new JavaPackage(pkg), type, compUnit.getImports(),
                            new ArrayList<TypeDeclaration>(),
                            PhysicalTypeCategory.CLASS), fields, body);

                } else if (body instanceof ClassOrInterfaceDeclaration) {

                    // Member is class or interface: add gvNIX xml element annot
                    createGvNixXmlElementClass((TypeDeclaration) body,
                            compUnit, fileDir);
                }
            }
        }

        List<AnnotationMetadata> annots = new ArrayList<AnnotationMetadata>();

        // ROO annotation to generate get and set methods if is not an enum
        if (!isEnum) {

            annots.add(new AnnotationMetadataBuilder(new JavaType(
                    "org.springframework.roo.addon.javabean.RooJavaBean"),
                    new ArrayList<AnnotationAttributeValue<?>>()).build());
        }

        // Create gvNIX xml element annotation and add it to list
        annots.add(getGvNixXmlElementAnnot(typeDecl));

        // Create gvNIX web service type with annots, fields, type and enums
        javaParserService.createGvNixWebServiceClass(type, annots,
                GvNIXAnnotationType.XML_ELEMENT, fields,
                new ArrayList<MethodMetadata>(),
                new ArrayList<ConstructorMetadata>(),
                new ArrayList<JavaType>(), physicalTypeCategory, enumConstants);
    }

    /**
     * Load field declaration from a type
     * 
     * @param id
     * @param compUnit
     * @param fields
     * @param body
     */
    private void loadFieldFromType(String id, WSCompilationUnit compUnit,
            List<FieldMetadata> fields, BodyDeclaration body) {

        // Fields from Inner Class type defined.
        FieldDeclaration field = (FieldDeclaration) body;
        if (field.getType() instanceof ReferenceType) {

            ReferenceType refType = (ReferenceType) field.getType();
            if (refType.getType() instanceof ClassOrInterfaceType) {

                ClassOrInterfaceType type = (ClassOrInterfaceType) refType
                        .getType();
                type.setScope(null);
                field.setType(type);
            }
        }

        FieldDeclaration fieldDecl = new FieldDeclaration(field.getJavaDoc(),
                field.getModifiers(), field.getAnnotations(), field.getType(),
                field.getVariables());

        for (VariableDeclarator var : fieldDecl.getVariables()) {

            // Set var initilize to null beacuse if implements an
            // interface the class is not well generated.
            var.setInit(null);
            
            fields.add(getGvNIXXmlElementFieldAnnotation(JavaParserFieldMetadataBuilder.getInstance(
                    id, fieldDecl, var, compUnit, new HashSet<JavaSymbolName>()).build()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createGvNixWebFaultClasses() {

        List<AnnotationMetadata> gvNixAnnotationList;

        // GvNIXWebFault annotation.
        AnnotationMetadata gvNIXWebFaultAnnotation;

        for (File webFaultFile : webFaults) {

            // Parse Java file.
            CompilationUnit compilationUnit;
            PackageDeclaration packageDeclaration;
            JavaType javaType;
            String declaredByMetadataId;
            // CompilationUnitServices to create the class in fileSystem.
            WSCompilationUnit compilationUnitServices;

            gvNixAnnotationList = new ArrayList<AnnotationMetadata>();
            try {
                compilationUnit = JavaParser.parse(webFaultFile);
                packageDeclaration = compilationUnit.getPackage();

                String packageName = packageDeclaration.getName().toString();

                // Get the first class or interface Java type
                List<TypeDeclaration> types = compilationUnit.getTypes();
                if (types != null) {
                    TypeDeclaration type = types.get(0);
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
                    if (type instanceof ClassOrInterfaceDeclaration) {

                        javaType = new JavaType(packageName.concat(".").concat(
                                type.getName()));

                        classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;

                        declaredByMetadataId = PhysicalTypeIdentifier
                                .createIdentifier(javaType, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

                        // Retrieve correct values.
                        // Get field declarations.
                        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
                        List<ConstructorMetadata> constructorMetadataList = new ArrayList<ConstructorMetadata>();
                        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();

                        // Extended classes.
                        List<JavaType> extendedClassesList = new ArrayList<JavaType>();
                        List<ClassOrInterfaceType> extendsClasses = classOrInterfaceDeclaration
                                .getExtends();
                        JavaType extendedJavaType;
                        for (ClassOrInterfaceType classOrInterfaceType : extendsClasses) {
                            extendedJavaType = new JavaType(
                                    classOrInterfaceType.getName());
                            extendedClassesList.add(extendedJavaType);
                        }

                        // CompilationUnitServices to create the class.
                        compilationUnitServices = new WSCompilationUnit(
                                new JavaPackage(compilationUnit.getPackage()
                                        .getName().toString()), javaType,
                                compilationUnit.getImports(),
                                new ArrayList<TypeDeclaration>(),
                                PhysicalTypeCategory.CLASS);

                        for (BodyDeclaration bodyDeclaration : classOrInterfaceDeclaration
                                .getMembers()) {

                            if (bodyDeclaration instanceof FieldDeclaration) {

                                loadFaultFieldDeclaration(declaredByMetadataId,
                                        compilationUnitServices,
                                        fieldMetadataList, bodyDeclaration);

                            } else if (bodyDeclaration instanceof ConstructorDeclaration) {

                                loadFaultConstructorDeclaration(
                                        declaredByMetadataId,
                                        compilationUnitServices,
                                        constructorMetadataList,
                                        bodyDeclaration);

                            } else if (bodyDeclaration instanceof MethodDeclaration) {

                                loadFaultMethodDeclaration(
                                        declaredByMetadataId,
                                        compilationUnitServices,
                                        methodMetadataList, (MethodDeclaration)bodyDeclaration);
                            }
                        }

                        // GvNIXWebFault Annotation.Get all annotations.
                        gvNIXWebFaultAnnotation = getGvNIXWebFaultAnnotation(
                                classOrInterfaceDeclaration, javaType);

                        gvNixAnnotationList.add(gvNIXWebFaultAnnotation);

                        javaParserService.createGvNixWebServiceClass(javaType,
                                gvNixAnnotationList,
                                GvNIXAnnotationType.WEB_FAULT,
                                fieldMetadataList, methodMetadataList,
                                constructorMetadataList, extendedClassesList,
                                PhysicalTypeCategory.CLASS, null);
                    }
                }

            } catch (ParseException e) {
                throw new IllegalStateException(
                        "Generated Web Fault service Element java file '"
                                + webFaultFile.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());

            } catch (IOException e) {
                throw new IllegalStateException(
                        "Generated Web Fault service Element java file '"
                                + webFaultFile.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());
            }

        }
    }

    /**
     * Loads fault method declaration into method list to generate fault web
     * service class
     * 
     * @param declaredByMetadataId
     * @param compilationUnitServices
     * @param methodMetadataList
     * @param bodyDeclaration
     */
    private void loadFaultMethodDeclaration(String declaredByMetadataId,
            WSCompilationUnit compilationUnitServices,
            List<MethodMetadata> methodMetadataList,
            MethodDeclaration methodDeclaration) {
        MethodMetadata tmpMethodMetadata;

        tmpMethodMetadata = JavaParserMethodMetadataBuilder.getInstance(declaredByMetadataId,
                methodDeclaration, compilationUnitServices,
                new HashSet<JavaSymbolName>()).build();

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(tmpMethodMetadata.getBody());
        MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                declaredByMetadataId, tmpMethodMetadata.getModifier(),
                tmpMethodMetadata.getMethodName(),
                tmpMethodMetadata.getReturnType(),
                tmpMethodMetadata.getParameterTypes(),
                tmpMethodMetadata.getParameterNames(), bodyBuilder);
        for (AnnotationMetadata annotationMetadata : tmpMethodMetadata
                .getAnnotations()) {
            methodMetadataBuilder.addAnnotation(annotationMetadata);
        }
        for (JavaType myJavaType : tmpMethodMetadata.getThrowsTypes()) {
            methodMetadataBuilder.addThrowsType(myJavaType);
        }

        methodMetadataList.add(methodMetadataBuilder.build());
    }

    /**
     * Loads fault constructor declaration into contructor list to generate
     * fault web service class
     * 
     * @param declaredByMetadataId
     * @param compilationUnitServices
     * @param constructorMetadataList
     * @param bodyDeclaration
     */
    private void loadFaultConstructorDeclaration(String declaredByMetadataId,
            WSCompilationUnit compilationUnitServices,
            List<ConstructorMetadata> constructorMetadataList,
            BodyDeclaration bodyDeclaration) {
        ConstructorMetadata constructorMetadata;
        ConstructorDeclaration constructorDeclaration;
        constructorDeclaration = (ConstructorDeclaration) bodyDeclaration;

        // TODO ??? what is this for? !!!
        JavaParserConstructorMetadataBuilder.getInstance(declaredByMetadataId,
                constructorDeclaration, compilationUnitServices,
                new HashSet<JavaSymbolName>());

        ConstructorMetadataBuilder constructorMetadataBuilder = new ConstructorMetadataBuilder(
                declaredByMetadataId);
        constructorMetadata = constructorMetadataBuilder.build();

        constructorMetadataList.add(constructorMetadata);
    }

    /**
     * Loads fault field declaration into field list to generate fault web
     * service class
     * 
     * @param declaredByMetadataId
     * @param compilationUnitServices
     * @param fieldMetadataList
     * @param bodyDeclaration
     */
    private void loadFaultFieldDeclaration(String declaredByMetadataId,
            WSCompilationUnit compilationUnitServices,
            List<FieldMetadata> fieldMetadataList,
            BodyDeclaration bodyDeclaration) {
        FieldMetadata fieldMetadata;
        FieldDeclaration fieldDeclaration;
        fieldDeclaration = (FieldDeclaration) bodyDeclaration;

        for (VariableDeclarator var : fieldDeclaration.getVariables()) {

            fieldMetadata = JavaParserFieldMetadataBuilder.getInstance(declaredByMetadataId,
                    fieldDeclaration, var, compilationUnitServices,
                    new HashSet<JavaSymbolName>()).build();

            fieldMetadataList.add(fieldMetadata);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<JavaType> createGvNixWebServiceClasses() {

        List<AnnotationMetadata> gvNixAnnotationList;

        List<JavaType> implementationClasses = new ArrayList<JavaType>();

        // GvNIXWebService annotation.
        AnnotationMetadata gvNIXWebServiceAnnotation;

        String fileDirectory;
        int lastPathSepratorIndex = 0;
        for (File webServiceFile : webServices) {

            fileDirectory = webServiceFile.getAbsolutePath();
            lastPathSepratorIndex = fileDirectory.lastIndexOf(File.separator);
            fileDirectory = fileDirectory.substring(0,
                    lastPathSepratorIndex + 1);

            // Parse Java file.
            CompilationUnit compilationUnit;
            PackageDeclaration packageDeclaration;
            JavaType javaType;
            String declaredByMetadataId;
            // CompilationUnitServices to create the class in fileSystem.
            WSCompilationUnit compilationUnitServices;

            gvNixAnnotationList = new ArrayList<AnnotationMetadata>();
            try {
                compilationUnit = JavaParser.parse(webServiceFile);
                packageDeclaration = compilationUnit.getPackage();

                String packageName = packageDeclaration.getName().toString();

                List<TypeDeclaration> types = compilationUnit.getTypes();
                if (types == null) {
                    // nothing to do
                    return implementationClasses;
                }

                // Get the first class or interface Java type
                TypeDeclaration type = types.get(0);
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
                ClassOrInterfaceDeclaration implementedInterface;

                if (type instanceof ClassOrInterfaceDeclaration) {

                    javaType = new JavaType(packageName.concat(".").concat(
                            type.getName()));

                    classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;

                    declaredByMetadataId = PhysicalTypeIdentifier
                            .createIdentifier(javaType, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

                    // Retrieve implemented interface.
                    implementedInterface = getWebServiceInterface(classOrInterfaceDeclaration);

                    // Retrieve correct values.
                    // Get field declarations.
                    List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
                    List<ConstructorMetadata> constructorMetadataList = new ArrayList<ConstructorMetadata>();
                    List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();

                    // Extended classes.
                    List<JavaType> extendedClassesList = new ArrayList<JavaType>();

                    // CompilationUnitServices to create the class.
                    compilationUnitServices = new WSCompilationUnit(
                            new JavaPackage(compilationUnit.getPackage()
                                    .getName().toString()), javaType,
                            compilationUnit.getImports(),
                            new ArrayList<TypeDeclaration>(),
                            PhysicalTypeCategory.CLASS);

                    // GvNIXWebFault Annotation.Get all annotations.
                    gvNIXWebServiceAnnotation = getGvNIXWebServiceAnnotation(
                            classOrInterfaceDeclaration, implementedInterface,
                            javaType);

                    gvNixAnnotationList.add(gvNIXWebServiceAnnotation);

                    // Default Web Service Namespace.
                    AnnotationAttributeValue<?> targetNamespaceAnnotationAttributeValue = gvNIXWebServiceAnnotation
                            .getAttribute(new JavaSymbolName("targetNamespace"));
                    String defaultNamespace = ((StringAttributeValue) targetNamespaceAnnotationAttributeValue)
                            .getValue();

                    // @GvNIXWebMethod annotations.
                    for (BodyDeclaration bodyDeclaration : classOrInterfaceDeclaration
                            .getMembers()) {

                        if (bodyDeclaration instanceof FieldDeclaration) {

                            loadWebServiceFieldDeclaration(
                                    declaredByMetadataId,
                                    compilationUnitServices, fieldMetadataList,
                                    bodyDeclaration);

                        } else if (bodyDeclaration instanceof MethodDeclaration) {

                            loadWebServiceMethodDeclaration(
                                    declaredByMetadataId,
                                    compilationUnitServices,
                                    implementedInterface, methodMetadataList,
                                    defaultNamespace, bodyDeclaration);

                        }

                    }

                    javaParserService.createGvNixWebServiceClass(javaType,
                            gvNixAnnotationList,
                            GvNIXAnnotationType.WEB_SERVICE, fieldMetadataList,
                            methodMetadataList, constructorMetadataList,
                            extendedClassesList, PhysicalTypeCategory.CLASS,
                            null);
                    implementationClasses.add(javaType);
                }

            } catch (ParseException e) {
                throw new IllegalStateException(
                        "Generated Web Service java file '"
                                + webServiceFile.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(
                        "Generated Web Service java file '"
                                + webServiceFile.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());

            }

        }
        return implementationClasses;
    }

    /**
     * Load WebService method declaration and complete with GvNIX annotations
     * 
     * @param declaredByMetadataId
     * @param compilationUnitServices
     * @param implementedInterface
     * @param methodMetadataList
     * @param defaultNamespace
     * @param bodyDeclaration
     */
    private void loadWebServiceMethodDeclaration(String declaredByMetadataId,
            WSCompilationUnit compilationUnitServices,
            ClassOrInterfaceDeclaration implementedInterface,
            List<MethodMetadata> methodMetadataList, String defaultNamespace,
            BodyDeclaration bodyDeclaration) {
        MethodMetadata methodMetadata;
        MethodMetadata tmpMethodMetadata;
        MethodDeclaration methodDeclaration;
        methodDeclaration = (MethodDeclaration) bodyDeclaration;

        tmpMethodMetadata = JavaParserMethodMetadataBuilder.getInstance(declaredByMetadataId,
                methodDeclaration, compilationUnitServices,
                new HashSet<JavaSymbolName>()).build();
                
        // Check method from interface because Web
        // Service
        // Annotations are defined there
        for (BodyDeclaration interfacebodyDeclaration : implementedInterface
                .getMembers()) {

            MethodDeclaration interfaceMethodDeclaration;

            if (interfacebodyDeclaration instanceof MethodDeclaration) {

                interfaceMethodDeclaration = (MethodDeclaration) interfacebodyDeclaration;

                if (interfaceMethodDeclaration.getName().contentEquals(
                        methodDeclaration.getName())) {

                    MethodMetadata interfaceTmpMethodMetadata = JavaParserMethodMetadataBuilder.getInstance(
                            declaredByMetadataId, interfaceMethodDeclaration,
                            compilationUnitServices,
                            new HashSet<JavaSymbolName>()).build();

                    MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                            declaredByMetadataId,
                            interfaceTmpMethodMetadata.getModifier(),
                            interfaceTmpMethodMetadata.getMethodName(),
                            interfaceTmpMethodMetadata.getReturnType(),
                            interfaceTmpMethodMetadata.getParameterTypes(),
                            interfaceTmpMethodMetadata.getParameterNames(),
                            new InvocableMemberBodyBuilder()
                                    .appendFormalLine(tmpMethodMetadata
                                            .getBody()));
                    for (AnnotationMetadata annotationMetadata : interfaceTmpMethodMetadata
                            .getAnnotations()) {
                        methodMetadataBuilder.addAnnotation(annotationMetadata);
                    }
                    for (JavaType myJavaType : interfaceTmpMethodMetadata
                            .getThrowsTypes()) {
                        methodMetadataBuilder.addThrowsType(myJavaType);
                    }
                    interfaceTmpMethodMetadata = methodMetadataBuilder.build();

                    // Web Service method Operation.
                    methodMetadata = getGvNIXWebMethodMetadata(
                            interfaceTmpMethodMetadata, defaultNamespace);

                    methodMetadataList.add(methodMetadata);

                }
            }
        }
    }

    /**
     * Load WebService Field declaration and complete with GvNIX annotations
     * 
     * @param declaredByMetadataId
     * @param compilationUnitServices
     * @param fieldMetadataList
     * @param bodyDeclaration
     */
    private void loadWebServiceFieldDeclaration(String declaredByMetadataId,
            WSCompilationUnit compilationUnitServices,
            List<FieldMetadata> fieldMetadataList,
            BodyDeclaration bodyDeclaration) {
        FieldMetadata fieldMetadata;
        FieldDeclaration tmpFieldDeclaration;
        FieldDeclaration fieldDeclaration;
        tmpFieldDeclaration = (FieldDeclaration) bodyDeclaration;
        fieldDeclaration = new FieldDeclaration(
                tmpFieldDeclaration.getJavaDoc(),
                tmpFieldDeclaration.getModifiers(),
                new ArrayList<AnnotationExpr>(), tmpFieldDeclaration.getType(),
                tmpFieldDeclaration.getVariables());

        for (VariableDeclarator var : fieldDeclaration.getVariables()) {

            fieldMetadata = JavaParserFieldMetadataBuilder.getInstance(declaredByMetadataId,
                    fieldDeclaration, var, compilationUnitServices,
                    new HashSet<JavaSymbolName>()).build();

            fieldMetadataList.add(fieldMetadata);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws ParseException
     */
    public ClassOrInterfaceDeclaration getWebServiceInterface(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration)
            throws ParseException, IOException {

        ClassOrInterfaceDeclaration implementedInterface = null;

        List<ClassOrInterfaceType> implementedInterfacesList = classOrInterfaceDeclaration
                .getImplements();

        String interfaceName;
        String fileInterfaceName;

        CompilationUnit compilationUnit;
        TypeDeclaration type;

        for (ClassOrInterfaceType classOrInterfaceType : implementedInterfacesList) {

            interfaceName = classOrInterfaceType.getName();
            interfaceName = interfaceName.concat(".java");

            for (File interfaceFile : webServiceInterfaces) {
                fileInterfaceName = getFilename(interfaceFile
                        .getAbsolutePath());

                if (!fileInterfaceName.contentEquals(interfaceName)) {
                    continue;
                }

                compilationUnit = JavaParser.parse(interfaceFile);

                List<TypeDeclaration> types = compilationUnit.getTypes();
                if (types == null) {
                    continue;
                }

                type = types.get(0);
                if (type instanceof ClassOrInterfaceDeclaration) {

                    implementedInterface = (ClassOrInterfaceDeclaration) type;
                    return implementedInterface;
                }
            }

        }

        return implementedInterface;
    }

    /**
     * Convert annotation @XmlElement values to GvNIXXmlElement.
     * 
     * <p>
     * Searches for Jaxb annotations in {@link ClassOrInterfaceDeclaration} to
     * convert values to {@link GvNIXXmlElement}.
     * </p>
     * 
     * @param typeDecl
     *            To retrieve values from @XmlElement annotations
     * @return {@link GvNIXXmlElement} to define in class
     */
    protected AnnotationMetadata getGvNixXmlElementAnnot(
            TypeDeclaration typeDecl) {

        // Attribute value list.
        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        boolean isNamespace = false;
        boolean isPropOrder = false;

        for (AnnotationExpr typeAnnot : typeDecl.getAnnotations()) {

            if (typeAnnot instanceof NormalAnnotationExpr) {

                NormalAnnotationExpr normalAnnot = (NormalAnnotationExpr) typeAnnot;
                String name = normalAnnot.getName().getName();
                List<MemberValuePair> pairs = normalAnnot.getPairs();
                if (name.contains(WSExportWsdlListener.xmlRootElement)) {

                    // It's @XmlRootElement
                    addNameAttr(attrs, pairs);

                } else if (name.contains(WSExportWsdlListener.xmlType)) {

                    // It's @XmlType

                    for (MemberValuePair pair : pairs) {

                        if (pair.getName().contentEquals("name")) {

                            // @XmlType.name
                            addXmlTypeNameAttr(attrs, pair);

                        } else if (pair.getName().contentEquals("propOrder")) {

                            // @XmlType.propOrder
                            addElementListAttr(attrs, pair);
                            isPropOrder = true;

                        } else if (pair.getName().contentEquals("namespace")) {

                            // @XmlType.namespace
                            addNamespaceAttr(attrs, pair);
                            isNamespace = true;
                        }
                    }
                }
            }
        }

        // Check correct values for @GvNIXXmlElement.
        if (!isPropOrder) {

            addElementListAttr(attrs);
        }

        if (!isNamespace) {

            addNamespaceAttr(attrs);
        }

        addExportedAttr(attrs);

        addEnumElementAttr(typeDecl, attrs);

        // Create annotation
        return new AnnotationMetadataBuilder(new JavaType(
                GvNIXXmlElement.class.getName()), attrs).build();
    }

    /**
     * Search name attributes on pairs and add it as name attrs.
     * 
     * @param attrs
     *            Annotation attributes to add names
     * @param pairs
     *            Pairs to search in name attributes
     */
    protected void addNameAttr(List<AnnotationAttributeValue<?>> attrs,
            List<MemberValuePair> pairs) {

        // Search name attribute in pairs
        for (MemberValuePair pair : pairs) {
            if (pair.getName().contentEquals("name")) {

                // Add name to attributes list
                attrs.add(new StringAttributeValue(new JavaSymbolName("name"),
                        ((StringLiteralExpr) pair.getValue()).getValue()));
                return;
            }
        }
    }

    /**
     * If pair has text, add it as xml type name attr.
     * 
     * @param attrs
     *            Annotation attributes to add xml type name
     * @param pair
     *            Pair to test has text
     */
    protected void addXmlTypeNameAttr(List<AnnotationAttributeValue<?>> attrs,
            MemberValuePair pair) {

        if (StringUtils.isNotBlank(pair.getValue().toString())) {

            attrs.add(new StringAttributeValue(
                    new JavaSymbolName("xmlTypeName"),
                    ((StringLiteralExpr) pair.getValue()).getValue()));
        }
    }

    /**
     * Get all pair values and add it as ignored attrs.
     * 
     * @param annotAttrs
     *            Annotation attributes to add ignored attrs
     * @param pairs
     *            Pair to get values
     */
    protected void addElementListAttr(
            List<AnnotationAttributeValue<?>> annotAttrs, MemberValuePair pair) {

        List<StringAttributeValue> attrs = new ArrayList<StringAttributeValue>();
        for (Expression value : ((ArrayInitializerExpr) pair.getValue())
                .getValues()) {

            attrs.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                    ((StringLiteralExpr) value).getValue()));
        }

        annotAttrs.add(new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("elementList"), attrs));
    }

    /**
     * Add pair value with namespace name into attributes list.
     * 
     * @param attrs
     *            Attributes list
     * @param pair
     *            Pair
     */
    protected void addNamespaceAttr(List<AnnotationAttributeValue<?>> attrs,
            MemberValuePair pair) {

        attrs.add(new StringAttributeValue(new JavaSymbolName("namespace"),
                ((StringLiteralExpr) pair.getValue()).getValue()));
    }

    /**
     * Add empty ignored attribute to list.
     * 
     * @param annotAttrs
     *            Attributes list
     * @param pair
     *            Pair
     */
    protected void addElementListAttr(
            List<AnnotationAttributeValue<?>> annotAttrs) {

        List<StringAttributeValue> attrs = new ArrayList<StringAttributeValue>();
        attrs.add(new StringAttributeValue(new JavaSymbolName("ignored"), ""));
        annotAttrs.add(new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("elementList"), attrs));
    }

    protected void addNamespaceAttr(List<AnnotationAttributeValue<?>> annotAttrs) {

        try {

            String ns = "";

            boolean exists = false;
            List<AnnotationExpr> annots = JavaParser.parse(schemaPackage)
                    .getPackage().getAnnotations();
            for (AnnotationExpr annot : annots) {
                if (annot instanceof NormalAnnotationExpr) {

                    NormalAnnotationExpr normalAnnot = (NormalAnnotationExpr) annot;
                    if (normalAnnot.getName().toString()
                            .contains("javax.xml.bind.annotation.XmlSchema")) {

                        // @XmlSchema

                        for (MemberValuePair pair : normalAnnot.getPairs()) {
                            if (pair.getName().contentEquals("namespace")) {

                                ns = ((StringLiteralExpr) pair.getValue())
                                        .getValue();
                                exists = true;
                                break;
                            }
                        }
                    }
                }

                if (exists) {
                    break;
                }
            }

            // look for @XmlSchema
            annotAttrs.add(new StringAttributeValue(new JavaSymbolName(
                    "namespace"), ns));

        } catch (ParseException e) {
            throw new IllegalStateException(
                    "Generated Xml Element service java file has errors:\n"
                            + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Generated Xml Element service java file has errors:\n"
                            + e.getMessage());
        }
    }

    /**
     * Add true exported attribute to list.
     * 
     * @param annotAttrs
     *            Attributes list
     * @param pair
     *            Pair
     */
    protected void addExportedAttr(List<AnnotationAttributeValue<?>> attrs) {

        // Exported attr: always true to know when export from WSDL
        attrs.add(new BooleanAttributeValue(new JavaSymbolName("exported"),
                true));
    }

    /**
     * Add true/false enum element attribute to list when if is/isnot enum.
     * 
     * @param typeDecl
     *            Type declaration
     * @param annotAttrs
     *            Attributes list
     */
    protected void addEnumElementAttr(TypeDeclaration typeDecl,
            List<AnnotationAttributeValue<?>> annotAttrs) {

        // Check if is an Enum class
        if (typeDecl instanceof EnumDeclaration) {
            annotAttrs.add(new BooleanAttributeValue(new JavaSymbolName(
                    "enumElement"), true));
        } else {
            annotAttrs.add(new BooleanAttributeValue(new JavaSymbolName(
                    "enumElement"), false));
        }
    }

    /**
     * {@inheritDoc}
     */
    public FieldMetadata getGvNIXXmlElementFieldAnnotation(FieldMetadata field) {

        AnnotationMetadata gvNixXmlElementAnnot;

        AnnotationMetadata xmlElementAnnot = MemberFindingUtils
                .getAnnotationOfType(field.getAnnotations(), new JavaType(
                        XmlElement.class.getName()));
        if (xmlElementAnnot != null) {

            // Field has XmlElement annotation: create GvNIXXmlElement from it
            gvNixXmlElementAnnot = getXmlElementFieldAnnotation(xmlElementAnnot);

        } else {

            AnnotationMetadata xmlElementRefAnnot = MemberFindingUtils
                    .getAnnotationOfType(field.getAnnotations(), new JavaType(
                            XmlElementRef.class.getName()));
            if (xmlElementRefAnnot != null) {

                // Field has XmlElementRef: create GvNIXXmlElement from it
                gvNixXmlElementAnnot = getXmlElementRefFieldAnnotation(field);

            } else {

                // Field no XmlElement, XmlElementRef: create empty
                // GvNIXXmlElement
                gvNixXmlElementAnnot = new AnnotationMetadataBuilder(
                        new JavaType(GvNIXXmlElementField.class.getName()),
                        new ArrayList<AnnotationAttributeValue<?>>()).build();
            }
        }

        List<AnnotationMetadata> annots = new ArrayList<AnnotationMetadata>();
        annots.add(gvNixXmlElementAnnot);

        // Create new Field with GvNIXAnnotation.
        FieldMetadataBuilder fieldMetadataBuilder = new FieldMetadataBuilder(
                field.getDeclaredByMetadataId(), field.getModifier(),
                field.getFieldName(), field.getFieldType(),
                field.getFieldInitializer());
        for (AnnotationMetadata annotationMetadata : annots) {
            fieldMetadataBuilder.addAnnotation(annotationMetadata);
        }

        return fieldMetadataBuilder.build();
    }

    /**
     * Get GvNIXXmlElement annotation related with XmlElement annotation.
     * 
     * <p>
     * Creates the GvNIXXmlElement with all attributes defined in XmlElement
     * annotation.
     * </p>
     * 
     * @param annot
     *            XmlElement annotation
     * @return GvNIXXmlElement annotation
     */
    protected AnnotationMetadata getXmlElementFieldAnnotation(
            AnnotationMetadata annot) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        for (JavaSymbolName attrName : annot.getAttributeNames()) {
            attrs.add(annot.getAttribute(attrName));
        }

        return new AnnotationMetadataBuilder(new JavaType(
                GvNIXXmlElementField.class.getName()), attrs).build();
    }

    /**
     * Get GvNIXXmlElement annotation related with XmlElementRef annotation.
     * 
     * <p>
     * Only when field is of JAXBElement type, creates the GvNIXXmlElement with
     * a "type" attribute with the first parameter type value.
     * </p>
     * 
     * @param xmlElementAnnotation
     *            XmlElementRef annotation
     * @return GvNIXXmlElement annotation
     */
    protected AnnotationMetadata getXmlElementRefFieldAnnotation(
            FieldMetadata field) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        JavaType type = field.getFieldType();
        List<JavaType> params = type.getParameters();
        if (type.getFullyQualifiedTypeName()
                .equals(JAXBElement.class.getName()) && !params.isEmpty()) {

            attrs.add(new ClassAttributeValue(new JavaSymbolName("type"),
                    params.get(0)));
        }

        return new AnnotationMetadataBuilder(new JavaType(
                GvNIXXmlElementField.class.getName()), attrs).build();
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationMetadata getGvNIXWebFaultAnnotation(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            JavaType exceptionType) {

        AnnotationMetadata gvNIXWebFaultAnnotationMetadata;
        List<AnnotationAttributeValue<?>> gvNIXWebFaultAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

        // @WebFault(name = "faultDetail", targetNamespace =
        // "http://apache.org/hello_world_soap_http/types")
        List<AnnotationExpr> annotationExprList = classOrInterfaceDeclaration
                .getAnnotations();

        String faultBeanWebFault = exceptionType.getFullyQualifiedTypeName();

        for (AnnotationExpr annotationExpr : annotationExprList) {

            if (annotationExpr instanceof NormalAnnotationExpr) {

                NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;

                StringAttributeValue nameStringAttributeValue;

                StringAttributeValue targetNamespaceStringAttributeValue;

                // Retrieve values.
                for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {

                    if (pair.getName().contentEquals("name")) {

                        // name
                        nameStringAttributeValue = new StringAttributeValue(
                                new JavaSymbolName("name"),
                                ((StringLiteralExpr) pair.getValue())
                                        .getValue());

                        gvNIXWebFaultAnnotationAttributes
                                .add(nameStringAttributeValue);
                    } else if (pair.getName().contentEquals("targetNamespace")) {

                        // targetNamespace
                        targetNamespaceStringAttributeValue = new StringAttributeValue(
                                new JavaSymbolName("targetNamespace"),
                                ((StringLiteralExpr) pair.getValue())
                                        .getValue());

                        gvNIXWebFaultAnnotationAttributes
                                .add(targetNamespaceStringAttributeValue);
                    }

                }
            }
        }

        // faultBean
        gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
                new JavaSymbolName("faultBean"), faultBeanWebFault));

        // Create GvNIXWebFault annotation.
        gvNIXWebFaultAnnotationMetadata = new AnnotationMetadataBuilder(
                new JavaType(GvNIXWebFault.class.getName()),
                gvNIXWebFaultAnnotationAttributes).build();

        return gvNIXWebFaultAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationMetadata getGvNIXWebServiceAnnotation(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            ClassOrInterfaceDeclaration implementedInterface, JavaType javaType) {

        AnnotationMetadata gvNIXWebServiceAnnotationMetadata;
        List<AnnotationAttributeValue<?>> gvNIXWebServiceAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

        /*
         * Class:
         * 
         * @javax.jws.WebService( serviceName = "TempConvert", portName =
         * "TempConvertSoap12", targetNamespace = "http://tempuri.org/",
         * wsdlLocation =
         * "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL",
         * endpointInterface = "org.tempuri.TempConvertSoap") Interface:
         * 
         * @WebService(targetNamespace = "http://tempuri.org/", name =
         * "TempConvertSoap") Result:
         * 
         * @WebService(name = "TempConvertSoap",portName = "TempConvertSoap12",
         * targetNamespace = "http://tempuri.org/", serviceName =
         * "TempConvert");
         * 
         * @WebService(targetNamespace =
         * "http://fps.amazonaws.com/doc/2008-09-17/", name =
         * "AmazonFPSPortType")
         * 
         * @XmlSeeAlso({ObjectFactory.class})
         * 
         * @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
         */

        /*
         * @GvNIXWebService address = X name.
         */

        // Search for interface annotation attribute values.
        List<AnnotationExpr> annotationInterfaceExprList = implementedInterface
                .getAnnotations();

        for (AnnotationExpr annotationExpr : annotationInterfaceExprList) {

            if (annotationExpr instanceof NormalAnnotationExpr) {

                NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;

                StringAttributeValue addressStringAttributeValue;

                if (normalAnnotationExpr.getName().getName()
                        .contains(WSExportWsdlListener.webServiceInterface)) {

                    // Retrieve values.
                    for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {

                        if (pair.getName().contentEquals("name")) {

                            // address
                            addressStringAttributeValue = new StringAttributeValue(
                                    new JavaSymbolName("name"),
                                    ((StringLiteralExpr) pair.getValue())
                                            .getValue());

                            gvNIXWebServiceAnnotationAttributes
                                    .add(addressStringAttributeValue);
                            break;
                        }
                    }
                } else if (normalAnnotationExpr.getName().getName()
                        .contains(WSExportWsdlListener.soapBinding)) {

                    for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {

                        EnumAttributeValue enumparameterStyleAttributeValue = new EnumAttributeValue(
                                new JavaSymbolName("parameterStyle"),
                                new EnumDetails(
                                        new JavaType(
                                                "org.gvnix.service.roo.addon.annotations.GvNIXWebService.GvNIXWebServiceParameterStyle"),
                                        new JavaSymbolName("WRAPPED")));

                        if (pair.getName().contentEquals("parameterStyle")) {

                            enumparameterStyleAttributeValue = new EnumAttributeValue(
                                    new JavaSymbolName("parameterStyle"),
                                    new EnumDetails(
                                            new JavaType(
                                                    "org.gvnix.service.roo.addon.annotations.GvNIXWebService.GvNIXWebServiceParameterStyle"),
                                            new JavaSymbolName(
                                                    ((FieldAccessExpr) pair
                                                            .getValue())
                                                            .getField())));

                            gvNIXWebServiceAnnotationAttributes
                                    .add(enumparameterStyleAttributeValue);
                        }
                    }
                }
            }
        }

        /*
         * @GvNIXWebService name = class portName. serviceName = class
         * serviceName. targetNamespace = class targetNamespace.
         */
        List<AnnotationExpr> annotationExprList = classOrInterfaceDeclaration
                .getAnnotations();

        for (AnnotationExpr annotationExpr : annotationExprList) {

            if (annotationExpr instanceof NormalAnnotationExpr) {

                NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;

                StringAttributeValue nameStringAttributeValue;

                StringAttributeValue targetNamespaceStringAttributeValue;

                StringAttributeValue serviceNameStringAttributeValue;

                // Retrieve values.
                for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {

                    if (pair.getName().contentEquals("serviceName")) {

                        // serviceName
                        serviceNameStringAttributeValue = new StringAttributeValue(
                                new JavaSymbolName("serviceName"),
                                ((StringLiteralExpr) pair.getValue())
                                        .getValue());

                        gvNIXWebServiceAnnotationAttributes
                                .add(serviceNameStringAttributeValue);
                        continue;
                    } else if (pair.getName().contentEquals("targetNamespace")) {

                        // targetNamespace
                        targetNamespaceStringAttributeValue = new StringAttributeValue(
                                new JavaSymbolName("targetNamespace"),
                                ((StringLiteralExpr) pair.getValue())
                                        .getValue());

                        gvNIXWebServiceAnnotationAttributes
                                .add(targetNamespaceStringAttributeValue);
                        continue;
                    } else if (pair.getName().contentEquals("portName")) {

                        // name
                        nameStringAttributeValue = new StringAttributeValue(
                                new JavaSymbolName("address"),
                                ((StringLiteralExpr) pair.getValue())
                                        .getValue());

                        gvNIXWebServiceAnnotationAttributes
                                .add(nameStringAttributeValue);
                        continue;
                    }

                }
            }
        }

        // fullyQualifiedTypeName
        StringAttributeValue fullyQualifiedStringAttributeValue = new StringAttributeValue(
                new JavaSymbolName("fullyQualifiedTypeName"),
                javaType.getFullyQualifiedTypeName());
        gvNIXWebServiceAnnotationAttributes
                .add(fullyQualifiedStringAttributeValue);

        // exported
        BooleanAttributeValue exportedAttributeValue = new BooleanAttributeValue(
                new JavaSymbolName("exported"), true);
        gvNIXWebServiceAnnotationAttributes.add(exportedAttributeValue);

        // Create GvNIXWebService annotation.
        gvNIXWebServiceAnnotationMetadata = new AnnotationMetadataBuilder(
                new JavaType(GvNIXWebService.class.getName()),
                gvNIXWebServiceAnnotationAttributes).build();

        return gvNIXWebServiceAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set Web Method and Parameters annotations.
     * </p>
     */
    public MethodMetadata getGvNIXWebMethodMetadata(
            MethodMetadata methodMetadata, String defaultNamespace) {

        MethodMetadata gvNIXMethodMetadata;

        // Method annotations.
        List<AnnotationMetadata> gvNIXWebMethodAnnotationMetadataList = new ArrayList<AnnotationMetadata>();

        AnnotationMetadata gvNIXWEbMethodAnnotationMetadata = getGvNIXWebMethodAnnotation(
                methodMetadata, defaultNamespace);

        Validate.isTrue(
                gvNIXWEbMethodAnnotationMetadata != null,
                "Generated Web Service method: '"
                        + methodMetadata.getMethodName()
                        + "' is not correctly generated with Web Service annotation values.\nRelaunch the command.");

        gvNIXWebMethodAnnotationMetadataList
                .add(gvNIXWEbMethodAnnotationMetadata);

        // Input Parameters annotations.
        List<AnnotatedJavaType> annotatedGvNIXWebParameterList = getGvNIXWebParamsAnnotations(
                methodMetadata, defaultNamespace);

        Validate.isTrue(
                gvNIXWEbMethodAnnotationMetadata != null,
                "Generated Web Service method: '"
                        + methodMetadata.getMethodName()
                        + "' is not correctly generated with Web Service annotation values for its parameters.\nRelaunch the command.");

        // Rebuild method with retrieved parameters.
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(methodMetadata.getBody());
        MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                methodMetadata.getDeclaredByMetadataId(),
                methodMetadata.getModifier(), methodMetadata.getMethodName(),
                methodMetadata.getReturnType(), annotatedGvNIXWebParameterList,
                methodMetadata.getParameterNames(), bodyBuilder);
        for (AnnotationMetadata annotationMetadata : gvNIXWebMethodAnnotationMetadataList) {
            methodMetadataBuilder.addAnnotation(annotationMetadata);
        }
        for (JavaType javaType : methodMetadata.getThrowsTypes()) {
            methodMetadataBuilder.addThrowsType(javaType);
        }

        gvNIXMethodMetadata = methodMetadataBuilder.build();

        return gvNIXMethodMetadata;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns 'null' if @WebMethod annotation is not defined.
     * </p>
     */
    public AnnotationMetadata getGvNIXWebMethodAnnotation(
            MethodMetadata methodMetadata, String defaultNamespace) {

        AnnotationMetadata gvNIXWEbMethodAnnotationMetadata = null;
        AnnotationAttributeValue<?> tmpAttributeValue;

        List<AnnotationAttributeValue<?>> gvNIXWEbMethodAnnotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();

        List<AnnotationMetadata> methodAnnotations = methodMetadata
                .getAnnotations();

        AnnotationMetadata webMethodAnnotation = MemberFindingUtils
                .getAnnotationOfType(methodAnnotations, new JavaType(
                        "javax.jws.WebMethod"));

        if (webMethodAnnotation == null) {
            return gvNIXWEbMethodAnnotationMetadata;
        }

        // String operationName();
        StringAttributeValue operationNameStringAttributeValue;
        tmpAttributeValue = webMethodAnnotation
                .getAttribute(new JavaSymbolName("operationName"));
        if (tmpAttributeValue == null) {
            operationNameStringAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("operationName"), methodMetadata
                            .getMethodName().getSymbolName());
        } else {
            operationNameStringAttributeValue = (StringAttributeValue) tmpAttributeValue;
        }

        gvNIXWEbMethodAnnotationAttributeValues
                .add(operationNameStringAttributeValue);

        // Check if exists action attribute value.
        tmpAttributeValue = webMethodAnnotation
                .getAttribute(new JavaSymbolName("action"));

        StringAttributeValue actionAttributeValue;
        if (tmpAttributeValue != null) {
            actionAttributeValue = new StringAttributeValue(new JavaSymbolName(
                    "action"),
                    ((StringAttributeValue) tmpAttributeValue).getValue());
        } else {
            actionAttributeValue = new StringAttributeValue(new JavaSymbolName(
                    "action"), "");
        }

        gvNIXWEbMethodAnnotationAttributeValues.add(actionAttributeValue);

        // @javax.jws.WebResult
        AnnotationMetadata webResultAnnotation = MemberFindingUtils
                .getAnnotationOfType(methodAnnotations, new JavaType(
                        "javax.jws.WebResult"));

        ClassAttributeValue resultTypeAttributeValue;
        StringAttributeValue resultNameAttributeValue;
        StringAttributeValue resultNamespaceAttributeValue = null;
        BooleanAttributeValue headerAttributeValue = null;
        StringAttributeValue partNameAttributeValue = null;

        if (webResultAnnotation == null) {

            resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"),
                    JavaType.VOID_PRIMITIVE);

            resultNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("resultName"), "void");

        } else {
            resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"),
                    methodMetadata.getReturnType());

            AnnotationAttributeValue<?> nameAttributeValue = webResultAnnotation
                    .getAttribute(new JavaSymbolName("name"));

            if (nameAttributeValue != null) {
                resultNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("resultName"),
                        ((StringAttributeValue) nameAttributeValue).getValue());
            } else {
                resultNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("resultName"), "return");
            }

            AnnotationAttributeValue<?> namespaceAttributeValue = webResultAnnotation
                    .getAttribute(new JavaSymbolName("targetNamespace"));

            if (namespaceAttributeValue != null) {
                resultNamespaceAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("resultNamespace"),
                        ((StringAttributeValue) namespaceAttributeValue)
                                .getValue());
            } else {
                resultNamespaceAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("resultNamespace"), defaultNamespace);
            }

            // Parameter webResultHeader.
            headerAttributeValue = (BooleanAttributeValue) webResultAnnotation
                    .getAttribute(new JavaSymbolName("header"));

            if (headerAttributeValue == null) {
                headerAttributeValue = new BooleanAttributeValue(
                        new JavaSymbolName("webResultHeader"), false);
            } else {
                headerAttributeValue = new BooleanAttributeValue(
                        new JavaSymbolName("webResultHeader"),
                        headerAttributeValue.getValue());
            }

            // Parameter webResultPartName.
            partNameAttributeValue = (StringAttributeValue) webResultAnnotation
                    .getAttribute(new JavaSymbolName("partName"));

            if (partNameAttributeValue == null) {
                partNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("webResultPartName"), "parameters");
            } else {
                partNameAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("webResultPartName"),
                        partNameAttributeValue.getValue());
            }

        }

        gvNIXWEbMethodAnnotationAttributeValues.add(resultTypeAttributeValue);
        gvNIXWEbMethodAnnotationAttributeValues.add(resultNameAttributeValue);

        if (resultNamespaceAttributeValue != null) {
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(resultNamespaceAttributeValue);
        }

        if (headerAttributeValue != null) {
            gvNIXWEbMethodAnnotationAttributeValues.add(headerAttributeValue);
        }
        if (partNameAttributeValue != null) {
            gvNIXWEbMethodAnnotationAttributeValues.add(partNameAttributeValue);
        }

        // @javax.xml.ws.RequestWrapper
        AnnotationMetadata requestWrapperAnnotation = MemberFindingUtils
                .getAnnotationOfType(methodAnnotations, new JavaType(
                        "javax.xml.ws.RequestWrapper"));

        if (requestWrapperAnnotation != null) {

            StringAttributeValue localNameAttributeValue = (StringAttributeValue) requestWrapperAnnotation
                    .getAttribute(new JavaSymbolName("localName"));

            StringAttributeValue requestWrapperNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperName"),
                    localNameAttributeValue.getValue());
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(requestWrapperNameAttributeValue);

            StringAttributeValue targetNamespaceAttributeValue = (StringAttributeValue) requestWrapperAnnotation
                    .getAttribute(new JavaSymbolName("targetNamespace"));

            StringAttributeValue requestTargetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperNamespace"),
                    targetNamespaceAttributeValue.getValue());
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(requestTargetNamespaceAttributeValue);

            StringAttributeValue classNameAttributeValue = (StringAttributeValue) requestWrapperAnnotation
                    .getAttribute(new JavaSymbolName("className"));

            StringAttributeValue requestClassNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("requestWrapperClassName"),
                    classNameAttributeValue.getValue());
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(requestClassNameAttributeValue);

        }

        // @javax.xml.ws.ResponseWrapper
        AnnotationMetadata responseWrapperAnnotation = MemberFindingUtils
                .getAnnotationOfType(methodAnnotations, new JavaType(
                        "javax.xml.ws.ResponseWrapper"));

        if (responseWrapperAnnotation != null) {

            StringAttributeValue localNameAttributeValue = (StringAttributeValue) responseWrapperAnnotation
                    .getAttribute(new JavaSymbolName("localName"));

            StringAttributeValue responseWrapperNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperName"),
                    localNameAttributeValue.getValue());
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(responseWrapperNameAttributeValue);

            StringAttributeValue targetNamespaceAttributeValue = (StringAttributeValue) responseWrapperAnnotation
                    .getAttribute(new JavaSymbolName("targetNamespace"));

            StringAttributeValue responseTargetNamespaceAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperNamespace"),
                    targetNamespaceAttributeValue.getValue());
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(responseTargetNamespaceAttributeValue);

            StringAttributeValue classNameAttributeValue = (StringAttributeValue) responseWrapperAnnotation
                    .getAttribute(new JavaSymbolName("className"));

            StringAttributeValue responseClassNameAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("responseWrapperClassName"),
                    classNameAttributeValue.getValue());
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(responseClassNameAttributeValue);

        }

        // @javax.jws.soap.SOAPBinding
        AnnotationMetadata sOAPBindingAnnotation = MemberFindingUtils
                .getAnnotationOfType(methodAnnotations, new JavaType(
                        "javax.jws.soap.SOAPBinding"));

        if (sOAPBindingAnnotation != null) {

            EnumAttributeValue sOAPBindingAttributeValue = (EnumAttributeValue) sOAPBindingAnnotation
                    .getAttribute(new JavaSymbolName("parameterStyle"));

            if (sOAPBindingAttributeValue != null) {

                gvNIXWEbMethodAnnotationAttributeValues
                        .add(new EnumAttributeValue(
                                new JavaSymbolName("parameterStyle"),
                                new EnumDetails(
                                        new JavaType(
                                                "org.gvnix.service.roo.addon.annotations.GvNIXWebMethod.GvNIXWebMethodParameterStyle"),
                                        sOAPBindingAttributeValue.getValue()
                                                .getField())));
            }
        }

        gvNIXWEbMethodAnnotationMetadata = new AnnotationMetadataBuilder(
                new JavaType(GvNIXWebMethod.class.getName()),
                gvNIXWEbMethodAnnotationAttributeValues).build();

        return gvNIXWEbMethodAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If there isn't WebParam annotation defined returns null.
     * </p>
     */
    public List<AnnotatedJavaType> getGvNIXWebParamsAnnotations(
            MethodMetadata methodMetadata, String defaultNamespace) {

        List<AnnotatedJavaType> annotatedGvNIXWebParameterList = new ArrayList<AnnotatedJavaType>();

        List<AnnotatedJavaType> parameterTypesList = methodMetadata
                .getParameterTypes();

        List<JavaSymbolName> parameterNamesList = methodMetadata
                .getParameterNames();

        if (parameterTypesList.isEmpty() && parameterNamesList.isEmpty()) {
            return annotatedGvNIXWebParameterList;
        }

        AnnotatedJavaType parameterWithAnnotations;
        List<AnnotationMetadata> parameterAnnotationList;

        for (AnnotatedJavaType parameterType : parameterTypesList) {

            parameterAnnotationList = new ArrayList<AnnotationMetadata>();

            AnnotationMetadata webParamAnnotationMetadata = MemberFindingUtils
                    .getAnnotationOfType(parameterType.getAnnotations(),
                            new JavaType("javax.jws.WebParam"));

            if (webParamAnnotationMetadata == null) {
                // If there is not WebParam annotation defined returns null.
                return null;
            }

            List<AnnotationAttributeValue<?>> gvNIXWebParamAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            StringAttributeValue nameWebParamAttributeValue = (StringAttributeValue) webParamAnnotationMetadata
                    .getAttribute(new JavaSymbolName("name"));

            gvNIXWebParamAttributeValueList.add(nameWebParamAttributeValue);

            ClassAttributeValue typeClassAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("type"), parameterType.getJavaType());

            gvNIXWebParamAttributeValueList.add(typeClassAttributeValue);

            // @GvNIXWebParam
            AnnotationMetadata gvNixWebParamAnnotationMetadata = new AnnotationMetadataBuilder(
                    new JavaType(GvNIXWebParam.class.getName()),
                    gvNIXWebParamAttributeValueList).build();

            parameterAnnotationList.add(gvNixWebParamAnnotationMetadata);

            // @WebParam
            parameterAnnotationList.add(webParamAnnotationMetadata);

            // Add annotation list to parameter.
            parameterWithAnnotations = new AnnotatedJavaType(
                    parameterType.getJavaType(), parameterAnnotationList);

            annotatedGvNIXWebParameterList.add(parameterWithAnnotations);
        }

        return annotatedGvNIXWebParameterList;
    }

    /**
     * Get XML document representation from WSDL if valid.
     * 
     * <p>
     * Check WSDL is not RPC Encoded and has only one compatible port.
     * </p>
     * 
     * @param url
     *            from WSDL file to export
     * @return XML document representation of WSDL
     */
    protected Document checkWSDLFile(String url) {

        // Check URL connection and WSDL format
        Element root = securityService.getWsdl(url).getDocumentElement();

        Validate.isTrue(!WsdlParserUtils.isRpcEncoded(root), "This Wsdl '" + url
                + "' is RPC Encoded and is not supported by the Add-on.");

        // Check if is compatible port defined with SOAP12 or SOAP11.
        WsdlParserUtils.checkCompatiblePort(root);

        return root.getOwnerDocument();
    }

    /**
     * {@inheritDoc}
     */
    public void addFileToUpdateAnnotation(File file,
            GvNIXAnnotationType gvNIXAnnotationType) {

        switch (gvNIXAnnotationType) {

        case XML_ELEMENT:
            xmlElements.add(file);
            break;

        case WEB_FAULT:
            webFaults.add(file);
            break;

        case WEB_SERVICE:
            webServices.add(file);
            break;

        case WEB_SERVICE_INTERFACE:
            webServiceInterfaces.add(file);
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resetGeneratedFilesList() {

        // Reset File List
        xmlElements = new ArrayList<File>();
        webFaults = new ArrayList<File>();
        webServices = new ArrayList<File>();
        webServiceInterfaces = new ArrayList<File>();
        schemaPackage = new File("");
    }

    /**
     * {@inheritDoc}
     */
    public void setSchemaPackageInfoFile(File schemaPackageInfoFile) {

        this.schemaPackage = schemaPackageInfoFile;
    }
    
	/**
	 * Extract the filename from the given path,
	 * e.g. "mypath/myfile.txt" -> "myfile.txt".
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename, or <code>null</code> if none
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}

}