package org.gvnix.weblayer.roo.addon.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.weblayer.roo.addon.GvNIXRooUtils;
import org.gvnix.weblayer.roo.addon.annotation.GvNIXWebLayer;
import org.gvnix.weblayer.roo.addon.entityshow.WebLayerEntityViewShowHelper;
import org.gvnix.weblayer.roo.addon.metadata.WebLayerShowMetadata;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
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
 * @since 0.9
 */
@Component
@Service
public final class WebLayerShowMetadataProvider implements MetadataProvider,
    MetadataNotificationListener {

  // private static final Logger logger = HandlerUtils
  // .getLogger(WebLayerShowMetadataProvider.class);

  @Reference
  private MetadataDependencyRegistry metadataDependencyRegistry;

  @Reference
  private MemberDetailsScanner memberDetailsScanner;

  @Reference
  private MetadataService metadataService;

  @Reference
  private ProjectOperations projectOperations;

  @Reference
  private TypeLocationService typeLocationService;

  @Reference
  private FileManager fileManager;

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
    // addMetadataTrigger(new JavaType(GvNIXWebLayer.class.getName()));
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
    // removeMetadataTrigger(new JavaType(GvNIXWebLayer.class.getName()));
  }

  public MetadataItem get(String metadataIdentificationString) {
    // Abort early if we can't continue
    ProjectMetadata projectMetadata = projectOperations.getProjectMetadata();
    if (projectMetadata == null) {
      return null;
    }

    // Here, governorType should be an Entity
    JavaType governorType = WebLayerShowMetadata
        .getJavaType(metadataIdentificationString);
    Path governorTypePath = WebLayerShowMetadata
        .getPath(metadataIdentificationString);

    String governorTypeMetadataKey = EntityMetadata.createIdentifier(
        governorType, governorTypePath);
    EntityMetadata governorTypeMetadata = (EntityMetadata) metadataService
        .get(governorTypeMetadataKey);
    if (governorTypeMetadata == null) {
      // Metadata not available yet, do nothing on this invoke
      return null;
    }

    String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
        governorType, governorTypePath);
    PhysicalTypeMetadata governorPhysicalTypeMetadata = (PhysicalTypeMetadata) metadataService
        .get(physicalTypeId);
    if (governorPhysicalTypeMetadata == null
        || !governorPhysicalTypeMetadata.isValid()
        || !(governorPhysicalTypeMetadata.getMemberHoldingTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
      return null;
    }
    ClassOrInterfaceTypeDetails governorTypeDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
        .getMemberHoldingTypeDetails();
    Assert
        .notNull(
            governorTypeDetails,
            "Governor failed to provide class type details, in violation of superclass contract");

    AnnotationMetadata gvNIXWebLayerShow = MemberFindingUtils
        .getAnnotationOfType(governorTypeDetails.getAnnotations(),
            new JavaType(GvNIXWebLayer.class.getName()));
    if (gvNIXWebLayerShow != null) {
      String viewPackage = gvNIXWebLayerShow
          .getAttribute(new JavaSymbolName("viewPackage")).getValue()
          .toString();

      // Create a new WebLayerViewShowMetadata instance. Currently this is
      // unnecessary since WebLayerViewShowMetadata is generating nothing
      WebLayerShowMetadata showMetadata = new WebLayerShowMetadata(
          metadataIdentificationString, governorType,
          governorPhysicalTypeMetadata, governorTypeMetadata);

      // Create a class <governoTypeName>ViewShow.java based in a template with
      // the metadata defining the show view.
      // TODO orovira: I'm pretty sure that the generation of this class would
      // be better if it is done without the template. Without template we have
      // more control in the roundtrips between our generation and the final
      // user customizations (via visual designer). Another way to get more
      // control could be to modify the WebLayerEntityViewShowHelper making it
      // smarter and taking care with the differences between the code it
      // generates and the one in the current state of view class.
      Map<String, String> substitutions = new HashMap<String, String>();
      substitutions.put("__ENTITY_SHOW_PACKAGE__", viewPackage);
      substitutions.put("__ENTITY_SHOW_CLASS__", governorType
          .getSimpleTypeName().concat("ViewShow"));

      WebLayerEntityViewShowHelper helper = new WebLayerEntityViewShowHelper(
          metadataService, memberDetailsScanner, governorType,
          WebLayerEntityViewShowHelper.INDENT, "mainLayout");
      substitutions
          .put("__FIELD_INSERT_POINT__", helper.getFieldDeclarations());
      substitutions.put("__FIELD_CREATION_INSERT_POINT__",
          helper.getFieldCreationStatements());

      JavaType form = new JavaType(viewPackage.concat(".").concat(
          governorType.getSimpleTypeName() + "ViewShow"));
      String viewShowResourceIdentifier = typeLocationService
          .getPhysicalLocationCanonicalPath(form, Path.SRC_MAIN_JAVA);

      GvNIXRooUtils.installFromTemplateIfNeeded(fileManager,
          viewShowResourceIdentifier, "WebLayerEntityShow.java", substitutions);

      return showMetadata;
    }
    return null;
  }

  /**
   * TODO to study how to check if this provider should or not to be notified.
   * This provider / listener should be invoked just when the governor type is
   * annotated with {@link GvNIXWebLayer}, but it has noticed that types without
   * the annotation invokes this provider
   */
  public void notify(String upstreamDependency, String downstreamDependency) {
    ProjectMetadata projectMetadata = projectOperations.getProjectMetadata();
    if (projectMetadata == null) {
      return;
    }

    if (MetadataIdentificationUtils.isIdentifyingClass(downstreamDependency)) {
      Assert.isTrue(
          MetadataIdentificationUtils.getMetadataClass(upstreamDependency)
              .equals(
                  MetadataIdentificationUtils
                      .getMetadataClass(PhysicalTypeIdentifier
                          .getMetadataIdentiferType())),
          "Expected class-level notifications only for PhysicalTypeIdentifier (not '"
              + upstreamDependency + "')");

      // A physical Java type has changed, and determine what the corresponding
      // local metadata identification string would have been
      JavaType typeName = PhysicalTypeIdentifier
          .getJavaType(upstreamDependency);
      Path typePath = PhysicalTypeIdentifier.getPath(upstreamDependency);
      downstreamDependency = createLocalIdentifier(typeName, typePath);

      // We only need to proceed if the downstream dependency relationship is
      // not already registered (if it's already registered, the event will be
      // delivered directly later on)
      if (metadataDependencyRegistry.getDownstream(upstreamDependency)
          .contains(downstreamDependency)) {
        return;
      }
    }

    // We should now have an instance-specific "downstream dependency" that can
    // be processed by this class
    Assert
        .isTrue(
            MetadataIdentificationUtils.getMetadataClass(downstreamDependency)
                .equals(
                    MetadataIdentificationUtils
                        .getMetadataClass(getProvidesType())),
            "Unexpected downstream notification for '" + downstreamDependency
                + "' to this provider (which uses '" + getProvidesType() + "'");

    metadataService.get(downstreamDependency, true);

  }

  /**
   * Define the unique ITD file name extension, here the resulting file name
   * will be **_ROO_GvNIXWebLayerShow.aj
   */
  public String getItdUniquenessFilenameSuffix() {
    return "GvNIXWebLayer";
  }

  // @Override
  protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
    JavaType javaType = WebLayerShowMetadata
        .getJavaType(metadataIdentificationString);
    Path path = WebLayerShowMetadata.getPath(metadataIdentificationString);
    return PhysicalTypeIdentifier.createIdentifier(javaType, path);
  }

  // @Override
  protected String createLocalIdentifier(JavaType javaType, Path path) {
    return WebLayerShowMetadata.createIdentifier(javaType, path);
  }

  public String getProvidesType() {
    return WebLayerShowMetadata.getMetadataIdentiferType();
  }

}