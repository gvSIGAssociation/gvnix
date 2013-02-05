/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
package org.gvnix.dynamic.configuration.roo.addon.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Abstract dynamic configuration component of property files list.
 * <p>
 * Extends this class to manage new properties file list values.
 * </p>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(componentAbstract = true)
public abstract class PropertiesListDynamicConfiguration extends
        PropertiesDynamicConfiguration {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(PropertiesListDynamicConfiguration.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilePath() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DynPropertyList read() {

        DynPropertyList dynProps = new DynPropertyList();
        try {

            // Get the property files to read from resources
            String resources = pathResolver.getIdentifier(
            		LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""), "");
            List<FileDetails> files = getFiles(resources);
            for (FileDetails file : files) {

                String path = file.getCanonicalPath();
                MutableFile mutableFile = fileManager.updateFile(path);

                // If managed file not exists, nothing to do
                if (mutableFile != null) {

                    Properties props = new Properties();
                    props.load(mutableFile.getInputStream());
                    for (Entry<Object, Object> prop : props.entrySet()) {

                        dynProps.add(new DynProperty(setKeyValue(prop.getKey()
                                .toString(), path), prop.getValue().toString()));
                    }
                }

            }

        } catch (IOException ioe) {

            throw new IllegalStateException(ioe);
        }

        return dynProps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DynPropertyList dynProps) {

        OutputStream outputStream = null;

        // Get the property files to write from resources
        String resources = pathResolver.getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""), "");
        List<FileDetails> files = getFiles(resources);
        for (FileDetails f : files) {

            try {

                // Get properties from the file
                MutableFile file = fileManager.updateFile(f.getCanonicalPath());
                Properties props = new Properties();
                props.load(file.getInputStream());
                for (DynProperty dynProp : dynProps) {

                    // If property belongs to file and exists on file, update it
                    String key = getKeyValue(dynProp.getKey());
                    if (isPropertyRelatedToFile(dynProp, file)) {
                        if (props.containsKey(key)) {

                            props.put(key, dynProp.getValue());

                        } else {

                            LOGGER.log(
                                    Level.WARNING,
                                    "Property key "
                                            .concat(dynProp.getKey())
                                            .concat(" to put value not exists on file"));
                        }
                    }
                }
                outputStream = file.getOutputStream();
                props.store(outputStream, null);

            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            } finally {
            	IOUtils.closeQuietly(outputStream);
            }
        }
    }

    /**
     * Is a dynamic property related to the file ?
     * 
     * @param dynProp
     *            Dynamic property
     * @param file
     *            Mutable file
     * @return Is property related to file ?
     */
    protected boolean isPropertyRelatedToFile(DynProperty dynProp,
            MutableFile file) {

        if (getFileIndentifierFromKey(dynProp.getKey()).equals(
                getFileIdentifierFromPath(file.getCanonicalPath()))) {
            return true;
        }

        return false;
    }

    /**
     * Get included files on a path.
     * 
     * @param source
     *            Initial path to search files
     * @return Files list
     */
    protected List<FileDetails> getFiles(String source) {

        List<FileDetails> result = new ArrayList<FileDetails>();
        try {

            // Find all paths from the source path
            SortedSet<FileDetails> paths = fileManager
                    .findMatchingAntPath(source + "/*");
            for (FileDetails path : paths) {

                // This path name is a file to include ?
                String pathName = path.getCanonicalPath();
                if (include(pathName)) {

                    // File to include !
                    result.add(path);

                } else {

                    // Iterate child paths (only for folders)
                    result.addAll(getFiles(pathName));
                }
            }
        } catch (IllegalArgumentException iae) {
            // Path is a a file: nothing to do
        	LOGGER.finest("Path element file");
        }

        return result;
    }

    /**
     * There is a file to include on dynamic configurations ?
     * 
     * @param path
     *            File path to check
     * @return Include on dynamic configuration ?
     */
    protected boolean include(String path) {

        return getFileName(path).startsWith(getFilePrefix())
                && path.endsWith(getFileSufix());
    }

    /**
     * Set key value to set on a dynamic property considering file name.
     * 
     * @param key
     *            Original key value
     * @param path
     *            File path
     * @return New key value
     */
    protected String setKeyValue(String key, String path) {

        return getPropertyTargetPrefix()
                .concat(getFileIdentifierFromPath(path)).concat(
                        key.substring(getPropertySourcePrefix().length() - 1,
                                key.length()));
    }

    /**
     * Get the file identifier from the property key
     * 
     * @param path
     *            File path
     * @return File identifier
     */
    protected String getFileIndentifierFromKey(String key) {

        String sufix = key.substring(getPropertyTargetPrefix().length(),
                key.length());
        return sufix.substring(0, sufix.indexOf("."));
    }

    /**
     * Get key value from a dynamic property.
     * 
     * @param key
     *            Original key value
     * @return New key value
     */
    protected String getKeyValue(String key) {

        return getPropertySourcePrefix().concat(
                key.substring(getPropertyTargetPrefix().length()
                        + getFileIndentifierFromKey(key).length() + 1));
    }

    /**
     * Get the file identifier from the file path.
     * 
     * @param path
     *            File path
     * @return File identifier
     */
    protected String getFileIdentifierFromPath(String path) {

        String fileName = getFileName(path);
        return fileName.substring(fileName.indexOf(getFilePrefix()),
                fileName.lastIndexOf(getFileSufix()));
    }

    /**
     * Get the file name part from a path.
     * 
     * @param path
     *            File path
     * @return File name part
     */
    protected String getFileName(String path) {

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public abstract String getFileSufix();

    public abstract String getFilePrefix();

    public abstract String getPropertyTargetPrefix();

    public abstract String getPropertySourcePrefix();

}
