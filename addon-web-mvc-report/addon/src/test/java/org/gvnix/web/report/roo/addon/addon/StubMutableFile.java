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

package org.gvnix.web.report.roo.addon.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.process.manager.MutableFile;

/**
 * Provides an easy way to create a {@link MutableFile} without actually
 * creating a file on the file system. Records all write operations in an
 * OutputStream and makes it available via {@link #getOutputAsString()}.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * 
 * @author Rossen Stoyanchev
 * @since 1.1.1
 */
public class StubMutableFile implements MutableFile {

    private final File file;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public StubMutableFile() {
        this.file = null;
    }

    public StubMutableFile(File file) {
        Validate.notNull(file, "File required");
        this.file = file;
    }

    public String getCanonicalPath() {
        try {
            return file.getCanonicalPath();
        }
        catch (IOException ioe) {
            throw new IllegalStateException(
                    "Cannot determine canoncial path for '" + file + "'", ioe);
        }
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        }
        catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable to acquire input stream for file '"
                            + getCanonicalPath() + "'", ioe);
        }
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public void setDescriptionOfChange(String message) {
    }

    public String getOutputAsString() {
        return output.toString();
    }

}
