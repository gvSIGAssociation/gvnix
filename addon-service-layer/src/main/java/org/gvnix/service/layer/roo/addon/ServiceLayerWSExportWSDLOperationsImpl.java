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

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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

    private static Logger logger = Logger
            .getLogger(ServiceLayerWSExportWSDLOperationsImpl.class.getName());

    public boolean isProjectAvailable() {

        return serviceLayerWsConfigService.isProjectAvailable();
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void exportWSDL(String url) {

        // 1) TODO: Check if WSDL is RPC enconded and copy file to project.
        Document wsdl = checkWSDLFile(url);

        // 2) TODO: Configure plugin cxf to generate java code using WSDL.
        serviceLayerWsConfigService.addImportLocation(url, CommunicationSense.EXPORT_WSDL);
        
        // 3) TODO: Run maven generate-sources command.

        // 4) TODO: Check generate classes.

        // 5) TODO: Convert java classes with gvNIX annotations.

    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Check if WSDL is RPC Encoded.
     * </p>
     * 
     * <p>
     * If WSDL is Document/Literal return Xml Document from WSDl.
     * </p>
     */
    public Document checkWSDLFile(String url) {

        Document wsdl = null;
        try {

            // Parse the wsdl location to a DOM document
            wsdl = XmlUtils.getDocumentBuilder().parse(url);
            Element root = wsdl.getDocumentElement();
            Assert.notNull(root, "No valid document format");

            if (WsdlParserUtils.isRpcEncoded(root)) {

                // TODO: No RPC Encoded WSDL. log message.
                logger
                        .warning("This Wsdl '"
                                + url
                                + "' is RPC Encoded and is not supported by the Add-on.");
                return null;
            }

        } catch (SAXException e) {

            Assert.state(false,
                    "The format of the web service to import has errors");

        } catch (IOException e) {

            Assert.state(false,
                    "There is no connection to the web service to import");
        }

        return wsdl;
    }

}
