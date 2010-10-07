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
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebMethod;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * <p>
 * Checks if @GvNIXWebServices annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportMetadataProvider extends AbstractItdMetadataProvider {

    @Reference
    private ServiceLayerWSExportValidationService serviceLayerWSExportValidationService;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private AnnotationsService annotationsService;

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSExportMetadataProvider.class.getName());

    protected void activate(ComponentContext context) {
	// Ensure we're notified of all metadata related to physical Java types,
	// in particular their initial creation
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
		.getMetadataIdentiferType(), getProvidesType());
	addMetadataTrigger(new JavaType(GvNIXWebService.class.getName()));
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#createLocalIdentifier(org.springframework.roo.model.JavaType, org.springframework.roo.project.Path)
     */
    protected String createLocalIdentifier(JavaType javaType, Path path) {
	return ServiceLayerWSExportMetadata.createIdentifier(javaType, path);
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
	    String metadataIdentificationString) {
	JavaType javaType = ServiceLayerWSExportMetadata
		.getJavaType(metadataIdentificationString);
	Path path = ServiceLayerWSExportMetadata
		.getPath(metadataIdentificationString);
	String physicalTypeIdentifier = PhysicalTypeIdentifier
		.createIdentifier(javaType, path);
	return physicalTypeIdentifier;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata(java.lang.String, org.springframework.roo.model.JavaType, org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
	    String metadataIdentificationString, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    String itdFilename) {

        ServiceLayerWSExportMetadata serviceLayerMetadata = null;

        if (serviceLayerWsConfigService
                        .isCxfInstalled(CommunicationSense.EXPORT)) {

            // TODO: Check if Web Service definition is correct.
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

            AnnotationMetadata gvNIXWebServiceAnnotation = MemberFindingUtils
                    .getTypeAnnotation(governorTypeDetails, new JavaType(
                            GvNIXWebService.class.getName()));

            // Show info
            logger.log(Level.WARNING,
                    "Check correct format to export the web service class: '"
                            + governorTypeDetails.getName() + "'");

            // Update CXF XML
            boolean updateGvNIXWebServiceAnnotation = serviceLayerWsConfigService
                    .exportClass(governorTypeDetails.getName(),
                            gvNIXWebServiceAnnotation);

            // Define Jax-WS plugin and creates and execution build for this
            // service
            // to generate the wsdl file to check errors before deploy.

            // Check values to generate Jax2Ws build plugin.
            StringAttributeValue serviceName = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("serviceName"));

            StringAttributeValue address = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("address"));

            StringAttributeValue fullyQualifiedTypeName = (StringAttributeValue) gvNIXWebServiceAnnotation
                    .getAttribute(new JavaSymbolName("fullyQualifiedTypeName"));

            serviceLayerWsConfigService.jaxwsBuildPlugin(governorTypeDetails
                    .getName(), serviceName.getValue(), address.getValue(),
                    fullyQualifiedTypeName.getValue());

            // TODO: Check method annotations
            List<? extends MethodMetadata> methodList = governorTypeDetails
                    .getDeclaredMethods();

            for (MethodMetadata methodMetadata : methodList) {

                AnnotationMetadata gvNixWebMethodAnnotation = MemberFindingUtils.getAnnotationOfType(methodMetadata
                        .getAnnotations(), new JavaType(GvNIXWebMethod.class
                        .getName()));
                        
                if (gvNixWebMethodAnnotation != null) {

                    // Check INPUT/OUTPUT parameters
                    serviceLayerWSExportValidationService
                            .checkAuthorizedJavaTypesInOperation(
                                    governorTypeDetails.getName(),
                                    methodMetadata.getMethodName());

                    // Check and update exceptions.
                    serviceLayerWSExportValidationService
                            .checkMethodExceptions(
                            governorTypeDetails.getName(), methodMetadata
                                    .getMethodName());
                    
                    // Check if attributes are defined in class.
                    Assert.isTrue(!gvNixWebMethodAnnotation.getAttributeNames()
                            .isEmpty(), "The annotation @GvNIXWebMethod for '"
                            + methodMetadata.getMethodName()
                            + "' method in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' must have all its attributes defined.");

                    // TODO: Check if attributes are correct to export method to
                    // web service annotation in ITD.

                }
            }

            // Update Annotation because Java Class or package has changed.
            if (updateGvNIXWebServiceAnnotation) {

                List<AnnotationAttributeValue<?>> gvNixAnnotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName("name")));
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName(
                                        "targetNamespace")));
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName("serviceName")));
                gvNixAnnotationAttributes
                        .add((StringAttributeValue) gvNIXWebServiceAnnotation
                                .getAttribute(new JavaSymbolName("address")));
                gvNixAnnotationAttributes.add(new StringAttributeValue(
                        new JavaSymbolName("fullyQualifiedTypeName"),
                        governorTypeDetails.getName()
                                .getFullyQualifiedTypeName()));
                annotationsService.addJavaTypeAnnotation(governorTypeDetails
                        .getName(), GvNIXWebService.class.getName(),
                        gvNixAnnotationAttributes, true);
            }

            serviceLayerMetadata = new ServiceLayerWSExportMetadata(
                    metadataIdentificationString, aspectName,
                    governorPhysicalTypeMetadata);

        }

        return serviceLayerMetadata;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.ItdMetadataProvider#getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
	return "GvNix_WebService";
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
	return ServiceLayerWSExportMetadata.getMetadataIdentiferType();
    }

}
