/*
 * Copyright (C) 2009 - CONSELLERIA D'INFRAESTRUCTURES I TRANSPORT 
 *                      GENERALITAT VALENCIANA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * You may obtain a copy of the License at http://www.gnu.org/licenses/gpl-2.0.html
 */
package org.gvnix.web.relation.styles.roo.addon;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadataProvider;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

/**
 * Provides {@link PaginatedRealationMetadata}
 * 
 * 
 * @author Ricardo García Fernández( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class PaginatedRealationMetadataProvider extends
	AbstractItdMetadataProvider {

    @Reference
    private BeanInfoMetadataProvider beanInfoMetadataProvider;

    protected void activate(ComponentContext context) {
	// Ensure we're notified of all metadata related to physical Java types,
	// in particular their initial creation
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
		.getMetadataIdentiferType(), getProvidesType());
	beanInfoMetadataProvider.addMetadataTrigger(new JavaType(
		RooEntity.class.getName()));
	addMetadataTrigger(new JavaType(RooEntity.class.getName()));
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata(java.lang.String, org.springframework.roo.model.JavaType, org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
	    String metadataIdentificationString, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    String itdFilename) {
	// We know governor type details are non-null and can be safely cast

	// Work out the MIDs of the other metadata we depend on
	JavaType javaType = PaginatedRealationMetadata
		.getJavaType(metadataIdentificationString);
	Path path = PaginatedRealationMetadata
		.getPath(metadataIdentificationString);
	String beanInfoMetadataKey = BeanInfoMetadata.createIdentifier(
		javaType, path);
	String entityMetadataKey = EntityMetadata.createIdentifier(javaType,
		path);

	// We need to lookup the metadata we depend on
	BeanInfoMetadata beanInfoMetadata = (BeanInfoMetadata) metadataService
		.get(beanInfoMetadataKey);
	EntityMetadata entityMetadata = (EntityMetadata) metadataService
		.get(entityMetadataKey);

	// We need to abort if we couldn't find dependent metadata
	if (beanInfoMetadata == null || !beanInfoMetadata.isValid()
		|| entityMetadata == null || !entityMetadata.isValid()) {
	    return null;
	}

	// We need to be informed if our dependent metadata changes
	metadataDependencyRegistry.registerDependency(beanInfoMetadataKey,
		metadataIdentificationString);
	metadataDependencyRegistry.registerDependency(entityMetadataKey,
		metadataIdentificationString);

	return new PaginatedRealationMetadata(metadataIdentificationString,
		aspectName, governorPhysicalTypeMetadata, beanInfoMetadata,
		entityMetadata);

    }

    public String getItdUniquenessFilenameSuffix() {
	return "PaginatedRealation";
    }

	protected String getGovernorPhysicalTypeIdentifier(
	    String metadataIdentificationString) {
	JavaType javaType = PaginatedRealationMetadata
		.getJavaType(metadataIdentificationString);
	Path path = PaginatedRealationMetadata
		.getPath(metadataIdentificationString);
	String physicalTypeIdentifier = PhysicalTypeIdentifier
		.createIdentifier(javaType, path);
	return physicalTypeIdentifier;
    }

    protected String createLocalIdentifier(JavaType javaType, Path path) {
	return PaginatedRealationMetadata.createIdentifier(javaType, path);
    }

	public String getProvidesType() {
	return PaginatedRealationMetadata.getMetadataIdentiferType();
    }
}
