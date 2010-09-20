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

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebServiceProxy;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

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

	ServiceLayerWSImportMetadata serviceLayerMetadata = new ServiceLayerWSImportMetadata(
		metadataIdentificationString, aspectName,
		governorPhysicalTypeMetadata);

	return serviceLayerMetadata;
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
