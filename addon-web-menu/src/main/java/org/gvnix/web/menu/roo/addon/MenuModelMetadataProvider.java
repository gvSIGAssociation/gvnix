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
package org.gvnix.web.menu.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;

/**
 * gvNIX menu model Metadata Provider
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component(immediate=true)
@Service
public class MenuModelMetadataProvider extends AbstractMenuMetadataProvider {

    protected void activate(ComponentContext context) {
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
	return MenuModelMetadata.getMetadataIdentiferType();
    }

    @Override
    protected String getMetadataIdentiferFinal() {
	return MenuModelMetadata.getMetadataIdentiferFinal();
    }

    @Override
    protected AbstractMenuMetadata createMenuModelMetadata(
	    PhysicalTypeMetadata physicalTypeMetadata) {
	PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
		.getPhysicalTypeDetails();

	if (!(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {

	    return null;
	}
	ClassOrInterfaceTypeDetails classDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;



	AnnotationMetadata gvnixAnnotation = MemberFindingUtils
		.getTypeAnnotation(classDetails, new JavaType(
			getAnnotationClass().getCanonicalName()));
	if (gvnixAnnotation == null) {
	    return null;
	}

	return new MenuModelMetadata(physicalTypeMetadata);
    }

    @Override
    protected Class getAnnotationClass() {
	return GvNIXMenuModel.class;
    }


}
