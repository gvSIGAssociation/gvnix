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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Element;

/**
 * Addon operations Test class.
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class GvNixServiceLayerOperationsImplTest {

    static final String WEB_XML = "webmvc-config.xml";

    // Project web config file path
    static final String WEB_XML_PATH = "WEB-INF/spring/webmvc-config.xml";

    // Class under test
    private GvNixServiceLayerOperationsImpl gvNixServiceLayerOperationsImpl;

    // Mock objects to emulate Roo OSGi Services
    private FileManager fileManager;
    private MetadataService metadataService;
    private PathResolver pathResolver;
    private ProjectOperations projectOperations;

    // Mock to emulate file management.
    private MutableFile webXmlMutableFile;
    private ProjectMetadata projectMetadata;
    private MetadataItem metadataItem;

    private static Logger logger = Logger
	    .getLogger(GvNixServiceLayerOperationsImplTest.class.getName());

    /**
     * Setup operations instance and Mock objects
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

	// Class under test
	gvNixServiceLayerOperationsImpl = new GvNixServiceLayerOperationsImpl();

	// Setup Mock service objects
	fileManager = createMock(FileManager.class);
	metadataService = createMock(MetadataService.class);
	pathResolver = createMock(PathResolver.class);
	projectOperations = createMock(ProjectOperations.class);

	// Mock Objects
	webXmlMutableFile = createMock(MutableFile.class);
	projectMetadata = createMock(ProjectMetadata.class);
	metadataItem = createMock(MetadataItem.class);

	// Inject mock objects in instance. This emulate OSGi environment
	ReflectionTestUtils.setField(gvNixServiceLayerOperationsImpl,
		"fileManager", fileManager);
	ReflectionTestUtils.setField(gvNixServiceLayerOperationsImpl,
		"metadataService", metadataService);
	ReflectionTestUtils.setField(gvNixServiceLayerOperationsImpl,
		"pathResolver", pathResolver);
	ReflectionTestUtils.setField(gvNixServiceLayerOperationsImpl,
		"projectOperations", projectOperations);

    }

    /**
     * Checks method
     * {@link GvNixServiceLayerOperationsImpl#areCxfDependenciesInstalled()}
     * 
     * @throws Exception
     */
    @Test
    public void testAreCxfDependenciesInstalled() throws Exception {

	boolean areCxfDependenciesInstalled;
	
	/*
	 * Test 1 - Todas las dependencias de CXF han sido definidas en el
	 * pom.xml.
	 */
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(projectMetadata);

	Dependency dependency;

	List<Element> dependencyList = gvNixServiceLayerOperationsImpl
		.getCxfDependencies();

	for (Element element : dependencyList) {

	    dependency = new Dependency(element);
	    expect(projectMetadata.isDependencyRegistered(dependency))
		    .andReturn(true);
	}

	replay(metadataService, projectMetadata);

	areCxfDependenciesInstalled = gvNixServiceLayerOperationsImpl.areCxfDependenciesInstalled();

	assertTrue("There is one or more depdencies not set.",
		areCxfDependenciesInstalled);
    }
}
