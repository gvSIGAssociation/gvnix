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

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * gvNix Wsdl parser utilities test.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WsdlParserUtilsTest {

    public static final String TEMPCONVERT_WSDL = "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL";

    /**
     * Checks method
     * {@link WsdlParserUtils#getLocalName()}
     * 
     * @throws Exception
     */
    @Test
    public void testGetLocalName() throws Exception {

	assertEquals("address", WsdlParserUtils.getLocalName("soap:address"));
    }
    
    /**
     * Checks method
     * {@link WsdlParserUtils#getNamespace()}
     * 
     * @throws Exception
     */
    @Test
    public void testGetNamespace() throws Exception {

	assertEquals("soap", WsdlParserUtils.getNamespace("soap:address"));
    }
    
    /**
     * Checks method
     * {@link WsdlParserUtils#getServiceClassPath()}
     * 
     * @throws Exception
     */
    @Test
    public void testGetServiceClassPath() throws Exception {
	
	Document wsdl = XmlUtils.getDocumentBuilder().parse(TEMPCONVERT_WSDL);
	Element root = wsdl.getDocumentElement();
	assertEquals("org.tempuri.TempConvert", WsdlParserUtils.getServiceClassPath(root));
    }
    
    /**
     * Checks method
     * {@link WsdlParserUtils#getPortTypeClassPath()}
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortTypeClassPath() throws Exception {
	
	Document wsdl = XmlUtils.getDocumentBuilder().parse(TEMPCONVERT_WSDL);
	Element root = wsdl.getDocumentElement();
	assertEquals("org.tempuri.TempConvertSoap", WsdlParserUtils.getPortTypeClassPath(root));
    }
    
}
