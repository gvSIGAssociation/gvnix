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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.annotations.GvNIXWebFault;
import org.gvnix.service.roo.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.ws.WSConfigService.CommunicationSense;
import org.gvnix.service.roo.addon.ws.export.WSExportExceptionMetadataProvider;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class WSExportExceptionMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private WSConfigService wSConfigService;
    @Reference
    private WSExportValidationService wSExportValidationService;
    @Reference
    private AnnotationsService annotationsService;

    private static Logger logger = Logger
            .getLogger(WSExportExceptionMetadataProvider.class.getName());

    protected void activate(ComponentContext context) {
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
        metadataDependencyRegistry.registerDependency(
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
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return WSExportExceptionMetadata.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = WSExportExceptionMetadata
                .getJavaType(metadataIdentificationString);
        Path path = WSExportExceptionMetadata
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
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        WSExportExceptionMetadata exceptionMetadata = null;

        if (wSConfigService.isProjectWebAvailable()) {

            // Install configuration to export services if it's not installed.
            wSConfigService.install(CommunicationSense.EXPORT);
            // Installs jax2ws plugin in project.
            wSConfigService.installJaxwsBuildPlugin();
            // Add GvNixAnnotations to the project.
            annotationsService.addGvNIXAnnotationsDependency();

            // Check if Web Service definition is correct.
            // DiSiD: Use getMemberHoldingTypeDetails instead of
            // getPhysicalTypeDetails
            // PhysicalTypeDetails physicalTypeDetails =
            // governorPhysicalTypeMetadata
            // .getPhysicalTypeDetails();
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

            AnnotationMetadata annotationMetadata = MemberFindingUtils
                    .getTypeAnnotation(governorTypeDetails, new JavaType(
                            GvNIXWebFault.class.getName()));

            boolean correctGvNIXWebFaultAnnotation = checkGvNixWebFaultAnnotationAttributes(
                    governorTypeDetails, annotationMetadata);

            if (correctGvNIXWebFaultAnnotation) {

                exceptionMetadata = new WSExportExceptionMetadata(
                        metadataIdentificationString, aspectName,
                        governorPhysicalTypeMetadata);

                // DiSiD: Use getAnnotations instead of getTypeAnnotations and
                // getMemberHoldingTypeDetails instead of getItdTypeDetails
                // if
                // (exceptionMetadata.getItdTypeDetails().getTypeAnnotations()
                if (exceptionMetadata.getMemberHoldingTypeDetails()
                        .getAnnotations()

                        .isEmpty()) {
                    logger.log(
                            Level.WARNING,
                            "The annotation @GvNIXWebFault is not declared correctly for '"

                                    // DiSiD: Use getMemberHoldingTypeDetails
                                    // instead of getPhysicalTypeDetails
                                    // + governorPhysicalTypeMetadata
                                    // .getPhysicalTypeDetails()
                                    + governorPhysicalTypeMetadata
                                            .getMemberHoldingTypeDetails()

                                            .getName()
                                            .getFullyQualifiedTypeName()
                                    + "'.\nThis will not export the Exception to be used in Web Service.\n@WebParam annotation will be deleted untill the annotation is defined correctly.");
                }
            }
        }

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
                && StringUtils.hasText(nameAttributeValue.getValue());

        Assert.isTrue(correctName,
                "@GvNIXWebFault annotation attribute value 'name' in '"
                        + governorTypeDetails.getName() + "' must be defined.");

        // Check targetNamespace.
        StringAttributeValue namespaceAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("targetNamespace"));

        correctNamespace = (namespaceAttributeValue != null)
                && StringUtils.hasText(namespaceAttributeValue.getValue())
                && wSExportValidationService
                        .checkNamespaceFormat(namespaceAttributeValue
                                .getValue());

        Assert.isTrue(
                correctNamespace,
                "@GvNIXWebFault annotation attribute value 'targetNamespace' in '"
                        + governorTypeDetails.getName()
                        + "' must be well formed.\ni.e.: http://my.example.com/");

        // Check faultBean.
        StringAttributeValue faultBeanAttributeValue = (StringAttributeValue) annotationMetadata
                .getAttribute(new JavaSymbolName("faultBean"));

        correctFaultBean = (faultBeanAttributeValue != null)
                && StringUtils.hasText(faultBeanAttributeValue.getValue())
                && governorTypeDetails.getName().getFullyQualifiedTypeName()
                        .contentEquals(faultBeanAttributeValue.getValue());

        Assert.isTrue(
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

}
