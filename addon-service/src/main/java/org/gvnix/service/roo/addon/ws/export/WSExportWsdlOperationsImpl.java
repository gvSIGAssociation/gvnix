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
package org.gvnix.service.roo.addon.ws.export;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.ws.WSConfigService.WsType;
import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 */
@Component
@Service
public class WSExportWsdlOperationsImpl implements WSExportWsdlOperations {

    @Reference
    private WSExportWsdlConfigService wSExportWsdlConfigService;

    private static final String GENERATED_CXF_SOURCES_DIR = "target/generated-sources/cxf/server/";

    /**
     * {@inheritDoc}
     */
    public List<JavaType> exportWSDL2Java(String url) {

        // Generate java files for WSDL using maven wsdl2java plugin.
        // Generated are paced in GENERATED_CXF_SOURCES_DIR.
        wSExportWsdlConfigService.exportWSDLWebService(url,
                WsType.EXPORT_WSDL);

        // Add GENERATED_CXF_SOURCES_DIR roo file monitor for get notification
        // of all files create by maven plugin
        wSExportWsdlConfigService
                .monitoringGeneratedSourcesDirectory(GENERATED_CXF_SOURCES_DIR);

        // WSExporWsdlListerner register all created files into
        // GENERATED_CXF_SOURCES_DIR,
        // Identify file type and call to wSExportWsdlConfigService for every
        // single file.

        // After maven wsdl2java plugin finished (its is supposed to finish
        // before this command [!!!]) this command copy generated files into
        // src folder and annotated with GvNIX classes.
        return updateAnnotationsToGvNIX();

    }

    /**
     * {@inheritDoc}
     * 
     * TODO this method must changed to private or removed
     * 
     * This method calls
     * {@link WSExportWsdlConfigService#generateGvNIXWebServiceFiles()}
     * 
     * @return implementation classes
     */
    public List<JavaType> updateAnnotationsToGvNIX() {

        return wSExportWsdlConfigService.generateGvNIXWebServiceFiles();
    }

}
