/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
package org.gvnix.service.layer.roo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerOperationsImpl implements ServiceLayerOperations {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private ClasspathOperations classpathOperations;
    @Reference
    private ServiceLayerActivationInfo serviceLayerActivationInfo;

    private ComponentContext context;

    private static final String DOCTYPE_PUBLIC = "-//tuckey.org//DTD UrlRewrite 3.0//EN";
    private static final String DOCTYPE_SYSTEM = "http://tuckey.org/res/dtds/urlrewrite3.0.dtd";
    
    protected void activate(ComponentContext context) {
	this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.service.layer.roo.addon.GvNixServiceLayerOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
	return serviceLayerActivationInfo.isProjectAvailable();
    }


    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Adds @org.springframework.stereotype.Service annotation to the class.
     * </p>
     */
    public void createServiceClass(JavaType serviceClass) {

	// Service class
	String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
		serviceClass, Path.SRC_MAIN_JAVA);

	// Service annotations
	List<AnnotationMetadata> serviceAnnotations = new ArrayList<AnnotationMetadata>();
	serviceAnnotations.add(new DefaultAnnotationMetadata(new JavaType(
		"org.springframework.stereotype.Service"),
		new ArrayList<AnnotationAttributeValue<?>>()));

	ClassOrInterfaceTypeDetails serviceDetails = new DefaultClassOrInterfaceTypeDetails(
		declaredByMetadataId, serviceClass, Modifier.PUBLIC,
		PhysicalTypeCategory.CLASS, null, null, null, null, null,
		null, serviceAnnotations, null);

	classpathOperations.generateClassFile(serviceDetails);

    }

}