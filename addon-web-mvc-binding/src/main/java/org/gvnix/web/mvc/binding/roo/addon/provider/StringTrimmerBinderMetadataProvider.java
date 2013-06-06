package org.gvnix.web.mvc.binding.roo.addon.provider;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.mvc.binding.roo.addon.annotation.GvNIXStringTrimmerBinder;
import org.gvnix.web.mvc.binding.roo.addon.metadata.StringTrimmerBinderMetadata;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;

/**
 * Provides {@link StringTrimmerBinderMetadata}. This type is called by Roo to
 * retrieve the metadata for this add-on. Use this type to reference external
 * types and services needed by the metadata type. Register metadata triggers
 * and dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @since 0.8
 */
@Component
@Service
public final class StringTrimmerBinderMetadataProvider extends
        AbstractItdMetadataProvider {

    private static final JavaType GVNIX_STRING_TRIMMER_BINDER = new JavaType(
            GvNIXStringTrimmerBinder.class.getName());

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(GVNIX_STRING_TRIMMER_BINDER);
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(GVNIX_STRING_TRIMMER_BINDER);
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails controllerClassOrInterfaceDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Validate.notNull(
                controllerClassOrInterfaceDetails,
                "Governor failed to provide class type details, in violation of superclass contract");

        AnnotationMetadata stringTrimmerAnnotation = MemberFindingUtils
                .getAnnotationOfType(
                        controllerClassOrInterfaceDetails.getAnnotations(),
                        GVNIX_STRING_TRIMMER_BINDER);

        boolean emptyAsNull = ((BooleanAttributeValue) stringTrimmerAnnotation
                .getAttribute(new JavaSymbolName("emptyAsNull"))).getValue()
                .booleanValue();
        // Pass dependencies required by the metadata in through its constructor
        return new StringTrimmerBinderMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, emptyAsNull);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXStringTrimmerBinder.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXStringTrimmerBinder";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = StringTrimmerBinderMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = StringTrimmerBinderMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(
                javaType,
                LogicalPath.getInstance(path.getPath(),
                        projectOperations.getFocusedModuleName()));
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return StringTrimmerBinderMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return StringTrimmerBinderMetadata.getMetadataIdentiferType();
    }
}
