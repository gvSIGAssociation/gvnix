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
package org.gvnix.addon.jpa.entitylistener;

import java.util.List;

import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataItem;

/**
 * Allows registry {@link MetadataItem} for classes which must be registered as
 * JPA EntityListeners.
 * <p/>
 * This use {@link MetadataDependencyRegistry} to add a dependency from the
 * required metadata to a MetadataListener which manage the
 * <code>META-INF/orm.xml</code> to register the entity listener.
 * <p/>
 * Registered {@link MetadataItem} must implement {@link JpaOrmEntityListener}
 * interface.
 * 
 * @author gvNIX Team
 * 
 */
public interface JpaOrmEntityListenerRegistry {

    /**
     * Register a {@link MetadataItem} as a jpa entity listener
     * 
     * @param metadataIdentifierType JavaType part of
     *        {@link MetadataItem#getId()}
     */
    void registerListenerMetadata(String metadataIdentifierType);

    /**
     * Remove register of a {@link MetadataItem} as a jpa entity listener
     * 
     * @param metadataIdentifierType JavaType part of
     *        {@link MetadataItem#getId()}
     */
    void deregisterListenerMetadata(String metadataIdentifierType);

    /**
     * Allow specify the order execution between to entity listener
     * 
     * @param metadataIdentifierTypeBefore JavaType part of
     *        {@link MetadataItem#getId()} which must be executed before
     * @param metadataIdentifierTypeAfter JavaType part of
     *        {@link MetadataItem#getId()} which must be executed after
     */
    void setListenerOrder(String metadataIdentifierTypeBefore,
            String metadataIdentifierTypeAfter);

    /**
     * @return a <em>immutable</em> list of JavaType part of
     *         {@link MetadataItem#getId()} in execution order
     */
    List<String> getListenerOrder();

}
