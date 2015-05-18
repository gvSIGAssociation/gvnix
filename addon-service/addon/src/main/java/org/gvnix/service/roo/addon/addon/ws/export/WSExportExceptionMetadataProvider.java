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

import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Metadata provider for {@link WSExportExceptionMetadata}
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
@Component
@Service
public class WSExportExceptionMetadataProvider extends
        AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(WSExportExceptionMetadataProvider.class);

    private WSConfigService wSConfigService;
    private WSExportValidationService wSExportValidationService;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebFault.class.getName()));
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
        return WSExportExceptionMetadata.createIdentifier(javaType, path);
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
        JavaType javaType = WSExportExceptionMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = WSExportExceptionMetadata
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

        WSExportExceptionMetadata exceptionMetadata = null;

        // Install configuration to export services if it's not installed.
        getWSConfigService().install(WsType.EXPORT);

        // Check if Web Service definition is correct.
        PhysicalTypeDetails physicalTypeDetails = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();

        ClassOrInterfaceTypeDetails governorTypeDetails;
        if (physicalTypeDetails == null
                || !(physicalTypeDetails instanceof ClassOrInterfaceTypeDetails)) {
            // There is a problem
            return null;
        }
        else {
            // We have reliable physical type details
            governorTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeDetails;
        }

        // Gets fault annotation
        AnnotationMetadata annotationMetadata = governorTypeDetails
                .getTypeAnnotation(new JavaType(GvNIXWebFault.class.getName()));

        // Checks attributes
        boolean correctGvNIXWebFaultAnnotation = checkGvNixWebFaultAnnotationAttributes(
                governorTypeDetails, annotationMetadata);

        if (!correctGvNIXWebFaultAnnotation) {
            return null;
        }

        // Generate metadata
        exceptionMetadata = new WSExportExceptionMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata);

        return exceptionMetadata;
    }

    /**
     * Check if @GvNIXWebFault annotation attributes are correct.
     * 
     * @param governorTypeDetails
     * @param annotationMetadata
     * @return
     */
    private boolean checkGvNixWebFaultAnnotationAttributes(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            AnnotationMetadata annotationMetadata) {

        // Check if are correct annotation attributes.
        boolean correctName = false;
        boolean correctNamespace = false;
        boolean correctFaultBean = false;

        // Check name.
        StringAttributeValue nameAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("name"));

        correctName = (nameAttributeValue != null)
                && StringUtils.isNotBlank(nameAttributeValue.getValue());

        Validate.isTrue(correctName,
                "@GvNIXWebFault annotation attribute value 'name' in '"
                        + governorTypeDetails.getName() + "' must be defined.");

        // Check targetNamespace.
        StringAttributeValue namespaceAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("targetNamespace"));

        correctNamespace = (namespaceAttributeValue != null)
                && StringUtils.isNotBlank(namespaceAttributeValue.getValue())
                && getWSExportValidationService().checkNamespaceFormat(
                        namespaceAttributeValue.getValue());

        Validate.isTrue(
                correctNamespace,
                "@GvNIXWebFault annotation attribute value 'targetNamespace' in '"
                        + governorTypeDetails.getName()
                        + "' must be well formed.\ni.e.: http://my.example.com/");

        // Check faultBean.
        StringAttributeValue faultBeanAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("faultBean"));

        correctFaultBean = (faultBeanAttributeValue != null)
                && StringUtils.isNotBlank(faultBeanAttributeValue.getValue())
                && governorTypeDetails.getName().getFullyQualifiedTypeName()
                        .contentEquals(faultBeanAttributeValue.getValue());

        Validate.isTrue(
                correctFaultBean,
                "@GvNIXWebFault annotation attribute value 'faultBean' in '"
                        + governorTypeDetails.getName()
                        + "' must have the same value that class complete name.\ni.e.: '"
                        + governorTypeDetails.getName()
                                .getFullyQualifiedTypeName() + "'");
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIX_WebFault";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
        return WSExportExceptionMetadata.getMetadataIdentiferType();
    }

    public WSExportValidationService getWSExportValidationService() {
        if (wSExportValidationService == null) {
            // Get all Services implement WSExportValidationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WSExportValidationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WSExportValidationService) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WSExportValidationService on WSExportExceptionMetadataProvider.");
                return null;
            }
        }
        else {
            return wSExportValidationService;
        }
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
                LOGGER.warning("Cannot load WSConfigService on WSExportExceptionMetadataProvider.");
                return null;
            }
        }
        else {
            return wSConfigService;
        }
    }

}
