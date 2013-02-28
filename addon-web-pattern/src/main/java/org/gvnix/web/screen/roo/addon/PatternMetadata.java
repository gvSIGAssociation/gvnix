/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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
package org.gvnix.web.screen.roo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdSourceFileComposer;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

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
public class PatternMetadata extends AbstractPatternMetadata {

    private static final String PROVIDES_TYPE_STRING = PatternMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public PatternMetadata(
            String mid,
            JavaType aspect,
            PhysicalTypeMetadata controllerMetadata,
            MemberDetails controllerDetails,
            WebScaffoldMetadata webScaffoldMetadata,
            List<StringAttributeValue> patterns,
            PhysicalTypeMetadata entityMetadata,
            SortedMap<JavaType, JavaTypeMetadataDetails> relatedEntities,
            SortedMap<JavaType, JavaTypeMetadataDetails> relatedFields,
            Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedDates,
            Map<JavaSymbolName, DateTimeFormatDetails> entityDateTypes) {

        super(mid, aspect, controllerMetadata, controllerDetails,
                webScaffoldMetadata, patterns, entityMetadata, relatedEntities,
                relatedFields, relatedDates, entityDateTypes);

        if (!isValid()) {

            // This metadata instance not be already produced at the time of
            // instantiation (will retry)
            return;
        }

        List<String> registerPatterns = getPatternTypeDefined(
                WebPatternType.register, this.patterns);
        if (!registerPatterns.isEmpty()) {

            // TODO findEntries method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails()
                    .getFindEntriesMethod() == null) {

                // TODO: If no find entries method, all other patterns are not
                // generated ?
                return;
            }

            for (String registerPattern : registerPatterns) {

                builder.addMethod(getCreateMethod(registerPattern));
                builder.addMethod(getUpdateMethod(registerPattern));
            }
        }

        List<String> tabularEditPatterns = getPatternTypeDefined(
                WebPatternType.tabular_edit_register, this.patterns);
        if (!tabularEditPatterns.isEmpty()) {

            // TODO findAll method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails().getFindAllMethod() == null) {

                // TODO: If no find all method, all other patterns are not
                // generated ?
                return;
            }

            for (String tabularEditPattern : tabularEditPatterns) {

                // Method only exists when this is a detail pattern (has master
                // entity)
                builder.addMethod(getCreateMethod(tabularEditPattern));
                builder.addMethod(getUpdateMethod(tabularEditPattern));
            }
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        new ItdSourceFileComposer(itdTypeDetails);
    }

    protected MethodMetadata getCreateMethod(String patternName) {

        // TODO Some code duplicated with same method in RelatedPatternMetadata

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("createPattern"
                + patternName);

        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        getRequestParam(methodParamNames, methodParamTypes);

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityTypeDetails.getPlural();

        bodyBuilder.appendFormalLine("create(".concat(
                entity.getSimpleTypeName().toLowerCase()).concat(
                ", bindingResult, uiModel, httpServletRequest);"));

        bodyBuilder.appendFormalLine("if ( bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        addBodyLinesForDialogBinding(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat(
                "?\" + refererQuery(httpServletRequest);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("return \"".concat("redirect:/")
                .concat(entityNamePlural.toLowerCase())
                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder.setAnnotations(getRequestMappingAnnotationCreateUpdate(
                RequestMethod.POST, patternName));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    protected MethodMetadata getUpdateMethod(String patternName) {

        // TODO Some code duplicated with same method in RelatedPatternMetadata

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("updatePattern"
                + patternName);

        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        getRequestParam(methodParamNames, methodParamTypes);

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {

            // If it already exists, just return null and omit its generation
            // via the ITD
            return null;
        }

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityTypeDetails.getPlural();

        bodyBuilder.appendFormalLine("update(".concat(
                entity.getSimpleTypeName().toLowerCase()).concat(
                ", bindingResult, uiModel, httpServletRequest);"));

        bodyBuilder.appendFormalLine("if ( bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        addBodyLinesForDialogBinding(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat(
                "?\" + refererQuery(httpServletRequest);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("return \"".concat("redirect:/")
                .concat(entityNamePlural.toLowerCase())
                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder.setAnnotations(getRequestMappingAnnotationCreateUpdate(
                RequestMethod.PUT, patternName));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    // Typically, no changes are required beyond this point

    public static final String getMetadataIdentiferType() {

        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType controller,
            LogicalPath path) {

        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, controller, path);
    }

    public static final JavaType getJavaType(String mid) {

        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, mid);
    }

    public static final LogicalPath getPath(String mid) {

        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                mid);
    }

    public static boolean isValid(String mid) {

        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                mid);
    }

}
