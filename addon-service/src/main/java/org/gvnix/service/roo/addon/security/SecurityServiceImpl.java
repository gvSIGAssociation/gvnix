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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
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
    private static final String AXIS_CLIENT_CONFIG_TEMPLATE_FILE = "client-config-axis-template.xml";

    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private AnnotationsService annotationsService;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This performs this operations:
     * </p>
     * <ul>
     * <li>Install Apache WSSJ4 depenency in pom</li>
     * <li>Creates axis <code>client-config.wsdd</code> file</li>
     * </ul>
     * 
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
                Path.SRC_MAIN_RESOURCES, "client-config.wsdd");
    }

    /**
     * Creates <code>client-config.wssd</code> file in application resources
     * folder
     */
    private void createAxisClientConfigFile() {
        String axisClientConfigPath = getAxisClientConfigPath();

        if (fileManager.exists(axisClientConfigPath)) {
            return;
        }

        // Gets the template
        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
                AXIS_CLIENT_CONFIG_TEMPLATE_FILE);

        // Create target file
        MutableFile cxfXmlMutableFile = fileManager
                .createFile(axisClientConfigPath);

        try {
            FileCopyUtils.copy(templateInputStream,
                    cxfXmlMutableFile.getOutputStream());
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }

        fileManager.scan();
    }

    /**
     * Adds Apache wss4j dependency to application pom.
     */
    protected void addDependencies() {

        annotationsService.addGvNIXAnnotationsDependency();
        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
                DEPENDENCIES_FILE);
        Assert.notNull(templateInputStream,
                "Can't adquire dependencies file ".concat(DEPENDENCIES_FILE));

        Document dependencyDoc;
        try {

            dependencyDoc = XmlUtils.getDocumentBuilder().parse(
                    templateInputStream);
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }

        Element dependencies = (Element) dependencyDoc.getFirstChild();

        List<Element> dependecyElementList = XmlUtils.findElements(
                "/dependencies/dependency", dependencies);
        List<Dependency> dependencyList = new ArrayList<Dependency>();

        for (Element element : dependecyElementList) {
            dependencyList.add(new Dependency(element));
        }
        projectOperations.addDependencies(dependencyList);
    }

    /**
     * {@inheritDoc}
     * <p>
     * First it tries to parse WSLD from the given URL. If
     * <code>SSLHandshakeException</code> is catch because our JVM installation
     * is not confident with the host server certificate where WSDL is available
     * the method tries to get the certificates in the certificates chain of the
     * host server and import them to:
     * <ol>
     * <li>a custom keystore in <code>SRC_MAIN_RESOURCES/gvnix-cacerts</code></li>
     * <li>the JVM cacerts keystore in
     * <code>$JAVA_HOME/jre/lib/security/cacerts</code>. Here we can have a
     * problem if JVM <code>cacerts</code> file is not writable by the user due
     * to file permissions. In this case We throw an exception informing about
     * the error.</li>
     * </ol>
     * With that operation we can try again to get the WSDL.<br/>
     * 
     * Also it exports the chain certificates to <code>.cer</code> files in
     * <code>SRC_MAIN_RESOURCES</code>, so the developer can distribute them for
     * its installation in other environments or just in case we reach the
     * problem with the JVM <code>cacerts</code> file permissions.
     * 
     * @see GvNix509TrustManager#saveCertFile(String, X509Certificate,
     *      FileManager, PathResolver)
     * @see <a href=
     *      "http://download.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html"
     *      >Java SE keytool</a>.
     *      </p>
     */
    public Document parseWsdlFromUrl(String urlStr, String keyStorePassphrase)
            throws Exception {

        try {
            // Parse the wsdl location to a DOM document
            Document wsdl = XmlUtils.getDocumentBuilder().parse(urlStr);
            Assert.notNull(wsdl, "No valid document format");

            return wsdl;

        } catch (SSLHandshakeException e) {

            // Create a SSL context
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());

            // passphrase of the keystore
            char[] passphrase = (StringUtils.hasText(keyStorePassphrase) ? keyStorePassphrase
                    .toCharArray() : "changeit".toCharArray());

            File keystore = createCacertsBasedOnJvmCacerts();
            Assert.isTrue(keystore.isFile(), "JVM cacerts file does not exist");

            tmf.init(GvNix509TrustManager.loadKeyStore(keystore, passphrase));

            X509TrustManager defaultTrustManager = (X509TrustManager) tmf
                    .getTrustManagers()[0];
            GvNix509TrustManager tm = new GvNix509TrustManager(
                    defaultTrustManager);
            context.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory factory = context.getSocketFactory();

            URL url = new URL(urlStr);

            String host = url.getHost();
            int port = url.getPort();
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            socket.setSoTimeout(10000);
            Document parsedDoc = null;
            InputStream uriStream = null;
            try {
                socket.startHandshake();
                URLConnection connection = url.openConnection();
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection)
                            .setSSLSocketFactory(factory);
                }

                uriStream = connection.getInputStream();
                parsedDoc = XmlUtils.getDocumentBuilder().parse(uriStream);

                socket.close();
            } catch (SSLException e1) {
                String alias;
                // Import new needed certificates to our custom keystore
                X509Certificate[] neededCertificates = tm.addCerts(host,
                        keystore, passphrase);
                if (neededCertificates != null) {
                    for (int i = 0; i < neededCertificates.length; i++) {
                        alias = host.concat("-" + (i + 1));
                        GvNix509TrustManager.saveCertFile(alias,
                                neededCertificates[i], fileManager,
                                projectOperations.getPathResolver());

                    }
                    // Import needed certificates to JVM cacerts keystore
                    File jvmCacerts = getJVMCacertsKeystore();
                    tm.addCerts(host, jvmCacerts, passphrase);
                    /*
                     * TODO: code to replace directly JVM cacerts by our custom
                     * keystore
                     * 
                     * if (GvNix509TrustManager.replaceJVMCacerts(keystore,
                     * jvmCacerts, fileManager)) {
                     * logger.info("JVM cacert has been replaced " +
                     * jvmCacerts.getAbsolutePath()); } else {
                     * logger.info("JVM cacerts can not been replaced. "
                     * .concat(
                     * "You should to import needed certificates in your ")
                     * .concat("JVM trustcacerts keystore. ")
                     * .concat("You have them in src/main/resources/*.cer")
                     * .concat("Use keytool for that: ") .concat(
                     * "http://download.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html"
                     * )); }
                     */
                }
                parsedDoc = parseWsdlFromUrl(urlStr, keyStorePassphrase);
            }
            Assert.notNull(parsedDoc, "No valid document format");
            return parsedDoc;
        } catch (SAXException e) {

            throw new IllegalStateException("The format of the wsdl has errors");

        } catch (IOException e) {

            throw new IllegalStateException("There is no access to the wsdl");
        }
    }

    /** {@inheritDoc} */
    public Document loadWsdlUrl(String url) {
        try {
            // read the WSDL with the support of the Security System
            // passphrase is null because we only work with default password
            // 'changeit'
            return parseWsdlFromUrl(url, null);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error parsing WSDL from ".concat(url), e);
        }
    }

    /**
     * Copy, if exists, the cacerts keystore of the JVM in a new keystore file
     * gvnix-cacerts
     * 
     * @return
     */
    private File createCacertsBasedOnJvmCacerts() {
        File jvmCacerts = getJVMCacertsKeystore();

        String cerFilePath = projectOperations.getPathResolver().getIdentifier(
                Path.SRC_MAIN_RESOURCES, "gvnix-cacerts");

        if (jvmCacerts.isFile()) {
            if (!fileManager.exists(cerFilePath)) {
                try {
                    FileCopyUtils.copy(new FileInputStream(jvmCacerts),
                            fileManager.createFile(cerFilePath)
                                    .getOutputStream());
                    fileManager.commit();
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Error creatin a copy of ".concat(jvmCacerts
                                    .getAbsolutePath()));
                }
            }
        }

        return new File(cerFilePath);
    }

    private File getJVMCacertsKeystore() {
        String SEP = File.separator;
        // TODO: intentar cargar el keystore usando el JVM API
        // System.getProperty("javax.net.ssl.trustStore")
        return new File(System.getProperty("java.home").concat(SEP)
                .concat("lib").concat(SEP).concat("security").concat(SEP)
                .concat("cacerts"));
    }

    /** {@inheritDoc} */
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
        } else {
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
     * 
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
     * @param doc
     *            client-config.wsdd document
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
