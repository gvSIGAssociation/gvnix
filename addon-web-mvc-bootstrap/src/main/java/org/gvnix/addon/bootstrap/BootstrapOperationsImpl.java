package org.gvnix.addon.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.process.manager.FileManager;
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
 * Implementation of operations this add-on offers.
 * 
 * @since 1.1
 */
@Component
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class BootstrapOperationsImpl implements BootstrapOperations {

    private Logger log = Logger.getLogger(getClass().getName());

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
     * if bootstrap is installed, the command is available
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
        updateJSPViewsToUseJQuery();

        // Adding image resources
        addImageResources();

        // Launching update command to be sure
        // that all tags and views are updated.
        updateTags();

        // Showing finished task
        log.log(Level.INFO, "Done");
    }

    /** {@inheritDoc} */
    public void updateTags() {
        // Updating al views to use jQuery
        updateJSPViewsToUseJQuery();

        // Checking installed addons
        checkAndUpdateDatatables();
        checkAndUpdateSecurity();

        log.log(Level.INFO,
                "*** All files are updated to use JQuery and Bootstrap 3 ");
    }

    /**
     * This method adds the bootstrap libraries to the gvNIX project
     */
    public void addBootstrapScriptsLibraries() {

        /**
         * Adding all documents on scripts folders
         * 
         */
        List<String> scriptsFolderFiles = new ArrayList<String>();
        scriptsFolderFiles.add("bootstrap.min.js");
        scriptsFolderFiles.add("offcanvas.js");
        scriptsFolderFiles.add("README.txt");
        scriptsFolderFiles.add("assets/html5shiv.js");
        scriptsFolderFiles.add("assets/respond.min.js");

        Iterator<String> scriptsFolderIterator = scriptsFolderFiles.iterator();

        while (scriptsFolderIterator.hasNext()) {
            String fileName = scriptsFolderIterator.next();
            final String scriptFile = pathResolver
                    .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                            "scripts/bootstrap/".concat(fileName));

            if (!fileManager.exists(scriptFile)) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "scripts/bootstrap/".concat(fileName));
                    outputStream = fileManager.createFile(scriptFile)
                            .getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        }
    }

    /**
     * This method adds the bootstrap styles to the gvNIX project
     */
    public void addBootstrapStyles() {

        /**
         * Adding all documents on styles folders
         * 
         */
        List<String> stylesFolderFiles = new ArrayList<String>();
        stylesFolderFiles.add("bootstrap/bootstrap.min.css");
        stylesFolderFiles.add("bootstrap/docs.css");
        stylesFolderFiles.add("bootstrap/jquery-ui.bootstrap.css");
        stylesFolderFiles.add("bootstrap/offcanvas.css");
        stylesFolderFiles
                .add("bootstrap/images/ui-bg_glass_75_ffffff_1x400.png");
        stylesFolderFiles.add("bootstrap/images/ui-icons_222222_256x240.png");

        stylesFolderFiles.add("fonts/glyphicons-halflings-regular.eot");
        stylesFolderFiles.add("fonts/glyphicons-halflings-regular.svg");
        stylesFolderFiles.add("fonts/glyphicons-halflings-regular.ttf");
        stylesFolderFiles.add("fonts/glyphicons-halflings-regular.woff");
        stylesFolderFiles.add("fonts/README.txt");
        stylesFolderFiles.add("images/sort_asc.png");
        stylesFolderFiles.add("images/sort_both.png");

        Iterator<String> stylesFolderIterator = stylesFolderFiles.iterator();

        while (stylesFolderIterator.hasNext()) {
            String fileName = stylesFolderIterator.next();
            final String styleFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "styles/".concat(fileName));

            if (!fileManager.exists(styleFile)) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "styles/".concat(fileName));
                    outputStream = fileManager.createFile(styleFile)
                            .getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        }
    }

    /**
     * This method replaces the old standard.css with a new one adapted to
     * Bootstrap3
     */
    public void replaceOldStyles() {

        final String styleFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "styles/standard.css");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    "styles/standard.css");
            if (!fileManager.exists(styleFile)) {
                outputStream = fileManager.createFile(styleFile)
                        .getOutputStream();
            }
            else {
                outputStream = fileManager.updateFile(styleFile)
                        .getOutputStream();
            }
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * This method adds the bootstrap tags to the correct folder
     */
    public void addBootstrapTags() {

        /**
         * Adding all documents on tags folders
         * 
         */
        List<String> tagsFolderFiles = new ArrayList<String>();
        tagsFolderFiles.add("bootstrap/dialog/modal/message-box.tagx");
        // Adding load scripts no datatable
        addLoadScriptsNoDatatables();
        tagsFolderFiles.add("bootstrap/util/load-styles-bootstrap.tagx");
        tagsFolderFiles.add("menu/category.tagx");
        tagsFolderFiles.add("menu/menu.tagx");
        tagsFolderFiles.add("jquery/util/panel.tagx");
        tagsFolderFiles.add("jquery/form/create.tagx");
        tagsFolderFiles.add("jquery/form/find.tagx");
        tagsFolderFiles.add("jquery/form/show.tagx");
        tagsFolderFiles.add("jquery/form/update.tagx");
        tagsFolderFiles.add("jquery/form/fields/checkbox.tagx");
        tagsFolderFiles.add("jquery/form/fields/datetime.tagx");
        tagsFolderFiles.add("jquery/form/fields/display.tagx");
        tagsFolderFiles.add("jquery/form/fields/editor.tagx");
        tagsFolderFiles.add("jquery/form/fields/input.tagx");
        tagsFolderFiles.add("jquery/form/fields/reference.tagx");
        tagsFolderFiles.add("jquery/form/fields/select.tagx");
        tagsFolderFiles.add("jquery/form/fields/simple.tagx");
        tagsFolderFiles.add("jquery/form/fields/textarea.tagx");

        Iterator<String> tagsFolderIterator = tagsFolderFiles.iterator();

        while (tagsFolderIterator.hasNext()) {
            String fileName = tagsFolderIterator.next();
            final String tagFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/".concat(fileName));

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "tags/".concat(fileName));
                if (!fileManager.exists(tagFile)) {
                    outputStream = fileManager.createFile(tagFile)
                            .getOutputStream();
                }
                else {
                    outputStream = fileManager.updateFile(tagFile)
                            .getOutputStream();
                }
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }

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

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils
                    .getInputStream(getClass(),
                            "tags/bootstrap/util/load-scripts-bootstrap-no-datatables.tagx");
            if (!fileManager.exists(tagFile)) {
                outputStream = fileManager.createFile(tagFile)
                        .getOutputStream();
            }
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

    }

    /**
     * This method updates HTML structure in default gvNIX layouts to use
     * bootsrap
     */
    public void updateGvNIXLayouts() {
        /**
         * Adding and replacing layouts
         * 
         */
        List<String> layoutsFolderFiles = new ArrayList<String>();
        layoutsFolderFiles.add("default-menu-cols.jspx");
        layoutsFolderFiles.add("default.jspx");

        Iterator<String> layoutsFolderIterator = layoutsFolderFiles.iterator();

        while (layoutsFolderIterator.hasNext()) {
            String fileName = layoutsFolderIterator.next();
            final String layoutFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/layouts/".concat(fileName));

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "layouts/".concat(fileName));
                if (!fileManager.exists(layoutFile)) {
                    outputStream = fileManager.createFile(layoutFile)
                            .getOutputStream();
                }
                else {
                    outputStream = fileManager.updateFile(layoutFile)
                            .getOutputStream();
                }
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }

        }
    }

    /**
     * This method updates HTML structure in default gvNIX views to use
     * bootstrap
     */
    public void updateViews() {

        /**
         * Adding and replacing gvNIX views
         * 
         */
        List<String> viewsFolderFiles = new ArrayList<String>();
        viewsFolderFiles.add("footer.jspx");
        viewsFolderFiles.add("header.jspx");
        viewsFolderFiles.add("index.jspx");
        viewsFolderFiles.add("uncaughtException.jspx");

        Iterator<String> viewsFolderIterator = viewsFolderFiles.iterator();

        while (viewsFolderIterator.hasNext()) {
            String fileName = viewsFolderIterator.next();
            final String viewFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/views/".concat(fileName));

            if (fileManager.exists(viewFile)) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "views/".concat(fileName));

                    outputStream = fileManager.updateFile(viewFile)
                            .getOutputStream();

                    IOUtils.copy(inputStream, outputStream);
                }
                catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }

        }
    }

    /**
     * This method copies images resources to gvNIX application
     */
    public void addImageResources() {
        /**
         * Adding and replacing images
         * 
         */
        List<String> imageFolderFiles = new ArrayList<String>();
        imageFolderFiles.add("logo_roo.png");
        imageFolderFiles.add("logo_spring.png");

        Iterator<String> imageFolderIterator = imageFolderFiles.iterator();

        while (imageFolderIterator.hasNext()) {
            String fileName = imageFolderIterator.next();
            final String imageFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "images/".concat(fileName));
            if (!fileManager.exists(imageFile)) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "images/".concat(fileName));

                    outputStream = fileManager.createFile(imageFile)
                            .getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        }
    }

    /**
     * This method checks if datatables is installed. If is installed add
     * necessary bootstrap javascript and css bootstrap styles
     */
    public void checkAndUpdateDatatables() {
        final String datatablesTagx = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/datatables/table.tagx");

        // Checking if the addon is installed
        if (fileManager.exists(datatablesTagx)) {

            /**
             * Installing script datatables files
             */
            final String scriptFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    "scripts/bootstrap/dataTables.bootstrap.js");

            if (!fileManager.exists(scriptFile)) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "scripts/bootstrap/dataTables.bootstrap.js");
                    outputStream = fileManager.createFile(scriptFile)
                            .getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }

            /**
             * Installing css datatable styles
             */
            final String styleFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP,
                    "styles/bootstrap/dataTables.bootstrap.css");
            if (!fileManager.exists(styleFile)) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "styles/bootstrap/dataTables.bootstrap.css");
                    outputStream = fileManager.createFile(styleFile)
                            .getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }

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
        final String loginFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/login.jspx");

        // Checking if the addon is installed
        if (fileManager.exists(loginFile)) {

            /**
             * Adding and replacing typical security views
             * 
             */

            final String viewFile = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/views/login.jspx");

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "views/login.jspx");
                if (!fileManager.exists(viewFile)) {
                    outputStream = fileManager.createFile(viewFile)
                            .getOutputStream();
                }
                else if (fileManager.exists(viewFile) && !isLoginModified()) {
                    outputStream = fileManager.updateFile(viewFile)
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
     * Check if load-scripts-bootstrap.tagx was modified and include datatables
     * 
     * @return
     */
    public boolean isLoadScriptsModified() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/bootstrap/util/load-scripts-bootstrap.tagx");
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(dirPath));
        final Element config = document.getDocumentElement();
        final Element urlElement = DomUtils.findFirstElementByName(
                "spring:url", config);
        String value = urlElement.getAttribute("value");
        if (value.contains("dataTables.bootstrap.css")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Check if login.jspx is modified with bootstrap
     * 
     * @return
     */
    public boolean isLoginModified() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/views/login.jspx");
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(dirPath));
        final Element config = document.getDocumentElement();
        final Element urlElement = DomUtils.findFirstElementByName("div",
                config);
        String value = urlElement.getAttribute("class");
        if (value.contains("alert alert-danger")) {
            return true;
        }
        else {
            return false;
        }
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

    /**
     * Updates all JSP pages of target controller to use JQuery
     * 
     */
    public void updateJSPViewsToUseJQuery() {

        String path = "";
        // Getting all views of the application
        String viewsPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/views/");
        File directory = new File(viewsPath);
        File[] folders = directory.listFiles();

        for (File folder : folders) {
            if (folder.isDirectory()) {
                path = folder.getName().concat("/");

                // List of pages to update
                // List of pages to update
                List<String> pageList = new ArrayList<String>();

                // Getting all jspx files inside the folder
                File[] files = folder.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    if (file.isFile()
                            && fileName.contains("jspx")
                            && (fileName.contains("create")
                                    || fileName.contains("update")
                                    || fileName.contains("show")
                                    || fileName.contains("list") || fileName
                                        .contains("find"))) {
                        pageList.add(file.getName());
                    }
                }

                // 3rd party add-ons could customize default Roo tags as gvNIX
                // does,
                // to avoid to overwrite them with jQuery namespaces we will
                // update
                // default Roo namespaces only
                Map<String, String> rooUriMap = new HashMap<String, String>();
                rooUriMap.put("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/form/fields");
                rooUriMap.put("xmlns:form", "urn:jsptagdir:/WEB-INF/tags/form");
                rooUriMap.put("xmlns:table",
                        "urn:jsptagdir:/WEB-INF/tags/form/fields");
                rooUriMap.put("xmlns:page", "urn:jsptagdir:/WEB-INF/tags/form");
                rooUriMap.put("xmlns:util", "urn:jsptagdir:/WEB-INF/tags/util");

                // new jQuery namespaces
                Map<String, String> uriMap = new HashMap<String, String>();
                uriMap.put("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
                uriMap.put("xmlns:form",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form");
                uriMap.put("xmlns:table",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
                uriMap.put("xmlns:page",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form");
                uriMap.put("xmlns:util",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/util");

                // do the update
                for (String jspxName : pageList) {
                    String tagxFile = "WEB-INF/views/".concat(path).concat(
                            jspxName);
                    WebProjectUtils.updateTagxUriInJspx(tagxFile, rooUriMap,
                            uriMap, projectOperations, fileManager);
                }
            }
        }
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
        PathResolver pathResolver = projectOperations.getPathResolver();
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "scripts/bootstrap/bootstrap.min.js");
        return fileManager.exists(dirPath);
    }

}