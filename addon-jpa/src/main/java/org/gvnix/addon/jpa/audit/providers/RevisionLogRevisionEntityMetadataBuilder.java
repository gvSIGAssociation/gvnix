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
package org.gvnix.addon.jpa.audit.providers;

import org.gvnix.addon.jpa.audit.JpaAuditRevisionEntityAnnotationValues;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.model.JavaType;

/**
 * Interface provider by {@link RevisionLogProvider} which produces required
 * artifacts for the entity annotated with
 * {@link org.gvnix.addon.jpa.audit.GvNIXJpaAuditRevisionEntity} to handle
 * revisionLog functionality.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
public interface RevisionLogRevisionEntityMetadataBuilder {

    /**
     * Initializes builder with commons required objects
     * 
     * @param builder
     * @param context
     */
    void initialize(ItdTypeDetailsBuilder builder, Context context);

    /**
     * Clean builder commons objects
     */
    void done();

    /**
     * Fill revision entity with required artifacts
     */
    void fillEntity();

    /**
     * Interface which contains generation time metadata information required by
     * RevisionLogRevisionEntityMetadataBuilder
     * 
     * @author gvNIX Team
     * 
     */
    public interface Context {

        /**
         * @return the helper
         */
        public ItdBuilderHelper getHelper();

        /**
         * @return the annotationValues
         */
        public JpaAuditRevisionEntityAnnotationValues getAnnotationValues();

        /**
         * @return metadataId
         */
        public String getMetadataId();

        /**
         * @return entity
         */
        public JavaType getEntity();
    }

}
