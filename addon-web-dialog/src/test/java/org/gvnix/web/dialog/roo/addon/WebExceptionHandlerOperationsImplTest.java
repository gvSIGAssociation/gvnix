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
package org.gvnix.web.dialog.roo.addon;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gvnix.web.dialog.roo.addon.WebExceptionHandlerOperationsImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * WebExceptionHandlerOperationsImpl's test case
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
public class WebExceptionHandlerOperationsImplTest {

    static final String EXC_WEB_XML = "exceptions-webmvc-config.xml";
    static final String NO_EXC_WEB_XML = "no-exceptions-webmvc-config.xml";

    // Project web config file path
    static final String WEB_XML_PATH = "WEB-INF/spring/webmvc-config.xml";

    // jspx Exceptions path
    static final String EXC_JSPX_PATH = "src/main/webapp/WEB-INF/views/";

    //Class under test
    private WebExceptionHandlerOperationsImpl webExceptionHandlerOperationsImpl;

    // Mock objects to emulate Roo OSGi Services
    private FileManager fileManager;
    private MetadataService metadataService;
    private PathResolver pathResolver;
    private PropFileOperations propFileOperations;

    // Mock to emulate file management.
    private MutableFile webXmlMutableFile;

    private static Logger logger = Logger
	    .getLogger(WebExceptionHandlerOperationsImplTest.class.getName());

    /**
     * Setup operations instance and Mock objects
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

	// Class under test
	webExceptionHandlerOperationsImpl = new WebExceptionHandlerOperationsImpl();

	// Setup Mock service objects
	fileManager = createMock(FileManager.class);
	metadataService = createMock(MetadataService.class);
	pathResolver = createMock(PathResolver.class);
	propFileOperations = createMock(PropFileOperations.class);

	// Mock Objects
	webXmlMutableFile = createMock(MutableFile.class);

	// Inject mock objects in instance. This emulate OSGi environment
	ReflectionTestUtils.setField(webExceptionHandlerOperationsImpl, "fileManager", fileManager);
	ReflectionTestUtils.setField(webExceptionHandlerOperationsImpl, "metadataService",
		metadataService);
	ReflectionTestUtils.setField(webExceptionHandlerOperationsImpl, "pathResolver", pathResolver);
	ReflectionTestUtils.setField(webExceptionHandlerOperationsImpl, "propFileOperations",
		propFileOperations);

    }

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
	expect(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, WEB_XML_PATH))
		.andReturn(EXC_WEB_XML);

	expect(fileManager.exists(EXC_WEB_XML)).andReturn(true);

	expect(fileManager.updateFile(EXC_WEB_XML))
		.andReturn(webXmlMutableFile);

	expect(webXmlMutableFile.getInputStream()).andReturn(
		getClass().getResourceAsStream(EXC_WEB_XML));

	replay(pathResolver, fileManager, webXmlMutableFile);
	
	result = webExceptionHandlerOperationsImpl.getHandledExceptionList();
	
	assertTrue("Test 1 \nThere aren't exceptions defined in " + EXC_WEB_XML
		+ " file",
		result != null);
	
	logger.log(Level.INFO, "Test 1 \nExceptions defined in " + EXC_WEB_XML
		+ " :\n"
		+ result);

	reset(pathResolver, fileManager, webXmlMutableFile);

	/*
	 * Test 2 - No encuentra excepciones instanciadas en el archivo de
	 * configuración NO_EXC_WEB_XML
	 */
	expect(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, WEB_XML_PATH))
		.andReturn(NO_EXC_WEB_XML);

	expect(fileManager.exists(NO_EXC_WEB_XML)).andReturn(true);

	expect(fileManager.updateFile(NO_EXC_WEB_XML))
		.andReturn(webXmlMutableFile);

	expect(webXmlMutableFile.getInputStream()).andReturn(
		getClass().getResourceAsStream(NO_EXC_WEB_XML));

	replay(pathResolver, fileManager, webXmlMutableFile);

	result = webExceptionHandlerOperationsImpl.getHandledExceptionList();

	expected = "Handled Exceptions:\n";

	assertEquals("Test 2 \nThere are exceptions defined in "
		+ NO_EXC_WEB_XML
		+ " file", expected, result);

	logger.log(Level.INFO, "Test 2 \nThere aren't exceptions defined in "
		+ NO_EXC_WEB_XML);

	reset(pathResolver, fileManager, webXmlMutableFile);

    }

    /**
     * Checks method
     * {@link WebExceptionHandlerOperationsImpl#updateWebMvcConfig()}
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateWebMvcConfig() throws Exception {

	String result;
	String expected;

	String exceptionName;
	String exceptionJspxPath;
	
	/*
	 * Test 1 - Añade una excepción al archivo de configuración WEB_XML_PATH
	 * 
	 * exceptionName - java.lang.Exception
	 */

	exceptionName = "java.lang.Exception";
	expected = "Exception";

	exceptionJspxPath = EXC_JSPX_PATH.concat(
		StringUtils.uncapitalize(expected)).concat(".jspx");
	
	expect(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, WEB_XML_PATH))
		.andReturn(EXC_WEB_XML);

	expect(fileManager.exists(EXC_WEB_XML)).andReturn(true);

	expect(fileManager.updateFile(EXC_WEB_XML))
		.andReturn(webXmlMutableFile);

	expect(webXmlMutableFile.getInputStream()).andReturn(
		getClass().getResourceAsStream(EXC_WEB_XML));

	// Search for an existing Exception jspx mapping
	expect(
		pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
			"WEB-INF/views/" + StringUtils.uncapitalize(expected)
				+ ".jspx")).andReturn(exceptionJspxPath);

	expect(fileManager.exists(exceptionJspxPath)).andReturn(false);

	// Output Stream
	expect(webXmlMutableFile.getOutputStream()).andReturn(
		new FileOutputStream("/tmp/exceptions-webmvc-config.xml"));

	replay(pathResolver, fileManager, webXmlMutableFile);

	result = webExceptionHandlerOperationsImpl
		.updateWebMvcConfig(exceptionName);

	assertEquals("Test 1 \nThere isn't new exception defined in "
		+ EXC_WEB_XML + " file", StringUtils.uncapitalize(expected),
		result);

	logger.log(Level.INFO, "Test 1 \nNew Exception view: '" + result
		+ "' defined in: " + EXC_WEB_XML);

	reset(pathResolver, fileManager, webXmlMutableFile);

    }
}
