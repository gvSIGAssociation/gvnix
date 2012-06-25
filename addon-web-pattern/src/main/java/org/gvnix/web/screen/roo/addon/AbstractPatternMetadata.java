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

import java.beans.Introspector;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gvnix.support.OperationUtils;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
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
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

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
 * @author Mario Martínez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public abstract class AbstractPatternMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaType ENTITY_BATCH_ANNOTATION = new JavaType(GvNIXEntityBatch.class.getName());

    private WebScaffoldMetadata webScaffoldMetadata;
    private SortedMap<JavaType, JavaTypeMetadataDetails> entities;
    private JavaType entity;
    private JavaType masterEntity;
    private JavaTypeMetadataDetails entityDetails;
    private JavaTypeMetadataDetails masterEntityDetails;
    private List<String> patterns;
    private SortedMap<JavaType, JavaTypeMetadataDetails> typesForPopulate;
    private Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relationsDateTypes;
    private List<MethodMetadata> controllerMethods;
    private List<FieldMetadata> controllerFields;
    private Map<JavaSymbolName, DateTimeFormatDetails> dateTypes;
    private String aspectControllerPackageFullyName;
    private MetadataService metadataService;

    public AbstractPatternMetadata(String mid, JavaType aspect, PhysicalTypeMetadata controller, WebScaffoldMetadata webScaffoldMetadata,
            WebScaffoldAnnotationValues webScaffoldValues, List<StringAttributeValue> patterns, List<MethodMetadata> controllerMethods,
            List<FieldMetadata> controllerFields, SortedMap<JavaType, JavaTypeMetadataDetails> entitiesDetails,
            SortedMap<JavaType, JavaTypeMetadataDetails> typesForPopulate, Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relationsDateTypes,
            MetadataService metadataService, PathResolver pathResolver, FileManager fileManager, Map<JavaSymbolName, DateTimeFormatDetails> dateTypes) {
    	
        super(mid, aspect, controller);
        
        // Required parameters: Web scaffold information and entities details (entity and optional master entity)
        Assert.notNull(webScaffoldMetadata, "Web scaffold metadata required");
        Assert.notNull(webScaffoldValues, "Web scaffold values required");
        Assert.notNull(entitiesDetails, "Related entities type metadata details required");
        
        if (!isValid()) {
        
        	// This metadata instance not be already produced at the time of instantiation (will retry)
            return;
        }
        
        this.webScaffoldMetadata = webScaffoldMetadata;
        this.entity = webScaffoldValues.getFormBackingObject();
        this.controllerMethods = controllerMethods;
        this.controllerFields = controllerFields;
        this.entities = entitiesDetails;
        this.entityDetails = entitiesDetails.get(entity);
        this.dateTypes = dateTypes;
        
        this.masterEntity = null;
        this.masterEntityDetails = null;
    	if (this instanceof RelatedPatternMetadata) {
    		try {
	        	// Is this a related pattern ? Then the other key is the master entity
        		SortedMap<JavaType, JavaTypeMetadataDetails> tempMap = new TreeMap<JavaType, JavaTypeMetadataDetails>(entitiesDetails);
        		tempMap.remove(entity);
	        	this.masterEntity = tempMap.lastKey();
	        	this.masterEntityDetails = tempMap.get(masterEntity);
	        } catch (NoSuchElementException e) {
	        	// This is a related pattern without master entity. Is this possible ?
			}
    	}
  
        Assert.notNull(entityDetails, "Metadata holder required for form backing type: " + entity);
        Assert.notNull(entityDetails.getPersistenceDetails(), "PersistenceMetadata details required for form backing type: " + entity);
        
        if (webScaffoldValues.isPopulateMethods()) {
        	
            filterAlreadyPopulatedTypes(typesForPopulate);
        }
        
        this.typesForPopulate = typesForPopulate;
        this.relationsDateTypes = relationsDateTypes;
        this.metadataService = metadataService;

        // TODO: Take care of attributes "create, update, delete" in RooWebScaffold annotation
        List<String> definedPatternsList = new ArrayList<String>();
        for (StringAttributeValue definedPattern : patterns) {
        	
            definedPatternsList.add(definedPattern.getValue());
        }

        this.aspectControllerPackageFullyName = aspect.getPackage().getFullyQualifiedPackageName();
        
        // Install Dialog Bean
        OperationUtils.installWebDialogClass(this.aspectControllerPackageFullyName.concat(".dialog"), pathResolver, fileManager);

        this.patterns = Collections.unmodifiableList(definedPatternsList);

        builder.addField(getDefinedPatternField());

        List<String> tabularPatterns = getPatternTypeDefined(WebPatternType.tabular, this.patterns);
        if (!tabularPatterns.isEmpty()) {
        	
            if (entityDetails.getPersistenceDetails().getFindAllMethod() == null) {
            	
            	// TODO: If no find all method, all other patterns are not generated ?
                return;
            }
            
            for (String tabularPattern : tabularPatterns) {
            	
	            builder.addMethod(getTabularMethod(tabularPattern));
            }
            
            builder.addMethod(getCreateListMethod());
            builder.addMethod(getUpdateListMethod());
            builder.addMethod(getDeleteListMethod());
            builder.addMethod(getFilterListMethod());
            builder.addMethod(getRefererRedirectMethod());
        }

        List<String> registerPatterns = getPatternTypeDefined(WebPatternType.register, this.patterns);
        if (!registerPatterns.isEmpty()) {
        	
            if (entityDetails.getPersistenceDetails().getFindEntriesMethod() == null) {
            	
            	// TODO: If no find entries method, all other patterns are not generated ?
                return;
            }
            
            for (String registerPattern : registerPatterns) {
            	
	            builder.addMethod(getRegisterMethod(registerPattern));
	            builder.addMethod(getCreateMethod(registerPattern));
	            builder.addMethod(getUpdateMethod(registerPattern, WebPatternType.register));
	            builder.addMethod(getDeleteMethod(registerPattern));
            }
            
            builder.addMethod(getRefererQueryMethod());
        }

        List<String> tabularEditPatterns = getPatternTypeDefined(WebPatternType.tabular_edit_register, this.patterns);
        if (!tabularEditPatterns.isEmpty()) {
        	
            if (entityDetails.getPersistenceDetails().getFindAllMethod() == null) {
            	
            	// TODO: If no find all method, all other patterns are not generated ?
                return;
            }
            
            for (String tabularEditPattern : tabularEditPatterns) {
            	
	            builder.addMethod(getTabularMethod(tabularEditPattern));
	            builder.addMethod(getCreateMethod(tabularEditPattern));
	            builder.addMethod(getUpdateMethod(tabularEditPattern, WebPatternType.tabular_edit_register));
	            
	            if (masterEntity != null) {
	            
	            	// Method only exists when this is a detail pattern (has master entity)
	            	builder.addMethod(getCreateFormMethod(tabularEditPattern, entitiesDetails.values()));
	            }
            }
            
            builder.addMethod(getDeleteListMethod());
            builder.addMethod(getFilterListMethod());
            builder.addMethod(getRefererRedirectMethod());
            builder.addMethod(getRefererQueryMethod());
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        new ItdSourceFileComposer(itdTypeDetails);
    }

    protected MethodMetadata getUpdateMethod(String patternName, WebPatternType patternType) {
    	
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("updatePattern" + patternName);

        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypesCreateUpdate();

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
        	
            // If it already exists, just return null and omit its generation via the ITD
            return null;
        }

        List<JavaSymbolName> methodParamNames = getMethodParameterNamesCreateUpdate();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityDetails.getPlural();

        bodyBuilder.appendFormalLine("update(".concat(
                entity.getSimpleTypeName().toLowerCase()).concat(
                ", bindingResult, uiModel, httpServletRequest);"));

        bodyBuilder.appendFormalLine("if ( bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat(
                "?\" + refererQuery(httpServletRequest);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        if (masterEntity == null) { 
	        bodyBuilder.appendFormalLine("return \"".concat("redirect:/")
	                .concat(entityNamePlural.toLowerCase())
	                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));
        }
        else {	
	        bodyBuilder.appendFormalLine("return \"".concat("redirect:/")
	                .concat(masterEntityDetails.getPlural().toLowerCase())
	                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));
        }

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder
                .setAnnotations(getRequestMappingAnnotationCreateUpdate(RequestMethod.PUT, patternName));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    protected MethodMetadata getCreateMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("createPattern" + patternName);

        List<AnnotatedJavaType> methodParamTypes = getMethodParameterTypesCreateUpdate();

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        List<JavaSymbolName> methodParamNames = getMethodParameterNamesCreateUpdate();

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityDetails.getPlural();

        bodyBuilder.appendFormalLine("create(".concat(
                entity.getSimpleTypeName().toLowerCase()).concat(
                ", bindingResult, uiModel, httpServletRequest);"));

        bodyBuilder.appendFormalLine("if ( bindingResult.hasErrors() ) {");
        bodyBuilder.indent();
        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat(
                "?\" + refererQuery(httpServletRequest);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        if (masterEntity == null) { 
	        bodyBuilder
	        .appendFormalLine("return \""
	                .concat("redirect:/")
	                .concat(entityNamePlural.toLowerCase())
	                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));
        }
        else {
	        bodyBuilder
	        .appendFormalLine("return \""
	                .concat("redirect:/")
	                .concat(masterEntityDetails.getPlural().toLowerCase())
	                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));
        }
        
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder
                .setAnnotations(getRequestMappingAnnotationCreateUpdate(RequestMethod.POST, patternName));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    protected MethodMetadata getDeleteMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("deletePattern" + patternName);

        FieldMetadata formBackingObjectIdField = entityDetails
                .getPersistenceDetails().getIdentifierField();
        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrPathVar = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPathVar.add(new StringAttributeValue(new JavaSymbolName(
                "value"), formBackingObjectIdField.getFieldName()
                .getSymbolName()));
        List<AnnotationMetadata> methodAttrPathVarAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder methodAttPathVarAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.web.bind.annotation.PathVariable"),
                reqParamAttrPathVar);
        methodAttrPathVarAnnotations.add(methodAttPathVarAnnotation.build());
        methodParamTypes.add(new AnnotatedJavaType(formBackingObjectIdField
                .getFieldType(), methodAttrPathVarAnnotations));

        List<AnnotationAttributeValue<?>> reqParamAttrPattern = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPattern.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "gvnixpattern"));
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
                "value"), "page"));
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
                "value"), "size"));
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
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        // id, pattern, page, size, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(formBackingObjectIdField.getFieldName());
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName("page"));
        methodParamNames.add(new JavaSymbolName("size"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("httpServletRequest"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityDetails.getPlural();

        bodyBuilder.appendFormalLine("delete(".concat(
                formBackingObjectIdField.getFieldName().getSymbolName())
                .concat(", page, size, uiModel);"));
        bodyBuilder
                .appendFormalLine("return \""
                        .concat("redirect:/")
                        .concat(entityNamePlural.toLowerCase())
                        .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                new JavaSymbolName("value"), "/{"
                        + formBackingObjectIdField.getFieldName()
                                .getSymbolName() + "}"));
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixpattern=" + patternName));
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
    
	/**
	 * @see org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.getCreateFormMethod
	 * 
	 * @param patternName
	 * @param dependentTypes
	 * @return
	 */
	protected MethodMetadata getCreateFormMethod(String patternName, Collection<JavaTypeMetadataDetails> dependentTypes) {
		
        Assert.notNull(masterEntity, "Master entity required to generate createForm");
        Assert.notNull(masterEntityDetails, "Master entity metadata required to generate createForm");
		
		JavaSymbolName methodName = new JavaSymbolName("createForm" + patternName);

		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		List<AnnotationMetadata> patternAnnotations = new ArrayList<AnnotationMetadata>();
		AnnotationMetadataBuilder patternRequestParam = new AnnotationMetadataBuilder(new JavaType("org.springframework.web.bind.annotation.RequestParam"));
		patternRequestParam.addStringAttribute("value", "gvnixpattern");
		patternRequestParam.addBooleanAttribute("required", true);
		patternAnnotations.add(patternRequestParam.build());
		paramTypes.add(new AnnotatedJavaType(new JavaType("java.lang.String"), patternAnnotations));
		List<AnnotationMetadata> referenceAnnotations = new ArrayList<AnnotationMetadata>();
		AnnotationMetadataBuilder referenceRequestParam = new AnnotationMetadataBuilder(new JavaType("org.springframework.web.bind.annotation.RequestParam"));
		referenceRequestParam.addStringAttribute("value", "gvnixreference");
		referenceRequestParam.addBooleanAttribute("required", true);
		referenceAnnotations.add(referenceRequestParam.build());
		paramTypes.add(new AnnotatedJavaType(new JavaType(masterEntityDetails.getPersistenceDetails().getIdentifierField().getFieldType().getFullyQualifiedTypeName()), referenceAnnotations));
		paramTypes.add(new AnnotatedJavaType(new JavaType("org.springframework.ui.Model"), null));
		
		MethodMetadata method = methodExists(methodName, paramTypes);
		if (method != null) {
			return null;
		}

		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
		paramNames.add(new JavaSymbolName("gvnixpattern"));
		paramNames.add(new JavaSymbolName("gvnixreference"));
		paramNames.add(new JavaSymbolName("uiModel"));

		List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		List<StringAttributeValue> values = new ArrayList<StringAttributeValue>();
		values.add(new StringAttributeValue(new JavaSymbolName("value"), "form"));
		values.add(new StringAttributeValue(new JavaSymbolName("value"), "gvnixpattern=" + patternName));
		values.add(new StringAttributeValue(new JavaSymbolName("value"), "gvnixreference"));
		requestMappingAttributes.add(new ArrayAttributeValue<StringAttributeValue>(new JavaSymbolName("params"), values));
		requestMappingAttributes.add(new EnumAttributeValue(new JavaSymbolName("method"), new EnumDetails(new JavaType("org.springframework.web.bind.annotation.RequestMethod"), new JavaSymbolName("GET"))));
		AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(new JavaType("org.springframework.web.bind.annotation.RequestMapping"), requestMappingAttributes);
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations.add(requestMapping);

		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine(masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + " " + masterEntity.getSimpleTypeName().toLowerCase() + " = " + masterEntity.getSimpleTypeName() + "." + masterEntityDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
		bodyBuilder.appendFormalLine(entity.getSimpleTypeName() + " " + entity.getSimpleTypeName().toLowerCase() + " = new " + entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
		// TODO Validate if detail pattern field is referencing entity pattern primary key field 
		bodyBuilder.appendFormalLine(entity.getSimpleTypeName().toLowerCase() + ".set" + masterEntity.getSimpleTypeName() + "(" + masterEntity.getSimpleTypeName().toLowerCase() + ");");
		// Add attribute with identical name as required by Roo create page
		bodyBuilder.appendFormalLine("uiModel.addAttribute(\"" + uncapitalize(entity.getSimpleTypeName()) + "\", " + entity.getSimpleTypeName().toLowerCase() + ");");
		if (!dateTypes.isEmpty()) {
			bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
		}
		// TODO Remove dependencies or add it from entity pattern ?
//		boolean listAdded = false;
//		for (JavaTypeMetadataDetails dependentType: dependentTypes) {
//			if (dependentType.getPersistenceDetails().getCountMethod() == null) {
//				continue;
//			}
//			if (!listAdded) {
//				String listShort = new JavaType("java.util.List").getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver());
//				String arrayListShort = new JavaType("java.util.ArrayList").getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver());
//				bodyBuilder.appendFormalLine(listShort + " dependencies = new " + arrayListShort + "();");
//				listAdded = true;
//			}
//			bodyBuilder.appendFormalLine("if (" + dependentType.getJavaType().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "." + dependentType.getPersistenceDetails().getCountMethod().getMethodName().getSymbolName() + "() == 0) {");
//			bodyBuilder.indent();
//			// Adding string array which has the fieldName at position 0 and the path at position 1
//			bodyBuilder.appendFormalLine("dependencies.add(new String[]{\"" + dependentType.getJavaType().getSimpleTypeName().toLowerCase() + "\", \"" + dependentType.getPlural().toLowerCase() + "\"});");
//			bodyBuilder.indentRemove();
//			bodyBuilder.appendFormalLine("}");
//		}
//		if (listAdded) {
//			bodyBuilder.appendFormalLine("uiModel.addAttribute(\"dependencies\", dependencies);");
//		}
		bodyBuilder.appendFormalLine("return \"" + webScaffoldMetadata.getAnnotationValues().getPath() + "/create\";");

		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT, paramTypes, paramNames, bodyBuilder);
		methodBuilder.setAnnotations(annotations);
		return methodBuilder.build();
	}

    /**
     * Get refererQuery method.
     * 
     * <p>Get referer url from request header and return query part without "form" parameter.
     * Remove "form" parameter is required to avoid mapping with create and update Roo patterns.</p>
     * 
     * @return Referer url query part without "form" param
     */
    protected MethodMetadata getRefererQueryMethod() {
    	
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("refererQuery");

        // Define method parameter types
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("httpServletRequest"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("String url = \"\";");
        bodyBuilder
                .appendFormalLine("String referer = httpServletRequest.getHeader(\"Referer\");");

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
        bodyBuilder.appendFormalLine("String[] params = new "
        		.concat(netURL.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()))
        		.concat("(referer).getQuery().split(\"&\");"));
        bodyBuilder.appendFormalLine("for (String param : params) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("if (!param.equals(\"form\")) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("url = url.concat(param).concat(\"&\");");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("if (url.endsWith(\"&\")) { url = url.substring(0, url.length() - 1); }");
        bodyBuilder.indentRemove();
        JavaType exception = new JavaType("java.lang.Exception");
        bodyBuilder.appendFormalLine("} catch ( ".concat(
                exception.getNameIncludingTypeParameters(false,
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
    
    protected List<AnnotationMetadataBuilder> getRequestMappingAnnotationCreateUpdate(
            RequestMethod requestMethod, String patternName) {
        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixpattern=" + patternName));
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

    protected List<AnnotatedJavaType> getMethodParameterTypesCreateUpdate() {
        // Define method parameter types
        // @RequestParam(value = "gvnixpattern", required = true) String
        // pattern, @Valid Owner owner, BindingResult bindingResult,
        // HttpServletRequest req, Model uiModel)
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();

        List<AnnotationAttributeValue<?>> reqParamAttrPattern = new ArrayList<AnnotationAttributeValue<?>>();
        reqParamAttrPattern.add(new StringAttributeValue(new JavaSymbolName(
                "value"), "gvnixpattern"));
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
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(entity
                .getFullyQualifiedTypeName()), methodAttrValidAnnotations));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.validation.BindingResult"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.ui.Model"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest"), null));
        return methodParamTypes;
    }

    protected List<JavaSymbolName> getMethodParameterNamesCreateUpdate() {
        // Define method parameter names
        // pattern, entity, bindingResult, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName(entity
                .getSimpleTypeName().toLowerCase()));
        methodParamNames.add(new JavaSymbolName("bindingResult"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("httpServletRequest"));
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
     * If there is patterns of given WebPatternType defined in GvNIXPattern it
     * returns its names, empty list otherwise
     * 
     * @param patternType
     * @param definedPatternsList
     * @return Some type pattern names list
     */
    protected List<String> getPatternTypeDefined(WebPatternType patternType,
            List<String> definedPatternsList) {
    	List<String> patternList = new ArrayList<String>();
        for (String definedPattern : definedPatternsList) {
            if (definedPattern.split("=")[1].equalsIgnoreCase(patternType
                    .name())) {
                patternList.add(definedPattern.split("=")[0]);
            }
        }
        return patternList;
    }

    /**
     * Update the annotations in formBackingType entity adding
     * {@link GvNIXBatchEntity}.
     * <p>
     * The method is <code>unused</code> until we find a way to invoke it
     * without get any issues
     */
    protected void annotateFormBackingObject() {

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
                entity, Path.SRC_MAIN_JAVA);
        if (formBackingTypeId == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + entity.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(formBackingTypeId, true);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type ".concat(entity
                        .getFullyQualifiedTypeName()));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type "
                        .concat(entity.getFullyQualifiedTypeName()));

        // Test if the type is an MutableClassOrInterfaceTypeDetails instance so
        // the annotation can be added
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        .concat(entity.getFullyQualifiedTypeName()));
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;
        return mutableTypeDetails;
    }

    protected FieldMetadataBuilder getGvNIXPatternField() {
        return new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                | Modifier.STATIC | Modifier.FINAL, new JavaSymbolName(
                "PATTERN_PARAM_NAME"), JavaType.STRING_OBJECT,
                "\"gvnixpattern\"");
    }

    protected FieldMetadata getDefinedPatternField() {
        String definedPatternIds = "";
        for (String definedPattern : patterns) {
            if (definedPatternIds.length() > 0) {
                definedPatternIds = definedPatternIds.concat(", ");
            }
            definedPatternIds = definedPatternIds.concat("\"")
                    .concat(definedPattern.split("=")[0]).concat("\"");
        }

        FieldMetadataBuilder fmb = null;

        JavaSymbolName fieldName = new JavaSymbolName("patterns");
        JavaType stringArray = new JavaType(
                JavaType.STRING_OBJECT.getFullyQualifiedTypeName(), 1,
                DataType.TYPE, null, null);

        FieldMetadata field = fieldExists(fieldName, stringArray);
        if (field != null) {
            String initializer = field.getFieldInitializer();
            initializer = initializer.replace("{", "").replace("}", "")
                    .replace(" ", "").replace("\"", "");
            for (String pattern : initializer.split(",")) {
                if (!definedPatternIds.contains(pattern)) {
                    if (definedPatternIds.length() > 0) {
                        definedPatternIds = definedPatternIds.concat(", ");
                    }
                    definedPatternIds = definedPatternIds.concat("\"")
                            .concat(pattern).concat("\"");
                }
            }
            fmb = new FieldMetadataBuilder(field);
            fmb.setFieldInitializer("{ ".concat(definedPatternIds).concat(" }"));
        }

        if (fmb == null) {
            fmb = new FieldMetadataBuilder(getId(), Modifier.PRIVATE
                    | Modifier.STATIC, fieldName, stringArray, "{ ".concat(
                    definedPatternIds).concat(" }"));
        }

        field = fmb.build();
        controllerFields.add(field);
        return field;
    }

    private FieldMetadata fieldExists(JavaSymbolName fieldName,
            JavaType fieldType) {
        for (FieldMetadata fieldMetadata : controllerFields) {
            if (fieldMetadata.getFieldName().equals(fieldName)
                    && fieldType.equals(fieldMetadata.getFieldType())) {
                return fieldMetadata;
            }
        }
        return null;
    }

    protected void addStaticFields() {
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
    protected MethodMetadata getTabularMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("tabular" + patternName);

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
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        // pattern, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName("uiModel"));
        methodParamNames.add(new JavaSymbolName("httpServletRequest"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityDetails.getPlural();

        // XXX: Following code generates the part of this method validating
        // defined patterns against a request. By now, we don't want to validate
        // them

        // bodyBuilder.appendFormalLine("if ( !isPatternDefined(pattern) ) {");
        // bodyBuilder.indent();
        // bodyBuilder.appendFormalLine("return \"redirect:/".concat(
        // entityNamePlural.toLowerCase()).concat("\";"));
        // bodyBuilder.indentRemove();
        // bodyBuilder.appendFormalLine("}");

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(entity);

        // Add date validation pattern to model if some date type field exists
        if (!dateTypes.isEmpty()) {
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
        }

        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        bodyBuilder.appendFormalLine(javaUtilList
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" ")
                .concat(entityNamePlural.toLowerCase())
                .concat(" = ")
                .concat(entity.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()))
                .concat(".")
                .concat(entityDetails.getPersistenceDetails()
                        .getFindAllMethod().getMethodName().getSymbolName()
                        .concat("();")));

        bodyBuilder.appendFormalLine("if (".concat(
                entityNamePlural.toLowerCase()).concat(".isEmpty()) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                entityNamePlural.toLowerCase()).concat("Tab\", null);"));

        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Info,
                "message_entitynotfound_problemdescription");

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // May we need to populate some Model Attributes with the data of
        // related entities
        addBodyLinesPopulatingRelatedEntitiesData(bodyBuilder);
        // for (JavaType type : typesForPopulate.keySet()) {
        // JavaTypeMetadataDetails javaTypeMd = typesForPopulate.get(type);
        // JavaTypePersistenceMetadataDetails javaTypePersistenceMd = javaTypeMd
        // .getPersistenceDetails();
        // if (javaTypePersistenceMd != null
        // && javaTypePersistenceMd.getFindAllMethod() != null) {
        // bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
        // .concat(javaTypeMd.getPlural().toLowerCase())
        // .concat("\", ")
        // .concat(type.getNameIncludingTypeParameters(false,
        // builder.getImportRegistrationResolver()))
        // .concat(".")
        // .concat(javaTypePersistenceMd.getFindAllMethod()
        // .getMethodName().getSymbolName())
        // .concat("());"));
        // } else if (javaTypeMd.isEnumType()) {
        // JavaType arrays = new JavaType("java.util.Arrays");
        // bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
        // .concat(javaTypeMd.getPlural().toLowerCase())
        // .concat("\", ")
        // .concat(arrays.getNameIncludingTypeParameters(false,
        // builder.getImportRegistrationResolver()))
        // .concat(".asList(")
        // .concat(type.getNameIncludingTypeParameters(false,
        // builder.getImportRegistrationResolver()))
        // .concat(".class.getEnumConstants()));"));
        // }
        // }

        bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                .concat(entityNamePlural.toLowerCase()).concat("Tab\", ")
                .concat(entityNamePlural.toLowerCase()).concat(");"));
        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixpattern=" + patternName));
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "!form"));
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
        return method;
    }

    protected MethodMetadata getRegisterMethod(String patternName) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("register" + patternName);

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
                "javax.servlet.http.HttpServletRequest"), null));
        methodParamTypes.add(new AnnotatedJavaType(new JavaType(
                "org.springframework.ui.Model"), null));

        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        // pattern, uiModel, httpServletRequest
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        methodParamNames.add(new JavaSymbolName("index"));
        methodParamNames.add(new JavaSymbolName("pattern"));
        methodParamNames.add(new JavaSymbolName("httpServletRequest"));
        methodParamNames.add(new JavaSymbolName("uiModel"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityDetails.getPlural();
        String entityName = uncapitalize(entity.getSimpleTypeName());

        // XXX: Following code generates the part of this method validating
        // defined patterns against a request. By now, we don't want to validate
        // them

        // bodyBuilder.appendFormalLine("if ( !isPatternDefined(pattern) ) {");
        // bodyBuilder.indent();
        // bodyBuilder.appendFormalLine("return \"redirect:/".concat(
        // entityNamePlural.toLowerCase()).concat("\";"));
        // bodyBuilder.indentRemove();
        // bodyBuilder.appendFormalLine("}");

        List<JavaType> typeParams = new ArrayList<JavaType>();
        typeParams.add(entity);
        
        // Add date validation pattern to model if some date type field exists
        if (!dateTypes.isEmpty()) {
            bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
        }
        
        JavaType javaUtilList = new JavaType("java.util.List", 0,
                DataType.TYPE, null, typeParams);
        bodyBuilder
                .appendFormalLine(javaUtilList
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(" ")
                        .concat(entityNamePlural.toLowerCase())
                        .concat(" = ")
                        .concat(entity.getNameIncludingTypeParameters(
                                false, builder.getImportRegistrationResolver()))
                        .concat(".")
                        .concat(entityDetails
                                .getPersistenceDetails()
                                .getFindEntriesMethod()
                                .getMethodName()
                                .getSymbolName()
                                .concat("(index == null ? 0 : (index.intValue() - 1), 1);")));

        bodyBuilder.appendFormalLine("if (".concat(
                entityNamePlural.toLowerCase()).concat(".isEmpty()) {"));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                entityName.toLowerCase()).concat("\", null);"));

        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Info,
                "message_entitynotfound_problemdescription");

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine(entity.getSimpleTypeName()
                .concat(" ").concat(entityName.toLowerCase()).concat(" = ")
                .concat(entityNamePlural.toLowerCase()).concat(".get(0);"));

        bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                .concat(entityName.toLowerCase()).concat("\", ")
                .concat(entityName.toLowerCase()).concat(");"));

        bodyBuilder.appendFormalLine(JavaType.LONG_PRIMITIVE
                .getNameIncludingTypeParameters()
                .concat(" count = ")
                .concat(entity.getSimpleTypeName())
                .concat(".")
                .concat(entityDetails.getPersistenceDetails()
                        .getCountMethod().getMethodName().getSymbolName())
                .concat("();"));
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"maxEntities"
                .concat("\", count == 0 ? 1 : count);"));

        // May we need to populate some Model Attributes with the data of
        // related entities
        addBodyLinesPopulatingRelatedEntitiesData(bodyBuilder);

        // Add date validation pattern to model if some date type field exists
        addBodyLinesRegisteringRelatedEntitiesDateTypesFormat(bodyBuilder);

        bodyBuilder.appendFormalLine("return \"".concat(
                entityNamePlural.toLowerCase()).concat("/\".concat(pattern);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT,
                methodParamTypes, methodParamNames, bodyBuilder);

        // Get Method RequestMapping annotation
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<AnnotationAttributeValue<? extends Object>> paramValues = new ArrayList<AnnotationAttributeValue<? extends Object>>();
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixpattern=" + patternName));
        paramValues.add(new StringAttributeValue(new JavaSymbolName("ignored"),
                "gvnixform"));
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
        return method;
    }

    /**
     * Adds body lines which populates required data for the related entities.
     * <p>
     * This lines are like Roo's ModelAttribute methods but registering the
     * model attributes directly in the method. The model attributes are set
     * with data (usually Sets) needed by the entity related entities.
     * 
     * @param bodyBuilder
     */
    private void addBodyLinesPopulatingRelatedEntitiesData(
            InvocableMemberBodyBuilder bodyBuilder) {
        for (JavaType type : typesForPopulate.keySet()) {
            JavaTypeMetadataDetails javaTypeMd = typesForPopulate.get(type);
            JavaTypePersistenceMetadataDetails javaTypePersistenceMd = javaTypeMd
                    .getPersistenceDetails();
            if (javaTypePersistenceMd != null
                    && javaTypePersistenceMd.getFindAllMethod() != null) {
                bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                        .concat(javaTypeMd.getPlural().toLowerCase())
                        .concat("\", ")
                        .concat(type.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".")
                        .concat(javaTypePersistenceMd.getFindAllMethod()
                                .getMethodName().getSymbolName())
                        .concat("());"));
            } else if (javaTypeMd.isEnumType()) {
                JavaType arrays = new JavaType("java.util.Arrays");
                bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                        .concat(javaTypeMd.getPlural().toLowerCase())
                        .concat("\", ")
                        .concat(arrays.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".asList(")
                        .concat(type.getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver()))
                        .concat(".class.getEnumConstants()));"));
            }
        }
    }

    /**
     * Adds body lines registering DateTime formats of the related entities.
     * <p>
     * If related entity has DateTime fields, we need to register, as model
     * attribute, the DateTime format in order to render, validate and send
     * those fields in the right format. This is similar to the Roo's
     * DateTimeFormatPatterns helper method.
     * 
     * @param bodyBuilder
     */
    private void addBodyLinesRegisteringRelatedEntitiesDateTypesFormat(
            InvocableMemberBodyBuilder bodyBuilder) {
        for (Entry<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> javaTypeDateTimeFormatDetailsEntry : relationsDateTypes
                .entrySet()) {

            String relatedEntityName = uncapitalize(javaTypeDateTimeFormatDetailsEntry
                    .getKey().getSimpleTypeName());
            for (Entry<JavaSymbolName, DateTimeFormatDetails> javaSymbolNameDateTimeFormatDetailsEntry : javaTypeDateTimeFormatDetailsEntry
                    .getValue().entrySet()) {

                String pattern;
                if (javaSymbolNameDateTimeFormatDetailsEntry.getValue().pattern != null) {
                    pattern = "\""
                            + javaSymbolNameDateTimeFormatDetailsEntry
                                    .getValue().pattern + "\"";
                } else {
                    JavaType dateTimeFormat = new JavaType(
                            "org.joda.time.format.DateTimeFormat");
                    String dateTimeFormatSimple = dateTimeFormat
                            .getNameIncludingTypeParameters(false,
                                    builder.getImportRegistrationResolver());
                    JavaType localeContextHolder = new JavaType(
                            "org.springframework.context.i18n.LocaleContextHolder");
                    String localeContextHolderSimple = localeContextHolder
                            .getNameIncludingTypeParameters(false,
                                    builder.getImportRegistrationResolver());
                    pattern = dateTimeFormatSimple
                            + ".patternForStyle(\""
                            + javaSymbolNameDateTimeFormatDetailsEntry
                                    .getValue().style + "\", "
                            + localeContextHolderSimple + ".getLocale())";
                }
                bodyBuilder.appendFormalLine("uiModel.addAttribute(\""
                        + relatedEntityName
                        + "_"
                        + javaSymbolNameDateTimeFormatDetailsEntry.getKey()
                                .getSymbolName().toLowerCase()
                        + "_date_format\", " + pattern + ");");
            }
        }
    }

    enum DialogType {
        Error, Info, Alert, Suggest;
    }

    /**
     * Using the given bodyBuilder adds code lines for set a Session Attribute
     * with an instance of Dialog bean
     * 
     * @param bodyBuilder
     * @param dialogType
     * @param messageDescriptionCode
     */
    private void addBodyLinesForDialogMessage(
            InvocableMemberBodyBuilder bodyBuilder, DialogType dialogType,
            String messageDescriptionCode) {
        JavaType httpSession = new JavaType("javax.servlet.http.HttpSession");
        bodyBuilder.appendFormalLine(httpSession
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(
                        " session = httpServletRequest.getSession();"));
        JavaType dialogJavaType = new JavaType(
                this.aspectControllerPackageFullyName.concat(".dialog.Dialog"));
        JavaType dialogTypeJavaType = new JavaType(dialogJavaType
                .getFullyQualifiedTypeName().concat(".DialogType"));
        bodyBuilder.appendFormalLine(dialogJavaType
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver())
                .concat(" dialog = new Dialog(")
                .concat(dialogTypeJavaType
                        .getNameIncludingTypeParameters(false,
                                builder.getImportRegistrationResolver())
                        .concat(".").concat(dialogType.name())
                        .concat(", \"message_")
                        .concat(dialogType.name().toLowerCase())
                        .concat("_title\", \"").concat(messageDescriptionCode)
                        .concat("\");")));
        bodyBuilder
                .appendFormalLine("session.setAttribute(\"dialogMessage\", dialog);");
    }

    protected MethodMetadata getIsPatternDefinedMethod() {
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

        // List<String> definedPatternsList = Arrays.asList(patterns);
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
                        ".asList(patterns);")));
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
    protected MethodMetadata getCreateListMethod() {
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
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
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
    protected MethodMetadata getUpdateListMethod() {
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
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
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
    protected MethodMetadata getDeleteListMethod() {
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
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
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
    protected MethodMetadata getFilterListMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("filterList");

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(entity
                .getFullyQualifiedTypeName().concat(".")
                .concat(entity.getSimpleTypeName()).concat("List")),
                null));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata method = methodExists(methodName, parameterTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("entities"));

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(entity.getSimpleTypeName()
                .concat("List list = new ")
                .concat(entity.getSimpleTypeName()).concat("List();"));
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
                        entity.getFullyQualifiedTypeName().concat(".")
                                .concat(entity.getSimpleTypeName())
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
    protected MethodMetadata getRefererRedirectMethod() {
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
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
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
        parameterTypes.add(new AnnotatedJavaType(new JavaType(entity
                .getFullyQualifiedTypeName().concat(".")
                .concat(entity.getSimpleTypeName()).concat("List")),
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
        bodyBuilder.appendFormalLine(entity
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()).concat(".")
                .concat(persistenceMethod.getName())
                .concat("(filterList(entities));"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("return getRefererRedirectViewName(httpServletRequest);");

        return bodyBuilder;
    }

    /**
     * Remove from <code>typesForPopulate</code> those types that are being
     * returned in any other populate method (these methods have ModelAttribute
     * method annotation)
     * 
     * @param typesForPopulate
     */
    private void filterAlreadyPopulatedTypes(
            SortedMap<JavaType, JavaTypeMetadataDetails> typesForPopulate) {

        Set<JavaType> keyTypesForPopulate = typesForPopulate.keySet();
        if (keyTypesForPopulate.isEmpty()) {
            return;
        }

        for (MethodMetadata method : controllerMethods) {
            JavaType returnType = method.getReturnType();
            if (returnType.isCommonCollectionType()) {
                for (JavaType genericType : returnType.getParameters()) {
                    if (typesForPopulate.keySet().contains(genericType)) {
                        typesForPopulate.remove(genericType);
                    }
                }
            } else if (typesForPopulate.keySet().contains(returnType)) {
                typesForPopulate.remove(returnType);
            }
        }
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

    protected String uncapitalize(String term) {
        // [ROO-1790] this is needed to adhere to the JavaBean naming
        // conventions (see JavaBean spec section 8.8)
        return Introspector.decapitalize(StringUtils.capitalize(term));
    }

    public WebScaffoldMetadata getWebScaffoldMetadata() {
        return this.webScaffoldMetadata;
    }

    public SortedMap<JavaType, JavaTypeMetadataDetails> getRelatedApplicationTypeMetadata() {
        return this.entities;
    }

    public List<String> getDefinedPatterns() {
        return patterns;
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
}
