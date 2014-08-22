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
package org.gvnix.addon.geo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.PhysicalTypeUtils;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

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

    @Reference
    TypeManagementService typeManagementService;

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
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
        metadataDependencyRegistry.deregisterDependency(
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
        LogicalPath path = GvNIXEntityMapLayerMetadata
                .getPath(metadataIdentificationString);

        // Getting controller
        ClassOrInterfaceTypeDetails controller = typeLocationService
                .getTypeDetails(javaType);

        // Getting @RooWebScaffold annotation
        String webScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(
                javaType, path);

        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataId);

        WebScaffoldAnnotationValues webScaffoldAnnotationValues = webScaffoldMetadata
                .getAnnotationValues();

        // Getting entity
        JavaType entity = webScaffoldAnnotationValues.getFormBackingObject();

        LogicalPath entityPath = PhysicalTypeUtils.getPath(entity,
                typeLocationService);

        String jpaMetadataId = JpaActiveRecordMetadata.createIdentifier(entity,
                entityPath);
        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) metadataService
                .get(jpaMetadataId);
        if (jpaMetadata == null) {
            // Unsupported type (by now)
            return null;
        }

        // Getting entity plural
        String plural = jpaMetadata.getPlural();

        // Getting @GvNIXEntityMapLayer annotation
        AnnotationMetadata entityMapLayerAnnotation = controller
                .getAnnotation(new JavaType(GvNIXEntityMapLayer.class.getName()));

        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArrayAttributeValue<StringAttributeValue> entityMapLayerAnnotationAttr = (ArrayAttributeValue) entityMapLayerAnnotation
                .getAttribute("maps");

        List<JavaType> controllersList = new ArrayList<JavaType>();
        // If not have paths, add to all maps controllers
        if (entityMapLayerAnnotationAttr == null) {
            controllersList = GeoUtils
                    .getAllMapsControllers(typeLocationService);
        }
        else {
            // If have paths, use it to obtain map controller
            List<StringAttributeValue> listPath = entityMapLayerAnnotationAttr
                    .getValue();
            Iterator<StringAttributeValue> it = listPath.iterator();
            while (it.hasNext()) {
                StringAttributeValue mapPathAttr = it.next();
                String mapPath = mapPathAttr.getValue();
                JavaType mapController = GeoUtils.getMapControllerByPath(
                        typeLocationService, mapPath).getType();
                controllersList.add(mapController);
            }
        }

        // Calculate in which Map controller must not be displayed entity
        List<JavaType> allMapControllers = GeoUtils
                .getAllMapsControllers(typeLocationService);
        List<JavaType> controllersToRemove = new ArrayList<JavaType>();
        for (JavaType mapController : allMapControllers) {
            if (controllersList.indexOf(mapController) == -1) {
                controllersToRemove.add(mapController);
            }
        }

        return new GvNIXEntityMapLayerMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, typeLocationService,
                typeManagementService, entity, plural, controllersList,
                controllersToRemove);
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
}