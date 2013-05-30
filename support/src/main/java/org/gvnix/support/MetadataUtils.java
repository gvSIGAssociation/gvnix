/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.support;

import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;

/**
 * Utils for work with Metadata and PhysicalMetadata.
 * 
 * @author gvNIX Team
 * @since 0.8.0
 */
public class MetadataUtils {

    /**
     * Returns an instance of {@link MutableClassOrInterfaceTypeDetails} of the
     * given JavaType 
     * <p/>
     * TODO Delete this method and replace occurences with
     * typeLocationService.getTypeDetails(type)
     * 
     * @param type
     * @param metadataService
     * @param physicalTypeMetadataProvider
     * @return
     * @deprecated Use typeLocationService.getTypeDetails(type) instead
     */
    public static ClassOrInterfaceTypeDetails getPhysicalTypeDetails(
            JavaType type, TypeLocationService typeLocationService) {
        return typeLocationService.getTypeDetails(type);
    }

}
