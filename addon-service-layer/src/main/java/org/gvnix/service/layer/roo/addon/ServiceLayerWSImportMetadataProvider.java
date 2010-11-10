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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebServiceProxy;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <p>
 * Checks if @GvNIXWebServicesProxy annotated classes have been updated and this
 * affects to Service Classes.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSImportMetadataProvider extends
	AbstractItdMetadataProvider {

    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSImportMetadataProvider.class.getName());

    protected void activate(ComponentContext context) {

	// Ensure we're notified of all metadata related to physical Java types,
	// in particular their initial creation
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
		.getMetadataIdentiferType(), getProvidesType());
	addMetadataTrigger(new JavaType(GvNIXWebServiceProxy.class.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * createLocalIdentifier(org.springframework.roo.model.JavaType,
     * org.springframework.roo.project.Path)
     */
    protected String createLocalIdentifier(JavaType javaType, Path path) {

	return ServiceLayerWSImportMetadata.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
	    String metadataIdentificationString) {

	JavaType javaType = ServiceLayerWSImportMetadata
		.getJavaType(metadataIdentificationString);
	Path path = ServiceLayerWSImportMetadata
		.getPath(metadataIdentificationString);
	String physicalTypeIdentifier = PhysicalTypeIdentifier
		.createIdentifier(javaType, path);

	return physicalTypeIdentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata
     * (java.lang.String, org.springframework.roo.model.JavaType,
     * org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
	    String metadataIdentificationString, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    String itdFilename) {

	ServiceLayerWSImportMetadata metadata = null;

	// Import service if project has required prerequisites
	if (serviceLayerWsConfigService.isProjectAvailable()) {

	    // Check if Web Service definition is correct.
	    PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
		    .getPhysicalTypeDetails();

	    ClassOrInterfaceTypeDetails governorTypeDetails;
	    if (physicalTypeDetails == null
		    || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {

		// There is a problem
		return null;

	    } else {

		// We have reliable physical type details
		governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
	    }

	    // Get upstreamDepency Class to check.
	    AnnotationMetadata annotation = MemberFindingUtils
		    .getTypeAnnotation(governorTypeDetails, new JavaType(
			    GvNIXWebServiceProxy.class.getName()));

	    // Wsdl location
	    StringAttributeValue url = (StringAttributeValue) annotation
		    .getAttribute(new JavaSymbolName("wsdlLocation"));
	    
	    try {
		
		// Parse the wsdl location to a DOM document
		Document wsdl = XmlUtils.getDocumentBuilder().parse(
			url.getValue());
		Element root = wsdl.getDocumentElement();
		Assert.notNull(root, "No valid document format");

		if (WsdlParserUtils.isRpcEncoded(root)) {

		    // Generate service infraestructure to import the service
		    serviceLayerWsConfigService.importService(
			    governorTypeDetails.getName(), url.getValue(),
			    CommunicationSense.IMPORT_RPC_ENCODED);
		}
		else {

		    // Generate service infraestructure to import the service
		    serviceLayerWsConfigService.importService(
			    governorTypeDetails.getName(), url.getValue(),
			    CommunicationSense.IMPORT);
		}

		// Generate source code client clases with Axis
		serviceLayerWsConfigService
			.mvn(ServiceLayerWsConfigService.GENERATE_SOURCES);

		// Create metadata
		metadata = new ServiceLayerWSImportMetadata(
			metadataIdentificationString, aspectName,
			governorPhysicalTypeMetadata);
		
	    } catch (SAXException e) {

		Assert.state(false,
			"The format of the web service to import has errors");

	    } 
	    catch (IOException e) {

		logger.log(Level.WARNING,
			"There is no connection to the web service to import");
	    }
	}

	return metadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {

	return "GvNix_WebServiceProxy";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {

	return ServiceLayerWSImportMetadata.getMetadataIdentiferType();
    }

}
