package org.gvnix.addon.loupefield;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 * 
 * @since 1.1
 */
@Component
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class LoupefieldOperationsImpl implements LoupefieldOperations {

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private I18nSupport i18nSupport;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private PropFileOperations propFileOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    private static final Logger LOGGER = HandlerUtils
            .getLogger(LoupefieldOperationsImpl.class);

    private static final JavaSymbolName PATH = new JavaSymbolName("path");
    private static final JavaSymbolName VALUE = new JavaSymbolName("value");

    private static final JavaType LOUPE_FIELD_ENTITY_ANNOTATION = new JavaType(
            "org.gvnix.addon.loupefield.LoupeFieldEntity");

    /** {@inheritDoc} */
    public boolean isSetupCommandAvailable() {
        // If jQuery is installed, setup command is available
        return projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-jquery")
                && !projectOperations
                        .isFeatureInstalledInFocusedModule("gvnix-loupe");
    }

    /** {@inheritDoc} */
    public boolean isSetCommandAvailable() {
        // If loupefields addon is installed, set command is available
        return projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-loupe");
    }

    /** {@inheritDoc} */
    public boolean isUpdateCommandAvailable() {
        // If loupefields addon is installed, update command is available
        return projectOperations
                .isFeatureInstalledInFocusedModule("gvnix-loupe");
    }

    /** {@inheritDoc} */
    public void setup() {
        // Adding tags/loupefield/select.tagx
        addTagx();
        // Adding scripts/loupefield/loupe-functions.js
        addLoupeFunctions();
        // Add necessary properties to messages.properties
        addI18nProperties();
        // Include loupe-functions.js into load-scripts.tagx
        addToLoadScripts();
    }

    /** {@inheritDoc} */
    public void setLoupeFields(JavaType controller, JavaType entity,
            JavaSymbolName field) {
        Validate.notNull(controller, "Controller Java Type required");
        Validate.notNull(entity, "Entity Java Type required");
        Validate.notNull(field, "Valid Field required");

        // Adding annotation to Entity
        doAddLoupeFieldEntityToEntity(entity, field);

    }

    /** {@inheritDoc} */
    public void update() {
        // Adding tags/loupefield/select.tagx
        updateTagx();
        // Adding scripts/loupefield/loupe-functions.js
        updateLoupeFunctions();
        // Add necessary properties to messages.properties
        addI18nProperties();
        // Include loupe-functions.js into load-scripts.tagx
        addToLoadScripts();
    }

    /**
     * This method adds <code>tags/loupefield/select.tagx</code> to the tags
     * folder
     */
    public void addTagx() {

        final String filePath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/loupefield/select.tagx");

        if (!fileManager.exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "tag/select.tagx");
                outputStream = fileManager.createFile(filePath)
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

        final String filePath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/loupefield/select.tagx");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    "tag/select.tagx");
            if (!fileManager.exists(filePath)) {
                outputStream = fileManager.createFile(filePath)
                        .getOutputStream();
            }
            else {
                outputStream = fileManager.updateFile(filePath)
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
     * This method adds <code>scripts/loupefield/loupe-functions.js</code> to
     * the scripts folder
     */
    public void addLoupeFunctions() {
        final String filePath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "scripts/loupefield/loupe-functions.js");

        if (!fileManager.exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "scripts/loupe-functions.js");
                outputStream = fileManager.createFile(filePath)
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
     * This method updates <code>scripts/loupefield/loupe-functions.js</code>
     * with the current version
     */
    public void updateLoupeFunctions() {
        final String filePath = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "scripts/loupefield/loupe-functions.js");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    "scripts/loupe-functions.js");
            if (!fileManager.exists(filePath)) {
                outputStream = fileManager.createFile(filePath)
                        .getOutputStream();
            }
            else {
                outputStream = fileManager.updateFile(filePath)
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
     * This method add necessary properties to messages.properties
     */
    public void addI18nProperties() {
        // Check if Valencian_Catalan language is supported and add properties
        // if so
        Set<I18n> supportedLanguages = i18nSupport.getSupportedLanguages();
        for (I18n i18n : supportedLanguages) {
            if (i18n.getLocale().equals(new Locale("ca"))) {
                MessageBundleUtils.installI18nMessages(
                        new ValencianCatalanLanguage(), projectOperations,
                        fileManager);
                MessageBundleUtils.addPropertiesToMessageBundle("ca",
                        getClass(), propFileOperations, projectOperations,
                        fileManager);
                break;
            }
        }
        // Add properties to Spanish messageBundle
        MessageBundleUtils.installI18nMessages(new SpanishLanguage(),
                projectOperations, fileManager);
        MessageBundleUtils.addPropertiesToMessageBundle("es", getClass(),
                propFileOperations, projectOperations, fileManager);

        // Add properties to default messageBundle
        MessageBundleUtils.addPropertiesToMessageBundle("en", getClass(),
                propFileOperations, projectOperations, fileManager);
    }

    /**
     * This method adds reference in laod-script.tagx to use loupe-functions.js
     */
    public void addToLoadScripts() {
        // Modify Roo load-scripts.tagx
        PathResolver pathResolver = projectOperations.getPathResolver();
        String docTagxPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/util/load-scripts.tagx");

        Validate.isTrue(fileManager.exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = fileManager.updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        // Add loupe-functions.js
        modified = WebProjectUtils.addJSToTag(docTagx, root, "loupe_js_url",
                "/resources/scripts/loupe-functions.js") || modified;

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }

    }

    private LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(projectOperations);
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
        PathResolver pathResolver = projectOperations.getPathResolver();
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "scripts/loupefield/loupe-functions.js");
        return fileManager.exists(dirPath);
    }

    /**
     * Annotations methods
     */
    private AnnotationMetadataBuilder getLoupeFieldEntityAnnotation(
            JavaType entity, JavaSymbolName field) {
        final List<AnnotationAttributeValue<?>> loupFieldAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        loupFieldAttributes.add(new StringAttributeValue(field, field
                .getSymbolName()));

        return new AnnotationMetadataBuilder(LOUPE_FIELD_ENTITY_ANNOTATION,
                loupFieldAttributes);
    }

    /**
     * Annotates given entity of its parent with LoupeFieldEntity
     * 
     * @param entity
     * @param fieldName
     */
    private void doAddLoupeFieldEntityToEntity(JavaType entity,
            JavaSymbolName field) {

        // Load class details. If class not found an exception will be raised.
        ClassOrInterfaceTypeDetails tmpDetails = typeLocationService
                .getTypeDetails(entity);

        // Checks if it's mutable
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class, tmpDetails,
                "Can't modify " + tmpDetails.getName());

        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                (ClassOrInterfaceTypeDetails) tmpDetails);

        List<? extends AnnotationMetadata> entityAnnotations = mutableTypeDetailsBuilder
                .build().getAnnotations();

        AnnotationMetadata loupeFieldAnnotation = MemberFindingUtils
                .getAnnotationOfType(entityAnnotations, new JavaType(
                        GvNIXLoupeFieldEntity.class.getName()));

        if (loupeFieldAnnotation != null) {
            // Already set annotation. Nothing to do
            LOGGER.info("Entity ".concat(entity.getFullyQualifiedTypeName()
                    .concat(" is already annotated with "
                            .concat(GvNIXLoupeFieldEntity.class.getName()))));
            return;
        }


        final List<AnnotationAttributeValue<?>> fieldValues = new ArrayList<AnnotationAttributeValue<?>>();

        fieldValues.add(new StringAttributeValue(new JavaSymbolName(
                "java.lang.String"), field.getSymbolName()));

        final List<AnnotationAttributeValue<?>> loupeFieldAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        loupeFieldAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<?>>(
                        new JavaSymbolName("fields"), fieldValues));


        // Creates GvNIXLoupeFieldEntity
        loupeFieldAnnotation = new AnnotationMetadataBuilder(new JavaType(
                GvNIXLoupeFieldEntity.class.getName()), loupeFieldAttributes)
                .build();

        // Adds GvNIXLoupeFieldEntity to the entity
        mutableTypeDetailsBuilder.addAnnotation(loupeFieldAnnotation);

        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());

    }

}