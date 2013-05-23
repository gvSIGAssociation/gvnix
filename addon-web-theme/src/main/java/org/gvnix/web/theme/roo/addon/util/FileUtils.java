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
package org.gvnix.web.theme.roo.addon.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities for handling {@link File} instances.
 * 
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
public abstract class FileUtils {

    /**
     * Finds files within a given directory and its subdirectories.
     * 
     * @param directory the directory to search in
     * @return the set of files found
     */
    public static Set<URL> findFilesURL(File directory) {

        // Set of File URLs
        Set<URL> urls = new HashSet<URL>();

        File[] found = directory.listFiles();
        if (found != null) {
            for (int i = 0; i < found.length; i++) {
                // recursive call if found is a directory
                if (found[i].isDirectory()) {
                    Set<URL> children = findFilesURL(found[i]);
                    urls.addAll(children);
                }
                // if found is file add to Set
                else {
                    try {
                        urls.add(new URL(found[i].getAbsolutePath()));
                    }
                    catch (MalformedURLException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return urls;
    }

    /**
     * Finds files within a given directory and its subdirectories.
     * 
     * @param directory the directory to search in
     * @return the set of files found
     */
    public static Set<URI> findFilesURI(File directory) {

        // Set of File URLs
        Set<URI> uris = new HashSet<URI>();

        File[] found = directory.listFiles();
        if (found != null) {
            for (int i = 0; i < found.length; i++) {
                // recursive call if found is a directory
                if (found[i].isDirectory()) {
                    Set<URI> children = findFilesURI(found[i]);
                    uris.addAll(children);
                }
                // if found is file add to Set
                else {
                    uris.add(found[i].toURI());
                }
            }
        }
        return uris;
    }
}
