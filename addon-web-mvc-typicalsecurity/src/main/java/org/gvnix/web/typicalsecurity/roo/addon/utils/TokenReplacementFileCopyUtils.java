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
package org.gvnix.web.typicalsecurity.roo.addon.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * @author rohit
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public class TokenReplacementFileCopyUtils {

    private static final int BUFFER_SIZE = 8192;

    public static int replaceAndCopy(InputStream in, OutputStream out,
            Properties replacement) throws IOException {
        assert in != null : "No InputStream specified";
        assert out != null : "No OutputStream specified";
        StringBuffer sb = new StringBuffer();
        try {
            int byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, bytesRead));
                byteCount += bytesRead;
            }
            String txt = sb.toString();
            for (Entry<Object, Object> entry : replacement.entrySet()) {
                txt = txt.replaceAll(entry.getKey().toString(), entry
                        .getValue().toString());
            }
            out.write(txt.getBytes());
            out.flush();
            return byteCount;
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
