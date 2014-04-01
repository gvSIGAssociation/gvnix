package org.gvnix.addon.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;

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
     * Use TypeLocationService to find types which are annotated with a given
     * annotation in the project
     */
    @Reference
    private TypeLocationService typeLocationService;

    /**
     * Use TypeManagementService to change types
     */
    @Reference
    private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // TODO: Check if jQuery is installed
        return true;
    }

    /** {@inheritDoc} */
    public void setup() {
        log.log(Level.INFO, "===========================================");
        log.log(Level.INFO, "========== Installing Bootstrap3 ==========");
        log.log(Level.INFO, "===========================================");
        log.log(Level.INFO, "");
        log.log(Level.INFO, ">>> Adding Bootstrap3 libraries");
        addBootstrapScriptsLibraries();
        addBootstrapStyles();
        log.log(Level.INFO, ">>> Replacing old styles");
        replaceOldStyles();
        log.log(Level.INFO, ">>> Adding Bootstrap tags");
        addBootstrapTags();
        log.log(Level.INFO, ">>> Updating Layouts");
        updateGvNIXLayouts();
        log.log(Level.INFO, ">>> Updating Views");
        updateViews();
        log.log(Level.INFO, ">>> Updating jQuery Elements");
        updateJQueryElements();
        log.log(Level.INFO, ">>> Adding Resources");
        addImageResources();
        log.log(Level.INFO, ">>> Checking installed addons");
        checkAndUpdateTypicalSecurity();
        log.log(Level.INFO, "Done");
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
        scriptsFolderFiles.add("dataTables.bootstrap.js");
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
        stylesFolderFiles.add("bootstrap/dataTables.bootstrap.css");
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
            outputStream = fileManager.updateFile(styleFile).getOutputStream();
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
        tagsFolderFiles.add("bootstrap/util/load-scripts-bootstrap.tagx");
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
        viewsFolderFiles.add("login.jspx");
        viewsFolderFiles.add("menu.jspx");
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
     * This method updates the JQuery elements in the application
     */
    public void updateJQueryElements() {
        // TODO: Review all views to change jQuery uri libs
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

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "images/".concat(fileName));
                if (!fileManager.exists(imageFile)) {
                    outputStream = fileManager.createFile(imageFile)
                            .getOutputStream();
                }
                else {
                    outputStream = fileManager.updateFile(imageFile)
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
     * This method checks if typical security is installed. If is installed
     * update views to use bootstrap
     */

    public void checkAndUpdateTypicalSecurity() {
        final String changePasswordFile = pathResolver
                .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                        "WEB-INF/views/changepassword/index.jspx");

        // Checking if the addon is installed
        if (fileManager.exists(changePasswordFile)) {

            log.log(Level.INFO,
                    ">>> Typical security is installed. Updating views.");

            /**
             * Adding and replacing typical security views
             * 
             */
            List<String> viewsFolderFiles = new ArrayList<String>();
            viewsFolderFiles.add("changepassword/index.jspx");
            viewsFolderFiles.add("changepassword/thanks.jspx");
            viewsFolderFiles.add("changepassword/views.xml");
            viewsFolderFiles.add("forgotpassword/index.jspx");
            viewsFolderFiles.add("forgotpassword/thanks.jspx");
            viewsFolderFiles.add("forgotpassword/views.xml");
            viewsFolderFiles.add("signup/error.jspx");
            viewsFolderFiles.add("signup/index.jspx");
            viewsFolderFiles.add("signup/thanks.jspx");
            viewsFolderFiles.add("signup/views.xml");
            viewsFolderFiles.add("roles/create.jspx");
            viewsFolderFiles.add("roles/list.jspx");
            viewsFolderFiles.add("roles/show.jspx");
            viewsFolderFiles.add("roles/update.jspx");
            viewsFolderFiles.add("roles/views.xml");
            viewsFolderFiles.add("userroles/create.jspx");
            viewsFolderFiles.add("userroles/list.jspx");
            viewsFolderFiles.add("userroles/show.jspx");
            viewsFolderFiles.add("userroles/update.jspx");
            viewsFolderFiles.add("userroles/views.xml");
            viewsFolderFiles.add("users/create.jspx");
            viewsFolderFiles.add("users/list.jspx");
            viewsFolderFiles.add("users/show.jspx");
            viewsFolderFiles.add("users/update.jspx");
            viewsFolderFiles.add("users/views.xml");

            Iterator<String> viewsFolderIterator = viewsFolderFiles.iterator();

            while (viewsFolderIterator.hasNext()) {
                String fileName = viewsFolderIterator.next();
                final String viewFile = pathResolver
                        .getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
                                "WEB-INF/views/".concat(fileName));

                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = FileUtils.getInputStream(getClass(),
                            "views/".concat(fileName));
                    if (!fileManager.exists(viewFile)) {
                        outputStream = fileManager.createFile(viewFile)
                                .getOutputStream();
                    }
                    else {
                        outputStream = fileManager.updateFile(viewFile)
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

    }

}