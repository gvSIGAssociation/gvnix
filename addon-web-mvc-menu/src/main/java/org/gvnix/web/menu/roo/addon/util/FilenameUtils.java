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
package org.gvnix.web.menu.roo.addon.util;

/**
 * Miscellaneous file name utility methods. Mainly for internal use within the
 * framework; based on Jakarta's Commons IO.
 * 
 * @author Enrique Ruiz( eruiz at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 * @see Jakarta's Commons IO
 */
public abstract class FilenameUtils {

    /**
     * The extension separator character.
     * 
     * @since Commons IO 1.4
     */
    public static final char EXTENSION_SEPARATOR = '.';

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Gets the name minus the path from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format. The text
     * after the last forward or backslash is returned.
     * 
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is
     * running on.
     * 
     * @param filename the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none
     *         exists
     */
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    /**
     * Gets the base name, minus the full path and extension, from a full
     * filename.
     * <p>
     * This method will handle a file in either Unix or Windows format. The text
     * after the last forward or backslash and before the last dot is returned.
     * 
     * <pre>
     * a/b/c.txt --> c
     * a.txt     --> a
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     * 
     * @param filename the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none
     *         exists
     */
    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    /**
     * Detect the last "." (if present) and extract from there the extension.
     * <p>
     * This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot.
     * 
     * <pre>
     * foo.txt      --> "txt"
     * a/b/c.jpg    --> "jpg"
     * a/b.txt/c    --> ""
     * a/b/c        --> ""
     * </pre>
     * 
     * @param filePath
     * @return File extension or empty String if no file extension exists.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        }
        else {
            return filename.substring(index + 1);
        }
    }

    /**
     * Removes the extension from a filename.
     * <p>
     * This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.
     * 
     * <pre>
     * foo.txt    --> foo
     * a\b\c.jpg  --> a\b\c
     * a\b\c      --> a\b\c
     * a.b\c      --> a.b\c
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is
     * running on.
     * 
     * @param filename the filename to query, null returns null
     * @return the filename minus the extension
     */
    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        }
        else {
            return filename.substring(0, index);
        }
    }

    /**
     * Returns the index of the last directory separator character.
     * <p>
     * This method will handle a file in either Unix or Windows format. The
     * position of the last forward or backslash is returned.
     * <p>
     * The output will be the same irrespective of the machine that the code is
     * running on.
     * 
     * @param filename the filename to find the last path separator in, null
     *        returns -1
     * @return the index of the last separator character, or -1 if there is no
     *         such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Returns the index of the last extension separator character, which is a
     * dot.
     * <p>
     * This method also checks that there is no directory separator after the
     * last dot. To do this it uses {@link #indexOfLastSeparator(String)} which
     * will handle a file in either Unix or Windows format.
     * <p>
     * The output will be the same irrespective of the machine that the code is
     * running on.
     * 
     * @param filename the filename to find the last path separator in, null
     *        returns -1
     * @return the index of the last separator character, or -1 if there is no
     *         such character
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }
}
