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
package org.gvnix.addon.jpa.addon.entitylistener;

import org.springframework.roo.model.JavaType;

/**
 * Operations for JpaOrmEntityListener add-on
 * <p/>
 * Declared operation to allow register a jpa entity-listener in the orm.xml
 * file.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public interface JpaOrmEntityListenerOperations {

    /**
     * Add a entity-listener on orm.xml
     * 
     * @param definition of target metadata
     * @param sourceMetadataProvider identification string (will be included on
     *        entity-listener declaration)
     */
    public void addEntityListener(JpaOrmEntityListener definition,
            String sourceMetadataProvider);

    /**
     * Clean up the entity-listener declaration on orm.xml for specified entity.
     * <p/>
     * This process checks that all classes registered as jpa-entity-listener on
     * a entity are available on project. If not, remove it from orm.xml.
     * 
     * @param entity
     */
    public void cleanUpEntityListeners(JavaType entity);

    /**
     * Informs if entity has any entity-listener registered
     * 
     * @param entity
     * @return
     */
    public boolean hasAnyListener(JavaType entity);

}
