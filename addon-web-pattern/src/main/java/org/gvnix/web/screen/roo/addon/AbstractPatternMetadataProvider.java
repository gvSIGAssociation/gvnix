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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link PatternMetadata}. This type is called by Roo to retrieve the
 * metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public abstract class AbstractPatternMetadataProvider extends
        AbstractMemberDiscoveringItdMetadataProvider {

    /** {@link RooWebScaffold} java type */
    protected static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(RooWebScaffold.class.getName());

    /** {@link GvNIXEntityBatch} java type */
    protected static final JavaType ENTITYBATCH_ANNOTATION = new JavaType(GvNIXEntityBatch.class.getName());

    /** {@link GvNIXRelationsPattern} java type */
    protected static final JavaType RELATIONSPATTERN_ANNOTATION = new JavaType(GvNIXRelationsPattern.class.getName());
    
    /** Name of {@link GvNIXRelationsPattern} attribute value */
    protected static final JavaSymbolName RELATIONSPATTERN_ANNOTATION_VALUE = new JavaSymbolName("value");

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected abstract void activate(ComponentContext context);

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected abstract void deactivate(ComponentContext context);

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected abstract ItdTypeDetailsProvidingMetadataItem getMetadata(
            String mid, JavaType aspect, PhysicalTypeMetadata controller, String fileName);

    /**
     * Read the values of GvNIXRelationsPattern and for the fields defined as
     * visible relationships retrieve its JavaTypeMetadataDetails
     * 
     * @param mid Metadata identification string
     * @param controller Controller physical type metadata
     * @param entity Entity java type
     * @param webMetadataService Web metadata service
     * @return
     */
    protected SortedMap<JavaType, JavaTypeMetadataDetails> getTypesForPopulate(
            String mid, PhysicalTypeMetadata controller, JavaType entity, WebMetadataService webMetadataService) {

    	// Fields name defined into relations pattern annotation
        Set<String> relationsPatternAnnotationFieldsName = getRelationsPatternAnnotationFieldsName(controller);

        // Fields available for scaffolding
        List<FieldMetadata> scaffoldEligibleFields = getScaffoldEligibleFieldMetadata(entity, mid, webMetadataService);
        
        SortedMap<JavaType, JavaTypeMetadataDetails> interestingFieldsTypeMetadata = new TreeMap<JavaType, JavaTypeMetadataDetails>();

        // We're interested only in those types defined in GvNIXRelationsPattern values
        for (FieldMetadata scaffoldElegibleField : scaffoldEligibleFields) {
        	
        	// This scaffold elegible field is defined into relations pattern annotation: is a valid field
            if (relationsPatternAnnotationFieldsName.contains(scaffoldElegibleField.getFieldName().getSymbolName())) {
            	
            	// Get valid field type (simple or collection)
            	JavaType validField = scaffoldElegibleField.getFieldType();
                if (validField.isCommonCollectionType() && !validField.getParameters().isEmpty()) {
                
                    validField = validField.getParameters().get(0);
                }
                
                SortedMap<JavaType, JavaTypeMetadataDetails> relatedAppTypeMetadata = getRelatedAppTypeMetadata(
                		validField, mid, webMetadataService);
                if (relatedAppTypeMetadata != null) {
                	
                	interestingFieldsTypeMetadata.putAll(relatedAppTypeMetadata);
                }
            }
        }
        
        return interestingFieldsTypeMetadata;
    }

    /**
     * For the given <code>type</code> the method returns a Map with its Related App. Types Metadata
     * 
     * @param field
     * @param mid
     * @param webMetadataService
     * @return
     */
    private SortedMap<JavaType, JavaTypeMetadataDetails> getRelatedAppTypeMetadata(
            JavaType field, String mid, WebMetadataService webMetadataService) {
    	
    	// Get field metadata, field details and field persistent details
        PhysicalTypeMetadata fieldMetadata = (PhysicalTypeMetadata) metadataService.get(
        		PhysicalTypeIdentifier.createIdentifier(field, Path.SRC_MAIN_JAVA));
        Assert.notNull(fieldMetadata,
                "Unable to obtain physical type metadata for type " + field.getFullyQualifiedTypeName());
        MemberDetails fieldDetails = getMemberDetails(fieldMetadata);
        MemberHoldingTypeDetails fieldPersistentDetails = MemberFindingUtils.getMostConcreteMemberHoldingTypeDetailsWithTag(
        		fieldDetails, PersistenceCustomDataKeys.PERSISTENT_TYPE);
        
        SortedMap<JavaType, JavaTypeMetadataDetails> relatedApplicationTypeMetadata = webMetadataService.getRelatedApplicationTypeMetadata(
        		field, fieldDetails, mid);
        if (fieldPersistentDetails == null || relatedApplicationTypeMetadata == null) {
        	
            return null;
        }

        return relatedApplicationTypeMetadata;
    }

    /**
     * Returns a map with Related entities JavaType as key, and their Map of the
     * Fields-DateTimeFormatDetails as value.
     * <p>
     * We need this data in order to register the right DateTimeFormatPattern of
     * the related entities in a master/detail pattern.
     * 
     * @param mid
     * @param controller
     * @param entity
     * @param webMetadataService
     * @return
     */
    protected Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> getRelationsDateTypePatterns(
            String mid, PhysicalTypeMetadata controller, JavaType entity, WebMetadataService webMetadataService) {

        Set<String> relationsPatternAnnotationFieldsName = getRelationsPatternAnnotationFieldsName(controller);

        List<FieldMetadata> scaffoldEligibleFields = getScaffoldEligibleFieldMetadata(entity, mid, webMetadataService);

        // We're interested only in those types defined in GvNIXRelationsPattern values
        Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedTypesDatePatterns = 
        		new LinkedHashMap<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>>();
        
        for (FieldMetadata scaffoldEligibleField : scaffoldEligibleFields) {
        	
            if (relationsPatternAnnotationFieldsName.contains(scaffoldEligibleField.getFieldName().getSymbolName())) {
            	
            	JavaType validField = scaffoldEligibleField.getFieldType();
                if (validField.isCommonCollectionType() && !validField.getParameters().isEmpty()) {
                	
                    validField = validField.getParameters().get(0);
                }
                
                relatedTypesDatePatterns.put(
                		validField, webMetadataService.getDatePatterns(validField, webMetadataService.getMemberDetails(validField), mid));
            }
        }

        return relatedTypesDatePatterns;
    }

    /**
     * Get fields name defined from a relations pattern annotation defined into a controller metadata.
     * 
     * <p>It parses the value of the GvNIXRelationsPattern annotation.
     * GvNIXRelationsPattern example:</p>
     * <code>{ "PatternName1: field1=tabular, field2=register", "PatternName2: field3=tabular_edit_register" }</code>
     * 
     * @param controllerMetadata Controller metadata
     * @return Set with the defined relations field names or null if controller is not a valid web scaffold class or 
     * empty set if not relations pattern annotation
     */
    private Set<String> getRelationsPatternAnnotationFieldsName(PhysicalTypeMetadata controllerMetadata) {
    	
    	// Must be a valid web scaffold controller 
        WebScaffoldAnnotationValues webScaffold = new WebScaffoldAnnotationValues(controllerMetadata);
        if (!webScaffold.isAnnotationFound() || webScaffold.getFormBackingObject() == null
        		|| controllerMetadata.getMemberHoldingTypeDetails() == null) {
        	
            return null;
        }

        ClassOrInterfaceTypeDetails controllerType = (ClassOrInterfaceTypeDetails) controllerMetadata.getMemberHoldingTypeDetails();
        Assert.notNull(controllerType,
                "Governor failed to provide class type details, in violation of superclass contract");
        
        Set<String> fields = new HashSet<String>();

        // Retrieve the fields defined as relationships in GvNIXRelationsPattern
        AnnotationMetadata relationsPatternAnnotation = MemberFindingUtils.getAnnotationOfType(
        		controllerType.getAnnotations(), RELATIONSPATTERN_ANNOTATION);
        if (relationsPatternAnnotation != null) {
        	
            AnnotationAttributeValue<?> relationsPatternValues = relationsPatternAnnotation.getAttribute(
            		RELATIONSPATTERN_ANNOTATION_VALUE);
            if (relationsPatternValues != null) {

                // From this relations pattern annotation example:
                // { "PatternName1: field1=tabular, field2=register", "PatternName2: field3=tabular_edit_register" }
                // Obtains "field1", "field2" and "field3" 
                
            	// Will store portions of relations pattern annotation values
                String[] patternDefinition = {};
                String[] fieldDefinition = {};
                String[] fieldName = {};
                
                @SuppressWarnings("unchecked")
                List<StringAttributeValue> relationsPatternList = (List<StringAttributeValue>) relationsPatternValues.getValue();
                for (StringAttributeValue strAttrValue : relationsPatternList) {
                	
                	// "PatternName1: field1=tabular, field2=register"
                    patternDefinition = strAttrValue.getValue().split(":");
                    fieldDefinition = patternDefinition[1].trim().split(",");
                    for (String fieldDef : fieldDefinition) {
                    	
                    	// "field1=tabular"
                        fieldName = fieldDef.trim().split("=");
                        fields.add(fieldName[0]);
                    }
                }
            }
        }

        return fields;
    }

    /**
     * Get scaffold eligible fields metadata from entity with some mid through web metadata service.
     * 
     * @param entityType Entity java type
     * @param mid Metadata identification string
     * @param webMetadataService Web metadata service
     * @return Entity scaffold elegible fields
     */
    private List<FieldMetadata> getScaffoldEligibleFieldMetadata(JavaType entityType, String mid, WebMetadataService webMetadataService) {
    	
        return webMetadataService.getScaffoldEligibleFieldMetadata(entityType, webMetadataService.getMemberDetails(entityType), mid);
    }

    /**
     * Define the unique ITD file name extension
     */
    public abstract String getItdUniquenessFilenameSuffix();

    @Override
    protected abstract String getGovernorPhysicalTypeIdentifier(String mid);

    @Override
    protected abstract String createLocalIdentifier(JavaType javaType, Path path);

    public abstract String getProvidesType();
    
}
