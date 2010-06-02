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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Operations Implementation for support JSP 2.0 in Roo projects
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * 
 */
@Component
@Service
public class ControllerJsp20SupportOperationsImpl implements
	ControllerJsp20SupportOperations {

    private static Logger logger = Logger
	    .getLogger(ControllerJsp20SupportOperations.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;

    // OSGi context
    private ComponentContext context;

    // Path to pom.xml file
    private String webXmlFileName;

    // is already activated the support?: null --> no checked
    private Boolean isActivated = null;

    // Informs if it's doing any operation on project at present
    private boolean working;

    // Dependencies

    // Servlet library
    static final Dependency DEPENDENCY_SERVLET_2_4 = new Dependency(
	    new JavaPackage("javax.servlet"),
	    new JavaSymbolName("servlet-api"), "2.4", DependencyType.JAR,
	    DependencyScope.PROVIDED);
    static final Dependency DEPENDENCY_SERVLET_2_5 = new Dependency(
	    new JavaPackage("javax.servlet"),
	    new JavaSymbolName("servlet-api"), "2.5", DependencyType.JAR,
	    DependencyScope.PROVIDED);

    // JSTL tag library (API and implementation)
    static final Dependency DEPENDENCY_JSTL_1_1_1 = new Dependency(
	    new JavaPackage("javax.servlet"), new JavaSymbolName("jstl"),
	    "1.1.1", DependencyType.JAR, DependencyScope.COMPILE);
    static final Dependency DEPENDENCY_JSTL_1_2 = new Dependency(
	    new JavaPackage("javax.servlet"), new JavaSymbolName("jstl"),
	    "1.2", DependencyType.JAR, DependencyScope.COMPILE);
    static final Dependency DEPENDENCY_JSTL_1_1_1_IMPL = new Dependency(
	    new JavaPackage("taglibs"), new JavaSymbolName("standard"),
	    "1.1.1", DependencyType.JAR, DependencyScope.COMPILE);

    // EL-API
    static final Dependency DEPENDENCY_EL_API_1_2 = new Dependency(
	    new JavaPackage("javax.el"), new JavaSymbolName("el-api"), "1.2",
	    DependencyType.JAR, DependencyScope.PROVIDED);

    // related dependencies used by dependency listener to identify if
    // dependencies
    // must be refresh
    static final List<Dependency> RELATED_DEPENDENCIES = Collections
	    .unmodifiableList(Arrays.asList(new Dependency[] {
		    DEPENDENCY_SERVLET_2_4, DEPENDENCY_SERVLET_2_5,
		    DEPENDENCY_JSTL_1_1_1, DEPENDENCY_JSTL_1_2,
		    DEPENDENCY_JSTL_1_1_1_IMPL }));

    // Plugin dependencies

    // Cargo maven plugin dependency
    static final Plugin CARGO_PLUGIN = new Plugin("org.codehaus.cargo",
	    "cargo-maven2-plugin", "1.0.1-alpha-2");
    // Cargo maven plugin dependency whit tomcat container configured
    private static Plugin CARGO_PLUGIN_TOMCAT = null;

    // /Cargo

    /*
     * Called on add-on implementation activation
     */
    protected void activate(ComponentContext context) {
	this.context = context;
	this.webXmlFileName = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/web.xml");
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.gvnix.web.mvc.jsp20.roo.addon.ControllerJsp20SupportOperations#
     * isActiveSupportAvailable()
     */
    public boolean isActiveSupportAvailable() {
	return !isActivated() && isWebProject();
    }

    /**
     * <p>
     * Checks if this is a web project
     * </p>
     *
     * <p>
     * This method caches value to best performance
     * </p>
     *
     * @see #projectValuesChanged()
     * @return
     */
    private boolean isWebProject() {
	return fileManager.exists(webXmlFileName);
    }

    /**
     * Notification that project information cached in this class cul This
     * method must be called when a project change is detected
     */
    protected void projectValuesChanged() {
	isActivated = null;
    }

    /**
     * <p>
     * Active support of JSP 2.0.
     * </p>
     *
     * <p>
     * This perform o refresh dependencies and active monitoring of dependencies
     * </p>
     */
    public void activeJSP20Support() {
	working = true;
	try {
	    fileManager.scan();
	    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		    .get(ProjectMetadata.getProjectIdentifier());

	    // Servlet 2.4
	    projectOperations.dependencyUpdate(DEPENDENCY_SERVLET_2_4);

	    // JSTL
	    projectOperations.dependencyUpdate(DEPENDENCY_JSTL_1_1_1);

	    // JSTL Impl
	    projectOperations.dependencyUpdate(DEPENDENCY_JSTL_1_1_1_IMPL);

	    // EL-API
	    if (projectMetadata.isDependencyRegistered(DEPENDENCY_EL_API_1_2)) {
		projectOperations.removeDependency(DEPENDENCY_EL_API_1_2);
	    }

	    // Cargo plugin
	    configureCargoPlugin(projectMetadata);

	    isActivated = Boolean.TRUE;
	} finally {
	    working = false;
	}
    }

    /**
     * Add or reconfigure cargo maven plug-in with a Tomcat 5.5 container
     *
     * @param projectMetadata
     */
    private void configureCargoPlugin(ProjectMetadata projectMetadata) {
	Plugin cargoPluginInstalled = null;
	for (Plugin tmpPlugin : projectMetadata.getBuildPlugin()) {
	    if (tmpPlugin.getGroupId().equals(CARGO_PLUGIN.getGroupId())
		    && tmpPlugin.getArtifactId().equals(
			    CARGO_PLUGIN.getArtifactId())) {
		cargoPluginInstalled = tmpPlugin;
		break;
	    }
	}
	Plugin cargoPlugin = getCargoPlugin();

	if (cargoPluginInstalled == null) {
	    projectOperations.addBuildPlugin(cargoPlugin);
	    return;
	}

	// Compare dependencies
	if (cargoPluginInstalled.getDependencies() == null
		&& cargoPlugin.getDependencies() != null) {
	    projectOperations.addBuildPlugin(cargoPlugin);
	    return;
	}

	if (cargoPluginInstalled.getDependencies().size() < cargoPlugin
		.getDependencies().size()) {
	    projectOperations.addBuildPlugin(cargoPlugin);
	    return;
	}

	// Compare configuration
	if (cargoPluginInstalled.getConfiguration() == null
		&& cargoPlugin.getConfiguration() != null) {
	    projectOperations.addBuildPlugin(cargoPlugin);
	    return;
	}
    }

    /**
     * Gets cargo plug-in definition object
     *
     * @return
     */
    private Plugin getCargoPlugin() {
	if (CARGO_PLUGIN_TOMCAT == null) {
	    InputStream templateInputStream = TemplateUtils.getTemplate(
		    getClass(), "dependencies.xml");
	    Assert.notNull(templateInputStream,
		    "Could not acquire dependencies.xml file");
	    Document dependencyDoc;
	    try {
		dependencyDoc = XmlUtils.getDocumentBuilder().parse(
			templateInputStream);
	    } catch (Exception e) {
		throw new IllegalStateException(e);
	    }

	    Element dependencies = (Element) dependencyDoc.getFirstChild();

	    Element plugin = XmlUtils.findFirstElement(
		    "/dependencies/cargo/plugin", dependencies);
	    Assert
		    .notNull(plugin,
			    "Element '/dependencies/jetty/plugin' not found in dependencies.xml");
	    CARGO_PLUGIN_TOMCAT = new Plugin(plugin);

	}
	return CARGO_PLUGIN_TOMCAT;
    }

    /**
     * <p>
     * Check if add-on has been activated.
     * </p>
     *
     * <p>
     * By now this checks if Servlet library dependency is set to 2.4 version
     * </p>
     *
     * <p>
     * This method caches value to best performance
     * </p>
     *
     * @see #projectValuesChanged()
     */
    public boolean isActivated() {
	if (isActivated == null) {
	    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		    .get(ProjectMetadata.getProjectIdentifier());
	    // XXX Any additional checks?
	    if (projectMetadata.isDependencyRegistered(DEPENDENCY_SERVLET_2_4)) {
		isActivated = Boolean.TRUE;
	    } else {
		isActivated = Boolean.FALSE;
	    }

	}
	return isActivated.booleanValue();
    }

    /**
     * <p>
     * Informs if add-on is working at present
     * </p>
     *
     * <p>
     * This is used by dependencies monitoring to ignored add-on actions
     * </p>
     *
     * @return
     */
    boolean isWorking() {
	return working;
    }

}
