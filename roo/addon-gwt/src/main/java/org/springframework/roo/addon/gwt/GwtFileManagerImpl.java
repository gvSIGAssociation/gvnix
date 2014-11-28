package org.springframework.roo.addon.gwt;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeParsingService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.process.manager.FileManager;

/**
 * Implementation of {@link GwtFileManager}.
 * 
 * @author James Tyrrell
 * @since 1.1.1
 */
@Component
@Service
public class GwtFileManagerImpl implements GwtFileManager {

    private static final String ROO_EDIT_WARNING = "// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.\n\n";

    @Reference protected FileManager fileManager;
    @Reference protected TypeLocationService typeLocationService;
    @Reference protected TypeParsingService typeParsingService;

    @Override
    public String write(final ClassOrInterfaceTypeDetails typeDetails,
            boolean includeWarning) {
        final String destFile = typeLocationService
                .getPhysicalTypeCanonicalPath(typeDetails
                        .getDeclaredByMetadataId());
        includeWarning &= !destFile.endsWith(".xml");
        includeWarning |= destFile.endsWith("_Roo_Gwt.java");
        String fileContents = typeParsingService
                .getCompilationUnitContents(typeDetails);
        if (includeWarning) {
            fileContents = ROO_EDIT_WARNING + fileContents;
        }
        else if (fileManager.exists(destFile)) {
            return fileContents;
        }
        write(destFile, fileContents, includeWarning);
        return fileContents;
    }

    @Override
    public String write(final ClassOrInterfaceTypeDetails typeDetails,
            final String warning) {
        final String destFile = typeLocationService
                .getPhysicalTypeCanonicalPath(typeDetails
                        .getDeclaredByMetadataId());
        final String fileContents = typeParsingService
                .getCompilationUnitContents(typeDetails);
        fileManager.createOrUpdateTextFileIfRequired(destFile, warning
                + fileContents, true);
        return fileContents;
    }

    @Override
    public void write(final List<ClassOrInterfaceTypeDetails> typeDetails,
            final boolean includeWarning) {
        for (final ClassOrInterfaceTypeDetails typeDetail : typeDetails) {
            write(typeDetail, includeWarning);
        }
    }

    @Override
    public void write(final String destFile, final String newContents) {
        write(destFile, newContents, true);
    }

    private void write(final String destFile, final String newContents,
            final boolean overwrite) {
        // Write to disk, or update a file if it is already present and
        // overwriting is allowed
        if (!fileManager.exists(destFile) || overwrite) {
            fileManager.createOrUpdateTextFileIfRequired(destFile, newContents,
                    true);
        }
    }

    @Override
    public void delete(final ClassOrInterfaceTypeDetails typeDetails) {
        final String file = typeLocationService
                .getPhysicalTypeCanonicalPath(typeDetails
                        .getDeclaredByMetadataId());

        delete(file);
    }

    @Override
    public void delete(final String file) {
        // Write to disk, or update a file if it is already present and
        // overwriting is allowed

        if (fileManager.exists(file)) {
            fileManager.delete(file);
        }
    }

    @Override
    public boolean fileExists(ClassOrInterfaceTypeDetails typeDetails) {
        final String file = typeLocationService
                .getPhysicalTypeCanonicalPath(typeDetails
                        .getDeclaredByMetadataId());
        return fileExists(file);
    }

    @Override
    public boolean fileExists(String file) {
        // TODO Auto-generated method stub
        return fileManager.exists(file);
    }
}