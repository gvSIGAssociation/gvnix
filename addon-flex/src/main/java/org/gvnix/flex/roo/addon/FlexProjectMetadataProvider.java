package org.gvnix.flex.roo.addon;

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

@Component(immediate = true)
@Service
public class FlexProjectMetadataProvider implements MetadataProvider,
        FileEventListener {

    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(MetadataIdentificationUtils
                    .getMetadataClass(FlexProjectMetadata
                            .getProjectIdentifier()));

    // private static final String FLEX_GROUP = "org.springframework.flex";

    @Reference private MetadataService metadataService;

    @Reference private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference private FileManager fileManager;

    @Reference private PathResolver pathResolver;

    private String flexServicesConfigIndentifier;

    protected void activate(ComponentContext context) {
    }

    private String getFlexServicesConfigIndentifier() {
        if (flexServicesConfigIndentifier == null) {
            flexServicesConfigIndentifier = pathResolver.getIdentifier(
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

        if (!fileManager.exists(getFlexServicesConfigIndentifier())) {
            return null;
        }

        return new FlexProjectMetadata(pathResolver);
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
     * metadataDependencyRegistry
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
            metadataService.evictAndGet(FlexProjectMetadata
                    .getProjectIdentifier());
            metadataDependencyRegistry.notifyDownstream(FlexProjectMetadata
                    .getProjectIdentifier());
        }
    }
}
