/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.support;

import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.Assert;

/**
 * 
 * Utils for work with Metadata and PhysicalMetadata.
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8.0
 */
public class MetadataUtils {

    /**
     * Returns an instance of {@link MutableClassOrInterfaceTypeDetails} of the
     * given JavaType
     * 
     * @param type
     * @param metadataService
     * @param physicalTypeMetadataProvider
     * @return
     */
    public static MutableClassOrInterfaceTypeDetails getPhysicalTypeDetails(
            JavaType type, MetadataService metadataService,
            PhysicalTypeMetadataProvider physicalTypeMetadataProvider) {
        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String entityId = physicalTypeMetadataProvider.findIdentifier(type);
        if (entityId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + type.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(entityId, true);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type ".concat(type
                        .getFullyQualifiedTypeName()));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type ".concat(type
                        .getFullyQualifiedTypeName()));
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(type.getFullyQualifiedTypeName()));
        return (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;
    }

}
