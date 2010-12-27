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
import org.apache.felix.scr.annotations.Reference;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Abstract dynamic configuration of Java class annotation attributes.
 * <p>
 * This component manage the annotation attribute values of certain Java classes
 * with some annotation.
 * </p>
 * 
 * TODO Annotations can appear multiple times
 * TODO Look at String attributes only
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(componentAbstract = true)
public abstract class AnnotationDynamicConfiguration extends
    AbstractItdMetadataProvider {

  private static final String TYPE_ID = AnnotationDynamicConfiguration.class
      .getName();

  private List<PhysicalTypeMetadata> types = new ArrayList<PhysicalTypeMetadata>();
  
  @Reference
  private ClasspathOperations classpathOperations;
  
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

    // List to include the dynamic property list from types
    DynPropertyList dynProps = new DynPropertyList();
    for (PhysicalTypeMetadata type : types) {

      // Get the annotation attributes from type
      AnnotationMetadata annot = MemberFindingUtils.getTypeAnnotation(
          ((ClassOrInterfaceTypeDetails) type.getPhysicalTypeDetails()),
          getAnnotationJavaType());
      List<JavaSymbolName> attrs = annot.getAttributeNames();

      // Iterate all attributes to create their dynamic configuration
      for (JavaSymbolName attr : attrs) {

        // Dynamic property with attribute name and value
        StringAttributeValue value = (StringAttributeValue) annot
            .getAttribute(new JavaSymbolName(attr.getSymbolName()));
        dynProps.add(new DynProperty(type.getPhysicalTypeDetails().getName()
            + "/" + annot.getAnnotationType() + "/" + attr.getSymbolName(), value.getValue()));
      }
    }

    return dynProps;
  }

  /**
   * {@inheritDoc}
   */
  public void write(DynPropertyList dynProps) {

    // Iterate all dynamic properties to update java annotation attributes
    for (DynProperty dynProp : dynProps) {

      // Dynamic property reference key
      String key = dynProp.getKey();

      // Obtain the java class from key
      int javaEnd = key.indexOf("/");
      String javaName = key.substring(0, javaEnd);
      JavaType javaType = new JavaType(javaName);
      ClassOrInterfaceTypeDetails javaClass = classpathOperations
          .getClassOrInterface(javaType);

      // Check and get mutable instance
      Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, javaClass,
          "Can't modify " + javaClass.getName());
      MutableClassOrInterfaceTypeDetails mutableClass = (MutableClassOrInterfaceTypeDetails) javaClass;

      // Iterate all java class annotations 
      for (AnnotationMetadata annot : mutableClass.getTypeAnnotations()) {

        // If any java class annotation type equals to this class type
        if (annot.getAnnotationType().equals(getAnnotationJavaType())) {

          // Drop current annotation
          mutableClass.removeTypeAnnotation(annot.getAnnotationType());

          // Get annotation and attribute name
          int annotEnd = key.indexOf("/", javaEnd + 1);
          String annotName = key.substring(javaEnd + 1, annotEnd);
          String attrName = key.substring(annotEnd + 1, key.length());

          // Add the same annotation with new attribute value
          List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
          attrs.add(new StringAttributeValue(new JavaSymbolName(attrName),
              dynProp.getValue()));
          AnnotationMetadata newAnnot = new DefaultAnnotationMetadata(
              new JavaType(annotName), attrs);
          mutableClass.addTypeAnnotation(newAnnot);

          break;
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  protected ItdTypeDetailsProvidingMetadataItem getMetadata(
                                                            String metadataId,
                                                            JavaType type,
                                                            PhysicalTypeMetadata metadata,
                                                            String file) {

    addTypeMetadata(metadata);
    return null;
  }

  /**
   * Registers classes with some annotation.
   * 
   * @param Metadata type to register
   */
  private void addTypeMetadata(PhysicalTypeMetadata type) {
    
    if (!this.types.contains(type)) {
      this.types.add(type);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  protected String createLocalIdentifier(JavaType type, Path path) {

    return PhysicalTypeIdentifierNamingUtils.createIdentifier(TYPE_ID, type,
        path);
  }

  /**
   * {@inheritDoc}
   */
  protected String getGovernorPhysicalTypeIdentifier(String metadataId) {

    JavaType type = PhysicalTypeIdentifierNamingUtils.getJavaType(TYPE_ID,
        metadataId);
    Path path = PhysicalTypeIdentifierNamingUtils.getPath(TYPE_ID, metadataId);
    String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(
        type, path);

    return physicalTypeIdentifier;
  }

  /**
   * {@inheritDoc}
   */
  public String getItdUniquenessFilenameSuffix() {

    return TYPE_ID;
  }

  /**
   * {@inheritDoc}
   */
  public String getProvidesType() {

    return MetadataIdentificationUtils.create(TYPE_ID);
  }

}
