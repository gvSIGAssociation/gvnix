/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.gvnix.flex.roo.addon;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;

/**
 * {@link MetadataProvider} for scaffolded Flex remoting destinations.
 * 
 * @author Jeremy Grelle
 */
@Component(immediate = true)
@Service
public class FlexScaffoldMetadataProvider extends AbstractItdMetadataProvider {

    @Reference
    private WebMetadataService webMetadataService;

    protected void activate(ComponentContext context) {
        this.metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(RooFlexScaffold.class.getName()));
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return FlexScaffoldMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = FlexScaffoldMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = FlexScaffoldMetadata
                .getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier
                .createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // We need to parse the annotation, which we expect to be present
        FlexScaffoldAnnotationValues annotationValues = new FlexScaffoldAnnotationValues(
                governorPhysicalTypeMetadata);
        if (!annotationValues.isAnnotationFound()
                || annotationValues.entity == null) {
            return null;
        }

        // Lookup the form backing object's metadata
        JavaType entityType = annotationValues.entity;
        Path path = Path.SRC_MAIN_JAVA;
        String entityMetadataKey = JpaActiveRecordMetadata.createIdentifier(
                entityType, LogicalPath.getInstance(path, ""));

        // We need to lookup the metadata we depend on
        JpaActiveRecordMetadata entityMetadata = (JpaActiveRecordMetadata) this.metadataService
                .get(entityMetadataKey);
        // We need to abort if we couldn't find dependent metadata
        if (entityMetadata == null || !entityMetadata.isValid()) {
            return null;
        }

        // We need to be informed if our dependent metadata changes
        this.metadataDependencyRegistry.registerDependency(entityMetadataKey,
                metadataIdentificationString);

        PhysicalTypeMetadata entityPhysicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(PhysicalTypeIdentifier.createIdentifier(entityType,
                        LogicalPath.getInstance(path, "")));
        Validate.notNull(
                entityPhysicalTypeMetadata,
                "Unable to obtain physical type metdata for type "
                        + entityType.getFullyQualifiedTypeName());
        ClassOrInterfaceTypeDetails entityClassOrInterfaceDetails = (ClassOrInterfaceTypeDetails) entityPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        MemberDetails entityMemberDetails = memberDetailsScanner
                .getMemberDetails(getClass().getName(),
                        entityClassOrInterfaceDetails);

        return new FlexScaffoldMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, annotationValues,
                entityMetadata,
                webMetadataService.getDynamicFinderMethodsAndFields(entityType,
                        entityMemberDetails, metadataIdentificationString),
                persistenceMemberLocator);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "Service";
    }

    public String getProvidesType() {
        return FlexScaffoldMetadata.getMetadataIdentiferType();
    }

}
