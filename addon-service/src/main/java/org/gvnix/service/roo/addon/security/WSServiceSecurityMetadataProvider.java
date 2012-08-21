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
package org.gvnix.service.roo.addon.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.callback.CallbackHandler;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceSecurity;
import org.gvnix.service.roo.addon.util.WsdlParserUtils;
import org.gvnix.service.roo.addon.ws.importt.WSImportMetadata;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Web Service Security Metadata provider
 * </p>
 * 
 * <p>
 * Manage {@link GvNIXWebServiceSecurity} annotation
 * </p>
 * 
 * <p>
 * In {@link #getMetadata(String, JavaType, PhysicalTypeMetadata, String)}:
 * <ul>
 * <li>creates Class ITD to make governor class implements
 * {@link CallbackHandler}</li>
 * <li>creates <code>{governor_Class_Name}Sercurity.properties</code> file in
 * <code>scr/main/resorces/{governor_Package}</code></li>
 * <li>updates <code>client-config.wsdd</code> file using
 * {@link SecurityService}</li>
 * <li>checks if {certificate} file exist in
 * <code>scr/main/resorces/{governor_Package}</code></li>
 * 
 * </ul>
 * </p>
 * 
 * @author Jose Manuel Vivó Arnal ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public final class WSServiceSecurityMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private SecurityService securityService;

    @Reference
    private ProjectOperations projectOperations;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXWebServiceSecurity.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(
                GvNIXWebServiceSecurity.class.getName()));
    }

    /**
     * <p>
     * Return an instance of the Metadata offered by this add-on and perform
     * this operations:
     * <ul>
     * <li>creates Class ITD to make governor class implements
     * {@link CallbackHandler}</li>
     * <li>creates <code>{governor_Class_Name}Sercurity.properties</code> file
     * in <code>scr/main/resorces/{governor_Package}</code></li>
     * <li>updates <code>client-config.wsdd</code> file using
     * {@link SecurityService}</li>
     * <li>checks if {certificate} file exist in
     * <code>scr/main/resorces/{governor_Package}</code></li>
     * </ul>
     * </p>
     * 
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Setup project
        securityService.setupWSSJ4();

        String serviceName = getServiceName(metadataIdentificationString);

        // create Metadata
        WSServiceSecurityMetadata metadata = new WSServiceSecurityMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, serviceName);

        try {
            // update client-config.wsdd
            securityService.addOrUpdateAxisClientService(
                    metadata.getServiceName(),
                    metadata.getServiceWsddConfigurationParameters());

            // write properties file
            createSecurityPropertiesFile(aspectName, metadata);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error generating security configuration", e);
        }

        // Checks for certificated file
        String certificatePath = projectOperations.getPathResolver()
                .getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                        metadata.getCertificatePath());

        Validate.isTrue(
                fileManager.exists(certificatePath),
                "Missing certificated file '"
                        .concat(Path.SRC_MAIN_RESOURCES.name()).concat("/")
                        .concat(metadata.getCertificatePath()).concat("' for ")
                        .concat(governorPhysicalTypeMetadata.getId()));

        return metadata;

    }

    /**
     * Creates <code>{governor_Class_Name}Sercurity.properties</code> file in
     * <code>scr/main/resorces/{governor_Package}</code>
     * 
     * @param aspectName
     * @param metadata
     * @throws IOException
     */
    private void createSecurityPropertiesFile(JavaType aspectName,
            WSServiceSecurityMetadata metadata) throws IOException {

        // Resolve absolute path
        String propertiesPath = projectOperations.getPathResolver()
                .getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                        metadata.getPropertiesPath());

        OutputStream os;

        // Gets properties form
        Properties properties = metadata.getSecurityProperties();

        // Checks if file exists
        MutableFile mutableFile;
        if (fileManager.exists(propertiesPath)) {
            // Load current file content
            Properties storedProperties = new Properties();
            mutableFile = fileManager.updateFile(propertiesPath);
            InputStream is = null;
            try {
                is = mutableFile.getInputStream();
                storedProperties.load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            // Compare content
            if (propertiesEquals(properties, storedProperties)) {
                // File Content is up to date --> exit
                return;
            }
        } else {
            // Unable to find the file, so let's create it
            mutableFile = fileManager.createFile(propertiesPath);
        }

        // Store properties in file
        os = null;
        try {
            os = mutableFile.getOutputStream();
            storeProperties(aspectName, metadata, os, properties);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * Compares the values of two Property instances
     * 
     * @param one
     * @param other
     * @return
     */
    private boolean propertiesEquals(Properties one, Properties other) {
        if (one.size() != other.size()) {
            return false;
        }
        Set<Entry<Object, Object>> entrySet = one.entrySet();
        Object otherValue;
        for (Entry<Object, Object> entry : entrySet) {
            otherValue = other.get(entry.getKey());
            if (otherValue == null) {
                if (entry.getValue() != null) {
                    return false;
                }
            } else if (!otherValue.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Store properties into a output stream
     * 
     * @param aspectName
     * @param metadata
     * @param os
     * @param properties
     * @throws IOException
     */
    private void storeProperties(JavaType aspectName,
            WSServiceSecurityMetadata metadata, OutputStream os,
            Properties properties) throws IOException {
        properties.store(
                os,
                "Service '".concat(metadata.getServiceName())
                        .concat("' security properities. Class ")
                        .concat(aspectName.getFullyQualifiedTypeName()));
    }

    /**
     * Gets Service Name from governors metadata
     * 
     * @param metadataIdentificationString
     * @return
     */
    private String getServiceName(String metadataIdentificationString) {
        // Gets WSImportMetadata id
        JavaType javaType = WSServiceSecurityMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = WSServiceSecurityMetadata
                .getPath(metadataIdentificationString);
        String wsImportMetadataId = WSImportMetadata.createIdentifier(javaType,
                path);

        // Gets WSImpotMetada
        WSImportMetadata wsImportMetadata = (WSImportMetadata) metadataService
                .get(wsImportMetadataId);

        Validate.notNull(wsImportMetadata,
                "Governor must be gvNIX WS import. Can't find metadata '"
                        .concat(wsImportMetadataId).concat("'"));

        // gets wsdl location
        String wsdlLocation = wsImportMetadata.getWsdlLocation();

        // loads wsdl
        Document wsdl = securityService.getWsdl(wsdlLocation);

        Validate.notNull(wsdl, "Can't get WSDl from ".concat(wsdlLocation));

        Validate.isTrue(WsdlParserUtils.isRpcEncoded(wsdl.getDocumentElement()),
                "Only RPC-Encoded services is supported");

        // Gets first port
        Element port = WsdlParserUtils.findFirstCompatiblePort(wsdl
                .getDocumentElement());

        return port.getAttribute("name");
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_Security.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIX_WebSecurity";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = WSServiceSecurityMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = WSServiceSecurityMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return WSServiceSecurityMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return WSServiceSecurityMetadata.getMetadataIdentiferType();
    }
}