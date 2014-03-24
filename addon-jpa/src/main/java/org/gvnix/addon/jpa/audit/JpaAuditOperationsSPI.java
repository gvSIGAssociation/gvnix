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

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * <code>jpa audit</code> internal operations for revision log providers.
 * 
 * @author gvNIX Team
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
     * @return class for audit user fields
     */
    JavaType getUserType();

    /**
     * @return if Spring Security is installed
     */
    boolean isSpringSecurityInstalled();

    /**
     * @return informs is {@link #getUserType()} implements Spring Security
     *         UserDetails interface
     */
    boolean isUserTypeSpringSecUserDetails();

    /**
     * @return informs if {@link #getUserType()} is an JPA entity
     */
    boolean isUserTypeEntity();

    /**
     * Performs a {@link MetadataService#evictAndGet(String)} of all entities
     * annotated with {@link GvNIXJpaAudit}. This regenerates related
     * <em>.aj</em> files.
     */
    void refreshAuditedEntities();
}