/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

/**
 * Abstract dynamic configuration of Java class annotation attributes.
 * <p>
 * This component manage the annotation attribute values of certain Java classes
 * with some annotation.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(componentAbstract = true)
public abstract class AnnotationDynamicConfiguration extends
    AbstractItdMetadataProvider {

  private static final String ANNOTATION_TYPE_STRING = AnnotationDynamicConfiguration.class
      .getName();
  private static final String ANNOTATION_TYPE = MetadataIdentificationUtils
      .create(ANNOTATION_TYPE_STRING);
  
  private List<PhysicalTypeMetadata> typesMetadata = new ArrayList<PhysicalTypeMetadata>();
  
  /**
   * Get the java type related to the annotation to include as dynamic configuration.
   * 
   * @return Annotation java type
   */
  protected abstract JavaType getAnnotationJavaType();

  /**
   * Add trigger for classes with some annotation.
   * 
   * @param context OSGi context
   */
  protected void activate(ComponentContext context) {

    metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
        .getMetadataIdentiferType(), getProvidesType());
    addMetadataTrigger(getAnnotationJavaType());
  }

  /**
   * {@inheritDoc}
   */
  public DynPropertyList read() {

    DynPropertyList dynProps = new DynPropertyList();
    for (PhysicalTypeMetadata typeMetadata : typesMetadata) {

      AnnotationMetadata annotation = MemberFindingUtils
          .getTypeAnnotation(((ClassOrInterfaceTypeDetails) typeMetadata
              .getPhysicalTypeDetails()), getAnnotationJavaType());
      List<JavaSymbolName> attrs = annotation.getAttributeNames();

      for (JavaSymbolName attr : attrs) {
        StringAttributeValue value = (StringAttributeValue) annotation
            .getAttribute(new JavaSymbolName(attr.getSymbolName()));
        dynProps.add(new DynProperty(typeMetadata
            .getPhysicalLocationCanonicalPath()
            + "/" + attr.getSymbolName(), value.getValue()));
      }
    }

    return dynProps;
  }

  /**
   * {@inheritDoc}
   */
  public void write(DynPropertyList dynProps) {

  }

  /**
   * {@inheritDoc}
   */
  protected ItdTypeDetailsProvidingMetadataItem getMetadata(
                                                            String metadataIdentificationString,
                                                            JavaType aspectName,
                                                            PhysicalTypeMetadata governorPhysicalTypeMetadata,
                                                            String itdFilename) {

    addTypeMetadata(governorPhysicalTypeMetadata);
    return null;
  }

  /**
   * Registers classes with some annotation.
   * 
   * @param Metadata type to register
   */
  private void addTypeMetadata(PhysicalTypeMetadata typeMetadata) {
    
    if (!this.typesMetadata.contains(typeMetadata)) {
      this.typesMetadata.add(typeMetadata);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  protected String createLocalIdentifier(JavaType javaType, Path path) {

    return PhysicalTypeIdentifierNamingUtils.createIdentifier(
        ANNOTATION_TYPE_STRING, javaType, path);
  }

  /**
   * {@inheritDoc}
   */
  protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {

    JavaType javaType = PhysicalTypeIdentifierNamingUtils.getJavaType(
        ANNOTATION_TYPE_STRING, metadataIdentificationString);
    Path path = PhysicalTypeIdentifierNamingUtils.getPath(ANNOTATION_TYPE_STRING,
        metadataIdentificationString);
    String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(
        javaType, path);

    return physicalTypeIdentifier;
  }

  /**
   * {@inheritDoc}
   */
  public String getItdUniquenessFilenameSuffix() {

    return ANNOTATION_TYPE_STRING;
  }

  /**
   * {@inheritDoc}
   */
  public String getProvidesType() {

    return ANNOTATION_TYPE;
  }

}
