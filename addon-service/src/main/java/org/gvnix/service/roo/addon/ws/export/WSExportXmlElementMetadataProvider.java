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
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.ws.WSConfigService.CommunicationSense;
import org.gvnix.service.roo.addon.ws.export.WSExportOperations.MethodParameterType;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

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
    private WSExportValidationService wSExportValidationService;
    @Reference
    private AnnotationsService annotationsService;

    private EntityMetadata entityMetadata;

    private List<FieldMetadata> fieldMetadataElementList = new ArrayList<FieldMetadata>();

    private List<FieldMetadata> fieldMetadataTransientList = new ArrayList<FieldMetadata>();

    protected void activate(ComponentContext context) {
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
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
        String physicalTypeIdentifier = PhysicalTypeIdentifier
                .createIdentifier(javaType, path);
        return physicalTypeIdentifier;
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
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        WSExportXmlElementMetadata wSExportXmlElementMetadata = null;

        if (wSConfigService.isProjectWebAvailable()) {

            // Install configuration to export services if it's not installed.
            wSConfigService.install(CommunicationSense.EXPORT);
            // Installs jax2ws plugin in project.
            wSConfigService.installJaxwsBuildPlugin();
            // Add GvNixAnnotations to the project.
            annotationsService.addGvNIXAnnotationsDependency();

            // We know governor type details are non-null and can be safely cast

            // Work out the MIDs of the other metadata we depend on
            JavaType javaType = WSExportXmlElementMetadata
                    .getJavaType(metadataIdentificationString);
            Path path = WSExportXmlElementMetadata
                    .getPath(metadataIdentificationString);
            String entityMetadataKey = EntityMetadata.createIdentifier(
                    javaType, path);
            String physicalTypeKey = PhysicalTypeIdentifier.createIdentifier(
                    javaType, path);

            // Check if Web Service definition is correct.
            PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                    .getMemberHoldingTypeDetails();

            ClassOrInterfaceTypeDetails governorTypeDetails;
            if (physicalTypeDetails == null
                    || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
                // There is a problem
                return null;
            } else {
                // We have reliable physical type details
                governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
            }

            // Reset entityMetadata.
            entityMetadata = null;
            // We need to lookup the metadata we depend on
            MetadataItem item = metadataService.get(entityMetadataKey);
            if (item != null) {
                entityMetadata = (EntityMetadata) item;
            }

            // We need to be informed if our dependent metadata changes
            metadataDependencyRegistry.registerDependency(physicalTypeKey,
                    metadataIdentificationString);

            // Redefine field lists.
            fieldMetadataElementList = new ArrayList<FieldMetadata>();
            fieldMetadataTransientList = new ArrayList<FieldMetadata>();

            AnnotationMetadata gvNixXmlElementAnnotationMetadata = MemberFindingUtils
                    .getTypeAnnotation(governorTypeDetails, new JavaType(
                            GvNIXXmlElement.class.getName()));

            // Field @XmlElement and @XmlTransient annotations lists.
            setTransientAndElementFields(governorTypeDetails,
                    gvNixXmlElementAnnotationMetadata, physicalTypeKey);

            // Create metaData with field list values.
            wSExportXmlElementMetadata = new WSExportXmlElementMetadata(
                    metadataIdentificationString, aspectName,
                    governorPhysicalTypeMetadata, fieldMetadataElementList,
                    fieldMetadataTransientList);

        }

        return wSExportXmlElementMetadata;
    }

    /**
     * Check correct format for annotation attribute values and define which
     * fields will be exported in XSD schema.
     * <p>
     * Fields defined in 'elementList' annotation attribute will be exported.
     * </p>
     * 
     * @param governorTypeDetails
     *            class to get fields to check.
     * @param gvNixXmlElementAnnotationMetadata
     *            to check element values.
     * @param physicalTypeKey
     *            Physical type identifier for fields
     * @return {@link List} of annotated {@link FieldMetadata}.
     */
    private void setTransientAndElementFields(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            AnnotationMetadata gvNixXmlElementAnnotationMetadata,
            String physicalTypeKey) {

        if (governorTypeDetails.getPhysicalTypeCategory().equals(
                PhysicalTypeCategory.CLASS)) {

            AnnotationAttributeValue<?> tmpAttribute;

            StringAttributeValue nameStringAtrributeValue = null;
            StringAttributeValue xmlTypeNameStringAtrributeValue = null;

            BooleanAttributeValue exportedAttributeValue;

            tmpAttribute = gvNixXmlElementAnnotationMetadata
                    .getAttribute(new JavaSymbolName("exported"));

            Assert.isTrue(
                    tmpAttribute != null,
                    "Attribute 'exported' in annotation @GvNIXXmlElement defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' has to be defined to export in XSD schema in WSDL.");

            exportedAttributeValue = (BooleanAttributeValue) tmpAttribute;

            tmpAttribute = gvNixXmlElementAnnotationMetadata
                    .getAttribute(new JavaSymbolName("name"));

            if (tmpAttribute != null) {
                nameStringAtrributeValue = (StringAttributeValue) tmpAttribute;
            }

            tmpAttribute = gvNixXmlElementAnnotationMetadata
                    .getAttribute(new JavaSymbolName("xmlTypeName"));
            if (tmpAttribute != null) {
                xmlTypeNameStringAtrributeValue = (StringAttributeValue) tmpAttribute;
            }

            if (!exportedAttributeValue.getValue()) {
                Assert.isTrue(
                        (nameStringAtrributeValue != null && StringUtils
                                .hasText(nameStringAtrributeValue.getValue()))
                                || (xmlTypeNameStringAtrributeValue != null && StringUtils
                                        .hasText(xmlTypeNameStringAtrributeValue
                                                .getValue())),
                        "Attribute 'name' or 'xmlTypeName' in annotation @GvNIXXmlElement defined in class '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' has to be defined to export in XSD schema in WSDL.");
            }

            // resultNamespace
            StringAttributeValue namespaceStringAttributeValue;

            tmpAttribute = gvNixXmlElementAnnotationMetadata
                    .getAttribute(new JavaSymbolName("namespace"));

            Assert.isTrue(
                    tmpAttribute != null
                            && StringUtils
                                    .hasText(((StringAttributeValue) tmpAttribute)
                                            .getValue()),
                    "Attribute 'namespace' in annotation @GvNIXXmlElement defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' has to be defined to export in XSD schema in WSDL.");

            namespaceStringAttributeValue = (StringAttributeValue) tmpAttribute;

            Assert.isTrue(
                    wSExportValidationService
                            .checkNamespaceFormat(namespaceStringAttributeValue
                                    .getValue()),
                    "Attribute 'namespace' in annotation @GvNIXXmlElement defined in class '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' has to start with 'http://'.\ni.e.: http://name.of.namespace/");

            // Retrieve Array attribute element
            @SuppressWarnings("unchecked")
            ArrayAttributeValue<StringAttributeValue> elementListArrayAttributeValue = (ArrayAttributeValue<StringAttributeValue>) gvNixXmlElementAnnotationMetadata
                    .getAttribute(new JavaSymbolName("elementList"));

            Assert.isTrue(
                    elementListArrayAttributeValue != null,
                    "Attribute 'elementList' in '@GvNIXXmlElement' annotation must be defined.\nArray with field names of '"
                            + governorTypeDetails.getName()
                                    .getFullyQualifiedTypeName()
                            + "' to be used in Web Service operation.\nIf you don't want to publish any fields set the attribute value: 'elementList = {\"\"}'");

            List<StringAttributeValue> elementListStringValue = elementListArrayAttributeValue
                    .getValue();

            List<FieldMetadata> declaredFieldList = new ArrayList<FieldMetadata>();

            // Fields from Entity MetaData.
            if (entityMetadata != null && entityMetadata.isValid()) {

                FieldMetadataBuilder builder;

                builder = getFieldMetadatBuilderForEntityField(entityMetadata
                        .getIdentifierField());
                if (builder.getDeclaredByMetadataId().equals(physicalTypeKey)) {
                    // Adds it only if it's declared in current class (not
                    // inherited)
                    declaredFieldList.add(builder.build());
                }
                builder = getFieldMetadatBuilderForEntityField(entityMetadata
                        .getVersionField());
                if (builder.getDeclaredByMetadataId().equals(physicalTypeKey)) {
                    // Adds it only if it's declared in current class (not
                    // inherited)
                    declaredFieldList.add(builder.build());
                }

            }
            // Check duplicated fields.
            for (FieldMetadata fieldMetadata : governorTypeDetails
                    .getDeclaredFields()) {

                if (!declaredFieldList.contains(fieldMetadata)) {
                    declaredFieldList.add(fieldMetadata);
                }
            }

            boolean containsValue;
            // Check fields from collection.
            for (FieldMetadata fieldMetadata : declaredFieldList) {

                containsValue = false;

                for (StringAttributeValue value : elementListStringValue) {

                    containsValue = value.getValue().contains(
                            fieldMetadata.getFieldName().getSymbolName());

                    if (containsValue) {

                        if (!exportedAttributeValue.getValue()) {

                            Assert.isTrue(
                                    wSExportValidationService.isJavaTypeAllowed(
                                            fieldMetadata.getFieldType(),
                                            MethodParameterType.XMLENTITY,
                                            governorTypeDetails.getName()),
                                    "The '"
                                            + MethodParameterType.XMLENTITY
                                            + "' type '"
                                            + fieldMetadata
                                                    .getFieldType()
                                                    .getFullyQualifiedTypeName()
                                            + "' is not allow to be used in web a service operation because it does not satisfy web services interoperatibily rules.\nThis is a disallowed Object defined in: '"
                                            + governorTypeDetails
                                                    .getName()
                                                    .getFullyQualifiedTypeName()
                                            + "'.");
                        }

                        fieldMetadataElementList.add(fieldMetadata);
                        break;

                    }
                }

                if (!containsValue) {
                    fieldMetadataTransientList.add(fieldMetadata);
                }
            }

        }

    }

    /**
     * Create FieldMetadaBuilder for a field declared in EntityMetadata
     * 
     * @param fieldMetadata
     * @return
     */
    private FieldMetadataBuilder getFieldMetadatBuilderForEntityField(
            FieldMetadata fieldMetadata) {
        // Gets Id form entityMetadata which declares the field.
        // It could be declared in a parent class
        final String entityMetadataId = fieldMetadata.getDeclaredByMetadataId();

        // Compute PhysicalType Id declarer field class
        final String tmpPhyId = PhysicalTypeIdentifier.createIdentifier(
                EntityMetadata.getJavaType(entityMetadataId),
                EntityMetadata.getPath(entityMetadataId));

        return new FieldMetadataBuilder(tmpPhyId, fieldMetadata);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNix_XmlElement";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
        return WSExportXmlElementMetadata.getMetadataIdentiferType();
    }

}
