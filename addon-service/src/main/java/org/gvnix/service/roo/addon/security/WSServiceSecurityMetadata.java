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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;

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
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;

/**
 * <p>
 * Web Service Security Metadata
 * </p>
 * 
 * <p>
 * Manage {@link GvNIXWebServiceSecurity} annotation
 * </p>
 * 
 * <p>
 * Prepares Metadata for configure Web service Proxy configuration. Currently
 * only can generate WS client configuration and only for <code>Signature</code>
 * action and <code>pck12</code> certificated.
 * </p>
 * 
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
 * 
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
    private static final String PROVIDES_TYPE_STRING = WSServiceSecurityMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    // From annotation
    @AutoPopulate
    private String password;

    @AutoPopulate
    private String alias;

    @AutoPopulate
    private String certificate;

    /**
     * Path (relative to classpath ) to Certificate
     */
    private final String certificatePath;

    /**
     * Path (relative to classpath) to Properties file
     */
    private final String propertiesPath;

    private final String serviceName;

    public WSServiceSecurityMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String serviceName) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Process values from the annotation, if present (XXX ???)
        AnnotationMetadata annotation = MemberFindingUtils
                .getDeclaredTypeAnnotation(governorTypeDetails, new JavaType(
                        GvNIXWebServiceSecurity.class.getName()));

        // XXX annotation null? (???)
        if (annotation != null) {
            AutoPopulationUtils.populate(this, annotation);
        }

        // ServiceName
        this.serviceName = serviceName;

        // Compute path to certificate
        certificatePath = getCertificatePath(governorTypeDetails.getName(),
                certificate);

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
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("handle");

        // Prepare method parameter definition
        List<JavaType> parameterTypes = new ArrayList<JavaType>();
        parameterTypes.add(new JavaType(
                "javax.security.auth.callback.Callback", 1, DataType.TYPE,
                null, null));

        List<AnnotatedJavaType> parameters = AnnotatedJavaType
                .convertFromJavaTypes(parameterTypes);

        // Check if a method with the same signature already exists in the
        // target type
        if (MemberFindingUtils.getDeclaredMethod(governorTypeDetails, methodName, parameterTypes) != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return null;
        }

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(new JavaType("java.io.IOException"));
        throwsTypes.add(new JavaType(
                "javax.security.auth.callback.UnsupportedCallbackException"));

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("callbacks"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder
                .appendFormalLine("((org.apache.ws.security.WSPasswordCallback) callbacks[0]).setPassword(\""
                        .concat(password).concat("\");"));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE,
                parameters, parameterNames, bodyBuilder);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Returns properties for service configuration
     * 
     * @return
     */
    public Properties getSecurityProperties() {
        Properties properties = new Properties();

        // security provider
        properties.put("org.apache.ws.security.crypto.provider",
                "org.apache.ws.security.components.crypto.Merlin");

        // certificate Keystore type
        properties.put("org.apache.ws.security.crypto.merlin.keystore.type",
                getKeystoreTypeString());

        // certificate keystore password
        properties.put(
                "org.apache.ws.security.crypto.merlin.keystore.password",
                password);

        // alias password
        properties.put("org.apache.ws.security.crypto.merlin.alias.password",
                password);

        // certificate keystore alias
        properties.put("org.apache.ws.security.crypto.merlin.keystore.alias",
                alias);

        // certificate keystore file
        properties.put("org.apache.ws.security.crypto.merlin.file",
                certificatePath);

        return properties;
    }

    /**
     * @return Map with parameters for client-config.wsdd
     */
    public Map<String, String> getServiceWsddConfigurationParameters() {

        Map<String, String> parameters = new HashMap<String, String>();

        // <parameter name="action" value="Signature"/>
        parameters.put("action", getActionString());

        // <parameter name="user" value="alias"/>
        parameters.put("user", alias);

        // <parameter name="passwordCallbackClass" value="governor"/>
        parameters.put("passwordCallbackClass", governorTypeDetails.getName()
                .getFullyQualifiedTypeName());

        // <parameter name="signaturePropFile" value="path_To_Properties"/>
        parameters.put("signaturePropFile", propertiesPath);

        // <parameter name="signatureKeyIdentifier" value="DirectReference" />
        parameters.put("signatureKeyIdentifier", "DirectReference");

        return parameters;
    }

    /**
     * <p>
     * Return security action string
     * </p>
     * 
     * <p>
     * currently a constant (<code>Signature</code>)
     * </p>
     * 
     * <p>
     * TODO parameterize it
     * </p>
     * 
     * @return
     */
    private String getActionString() {
        return "Signature";
    }

    /**
     * <p>
     * Returns keystore type string identifier
     * </p>
     * 
     * <p>
     * currently constant (<code>pkcs12</code>)
     * </p>
     * 
     * <p>
     * TODO identify it from values
     * </p>
     * 
     * @return
     */
    private String getKeystoreTypeString() {
        return "pkcs12";
    }

    /**
     * @return service name (identifier)
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return password for certificate keystore
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return alias to use form certificate keystore
     */
    public String getAlias() {
        return alias;
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
        ToStringCreator tsc = new ToStringCreator(this);
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

    public static final String createIdentifier(JavaType javaType, Path path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

}
