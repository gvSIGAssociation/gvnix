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
package org.gvnix.web.screen.roo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;

import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdSourceFileComposer;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.style.ToStringCreator;
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
public class PatternMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final Logger logger = HandlerUtils
            .getLogger(PatternMetadata.class);

    private static final String PROVIDES_TYPE_STRING = PatternMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaType ENTITY_BATCH_ANNOTATION = new JavaType(
            GvNIXEntityBatch.class.getName());

    private WebScaffoldMetadata webScaffoldMetadata;
    private WebScaffoldAnnotationValues annotationValues;
    private JavaType formBackingType;
    private List<String> definedPatterns;
    SortedMap<JavaType, JavaTypeMetadataDetails> relatedApplicationTypeMetadata;
    private JavaTypeMetadataDetails javaTypeMetadataHolder;
    private List<MethodMetadata> controllerMethods;

    private MetadataService metadataService;
    private PropFileOperations propFileOperations;

    public PatternMetadata(
            String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            WebScaffoldMetadata webScaffoldMetadata,
            WebScaffoldAnnotationValues annotationValues,
            List<StringAttributeValue> definedPatterns,
            List<MethodMetadata> controllerMethods,
            SortedMap<JavaType, JavaTypeMetadataDetails> relatedApplicationTypeMetadata,
            MetadataService metadataService,
            PropFileOperations propFileOperations) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Assert.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");
        Assert.notNull(webScaffoldMetadata, "WebScaffoldMetadata required");
        Assert.notNull(annotationValues, "Annotation values required");
        Assert.notNull(relatedApplicationTypeMetadata,
                "Related application type metadata required");
        if (!isValid()) {
            return;
        }
        this.webScaffoldMetadata = webScaffoldMetadata;
        this.annotationValues = annotationValues;
        this.formBackingType = annotationValues.getFormBackingObject();
        this.controllerMethods = controllerMethods;
        this.relatedApplicationTypeMetadata = relatedApplicationTypeMetadata;
        this.javaTypeMetadataHolder = relatedApplicationTypeMetadata
                .get(formBackingType);
        Assert.notNull(javaTypeMetadataHolder,
                "Metadata holder required for form backing type: "
                        + formBackingType);

        this.metadataService = metadataService;
        this.propFileOperations = propFileOperations;

        /*
         * TODO: Take care of attributes "create, update, delete" in
         * RooWebScaffold annotation
         */
        List<String> definedPatternsList = new ArrayList<String>();
        for (StringAttributeValue definedPattern : definedPatterns) {
            definedPatternsList.add(definedPattern.getValue());
        }

        this.definedPatterns = Collections
                .unmodifiableList(definedPatternsList);

        // add a field and common methods
        builder.addField(getGvNIXPatternField());
        builder.addField(getDefinedPatternField());
        builder.addMethod(getIsPatternDefinedMethod());

        if (isPatternTypeDefined(WebPattern.tabular, definedPatternsList)) {
            // annotateFormBackingObject();
            builder.addMethod(getCreateListMethod());
            builder.addMethod(getUpdateListMethod());
            builder.addMethod(getDeleteListMethod());
            builder.addMethod(getFilterListMethod());
            builder.addMethod(getRefererRedirectMethod());
            builder.addMethod(getTabularMethod());
            // logger.warning(formBackingType.getSimpleTypeName().concat(
            // " must be annotated with @GvNIXEntityBatch"));
        }

        if (isPatternTypeDefined(WebPattern.register, definedPatternsList)) {
            addStaticFields();
            builder.addMethod(getRegisterMethod());
            builder.addMethod(getUpdateMethod());
            builder.addMethod(getCreateMethod());
            builder.addMethod(getDeleteMethod());
            builder.addMethod(getRefererQueryMethod());
            builder.addMethod(getRefererQueryNoIndexMethod());
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        new ItdSourceFileComposer(itdTypeDetails);
    }

    private MethodMetadata getUpdateMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("update");

        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypesCreateUpdate();

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        List<JavaSymbolName> methodParamNames = getMethodParameterNamesCreateUpdate();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = javaTypeMetadataHolder.getPlural();

        bodyBuilder.appendFormalLine("String viewName = update(".concat(
                formBackingType.getSimpleTypeName().toLowerCase()).concat(
                ", bindingResult, uiModel, request);"));

        bodyBuilder.appendFormalLine("if ( bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return viewName + \"?\" + refererQuery(request);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder
                .appendFormalLine("return \""
                        .concat("redirect:/")
                        .concat(entityNamePlural.toLowerCase())
                        .concat("/\" + \"?\" + FORM_PARAM_NAME + \"&\" + refererQuery(request);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder
                .setAnnotations(getRequestMappingAnnotationCreateUpdate(RequestMethod.PUT));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    private MethodMetadata getCreateMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("create");

        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypesCreateUpdate();

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        List<JavaSymbolName> methodParamNames = getMethodParameterNamesCreateUpdate();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = javaTypeMetadataHolder.getPlural();

        bodyBuilder.appendFormalLine("String viewName = create(".concat(
                formBackingType.getSimpleTypeName().toLowerCase()).concat(
                ", bindingResult, uiModel, request);"));

        bodyBuilder.appendFormalLine("if ( bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return viewName + \"?\" + refererQuery(request);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine(JavaType.LONG_PRIMITIVE
                .getNameIncludingTypeParameters()
                .concat(" count = ")
                .concat(formBackingType.getSimpleTypeName())
                .concat(".")
                .concat(javaTypeMetadataHolder.getPersistenceDetails()
                        .getCountMethod().getMethodName().getSymbolName())
                .concat("();"));
        bodyBuilder
                .appendFormalLine("return \""
                        .concat("redirect:/")
                        .concat(entityNamePlural.toLowerCase())
                        .concat("/\" + \"?\" + FORM_PARAM_NAME + \"&\" + refererQuery(request, (count == 0 ? 1 : count));"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder
                .setAnnotations(getRequestMappingAnnotationCreateUpdate(RequestMethod.POST));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    private MethodMetadata getDeleteMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("delete");

        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrPathVar = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPathVar.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "id"));
        List<AnnotationMetadata> methodAttrPathVarAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttPathVarAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.PathVariable"),
                reqParamAttrPathVar);
        methodAttrPathVarAnnotations.add(methodAttPathVarAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(JavaType.LONG_OBJECT,
                methodAttrPathVarAnnotations));

        List<AnnotationAttributeValue<?>> reqParamAttrPattern = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPattern.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "PATTERN_PARAM_NAME"));
        reqParamAttrPattern.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), false));
        List<AnnotationMetadata> methodAttrPatternAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrPatternAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrPattern);
        methodAttrPatternAnnotations.add(methodAttrPatternAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(JavaType.STRING_OBJECT,
                methodAttrPatternAnnotations));

        List<AnnotationAttributeValue<?>> reqParamAttrPage = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPage.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "PATTERN_PARAM_NAME"));
        reqParamAttrPage.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), false));
        List<AnnotationMetadata> methodAttrPageAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrPageAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrPage);
        methodAttrPageAnnotations.add(methodAttrPageAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(JavaType.INT_OBJECT,
                methodAttrPageAnnotations));

        List<AnnotationAttributeValue<?>> reqParamAttrSize = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrSize.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "PATTERN_PARAM_NAME"));
        reqParamAttrSize.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), false));
        List<AnnotationMetadata> methodAttrSizeAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrSizeAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrSize);
        methodAttrSizeAnnotations.add(methodAttrSizeAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(JavaType.INT_OBJECT,
                methodAttrSizeAnnotations));

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.ui.Model"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        // id, pattern, page, size, uiModel, request
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("id"));
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName("page"));
        methodParamNames.add(new JavaSymbolName("size"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("request"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = javaTypeMetadataHolder.getPlural();

        bodyBuilder.appendFormalLine("delete(id, page, size, uiModel);");
        bodyBuilder
                .appendFormalLine("return \""
                        .concat("redirect:/")
                        .concat(entityNamePlural.toLowerCase())
                        .concat("/\" + \"?\" + FORM_PARAM_NAME + \"&\" + refererQuery(request, 1L);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                new JavaSymbolName("value"), "/{"
                        + javaTypeMetadataHolder.getPersistenceDetails()
                                .getIdentifierField().getFieldName()
                                .getSymbolName() + "}"));
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "PATTERN_PARAM_NAME"));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        new JavaSymbolName("params"), paramValues));
        requestMappingAttributes.add(new EnumAttributeValue(new JavaSymbolName(
                "method"), new EnumDetails(new JavaType(
                "org.springframework.web.bind.annotation.RequestMethod"),
                new JavaSymbolName(RequestMethod.DELETE.name()))));
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);

        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    private MethodMetadata getRefererQueryMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("refererQuery");

        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));
        methodParamTypes.add(new AnnotatedJavaType(JavaType.LONG_OBJECT, null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        // request
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("request"));
        methodParamNames.add(new JavaSymbolName("i"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("String query = refererQuery(request);");
        bodyBuilder
                .appendFormalLine("int ini = query.indexOf(\"&\" + INDEX_PARAM_NAME + \"=\");");
        bodyBuilder
                .appendFormalLine("String index = \"&\" + INDEX_PARAM_NAME + \"=\" + i;");

        bodyBuilder.appendFormalLine("if (ini == -1) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("ini = query.indexOf(\"?\" + INDEX_PARAM_NAME + \"=\");");
        bodyBuilder
                .appendFormalLine("index = \"?\" + INDEX_PARAM_NAME + \"=\" + i;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("if (ini == -1) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("ini = query.startsWith(INDEX_PARAM_NAME + \"=\") ? 0 : -1;");
        bodyBuilder.appendFormalLine("index = INDEX_PARAM_NAME + \"=\" + i;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("if (ini == -1) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return \"\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder
                .appendFormalLine("int end = query.indexOf(\"&\", ini + 1);");
        bodyBuilder.appendFormalLine("end = end == -1 ? query.length() : end;");

        bodyBuilder
                .appendFormalLine("String prefix = query.substring(0, ini);");
        bodyBuilder
                .appendFormalLine("String sufix = query.substring(end, query.length());");

        bodyBuilder
                .appendFormalLine("return prefix.concat(index).concat(sufix);");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), 0, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    private MethodMetadata getRefererQueryNoIndexMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("refererQuery");

        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        // request
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("request"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("String url = \"\";");
        bodyBuilder
                .appendFormalLine("String referer = request.getHeader(REFERER_PARAM_NAME);");

        JavaType stringUtils = new JavaType(
                "org.springframework.util.StringUtils");
        bodyBuilder.appendFormalLine("if (".concat(
                stringUtils.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                ".hasText(referer)) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        JavaType netURL = new JavaType("java.net.URL");
        bodyBuilder.appendFormalLine("url = new ".concat(
                netURL.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                "(referer).getQuery();"));
        bodyBuilder
                .appendFormalLine("if ( url.contains(PATTERN_PARAM_NAME) ) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return url;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        JavaType netMalformedEx = new JavaType("java.net.MalformedURLException");
        bodyBuilder.appendFormalLine("} catch ( ".concat(
                netMalformedEx.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                " e ) {"));
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return url;");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), 0, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    private List<AnnotationMetadataBuilder> getRequestMappingAnnotationCreateUpdate(
            RequestMethod requestMethod) {
        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "PATTERN_PARAM_NAME"));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        new JavaSymbolName("params"), paramValues));
        requestMappingAttributes.add(new EnumAttributeValue(new JavaSymbolName(
                "method"), new EnumDetails(new JavaType(
                "org.springframework.web.bind.annotation.RequestMethod"),
                new JavaSymbolName(requestMethod.name()))));
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        return annotations;
    }

    private List<AnnotatedJavaType> getMethodParameterTypesCreateUpdate() {
        // Define method parameter types
        // @RequestParam(value = "gvnixpattern", required = true) String
        // pattern, @Valid Owner owner, BindingResult bindingResult,
        // HttpServletRequest req, Model uiModel)
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrPattern = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPattern.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "PATTERN_PARAM_NAME"));
        reqParamAttrPattern.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), false));
        List<AnnotationMetadata> methodAttrPatternAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrPatternAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrPattern);
        methodAttrPatternAnnotations.add(methodAttrPatternAnnotation.build());

        List<AnnotationMetadata> methodAttrValidAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttValidAnnotation = new AnnotationMetadataBuilder(
                new JavaType("javax.validation.Valid"));
        methodAttrValidAnnotations.add(methodAttValidAnnotation.build());

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(String.class
                .getName()), methodAttrPatternAnnotations));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(formBackingType
                .getSimpleTypeName()), methodAttrValidAnnotations));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.validation.BindingResult"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.ui.Model"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));
        return methodParamTypes;
    }

    private List<JavaSymbolName> getMethodParameterNamesCreateUpdate() {
        // Define method parameter names
        // pattern, entity, bindingResult, uiModel, request
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName(formBackingType
                .getSimpleTypeName().toLowerCase()));
        methodParamNames.add(new JavaSymbolName("bindingResult"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("request"));
        return methodParamNames;
    }

    /**
     * Enumeration of HTTP Request method types
     * 
     */
    public enum RequestMethod {
        DELETE, PUT, POST, GET;
    }

    /**
     * Enumeration of some persistence method names
     * 
     */
    public enum PersistenceMethod {
        REMOVE("remove"), PERSIST("persist"), MERGE("merge");

        private String name;

        private PersistenceMethod(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * If there is a pattern of givne WebPattern defined in GvNIXPattern it
     * returns true, false otherwise
     * 
     * @param patternType
     * @param definedPatternsList
     * @return
     */
    private boolean isPatternTypeDefined(WebPattern patternType,
            List<String> definedPatternsList) {
        for (String definedPattern : definedPatternsList) {
            if (definedPattern.split("=")[1].equalsIgnoreCase(patternType
                    .name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the annotations in formBackingType entity adding
     * {@link GvNIXBatchEntity}.
     * <p>
     * The method is <code>unused</code> until we find a way to invoke it
     * without get any issues
     */
    @SuppressWarnings("unused")
    private void annotateFormBackingObject() {

        // Test if the annotation already exists on the target type
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = getMutableTypeDetailsFormbakingObject();
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        ENTITY_BATCH_ANNOTATION);

        // Annotate formBackingType with GvNIXEntityBatch just if is not
        // annotated already. We don't need to update attributes
        if (annotationMetadata == null) {
            // Prepare annotation builder
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    ENTITY_BATCH_ANNOTATION);
            mutableTypeDetails.addTypeAnnotation(annotationBuilder.build());
        }
    }

    /**
     * Return the MutableClassOrInterfaceTypeDetails instance of the
     * formbackingType
     * 
     * @return
     */
    private MutableClassOrInterfaceTypeDetails getMutableTypeDetailsFormbakingObject() {
        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String formBackingTypeId = PhysicalTypeIdentifier.createIdentifier(
                formBackingType, Path.SRC_MAIN_JAVA);
        if (formBackingTypeId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + formBackingType.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(formBackingTypeId, true);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type ".concat(formBackingType
                        .getFullyQualifiedTypeName()));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type "
                        .concat(formBackingType.getFullyQualifiedTypeName()));

        // Test if the type is an MutableClassOrInterfaceTypeDetails instance so
        // the annotation can be added
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(formBackingType.getFullyQualifiedTypeName()));
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;
        return mutableTypeDetails;
    }

    private FieldMetadataBuilder getGvNIXPatternField() {
        return new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "PATTERN_PARAM_NAME"), JavaType.STRING_OBJECT,
                "\"gvnixpattern\"");
    }

    private FieldMetadataBuilder getDefinedPatternField() {

        String definedPatternIds = "";
        for (String definedPattern : definedPatterns) {
            if (definedPatternIds.length() > 0) {
                definedPatternIds = definedPatternIds.concat(", ");
            }
            definedPatternIds = definedPatternIds.concat("\"")
                    .concat(definedPattern.split("=")[0]).concat("\"");
        }

        JavaType stringArray = new JavaType(
                JavaType.STRING_OBJECT.getFullyQualifiedTypeName(), 1,
                DataType.TYPE, null, null);
        return new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC, new JavaSymbolName("definedPatterns"),
                stringArray, "{ ".concat(definedPatternIds).concat(" }"));
    }

    private void addStaticFields() {
        FieldMetadataBuilder field = new FieldMetadataBuilder(getId(),
                Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
                new JavaSymbolName("INDEX_PARAM_NAME"), JavaType.STRING_OBJECT,
                "\"index\"");
        builder.addField(field);

        field = new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "FORM_PARAM_NAME"), JavaType.STRING_OBJECT, "\"gvnixform\"");
        builder.addField(field);

        field = new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "REFERER_PARAM_NAME"), JavaType.STRING_OBJECT, "\"Referer\"");
        builder.addField(field);
    }

    /**
     * Returns the method handling requests of a given gvnixpattern of type
     * tabular
     * 
     * @return
     */
    private MethodMetadata getTabularMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("tabular");

        // Properties defined for the method
        Map<String, String> properties = new HashMap<String, String>();

        // Define method parameter types
        // @RequestParam(value = "gvnixpattern", required = true) String
        // pattern, HttpServletRequest req, Model uiModel)
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();
        List<AnnotationAttributeValue<?>> requestParamAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestParamAttributes.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "gvnixpattern"));
        requestParamAttributes.add(new BooleanAttributeValue(
                new JavaSymbolName("required"), true));
        List<AnnotationMetadata> methodAttributesAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttributesAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                requestParamAttributes);
        methodAttributesAnnotations.add(methodAttributesAnnotation.build());

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(String.class
                .getName()), methodAttributesAnnotations));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.ui.Model"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        // pattern, uiModel, request
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("request"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = javaTypeMetadataHolder.getPlural();

        bodyBuilder.appendFormalLine("if ( !isPatternDefined(pattern) ) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat("\";"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(formBackingType);
        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        bodyBuilder.appendFormalLine(javaUtilList
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" ")
                .concat(entityNamePlural.toLowerCase())
                .concat(" = ")
                .concat(formBackingType.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()))
                .concat(".")
                .concat(javaTypeMetadataHolder.getPersistenceDetails()
                        .getFindAllMethod().getMethodName().getSymbolName()
                        .concat("();")));

        bodyBuilder.appendFormalLine("if (".concat(
                entityNamePlural.toLowerCase()).concat(".isEmpty()) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                entityNamePlural.toLowerCase()).concat("\", null);"));
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"MESSAGE_INFO\",\"message_entitynotfound_problemdescription\");");
        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                .concat(entityNamePlural.toLowerCase()).concat("\", ")
                .concat(entityNamePlural.toLowerCase()).concat(");"));
        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));
        properties.put("message_entitynotfound_problemdescription",
                "There are not data of this entity");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixpattern"));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        new JavaSymbolName("params"), paramValues));
        requestMappingAttributes.add(new EnumAttributeValue(new JavaSymbolName(
                "method"), new EnumDetails(new JavaType(
                "org.springframework.web.bind.annotation.RequestMethod"),
                new JavaSymbolName(RequestMethod.GET.name()))));
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        propFileOperations
                .addProperties(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);
        return method;
    }

    private MethodMetadata getRegisterMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("register");

        // Properties defined for the method
        Map<String, String> properties = new HashMap<String, String>();

        // Define method parameter types
        // @RequestParam(value = "gvnixpattern", required = true) String
        // pattern, HttpServletRequest req, Model uiModel)
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrIndex = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrIndex.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "index"));
        reqParamAttrIndex.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), true));
        List<AnnotationMetadata> methodAttrIndexAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrIndexAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrIndex);
        methodAttrIndexAnnotations.add(methodAttrIndexAnnotation.build());

        List<AnnotationAttributeValue<?>> reqParamAttrPattern = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPattern.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "gvnixpattern"));
        reqParamAttrPattern.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), true));
        List<AnnotationMetadata> methodAttrPatternAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttrPatternAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestParam"),
                reqParamAttrPattern);
        methodAttrPatternAnnotations.add(methodAttrPatternAnnotation.build());

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(Integer.class
                .getName()), methodAttrIndexAnnotations));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(String.class
                .getName()), methodAttrPatternAnnotations));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.ui.Model"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        // pattern, uiModel, request
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("index"));
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("request"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = javaTypeMetadataHolder.getPlural();

        bodyBuilder.appendFormalLine("if ( !isPatternDefined(pattern) ) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat("\";"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(formBackingType);
        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        bodyBuilder
                .appendFormalLine(javaUtilList
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(" ")
                        .concat(entityNamePlural.toLowerCase())
                        .concat(" = ")
                        .concat(formBackingType.getNameIncludingTypeParameters(
                                false, builder.getImportRegistrationResolver()))
                        .concat(".")
                        .concat(javaTypeMetadataHolder
                                .getPersistenceDetails()
                                .getFindEntriesMethod()
                                .getMethodName()
                                .getSymbolName()
                                .concat("(index == null ? 0 : (index.intValue() - 1), 1);")));

        bodyBuilder.appendFormalLine(formBackingType
                .getSimpleTypeName()
                .concat(" ")
                .concat(formBackingType.getSimpleTypeName().toLowerCase()
                        .concat(" = null;")));
        bodyBuilder.appendFormalLine("if (!".concat(
                entityNamePlural.toLowerCase()).concat(".isEmpty()) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(formBackingType.getSimpleTypeName()
                .toLowerCase().concat(" = ")
                .concat(entityNamePlural.toLowerCase()).concat(".get(0);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                entityNamePlural.toLowerCase()).concat("\", null);"));
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"MESSAGE_INFO\",\"message_entitynotfound_problemdescription\");");
        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                .concat(entityNamePlural.toLowerCase()).concat("\", ")
                .concat(formBackingType.getSimpleTypeName().toLowerCase())
                .concat(");"));
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat("itemId")
                .concat("\", ")
                .concat(formBackingType.getSimpleTypeName().toLowerCase())
                .concat(".getId()").concat(");"));

        bodyBuilder.appendFormalLine(JavaType.LONG_PRIMITIVE
                .getNameIncludingTypeParameters()
                .concat(" count = ")
                .concat(formBackingType.getSimpleTypeName())
                .concat(".")
                .concat(javaTypeMetadataHolder.getPersistenceDetails()
                        .getCountMethod().getMethodName().getSymbolName())
                .concat("();"));
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"maxEntities"
                .concat("\", count == 0 ? 1 : count);"));

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));

        properties.put("message_entitynotfound_problemdescription",
                "There are not data of this entity");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixpattern"));
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "index"));
        requestMappingAttributes
                .add(new ArrayAttributeValue<AnnotationAttributeValue<? extends Object>>(
                        new JavaSymbolName("params"), paramValues));
        requestMappingAttributes.add(new EnumAttributeValue(new JavaSymbolName(
                "method"), new EnumDetails(new JavaType(
                "org.springframework.web.bind.annotation.RequestMethod"),
                new JavaSymbolName(RequestMethod.GET.name()))));
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        propFileOperations
                .addProperties(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);
        return method;
    }

    private MethodMetadata getIsPatternDefinedMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("isPatternDefined");

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING_OBJECT, null));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("pattern"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        // List<String> definedPatternsList = Arrays.asList(definedPatterns);
        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(JavaType.STRING_OBJECT);
        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        JavaType javaUtilArrays = new JavaType("java.util.Arrays", 0,
                DataType.TYPE, null, null);
        bodyBuilder.appendFormalLine(javaUtilList
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" definedPatternsList = ")
                .concat(javaUtilArrays.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(
                        ".asList(definedPatterns);")));
        bodyBuilder
                .appendFormalLine("return definedPatternsList.contains(pattern);");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName,
                JavaType.BOOLEAN_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of createList() method for ITD
     * 
     * @return
     */
    private MethodMetadata getCreateListMethod() {
        // Here we're sure that Entity.createList() method exists

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("createList");

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = getRequestMappingAttributes(RequestMethod.POST);
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypes();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> methodParamNames = getMethodParameterNames();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = getMethodBodyBuilder(PersistenceMethod.PERSIST);
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of updateList() method for ITD
     * 
     * @return
     */
    private MethodMetadata getUpdateListMethod() {
        // Here we're sure that Entity.updateList() method exists

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("updateList");

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = getRequestMappingAttributes(RequestMethod.PUT);
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypes();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> methodParamNames = getMethodParameterNames();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = getMethodBodyBuilder(PersistenceMethod.MERGE);
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of deleteList() method for ITD
     * 
     * @return
     */
    private MethodMetadata getDeleteListMethod() {
        // Here we're sure that Entity.deleteList() method exists

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("deleteList");

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = getRequestMappingAttributes(RequestMethod.DELETE);
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypes();

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> methodParamNames = getMethodParameterNames();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = getMethodBodyBuilder(PersistenceMethod.REMOVE);
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.RequestMapping"),
                requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);
        methodBuilder.setAnnotations(annotations);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of filterList(EntityList) method for ITD
     * <p>
     * The generated method removes from the passed list the objects not
     * involved in the operation.
     * </p>
     * 
     * @return
     */
    private MethodMetadata getFilterListMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("filterList");

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(formBackingType
                .getFullyQualifiedTypeName().concat(".")
                .concat(formBackingType.getSimpleTypeName()).concat("List")),
                null));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("entities"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(formBackingType.getSimpleTypeName()
                .concat("List list = new ")
                .concat(formBackingType.getSimpleTypeName()).concat("List();"));
        bodyBuilder
                .appendFormalLine("for ( Integer select : entities.getSelected() ) {");

        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if ( select != null ) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("list.getList().add(entities.getList().get(select));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();

        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return list;");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, new JavaType(
                        formBackingType.getFullyQualifiedTypeName().concat(".")
                                .concat(formBackingType.getSimpleTypeName())
                                .concat("List")), parameterTypes,
                parameterNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Generates the MethodMedata of
     * getRefererRedirectViewName(HttpServletRequest) method for ITD
     * <p>
     * The generated method redirects to the Referer URL
     * </p>
     * 
     * @return
     */
    private MethodMetadata getRefererRedirectMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName(
                "getRefererRedirectViewName");

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("httpServletRequest"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder
                .appendFormalLine("String referer = httpServletRequest.getHeader(\"Referer\");");

        JavaType stringUtils = new JavaType(
                "org.springframework.util.StringUtils");
        bodyBuilder.appendFormalLine("if (!".concat(
                stringUtils.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())).concat(
                ".hasText(referer)) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return null;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("return \"redirect:\".concat(referer);");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                parameterTypes, parameterNames, bodyBuilder);

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    /**
     * Returns the Attributes of the {@link RequestMapping} annotation for the
     * methods
     * 
     * <p>
     * Returns:<br>
     * <code>value="/list", method = RequestMethod.DELETE</code> as attributes
     * of RequestMapping annotation
     * </p>
     * 
     * @param requestMethod
     * @return
     */
    private List<AnnotationAttributeValue<?>> getRequestMappingAttributes(
            RequestMethod requestMethod) {
        // Define method annotations
        // @RequestMapping(value="/list", method = RequestMethod.DELETE)
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                new JavaSymbolName("value"), "/list"));
        requestMappingAttributes.add(new EnumAttributeValue(new JavaSymbolName(
                "method"), new EnumDetails(new JavaType(
                "org.springframework.web.bind.annotation.RequestMethod"),
                new JavaSymbolName(requestMethod.name()))));

        return requestMappingAttributes;
    }

    /**
     * Returns the list of Parameter Types for the methods
     * <p>
     * Returns a list containing:<br/>
     * <code>@Valid formBackingObjectList, BindingResult, HttpServletRequest</cod>
     * </p>
     * 
     * @return
     */
    private List<AnnotatedJavaType> getMethodParameterTypes() {
        /*
         * Define method parameter types. (@Valid formBackingObjectList,
         * BindingResult, HttpServletRequest)
         */
        List<AnnotationMetadata> methodAttributesAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttributesAnnotation = new AnnotationMetadataBuilder(
                new JavaType("javax.validation.Valid"));
        methodAttributesAnnotations.add(methodAttributesAnnotation.build());

        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(formBackingType
                .getFullyQualifiedTypeName().concat(".")
                .concat(formBackingType.getSimpleTypeName()).concat("List")),
                methodAttributesAnnotations));
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.validation.BindingResult"), null));
        /*
         * parameterTypes.add(new AnnotatedJavaType(new JavaType(
         * "org.springframework.ui.Model"), null));
         */
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        return parameterTypes;
    }

    /**
     * Returns the list of parameter names for the methods
     * <p>
     * Returns:<br/>
     * <code>entities, bindingResult, httpServletRequest</code>
     * </p>
     * 
     * @return
     */
    private List<JavaSymbolName> getMethodParameterNames() {
        // Define method parameter names (entities, bindingResult,
        // httpServletRequest)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("entities"));
        parameterNames.add(new JavaSymbolName("bindingResult"));
        // parameterNames.add(new JavaSymbolName("uiModel"));
        parameterNames.add(new JavaSymbolName("httpServletRequest"));
        return parameterNames;
    }

    /**
     * Returns the method body of the methods given a {@link PersistenceMethod}
     * <p>
     * Example:<br/>
     * <code>
     * if ( !bindingResult.hasErrors() ) {<br/>
     * &nbsp;&nbsp;Car.persist(filterList(entities));<br/>
     * }<br/>
     * return getRefererRedirectViewName(httpServletRequest);
     * </code>
     * 
     * @param persistenceMethod
     * @return
     */
    private InvocableMemberBodyBuilder getMethodBodyBuilder(
            PersistenceMethod persistenceMethod) {
        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        // test if form has errors
        bodyBuilder.appendFormalLine("if ( !bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(formBackingType
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(".")
                .concat(persistenceMethod.getName())
                .concat("(filterList(entities));"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("return getRefererRedirectViewName(httpServletRequest);");

        return bodyBuilder;
    }

    /**
     * Returns the method if exists or null otherwise. With this we assure that
     * a method is defined once in the Class
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        List<JavaType> nonAnnotatedJavaTypes = AnnotatedJavaType
                .convertFromAnnotatedJavaTypes(paramTypes);
        for (MethodMetadata methodMetadata : controllerMethods) {
            if (methodMetadata.getMethodName().equals(methodName)
                    && nonAnnotatedJavaTypes.equals(AnnotatedJavaType
                            .convertFromAnnotatedJavaTypes(methodMetadata
                                    .getParameterTypes()))) {
                return methodMetadata;
            }
        }
        return null;
    }

    public WebScaffoldMetadata getWebScaffoldMetadata() {
        return this.webScaffoldMetadata;
    }

    public SortedMap<JavaType, JavaTypeMetadataDetails> getRelatedApplicationTypeMetadata() {
        return this.relatedApplicationTypeMetadata;
    }

    public List<String> getDefinedPatterns() {
        return definedPatterns;
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
