/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.audit;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link JpaAuditUserServiceMetadata}. Prepares all required
 * information to construct a new instance of
 * {@link JpaAuditUserServiceMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
public final class JpaAuditUserServiceMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference
    private JpaAuditOperationsMetadata operations;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(
                GvNIXJpaAuditUserService.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(
                GvNIXJpaAuditUserService.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Get annotations values
        final JpaAuditUserServiceAnnotationValues annotationValues = new JpaAuditUserServiceAnnotationValues(
                governorPhysicalTypeMetadata);

        // get target entity class details
        final JavaType userType = annotationValues.getUserType();
        if (!annotationValues.isAnnotationFound() || userType == null) {
            return null;
        }

        // Generate metadata
        return new JpaAuditUserServiceMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, annotationValues,
                userType, operations.isSpringSecurityInstalled(),
                operations.isUserTypeSpringSecUserDetails());
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXJpaAuditUserService.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXJpaAuditUserService";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = JpaAuditUserServiceMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = JpaAuditUserServiceMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return JpaAuditUserServiceMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return JpaAuditUserServiceMetadata.getMetadataIdentiferType();
    }
}