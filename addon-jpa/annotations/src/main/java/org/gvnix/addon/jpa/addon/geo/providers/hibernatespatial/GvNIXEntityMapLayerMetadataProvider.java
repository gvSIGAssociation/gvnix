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
package org.gvnix.addon.jpa.addon.geo.providers.hibernatespatial;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.PhysicalTypeUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link GvNIXEntityMapLayerMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.4
 */
@Component
@Service
public final class GvNIXEntityMapLayerMetadataProvider extends
        AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(GvNIXEntityMapLayerMetadataProvider.class);

    TypeManagementService typeManagementService;

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
        addMetadataTrigger(new JavaType(GvNIXEntityMapLayer.class.getName()));
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
        removeMetadataTrigger(new JavaType(GvNIXEntityMapLayer.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = GvNIXEntityMapLayerMetadata
                .getJavaType(metadataIdentificationString);

        // Getting entity details
        ClassOrInterfaceTypeDetails entityDetails = getTypeLocationService()
                .getTypeDetails(javaType);

        LogicalPath entityPath = PhysicalTypeUtils.getPath(javaType,
                getTypeLocationService());

        String jpaMetadataId = JpaActiveRecordMetadata.createIdentifier(
                javaType, entityPath);
        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) getMetadataService()
                .get(jpaMetadataId);
        if (jpaMetadata == null) {
            // Unsupported type (by now)
            return null;
        }

        // Getting entity plural
        String plural = jpaMetadata.getPlural();

        // Getting declared fields
        List<JavaSymbolName> geoFieldNames = new ArrayList<JavaSymbolName>();
        List<? extends FieldMetadata> fields = entityDetails
                .getDeclaredFields();
        for (FieldMetadata field : fields) {
            // Getting field type to get package
            JavaType fieldType = field.getFieldType();
            JavaPackage fieldPackage = fieldType.getPackage();
            // If has jts field, annotate entity
            if (fieldPackage.toString().equals("com.vividsolutions.jts.geom")) {
                geoFieldNames.add(field.getFieldName());
            }
        }

        return new GvNIXEntityMapLayerMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata,
                getTypeLocationService(), getTypeManagementService(), javaType,
                plural, geoFieldNames);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXEntityMapLayer.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXEntityMapLayer";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = GvNIXEntityMapLayerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = GvNIXEntityMapLayerMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return GvNIXEntityMapLayerMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return GvNIXEntityMapLayerMetadata.getMetadataIdentiferType();
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
                LOGGER.warning("Cannot load TypeManagementService on GvNIXEntityMapLayer.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }

    }
}