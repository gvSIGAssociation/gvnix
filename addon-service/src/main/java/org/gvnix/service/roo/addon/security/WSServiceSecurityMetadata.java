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
package org.gvnix.service.roo.addon.security;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceSecurity;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * <p>
 * Web Service Security Metadata
 * </p>
 * <p>
 * Manage {@link GvNIXWebServiceSecurity} annotation
 * </p>
 * <p>
 * Prepares Metadata for configure Web service Proxy configuration. Currently
 * only can generate WS client configuration and only for <code>Signature</code>
 * action and <code>pck12</code> certificated.
 * </p>
 * <p>
 * This generates:
 * <ul>
 * <li>a ITD with PasswordHandler implementation (internal ITD builder)</li>
 * <li> <code>properties</code> for configuration file (
 * {@link #getSecurityProperties()})</li>
 * <li>a <code>service</code> parameters for <code>client-config.wsdd</code>
 * file ( {@link #getServiceWsddConfigurationParameters()})
 * </ul>
 * </p>
 * <p>
 * TODO extend it to support more wssl4 security actions
 * </p>
 * 
 * @author Jose Manuel Vivó Arnal ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WSServiceSecurityMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {
    private static final JavaSymbolName CALBACK_PARAM_NAME = new JavaSymbolName(
            "callbacks");
    private static final JavaType CALLBACKS_PARAM_TYPE = new JavaType(
            "javax.security.auth.callback.Callback", 1, DataType.TYPE, null,
            null);
    private static final JavaType UNSUPPORTED_CALLBACK_TYPE = new JavaType(
            "javax.security.auth.callback.UnsupportedCallbackException");
    private static final JavaType IO_EXCEPTION_TYPE = new JavaType(
            "java.io.IOException");
    private static final String PROVIDES_TYPE_STRING = WSServiceSecurityMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName HANDLE_METHOD_NAME = new JavaSymbolName(
            "handle");

    private final String certificate;

    /**
     * Path (relative to classpath ) to Certificate
     */
    private final String certificatePath;

    /**
     * Path (relative to classpath) to Properties file
     */
    private final String propertiesPath;

    private final String serviceName;

    private final JavaType serviceClass;

    public WSServiceSecurityMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String serviceName, String certificate) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Process values from the annotation, if present (XXX ???)
        AnnotationMetadata annotation = governorTypeDetails
                .getAnnotation(new JavaType(GvNIXWebServiceSecurity.class
                        .getName()));

        // XXX annotation null? (???)
        if (annotation != null) {
            AutoPopulationUtils.populate(this, annotation);
        }

        // ServiceClass
        this.serviceClass = governorTypeDetails.getName();

        // ServiceName
        this.serviceName = serviceName;

        // Certificate
        this.certificate = certificate;

        // CertificatePath
        this.certificatePath = getCertificatePath(serviceClass, certificate);

        // Compute path to properties
        propertiesPath = getPropertiesPath(governorTypeDetails.getName());

        // Adding CallBackHander implementation
        builder.addImplementsType(new JavaType(CallbackHandler.class
                .getCanonicalName()));

        // Adding default constructor
        builder.addConstructor(getDefaultConstructor());

        // Adding CallBackHandler method
        MethodMetadata callBackHandler = getCallBackHandlerMethod();
        if (callBackHandler != null) {
            builder.addMethod(callBackHandler);
        }

        // Create output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Returns default class constructor (with no parameters)
     * 
     * @return
     */
    private ConstructorMetadata getDefaultConstructor() {
        // Checks if default constructor is already defined
        for (ConstructorMetadata constructor : governorTypeDetails
                .getDeclaredConstructors()) {
            if (constructor.getParameterTypes() == null
                    || constructor.getParameterTypes().isEmpty()) {
                return constructor;
            }

        }

        // Create the constructor
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("super();");

        ConstructorMetadataBuilder constructorBuilder = new ConstructorMetadataBuilder(
                getId());
        constructorBuilder.setModifier(Modifier.PUBLIC);
        constructorBuilder.setBodyBuilder(bodyBuilder);
        return constructorBuilder.build();

    }

    /**
     * Return method for handle password
     * 
     * @return
     */
    private MethodMetadata getCallBackHandlerMethod() {

        // Prepare method parameter definition
        List<JavaType> parameterTypes = new ArrayList<JavaType>();
        parameterTypes.add(CALLBACKS_PARAM_TYPE);

        List<AnnotatedJavaType> parameters = AnnotatedJavaType
                .convertFromJavaTypes(parameterTypes);

        // Check if a method with the same signature already exists in the
        // target type
        if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                HANDLE_METHOD_NAME, parameterTypes) != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return null;
        }

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(IO_EXCEPTION_TYPE);
        throwsTypes.add(UNSUPPORTED_CALLBACK_TYPE);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CALBACK_PARAM_NAME);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("final String propPath = \"".concat(
                propertiesPath).concat("\";"));
        bodyBuilder
                .appendFormalLine("final String propKey = \"org.apache.ws.security.crypto.merlin.keystore.password\";");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder.append("// Get class loader to get file from project");
        bodyBuilder.newLine();
        bodyBuilder
                .appendFormalLine("ClassLoader classLoader = Thread.currentThread().getContextClassLoader();");

        bodyBuilder
                .appendFormalLine("java.io.File file = new java.io.File(classLoader.getResource(propPath).toURI());");
        bodyBuilder.appendFormalLine("if (file != null && file.exists()) {");
        bodyBuilder.indent();
        bodyBuilder.append("// Load properties");
        bodyBuilder.newLine();
        bodyBuilder
                .appendFormalLine("java.util.Properties properties = new java.util.Properties();");
        bodyBuilder.appendFormalLine("java.io.FileInputStream ins = null;");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("ins = new java.io.FileInputStream(file);");
        bodyBuilder.appendFormalLine("properties.load(ins);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} finally {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (ins != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("ins.close();");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}"); // End if
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}"); // End try (ins)
        bodyBuilder
                .appendFormalLine("String value = properties.getProperty(propKey);");
        bodyBuilder.appendFormalLine("if (value != null) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("((org.apache.ws.security.WSPasswordCallback) callbacks[0]).setPassword(value);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {"); // Else value != null
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("throw new IOException(\"Property \".concat(propKey).concat(\" not exists\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}"); // Endif value != null
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {"); // Else file.exists()
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("throw new IOException(\"File \".concat(propPath).concat(\" not exists\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}"); // Endif file.exists()
        bodyBuilder.indentRemove();
        bodyBuilder
                .appendFormalLine("} catch (java.net.URISyntaxException e) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("throw new IOException(\"Problem getting \".concat(propPath).concat(\" file\"),e);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}"); // End try

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, HANDLE_METHOD_NAME,
                JavaType.VOID_PRIMITIVE, parameters, parameterNames,
                bodyBuilder);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * @return Map with parameters for client-config.wsdd
     */
    public static Map<String, String> getServiceWsddConfigurationParameters(
            JavaType serviceClass, String alias, String propertiesPath) {

        Map<String, String> parameters = new HashMap<String, String>();

        // <parameter name="action" value="Signature"/>
        parameters.put("action", "Signature");

        // <parameter name="user" value="alias"/>
        parameters.put("user", alias);

        // <parameter name="passwordCallbackClass" value="governor"/>
        parameters.put("passwordCallbackClass",
                serviceClass.getFullyQualifiedTypeName());

        // <parameter name="signaturePropFile" value="path_To_Properties"/>
        parameters.put("signaturePropFile", propertiesPath);

        // <parameter name="signatureKeyIdentifier" value="DirectReference" />
        parameters.put("signatureKeyIdentifier", "DirectReference");

        return parameters;
    }

    /**
     * @return service name (identifier)
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return service class
     */
    public JavaType getServiceClass() {
        return serviceClass;
    }

    /**
     * @return certificate keystore file
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * @return properties file path (relative to classpath)
     */
    public String getPropertiesPath() {
        return propertiesPath;
    }

    /**
     * @return certificate keystore file path (relative to classpath)
     */
    public String getCertificatePath() {
        return certificatePath;
    }

    /**
     * Compute certificate keystore file path (relative to classpath) for a
     * serviceClass and certificateFileName
     * 
     * @param serviceClass
     * @param certificateFileName
     * @return
     */
    public static String getCertificatePath(JavaType serviceClass,
            String certificateFileName) {
        String path = serviceClass.getFullyQualifiedTypeName();
        path = path.replace('.', '/');
        path = path.substring(0, path.lastIndexOf('/') + 1);
        return path.concat(certificateFileName);
    }

    /**
     * Compute property file path (relative to classpath) for a serviceClass
     * 
     * @param serviceClass
     * @return
     */
    public static String getPropertiesPath(JavaType serviceClass) {
        String classpath = serviceClass.getFullyQualifiedTypeName();
        String path = classpath.replace('.', '/');
        return path.concat("Security.properties");
    }

    // Typically, no changes are required beyond this point

    @Override
    public String toString() {
        ToStringBuilder tsc = new ToStringBuilder(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("aspectName", aspectName);
        tsc.append("destinationType", destination);
        tsc.append("governor", governorPhysicalTypeMetadata.getId());
        tsc.append("itdTypeDetails", itdTypeDetails);
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

}
