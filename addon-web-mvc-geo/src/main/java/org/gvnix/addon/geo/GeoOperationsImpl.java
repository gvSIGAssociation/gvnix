package org.gvnix.addon.geo;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of GEO Addon operations
 * 
 * @since 1.1
 */
@Component
@Service
public class GeoOperationsImpl implements GeoOperations {

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private MetadataService metadataService;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeManagementService typeManagementService;

    private static final Logger LOGGER = Logger
            .getLogger(GeoOperationsImpl.class.getName());

    private static final JavaType SCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    private static final JavaType CONVERSION_SERVICE_ANNOTATION = new JavaType(
            RooConversionService.class.getName());

    private static final JavaType GEO_CONVERSION_SERVICE_ANNOTATION = new JavaType(
            GvNIXGeoConversionService.class.getName());

    /**
     * This method checks if setup command is available
     * 
     * @return true if setup command is available
     */
    @Override
    public boolean isSetupCommandAvailable() {
        return projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-geo-persistence")
                && projectOperations
                        .isFeatureInstalledInFocusedModule("gvnix-jquery");
    }

    /**
     * This method imports all necessary element to build a gvNIX GEO
     * application
     */
    @Override
    public void setup() {
        // Adding project dependencies
        updatePomDependencies();
        // Locate all ApplicationConversionServiceFactoryBean and annotate it
        annotateApplicationConversionService();
    }

    /**
     * This method annotate ApplicationConversionServices classes to transform
     * GEO elements
     */
    public void annotateApplicationConversionService() {
        // Validate that exists web layer
        Set<JavaType> controllers = typeLocationService
                .findTypesWithAnnotation(SCAFFOLD_ANNOTATION);

        Validate.notEmpty(
                controllers,
                "There's not exists any web layer on this gvNIX application. Execute 'web mvc all --package ~.web' to create web layer.");

        for (JavaType conversorService : typeLocationService
                .findTypesWithAnnotation(CONVERSION_SERVICE_ANNOTATION)) {

            Validate.notNull(conversorService, "RooConversionService required");

            ClassOrInterfaceTypeDetails applicationConversionService = typeLocationService
                    .getTypeDetails(conversorService);

            // Only for @RooConversionService annotated controllers
            final AnnotationMetadata rooConversionServiceAnnotation = MemberFindingUtils
                    .getAnnotationOfType(
                            applicationConversionService.getAnnotations(),
                            CONVERSION_SERVICE_ANNOTATION);

            Validate.isTrue(rooConversionServiceAnnotation != null,
                    "Operation for @RooConversionService annotated classes only.");

            final boolean isGeoConversionServiceAnnotated = MemberFindingUtils
                    .getAnnotationOfType(
                            applicationConversionService.getAnnotations(),
                            GEO_CONVERSION_SERVICE_ANNOTATION) != null;

            // If annotation already exists on the target type do nothing
            if (isGeoConversionServiceAnnotated) {
                return;
            }

            ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    applicationConversionService);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    GEO_CONVERSION_SERVICE_ANNOTATION);

            // Add annotation to target type
            detailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(detailsBuilder
                    .build());
        }
    }

    /**
     * This method updates Pom dependencies and repositories
     */
    public void updatePomDependencies() {
        final Element configuration = XmlUtils.getConfiguration(getClass());
        GeoUtils.updatePom(configuration, projectOperations, metadataService);
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
        return FEATURE_NAME_GVNIX_GEO;
    }

    /**
     * Returns true if GEO is installed
     */
    @Override
    public boolean isInstalledInModule(String moduleName) {
        return true;
    }

}