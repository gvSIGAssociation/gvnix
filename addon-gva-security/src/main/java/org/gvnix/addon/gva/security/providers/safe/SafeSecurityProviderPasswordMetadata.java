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
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXPasswordHandlerSAFE} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class SafeSecurityProviderPasswordMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName CALLBACKS_PARAM = new JavaSymbolName(
            "callbacks");

    private static final JavaType IO_EXCEPTION = new JavaType(
            "java.io.IOException");

    private static final JavaType CALLBACK_EXCEPTION = new JavaType(
            "javax.security.auth.callback.UnsupportedCallbackException");

    private static final JavaType CALLBACK_PARAM_TYPE = new JavaType(
            "javax.security.auth.callback.Callback");

    private static final JavaType CALLBACK_PARAM_ARRAY_TYPE = new JavaType(
            CALLBACK_PARAM_TYPE.getFullyQualifiedTypeName(), 1, DataType.TYPE,
            null, null);

    private static final JavaSymbolName HANDLE_METHOD = new JavaSymbolName(
            "handle");

    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderPasswordMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    public SafeSecurityProviderPasswordMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        /*Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");*/

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this,
                builder.getImportRegistrationResolver());

        builder.addMethod(getHandleMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>handle</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getHandleMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(CALLBACK_PARAM_ARRAY_TYPE);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(HANDLE_METHOD,
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
        throwsTypes.add(CALLBACK_EXCEPTION);
        throwsTypes.add(IO_EXCEPTION);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(CALLBACKS_PARAM);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildHandleMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, HANDLE_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>handle</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildHandleMethodBody(InvocableMemberBodyBuilder bodyBuilder) {
        // Properties configuration = SafeProvider.getSafeProperties();
        bodyBuilder.appendFormalLine(String.format(
                "%s configuration = SafeProvider.getSafeProperties();",
                helper.getFinalTypeName(new JavaType("java.util.Properties"))));

        // for(Callback callback: callbacks) {
        bodyBuilder.appendFormalLine(String.format(
                "for(%s callback: callbacks) {", helper
                        .getFinalTypeName(new JavaType(
                                "javax.security.auth.callback.Callback"))));

        bodyBuilder.indent();

        // WSPasswordCallback pwdCallback = (WSPasswordCallback)callback;
        bodyBuilder.appendFormalLine(String.format(
                "%s pwdCallback = (%s)callback;", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.ws.security.WSPasswordCallback")),
                helper.getFinalTypeName(new JavaType(
                        "org.apache.ws.security.WSPasswordCallback"))));

        bodyBuilder.appendFormalLine("int usage = pwdCallback.getUsage();");

        bodyBuilder
                .appendFormalLine("if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("pwdCallback.setPassword(configuration.getProperty(\"org.apache.ws.security.crypto.merlin.alias.password\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
    }

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
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
}
