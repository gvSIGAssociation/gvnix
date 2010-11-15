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
        Document wsdlDocument = checkWSDLFile(wsdlLocation);

        // 2) Configure plugin cxf to generate java code using WSDL.
        serviceLayerWsConfigService.addExportLocation(wsdlLocation, wsdlDocument, type);

        // 3) Reset File List
        resetGeneratedFilesList();

        // 3) Run maven generate-sources command.
        try {
            serviceLayerWsConfigService
                    .mvn(ServiceLayerWsConfigService.GENERATE_SOURCES);
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
     * <p>
     * Monitoring file creation only.
     * </p>
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
            fileDirectory = fileDirectory.substring(0,
                    lastPathSepratorIndex + 1);

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
                                        tmpFieldDeclaration.getAnnotations(),
                                        tmpFieldDeclaration.getType(),
                                        tmpFieldDeclaration.getVariables());

                                for (VariableDeclarator var : fieldDeclaration
                                        .getVariables()) {

                                    fieldMetadata = new JavaParserFieldMetadata(
                                            declaredByMetadataId,
                                            fieldDeclaration, var,
                                            compilationUnitServices,
                                            new HashSet<JavaSymbolName>());

                                    fieldMetadata = getGvNIXXmlElementFieldAnnotation(fieldMetadata);

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
                                constructorMetadataList,
                                new ArrayList<JavaType>());

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
                        List<ClassOrInterfaceType> extendsClasses = classOrInterfaceDeclaration
                                .getExtends();
                        JavaType extendedJavaType;
                        for (ClassOrInterfaceType classOrInterfaceType : extendsClasses) {
                            extendedJavaType = new JavaType(
                                    classOrInterfaceType.getName());
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
                                                                .lastIndexOf(
                                                                        "}")));

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
            fileDirectory = fileDirectory.substring(0,
                    lastPathSepratorIndex + 1);

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

                        // Retrieve implemented interface.
                        implementedInterface = getWebServiceInterface(classOrInterfaceDeclaration);

                        // Retrieve correct values.
                        // Get field declarations.
                        List<FieldMetadata> fieldMetadataList = new ArrayList<FieldMetadata>();
                        List<ConstructorMetadata> constructorMetadataList = new ArrayList<ConstructorMetadata>();
                        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();

                        MethodMetadata methodMetadata;
                        MethodMetadata tmpMethodMetadata;
                        MethodDeclaration methodDeclaration;

                        FieldMetadata fieldMetadata;
                        FieldDeclaration tmpFieldDeclaration;
                        FieldDeclaration fieldDeclaration;

                        // Extended classes.
                        List<JavaType> extendedClassesList = new ArrayList<JavaType>();

                        // CompilationUnitServices to create the class.
                        compilationUnitServices = new ServiceLayerWSCompilationUnit(
                                new JavaPackage(compilationUnit.getPackage()
                                        .getName().toString()), javaType,
                                compilationUnit.getImports(),
                                new ArrayList<TypeDeclaration>());

                        // GvNIXWebFault Annotation.Get all annotations.
                        gvNIXWebServiceAnnotation = getGvNIXWebServiceAnnotation(
                                classOrInterfaceDeclaration,
                                implementedInterface, javaType);

                        gvNixAnnotationList.add(gvNIXWebServiceAnnotation);

                        // Default Web Service Namespace.
                        AnnotationAttributeValue<?> targetNamespaceAnnotationAttributeValue = gvNIXWebServiceAnnotation.getAttribute(new JavaSymbolName("targetNamespace"));
                        String defaultNamespace = ((StringAttributeValue) targetNamespaceAnnotationAttributeValue ).getValue();
                        
                        // @GvNIXWebMethod annotations.
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

                            } else if (bodyDeclaration instanceof MethodDeclaration) {

                                methodDeclaration = (MethodDeclaration) bodyDeclaration;

                                tmpMethodMetadata = new JavaParserMethodMetadata(
                                        declaredByMetadataId,
                                        methodDeclaration,
                                        compilationUnitServices,
                                        new HashSet<JavaSymbolName>());

                                // Check method from interface because Web
                                // Service
                                // Annotations are defined there
                                for (BodyDeclaration interfacebodyDeclaration : implementedInterface
                                        .getMembers()) {

                                    MethodDeclaration interfaceMethodDeclaration;

                                    if (interfacebodyDeclaration instanceof MethodDeclaration) {

                                        interfaceMethodDeclaration = (MethodDeclaration) interfacebodyDeclaration;

                                        if (interfaceMethodDeclaration
                                                .getName().contentEquals(
                                                        methodDeclaration
                                                                .getName())) {

                                            MethodMetadata interfaceTmpMethodMetadata = new JavaParserMethodMetadata(
                                                    declaredByMetadataId,
                                                    interfaceMethodDeclaration,
                                                    compilationUnitServices,
                                                    new HashSet<JavaSymbolName>());

                                            interfaceTmpMethodMetadata = new DefaultMethodMetadata(
                                                    declaredByMetadataId,
                                                    interfaceTmpMethodMetadata
                                                            .getModifier(),
                                                    interfaceTmpMethodMetadata
                                                            .getMethodName(),
                                                    interfaceTmpMethodMetadata
                                                            .getReturnType(),
                                                    interfaceTmpMethodMetadata
                                                            .getParameterTypes(),
                                                    interfaceTmpMethodMetadata
                                                            .getParameterNames(),
                                                    interfaceTmpMethodMetadata
                                                            .getAnnotations(),
                                                    interfaceTmpMethodMetadata
                                                            .getThrowsTypes(),
                                                    tmpMethodMetadata.getBody());

                                            // Web Service method Operation.
                                            methodMetadata = getGvNIXWebMethodMetadata(
                                                    interfaceTmpMethodMetadata,
                                                    defaultNamespace);

                                            methodMetadataList
                                                    .add(methodMetadata);

                                        }
                                    }
                                }

                            }

                        }

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
     * 
     * @throws IOException
     * @throws ParseException
     * 
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

                        if (pair.getName().contentEquals("name")) {

                            if (StringUtils.hasText(pair.getValue().toString())) {

                                nameStringAttributeValue = new StringAttributeValue(
                                        new JavaSymbolName("xmlTypeName"),
                                        ((StringLiteralExpr) pair.getValue())
                                                .getValue());

                                annotationAttributeValues
                                        .add(nameStringAttributeValue);
                                continue;
                            }
                        } else if (pair.getName().contentEquals("propOrder")) {

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
                            continue;

                        } else if (pair.getName().contentEquals("namespace")) {

                            namespaceStringAttributeValue = new StringAttributeValue(
                                    new JavaSymbolName("namespace"),
                                    ((StringLiteralExpr) pair.getValue())
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

        // Check correct values for @GvNIXXmlElement.
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
                Assert.state(false, "Generated Xml Element service java file '"
                        + classOrInterfaceDeclaration.getName()
                        + "' has errors:\n" + e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Assert.state(false, "Generated Xml Element service java file '"
                        + classOrInterfaceDeclaration.getName()
                        + "' has errors:\n" + e.getMessage());

            }

        }

        // Exported attribute value
        BooleanAttributeValue exportedBooleanAttributeValue = new BooleanAttributeValue(new JavaSymbolName("exported"), true);
        annotationAttributeValues.add(exportedBooleanAttributeValue);

        // Create annotation.
        gvNIXXmlElementAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(GvNIXXmlElement.class.getName()),
                annotationAttributeValues);

        return gvNIXXmlElementAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates {@link FieldMetadata} with @GvNIXXmlElementField annotation even
     * if not exists @XmlElement.
     * </p>
     */
    public FieldMetadata getGvNIXXmlElementFieldAnnotation(
            FieldMetadata fieldMetadata) {

        FieldMetadata convertedFieldMetadata;
        List<AnnotationMetadata> updatedAnnotationMetadataList = new ArrayList<AnnotationMetadata>();

        AnnotationMetadata xmlElementAnnotation = MemberFindingUtils
        .getAnnotationOfType(fieldMetadata.getAnnotations(), new JavaType(
                "javax.xml.bind.annotation.XmlElement"));

        AnnotationMetadata gvNIXXmlElementFieldAnnotation;
        if (xmlElementAnnotation == null) {

            gvNIXXmlElementFieldAnnotation = new DefaultAnnotationMetadata(
                    new JavaType(GvNIXXmlElementField.class.getName()),
                    new ArrayList<AnnotationAttributeValue<?>>());
        }
        else {

            List<JavaSymbolName> annotationAttributeNames = xmlElementAnnotation.getAttributeNames();
            
            AnnotationAttributeValue<?> tmpAnnotationAttributeValue;
            List<AnnotationAttributeValue<?>> gvNIXXmlElementFieldAttributes = new ArrayList<AnnotationAttributeValue<?>>();

            for (JavaSymbolName javaSymbolName : annotationAttributeNames) {
                tmpAnnotationAttributeValue = xmlElementAnnotation.getAttribute(javaSymbolName);
                gvNIXXmlElementFieldAttributes.add(tmpAnnotationAttributeValue);
            }
            
            gvNIXXmlElementFieldAnnotation = new DefaultAnnotationMetadata(
                    new JavaType(GvNIXXmlElementField.class.getName()),
                    gvNIXXmlElementFieldAttributes);
        }
        
        updatedAnnotationMetadataList.add(gvNIXXmlElementFieldAnnotation);

        // Create new Field with GvNIXAnnotation.
        convertedFieldMetadata = new DefaultFieldMetadata(fieldMetadata
                .getDeclaredByMetadataId(), fieldMetadata.getModifier(),
                fieldMetadata.getFieldName(), fieldMetadata.getFieldType(),
                fieldMetadata.getFieldInitializer(),
                updatedAnnotationMetadataList);
        
        return convertedFieldMetadata;
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
            ClassOrInterfaceDeclaration implementedInterface, JavaType javaType) {

        AnnotationMetadata gvNIXWebServiceAnnotationMetadata;
        List<AnnotationAttributeValue<?>> gvNIXWebServiceAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();

        /*
         * TODO: Class:
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
         * name = class portName. serviceName = class serviceName.
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
        gvNIXWebServiceAnnotationAttributes
                .add(fullyQualifiedStringAttributeValue);

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
     * Set Web Method and Parameters annotations.
     * </p>
     * 
     */
    public MethodMetadata getGvNIXWebMethodMetadata(
            MethodMetadata methodMetadata, String defaultNamespace) {

        MethodMetadata gvNIXMethodMetadata;

        // Method annotations.
        List<AnnotationMetadata> gvNIXWebMethodAnnotationMetadataList = new ArrayList<AnnotationMetadata>();

        AnnotationMetadata gvNIXWEbMethodAnnotationMetadata = getGvNIXWebMethodAnnotation(methodMetadata, defaultNamespace);

        Assert
                .isTrue(
                        gvNIXWEbMethodAnnotationMetadata != null,
                        "Generated Web Service method: '"
                                + methodMetadata.getMethodName()
                                + "' is not correctly generated with Web Service annotation values.\nRelaunch the command.");

        gvNIXWebMethodAnnotationMetadataList
                .add(gvNIXWEbMethodAnnotationMetadata);

        // Input Parameters annotations.
        List<AnnotatedJavaType> annotatedGvNIXWebParameterList = getGvNIXWebParamsAnnotations(methodMetadata, defaultNamespace);

        Assert
                .isTrue(
                        gvNIXWEbMethodAnnotationMetadata != null,
                        "Generated Web Service method: '"
                                + methodMetadata.getMethodName()
                                + "' is not correctly generated with Web Service annotation values for its parameters.\nRelaunch the command.");

        // Rebuild method with retrieved parameters.
        gvNIXMethodMetadata = new DefaultMethodMetadata(methodMetadata
                .getDeclaredByMetadataId(), methodMetadata.getModifier(),
                methodMetadata.getMethodName(), methodMetadata.getReturnType(),
                annotatedGvNIXWebParameterList, methodMetadata
                        .getParameterNames(),
                gvNIXWebMethodAnnotationMetadataList, methodMetadata
                        .getThrowsTypes(), methodMetadata.getBody().substring(
                        methodMetadata.getBody().indexOf("{") + 1,
                        methodMetadata.getBody().lastIndexOf("}")));

        return gvNIXMethodMetadata;
    }

    /**
     * {@inheritDoc}
     * 
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
                    ((StringAttributeValue) tmpAttributeValue)
                            .getValue());
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
        
        if (webResultAnnotation == null) {

            resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"),
                    JavaType.VOID_PRIMITIVE);
            
            resultNameAttributeValue = new StringAttributeValue(new JavaSymbolName("resultName"), "void");
            
        } else {
            resultTypeAttributeValue = new ClassAttributeValue(
                    new JavaSymbolName("webResultType"), methodMetadata
                            .getReturnType());

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
                        ((StringAttributeValue) namespaceAttributeValue).getValue());
            } else {
                resultNamespaceAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("resultNamespace"), defaultNamespace);
            }

        }

        gvNIXWEbMethodAnnotationAttributeValues.add(resultTypeAttributeValue);
        gvNIXWEbMethodAnnotationAttributeValues.add(resultNameAttributeValue);

        if (resultNamespaceAttributeValue != null) {
            gvNIXWEbMethodAnnotationAttributeValues
                    .add(resultNamespaceAttributeValue);
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

        gvNIXWEbMethodAnnotationMetadata = new DefaultAnnotationMetadata(
                new JavaType(GvNIXWebMethod.class.getName()),
                gvNIXWEbMethodAnnotationAttributeValues);

        return gvNIXWEbMethodAnnotationMetadata;
    }

    /**
     * {@inheritDoc}
     * 
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
            AnnotationMetadata gvNixWebParamAnnotationMetadata = new DefaultAnnotationMetadata(
                    new JavaType(GvNIXWebParam.class.getName()),
                    gvNIXWebParamAttributeValueList);

            parameterAnnotationList.add(gvNixWebParamAnnotationMetadata);

            // @WebParam
            parameterAnnotationList.add(webParamAnnotationMetadata);

            // Add annotation list to parameter.
            parameterWithAnnotations = new AnnotatedJavaType(parameterType
                    .getJavaType(), parameterAnnotationList);

            annotatedGvNIXWebParameterList.add(parameterWithAnnotations);
        }

        return annotatedGvNIXWebParameterList;
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

            // Check if is compatible port defined with SOAP12 or SOAP11.
            WsdlParserUtils.checkCompatiblePort(root);

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

            Element executionPhase = XmlUtils.findFirstElementByName("phase",
                    oldGenerateSourcesCxfServer);

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
        // Reset File List
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
