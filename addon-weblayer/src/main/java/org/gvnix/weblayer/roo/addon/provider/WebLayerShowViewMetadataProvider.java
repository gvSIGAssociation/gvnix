package org.gvnix.weblayer.roo.addon.provider;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.weblayer.roo.addon.annotation.GvNIXWebLayerShow;
import org.gvnix.weblayer.roo.addon.metadata.WebLayerShowViewMetadata;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link WebLayerShowViewMetadata}. This type is called by Roo to
 * retrieve the metadata for this add-on. Use this type to reference external
 * types and services needed by the metadata type. Register metadata triggers
 * and dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.9
 */
@Component
@Service
public final class WebLayerShowViewMetadataProvider extends
    AbstractItdMetadataProvider {

  private static final JavaType GVNIX_WEBLAYER_SHOW_VIEW = new JavaType(
      GvNIXWebLayerShow.class.getName());

  /**
   * The activate method for this OSGi component, this will be called by the
   * OSGi container upon bundle activation (result of the 'addon install'
   * command)
   * 
   * @param context the component context can be used to get access to the OSGi
   *          container (ie find out if certain bundles are active)
   */
  protected void activate(ComponentContext context) {
    metadataDependencyRegistry.registerDependency(
        PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    addMetadataTrigger(GVNIX_WEBLAYER_SHOW_VIEW);
  }

  /**
   * The deactivate method for this OSGi component, this will be called by the
   * OSGi container upon bundle deactivation (result of the 'addon uninstall'
   * command)
   * 
   * @param context the component context can be used to get access to the OSGi
   *          container (ie find out if certain bundles are active)
   */
  protected void deactivate(ComponentContext context) {
    metadataDependencyRegistry.deregisterDependency(
        PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    removeMetadataTrigger(GVNIX_WEBLAYER_SHOW_VIEW);
  }

  /**
   * Return an instance of the Metadata offered by this add-on
   */
  @Override
  protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString,
                                                            JavaType aspectName,
                                                            PhysicalTypeMetadata governorPhysicalTypeMetadata,
                                                            String itdFilename) {

    // We know governor type details are non-null and can be safely cast
    ClassOrInterfaceTypeDetails controllerClassOrInterfaceDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
        .getMemberHoldingTypeDetails();
    Assert
        .notNull(
            controllerClassOrInterfaceDetails,
            "Governor failed to provide class type details, in violation of superclass contract");

    // Pass dependencies required by the metadata in through its constructor
    return new WebLayerShowViewMetadata(metadataIdentificationString,
        aspectName, governorPhysicalTypeMetadata);
  }

  /**
   * Define the unique ITD file name extension, here the resulting file name
   * will be **_ROO_GvNIXStringTrimmerBinder.aj
   */
  public String getItdUniquenessFilenameSuffix() {
    return "GvNIXWebLayerShowView";
  }

  @Override
  protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
    JavaType javaType = WebLayerShowViewMetadata
        .getJavaType(metadataIdentificationString);
    Path path = WebLayerShowViewMetadata.getPath(metadataIdentificationString);
    return PhysicalTypeIdentifier.createIdentifier(javaType, path);
  }

  @Override
  protected String createLocalIdentifier(JavaType javaType, Path path) {
    return WebLayerShowViewMetadata.createIdentifier(javaType, path);
  }

  public String getProvidesType() {
    return WebLayerShowViewMetadata.getMetadataIdentiferType();
  }
}
