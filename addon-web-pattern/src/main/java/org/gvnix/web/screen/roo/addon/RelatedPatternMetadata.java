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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdSourceFileComposer;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.EnumDetails;
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
public class RelatedPatternMetadata extends AbstractPatternMetadata {

    private static final String PROVIDES_TYPE_STRING = RelatedPatternMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    public RelatedPatternMetadata(String mid, JavaType aspect, PhysicalTypeMetadata controllerMetadata, MemberDetails controllerDetails,
    		WebScaffoldMetadata webScaffoldMetadata, List<StringAttributeValue> patterns,
    		PhysicalTypeMetadata entityMetadata, JavaTypeMetadataDetails masterEntityDetails,
    		SortedMap<JavaType, JavaTypeMetadataDetails> relatedEntities, SortedMap<JavaType, JavaTypeMetadataDetails> relatedFields,
    		Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedDates, Map<JavaSymbolName, DateTimeFormatDetails> entityDateTypes) {
    	
        super(mid, aspect, controllerMetadata, controllerDetails, webScaffoldMetadata, patterns,
        		entityMetadata, masterEntityDetails, relatedEntities, relatedFields, relatedDates, entityDateTypes);

        List<String> tabularEditPatterns = getPatternTypeDefined(WebPatternType.tabular_edit_register, this.patterns);
        if (!tabularEditPatterns.isEmpty()) {
        	
            for (String tabularEditPattern : tabularEditPatterns) {
            	
            	// Method only exists when this is a detail pattern (has master entity)
            	builder.addMethod(getCreateFormMethod(tabularEditPattern));
            }
        }
        
        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        new ItdSourceFileComposer(itdTypeDetails);

        Assert.isTrue(isValid(mid), "Metadata identification string '" + mid + "' does not appear to be a valid");
    }

	/**
	 * @see org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.getCreateFormMethod
	 * 
	 * @param patternName
	 * @return
	 */
	protected MethodMetadata getCreateFormMethod(String patternName) {
		
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
		if (!entityDateTypes.isEmpty()) {
			bodyBuilder.appendFormalLine("addDateTimeFormatPatterns(uiModel);");
		}
		
		// TODO Remove dependencies or add it from entity pattern ?

		bodyBuilder.appendFormalLine("return \"" + webScaffoldMetadata.getAnnotationValues().getPath() + "/create\";");

		MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName, JavaType.STRING_OBJECT, paramTypes, paramNames, bodyBuilder);
		methodBuilder.setAnnotations(annotations);
		return methodBuilder.build();
	}

    // Typically, no changes are required beyond this point

    public static final String getMetadataIdentiferType() {
    	
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType controller, Path path) {
    	
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, controller, path);
    }

    public static final JavaType getJavaType(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, mid);
    }

    public static final Path getPath(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, mid);
    }

    public static boolean isValid(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, mid);
    }
    
}
