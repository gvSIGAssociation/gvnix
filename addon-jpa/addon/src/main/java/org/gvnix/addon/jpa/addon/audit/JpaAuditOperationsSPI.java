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
package org.gvnix.addon.jpa.addon.audit;

import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAudit;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAuditRevisionEntity;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * <code>jpa audit</code> internal operations for revision log providers.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.3.0
 */
public interface JpaAuditOperationsSPI {

    public static final String REVISION_LOG_ENTITY_NAME = "RevisionLogEntity";

    public static final JavaType GVNIX_REVION_ENTITY_ANNOTATION = new JavaType(
            GvNIXJpaAuditRevisionEntity.class);

    /**
     * Install Revision entity
     * 
     * @param revisionLogEntity (optional) class to generate
     */
    void installRevisonEntity(JavaType revisionLogEntity);

    /**
     * @return current RevisionEntity
     */
    JavaType getRevisionEntityJavaType();

    /**
     * @return get the lowest package which a entity is found in current project
     */
    JavaPackage getBaseDomainPackage();

    /**
     * @return class which informs of current user at runtime
     */
    JavaType getUserServiceType();

    /**
     * @return if Spring Security is installed
     */
    boolean isSpringSecurityInstalled();

    /**
     * Performs a {@link MetadataService#evictAndGet(String)} of all entities
     * annotated with {@link GvNIXJpaAudit}. This regenerates related
     * <em>.aj</em> files.
     */
    void refreshAuditedEntities();

    /**
     * Clean the User service Cache data
     */
    public void evictUserServiceInfoCache();
}