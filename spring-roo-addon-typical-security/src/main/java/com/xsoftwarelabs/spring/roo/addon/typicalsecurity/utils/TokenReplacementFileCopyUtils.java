/**
 * 
 */
package com.xsoftwarelabs.spring.roo.addon.typicalsecurity.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;

/**
 * @author rohit 
 *
 */
public class TokenReplacementFileCopyUtils extends FileCopyUtils {
	public static int replaceAndCopy(InputStream in, OutputStream out, Properties replacement) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");
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
                        txt = txt.replaceAll(entry.getKey().toString(), entry.getValue().toString());
                }
                out.write(txt.getBytes());
                out.flush();
                return byteCount;
        } finally {
                try {
                        in.close();
                } catch (IOException ex) {
                }
                try {
                        out.close();
                } catch (IOException ex) {
                }
        }
}
}
