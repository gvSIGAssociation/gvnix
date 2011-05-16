package org.gvnix.web.mvc.binding.roo.addon;

import java.io.File;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * File Listener that checks any changes configuration files or Initializer java
 * file.<br/>
 * If this happens uses the operation class methods to update the configuration.
 * 
 * @author jmvivo
 * 
 */
@Component
@Service
public class WebBinderFileListener implements FileEventListener {

    @Reference
    private WebBinderOperations webBinderOperations;
    @Reference
    private ProjectOperations projectOperations;

    public void onFileEvent(FileEvent fileEvent) {

        // Checks if is mvc config file
        if (fileEvent.getFileDetails().getCanonicalPath()
                .equals(webBinderOperations.getPathToMvcConfig())) {
            if (fileEvent.getOperation() == FileOperation.UPDATED) {
                webBinderOperations.clearCurrentIntializer();
            }
        }

        JavaType currentInitializer = webBinderOperations
                .getCurrentInitializer();
        if (currentInitializer != null) {
            PathResolver pathResolver = projectOperations.getPathResolver();
            // Check if is java file of initialize
            String physicalTypeIdentifier = pathResolver.getIdentifier(
                    Path.SRC_MAIN_JAVA,
                    currentInitializer.getFullyQualifiedTypeName()
                            .replaceAll("\\.", File.separator).concat(".java"));
            if (fileEvent.getFileDetails().getFile().getAbsolutePath()
                    .equals(physicalTypeIdentifier)) {
                // It's the initilaizer file
                if (fileEvent.getOperation() == FileOperation.DELETED) {
                    // if it has been deleted this clear the configuration
                    webBinderOperations.drop();
                }

            }
            /*
             * TODO: Try to fix RENAMED event since now when rename file is
             * performed (mv file newfile) performs CREATED and DELETED events
             */
            else if (fileEvent.getOperation() == FileOperation.RENAMED
                    && fileEvent.getPreviousName().getAbsolutePath()
                            .equals(physicalTypeIdentifier)) {
                // The initilaizer file has been renamed, update the mvc config
                JavaType newType = null; // FIXME
                webBinderOperations.renameInitializer(newType);
            }
        }

    }

}
