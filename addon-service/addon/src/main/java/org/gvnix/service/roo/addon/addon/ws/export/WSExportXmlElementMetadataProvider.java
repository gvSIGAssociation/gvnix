/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.roo.addon.addon.ws.export;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.JavaParserService;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.gvnix.service.roo.addon.annotations.GvNIXXmlElement;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * <p>
 * Checks if @GvNIXXmlElement annotated classes have been updated and this
 * affects to Service Contract WSDL.
 * </p>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
@Component
@Service
public class WSExportXmlElementMetadataProvider extends
        AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(WSExportXmlElementMetadataProvider.class);

    private WSConfigService wSConfigService;
    private JavaParserService javaParserService;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        // Notify when physical type with gvNIX xml element annotation modified
        getMetadataDependencyRegistry().registerDependency(
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
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {

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
        LogicalPath path = WSExportXmlElementMetadata
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
        getWSConfigService().install(WsType.EXPORT);

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
        getMetadataDependencyRegistry().registerDependency(physicalTypeId, id);

        AnnotationMetadata annotation = typeDetails
                .getTypeAnnotation(new JavaType(GvNIXXmlElement.class.getName()));

        // Create metaData with field list values.
        return new WSExportXmlElementMetadata(id, aspectName, physicalType,
                getDeclaredFields(typeDetails, annotation),
                getJavaParserService());
    }

    /**
     * Get defined fields to be exported as elements in XSD schema.
     * <ul>
     * <li>Fields defined in 'elementList' annotation attribute will exported</li>
     * <li>Only executed if typeDetails is a class</li>
     * </ul>
     * 
     * @param typeDetails class to get fields to check
     * @param annotation to check element values.
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
                declaredFields.add(getJavaParserService().getFieldByNameInAll(
                        typeDetails.getName(),
                        new JavaSymbolName(elementListName.getValue())));
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

    public WSConfigService getWSConfigService() {
        if (wSConfigService == null) {
            // Get all Services implement WSConfigService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WSConfigService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WSConfigService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WSConfigService on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return wSConfigService;
        }
    }

    public JavaParserService getJavaParserService() {
        if (javaParserService == null) {
            // Get all Services implement JavaParserService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                JavaParserService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (JavaParserService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load JavaParserService on WSConfigServiceImpl.");
                return null;
            }
        }
        else {
            return javaParserService;
        }
    }

}
