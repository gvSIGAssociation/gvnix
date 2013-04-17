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
package org.gvnix.addon.datatables;

import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link DatatablesMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public final class DatatablesMetadataProvider extends
        AbstractItdMetadataProvider {

    @Reference private WebMetadataService webMetadataService;

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXDatatables.class.getName()));
    }

    /**
     * Unregister this provider
     * 
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(GvNIXDatatables.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        JavaType javaType = DatatablesMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = DatatablesMetadata
                .getPath(metadataIdentificationString);

        final DatatablesAnnotationValues annotationValues = new DatatablesAnnotationValues(
                governorPhysicalTypeMetadata);

        String webScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(
                javaType, path);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataId);

        JavaType webScaffoldAspectName = webScaffoldMetadata.getAspectName();

        JavaType entity = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();

        List<FieldMetadata> identifiers = persistenceMemberLocator
                .getIdentifierFields(entity);

        String JpaMetadataId = JpaActiveRecordMetadata.createIdentifier(entity,
                path);
        JpaActiveRecordMetadata jpaMetadata = (JpaActiveRecordMetadata) metadataService
                .get(JpaMetadataId);
        String plural = jpaMetadata.getPlural();

        JavaSymbolName entityManagerMethodName = jpaMetadata
                .getEntityManagerMethod().getMethodName();

        // check if has metadata types
        final MemberDetails entityMemberDetails = getMemberDetails(entity);

        final Map<JavaSymbolName, DateTimeFormatDetails> datePatterns = webMetadataService
                .getDatePatterns(entity, entityMemberDetails,
                        metadataIdentificationString);
        boolean hasDateTypes = !datePatterns.isEmpty();

        return new DatatablesMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, annotationValues, entity,
                identifiers, plural, entityManagerMethodName, hasDateTypes,
                webScaffoldAspectName);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXDatatables.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXDatatables";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = DatatablesMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = DatatablesMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return DatatablesMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return DatatablesMetadata.getMetadataIdentiferType();
    }
}