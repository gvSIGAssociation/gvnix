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
package org.gvnix.web.exception.handler.roo.addon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.unitils.UnitilsJUnit4;
import org.unitils.inject.annotation.InjectIntoByType;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.Mock;

/**
 * WebExceptionHandlerOperationsImpl's test case
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
public class WebExceptionHandlerOperationsImplUnitilsTest extends UnitilsJUnit4 {

    static final String EXC_WEB_XML = "exceptions-webmvc-config.xml";
    static final String NO_EXC_WEB_XML = "no-exceptions-webmvc-config.xml";

    // Project web config file path
    static final String WEB_XML_PATH = "WEB-INF/spring/webmvc-config.xml";

    // jspx Exceptions path
    static final String EXC_JSPX_PATH = "src/main/webapp/WEB-INF/views/";

    //Class under test
    @TestedObject
    private WebExceptionHandlerOperationsImpl webExceptionHandlerOperationsImpl;

    // Mock objects to emulate Roo OSGi Services
    @InjectIntoByType
    private Mock<FileManager> fileManager;
    @InjectIntoByType
    private Mock<MetadataService> metadataService;
    @InjectIntoByType
    private Mock<PathResolver> pathResolver;
    @InjectIntoByType
    private Mock<PropFileOperations> propFileOperations;

    // Mock to emulate file management.
    private Mock<MutableFile> webXmlMutableFile;

    private static Logger logger = Logger
	    .getLogger(WebExceptionHandlerOperationsImplUnitilsTest.class.getName());

    /**
     * Checks method
     * {@link WebExceptionHandlerOperationsImpl#getHandledExceptionList()}
     * 
     * @throws Exception
     */
    @Test
    public void testGetHandledExceptionList() throws Exception {

	String result;
	String expected;

	/*
	 * Test 1 - Encuentra excepciones instanciadas en el archivo de
	 * configuración EXC_WEB_XML
	 */
	pathResolver.returns(EXC_WEB_XML).getIdentifier(Path.SRC_MAIN_WEBAPP,
		WEB_XML_PATH);

	fileManager.returns(true).exists(EXC_WEB_XML);

	fileManager.returns(webXmlMutableFile).updateFile(EXC_WEB_XML);

	webXmlMutableFile.returns(getClass().getResourceAsStream(EXC_WEB_XML))
		.getInputStream();

	expected = "Handled Exceptions:\n" +
			".DataAccessException\n" +
			".NoSuchRequestHandlingMethodException\n" +
			".TypeMismatchException\n" +
			".MissingServletRequestParameterException\n";
	
	result = webExceptionHandlerOperationsImpl.getHandledExceptionList();
	
	assertTrue("Test 1 \nThere aren't exceptions defined in " + EXC_WEB_XML
		+ " file", expected.equals(result));

	logger.log(Level.INFO, "Test 1 \nExceptions defined in " + EXC_WEB_XML
		+ " :\n"
		+ result);

	/*
	 * Test 2 - No encuentra excepciones instanciadas en el archivo de
	 * configuración NO_EXC_WEB_XML
	 */

	// Reset el comportamiento del Mock
	webXmlMutableFile.resetBehavior();

	pathResolver.returns(NO_EXC_WEB_XML).getIdentifier(
		Path.SRC_MAIN_WEBAPP, NO_EXC_WEB_XML);

	fileManager.returns(true).exists(NO_EXC_WEB_XML);

	fileManager.returns(webXmlMutableFile).updateFile(NO_EXC_WEB_XML);

	webXmlMutableFile.returns(
		getClass().getResourceAsStream(NO_EXC_WEB_XML))
		.getInputStream();

	expected = "Handled Exceptions:\n";

	result = webExceptionHandlerOperationsImpl.getHandledExceptionList();

	assertEquals("Test 2 \nThere are exceptions defined in "
		+ NO_EXC_WEB_XML + " file", expected, result);

	logger.log(Level.INFO, "Test 2 \nThere aren't exceptions defined in "
		+ NO_EXC_WEB_XML);

    }

}
