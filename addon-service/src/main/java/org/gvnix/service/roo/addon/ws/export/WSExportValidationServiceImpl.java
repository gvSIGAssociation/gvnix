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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private AnnotationsService annotationsService;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private JavaParserService javaParserService;

    private static final String ITD_TEMPLATE = "Web_Faults_gvnix_service_layer-template.aj";

    private static final String ITD_FILE_NAME = "_Web_Faults_gvnix_service_layer.aj";

    private static Logger logger = Logger
            .getLogger(WSExportValidationServiceImpl.class.getName());

    /**
     * {@inheritDoc}
     */
    public void prepareExceptions(MethodMetadata method, String targetNamespace) {

        // Method is required
        Assert.isTrue(method != null, "The method doesn't exists in the class");

        // Get all throws types
        List<JavaType> throwsTypes = method.getThrowsTypes();
        for (JavaType throwsType : throwsTypes) {

            // Check if throws java type extends java.lang.Throwable
            boolean extendsThrowable = checkExceptionExtension(throwsType);
            Assert.isTrue(
                    extendsThrowable,
                    "The '"
                            + throwsType.getFullyQualifiedTypeName()
                            + "' class doesn't extend from 'java.lang.Throwable'."
                            + "'\nIt can't be used as Exception in method to be thrown.");

            // Create annotation attributes
            List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
            attrs.add(new StringAttributeValue(new JavaSymbolName("name"),
                    StringUtils.uncapitalize(throwsType.getSimpleTypeName())));
            attrs.add(new StringAttributeValue(new JavaSymbolName(
                    "targetNamespace"), targetNamespace));
            attrs.add(new StringAttributeValue(new JavaSymbolName("faultBean"),
                    throwsType.getFullyQualifiedTypeName()));

            // Is the throws type defined in the project ?
            String id = getJavaTypeIdentifier(throwsType);
            if (fileManager.exists(id)) {

                // Exception defined into project: Define annotation.
                annotationsService.addJavaTypeAnnotation(throwsType,
                        GvNIXWebFault.class.getName(), attrs, false);

            } else {

                // Exception imported: Add definition in own AspectJ file
                exportImportedException(throwsType, attrs);
            }
        }
    }

    /**
     * Add declaration of <code>@WebFault</code> to java type in AspectJ.
     * 
     * <p>
     * This AspectJ declarations are defined for non project exceptions. Are
     * placed in exceptions subpackage in top level package.
     * </p>
     * 
     * @param javaType
     *            to add exception ITD with @WebFault declaration
     * @param attrs
     *            defined for annotation
     */
    protected void exportImportedException(JavaType javaType,
            List<AnnotationAttributeValue<?>> attrs) {

        Map<String, String> params = new HashMap<String, String>();

        // Add project fully qualified top level package
        ProjectMetadata project = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        Assert.isTrue(project != null, "Project is not available.");
        String topLevelPath = project.getTopLevelPackage()
                .getFullyQualifiedPackageName();
        params.put("project_base_package", topLevelPath);

        // Add project name prefix (before '-' char)
        String projectName = StringUtils.capitalize(project.getProjectName());
        int index = projectName.indexOf("-");
        if (index != -1 && index != 0) {

            projectName = projectName.substring(0, index);
        }
        params.put("project_name", projectName);

        // Get the ITD template and replace params with values
        String template;
        try {
            template = FileCopyUtils.copyToString(new InputStreamReader(this
                    .getClass().getResourceAsStream(ITD_TEMPLATE)));
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable load ITD web fault definitions template", ioe);
        }
        template = replaceParams(template, params);

        // ITD already exists on disk ?
        String id = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_JAVA,
                topLevelPath.concat(".exceptions.").replace('.', '/')
                        .concat(projectName).concat(ITD_FILE_NAME));
        if (!fileManager.exists(id)) {
            // Save ITD on disk if not exists
            fileManager.createOrUpdateTextFileIfRequired(id, template, true);
        }

        // Get web fault declaration replacing atributes with input values
        String webFault = "declare @type: ${exception_class_name}: @WebFault(name = \"${name}\","
                + " targetNamespace = \"${targetNamespace}\", faultBean = \"${faultBean}\");";
        Map<String, String> exceptions = new HashMap<String, String>();
        exceptions.put("exception_class_name",
                javaType.getFullyQualifiedTypeName());
        for (AnnotationAttributeValue<?> attr : attrs) {
            exceptions.put(attr.getName().toString(), attr.getValue()
                    .toString());
        }
        webFault = replaceParams(webFault, exceptions);

        // Get ITD from disk and add web fault declaration if not exists
        String content;
        try {
            content = FileCopyUtils.copyToString(new FileReader(id));
        } catch (Exception e) {
            throw new IllegalStateException("Could not get the file:\t'" + id
                    + "'", e.getCause());
        }
        if (!content.contains(webFault)) {
            // Exception is not already defined
            fileManager.createOrUpdateTextFileIfRequired(
                    id,
                    content.substring(0, content.lastIndexOf("}"))
                            .concat("    ").concat(webFault).concat("\n\n}"),
                    true);
        } else {
            // Exception is already defined
            logger.log(Level.FINE,
                    "Exception '" + javaType.getFullyQualifiedTypeName()
                            + "' has already been exported as @WebFault.");
        }
    }

    /**
     * Replace in template map params keys with map params values.
     * 
     * @param template
     *            to update
     * @param params
     *            to replace in template, key and value
     * @return Updated template
     */
    private String replaceParams(String template, Map<String, String> params) {

        for (Entry<String, String> entry : params.entrySet()) {
            template = StringUtils.replace(template, "${" + entry.getKey()
                    + "}", entry.getValue());
        }

        return template;
    }

    /**
     * Check if throws java type extends java.lang.Throwable.
     * 
     * <p>
     * Check imported exception or project exception and their extends types.
     * </p>
     * 
     * @param throwsType
     *            Exception java type
     * @return true if is an Exception
     */
    protected boolean checkExceptionExtension(JavaType throwsType) {

        boolean result = false;

        String fileLocation = getJavaTypeIdentifier(throwsType);
        if (fileManager.exists(fileLocation)) {

            // Exception defined in project

            // Get mutable type details from throws type
            ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                    .getClassOrInterface(throwsType);
            Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                    typeDetails, "Can't modify " + typeDetails.getName());
            MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) typeDetails;

            // Check for each extended class
            for (JavaType extendedJavaType : mutableTypeDetails
                    .getExtendsTypes()) {
                result = checkExceptionExtension(extendedJavaType);
            }
        } else {

            // Exception imported

            // Check if throws type extends from 'java.lang.Throwable'.
            result = extendsJavaType(throwsType, "java.lang.Throwable");
        }

        return result;
    }

    /**
     * Check if a java type extends another java type name.
     * 
     * <p>
     * Check all super classes of this java type.
     * </p>
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

            // Java type not exists: nor in JDK, nor in project
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void prepareAllowedJavaTypes(MethodMetadata method) {

        Assert.isTrue(method != null, "The method doesn't exists in the class");

        // Check method return type is allowed
        JavaType returnType = method.getReturnType();
        Assert.isTrue(
                isTypeAllowed(returnType, MethodParameterType.RETURN),
                "The '"
                        + MethodParameterType.RETURN
                        + "' type '"
                        + returnType.getFullyQualifiedTypeName()
                        + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.");

        // Check method parameters types are allowed
        List<AnnotatedJavaType> parameterTypes = method.getParameterTypes();
        for (AnnotatedJavaType parameterType : parameterTypes) {

            Assert.isTrue(
                    isTypeAllowed(parameterType.getJavaType(),
                            MethodParameterType.PARAMETER),
                    "The '"
                            + MethodParameterType.PARAMETER
                            + "' type '"
                            + parameterType.getJavaType()
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

        if (isBasicAllowed(javaType)) {

            // Allowed: Basic JDK java types
            return true;

        } else {

            // Not allowed: Java type is an XML entity field already
            if (type.equals(MethodParameterType.XMLENTITY)) {
                return false;
            }

            if (fileManager.exists(javaId)) {

                // File exists in project sources

                // Add gvNIX XML Element annotation
                MutableClassOrInterfaceTypeDetails mutableTypeDetails = getTypeDetails(javaType);
                annotationsService.addJavaTypeAnnotation(
                        mutableTypeDetails.getName(),
                        GvNIXXmlElement.class.getName(),
                        getGvNIXXmlElementAnnotation(javaType,
                                mutableTypeDetails), false);

                return true;

            } else {

                // File not exists in project nor is a JDK basic type
                return false;
            }
        }
    }

    /**
     * Check basic JDK allowed java types.
     * 
     * <ul>
     * <li>Java type is primitive</li>
     * <li>Java type is in JDK 'java.lang' package (String)</li>
     * <li>Java type is in JDK 'java.util' package (Date)</li>
     * <li>Java type is in JDK 'java.math' package (BigDecimal)</li>
     * </ul>
     * 
     * @param javaType
     * @return
     */
    protected boolean isBasicAllowed(JavaType javaType) {

        // Get the java type name and check primitive or allowed packages
        String javaName = javaType.getFullyQualifiedTypeName();
        if (javaType.isPrimitive() || javaName.startsWith("java.lang")
                || javaName.startsWith("java.util")
                || javaName.startsWith("java.math")) {

            return true;
        }

        return false;
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

        // Get allowed element fields and create an attribute list with them
        List<FieldMetadata> fields = getAllowedElementFields(typeDetails);
        List<StringAttributeValue> values = new ArrayList<StringAttributeValue>();
        for (FieldMetadata field : fields) {

            StringAttributeValue value = new StringAttributeValue(
                    new JavaSymbolName("ignored"), field.getFieldName()
                            .getSymbolName());
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        ArrayAttributeValue<StringAttributeValue> attrs = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("elementList"), values);

        annotation.add(attrs);

        // xmlTypeName from java type simple type, if not empty
        if (attrs != null && !attrs.getValue().isEmpty()) {

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
     * Get allowed element fields from type details (Java) and related AJs.
     * 
     * <ul>
     * <li>Get all fields from type details (Java) and related AJs.</li>
     * <li>Remove not allowed types fields.</li>
     * </ul>
     * 
     * @param typeDetails
     *            Type details (Java)
     * @return Field metadata list of allowed element fields
     */
    protected List<FieldMetadata> getAllowedElementFields(
            ClassOrInterfaceTypeDetails typeDetails) {

        // Element fields list
        List<FieldMetadata> fields = javaParserService
                .getFieldsInAll(typeDetails.getName());

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
     * {@inheritDoc}
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
     * Get the path identifier for a java type in the project.
     * 
     * @param javaType
     *            Java type
     * @return Path identifier in project
     */
    protected String getJavaTypeIdentifier(JavaType javaType) {

        return projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_JAVA,
                javaType.getFullyQualifiedTypeName().replace('.', '/')
                        .concat(".java"));
    }

    /**
     * {@inheritDoc}
     */
    public String getWebServiceDefaultNamespace(JavaType javaType) {

        // Get and validate mutable type details
        ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getClassOrInterface(javaType);
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                typeDetails, "Can't modify " + typeDetails.getName());

        // Get and validate gvNIX web service annotation
        AnnotationMetadata annotation = MemberFindingUtils.getTypeAnnotation(
                typeDetails, new JavaType(GvNIXWebService.class.getName()));
        Assert.isTrue(
                annotation != null,
                "Launch command 'service define ws --class "
                        + javaType.getFullyQualifiedTypeName()
                        + "' to export class to Web Service.");

        // Get and validate gvNIX web service annotation target namespace attr
        StringAttributeValue targetNamespace = (StringAttributeValue) annotation
                .getAttribute(new JavaSymbolName("targetNamespace"));
        Assert.isTrue(
                targetNamespace != null
                        && StringUtils.hasText(targetNamespace.getValue()),
                "You must define 'targetNamespace' annotation attribute in @GvNIXWebService in class: '"
                        + javaType.getFullyQualifiedTypeName() + "'.");

        // Get and validate gvNIX web service annotation target namespace value
        String targetNamespaceValue = targetNamespace.getValue();
        Assert.isTrue(
                checkNamespaceFormat(targetNamespaceValue),
                "Attribute 'targetNamespace' in @GvNIXWebService for Web Service class '"
                        + javaType.getFullyQualifiedTypeName()
                        + "'has to start with 'http://'.\ni.e.: http://name.of.namespace/");

        return targetNamespaceValue;
    }

}
