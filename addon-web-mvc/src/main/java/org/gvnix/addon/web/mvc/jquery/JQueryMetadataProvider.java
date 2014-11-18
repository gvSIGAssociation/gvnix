/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.web.mvc.jquery;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides the {@link JQueryMetadata}, that is, this class collects from OSGi
 * services the needed info to create the {@link JQueryMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public final class JQueryMetadataProvider extends AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(JQueryMetadataProvider.class);

    /**
     * Registers {@link JQueryMetadata} identifier into the
     * {@link MetadataDependencyRegistry} having as {@code upstreamDependency}
     * the identifier for any Java class in current project, that is, this
     * provider will receive changes in any meta-data of any Java class.
     * <p/>
     * Adds the meta-data creation trigger for {@link JQueryMetadata}. This
     * trigger causes that when one Java class is annotated with
     * {@link JQueryMetadata} the method {@link #get(String)} will be invoked to
     * create the meta-data instance. Note the meta-data class contains and
     * composes the details of the related Java type and
     * {@link AbstractItdMetadataProvider#get(String)} will create the meta-data
     * related ITD.
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());

        addMetadataTrigger(new JavaType(GvNIXWebJQuery.class.getName()));
    }

    /**
     * Unregister itself and the metadata trigger for {@link JQueryMetadata}
     * 
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXWebJQuery.class.getName()));
    }

    /**
     * Creates the meta-data instance offered by this add-on.
     * <p/>
     * Note this method is called by {@link #get(String)}
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Java type annotated with annotation related to given meta-data
        JavaType javaType = JQueryMetadata
                .getJavaType(metadataIdentificationString);

        // Path to Java type annotated with annotation related to given
        // meta-data
        LogicalPath path = JQueryMetadata.getPath(metadataIdentificationString);

        // --- Get @RooWebScaffold annotation values ---

        // Create the ID to get the annotation
        String webScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(
                javaType, path);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) getMetadataService()
                .get(webScaffoldMetadataId);
        WebScaffoldAnnotationValues webScaffoldAnnotationValues = webScaffoldMetadata
                .getAnnotationValues();

        return new JQueryMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, webScaffoldAnnotationValues);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be {@code **_ROO_GvNIXWebJQuery.aj}
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXWebJQuery";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = JQueryMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = JQueryMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return JQueryMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return JQueryMetadata.getMetadataIdentiferType();
    }
}