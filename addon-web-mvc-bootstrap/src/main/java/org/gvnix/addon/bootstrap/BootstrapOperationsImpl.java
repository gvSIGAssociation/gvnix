package org.gvnix.addon.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.support.WebProjectUtils;

/**
 * Implementation of Bootstrap Addon operations
 * 
 * @since 1.1
 */
@Component
@Service
public class BootstrapOperationsImpl implements BootstrapOperations {

    private static final String VIEWS = "views/";

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private MetadataService metadataService;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    private static final Logger LOGGER = Logger
            .getLogger(BootstrapOperationsImpl.class.getName());

    /**
     * If JQuery is installed, the command is available
     */
    public boolean isSetupCommandAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-jquery")
                && !projectOperations
                        .isFeatureInstalledInFocusedModule(FEATURE_NAME_GVNIX_BOOTSTRAP);
    }

    /**
     * If bootstrap is installed, the command is available
     */
    public boolean isUpdateCommandAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FEATURE_NAME_GVNIX_BOOTSTRAP);
    }

    /** {@inheritDoc} */
    public void setup() {
        // Adding Bootstrap Dependency
        addBootstrapDependency();

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
        BootstrapUtils.updateJSPViewsToUseJQuery(pathResolver, getWebappPath(),
                projectOperations, fileManager);

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
        BootstrapUtils.updateJSPViewsToUseJQuery(pathResolver, getWebappPath(),
                projectOperations, fileManager);
    }

    /**
     * This method adds bootstrap addon dependency on gvNIX Project
     */
    public void addBootstrapDependency() {
        final Element configuration = XmlUtils.getConfiguration(getClass());

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
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
            final String scriptFile = pathResolver
                    .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                            "scripts/bootstrap/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExists(fileManager,
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
            final String scriptFile = pathResolver
                    .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                            "scripts/bootstrap/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), scriptFile, fileName,
                    "scripts/bootstrap/");
        }
    }

    /**
     * This method adds the bootstrap styles to the gvNIX project
     */
    public void addBootstrapStyles() {

        List<String> stylesFolderFiles = new ArrayList<String>();
        Collections.addAll(stylesFolderFiles, "bootstrap/bootstrap.min.css",
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
            final String styleFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "styles/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExists(fileManager,
                    getClass(), styleFile, fileName, "styles/");

        }
    }

    /**
     * This method restores the bootstrap styles to the gvNIX project
     */
    public void restoreBootstrapStyles() {

        List<String> stylesFolderFiles = new ArrayList<String>();
        Collections.addAll(stylesFolderFiles, "bootstrap/bootstrap.min.css",
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
            final String styleFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "styles/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), styleFile, fileName, "styles/");

        }
    }

    /**
     * This method replaces the old standard.css with a new one adapted to
     * Bootstrap3
     */
    public void replaceOldStyles() {

        final String styleFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "styles/standard.css");

        BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                fileManager, getClass(), styleFile, "standard.css", "styles/");
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
            final String tagFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), tagFile, fileName, "tags/");

        }
    }

    /**
     * This method copies load-scripts-bootstrap-no-datatables.tagx to the
     * project
     */
    public void addLoadScriptsNoDatatables() {
        final String tagFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                "WEB-INF/tags/bootstrap/util/load-scripts-bootstrap.tagx");

        BootstrapUtils.createFilesInLocationIfNotExists(fileManager,
                getClass(), tagFile,
                "load-scripts-bootstrap-no-datatables.tagx",
                "tags/bootstrap/util/");
    }

    /**
     * This method updates HTML structure in default gvNIX layouts to use
     * bootsrap
     */
    public void updateGvNIXLayouts() {

        List<String> layoutsFolderFiles = new ArrayList<String>();
        Collections.addAll(layoutsFolderFiles, "default-menu-cols.jspx",
                "default.jspx");

        Iterator<String> layoutsFolderIterator = layoutsFolderFiles.iterator();

        while (layoutsFolderIterator.hasNext()) {
            String fileName = layoutsFolderIterator.next();
            final String layoutFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/layouts/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), layoutFile, fileName, "layouts/");

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
            final String viewFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/views/".concat(fileName));

            BootstrapUtils.updateFilesInLocationIfExists(fileManager,
                    getClass(), viewFile, fileName, VIEWS);
        }
    }

    /**
     * This method copies images resources to gvNIX application
     */
    public void addImageResources() {

        List<String> imageFolderFiles = new ArrayList<String>();
        Collections.addAll(imageFolderFiles, "logo_gvnix.png");

        Iterator<String> imageFolderIterator = imageFolderFiles.iterator();

        while (imageFolderIterator.hasNext()) {
            String fileName = imageFolderIterator.next();
            final String imageFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "images/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExists(fileManager,
                    getClass(), imageFile, fileName, "images/");
        }
    }

    /**
     * This method copies images resources to gvNIX application
     */
    public void restoreImageResources() {

        List<String> imageFolderFiles = new ArrayList<String>();
        Collections.addAll(imageFolderFiles, "logo_gvnix.png");

        Iterator<String> imageFolderIterator = imageFolderFiles.iterator();

        while (imageFolderIterator.hasNext()) {
            String fileName = imageFolderIterator.next();
            final String imageFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "images/".concat(fileName));

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), imageFile, fileName, "images/");
        }
    }

    /**
     * This method update security views to use bootstrap
     */
    public void updateSecurityAddonToBootstrap() {
        final String viewFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/login.jspx");
        BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                fileManager, getClass(), viewFile, "login.jspx", VIEWS);
    }

    /**
     * Check if login.jspx is modified with bootstrap
     * 
     * @return
     */
    @Override
    public boolean isLoginModified() {
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/views/login.jspx");
        final Document document = XmlUtils.readXml(fileManager
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
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/views/changepassword/index.jspx");
        return fileManager.exists(dirPath);
    }

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    public LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(projectOperations);
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
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "scripts/bootstrap/bootstrap.min.js");
        return fileManager.exists(dirPath);
    }
}