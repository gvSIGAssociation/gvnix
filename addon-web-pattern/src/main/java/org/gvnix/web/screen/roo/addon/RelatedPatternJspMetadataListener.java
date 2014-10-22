/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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
package org.gvnix.web.screen.roo.addon;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * This Listener produces MVC artifacts for a given RelatedPatternJspMetadata
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component(immediate = true)
@Service
public class RelatedPatternJspMetadataListener extends
        AbstractPatternJspMetadataListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;
    @Reference
    private MetadataService metadataService;
    @Reference
    private WebMetadataService webMetadataService;
    @Reference
    private FileManager fileManager;
    @Reference
    private TilesOperations tilesOperations;
    @Reference
    private MenuOperations menuOperations;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private PropFileOperations propFileOperations;
    @Reference
    WebScreenOperations webScreenOperations;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private TypeLocationService typeLocationService;
    private final Map<JavaType, String> fBackObjType2LocMids = new HashMap<JavaType, String>();

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                RelatedPatternMetadata.getMetadataIdentiferType(),
                getProvidesType());
        _fileManager = fileManager;
        _tilesOperations = tilesOperations;
        _menuOperations = menuOperations;
        _projectOperations = projectOperations;
        _propFileOperations = propFileOperations;
        _webScreenOperations = webScreenOperations;
        _metadataService = metadataService;
        _pathResolver = pathResolver;
        _typeLocationService = typeLocationService;
    }

    public MetadataItem get(String metadataIdentificationString) {
        JavaType javaType = RelatedPatternJspMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = RelatedPatternJspMetadata
                .getPath(metadataIdentificationString);
        String patternMetadataKey = RelatedPatternMetadata.createIdentifier(
                javaType, path);
        RelatedPatternMetadata relatedPatternMetadata = (RelatedPatternMetadata) metadataService
                .get(patternMetadataKey);

        if (relatedPatternMetadata == null || !relatedPatternMetadata.isValid()) {
            return null;
        }

        webScaffoldMetadata = relatedPatternMetadata.getWebScaffoldMetadata();
        Validate.notNull(webScaffoldMetadata, "Web scaffold metadata required");

        webScaffoldAnnotationValues = webScaffoldMetadata.getAnnotationValues();
        Validate.notNull(webScaffoldAnnotationValues,
                "Web scaffold annotation values required");

        formbackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        Validate.notNull(formbackingType, "formbackingType required");
        entityName = uncapitalize(formbackingType.getSimpleTypeName());

        MemberDetails memberDetails = webMetadataService
                .getMemberDetails(formbackingType);
        JavaTypeMetadataDetails formBackingTypeMetadataDetails = webMetadataService
                .getJavaTypeMetadataDetails(formbackingType, memberDetails,
                        metadataIdentificationString);
        Validate.notNull(
                formBackingTypeMetadataDetails,
                "Unable to obtain metadata for type "
                        + formbackingType.getFullyQualifiedTypeName());
        fBackObjType2LocMids.put(formbackingType, metadataIdentificationString);

        eligibleFields = webMetadataService.getScaffoldEligibleFieldMetadata(
                formbackingType, memberDetails, metadataIdentificationString);

        if (eligibleFields.size() == 0) {
            return null;
        }

        relatedDomainTypes = relatedPatternMetadata
                .getRelatedApplicationTypeMetadata();
        Validate.notNull(relatedDomainTypes, "Related domain types required");

        formbackingTypeMetadata = relatedDomainTypes.get(formbackingType);
        Validate.notNull(formbackingTypeMetadata,
                "Form backing type metadata required");

        formbackingTypePersistenceMetadata = formbackingTypeMetadata
                .getPersistenceDetails();
        if (formbackingTypePersistenceMetadata == null) {
            return null;
        }

        for (String definedPattern : relatedPatternMetadata
                .getDefinedPatterns()) {
            installMvcArtifacts(definedPattern);
        }

        return new RelatedPatternJspMetadata(metadataIdentificationString,
                relatedPatternMetadata);
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            JavaType javaType = RelatedPatternMetadata
                    .getJavaType(upstreamDependency);
            LogicalPath path = RelatedPatternMetadata
                    .getPath(upstreamDependency);
            downstreamDependency = RelatedPatternJspMetadata.createIdentifier(
                    javaType, path);

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                    .contains(downstreamDependency)) {
                return;
            }
        }
        else {
            // This is the generic fallback listener, ie from
            // MetadataDependencyRegistry.addListener(this) in the activate()
            // method

            // Get the metadata that just changed
            MetadataItem metadataItem = metadataService.get(upstreamDependency);

            // We don't have to worry about physical type metadata, as we
            // monitor the relevant .java once the DOD governor is first
            // detected
            if (metadataItem == null
                    || !metadataItem.isValid()
                    || !(metadataItem instanceof ItdTypeDetailsProvidingMetadataItem)) {
                // There's something wrong with it or it's not for an ITD, so
                // let's gracefully abort
                return;
            }

            // Let's ensure we have some ITD type details to actually work with
            ItdTypeDetailsProvidingMetadataItem itdMetadata = (ItdTypeDetailsProvidingMetadataItem) metadataItem;
            ItdTypeDetails itdTypeDetails = itdMetadata
                    .getMemberHoldingTypeDetails();
            if (itdTypeDetails == null) {
                return;
            }

            String localMid = fBackObjType2LocMids.get(itdTypeDetails
                    .getGovernor().getName());
            if (localMid != null) {
                metadataService.evictAndGet(localMid);
            }
            return;
        }

        metadataService.evictAndGet(downstreamDependency);
    }

    @Override
    public boolean isRelatedPattern() {
        return true;
    }

    public String getProvidesType() {
        return RelatedPatternJspMetadata.getMetadataIdentiferType();
    }

}
