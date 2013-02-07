package org.springframework.flex.roo.addon;

import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.project.PathResolver;

public class FlexProjectMetadata extends AbstractMetadataItem {

    private static final String FLEX_PROJECT_IDENTIFIER = MetadataIdentificationUtils
            .create(FlexProjectMetadata.class.getName(), "flex_project");

    private PathResolver pathResolver;

    public FlexProjectMetadata(PathResolver flexPathResolver) {
        super(FLEX_PROJECT_IDENTIFIER);
        this.pathResolver = flexPathResolver;
    }

    public PathResolver getPathResolver() {
        return pathResolver;
    }

    public static final String getProjectIdentifier() {
        return FLEX_PROJECT_IDENTIFIER;
    }
}
