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
package org.gvnix.addon.jpa.addon.audit.providers;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.addon.audit.JpaAuditMetadata;
import org.gvnix.addon.jpa.addon.audit.JpaAuditOperationsSPI;
import org.gvnix.addon.jpa.addon.audit.JpaAuditRevisionEntityAnnotationValues;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAudit;
import org.springframework.roo.classpath.PhysicalTypeMetadata;

/**
 * Interface for Revision Log providers
 * <p/>
 * Providers must implements this interface and must be annotated with
 * {@link Component} and {@link Service}.
 * <p/>
 * Provider registry will be done automatically by the OSGi injection mechanism
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
public interface RevisionLogProvider {

    /**
     * @return if this provider can be configured on current project
     */
    boolean isAvailable();

    /**
     * @return if this provider is the provider set on current project. <b>Just
     *         ONLY ONE provider can return true at time</b>
     */
    boolean isActive();

    /**
     * @return Name for this Provider
     */
    String getName();

    /**
     * @return Description of this provider
     */
    String getDescription();

    /**
     * @return Value to use when {@link GvNIXJpaAudit#storeRevisionLog()} is set
     *         to PROVIDER_DEFAULT
     */
    boolean getDefaultValueOfRevisionLogAttribute();

    /**
     * Performs the requiered operation to setup this provider on current
     * project
     */
    void setup(JpaAuditOperationsSPI operations);

    /**
     * Create an instance of {@link RevisionLogMetadataBuilder} to generate
     * {@link JpaAuditMetadata} for <code>governorPhysicalTypeMetadata</code>
     * entity.
     * 
     * @param operations
     * @param governorPhysicalTypeMetadata
     * @return
     */
    RevisionLogMetadataBuilder getMetadataBuilder(
            JpaAuditOperationsSPI operations,
            PhysicalTypeMetadata governorPhysicalTypeMetadata);

    /**
     * Create an instance of {@link RevisionLogRevisionEntityMetadataBuilder} to
     * generate {@link JpaAuditRevisionEntityAnnotationValues} in
     * <code>governorPhysicalTypeMetadata</code> class.
     * 
     * @param operations
     * @param governorPhysicalTypeMetadata
     * @return
     */
    RevisionLogRevisionEntityMetadataBuilder getRevisonEntityMetadataBuilder(
            JpaAuditOperationsSPI operations,
            PhysicalTypeMetadata governorPhysicalTypeMetadata);

}
