/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.layer.roo.addon;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.metadata.*;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportMetadataNotificationListener implements
	MetadataNotificationListener {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSExportMetadataNotificationListener.class.getName());

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    protected void activate(ComponentContext context) {
	metadataDependencyRegistry.addNotificationListener(this);
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.metadata.MetadataNotificationListener#notify(java.lang.String, java.lang.String)
     */
    public void notify(String upstreamDependency, String downstreamDependency) {


	if (MetadataIdentificationUtils.getMetadataClass(upstreamDependency)
		.equals(
			MetadataIdentificationUtils
				.getMetadataClass(PhysicalTypeIdentifier
					.getMetadataIdentiferType()))) {

	    // Show info
	    logger
		    .log(
			    Level.WARNING,
			    "The Service contract has been changed.\n You have to use the command 'service operation' to update the web service contract.");
	}

    }

}
