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

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.logging.HandlerUtils;

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
    private static final Logger logger = HandlerUtils
            .getLogger(AbstractPatternMetadataProvider.class);

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
