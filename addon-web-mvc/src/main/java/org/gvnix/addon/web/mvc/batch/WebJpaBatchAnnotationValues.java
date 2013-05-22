/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
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
 * this program. If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.web.mvc.batch;

import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Represents a parsed {@link GvNIXWebJpaBatch} values
 * 
 * @author gvNIX Team
 */
public class WebJpaBatchAnnotationValues extends AbstractAnnotationValues {

    public static final JavaType WEB_JPA_BATCH_ANNOTATION = new JavaType(
            GvNIXWebJpaBatch.class);

    @AutoPopulate JavaType service;

    public WebJpaBatchAnnotationValues(
            final ClassOrInterfaceTypeDetails governorPhysicalTypeDetails) {
        super(governorPhysicalTypeDetails, WEB_JPA_BATCH_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * Constructor
     * 
     * @param governorPhysicalTypeMetadata
     */
    public WebJpaBatchAnnotationValues(
            final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, WEB_JPA_BATCH_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * Returns value of service annotation value
     * 
     * @return
     */
    public JavaType getService() {
        return service;
    }
}
