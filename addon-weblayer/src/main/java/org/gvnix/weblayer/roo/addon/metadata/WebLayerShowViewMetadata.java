package org.gvnix.weblayer.roo.addon.metadata;

import java.lang.reflect.Modifier;

import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register the code in
 * the ITD.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.9
 */
public class WebLayerShowViewMetadata extends
    AbstractItdTypeDetailsProvidingMetadataItem {

  private static final String PROVIDES_TYPE_STRING = WebLayerShowViewMetadata.class
      .getName();

  private static final String PROVIDES_TYPE = MetadataIdentificationUtils
      .create(PROVIDES_TYPE_STRING);

  public WebLayerShowViewMetadata(String identifier, JavaType aspectName,
      PhysicalTypeMetadata governorPhysicalTypeMetadata) {
    super(identifier, aspectName, governorPhysicalTypeMetadata);
    Assert.isTrue(isValid(identifier), "Metadata identification string '"
        + identifier + "' does not appear to be a valid");

    // Adding a new method definition
    builder.addMethod(getCutomizeViewAndComponentsMethod());

    // Create a representation of the desired output ITD
    itdTypeDetails = builder.build();
  }

  /**
   * Builds the method initStringTrimmerBinder annotated with @InitBinder. This
   * method registers the StringTrimmerEditor
   * 
   * @param emptyAsNull if true the editor registered will convert empty Strings
   *          to <code>null</code>
   */
  private MethodMetadata getCutomizeViewAndComponentsMethod() {
    // Specify the desired method name
    JavaSymbolName methodName = new JavaSymbolName("cusotmizeViewAndComponets");

    // Check if a method with the same name already exists in the
    // target type
    MethodMetadata method = methodExists(methodName);
    if (method != null) {
      // If it already exists, just return the method and omit its
      // generation via the ITD
      return method;
    }

    // Create the method body
    InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
    bodyBuilder.appendFormalLine("// TODO write something here");

    // Use the MethodMetadataBuilder for easy creation of MethodMetadata
    MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(),
        Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE, bodyBuilder);

    return methodBuilder.build();
  }

  private MethodMetadata methodExists(JavaSymbolName methodName) {
    for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
      if (method.getMethodName().equals(methodName)) {
        return method;
      }
    }
    return null;
  }

  // Typically, no changes are required beyond this point

  @Override
  public String toString() {
    ToStringCreator tsc = new ToStringCreator(this);
    tsc.append("identifier", getId());
    tsc.append("valid", valid);
    tsc.append("aspectName", aspectName);
    tsc.append("destinationType", destination);
    tsc.append("governor", governorPhysicalTypeMetadata.getId());
    tsc.append("itdTypeDetails", itdTypeDetails);
    return tsc.toString();
  }

  public static final String getMetadataIdentiferType() {
    return PROVIDES_TYPE;
  }

  public static final String createIdentifier(JavaType javaType, Path path) {
    return PhysicalTypeIdentifierNamingUtils.createIdentifier(
        PROVIDES_TYPE_STRING, javaType, path);
  }

  public static final JavaType getJavaType(String metadataIdentificationString) {
    return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING,
        metadataIdentificationString);
  }

  public static final Path getPath(String metadataIdentificationString) {
    return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
        metadataIdentificationString);
  }

  public static boolean isValid(String metadataIdentificationString) {
    return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
        metadataIdentificationString);
  }
}
