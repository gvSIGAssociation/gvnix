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
package org.gvnix.service.roo.addon.ws.export;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.JavaParserService;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.ws.WSConfigService.WsType;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

/**
 * <p>
 * Checks if @GvNIXXmlElement annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class WSExportXmlElementMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private WSConfigService wSConfigService;
    @Reference
    private JavaParserService javaParserService;

    protected void activate(ComponentContext context) {

        // Notify when physical type with gvNIX xml element annotation modified
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXXmlElement.class.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * createLocalIdentifier(org.springframework.roo.model.JavaType,
     * org.springframework.roo.project.Path)
     */
    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {

        // Get annotation identifier for this java type at path
        return WSExportXmlElementMetadata.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {

        JavaType javaType = WSExportXmlElementMetadata
                .getJavaType(metadataIdentificationString);
        Path path = WSExportXmlElementMetadata
                .getPath(metadataIdentificationString);

        // Get physical type identifier for this java type
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata
     * (java.lang.String, org.springframework.roo.model.JavaType,
     * org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String id,
            JavaType aspectName, PhysicalTypeMetadata physicalType,
            String itdFilename) {

        // Install web service (dependencies, properties, plugins and config)
        wSConfigService.install(WsType.EXPORT);

        // We know governor type details are non-null and can be safely cast

        // Check if Web Service definition is correct
        // TODO What is this for ?
        PhysicalTypeDetails physicalTypeDetails = physicalType
                .getMemberHoldingTypeDetails();
        if (physicalTypeDetails == null
                || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {

            // There is a problem
            return null;
        }

        // We have reliable physical type details
        ClassOrInterfaceTypeDetails typeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;

        // Work out the MIDs of the other metadata we depend on
        String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                WSExportXmlElementMetadata.getJavaType(id),
                WSExportXmlElementMetadata.getPath(id));

        // We need to be informed if our dependent metadata changes
        // TODO What is this for ?
        metadataDependencyRegistry.registerDependency(physicalTypeId, id);

        AnnotationMetadata annotation = MemberFindingUtils.getTypeAnnotation(
                typeDetails, new JavaType(GvNIXXmlElement.class.getName()));

        // Create metaData with field list values.
        return new WSExportXmlElementMetadata(id, aspectName, physicalType,
                getDeclaredFields(typeDetails, annotation), javaParserService);
    }

    /**
     * Get defined fields to be exported as elements in XSD schema.
     * 
     * <ul>
     * <li>Fields defined in 'elementList' annotation attribute will exported</li>
     * <li>Only executed if typeDetails is a class</li>
     * </ul>
     * 
     * @param typeDetails
     *            class to get fields to check
     * @param annotation
     *            to check element values.
     * @return {@link List} of annotated {@link FieldMetadata}
     */
    protected List<FieldMetadata> getDeclaredFields(
            ClassOrInterfaceTypeDetails typeDetails,
            AnnotationMetadata annotation) {

        // Redefine field lists.
        List<FieldMetadata> declaredFields = new ArrayList<FieldMetadata>();

        if (typeDetails.getPhysicalTypeCategory().equals(
                PhysicalTypeCategory.CLASS)) {

            // Get gvNIX xml element element list attributes
            @SuppressWarnings("unchecked")
            ArrayAttributeValue<StringAttributeValue> elementListAttr = (ArrayAttributeValue<StringAttributeValue>) annotation
                    .getAttribute(new JavaSymbolName("elementList"));

            // Iterate all element list names
            for (StringAttributeValue elementListName : elementListAttr
                    .getValue()) {

                // Add field as declared into list
                declaredFields.add(javaParserService.getFieldByNameInAll(
                        typeDetails.getName(), new JavaSymbolName(
                                elementListName.getValue())));
            }
        }

        return declaredFields;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {

        // Aspect Java file name sufix
        return "GvNix_XmlElement";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {

        // Get metadata identifier for this annotation
        return WSExportXmlElementMetadata.getMetadataIdentiferType();
    }

}
