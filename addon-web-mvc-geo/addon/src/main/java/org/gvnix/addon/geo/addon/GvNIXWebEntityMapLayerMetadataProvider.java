/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.geo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.geo.annotations.GvNIXWebEntityMapLayer;
import org.gvnix.support.PhysicalTypeUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.addon.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link GvNIXWebEntityMapLayerMetadata}.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.4
 */
@Component
@Service
public final class GvNIXWebEntityMapLayerMetadataProvider extends
        AbstractItdMetadataProvider {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(GvNIXWebEntityMapLayerMetadataProvider.class);

    TypeManagementService typeManagementService;

    private PhysicalTypeUtils physicalTypeUtils;

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebEntityMapLayer.class.getName()));
    }

    /**
     * Unregister this provider
     * 
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(
                GvNIXWebEntityMapLayer.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = GvNIXWebEntityMapLayerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXWebEntityMapLayerMetadata
                .getPath(metadataIdentificationString);

        // Getting @RooWebScaffold annotation
        String webScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(
                javaType, path);

        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) getMetadataService()
                .get(webScaffoldMetadataId);

        WebScaffoldAnnotationValues webScaffoldAnnotationValues = webScaffoldMetadata
                .getAnnotationValues();

        // Getting entity
        JavaType entity = webScaffoldAnnotationValues.getFormBackingObject();

        LogicalPath entityPath = getPhysicalTypeUtils().getPath(entity,
                getTypeLocationService());

        String jpaMetadataId = JpaActiveRecordMetadata.createIdentifier(entity,
                entityPath);
        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) getMetadataService()
                .get(jpaMetadataId);
        if (jpaMetadata == null) {
            // Unsupported type (by now)
            return null;
        }

        // Getting entity plural
        String plural = jpaMetadata.getPlural();

        return new GvNIXWebEntityMapLayerMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata,
                getTypeLocationService(), getTypeManagementService(), javaType,
                entity, plural);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXWebEntityMapLayer.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXWebEntityMapLayer";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = GvNIXWebEntityMapLayerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXWebEntityMapLayerMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return GvNIXWebEntityMapLayerMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return GvNIXWebEntityMapLayerMetadata.getMetadataIdentiferType();
    }

    public TypeManagementService getTypeManagementService() {
        if (typeManagementService == null) {
            // Get all Services implement TypeManagementService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeManagementService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeManagementService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeManagementService on GvNIXWebEntityMapLayerMetadataProvider.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
    }

    public PhysicalTypeUtils getPhysicalTypeUtils() {
        if (physicalTypeUtils == null) {
            // Get all Services implement PhysicalTypeUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                PhysicalTypeUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    physicalTypeUtils = (PhysicalTypeUtils) this.context
                            .getService(ref);
                    return physicalTypeUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PhysicalTypeUtils on GvNIXWebEntityMapLayerMetadataProvider.");
                return null;
            }
        }
        else {
            return physicalTypeUtils;
        }

    }
}