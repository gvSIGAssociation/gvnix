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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

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
    private final JavaType destination;

    private final JavaType aspectName;
    private final PhysicalTypeMetadata governorPhysicalTypeMetadata;

    private static final String PROVIDES_TYPE_STRING = OCCChecksumMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    // Template portions to create *_Roo_GvNIXRelatedPattern.aj when replacing vars with values and concatenated in order
    private static final String ITD_TEMPLATE_CLASS_START = "Entity_gvnix_persistence_occ.aj_template1";
    private static final String ITD_TEMPLATE_METHODGET_MESSAGE_DIGEST = "Entity_gvnix_persistence_occ.aj_template2";
    private static final String ITD_TEMPLATE_METHOD_REMOVE = "Entity_gvnix_persistence_occ.aj_template3";
    private static final String ITD_TEMPLATE_METHOD_MERGE = "Entity_gvnix_persistence_occ.aj_template4";
    private static final String ITD_TEMPLATE_METHOD_CHECK_CONCURRENCY = "Entity_gvnix_persistence_occ.aj_template5";
    private static final String ITD_TEMPLATE_METHOD_LOAD_CHECKSUM = "Entity_gvnix_persistence_occ.aj_template6";
    private static final String ITD_TEMPLATE_METHOD_CHECKSUM_DIGEST = "Entity_gvnix_persistence_occ.aj_template7";
    private static final String ITD_TEMPLATE_CLASS_END = "Entity_gvnix_persistence_occ.aj_template8";

    private static final String TO_STRING_CODE_LINE_FORMAT = "\tsb.append((String.valueOf(${property}).equals(\"null\") ? nullstr : String.valueOf(${property})) + separator);\n";

    private JpaActiveRecordMetadata entityMetadata;

    // DiSiD: Used to get the type members
    private final MemberDetailsScanner memberDetailsScanner;

    private String itdFileContents = null;

    // From annotation
    @AutoPopulate
    private final String fieldName = "occChekcsum";
    @AutoPopulate
    private final String digestMethod = "md5";

    public OCCChecksumMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaActiveRecordMetadata entityMetadata,
            MemberDetailsScanner memberDetailsScanner, TypeManagementService typeManagementService,
            PersistenceMemberLocator persistenceMemberLocator) {

        // From AbstractItdTypeDetailsProvidingMetadataItem
        super(identifier);
        Validate.notNull(aspectName, "Aspect name required");
        Validate.notNull(governorPhysicalTypeMetadata,
                "Governor physical type metadata required");

        this.aspectName = aspectName;
        this.governorPhysicalTypeMetadata = governorPhysicalTypeMetadata;

        // DiSiD: Initialize memberDetailsScanner
        this.memberDetailsScanner = memberDetailsScanner;

        PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();

        if (!(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
            // There is a problem
            valid = false;
        } else {
            // We have reliable physical type details
            governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }

        this.destination = governorTypeDetails.getName();

        Validate.isTrue(isValid(identifier), "Metadata identification string '"
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
            addChecksumFieldToEntity(field, getter, setter, typeManagementService);

            // Generates ITD
            itdFileContents = generateITDContents(field, persistenceMemberLocator);
        }
    }
    
    /**
     * Replace some vars with values in all AspectJ template portions and concat results in order into one string. 
     * 
     * <p>Vars has next format: ${entity_package}.
     * Template portions will be placed at same package in src/main/resources.
     * If some method placed in a portion already defined (push-in), don't add it to result string.</p>
     * 
     * @param checksumField Checksum field to get field name (one var value).
     * @param persistenceMemberLocator To get identifier field (one var value).
     * @return All template portions replacing vars with values and concatenated in order.
     */
    private String generateITDContents(FieldMetadata checksumField, PersistenceMemberLocator persistenceMemberLocator) {

    	StringBuilder contents = new StringBuilder();
    	
    	contents.append(generateITDContent(ITD_TEMPLATE_CLASS_START, checksumField, persistenceMemberLocator));
    	if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, new JavaSymbolName("getMessageDigest"), null) == null) {
    		contents.append(generateITDContent(ITD_TEMPLATE_METHODGET_MESSAGE_DIGEST, checksumField, persistenceMemberLocator));
    	}
    	if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, new JavaSymbolName("remove"), null) == null) {
    		contents.append(generateITDContent(ITD_TEMPLATE_METHOD_REMOVE, checksumField, persistenceMemberLocator));
    	}
    	if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, new JavaSymbolName("merge"), null) == null) {
    		contents.append(generateITDContent(ITD_TEMPLATE_METHOD_MERGE, checksumField, persistenceMemberLocator));
    	}
    	List<JavaType> parameters = new ArrayList<JavaType>();
    	parameters.add(new JavaType(governorTypeDetails.getName().getSimpleTypeName()));
    	if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, new JavaSymbolName("checkConcurrency")) == null) {
    		contents.append(generateITDContent(ITD_TEMPLATE_METHOD_CHECK_CONCURRENCY, checksumField, persistenceMemberLocator));
    	}
    	if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, new JavaSymbolName("loadChecksum"), null) == null) {
    		contents.append(generateITDContent(ITD_TEMPLATE_METHOD_LOAD_CHECKSUM, checksumField, persistenceMemberLocator));
    	}
    	if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, new JavaSymbolName("checksumDigest"), null) == null) {
    		contents.append(generateITDContent(ITD_TEMPLATE_METHOD_CHECKSUM_DIGEST, checksumField, persistenceMemberLocator));
    	}
    	contents.append(generateITDContent(ITD_TEMPLATE_CLASS_END, checksumField, persistenceMemberLocator));
        
        return contents.toString();
    }

    /**
     * Replace some vars with values in a template file name. 
     * 
     * <p>Vars has next format: ${entity_package}.
     * Template will be placed at same package in src/main/resources.</p>
     * 
     * @param templateName File name with a template.
     * @param checksumField Checksum field to get field name (one var value).
     * @param persistenceMemberLocator To get identifier field (one var value).
     * @return String replacing vars with values.
     */
    private String generateITDContent(String templateName, FieldMetadata checksumField, PersistenceMemberLocator persistenceMemberLocator) {

        // We use a template for generate ITD because the class
        // org.springframework.roo.classpath.details.DefaultItdTypeDetailsBuilder
        // dosen't have metadata for manage 'pointcuts' and 'advices'.

        String template;
        try {
            template = IOUtils.toString(new InputStreamReader(this
                    .getClass().getResourceAsStream(templateName)));
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable load " + templateName, ioe);
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

        // Adds find by id method name ('findById_method')
        params.put("findById_method", entityMetadata.getFindMethod()
                .getMethodName().getSymbolName());

        // Adds id field name ('id_field')
        // TODO Now get identifier is a collection, temporaly getted first
        params.put("id_field", persistenceMemberLocator.getIdentifierFields(governorPhysicalTypeMetadata.getMemberHoldingTypeDetails()
                .getName()).get(0)
                .getFieldName().getSymbolName());

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
     * <li>has accessor</li>
     * <li>no {@link javax.persistence.Version} annotated</li>
     * <li>no {@link javax.persistence.EmbeddedId} annotated</li>
     * <li>no relationship</li>
     * <li>no transient</li>
     * <li></li>
     * </ol>
     * 
     * TODO Include support for relationship and embeddedId
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
                        
                        if (MemberFindingUtils.getAnnotationOfType(field
                                .getAnnotations(), new JavaType(
                                "javax.persistence.EmbeddedId")) != null) {
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
            MethodMetadata getter, MethodMetadata setter, TypeManagementService typeManagementService) {

        PhysicalTypeDetails ptd = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();

        Validate.isInstanceOf(
                ClassOrInterfaceTypeDetails.class,
                ptd,
                "Java source code is immutable for type "
                        + PhysicalTypeIdentifier
                                .getFriendlyName(governorPhysicalTypeMetadata
                                        .getId()));
        
        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetails = new ClassOrInterfaceTypeDetailsBuilder((ClassOrInterfaceTypeDetails)ptd);

        // Try to locate an existing field with @javax.persistence.Version

        try {
        	// FIXME List type is not equal to object to check (it's a bug??)
            if (!mutableTypeDetails.getDeclaredFields().contains(field)) {
                mutableTypeDetails.addField(new FieldMetadataBuilder(governorTypeDetails.getDeclaredByMetadataId(), field));
            }
            if (!mutableTypeDetails.getDeclaredMethods().contains(getter)) {
                mutableTypeDetails.addMethod(getter);
            }
            if (!mutableTypeDetails.getDeclaredMethods().contains(setter)) {
                mutableTypeDetails.addMethod(setter);
            }
            typeManagementService.createOrUpdateTypeOnDisk(mutableTypeDetails.build());

        } catch (IllegalArgumentException e) {
            // TODO In some cases, one element is added more than one time
        }

    }

    @Override
    public String toString() {
    	ToStringBuilder tsc = new ToStringBuilder(this);
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
        	Validate.isTrue(found.size() == 1,
                    "More than 1 field was annotated with @javax.persistence.Version in '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName() + "'");
            FieldMetadata field = found.get(0);

            Validate.isTrue(
                    field.getFieldType().equals(
                            new JavaType(String.class.getName())), "Field '"
                            + field.getFieldName().getSymbolName()
                            + "' must be java.lang.String");
            Validate.isTrue(MemberFindingUtils.getAnnotationOfType(field
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
        StringBuilder fieldNameBuilder = new StringBuilder();
        JavaSymbolName checksumField = null;
        while (true) {
            // Compute the required field name
            fieldNameBuilder.append('_');

            checksumField = new JavaSymbolName(fieldNameBuilder.toString().concat(this.fieldName));
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
                JavaType.STRING).build();

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

    public static final String createIdentifier(JavaType javaType, LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
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
