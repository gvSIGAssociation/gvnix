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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Utils to work with physical types.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
@Component
@Service
public class PhysicalTypeUtilsImpl implements PhysicalTypeUtils {

    /**
     * Returns the {@link LogicalPath} of the given {@link JavaType}.
     * <p/>
     * Note multi-module projects require you use {@link LogicalPath} to access
     * to physical types.
     * 
     * @param javaType the type (required)
     * @param typeLocationService Type locator service
     * @return
     */
    @Override
    public LogicalPath getPath(JavaType javaType,
            TypeLocationService typeLocationService) {
        final ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getTypeDetails(javaType);
        if (typeDetails == null) {
            return null;
        }
        final LogicalPath path = PhysicalTypeIdentifier.getPath(typeDetails
                .getDeclaredByMetadataId());
        return path;
    }
}
