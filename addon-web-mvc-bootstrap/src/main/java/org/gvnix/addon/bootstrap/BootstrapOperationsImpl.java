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
package org.gvnix.addon.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.bootstrap.listener.BootstrapDependencyListener;
import org.gvnix.support.WebProjectUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Bootstrap Addon operations
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.1
 */
@Component
@Service
public class BootstrapOperationsImpl implements BootstrapOperations {

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private static final String VIEWS = "views/";

    private FileManager fileManager;

    private PathResolver pathResolver;

    private MetadataService metadataService;

    private ProjectOperations projectOperations;

    private WebProjectUtils webProjectUtils;

    /**
     * Uses to ensure that dependencyListener will be loaded
     */
    @Reference
    private BootstrapDependencyListener dependencyListener;

    private static final Logger LOGGER = Logger
            .getLogger(BootstrapOperationsImpl.class.getName());

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * If JQuery is installed, the command is available
     */
    public boolean isSetupCommandAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-jquery")
                && !getProjectOperations().isFeatureInstalledInFocusedModule(
                        FEATURE_NAME_GVNIX_BOOTSTRAP);
    }

    /**
     * If bootstrap is installed, the command is available
     */
    public boolean isUpdateCommandAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FEATURE_NAME_GVNIX_BOOTSTRAP);
    }

    /** {@inheritDoc} */
    public void setup() {

        // Adding Bootstrap libraries
        addBootstrapScriptsLibraries();

        // Adding bootstrap css
        addBootstrapStyles();

        // Replacing old styles
        replaceOldStyles();

        // Adding bootstrap tags
        addBootstrapTags();

        // Adding layouts
        updateGvNIXLayouts();

        // Updating Views and jQuery elements
        updateViews();

        // Adding image resources
        addImageResources();

        // Updating all views to use jQuery
        BootstrapUtils.updateJSPViewsToUseJQuery(getPathResolver(),
                getWebappPath(), getProjectOperations(), getFileManager(),
                getWebProjectUtils());

    }

    /** {@inheritDoc} */
    public void updateTags() {
        // Adding Bootstrap libraries
        restoreBootstrapScriptsLibraries();

        // Adding bootstrap css
        restoreBootstrapStyles();

        // Replacing old styles
        replaceOldStyles();

        // Adding bootstrap tags
        addBootstrapTags();

        // Adding layouts
        updateGvNIXLayouts();

        // Updating Views and jQuery elements
        updateViews();

        // Adding image resources
        restoreImageResources();

        // Updating all views to use jQuery
        BootstrapUtils.updateJSPViewsToUseJQuery(getPathResolver(),
                getWebappPath(), getProjectOperations(), getFileManager(),
                getWebProjectUtils());
    }

    /**
     * This method adds the bootstrap libraries to the gvNIX project
     */
    public void addBootstrapScriptsLibraries() {

        List<String> scriptsFolderFiles = new ArrayList<String>();
        Collections.addAll(scriptsFolderFiles, "bootstrap.min.js",
                "offcanvas.js", "README.txt", "assets/html5shiv.js",
                "assets/respond.min.js");

        Iterator<String> scriptsFolderIterator = scriptsFolderFiles.iterator();

        while (scriptsFolderIterator.hasNext()) {
            String fileName = scriptsFolderIterator.next();
            final String scriptFile = getPathResolver()
                    .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                            "scripts/bootstrap/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExists(getFileManager(),
                    getClass(), scriptFile, fileName, "scripts/bootstrap/");
        }
    }

    /**
     * This method restore the bootstrap libraries to the gvNIX project
     */
    public void restoreBootstrapScriptsLibraries() {

        List<String> scriptsFolderFiles = new ArrayList<String>();
        Collections.addAll(scriptsFolderFiles, "bootstrap.min.js",
                "offcanvas.js", "README.txt", "assets/html5shiv.js",
                "assets/respond.min.js");

        Iterator<String> scriptsFolderIterator = scriptsFolderFiles.iterator();

        while (scriptsFolderIterator.hasNext()) {
            String fileName = scriptsFolderIterator.next();
            final String scriptFile = getPathResolver()
                    .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                            "scripts/bootstrap/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    getFileManager(), getClass(), scriptFile, fileName,
                    "scripts/bootstrap/");
        }
    }

    /**
     * This method adds the bootstrap styles to the gvNIX project
     */
    public void addBootstrapStyles() {

        List<String> stylesFolderFiles = new ArrayList<String>();
        Collections.addAll(stylesFolderFiles, "bootstrap/print.css",
                "bootstrap/bootstrap.min.css",
                "bootstrap/jquery-ui.bootstrap.css", "bootstrap/offcanvas.css",
                "bootstrap/images/ui-bg_glass_75_ffffff_1x400.png",
                "bootstrap/images/ui-icons_222222_256x240.png",
                "fonts/glyphicons-halflings-regular.eot",
                "fonts/glyphicons-halflings-regular.svg",
                "fonts/glyphicons-halflings-regular.ttf",
                "fonts/glyphicons-halflings-regular.woff", "fonts/README.txt",
                "images/sort_asc.png", "images/sort_both.png",
                "images/sort_desc.png");

        Iterator<String> stylesFolderIterator = stylesFolderFiles.iterator();

        while (stylesFolderIterator.hasNext()) {
            String fileName = stylesFolderIterator.next();
            final String styleFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "styles/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExists(getFileManager(),
                    getClass(), styleFile, fileName, "styles/");

        }
    }

    /**
     * This method restores the bootstrap styles to the gvNIX project
     */
    public void restoreBootstrapStyles() {

        List<String> stylesFolderFiles = new ArrayList<String>();
        Collections.addAll(stylesFolderFiles, "bootstrap/print.css",
                "bootstrap/bootstrap.min.css",
                "bootstrap/jquery-ui.bootstrap.css", "bootstrap/offcanvas.css",
                "bootstrap/images/ui-bg_glass_75_ffffff_1x400.png",
                "bootstrap/images/ui-icons_222222_256x240.png",
                "fonts/glyphicons-halflings-regular.eot",
                "fonts/glyphicons-halflings-regular.svg",
                "fonts/glyphicons-halflings-regular.ttf",
                "fonts/glyphicons-halflings-regular.woff", "fonts/README.txt",
                "images/sort_asc.png", "images/sort_both.png",
                "images/sort_desc.png");

        Iterator<String> stylesFolderIterator = stylesFolderFiles.iterator();

        while (stylesFolderIterator.hasNext()) {
            String fileName = stylesFolderIterator.next();
            final String styleFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "styles/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    getFileManager(), getClass(), styleFile, fileName,
                    "styles/");

        }
    }

    /**
     * This method replaces the old standard.css with a new one adapted to
     * Bootstrap3
     */
    public void replaceOldStyles() {

        final String styleFile = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "styles/standard.css");

        BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                getFileManager(), getClass(), styleFile, "standard.css",
                "styles/");
    }

    /**
     * This method adds the bootstrap tags to the correct folder
     */
    public void addBootstrapTags() {

        List<String> tagsFolderFiles = new ArrayList<String>();
        Collections.addAll(tagsFolderFiles,
                "bootstrap/dialog/modal/message-box.tagx",
                "bootstrap/util/load-styles-bootstrap.tagx",
                "menu/category.tagx", "menu/menu.tagx",
                "jquery/util/panel.tagx", "jquery/form/create.tagx",
                "jquery/form/find.tagx", "jquery/form/show.tagx",
                "jquery/form/update.tagx", "jquery/form/fields/checkbox.tagx",
                "jquery/form/fields/datetime.tagx",
                "jquery/form/fields/display.tagx",
                "jquery/form/fields/editor.tagx",
                "jquery/form/fields/input.tagx",
                "jquery/form/fields/reference.tagx",
                "jquery/form/fields/select.tagx",
                "jquery/form/fields/simple.tagx",
                "jquery/form/fields/textarea.tagx");

        // Adding load scripts no datatable
        addLoadScriptsNoDatatables();

        Iterator<String> tagsFolderIterator = tagsFolderFiles.iterator();

        while (tagsFolderIterator.hasNext()) {
            String fileName = tagsFolderIterator.next();
            final String tagFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    getFileManager(), getClass(), tagFile, fileName, "tags/");

        }
    }

    /**
     * This method copies load-scripts-bootstrap-no-datatables.tagx to the
     * project
     */
    public void addLoadScriptsNoDatatables() {
        final String tagFile = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                "WEB-INF/tags/bootstrap/util/load-scripts-bootstrap.tagx");

        BootstrapUtils.createFilesInLocationIfNotExists(getFileManager(),
                getClass(), tagFile,
                "load-scripts-bootstrap-no-datatables.tagx",
                "tags/bootstrap/util/");
    }

    /**
     * This method updates HTML structure in default gvNIX layouts to use
     * bootstrap
     */
    public void updateGvNIXLayouts() {

        List<String> layoutsFolderFiles = new ArrayList<String>();
        Collections.addAll(layoutsFolderFiles, "default-menu-cols.jspx",
                "default.jspx");

        Iterator<String> layoutsFolderIterator = layoutsFolderFiles.iterator();

        while (layoutsFolderIterator.hasNext()) {
            String fileName = layoutsFolderIterator.next();
            final String layoutFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/layouts/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    getFileManager(), getClass(), layoutFile, fileName,
                    "layouts/");

        }
    }

    /**
     * This method updates HTML structure in default gvNIX views to use
     * bootstrap
     */
    public void updateViews() {

        List<String> viewsFolderFiles = new ArrayList<String>();
        Collections.addAll(viewsFolderFiles, "footer.jspx", "header.jspx",
                "index.jspx", "uncaughtException.jspx");

        Iterator<String> viewsFolderIterator = viewsFolderFiles.iterator();

        while (viewsFolderIterator.hasNext()) {
            String fileName = viewsFolderIterator.next();
            final String viewFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/views/".concat(fileName));

            BootstrapUtils.updateFilesInLocationIfExists(getFileManager(),
                    getClass(), viewFile, fileName, VIEWS);
        }
    }

    /**
     * This method copies images resources to gvNIX application
     */
    public void addImageResources() {

        List<String> imageFolderFiles = new ArrayList<String>();
        Collections.addAll(imageFolderFiles, "logo_gvnix.png", "favicon.ico");

        Iterator<String> imageFolderIterator = imageFolderFiles.iterator();

        while (imageFolderIterator.hasNext()) {
            String fileName = imageFolderIterator.next();
            final String imageFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "images/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    getFileManager(), getClass(), imageFile, fileName,
                    "images/");
        }
    }

    /**
     * This method copies images resources to gvNIX application
     */
    public void restoreImageResources() {

        List<String> imageFolderFiles = new ArrayList<String>();
        Collections.addAll(imageFolderFiles, "logo_gvnix.png", "favicon.ico");

        Iterator<String> imageFolderIterator = imageFolderFiles.iterator();

        while (imageFolderIterator.hasNext()) {
            String fileName = imageFolderIterator.next();
            final String imageFile = getPathResolver().getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "images/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    getFileManager(), getClass(), imageFile, fileName,
                    "images/");
        }
    }

    /**
     * This method update security views to use bootstrap
     */
    public void updateSecurityAddonToBootstrap() {
        final String viewFile = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/login.jspx");
        BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                getFileManager(), getClass(), viewFile, "login.jspx", VIEWS);
    }

    /**
     * Check if login.jspx is modified with bootstrap
     *
     * @return
     */
    @Override
    public boolean isLoginModified() {
        String dirPath = getPathResolver().getIdentifier(getWebappPath(),
                "WEB-INF/views/login.jspx");
        final Document document = XmlUtils.readXml(getFileManager()
                .getInputStream(dirPath));
        final Element config = document.getDocumentElement();
        final Element urlElement = DomUtils.findFirstElementByName("div",
                config);
        String value = urlElement.getAttribute("class");
        return value.contains("alert alert-danger");
    }

    /**
     * Check if typical security is installed
     */
    @Override
    public boolean isTypicalSecurityInstalled() {
        String dirPath = getPathResolver().getIdentifier(getWebappPath(),
                "WEB-INF/views/changepassword/index.jspx");
        return getFileManager().exists(dirPath);
    }

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     *
     * @return
     */
    public LogicalPath getWebappPath() {
        return getWebProjectUtils().getWebappPath(getProjectOperations());
    }

    // Feature methods -----

    /**
     * Gets the feature name managed by this operations class.
     *
     * @return feature name
     */
    @Override
    public String getName() {
        return FEATURE_NAME_GVNIX_BOOTSTRAP;
    }

    /**
     * Returns true if bootstrap is installed
     */
    @Override
    public boolean isInstalledInModule(String moduleName) {
        String dirPath = getPathResolver().getIdentifier(getWebappPath(),
                "scripts/bootstrap/bootstrap.min.js");
        return getFileManager().exists(dirPath);
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on BootstrapOperationsImpl.");
                return null;
            }
        }
        else {
            return fileManager;
        }
    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (PathResolver) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on BootstrapOperationsImpl.");
                return null;
            }
        }
        else {
            return pathResolver;
        }
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on BootstrapOperationsImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on BootstrapOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public WebProjectUtils getWebProjectUtils() {
        if (webProjectUtils == null) {
            // Get all Services implement WebProjectUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebProjectUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    webProjectUtils = (WebProjectUtils) this.context
                            .getService(ref);
                    return webProjectUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WebProjectUtils on BootstrapOperationsImpl.");
                return null;
            }
        }
        else {
            return webProjectUtils;
        }

    }
}