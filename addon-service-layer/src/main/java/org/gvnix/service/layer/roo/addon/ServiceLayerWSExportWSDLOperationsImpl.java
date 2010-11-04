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

import java.io.File;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.springframework.roo.file.monitor.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerWSExportWSDLOperationsImpl implements
        ServiceLayerWSExportWSDLOperations {

    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private NotifiableFileMonitorService fileMonitorService;
    @Reference
    private PathResolver pathResolver;

    private static final String GENERATED_CXF_SOURCES_DIR = "target/generated-sources/cxf/server/";

    private static Logger logger = Logger
            .getLogger(ServiceLayerWSExportWSDLOperationsImpl.class.getName());

    public boolean isProjectAvailable() {

        return serviceLayerWsConfigService.isProjectAvailable();
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void exportWSDL2Java(String url) {

        // Check WSDL, configure plugin and generate sources.
        serviceLayerWsConfigService.exportWSDLWebService(url,
                CommunicationSense.EXPORT_WSDL);

        // Check generated classes.
        String generateSourcesDirectory = pathResolver.getIdentifier(Path.ROOT,
                GENERATED_CXF_SOURCES_DIR);

        DirectoryMonitoringRequest directoryMonitoringRequest = new DirectoryMonitoringRequest(
                new File(generateSourcesDirectory), true, (MonitoringRequest
                        .getInitialMonitoringRequest(generateSourcesDirectory))
                        .getNotifyOn());

        fileMonitorService.add(directoryMonitoringRequest);
        fileMonitorService.scanAll();

        // Remove Directory listener.
        fileMonitorService.remove(directoryMonitoringRequest);

        // 5) TODO: Convert java classes with gvNIX annotations.
        // Using ServiceLayerWSExportWSDLListener
        updateAnnotationsToGvNIX();

    }

    /**
     * {@inheritDoc}
     * <p>
     * Check the files listed in Arrays in this class.
     * </p>
     * TODO:
     */
    public void updateAnnotationsToGvNIX() {

        serviceLayerWsConfigService.generateGvNIXWebServiceFiles();
    }

}
