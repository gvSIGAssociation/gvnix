package org.gvnix.weblayer.roo.addon.provider;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.weblayer.roo.addon.annotation.GvNIXWebLayer;
import org.gvnix.weblayer.roo.addon.metadata.WebLayerShowMetadata;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link WebLayerShowMetadata}. This type is called by Roo to retrieve
 * the metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 * @since 0.9
 */
@Component
@Service
public final class WebLayerShowMetadataProvider extends
        AbstractItdMetadataProvider {

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebLayer.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXWebLayer.class.getName()));
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
        ClassOrInterfaceTypeDetails classOrInterfaceDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(
                classOrInterfaceDetails,
                "Governor failed to provide class type details, in violation of superclass contract");

        // We need to know the metadata of the Entity through
        JavaType entityType = WebLayerShowMetadata
                .getJavaType(metadataIdentificationString);
        Path path = WebLayerShowMetadata.getPath(metadataIdentificationString);
        String entityMetadataKey = EntityMetadata.createIdentifier(entityType,
                path);
        EntityMetadata entityMetadata = (EntityMetadata) metadataService
                .get(entityMetadataKey);
        if (entityMetadata == null) {
            // Metadata not available yet, do nothing on this invoke
            return null;
        }

        // Pass dependencies required by the metadata in through its constructor
        return new WebLayerShowMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, entityMetadata);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXWebLayerShow.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXWebLayer";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = WebLayerShowMetadata
                .getJavaType(metadataIdentificationString);
        Path path = WebLayerShowMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return WebLayerShowMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return WebLayerShowMetadata.getMetadataIdentiferType();
    }
}