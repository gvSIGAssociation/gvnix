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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataDependencyRegistry;

/**
 * Allows registry metadata for classes which must be registered as JPA
 * EntityListeners.
 * <p/>
 * This use {@link MetadataDependencyRegistry} to add a dependency from the
 * required metadata to a MetadataListener which manage the
 * <code>META-INF/orm.xml</code> to register the entity listener.
 * <p/>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * 
 */
@Component
@Service
public class JpaOrmEntityListenerRegistryImpl implements
        JpaOrmEntityListenerRegistry {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    private List<String> order = new ArrayList<String>();

    private List<String> publicOrder = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListenerMetadata(String metadataIdentifierType) {
        if (StringUtils.isBlank(metadataIdentifierType)) {
            throw new IllegalArgumentException(
                    "metadataIdentifierType cannot be blank");
        }
        metadataDependencyRegistry.registerDependency(metadataIdentifierType,
                JpaOrmEntityListenerMetadata.getMetadataIdentiferType());

        // The metadataIdentifier can be already registered by a
        // setListenerOrder call
        if (!order.contains(metadataIdentifierType)) {
            order.add(metadataIdentifierType);
        }
        clearPublicOrder();
    }

    private void clearPublicOrder() {
        publicOrder = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregisterListenerMetadata(String metadataIdentifierType) {
        metadataDependencyRegistry.deregisterDependency(metadataIdentifierType,
                JpaOrmEntityListenerMetadata.getMetadataIdentiferType());
        clearPublicOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListenerOrder(String metadataIdentifierTypeBefore,
            String metadataIdentifierTypeAfter) {
        if (!order.contains(metadataIdentifierTypeBefore)) {
            order.add(metadataIdentifierTypeBefore);
        }
        if (!order.contains(metadataIdentifierTypeAfter)) {
            order.add(metadataIdentifierTypeAfter);
        }
        int indexBefore = order.indexOf(metadataIdentifierTypeBefore);
        int indexAfter = order.indexOf(metadataIdentifierTypeAfter);

        if (indexBefore > indexAfter) {
            order.add(indexAfter, order.remove(indexBefore));
        }
        clearPublicOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getListenerOrder() {
        if (publicOrder == null) {
            publicOrder = Collections.unmodifiableList(order);
        }
        return publicOrder;
    }

}
