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
import org.gvnix.support.MetadataUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
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

    @Reference
    private MemberDetailsScanner memberDetailsScanner;

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
        MutableClassOrInterfaceTypeDetails formBackingTypeMetadata = MetadataUtils
                .getPhysicalTypeDetails(formBakingObjectType, metadataService,
                        physicalTypeMetadataProvider);

        MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(
                getClass().getName(), formBackingTypeMetadata);

        List<FieldMetadata> fields = MemberFindingUtils
                .getFields(memberDetails);

        Assert.notNull(formBackingTypeMetadata, "Cannot locate Metadata for '"
                .concat(formBakingObjectType.getFullyQualifiedTypeName())
                .concat("'."));

        // List<? extends FieldMetadata> fields = formBackingTypeMetadata
        // .getDeclaredFields();

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
    @Deprecated
    public MutableClassOrInterfaceTypeDetails getPhysicalTypeDetails(
            JavaType type) {

        return MetadataUtils.getPhysicalTypeDetails(type, metadataService,
                physicalTypeMetadataProvider);
    }
}
