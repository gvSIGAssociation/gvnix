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

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerWSExportWSDLListener implements FileEventListener {

    private String generateSourcesDirectory;
    
    @Reference
    private PathResolver pathResolver;

    protected static Logger logger = Logger
            .getLogger(ServiceLayerWSExportWSDLListener.class.getName());
            
    protected void activate(ComponentContext context) {

        this.generateSourcesDirectory = pathResolver.getIdentifier(Path.ROOT,
                "target/generated-sources/cxf/");
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)
     */
    public void onFileEvent(FileEvent fileEvent) {
        // TODO Auto-generated method stub

        if (fileEvent.getFileDetails().getFile().getAbsolutePath().contains(
                this.generateSourcesDirectory)) {
            logger.info(fileEvent.getFileDetails().getFile().getAbsolutePath());
        }
    }

}
