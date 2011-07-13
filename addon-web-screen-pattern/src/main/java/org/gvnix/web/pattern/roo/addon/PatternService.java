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
package org.gvnix.web.pattern.roo.addon;

import java.util.List;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaType;

/**
 * Interface to provide common services to Screen Pattern management components
 * 
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
public interface PatternService {

    /**
     * Check if there are pattern names used more than once in project
     * 
     * TODO: Refactor this method to check <i>only one pattern</i> instead
     * <i>all patterns</i> in whole project
     * 
     * @return duplicated pattern found, otherwise return <code>null</code>
     * 
     */
    String findPatternDefinedMoreThanOnceInProject();

    /**
     * Look at Entity for a One-to-many field by name
     * 
     * @param entityJavaType
     * @param fieldName
     * @return
     */
    public FieldMetadata getOneToManyFieldFromEntityJavaType(
            JavaType entityJavaType, String fieldName);

    /**
     * Returns the list of fields metadata of those fields annotated with
     * OneToMany
     * 
     * @param formBakingObjectType
     * @return
     */
    public List<FieldMetadata> getOneToManyFieldsFromEntityJavaType(
            JavaType formBakingObjectType);

    /**
     * Returns an instance of MutableClassOrInterfaceTypeDetails of the type
     * given
     * 
     * @param type
     * 
     * @return
     */
    public MutableClassOrInterfaceTypeDetails getPhysicalTypeDetails(
            JavaType type);

}
