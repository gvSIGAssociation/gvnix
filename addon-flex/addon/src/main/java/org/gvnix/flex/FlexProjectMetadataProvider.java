package org.gvnix.flex;

import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

@Component
@Service
public class FlexProjectMetadataProvider implements MetadataProvider,
        FileEventListener {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(FlexProjectMetadataProvider.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(MetadataIdentificationUtils
                    .getMetadataClass(FlexProjectMetadata
                            .getProjectIdentifier()));

    // private static final String FLEX_GROUP = "org.springframework.flex";

    private MetadataService metadataService;

    private MetadataDependencyRegistry metadataDependencyRegistry;

    private FileManager fileManager;

    private PathResolver pathResolver;

    private String flexServicesConfigIndentifier;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    private String getFlexServicesConfigIndentifier() {
        if (flexServicesConfigIndentifier == null) {
            flexServicesConfigIndentifier = getPathResolver().getIdentifier(
                    LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                    "WEB-INF/flex/services-config.xml");
        }
        return flexServicesConfigIndentifier;
    }

    public MetadataItem get(String metadataIdentificationString) {
        Validate.isTrue(
                FlexProjectMetadata.getProjectIdentifier().equals(
                        metadataIdentificationString),
                "Unexpected metadata request '" + metadataIdentificationString
                        + "' for this provider");

        if (!getFileManager().exists(getFlexServicesConfigIndentifier())) {
            return null;
        }

        return new FlexProjectMetadata(getPathResolver());
    }

    /*
     * public void notify(String upstreamDependency, String
     * downstreamDependency) {
     * Validate.isTrue(MetadataIdentificationUtils.isValid(upstreamDependency),
     * "Upstream dependency is an invalid metadata identification string ('" +
     * upstreamDependency + "')");
     * 
     * if (upstreamDependency.equals(ProjectMetadata.getProjectIdentifier())) {
     * //recalculate the FlexProjectMetadata and notify
     * metadataService.get(FlexProjectMetadata.getProjectIdentifier(), true);
     * getMetadataDependencyRegistry()
     * .notifyDownstream(FlexProjectMetadata.getProjectIdentifier()); } }
     */

    public String getProvidesType() {
        return PROVIDES_TYPE;
    }

    public void onFileEvent(FileEvent fileEvent) {
        Validate.notNull(fileEvent, "File event required");

        if (fileEvent.getFileDetails().getCanonicalPath()
                .equals(getFlexServicesConfigIndentifier())) {
            // Something happened to the services-config

            // Don't notify if we're shutting down
            if (fileEvent.getOperation() == FileOperation.MONITORING_FINISH) {
                return;
            }

            // Otherwise let everyone know something has happened of interest,
            // plus evict any cached entries from the MetadataService
            getMetadataService().evictAndGet(
                    FlexProjectMetadata.getProjectIdentifier());
            getMetadataDependencyRegistry().notifyDownstream(
                    FlexProjectMetadata.getProjectIdentifier());
        }
    }

    public MetadataDependencyRegistry getMetadataDependencyRegistry() {
        if (metadataDependencyRegistry == null) {
            // Get all Services implement MetadataDependencyRegistry interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataDependencyRegistry.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataDependencyRegistry) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataDependencyRegistry on FlexProjectMetadataProvider.");
                return null;
            }
        }
        else {
            return metadataDependencyRegistry;
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
                LOGGER.warning("Cannot load PathResolver on FlexProjectMetadataProvider.");
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
                LOGGER.warning("Cannot load MetadataService on FlexProjectMetadataProvider.");
                return null;
            }
        }
        else {
            return metadataService;
        }
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
                LOGGER.warning("Cannot load FileManager on FlexProjectMetadataProvider.");
                return null;
            }
        }
        else {
            return fileManager;
        }
    }
}
