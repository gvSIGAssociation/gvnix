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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebServiceProxy;

import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;

/**
 * Addon for Handle Web Service Proxy Layer
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerWsImportOperationsImpl implements ServiceLayerWsImportOperations {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWsImportOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private AnnotationsService annotationsService;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.service.layer.roo.addon.ServiceLayerWsImportOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {

	return serviceLayerWsConfigService.isProjectAvailable();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * If the class to add annotation doesn't exist it will be created
     * automatically in 'src/main/java' directory inside the package defined.
     * </p>
     */
    public void addImportAnnotation(JavaType serviceClass, String wsdlLocation) {

	// Service class path
	String fileLocation = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
		serviceClass.getFullyQualifiedTypeName().replace('.', '/')
			.concat(".java"));

	// If class not exists, create it
	if (!fileManager.exists(fileLocation)) {
	    
	    // Create service class with Service Annotation.
	    javaParserService.createServiceClass(serviceClass);
	    logger.log(Level.INFO, "New service class created: "
		    + serviceClass.getSimpleTypeName());
	}
	
	// Add the import definition annotation and attributes to the class
	List<AnnotationAttributeValue<?>> annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
	annotationAttributeValues.add(new StringAttributeValue(
		new JavaSymbolName("wsdlLocation"), wsdlLocation));
	annotationsService.addJavaTypeAnnotation(serviceClass,
		GvNIXWebServiceProxy.class.getName(),
		annotationAttributeValues, false);
	
	// Add GvNixAnnotations to the project.
	annotationsService.addGvNIXAnnotationsDependency();
    }

}
