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
package org.gvnix.addon.jpa.audit;

import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Represents a parsed {@link GvNIXJpaAuditRevisionEntity} values
 * 
 * @author gvNIX Team
 * @since 1.3
 */
public class JpaAuditRevisionEntityAnnotationValues extends
        AbstractAnnotationValues {

    public static final JavaType JPA_AUDIT_REV_ANN = new JavaType(
            GvNIXJpaAuditRevisionEntity.class);

    @AutoPopulate
    String[] value;

    public JpaAuditRevisionEntityAnnotationValues(
            final ClassOrInterfaceTypeDetails governorPhysicalTypeDetails) {
        super(governorPhysicalTypeDetails, JPA_AUDIT_REV_ANN);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * Constructor
     * 
     * @param governorPhysicalTypeMetadata
     */
    public JpaAuditRevisionEntityAnnotationValues(
            final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, JPA_AUDIT_REV_ANN);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String[] getValue() {
        return value;
    }
}
