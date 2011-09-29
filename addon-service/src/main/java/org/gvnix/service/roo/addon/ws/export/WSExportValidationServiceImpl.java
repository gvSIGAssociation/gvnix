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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.JavaParserService;
import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.ws.export.WSExportOperations.MethodParameterType;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;

/**
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Jose Manuel Vivó Arnal at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WSExportValidationServiceImpl implements WSExportValidationService {

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private WSConfigService wSConfigService;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private TypeLocationService typeLocationService;

    private static final String ITD_TEMPLATE = "Web_Faults_gvnix_service_layer-template.aj";

    private static final String ITD_FILE_NAME = "_Web_Faults_gvnix_service_layer.aj";

    private static final Set<String> invalidClasses = new HashSet<String>();

    static {
        invalidClasses.add(HashMap.class.getName());
        invalidClasses.add(TreeMap.class.getName());
    }

    private static final Set<String> invalidIntefaces = new HashSet<String>();

    static {
        invalidIntefaces.add(Map.class.getName());
    }

    private static final String ITERABLE = Iterable.class.getName();

    private static final String MAP = Map.class.getName();

    private static Logger logger = Logger
            .getLogger(WSExportValidationServiceImpl.class.getName());

    /**
     * {@inheritDoc}
     */
    public boolean prepareMethodExceptions(MethodMetadata method,
            String webServiceTargetNamespace) {

        Assert.isTrue(method != null, "The method doesn't exists in the class");

        List<JavaType> throwsTypes = method.getThrowsTypes();

        String fileLocation;

        boolean extendsThrowable = true;

        for (JavaType throwType : throwsTypes) {

            extendsThrowable = checkExceptionExtension(throwType);

            Assert.isTrue(
                    extendsThrowable,
                    "The '"
                            + throwType.getFullyQualifiedTypeName()
                            + "' class doesn't extend from 'java.lang.Throwable'."
                            + "'\nIt can't be used as Exception in method to be thrown.");

            fileLocation = projectOperations.getPathResolver().getIdentifier(
                    Path.SRC_MAIN_JAVA,
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
     * Adds a declaration of <code>@WebFault</code> to exceptionClass in AspectJ
     * file.
     * 
     * @param exceptionClass
     *            to export as web service exception.
     * @param annotationAttributeValues
     *            defined for annotation.
     */
    protected void exportImportedException(JavaType exceptionClass,
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
        String fileLocation = projectOperations.getPathResolver()
                .getIdentifier(
                        Path.SRC_MAIN_JAVA,
                        topLevelPath.concat(".exceptions.").replace('.', '/')
                                .concat(aspectName).concat(ITD_FILE_NAME));

        if (!fileManager.exists(fileLocation)) {
            // Create file.
            fileManager.createOrUpdateTextFileIfRequired(fileLocation,
                    template, true);
        }

        // 3) Add web Fault.
        String webFaultDeclaration = "declare @type: ${exception_class_name}: @WebFault(name = \"${name}\", targetNamespace = \"${targetNamespace}\", faultBean = \"${faultBean}\");";

        Map<String, String> exceptionDeclaration = new HashMap<String, String>();
        exceptionDeclaration.put("exception_class_name",
                exceptionClass.getFullyQualifiedTypeName());

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

            updatedFilecontents = fileContents.substring(0, eOFindex)
                    .concat("    ").concat(webFaultDeclaration).concat("\n\n}");

            fileManager.createOrUpdateTextFileIfRequired(fileLocation,
                    updatedFilecontents, true);

        } else {
            logger.log(Level.FINE,
                    "Exception '" + exceptionClass.getFullyQualifiedTypeName()
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

        fileLocation = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_JAVA,
                throwType.getFullyQualifiedTypeName().replace('.', '/')
                        .concat(".java"));

        // Exception defined in the project or imported.
        if (fileManager.exists(fileLocation)) {

            // Load class or interface details.
            // If class not found an exception will be raised.
            ClassOrInterfaceTypeDetails typeDetails = typeLocationService
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
     * Check if a java type extends another java type name.
     * 
     * @param javaType
     *            to check if extends a java type name
     * @param ext
     *            Extends java type name to check
     * @return javaType extends ext ?
     */
    private boolean extendsJavaType(JavaType javaType, String ext) {

        // Get the java type name
        String javaName = javaType.getFullyQualifiedTypeName();

        // Java name and extends name are the same
        if (javaName.contentEquals(ext)) {
            return true;
        }

        try {

            // Get java class
            Class<?> javaClass = Class.forName(javaName);

            // Java class has no superclass
            if (javaClass.getSuperclass() == null) {
                return false;
            }

            // Search on extends for each superclass
            return extendsJavaType(new JavaType(javaClass.getSuperclass()
                    .getName()), ext);

        } catch (ClassNotFoundException e) {

            throw new IllegalArgumentException(
                    "The class: '"
                            + javaName
                            + "' doesn't exist while checking if extends '"
                            + ext
                            + "'.\nClasses that are not from JDK or project can't be used in Web Services.");
        }
    }

    /**
     * Check if a java type implements another java type name.
     * 
     * @param javaType
     *            to check if implements a java type name
     * @param impl
     *            Implements java type name to check
     * @return javaType implements impl ?
     */
    private boolean implementsJavaType(JavaType javaType, String impl) {

        // Get the java type name
        String javaName = javaType.getFullyQualifiedTypeName();

        // Java name and implements name are the same
        if (javaName.contentEquals(impl)) {
            return true;
        }

        try {

            // Get java name interfaces
            Class<?>[] javaInterfaces = Class.forName(javaName).getInterfaces();

            // Java class has no interfaces
            if (javaInterfaces.length == 0) {
                return false;
            }

            // Search on implements for each interface
            for (int i = 0; i < javaInterfaces.length; i++) {
                if (implementsJavaType(
                        new JavaType(javaInterfaces[i].getName()), impl)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {

            logger.warning("The class: '" + javaName
                    + "' doesn't exist while checking if extends '" + impl
                    + "'.");
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If exists any disallowed JavaType in operation:
     * </p>
     * <p>
     * Cancel all the process and show a message explaining that it's not
     * possible to publish this operation because the parameter can't be
     * Marshalled according Ws-I standards.
     * </p>
     */
    public void prepareAuthorizedJavaTypesInOperation(MethodMetadata method) {

        Assert.isTrue(method != null, "The method doesn't exists in the class");

        // Check Return type
        JavaType returnType = method.getReturnType();

        Assert.isTrue(
                isTypeAllowed(returnType, MethodParameterType.RETURN),
                "The '"
                        + MethodParameterType.RETURN
                        + "' type '"
                        + returnType.getFullyQualifiedTypeName()
                        + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.");

        // Check Input Parameters
        List<AnnotatedJavaType> inputParametersList = method
                .getParameterTypes();

        for (AnnotatedJavaType annotatedJavaType : inputParametersList) {

            Assert.isTrue(
                    isTypeAllowed(annotatedJavaType.getJavaType(),
                            MethodParameterType.PARAMETER),
                    "The '"
                            + MethodParameterType.PARAMETER
                            + "' type '"
                            + annotatedJavaType.getJavaType()
                                    .getFullyQualifiedTypeName()
                            + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.");

        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTypeAllowed(JavaType javaType, MethodParameterType type) {

        // javaType is required
        Assert.isTrue(javaType != null, "JavaType '" + type
                + "' type can't be 'null'.");

        // Get the java type name
        String javaName = javaType.getFullyQualifiedTypeName();

        // Get the file path related with java type name
        String javaPath = javaName.replace('.', File.separatorChar).concat(
                ".java");
        String javaId = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_JAVA, javaPath);

        // Is an allowed java type in class loader ?

        // File not exists in system class loader
        // FIXME: Only search in class loader !
        if (!fileManager.exists(javaId)) {

            // Java type has parameters (array) or is a collection (implements
            // iterable) or is a map (implements map) ?
            List<JavaType> params = javaType.getParameters();
            if ((!params.isEmpty() || implementsJavaType(javaType, ITERABLE) || implementsJavaType(
                    javaType, MAP))) {

                // Is an allowed java type in our project ?
                return isClassLoaderTypeAllowed(javaType, type);
            }
        }

        // Is an allowed java type in JDK ?

        // Allowed: Java type is primitive
        if (javaType.isPrimitive()) {
            return true;
        }

        // Allowed: Java type is in 'java.lang' package (no collections)
        if (javaName.startsWith("java.lang")) {
            return true;
        }

        // Allowed: Java type is in 'java.util' package (no collections)
        if (javaName.startsWith("java.util")) {
            return true;
        }

        // Is an allowed java type in class loader ?

        // File exists in system class loader
        // FIXME: Only search in class loader !
        if (fileManager.exists(javaId)) {

            // Not allowed: Java type is an XML entity field
            if (type.equals(MethodParameterType.XMLENTITY)) {
                return false;
            }

            // Get mutable class or interface type details from java type
            MutableClassOrInterfaceTypeDetails mutableTypeDetails = getTypeDetails(javaType);

            // Not allowed: Type details superclass is not allowed java type
            ClassOrInterfaceTypeDetails typeDetailsSuper = mutableTypeDetails
                    .getSuperclass();
            if (typeDetailsSuper != null
                    && !isTypeAllowed(typeDetailsSuper.getName(), type)) {
                return false;
            }

            // Add gvNIX XML Element annotation
            annotationsService.addJavaTypeAnnotation(
                    mutableTypeDetails.getName(),
                    GvNIXXmlElement.class.getName(),
                    getGvNIXXmlElementAnnotation(javaType, mutableTypeDetails),
                    false);

            return true;
        }

        // TODO: Create Aj file to declare objects doesn't belong to project
        // In Roo next version fix it with Classpath loaders.

        logger.log(
                Level.INFO,
                "The ".concat(type.toString())
                        .concat(" parameter type: '")
                        .concat(javaName)
                        .concat("' does not belong to project class definitions and its not mapped to be used in web service operation."));

        return true;
    }

    /**
     * Is a java type from class loader allowed ?
     * 
     * <ul>
     * <li>Java type is allowed type</li>
     * <li>Java type parameters are allowed types, if exists</li>
     * <li></li>
     * </ul>
     * 
     * @param javaType
     *            Java type
     * @param type
     *            Type of the java type
     * @return Is a java type from class loader allowed ?
     */
    protected boolean isClassLoaderTypeAllowed(JavaType javaType,
            MethodParameterType type) {

        // Not allowed: JavaType is a not allowed type
        if (isNotAllowedType(javaType)) {
            logger.log(
                    Level.WARNING,
                    "The '"
                            + type
                            + "' type '"
                            + javaType.getFullyQualifiedTypeName()
                            + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules."
                            + "\nThis is a disallowed collection defined");
            return false;
        }

        // Java type is a collection of objects
        List<JavaType> params = javaType.getParameters();
        if (!params.isEmpty()) {

            // Allowed: All java type parameters are allowed
            boolean parameterAllowed = true;
            for (JavaType param : params) {
                parameterAllowed = parameterAllowed
                        && isTypeAllowed(param, type);
            }
            return parameterAllowed;
        }

        return true;
    }

    /**
     * Create @GvNIXXmlElement annotation for java type with type fields.
     * 
     * <ul>
     * <li>name attribute value from java type simple name</li>
     * <li>namespace attribute value from java type package</li>
     * <li>elementList attribute from type details allowed element fields</li>
     * <li>exported attribute is always false (when code first)</li>
     * <li>xmlTypeName from java type simple type, if not empty</li>
     * </ul>
     * 
     * @param javaType
     *            To get name, namespace and xmlTypeName annotation attributes
     * @param typeDetails
     *            To get elementList annotation attribute
     * @return List of annotation attribute values
     */
    protected List<AnnotationAttributeValue<?>> getGvNIXXmlElementAnnotation(
            JavaType javaType, MutableClassOrInterfaceTypeDetails typeDetails) {

        List<AnnotationAttributeValue<?>> annotation = new ArrayList<AnnotationAttributeValue<?>>();

        // name attribute value from java type simple name
        StringAttributeValue name = new StringAttributeValue(
                new JavaSymbolName("name"), StringUtils.uncapitalize(javaType
                        .getSimpleTypeName()));
        annotation.add(name);

        // namespace attribute value from java type package
        StringAttributeValue namespace = new StringAttributeValue(
                new JavaSymbolName("namespace"),
                wSConfigService.convertPackageToTargetNamespace(javaType
                        .getPackage().toString()));
        annotation.add(namespace);

        // elementList attribute from type details allowed element fields
        ArrayAttributeValue<StringAttributeValue> fields = getFields(typeDetails);
        annotation.add(fields);

        // xmlTypeName from java type simple type, if not empty
        if (fields != null && !fields.getValue().isEmpty()) {

            StringAttributeValue xmlTypeName = new StringAttributeValue(
                    new JavaSymbolName("xmlTypeName"),
                    javaType.getSimpleTypeName());
            annotation.add(xmlTypeName);

        } else {

            StringAttributeValue xmlTypeName = new StringAttributeValue(
                    new JavaSymbolName("xmlTypeName"), "");
            annotation.add(xmlTypeName);
        }

        // exported attribute is always false (when code first)
        BooleanAttributeValue exported = new BooleanAttributeValue(
                new JavaSymbolName("exported"), false);
        annotation.add(exported);

        return annotation;
    }

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
     * TODO Utility class: Remove from interface?
     * 
     * @param governorTypeDetails
     *            class to get fields to check.
     * @return {@link ArrayAttributeValue} with fields to be published as
     *         '@XmlElement.'
     */
    protected ArrayAttributeValue<StringAttributeValue> getFields(
            ClassOrInterfaceTypeDetails governorTypeDetails) {

        // Get the entity metadata (AJ) from governor type (Java) name
        EntityMetadata entity = getEntityMetadata(governorTypeDetails.getName());

        // Get allowed element fields
        List<FieldMetadata> fields = getAllowedElementFields(
                governorTypeDetails, entity);

        // Create a list of values from element list field names
        List<StringAttributeValue> values = new ArrayList<StringAttributeValue>();
        for (FieldMetadata field : fields) {

            StringAttributeValue value = new StringAttributeValue(
                    new JavaSymbolName("ignored"), field.getFieldName()
                            .getSymbolName());
            if (!values.contains(value)) {
                values.add(value);
            }
        }

        return new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("elementList"), values);
    }

    /**
     * Get allowed element fields from governor type and entity metadata.
     * 
     * <ul>
     * <li>Get identifier and version fields from entity.</li>
     * <li>Get all fields from governor and remove not allowed types fields.</li>
     * <li>Remove not allowed entity types fields for entity.</li>
     * </ul>
     * 
     * @param governorTypeDetails
     *            Governor type (Java)
     * @param entity
     *            Entity metadata (AJ)
     * @return Field metadata list of allowed element fields
     */
    protected List<FieldMetadata> getAllowedElementFields(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            EntityMetadata entity) {

        // Element fields list
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();

        // Element list: Add identifier and version fields from entity (AJ)
        if (entity != null && entity.isValid()) {
            // Identifier and/or version can not exists
            if (entity.getIdentifierField() != null) {
                fields.add(entity.getIdentifierField());
            }
            if (entity.getVersionField() != null) {
                fields.add(entity.getVersionField());
            }
        }

        // Element list: Add all fields from governor type (Java)
        // May have transient fields
        for (FieldMetadata field : governorTypeDetails.getDeclaredFields()) {
            // Not repeat fields, like previous identifier and/or version
            if (!fields.contains(field)) {
                fields.add(field);
            }
        }

        // Element list: Remove transient fields
        fields.removeAll(getTransientFields(governorTypeDetails));

        // Temporary fields list initialized with element fields
        List<FieldMetadata> tmpFields = new ArrayList<FieldMetadata>();
        tmpFields.addAll(fields);

        // Element list: Remove not allowed types fields
        // from governor type (Java) name
        for (FieldMetadata tmpField : tmpFields) {

            boolean isAllowed = isTypeAllowed(tmpField.getFieldType(),
                    MethodParameterType.XMLENTITY);

            // Add field that implements disallowed collection interface
            if (!isAllowed) {
                fields.remove(tmpField);
            }
        }

        return fields;
    }

    /**
     * Get transient fields from governor type.
     * 
     * <ul>
     * <li>Get OneToMany fields from governor type (Java).</li>
     * <li>Get ManyToOne fields from governor type (Java).</li>
     * <li>Get OneToOne fields from governor type (Java).</li>
     * </ul>
     * 
     * @param governorTypeDetails
     *            Governor type (Java)
     * @return Field metadata list of transient fields
     */
    protected List<FieldMetadata> getTransientFields(
            ClassOrInterfaceTypeDetails governorTypeDetails) {

        // Fields list
        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();

        // Add OneToMany fields from governor type (Java)
        fields.addAll(MemberFindingUtils.getFieldsWithAnnotation(
                governorTypeDetails,
                new JavaType("javax.persistence.OneToMany")));

        // Add ManyToOne fields from governor type (Java)
        fields.addAll(MemberFindingUtils.getFieldsWithAnnotation(
                governorTypeDetails,
                new JavaType("javax.persistence.ManyToOne")));

        // Add OneToOne fields from governor type (Java)
        fields.addAll(MemberFindingUtils
                .getFieldsWithAnnotation(governorTypeDetails, new JavaType(
                        "javax.persistence.OneToOne")));

        return fields;
    }

    /**
     * Get mutable class or interface type details from java type.
     * 
     * @param javaType
     *            Java type
     * @return Mutable class or interface type
     */
    protected MutableClassOrInterfaceTypeDetails getTypeDetails(
            JavaType javaType) {

        // Get mutable class or interface type details from java type
        String id = PhysicalTypeIdentifier.createIdentifier(javaType,
                Path.SRC_MAIN_JAVA);
        PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                .get(id);
        Assert.notNull(ptm, "Java source class doesn't exists.");
        PhysicalTypeDetails ptd = ptm.getMemberHoldingTypeDetails();
        Assert.notNull(ptd, "Java source code details unavailable for type "
                + PhysicalTypeIdentifier.getFriendlyName(id));
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(id));

        return (MutableClassOrInterfaceTypeDetails) ptd;
    }

    /**
     * Get the entity metadata (AJ) from governor type (Java) name.
     * 
     * @param name
     *            Gobernor type (Java) name
     * @return Related entity metadata (AJ)
     */
    protected EntityMetadata getEntityMetadata(JavaType name) {

        return (EntityMetadata) metadataService.get(EntityMetadata
                .createIdentifier(name, Path.SRC_MAIN_JAVA));
    }

    /**
     * Checks correct namespace URI format. Suffix 'http://'.
     * <p>
     * If String is blank is also correct.
     * </p>
     * 
     * @param namespace
     *            string to check as correct namespace.
     * @return true if is blank or if has correct URI format.
     */
    public boolean checkNamespaceFormat(String namespace) {
        if (StringUtils.hasText(namespace)) {

            try {
                new URI(namespace);

            } catch (URISyntaxException e) {
                return false;
            }

        }
        return true;
    }

    /**
     * Check if a java type is a not allowed type.
     * 
     * <p>
     * The list of not allowed types are some sorted collections:
     * </p>
     * <ul>
     * <li>Is an array</li>
     * <li>Extends HashMap class</li>
     * <li>Extends TreeMap class</li>
     * <li>Implements Map interface</li>
     * </ul>
     * 
     * @param javaType
     *            The java type to check
     * @return Is the java type not allowed ?
     */
    protected boolean isNotAllowedType(JavaType javaType) {

        // Java type name
        String javaName = javaType.getFullyQualifiedTypeName();

        // Check if java type is or extends not allowed classes
        for (String invalidClass : invalidClasses) {

            if (extendsJavaType(javaType, invalidClass)) {

                logger.warning("The method parameter '"
                        + javaName
                        + "' type is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules."
                        + "\nThis is or Extends a disallowed collection.");
                return true;
            }
        }

        // Check if java type is or implements not allowed interfaces
        for (String invalidInterface : invalidIntefaces) {

            if (implementsJavaType(javaType, invalidInterface)) {
                logger.warning("The method parameter '"
                        + javaName
                        + "' type is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules."
                        + "\nThis is or Implements a disallowed collection.");
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getWebServiceDefaultNamespace(JavaType serviceClass) {

        // Retrieve Web Service target Namespace value.
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = typeLocationService
                .getClassOrInterface(serviceClass);

        // Check and get mutable instance
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                classOrInterfaceTypeDetails, "Can't modify "
                        + classOrInterfaceTypeDetails.getName());

        AnnotationMetadata gvNixWebServiceAnnotaionMetadata = MemberFindingUtils
                .getTypeAnnotation(classOrInterfaceTypeDetails, new JavaType(
                        GvNIXWebService.class.getName()));

        Assert.isTrue(
                gvNixWebServiceAnnotaionMetadata != null,
                "Launch command 'service define ws --class "
                        + serviceClass.getFullyQualifiedTypeName()
                        + "' to export class to Web Service.");

        StringAttributeValue webServiceTargetNamespaceAttributeValue = (StringAttributeValue) gvNixWebServiceAnnotaionMetadata
                .getAttribute(new JavaSymbolName("targetNamespace"));

        Assert.isTrue(
                webServiceTargetNamespaceAttributeValue != null
                        && StringUtils
                                .hasText(webServiceTargetNamespaceAttributeValue
                                        .getValue()),
                "You must define 'targetNamespace' annotation attribute in @GvNIXWebService in class: '"
                        + serviceClass.getFullyQualifiedTypeName() + "'.");

        String webServiceTargetNamespace = webServiceTargetNamespaceAttributeValue
                .getValue();

        Assert.isTrue(
                checkNamespaceFormat(webServiceTargetNamespace),
                "Attribute 'targetNamespace' in @GvNIXWebService for Web Service class '"
                        + serviceClass.getFullyQualifiedTypeName()
                        + "'has to start with 'http://'.\ni.e.: http://name.of.namespace/");
        return webServiceTargetNamespace;
    }

}
