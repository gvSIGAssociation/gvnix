/**
 * 
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
 */
public class TokenReplacementFileCopyUtils {

    private static int BUFFER_SIZE = 8192;

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
