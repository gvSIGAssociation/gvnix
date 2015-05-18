/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.support;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;

/**
 * Utils for work with Metadata and PhysicalMetadata.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.8.0
 */
@Component
@Service
public class MetadataUtilsImpl implements MetadataUtils {

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
    @Override
    public ClassOrInterfaceTypeDetails getPhysicalTypeDetails(JavaType type,
            TypeLocationService typeLocationService) {
        return typeLocationService.getTypeDetails(type);
    }

}
