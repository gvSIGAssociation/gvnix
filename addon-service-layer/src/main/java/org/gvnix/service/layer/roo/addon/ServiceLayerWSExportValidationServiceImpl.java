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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsExportOperations.MethodParameterType;
import org.gvnix.service.layer.roo.addon.annotations.*;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;

import com.sun.org.apache.xerces.internal.impl.XMLEntityManager.Entity;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportValidationServiceImpl implements
        ServiceLayerWSExportValidationService {

    @Reference
    private PathResolver pathResolver;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private ClasspathOperations classpathOperations;

    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private AnnotationsService annotationsService;

    private static final String ITD_TEMPLATE = "Web_Faults_gvnix_service_layer.aj_template";

    private static final String ITD_FILE_NAME = "_Web_Faults_gvnix_service_layer.aj";

    private static final Set<String> notAllowedClassCollectionTypes = new HashSet<String>();

    static {
        notAllowedClassCollectionTypes.add(HashMap.class.getName());
        notAllowedClassCollectionTypes.add(TreeMap.class.getName());
    }

    private static final Set<String> notAllowedIntefaceCollectionTypes = new HashSet<String>();

    static {
        notAllowedIntefaceCollectionTypes.add(Map.class.getName());
    }

    private static final String ITERABLE = Iterable.class.getName();
    private static final String MAP = Map.class.getName();

    private static Logger logger = Logger
            .getLogger(ServiceLayerWSExportValidationServiceImpl.class
                    .getName());

    /**
     * {@inheritDoc}
     * <p>
     * Add web services annotations to each founded exception.
     * </p>
     * <p>
     * Two types of exceptions and two ways to define annotations:
     * </p>
     * <ul>
     * <li>
     * Exceptions defined in the project.
     * <p>
     * Add @GvNIXWebFault annotation to Exception.
     * </p>
     * </li>
     * <li>
     * Exceptions imported into the project.
     * <p>
     * Add web service fault annotation using AspectJ template.
     * </p>
     * </li>
     * </ul>
     */
    public boolean checkMethodExceptions(JavaType serviceClass,
            JavaSymbolName methodName, String webServiceTargetNamespace) {

        MethodMetadata methodToCheck = javaParserService
                .getMethodByNameInClass(serviceClass, methodName);

        Assert.isTrue(methodToCheck != null, "The method: '" + methodName
                + " doesn't exists in the class '"
                + serviceClass.getFullyQualifiedTypeName() + "'.");

        List<JavaType> throwsTypes = methodToCheck.getThrowsTypes();

        String fileLocation;

        boolean extendsThrowable = true;

        for (JavaType throwType : throwsTypes) {

            extendsThrowable = checkExceptionExtension(throwType);

            Assert
                    .isTrue(
                            extendsThrowable,
                            "The '"
                                    + throwType.getFullyQualifiedTypeName()
                                    + "' class doesn't extend from 'java.lang.Throwable' in method '"
                                    + methodName
                                    + "' from class '"
                                    + serviceClass.getFullyQualifiedTypeName()
                                    + "'.\nIt can't be used as Exception in method to be thrown.");

            fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
                    throwType.getFullyQualifiedTypeName().replace('.', '/')
                            .concat(".java"));

            List<AnnotationAttributeValue<?>> gvNIXWebFaultAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
            gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("name"), StringUtils
                            .uncapitalize(throwType.getSimpleTypeName())));
            gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("targetNamespace"),
                    webServiceTargetNamespace));
            gvNIXWebFaultAnnotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("faultBean"), throwType
                            .getFullyQualifiedTypeName()));

            // Exception defined in the project or imported.
            if (fileManager.exists(fileLocation)) {

                // Define annotation.
                annotationsService.addJavaTypeAnnotation(throwType,
                        GvNIXWebFault.class.getName(),
                        gvNIXWebFaultAnnotationAttributes, false);

            } else {
                // Add definition to AspectJ file.
                exportImportedException(throwType,
                        gvNIXWebFaultAnnotationAttributes);
            }

        }

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates AspectJ template if not exists.
     * </p>
     * <p>
     * Updates with exceptionClass annotation values.
     * </p>
     */
    public void exportImportedException(JavaType exceptionClass,
            List<AnnotationAttributeValue<?>> annotationAttributeValues) {

        String template;
        try {
            template = FileCopyUtils.copyToString(new InputStreamReader(this
                    .getClass().getResourceAsStream(ITD_TEMPLATE)));
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable load ITD web fault definitions template", ioe);
        }

        Map<String, String> params = new HashMap<String, String>();

        String topLevelPath = "";
        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());

        Assert.isTrue(projectMetadata != null, "Project is not available.");

        topLevelPath = projectMetadata.getTopLevelPackage()
                .getFullyQualifiedPackageName();

        // Adds project base package name.
        params.put("project_base_package", topLevelPath);

        String aspectName = StringUtils.capitalize(projectMetadata
                .getProjectName());

        int splitIndex = aspectName.indexOf("-");
        if (splitIndex != -1 && splitIndex != 0) {

            aspectName = aspectName.substring(0, splitIndex);
        }

        // Adds aspect name.
        params.put("project_name", aspectName);

        // Adds entity class
        template = replaceParams(template, params);

        // 2) Exists template ?
        String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
                topLevelPath.concat(".exceptions.").replace('.', '/').concat(
                        aspectName).concat(ITD_FILE_NAME));

        if (!fileManager.exists(fileLocation)) {
            // Create file.
            fileManager
                    .createOrUpdateTextFileIfRequired(fileLocation, template);
        }

        // 3) Add web Fault.
        String webFaultDeclaration = "declare @type: ${exception_class_name}: @WebFault(name = \"${name}\", targetNamespace = \"${targetNamespace}\", faultBean = \"${faultBean}\");";

        Map<String, String> exceptionDeclaration = new HashMap<String, String>();
        exceptionDeclaration.put("exception_class_name", exceptionClass
                .getFullyQualifiedTypeName());

        for (AnnotationAttributeValue<?> annotationAttributeValue : annotationAttributeValues) {
            exceptionDeclaration
                    .put(annotationAttributeValue.getName().toString(),
                            annotationAttributeValue.getValue().toString());
        }

        webFaultDeclaration = replaceParams(webFaultDeclaration,
                exceptionDeclaration);

        // Update file with definition.
        String fileContents;
        try {
            fileContents = FileCopyUtils.copyToString(new FileReader(
                    fileLocation));
        } catch (Exception e) {
            throw new IllegalStateException("Could not get the file:\t'"
                    + fileLocation + "'", e.getCause());
        }

        // Check if Exception has already been defined.
        if (!fileContents.contains(webFaultDeclaration)) {
            String updatedFilecontents;

            int eOFindex = fileContents.lastIndexOf("}");

            updatedFilecontents = fileContents.substring(0, eOFindex).concat(
                    "    ").concat(webFaultDeclaration).concat("\n\n}");

            fileManager.createOrUpdateTextFileIfRequired(fileLocation,
                    updatedFilecontents);

        } else {
            logger.log(Level.FINE, "Exception '"
                    + exceptionClass.getFullyQualifiedTypeName()
                    + "' has already been exported as @WebFault.");
        }

    }

    /**
     * Replace parameters defined in template '${paramName}' with parameter map
     * values.
     * 
     * @param template
     *            to update.
     * @param params
     *            to set in template, key and value.
     * @return Updated template.
     */
    private String replaceParams(String template, Map<String, String> params) {
        for (Entry<String, String> entry : params.entrySet()) {
            template = StringUtils.replace(template, "${" + entry.getKey()
                    + "}", entry.getValue());
        }
        return template;
    }

    /**
     * Check for each method exception if its extended classes are extending
     * from 'java.jang.Throwable'.
     * 
     * @param throwType
     *            for check.
     * @return true if is an Exception.
     */
    private boolean checkExceptionExtension(JavaType throwType) {

        String fileLocation;
        boolean extendsThrowable = false;

        fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA, throwType
                .getFullyQualifiedTypeName().replace('.', '/').concat(".java"));

        // Exception defined in the project or imported.
        if (fileManager.exists(fileLocation)) {

            // Load class or interface details.
            // If class not found an exception will be raised.
            ClassOrInterfaceTypeDetails typeDetails = classpathOperations
                    .getClassOrInterface(throwType);

            // Check and get mutable instance
            Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                    typeDetails, "Can't modify " + typeDetails.getName());
            MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) typeDetails;

            for (JavaType extendedJavaType : mutableTypeDetails
                    .getExtendsTypes()) {

                // Check for each extended class.
                extendsThrowable = checkExceptionExtension(extendedJavaType);

            }

        } else {

            // Check if extends from 'java.lang.Throwable'.
            extendsThrowable = extendsJavaType(throwType, "java.lang.Throwable");

        }

        return extendsThrowable;
    }

    /**
     * Check if javaType extends from 'extendedJavaType' class.
     * 
     * @param javaType
     *            to check if extends from type.
     * @param extendedJavaType
     *            Java type to check.
     * @return true if class extends from extendedJavaType or false if is not
     *         extending.
     */
    private boolean extendsJavaType(JavaType javaType, String extendedJavaType) {

        if (javaType.getFullyQualifiedTypeName()
                .contentEquals(extendedJavaType)) {
            return true;
        }
        try {
            
            Class<?> classToCheck = Class.forName(javaType
                    .getFullyQualifiedTypeName());

            if (classToCheck.getSuperclass() == null) {
                return false;
            }

            return extendsJavaType(new JavaType(classToCheck
                    .getSuperclass().getName()), extendedJavaType);

        } catch (ClassNotFoundException e) {

            throw new IllegalArgumentException(
                    "The class: '"
                            + javaType.getFullyQualifiedTypeName()
                            + "' doesn't exist while checking if extends '"
                            + extendedJavaType
                            + "'.\nClasses that are not from JDK or project can't be used in Web Services.");
        }
    }

    /**
     * Check if javaType implements from 'implmentedJavaType' class.
     * 
     * @param javaType
     *            to check if implements from type.
     * @param implmentedJavaType
     *            Java type to check.
     * @return true if class implements from implementedJavaType or false if is
     *         not implementing.
     */
    private boolean implementsJavaType(JavaType javaType,
            String implmentedJavaType) {

        if (javaType.getFullyQualifiedTypeName().contentEquals(
                implmentedJavaType)) {
            return true;
        }
        try {
            Class<?> classToCheck = Class.forName(javaType
                    .getFullyQualifiedTypeName());

            Class<?>[] interfaceArray = classToCheck.getInterfaces();

            if (interfaceArray.length == 0) {
                return false;
            } else {

                Class<?> interfaceToCheck;
                boolean implementsJavaType = false;
                for (int i = 0; i < interfaceArray.length; i++) {
                    interfaceToCheck = interfaceArray[i];

                    implementsJavaType = implementsJavaType(new JavaType(
                            interfaceToCheck
                            .getName()), implmentedJavaType);

                    if (implementsJavaType) {
                        return implementsJavaType;
                    }
                }
                return implementsJavaType;
            }

        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "The class: '"
                    + javaType.getFullyQualifiedTypeName()
                    + "' doesn't exist while checking if extends '"
                    + implmentedJavaType + "'.");
            return false;
        }

    }

    /**
     * {@inheritDoc}
     * 
     * 
     * <p>
     * If exists any disallowed JavaType in operation:
     * </p>
     * <p>
     * Cancel all the process and show a message explaining that it's not
     * possible to publish this operation because the parameter can't be
     * Marshalled according Ws-I standards.
     * </p>
     */
    public void checkAuthorizedJavaTypesInOperation(JavaType serviceClass,
            JavaSymbolName methodName) {

        MethodMetadata methodToCheck = javaParserService
                .getMethodByNameInClass(serviceClass, methodName);

        Assert.isTrue(methodToCheck != null, "The method: '" + methodName
                + " doesn't exists in the class '"
                + serviceClass.getFullyQualifiedTypeName() + "'.");

        // Check Return type
        JavaType returnType = methodToCheck.getReturnType();

        Assert
                .isTrue(
                        isJavaTypeAllowed(returnType,
                                MethodParameterType.RETURN, serviceClass),
                        "The '"
                                + MethodParameterType.RETURN
                                + "' type '"
                                + returnType.getFullyQualifiedTypeName()
                                + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules."
                                + "\nDefined in: '"
                                + serviceClass.getFullyQualifiedTypeName()
                                + "'.");

        // Check Input Parameters
        List<AnnotatedJavaType> inputParametersList = methodToCheck
                .getParameterTypes();

        for (AnnotatedJavaType annotatedJavaType : inputParametersList) {

            Assert
                    .isTrue(
                            isJavaTypeAllowed(annotatedJavaType.getJavaType(),
                                    MethodParameterType.PARAMETER, serviceClass),
                            "The '"
                                    + MethodParameterType.PARAMETER
                                    + "' type '"
                                    + annotatedJavaType.getJavaType()
                                            .getFullyQualifiedTypeName()
                                    + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules."
                                    + "\nDefined in: '"
                                    + serviceClass.getFullyQualifiedTypeName()
                                    + "'.");

        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check allowed JavaType:
     * </p>
     * <ul>
     * <li>
     * Java basic types and basic objects.</li>
     * <li>
     * Project {@link Entity}. Adds @GvNIXXmlElement annotation to Entity.</li>
     * <li>
     * Collections that don't implement/extend: Map, Set, Tree.</li>
     * </ul>
     */
    public boolean isJavaTypeAllowed(JavaType javaType,
            MethodParameterType methodParameterType, JavaType serviceClass) {

        // Is not null.
        Assert.isTrue(javaType != null, "JavaType '" + methodParameterType
                + "' type can't be 'null'.");

        String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
                javaType.getFullyQualifiedTypeName().replace('.', File.separatorChar).concat(
                        ".java"));

        // It's an imported collection or map ?
        // FIX: Only permits to check classes imported into project that are loaded in system classLloader.
        if (!fileManager.exists(fileLocation) && (!javaType.getParameters().isEmpty() 
                ||implementsJavaType(javaType, ITERABLE)
                || implementsJavaType(javaType, MAP))) {

            // Check if javaType is an available collection.
            if (isNotAllowedCollectionType(javaType)) {
                logger
                        .log(
                                Level.WARNING,
                                "The '"
                                        + methodParameterType
                                        + "' type '"
                                        + javaType.getFullyQualifiedTypeName()
                                        + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.\nThis is a disallowed collection defined in: '"
                                        + serviceClass
                                                .getFullyQualifiedTypeName()
                                        + "'.");

                return false;
            }

            boolean parameterAllowed = true;

            // if its a Collection of Objects.
            List<JavaType> parameterList = javaType.getParameters();
            if (!parameterList.isEmpty()) {

                // 1) yes - check if is allowed
                // Check if is not an allowed collection
                // 1.1) yes - recursive with its javaType.
                // 1.2) no - error.

                // Check collection's parameter.

                for (JavaType parameterJavaType : parameterList) {
                    parameterAllowed = parameterAllowed
                            && isJavaTypeAllowed(parameterJavaType,
                                    methodParameterType, serviceClass);
                }
            }
            return parameterAllowed;
        }

        // 2) no continue.

        // Check if is primitive value.
        if (javaType.isPrimitive()) {
            return true;
        }

        // Java Types in 'java.lang' package that aren't collections.
        if (javaType.getFullyQualifiedTypeName().startsWith("java.lang")) {
            return true;
        }

        // Java Types in 'java.util' package that aren't collections like Date.
        if (javaType.getFullyQualifiedTypeName().startsWith("java.util")) {
            return true;
        }

        if (fileManager.exists(fileLocation)) {

            // If it's an entity field set as not allow.
            if (methodParameterType.equals(MethodParameterType.XMLENTITY)) {
                return false;
            }

            // MetadataID
            String targetId = PhysicalTypeIdentifier.createIdentifier(javaType,
                    Path.SRC_MAIN_JAVA);

            // Obtain the physical type and itd mutable details
            PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                    .get(targetId);
            Assert.notNull(ptm, "Java source class doesn't exists.");

            PhysicalTypeDetails ptd = ptm.getPhysicalTypeDetails();
            Assert.notNull(ptd,
                    "Java source code details unavailable for type "
                            + PhysicalTypeIdentifier.getFriendlyName(targetId));
            Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, ptd,
                    "Java source code is immutable for type "
                            + PhysicalTypeIdentifier.getFriendlyName(targetId));
            MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

            // Add @GvNIXXmlElement annotation.
            List<AnnotationAttributeValue<?>> annotationAttributeValueList = new ArrayList<AnnotationAttributeValue<?>>();

            StringAttributeValue nameStringAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("name"), StringUtils
                            .uncapitalize(javaType.getSimpleTypeName()));

            annotationAttributeValueList.add(nameStringAttributeValue);

            StringAttributeValue namespaceStringAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("namespace"),
                    serviceLayerWsConfigService
                            .convertPackageToTargetNamespace(javaType
                                    .getPackage().toString()));

            annotationAttributeValueList.add(namespaceStringAttributeValue);

            // Create attribute elementList for allowed javaType fields.
            ArrayAttributeValue<StringAttributeValue> elementListArrayAttributeValue = getElementFields(
                    mutableTypeDetails, methodParameterType);

            StringAttributeValue xmlTypeNameStringAttributeValue;
            if (elementListArrayAttributeValue != null
                    && !elementListArrayAttributeValue.getValue().isEmpty()) {

                xmlTypeNameStringAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("xmlTypeName"), javaType
                                .getSimpleTypeName());
                annotationAttributeValueList
                        .add(xmlTypeNameStringAttributeValue);

            }
            else {
                
                xmlTypeNameStringAttributeValue = new StringAttributeValue(
                        new JavaSymbolName("xmlTypeName"), "");
                annotationAttributeValueList
                        .add(xmlTypeNameStringAttributeValue);
            }

            annotationAttributeValueList.add(elementListArrayAttributeValue);

            // Exported attribute value
            BooleanAttributeValue exportedBooleanAttributeValue = new BooleanAttributeValue(new JavaSymbolName("exported"), false);

            annotationAttributeValueList.add(exportedBooleanAttributeValue);

            annotationsService.addJavaTypeAnnotation(mutableTypeDetails
                    .getName(), GvNIXXmlElement.class.getName(),
                    annotationAttributeValueList, false);

            return true;

        }

        // TODO: Create an Aj file to declare objects that doesn't belong to
        // project. In Roo next version fix it with Classpath loaders.

        logger.log(Level.INFO, "The " + methodParameterType
                + " parameter type: '" + javaType.getFullyQualifiedTypeName()
                                + "' in method '' from class '"
                                + serviceClass.getFullyQualifiedTypeName()
                                + "' does not belong to project class definitions and its not mapped to be used in web service operation.");
        
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public ArrayAttributeValue<StringAttributeValue> getElementFields(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            MethodParameterType methodParameterType) {

        List<FieldMetadata> fieldMetadataTransientList = new ArrayList<FieldMetadata>();
        List<FieldMetadata> fieldMetadataElementList = new ArrayList<FieldMetadata>();

        List<FieldMetadata> tmpFieldMetadataElementList = new ArrayList<FieldMetadata>();

        String identifier = EntityMetadata.createIdentifier(governorTypeDetails
                .getName(), Path.SRC_MAIN_JAVA);

        // Obtain the entity metadata type and itd mutable details.
        EntityMetadata entityMetadata = (EntityMetadata) metadataService
                .get(identifier);

        // Check if exists add id and version fields.
        if (entityMetadata != null && entityMetadata.isValid()) {
            fieldMetadataElementList.add(entityMetadata.getIdentifierField());
            fieldMetadataElementList.add(entityMetadata.getVersionField());
        }

        // Retrieve the fields that are defined as OneToMany relationship.
        List<FieldMetadata> oneToManyFieldMetadataList = MemberFindingUtils
                .getFieldsWithAnnotation(governorTypeDetails, new JavaType(
                        "javax.persistence.OneToMany"));

        fieldMetadataTransientList.addAll(oneToManyFieldMetadataList);

        // Retrieve the fields that are defined as ManyToOne relationship.
        List<FieldMetadata> manyToOneFieldMetadataList = MemberFindingUtils
                .getFieldsWithAnnotation(governorTypeDetails, new JavaType(
                        "javax.persistence.ManyToOne"));

        fieldMetadataTransientList.addAll(manyToOneFieldMetadataList);

        // Retrieve the fields that are defined as OneToOne relationship.
        List<FieldMetadata> oneToOneFieldMetadataList = MemberFindingUtils
                .getFieldsWithAnnotation(governorTypeDetails, new JavaType(
                        "javax.persistence.OneToOne"));

        fieldMetadataTransientList.addAll(oneToOneFieldMetadataList);

        // Unsupported collection.
        List<? extends FieldMetadata> declaredFieldList = governorTypeDetails
                .getDeclaredFields();

        // Remove checked fields from collection.
        for (FieldMetadata declaredField : declaredFieldList) {
            if (!fieldMetadataElementList.contains(declaredField)) {
                fieldMetadataElementList.add(declaredField);
            }
        }

        fieldMetadataElementList.removeAll(fieldMetadataTransientList);
        tmpFieldMetadataElementList.addAll(fieldMetadataElementList);

        boolean isAllowed;

        // Transient collection fields.
        for (FieldMetadata fieldMetadata : tmpFieldMetadataElementList) {

            isAllowed = isJavaTypeAllowed(fieldMetadata.getFieldType(),
                    MethodParameterType.XMLENTITY, governorTypeDetails
                            .getName());

            // Add field that implements disallowed collection
            // interface.
            if (!isAllowed) {
                fieldMetadataElementList.remove(fieldMetadata);
            }
        }

        // Create array Attribute.
        StringAttributeValue propOrderAttributeValue;
        List<StringAttributeValue> propOrderList = new ArrayList<StringAttributeValue>();

        for (FieldMetadata fieldMetadata : fieldMetadataElementList) {
            propOrderAttributeValue = new StringAttributeValue(
                    new JavaSymbolName("ignored"), fieldMetadata.getFieldName()
                            .getSymbolName());

            if (!propOrderList.contains(propOrderAttributeValue)) {
                propOrderList.add(propOrderAttributeValue);
            }
        }

        ArrayAttributeValue<StringAttributeValue> propOrderAttributeList = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("elementList"), propOrderList);

        return propOrderAttributeList;

    }

    /**
     * Checks correct namespace URI format. Suffix 'http://'.
     * 
     * <p>
     * If String is blank is also correct.
     * </p>
     * 
     * @param namespace
     *            string to check as correct namespace.
     * 
     * @return true if is blank or if has correct URI format.
     */
    public boolean checkNamespaceFormat(String namespace) {
        if (StringUtils.hasText(namespace)) {

            try {
                URI uri = new URI(namespace);

            } catch (URISyntaxException e) {
                return false;
            }

        }
        return true;
    }

    /**
     * {@inheritDoc}
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
     */
    public boolean isNotAllowedCollectionType(JavaType javaType) {

        boolean notAllowed = false;

        // Check if JavaType is or Extends notAllowedClassCollectionTypes.
        for (String notAllowedClassCollection : notAllowedClassCollectionTypes) {

            notAllowed = extendsJavaType(javaType, notAllowedClassCollection);

            if (notAllowed) {

                logger
                        .warning("The method parameter '"
                                + javaType.getFullyQualifiedTypeName()
                                + "' type '"
                                + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.\nThis is or Extends a disallowed collection.");
                return notAllowed;
            }
        }

        for (String notAllowedInterfaceCollection : notAllowedIntefaceCollectionTypes) {

            notAllowed = implementsJavaType(javaType,
                    notAllowedInterfaceCollection);

            if (notAllowed) {
                logger
                        .warning("The method parameter '"
                                + javaType.getFullyQualifiedTypeName()
                                + "' type '"
                                + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.\nThis is or Implements a disallowed collection.");
                return notAllowed;
            }
        }

        return notAllowed;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public String getWebServiceDefaultNamespace(JavaType serviceClass) {

        // Retrieve Web Service target Namespace value.
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = classpathOperations
                .getClassOrInterface(serviceClass);

        // Check and get mutable instance
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                classOrInterfaceTypeDetails, "Can't modify "
                        + classOrInterfaceTypeDetails.getName());

        AnnotationMetadata gvNixWebServiceAnnotaionMetadata = MemberFindingUtils
                .getTypeAnnotation(classOrInterfaceTypeDetails, new JavaType(
                        GvNIXWebService.class.getName()));

        Assert.isTrue(gvNixWebServiceAnnotaionMetadata != null,
                "Launch command 'service define ws --class "
                        + serviceClass.getFullyQualifiedTypeName()
                        + "' to export class to Web Service.");

        StringAttributeValue webServiceTargetNamespaceAttributeValue = (StringAttributeValue) gvNixWebServiceAnnotaionMetadata
                .getAttribute(new JavaSymbolName("targetNamespace"));

        Assert
                .isTrue(
                        webServiceTargetNamespaceAttributeValue != null
                                && StringUtils
                                        .hasText(webServiceTargetNamespaceAttributeValue
                                                .getValue()),
                        "You must define 'targetNamespace' annotation attribute in @GvNIXWebService in class: '"
                                + serviceClass.getFullyQualifiedTypeName()
                                + "'.");

        String webServiceTargetNamespace = webServiceTargetNamespaceAttributeValue
                .getValue();

        Assert
                .isTrue(
                        checkNamespaceFormat(webServiceTargetNamespace),
                        "Attribute 'targetNamespace' in @GvNIXWebService for Web Service class '"
                                + serviceClass.getFullyQualifiedTypeName()
                                + "'has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        return webServiceTargetNamespace;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public boolean checkIsNotRooEntity(JavaType serviceClass) {

        ClassOrInterfaceTypeDetails typeDetails = classpathOperations
                .getClassOrInterface(serviceClass);

        String identifier = EntityMetadata.createIdentifier(typeDetails
                .getName(), Path.SRC_MAIN_JAVA);

        EntityMetadata entityMetadata = (EntityMetadata) metadataService
                .get(identifier);

        return entityMetadata == null;
    }

}
