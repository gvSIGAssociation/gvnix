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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;

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
public abstract class AbstractPatternMetadataProvider extends AbstractMemberDiscoveringItdMetadataProvider {

    /** {@link RooWebScaffold} java type */
    protected static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(RooWebScaffold.class.getName());

    protected PatternService _patternService;

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
            String mid, JavaType aspect, PhysicalTypeMetadata controller, String file);

    /**
     * Read the values of GvNIXRelationsPattern and for each field defined as relation retrieve its java type details.
     * 
     * <p>It parses the value of the GvNIXRelationsPattern annotation. GvNIXRelationsPattern example:</p>
     * <code>{ "PatternName1: field1=tabular, field2=register", "PatternName2: field3=tabular_edit_register" }</code>
     * 
     * @param mid Metadata identification string
     * @param controller Controller physical type metadata
     * @param entity Entity java type
     * @param webMetadataService Web metadata service
     * @return Map with java types and java type details
     */
    protected SortedMap<JavaType, JavaTypeMetadataDetails> getRelationFieldsDetails(
            String mid, PhysicalTypeMetadata controller, JavaType entity, WebMetadataService webMetadataService) {

        SortedMap<JavaType, JavaTypeMetadataDetails> relatedApplicationTypeMetadata = 
        		new TreeMap<JavaType, JavaTypeMetadataDetails>();

        // For each eligible field: put all java types details in the same map
    	List<JavaType> relations = getRelationFields(
				mid, controller, entity, webMetadataService);
        for (JavaType relation : relations) {

            SortedMap<JavaType, JavaTypeMetadataDetails> tmp = getFieldJavaTypesDetails(
            		relation, mid, webMetadataService);
            if (tmp != null) {
            	
            	relatedApplicationTypeMetadata.putAll(tmp);
            }
		}
        
        return relatedApplicationTypeMetadata;
    }

    /**
     * Read the values of GvNIXRelationsPattern and for each field defined as relation retrieve its java type.
     * 
     * @param mid Metadata identification string
     * @param controller Controller physical type metadata
     * @param entity Entity java type
     * @param webMetadataService Web metadata service
     * @return Map with java types and java type details
     */
	private List<JavaType> getRelationFields(
			String mid, PhysicalTypeMetadata controller, JavaType entity, WebMetadataService webMetadataService) {
		
		// Fields name defined into relations pattern annotation
        Set<String> relations = _patternService.getRelationsFields(controller);

        // Get each relation java type from eligible scaffolding fields
        List<FieldMetadata> scaffoldFields = getScaffoldEligibleFields(entity, mid, webMetadataService);
        
        // We're interested only in those types defined in GvNIXRelationsPattern values
        List<JavaType> validFields = new ArrayList<JavaType>();
        for (FieldMetadata scaffoldEligibleField : scaffoldFields) {
        	
        	// This scaffold eligible field is defined into relations pattern annotation: is a valid field
            if (relations.contains(scaffoldEligibleField.getFieldName().getSymbolName())) {
            	
            	// Get valid field type (simple or collection)
            	JavaType validField = scaffoldEligibleField.getFieldType();
                if (validField.isCommonCollectionType() && !validField.getParameters().isEmpty()) {
                
                    validField = validField.getParameters().get(0);
                }
                
                validFields.add(validField);
            }
        }
        
		return validFields;
	}

    /**
     * For the given type returns a Map with its related application types metadata.
     * 
     * @param type Field java type
     * @param mid Metadata identification string
     * @param webMetadataService Web metadata service
     * @return Map with java type and java details related to field
     */
    private SortedMap<JavaType, JavaTypeMetadataDetails> getFieldJavaTypesDetails(
            JavaType type, String mid, WebMetadataService webMetadataService) {
    	
    	// Get field metadata, field details and field persistent details
        PhysicalTypeMetadata typeMetadata = (PhysicalTypeMetadata) metadataService.get(
        		PhysicalTypeIdentifier.createIdentifier(type, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, "")));
        Validate.notNull(typeMetadata,
                "Unable to obtain physical type metadata for type " + type.getFullyQualifiedTypeName());
        MemberDetails typeDetails = getMemberDetails(typeMetadata);
        MemberHoldingTypeDetails typePersistentDetails = MemberFindingUtils.getMostConcreteMemberHoldingTypeDetailsWithTag(
        		typeDetails, CustomDataKeys.PERSISTENT_TYPE);
        
        // Get field related application type metadata if field persistent details no null
        SortedMap<JavaType, JavaTypeMetadataDetails> typeRelatedApplicationTypeMetadata = 
        		webMetadataService.getRelatedApplicationTypeMetadata(type, typeDetails, mid);
        if (typePersistentDetails == null || typeRelatedApplicationTypeMetadata == null) {
        	
            return null;
        }

        return typeRelatedApplicationTypeMetadata;
    }

    /**
     * Get scaffold eligible fields metadata from entity with some mid through web metadata service.
     * 
     * @param entity Entity java type
     * @param mid Metadata identification string
     * @param webMetadataService Web metadata service
     * @return Entity scaffold eligible fields
     */
    private List<FieldMetadata> getScaffoldEligibleFields(
    		JavaType entity, String mid, WebMetadataService webMetadataService) {
    	
        return webMetadataService.getScaffoldEligibleFieldMetadata(entity, webMetadataService.getMemberDetails(entity), mid);
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
    protected Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> getRelationFieldsDateFormat(
            String mid, PhysicalTypeMetadata controller, JavaType entity, WebMetadataService webMetadataService) {

        // We're interested only in those types defined in GvNIXRelationsPattern values
        Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relatedTypesDatePatterns = 
        		new LinkedHashMap<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>>();
        
        List<JavaType> validFields = getRelationFields(mid, controller, entity, webMetadataService);
        for (JavaType validField : validFields) {
            relatedTypesDatePatterns.put(
            		validField, webMetadataService.getDatePatterns(
            				validField, webMetadataService.getMemberDetails(validField), mid));
		}

        return relatedTypesDatePatterns;
    }

    /**
     * Define the unique ITD file name extension
     */
    public abstract String getItdUniquenessFilenameSuffix();

    @Override
    protected abstract String getGovernorPhysicalTypeIdentifier(String mid);

    @Override
    protected abstract String createLocalIdentifier(JavaType javaType, LogicalPath path);

    public abstract String getProvidesType();
    
}
