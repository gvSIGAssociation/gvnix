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
package org.gvnix.service.roo.addon.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.X509TrustManager;

import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;

/**
 * Implementation of X509TrustManager in order to deal with SSL connection
 * against secure servers that uses certificates not included in the JVM
 * keystore (Usually in $JAVA_HOME/jre/lib/security/cacerts)
 * 
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
public class GvNix509TrustManager implements X509TrustManager {

    private static Logger logger = Logger.getLogger(GvNix509TrustManager.class
            .getName());

    private final X509TrustManager tm;
    private X509Certificate[] chain;

    GvNix509TrustManager(X509TrustManager tm) {
        this.tm = tm;
    }

    public X509Certificate[] getAcceptedIssuers() {
        throw new UnsupportedOperationException();
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        this.chain = chain;
        tm.checkServerTrusted(chain, authType);
    }

    /**
     * Import certs in this.chain to the keystore given by the file passed.
     * 
     * @param host
     * @param keystore
     * @param pass
     * @return
     * @throws Exception
     */
    public X509Certificate[] addCerts(String host, File keystore, char[] pass)
            throws Exception {

        // Specific Exceptions thrown in this code: NoSuchAlgorithmException,
        // KeyStoreException, CertificateException, IOException

        X509Certificate[] chain = this.chain;
        if (chain == null) {
            return null;
        }

        KeyStore ks = loadKeyStore(keystore, pass);

        String alias = host;
        for (int i = 0; i < chain.length; i++) {

            X509Certificate cert = chain[i];
            alias = alias.concat("-" + (i + 1));
            ks.setCertificateEntry(alias, cert);
            alias = host;
        }

        if (keystore.canWrite()) {

            OutputStream out = new FileOutputStream(keystore);
            ks.store(out, pass);
            out.close();

        } else {

            throw new Exception(
                    keystore.getAbsolutePath()
                            .concat(" is not writable. ")
                            .concat("You should to import needed certificates in your")
                            .concat(" JVM trustcacerts keystore.\n")
                            .concat("You have them in src/main/resources/*.cer.\n")
                            .concat("Use keytool for that: ")
                            .concat("http://download.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html"));
        }

        return chain;
    }

    /**
     * Export the given certificate to a file in SRC_MAIN_RESOURCES. The cert
     * file will have given <code>{alias}.cer</code> as file name.
     * <p>
     * <b>We don't use Roo FileManager API</b> here in order to create cert
     * files because in this way if we have any problem importing them to the
     * JVM <code>cacerts</cacerts> Roo won't undo the cert files creation.
     * </p>
     * 
     * @param alias
     * @param cert
     * @param fileManager
     * @param pathResolver
     * @throws Exception
     */
    public static void saveCertFile(String alias, X509Certificate cert,
            FileManager fileManager, PathResolver pathResolver)
            throws Exception {

        String aliasCerFileName = alias.concat(".cer");
        String cerFilePath = pathResolver.getIdentifier(
                Path.SRC_MAIN_RESOURCES, aliasCerFileName);

        if (!fileManager.exists(cerFilePath)) {

            File cerFile = new File(cerFilePath);
            OutputStream os = new FileOutputStream(cerFile);
            os.write(cert.getEncoded());
            os.close();
            logger.info("Created ".concat(Path.SRC_MAIN_RESOURCES.getName())
                    .concat("/").concat(aliasCerFileName));
        }
    }

    /**
     * Loads keystore in the given file using passphrase as keystore password.
     * 
     * @param keystore
     * @param pass
     * @return
     * @throws Exception
     *             will be a IOExecption if the given password is a wrong one
     */
    public static KeyStore loadKeyStore(File keystore, char[] pass)
            throws Exception {

        Assert.notNull(keystore, "keystore must be a vaild keystore file");
        InputStream in = new FileInputStream(keystore);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, pass);
        in.close();

        return ks;
    }

}
