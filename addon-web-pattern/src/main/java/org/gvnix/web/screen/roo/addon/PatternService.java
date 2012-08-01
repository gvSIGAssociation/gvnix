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
import java.util.Set;

import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.Assert;

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
    
    /** Name of {@link GvNIXPattern} attribute value */
    public static final JavaSymbolName PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName("value");

    /** {@link GvNIXRelatedPattern} JavaType */
    public static final JavaType RELATED_PATTERN_ANNOTATION = new JavaType(GvNIXRelatedPattern.class.getName());
    
    /** Name of {@link GvNIXRelatedPattern} attribute value */
    public static final JavaSymbolName RELATED_PATTERN_ANNOTATION_ATTR_VALUE_NAME = new JavaSymbolName("value");

    /** {@link GvNIXRelationsPattern} java type */
    public static final JavaType RELATIONSPATTERN_ANNOTATION = new JavaType(GvNIXRelationsPattern.class.getName());
    
    /** Name of {@link GvNIXRelationsPattern} attribute value */
    public static final JavaSymbolName RELATIONSPATTERN_ANNOTATION_VALUE = new JavaSymbolName("value");

    public static final JavaType ONETOMANY_ANNOTATION = new JavaType("javax.persistence.OneToMany");
    public static final JavaType MANYTOMANY_ANNOTATION = new JavaType("javax.persistence.ManyToMany");

    /**
     * Check if there is a pattern name used already in project.
     * 
     * <p>Check if supplied pattern name exists and checks in all project if there is already repeated names.</p>
     * 
     * @param Pattern name (can be null)
     * @return duplicated pattern found
     * 
     */
    boolean isPatternDuplicated(String name);

    /**
     * Look at Entity for a One-to-many field by name
     * 
     * @param entityJavaType
     * @param fieldName
     * @return
     */
    public FieldMetadata getToManyFieldFromEntityJavaType(
            JavaType entityJavaType, String fieldName);

    /**
     * Returns the list of fields metadata of those fields annotated with
     * OneToMany or ManyToMany
     * 
     * @param formBakingObjectType
     * @return
     */
    public List<FieldMetadata> getToManyFieldsFromEntityJavaType(
            JavaType formBakingObjectType);
    
    /**
     * Gets controller's mutableType physical detail
     * 
     * <p>
     * Also checks if <code>controllerClass</code> is really a controller
     * (annotated with @RooWebScaffold using {@link Assert})
     * </p>
     * 
     * @param controllerClass
     * @return
     */
    public MutableClassOrInterfaceTypeDetails getControllerMutableTypeDetails(JavaType controllerClass);
    
    /**
     * Get pattern attributes of a controller java type.
     * 
     * <p>Get pattern attributes from @GvNIXPattern</p>
     * 
     * @param controllerClass Controller java type
     * @return Pattern attributes list
     */
    public List<StringAttributeValue> getPatternAttributes(JavaType controllerClass);
    
    /**
     * Get related pattern attributes of a controller java type.
     *
     * <p>Get pattern attributes from @GvNIXRelatedPattern</p>
     * 
     * @param controllerClass Controller java type
     * @return Related pattern attributes list
     */
    public List<StringAttributeValue> getRelatedPatternAttributes(JavaType controllerClass);

    /**
     * Given a pattern name says if it exists defined as Master pattern in
     * GvNIXPattern attributes
     * 
     * @param patternValues Pattern annotation attributes
     * @param name Pattern name
     * @return true if exists
     */
    public boolean patternExists(List<StringAttributeValue> patternValues, JavaSymbolName name);
    
    /**
     * Get the type of a pattern name from attributes.
     * 
     * @param patternValues
     *            pattern attributes
     * @param name
     *            identifier to compare
     * @return pattern type of pattern name from attributes
     */
    public String patternType(List<StringAttributeValue> patternValues, JavaSymbolName name);

    /**
     * Given a pattern name says if it exists defined as Master pattern in
     * GvNIXPattern attributes, return this pattern attribute
     * 
     * @param patternValues Pattern annotation attributes
     * @param name Pattern name
     * @return Pattern attribute (i.e. name=type)
     */
    public String pattern(List<StringAttributeValue> patternValues, JavaSymbolName name);

    /**
     * Checks if pattern annotation attribute uses the very same identifier
     * than <code>name</code>
     * 
     * @param value
     *            pattern annotation value item
     * @param name
     *            identifier to compare
     * @return true if pattern name exists in attributes
     */
    public boolean equalsPatternName(StringAttributeValue value, JavaSymbolName name);
    
    /**
     * Get pattern type with some name defined into attribute value.
     * 
     * @param value
     *            pattern annotation attribute value (i.e. name=type)
     * @param name
     *            pattern name to search
     * @return type of pattern name from attribute value
     */
    public String patternType(String value, JavaSymbolName name);

    /**
     * Given the param relationsPatternValues it returns a Map where keys are
     * the fieldName part and values are patternId=patternType. That is, for:<br/>
     * 
     * relationsPatternValues = {"patternId: field1=tabular, field2=register"}
     * it returns <br/>
     * {field1 => "patternId=tabular", field2 => "patternId=register"}
     * 
     * @param relationsPatternValues
     * @return
     */
    public Map<String, String> getFieldsPatternIdAndType(AnnotationAttributeValue<?> relationsPatternValues);
    
    /**
     * If there is a pattern of given WebPatternType defined in GvNIXPattern it
     * returns true, false otherwise
     * 
     * @param patternType
     * @param definedPatternsList
     * @return
     */
    public boolean isPatternTypeDefined(WebPatternType patternType, List<String> definedPatternsList);
    
	/**
	 * Get entity fields names defined into relations pattern annotation on its related controller. 
	 * 
	 * @param entity Entity java type
	 * @return Relations pattern fields names
	 */
    public Set<String> getRelationsFields(JavaType entity);
    
    /**
     * Read the values of GvNIXRelationsPattern and for each field defined as relation retrieve its name.
     * 
     * @param controller Controller metadata
     * @return Set with the defined relations field names or null if controller is not a valid web scaffold class or 
     * empty set if not relations pattern annotation
     */
    public Set<String> getRelationsFields(PhysicalTypeMetadata controller);
 
}
