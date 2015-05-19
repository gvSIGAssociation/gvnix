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
package org.gvnix.addon.jpa.addon.batch;

import org.gvnix.addon.jpa.annotations.batch.GvNIXJpaBatch;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Represents a parsed {@link GvNIXJpaBatch} values
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public class JpaBatchAnnotationValues extends AbstractAnnotationValues {

    public static final JavaType JPA_BATCH_ANNOTATION = new JavaType(
            GvNIXJpaBatch.class);

    @AutoPopulate
    JavaType entity;

    public JpaBatchAnnotationValues(
            final ClassOrInterfaceTypeDetails governorPhysicalTypeDetails) {
        super(governorPhysicalTypeDetails, JPA_BATCH_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * Constructor
     * 
     * @param governorPhysicalTypeMetadata
     */
    public JpaBatchAnnotationValues(
            final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, JPA_BATCH_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public JavaType getEntity() {
        return entity;
    }
}
