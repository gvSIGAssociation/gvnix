/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
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
package org.gvnix.occ.roo.addon;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.finder.FinderOperationsImpl;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.itd.ItdTriggerBasedMetadataProvider;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.logging.HandlerUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * gvNIX OCCChecksum Metadata provider
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class OCCChecksumMetadataProvider implements
        ItdTriggerBasedMetadataProvider, MetadataNotificationListener {

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private static final Logger LOGGER = HandlerUtils
            .getLogger(FinderOperationsImpl.class);

    // From AbstractItdMetadataProvider
    private boolean governorTypeDetail = true;
    private boolean governorBeingAClass = true;
    private MetadataService metadataService;
    private MetadataDependencyRegistry metadataDependencyRegistry;
    private FileManager fileManager;
    private MemberDetailsScanner memberDetailsScanner;

    private PersistenceMemberLocator persistenceMemberLocator;

    private TypeManagementService typeManagementService;

    /**
     * The annotations which, if present on a class or interface, will cause
     * metadata to be created
     */
    private final List<JavaType> metadataTriggers = new ArrayList<JavaType>();

    /** We don't care about trigger annotations; we always produce metadata */
    private boolean ignoreTriggerAnnotations = false;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        // Ensure we're notified of all metadata related to physical Java types,
        // in particular their initial creation
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXEntityOCCChecksum.class.getName()));

    }

    protected void deactivate(ComponentContext context) {
        // TODO:
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * createLocalIdentifier(org.springframework.roo.model.JavaType,
     * org.springframework.roo.project.Path)
     */
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return OCCChecksumMetadata.createIdentifier(javaType, path);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.roo.classpath.itd.ItdMetadataProvider#
     * getItdUniquenessFilenameSuffix()
     */
    public String getItdUniquenessFilenameSuffix() {
        return "gvNIX_occChecksum";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.roo.metadata.MetadataProvider#getProvidesType()
     */
    public String getProvidesType() {
        return OCCChecksumMetadata.getMetadataIdentiferType();
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.springframework.roo.classpath.itd.AbstractItdMetadataProvider#
     * getGovernorPhysicalTypeIdentifier(java.lang.String)
     */
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = OCCChecksumMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = OCCChecksumMetadata
                .getPath(metadataIdentificationString);
        String physicalTypeIdentifier = PhysicalTypeIdentifier
                .createIdentifier(javaType, path);
        return physicalTypeIdentifier;
    }

    protected OCCChecksumMetadata getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        Path path = Path.SRC_MAIN_JAVA;

        // We know governor type details are non-null and can be safely cast
        // We get govenor's EntityMetadata
        JavaType entityType = governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails().getName();
        String entityMetadataKey = JpaActiveRecordMetadata.createIdentifier(
                entityType, LogicalPath.getInstance(path, ""));

        // We get governor's Entity
        JpaActiveRecordMetadata entityMetadata = (JpaActiveRecordMetadata) getMetadataService()
                .get(entityMetadataKey);
        if (entityMetadata == null) {
            return null;
        }

        FieldMetadata versionField = getPersistenceMemberLocator()
                .getVersionField(entityType);

        if (versionField != null) {
            String declaredByType = entityMetadataKey
                    .substring(entityMetadataKey.lastIndexOf('?') + 1);
            if (!versionField.getDeclaredByMetadataId()
                    .endsWith(declaredByType)) {
                throw new IllegalStateException(
                        "You are trying to apply OCC Checksum on an Entity "
                                .concat("that extends of another one with a ")
                                .concat("@javax.persistence.Version field. ")
                                .concat("You should to apply OCC Checksum ")
                                .concat("over that Entity in your class: ")
                                .concat(declaredByType));
            }
        }

        List<FieldMetadata> idFields = getPersistenceMemberLocator()
                .getIdentifierFields(entityType);

        if (idFields.isEmpty()) {
            // Can't create metadata yet
            return null;
        }

        FieldMetadata idField = idFields.get(0);

        OCCChecksumMetadata metadata = new OCCChecksumMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, entityMetadata,
                getMemberDetailsScanner(), getTypeManagementService(),
                getPersistenceMemberLocator(), idField, versionField);

        return metadata;
    }

    // From AbstractItdMetadataProvider
    public final void notify(String upstreamDependency,
            String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Validate.isTrue(
                    MetadataIdentificationUtils.getMetadataClass(
                            upstreamDependency).equals(
                            MetadataIdentificationUtils
                                    .getMetadataClass(PhysicalTypeIdentifier
                                            .getMetadataIdentiferType())),
                    "Expected class-level notifications only for physical Java types (not '"
                            + upstreamDependency + "')");

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            JavaType javaType = PhysicalTypeIdentifier
                    .getJavaType(upstreamDependency);
            LogicalPath path = PhysicalTypeIdentifier
                    .getPath(upstreamDependency);
            downstreamDependency = createLocalIdentifier(javaType, path);

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (getMetadataDependencyRegistry().getDownstream(
                    upstreamDependency).contains(downstreamDependency)) {
                return;
            }
        }

        // We should now have an instance-specific "downstream dependency" that
        // can be processed by this class
        Validate.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        getMetadataService().evict(downstreamDependency);
        if (get(downstreamDependency) != null) {
            getMetadataDependencyRegistry().notifyDownstream(
                    downstreamDependency);
        }
    }

    /**
     * Registers an additional {@link JavaType} that will trigger metadata
     * registration.
     * 
     * @param javaType the type-level annotation to detect that will cause
     *        metadata creation (required)
     */
    public void addMetadataTrigger(JavaType javaType) {
        Validate.notNull(javaType,
                "Java type required for metadata trigger registration");
        this.metadataTriggers.add(javaType);
    }

    /**
     * Removes a {@link JavaType} metadata trigger registration. If the type was
     * never registered, the method returns without an error.
     * 
     * @param javaType to remove (required)
     */
    public void removeMetadataTrigger(JavaType javaType) {
        Validate.notNull(javaType,
                "Java type required for metadata trigger deregistration");
        this.metadataTriggers.remove(javaType);
    }

    protected boolean isIgnoreTriggerAnnotations() {
        return ignoreTriggerAnnotations;
    }

    protected void setIgnoreTriggerAnnotations(boolean ignoreTriggerAnnotations) {
        this.ignoreTriggerAnnotations = ignoreTriggerAnnotations;
    }

    /*
     * (non-Javadoc)
     *
     * TODO Overwritted method required because in old Roo versions can't use
     * builder to include AJs precedence. Used a template instead.
     *
     * @see
     * org.springframework.roo.metadata.MetadataProvider#get(java.lang.String)
     */
    public final MetadataItem get(String metadataIdentificationString) {
        Validate.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        metadataIdentificationString).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected request for '" + metadataIdentificationString
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        // Remove the upstream dependencies for this instance (we'll be
        // recreating them later, if needed)
        getMetadataDependencyRegistry().deregisterDependencies(
                metadataIdentificationString);

        // Compute the identifier for the Physical Type Metadata we're
        // correlated with
        String governorPhysicalTypeIdentifier = getGovernorPhysicalTypeIdentifier(metadataIdentificationString);

        // Obtain the physical type
        PhysicalTypeMetadata governorPhysicalTypeMetadata = (PhysicalTypeMetadata) getMetadataService()
                .get(governorPhysicalTypeIdentifier);

        if (governorPhysicalTypeMetadata == null
                || !governorPhysicalTypeMetadata.isValid()) {
            // We can't get even basic information about the physical type, so
            // abort (the ITD will be deleted by ItdFileDeletionService)
            return null;
        }

        // Determine ITD details
        String itdFilename = governorPhysicalTypeMetadata
                .getItdCanoncialPath(this);
        JavaType aspectName = governorPhysicalTypeMetadata.getItdJavaType(this);

        // Flag to indicate whether we'll even try to create this metadata
        boolean produceMetadata = false;

        // Determine if we should generate the metadata on the basis of it
        // containing a trigger annotation
        ClassOrInterfaceTypeDetails cid = null;

        if (governorPhysicalTypeMetadata.getMemberHoldingTypeDetails() != null
                && governorPhysicalTypeMetadata.getMemberHoldingTypeDetails() instanceof ClassOrInterfaceTypeDetails) {
            cid = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                    .getMemberHoldingTypeDetails();

            // Only create metadata if the type is annotated with one of the
            // metadata triggers
            for (JavaType trigger : metadataTriggers) {
                if (MemberFindingUtils.getDeclaredTypeAnnotation(cid, trigger) != null) {
                    produceMetadata = true;
                    break;
                }
            }
        }

        // Fallback to ignoring trigger annotations
        if (ignoreTriggerAnnotations) {
            produceMetadata = true;
        }

        // Cancel production if the governor type details are required, but
        // aren't available
        if (governorTypeDetail && cid == null) {
            produceMetadata = false;
        }

        // Cancel production if the governor is not a class, and the subclass
        // only wants to know about classes
        if (cid != null && governorBeingAClass
                && cid.getPhysicalTypeCategory() != PhysicalTypeCategory.CLASS) {
            produceMetadata = false;
        }

        if (produceMetadata) {
            // This type contains an annotation we were configured to detect, or
            // there is an ITD (which may need deletion), so we need to produce
            // the metadata
            OCCChecksumMetadata metadata;
            metadata = getMetadata(metadataIdentificationString, aspectName,
                    governorPhysicalTypeMetadata, itdFilename);

            // Register a direct connection between the physical type and this
            // metadata
            // (this is needed so changes to the inheritance hierarchies are
            // eventually notified to us)
            getMetadataDependencyRegistry().registerDependency(
                    governorPhysicalTypeMetadata.getId(),
                    metadataIdentificationString);

            // Quit if the subclass returned null; it might not have experienced
            // issues parsing etc
            if (metadata == null) {
                return null;
            }

            // Handle the management of the ITD file
            if (metadata.getItdFileContents() != null) {
                String itd = metadata.getItdFileContents();

                // Output the ITD if there is actual content involved
                // (if there is no content, we continue on to the deletion phase
                // at the bottom of this conditional block)
                if (itd.length() > 0) {

                    MutableFile mutableFile = null;
                    if (getFileManager().exists(itdFilename)) {
                        // First verify if the file has even changed
                        File newFile = new File(itdFilename);
                        String existing = null;
                        try {
                            existing = IOUtils
                                    .toString(new FileReader(newFile));
                        }
                        catch (IOException ignoreAndJustOverwriteIt) {
                            LOGGER.finest("Problem writting ".concat(
                                    newFile.getAbsolutePath()).concat(": ")
                                    + ignoreAndJustOverwriteIt);
                        }

                        if (!itd.equals(existing)) {
                            mutableFile = getFileManager().updateFile(
                                    itdFilename);
                        }

                    }
                    else {
                        mutableFile = getFileManager().createFile(itdFilename);
                        Validate.notNull(mutableFile,
                                "Could not create ITD file '" + itdFilename
                                        + "'");
                    }

                    try {
                        if (mutableFile != null) {
                            InputStream inputStream = null;
                            OutputStream outputStream = null;
                            try {
                                inputStream = IOUtils.toInputStream(itd);
                                outputStream = mutableFile.getOutputStream();
                                IOUtils.copy(inputStream, outputStream);
                            }
                            finally {
                                IOUtils.closeQuietly(inputStream);
                                IOUtils.closeQuietly(outputStream);
                            }
                        }
                    }
                    catch (IOException ioe) {
                        throw new IllegalStateException("Could not output '"
                                .concat(mutableFile.getCanonicalPath()).concat(
                                        "'"), ioe);
                    }

                    // Important to exit here, so we don't proceed onto the
                    // delete operation below
                    // (as we have a valid ITD that has been written out by now)
                    return metadata;
                }

            }

            // Delete the ITD if we determine deletion is appropriate
            // DiSiD: Removed because in shell restart some AJ files deleted
            // and not recreated
            // if (metadata.isValid() && getFileManager().exists(itdFilename)) {
            // getFileManager().delete(itdFilename);
            // }

            return metadata;
        }

        return null;
    }

    public final String getIdForPhysicalJavaType(
            String physicalJavaTypeIdentifier) {
        Validate.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        physicalJavaTypeIdentifier).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(PhysicalTypeIdentifier
                                        .getMetadataIdentiferType())),
                "Expected a valid physical Java type instance identifier (not '"
                        + physicalJavaTypeIdentifier + "')");
        JavaType javaType = PhysicalTypeIdentifier
                .getJavaType(physicalJavaTypeIdentifier);
        LogicalPath path = PhysicalTypeIdentifier
                .getPath(physicalJavaTypeIdentifier);
        return createLocalIdentifier(javaType, path);
    }

    /**
     * If set to true (default is true), ensures subclass not called unless the
     * governor type details are available.
     * 
     * @param governorTypeDetail true means governor type details must be
     *        available
     */
    public void setDependsOnGovernorTypeDetailAvailability(
            boolean governorTypeDetail) {
        this.governorTypeDetail = governorTypeDetail;
    }

    /**
     * If set to true (default is true), ensures the governor type details
     * represent a class. Note that
     * {@link #setDependsOnGovernorTypeDetailAvailability(boolean)} must also be
     * true to ensure this can be relied upon.
     * 
     * @param governorBeingAClass true means governor type detail must represent
     *        a class
     */
    public void setDependsOnGovernorBeingAClass(boolean governorBeingAClass) {
        this.governorBeingAClass = governorBeingAClass;
    }

    public MetadataDependencyRegistry getMetadataDependencyRegistry() {
        if (metadataDependencyRegistry == null) {
            // Get all Services implement MetadataDependencyRegistry interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataDependencyRegistry.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataDependencyRegistry) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataDependencyRegistry on OCCChecksumMetadataProvider.");
                return null;
            }
        }
        else {
            return metadataDependencyRegistry;
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
                LOGGER.warning("Cannot load MetadataService on OCCChecksumMetadataProvider.");
                return null;
            }
        }
        else {
            return metadataService;
        }
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
                LOGGER.warning("Cannot load FileManager on OCCChecksumMetadataProvider.");
                return null;
            }
        }
        else {
            return fileManager;
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
                LOGGER.warning("Cannot load MemberDetailsScanner on OCCChecksumMetadataProvider.");
                return null;
            }
        }
        else {
            return memberDetailsScanner;
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
                LOGGER.warning("Cannot load PersistenceMemberLocator on OCCChecksumMetadataProvider.");
                return null;
            }
        }
        else {
            return persistenceMemberLocator;
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
                LOGGER.warning("Cannot load TypeManagementService on OCCChecksumMetadataProvider.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
    }

}
