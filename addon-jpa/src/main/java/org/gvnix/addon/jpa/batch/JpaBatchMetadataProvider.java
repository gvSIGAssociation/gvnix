/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
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
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.addon.jpa.batch;

import static org.springframework.roo.classpath.customdata.CustomDataKeys.PERSISTENT_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link JpaBatchMetadata}. Prepares all required information to
 * construct a new instance of {@link JpaBatchMetadata}. Also register metadata
 * dependencies.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public final class JpaBatchMetadataProvider extends
        AbstractMemberDiscoveringItdMetadataProvider {

    private Map<JavaType, String> entityToBatchMidMap = new HashMap<JavaType, String>();

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *            OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXJpaBatch.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *            OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXJpaBatch.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        final JpaBatchAnnotationValues annotationValues = new JpaBatchAnnotationValues(
                governorPhysicalTypeMetadata);

        final JavaType targetEntity = annotationValues.getEntity();
        if (!annotationValues.isAnnotationFound() || targetEntity == null) {
            return null;
        }

        final MemberDetails targetEntityMemberDetails = getMemberDetails(targetEntity);
        if (targetEntityMemberDetails == null) {
            return null;
        }

        final MemberHoldingTypeDetails targetEntityMemberHoldingTypeDetails = MemberFindingUtils
                .getMostConcreteMemberHoldingTypeDetailsWithTag(
                        targetEntityMemberDetails, PERSISTENT_TYPE);
        if (targetEntityMemberHoldingTypeDetails == null) {
            return null;
        }

        // get target entity MID
        final String domainTypeMid = typeLocationService
                .getPhysicalTypeIdentifier(targetEntity);
        if (domainTypeMid == null) {
            return null;
        }

        LogicalPath path = JpaBatchMetadata
                .getPath(metadataIdentificationString);

        String entityMetadataKey = JpaActiveRecordMetadata.createIdentifier(
                targetEntity, path);
        // register downstream dependency (entity --> jpaBatch)
        metadataDependencyRegistry.registerDependency(domainTypeMid,
                metadataIdentificationString);
        List<FieldMetadata> identifiers = persistenceMemberLocator
                .getIdentifierFields(targetEntity);

        JpaActiveRecordMetadata entityMetadata = (JpaActiveRecordMetadata) metadataService
                .get(entityMetadataKey);

        JpaCrudAnnotationValues crudAnnotationValues = new JpaCrudAnnotationValues(
                entityMetadata);

        // TODO get more data
        return new JpaBatchMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, annotationValues, identifiers,
                entityMetadata, crudAnnotationValues);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXJpaBatch.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXJpaBatch";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = JpaBatchMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = JpaBatchMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return JpaBatchMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return JpaBatchMetadata.getMetadataIdentiferType();
    }

    @Override
    protected String getLocalMidToRequest(final ItdTypeDetails itdTypeDetails) {
        final JavaType governor = itdTypeDetails.getName();

        // If the governor is a form backing object, refresh its local metadata
        final String localMid = entityToBatchMidMap.get(governor);
        if (localMid != null) {
            return localMid;
        }

        // If the governor is a layer component that manages a form backing
        // object, refresh that object's local metadata
        return getRelatedEntityComponent(governor);
    }

    /**
     * If the given governor is a layer component (service, repository, etc.)
     * that manages an entity for which we maintain web scaffold metadata,
     * returns the ID of that metadata, otherwise returns <code>null</code>.
     * TODO doesn't handle the case where the governor is a component that
     * manages multiple entities, as it always returns the MID for the first
     * entity found (in annotation order) for which we provide web metadata. We
     * would need to enhance
     * {@link AbstractMemberDiscoveringItdMetadataProvider#getLocalMidToRequest}
     * to return a list of MIDs, rather than only one.
     * 
     * @param governor the governor to check (required)
     * @return see above
     */
    private String getRelatedEntityComponent(final JavaType governor) {
        final ClassOrInterfaceTypeDetails governorTypeDetails = typeLocationService
                .getTypeDetails(governor);
        if (governorTypeDetails != null) {
            for (final JavaType type : governorTypeDetails.getLayerEntities()) {
                final String localMid = entityToBatchMidMap.get(type);
                if (localMid != null) {
                    /*
                     * The ITD's governor is a layer component that manages an
                     * entity for which we maintain web scaffold metadata =>
                     * refresh that MD in case a layer has appeared or gone
                     * away.
                     */
                    return localMid;
                }
            }
        }
        return null;
    }
}