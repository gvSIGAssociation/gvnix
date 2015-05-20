/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.service.roo.addon.addon;

import java.util.List;

import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.model.JavaType;

/**
 * Utilities to manage annotations.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public interface AnnotationsService {

    /**
     * Add repository and dependency with this addon.
     * <p>
     * Repository and dependency with this addon required in project because
     * annotations from this addon are used along project.
     * </p>
     * <p>
     * Dependency will be updated if version is newer.
     * </p>
     */
    public void addAddonDependency();

    /**
     * Add an annotation to a JavaType with some attributes.
     * <p>
     * If annotation already assined on class, message will be raised.
     * </p>
     * 
     * @param serviceClass Java type to add de annotation
     * @param annotation Annotation class full name, null if not
     * @param annotationAttributeValues Attribute list for the annotation
     * @param forceUpdate overrides annotation value if is true.
     */
    public void addJavaTypeAnnotation(JavaType serviceClass, String annotation,
            List<AnnotationAttributeValue<?>> annotationAttributeValues,
            boolean forceUpdate);

}
