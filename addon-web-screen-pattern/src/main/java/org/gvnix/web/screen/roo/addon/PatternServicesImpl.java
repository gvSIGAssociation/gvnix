/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.screen.roo.addon;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.Assert;

/**
 * Provide common services to Screen Pattern management components
 * 
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
@Component
@Service
public class PatternServicesImpl implements PatternService {

    public static final JavaType ONETOMANY_ANNOTATION = new JavaType(
            "javax.persistence.OneToMany");

    @Reference
    private MetadataService metadataService;

    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    /** {@inheritDoc} */
    public String findPatternDefinedMoreThanOnceInProject() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public FieldMetadata getOneToManyFieldFromEntityJavaType(
            JavaType formBakingObjectType, String fieldName) {
        List<FieldMetadata> oneToManyFields = getOneToManyFieldsFromEntityJavaType(formBakingObjectType);
        for (FieldMetadata field : oneToManyFields) {
            if (field.getFieldName().getSymbolName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<FieldMetadata> getOneToManyFieldsFromEntityJavaType(
            JavaType formBakingObjectType) {
        List<FieldMetadata> oneToManyFields = new ArrayList<FieldMetadata>();
        MutableClassOrInterfaceTypeDetails formBackingTypeMetadata = getPhysicalTypeDetails(formBakingObjectType);

        Assert.notNull(formBackingTypeMetadata, "Cannot locate Metadata for '"
                .concat(formBakingObjectType.getFullyQualifiedTypeName())
                .concat("'."));

        List<? extends FieldMetadata> fields = formBackingTypeMetadata
                .getDeclaredFields();

        for (FieldMetadata field : fields) {
            for (AnnotationMetadata fieldAnnotation : field.getAnnotations()) {
                if (fieldAnnotation.getAnnotationType().equals(
                        ONETOMANY_ANNOTATION)) {
                    oneToManyFields.add(field);
                }
            }
        }
        return oneToManyFields;
    }

    /** {@inheritDoc} */
    public MutableClassOrInterfaceTypeDetails getPhysicalTypeDetails(
            JavaType type) {
        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String entityId = physicalTypeMetadataProvider.findIdentifier(type);
        if (entityId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + type.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(entityId, true);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type ".concat(type
                        .getFullyQualifiedTypeName()));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type ".concat(type
                        .getFullyQualifiedTypeName()));
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(type.getFullyQualifiedTypeName()));
        return (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;
    }
}
