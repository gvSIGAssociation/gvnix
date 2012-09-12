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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
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
public class RelatedPatternMetadata extends AbstractPatternMetadata {

    private static final String PROVIDES_TYPE_STRING = RelatedPatternMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);
    
    private MemberDetails entityDetails;
    private MemberDetails masterEntityDetails;
    private JavaType masterEntity;
    private JavaTypeMetadataDetails masterEntityJavaDetails;
 
    public RelatedPatternMetadata(String mid, JavaType aspect, PhysicalTypeMetadata controllerMetadata, MemberDetails controllerDetails,
    		WebScaffoldMetadata webScaffoldMetadata, List<StringAttributeValue> patterns, PhysicalTypeMetadata entityMetadata, MemberDetails entityDetails,
    		JavaTypeMetadataDetails masterEntityJavaDetails, MemberDetails masterEntityDetails,
    		SortedMap<JavaType, JavaTypeMetadataDetails> relatedEntities, SortedMap<JavaType, JavaTypeMetadataDetails> relatedFields,
    		Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedDates, Map<JavaSymbolName, DateTimeFormatDetails> entityDateTypes) {
    	
        super(mid, aspect, controllerMetadata, controllerDetails, webScaffoldMetadata, patterns, entityMetadata, 
        		relatedEntities, relatedFields, relatedDates, entityDateTypes);

        if (!isValid()) {
        
        	// This metadata instance not be already produced at the time of instantiation (will retry)
            return;
        }
        
        this.entityDetails = entityDetails;
        this.masterEntityDetails = masterEntityDetails;
        this.masterEntityJavaDetails = masterEntityJavaDetails;
        if (masterEntityDetails != null) {
        	this.masterEntity = masterEntityJavaDetails.getJavaType();
        }

        List<String> registerPatterns = getPatternTypeDefined(WebPatternType.register, this.patterns);
        if (!registerPatterns.isEmpty()) {
        	
        	// TODO findEntries method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails().getFindEntriesMethod() == null) {
            	
            	// TODO: If no find entries method, all other patterns are not generated ?
                return;
            }
            
            for (String registerPattern : registerPatterns) {
            	
	            builder.addMethod(getCreateMethod(registerPattern));
	            builder.addMethod(getUpdateMethod(registerPattern));
            }
        }
        
        List<String> tabularEditPatterns = getPatternTypeDefined(WebPatternType.tabular_edit_register, this.patterns);
        if (!tabularEditPatterns.isEmpty()) {

        	// TODO findAll method required on this pattern ?
            if (entityTypeDetails.getPersistenceDetails().getFindAllMethod() == null) {
            	
            	// TODO: If no find all method, all other patterns are not generated ?
                return;
            }
            
            for (String tabularEditPattern : tabularEditPatterns) {
            	
            	// Method only exists when this is a detail pattern (has master entity)
            	builder.addMethod(getCreateFormMethod(tabularEditPattern));
	            builder.addMethod(getCreateMethod(tabularEditPattern));
	            builder.addMethod(getUpdateMethod(tabularEditPattern));
            }
        }
        
        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        new ItdSourceFileComposer(itdTypeDetails);
    }

	/**
	 * @see org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.getCreateFormMethod
	 * 
	 * @param patternName
	 * @return
	 */
	protected MethodMetadata getCreateFormMethod(String patternName) {
		
		Validate.notNull(masterEntity, "Master entity required to generate createForm");
		Validate.notNull(masterEntityJavaDetails, "Master entity metadata required to generate createForm");
		
		JavaSymbolName methodName = new JavaSymbolName("createForm" + patternName);
		
		// Create method params: annotation, type and name

		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();

		// @RequestParam(value = "gvnixpattern", required = true) String gvnixpattern
		Entry<JavaSymbolName, AnnotatedJavaType> gvnixpatternParam = getPatternRequestParam(true);
		paramNames.add(gvnixpatternParam.getKey());
		paramTypes.add(gvnixpatternParam.getValue());
		
		// @RequestParam(value = "gvnixreference", required = true) MasterEntityIdType gvnixreference
		Entry<JavaSymbolName, AnnotatedJavaType> gvnixreferenceParam = getReferenceRequestParam();
		paramNames.add(gvnixreferenceParam.getKey());
		paramTypes.add(gvnixreferenceParam.getValue());

		// Model uiModel
		Entry<JavaSymbolName, AnnotatedJavaType> modelParam = getModelRequestParam();
		paramNames.add(modelParam.getKey());
		paramTypes.add(modelParam.getValue());

		// Create method annotation
		
		List<AnnotationMetadataBuilder> methodAnnotations = new ArrayList<AnnotationMetadataBuilder>();

		// @RequestMapping(params = { "form", "gvnixpattern=AplicacionListados2", "gvnixreference" }, method = RequestMethod.GET)
		methodAnnotations.add(getRequestMapping(patternName, RequestMethod.GET));

		// If method exists (in java file, by example) no create it in AspectJ file
		if (methodExists(methodName, paramTypes) != null) {
			return null;
		}
		
		// Create method body
		
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

		String entityName = entity.getSimpleTypeName();

		appendMasterRelationToEntity(bodyBuilder);
		
		/*
		 * uiModel.addAttribute("entityName", entityname);
		 * addDateTimeFormatPatterns(uiModel);  // Only if date types exists
		 * return "entitynames/create";
		 */
		
		// Add attribute with identical name as required by Roo create page
		bodyBuilder.appendFormalLine(
				"uiModel.addAttribute(\"" + uncapitalize(entityName) + "\", " + entityName.toLowerCase() + ");");
		if (!entityDateTypes.isEmpty()) {
			
			bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
		}
		
		bodyBuilder.appendFormalLine("return \"" + webScaffoldMetadata.getAnnotationValues().getPath() + "/create\";");

		// TODO Remove dependencies or add it from entity pattern ?

		MethodMetadataBuilder method = new MethodMetadataBuilder(
				getId(), Modifier.PUBLIC, methodName, JavaType.STRING, paramTypes, paramNames, bodyBuilder);
		method.setAnnotations(methodAnnotations);
		
		return method.build();
	}

	protected void appendMasterRelationToEntity(InvocableMemberBodyBuilder bodyBuilder) {
		
		String masterEntityName = masterEntity.getSimpleTypeName();
		String entityName = entity.getSimpleTypeName();

		// Get field from entity related with some master entity defined into the fields names list
		FieldMetadata relationField = getFieldRelationMasterEntity();
		
		// TODO Unify next 3 cases code
        if (relationField == null) {
        	
        	// TODO This case is already required ? 
        	
    		/*
    		 * MasterEntityName masterentityname = MasterEntityName.findMasterEntityName(gvnixreference);
    		 * EntityName entityname = new EntityName();
    		 * entityname.setMasterEntityName(masterentityname);
    		 */
        	
    		bodyBuilder.appendFormalLine(
    				masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + 
    				" " + masterEntityName.toLowerCase() + " = " + masterEntityName + "." + 
    						masterEntityJavaDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
    		bodyBuilder.appendFormalLine(entityName + " " + entityName.toLowerCase() + " = new " + 
    						entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
    		bodyBuilder.appendFormalLine(
    				entityName.toLowerCase() + ".set" + masterEntityName + "(" + masterEntityName.toLowerCase() + ");");
        }
        else if (entityTypeDetails.getPersistenceDetails().getRooIdentifierFields().contains(relationField)) {
        	
    		/*
    		 * EntityPK entitypk = new EntityPK(null, ... gvnixreference, ... null);
    		 * EntityName entityname = new EntityName();
    		 * entity.setId(entitypk);
             */
        	
            // TODO When field metadata in composite roo identifier: use PK constructor
        	StringBuilder tmpBody = new StringBuilder();
        	tmpBody.append(entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getNameIncludingTypeParameters(
        			false, builder.getImportRegistrationResolver()) + 
        			" " + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getSimpleTypeName().toLowerCase() +
        			" = new " + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getNameIncludingTypeParameters(
        					false, builder.getImportRegistrationResolver()) + "(");
        	Iterator<FieldMetadata> fields = entityTypeDetails.getPersistenceDetails().getRooIdentifierFields().iterator();
        	while (fields.hasNext()) {
        		FieldMetadata field = fields.next();
        		if (field.getFieldName().equals(relationField.getFieldName())) {
        			tmpBody.append("gvnixreference");	
        		}
        		else {
        			tmpBody.append("null");
        		}
        		if (fields.hasNext()) {
        			tmpBody.append(", ");
        		}
			}
        	tmpBody.append(");");
        	bodyBuilder.appendFormalLine(tmpBody.toString());

    		bodyBuilder.appendFormalLine(entityName + " " + entityName.toLowerCase() + " = new " + 
					entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
    		bodyBuilder.appendFormalLine(entityName.toLowerCase() + ".set" + 
					entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldName().getSymbolNameCapitalisedFirstLetter() +
					"(" + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getSimpleTypeName().toLowerCase() + ");");
        }
        else {
        	
    		/*
    		 * MasterEntityName masterentityname = MasterEntityName.findMasterEntityName(gvnixreference);
    		 * EntityName entityname = new EntityName();
    		 * entityname.setRelationFieldName(masterentityname);
    		 */
        	
    		bodyBuilder.appendFormalLine(
    				masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + 
    				" " + masterEntityName.toLowerCase() + " = " + masterEntityName + "." + 
    						masterEntityJavaDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
    		bodyBuilder.appendFormalLine(entityName + " " + entityName.toLowerCase() + " = new " + 
    						entity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "();");
			bodyBuilder.appendFormalLine(
					entityName.toLowerCase() + ".set" + relationField.getFieldName().getSymbolNameCapitalisedFirstLetter() + "(" + masterEntityName.toLowerCase() + ");");
        }
	}

    protected MethodMetadata getCreateMethod(String patternName) {
    	
    	// TODO Some code duplicated with same method in PatternMetadata
    	
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("createPattern" + patternName);

        // @RequestParam(value = "gvnixpattern", required = true) String gvnixpattern, @Valid Owner owner, BindingResult bindingResult, HttpServletRequest req, Model uiModel)
        
        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();
        
        getRequestParam(methodParamNames, methodParamTypes);
        
        Entry<JavaSymbolName, AnnotatedJavaType> referenceRequestParam = getReferenceRequestParam();
        methodParamNames.add(referenceRequestParam.getKey());
        methodParamTypes.add(referenceRequestParam.getValue());
        
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
            // If it already exists, just return null and omit its
            // generation via the ITD
            return null;
        }

        // Create method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        String entityNamePlural = entityTypeDetails.getPlural();

        // TODO Set master pattern reference into this entity if master has composite PK
        
        // TODO Unify with method appendMasterRelationToEntity
        
		/*
		 * MasterEntityName masterentityname = MasterEntityName.findMasterEntityName(gvnixreference);
		 * 		EntityName entityname = new EntityName();
		 * entityname.setMasterEntityName(masterentityname);
		 * 		uiModel.addAttribute("entityName", entityname);
		 * 		addDateTimeFormatPatterns(uiModel);  // Only if date types exists
		 * 		return "entitynames/create";
		 */
		String masterEntityName = masterEntity.getSimpleTypeName();
		String entityName = entity.getSimpleTypeName();

		// Get field from entity related with some master entity defined into the fields names list
		FieldMetadata relationField = getFieldRelationMasterEntity();
		
		// TODO Unify next 3 cases code
        if (relationField == null) {
        	
        	// TODO This case is already required ? 

    		/*
    		 * MasterEntityName masterentityname = MasterEntityName.findMasterEntityName(gvnixreference);
    		 * entityname.setMasterEntityName(masterentityname);
    		 */
        	
    		bodyBuilder.appendFormalLine(
    				masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + 
    				" " + masterEntityName.toLowerCase() + " = " + masterEntityName + "." + 
    						masterEntityJavaDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
    		bodyBuilder.appendFormalLine(
    				entityName.toLowerCase() + ".set" + masterEntityName + "(" + masterEntityName.toLowerCase() + ");");
        }
        else if (entityTypeDetails.getPersistenceDetails().getRooIdentifierFields().contains(relationField)) {
        	
            /*
             *  EntityPK entitypk = new EntityPK(entity.getId().getField1(), ... gvnixreference, ... entity.getId().getFieldN());
             *  entity.setId(entitypk);
             */
        	
            // When field metadata in composite roo identifier: use PK constructor
        	StringBuilder tmpBody = new StringBuilder();
        	tmpBody.append(entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getNameIncludingTypeParameters(
        			false, builder.getImportRegistrationResolver()) + 
        			" " + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getSimpleTypeName().toLowerCase() +
        			" = new " + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getNameIncludingTypeParameters(
        					false, builder.getImportRegistrationResolver()) + "(");
        	Iterator<FieldMetadata> fields = entityTypeDetails.getPersistenceDetails().getRooIdentifierFields().iterator();
        	while (fields.hasNext()) {
        		FieldMetadata field = fields.next();
        		if (field.getFieldName().equals(relationField.getFieldName())) {
        			tmpBody.append("gvnixreference");	
        		}
        		else {
        			tmpBody.append(entityTypeDetails.getJavaType().getSimpleTypeName().toLowerCase() + 
                			".get" + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldName().getSymbolNameCapitalisedFirstLetter() +
                			"().get" + field.getFieldName().getSymbolNameCapitalisedFirstLetter() + "()");
        		}
        		if (fields.hasNext()) {
        			tmpBody.append(", ");
        		}
			}
        	tmpBody.append(");");
        	bodyBuilder.appendFormalLine(tmpBody.toString());
        	
    		bodyBuilder.appendFormalLine(entityName.toLowerCase() + ".set" + 
					entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldName().getSymbolNameCapitalisedFirstLetter() +
					"(" + entityTypeDetails.getPersistenceDetails().getIdentifierField().getFieldType().getSimpleTypeName().toLowerCase() + ");");
        }
        else {
        	
    		/*
    		 * MasterEntityName masterentityname = MasterEntityName.findMasterEntityName(gvnixreference);
    		 * entityname.setRelationFieldName(masterentityname);
    		 */
        	
    		bodyBuilder.appendFormalLine(
    				masterEntity.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + 
    				" " + masterEntityName.toLowerCase() + " = " + masterEntityName + "." + 
    						masterEntityJavaDetails.getPersistenceDetails().getFindMethod().getMethodName() + "(gvnixreference);");
			bodyBuilder.appendFormalLine(
					entityName.toLowerCase() + ".set" + relationField.getFieldName().getSymbolNameCapitalisedFirstLetter() + "(" + masterEntityName.toLowerCase() + ");");
        }
        
        
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

        bodyBuilder
        .appendFormalLine("return \""
                .concat("redirect:/")
                .concat(masterEntityJavaDetails.getPlural().toLowerCase())
                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));
        
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder
                .setAnnotations(getRequestMappingAnnotationCreateUpdate(RequestMethod.POST, patternName));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

    protected MethodMetadata getUpdateMethod(String patternName) {
    	
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("updatePattern" + patternName);

        List<JavaSymbolName> methodParamNames = new ArrayList<JavaSymbolName>();
        List<AnnotatedJavaType> methodParamTypes = new ArrayList<AnnotatedJavaType>();
        
        getRequestParam(methodParamNames, methodParamTypes);
        
        MethodMetadata method = methodExists(methodName, methodParamTypes);
        if (method != null) {
        	
            // If it already exists, just return null and omit its generation via the ITD
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
        addBodyLinesForDialogMessage(bodyBuilder, DialogType.Error,
                "message_errorbinding_problemdescription");
        bodyBuilder.appendFormalLine("return \"redirect:/".concat(
                entityNamePlural.toLowerCase()).concat(
                "?\" + refererQuery(httpServletRequest);"));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("return \"".concat("redirect:/")
                .concat(masterEntityJavaDetails.getPlural().toLowerCase())
                .concat("?gvnixform&\" + refererQuery(httpServletRequest);"));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                methodParamTypes, methodParamNames, bodyBuilder);

        methodBuilder
                .setAnnotations(getRequestMappingAnnotationCreateUpdate(RequestMethod.PUT, patternName));

        method = methodBuilder.build();
        controllerMethods.add(method);
        return method;
    }

	/**
	 * Get field from entity related with some master entity defined into the fields names list.
	 * 
	 * @param relationsFields Master fields names
	 * @param masterEntityDetails Master entity details
	 * @param entityTypeDetails Entity details
	 * @return Entity field related with master entity field
	 */
	protected FieldMetadata getFieldRelationMasterEntity() {
		
        FieldMetadata field = null;

		// Get field from master entity with OneToMany or ManyToMany annotation and "mappedBy" attribute has some value
		String masterField = null;
        List<FieldMetadata> masterFields = masterEntityDetails.getFields();
        for (FieldMetadata tmpMasterField : masterFields) {
			
        	List<AnnotationMetadata> masterFieldAnnotations = tmpMasterField.getAnnotations();
        	for (AnnotationMetadata masterFieldAnnotation : masterFieldAnnotations) {
        		
        		// TODO May be more fields on relationsFields var
				AnnotationAttributeValue<?> masterFieldMappedBy = masterFieldAnnotation.getAttribute(new JavaSymbolName("mappedBy"));
				JavaType annotationType = masterFieldAnnotation.getAnnotationType();
				boolean isOneToMany = annotationType.equals(new JavaType("javax.persistence.OneToMany"));
				boolean isManyToMany = annotationType.equals(new JavaType("javax.persistence.ManyToMany"));
				if (isOneToMany || isManyToMany) {
					
					masterField = masterFieldMappedBy.getValue().toString();
				}
			}
		}
        
        if (masterField != null) {

			// Get field from entity with Column annotation and "name" attribute same as previous "mappedBy"
	        List<FieldMetadata> fields = entityDetails.getFields();
	        fields.addAll(entityTypeDetails.getPersistenceDetails().getRooIdentifierFields());
	        for (FieldMetadata tmpField : fields) {

				if (masterField.equals(tmpField.getFieldName().getSymbolName())) {
					
					field = tmpField;
				}
			}
        }
        
		return field;
	}

	/**
	 * Get the RequestParam annotation for gvnixreference.
	 * 
	 * <p>Key has the param name and value has the annotation.</p>
	 * 
	 * <code>@RequestParam(value = "gvnixreference", required = true) MasterEntityIdType gvnixreference</code>
	 * 
	 * @return Request param name and annotation
	 */
	protected Entry<JavaSymbolName, AnnotatedJavaType> getReferenceRequestParam() {
		
		AnnotationMetadataBuilder gvnixreferenceParamBuilder = new AnnotationMetadataBuilder(
				new JavaType("org.springframework.web.bind.annotation.RequestParam"));
		gvnixreferenceParamBuilder.addStringAttribute("value", "gvnixreference");
		gvnixreferenceParamBuilder.addBooleanAttribute("required", true);
		List<AnnotationMetadata> gvnixreferenceParam = new ArrayList<AnnotationMetadata>();
		gvnixreferenceParam.add(gvnixreferenceParamBuilder.build());
		
		return new SimpleEntry<JavaSymbolName, AnnotatedJavaType>(
				new JavaSymbolName("gvnixreference"), 
				new AnnotatedJavaType(new JavaType(
						masterEntityJavaDetails.getPersistenceDetails().getIdentifierField().getFieldType().getFullyQualifiedTypeName()), 
						gvnixreferenceParam));
	}

    // Typically, no changes are required beyond this point

    public static final String getMetadataIdentiferType() {
    	
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType controller, LogicalPath path) {
    	
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, controller, path);
    }

    public static final JavaType getJavaType(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, mid);
    }

    public static final LogicalPath getPath(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, mid);
    }

    public static boolean isValid(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, mid);
    }
    
}
