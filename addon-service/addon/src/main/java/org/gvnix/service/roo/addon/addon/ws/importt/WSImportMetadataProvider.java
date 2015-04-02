/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
package org.gvnix.service.roo.addon.addon.ws.importt;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.security.SecurityService;
import org.gvnix.service.roo.addon.addon.util.WsdlParserUtils;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceProxy;
import org.gvnix.support.OperationUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.w3c.dom.Element;

/**
 * <p>
 * Checks if @GvNIXWebServicesProxy annotated classes have been updated and this
 * affects to Service Classes.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WSImportMetadataProvider extends AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(WSImportMetadataProvider.class);

    private WSConfigService wSConfigService;

    private SecurityService securityService;

    private ProjectOperations projectOperations;

    private OperationUtils operationUtils;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebServiceProxy.class.getName()));
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

        return WSImportMetadata.createIdentifier(javaType, path);
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

        JavaType javaType = WSImportMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = WSImportMetadata
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

        WSImportMetadata metadata = null;

        // Import service if project has required prerequisites
        if (getOperationUtils().isProjectAvailable(getMetadataService(),
                getProjectOperations())) {

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

            // Get upstreamDepency Class to check.
            AnnotationMetadata annotation = governorTypeDetails
                    .getTypeAnnotation(new JavaType(GvNIXWebServiceProxy.class
                            .getName()));

            // Wsdl location
            StringAttributeValue url = (StringAttributeValue) annotation
                    .getAttribute(new JavaSymbolName("wsdlLocation"));

            try {

                // Check URL connection and WSDL format
                Element root = getSecurityService().getWsdl(url.getValue())
                        .getDocumentElement();

                boolean generate;
                if (WsdlParserUtils.isRpcEncoded(root)) {

                    // Generate service infraestructure to import the service
                    generate = getWSConfigService().importService(
                            governorTypeDetails.getName(), url.getValue(),
                            WsType.IMPORT_RPC_ENCODED);
                }
                else {

                    // Generate service infraestructure to import the service
                    generate = getWSConfigService().importService(
                            governorTypeDetails.getName(), url.getValue(),
                            WsType.IMPORT);
                }

                // Generate source code client classes if necessary
                if (generate) {

                    getWSConfigService().mvn(WSConfigService.GENERATE_SOURCES,
                            WSConfigService.GENERATE_SOURCES_INFO);
                }

                // Create metadata
                metadata = new WSImportMetadata(metadataIdentificationString,
                        aspectName, governorPhysicalTypeMetadata,
                        getSecurityService());

            }
            catch (IOException e) {

                throw new IllegalStateException(
                        "Error generating web service sources");
            }
        }

        return metadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {

        return "GvNix_WebServiceProxy";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {

        return WSImportMetadata.getMetadataIdentiferType();
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
                LOGGER.warning("Cannot load WSConfigService on WSImportMetadataProvider.");
                return null;
            }
        }
        else {
            return wSConfigService;
        }
    }

    public SecurityService getSecurityService() {
        if (securityService == null) {
            // Get all Services implement SecurityService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                SecurityService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (SecurityService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load SecurityService on WSImportMetadataProvider.");
                return null;
            }
        }
        else {
            return securityService;
        }
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on WSImportMetadataProvider.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public OperationUtils getOperationUtils() {
        if (operationUtils == null) {
            // Get all Services implement OperationUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                OperationUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    operationUtils = (OperationUtils) this.context
                            .getService(ref);
                    return operationUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load OperationUtils on WSImportMetadataProvider.");
                return null;
            }
        }
        else {
            return operationUtils;
        }

    }

}
