package org.gvnix.addon.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

        // Launching update command to be sure
        // that all tags and views are updated.
        updateTags();

        // Showing finished task
        LOGGER.log(Level.INFO, "Done");
    }

    /** {@inheritDoc} */
    public void updateTags() {
        // Updating all views to use jQuery
        BootstrapUtils.updateJSPViewsToUseJQuery(pathResolver, getWebappPath(),
                projectOperations, fileManager);

        // Checking installed addons
        checkAndUpdateDatatables();
        checkAndUpdateSecurity();
        checkAndUpdateTypicalSecurity();
        checkAndUpdateGeo();

        LOGGER.log(Level.INFO,
                "*** All files are updated to use JQuery and Bootstrap 3 ");
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
        Collections.addAll(imageFolderFiles, "logo_roo.png", "logo_spring.png");

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
     * This method checks if datatables is installed. If is installed add
     * necessary bootstrap javascript and css bootstrap styles
     */
    public void checkAndUpdateDatatables() {

        // Checking if the addon is installed using FEATURES
        if (projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-datatables")) {

            /**
             * Installing script datatables files
             */
            final String scriptFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    "scripts/bootstrap/dataTables.bootstrap.js");

            BootstrapUtils.createFilesInLocationIfNotExists(fileManager,
                    getClass(), scriptFile, "dataTables.bootstrap.js",
                    "scripts/bootstrap/");

            /**
             * Installing css datatable styles
             */
            final String styleFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    "styles/bootstrap/dataTables.bootstrap.css");

            BootstrapUtils.createFilesInLocationIfNotExists(fileManager,
                    getClass(), styleFile, "dataTables.bootstrap.css",
                    "styles/bootstrap/");

            /**
             * Adding references to load-scripts-bootsrap.tagx
             */

            final String loadScriptsFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    "WEB-INF/tags/bootstrap/util/load-scripts-bootstrap.tagx");

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "tags/bootstrap/util/load-scripts-bootstrap.tagx");
                if (!fileManager.exists(loadScriptsFile)) {
                    outputStream = fileManager.createFile(loadScriptsFile)
                            .getOutputStream();
                }
                else if (fileManager.exists(loadScriptsFile)
                        && !isLoadScriptsModified()) {
                    outputStream = fileManager.updateFile(loadScriptsFile)
                            .getOutputStream();
                }
                if (outputStream != null) {
                    IOUtils.copy(inputStream, outputStream);
                }
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                if (outputStream != null) {
                    IOUtils.closeQuietly(outputStream);
                }

            }
        }
    }

    /**
     * This method checks if security is installed. If is installed update views
     * to use bootstrap
     */
    public void checkAndUpdateSecurity() {

        // Checking if the addon is installed using features
        if (projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.SECURITY)
                && !isTypicalSecurityInstalled() && !isLoginModified()) {

            final String viewFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/views/login.jspx");

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), viewFile, "login.jspx", VIEWS);

        }

    }

    /**
     * This method checks if typical security is installed. If is installed
     * update views to use bootstrap
     */

    public void checkAndUpdateTypicalSecurity() {
        // Checking if the addon is installed using features
        if (projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.SECURITY)
                && isTypicalSecurityInstalled() && !isLoginModified()) {

            List<String> viewsFolderFiles = new ArrayList<String>();
            Collections.addAll(viewsFolderFiles, "changepassword/index.jspx",
                    "changepassword/thanks.jspx", "forgotpassword/index.jspx",
                    "forgotpassword/thanks.jspx", "signup/error.jspx",
                    "signup/index.jspx", "signup/thanks.jspx",
                    "roles/create.jspx", "roles/list.jspx", "roles/show.jspx",
                    "roles/update.jspx", "userroles/create.jspx",
                    "userroles/list.jspx", "userroles/show.jspx",
                    "userroles/update.jspx", "users/create.jspx",
                    "users/list.jspx", "users/show.jspx", "users/update.jspx");

            Iterator<String> viewsFolderIterator = viewsFolderFiles.iterator();

            while (viewsFolderIterator.hasNext()) {
                String fileName = viewsFolderIterator.next();
                final String viewFile = pathResolver
                        .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                                "WEB-INF/views/".concat(fileName));

                BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                        fileManager, getClass(), viewFile, fileName, VIEWS);
            }

            // Copying typical-security login

            final String loginView = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/views/login.jspx");

            BootstrapUtils.createFilesInLocationIfNotExistsUpdateIfExists(
                    fileManager, getClass(), loginView, "login-typical.jspx",
                    VIEWS);
        }

    }

    /**
     * This method checks if geo is installed. If is installed and update views
     * to use bootstrap
     */

    public void checkAndUpdateGeo() {
        // Checking if the geo addon is installed using features
        if (projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-geo-web-mvc")) {
            updateLoadScripts("js_leaflet_geo_js",
                    "/resources/scripts/leaflet/leaflet_bootstrap.js", false);
            updateLoadScripts("styles_leaflet_geo_css",
                    "/resources/styles/leaflet/leaflet.bootstrap.css", true);
            updateLoadScripts("styles_gvnix_leaflet_geo_css",
                    "/resources/styles/leaflet/gvnix.leaflet.bootstrap.css",
                    true);

            LOGGER.log(
                    Level.INFO,
                    "** Remember to update all your map 'views.xml' to use 'default-map' layout instead of 'default'.");
        }

    }

    /**
     * This method adds reference in laod-script.tagx
     */
    public void updateLoadScripts(String varName, String url, boolean isCSS) {
        // Modify Roo load-scripts.tagx
        String docTagxPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/util/load-scripts.tagx");

        Validate.isTrue(fileManager.exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = fileManager.updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        if (isCSS) {
            modified = WebProjectUtils.updateCssToTag(docTagx, root, varName,
                    url) || modified;
        }
        else {
            modified = WebProjectUtils.updateJSToTag(docTagx, root, varName,
                    url) || modified;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }

    }

    /**
     * Check if load-scripts-bootstrap.tagx was modified and include datatables
     * 
     * @return
     */
    public boolean isLoadScriptsModified() {
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/bootstrap/util/load-scripts-bootstrap.tagx");
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(dirPath));
        final Element config = document.getDocumentElement();
        final Element urlElement = DomUtils.findFirstElementByName(
                "spring:url", config);
        String value = urlElement.getAttribute("value");

        return value.contains("dataTables.bootstrap.css");
    }

    /**
     * Check if login.jspx is modified with bootstrap
     * 
     * @return
     */
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

    /**
     * Check if typical security is installed
     */

    public boolean isTypicalSecurityInstalled() {
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/views/changepassword/index.jspx");
        return fileManager.exists(dirPath);
    }

}