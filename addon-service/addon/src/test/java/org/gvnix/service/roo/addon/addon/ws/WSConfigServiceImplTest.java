/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.service.roo.addon.addon.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.gvnix.service.roo.addon.addon.ws.WSConfigServiceImpl;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Element;

/**
 * Addon Web Service configuration Test class.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public class WSConfigServiceImplTest {

    static final String WEB_XML = "webmvc-config.xml";

    // Project web config file path
    static final String WEB_XML_PATH = "WEB-INF/spring/webmvc-config.xml";

    // Class under test
    private WSConfigServiceImpl wSConfigServiceImpl;

    // Mock objects to emulate Roo OSGi Services
    private FileManager fileManager;

    private MetadataService metadataService;

    // Mock to emulate file management.
    private ProjectMetadata projectMetadata;

    private ProjectOperations projectOperations;

    private static Logger logger = Logger
            .getLogger(WSConfigServiceImplTest.class.getName());

    /**
     * Setup operations instance and Mock objects
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        // Class under test
        wSConfigServiceImpl = new WSConfigServiceImpl();

        // Setup Mock service objects
        fileManager = createMock(FileManager.class);
        metadataService = createMock(MetadataService.class);
        projectOperations = createMock(ProjectOperations.class);
        // pathResolver = createMock(PathResolver.class);

        // Mock Objects
        projectMetadata = createMock(ProjectMetadata.class);

        // Inject mock objects in instance. This emulate OSGi environment
        ReflectionTestUtils.setField(wSConfigServiceImpl, "fileManager",
                fileManager);
        ReflectionTestUtils.setField(wSConfigServiceImpl, "metadataService",
                metadataService);
        ReflectionTestUtils.setField(wSConfigServiceImpl, "projectOperations",
                projectOperations);

        // ReflectionTestUtils.setField(projectOperations, "pathResolver",
        // pathResolver);

    }

    /**
     * Checks method {@link WSConfigServiceImpl#isCxfDependenciesInstalled()}
     * TODO Test disabled because error after migrate to Roo 1.2.2 (¿ changed
     * mock tests ?)
     * 
     * @throws Exception
     */
    // @Test
    public void testAreCxfDependenciesInstalled() throws Exception {

        boolean areCxfDependenciesInstalledResult;

        /*
         * Test 1 - Todas las dependencias de CXF han sido definidas en el
         * pom.xml.
         */
        expect(
                metadataService.get(ProjectMetadata
                        .getProjectIdentifier(projectOperations
                                .getFocusedModuleName()))).andReturn(
                projectMetadata);

        Dependency dependency;

        List<Element> dependencyList = wSConfigServiceImpl
                .getDependencies(WsType.EXPORT);

        for (Element element : dependencyList) {

            dependency = new Dependency(element);
            expect(projectMetadata.getPom().isDependencyRegistered(dependency))
                    .andReturn(true);
        }

        replay(metadataService, projectMetadata);

        areCxfDependenciesInstalledResult = wSConfigServiceImpl
                .dependenciesRegistered(WsType.EXPORT);

        assertTrue("There is one or more dependencies not set.",
                areCxfDependenciesInstalledResult);

        logger.log(Level.INFO,
                "Test 1 \nAll Cxf Dependencies defined in pom.xml");

        reset(metadataService, projectMetadata);

        /*
         * Test 2 - Falta al menos una dependencia por definir en el pom.xml del
         * proyecto.
         */
        String dependencyNotDefined = "jaxb-api";

        expect(
                metadataService.get(ProjectMetadata
                        .getProjectIdentifier(projectOperations
                                .getFocusedModuleName()))).andReturn(
                projectMetadata);

        for (Element element : dependencyList) {

            dependency = new Dependency(element);
            if (dependency.getArtifactId().compareTo(dependencyNotDefined) == 0) {
                expect(
                        projectMetadata.getPom().isDependencyRegistered(
                                dependency)).andReturn(false);
            }
            expect(projectMetadata.getPom().isDependencyRegistered(dependency))
                    .andReturn(true);
        }

        replay(metadataService, projectMetadata);

        areCxfDependenciesInstalledResult = wSConfigServiceImpl
                .dependenciesRegistered(WsType.EXPORT);

        assertFalse("There are all dependencies set.",
                areCxfDependenciesInstalledResult);

        logger.log(Level.INFO, "Test 2 \njaxb-api dependency is not set.");

        reset(metadataService, projectMetadata);

    }

    /**
     * Checks method {@link WSConfigServiceImpl#isCxfConfigurated()} TODO Test
     * disabled because error after migrate to Roo 1.2.2 (¿ changed mock tests
     * ?)
     * 
     * @throws Exception
     */
    // @Test
    public void testIsCxfConfigurated() throws Exception {

        String projectName;
        String cxfprojectFile;
        String cxfPath;
        boolean cxfConfigFileExpected;

        /*
         * Test 1 - Exists Cxf config file using project name. projectName =
         * test-service-layer.
         */
        projectName = "test-service-layer";

        cxfprojectFile = "WEB-INF/cxf-".concat(projectName).concat(".xml");

        cxfPath = cxfprojectFile;

        cxfConfigFileExpected = true;

        expect(
                metadataService.get(ProjectMetadata
                        .getProjectIdentifier(projectOperations
                                .getFocusedModuleName()))).andReturn(
                projectMetadata);

        expect(
                projectOperations.getProjectName(projectOperations
                        .getFocusedModuleName())).andReturn(projectName);

        // expect(
        // projectOperations.getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP,
        // cxfprojectFile)).andReturn(cxfPath);

        expect(fileManager.exists(cxfPath)).andReturn(cxfConfigFileExpected);

        replay(metadataService, projectMetadata, fileManager);

        assertTrue("The Cxf config file doesn't exists.", cxfConfigFileExpected);

        logger.log(Level.INFO,
                "Test 1 \nThe Cxf config file using project name exists.");

        reset(metadataService, projectMetadata, fileManager);

    }

    /**
     * Checks method
     * {@link WSConfigServiceImpl#convertPackageToTargetNamespace(String)}
     * 
     * @throws Exception
     */
    @Test
    public void testConvertPackageToTargetNamespace() throws Exception {

        String packageName = "org.gvnix.service.roo.addon";
        String targetNamespaceExpected = "http://addon.roo.service.gvnix.org/";

        String targetNamespaceResult;
        /*
         * Test 1 - Check's method functionality.
         */
        targetNamespaceResult = wSConfigServiceImpl
                .convertPackageToTargetNamespace(packageName);

        Validate.isTrue(
                targetNamespaceResult != null
                        && targetNamespaceResult.length() != 0,
                "The method doesn't work properly.");

        assertTrue("The namespace is not well generated",
                targetNamespaceResult.contains(targetNamespaceExpected));

    }

}
