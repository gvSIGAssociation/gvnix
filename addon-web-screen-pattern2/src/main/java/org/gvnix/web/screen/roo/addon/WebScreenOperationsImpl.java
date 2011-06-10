/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.screen.roo.addon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Implementation of web MVC screen patterns operations.
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 * @since 0.8
 */
@Component
@Service
public class WebScreenOperationsImpl implements WebScreenOperations {

    /**
     * Name of {@link GvNIXPattern} attribute value
     */
    public static final JavaSymbolName PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName(
            "value");

    /**
     * {@link GvNIXPattern} JavaType
     */
    public static final JavaType PATTERN_ANNOTATION = new JavaType(
            GvNIXPattern.class.getName());

    /**
     * Name of {@link GvNIXRelationPattern} attribute value
     */
    public static final JavaSymbolName RELATION_PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName(
            "value");

    /**
     * {@link GvNIXRelationPattern} JavaType
     */
    public static final JavaType RELATION_PATTERN_ANNOTATION = new JavaType(
            GvNIXRelationsPattern.class.getName());

    /**
     * {@link GvNIXPattern} JavaType
     */
    public static final JavaType RELATED_PATTERN_ANNOTATION = new JavaType(
            GvNIXRelatedPattern.class.getName());

    /**
     * Name of {@link GvNIXRelatedPattern} attribute value
     */
    public static final JavaSymbolName RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName(
            "value");

    /**
     * Name of {@link RooWebScaffold} attribute formBackingObject
     */
    public static final JavaSymbolName ROOWEBSCAFFOLD_ANNOTATION_ATTR_VALUE_FORMBACKINGOBJECT = new JavaSymbolName(
            "formBackingObject");

    /**
     * {@link RooWebScaffold} JavaType
     */
    public static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    /**
     * {@link GvNIXEntityBatch} JavaType
     */
    public static final JavaType ENTITYBATCH_ANNOTATION = new JavaType(
            GvNIXEntityBatch.class.getName());

    public static final JavaType ONETOMANY_ANNOTATION = new JavaType(
            "javax.persistence.OneToMany");

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference
    private MetadataService metadataService;

    @Reference
    private TypeLocationService typeLocationService;

    /**
     * Use the PhysicalTypeMetadataProvider to access information about a
     * physical type in the project
     */
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    @Reference
    private PatternService patternService;

    @Reference
    private WebScreenConfigService configService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        return configService.isSpringMvcProject();
    }

    /** {@inheritDoc} */
    public void addPattern(JavaType controllerClass, JavaSymbolName name,
            WebPattern pattern) {

        Assert.notNull(controllerClass, "controller is required");
        Assert.notNull(name, "id is required");
        Assert.notNull(pattern, "pattern is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        MutableClassOrInterfaceTypeDetails controllerDetails = getControllerMutableTypeDetails(controllerClass);

        // TODO Refactor to check only this pattern in others controllers
        String patternDefinedTwice = patternService
                .findPatternDefinedMoreThanOnceInProject();
        Assert.isNull(patternDefinedTwice,
                "There is a pattern name used more than once in the project");

        // All checks passed OK.

        // Setup project for use annotation
        configService.setup();

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        PATTERN_ANNOTATION);

        // List of pattern to use
        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXPattern alredy exists

            // Loads previously registered pattern into patterList
            // Also checks if name is used previously
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(PATTERN_ANNOTATION_ATTR_VALUE_NAME);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();

                if (previousValues != null && !previousValues.isEmpty()) {
                    for (StringAttributeValue value : previousValues) {
                        // Check if name is already used
                        Assert.isTrue(!equalsPatternName(value, name),
                                "Pattern name already used in class");

                        patternList.add(value);
                    }
                }
            }
            isAlreadyAnnotated = true;
        }
        // Build string parameter for the pattern
        String patternParameter = name.toString().concat("=")
                .concat(pattern.toString());

        // Adds new pattern
        patternList.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                patternParameter));

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                PATTERN_ANNOTATION);
        annotationBuilder
                .addAttribute(new ArrayAttributeValue<StringAttributeValue>(
                        PATTERN_ANNOTATION_ATTR_VALUE_NAME, patternList));

        // Add or update annotation to target type
        if (isAlreadyAnnotated) {
            controllerDetails.updateTypeAnnotation(annotationBuilder.build(),
                    new HashSet<JavaSymbolName>());
        } else {
            controllerDetails.addTypeAnnotation(annotationBuilder.build());
        }

        if (pattern.equals(WebPattern.tabular)) {
            annotateFormBackingObject(controllerDetails);
        }
    }

    /**
     * <p>
     * Gets controller's mutableType physical detail
     * </p>
     * <p>
     * Also checks if <code>controllerClass</code> is really a controller
     * (annotated with @RooWebScaffold using {@link Assert})
     * </p>
     * 
     * TODO: refactor this code to use getPhysicalTypeDetails(JavaType type)
     * 
     * @param controllerClass
     * @return
     */
    private MutableClassOrInterfaceTypeDetails getControllerMutableTypeDetails(
            JavaType controllerClass) {
        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String controllerId = physicalTypeMetadataProvider
                .findIdentifier(controllerClass);
        if (controllerId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + controllerClass.getFullyQualifiedTypeName() + "'");
        }

        final String controllerFriendlyName = PhysicalTypeIdentifier
                .getFriendlyName(controllerId);
        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(controllerId);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type "
                        .concat(controllerFriendlyName));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type "
                        .concat(controllerFriendlyName));

        // Test if the type is an MutableClassOrInterfaceTypeDetails instance so
        // the annotation can be added
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(controllerFriendlyName));
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;

        // Test if has the @RooWebScaffold
        Assert.notNull(
                MemberFindingUtils.getAnnotationOfType(
                        mutableTypeDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION),
                controllerClass.getSimpleTypeName().concat(
                        " has not @RooWebScaffold annotation"));
        return mutableTypeDetails;
    }

    /**
     * For a given controller, this method inspect the OneToMany fields in its
     * formBackingObjet and, based on GvNIXRelationsPattern annotationValues,
     * annotates the controllers exposing these entities with the needed
     * GvNIXPattern annotation
     * 
     * @param controllerDetails
     * @param annotationValues
     */
    private void annotateFormBackingObjectRelationsControllers(
            MutableClassOrInterfaceTypeDetails controllerDetails,
            AnnotationAttributeValue<?> annotationValues) {

        JavaType formBakingObjectType = getFormBakingObject(controllerDetails);

        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String formBackingTypeId = EntityMetadata.createIdentifier(
                formBakingObjectType, Path.SRC_MAIN_JAVA);
        EntityMetadata formBackingTypeMetadata = (EntityMetadata) metadataService
                .get(formBackingTypeId, true);
        if (formBackingTypeMetadata == null) {
            throw new IllegalArgumentException("Cannot locate Metadata for '"
                    + formBakingObjectType.getFullyQualifiedTypeName() + "'");
        }

        List<FieldMetadata> fieldAnotations = patternService
                .getOneToManyFieldsFromEntityJavaType(formBakingObjectType);
        for (FieldMetadata field : fieldAnotations) {
            if (field.getFieldType().isCommonCollectionType()) {
                annotateEntityController(
                        field.getFieldType().getParameters().get(0),
                        getGvNIXRelatedPatternForEntityAndValues(
                                field.getFieldName(), annotationValues));
            }
        }

    }

    /**
     * For given parameters, entity and relationsPatternValues in the following
     * annotation value format:
     * <p>
     * <code>@GvNIXRelationsPattern({"pattern_id1: field1=table, field2=table", "pattern_id2: field2=table"})</code>
     * <p>
     * the method parse the value and creates an instance of
     * {@link GvNIXRelatedPattern} with the value of its part in the
     * GvNIXRelationsPattern value. That is, in the given example, for (field1,
     * {"pattern_id1: field1=table, field2=table",
     * "pattern_id2: field2=table"}), it returns:<br/>
     * GvNIXRelatedPattern({"pattern_id1=table"})
     * 
     * @param entity
     * @param relationsPatternValues
     * @return
     */
    private AnnotationMetadata getGvNIXRelatedPatternForEntityAndValues(
            JavaSymbolName fieldName,
            AnnotationAttributeValue<?> relationsPatternValues) {
        List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();
        if (relationsPatternValues != null) {

            @SuppressWarnings("unchecked")
            List<StringAttributeValue> relationsPatternList = (List<StringAttributeValue>) relationsPatternValues
                    .getValue();
            List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

            // Parse annotationValues finding the field interesting part
            if (relationsPatternList != null) {
                String[] patternDef = {};
                String[] fieldDefinitions = {};
                String[] fieldPatternType = {};
                for (StringAttributeValue strAttrValue : relationsPatternList) {
                    patternDef = strAttrValue.getValue().split(":");
                    fieldDefinitions = patternDef[1].trim().split(",");
                    for (String fieldDef : fieldDefinitions) {
                        fieldPatternType = fieldDef.trim().split("=");

                        if (fieldName.getSymbolName().equalsIgnoreCase(
                                fieldPatternType[0].trim())) {
                            patternList
                                    .add(new StringAttributeValue(
                                            new JavaSymbolName("ignored"),
                                            patternDef[0]
                                                    .trim()
                                                    .concat("=")
                                                    .concat(fieldPatternType[1]
                                                            .trim())));
                        }
                    }
                }
                attributes
                        .add(new ArrayAttributeValue<StringAttributeValue>(
                                RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME,
                                patternList));
            }
        }
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                RELATED_PATTERN_ANNOTATION);
        annotationBuilder.setAttributes(attributes);

        return annotationBuilder.build();
    }

    /**
     * Given a Entity this method look for the WebScaffold exposing it and adds
     * {@link GvNIXRelatedPattern} in parameter annotation
     * 
     * @param entity
     * @param annotation
     */
    private void annotateEntityController(JavaType entity,
            AnnotationMetadata annotation) {
        // MutableClassOrInterfaceTypeDetails mcitd =
        // getPhysicalTypeDetails(entity);
        AnnotationMetadata annotationMetadata = null;
        for (ClassOrInterfaceTypeDetails cid : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROOWEBSCAFFOLD_ANNOTATION)) {
            annotationMetadata = MemberFindingUtils.getAnnotationOfType(
                    cid.getAnnotations(), ROOWEBSCAFFOLD_ANNOTATION);
            if (annotationMetadata != null) {
                AnnotationAttributeValue<?> annotationValues = annotationMetadata
                        .getAttribute(new JavaSymbolName("formBackingObject"));
                if (annotationValues != null) {
                    if (annotationValues.getName().compareTo(
                            new JavaSymbolName("formBackingObject")) == 0
                            && ((JavaType) annotationValues.getValue())
                                    .getFullyQualifiedTypeName()
                                    .equalsIgnoreCase(
                                            entity.getFullyQualifiedTypeName())) {
                        addOrUpdateGvNIXRelatedPatternToController(
                                cid.getName(), annotation);
                    }
                }
            }
        }
    }

    /**
     * Annotates with GvNIXRelatedPattern (or updates the annotation value) the
     * given controller
     * 
     * @param controllerClass
     * @param annotation
     *            Instace of GvNIXPattern annotation metadata
     */
    private void addOrUpdateGvNIXRelatedPatternToController(
            JavaType controllerClass, AnnotationMetadata annotation) {

        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String controllerId = physicalTypeMetadataProvider
                .findIdentifier(controllerClass);
        if (controllerId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + controllerClass.getFullyQualifiedTypeName() + "'");
        }

        final String controllerFriendlyName = PhysicalTypeIdentifier
                .getFriendlyName(controllerId);
        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(controllerId);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type "
                        .concat(controllerFriendlyName));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type "
                        .concat(controllerFriendlyName));

        // Test if the type is an MutableClassOrInterfaceTypeDetails instance so
        // the annotation can be added
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(controllerFriendlyName));
        MutableClassOrInterfaceTypeDetails controllerDetails = (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;

        // Test if has the @RooWebScaffold
        Assert.notNull(
                MemberFindingUtils.getAnnotationOfType(
                        controllerDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION),
                controllerClass.getSimpleTypeName().concat(
                        " has not @RooWebScaffold annotation"));

        // All checks passed OK.
        // We don't need to do Setup because it must be done by addPattern call

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        RELATED_PATTERN_ANNOTATION);

        // List of pattern to use
        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXPattern alredy exists

            // Loads previously registered pattern into patterList
            // Also checks if name is used previously
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();
                patternList.addAll(previousValues);
            }
            isAlreadyAnnotated = true;
        }

        // We're sure that annotation has set a value since we created this
        // instance
        @SuppressWarnings("unchecked")
        List<StringAttributeValue> newValues = (List<StringAttributeValue>) annotation
                .getAttribute(RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME)
                .getValue();
        patternList.addAll(newValues);

        AnnotationAttributeValue<?> gvNIXRelatedPatternValue = new ArrayAttributeValue<StringAttributeValue>(
                RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME, patternList);
        // Check if the controller has defined the same pattern more
        // than once
        // TODO: next check must be over if pattern id is already defined in
        // project
        // Assert.isTrue(
        // arePatternsDefinedOnceInController(gvNIXRelatedPatternValue),
        // "Controller ".concat(
        // controllerClass.getFullyQualifiedTypeName()).concat(
        // " has the same pattern defined more than once"));

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                RELATED_PATTERN_ANNOTATION);
        annotationBuilder.addAttribute(gvNIXRelatedPatternValue);

        // Add or update annotation to target type
        if (isAlreadyAnnotated) {
            controllerDetails.updateTypeAnnotation(annotationBuilder.build(),
                    new HashSet<JavaSymbolName>());
        } else {
            controllerDetails.addTypeAnnotation(annotationBuilder.build());
        }

        List<String> definedPatternsList = new ArrayList<String>();
        for (StringAttributeValue definedPattern : patternList) {
            definedPatternsList.add(definedPattern.getValue());
        }
        if (isPatternTypeDefined(WebPattern.tabular, definedPatternsList)) {
            annotateFormBackingObject(controllerDetails);
        }
    }

    /**
     * If there is a pattern of givne WebPattern defined in GvNIXPattern it
     * returns true, false otherwise
     * 
     * @param patternType
     * @param definedPatternsList
     * @return
     */
    private boolean isPatternTypeDefined(WebPattern patternType,
            List<String> definedPatternsList) {
        for (String definedPattern : definedPatternsList) {
            if (definedPattern.split("=")[1].equalsIgnoreCase(patternType
                    .name())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean arePatternsDefinedOnceInController(
            AnnotationAttributeValue<?> values) {
        List<String> auxList = new ArrayList<String>();
        for (String value : getPatternNames(values)) {
            if (auxList.contains(value)) {
                return false;
            } else {
                auxList.add(value);
            }
        }
        return true;

    }

    private List<String> getPatternNames(AnnotationAttributeValue<?> values) {
        List<String> patternNames = new ArrayList<String>();
        if (values != null) {
            @SuppressWarnings("unchecked")
            List<StringAttributeValue> attrValues = (List<StringAttributeValue>) values
                    .getValue();

            if (attrValues != null) {
                String[] pattern = {};
                for (StringAttributeValue strAttrValue : attrValues) {
                    pattern = strAttrValue.getValue().split("=");
                    patternNames.add(pattern[0]);
                }
            }
        }
        return patternNames;
    }

    /**
     * Returns an instace of MutableClassOrInterfaceTypeDetails of the type
     * given
     * 
     * @param type
     * @return
     */
    private MutableClassOrInterfaceTypeDetails getPhysicalTypeDetails(
            JavaType type) {
        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String entityId = physicalTypeMetadataProvider.findIdentifier(type);
        if (entityId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + type.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(entityId, true);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type ".concat(type
                        .getFullyQualifiedTypeName()));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type ".concat(type
                        .getFullyQualifiedTypeName()));
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(type.getFullyQualifiedTypeName()));
        return (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;
    }

    /**
     * Annotates with GvNIXEntityBatch the JavaType defined by formBackingObject
     * in controller web scaffold
     * 
     * @param controllerDetails
     */
    private void annotateFormBackingObject(
            MutableClassOrInterfaceTypeDetails controllerDetails) {
        JavaType formBackingObject = getFormBakingObject(controllerDetails);
        annotateTypeWithGvNIXEntityBatch(formBackingObject);
    }

    private void annotateTypeWithGvNIXEntityBatch(JavaType type) {
        MutableClassOrInterfaceTypeDetails typeMutableDetails = getPhysicalTypeDetails(type);
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(typeMutableDetails.getAnnotations(),
                        ENTITYBATCH_ANNOTATION);

        // Annotate type with GvNIXEntityBatch just if is not
        // annotated already. We don't need to update attributes
        if (annotationMetadata == null) {
            // Prepare annotation builder
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    ENTITYBATCH_ANNOTATION);
            typeMutableDetails.addTypeAnnotation(annotationBuilder.build());
        }
    }

    private JavaType getFormBakingObject(
            MutableClassOrInterfaceTypeDetails controllerDetails) {
        AnnotationMetadata rooWebScaffoldAnnotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION);

        AnnotationAttributeValue<?> formbakingObjectAttValue = rooWebScaffoldAnnotationMetadata
                .getAttribute(new JavaSymbolName("formBackingObject"));

        JavaType formBakingObjectType = (JavaType) formbakingObjectAttValue
                .getValue();
        Assert.notNull(formBakingObjectType,
                "formBakingObject attribute of RooWebScaffold in "
                        + controllerDetails.getName().getSimpleTypeName()
                        + " must be set");

        return formBakingObjectType;
    }

    /**
     * Checks if pattern annotation value element uses the very same identifier
     * than <code>name</code>
     * 
     * @param value
     *            pattern annotation value item
     * @param name
     *            identifier to compare
     * @return
     */
    private boolean equalsPatternName(StringAttributeValue value,
            JavaSymbolName name) {
        String current = value.getValue().replace(" ", "");

        return current.startsWith(name.getSymbolName().concat("="));
    }

    /** {@inheritDoc} */
    public void addRelationPattern(JavaType controllerClass,
            JavaSymbolName name, JavaSymbolName field, WebPattern type) {

        Assert.notNull(controllerClass, "controller is required");
        Assert.notNull(name, "name is required");
        Assert.notNull(field, "field is required");
        Assert.notNull(type, "type is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = getControllerMutableTypeDetails(controllerClass);

        // Get @GvNIXPattern annotation from controller
        AnnotationMetadata patternAnnotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        PATTERN_ANNOTATION);
        Assert.notNull(
                patternAnnotationMetadata,
                "Missing ".concat(PATTERN_ANNOTATION.getSimpleTypeName())
                        .concat(" annotation in controller "
                                .concat(controllerClass
                                        .getFullyQualifiedTypeName())));

        // look for pattern name in @GvNIXPattern values
        AnnotationAttributeValue<?> patternAnnotationValues = patternAnnotationMetadata
                .getAttribute(PATTERN_ANNOTATION_ATTR_VALUE_NAME);
        Assert.notNull(
                patternAnnotationValues,
                "Missing values in ".concat(
                        PATTERN_ANNOTATION.getSimpleTypeName()).concat(
                        " annotation in controller ".concat(controllerClass
                                .getFullyQualifiedTypeName())));

        @SuppressWarnings("unchecked")
        List<StringAttributeValue> patternValues = (List<StringAttributeValue>) patternAnnotationValues
                .getValue();
        Assert.isTrue(
                patternValues != null && !patternValues.isEmpty(),
                "Missing values in ".concat(
                        PATTERN_ANNOTATION.getSimpleTypeName()).concat(
                        " annotation in controller ".concat(controllerClass
                                .getFullyQualifiedTypeName())));

        boolean foundPattern = false;
        for (StringAttributeValue value : patternValues) {
            // Check if name is already used
            if (equalsPatternName(value, name)) {
                foundPattern = true;
                break;
            }
        }
        Assert.isTrue(
                foundPattern,
                "Pattern name '".concat(name.getSymbolName())
                        .concat("' not found in values of ")
                        .concat(PATTERN_ANNOTATION.getSimpleTypeName())
                        .concat(" annotation in controller ")
                        .concat(controllerClass.getFullyQualifiedTypeName()));

        // Get @RooController annotation to get formbackingObject definition
        AnnotationMetadata controllerAnnotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION);

        // Check if filed exists in formBackingObject entity and is OneToMany
        JavaType formBackingObject = (JavaType) controllerAnnotationMetadata
                .getAttribute(
                        ROOWEBSCAFFOLD_ANNOTATION_ATTR_VALUE_FORMBACKINGOBJECT)
                .getValue();
        Assert.notNull(
                patternService.getOneToManyFieldFromEntityJavaType(
                        formBackingObject, field.getSymbolName()),
                "Field '".concat(field.getSymbolName())
                        .concat("' not found or is not OneToMany in '")
                        .concat(formBackingObject.getFullyQualifiedTypeName())
                        .concat("' entity."));

        // All checks passed OK.

        // We don't need to do Setup because it must be done by addPattern call

        // List of pattern to use in annotation
        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

        // Prepare pattern prefix and previous value
        String patternPrefix = name.getSymbolName().concat(":");
        String patternFieldValue = field.getSymbolName().concat("=")
                .concat(type.name());
        String finalValue = patternPrefix.concat(" ").concat(patternFieldValue);
        int previousValueIndex = -1;

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        RELATION_PATTERN_ANNOTATION);

        // Check if @GvNIXRelationPattern
        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXRelationPattern already exists

            // Loads previously registered values into patterList
            // Also identifies previous value of target pattern
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(RELATION_PATTERN_ANNOTATION_ATTR_VALUE_NAME);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();

                if (previousValues != null && !previousValues.isEmpty()) {
                    String strValue;
                    int tmpIndex = 0;
                    for (StringAttributeValue value : previousValues) {
                        strValue = value.getValue();
                        if (strValue.trim().startsWith(patternPrefix)) {
                            // Found previous

                            // Check for duplicates pattern definition
                            Assert.isTrue(
                                    previousValueIndex < 0,
                                    "Duplicate definition for pattern '"
                                            .concat(name.getSymbolName())
                                            .concat("' in ")
                                            .concat(RELATION_PATTERN_ANNOTATION
                                                    .getSimpleTypeName())
                                            .concat(" annotation in ")
                                            .concat(controllerClass
                                                    .getFullyQualifiedTypeName()));

                            // Check if already exist field declaration for
                            // pattern name
                            boolean existsField = existsFieldPatternDeclaration(
                                    strValue, field);
                            Assert.isTrue(
                                    !existsField,
                                    "Field '"
                                            .concat(field.getSymbolName())
                                            .concat("' is already defined for pattern '")
                                            .concat(name.getSymbolName())
                                            .concat("' in controller ")
                                            .concat(controllerClass
                                                    .getFullyQualifiedTypeName()));
                            // Prepare final value
                            finalValue = value.getValue().concat(", ")
                                    .concat(patternFieldValue);
                            // skip from pattern list but store position
                            // to prevent unnecessary changes in file
                            previousValueIndex = tmpIndex;

                        } else {
                            patternList.add(value);
                        }
                        tmpIndex++;
                    }
                }
            }
            isAlreadyAnnotated = true;
        }

        // Adds final pattern value
        if (previousValueIndex >= 0) {
            // Restore previous value position if any
            patternList.add(previousValueIndex, new StringAttributeValue(
                    new JavaSymbolName("ignored"), finalValue));
        } else {
            patternList.add(new StringAttributeValue(new JavaSymbolName(
                    "ignored"), finalValue));
        }

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                RELATION_PATTERN_ANNOTATION);
        AnnotationAttributeValue<?> annotationValues = new ArrayAttributeValue<StringAttributeValue>(
                RELATION_PATTERN_ANNOTATION_ATTR_VALUE_NAME, patternList);
        annotationBuilder.addAttribute(annotationValues);

        // Add or update annotation to target type
        if (isAlreadyAnnotated) {
            mutableTypeDetails.updateTypeAnnotation(annotationBuilder.build(),
                    new HashSet<JavaSymbolName>());
        } else {
            mutableTypeDetails.addTypeAnnotation(annotationBuilder.build());
        }

        // TODO Synchronize it with oscar's change
        // if (type.equals(WebPattern.tabular)) {
        annotateFormBackingObjectRelationsControllers(mutableTypeDetails,
                annotationValues);
        // annotateFormBackingObject(mutableTypeDetails);
        // }
    }

    private boolean existsFieldPatternDeclaration(String definition,
            JavaSymbolName field) {
        char[] fieldName = field.getSymbolName().toCharArray();
        StringBuilder sb = new StringBuilder((fieldName.length * 3) + 15);
        sb.append("[,: ]");
        for (char ch : fieldName) {
            sb.append('[');
            sb.append(ch);
            sb.append(']');
        }
        sb.append("[ ]*[=]");
        Pattern pattern = Pattern.compile(sb.toString());
        return pattern.matcher(definition).find();
    }
}
