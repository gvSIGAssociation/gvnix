package org.springframework.flex.roo.addon;

import org.springframework.flex.roo.addon.mojos.FlexPathResolver;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;


public class FlexProjectMetadata extends AbstractMetadataItem {

    private static final String FLEX_PROJECT_IDENTIFIER = MetadataIdentificationUtils.create(FlexProjectMetadata.class.getName(), "flex_project");
    
    private FlexPathResolver pathResolver;

    public FlexProjectMetadata(FlexPathResolver flexPathResolver) {
        super(FLEX_PROJECT_IDENTIFIER);
        this.pathResolver = flexPathResolver;
    }
    
    public FlexPathResolver getPathResolver() {
        return pathResolver;
    }
    
    public static final String getProjectIdentifier() {
        return FLEX_PROJECT_IDENTIFIER;
    }
}
