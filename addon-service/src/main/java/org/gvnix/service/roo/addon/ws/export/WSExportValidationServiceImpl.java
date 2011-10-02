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
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.JavaParserService;
import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.annotations.GvNIXWebService;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.ws.WSConfigService;
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
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
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

    /**
     * {@inheritDoc}
     */
    public void addGvNixWebFaultToExceptions(MethodMetadata method,
            String targetNamespace) {

        // Method is required
        Assert.isTrue(method != null, "The method doesn't exists in the class");

        // Get all throws types
        List<JavaType> types = method.getThrowsTypes();
        for (JavaType type : types) {

            addGvNixWebFaultToException(targetNamespace, type);
        }
    }

    /**
     * Add GvNIXWebFault to exception and extends java types in project.
     * 
     * @param method
     *            Add annotation to java type exception in project
     * @param targetNamespace
     *            Target namespace to add as annotation attribute
     */
    protected void addGvNixWebFaultToException(String targetNamespace,
            JavaType type) {

        // Exception type exists in the project sources
        if (fileManager.exists(getJavaTypeIdentifier(type))) {

            // Add GvNIXWebFault annotation to type
            addGvNixWebFaultAnnotation(targetNamespace, type);

            // Add gvNIX web fault to parent types in project too
            for (JavaType extend : getTypeDetails(type).getExtendsTypes()) {
                addGvNixWebFaultToException(targetNamespace, extend);
            }
        }
    }

    /**
     * Add GvNIXWebFault with attributes to exception java type in project.
     * 
     * <ul>
     * <li>name: from uncapitalized java type simple type name</li>
     * <li>targetNamespace: from input parameter</li>
     * <li>faultBean: from java type fully qualified type name</li>
     * </ul>
     * 
     * @param targetNamespace
     *            Target namespace to add as annotation attribute
     * @param type
     *            Type to add annotation
     */
    protected void addGvNixWebFaultAnnotation(String targetNamespace,
            JavaType type) {

        // Annotation attributes: name, target namespace and fault bean
        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
        attrs.add(new StringAttributeValue(new JavaSymbolName("name"),
                StringUtils.uncapitalize(type.getSimpleTypeName())));
        attrs.add(new StringAttributeValue(
                new JavaSymbolName("targetNamespace"), targetNamespace));
        attrs.add(new StringAttributeValue(new JavaSymbolName("faultBean"),
                type.getFullyQualifiedTypeName()));

        // Add gvNIX web fault annotation to exception
        annotationsService.addJavaTypeAnnotation(type,
                GvNIXWebFault.class.getName(), attrs, false);
    }

    /**
     * {@inheritDoc}
     */
    public void addGvNixXmlElementToTypes(MethodMetadata method) {

        Assert.isTrue(method != null, "The method doesn't exists in the class");

        // Add gvNIX xml element annotation to method return type in project
        addGvNixXmlElementToType(method.getReturnType());

        // Add gvNIX xml element annotation to parameters types in project
        for (AnnotatedJavaType parameterType : method.getParameterTypes()) {

            addGvNixXmlElementToType(parameterType.getJavaType());
        }
    }

    /**
     * Add GvNIXXmlElement to java type in project.
     * 
     * <p>
     * GvNIXXmlElement annotation is added to parent extend types in project and
     * type parameters in project too.
     * </p>
     * 
     * @param javaType
     *            Java type (can't be null)
     * @return Is it allowed ?
     */
    protected void addGvNixXmlElementToType(JavaType javaType) {

        // javaType is required
        Assert.isTrue(javaType != null, "JavaType type can't be 'null'.");

        // Java type exists in the project sources
        if (fileManager.exists(getJavaTypeIdentifier(javaType))) {

            MutableClassOrInterfaceTypeDetails typeDetails = getTypeDetails(javaType);

            // Add gvNIX XML Element annotation
            addGvNixXmlElementAnnotation(javaType, typeDetails.getName());

            // Add gvNIX XML Element to parent type (b.e. Owner->AbstractPerson)
            for (JavaType extend : typeDetails.getExtendsTypes()) {
                addGvNixXmlElementToType(extend);
            }
        }

        // Check parameters types (b.e. List<Owner>)
        for (JavaType paramType : javaType.getParameters()) {
            addGvNixXmlElementToType(paramType);
        }
    }

    /**
     * Add @GvNIXXmlElement annotation with attributes to java type.
     * 
     * <ul>
     * <li>name attribute value from java type simple name</li>
     * <li>namespace attribute value from java type package</li>
     * <li>elementList attribute from all not transient fields (Java and AJs)</li>
     * <li>exported attribute is always false</li>
     * <li>xmlTypeName from java simple type, if not empty</li>
     * </ul>
     * 
     * @param javaType
     *            To get attributes for gvNIX annotation
     * @param typeName
     *            Type name to add annotation
     */
    protected void addGvNixXmlElementAnnotation(JavaType javaType,
            JavaType typeName) {

        List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();

        // name attribute value from java type simple name
        StringAttributeValue name = new StringAttributeValue(
                new JavaSymbolName("name"), StringUtils.uncapitalize(javaType
                        .getSimpleTypeName()));
        attrs.add(name);

        // namespace attribute value from java type package
        StringAttributeValue namespace = new StringAttributeValue(
                new JavaSymbolName("namespace"),
                wSConfigService.convertPackageToTargetNamespace(javaType
                        .getPackage().toString()));
        attrs.add(namespace);

        // Create attribute list with all (Java & AJs) no transient fields
        List<FieldMetadata> fields = javaParserService.getFieldsInAll(typeName);
        List<StringAttributeValue> values = new ArrayList<StringAttributeValue>();
        for (FieldMetadata field : fields) {

            // Transient fields can't have JAXB annotations (b.e. entityManager)
            if (field.getModifier() != Modifier.TRANSIENT) {

                // Create an attribute list with fields
                StringAttributeValue value = new StringAttributeValue(
                        new JavaSymbolName("ignored"), field.getFieldName()
                                .getSymbolName());
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
        }
        ArrayAttributeValue<StringAttributeValue> elements = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("elementList"), values);

        attrs.add(elements);

        // xmlTypeName from java type simple type, if not empty
        if (elements != null && !elements.getValue().isEmpty()) {

            StringAttributeValue xmlTypeName = new StringAttributeValue(
                    new JavaSymbolName("xmlTypeName"),
                    javaType.getSimpleTypeName());
            attrs.add(xmlTypeName);

        } else {

            StringAttributeValue xmlTypeName = new StringAttributeValue(
                    new JavaSymbolName("xmlTypeName"), "");
            attrs.add(xmlTypeName);
        }

        // exported attribute is always false (when code first)
        BooleanAttributeValue exported = new BooleanAttributeValue(
                new JavaSymbolName("exported"), false);
        attrs.add(exported);

        annotationsService.addJavaTypeAnnotation(typeName,
                GvNIXXmlElement.class.getName(), attrs, false);
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
                javaType.getFullyQualifiedTypeName()
                        .replace('.', File.separatorChar).concat(".java"));
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
