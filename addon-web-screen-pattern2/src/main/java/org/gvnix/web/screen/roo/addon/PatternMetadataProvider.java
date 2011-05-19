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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadataProvider;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
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

    @Reference
    WebScaffoldMetadataProvider webScaffoldMetadataProvider;

    @Reference
    ProjectOperations projectOperations;

    @Reference
    PropFileOperations propFileOperations;

    @Reference
    WebMetadataService webMetadataService;

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
        JavaType controllerType = PatternMetadata
                .getJavaType(metadataIdentificationString);

        // We need to know the metadata of the Controller through
        // WebScaffoldMetada
        Path path = PatternMetadata.getPath(metadataIdentificationString);
        String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
                controllerType, path);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) webScaffoldMetadataProvider
                .get(webScaffoldMetadataKey);
        if (webScaffoldMetadata == null) {
            logger.warning("The pattern can not be defined over a Controlloer without "
                    + "@RooWebScaffold annotation and its 'fromBackingObject' attribute "
                    + "set. Check "
                    + controllerType.getFullyQualifiedTypeName());
            return null;
        }

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails controllerClassOrInterfaceDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(
                controllerClassOrInterfaceDetails,
                "Governor failed to provide class type details, in violation of superclass contract");
        MemberDetails controllerMemberDetails = memberDetailsScanner
                .getMemberDetails(getClass().getName(),
                        controllerClassOrInterfaceDetails);

        List<StringAttributeValue> definedPatterns = new ArrayList<StringAttributeValue>();

        AnnotationMetadata gvNixPatternAnnotation = MemberFindingUtils
                .getAnnotationOfType(
                        controllerClassOrInterfaceDetails.getAnnotations(),
                        new JavaType(GvNIXPattern.class.getName()));

        if (gvNixPatternAnnotation != null) {
            AnnotationAttributeValue<?> val = gvNixPatternAnnotation
                    .getAttribute(new JavaSymbolName("value"));

            if (val != null) {
                // Ensure we have an array of strings
                if (!(val instanceof ArrayAttributeValue<?>)) {
                    // logger.warning(getErrorMsg());
                    return null;
                }

                ArrayAttributeValue<?> arrayVal = (ArrayAttributeValue<?>) val;
                HashMap<String, String> validPatterns = getValidPatterns(arrayVal);

                for (String pattern : validPatterns.keySet()) {
                    StringAttributeValue newSV = new StringAttributeValue(
                            new JavaSymbolName("ignored"), pattern.concat("=")
                                    .concat(validPatterns.get(pattern)));
                    definedPatterns.add(newSV);
                }
            }
        }

        // Pass dependencies required by the metadata in through its constructor
        return new PatternMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata,
                MemberFindingUtils.getMethods(controllerMemberDetails),
                metadataService, memberDetailsScanner,
                metadataDependencyRegistry, webScaffoldMetadata,
                webMetadataService, fileManager, projectOperations,
                propFileOperations, definedPatterns);
    }

    private HashMap<String, String> getValidPatterns(
            ArrayAttributeValue<?> arrayVal) {
        HashMap<String, String> validPatterns = new HashMap<String, String>();
        StringAttributeValue sv = null;
        String[] pattern = {};
        for (Object o : arrayVal.getValue()) {
            if (!(o instanceof StringAttributeValue)) {
                return null;
            }
            sv = (StringAttributeValue) o;
            pattern = sv.getValue().split("=");
            // TODO: Change the next test using validation over Enumeration
            if (pattern[1].equalsIgnoreCase("table")) {
                if (validPatterns.containsKey(pattern[0])) {
                    throw new IllegalStateException(
                            "Pattern "
                                    .concat(pattern[0])
                                    .concat(" already defined. You can't define twice the same pattern."));
                } else {
                    validPatterns.put(pattern[0], pattern[1]);
                }
            }
        }
        return validPatterns;
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
