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
package org.gvnix.web.exception.handler.roo.addon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.logging.HandlerUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Provides {@link ModalDialogMetadata}. This type is called by Roo to retrieve
 * the metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
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

    private static final Logger LOGGER = HandlerUtils
            .getLogger(ModalDialogMetadataProvider.class);

    private static final JavaSymbolName VALUE = new JavaSymbolName("value");
    private static final JavaType MODAL_DIALOGS = new JavaType(
            GvNIXModalDialogs.class.getName());

    private PathResolver pathResolver;
    private WebModalDialogOperations webModalDialogOperations;

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
                "org.springframework.stereotype.Controller"));
        // we don't need to listen changes over our Annotation
        // @GvNIXModalDialogs because it must always be in @Controller class
        // addMetadataTrigger(MODAL_DIALOGS);
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
                "org.springframework.stereotype.Controller"));
        // removeMetadataTrigger(MODAL_DIALOGS);
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Check if GvNIXModalDialogs support is set
        if (!isModalDialogSupported(aspectName)) {
            // Nothing to do if ModalDialogs aren't supported
            return null;
        }

        MemberDetails memberDetails = getMemberDetails(governorPhysicalTypeMetadata);
        JavaType controllerType = ModalDialogMetadata
                .getJavaType(metadataIdentificationString);
        getWebModalDialogOperations().addDefaultModalDialogAnnotation(
                controllerType);
        List<String> definedModalDialogs = getDefinedModalDialogsIfAny(controllerType);
        return new ModalDialogMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, memberDetails,
                definedModalDialogs, getFileManager(), getPathResolver());
    }

    private List<String> getDefinedModalDialogsIfAny(JavaType aspectName) {
        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                .getTypeDetails(aspectName);
        if (controllerDetails == null) {
            return null;
        }
        // Test if has the @Controller
        Validate.notNull(
                MemberFindingUtils.getAnnotationOfType(controllerDetails
                        .getAnnotations(), new JavaType(
                        "org.springframework.stereotype.Controller")),
                aspectName.getSimpleTypeName().concat(
                        " has not @Controller annotation"));

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        MODAL_DIALOGS);
        List<String> modalDialogsList = new ArrayList<String>();
        if (annotationMetadata != null) {
            // @GvNIXModalDialog already exists

            // Loads previously registered modal dialog into modalDialogsList
            // Also checks if name is used previously
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(VALUE);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();

                if (previousValues != null && !previousValues.isEmpty()) {
                    for (StringAttributeValue value : previousValues) {
                        if (!modalDialogsList.contains(value.getValue())) {
                            modalDialogsList.add(value.getValue());
                        }
                    }
                }
            }
        }
        return modalDialogsList;
    }

    /**
     * GvNIXModalDialogs are supported if Dialog class is installed in project
     * and message-box.tagx is of type 'modal'.
     * 
     * @param aspectName
     * @return
     */
    private boolean isModalDialogSupported(JavaType aspectName) {
        String aspectPackage = aspectName.getPackage()
                .getFullyQualifiedPackageName();
        String modalDialogTypePath = getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                File.separator.concat(
                        aspectPackage.replace(".", File.separator)).concat(
                        "/dialog/Dialog.java"));

        return getFileManager().exists(modalDialogTypePath)
                && getWebModalDialogOperations().isMessageBoxOfTypeModal();
    }

    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXModalDialog";
    }

    public String getProvidesType() {
        return ModalDialogMetadata.getMetadataIdentiferType();
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return ModalDialogMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = ModalDialogMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = ModalDialogMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (PathResolver) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on ModalDialogMetadataProvider.");
                return null;
            }
        }
        else {
            return pathResolver;
        }
    }

    public WebModalDialogOperations getWebModalDialogOperations() {
        if (webModalDialogOperations == null) {
            // Get all Services implement WebModalDialogOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebModalDialogOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WebModalDialogOperations) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WebModalDialogOperations on ModalDialogMetadataProvider.");
                return null;
            }
        }
        else {
            return webModalDialogOperations;
        }
    }

}
