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
package org.gvnix.occ.roo.addon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;

/**
 * gvNIX OCCChecksum Metadata
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class OCCChecksumMetadata extends AbstractMetadataItem implements
        ItdTypeDetailsProvidingMetadataItem {

    private final Logger logger = HandlerUtils
            .getLogger(OCCChecksumMetadata.class);

    // From AbstractItdTypeDetailsProvidingMetadataItem
    private ClassOrInterfaceTypeDetails governorTypeDetails;
    private JavaType destination;

    private JavaType aspectName;
    private PhysicalTypeMetadata governorPhysicalTypeMetadata;

    private static final String PROVIDES_TYPE_STRING = OCCChecksumMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final String ITD_TEMPLATE = "Entity_gvnix_persistence_occ.aj_template";

    private static final String TO_STRING_CODE_LINE_FORMAT = "\tsb.append((String.valueOf(${property}).equals(\"null\") ? nullstr : String.valueOf(${property})) + separator);\n";

    private EntityMetadata entityMetadata;

    // DiSiD: Used to get the type members
    private MemberDetailsScanner memberDetailsScanner;

    private String itdFileContents = null;

    // From annotation
    @AutoPopulate
    private final String fieldName = "occChekcsum";
    @AutoPopulate
    private final String digestMethod = "md5";

    public OCCChecksumMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            EntityMetadata entityMetadata,
            MemberDetailsScanner memberDetailsScanner) {

        // From AbstractItdTypeDetailsProvidingMetadataItem
        super(identifier);
        Assert.notNull(aspectName, "Aspect name required");
        Assert.notNull(governorPhysicalTypeMetadata,
                "Governor physical type metadata required");

        this.aspectName = aspectName;
        this.governorPhysicalTypeMetadata = governorPhysicalTypeMetadata;

        // DiSiD: Initialize memberDetailsScanner
        this.memberDetailsScanner = memberDetailsScanner;

        PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();

        if (physicalTypeDetails == null
                || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
            // There is a problem
            valid = false;
        } else {
            // We have reliable physical type details
            governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }

        this.destination = governorTypeDetails.getName();

        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        if (entityMetadata != null) {
            this.entityMetadata = entityMetadata;

            if (!isValid()) {
                return;
            }

            // Process values from the annotation, if present
            AnnotationMetadata annotation = MemberFindingUtils
                    .getDeclaredTypeAnnotation(
                            governorTypeDetails,
                            new JavaType(GvNIXEntityOCCChecksum.class.getName()));
            if (annotation != null) {
                AutoPopulationUtils.populate(this, annotation);
            }

            // Adds field to entity java file add persist it
            FieldMetadata field = getChecksumField();
            MethodMetadata getter = getChecksumAccessor();
            MethodMetadata setter = getChecksumMutator();
            addChecksumFieldToEntity(field, getter, setter);

            // Generates ITD
            itdFileContents = generateITDContents(field);
        }
    }

    private String generateITDContents(FieldMetadata checksumField) {

        // We use a template for generate ITD because the class
        // org.springframework.roo.classpath.details.DefaultItdTypeDetailsBuilder
        // dosen't have metadata for manage 'pointcuts' and 'advices'.

        String template;
        try {
            template = FileCopyUtils.copyToString(new InputStreamReader(this
                    .getClass().getResourceAsStream(ITD_TEMPLATE)));
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable load ITD Checksum_occ template", ioe);
        }

        Map<String, String> params = new HashMap<String, String>(10);

        // Adds digest generator method ('digest_method')
        params.put("digest_method", this.digestMethod);

        // Adds entity class package ('entity_package')
        params.put("entity_package", governorTypeDetails.getName().getPackage()
                .getFullyQualifiedPackageName());

        // Adds entity class name ('Entity_class')
        params.put("entity_class", governorTypeDetails.getName()
                .getSimpleTypeName());

        // Adds merge method name ('merge_method')
        params.put("merge_method", entityMetadata.getMergeMethod()
                .getMethodName().getSymbolName());

        // Adds merge method name ('remove_method')
        params.put("remove_method", entityMetadata.getRemoveMethod()
                .getMethodName().getSymbolName());

        // Adds find by id method name ('findById_method')
        params.put("findById_method", entityMetadata.getFindMethod()
                .getMethodName().getSymbolName());

        // Adds id field name ('id_field')
        params.put("id_field", entityMetadata.getIdentifierField()
                .getFieldName().getSymbolName());

        // Adds id field accessor ('id_accessor')
        params.put("id_accessor", entityMetadata.getIdentifierAccessor()
                .getMethodName().getSymbolName());

        // Adds checksum field name ('checksum_field')
        params.put("checksum_field", checksumField.getFieldName()
                .getSymbolName());

        // Adds the code to transform local fields to a string
        // ('local_fields_to_String')
        params.put("local_fields_to_String", getCodeToTranformFieldsToString());

        return replaceParams(template, params);

    }

    /**
     * Gets java code to generate the string that represents the object state.
     * 
     * Currently it uses all the entity's properties that fulfill this
     * conditions:
     * <ol>
     * <li>has accessor and muttator</li>
     * <li>no {@link javax.persistence.Version} annotated</li>
     * <li>no relationship</li>
     * <li>no transient</li>
     * <li></li>
     * </ol>
     * 
     * 
     * @return
     */
    private String getCodeToTranformFieldsToString() {
        StringBuilder strb = new StringBuilder();

        MemberDetails members = memberDetailsScanner.getMemberDetails(
                governorTypeDetails.getClass().getName(), governorTypeDetails);
        for (MemberHoldingTypeDetails memberHoldingTypeDetails : members
                .getDetails()) {
            for (MethodMetadata method : memberHoldingTypeDetails
                    .getDeclaredMethods()) {
                if (BeanInfoUtils.isAccessorMethod(method)) {

                    JavaSymbolName propertyName = BeanInfoUtils
                            .getPropertyNameForJavaBeanMethod(method);

                    FieldMetadata field = BeanInfoUtils
                            .getFieldForPropertyName(members, propertyName);
                    if (field != null) {
                        if (MemberFindingUtils.getAnnotationOfType(field
                                .getAnnotations(), new JavaType(
                                "javax.persistence.Version")) != null) {
                            continue;
                        }

                        if (MemberFindingUtils.getAnnotationOfType(field
                                .getAnnotations(), new JavaType(
                                "javax.persistence.Transient")) != null) {
                            continue;
                        }

                        if (Modifier.isTransient(field.getModifier())) {
                            continue;
                        }

                        if (MemberFindingUtils.getAnnotationOfType(field
                                .getAnnotations(), new JavaType(
                                "javax.persistence.ManyToMany")) != null) {
                            continue;
                        }

                        if (MemberFindingUtils.getAnnotationOfType(field
                                .getAnnotations(), new JavaType(
                                "javax.persistence.OneToMany")) != null) {
                            continue;
                        }

                        if (MemberFindingUtils.getAnnotationOfType(field
                                .getAnnotations(), new JavaType(
                                "javax.persistence.ManyToOne")) != null) {
                            continue;
                        }

                        strb.append(StringUtils.replace(
                                TO_STRING_CODE_LINE_FORMAT, "${property}",
                                field.getFieldName().toString()));
                    }
                }
            }
        }
        return strb.toString();
    }

    private String replaceParams(String template, Map<String, String> params) {
        for (Entry<String, String> entry : params.entrySet()) {
            template = StringUtils.replace(template, "${" + entry.getKey()
                    + "}", entry.getValue());
        }
        return template;
    }

    private void addChecksumFieldToEntity(FieldMetadata field,
            MethodMetadata getter, MethodMetadata setter) {

        PhysicalTypeDetails ptd = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();

        Assert.isInstanceOf(
                MutableClassOrInterfaceTypeDetails.class,
                ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier
                                .getFriendlyName(governorPhysicalTypeMetadata
                                        .getId()));
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) ptd;

        // Try to locate an existing field with @javax.persistence.Version

        try {
            if (!mutableTypeDetails.getDeclaredFields().contains(field)) {
                mutableTypeDetails.addField(field);
            }
            if (!mutableTypeDetails.getDeclaredMethods().contains(getter)) {
                mutableTypeDetails.addMethod(getter);
            }
            if (!mutableTypeDetails.getDeclaredMethods().contains(setter)) {
                mutableTypeDetails.addMethod(setter);
            }
        } catch (IllegalArgumentException e) {
            // TODO In some cases, one element is added more than one time
        }

    }

    @Override
    public String toString() {
        ToStringCreator tsc = new ToStringCreator(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("aspectName", aspectName);
        tsc.append("destinationType", destination);
        tsc.append("governor", governorPhysicalTypeMetadata.getId());
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    /**
     * Locates the checksum field.
     * 
     * @return the checksum (may return null)
     */
    public FieldMetadata getChecksumField() {

        // Try to locate an existing field with @javax.persistence.Version
        List<FieldMetadata> found = MemberFindingUtils.getFieldsWithAnnotation(
                governorTypeDetails, new JavaType("javax.persistence.Version"));
        if (found.size() > 0) {
            Assert.isTrue(found.size() == 1,
                    "More than 1 field was annotated with @javax.persistence.Version in '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName() + "'");
            FieldMetadata field = found.get(0);

            Assert.isTrue(
                    field.getFieldType().equals(
                            new JavaType(String.class.getName())), "Field '"
                            + field.getFieldName().getSymbolName()
                            + "' must be java.lang.String");
            Assert.isTrue(MemberFindingUtils.getAnnotationOfType(field
                    .getAnnotations(), new JavaType(
                    "javax.persistence.Transient")) != null, "Field '"
                    + field.getFieldName().getSymbolName()
                    + "' must have @Transient annotation");

            if (!field.getFieldName().getSymbolName().equals(fieldName)) {
                logger.warning(governorTypeDetails.getName()
                        .getFullyQualifiedTypeName()
                        + ": The @Version field name ("
                        + field.getFieldName().getSymbolName()
                        + ") does not match with @GvNIXEntityOCCChecksum.fieldName ("
                        + fieldName + ")");
            }

            return field;
        }

        // Ensure there isn't already a field called like fieldName; if so,
        // compute a
        // unique name (it's not really a fatal situation at the end of the day)
        int index = -1;
        JavaSymbolName checksumField = null;
        while (true) {
            // Compute the required field name
            index++;
            String fieldName = "";
            for (int i = 0; i < index; i++) {
                fieldName = fieldName + "_";
            }
            fieldName = fieldName + this.fieldName;

            checksumField = new JavaSymbolName(fieldName);
            if (MemberFindingUtils.getField(governorTypeDetails, checksumField) == null) {
                // Found a usable field name
                break;
            }
        }

        // We're creating one
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder idAnnotation = new AnnotationMetadataBuilder(
                new JavaType("javax.persistence.Version"),
                new ArrayList<AnnotationAttributeValue<?>>());
        annotations.add(idAnnotation);

        idAnnotation = new AnnotationMetadataBuilder(new JavaType(
                "javax.persistence.Transient"),
                new ArrayList<AnnotationAttributeValue<?>>());
        annotations.add(idAnnotation);

        FieldMetadata field = new FieldMetadataBuilder(getId(),
                Modifier.PRIVATE, annotations, checksumField,
                JavaType.STRING_OBJECT).build();

        return field;
    }

    /**
     * Locates the checksum accessor method.
     * 
     * @return the version identifier (may return null if there is no version
     *         field declared in this class)
     */
    public MethodMetadata getChecksumAccessor() {
        FieldMetadata checksum = getChecksumField();

        // Compute the name of the accessor that will be produced
        String requiredAccessorName = "get"
                + StringUtils.capitalize(checksum.getFieldName()
                        .getSymbolName());

        // See if the user provided the field, and thus the accessor method
        if (!getId().equals(checksum.getDeclaredByMetadataId())) {
            MethodMetadata method = MemberFindingUtils.getMethod(
                    governorTypeDetails, new JavaSymbolName(
                            requiredAccessorName), new ArrayList<JavaType>());
            return method;
        }

        // We declared the field in this ITD, so produce a public accessor for
        // it
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this."
                + checksum.getFieldName().getSymbolName() + ";");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                new JavaSymbolName(requiredAccessorName),
                checksum.getFieldType(), new ArrayList<AnnotatedJavaType>(),
                new ArrayList<JavaSymbolName>(), bodyBuilder).build();
    }

    /**
     * Locates the checksum mutator
     * 
     * @return the version identifier (may return null if there is no version
     *         field declared in this class)
     */
    public MethodMetadata getChecksumMutator() {

        // Locate the version field, and compute the name of the mutator that
        // will be produced
        FieldMetadata chekcsum = getChecksumField();
        if (chekcsum == null) {
            // There's no version field, so there certainly won't be a mutator
            // for it
            return null;
        }
        String requiredMutatorName = "set"
                + StringUtils.capitalize(chekcsum.getFieldName()
                        .getSymbolName());

        List<JavaType> paramTypes = new ArrayList<JavaType>();
        paramTypes.add(chekcsum.getFieldType());
        List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
        paramNames.add(new JavaSymbolName("checksum"));

        // See if the user provided the field, and thus the accessor method
        if (!getId().equals(chekcsum.getDeclaredByMetadataId())) {
            MethodMetadata method = MemberFindingUtils.getMethod(
                    governorTypeDetails,
                    new JavaSymbolName(requiredMutatorName), paramTypes);
            return method;
        }

        // We declared the field in this ITD, so produce a public mutator for it
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("this."
                + chekcsum.getFieldName().getSymbolName() + " = checksum;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                new JavaSymbolName(requiredMutatorName),
                JavaType.VOID_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(paramTypes), paramNames,
                bodyBuilder).build();
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public String getItdFileContents() {
        return itdFileContents;
    }

    public ItdTypeDetails getMemberHoldingTypeDetails() {
        // TODO Auto-generated method stub
        return null;
    }

}
