package org.gvnix.web.mvc.binding.roo.addon.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.web.mvc.binding.roo.addon.annotations.GvNIXStringTrimmerBinder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of commands that are available via the Roo shell
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.8
 */
@Component
@Service
public class WebBinderOperationsImpl implements WebBinderOperations {
    private static final Logger LOGGER = HandlerUtils
            .getLogger(WebBinderOperationsImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private OperationUtils operationUtils;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference
    private MetadataService metadataService;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given
     * annotation in the project
     */
    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private FileManager fileManager;

    @Reference
    private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isStringTrimmerAvailable() {
        return projectOperations.isProjectAvailable(projectOperations
                .getFocusedModuleName())
                && getOperationUtils().isSpringMvcProject(metadataService,
                        fileManager, projectOperations);
    }

    /** {@inheritDoc} */
    public void bindStringTrimmer(JavaType controller, boolean emptyAsNull) {
        setup();
        addBindStringTrimmer(controller, emptyAsNull);
    }

    /** {@inheritDoc} */
    public void bindStringTrimmerAll(boolean emptyAsNull) {
        setup();
        // Use the TypeLocationService to scan project for all types with a
        // specific annotation
        for (JavaType type : typeLocationService
                .findTypesWithAnnotation(new JavaType(
                        "org.springframework.stereotype.Controller"))) {
            addBindStringTrimmer(type, emptyAsNull);
        }
    }

    /**
     * Annotate provided Java type with @GvNIXStringTrimmerBinder
     * 
     * @param emptyAsNull
     */
    private void addBindStringTrimmer(JavaType controller, boolean emptyAsNull) {
        // Use Roo's Assert type for null checks
        Validate.notNull(controller, "Java type required");

        // Retrieve the Java source type the annotation is being added to
        ClassOrInterfaceTypeDetails mutableTypeDetails = typeLocationService
                .getTypeDetails(controller);

        // Check if provided JavaType is a @Controller class
        AnnotationMetadata controllerAnnotation = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        new JavaType(
                                "org.springframework.stereotype.Controller"));
        if (controllerAnnotation == null) {
            LOGGER.warning("Java type ".concat(controller.getSimpleTypeName())
                    .concat(" must be annotated with @Controller"));
            return;
        }

        // Test if the annotation already exists on the target type
        if (MemberFindingUtils.getAnnotationOfType(mutableTypeDetails
                .getAnnotations(),
                new JavaType(GvNIXStringTrimmerBinder.class.getName())) == null) {

            // Annotation Attributes
            BooleanAttributeValue bav = new BooleanAttributeValue(
                    new JavaSymbolName("emptyAsNull"), emptyAsNull);
            List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();
            attributes.add(bav);

            // Create JavaType instance for GvNIXStringTrimmerBinder annotation
            JavaType stringTrimmerBinder = new JavaType(
                    GvNIXStringTrimmerBinder.class.getName());

            // Create Annotation metadata
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    stringTrimmerBinder, attributes);

            // Add annotation to target type and save changes to disk
            ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    mutableTypeDetails);
            detailsBuilder.addAnnotation(annotationBuilder.build());
            typeManagementService.createOrUpdateTypeOnDisk(detailsBuilder
                    .build());
        }
    }

    /** {@inheritDoc} */
    public void setup() {
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Add addon repository and dependency to get annotations
        addAnnotations(configuration);
    }

    /**
     * Add addon repository and dependency to get annotations.
     * 
     * @param configuration Configuration element
     */
    private void addAnnotations(Element configuration) {

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {

            projectOperations.addRepository(projectOperations
                    .getFocusedModuleName(), new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);
    }

    public OperationUtils getOperationUtils() {
        if (operationUtils == null) {
            // Get all Services implement OperationUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                OperationUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    operationUtils = (OperationUtils) this.context
                            .getService(ref);
                    return operationUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load OperationUtils on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return operationUtils;
        }

    }
}
