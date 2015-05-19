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

package org.gvnix.addon.loupefield.addon;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.loupefield.annotations.GvNIXLoupeController;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.framework.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.plural.addon.PluralMetadata;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.*;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.*;
import org.w3c.dom.*;

/**
 * Implementation of operations this add-on offers.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1
 */
@Component
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class LoupefieldOperationsImpl implements LoupefieldOperations {

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private FileManager fileManager;

    private PathResolver pathResolver;

    private I18nSupport i18nSupport;

    private ProjectOperations projectOperations;

    private PropFileOperations propFileOperations;

    private TypeLocationService typeLocationService;

    private TypeManagementService typeManagementService;

    private MetadataService metadataService;

    private PersistenceMemberLocator persistenceMemberLocator;

    private WebProjectUtils webProjectUtils;
    private MessageBundleUtils messageBundleUtils;

    private MemberDetailsScanner memberDetailsScanner;

    private static final Logger LOGGER = HandlerUtils
            .getLogger(LoupefieldOperationsImpl.class);

    private static final JavaType ANNOTATION_LOUPE_CONTROLLER = new JavaType(
            "org.gvnix.addon.loupefield.annotations.GvNIXLoupeController");

    protected void activate(final ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /** {@inheritDoc} */
    public boolean isSetupCommandAvailable() {
        // If jQuery is installed, setup command is available
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-jquery")
                && !getProjectOperations().isFeatureInstalledInFocusedModule(
                        "gvnix-loupe");
    }

    /** {@inheritDoc} */
    public boolean isUpdateCommandAvailable() {
        // If loupefields addon is installed, update command is available
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-loupe");
    }

    /** {@inheritDoc} */
    public boolean isSetCommandAvailable() {
        // If loupefields addon is installed, set command is available
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-loupe");
    }

    /** {@inheritDoc} */
    public void setup() {
        // Adding tags/loupefield/select.tagx
        addTagx();
        // Adding scripts/loupefield/jquery.loupeField.ext.gvnix.js
        addLoupeFunctions();
        // Adding styles/loupefield/loupeField.css
        addLoupeStyles();
        // Add necessary properties to messages.properties
        addI18nProperties();
        // Include jquery.loupeField.ext.gvnix.js into load-scripts.tagx
        addToLoadScripts("loupe_js_url",
                "/resources/scripts/loupefield/jquery.loupeField.ext.gvnix.js",
                false);
        // Add style css for components
        addToLoadScripts("loupe_css_url",
                "/resources/styles/loupefield/loupeField.css", true);
        // Add Necessary Dependencies
        setupProjectPom();
    }

    /** {@inheritDoc} */
    public void update() {
        // Adding tags/loupefield/select.tagx
        updateTagx();
        // Adding scripts/loupefield/jquery.loupeField.ext.gvnix.js
        updateLoupeFunctions();
        // Adding styles/loupefield/loupeField.css
        updateLoupeStyles();
        // Add necessary properties to messages.properties
        addI18nProperties();
        // Include jquery.loupeField.ext.gvnix.js into load-scripts.tagx
        addToLoadScripts("loupe_js_url",
                "/resources/scripts/loupefield/jquery.loupeField.ext.gvnix.js",
                false);
        // Add loupeField.css on load Scripts
        addToLoadScripts("loupe_css_url",
                "/resources/styles/loupefield/loupeField.css", true);
        // Add Necessary Dependencies
        setupProjectPom();
    }

    /** {@inheritDoc} */
    public void setLoupeController(JavaType controller) {
        Validate.notNull(controller, "Controller Java Type required");

        // Adding annotation to Controller
        doAddControllerAnnotation(controller);

        // Adding uri to create.jspx and update.jspx views
        updateCreateAndUpdateViews(controller);
    }

    /** {@inheritDoc} */
    public void setLoupeField(JavaType controller, JavaSymbolName field,
            String additionalFields, String caption, String baseFilter,
            String listPath, String max) {
        Validate.notNull(controller, "Controller Java Type required");

        // Getting existing controller and webscaffold annotation values
        ClassOrInterfaceTypeDetails existingController = getTypeLocationService()
                .getTypeDetails(controller);
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                existingController);

        // Checks if controller is annotated with GvNIXLoupeController
        if (!isControllerAnnotated(existingController)) {
            LOGGER.log(
                    Level.INFO,
                    "Controller "
                            .concat(controller.getSimpleTypeName())
                            .concat(" must be annoted with @GvNIXLoupeController. Use 'web mvc loupe set' to annote controller and update views."));
        }

        // Checks if field exists and gets type
        JavaType fieldType = existsField(annotationValues, field);
        if (fieldType == null) {
            return;
        }

        // Getting Related entity and its fields
        ClassOrInterfaceTypeDetails relatedEntity = getTypeLocationService()
                .getTypeDetails(fieldType);

        MemberDetails memberDetails = getMemberDetailsScanner()
                .getMemberDetails(getClass().getName(), relatedEntity);

        if (relatedEntity == null) {
            LOGGER.log(Level.INFO, String.format(
                    "Field '%s' could not implements Loupe Field.",
                    StringUtils.uncapitalize(field.getReadableSymbolName())));
            return;
        }

        List<? extends FieldMetadata> relatedFields = memberDetails.getFields();
        if (relatedFields == null) {
            LOGGER.log(Level.INFO, String.format(
                    "Field '%s' could not implements Loupe Field.",
                    StringUtils.uncapitalize(field.getReadableSymbolName())));
            return;
        }

        // Checks if additional fields exists as fields in related entity
        if (!checkIfAdditionalFieldsExists(relatedEntity, relatedFields,
                additionalFields)) {
            return;
        }

        // Checks if caption field exists as field in related entity
        if (!checkIfCaptionExists(relatedEntity, relatedFields, caption)) {
            return;
        }

        // Checks if listPath view exists in project
        if (StringUtils.isNotBlank(listPath)) {
            if (!existsView(listPath)) {
                return;
            }
        }

        // Getting identifiers
        List<FieldMetadata> identifiers = getPersistenceMemberLocator()
                .getIdentifierFields(fieldType);

        if (identifiers.isEmpty()) {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "Could not locate any field annoted with @Id for entity '%s'",
                            fieldType.getSimpleTypeName()));
            return;
        }

        // Getting plural
        final PluralMetadata pluralMetadata = (PluralMetadata) getMetadataService()
                .get(PluralMetadata.createIdentifier(fieldType,
                        PhysicalTypeIdentifier.getPath(relatedEntity
                                .getDeclaredByMetadataId())));
        String plural = pluralMetadata.getPlural().toLowerCase();

        // Update field in views create and update
        updateViews(identifiers, plural, annotationValues.getPath(), field,
                additionalFields, caption, baseFilter, listPath, "create");
        updateViews(identifiers, plural, annotationValues.getPath(), field,
                additionalFields, caption, baseFilter, listPath, "update");

        // Alert if additionalFields is empty, only can search by id
        if (additionalFields == null) {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "INFO: You don't specify additionalFields, so you can filter by '%s' only",
                            identifiers.get(0).getFieldName().getSymbolName()));
        }

        // Creates loupe-callbacks.js and add to load-script.js if not
        // exists
        addCallbacksFile();

        // Show message to developer with callbacks configuration
        LOGGER.log(
                Level.INFO,
                String.format(
                        "INFO: You can configure callbacks functions for field '%s' editing '%s'. You can add onDraw%s%s function and onSet%s%s function if not exists yet.",
                        field.getSymbolName(),
                        "scripts/loupefield/loupe-callbacks.js", field
                                .getReadableSymbolName(), annotationValues
                                .getFormBackingObject().getSimpleTypeName(),
                        field.getReadableSymbolName(), annotationValues
                                .getFormBackingObject().getSimpleTypeName()));

    }

    /**
     * This method adds <code>tags/loupefield/loupe.tagx</code> to the tags
     * folder
     */
    public void addTagx() {

        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/loupefield/loupe.tagx");

        if (!getFileManager().exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "tag/loupe.tagx");
                outputStream = getFileManager().createFile(filePath)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    /**
     * This method update <code>tags/loupefield/select.tagx</code> with the
     * current tagx version
     */
    public void updateTagx() {

        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/loupefield/loupe.tagx");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils
                    .getInputStream(getClass(), "tag/loupe.tagx");
            if (!getFileManager().exists(filePath)) {
                outputStream = getFileManager().createFile(filePath)
                        .getOutputStream();
            }
            else {
                outputStream = getFileManager().updateFile(filePath)
                        .getOutputStream();
            }
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * This method adds
     * <code>scripts/loupefield/jquery.loupeField.ext.gvnix.js</code> to the
     * scripts folder
     */
    public void addLoupeFunctions() {
        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                "scripts/loupefield/jquery.loupeField.ext.gvnix.js");

        if (!getFileManager().exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "scripts/jquery.loupeField.ext.gvnix.js");
                outputStream = getFileManager().createFile(filePath)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    /**
     * This method adds <code>styles/loupefield/loupeField.css</code> to the
     * styles folder
     */
    public void addLoupeStyles() {
        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "styles/loupefield/loupeField.css");

        if (!getFileManager().exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "styles/loupeField.css");
                outputStream = getFileManager().createFile(filePath)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    /**
     * This method adds <code>scripts/loupefield/loupe-callbacks.js</code> to
     * the scripts folder and add to load-scripts.js to load in all pages
     */
    public void addCallbacksFile() {
        // Adding callbacks .js file
        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "scripts/loupefield/loupe-callbacks.js");
        if (!getFileManager().exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "scripts/loupe-callbacks.js");
                outputStream = getFileManager().createFile(filePath)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
            // Adding to load-scripts
            addToLoadScripts("loupe_callbacks_js_url",
                    "/resources/scripts/loupefield/loupe-callbacks.js", false);

        }
    }

    /**
     * This method updates
     * <code>scripts/loupefield/jquery.loupeField.ext.gvnix.js</code> with the
     * current version
     */
    public void updateLoupeFunctions() {
        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                "scripts/loupefield/jquery.loupeField.ext.gvnix.js");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    "scripts/jquery.loupeField.ext.gvnix.js");
            if (!getFileManager().exists(filePath)) {
                outputStream = getFileManager().createFile(filePath)
                        .getOutputStream();
            }
            else {
                outputStream = getFileManager().updateFile(filePath)
                        .getOutputStream();
            }
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * This method updates <code>styles/loupefield/loupeField.css</code> to the
     * styles folder
     */
    public void updateLoupeStyles() {
        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "styles/loupefield/loupeField.css");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    "styles/loupeField.css");
            outputStream = getFileManager().createFile(filePath)
                    .getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * This method add necessary properties to messages.properties
     */
    public void addI18nProperties() {
        // Check if Valencian_Catalan language is supported and add properties
        // if so
        Set<I18n> supportedLanguages = getI18nSupport().getSupportedLanguages();
        for (I18n i18n : supportedLanguages) {
            if (i18n.getLocale().equals(new Locale("ca"))) {
                getMessageBundleUtils().installI18nMessages(
                        new ValencianCatalanLanguage(), getProjectOperations(),
                        getFileManager());
                getMessageBundleUtils().addPropertiesToMessageBundle("ca",
                        getClass(), getPropFileOperations(),
                        getProjectOperations(), getFileManager());
                break;
            }
        }
        // Add properties to Spanish messageBundle
        getMessageBundleUtils().installI18nMessages(new SpanishLanguage(),
                getProjectOperations(), getFileManager());
        getMessageBundleUtils().addPropertiesToMessageBundle("es", getClass(),
                getPropFileOperations(), getProjectOperations(),
                getFileManager());

        // Add properties to default messageBundle
        getMessageBundleUtils().addPropertiesToMessageBundle("en", getClass(),
                getPropFileOperations(), getProjectOperations(),
                getFileManager());
    }

    /**
     * This method adds reference in laod-script.tagx to use
     * jquery.loupeField.ext.gvnix.js
     */
    public void addToLoadScripts(String varName, String url, boolean isCss) {
        // Modify Roo load-scripts.tagx
        String docTagxPath = getPathResolver().getIdentifier(getWebappPath(),
                "WEB-INF/tags/util/load-scripts.tagx");

        Validate.isTrue(getFileManager().exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = getFileManager().updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        if (isCss) {
            modified = getWebProjectUtils().addCssToTag(docTagx, root, varName,
                    url)
                    || modified;
        }
        else {
            modified = getWebProjectUtils().addJSToTag(docTagx, root, varName,
                    url)
                    || modified;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }

    }

    private LogicalPath getWebappPath() {
        return getWebProjectUtils().getWebappPath(getProjectOperations());
    }

    /**
     * Annotates given Controller with GvNIXLoupeController
     * 
     * @param controller
     */
    private void doAddControllerAnnotation(JavaType controller) {
        Validate.notNull(controller, "Controller required");

        // Getting current controller
        ClassOrInterfaceTypeDetails existingController = getTypeLocationService()
                .getTypeDetails(controller);

        // Get @Controller annotation
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                existingController);
        JavaType entity = annotationValues.getFormBackingObject();
        // Validating if is a controller
        Validate.notNull(entity, "Operation only supported for controllers");

        // Checking if is already annoted
        final boolean isAlreadyAnnotated = MemberFindingUtils
                .getAnnotationOfType(existingController.getAnnotations(),
                        ANNOTATION_LOUPE_CONTROLLER) != null;

        if (!isAlreadyAnnotated) {
            ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    existingController);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    ANNOTATION_LOUPE_CONTROLLER);

            // Add annotation to target type
            detailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            getTypeManagementService().createOrUpdateTypeOnDisk(
                    detailsBuilder.build());
        }

    }

    /**
     * Update project pom: install repositories and dependencies
     */
    private void setupProjectPom() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            getProjectOperations().addRepositories(
                    getProjectOperations().getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            getProjectOperations().addProperty(
                    getProjectOperations().getFocusedModuleName(),
                    new Property(property));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(
                getMetadataService(), getProjectOperations(), depens);
    }

    /**
     * This method updates create and update views adding loupefield uri
     * 
     * @param controller
     */
    private void updateCreateAndUpdateViews(JavaType controller) {
        Map<String, String> uriMap = new HashMap<String, String>(1);
        uriMap.put("xmlns:loupefield", "urn:jsptagdir:/WEB-INF/tags/loupefield");

        ClassOrInterfaceTypeDetails existingController = getTypeLocationService()
                .getTypeDetails(controller);
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                existingController);

        updateJspx(controller, annotationValues.getPath(), uriMap, "create");
        updateJspx(controller, annotationValues.getPath(), uriMap, "update");
    }

    /**
     * @param controller
     * @param controllerPath
     * @param uriMap
     * @param jspxName
     */
    private void updateJspx(JavaType controller, String controllerPath,
            Map<String, String> uriMap, String jspxName) {
        Validate.notBlank(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");
        Validate.isTrue(controllerPath != null && !controllerPath.isEmpty(),
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + controller.getSimpleTypeName() + "'");

        if (controllerPath != null) {
            getWebProjectUtils().addTagxUriInJspx(controllerPath, jspxName,
                    uriMap, getProjectOperations(), getFileManager());
        }
    }

    /**
     * This method checks if a view exists in the project
     * 
     * @param path
     * @return
     */
    private boolean existsView(String path) {
        String viewFileName = path.concat(".jspx");
        final String filePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/".concat(viewFileName));
        if (!getFileManager().exists(filePath)) {
            LOGGER.log(
                    Level.INFO,
                    "View '".concat(viewFileName).concat(
                            "' doesn't exists in proyect."));
            return false;
        }
        return true;
    }

    /**
     * 
     * This method checks if controller is annotated with @GvNIXLoupeController
     * 
     * @param controller
     * @return
     */
    private boolean isControllerAnnotated(ClassOrInterfaceTypeDetails controller) {
        AnnotationMetadata annotations = controller.getAnnotation(new JavaType(
                GvNIXLoupeController.class));
        if (annotations == null) {
            return false;
        }
        return true;
    }

    /**
     * This method checks if field exists in the Controller related entity
     * 
     * @param controller
     * @return
     */
    private JavaType existsField(WebScaffoldAnnotationValues annotationValues,
            JavaSymbolName field) {
        JavaType entity = annotationValues.getFormBackingObject();

        final ClassOrInterfaceTypeDetails cid = getTypeLocationService()
                .getTypeDetails(entity);

        MemberDetails memberDetails = getMemberDetailsScanner()
                .getMemberDetails(getClass().getName(), cid);

        if (cid == null) {
            LOGGER.log(Level.INFO,
                    "Controller Entity cannnot be resolved to a type in your project");
            return null;
        }

        List<? extends FieldMetadata> fieldList = memberDetails.getFields();
        Iterator<? extends FieldMetadata> it = fieldList.iterator();

        JavaType fieldType = null;

        boolean exists = false;

        while (it.hasNext()) {
            FieldMetadata currentField = it.next();
            if (field.getReadableSymbolName().equals(
                    currentField.getFieldName().getReadableSymbolName())) {
                fieldType = currentField.getFieldType();
                exists = true;
            }
        }
        if (!exists) {
            LOGGER.log(Level.INFO, "The field '" + field.getSymbolName()
                    + "' can not be resolved as field of your entity.");
            return null;
        }

        return fieldType;
    }

    /**
     * 
     * This method checks if all additionalField exists
     * 
     * @param entity
     * @param relatedFields
     * @param additionalFields
     */
    private boolean checkIfAdditionalFieldsExists(
            ClassOrInterfaceTypeDetails entity,
            List<? extends FieldMetadata> relatedFields, String additionalFields) {
        if (StringUtils.isNotBlank(additionalFields)) {
            String[] additionalFieldsList = additionalFields.split(",");
            for (int i = 0; i < additionalFieldsList.length; i++) {
                boolean exists = false;
                Iterator<? extends FieldMetadata> it = relatedFields.iterator();
                while (it.hasNext()) {
                    FieldMetadata relatedField = it.next();
                    String additionalField = additionalFieldsList[i];
                    String relatedFieldName = StringUtils
                            .uncapitalize(relatedField.getFieldName()
                                    .getSymbolName());
                    if (relatedFieldName.equals(additionalField)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    LOGGER.log(
                            Level.INFO,
                            String.format(
                                    "Additional field '%s' doesn't exists in related entity '%s'",
                                    additionalFieldsList[i], entity.getName()
                                            .getSimpleTypeName()));
                    return false;
                }

            }
        }
        return true;
    }

    /**
     * This method checks if caption exists as a related field
     * 
     * @param entity
     * @param relatedFields
     * @param caption
     * @return
     */
    private boolean checkIfCaptionExists(ClassOrInterfaceTypeDetails entity,
            List<? extends FieldMetadata> relatedFields, String caption) {
        if (StringUtils.isNotBlank(caption)) {
            boolean exists = false;
            Iterator<? extends FieldMetadata> it = relatedFields.iterator();
            while (it.hasNext()) {
                FieldMetadata relatedField = it.next();
                String relatedFieldName = StringUtils.uncapitalize(relatedField
                        .getFieldName().getSymbolName());
                if (relatedFieldName.equals(caption)) {
                    exists = true;
                }
            }
            if (!exists) {
                LOGGER.log(
                        Level.INFO,
                        String.format(
                                "Caption field '%s' doesn't exists in related entity '%s'",
                                caption, entity.getName().getSimpleTypeName()));
                return false;
            }

        }
        return true;
    }

    /**
     * 
     * This method update field in view to use loupe element
     * 
     * @param controller
     * @param path
     * @param field
     * @param additionalFields
     * @param caption
     * @param baseFilter
     * @param listPath
     * @param max
     */
    private void updateViews(List<FieldMetadata> identifiers,
            String entityPlural, String path, JavaSymbolName field,
            String additionalFields, String caption, String baseFilter,
            String listPath, String viewName) {

        String relativePath = "WEB-INF/views/".concat(path).concat("/")
                .concat(viewName).concat(".jspx");

        String docJspx = getPathResolver().getIdentifier(
                getWebProjectUtils().getWebappPath(getProjectOperations()),
                relativePath);

        Document docJspXml = getWebProjectUtils().loadXmlDocument(docJspx,
                getFileManager());
        if (docJspXml == null) {
            LOGGER.log(Level.INFO,
                    "Could not locate file '".concat(relativePath).concat("'"));
            return;
        }

        Element docRoot = docJspXml.getDocumentElement();
        Element form = XmlUtils.findFirstElement(
                String.format("/div/%s", viewName), docRoot);
        Element element = XmlUtils.findFirstElement(
                String.format("/div/%s/*[@field='%s']", viewName,
                        StringUtils.uncapitalize(field.getSymbolName())),
                docRoot);

        if (element == null) {
            LOGGER.log(Level.INFO, String.format(
                    "Could not locate field '%s' on '%s/%s'",
                    StringUtils.uncapitalize(field.getSymbolName()), path,
                    viewName));
            return;
        }

        // Creating loupe element
        Element loupe = docJspXml.createElement("loupefield:loupe");

        // Copying element attributes to new loupe element
        NamedNodeMap elementAttributes = element.getAttributes();
        for (int i = 0; i < elementAttributes.getLength(); i++) {
            Node attr = elementAttributes.item(i);
            loupe.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }

        // Changing z value
        loupe.setAttribute("z", "user-managed");

        // Adding pkField
        String pkField = identifiers.get(0).getFieldName().getSymbolName();
        loupe.setAttribute("pkField", pkField);

        // Adding controllerPath
        loupe.setAttribute("controllerPath", path);

        // Adding additionalFields attribute
        if (StringUtils.isNotBlank(additionalFields)) {

            loupe.setAttribute("additionalFields", additionalFields);

        }

        // Adding caption Attribute
        if (StringUtils.isNotBlank(caption)) {
            loupe.setAttribute("caption", caption);
        }

        // Adding baseFilter
        if (StringUtils.isNotBlank(baseFilter)) {
            loupe.setAttribute("baseFilter", baseFilter);
        }

        // Adding listPath
        if (StringUtils.isNotBlank(listPath)) {
            loupe.setAttribute("listPath", listPath);
        }
        else {
            String entityPath = entityPlural.concat("/list");
            loupe.setAttribute("listPath", entityPath);
        }

        // Append new loupe element to view
        form.appendChild(loupe);

        // Remove old element
        form.removeChild(element);

        DomUtils.removeTextNodes(docJspXml);
        getFileManager().createOrUpdateTextFileIfRequired(docJspx,
                XmlUtils.nodeToString(docJspXml), true);

    }

    /***
     * FEATURE METHODS
     */

    @Override
    public String getName() {
        return FEATURE_NAME_GVNIX_LOUPEFIELDS;
    }

    @Override
    public boolean isInstalledInModule(String moduleName) {
        String dirPath = getPathResolver().getIdentifier(getWebappPath(),
                "scripts/loupefield/jquery.loupeField.ext.gvnix.js");
        return getFileManager().exists(dirPath);
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return fileManager;
        }
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
                LOGGER.warning("Cannot load PathResolver on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return pathResolver;
        }
    }

    public I18nSupport getI18nSupport() {
        if (i18nSupport == null) {
            // Get all Services implement I18nSupport interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(I18nSupport.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (I18nSupport) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load I18nSupport on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return i18nSupport;
        }
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
                LOGGER.warning("Cannot load ProjectOperations on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public PropFileOperations getPropFileOperations() {
        if (propFileOperations == null) {
            // Get all Services implement PropFileOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                PropFileOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (PropFileOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PropFileOperations on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return propFileOperations;
        }
    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeLocationService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return typeLocationService;
        }
    }

    public TypeManagementService getTypeManagementService() {
        if (typeManagementService == null) {
            // Get all Services implement TypeManagementService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeManagementService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeManagementService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeManagementService on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public PersistenceMemberLocator getPersistenceMemberLocator() {
        if (persistenceMemberLocator == null) {
            // Get all Services implement PersistenceMemberLocator interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                PersistenceMemberLocator.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (PersistenceMemberLocator) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PersistenceMemberLocator on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return persistenceMemberLocator;
        }
    }

    public WebProjectUtils getWebProjectUtils() {
        if (webProjectUtils == null) {
            // Get all Services implement WebProjectUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebProjectUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    webProjectUtils = (WebProjectUtils) this.context
                            .getService(ref);
                    return webProjectUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WebProjectUtils on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return webProjectUtils;
        }

    }

    public MessageBundleUtils getMessageBundleUtils() {
        if (messageBundleUtils == null) {
            // Get all Services implement MessageBundleUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MessageBundleUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    messageBundleUtils = (MessageBundleUtils) this.context
                            .getService(ref);
                    return messageBundleUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MessageBundleUtils on LoupeFieldOperationsImpl.");
                return null;
            }
        }
        else {
            return messageBundleUtils;
        }

    }

    public MemberDetailsScanner getMemberDetailsScanner() {
        if (memberDetailsScanner == null) {
            // Get all Services implement MemberDetailsScanner interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MemberDetailsScanner.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MemberDetailsScanner) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MemberDetailsScanner on "
                        .concat(getClass().getSimpleName()));
                return null;
            }
        }
        else {
            return memberDetailsScanner;
        }
    }
}