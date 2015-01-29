/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2014 Generalitat Valenciana
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
package org.gvnix.addon.jpa.addon.entitylistener;

import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaType;

/**
 * Required interface for any {@link MetadataItem} which must be registered as a
 * JPA entity listener.
 * 
 * @author gvNIX Team
 * 
 */
public interface JpaOrmEntityListener extends MetadataItem {

    /**
     * @return target JPA entity
     */
    JavaType getEntityClass();

    /**
     * @return entity-listener to register
     */
    JavaType getListenerClass();
}
