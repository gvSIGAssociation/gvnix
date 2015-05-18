/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.web.mvc.binding.roo.addon.addon.provider;

import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.mvc.binding.roo.addon.addon.metadata.StringTrimmerBinderMetadata;
import org.gvnix.web.mvc.binding.roo.addon.annotations.GvNIXStringTrimmerBinder;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link StringTrimmerBinderMetadata}. This type is called by Roo to
 * retrieve the metadata for this add-on. Use this type to reference external
 * types and services needed by the metadata type. Register metadata triggers
 * and dependencies here. Also define the unique add-on ITD identifier.
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.8
 */
@Component
@Service
public final class StringTrimmerBinderMetadataProvider extends
        AbstractItdMetadataProvider {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(StringTrimmerBinderMetadataProvider.class);

    private static final JavaType GVNIX_STRING_TRIMMER_BINDER = new JavaType(
            GvNIXStringTrimmerBinder.class.getName());

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    private ProjectOperations projectOperations;

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
        addMetadataTrigger(GVNIX_STRING_TRIMMER_BINDER);
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
        removeMetadataTrigger(GVNIX_STRING_TRIMMER_BINDER);
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails controllerDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Validate.notNull(
                controllerDetails,
                "Governor failed to provide class type details, in violation of superclass contract");

        AnnotationMetadata stringTrimmerAnnotation = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        GVNIX_STRING_TRIMMER_BINDER);

        boolean emptyAsNull = ((BooleanAttributeValue) stringTrimmerAnnotation
                .getAttribute(new JavaSymbolName("emptyAsNull"))).getValue()
                .booleanValue();
        // Pass dependencies required by the metadata in through its constructor
        return new StringTrimmerBinderMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, emptyAsNull);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXStringTrimmerBinder.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXStringTrimmerBinder";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = StringTrimmerBinderMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = StringTrimmerBinderMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, LogicalPath
                .getInstance(path.getPath(), getProjectOperations()
                        .getFocusedModuleName()));
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return StringTrimmerBinderMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return StringTrimmerBinderMetadata.getMetadataIdentiferType();
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
                LOGGER.warning("Cannot load ProjectOperations on StringTrimmerBinderMetadataProvider.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }
}
