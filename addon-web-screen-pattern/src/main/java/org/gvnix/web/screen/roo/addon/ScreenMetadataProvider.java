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
package org.gvnix.web.screen.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

/**
 * Provides {@link ScreenMetadata}
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ScreenMetadataProvider extends AbstractItdMetadataProvider {

    @Reference private ScreenOperations operations;

    protected void activate(ComponentContext context) {
	// Ensure we're notified of all metadata related to physical Java types,
	// in particular their initial creation
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
		.getMetadataIdentiferType(), getProvidesType());
	addMetadataTrigger(new JavaType(RooEntity.class.getName()));
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata(java.lang.String, org.springframework.roo.model.JavaType, org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
	    String metadataIdentificationString, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    String itdFilename) {

	if (!operations.isProjectAvailable()
		|| !operations.isSpringMvcProject()
		|| !operations.isActivated()) {
	    return null;
	}

	// We know governor type details are non-null and can be safely cast

	// Work out the MIDs of the other metadata we depend on
	JavaType javaType = ScreenMetadata
		.getJavaType(metadataIdentificationString);
	Path path = ScreenMetadata
		.getPath(metadataIdentificationString);
	String entityMetadataKey = EntityMetadata.createIdentifier(javaType,
		path);

	// We need to lookup the metadata we depend on
	EntityMetadata entityMetadata = (EntityMetadata) metadataService
		.get(entityMetadataKey);

	// We need to abort if we couldn't find dependent metadata
	if (entityMetadata == null || !entityMetadata.isValid()) {
	    return null;
	}

	// We need to be informed if our dependent metadata changes
	metadataDependencyRegistry.registerDependency(entityMetadataKey,
		metadataIdentificationString);

	return new ScreenMetadata(metadataIdentificationString,
		aspectName, governorPhysicalTypeMetadata, entityMetadata);

    }

    public String getItdUniquenessFilenameSuffix() {
	return "gvNIX_related_entries";
    }

    protected String getGovernorPhysicalTypeIdentifier(
	    String metadataIdentificationString) {
	JavaType javaType = ScreenMetadata
		.getJavaType(metadataIdentificationString);
	Path path = ScreenMetadata
		.getPath(metadataIdentificationString);
	String physicalTypeIdentifier = PhysicalTypeIdentifier
		.createIdentifier(javaType, path);
	return physicalTypeIdentifier;
    }

    protected String createLocalIdentifier(JavaType javaType, Path path) {
	return ScreenMetadata.createIdentifier(javaType, path);
    }

	public String getProvidesType() {
	return ScreenMetadata.getMetadataIdentiferType();
    }
}
