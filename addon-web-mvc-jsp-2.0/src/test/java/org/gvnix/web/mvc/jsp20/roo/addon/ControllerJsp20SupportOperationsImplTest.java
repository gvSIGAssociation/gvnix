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


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * ControllerJsp20SupportOperationsImpl's test case
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 *
 */
@RunWith(JUnit4.class)
public class ControllerJsp20SupportOperationsImplTest {

    static final String TMP_WEB_XML = "/tmp/web.xml";

    // Class under test
    private ControllerJsp20SupportOperationsImpl operations;

    // Mock objects to emulate Roo OSGi Services
    private FileManager fileManager;
    private MetadataService metadataService;
    private PathResolver pathResolver;
    private ProjectOperations projectOperations;
    private ComponentContext osgiComponentContext;

    /**
     * Setup operations instance and Mock objects
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	// Class under test
	operations = new ControllerJsp20SupportOperationsImpl();

	// Setup Mock service objects
	fileManager = createMock(FileManager.class);
	metadataService = createMock(MetadataService.class);
	pathResolver = createMock(PathResolver.class);
	projectOperations = createMock(ProjectOperations.class);
	osgiComponentContext = createMock(ComponentContext.class);

	// Inject mock objects in instance. This emulate OSGi environment
	ReflectionTestUtils.setField(operations, "fileManager", fileManager);
	ReflectionTestUtils.setField(operations, "metadataService",
		metadataService);
	ReflectionTestUtils.setField(operations, "pathResolver", pathResolver);
	ReflectionTestUtils.setField(operations, "projectOperations",
		projectOperations);

	// Call activate OSGi method
	expect(
		pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
			"WEB-INF/web.xml")).andReturn(TMP_WEB_XML);
	replay(pathResolver);
	operations.activate(osgiComponentContext);
	verify(pathResolver);
	reset(pathResolver);
    }

    /**
     * Checks method
     * {@link ControllerJsp20SupportOperationsImpl#isActiveSupportAvailable()}
     *
     */
    @Test
    public void testIsAvailable() {
	// No web project: isActiveSupportAvailable() == false
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(getProyectMetadataWithOutWeb(pathResolver));
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.FALSE)
		.anyTimes();
	replay(metadataService, fileManager);
	assertFalse(operations.isActiveSupportAvailable());
	reset(metadataService, fileManager);
	operations.projectValuesChanged(); // Clears the state cache

	// Web project without support installed:
	// isActiveSupportAvailable() == true
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(getProyectMetadataWithWeb(pathResolver));
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();
	replay(metadataService, fileManager);
	assertTrue(operations.isActiveSupportAvailable());
	reset(metadataService, fileManager);
	operations.projectValuesChanged(); // Clears the state cache

	// Web project with support installed:
	// isActiveSupportAvailable() == false
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(getProyectMetadataWithSupportInstalled(pathResolver));
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();
	replay(metadataService, fileManager);
	assertFalse(operations.isActiveSupportAvailable());
	reset(metadataService, fileManager);
	operations.projectValuesChanged(); // Clears the state cache
    }

    /**
     * Checks method {@link ControllerJsp20SupportOperationsImpl#isActivated()}
     *
     */
    @Test
    public void testIsActivated() {
	// No web project: isActivated() == false
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(getProyectMetadataWithOutWeb(pathResolver));
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.FALSE)
		.anyTimes();
	replay(metadataService, fileManager);
	assertFalse(operations.isActivated());
	reset(metadataService, fileManager);
	operations.projectValuesChanged(); // Clears the state cache

	// Web project without support installed: isActivated() == false
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(getProyectMetadataWithWeb(pathResolver));
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();
	replay(metadataService, fileManager);
	assertFalse(operations.isActivated());
	reset(metadataService, fileManager);
	operations.projectValuesChanged(); // Clears the state cache

	// Web project with support installed: isActivated() == true
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(getProyectMetadataWithSupportInstalled(pathResolver));
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();
	replay(metadataService, fileManager);
	assertTrue(operations.isActivated());
	reset(metadataService, fileManager);
	operations.projectValuesChanged(); // Clears the state cache
    }

    /**
     * Checks method
     * {@link ControllerJsp20SupportOperationsImpl#activeJSP20Support()}
     *
     */
    @Test
    public void testActiveJSP20Support() {
	prepareMocksForActiveOperation(fileManager, metadataService, projectOperations, pathResolver);
	operations.activeJSP20Support();
	verify(projectOperations);
	reset(projectOperations, metadataService, fileManager);

    }

    /**
     * Checks method with already activated configuration
     * {@link ControllerJsp20SupportOperationsImpl#activeJSP20Support()}
     *
     */
    @Test
    public void testActiveJSP20SupportAlredyActivated() {
	prepareMocksForActiveOperationAlreadyActivated(fileManager, metadataService, projectOperations, pathResolver);
	operations.activeJSP20Support();
	verify(projectOperations);
	reset(projectOperations, metadataService, fileManager);

    }

    // *************************************************************
    // *************************************************************
    // *************************************************************
    // Utility methods
    // *************************************************************

    /**
     * Prepares mocks for {@link ControllerJsp20SupportOperationsImpl#isActivated()} == false method call
     */
    static void prepareMocksForIsActivatedFalse(FileManager fileManager, MetadataService metadataService, ProjectOperations projectOperations, PathResolver pathResolver) {
	expect(fileManager.scan()).andStubReturn(1);
	ProjectMetadata projectMetadata =getProyectMetadataWithWeb(pathResolver);
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(projectMetadata).anyTimes();
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();

	replay(projectOperations, metadataService, fileManager);
    }


    /**
     * Prepares mocks for {@link ControllerJsp20SupportOperationsImpl#isActivated()} == true method call
     */
    static void prepareMocksForIsActivatedTrue(FileManager fileManager, MetadataService metadataService, ProjectOperations projectOperations, PathResolver pathResolver) {
	expect(fileManager.scan()).andStubReturn(1);
	ProjectMetadata projectMetadata = getProyectMetadataWithSupportInstalled(pathResolver);
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
		.andReturn(projectMetadata).anyTimes();
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();

	replay(projectOperations, metadataService, fileManager);
    }

    /**
     * Prepares mocks for {@link ControllerJsp20SupportOperationsImpl#activeJSP20Support()} method call
     */
    static void prepareMocksForActiveOperation(FileManager fileManager, MetadataService metadataService, ProjectOperations projectOperations, PathResolver pathResolver) {
	expect(fileManager.scan()).andStubReturn(1);
	ProjectMetadata projectMetadata = getProyectMetadataWithWeb(pathResolver);
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
	.andReturn(projectMetadata).anyTimes();
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();

	// Servlet 2.4
	projectOperations
		.dependencyUpdate(ControllerJsp20SupportOperationsImpl.DEPENDENCY_SERVLET_2_4);

	// JSTL
	projectOperations
		.dependencyUpdate(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_1_1);

	// JSTL Impl
	projectOperations
		.dependencyUpdate(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_1_1_IMPL);

	// Remove EL

	projectOperations.removeDependency(ControllerJsp20SupportOperationsImpl.DEPENDENCY_EL_API_1_2);

	projectOperations.addBuildPlugin(isA(Plugin.class));

	replay(projectOperations, metadataService, fileManager);
    }


    /**
     * Prepares mocks for {@link ControllerJsp20SupportOperationsImpl#activeJSP20Support()} method call
     */
    static void prepareMocksForActiveOperationAlreadyActivated(FileManager fileManager, MetadataService metadataService, ProjectOperations projectOperations, PathResolver pathResolver) {
	expect(fileManager.scan()).andStubReturn(1);
	ProjectMetadata projectMetadata = getProyectMetadataWithSupportInstalled(pathResolver);
	expect(metadataService.get(ProjectMetadata.getProjectIdentifier()))
	.andReturn(projectMetadata).anyTimes();
	expect(fileManager.exists(TMP_WEB_XML)).andReturn(Boolean.TRUE)
		.anyTimes();

	// Servlet 2.4
	projectOperations
		.dependencyUpdate(ControllerJsp20SupportOperationsImpl.DEPENDENCY_SERVLET_2_4);

	// JSTL
	projectOperations
		.dependencyUpdate(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_1_1);

	// JSTL Impl
	projectOperations
		.dependencyUpdate(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_1_1_IMPL);

	projectOperations.addBuildPlugin(isA(Plugin.class));

	replay(projectOperations, metadataService, fileManager);
    }


    static ProjectMetadata createProyectMetadata(Set<Dependency> dependencies,
	    Set<Plugin> plugins,PathResolver pathResolver) {

	return new ProjectMetadata(new JavaPackage("unit.test.addons"),
		"unit-test", dependencies, plugins, new HashSet<Repository>(),
		new HashSet<Repository>(), new HashSet<Property>(),
		pathResolver);
    }

    static ProjectMetadata getProyectMetadataWithOutWeb(PathResolver pathResolver) {
	return createProyectMetadata(new HashSet<Dependency>(),
		new HashSet<Plugin>(),pathResolver);
    }

    static ProjectMetadata getProyectMetadataWithWeb(PathResolver pathResolver) {
	HashSet<Dependency> dependencies = new HashSet<Dependency>();
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_SERVLET_2_5);
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_2);
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_EL_API_1_2);

	return createProyectMetadata(dependencies,
		new HashSet<Plugin>(),pathResolver);
    }

    static ProjectMetadata getProyectMetadataWithWebWithoutEL(PathResolver pathResolver) {
	HashSet<Dependency> dependencies = new HashSet<Dependency>();
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_SERVLET_2_5);
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_2);

	return createProyectMetadata(dependencies,
		new HashSet<Plugin>(),pathResolver);
    }

    static ProjectMetadata getProyectMetadataWithSupportInstalled(PathResolver pathResolver) {
	HashSet<Dependency> dependencies = new HashSet<Dependency>();
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_SERVLET_2_4);
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_1_1);
	dependencies
		.add(ControllerJsp20SupportOperationsImpl.DEPENDENCY_JSTL_1_1_1_IMPL);

	HashSet<Plugin> plugins = new HashSet<Plugin>();
	plugins.add(ControllerJsp20SupportOperationsImpl.CARGO_PLUGIN);

	return createProyectMetadata(dependencies, plugins, pathResolver);
    }

}
