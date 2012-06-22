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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadataProvider;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link RelatedPatternMetadata}. This type is called by Roo to
 * retrieve the metadata for this add-on. Use this type to reference external
 * types and services needed by the metadata type. Register metadata triggers
 * and dependencies here. Also define the unique add-on ITD identifier.
 * 
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component(immediate = true)
@Service
public final class RelatedPatternMetadataProvider extends
        AbstractPatternMetadataProvider {

    /** {@link GvNIXRelatedPattern} JavaType */
    public static final JavaType RELATED_PATTERN_ANNOTATION = new JavaType(GvNIXRelatedPattern.class.getName());
    
    /** Name of {@link GvNIXRelatedPattern} attribute value */
    public static final JavaSymbolName RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName("value");

    @Reference WebScaffoldMetadataProvider webScaffoldMetadataProvider;
    @Reference ProjectOperations projectOperations;
    @Reference PropFileOperations propFileOperations;
    @Reference WebMetadataService webMetadataService;
    @Reference PatternService patternService;

    private final Map<JavaType, String> entityToWebScaffoldMidMap = new LinkedHashMap<JavaType, String>();
    private final Map<String, JavaType> webScaffoldMidToEntityMap = new LinkedHashMap<String, JavaType>();

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    @Override
    protected void activate(ComponentContext context) {
        // next line adding a notification listener over this class allow method
        // getLocalMidToRequest(ItdTypeDetails) to be invoked
        metadataDependencyRegistry.addNotificationListener(this);
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(RELATED_PATTERN_ANNOTATION);
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    @Override
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.removeNotificationListener(this);
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(RELATED_PATTERN_ANNOTATION);
    }

    @Override
    protected String getLocalMidToRequest(ItdTypeDetails itdTypeDetails) {
        // Determine the governor for this ITD, and whether any metadata is even
        // hoping to hear about changes to that JavaType and its ITDs
        JavaType governor = itdTypeDetails.getName();
        String localMid = entityToWebScaffoldMidMap.get(governor);
        return localMid == null ? null : localMid;
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // We need to parse the annotation, which we expect to be present
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                governorPhysicalTypeMetadata);
        if (!annotationValues.isAnnotationFound()
                || annotationValues.getFormBackingObject() == null
                || governorPhysicalTypeMetadata.getMemberHoldingTypeDetails() == null) {
            return null;
        }

        // Setup project for use annotation
        // configService.setup();

        JavaType controllerType = RelatedPatternMetadata
                .getJavaType(metadataIdentificationString);

        // We need to know the metadata of the Controller through
        // WebScaffoldMetada
        Path path = RelatedPatternMetadata
                .getPath(metadataIdentificationString);
        String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
                controllerType, path);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataKey);
        if (webScaffoldMetadata == null) {
        	
            // The pattern can not be defined over a Controller without @RooWebScaffold annotation 
            return null;
        }

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(
                cid,
                "Governor failed to provide class type details, in violation of superclass contract");

        // Check if the controller has defined the same pattern more than once
        Assert.isTrue(!patternService.isPatternDuplicated(null), "There is a pattern name used more than once in the project");

        List<StringAttributeValue> patternList = patternService.getPatternAttributes(controllerType);

        // Lookup the form backing object's metadata and check that
        JavaType formBackingType = annotationValues.getFormBackingObject();

        PhysicalTypeMetadata formBackingObjectPhysicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(PhysicalTypeIdentifier.createIdentifier(formBackingType,
                        Path.SRC_MAIN_JAVA));
        Assert.notNull(formBackingObjectPhysicalTypeMetadata,
                "Unable to obtain physical type metadata for type "
                        + formBackingType.getFullyQualifiedTypeName());
        MemberDetails formBackingObjectMemberDetails = getMemberDetails(formBackingObjectPhysicalTypeMetadata);

        MemberHoldingTypeDetails memberHoldingTypeDetails = MemberFindingUtils
                .getMostConcreteMemberHoldingTypeDetailsWithTag(
                        formBackingObjectMemberDetails,
                        PersistenceCustomDataKeys.PERSISTENT_TYPE);
        SortedMap<JavaType, JavaTypeMetadataDetails> relatedApplicationTypeMetadata = webMetadataService
                .getRelatedApplicationTypeMetadata(formBackingType,
                        formBackingObjectMemberDetails,
                        metadataIdentificationString);
        if (memberHoldingTypeDetails == null
                || relatedApplicationTypeMetadata == null
                || relatedApplicationTypeMetadata.get(formBackingType) == null
                || relatedApplicationTypeMetadata.get(formBackingType)
                        .getPersistenceDetails() == null) {
            return null;
        }
        // Remember that this entity JavaType matches up with this metadata
        // identification string
        // Start by clearing the previous association
        // Working in the same way as WebScaffoldMetadataProvider
        JavaType oldEntity = webScaffoldMidToEntityMap
                .get(metadataIdentificationString);
        if (oldEntity != null) {
            entityToWebScaffoldMidMap.remove(oldEntity);
        }
        entityToWebScaffoldMidMap.put(formBackingType,
                metadataIdentificationString);
        webScaffoldMidToEntityMap.put(metadataIdentificationString,
                formBackingType);

        MemberDetails memberDetails = getMemberDetails(governorPhysicalTypeMetadata);

        Map<JavaSymbolName, DateTimeFormatDetails> dateTypes = webMetadataService
                .getDatePatterns(formBackingType,
                        formBackingObjectMemberDetails,
                        metadataIdentificationString);

        // Pass dependencies required by the metadata in through its constructor
        return new RelatedPatternMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, webScaffoldMetadata,
                annotationValues, patternList,
                MemberFindingUtils.getMethods(memberDetails),
                MemberFindingUtils.getFields(memberDetails),
                relatedApplicationTypeMetadata, getRelationFieldsDetails(
                        metadataIdentificationString,
                        governorPhysicalTypeMetadata, formBackingType,
                        webMetadataService), getRelationFieldsDateFormat(
                        metadataIdentificationString,
                        governorPhysicalTypeMetadata, formBackingType,
                        webMetadataService), metadataService,
                propFileOperations, projectOperations.getPathResolver(),
                fileManager, dateTypes);
    }

    /**
     * {@inheritDoc}, here the resulting file name will be
     * **_ROO_GvNIXRelatedPattern.aj
     */
    @Override
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXRelatedPattern";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = RelatedPatternMetadata
                .getJavaType(metadataIdentificationString);
        Path path = RelatedPatternMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return RelatedPatternMetadata.createIdentifier(javaType, path);
    }

    @Override
    public String getProvidesType() {
        return RelatedPatternMetadata.getMetadataIdentiferType();
    }
}
