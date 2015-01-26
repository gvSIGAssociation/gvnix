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

import static org.springframework.roo.classpath.customdata.CustomDataKeys.PERSISTENT_TYPE;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

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

    private static final JavaType SEC_USER_DETAILS = new JavaType(
            "org.springframework.security.core.userdetails.UserDetails");

    private JpaAuditOperationsMetadata operations;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
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
        getMetadataDependencyRegistry().deregisterDependency(
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

        // Manage date format
        String dateTimePattern = annotationValues
                .getAuditDateTimeFormatPattern();
        String dateTimeStyle;
        boolean usePattern = false;
        if (StringUtils.isBlank(dateTimePattern)) {
            dateTimeStyle = annotationValues.getAuditDateTimeFormatStyle();
        }
        else {
            usePattern = true;
            dateTimeStyle = null;
        }

        boolean userTypeIsUserDetails = false;
        boolean userTypeIsEntity = false;
        ClassOrInterfaceTypeDetails userTypeDetails = getTypeLocationService()
                .getTypeDetails(userType);
        if (userTypeDetails != null) {
            // Try to identify if userType implements UserDetails
            userTypeIsUserDetails = false;
            for (JavaType implementType : userTypeDetails.getImplementsTypes()) {
                if (SEC_USER_DETAILS.equals(implementType)) {
                    userTypeIsUserDetails = true;
                }
            }

            // Try to determine if userType is an entity
            final MemberDetails userTypeMemberDetails = getMemberDetails(userTypeDetails);
            if (userTypeMemberDetails != null) {

                final MemberHoldingTypeDetails userTypeMHTD = MemberFindingUtils
                        .getMostConcreteMemberHoldingTypeDetailsWithTag(
                                userTypeMemberDetails, PERSISTENT_TYPE);
                if (userTypeMHTD != null) {
                    userTypeIsEntity = true;
                }

            }
        }

        getOperations().evictUserServiceInfoCache();

        // Generate metadata
        return new JpaAuditUserServiceMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, annotationValues,
                userType, getOperations().isSpringSecurityInstalled(),
                userTypeIsUserDetails, userTypeIsEntity, usePattern,
                dateTimePattern, dateTimeStyle);
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

    public JpaAuditOperationsMetadata getOperations() {
        if (operations == null) {
            // Get all Services implement JpaAuditOperationsMetadata interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                JpaAuditOperationsMetadata.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (JpaAuditOperationsMetadata) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load JpaAuditOperationsMetadata on JpaAuditUserServiceMetadataProvider.");
                return null;
            }
        }
        else {
            return operations;
        }

    }
}