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
package org.gvnix.web.mvc.jsp20.roo.addon;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * ControllerJsp20SupportPomListener's test case
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 *
 */
public class ControllerJsp20SupportPomListenerTest {

    private static final String POM_XML = "/tmp/pom.xml";

    // Class under test
    private ControllerJsp20SupportPomListener listener;

    // Operations class
    private ControllerJsp20SupportOperationsImpl operations;

    // Mock objects to emulate Roo OSGi Services
    private PathResolver pathResolver;
    private ComponentContext osgiComponentContext;
    private FileManager fileManager;
    private MetadataService metadataService;
    private ProjectOperations projectOperations;

    /**
     * Setup operations instance and Mock objects
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	listener = new ControllerJsp20SupportPomListener();

	operations = new ControllerJsp20SupportOperationsImpl();


	// Setup Mock service objects
	pathResolver = createMock(PathResolver.class);
	fileManager = createMock(FileManager.class);
	metadataService = createMock(MetadataService.class);
	pathResolver = createMock(PathResolver.class);
	projectOperations = createMock(ProjectOperations.class);

	osgiComponentContext = createMock(ComponentContext.class);

	// Inject mock objects in instance. This emulate OSGi environment
	ReflectionTestUtils.setField(listener, "pathResolver", pathResolver);
	ReflectionTestUtils.setField(listener, "operations", operations);

	// Inject mock objects in operations' instance. This emulate OSGi environment
	ReflectionTestUtils.setField(operations, "fileManager", fileManager);
	ReflectionTestUtils.setField(operations, "metadataService",
		metadataService);
	ReflectionTestUtils.setField(operations, "pathResolver", pathResolver);
	ReflectionTestUtils.setField(operations, "projectOperations",
		projectOperations);


	// Call activate OSGi method
	expect(pathResolver.getIdentifier(Path.ROOT, "/pom.xml")).andReturn(
		POM_XML);
	replay(pathResolver);
	listener.activate(osgiComponentContext);
	verify(pathResolver);
	reset(pathResolver);

	// Call operations' activate OSGi method
	expect(
		pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
			"WEB-INF/web.xml")).andReturn(ControllerJsp20SupportOperationsImplTest.TMP_WEB_XML);
	replay(pathResolver);
	operations.activate(osgiComponentContext);
	verify(pathResolver);
	reset(pathResolver);
    }

    /**
     * Test method for
     * {@link org.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportPomListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)}
     *  when file event is null.
     *
     *
     */
    @Test
    public void testOnFileEventEventNull() {
	// Check null FileEvent
	Exception ex = null;
	replay();
	try {
	    listener.onFileEvent(null);
	} catch (Exception e) {
	    ex = e;
	}
	assertNotNull(ex);
	reset();
	ex = null;

    }

    /**
     * Test method for
     * {@link org.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportPomListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)}
     *  when operations isn't activated and file matches.
     */
    @Test
    public void testOnFileEventEventNotActivated() {
	ControllerJsp20SupportOperationsImplTest.prepareMocksForIsActivatedFalse(fileManager,metadataService,projectOperations,pathResolver);
	listener.onFileEvent(getFileEventMatchUpdate());
	reset();

    }

    /**
     * Test method for
     * {@link org.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportPomListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)}
     * when operations is activated and no match file.
     */
    @Test
    public void testOnFileEventEventActivatedNoMatch() {
	ControllerJsp20SupportOperationsImplTest.prepareMocksForIsActivatedTrue(fileManager,metadataService,projectOperations,pathResolver);
	listener.onFileEvent(getFileEventNoMatch());
	reset(fileManager,metadataService);
    }

    /**
     * Test method for
     * {@link org.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportPomListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)}
     * when operations is activated and file matches in a finish-monitoring event.
     */
    @Test
    public void testOnFileEventEventActivatedAndMatchFinishMonitoring() {
	ControllerJsp20SupportOperationsImplTest.prepareMocksForIsActivatedTrue(fileManager,metadataService,projectOperations,pathResolver);
	listener.onFileEvent(getFileEventMatchMonitoringFinish());
	verify(projectOperations);
	reset(projectOperations, metadataService, fileManager);


    }



    /**
     * Test method for
     * {@link org.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportPomListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)}
     * when operations is activated and file matches in a start-monitoring event.
     */
    @Test
    public void testOnFileEventEventActivatedAndMatchStartMonitoring() {
	ControllerJsp20SupportOperationsImplTest.prepareMocksForActiveOperationAlreadyActivated(fileManager,metadataService,projectOperations,pathResolver);
	listener.onFileEvent(getFileEventMatchMonitoringStart());
	verify(projectOperations);
	reset(projectOperations, metadataService, fileManager);
    }


    /**
     * Test method for
     * {@link org.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportPomListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)}
     * when operations is activated and file matches in a update event.
     */
    @Test
    public void testOnFileEventEventActivatedAndMatchUpdate() {
	ControllerJsp20SupportOperationsImplTest.prepareMocksForActiveOperationAlreadyActivated(fileManager,metadataService,projectOperations,pathResolver);
	listener.onFileEvent(getFileEventMatchUpdate());
	verify(projectOperations);
	reset(projectOperations, metadataService, fileManager);
    }

    // *************************************************************
    // *************************************************************
    // *************************************************************
    // Utility methods
    // *************************************************************


    private FileEvent getFileEventNoMatch() {
	return new FileEvent(new FileDetails(new File("/tmp/x.pom"), System
		.currentTimeMillis()), FileOperation.UPDATED, null);
    }

    private FileEvent getFileEventMatchMonitoringFinish() {
	return new FileEvent(new FileDetails(new File(POM_XML), System
		.currentTimeMillis()), FileOperation.MONITORING_FINISH, null);
    }

    private FileEvent getFileEventMatchMonitoringStart() {
	return new FileEvent(new FileDetails(new File(POM_XML), System
		.currentTimeMillis()), FileOperation.MONITORING_START, null);
    }

    private FileEvent getFileEventMatchUpdate() {
	return new FileEvent(new FileDetails(new File(POM_XML), System
		.currentTimeMillis()), FileOperation.UPDATED, null);

    }

}
