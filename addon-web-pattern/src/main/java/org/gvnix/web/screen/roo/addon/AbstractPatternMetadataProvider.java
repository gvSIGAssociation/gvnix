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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link PatternMetadata}. This type is called by Roo to retrieve the
 * metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public abstract class AbstractPatternMetadataProvider extends
        AbstractMemberDiscoveringItdMetadataProvider {

    /**
     * {@link RooWebScaffold} JavaType
     */
    protected static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    /**
     * {@link GvNIXEntityBatch} JavaType
     */
    protected static final JavaType ENTITYBATCH_ANNOTATION = new JavaType(
            GvNIXEntityBatch.class.getName());

    /**
     * {@link GvNIXPattern} JavaType
     */
    protected static final JavaType RELATIONS_PATTERN_ANNOTATION = new JavaType(
            GvNIXRelationsPattern.class.getName());
    /**
     * Name of {@link GvNIXPattern} attribute value
     */
    protected static final JavaSymbolName RELATIONS_PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName(
            "value");

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected abstract void activate(ComponentContext context);

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected abstract void deactivate(ComponentContext context);

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected abstract ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename);

    protected boolean arePatternsDefinedOnceInController(
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

    protected List<String> getPatternNames(AnnotationAttributeValue<?> values) {
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
     * Read the values of GvNIXRelationsPattern and for the fields defined as
     * visible relationships retrieve its JavaTypeMetadataDetails
     * 
     * @param metadataIdentificationString
     * @param controllerPhysicalTypeMetadata
     * @param formBackingType
     * @param webMetadataService
     * @return
     */
    protected SortedMap<JavaType, JavaTypeMetadataDetails> getTypesForPopulate(
            String metadataIdentificationString,
            PhysicalTypeMetadata controllerPhysicalTypeMetadata,
            JavaType formBackingType, WebMetadataService webMetadataService) {

        Set<String> fields = getDefinedRelations(metadataIdentificationString,
                controllerPhysicalTypeMetadata, formBackingType,
                webMetadataService);

        List<FieldMetadata> eligibleFields = getEligibleFieldsFromJavatType(
                formBackingType, metadataIdentificationString,
                webMetadataService);

        // We're interested only in those types defined in GvNIXRelationsPattern
        // values
        SortedMap<JavaType, JavaTypeMetadataDetails> interestingFieldsTypeMetadata = new TreeMap<JavaType, JavaTypeMetadataDetails>();
        JavaType interestingFieldType = null;
        for (FieldMetadata fieldMetadata : eligibleFields) {
            if (fields.contains(fieldMetadata.getFieldName().getSymbolName())) {
                interestingFieldType = fieldMetadata.getFieldType();
                if (interestingFieldType.isCommonCollectionType()
                        && !interestingFieldType.getParameters().isEmpty()) {
                    interestingFieldType = interestingFieldType.getParameters()
                            .get(0);
                }
                interestingFieldsTypeMetadata.putAll(getRelatedAppTypeMetadata(
                        interestingFieldType, metadataIdentificationString,
                        webMetadataService));
            }
        }
        return interestingFieldsTypeMetadata;
    }

    /**
     * For the given <code>type</code> the method returns a Map with its Related
     * App. Types Metadata
     * 
     * @param type
     * @param metadataIdentificationString
     * @param webMetadataService
     * @return
     */
    private SortedMap<JavaType, JavaTypeMetadataDetails> getRelatedAppTypeMetadata(
            JavaType type, String metadataIdentificationString,
            WebMetadataService webMetadataService) {
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(PhysicalTypeIdentifier.createIdentifier(type,
                        Path.SRC_MAIN_JAVA));
        Assert.notNull(
                physicalTypeMetadata,
                "Unable to obtain physical type metadata for type "
                        + type.getFullyQualifiedTypeName());

        MemberDetails typeMd = getMemberDetails(physicalTypeMetadata);

        MemberHoldingTypeDetails memberHoldingTypeDetails = MemberFindingUtils
                .getMostConcreteMemberHoldingTypeDetailsWithTag(typeMd,
                        PersistenceCustomDataKeys.PERSISTENT_TYPE);
        SortedMap<JavaType, JavaTypeMetadataDetails> relatedApplicationTypeMetadata = webMetadataService
                .getRelatedApplicationTypeMetadata(type, typeMd,
                        metadataIdentificationString);
        if (memberHoldingTypeDetails == null
                || relatedApplicationTypeMetadata == null) {
            return null;
        }

        return relatedApplicationTypeMetadata;
    }

    /**
     * Returns a map with Related entities JavaType as key, and their Map of the
     * Fields-DateTimeFormatDetails as value.
     * <p>
     * We need this data in order to register the right DateTimeFormatPattern of
     * the related entities in a master/detail pattern.
     * 
     * @param metadataIdentificationString
     * @param controllerPhysicalTypeMetadata
     * @param formBackingType
     * @param webMetadataService
     * @return
     */
    protected Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> getRelationsDateTypePatterns(
            String metadataIdentificationString,
            PhysicalTypeMetadata controllerPhysicalTypeMetadata,
            JavaType formBackingType, WebMetadataService webMetadataService) {

        Set<String> fields = getDefinedRelations(metadataIdentificationString,
                controllerPhysicalTypeMetadata, formBackingType,
                webMetadataService);

        List<FieldMetadata> eligibleFields = getEligibleFieldsFromJavatType(
                formBackingType, metadataIdentificationString,
                webMetadataService);

        // We're interested only in those types defined in GvNIXRelationsPattern
        // values
        Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedTypesDatePatterns = new LinkedHashMap<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>>();
        JavaType interestingFieldType = null;
        for (FieldMetadata fieldMetadata : eligibleFields) {
            if (fields.contains(fieldMetadata.getFieldName().getSymbolName())) {
                interestingFieldType = fieldMetadata.getFieldType();
                if (interestingFieldType.isCommonCollectionType()
                        && !interestingFieldType.getParameters().isEmpty()) {
                    interestingFieldType = interestingFieldType.getParameters()
                            .get(0);
                }
                relatedTypesDatePatterns
                        .put(interestingFieldType,
                                webMetadataService
                                        .getDatePatterns(
                                                interestingFieldType,
                                                webMetadataService
                                                        .getMemberDetails(interestingFieldType),
                                                metadataIdentificationString));
            }
        }

        return relatedTypesDatePatterns;
    }

    /**
     * For a given controller and formBackingObject, returns a Set with the
     * defined related field names. It parses the value of the
     * GvNIXRelationsPattern annotation.
     * 
     * @param metadataIdentificationString
     * @param controllerPhysicalTypeMetadata
     * @param formBackingType
     * @param webMetadataService
     * @return
     */
    private Set<String> getDefinedRelations(
            String metadataIdentificationString,
            PhysicalTypeMetadata controllerPhysicalTypeMetadata,
            JavaType formBackingType, WebMetadataService webMetadataService) {
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                controllerPhysicalTypeMetadata);
        if (!annotationValues.isAnnotationFound()
                || annotationValues.getFormBackingObject() == null
                || controllerPhysicalTypeMetadata.getMemberHoldingTypeDetails() == null) {
            return null;
        }

        ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) controllerPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(
                cid,
                "Governor failed to provide class type details, in violation of superclass contract");

        // Retrieve the fields defined as visible relationships in
        // GvNIXRelationsPattern
        AnnotationMetadata gvNixRelationsPatternAnnotation = MemberFindingUtils
                .getAnnotationOfType(cid.getAnnotations(),
                        RELATIONS_PATTERN_ANNOTATION);
        Set<String> fields = new HashSet<String>();
        if (gvNixRelationsPatternAnnotation != null) {
            AnnotationAttributeValue<?> relationsPatternValues = gvNixRelationsPatternAnnotation
                    .getAttribute(RELATIONS_PATTERN_ANNOTATION_ATTR_VALUE_NAME);

            if (relationsPatternValues != null) {
                @SuppressWarnings("unchecked")
                List<StringAttributeValue> relationsPatternList = (List<StringAttributeValue>) relationsPatternValues
                        .getValue();

                String[] patternDef = {};
                String[] fieldDefinitions = {};
                String[] fieldPatternType = {};
                for (StringAttributeValue strAttrValue : relationsPatternList) {
                    patternDef = strAttrValue.getValue().split(":");
                    fieldDefinitions = patternDef[1].trim().split(",");
                    for (String fieldDef : fieldDefinitions) {
                        fieldPatternType = fieldDef.trim().split("=");
                        fields.add(fieldPatternType[0]);
                    }
                }
            }
        }

        return fields;
    }

    private List<FieldMetadata> getEligibleFieldsFromJavatType(
            JavaType javaType, String metadataIdentificationString,
            WebMetadataService webMetadataService) {
        MemberDetails javaTypeMemberDetails = webMetadataService
                .getMemberDetails(javaType);

        return webMetadataService.getScaffoldEligibleFieldMetadata(javaType,
                javaTypeMemberDetails, metadataIdentificationString);
    }

    /**
     * Define the unique ITD file name extension
     */
    public abstract String getItdUniquenessFilenameSuffix();

    @Override
    protected abstract String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString);

    @Override
    protected abstract String createLocalIdentifier(JavaType javaType, Path path);

    public abstract String getProvidesType();
}
