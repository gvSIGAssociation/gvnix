/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.addon.geo.addon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.gvnix.addon.geo.annotations.GvNIXMapViewer;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */

public class GeoUtils {

    private static final JavaType MAP_VIEWER_ANNOTATION = new JavaType(
            GvNIXMapViewer.class.getName());

    /**
     * 
     * This method updates dependencies and repositories with the added to
     * configuration.xml file
     * 
     * @param configuration
     * @param moduleName
     * @param projectOperations
     */
    public static void updatePom(final Element configuration,
            ProjectOperations projectOperations, MetadataService metadataService) {

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepositories(
                    projectOperations.getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            projectOperations.addProperty(projectOperations
                    .getFocusedModuleName(), new Property(property));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }

    /**
     * This method returns a map controller by recived path
     * 
     * @param path
     * @return
     */
    public static ClassOrInterfaceTypeDetails getMapControllerByPath(
            TypeLocationService typeLocationService, String path) {
        // Looking for Controller with current path on @RequestMapping
        // annotation
        Set<ClassOrInterfaceTypeDetails> controllersList = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
                        "org.gvnix.addon.geo.GvNIXMapViewer"));

        Validate.notNull(controllersList,
                "Controllers with @GvNIXMapViewer annotation doesn't found");

        // Creating empty controller where set current map controller
        ClassOrInterfaceTypeDetails mapController = null;

        Iterator<ClassOrInterfaceTypeDetails> it = controllersList.iterator();
        while (it.hasNext()) {
            ClassOrInterfaceTypeDetails controller = it.next();
            AnnotationMetadata requestMapping = controller
                    .getAnnotation(SpringJavaType.REQUEST_MAPPING);
            AnnotationAttributeValue<Object> annotationPath = requestMapping
                    .getAttribute("value");
            String annotationPathValue = annotationPath.getValue().toString()
                    .replaceAll("/", "");
            if (path.equals(annotationPathValue)) {
                mapController = controller;
                break;
            }
        }

        return mapController;
    }

    /**
     * 
     * This method returns all available maps
     * 
     * @param typeLocationService
     * @return
     */
    public static List<String> getAllMaps(
            TypeLocationService typeLocationService) {
        List<String> paths = new ArrayList<String>();

        for (JavaType mapViewer : typeLocationService
                .findTypesWithAnnotation(MAP_VIEWER_ANNOTATION)) {
            Validate.notNull(mapViewer, "@GvNIXMapViewer required");

            ClassOrInterfaceTypeDetails mapViewerController = typeLocationService
                    .getTypeDetails(mapViewer);

            // Getting RequestMapping annotations
            final AnnotationMetadata requestMappingAnnotation = MemberFindingUtils
                    .getAnnotationOfType(mapViewerController.getAnnotations(),
                            SpringJavaType.REQUEST_MAPPING);

            Validate.notNull(mapViewer, String.format(
                    "Error on %s getting @RequestMapping value", mapViewer));

            String requestMappingPath = requestMappingAnnotation
                    .getAttribute("value").getValue().toString();

            String currentPath = requestMappingPath.toString().replaceAll("/",
                    "");

            paths.add(currentPath);
        }

        return paths;
    }

    /**
     * 
     * This method returns all available maps Controllers
     * 
     * @param typeLocationService
     * @return
     */
    public static List<JavaType> getAllMapsControllers(
            TypeLocationService typeLocationService) {
        List<JavaType> controllers = new ArrayList<JavaType>();

        for (JavaType mapViewer : typeLocationService
                .findTypesWithAnnotation(MAP_VIEWER_ANNOTATION)) {
            Validate.notNull(mapViewer, "@GvNIXMapViewer required");

            ClassOrInterfaceTypeDetails mapViewerController = typeLocationService
                    .getTypeDetails(mapViewer);

            // Getting RequestMapping annotations
            final AnnotationMetadata requestMappingAnnotation = MemberFindingUtils
                    .getAnnotationOfType(mapViewerController.getAnnotations(),
                            SpringJavaType.REQUEST_MAPPING);

            Validate.notNull(mapViewer, String.format(
                    "Error on %s getting @RequestMapping value", mapViewer));

            controllers.add(mapViewer);
        }

        return controllers;
    }

    /**
     * 
     * This method returns map controller by path
     * 
     * @param typeLocationService
     * @return
     */
    public static JavaType getMapControllerByPath(String path,
            TypeLocationService typeLocationService) {

        for (JavaType mapViewer : typeLocationService
                .findTypesWithAnnotation(MAP_VIEWER_ANNOTATION)) {
            Validate.notNull(mapViewer, "@GvNIXMapViewer required");

            ClassOrInterfaceTypeDetails mapViewerController = typeLocationService
                    .getTypeDetails(mapViewer);

            // Getting RequestMapping annotations
            final AnnotationMetadata requestMappingAnnotation = MemberFindingUtils
                    .getAnnotationOfType(mapViewerController.getAnnotations(),
                            SpringJavaType.REQUEST_MAPPING);

            Validate.notNull(mapViewer, String.format(
                    "Error on %s getting @RequestMapping value", mapViewer));

            String requestMappingPath = requestMappingAnnotation
                    .getAttribute("value").getValue().toString();

            String currentPath = requestMappingPath.toString().replaceAll("/",
                    "");

            if (currentPath.equals(path)) {
                return mapViewer;
            }
        }

        return null;
    }

    /**
     * This method checks if the current entity is a GEO entity or not
     * 
     * @param scaffoldAnnotation
     * @param typeLocationService
     * @return
     */
    public static boolean isGeoEntity(AnnotationMetadata scaffoldAnnotation,
            TypeLocationService typeLocationService) {
        // Checking that entity has GEO fields
        Object entity = scaffoldAnnotation.getAttribute("formBackingObject")
                .getValue();
        ClassOrInterfaceTypeDetails entityDetails = typeLocationService
                .getTypeDetails((JavaType) entity);

        List<? extends FieldMetadata> entityFields = entityDetails
                .getDeclaredFields();

        Iterator<? extends FieldMetadata> fieldsIterator = entityFields
                .iterator();

        boolean isValidEntity = false;

        while (fieldsIterator.hasNext()) {
            // Getting field
            FieldMetadata field = fieldsIterator.next();

            // Getting field type to get package
            JavaType fieldType = field.getFieldType();
            JavaPackage fieldPackage = fieldType.getPackage();

            // If is jts field, annotate controller with maps
            if (fieldPackage.toString().equals("com.vividsolutions.jts.geom")) {
                isValidEntity = true;
                break;
            }
        }

        return isValidEntity;
    }
}
