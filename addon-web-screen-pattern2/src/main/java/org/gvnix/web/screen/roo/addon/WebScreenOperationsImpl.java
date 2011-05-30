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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
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
     * {@link RooWebScaffold} JavaType
     */
    public static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    /**
     * {@link GvNIXEntityBatch} JavaType
     */
    public static final JavaType ENTITYBATCH_ANNOTATION = new JavaType(
            GvNIXEntityBatch.class.getName());

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference
    private MetadataService metadataService;

    /**
     * Use the PhysicalTypeMetadataProvider to access information about a
     * physical type in the project
     */
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

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
                        " doesn't has @RooWebScaffold annotation"));

        // All checks passed OK.

        // Setup project for use annotation
        configService.setup();

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
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
            mutableTypeDetails.updateTypeAnnotation(annotationBuilder.build(),
                    new HashSet<JavaSymbolName>());
        } else {
            mutableTypeDetails.addTypeAnnotation(annotationBuilder.build());
        }

        annotateFormBackingObject(mutableTypeDetails);

    }

    private void annotateFormBackingObject(
            MutableClassOrInterfaceTypeDetails mutableTypeDetails) {
        AnnotationMetadata rooWebScaffoldAnnotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION);

        AnnotationAttributeValue<?> formbakingObjectAttValue = rooWebScaffoldAnnotationMetadata
                .getAttribute(new JavaSymbolName("formBackingObject"));

        JavaType formBakingObjectType = (JavaType) formbakingObjectAttValue
                .getValue();
        Assert.notNull(formBakingObjectType,
                "formBakingObject attribute of RooWebScaffold in "
                        + mutableTypeDetails.getName().getSimpleTypeName()
                        + " must be set");

        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String formBackingTypeId = physicalTypeMetadataProvider
                .findIdentifier(formBakingObjectType);
        if (formBackingTypeId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + formBakingObjectType.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(formBackingTypeId, true);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type "
                        .concat(formBakingObjectType
                                .getFullyQualifiedTypeName()));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type "
                        .concat(formBakingObjectType
                                .getFullyQualifiedTypeName()));

        // Test if the type is an MutableClassOrInterfaceTypeDetails instance so
        // the annotation can be added
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(formBakingObjectType
                                .getFullyQualifiedTypeName()));

        // Test if the annotation already exists on the target type
        MutableClassOrInterfaceTypeDetails formBakingObjectMutableTypeDetails = (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(
                        formBakingObjectMutableTypeDetails.getAnnotations(),
                        ENTITYBATCH_ANNOTATION);

        // Annotate formBackingType with GvNIXEntityBatch just if is not
        // annotated already. We don't need to update attributes
        if (annotationMetadata == null) {
            // Prepare annotation builder
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    ENTITYBATCH_ANNOTATION);
            formBakingObjectMutableTypeDetails
                    .addTypeAnnotation(annotationBuilder.build());
        }
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

}
