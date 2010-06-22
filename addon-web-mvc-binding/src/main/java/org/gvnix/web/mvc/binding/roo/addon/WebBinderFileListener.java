package org.gvnix.web.mvc.binding.roo.addon;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

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
    private ClasspathOperations classpathOperations;

    public void onFileEvent(FileEvent fileEvent) {

	// Checks if is mvc config file
	if (fileEvent.getFileDetails().getCanonicalPath().equals(
		webBinderOperations.getPathToMvcConfig())) {
	    if (fileEvent.getOperation() == FileOperation.UPDATED) {
		webBinderOperations.clearCurrentIntializer();
	    }
	}

	if (webBinderOperations.getCurrentInitializer() != null) {
	    // Check if is java file of initialize
	    String physicalTypeIdentifier = PhysicalTypeIdentifier
		    .createIdentifier(webBinderOperations
			    .getCurrentInitializer(), Path.SRC_MAIN_JAVA);
	    String path = classpathOperations
		    .getPhysicalLocationCanonicalPath(physicalTypeIdentifier);
	    if (fileEvent.getFileDetails().getFile().getAbsolutePath().equals(
		    path)) {
		// It's the initilaizer file
		if (fileEvent.getOperation() == FileOperation.DELETED) {
		    // if it has been deleted this clear the configuration
		    webBinderOperations.drop();
		}

	    } else if (fileEvent.getOperation() == FileOperation.RENAMED
		    && fileEvent.getPreviousName().getAbsolutePath().equals(
			    path)) {
		// The initilaizer file has been renamed, update the mvc config
		JavaType newType = null; // FIXME
		webBinderOperations.renameInitializer(newType);
	    }
	}

    }

}
