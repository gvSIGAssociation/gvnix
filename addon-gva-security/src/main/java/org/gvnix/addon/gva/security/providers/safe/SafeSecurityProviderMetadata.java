/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.gva.security.providers.safe;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXPasswordHandlerSAFE} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class SafeSecurityProviderMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName SAFE_PROPERTIES_METHOD = new JavaSymbolName(
            "getSafeProperties");
    private static final JavaSymbolName APPLICATION_ROL_METHOD = new JavaSymbolName(
            "convertToApplicationRol");
    private static final JavaSymbolName CONVERT_WS_METHOD = new JavaSymbolName(
            "convertWSInfoToUser");
    private static final JavaSymbolName CONVERT_WS_TODAS_METHOD = new JavaSymbolName(
            "convertWSInfoToUserTodasAplicaciones");
    private static final JavaSymbolName SECURITY_METHOD = new JavaSymbolName(
            "setSecurity");
    private static final JavaSymbolName RETRIEVE_USER_METHOD = new JavaSymbolName(
            "retrieveUser");
    private static final JavaSymbolName ADDITIONAL_CHECKS_METHOD = new JavaSymbolName(
            "additionalAuthenticationChecks");
    private static final JavaType JAVA_TYPE_STRING = new JavaType(
            "java.lang.String");
    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;
    private JavaPackage governorsPackage;

    public SafeSecurityProviderMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        /*Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");*/

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.governorsPackage = governorPhysicalTypeMetadata.getType()
                .getPackage();
        // Imports
        helper.getFinalTypeName(new JavaType(
                "org.springframework.security.authentication.encoding.PlaintextPasswordEncoder"));

        // Adding Fields
        builder.addField(getField("logger",
                "Logger.getLogger(SafeProvider.class.getName())", new JavaType(
                        "org.apache.log4j.Logger"), Modifier.PRIVATE
                        + Modifier.STATIC));

        builder.addField(getField("PROPERTIES_PATH",
                "\"safe_client_sign.properties\"", JAVA_TYPE_STRING,
                Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL));

        builder.addField(getField(
                "passwordEncoder",
                "new PlaintextPasswordEncoder()",
                new JavaType(
                        "org.springframework.security.authentication.encoding.PasswordEncoder"),
                Modifier.PRIVATE));

        builder.addField(getField("saltSource", null, new JavaType(
                "org.springframework.security.authentication.dao.SaltSource"),
                Modifier.PRIVATE));

        builder.addField(getField("endpoint", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        builder.addField(getField("endpointAutoriza", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        builder.addField(getField("applicationId", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        builder.addField(getField("environment", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        builder.addField(getField("prop", null, new JavaType(
                "java.util.Properties"), Modifier.PUBLIC));

        builder.addField(getField("loader", null, new JavaType(
                "java.lang.ClassLoader"), Modifier.PUBLIC));

        builder.addField(getField("stream", null, new JavaType(
                "java.io.InputStream"), Modifier.PUBLIC));

        builder.addField(getField("mapRoles", "true",
                JavaType.BOOLEAN_PRIMITIVE, Modifier.PRIVATE));

        builder.addField(getField("active", null, JavaType.BOOLEAN_PRIMITIVE,
                Modifier.PRIVATE));

        builder.addField(getField("filtrarPorAplicacion", null,
                JavaType.BOOLEAN_PRIMITIVE, Modifier.PRIVATE));

        // Creating getters and setters
        builder.addMethod(getGetterMethod("saltSource", new JavaType(
                "org.springframework.security.authentication.dao.SaltSource")));
        builder.addMethod(getSetterMethod("saltSource", new JavaType(
                "org.springframework.security.authentication.dao.SaltSource")));
        builder.addMethod(getGetterMethod(
                "passwordEncoder",
                new JavaType(
                        "org.springframework.security.authentication.encoding.PasswordEncoder")));
        builder.addMethod(getSetterMethod(
                "passwordEncoder",
                new JavaType(
                        "org.springframework.security.authentication.encoding.PasswordEncoder")));
        builder.addMethod(getGetterMethod("endpoint", JAVA_TYPE_STRING));
        builder.addMethod(getSetterMethod("endpoint", JAVA_TYPE_STRING));
        builder.addMethod(getGetterMethod("endpointAutoriza", JAVA_TYPE_STRING));
        builder.addMethod(getSetterMethod("endpointAutoriza", JAVA_TYPE_STRING));
        builder.addMethod(getGetterMethod("applicationId", JAVA_TYPE_STRING));
        builder.addMethod(getSetterMethod("applicationId", JAVA_TYPE_STRING));
        builder.addMethod(getGetterMethod("environment", JAVA_TYPE_STRING));
        builder.addMethod(getSetterMethod("environment", JAVA_TYPE_STRING));
        builder.addMethod(getGetterMethod("mapRoles",
                JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getSetterMethod("mapRoles",
                JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getGetterMethod("active", JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getSetterMethod("active", JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getGetterMethod("filtrarPorAplicacion",
                JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getSetterMethod("filtrarPorAplicacion",
                JavaType.BOOLEAN_PRIMITIVE));

        // Creating methods
        builder.addMethod(getAdditionalAuthenticationChecksMethod());
        builder.addMethod(getRetrieveUserMethod());
        builder.addMethod(getSetSecurityMethod());
        builder.addMethod(getConvertWSInfoToUserMethod());
        builder.addMethod(getConvertWSInfoToUserTodasAplicacionesMethod());
        builder.addMethod(getConvertToApplicationRolMethod());
        builder.addMethod(getGetSafePropertiesMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>additionalAuthenticationChecks</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getAdditionalAuthenticationChecksMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        new JavaType(
                                "org.springframework.security.core.userdetails.UserDetails"),
                        new JavaType(
                                "org.springframework.security.authentication.UsernamePasswordAuthenticationToken"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(ADDITIONAL_CHECKS_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(new JavaType(
                "org.springframework.security.core.AuthenticationException"));

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userDetails"));
        parameterNames.add(new JavaSymbolName("authentication"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildAdditionalAuthenticationChecksMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, ADDITIONAL_CHECKS_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>retrieveUser</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getRetrieveUserMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        JavaType.STRING,
                        new JavaType(
                                "org.springframework.security.authentication.UsernamePasswordAuthenticationToken"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(RETRIEVE_USER_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("username"));
        parameterNames.add(new JavaSymbolName("authentication"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildRetrieveUserMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(),
                Modifier.PUBLIC,
                RETRIEVE_USER_METHOD,
                new JavaType(
                        "org.springframework.security.core.userdetails.UserDetails"),
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>setSecurity</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getSetSecurityMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(new JavaType(
                        "org.apache.cxf.endpoint.Client"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SECURITY_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("client"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetSecurityMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, SECURITY_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>convertWSInfoToUser</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getConvertWSInfoToUserMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse"),
                        JavaType.STRING,
                        new JavaType(
                                "java.util.List",
                                0,
                                DataType.TYPE,
                                null,
                                Arrays.asList(new JavaType(
                                        "es.gva.dgm.ayf.war.definitions.v2u00.Permisoapp"))));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(CONVERT_WS_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userFromWS"));
        parameterNames.add(new JavaSymbolName("username"));
        parameterNames.add(new JavaSymbolName("listPermisos"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildConvertWSInfoToUserMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, CONVERT_WS_METHOD, new JavaType(
                        "SafeUser"), parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>convertWSInfoToUserTodasAplicaciones</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getConvertWSInfoToUserTodasAplicacionesMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse"),
                        JavaType.STRING,
                        new JavaType(
                                "java.util.List",
                                0,
                                DataType.TYPE,
                                null,
                                Arrays.asList(new JavaType(
                                        "es.gva.dgm.ayf.war.definitions.v2u00.Permiso"))));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(CONVERT_WS_TODAS_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userFromWS"));
        parameterNames.add(new JavaSymbolName("username"));
        parameterNames.add(new JavaSymbolName("listPermisos"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildConvertWSInfoToUserTodasAplicacionesMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, CONVERT_WS_TODAS_METHOD,
                new JavaType("SafeUser"), parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>additionalAuthenticationChecks</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getConvertToApplicationRolMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(JAVA_TYPE_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(APPLICATION_ROL_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("idgrupo"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildConvertToApplicationRolMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, APPLICATION_ROL_METHOD,
                JavaType.STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>getSafeProperties</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getGetSafePropertiesMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SAFE_PROPERTIES_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetSafePropertiesMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC,
                SAFE_PROPERTIES_METHOD, new JavaType("java.util.Properties"),
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>buildAdditionalAuthenticationChecks</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildAdditionalAuthenticationChecksMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Object salt = null;
        bodyBuilder.appendFormalLine(String.format("%s salt = null;",
                helper.getFinalTypeName(new JavaType("java.lang.Object"))));
        bodyBuilder.appendFormalLine("if (this.saltSource != null) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("salt = this.saltSource.getSalt(userDetails);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("if (authentication.getCredentials() == null) {");
        bodyBuilder.indent();
        // throw new BadCredentialsException("Bad credentials:
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(\"Bad credentials: \"",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.BadCredentialsException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ userDetails.getUsername());");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("String presentedPassword = authentication.getCredentials().toString();");
        bodyBuilder
                .appendFormalLine("if (!passwordEncoder.isPasswordValid(userDetails.getPassword(),presentedPassword, salt)) {");
        bodyBuilder.indent();
        // throw new BadCredentialsException("Bad credentials:
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(\"Bad credentials: \"",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.BadCredentialsException"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("+ userDetails.getUsername() + \" password check\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

    }

    /**
     * Builds body method for <code>retrieveUser</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildRetrieveUserMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("if(getActive()){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("String presentedPassword = authentication.getCredentials().toString();");

        // AutenticaUsuarioLDAPWSRequest aut = new
        // AutenticaUsuarioLDAPWSRequest();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s aut = new %s();",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSRequest")),
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSRequest"))));
        bodyBuilder.appendFormalLine("aut.setUsuarioLDAP(username);");
        bodyBuilder.appendFormalLine("aut.setPwdLDAP(presentedPassword);");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();

        // AutenticacionArangiService autService = new
        // AutenticacionArangiService(
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autService = new %s(",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiService")),
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiService"))));
        bodyBuilder.indent();

        // new URL(getEndpoint()));
        bodyBuilder.appendFormalLine(String.format("new %s(getEndpoint()));",
                helper.getFinalTypeName(new JavaType("java.net.URL"))));
        bodyBuilder.indentRemove();

        // AutenticacionArangiPortType port = autService
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s port = autService",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiPortType"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(".getAutenticacionArangiPortTypeSoap11();");
        bodyBuilder.indentRemove();

        // Client client = ClientProxy.getClient(port);
        bodyBuilder.appendFormalLine(String.format(
                "%s client = %s.getClient(port);", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.cxf.endpoint.Client")), helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.cxf.frontend.ClientProxy"))));
        bodyBuilder.appendFormalLine("setSecurity(client);");

        // AutenticaUsuarioLDAPWSResponse response1 = port
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s response1 = port",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSResponse"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(".autenticaUsuarioLDAPWS(aut);");
        bodyBuilder.indentRemove();

        // GetInformacionWSRequest getInformacionWSRequest = new
        // GetInformacionWSRequest();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s getInformacionWSRequest = new %s();",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSRequest")),
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSRequest"))));
        bodyBuilder
                .appendFormalLine("getInformacionWSRequest.setToken(response1.getToken());");

        // GetInformacionWSResponse getInformacionWSResponse = port
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s getInformacionWSResponse = port",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(".getInformacionWS(getInformacionWSRequest);");
        bodyBuilder.indentRemove();

        bodyBuilder
                .appendFormalLine("// Checking if is necessary filter by applicationId");
        bodyBuilder.appendFormalLine("");

        // RetornaAutorizacionWSRequest retornaUsuApliAut = null;
        // RetornaTodasAutorizacionesDNIWSRequest retornaTodasAut = null;
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s retornaUsuApliAut = null;",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaAutorizacionWSRequest"))));
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s retornaTodasAut = null;",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaTodasAutorizacionesDNIWSRequest"))));

        // if(getFiltrarPorAplicacion()){
        bodyBuilder.appendFormalLine("if(getFiltrarPorAplicacion()){");
        bodyBuilder.indent();

        // retornaUsuApliAut = new RetornaAutorizacionWSRequest();
        // String applicationId = getApplicationId();
        // retornaUsuApliAut.setIdAplicacion(applicationId);
        // retornaUsuApliAut.setUsuarioHDFI(getInformacionWSResponse.getIdHDFI())
        bodyBuilder
                .appendFormalLine("retornaUsuApliAut = new RetornaAutorizacionWSRequest();");
        bodyBuilder
                .appendFormalLine("String applicationId = getApplicationId();");
        bodyBuilder
                .appendFormalLine("retornaUsuApliAut.setIdAplicacion(applicationId);");
        bodyBuilder
                .appendFormalLine("retornaUsuApliAut.setUsuarioHDFI(getInformacionWSResponse.getIdHDFI());");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();

        // retornaTodasAut = new RetornaTodasAutorizacionesDNIWSRequest();
        // retornaTodasAut.setTipoBusqueda("ambas");
        // retornaTodasAut.setUsuarioDNI(getInformacionWSResponse.getNif());

        bodyBuilder
                .appendFormalLine("retornaTodasAut = new RetornaTodasAutorizacionesDNIWSRequest();");
        bodyBuilder
                .appendFormalLine("retornaTodasAut.setTipoBusqueda(\"ambas\");");
        bodyBuilder
                .appendFormalLine("retornaTodasAut.setUsuarioDNI(getInformacionWSResponse.getNif());");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // AutorizacionService autorizaService = new AutorizacionService(
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autorizaService = new %s(",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionService")),
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionService"))));
        bodyBuilder.indent();

        // new URL(getEndpointAutoriza()));
        bodyBuilder.appendFormalLine(String.format(
                "new %s(getEndpointAutoriza()));",
                helper.getFinalTypeName(new JavaType("java.net.URL"))));
        bodyBuilder.indentRemove();

        // AutorizacionPortType autorizaPort = autorizaService
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autorizaPort = autorizaService",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionPortType"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(".getAutorizacionPortTypeSoap11();");
        bodyBuilder.indentRemove();

        // Client autorizaClient = ClientProxy.getClient(autorizaPort);
        bodyBuilder.appendFormalLine(String.format(
                "%s autorizaClient = %s.getClient(autorizaPort);", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.cxf.endpoint.Client")), helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.cxf.frontend.ClientProxy"))));
        bodyBuilder.appendFormalLine("setSecurity(autorizaClient);");

        bodyBuilder
                .appendFormalLine("// Checking if is necessary filter by applicationId");
        bodyBuilder.appendFormalLine("");

        // SafeUser user = null;
        bodyBuilder.appendFormalLine("SafeUser user = null;");

        // if(getFiltrarPorAplicacion()){
        bodyBuilder.appendFormalLine("if(getFiltrarPorAplicacion()){");
        bodyBuilder.indent();
        // RetornaAutorizacionWSResponse autorizaResponse = autorizaPort
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autorizaResponse = autorizaPort",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaAutorizacionWSResponse"))));
        bodyBuilder.indent();

        bodyBuilder
                .appendFormalLine(".retornaAutorizacionWS(retornaUsuApliAut);");
        bodyBuilder.indentRemove();

        // List<Permisoapp> listaPermisos = autorizaResponse.getPermisoapp();
        bodyBuilder
                .appendFormalLine(String
                        .format("%s listaPermisos = autorizaResponse.getPermisoapp();",
                                helper.getFinalTypeName(new JavaType(
                                        "java.util.List",
                                        0,
                                        DataType.TYPE,
                                        null,
                                        Arrays.asList(new JavaType(
                                                "es.gva.dgm.ayf.war.definitions.v2u00.Permisoapp"))))));
        bodyBuilder
                .appendFormalLine("user = convertWSInfoToUser(getInformacionWSResponse,");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("username, listaPermisos);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();
        // RetornaTodasAutorizacionesDNIWSResponse autorizaResponse =
        // autorizaPort
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autorizaResponse = autorizaPort",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaTodasAutorizacionesDNIWSResponse"))));
        bodyBuilder.indent();

        bodyBuilder
                .appendFormalLine(".retornaTodasAutorizacionesDNIWS(retornaTodasAut);");
        bodyBuilder.indentRemove();

        // List<Permiso> listaPermisos = autorizaResponse.getLista();
        bodyBuilder
                .appendFormalLine(String
                        .format("%s listaPermisos = autorizaResponse.getLista();",
                                helper.getFinalTypeName(new JavaType(
                                        "java.util.List",
                                        0,
                                        DataType.TYPE,
                                        null,
                                        Arrays.asList(new JavaType(
                                                "es.gva.dgm.ayf.war.definitions.v2u00.Permiso"))))));
        bodyBuilder
                .appendFormalLine("user = convertWSInfoToUserTodasAplicaciones(getInformacionWSResponse,");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("username, listaPermisos);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Object salt = null;
        bodyBuilder.appendFormalLine(String.format("%s salt = null;",
                helper.getFinalTypeName(new JavaType("java.lang.Object"))));
        bodyBuilder.appendFormalLine("if (this.saltSource != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("salt = this.saltSource.getSalt(user);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("user.setPassword(passwordEncoder.encodePassword(presentedPassword,salt));");
        bodyBuilder.appendFormalLine("return user;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("catch (Exception e) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("logger.warn(\"Solicitud de login denegada (usuario='\" + username");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ \"'): \" + e.getLocalizedMessage());");
        bodyBuilder.indentRemove();

        // if (e instanceof SOAPFaultException) {
        bodyBuilder.appendFormalLine(String.format("if (e instanceof %s) {",
                helper.getFinalTypeName(new JavaType(
                        "javax.xml.ws.soap.SOAPFaultException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("String message = e.getMessage();");
        bodyBuilder
                .appendFormalLine("if (message.indexOf(\"autenticaUsuarioLDAPWS\") > 0) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("message = \"Por favor, compruebe que el usuario y password son correctos\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // throw new AuthenticationServiceException(\"Acceso denegado.
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(\"Acceso denegado. \"",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.AuthenticationServiceException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ message, e);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();

        // throw new AuthenticationServiceException(
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.AuthenticationServiceException"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\"Error en servicio web de login al validar al usuario.\"");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ e.getMessage(), e);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("SafeUser user = new SafeUser();");
        bodyBuilder.appendFormalLine("user.setUsername(username);");
        bodyBuilder.appendFormalLine("return user;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
    }

    /**
     * Builds body method for <code>setSecurity</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildSetSecurityMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Properties configuration = getSafeProperties();
        bodyBuilder.appendFormalLine(String.format(
                "%s configuration = getSafeProperties();",
                helper.getFinalTypeName(new JavaType("java.util.Properties"))));

        // org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
        bodyBuilder.appendFormalLine(String.format(
                "%s cxfEndpoint = client.getEndpoint();", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.cxf.endpoint.Endpoint"))));

        // Map<String, Object> outProps = new HashMap<String, Object>();
        bodyBuilder.appendFormalLine(String.format(
                "%s<String, Object> outProps = new %s<String, Object>();",
                helper.getFinalTypeName(new JavaType("java.util.Map")),
                helper.getFinalTypeName(new JavaType("java.util.HashMap"))));

        // outProps.put(WSHandlerConstants.ACTION,
        // WSHandlerConstants.SIGNATURE);
        bodyBuilder.appendFormalLine(String.format(
                "outProps.put(%s.ACTION, %s.SIGNATURE);",
                helper.getFinalTypeName(new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants")),
                new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants")));
        bodyBuilder.appendFormalLine("outProps.put(");
        bodyBuilder.indent();

        // WSHandlerConstants.USER,
        bodyBuilder.appendFormalLine(String.format("%s.USER,", helper
                .getFinalTypeName(new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants"))));
        bodyBuilder.appendFormalLine("configuration");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(".getProperty(\"org.apache.ws.security.crypto.merlin.keystore.alias\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("outProps.put(");
        bodyBuilder.indent();

        // WSHandlerConstants.SIGNATURE_USER,
        bodyBuilder.appendFormalLine(String.format("%s.SIGNATURE_USER,", helper
                .getFinalTypeName(new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants"))));
        bodyBuilder.appendFormalLine("configuration");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(".getProperty(\"org.apache.ws.security.crypto.merlin.keystore.alias\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();

        // outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS,
        bodyBuilder.appendFormalLine(String.format(
                "outProps.put(%s.PW_CALLBACK_CLASS,",
                helper.getFinalTypeName(new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("PasswordHandler.class.getCanonicalName());");
        bodyBuilder.indentRemove();

        // outProps.put(WSHandlerConstants.SIG_PROP_FILE, PROPERTIES_PATH);
        bodyBuilder.appendFormalLine(String.format(
                "outProps.put(%s.SIG_PROP_FILE, PROPERTIES_PATH);",
                helper.getFinalTypeName(new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants"))));

        // outProps.put(WSHandlerConstants.SIG_KEY_ID, \"DirectReference\");
        bodyBuilder.appendFormalLine(String.format(
                "outProps.put(%s.SIG_KEY_ID, \"DirectReference\");",
                helper.getFinalTypeName(new JavaType(
                        "org.apache.ws.security.handler.WSHandlerConstants"))));

        // WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s wssOut = new %s(outProps);",
                        helper.getFinalTypeName(new JavaType(
                                "org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor")),
                        helper.getFinalTypeName(new JavaType(
                                "org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor"))));
        bodyBuilder
                .appendFormalLine("cxfEndpoint.getOutInterceptors().add(wssOut);");

    }

    /**
     * Builds body method for <code>convertWsInfoUser</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildConvertWSInfoToUserMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("SafeUser user = new SafeUser();");
        bodyBuilder.appendFormalLine("user.setNombre(userFromWS.getNombre());");
        bodyBuilder.appendFormalLine("user.setEmail(userFromWS.getEmail());");
        bodyBuilder
                .appendFormalLine("user.setApellido1(userFromWS.getApellido1());");
        bodyBuilder
                .appendFormalLine("user.setApellido2(userFromWS.getApellido2());");
        bodyBuilder.appendFormalLine("user.setCif(userFromWS.getCif());");
        bodyBuilder
                .appendFormalLine("user.setHabilitado(userFromWS.getHabilitado());");
        bodyBuilder.appendFormalLine("user.setIdHDFI(userFromWS.getIdHDFI());");
        bodyBuilder
                .appendFormalLine("user.setIusserDN(userFromWS.getIssuerDN());");
        bodyBuilder.appendFormalLine("user.setNif(userFromWS.getNif());");
        bodyBuilder.appendFormalLine("user.setOid(userFromWS.getOid());");
        bodyBuilder
                .appendFormalLine("user.setRazonSocial(userFromWS.getRazonSocial());");
        bodyBuilder
                .appendFormalLine("user.setRepresentante(userFromWS.getRepresentante());");
        bodyBuilder
                .appendFormalLine("user.setSerialNumber(userFromWS.getSerialNumber());");
        bodyBuilder
                .appendFormalLine("user.setSubjectDN(userFromWS.getSubjectDN());");
        bodyBuilder
                .appendFormalLine("user.setTipoAut(userFromWS.getTipoAut());");
        bodyBuilder
                .appendFormalLine("user.setTipoCertificado(userFromWS.getTipoCertificado());");
        bodyBuilder.appendFormalLine("// Spring Security User info");
        bodyBuilder.appendFormalLine("user.setUsername(username);");
        bodyBuilder
                .appendFormalLine("user.setAccountNonExpired(true); // Status info");
        bodyBuilder
                .appendFormalLine("user.setAccountNonLocked(true);// Status info");
        bodyBuilder
                .appendFormalLine("user.setCredentialsNonExpired(true); // Status info");
        bodyBuilder.appendFormalLine("user.setEnabled(true);// Status info");
        bodyBuilder.appendFormalLine("// Roles");
        bodyBuilder.appendFormalLine("if (listPermisos == null) {");
        bodyBuilder.indent();

        // throw new BadCredentialsException(
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.BadCredentialsException"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\" El usuario proporcionado no tiene módulos asignados\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s<%s> authorities = new %s<%s>();",
                        helper.getFinalTypeName(new JavaType("java.util.Set")),
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.core.GrantedAuthority")),
                        helper.getFinalTypeName(new JavaType(
                                "java.util.HashSet")),
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.core.GrantedAuthority"))));

        // Iterator<Permisoapp> iter = listPermisos.iterator();
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> iter = listPermisos.iterator();", helper
                        .getFinalTypeName(new JavaType("java.util.Iterator")),
                helper.getFinalTypeName(new JavaType(
                        "es.gva.dgm.ayf.war.definitions.v2u00.Permisoapp"))));
        bodyBuilder.appendFormalLine("while (iter.hasNext()) {");
        bodyBuilder.indent();

        // Permisoapp permisoApp = iter.next();
        bodyBuilder.appendFormalLine(String.format(
                "%s permisoApp = iter.next();",
                helper.getFinalTypeName(new JavaType(
                        "es.gva.dgm.ayf.war.definitions.v2u00.Permisoapp"))));
        bodyBuilder
                .appendFormalLine("String rolUsu = convertToApplicationRol(permisoApp.getIdgrupo());");
        bodyBuilder.appendFormalLine("if (rolUsu != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("String[] roles = rolUsu.split(\",\");");
        bodyBuilder.appendFormalLine("for(int i = 0;i < roles.length;i++){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("SafeUserAuthority usAuth = new SafeUserAuthority();");
        bodyBuilder.appendFormalLine("usAuth.setAuthority(roles[i]);");
        bodyBuilder
                .appendFormalLine("usAuth.setIdgrupo(permisoApp.getIdgrupo());");
        bodyBuilder
                .appendFormalLine("usAuth.setIdaplicacion(permisoApp.getIdaplicacion());");
        bodyBuilder.appendFormalLine("usAuth.setNif(permisoApp.getNif());");
        bodyBuilder
                .appendFormalLine("usAuth.setUsrtipo(permisoApp.getUsrtipo());");
        bodyBuilder.appendFormalLine("usAuth.setIdrol(permisoApp.getIdrol());");
        bodyBuilder.appendFormalLine("authorities.add(usAuth);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("user.setAuthorities(authorities);");
        bodyBuilder.appendFormalLine("return user;");

    }

    /**
     * Builds body method for <code>convertWSInfoToUserTodasAplicaciones</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildConvertWSInfoToUserTodasAplicacionesMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("SafeUser user = new SafeUser();");
        bodyBuilder.appendFormalLine("user.setNombre(userFromWS.getNombre());");
        bodyBuilder.appendFormalLine("user.setEmail(userFromWS.getEmail());");
        bodyBuilder
                .appendFormalLine("user.setApellido1(userFromWS.getApellido1());");
        bodyBuilder
                .appendFormalLine("user.setApellido2(userFromWS.getApellido2());");
        bodyBuilder.appendFormalLine("user.setCif(userFromWS.getCif());");
        bodyBuilder
                .appendFormalLine("user.setHabilitado(userFromWS.getHabilitado());");
        bodyBuilder.appendFormalLine("user.setIdHDFI(userFromWS.getIdHDFI());");
        bodyBuilder
                .appendFormalLine("user.setIusserDN(userFromWS.getIssuerDN());");
        bodyBuilder.appendFormalLine("user.setNif(userFromWS.getNif());");
        bodyBuilder.appendFormalLine("user.setOid(userFromWS.getOid());");
        bodyBuilder
                .appendFormalLine("user.setRazonSocial(userFromWS.getRazonSocial());");
        bodyBuilder
                .appendFormalLine("user.setRepresentante(userFromWS.getRepresentante());");
        bodyBuilder
                .appendFormalLine("user.setSerialNumber(userFromWS.getSerialNumber());");
        bodyBuilder
                .appendFormalLine("user.setSubjectDN(userFromWS.getSubjectDN());");
        bodyBuilder
                .appendFormalLine("user.setTipoAut(userFromWS.getTipoAut());");
        bodyBuilder
                .appendFormalLine("user.setTipoCertificado(userFromWS.getTipoCertificado());");
        bodyBuilder.appendFormalLine("// Spring Security User info");
        bodyBuilder.appendFormalLine("user.setUsername(username);");
        bodyBuilder
                .appendFormalLine("user.setAccountNonExpired(true); // Status info");
        bodyBuilder
                .appendFormalLine("user.setAccountNonLocked(true);// Status info");
        bodyBuilder
                .appendFormalLine("user.setCredentialsNonExpired(true); // Status info");
        bodyBuilder.appendFormalLine("user.setEnabled(true);// Status info");
        bodyBuilder.appendFormalLine("// Roles");
        bodyBuilder.appendFormalLine("if (listPermisos == null) {");
        bodyBuilder.indent();

        // throw new BadCredentialsException(
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.BadCredentialsException"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\" El usuario proporcionado no tiene módulos asignados\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s<%s> authorities = new %s<%s>();",
                        helper.getFinalTypeName(new JavaType("java.util.Set")),
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.core.GrantedAuthority")),
                        helper.getFinalTypeName(new JavaType(
                                "java.util.HashSet")),
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.core.GrantedAuthority"))));

        // Iterator<Permiso> iter = listPermisos.iterator();
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> iter = listPermisos.iterator();", helper
                        .getFinalTypeName(new JavaType("java.util.Iterator")),
                helper.getFinalTypeName(new JavaType(
                        "es.gva.dgm.ayf.war.definitions.v2u00.Permiso"))));
        bodyBuilder.appendFormalLine("while (iter.hasNext()) {");
        bodyBuilder.indent();

        // Permisoapp permisoApp = iter.next();
        bodyBuilder.appendFormalLine(String.format("%s permiso = iter.next();",
                helper.getFinalTypeName(new JavaType(
                        "es.gva.dgm.ayf.war.definitions.v2u00.Permiso"))));
        bodyBuilder
                .appendFormalLine("String rolUsu = convertToApplicationRol(permiso.getIdgrupo());");
        bodyBuilder.appendFormalLine("if (rolUsu != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("String[] roles = rolUsu.split(\",\");");
        bodyBuilder.appendFormalLine("for(int i = 0;i < roles.length;i++){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("SafeUserAuthority usAuth = new SafeUserAuthority();");
        bodyBuilder.appendFormalLine("usAuth.setAuthority(roles[i]);");
        bodyBuilder
                .appendFormalLine("usAuth.setIdgrupo(permiso.getIdgrupo());");
        bodyBuilder
                .appendFormalLine("usAuth.setIdaplicacion(permiso.getIdaplicacion());");
        bodyBuilder.appendFormalLine("usAuth.setNif(userFromWS.getNif());");
        bodyBuilder
                .appendFormalLine("usAuth.setUsrtipo(permiso.getUsrtipo());");
        bodyBuilder.appendFormalLine("usAuth.setIdrol(permiso.getIdrol());");
        bodyBuilder.appendFormalLine("authorities.add(usAuth);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("user.setAuthorities(authorities);");
        bodyBuilder.appendFormalLine("return user;");

    }

    /**
     * Builds body method for <code>convertToApplicationRol</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildConvertToApplicationRolMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("if (!mapRoles) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return idgrupo;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("if (prop == null){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("prop = new Properties();");

        // loader = Thread.currentThread().getContextClassLoader();
        bodyBuilder.appendFormalLine(String.format(
                "loader = %s.currentThread().getContextClassLoader();",
                helper.getFinalTypeName(new JavaType("java.lang.Thread"))));
        bodyBuilder
                .appendFormalLine("stream = loader.getResourceAsStream(\"safe_client_roles.properties\");");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("prop.load(stream);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // catch (IOException e) {
        bodyBuilder.appendFormalLine(String.format("catch (%s e) {",
                helper.getFinalTypeName(new JavaType("java.io.IOException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("e.printStackTrace();");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // StringBuilder sbuilder = new StringBuilder(\"SAFE.\");
        bodyBuilder.appendFormalLine(String.format(
                "%s sbuilder = new %s(\"SAFE.\");", helper
                        .getFinalTypeName(new JavaType(
                                "java.lang.StringBuilder")), helper
                        .getFinalTypeName(new JavaType(
                                "java.lang.StringBuilder"))));
        bodyBuilder
                .appendFormalLine("if (getEnvironment() != null && !getEnvironment().isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("sbuilder.append('.');");
        bodyBuilder.appendFormalLine("sbuilder.append(getEnvironment());");
        bodyBuilder.appendFormalLine("sbuilder.append('.');");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("sbuilder.append(\"role.\");");
        bodyBuilder.appendFormalLine("sbuilder.append(idgrupo);");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return prop.getProperty(sbuilder.toString());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // catch (MissingResourceException e) {
        bodyBuilder.appendFormalLine(String.format("catch (%s e) {", helper
                .getFinalTypeName(new JavaType(
                        "java.util.MissingResourceException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return idgrupo;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
    }

    /**
     * Builds body method for <code>getSafeProperties</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetSafePropertiesMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Properties configuration = new Properties();
        bodyBuilder.appendFormalLine(String.format(
                "%s configuration = new %s();",
                helper.getFinalTypeName(new JavaType("java.util.Properties")),
                helper.getFinalTypeName(new JavaType("java.util.Properties"))));

        // ClassLoader classLoader = Thread.currentThread()
        bodyBuilder.appendFormalLine(String.format(
                "%s classLoader = %s.currentThread()",
                helper.getFinalTypeName(new JavaType("java.lang.ClassLoader")),
                helper.getFinalTypeName(new JavaType("java.lang.Thread"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(".getContextClassLoader();");
        bodyBuilder.indentRemove();

        // InputStream in = classLoader.getResourceAsStream(PROPERTIES_PATH);
        bodyBuilder.appendFormalLine(String.format(
                "%s in = classLoader.getResourceAsStream(PROPERTIES_PATH);",
                helper.getFinalTypeName(new JavaType("java.io.InputStream"))));

        // configuration = new Properties();
        bodyBuilder.appendFormalLine(String.format("configuration = new %s();",
                helper.getFinalTypeName(new JavaType("java.util.Properties"))));
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("configuration.load(in);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // catch (IOException e) {
        bodyBuilder.appendFormalLine(String.format("catch (%s e) {",
                helper.getFinalTypeName(new JavaType("java.io.IOException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("e.printStackTrace();");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return configuration;");

    }

    /**
     * Gets all getters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getGetterMethod(String propertyName,
            JavaType returnType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "get".concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName, returnType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets all setters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getSetterMethod(String propertyName,
            JavaType parameterType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(parameterType);

        // Check if a method with the same signature already exists in the
        // target type
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "set".concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(propertyName));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for all getters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("return this.".concat(propertyName)
                .concat(";"));

    }

    /**
     * Builds body method for all setters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildSetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("this.".concat(propertyName).concat(" = ")
                .concat(propertyName).concat(";"));

    }

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getField(String name, String value,
            JavaType javaType, int modifier) {
        JavaSymbolName curName = new JavaSymbolName(name);
        String initializer = value;
        FieldMetadata field = getOrCreateField(curName, javaType, initializer,
                modifier, null);
        return field;
    }

    /**
     * Gets or creates a field based on parameters.<br>
     * First try to get a suitable field (by name and type). If not found create
     * a new one (adding a counter to name if it's needed)
     * 
     * @param fielName
     * @param fieldType
     * @param initializer (String representation)
     * @param modifier See {@link Modifier}
     * @param annotations optional (can be null)
     * @return
     */
    private FieldMetadata getOrCreateField(JavaSymbolName fielName,
            JavaType fieldType, String initializer, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = fielName;

        // Check if field exist
        FieldMetadata currentField = governorTypeDetails
                .getDeclaredField(curName);
        if (currentField != null) {
            if (!currentField.getFieldType().equals(fieldType)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = curName;
                int i = 1;
                while (governorTypeDetails.getDeclaredField(newName) != null) {
                    newName = new JavaSymbolName(curName.getSymbolName()
                            .concat(StringUtils.repeat('_', i)));
                    i++;
                }
                curName = newName;
            }
        }
        if (currentField == null) {
            // create field
            if (annotations == null) {
                annotations = new ArrayList<AnnotationMetadataBuilder>(0);
            }
            // Using the FieldMetadataBuilder to create the field
            // definition.
            final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                    getId(), modifier, annotations, curName, // Field
                    fieldType); // Field type
            fieldBuilder.setFieldInitializer(initializer);
            currentField = fieldBuilder.build(); // Build and return a
            // FieldMetadata
            // instance
        }
        return currentField;

    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
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

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }
}
