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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.*;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.javaparser.details.*;
import org.springframework.roo.file.monitor.DirectoryMonitoringRequest;
import org.springframework.roo.file.monitor.NotifiableFileMonitorService;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.*;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportWSDLConfigServiceImpl implements
        ServiceLayerWSExportWSDLConfigService {

    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private MetadataService metadataService;
    @Reference
    private FileManager fileManager;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private NotifiableFileMonitorService fileMonitorService;
    @Reference
    private JavaParserService javaParserService;

    private static final String CXF_WSDL2JAVA_EXECUTION_ID = "generate-sources-cxf-server";
    private static final String SCHEMA_PACKAGE_INFO = "package-info.java";

    private List<File> gVNIXXmlElementList = new ArrayList<File>();
    private List<File> gVNIXWebFaultList = new ArrayList<File>();
    private List<File> gVNIXWebServiceList = new ArrayList<File>();
    private List<File> gVNIXWebServiceInterfaceList = new ArrayList<File>();

    protected static Logger logger = Logger
            .getLogger(ServiceLayerWSExportWSDLConfigService.class.getName());

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Check correct WSDL format
     * </p>
     * <p>
     * Configure plugin to generate sources
     * </p>
     * <p>
     * Generate java sources
     * </p>
     */
    public void exportWSDLWebService(String wsdlLocation,
            CommunicationSense type) {

        // 1) Check if WSDL is RPC enconded and copy file to project.
        Document wsdl = checkWSDLFile(wsdlLocation);

        // 2) Configure plugin cxf to generate java code using WSDL.
        serviceLayerWsConfigService.addImportLocation(wsdlLocation, type);

        // 3) Reset File List
        resetGeneratedFilesList();
        
        // 3) Run maven generate-sources command.
        try {
            serviceLayerWsConfigService.mvn(ServiceLayerWsConfigService.GENERATE_SOURCES);
        } catch (IOException e) {
            Assert.state(false,
                    "There is an error generating java sources with '"
                            + wsdlLocation + "'.\n" + e.getMessage());
        }

        // Remove plugin execution
        removeCxfWsdl2JavaPluginExecution();

    }

    /**
     * {@inheritDoc}
     * <p>Monitoring file creation only.</p>
     */
    public void monitoringGeneratedSourcesDirectory(String directoryToMonitoring) {

        String generateSourcesDirectory = pathResolver.getIdentifier(Path.ROOT,
                directoryToMonitoring);

        // Monitoring only created files.
        Set<FileOperation> notifyOn = new HashSet<FileOperation>();
        notifyOn.add(FileOperation.CREATED);

        DirectoryMonitoringRequest directoryMonitoringRequest = new DirectoryMonitoringRequest(
                new File(generateSourcesDirectory), true, notifyOn);

        fileMonitorService.add(directoryMonitoringRequest);
        fileMonitorService.scanAll();

        // Remove Directory listener.
        fileMonitorService.remove(directoryMonitoringRequest);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Create files from {@link File} lists:
     * </p>
     * <ul>
     * <li>gVNIXXmlElementList</li>
     * <li>gVNIXWebFaultList</li>
     * <li>gVNIXWebServiceList</li>
     * </ul>
     * 
     * <p>
     * Creates GvNIX annotations attributes from defined attributes in files.
     * </p>
     */
    public void generateGvNIXWebServiceFiles() {

        // TODO: Create @GvNIXXmlElement files.
        generateGvNIXXmlElementsClasses();

        // Create @GvNIXWebFault files.
        generateGvNIXWebFaultClasses();
        
        // TODO: Create @GvNIXWebService files.
        generateGvNIXWebServiceClasses();
    }

    /**
     * {@inheritDoc}
     * 
     * TODO: Generate @GvNIXXmlFieldElement annotations to declared fields.
     */
    public void generateGvNIXXmlElementsClasses() {

        AnnotationMetadata rooEntityAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(
                        "org.springframework.roo.addon.javabean.RooJavaBean"),
                new ArrayList<AnnotationAttributeValue<?>>());

        List<AnnotationMetadata> gvNixAnnotationList;

        // GvNIXXmlElement annotation.
        AnnotationMetadata gvNixXmlElementAnnotation;

        String fileDirectory = null;
        int lastPathSepratorIndex = 0;
        for (File xmlElementFile : gVNIXXmlElementList) {

            fileDirectory = xmlElementFile.getAbsolutePath();
            lastPathSepratorIndex = fileDirectory.lastIndexOf(File.separator);
            fileDirectory = fileDirectory.substring(0, lastPathSepratorIndex + 1);
            
            // Parse Java file.
            CompilationUnit compilationUnit;
            PackageDeclaration packageDeclaration;
            JavaType javaType;
            String declaredByMetadataId;
            // CompilationUnitServices to create the class in fileSystem.
            ServiceLayerWSCompilationUnit compilationUnitServices;

            gvNixAnnotationList = new ArrayList<AnnotationMetadata>();
            try {
                compilationUnit = JavaParser.parse(xmlElementFile);
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
                                .createIdentifier(javaType, Path.SRC_MAIN_JAVA);

                        // TODO: Retrieve correct values.
                        // Get field declarations.
                        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
                        List<ConstructorMetadata> constructorMetadataList = new ArrayList<ConstructorMetadata>();
                        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();

                        FieldMetadata fieldMetadata;
                        FieldDeclaration tmpFieldDeclaration;
                        FieldDeclaration fieldDeclaration;

                        // CompilationUnitServices to create the class.
                        compilationUnitServices = new ServiceLayerWSCompilationUnit(
                                new JavaPackage(compilationUnit.getPackage()
                                        .getName().toString()), javaType,
                                compilationUnit.getImports(),
                                new ArrayList<TypeDeclaration>());

                        for (BodyDeclaration bodyDeclaration : classOrInterfaceDeclaration
                                .getMembers()) {

                            if (bodyDeclaration instanceof FieldDeclaration) {

                                tmpFieldDeclaration = (FieldDeclaration) bodyDeclaration;
                                fieldDeclaration = new FieldDeclaration(
                                        tmpFieldDeclaration.getJavaDoc(),
                                        tmpFieldDeclaration.getModifiers(),
                                        new ArrayList<AnnotationExpr>(),
                                        tmpFieldDeclaration.getType(),
                                        tmpFieldDeclaration.getVariables());

                                for (VariableDeclarator var : fieldDeclaration
                                        .getVariables()) {

                                    fieldMetadata = new JavaParserFieldMetadata(
                                            declaredByMetadataId,
                                            fieldDeclaration, var,
                                            compilationUnitServices,
                                            new HashSet<JavaSymbolName>());

                                    fieldMetadataList.add(fieldMetadata);

                                }
                            }
                        }

                        // ROO entity to generate getters and setters methods.
                        gvNixAnnotationList.add(rooEntityAnnotationMetadata);

                        // Get all annotations.
                        gvNixXmlElementAnnotation = getGvNIXXmlElementAnnotation(
                                classOrInterfaceDeclaration, fileDirectory);
                        gvNixAnnotationList.add(gvNixXmlElementAnnotation);

                        javaParserService.createGvNIXWebServiceClass(javaType,
                                gvNixAnnotationList,
                                GvNIXAnnotationType.XML_ELEMENT,
                                fieldMetadataList, methodMetadataList,
                                constructorMetadataList, new ArrayList<JavaType>());

                    }
                }

            } catch (ParseException e) {
                Assert.state(false, "Generated Xml Element service java file '"
                        + xmlElementFile.getAbsolutePath() + "' has errors:\n"
                        + e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Assert.state(false, "Generated Xml Element service java file '"
                        + xmlElementFile.getAbsolutePath() + "' has errors:\n"
                        + e.getMessage());
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public void generateGvNIXWebFaultClasses() {

        List<AnnotationMetadata> gvNixAnnotationList;

        // GvNIXWebFault annotation.
        AnnotationMetadata gvNIXWebFaultAnnotation;

        for (File webFaultFile : gVNIXWebFaultList) {

            // Parse Java file.
            CompilationUnit compilationUnit;
            PackageDeclaration packageDeclaration;
            JavaType javaType;
            String declaredByMetadataId;
            // CompilationUnitServices to create the class in fileSystem.
            ServiceLayerWSCompilationUnit compilationUnitServices;

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
                                .createIdentifier(javaType, Path.SRC_MAIN_JAVA);

                        // Retrieve correct values.
                        // Get field declarations.
                        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
                        List<ConstructorMetadata> constructorMetadataList = new ArrayList<ConstructorMetadata>();
                        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();

                        FieldMetadata fieldMetadata;
                        FieldDeclaration fieldDeclaration;
                        MethodMetadata methodMetadata;
                        MethodMetadata tmpMethodMetadata;
                        MethodDeclaration methodDeclaration;
                        ConstructorMetadata constructorMetadata;
                        ConstructorMetadata tmpConstructorMetadata;
                        ConstructorDeclaration constructorDeclaration;

                        // Extended classes.
                        List<JavaType> extendedClassesList = new ArrayList<JavaType>();
                        List<ClassOrInterfaceType> extendsClasses = classOrInterfaceDeclaration.getExtends();
                        JavaType extendedJavaType;
                        for (ClassOrInterfaceType classOrInterfaceType : extendsClasses) {
                            extendedJavaType = new JavaType(classOrInterfaceType.getName());
                            extendedClassesList.add(extendedJavaType);
                        }
                        
                        // CompilationUnitServices to create the class.
                        compilationUnitServices = new ServiceLayerWSCompilationUnit(
                                new JavaPackage(compilationUnit.getPackage()
                                        .getName().toString()), javaType,
                                compilationUnit.getImports(),
                                new ArrayList<TypeDeclaration>());

                        for (BodyDeclaration bodyDeclaration : classOrInterfaceDeclaration
                                .getMembers()) {

                            if (bodyDeclaration instanceof FieldDeclaration) {

                                fieldDeclaration = (FieldDeclaration) bodyDeclaration;

                                for (VariableDeclarator var : fieldDeclaration
                                        .getVariables()) {

                                    fieldMetadata = new JavaParserFieldMetadata(
                                            declaredByMetadataId,
                                            fieldDeclaration, var,
                                            compilationUnitServices,
                                            new HashSet<JavaSymbolName>());

                                    fieldMetadataList.add(fieldMetadata);
                                }

                            } else if (bodyDeclaration instanceof ConstructorDeclaration) {
                                
                                constructorDeclaration = (ConstructorDeclaration) bodyDeclaration;
                                
                                tmpConstructorMetadata = new JavaParserConstructorMetadata(
                                        declaredByMetadataId,
                                        constructorDeclaration,
                                        compilationUnitServices,
                                        new HashSet<JavaSymbolName>());
                                
                                constructorMetadata = new DefaultConstructorMetadata(
                                        declaredByMetadataId,
                                        tmpConstructorMetadata.getModifier(),
                                        tmpConstructorMetadata
                                                .getParameterTypes(),
                                        tmpConstructorMetadata
                                                .getParameterNames(),
                                        tmpConstructorMetadata.getAnnotations(),
                                        tmpConstructorMetadata
                                                .getBody()
                                                .substring(
                                                        tmpConstructorMetadata
                                                                .getBody()
                                                                .indexOf("{") + 1,
                                                        tmpConstructorMetadata
                                                                .getBody()
                                                                .lastIndexOf("}")));

                                constructorMetadataList.add(constructorMetadata);
                                
                            } else if (bodyDeclaration instanceof MethodDeclaration) {
                                
                                methodDeclaration = (MethodDeclaration) bodyDeclaration;
                                
                                tmpMethodMetadata = new JavaParserMethodMetadata(
                                        declaredByMetadataId,
                                        methodDeclaration,
                                        compilationUnitServices,
                                        new HashSet<JavaSymbolName>());
                                
                                methodMetadata = new DefaultMethodMetadata(
                                        declaredByMetadataId, tmpMethodMetadata
                                                .getModifier(),
                                        tmpMethodMetadata.getMethodName(),
                                        tmpMethodMetadata.getReturnType(),
                                        tmpMethodMetadata.getParameterTypes(),
                                        tmpMethodMetadata.getParameterNames(),
                                        tmpMethodMetadata.getAnnotations(),
                                        tmpMethodMetadata.getThrowsTypes(),
                                        tmpMethodMetadata.getBody().substring(
                                                tmpMethodMetadata.getBody()
                                                        .indexOf("{") + 1,
                                                tmpMethodMetadata.getBody()
                                                        .lastIndexOf("}")));
                                
                                methodMetadataList.add(methodMetadata);
                            }
                        }

                        // GvNIXWebFault Annotation.Get all annotations.
                        gvNIXWebFaultAnnotation = getGvNIXWebFaultAnnotation(
                                classOrInterfaceDeclaration, javaType);

                        gvNixAnnotationList.add(gvNIXWebFaultAnnotation);

                        javaParserService.createGvNIXWebServiceClass(javaType,
                                gvNixAnnotationList,
                                GvNIXAnnotationType.WEB_FAULT,
                                fieldMetadataList, methodMetadataList,
                                constructorMetadataList, extendedClassesList);
                    }
                }

            } catch (ParseException e) {
                Assert.state(false,
                        "Generated Web Fault service Element java file '"
                                + webFaultFile.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Assert.state(false,
                        "Generated Web Fault service Element java file '"
                                + webFaultFile.getAbsolutePath()
                                + "' has errors:\n" + e.getMessage());
            }

        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void generateGvNIXWebServiceClasses() {
        // TODO Auto-generated method stub

        List<AnnotationMetadata> gvNixAnnotationList;

        // GvNIXWebService annotation.
        AnnotationMetadata gvNIXWebServiceAnnotation;

        String fileDirectory;
        int lastPathSepratorIndex = 0;
        for (File webServiceFile : gVNIXWebServiceList) {

            fileDirectory = webServiceFile.getAbsolutePath();
            lastPathSepratorIndex = fileDirectory.lastIndexOf(File.separator);
            fileDirectory = fileDirectory.substring(0, lastPathSepratorIndex + 1);

            // Parse Java file.
            CompilationUnit compilationUnit;
            PackageDeclaration packageDeclaration;
            JavaType javaType;
            String declaredByMetadataId;
            // CompilationUnitServices to create the class in fileSystem.
            ServiceLayerWSCompilationUnit compilationUnitServices;

            gvNixAnnotationList = new ArrayList<AnnotationMetadata>();
            try {
                compilationUnit = JavaParser.parse(webServiceFile);
                packageDeclaration = compilationUnit.getPackage();

                String packageName = packageDeclaration.getName().toString();

                // Get the first class or interface Java type
                List<TypeDeclaration> types = compilationUnit.getTypes();
                if (types != null) {
                    TypeDeclaration type = types.get(0);
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
                    ClassOrInterfaceDeclaration implementedInterface;
                    
                    if (type instanceof ClassOrInterfaceDeclaration) {

                        javaType = new JavaType(packageName.concat(".").concat(
                                type.getName()));

                        classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;

                        declaredByMetadataId = PhysicalTypeIdentifier
                                .createIdentifier(javaType, Path.SRC_MAIN_JAVA);

                        // Retireve implemented interface.
                        implementedInterface = getWebServiceInterface(classOrInterfaceDeclaration);
                        
                        // Retrieve correct values.
                        // Get field declarations.
                        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
                        List<ConstructorMetadata> constructorMetadataList = new ArrayList<ConstructorMetadata>();
                        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();

                        FieldMetadata fieldMetadata;
                        FieldDeclaration fieldDeclaration;
                        MethodMetadata methodMetadata;
                        MethodMetadata tmpMethodMetadata;
                        MethodDeclaration methodDeclaration;
                        ConstructorMetadata constructorMetadata;
                        ConstructorMetadata tmpConstructorMetadata;
                        ConstructorDeclaration constructorDeclaration;

                        // Extended classes.
                        List<JavaType> extendedClassesList = new ArrayList<JavaType>();

                        // CompilationUnitServices to create the class.
                        compilationUnitServices = new ServiceLayerWSCompilationUnit(
                                new JavaPackage(compilationUnit.getPackage()
                                        .getName().toString()), javaType,
                                compilationUnit.getImports(),
                                new ArrayList<TypeDeclaration>());

                        for (BodyDeclaration bodyDeclaration : classOrInterfaceDeclaration
                                .getMembers()) {

                            if (bodyDeclaration instanceof FieldDeclaration) {

                                fieldDeclaration = (FieldDeclaration) bodyDeclaration;

                                for (VariableDeclarator var : fieldDeclaration
                                        .getVariables()) {

                                    fieldMetadata = new JavaParserFieldMetadata(
                                            declaredByMetadataId,
                                            fieldDeclaration, var,
                                            compilationUnitServices,
                                            new HashSet<JavaSymbolName>());

                                    fieldMetadataList.add(fieldMetadata);
                                }

                            } else if (bodyDeclaration instanceof ConstructorDeclaration) {

                                constructorDeclaration = (ConstructorDeclaration) bodyDeclaration;

                                tmpConstructorMetadata = new JavaParserConstructorMetadata(
                                        declaredByMetadataId,
                                        constructorDeclaration,
                                        compilationUnitServices,
                                        new HashSet<JavaSymbolName>());

                                constructorMetadata = new DefaultConstructorMetadata(
                                        declaredByMetadataId,
                                        tmpConstructorMetadata.getModifier(),
                                        tmpConstructorMetadata
                                                .getParameterTypes(),
                                        tmpConstructorMetadata
                                                .getParameterNames(),
                                        tmpConstructorMetadata.getAnnotations(),
                                        tmpConstructorMetadata
                                                .getBody()
                                                .substring(
                                                        tmpConstructorMetadata
                                                                .getBody()
                                                                .indexOf("{") + 1,
                                                        tmpConstructorMetadata
                                                                .getBody()
                                                                .lastIndexOf("}")));

                                constructorMetadataList
                                        .add(constructorMetadata);

                            } else if (bodyDeclaration instanceof MethodDeclaration) {

                                methodDeclaration = (MethodDeclaration) bodyDeclaration;

                                tmpMethodMetadata = new JavaParserMethodMetadata(
                                        declaredByMetadataId,
                                        methodDeclaration,
                                        compilationUnitServices,
                                        new HashSet<JavaSymbolName>());

                                methodMetadata = new DefaultMethodMetadata(
                                        declaredByMetadataId, tmpMethodMetadata
                                                .getModifier(),
                                        tmpMethodMetadata.getMethodName(),
                                        tmpMethodMetadata.getReturnType(),
                                        tmpMethodMetadata.getParameterTypes(),
                                        tmpMethodMetadata.getParameterNames(),
                                        tmpMethodMetadata.getAnnotations(),
                                        tmpMethodMetadata.getThrowsTypes(),
                                        tmpMethodMetadata.getBody().substring(
                                                tmpMethodMetadata.getBody()
                                                        .indexOf("{") + 1,
                                                tmpMethodMetadata.getBody()
                                                        .lastIndexOf("}")));

                                methodMetadataList.add(methodMetadata);
                            }
                        }

                        // GvNIXWebFault Annotation.Get all annotations.
                        gvNIXWebServiceAnnotation = getGvNIXWebServiceAnnotation(
                                classOrInterfaceDeclaration, implementedInterface, javaType);

                        gvNixAnnotationList.add(gvNIXWebServiceAnnotation);

                        javaParserService.createGvNIXWebServiceClass(javaType,
                                gvNixAnnotationList,
                                GvNIXAnnotationType.WEB_SERVICE,
                                fieldMetadataList, methodMetadataList,
                                constructorMetadataList, extendedClassesList);
                    }
                }

            } catch (ParseException e) {
                Assert.state(false, "Generated Web Service java file '"
                        + webServiceFile.getAbsolutePath() + "' has errors:\n"
                        + e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Assert.state(false, "Generated Web Service java file '"
                        + webServiceFile.getAbsolutePath() + "' has errors:\n"
                        + e.getMessage());

            }

        }
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     * @throws ParseException 
     * 
     */
    public ClassOrInterfaceDeclaration getWebServiceInterface(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration) throws ParseException, IOException {

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

            for (File interfaceFile : gVNIXWebServiceInterfaceList) {

                fileInterfaceName = StringUtils.getFilename(interfaceFile
                        .getAbsolutePath());

                if (fileInterfaceName.contentEquals(interfaceName)) {

                    compilationUnit = JavaParser.parse(interfaceFile);

                    List<TypeDeclaration> types = compilationUnit.getTypes();
                    if (types != null) {
                        type = types.get(0);
                        if (type instanceof ClassOrInterfaceDeclaration) {

                            implementedInterface = (ClassOrInterfaceDeclaration) type;

                            return implementedInterface;
                        }
                    }
                }
            }

        }

        return implementedInterface;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Searches for Jaxb annotations in {@link ClassOrInterfaceDeclaration} to
     * convert values to {@link GvNIXXmlElement}.
     * </p>
     */
    public AnnotationMetadata getGvNIXXmlElementAnnotation(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            String fileDirectory) {

        AnnotationMetadata gvNIXXmlElementAnnotationMetadata;

        List<AnnotationExpr> annotationExprList = classOrInterfaceDeclaration
                .getAnnotations();

        // Attribute value list.
        List<AnnotationAttributeValue<?>> annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();

        // name
        StringAttributeValue nameStringAttributeValue = null;
        // namespace
        StringAttributeValue namespaceStringAttributeValue = null;
        // element list values
        List<StringAttributeValue> elementListStringAttributeValues = new ArrayList<StringAttributeValue>();
        ArrayAttributeValue<StringAttributeValue> elementListArrayAttributeValue;

        boolean existsNamespace = false;
        boolean existsPropOrder = false;

        for (AnnotationExpr annotationExpr : annotationExprList) {

            if (annotationExpr instanceof NormalAnnotationExpr) {

                NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;

                if (normalAnnotationExpr.getName().getName().contains(
                        ServiceLayerWSExportWSDLListener.xmlRootElement)) {

                    // Retrieve values.
                    for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {

                        if (pair.getName().contentEquals("name")) {
                            nameStringAttributeValue = new StringAttributeValue(
                                    new JavaSymbolName("name"),
                                    ((StringLiteralExpr) pair.getValue())
                                            .getValue());

                            annotationAttributeValues
                                    .add(nameStringAttributeValue);
                            break;
                        }

                    }

                } else if (normalAnnotationExpr.getName().getName().contains(
                        ServiceLayerWSExportWSDLListener.xmlType)) {

                    for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {

                        // TODO: New Attribute to add to XmlType name

                        // if (pair.getName().contentEquals("name")
                        // && !existsNameInXmlElement) {
                        //
                        // if (StringUtils.hasText(pair.getValue().toString()))
                        // {
                        //
                        // nameStringAttributeValue = new StringAttributeValue(
                        // new JavaSymbolName("name"),
                        // ((StringLiteralExpr) pair.getValue())
                        // .getValue());
                        //
                        // annotationAttributeValues
                        // .add(nameStringAttributeValue);
                        // break;
                        // }
                        // } else

                        if (pair.getName().contentEquals("propOrder")) {

                            // Arraye pair.getValue();
                            ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) pair
                                    .getValue();

                            for (Expression expression : arrayInitializerExpr
                                    .getValues()) {

                                StringAttributeValue stringAttributeValue = new StringAttributeValue(
                                        new JavaSymbolName("ignored"),
                                        ((StringLiteralExpr) expression)
                                                .getValue());

                                elementListStringAttributeValues
                                        .add(stringAttributeValue);
                            }

                            elementListArrayAttributeValue = new ArrayAttributeValue<StringAttributeValue>(
                                    new JavaSymbolName("elementList"),
                                    elementListStringAttributeValues);

                            annotationAttributeValues
                                    .add(elementListArrayAttributeValue);

                            existsPropOrder = true;
                            break;

                        } else if (pair.getName().contentEquals("namespace")) {

                            namespaceStringAttributeValue = new StringAttributeValue(
                                    new JavaSymbolName("namespace"),((StringLiteralExpr) pair.getValue())
                                    .getValue());

                            annotationAttributeValues
                                    .add(namespaceStringAttributeValue);

                            existsNamespace = true;
                        }

                    }

                }

            } else if (annotationExpr instanceof SingleMemberAnnotationExpr) {

                SingleMemberAnnotationExpr singleMemberAnnotationExpr = (SingleMemberAnnotationExpr) annotationExpr;

                if (singleMemberAnnotationExpr.getName().getName().contains(
                        ServiceLayerWSExportWSDLListener.xmlAccessorType)) {

                }
            }

        }

        // TODO: Check correct values for @GvNIXXmlElement.
        if (!existsPropOrder) {

            StringAttributeValue stringAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("ignored"), "");

            elementListStringAttributeValues = new ArrayList<StringAttributeValue>();
            elementListStringAttributeValues.add(stringAttributeValue);

            elementListArrayAttributeValue = new ArrayAttributeValue<StringAttributeValue>(
                    new JavaSymbolName("elementList"),
                    elementListStringAttributeValues);

            annotationAttributeValues.add(elementListArrayAttributeValue);
        }

        if (!existsNamespace) {

            String filePackageInfo = fileDirectory.concat(SCHEMA_PACKAGE_INFO);
            File packageInfoFile = new File(filePackageInfo);
            CompilationUnit compilationUnit;

            String namespace = "";
            try {

                compilationUnit = JavaParser.parse(packageInfoFile);

                List<AnnotationExpr> packageAnnotations = compilationUnit
                        .getPackage().getAnnotations();

                boolean exists = false;
                for (AnnotationExpr packageAnnotationExpr : packageAnnotations) {

                    if (packageAnnotationExpr instanceof NormalAnnotationExpr) {

                        NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) packageAnnotationExpr;

                        if (normalAnnotationExpr.getName().toString().contains(
                                "javax.xml.bind.annotation.XmlSchema")) {

                            List<MemberValuePair> pairs = normalAnnotationExpr
                                    .getPairs();

                            for (MemberValuePair memberValuePair : pairs) {

                                if (memberValuePair.getName().contentEquals(
                                        "namespace")) {
                                    namespace = ((StringLiteralExpr) memberValuePair
                                            .getValue()).getValue();
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

                namespaceStringAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("namespace"), namespace);

                annotationAttributeValues.add(namespaceStringAttributeValue);

            } catch (ParseException e) {
                Assert.state(false,
                        "Generated Xml Element service java file '"+classOrInterfaceDeclaration.getName()+"' has errors:\n"
                                + e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Assert.state(false,
                        "Generated Xml Element service java file '"+classOrInterfaceDeclaration.getName()+"' has errors:\n"
                                + e.getMessage());

            }

        }

        // Create annotation.
        gvNIXXmlElementAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(GvNIXXmlElement.class.getName()),
                annotationAttributeValues);

        return gvNIXXmlElementAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationMetadata getGvNIXWebFaultAnnotation(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, JavaType exceptionType) {

        AnnotationMetadata gvNIXWebFaultAnnotationMetadata;
        List<AnnotationAttributeValue<?>> gvNIXWebFaultAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

        // @WebFault(name = "faultDetail", targetNamespace = "http://apache.org/hello_world_soap_http/types")
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
                new JavaSymbolName("faultBean"),faultBeanWebFault));

        // Create GvNIXWebFault annotation.
        gvNIXWebFaultAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(GvNIXWebFault.class.getName()),
                gvNIXWebFaultAnnotationAttributes);

        return gvNIXWebFaultAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationMetadata getGvNIXWebServiceAnnotation(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
            ClassOrInterfaceDeclaration implementedInterface,
            JavaType javaType) {

        AnnotationMetadata gvNIXWebServiceAnnotationMetadata;
        List<AnnotationAttributeValue<?>> gvNIXWebServiceAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

        /*
         * TODO: 
         * Class:
         * 
         * @javax.jws.WebService( serviceName = "TempConvert", portName =
         * "TempConvertSoap12", targetNamespace = "http://tempuri.org/",
         * wsdlLocation =
         * "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL",
         * endpointInterface = "org.tempuri.TempConvertSoap")
         * 
         * Interface:
         * 
         * @WebService(targetNamespace = "http://tempuri.org/", name =
         * "TempConvertSoap")
         * 
         * Result:
         * 
         * @WebService(name = "TempConvertSoap",portName = "TempConvertSoap12",
         * targetNamespace = "http://tempuri.org/", serviceName =
         * "TempConvert");
         */
        
        /*
         * @GvNIXWebService
         * 
         * address = X name.
         */

        // Search for interface annotation attribute values.
        List<AnnotationExpr> annotationInterfaceExprList = implementedInterface
        .getAnnotations();

        for (AnnotationExpr annotationExpr : annotationInterfaceExprList) {
            
            if (annotationExpr instanceof NormalAnnotationExpr) {

                NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;
            
                StringAttributeValue addressStringAttributeValue;
                
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
            }
        }
        
        /*
         * @GvNIXWebService
         * 
         * name = class portName.
         * serviceName = class serviceName.
         * targetNamespace = class targetNamespace.
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
                new JavaSymbolName("fullyQualifiedTypeName"), javaType
                        .getFullyQualifiedTypeName());
        gvNIXWebServiceAnnotationAttributes.add(fullyQualifiedStringAttributeValue);

        // exported
        BooleanAttributeValue exportedAttributeValue = new BooleanAttributeValue(
                new JavaSymbolName("exported"), true);
        gvNIXWebServiceAnnotationAttributes.add(exportedAttributeValue);

        // Create GvNIXWebService annotation.
        gvNIXWebServiceAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(GvNIXWebService.class.getName()),
                gvNIXWebServiceAnnotationAttributes);

        return gvNIXWebServiceAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Check if WSDL is RPC Encoded.
     * </p>
     * 
     * <p>
     * If WSDL is Document/Literal return Xml Document from WSDl.
     * </p>
     */
    public Document checkWSDLFile(String url) {

        Document wsdl = null;
        try {

            // Parse the wsdl location to a DOM document
            wsdl = XmlUtils.getDocumentBuilder().parse(url);
            Element root = wsdl.getDocumentElement();
            Assert.notNull(root, "No valid document format");

            Assert.isTrue(!WsdlParserUtils.isRpcEncoded(root), "This Wsdl '"
                    + url
                    + "' is RPC Encoded and is not supported by the Add-on.");

        } catch (SAXException e) {

            Assert.state(false, "The format of the web service '" + url
                    + "' to export has errors.");

        } catch (IOException e) {

            Assert.state(false, "There is no connection to the web service '"
                    + url + "' to export.");
        }

        return wsdl;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Search the execution element using id defined in
     * CXF_WSDL2JAVA_EXECUTION_ID field.
     * </p>
     */
    public void removeCxfWsdl2JavaPluginExecution() {

        // Get pom.xml
        String pomPath = getPomFilePath();
        Assert.notNull(pomPath, "pom.xml configuration file not found.");

        // Get a mutable pom.xml reference to modify it
        MutableFile pomMutableFile = null;
        Document pom;
        try {
            pomMutableFile = fileManager.updateFile(pomPath);
            pom = XmlUtils.getDocumentBuilder().parse(
                    pomMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element root = pom.getDocumentElement();

        // Get plugin element
        Element codegenWsPlugin = XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']",
                        root);

        // If plugin element not exists, message error
        Assert
                .notNull(codegenWsPlugin,
                        "Codegen plugin is not defined in the pom.xml, relaunch again this command.");

        // Checks if already exists the execution.
        Element oldGenerateSourcesCxfServer = XmlUtils.findFirstElement(
                "/project/build/plugins/plugin/executions/execution[id='"
                        + CXF_WSDL2JAVA_EXECUTION_ID + "']", root);

        if (oldGenerateSourcesCxfServer != null) {

            Element executionPhase = XmlUtils.findFirstElementByName("phase", oldGenerateSourcesCxfServer);
            
            if (executionPhase != null) {

                Element newPhase = pom.createElement("phase");
                newPhase.setTextContent("none");

                // Remove existing wsdlOption.
                executionPhase.getParentNode().replaceChild(newPhase,
                        executionPhase);

                // Write new XML to disk.
                XmlUtils.writeXml(pomMutableFile.getOutputStream(), pom);
            }

        }

    }

    /**
     * {@inheritDoc}
     * 
     */
    public void addFileToUpdateAnnotation(File file,
            GvNIXAnnotationType gvNIXAnnotationType) {

        switch (gvNIXAnnotationType) {

        case XML_ELEMENT:
            gVNIXXmlElementList.add(file);
            break;

        case WEB_FAULT:
            gVNIXWebFaultList.add(file);
            break;

        case WEB_SERVICE:
            gVNIXWebServiceList.add(file);
            break;

        case WEB_SERVICE_INTERFACE:
            gVNIXWebServiceInterfaceList.add(file);
            break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void resetGeneratedFilesList() {
        //  Reset File List
        gVNIXXmlElementList = new ArrayList<File>();
        gVNIXWebFaultList = new ArrayList<File>();
        gVNIXWebServiceList = new ArrayList<File>();
        gVNIXWebServiceInterfaceList = new ArrayList<File>();
    }

    /**
     * Check if pom.xml file exists in the project and return the path.
     * 
     * <p>
     * Checks if exists pom.xml config file. If not exists, null will be
     * returned.
     * </p>
     * 
     * @return Path to the pom.xml file or null if not exists.
     */
    private String getPomFilePath() {

        // Project ID
        String prjId = ProjectMetadata.getProjectIdentifier();
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(prjId);
        Assert.isTrue(projectMetadata != null, "Project metadata required");

        String pomFileName = "pom.xml";

        // Checks for pom.xml
        String pomPath = pathResolver.getIdentifier(Path.ROOT, pomFileName);

        boolean pomInstalled = fileManager.exists(pomPath);

        if (pomInstalled) {

            return pomPath;
        } else {

            return null;
        }
    }

}
