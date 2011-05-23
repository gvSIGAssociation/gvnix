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
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link PatternMetadata}. This type is called by Roo to retrieve the
 * metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public final class PatternMetadataProvider extends AbstractItdMetadataProvider {
    private static final Logger logger = HandlerUtils
            .getLogger(PatternMetadataProvider.class);

    /**
     * {@link GvNIXPattern} JavaType
     */
    public static final JavaType PATTERN_ANNOTATION = new JavaType(
            GvNIXPattern.class.getName());
    /**
     * Name of {@link GvNIXPattern} attribute value
     */
    public static final JavaSymbolName PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName(
            "value");

    /**
     * {@link RooWebScaffold} JavaType
     */
    public static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    ProjectOperations projectOperations;

    @Reference
    PropFileOperations propFileOperations;

    @Reference
    WebMetadataService webMetadataService;

    @Reference
    private WebScreenConfigService configService;

    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXPattern.class.getName()));
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
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXPattern.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Setup project for use annotation
        configService.setup();

        // We need to parse the annotation, which we expect to be present
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                governorPhysicalTypeMetadata);
        if (!annotationValues.isAnnotationFound()
                || annotationValues.getFormBackingObject() == null
                || governorPhysicalTypeMetadata.getMemberHoldingTypeDetails() == null) {
            return null;
        }

        JavaType controllerType = PatternMetadata
                .getJavaType(metadataIdentificationString);

        // We need to know the metadata of the Controller through
        // WebScaffoldMetada
        Path path = PatternMetadata.getPath(metadataIdentificationString);
        String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
                controllerType, path);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataKey);
        if (webScaffoldMetadata == null) {
            /*
             * logger.warning(
             * "The pattern can not be defined over a Controlloer without " +
             * "@RooWebScaffold annotation and its 'fromBackingObject' attribute "
             * + "set. Check " + controllerType.getFullyQualifiedTypeName());
             */
            return null;
        }

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(
                cid,
                "Governor failed to provide class type details, in violation of superclass contract");

        AnnotationMetadata gvNixPatternAnnotation = MemberFindingUtils
                .getAnnotationOfType(cid.getAnnotations(), PATTERN_ANNOTATION);

        // Check if there are pattern names used more than once in project
        String patternDefinedTwice = findPatternDefinedMoreThanOnceInProject();
        Assert.isNull(patternDefinedTwice,
                "There is a pattern name used more than once in the project");

        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

        if (gvNixPatternAnnotation != null) {
            AnnotationAttributeValue<?> thisAnnotationValue = gvNixPatternAnnotation
                    .getAttribute(PATTERN_ANNOTATION_ATTR_VALUE_NAME);

            if (thisAnnotationValue != null) {
                // Check if the controller has defined the same pattern more
                // than once
                Assert.isTrue(
                        arePatternsDefinedOnceInController(thisAnnotationValue),
                        "Controller "
                                .concat(cid.getName()
                                        .getFullyQualifiedTypeName())
                                .concat(" has the same pattern defined more than once"));

                ArrayAttributeValue<?> arrayVal = (ArrayAttributeValue<?>) thisAnnotationValue;

                if (arrayVal != null) {
                    @SuppressWarnings("unchecked")
                    List<StringAttributeValue> values = (List<StringAttributeValue>) arrayVal
                            .getValue();
                    for (StringAttributeValue value : values) {
                        patternList.add(value);
                    }
                }
            }
        }

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
        if (memberHoldingTypeDetails == null) {
            return null;
        }

        MemberDetails memberDetails = getMemberDetails(governorPhysicalTypeMetadata);

        // Pass dependencies required by the metadata in through its constructor
        return new PatternMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, annotationValues, memberDetails,
                patternList, physicalTypeMetadataProvider, metadataService);
    }

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

    private String findPatternDefinedMoreThanOnceInProject() {
        List<String> definedPatternsInProject = new ArrayList<String>();
        AnnotationMetadata annotationMetadata = null;
        for (ClassOrInterfaceTypeDetails cid : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(PATTERN_ANNOTATION)) {
            annotationMetadata = MemberFindingUtils.getAnnotationOfType(
                    cid.getAnnotations(), PATTERN_ANNOTATION);
            if (annotationMetadata != null) {
                AnnotationAttributeValue<?> annotationValues = annotationMetadata
                        .getAttribute(PATTERN_ANNOTATION_ATTR_VALUE_NAME);
                for (String patternName : getPatternNames(annotationValues)) {
                    if (definedPatternsInProject.contains(patternName)) {
                        return patternName;
                    } else {
                        definedPatternsInProject.add(patternName);
                    }
                }
            }
        }
        return null;
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
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXPattern.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXPattern";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = PatternMetadata
                .getJavaType(metadataIdentificationString);
        Path path = PatternMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return PatternMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return PatternMetadata.getMetadataIdentiferType();
    }
}
