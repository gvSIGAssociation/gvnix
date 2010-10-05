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
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebService;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.metadata.*;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportMetadataNotificationListener implements
	MetadataNotificationListener {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSExportMetadataNotificationListener.class.getName());

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;

    @Reference
    private MetadataService metadataService;

    @Reference
    private AnnotationsService annotationsService;

    protected void activate(ComponentContext context) {
	metadataDependencyRegistry.addNotificationListener(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.metadata.MetadataNotificationListener#notify(java.lang.String, java.lang.String)
     */
    public void notify(String upstreamDependency, String downstreamDependency) {

	if (MetadataIdentificationUtils.getMetadataClass(upstreamDependency)
		.equals(
			MetadataIdentificationUtils
				.getMetadataClass(ServiceLayerWSExportMetadata
					.getMetadataIdentiferType()))) {

	    // Get upstreamDepency Class to check.
	    ClassOrInterfaceTypeDetails governorTypeDetails = getJavaType(upstreamDependency);

	    AnnotationMetadata gvNIXWebServiceAnnotation = MemberFindingUtils
		    .getTypeAnnotation(governorTypeDetails, new JavaType(
			    GvNIXWebService.class.getName()));

	    // Show info
	    logger.log(Level.WARNING,
		    "Check correct format to export the web service class: '"
			    + governorTypeDetails.getName() + "'");

	    // Update CXF XML
	    boolean updateGvNIXWebServiceAnnotation = serviceLayerWsConfigService
		    .exportClass(governorTypeDetails
		    .getName(), gvNIXWebServiceAnnotation);

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

	}

    }


    /**
     * Retrieves the JavaType related to upstreamDependency Metadata.
     * 
     * @param upstreamDependency
     *            {@link ServiceLayerWSExportMetadata} to retrieve JavaType.
     * 
     * @return {@link JavaType} related.
     */
    private ClassOrInterfaceTypeDetails getJavaType(String upstreamDependency) {

	JavaType javaType = ServiceLayerWSExportMetadata
		.getJavaType(upstreamDependency);

	Path webPath = ServiceLayerWSExportMetadata.getPath(upstreamDependency);

	String serviceLayerWSExportMetadataKey = ServiceLayerWSExportMetadata
		.createIdentifier(
		javaType, webPath);

	ServiceLayerWSExportMetadata serviceLayerWSExportMetadata = (ServiceLayerWSExportMetadata) metadataService
		.get(serviceLayerWSExportMetadataKey);

	DefaultItdTypeDetails defaultItdTypeDetails = (DefaultItdTypeDetails) serviceLayerWSExportMetadata
		.getItdTypeDetails();

	Assert.isTrue(defaultItdTypeDetails != null, "Metadata related to '"
		+ upstreamDependency + "' doesn't exist.");

	return defaultItdTypeDetails.getGovernor();
    }

    public void setJax2WsPomConfiguration() {

    }

}
