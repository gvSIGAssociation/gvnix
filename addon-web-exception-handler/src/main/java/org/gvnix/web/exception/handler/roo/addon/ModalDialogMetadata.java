/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.exception.handler.roo.addon;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdSourceFileComposer;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public class ModalDialogMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private final MemberDetails memberDetails;

    private static final String PROVIDES_TYPE_STRING = ModalDialogMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public ModalDialogMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            MemberDetails memberDetails) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.memberDetails = memberDetails;

        builder.addMethod(getModalDialogMethod());

        itdTypeDetails = builder.build();

        new ItdSourceFileComposer(itdTypeDetails);
    }

    /**
     * 
     * @param aspectName
     * @return
     */
    private MethodMetadata getModalDialogMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("modalDialog");

        JavaType dialogType = getJavaTypeForClassName("DialogType");
        List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
        paramTypes.add(new AnnotatedJavaType(dialogType,
                new ArrayList<AnnotationMetadata>()));
        paramTypes.add(new AnnotatedJavaType(JavaType.STRING_OBJECT,
                new ArrayList<AnnotationMetadata>()));
        paramTypes.add(new AnnotatedJavaType(JavaType.STRING_OBJECT,
                new ArrayList<AnnotationMetadata>()));
        paramTypes.add(new AnnotatedJavaType(JavaType.STRING_OBJECT,
                new ArrayList<AnnotationMetadata>()));
        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(JavaType.STRING_OBJECT);
        typeParams.add(new JavaType("java.lang.Object"));
        JavaType hashMap = new JavaType("java.util.HashMap", 0, DataType.TYPE,
                null, typeParams);
        paramTypes.add(new AnnotatedJavaType(hashMap,
                new ArrayList<AnnotationMetadata>()));
        paramTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"),
                new ArrayList<AnnotationMetadata>()));

        MethodMetadata modalDialogMethod = methodExists(methodName, paramTypes);
        if (modalDialogMethod != null) {
            return modalDialogMethod;
        }

        List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
        paramNames.add(new JavaSymbolName("dialogType"));
        paramNames.add(new JavaSymbolName("page"));
        paramNames.add(new JavaSymbolName("title"));
        paramNames.add(new JavaSymbolName("description"));
        paramNames.add(new JavaSymbolName("params"));
        paramNames.add(new JavaSymbolName("httpServletRequest"));

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        JavaType httpSession = new JavaType("javax.servlet.http.HttpSession");
        bodyBuilder.appendFormalLine(httpSession
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(
                        " session = httpServletRequest.getSession();"));
        JavaType modalDialog = getJavaTypeForClassName("ModalDialog");
        bodyBuilder
                .appendFormalLine(modalDialog
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(" modalDialog = new ModalDialog(dialogType, page, title, description, params);"));
        bodyBuilder
                .appendFormalLine("session.setAttribute(\"dialogMessage\", modalDialog);");

        return new MethodMetadataBuilder(getId(), 0, methodName,
                JavaType.VOID_PRIMITIVE, paramTypes, paramNames, bodyBuilder)
                .build();
    }

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> parameters) {
        return MemberFindingUtils.getMethod(memberDetails, methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(parameters));
    }

    /**
     * Returns the JavaType for the given className. It gets the fully qualified
     * package name from the aspect of this metadata, that is the aspect of a
     * Controller class.
     * <p>
     * Since the add-on installs ModalDialog class in subpackage
     * <code>controller_package.servlet.handler</code>, it builds the package
     * name adding <code>.servlet.handler.[className]</code> to the aspect
     * package.
     * <p>
     * DialogType is special because is an enum type defined in ModalDialog
     * class
     * 
     * @param className
     *            Right now it expects only "DialogType" or "ModalDialog"
     * @return
     */
    private JavaType getJavaTypeForClassName(String className) {
        String typePackage = aspectName.getPackage()
                .getFullyQualifiedPackageName().concat(".servlet.handler.");
        if (className.equals("DialogType")) {
            return new JavaType(typePackage.concat("ModalDialog.").concat(
                    className));
        }
        return new JavaType(typePackage.concat(className));
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
