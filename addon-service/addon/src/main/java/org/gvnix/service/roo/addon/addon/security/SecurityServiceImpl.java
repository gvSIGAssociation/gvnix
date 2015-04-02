/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
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
package org.gvnix.service.roo.addon.addon.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.Transformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.addon.AnnotationsService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link SecurityService}
 * 
 * @author Jose Manuel Vivó Arnal ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class SecurityServiceImpl implements SecurityService {

    private static final String DEPENDENCIES_FILE = "dependencies-wssl4.xml";
    private static final String AXIS_CL_CONF_TEMPL = "client-config-axis-template.xml";

    private static Logger LOGGER = Logger.getLogger(SecurityServiceImpl.class
            .getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private AnnotationsService annotationsService;

    /**
     * {@inheritDoc}
     * <p>
     * This performs this operations:
     * </p>
     * <ul>
     * <li>Install Apache WSSJ4 depenency in pom</li>
     * <li>Creates axis <code>client-config.wsdd</code> file</li>
     * </ul>
     **/
    public void setupWSSJ4() {
        addDependencies();
        createAxisClientConfigFile();
    }

    /**
     * @return Returns the path to axis client wsdd config file
     */
    private String getAxisClientConfigPath() {
        return projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                "client-config.wsdd");
    }

    /**
     * Creates client-config.wssd file in application resources folder.
     */
    private void createAxisClientConfigFile() {
        String axisClientConfigPath = getAxisClientConfigPath();

        if (fileManager.exists(axisClientConfigPath)) {
            return;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(),
                    AXIS_CL_CONF_TEMPL);
            outputStream = fileManager.createFile(axisClientConfigPath)
                    .getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        }
        catch (Exception e) {

            throw new IllegalStateException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        fileManager.scan();
    }

    /**
     * Adds Apache wss4j dependency to application pom.
     */
    protected void addDependencies() {

        annotationsService.addAddonDependency();
        InputStream templateInputStream = FileUtils.getInputStream(getClass(),
                DEPENDENCIES_FILE);
        Validate.notNull(templateInputStream,
                "Can't adquire dependencies file ".concat(DEPENDENCIES_FILE));

        Document dependencyDoc;
        try {

            dependencyDoc = XmlUtils.getDocumentBuilder().parse(
                    templateInputStream);
        }
        catch (Exception e) {

            throw new IllegalStateException(e);
        }

        Element dependencies = (Element) dependencyDoc.getFirstChild();

        List<Element> dependecyElementList = XmlUtils.findElements(
                "/dependencies/dependency", dependencies);
        List<Dependency> dependencyList = new ArrayList<Dependency>();

        for (Element element : dependecyElementList) {
            dependencyList.add(new Dependency(element));
        }
        projectOperations.addDependencies(
                projectOperations.getFocusedModuleName(), dependencyList);
    }

    /**
     * Parse a WSDL location and if it's needed it manage keystore certs.
     * <p>
     * First it tries to parse WSDL from the given URL. If
     * <code>SSLHandshakeException</code> is catch because our JVM installation
     * is not confident with the host server certificate where WSDL is available
     * the method tries install required certificates with
     * {@link SecurityServiceImpl#installCertificates(String, String)} and retry
     * to parse WSDL from URL.
     * </p>
     * 
     * @param loc Location
     * @param pass Password
     * @return WSDL document
     * @throws Exception
     */
    protected Document getWsdl(String loc, String pass) throws Exception {

        try {

            // Parse the wsdl location to a DOM document
            Document wsdl = XmlUtils.getDocumentBuilder().parse(loc);
            Validate.notNull(wsdl, "No valid document format");

            return wsdl;

        }
        catch (SSLHandshakeException e) {

            // JVM is not confident with WSDL host server certificate
            return installCertificates(loc, pass);

        }
        catch (SAXException e) {
            LOGGER.log(Level.SEVERE,
                    "The format of the wsdl has errors: ".concat(loc), e);
            throw new IllegalStateException(
                    "The format of the wsdl has errors: ".concat(loc));

        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "The format of the wsdl has errors: ".concat(loc), e);
            throw new IllegalStateException(
                    "There is not access to the wsdl: ".concat(loc));
        }
    }

    /**
     * Get certificates in the chain of the host server and import them.
     * <p>
     * Tries to get the certificates in the certificates chain of the host
     * server and import them to:
     * <ol>
     * <li>A custom keystore in <code>SRC_MAIN_RESOURCES/gvnix-cacerts</code></li>
     * <li>The JVM cacerts keystore in
     * <code>$JAVA_HOME/jre/lib/security/cacerts</code>. Here we can have a
     * problem if JVM <code>cacerts</code> file is not writable by the user due
     * to file permissions. In this case we throw an exception informing about
     * the error.</li>
     * </ol>
     * </p>
     * <p>
     * With that operation we can try again to get the WSDL.<br/>
     * Also it exports the chain certificates to <code>.cer</code> files in
     * <code>SRC_MAIN_RESOURCES</code>, so the developer can distribute them for
     * its installation in other environments or just in case we reach the
     * problem with the JVM <code>cacerts</code> file permissions.
     * </p>
     * 
     * @see GvNix509TrustManager#saveCertFile(String, X509Certificate,
     *      FileManager, PathResolver)
     * @see <a href=
     *      "http://download.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html"
     *      >Java SE keytool</a>.
     */
    protected Document installCertificates(String loc, String pass)
            throws NoSuchAlgorithmException, KeyStoreException, Exception,
            KeyManagementException, MalformedURLException, IOException,
            UnknownHostException, SocketException, SAXException {

        // Create a SSL context
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Passphrase of the keystore: "changeit" by default
        char[] passArray = (StringUtils.isNotBlank(pass) ? pass.toCharArray()
                : "changeit".toCharArray());

        // Get the project keystore and copy it from JVM if not exists
        File keystore = getProjectKeystore();

        tmf.init(GvNix509TrustManager.loadKeyStore(keystore, passArray));

        X509TrustManager defaultTrustManager = (X509TrustManager) tmf
                .getTrustManagers()[0];
        GvNix509TrustManager tm = new GvNix509TrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();

        // Open URL location (default 443 port if not defined)
        URL url = new URL(loc);
        String host = url.getHost();
        int port = url.getPort() == -1 ? 443 : url.getPort();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);

        Document doc = null;
        try {

            socket.startHandshake();
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(factory);
            }

            doc = XmlUtils.getDocumentBuilder().parse(
                    connection.getInputStream());

            socket.close();

        }
        catch (SSLException ssle) {

            // Get needed certificates for this host
            getCerts(tm, host, keystore, passArray);
            doc = getWsdl(loc, pass);

        }
        catch (IOException ioe) {

            invalidHostCert(passArray, keystore, tm, host);
        }

        Validate.notNull(doc, "No valid document format");
        return doc;
    }

    /**
     * Throw an illegal state exception with a invalid host cert message.
     * 
     * @param pass Password
     * @param keystore Keystore
     * @param host Host destination
     */
    protected void invalidHostCert(char[] pass, File keystore,
            GvNix509TrustManager tm, String host) {

        StringBuffer msg = new StringBuffer("There is not access to the WSDL.");
        X509Certificate[] certs = getCerts(tm, host, keystore, pass);
        if (certs != null) {

            msg.append(" Maybe the emited certificate does not match the hostname where WSDL resides.\n");
            for (X509Certificate x509Certificate : certs) {

                // X.500 distinguished name
                String dn = x509Certificate.getSubjectX500Principal().getName();

                // X.500 common name from distinguished name
                String cn = getCn(dn);
                if (cn != null) {
                    msg.append(" * Possible hostname: ".concat(cn).concat("\n"));
                }
                else
                    msg.append(" * Possible hostname (check Cert. Distinguished name): "
                            .concat(dn).concat("\n"));
            }
        }

        throw new IllegalStateException(msg.toString());
    }

    /**
     * Given a X.500 Subject Distinguished name it returns the Common Name.
     * <p>
     * If CN exists, null otherwise
     * </p>
     * 
     * @param dn Distinguished name
     * @return Common name if exists, null otherwise
     */
    private String getCn(String dn) {

        int i = dn.indexOf("CN=");
        if (i == -1) {
            return null;
        }

        // get the remaining DN without CN=
        String cn = dn.substring(i);
        i = cn.indexOf(",");
        if (i == -1) {
            return cn.substring(3);
        }

        return cn.substring(3, i);
    }

    /**
     * Stores in keystore needed certificates.
     * 
     * @param tm
     * @param host
     * @param keystore
     * @param pass
     * @return
     */
    private X509Certificate[] getCerts(GvNix509TrustManager tm, String host,
            File keystore, char[] pass) {

        X509Certificate[] certs = null;

        try {

            certs = tm.addCerts(host, keystore, pass);
            if (certs != null) {

                for (int i = 0; i < certs.length; i++) {

                    GvNix509TrustManager.saveCertFile(
                            host.concat("-" + (i + 1)), certs[i], fileManager,
                            projectOperations.getPathResolver());
                }

                // Import needed certificates to JVM cacerts keystore
                tm.addCerts(host, getJvmKeystore(), pass);
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error loading or saving X509 certificates in keystore", e);
            throw new IllegalStateException(
                    "Error loading or saving X509 certificates in keystore");
        }

        return certs;
    }

    /**
     * {@inheritDoc}
     */
    public Document getWsdl(String url) {

        try {

            // Read WSDL with the support of the Security System
            return getWsdl(url, null);

        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Error parsing WSDL from ".concat(url), e);
        }
    }

    /**
     * Copy, if exists, the JVM keystore file in the project resources folder.
     * <p>
     * Destination resources file name is gvnix-cacerts.
     * </p>
     * 
     * @return File created or existing file
     */
    private File getProjectKeystore() {

        // Get path to file at resources
        String path = projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                "gvnix-cacerts");

        // Get JVM keystore file and validate it
        File keystore = getJvmKeystore();
        Validate.isTrue(keystore.isFile(), "JVM cacerts file does not exist");

        // When JVM keystore is valid file and already not copied to project
        if (!fileManager.exists(path)) {

            // Write JVM keystore into resources file path
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(keystore);
                outputStream = fileManager.createFile(path).getOutputStream();
                IOUtils.copy(inputStream, outputStream);
                fileManager.commit();
            }
            catch (IOException e) {
                throw new IllegalStateException(
                        "Error creatin a copy of ".concat(keystore
                                .getAbsolutePath()));
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        // Return existing or created project resources keystore
        return new File(path);
    }

    /**
     * Get JVM keytore file.
     * <p>
     * "java.home" system property is required.
     * </p>
     * 
     * @return JVM keystore file
     */
    private File getJvmKeystore() {

        return new File(System.getProperty("java.home").concat(File.separator)
                .concat("lib").concat(File.separator).concat("security")
                .concat(File.separator).concat("cacerts"));
    }

    /**
     * {@inheritDoc}
     */
    public void addOrUpdateAxisClientService(String serviceName,
            Map<String, String> parameters) throws SAXException, IOException {

        createAxisClientConfigFile();
        String axisClientPath = getAxisClientConfigPath();
        Document document = XmlUtils.getDocumentBuilder().parse(
                new File(axisClientPath));

        Element deployment = (Element) document.getFirstChild();

        Element serviceElementDescriptor = getAxisClientService(document,
                serviceName, parameters);

        List<Element> previousServices = XmlUtils.findElements(
                "/deployment/service[@name='".concat(serviceName).concat("']"),
                deployment);

        if (previousServices.isEmpty()) {
            deployment.appendChild(serviceElementDescriptor);
        }
        else {
            deployment.replaceChild(serviceElementDescriptor,
                    previousServices.get(0));
        }

        OutputStream outputStream = new ByteArrayOutputStream();

        Transformer transformer = XmlUtils.createIndentingTransformer();

        XmlUtils.writeXml(transformer, outputStream, document);

        fileManager.createOrUpdateTextFileIfRequired(axisClientPath,
                outputStream.toString(), false);

    }

    /**
     * <p>
     * Create a xml element for a axis security service cliente configuration
     * </p>
     * <p>
     * Result element will be like this:
     * </p>
     * 
     * <pre>
     * &lt;service name="{serviceName}"&gt;
     *      &lt;requestFlow&gt;
     *          &lt;handler type="java:org.apache.ws.axis.security.WSDoAllSender"&gt;
     *             &lt;!-- For every entry in parameters --&gt;
     *             &lt;parameter name="{paramKey}" value="{paramValue}"/&gt;
     *             &lt;!-- End for every entry --&gt;
     *          &lt;/handler&gt;
     *      &lt;/requestFlow&gt;
     * &lt;/service&gt;
     * </pre>
     * 
     * @param doc client-config.wsdd document
     * @param serviceName
     * @param parameters
     * @return
     */
    private Element getAxisClientService(Document doc, String serviceName,
            Map<String, String> parameters) {

        // <service name="Service_Name">
        Element service = doc.createElement("service");
        // FIXME set service name
        service.setAttribute("name", serviceName);

        // <requestFlow>
        Element requestFlow = doc.createElement("requestFlow");

        // <handler type="java:org.apache.ws.axis.security.WSDoAllSender">
        Element handler = doc.createElement("handler");
        handler.setAttribute("type",
                "java:org.apache.ws.axis.security.WSDoAllSender");

        Element parameter;

        Set<Entry<String, String>> entrySet = parameters.entrySet();
        for (Entry<String, String> entry : entrySet) {
            // <parameter name="{paramKey}" value="{paramValue}"/>
            parameter = doc.createElement("parameter");
            parameter.setAttribute("name", entry.getKey());
            parameter.setAttribute("value", entry.getValue());
            handler.appendChild(parameter);
        }

        // </handler>
        requestFlow.appendChild(handler);

        // </requestFlow>
        service.appendChild(requestFlow);

        // </service>
        return service;
    }
}
