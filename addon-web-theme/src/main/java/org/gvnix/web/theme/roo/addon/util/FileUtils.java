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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
     * TODO Duplicated code When finding bundle use URL and when finding local
     * files use URI Avoid error "unknown protocol: bundle" on commands
     * 
     * @param directory the directory to search in
     * @return the set of files found
     */
    public static List<URL> findFilesURL(File directory) {

        // Set of File URLs
        List<URL> urls = new ArrayList<URL>();

        File[] found = directory.listFiles();
        if (found != null) {
            for (int i = 0; i < found.length; i++) {
                // recursive call if found is a directory
                if (found[i].isDirectory()) {
                    List<URL> children = findFilesURL(found[i]);
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
        return removeDuplicates(urls);
    }

    public static List<URL> removeDuplicates(List<URL> urls) {
        List<URL> filtered = new ArrayList<URL>();
        boolean found = false;
        for (URL url : urls) {
            found = false;
            for (URL toCheck : filtered) {
                if (toCheck.getPath().equals(url.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                filtered.add(url);
            }
        }
        return filtered;
    }

    /**
     * Finds files within a given directory and its subdirectories.
     * 
     * TODO Duplicated code When finding bundle use URL and when finding local
     * files use URI Avoid error "unknown protocol: bundle" on commands
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
