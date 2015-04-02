/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
package org.gvnix.occ.roo.addon.addon;

import org.springframework.roo.model.JavaType;

/**
 * gvNIX OCCChecksum operation service
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface OCCChecksumOperations {

    /**
     * <p>
     * Informs if OCCChecksum operations are available
     * </p>
     */
    public boolean isOCCChecksumAvailable();

    /**
     * <p>
     * Adds Optimistic Concurrency Control to a entity
     * </p>
     * <p>
     * If this entity already has applied this behavior this action will have no
     * effect.
     * </p>
     * 
     * @param entity to add OCCChecksum behavior
     * @param fieldName
     * @param digestMethod
     */
    public void addOccToEntity(JavaType entity, String fieldName,
            String digestMethod);

    /**
     * <p>
     * Adds Optimistic Concurrency Control to all entities on project
     * </p>
     * <p>
     * If any entity already has applied this behavior this action will have no
     * effect in it.
     * </p>
     * 
     * @param fieldName
     * @param digestMethod <i>based on
     *        {@link org.springframework.roo.addon.web.mvc.controller.ControllerOperations#generateAll(org.springframework.roo.model.JavaPackage)}
     *        </i>
     */
    public void addOccAll(String fieldName, String digestMethod);

    /**
     * Adds GvNIX annotations library dependency to the current project TO BE
     * REMOVED FROM API
     */
    public void addGvNIXAnnotationsDependecy();

}
