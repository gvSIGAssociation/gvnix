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
package org.gvnix.web.exception.handler.roo.addon;

import java.io.File;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

/**
 * Provides {@link ModalDialogMetadata}. This type is called by Roo to retrieve
 * the metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public final class ModalDialogMetadataProvider extends
        AbstractItdMetadataProvider {

    // @Reference
    // FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

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
        addMetadataTrigger(new JavaType(
                "org.springframework.stereotype.Controller"));
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
                "org.springframework.stereotype.Controller"));
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {
        // Check if ModalDialogs support is set in current project checking if
        // ModalDialog.java exists
        if (!isModalDialogSupported(aspectName)) {
            // Nothing to do if Modaldialogs aren't supported
            return null;
        }

        MemberDetails memberDetails = getMemberDetails(governorPhysicalTypeMetadata);
        return new ModalDialogMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, memberDetails);
    }

    /**
     * ModalDialogs are supported if ModalDialog class is installed in project.
     * 
     * TODO: Maybe the condition could be improved checking if message-box.tagx
     * exists too
     * 
     * @param aspectName
     * @return
     */
    private boolean isModalDialogSupported(JavaType aspectName) {
        String aspectPackage = aspectName.getPackage()
                .getFullyQualifiedPackageName();
        String modalDialogTypePath = pathResolver.getIdentifier(
                Path.SRC_MAIN_JAVA,
                File.separator.concat(
                        aspectPackage.replace(".", File.separator)).concat(
                        "/servlet/handler/ModalDialog.java"));

        return fileManager.exists(modalDialogTypePath);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXModalDialog";
    }

    public String getProvidesType() {
        return ModalDialogMetadata.getMetadataIdentiferType();
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return ModalDialogMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = ModalDialogMetadata
                .getJavaType(metadataIdentificationString);
        Path path = ModalDialogMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

}
