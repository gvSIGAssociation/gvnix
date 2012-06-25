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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MetadataUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.Assert;

/**
 * Provide common services to Screen Pattern management components. 
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez Sánchez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class PatternServicesImpl implements PatternService {

    @Reference private MetadataService metadataService;
    @Reference private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;
    @Reference private MemberDetailsScanner memberDetailsScanner;
    @Reference private TypeLocationService typeLocationService;

    /** {@inheritDoc} */
    public boolean isPatternDuplicated(String name) {
    	
        List<String> definedPatterns = new ArrayList<String>();
        AnnotationMetadata patternAnnotation = null;
        for (ClassOrInterfaceTypeDetails patternAnnotationClass : typeLocationService.findClassesOrInterfaceDetailsWithAnnotation(
        		PATTERN_ANNOTATION)) {
        	
            patternAnnotation = MemberFindingUtils.getAnnotationOfType(
            		patternAnnotationClass.getAnnotations(), PATTERN_ANNOTATION);
            if (patternAnnotation != null) {
            	
                AnnotationAttributeValue<?> patternAnnotationValue = patternAnnotation.getAttribute(
                		PATTERN_ANNOTATION_ATTR_VALUE_NAME);
                for (String patternName : getPatternNames(patternAnnotationValue)) {
                	
                	if (name != null && name.equals(patternName)) {
                		
                		// Supplied name already exists in project
                		return true;
                	}
                    if (definedPatterns.contains(patternName)) {
                    	
                    	// There is already duplicated pattern names in project 
                        return true;
                        
                    } else {
                    	
                    	// No duplicated
                        definedPatterns.add(patternName);
                    }
                }
            }
        }
        
        return false;
    }

    protected List<String> getPatternNames(AnnotationAttributeValue<?> values) {
    	
        List<String> patternNames = new ArrayList<String>();
        if (values != null) {
        	
            @SuppressWarnings("unchecked")
            List<StringAttributeValue> attrValues = (List<StringAttributeValue>) values.getValue();

            if (attrValues != null) {
            	
                String[] pattern = {};
                for (StringAttributeValue strAttrValue : attrValues) {
                	
                    pattern = strAttrValue.getValue().split("=");
                    patternNames.add(pattern[0]);
                }
            }
        }
        
        return patternNames;
    }

    /** {@inheritDoc} */
    public FieldMetadata getOneToManyFieldFromEntityJavaType(JavaType formBakingObjectType, String fieldName) {
    	
        List<FieldMetadata> oneToManyFields = getOneToManyFieldsFromEntityJavaType(formBakingObjectType);
        for (FieldMetadata field : oneToManyFields) {
            if (field.getFieldName().getSymbolName().equals(fieldName)) {
                return field;
            }
        }
        
        return null;
    }

    /** {@inheritDoc} */
    public List<FieldMetadata> getOneToManyFieldsFromEntityJavaType(
            JavaType formBakingObjectType) {
    	
        List<FieldMetadata> oneToManyFields = new ArrayList<FieldMetadata>();
        MutableClassOrInterfaceTypeDetails formBackingTypeMetadata = MetadataUtils
                .getPhysicalTypeDetails(formBakingObjectType, metadataService,
                        physicalTypeMetadataProvider);

        MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(
                getClass().getName(), formBackingTypeMetadata);

        List<FieldMetadata> fields = MemberFindingUtils
                .getFields(memberDetails);

        Assert.notNull(formBackingTypeMetadata, "Cannot locate Metadata for '"
                .concat(formBakingObjectType.getFullyQualifiedTypeName())
                .concat("'."));

        for (FieldMetadata field : fields) {
            for (AnnotationMetadata fieldAnnotation : field.getAnnotations()) {
                if (fieldAnnotation.getAnnotationType().equals(
                        ONETOMANY_ANNOTATION)) {
                    oneToManyFields.add(field);
                }
            }
        }
        
        return oneToManyFields;
    }

    /** {@inheritDoc} */
    public MutableClassOrInterfaceTypeDetails getControllerMutableTypeDetails(
            JavaType controllerClass) {
    	
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = MetadataUtils.getPhysicalTypeDetails(
        		controllerClass, metadataService, physicalTypeMetadataProvider);
        
        // Test if has the @RooWebScaffold
        Assert.notNull(
                MemberFindingUtils.getAnnotationOfType(
                        mutableTypeDetails.getAnnotations(),
                        WebScreenOperationsImpl.ROOWEBSCAFFOLD_ANNOTATION),
                controllerClass.getSimpleTypeName().concat(
                        " has not @RooWebScaffold annotation"));
        
        return mutableTypeDetails;
    }

    /** {@inheritDoc} */
	public List<StringAttributeValue> getPatternAttributes(JavaType controllerClass) {
		
        List<StringAttributeValue> patternValues = new ArrayList<StringAttributeValue>();

		// Get @GvNIXPattern annotation from controller
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = getControllerMutableTypeDetails(controllerClass);
        AnnotationMetadata patternAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(
        		mutableTypeDetails.getAnnotations(), WebScreenOperationsImpl.PATTERN_ANNOTATION);
        if (patternAnnotationMetadata != null) {

	        // look for pattern name in @GvNIXPattern values
	        AnnotationAttributeValue<?> patternAnnotationValues = patternAnnotationMetadata
	                .getAttribute(WebScreenOperationsImpl.PATTERN_ANNOTATION_ATTR_VALUE_NAME);
	        
	        if (patternAnnotationValues != null) {
	        
	        	@SuppressWarnings("unchecked")
	        	List<StringAttributeValue> values = (List<StringAttributeValue>)patternAnnotationValues.getValue();
	        	patternValues.addAll(values);
	        }
        }
        
		return patternValues;
	}
	
    /** {@inheritDoc} */
	public List<StringAttributeValue> getRelatedPatternAttributes(JavaType controllerClass) {
		
        List<StringAttributeValue> patternValues = new ArrayList<StringAttributeValue>();

		// Get @GvNIXPattern annotation from controller
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = getControllerMutableTypeDetails(controllerClass);
        AnnotationMetadata patternAnnotationMetadata = MemberFindingUtils.getAnnotationOfType(
        		mutableTypeDetails.getAnnotations(), WebScreenOperationsImpl.RELATED_PATTERN_ANNOTATION);
        if (patternAnnotationMetadata != null) {

	        // look for pattern name in @GvNIXPattern values
	        AnnotationAttributeValue<?> patternAnnotationValues = patternAnnotationMetadata
	                .getAttribute(WebScreenOperationsImpl.RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME);
	        
	        if (patternAnnotationValues != null) {
	        
	        	@SuppressWarnings("unchecked")
	        	List<StringAttributeValue> values = (List<StringAttributeValue>)patternAnnotationValues.getValue();
	        	patternValues.addAll(values);
	        }
        }
        
		return patternValues;
	}

	/** {@inheritDoc} */
    public boolean patternExists(List<StringAttributeValue> patternValues, JavaSymbolName name) {
    	
        if (pattern(patternValues, name) == null) {
        	return false;
        }
        else {
        	return true;
        }
    }

    /** {@inheritDoc} */
    public String patternType(List<StringAttributeValue> patternValues, JavaSymbolName name) {
    	
        String pattern = pattern(patternValues, name);
        return patternType(pattern, name);
    }
   
    /** {@inheritDoc} */
    public String pattern(List<StringAttributeValue> patternValues, JavaSymbolName name) {
    	
        for (StringAttributeValue value : patternValues) {
            // Check if name is already used
            if (equalsPatternName(value, name)) {
                return value.getValue();
            }
        }
        
        return null;
    }
     
    /** {@inheritDoc} */
    public boolean equalsPatternName(StringAttributeValue value, JavaSymbolName name) {
    	
        if (patternType(value.getValue(), name) != null) {
        	return true;
        }
        
        return false;
    }

    /** {@inheritDoc} */
    public String patternType(String value, JavaSymbolName name) {
    	
        String current = value.replace(" ", "");
        String patternName = name.getSymbolName().concat("=");
        if (current.startsWith(patternName)) {
        
        	return current.substring(patternName.length(), current.length());
        }
        
        return null;
    }

    /** {@inheritDoc} */
    public Map<String, String> getFieldsPatternIdAndType(AnnotationAttributeValue<?> relationsPatternValues) {
    	
    	// TODO Unify with other methods here ?
    	
        Map<String, String> fieldsPatternIdAndType = new HashMap<String, String>();
        
        // Parse annotationValues finding the field interesting part
        @SuppressWarnings("unchecked")
        List<StringAttributeValue> relationsPatternList = (List<StringAttributeValue>) relationsPatternValues.getValue();
        if (relationsPatternList != null) {
        	
            String[] patternDef = {};
            String[] fieldDefinitions = {};
            String[] fieldPatternType = {};
            
            for (StringAttributeValue strAttrValue : relationsPatternList) {
            	
                patternDef = strAttrValue.getValue().split(":");
                fieldDefinitions = patternDef[1].trim().split(",");
                for (String fieldDef : fieldDefinitions) {
                	
                    fieldPatternType = fieldDef.trim().split("=");
                    fieldsPatternIdAndType.put(
                    		fieldPatternType[0].trim(), 
                    		patternDef[0].trim().concat("=").concat(fieldPatternType[1].trim()));
                }
            }
        }
        
        return fieldsPatternIdAndType;
    }

    /** {@inheritDoc} */
    public boolean isPatternTypeDefined(WebPatternType patternType, List<String> definedPatternsList) {
    	
    	// TODO Unify with other methods here ?
    	
        for (String definedPattern : definedPatternsList) {
        	
            if (definedPattern.split("=")[1].equalsIgnoreCase(patternType.name())) {
            	
                return true;
            }
        }
        
        return false;
    }

}
