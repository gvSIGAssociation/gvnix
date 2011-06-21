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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;

/**
 * Abstract dynamic configuration of Java class annotation attributes.
 * <p>
 * This component manage the annotation attribute values of certain Java classes
 * with some annotation.
 * </p>
 * <p>
 * Only annotation string attributes are considered.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(componentAbstract = true)
public abstract class AnnotationClassDynamicConfiguration extends
        AbstractItdMetadataProvider {

    private final List<PhysicalTypeMetadata> types = new ArrayList<PhysicalTypeMetadata>();

    @Reference
    private TypeLocationService typeLocationService;

    private static final Logger logger = HandlerUtils
            .getLogger(AnnotationClassDynamicConfiguration.class);

    /**
     * Get the java type related to the annotation to include as dynamic
     * configuration.
     * 
     * @return Annotation java type
     */
    protected abstract JavaType getAnnotationJavaType();

    /**
     * Get the fully qualified type name of managed annotation.
     * 
     * @return Fully qualified type name
     */
    protected String getAnnotationTypeName() {

        return getAnnotationJavaType().getFullyQualifiedTypeName();
    }

    /**
     * Add trigger for classes with some annotation.
     * 
     * @param context
     *            OSGi context
     */
    protected void activate(ComponentContext context) {

        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
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
                    (type.getMemberHoldingTypeDetails()),
                    getAnnotationJavaType());
            List<JavaSymbolName> attrs = annot.getAttributeNames();

            // Iterate all attributes to create their dynamic configuration
            for (JavaSymbolName attr : attrs) {

                // Dynamic property with attribute name and value
                AnnotationAttributeValue<?> value = annot
                        .getAttribute(new JavaSymbolName(attr.getSymbolName()));

                // Only annotation string attributes are considered
                if (value instanceof StringAttributeValue) {

                    dynProps.add(new DynProperty(type
                            .getMemberHoldingTypeDetails().getName()
                            + "/"
                            + annot.getAnnotationType()
                            + "/"
                            + attr.getSymbolName(), (String) value.getValue()));
                }
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

            MutableClassOrInterfaceTypeDetails mutableClass = null;
            List<AnnotationMetadata> annots = new ArrayList<AnnotationMetadata>();
            try {

                JavaType javaType = new JavaType(javaName);
                ClassOrInterfaceTypeDetails javaClass = typeLocationService
                        .getClassOrInterface(javaType);

                // Check and get mutable instance
                Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                        javaClass, "Can't modify " + javaClass.getName());
                mutableClass = (MutableClassOrInterfaceTypeDetails) javaClass;
                annots = mutableClass.getAnnotations();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Class " + javaName
                        + " to update annotation attribute value not exists");
                continue;
            }

            // Get annotation and attribute name
            int annotEnd = key.indexOf("/", javaEnd + 1);
            String annotName = key.substring(javaEnd + 1, annotEnd);
            String attrName = key.substring(annotEnd + 1, key.length());

            // Iterate all java class annotations
            AnnotationMetadata annotation = null;
            boolean exists = false;
            for (AnnotationMetadata annot : annots) {

                // If any java class annotation type equals to this class type
                if (annot.getAnnotationType().equals(getAnnotationJavaType())) {

                    if (annot.getAttribute(new JavaSymbolName(attrName)) == null) {

                        // Referenced attribute not exists on annotation
                        logger.log(
                                Level.WARNING,
                                "On class "
                                        + javaName
                                        + " not exists the annotation attribute to update");
                    } else {

                        // Take the annotation with the attribute
                        annotation = annot;
                    }

                    exists = true;
                    break;
                }
            }

            if (!exists) {

                // Referenced annotation not exists
                logger.log(
                        Level.WARNING,
                        "On class "
                                + javaName
                                + " not exists the annotation to update the attribute value");
            }

            if (annotation != null) {

                // Drop current annotation
                mutableClass.removeTypeAnnotation(annotation
                        .getAnnotationType());

                // Add the same annotation with new attribute value
                List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
                attrs.add(new StringAttributeValue(
                        new JavaSymbolName(attrName), dynProp.getValue()));
                AnnotationMetadataBuilder newAnnotBuilder = new AnnotationMetadataBuilder(
                        new JavaType(annotName), attrs);
                mutableClass.addTypeAnnotation(newAnnotBuilder.build());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataId, JavaType type, PhysicalTypeMetadata metadata,
            String file) {

        addTypeMetadata(metadata);
        return null;
    }

    /**
     * Registers classes with some annotation.
     * 
     * @param Metadata
     *            type to register
     */
    private void addTypeMetadata(PhysicalTypeMetadata type) {

        if (!this.types.contains(type)) {
            this.types.add(type);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createLocalIdentifier(JavaType type, Path path) {

        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                getAnnotationTypeName(), type, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGovernorPhysicalTypeIdentifier(String metadataId) {

        JavaType type = PhysicalTypeIdentifierNamingUtils.getJavaType(
                getAnnotationTypeName(), metadataId);
        Path path = PhysicalTypeIdentifierNamingUtils.getPath(
                getAnnotationTypeName(), metadataId);
        String physicalTypeIdentifier = PhysicalTypeIdentifier
                .createIdentifier(type, path);

        return physicalTypeIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public String getItdUniquenessFilenameSuffix() {

        return getAnnotationTypeName();
    }

    /**
     * {@inheritDoc}
     */
    public String getProvidesType() {

        return MetadataIdentificationUtils.create(getAnnotationTypeName());
    }

}
