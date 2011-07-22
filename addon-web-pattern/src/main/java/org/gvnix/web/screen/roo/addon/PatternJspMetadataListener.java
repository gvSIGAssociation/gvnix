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
package org.gvnix.web.screen.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;

/**
 * This Listener produces MVC artifacts for a given PatternJspMetadata
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component(immediate = true)
@Service
public class PatternJspMetadataListener extends
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
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PatternMetadata.getMetadataIdentiferType(), getProvidesType());
        this.context = context;
        _fileManager = fileManager;
        _tilesOperations = tilesOperations;
        _menuOperations = menuOperations;
        _projectOperations = projectOperations;
        _propFileOperations = propFileOperations;
        _webScreenOperations = webScreenOperations;
        _metadataService = metadataService;
        _physicalTypeMetadataProvider = physicalTypeMetadataProvider;
    }

    public MetadataItem get(String metadataIdentificationString) {
        JavaType javaType = PatternJspMetadata
                .getJavaType(metadataIdentificationString);
        Path path = PatternJspMetadata.getPath(metadataIdentificationString);
        String patternMetadataKey = PatternMetadata.createIdentifier(javaType,
                path);
        PatternMetadata patternMetadata = (PatternMetadata) metadataService
                .get(patternMetadataKey);

        if (patternMetadata == null || !patternMetadata.isValid()) {
            return null;
        }

        webScaffoldMetadata = patternMetadata.getWebScaffoldMetadata();
        Assert.notNull(webScaffoldMetadata, "Web scaffold metadata required");

        webScaffoldAnnotationValues = webScaffoldMetadata.getAnnotationValues();
        Assert.notNull(webScaffoldAnnotationValues,
                "Web scaffold annotation values required");

        formbackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        Assert.notNull(formbackingType, "formbackingType required");
        entityName = uncapitalize(formbackingType.getSimpleTypeName());

        MemberDetails memberDetails = webMetadataService
                .getMemberDetails(formbackingType);
        JavaTypeMetadataDetails formBackingTypeMetadataDetails = webMetadataService
                .getJavaTypeMetadataDetails(formbackingType, memberDetails,
                        metadataIdentificationString);
        Assert.notNull(
                formBackingTypeMetadataDetails,
                "Unable to obtain metadata for type "
                        + formbackingType.getFullyQualifiedTypeName());

        eligibleFields = webMetadataService.getScaffoldEligibleFieldMetadata(
                formbackingType, memberDetails, metadataIdentificationString);

        if (eligibleFields.size() == 0) {
            return null;
        }

        relatedDomainTypes = patternMetadata
                .getRelatedApplicationTypeMetadata();
        Assert.notNull(relatedDomainTypes, "Related domain types required");

        formbackingTypeMetadata = relatedDomainTypes.get(formbackingType);
        Assert.notNull(formbackingTypeMetadata,
                "Form backing type metadata required");

        formbackingTypePersistenceMetadata = formbackingTypeMetadata
                .getPersistenceDetails();
        if (formbackingTypePersistenceMetadata == null) {
            return null;
        }

        for (String definedPattern : patternMetadata.getDefinedPatterns()) {
            installMvcArtifacts(definedPattern);
        }

        return new PatternJspMetadata(metadataIdentificationString,
                patternMetadata);
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Assert.isTrue(
                    MetadataIdentificationUtils.getMetadataClass(
                            upstreamDependency).equals(
                            MetadataIdentificationUtils
                                    .getMetadataClass(PatternMetadata
                                            .getMetadataIdentiferType())),
                    "Expected class-level notifications only for gvNIX Report metadata (not '"
                            + upstreamDependency + "')");

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            JavaType javaType = PatternMetadata.getJavaType(upstreamDependency);
            Path path = PatternMetadata.getPath(upstreamDependency);
            downstreamDependency = PatternJspMetadata.createIdentifier(
                    javaType, path);

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                    .contains(downstreamDependency)) {
                return;
            }
        } else {
            return;
        }

        // We should now have an instance-specific "downstream dependency"that
        // can be processed by this class
        Assert.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        metadataService.get(downstreamDependency, true);

    }

    @Override
    public boolean isRelatedPattern() {
        return false;
    }

    public String getProvidesType() {
        return PatternJspMetadata.getMetadataIdentiferType();
    }

}
