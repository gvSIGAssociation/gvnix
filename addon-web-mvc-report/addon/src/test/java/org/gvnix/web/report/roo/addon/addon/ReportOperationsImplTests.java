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

package org.gvnix.web.report.roo.addon.addon;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */

public class ReportOperationsImplTests {
    private static final String WEB_MVC_CONFIG = "/org/gvnix/web/report/roo/addon/src/test/resources/webmvc-config.xml";
    @Mock
    private FileManager fileManager;
    @Mock
    private ProjectOperations projectOperations;

    @Before
    public void setUp() {
        initMocks(this);
        // operations = new ReportOperationsImpl(fileManager, projectOperations,
        // typeLocationService, metadataService, physicalTypeMetadataProvider);
    }

    @Test
    public void testAddJasperReportsViewResolver() {
        String webMvcConfig = WEB_MVC_CONFIG;
        PathResolver pathResolver = projectOperations.getPathResolver();
        when(
                pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "WEB-INF/spring/webmvc-config.xml")).thenReturn(
                webMvcConfig);
        StubMutableFile webMvcConfigFile = new StubMutableFile(new File(
                getClass().getResource(webMvcConfig).getFile()));
        when(fileManager.exists(webMvcConfig)).thenReturn(true);
        when(fileManager.updateFile(webMvcConfig)).thenReturn(webMvcConfigFile);

        // operations.addJasperReportsViewResolver();
        String output = webMvcConfigFile.getOutputAsString();
        assertThat(
                output,
                containsString("<bean id=\"jasperReportsXmlViewResolver\" class=\"org.springframework.web.servlet.view.XmlViewResolver\" p:location=\"/WEB-INF/spring/jasper-views.xml\" p:order=\"0\" />"));
        assertThat(output,
                containsString("<import resource=\"jasper-views.xml\" />"));

    }
}
