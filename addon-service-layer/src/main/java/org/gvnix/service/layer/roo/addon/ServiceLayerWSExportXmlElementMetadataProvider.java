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
package org.gvnix.service.layer.roo.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.CommunicationSense;
import org.gvnix.service.layer.roo.addon.annotations.GvNIXXmlElement;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * <p>
 * Checks if @GvNIXXmlElement annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ServiceLayerWSExportXmlElementMetadataProvider extends AbstractItdMetadataProvider {

    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;
    @Reference
    private AnnotationsService annotationsService;

    private List<FieldMetadata> fieldMetadataElementList = new ArrayList<FieldMetadata>();

    private List<FieldMetadata> fieldMetadataTransientList = new ArrayList<FieldMetadata>();

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSExportXmlElementMetadataProvider.class.getName());

    protected void activate(ComponentContext context) {
	// Ensure we're notified of all metadata related to physical Java types,
	// in particular their initial creation
	metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
		.getMetadataIdentiferType(), getProvidesType());
	addMetadataTrigger(new JavaType(GvNIXXmlElement.class.getName()));
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#createLocalIdentifier(org.springframework.roo.model.JavaType, org.springframework.roo.project.Path)
     */
    protected String createLocalIdentifier(JavaType javaType, Path path) {
	return ServiceLayerWSExportXmlElementMetadata.createIdentifier(
		javaType, path);
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
	    String metadataIdentificationString) {
	JavaType javaType = ServiceLayerWSExportXmlElementMetadata
		.getJavaType(metadataIdentificationString);
	Path path = ServiceLayerWSExportXmlElementMetadata
		.getPath(metadataIdentificationString);
	String physicalTypeIdentifier = PhysicalTypeIdentifier
		.createIdentifier(javaType, path);
	return physicalTypeIdentifier;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#getMetadata(java.lang.String, org.springframework.roo.model.JavaType, org.springframework.roo.classpath.PhysicalTypeMetadata, java.lang.String)
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
	    String metadataIdentificationString, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    String itdFilename) {

        ServiceLayerWSExportXmlElementMetadata serviceLayerWSExportXmlElementMetadata = null;
        
        if (serviceLayerWsConfigService.isProjectAvailable()) {

            // Install configuration to export services if it's not installed.
            serviceLayerWsConfigService.install(CommunicationSense.EXPORT);
            // Installs jax2ws plugin in project.
            serviceLayerWsConfigService.installJaxwsBuildPlugin();
            // Add GvNixAnnotations to the project.
            annotationsService.addGvNIXAnnotationsDependency();

            // We know governor type details are non-null and can be safely cast

            // Work out the MIDs of the other metadata we depend on
            JavaType javaType = ServiceLayerWSExportXmlElementMetadata
                    .getJavaType(metadataIdentificationString);
            Path path = ServiceLayerWSExportXmlElementMetadata
                    .getPath(metadataIdentificationString);
            String entityMetadataKey = EntityMetadata.createIdentifier(
                    javaType, path);

            // Check if Web Service definition is correct.
            PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                    .getPhysicalTypeDetails();

            ClassOrInterfaceTypeDetails governorTypeDetails;
            if (physicalTypeDetails == null
                    || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
                // There is a problem
                return null;
            } else {
                // We have reliable physical type details
                governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
            }
            
            // We need to lookup the metadata we depend on
            EntityMetadata entityMetadata = (EntityMetadata) metadataService
                    .get(entityMetadataKey);

            // We need to be informed if our dependent metadata changes
            metadataDependencyRegistry.registerDependency(entityMetadataKey,
                    metadataIdentificationString);

            // Fields from Entity MetaData.
            List<FieldMetadata> entityFieldList = new ArrayList<FieldMetadata>();

            if (entityMetadata != null && entityMetadata.isValid()) {
                entityFieldList.add(entityMetadata.getIdentifierField());
                entityFieldList.add(entityMetadata.getVersionField());
            }

            // Redefine field lists.
            fieldMetadataElementList = new ArrayList<FieldMetadata>();
            fieldMetadataTransientList = new ArrayList<FieldMetadata>();

            // Add Entity fields
            fieldMetadataElementList.addAll(entityFieldList);

            AnnotationMetadata gvNixXmlElementAnnotationMetadata = MemberFindingUtils
                    .getTypeAnnotation(governorTypeDetails,
                    new JavaType(GvNIXXmlElement.class.getName()));

            // Field @XmlElement and @XmlTransient annotations lists.
            setTransientAndElementFields(governorTypeDetails,
                    gvNixXmlElementAnnotationMetadata);

            // Create metaData with field list values.
            serviceLayerWSExportXmlElementMetadata = new ServiceLayerWSExportXmlElementMetadata(
                    metadataIdentificationString, aspectName,
                    governorPhysicalTypeMetadata, entityMetadata,
                    fieldMetadataElementList, fieldMetadataTransientList);

        }
        
	return serviceLayerWSExportXmlElementMetadata;
    }

    /**
     * Retrieves all related fields annotated with @OneToMany, @ManyToOne,
     * 
     * @OneToOne, related to persistence and not allowed Collection fields.
     * 
     * @param governorTypeDetails
     *            class to get fields to check.
     * @param gvNixXmlElementAnnotationMetadata
     *            to check element values.
     * @return {@link List} of annotated {@link FieldMetadata}.
     */
    public void setTransientAndElementFields(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            AnnotationMetadata gvNixXmlElementAnnotationMetadata) {

        // Retrieve Array attribute element
        ArrayAttributeValue<StringAttributeValue> elementListArrayAttributeValue = (ArrayAttributeValue) gvNixXmlElementAnnotationMetadata
                .getAttribute(new JavaSymbolName("elementList"));

        Assert
                .isTrue(
                        elementListArrayAttributeValue != null,
                        "Attribute 'elementList' in '@GvNIXXmlElement' annotation must be defined.\nArray with field names of '"
                                + governorTypeDetails.getName()
                                        .getFullyQualifiedTypeName()
                                + "' to be used in Web Service operation.\nIf you want to publish all fields set the attribute value: 'elementList = {\"\"}'");

        List<StringAttributeValue> elementListStringValue = elementListArrayAttributeValue
                .getValue();

        // Unsupported collection.
        List<? extends FieldMetadata> declaredFieldList = governorTypeDetails
                .getDeclaredFields();

        boolean containsValue;

        // Check fields from collection.
        for (FieldMetadata fieldMetadata : declaredFieldList) {

            containsValue = true;

            for (StringAttributeValue value : elementListStringValue) {

                containsValue = value.getValue().contentEquals(
                        fieldMetadata.getFieldName().getSymbolName());

                if (containsValue) {
                    fieldMetadataElementList.add(fieldMetadata);
                    break;
                }
            }
            
            if (!containsValue) {
                fieldMetadataTransientList.add(fieldMetadata);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.itd.ItdMetadataProvider#getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
	return "GvNix_XmlElement";
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
	return ServiceLayerWSExportXmlElementMetadata
		.getMetadataIdentiferType();
    }

}
