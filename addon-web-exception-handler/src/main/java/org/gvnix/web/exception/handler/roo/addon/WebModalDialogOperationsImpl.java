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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MetadataUtils;
import org.gvnix.support.OperationUtils;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Operations of Dialogs .
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebModalDialogOperationsImpl implements WebModalDialogOperations {

    private static final JavaSymbolName VALUE = new JavaSymbolName("value");
    private static final JavaSymbolName ARRAY_ELEMENT = new JavaSymbolName(
            "__ARRAY_ELEMENT__");

    private static final JavaType MODAL_DIALOGS = new JavaType(
            GvNIXModalDialogs.class.getName());

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference
    private SharedOperations sharedOperations;

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
        return OperationUtils.isProjectAvailable(metadataService)
                && OperationUtils.isSpringMvcProject(metadataService,
                        fileManager);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * addModalDialogAnnotation(org.springframework.roo.model.JavaType,
     * org.springframework.roo.model.JavaSymbolName)
     */
    public void addModalDialogAnnotation(JavaType controllerClass,
            JavaSymbolName name) {
        Assert.notNull(controllerClass, "controller is required");
        Assert.notNull(name, "name is required");

        annotateWithModalDialog(controllerClass, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * addDefaultModalDialogAnnotation(org.springframework.roo.model.JavaType)
     */
    public void addDefaultModalDialogAnnotation(JavaType controllerClass) {
        Assert.notNull(controllerClass, "controller is required");

        annotateWithModalDialog(controllerClass, null);
    }

    /**
     * Annotates given controller class with {@link GvNIXModalDialogs} with
     * value name if informed
     * 
     * @param controllerClass
     * @param name
     *            name of the modal dialog. May be null.
     */
    private void annotateWithModalDialog(JavaType controllerClass,
            JavaSymbolName name) {
        Assert.notNull(controllerClass, "controller is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        MutableClassOrInterfaceTypeDetails controllerDetails = MetadataUtils
                .getPhysicalTypeDetails(controllerClass, metadataService,
                        physicalTypeMetadataProvider);
        // Test if has the @RooWebScaffold
        Assert.notNull(
                MemberFindingUtils.getAnnotationOfType(controllerDetails
                        .getAnnotations(),
                        new JavaType(RooWebScaffold.class.getName())),
                controllerClass.getSimpleTypeName().concat(
                        " has not @RooWebScaffold annotation"));

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        MODAL_DIALOGS);

        // List of pattern to use
        List<StringAttributeValue> modalDialogsList = new ArrayList<StringAttributeValue>();

        boolean isAlreadyAnnotated = false;
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
                        if (!modalDialogsList.contains(value)) {
                            modalDialogsList.add(value);
                        }
                    }
                }
            }
            isAlreadyAnnotated = true;
        }

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                MODAL_DIALOGS);

        if (name != null) {
            StringAttributeValue newModalDialogValue = new StringAttributeValue(
                    ARRAY_ELEMENT, name.getSymbolName());
            if (!modalDialogsList.contains(newModalDialogValue)) {
                modalDialogsList.add(newModalDialogValue);
            }

            // Add attribute values
            annotationBuilder
                    .addAttribute(new ArrayAttributeValue<StringAttributeValue>(
                            VALUE, modalDialogsList));
        }

        if (isAlreadyAnnotated) {
            controllerDetails.updateTypeAnnotation(annotationBuilder.build(),
                    new HashSet<JavaSymbolName>());
        } else {
            controllerDetails.addTypeAnnotation(annotationBuilder.build());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * setupModalDialogsSupport()
     */
    public void setupModalDialogsSupport() {
        setupMavenDependency();
        sharedOperations.installWebServletClass("Dialog");
        sharedOperations.installMvcArtifacts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * setupMavenDependency()
     */
    public void setupMavenDependency() {
        Element configuration = XmlUtils.getConfiguration(getClass(),
                "configuration.xml");

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepository(new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);
        for (Element depen : depens) {
            projectOperations.addDependency(new Dependency(depen));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * isMessageBoxOfTypeModal()
     */
    public boolean isMessageBoxOfTypeModal() {
        String defaultJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/layouts/default.jspx");

        // TODO: Check if it's necessary to add message-box in home-default.jspx
        // layout (when exists)

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return false;
        }

        InputStream defulatJspxIs = fileManager.getInputStream(defaultJspx);

        Document defaultJspxXml;
        try {
            defaultJspxXml = XmlUtils.getDocumentBuilder().parse(defulatJspxIs);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element lsHtml = defaultJspxXml.getDocumentElement();

        // Check if dialog:message-box is of type modal
        String dialogNS = lsHtml.getAttribute("xmlns:dialog");
        if (dialogNS.equals("urn:jsptagdir:/WEB-INF/tags/dialog/modal")) {
            return true;
        }

        return false;
    }

}
