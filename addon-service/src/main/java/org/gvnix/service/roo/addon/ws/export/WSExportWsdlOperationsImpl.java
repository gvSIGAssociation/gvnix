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
package org.gvnix.service.roo.addon.ws.export;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
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
    public List<JavaType> exportWsdl(String url) {

        // Create WSDL javas with wsdl2java plugin at GENERATED_CXF_SOURCES_DIR
        wSExportWsdlConfigService.generateJavaFromWsdl(url);

        // Monitoring GENERATED_CXF_SOURCES_DIR created files
        wSExportWsdlConfigService.monitorFolder(GENERATED_CXF_SOURCES_DIR);

        // Generate gvNIX javas into src folder from monitored files
        return wSExportWsdlConfigService.createGvNixClasses();
    }

}
