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

import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface to provide common services to Screen Pattern management components
 * 
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
public interface PatternService {

    /** {@link GvNIXPattern} java type */
    public static final JavaType PATTERN_ANNOTATION = new JavaType(GvNIXPattern.class.getName());
    
    /** {@link GvNIXRelatedPattern} JavaType */
    public static final JavaType RELATED_PATTERN_ANNOTATION = new JavaType(GvNIXRelatedPattern.class.getName());
    
    /** {@link GvNIXRelationsPattern} java type */
    public static final JavaType RELATIONSPATTERN_ANNOTATION = new JavaType(GvNIXRelationsPattern.class.getName());
    
    public static final JavaType ONETOMANY_ANNOTATION = new JavaType("javax.persistence.OneToMany");
    public static final JavaType MANYTOMANY_ANNOTATION = new JavaType("javax.persistence.ManyToMany");


    /**
     * Check if there is a pattern name used already in project.
     * 
     * <p>Pattern names are get from {@link GvNIXPattern} annotation.</p>
     *  
     * @param patternName Pattern name to search
     * @return Pattern name already used ?
     * 
     */
    boolean existsMasterPatternDefined(String patternName);

    /**
     * Checks if there is repeated pattern names in project.
     * 
     * <p>Pattern names are get from {@link GvNIXPattern} annotation.</p>
     * 
     * @return Duplicated pattern found ?
     */
    boolean existsMasterPatternDuplicated();

    /**
     * Get used pattern names in project.
     * 
     * <p>Pattern names are get from {@link GvNIXPattern} annotation.</p>
     * 
     * @return Pattern names.
     */
    public List<String> getMasterPatternNames();

    /**
     * Get related patterns attributes of a controller java type.
     *
     * <p>Pattern attributes are get from {@link GvNIXRelatedPattern} annotation.</p>
     * 
     * @param controller Controller java type
     * @return Related patterns attributes list
     */
    public List<StringAttributeValue> getControllerRelatedPatternsValueAttributes(JavaType controller);
    
    /**
     * Get related patterns attributes of a entity java type related controller.
     *
     * <p>Pattern attributes are get from {@link GvNIXRelatedPattern} annotation.</p>
     * 
     * @param controller Controller java type
     * @return Related patterns attributes list
     */
	public List<StringAttributeValue> getEntityRelatedPatternsValueAttributes(JavaType entity);
	
    /**
     * Get controller type details.
     * 
     * <p>Checks if <code>controller</code> is really a controller (annotated with {@link RooWebScaffold} using {@link Assert}).</p>
     * 
     * @param controller Controller java type
     * @return Controller type details
     */
    public ClassOrInterfaceTypeDetails getControllerTypeDetails(JavaType controller);
    
    /**
     * Get master pattern attributes of a controller java type.
     * 
     * <p>Pattern attributes are get from {@link GvNIXPattern} annotation.</p>
     * 
     * @param controller Controller java type
     * @return Pattern attributes list
     */
    public List<StringAttributeValue> getControllerMasterPattern(JavaType controller);
    
    /**
     * Exists a master pattern name defined in a controller ?.
     * 
     * <p>Pattern name is get from {@link GvNIXPattern} annotation.</p>
     * 
     * @param controller Controller java type
     * @param patternName Pattern name to search
     * @return true if exists
     */
    public boolean existsControllerMasterPattern(JavaType controller, JavaSymbolName patternName);
    
    /**
     * Get the type of a master pattern name in a controller.
     * 
     * <p>Pattern name is get from {@link GvNIXPattern} annotation.</p>
     * 
     * @param controller Controller java type
     * @param patternName Pattern name
     * @return Pattern type
     */
    public String getControllerMasterPatternType(JavaType controller, JavaSymbolName patternName);
    
    /**
     * Get OneToMany or ManyToMany annotated field with some name from an entity.
     * 
     * @param entity Entity
     * @param fieldName Field name
     * @return Field metadata if field is annotated with OneToMany or ManyToMany annotations.
     */
    public FieldMetadata getEntityToManyField(JavaType entity, String fieldName);

    /**
     * Get the list of fields metadata of fields annotated with OneToMany or ManyToMany in a entity.
     * 
     * @param entity Entity
     * @return Field metadata with OneToMany or ManyToMany annotation in a entity.
     */
    public List<FieldMetadata> getEntityToManyFields(JavaType entity);

    /**
     * Exists a pattern of given {@link WebPatternType} defined in master pattern ?.
     * 
     * @param patternType Pattern type
     * @param relatedPattern Related patterns
     * @return Exists a pattern in list with some type
     */
    public boolean existsRelatedPatternType(String patternType, List<StringAttributeValue> relatedPattern);
    
    /**
     * Get used pattern names in project.
     * 
     * <p>Pattern names are get from {@link GvNIXRelatedPattern} annotation.</p>
     * 
     * @return Pattern names.
     */
    public List<String> getRelatedPatternNames();
    
	/**
	 * Get related pattern names from a controller.
	 * 
     * <p>Patterns are get from {@link GvNIXRelatedPattern} annotation.</p>
	 * 
	 * @param controller Controller
	 * @return Related pattern names
	 */
    public List<String> getControllerRelatedPatternNames(JavaType controller);
    
	/**
	 * Get entity fields defined into relations pattern annotation on its related controller. 
	 * 
     * <p>Patterns are get from {@link GvNIXRelationsPattern} annotation.
     * Returns empty set if not relations pattern annotation or null if no entity or controller.</p>
     * 
	 * @param entity Entity java type
	 * @return Relations pattern fields
	 */
    public List<String> getEntityRelationsPatternsFields(JavaType entity);
    
    /**
     * Get controller fields defined into relations pattern annotation.
     * 
     * <p>Patterns are get from {@link GvNIXRelationsPattern} annotation.
     * Returns empty set if not relations pattern annotation or null if no controller.</p>
     * 
     * @param controller Controller metadata
     * @return Relations pattern fields
     */
    public List<String> getControllerRelationsPatternsFields(PhysicalTypeMetadata controller);

    /**
     * Get a Map where keys is the field and value is the relations pattern for this field.<br/>
     * 
     * <p>Patterns are get from {@link GvNIXRelationsPattern} annotation.</p>
     * 
     * <code>
     * For {"patternId: field1=tabular, field2=register"}
     * it returns
     * {field1 => "patternId=tabular", field2 => "patternId=register"}
     * </code>
     * 
     * @param relationsPatternAttributes Relations pattern attributes
     * @return Map with relations fields and patterns
     */
    public Map<String, String> getRelationsPatternFieldAndRelatedPattern(AnnotationAttributeValue<?> relationsPatternAttributes);
    
    /**
     * Get the controller related with some entity.
     * 
     * <p>Related controller is a class with RooWebScaffold annotation with form backing object attribute that points to entity.
     * Return null if not exists entity or related controller.</p>
     * 
     * TODO Can exists more than one controller related to an entity.
     * 
     * @param entity Entity to get it controller
     * @return Controller type metadata
     */
    public PhysicalTypeMetadata getEntityController(JavaType entity);
}
