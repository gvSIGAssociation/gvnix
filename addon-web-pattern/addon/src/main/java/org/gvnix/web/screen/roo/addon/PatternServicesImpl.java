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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;

import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

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

    private static final Logger LOGGER = Logger
            .getLogger(PatternServicesImpl.class.getName());

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private MetadataService metadataService;
    private MemberDetailsScanner memberDetailsScanner;
    private TypeLocationService typeLocationService;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /** {@inheritDoc} */
    public boolean existsMasterPatternDefined(String patternName) {

        // Get all pattern names
        if (patternName != null) {
            for (String tmpPatternName : getMasterPatternNames()) {
                if (patternName.equals(tmpPatternName)) {

                    // Supplied name already exists in project
                    return true;
                }
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    public boolean existsMasterPatternDuplicated() {

        List<String> patternNames = new ArrayList<String>();

        // Get all master pattern names
        for (String tmpPatternName : getMasterPatternNames()) {

            if (patternNames.contains(tmpPatternName)) {

                // There is already duplicated pattern names in project
                return true;

            }
            else {

                // No duplicated
                patternNames.add(tmpPatternName);
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    public List<String> getMasterPatternNames() {

        List<String> patternNames = new ArrayList<String>();

        for (ClassOrInterfaceTypeDetails typeDetails : getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(PATTERN_ANNOTATION)) {

            patternNames.addAll(getMasterPatternsNames(typeDetails));
        }

        return patternNames;
    }

    /**
     * Get master patterns names defined into a controller.
     * 
     * @param controller Controller type details
     * @return Master patterns list names
     */
    protected List<String> getMasterPatternsNames(
            ClassOrInterfaceTypeDetails controller) {

        List<String> patternNames = new ArrayList<String>();

        // { "PatternName1=tabular", "PatternName2=register",
        // "PatternName3=tabular_edit_register" }
        List<StringAttributeValue> patternAttributes = getAnnotationValueStringAttributes(
                controller, WebScreenOperationsImpl.PATTERN_ANNOTATION);
        for (StringAttributeValue tmpPatternAttribute : patternAttributes) {

            patternNames.add(getMasterPatternName(tmpPatternAttribute));
        }

        return patternNames;
    }

    /**
     * Get master pattern name defined into a master pattern attribute.
     * 
     * @param patternAttribute Master pattern attribute
     * @return Master pattern name
     */
    protected String getMasterPatternName(StringAttributeValue patternAttribute) {

        // "PatternName1: tabular"
        String patternValue = patternAttribute.getValue();
        String[] patternName = patternValue.split("=");

        // "PatternName1"
        return patternName[0];
    }

    /**
     * Get string attributes of a annotation from a type details.
     * 
     * @param typeDetails Type details
     * @param annotation Annotation java type
     * @return Type details annotation string attributes
     */
    protected List<StringAttributeValue> getAnnotationValueStringAttributes(
            ClassOrInterfaceTypeDetails typeDetails, JavaType annotation) {

        List<StringAttributeValue> patternAttributes = new ArrayList<StringAttributeValue>();

        AnnotationMetadata patternAnnotation = MemberFindingUtils
                .getAnnotationOfType(typeDetails.getAnnotations(), annotation);
        if (patternAnnotation != null) {

            // look for pattern name in @GvNIXPattern values
            AnnotationAttributeValue<?> patternValues = patternAnnotation
                    .getAttribute("value");
            if (patternValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> patternValue = (List<StringAttributeValue>) patternValues
                        .getValue();
                patternAttributes.addAll(patternValue);
            }
        }

        return patternAttributes;
    }

    /** {@inheritDoc} */
    public List<StringAttributeValue> getControllerRelatedPatternsValueAttributes(
            JavaType controller) {

        return getControllerAnnotationValueAttributes(controller,
                WebScreenOperationsImpl.RELATED_PATTERN_ANNOTATION);
    }

    /** {@inheritDoc} */
    public List<StringAttributeValue> getEntityRelatedPatternsValueAttributes(
            JavaType entity) {

        PhysicalTypeMetadata controller = getEntityController(entity);
        return getControllerAnnotationValueAttributes(controller.getType(),
                WebScreenOperationsImpl.RELATED_PATTERN_ANNOTATION);
    }

    /**
     * Get string attributes of a annotation from a java type.
     * 
     * @param controller Java type
     * @param annotation Annotation java type
     * @return Java type annotation string attributes
     */
    protected List<StringAttributeValue> getControllerAnnotationValueAttributes(
            JavaType controller, JavaType annotation) {

        // Get @GvNIXPattern annotation from controller
        ClassOrInterfaceTypeDetails controllerDetails = getControllerTypeDetails(controller);
        return getAnnotationValueStringAttributes(controllerDetails, annotation);
    }

    /** {@inheritDoc} */
    public ClassOrInterfaceTypeDetails getControllerTypeDetails(
            JavaType controller) {

        ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                .getTypeDetails(controller);

        // The Java source type exists ?
        if (controllerDetails == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + controller.getFullyQualifiedTypeName() + "'");
        }

        // Test if has the @RooWebScaffold
        Validate.notNull(
                MemberFindingUtils.getAnnotationOfType(
                        controllerDetails.getAnnotations(),
                        WebScreenOperationsImpl.ROOWEBSCAFFOLD_ANNOTATION),
                controller.getSimpleTypeName().concat(
                        " has not @RooWebScaffold annotation"));

        return controllerDetails;
    }

    /**
     * Given a pattern name says if it exists defined as Master pattern in
     * GvNIXPattern attributes, return this pattern attribute
     * 
     * @param patternAttributes Pattern annotation attributes
     * @param patternName Pattern name
     * @return Pattern attribute (i.e. name=type)
     */
    protected String getControllerMasterPattern(JavaType controller,
            JavaSymbolName patternName) {

        List<StringAttributeValue> patternAttributes = getControllerMasterPattern(controller);
        for (StringAttributeValue patternAttribute : patternAttributes) {

            // Check if pattern has defined pattern name
            String pattern = patternAttribute.getValue();
            if (existsMasterPatternName(pattern, patternName)) {

                return pattern;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    public List<StringAttributeValue> getControllerMasterPattern(
            JavaType controller) {

        return getControllerAnnotationValueAttributes(controller,
                WebScreenOperationsImpl.PATTERN_ANNOTATION);
    }

    /**
     * Get the pattern type with some name from pattern value.
     * <p>
     * If pattern has not defined pattern name, return null.
     * </p>
     * 
     * @param pattern pattern annotation attribute value (i.e. name=type)
     * @param patternName pattern name (i.e. name) to search
     * @return Pattern type (i.e. type) or null if distinct name
     */
    protected boolean existsMasterPatternName(String pattern,
            JavaSymbolName patternName) {

        String current = pattern.replace(" ", "");
        String patternPrefix = patternName.getSymbolName().concat("=");
        if (current.startsWith(patternPrefix)) {

            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    public boolean existsControllerMasterPattern(JavaType controller,
            JavaSymbolName patternName) {

        return getControllerMasterPattern(controller, patternName) == null ? false
                : true;
    }

    /** {@inheritDoc} */
    public String getControllerMasterPatternType(JavaType controller,
            JavaSymbolName patternName) {

        String pattern = getControllerMasterPattern(controller, patternName);
        if (existsMasterPatternName(pattern, patternName)) {

            String current = pattern.replace(" ", "");
            String patternPrefix = patternName.getSymbolName().concat("=");
            return current.substring(patternPrefix.length(), current.length());
        }

        return null;
    }

    /** {@inheritDoc} */
    public FieldMetadata getEntityToManyField(JavaType entity, String fieldName) {

        List<FieldMetadata> toManyFields = getEntityToManyFields(entity);
        for (FieldMetadata toManyField : toManyFields) {
            if (toManyField.getFieldName().getSymbolName().equals(fieldName)) {
                return toManyField;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    public List<FieldMetadata> getEntityToManyFields(JavaType entity) {

        List<FieldMetadata> toManyFields = new ArrayList<FieldMetadata>();

        ClassOrInterfaceTypeDetails entityTypeDetails = getTypeLocationService()
                .getTypeDetails(entity);
        Validate.notNull(entityTypeDetails, "Cannot locate Metadata for '"
                .concat(entity.getFullyQualifiedTypeName()).concat("'."));
        MemberDetails entityMemberDetails = getMemberDetailsScanner()
                .getMemberDetails(getClass().getName(), entityTypeDetails);
        List<FieldMetadata> entityFields = entityMemberDetails.getFields();

        for (FieldMetadata entityField : entityFields) {
            for (AnnotationMetadata entityFieldAnnotation : entityField
                    .getAnnotations()) {
                if (entityFieldAnnotation.getAnnotationType().equals(
                        ONETOMANY_ANNOTATION)
                        || entityFieldAnnotation.getAnnotationType().equals(
                                MANYTOMANY_ANNOTATION)) {

                    toManyFields.add(entityField);
                }
            }
        }

        return toManyFields;
    }

    /** {@inheritDoc} */
    public boolean existsRelatedPatternType(String patternType,
            List<StringAttributeValue> relatedPattern) {

        for (StringAttributeValue relatedPatternAttribute : relatedPattern) {

            if (getRelatedPatternType(relatedPatternAttribute)
                    .equalsIgnoreCase(patternType)) {

                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    public List<String> getRelatedPatternNames() {

        List<String> patternNames = new ArrayList<String>();

        for (ClassOrInterfaceTypeDetails typeDetails : getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        RELATED_PATTERN_ANNOTATION)) {

            patternNames.addAll(getControllerRelatedPatternNames(typeDetails
                    .getType()));
        }

        return patternNames;
    }

    /** {@inheritDoc} */
    public List<String> getControllerRelatedPatternNames(JavaType controller) {

        return getRelatedPatternNames(getControllerAnnotationValueAttributes(
                controller, WebScreenOperationsImpl.RELATED_PATTERN_ANNOTATION));
    }

    /**
     * Get relateds pattern types from a controller.
     * <p>
     * Patterns are get from {@link GvNIXRelatedPattern} annotation.
     * </p>
     * 
     * @param controller Controller
     * @return Related pattern types
     */
    public List<String> getControllerRelatedPatternTypes(JavaType controller) {

        return getRelatedPatternTypes(getControllerAnnotationValueAttributes(
                controller, WebScreenOperationsImpl.RELATED_PATTERN_ANNOTATION));
    }

    /**
     * Get related pattern names from related patterns attributes.
     * 
     * @param relatedPattern Related pattern attributes
     * @return Related pattern names
     */
    protected List<String> getRelatedPatternNames(
            List<StringAttributeValue> relatedPattern) {

        List<String> names = new ArrayList<String>();

        for (StringAttributeValue relatedAttribute : relatedPattern) {

            names.add(getRelatedPatternName(relatedAttribute));
        }

        return names;
    }

    /**
     * Get relateds patterns types from related pattern attributes.
     * 
     * @param relatedPattern Relateds patterns attributes
     * @return Relateds patterns types
     */
    protected List<String> getRelatedPatternTypes(
            List<StringAttributeValue> relatedPattern) {

        List<String> names = new ArrayList<String>();

        for (StringAttributeValue relatedPatternAttribute : relatedPattern) {

            names.add(getRelatedPatternType(relatedPatternAttribute));
        }

        return names;
    }

    /**
     * Get related pattern type from a related pattern attribute.
     * 
     * @param relatedPattern Related pattern attribute
     * @return Related pattern type
     */
    protected String getRelatedPatternType(StringAttributeValue relatedPattern) {

        // For "PatternName1=register"
        // Obtains "register"
        return relatedPattern.getValue().split("=")[1];
    }

    /**
     * Get related pattern name from a related pattern attribute.
     * 
     * @param relatedPattern Related pattern attribute
     * @return Related pattern name
     */
    protected String getRelatedPatternName(StringAttributeValue relatedPattern) {

        // For "PatternName1=register"
        // Obtains "PatternName1"
        return relatedPattern.getValue().split("=")[0];
    }

    /** {@inheritDoc} */
    public List<String> getEntityRelationsPatternsFields(JavaType entity) {

        return getControllerRelationsPatternsFields(getEntityController(entity));
    }

    /** {@inheritDoc} */
    public List<String> getControllerRelationsPatternsFields(
            PhysicalTypeMetadata controller) {

        // Must be a valid web scaffold controller
        WebScaffoldAnnotationValues webScaffold = new WebScaffoldAnnotationValues(
                controller);
        if (!webScaffold.isAnnotationFound()
                || webScaffold.getFormBackingObject() == null
                || controller.getMemberHoldingTypeDetails() == null) {

            return null;
        }

        ClassOrInterfaceTypeDetails controllerType = (ClassOrInterfaceTypeDetails) controller
                .getMemberHoldingTypeDetails();
        Validate.notNull(
                controllerType,
                "Governor failed to provide class type details, in violation of superclass contract");

        List<String> fields = new ArrayList<String>();

        // Retrieve the fields defined as relationships in GvNIXRelationsPattern
        AnnotationMetadata relationsAnnotation = MemberFindingUtils
                .getAnnotationOfType(controllerType.getAnnotations(),
                        RELATIONSPATTERN_ANNOTATION);
        if (relationsAnnotation != null) {

            AnnotationAttributeValue<?> relationsAttribute = relationsAnnotation
                    .getAttribute(new JavaSymbolName("value"));
            if (relationsAttribute != null) {

                fields.addAll(getRelationsPatternFields(relationsAttribute));
            }
        }

        return fields;
    }

    /**
     * Get relations pattern fields defined into a relations pattern attribute.
     * 
     * @param relationsPattern Relations pattern attribute
     * @return Relations pattern fields
     */
    protected List<String> getRelationsPatternFields(
            AnnotationAttributeValue<?> relationsPattern) {

        List<String> fields = new ArrayList<String>();

        // For { "PatternName1: field1=tabular, field2=register",
        // "PatternName2: field3=tabular_edit_register" }
        // Obtains "field1", "field2" and "field3"

        @SuppressWarnings("unchecked")
        List<StringAttributeValue> relations = (List<StringAttributeValue>) relationsPattern
                .getValue();
        for (StringAttributeValue relation : relations) {

            fields.addAll(getRelationsPatternFields(relation));
        }

        return fields;
    }

    /**
     * Get relations pattern fields defined into a relations pattern attribute.
     * 
     * @param relationsAttributes Relations pattern attribute
     * @return Relations pattern fields
     */
    protected List<String> getRelationsPatternFields(
            StringAttributeValue relationsPattern) {

        List<String> fields = new ArrayList<String>();

        // For "PatternName1: field1=tabular, field2=register"
        // Obtains "field1" and "field2"
        String[] patternsDefinition = relationsPattern.getValue().split(":");
        String[] fieldsDefinition = patternsDefinition[1].trim().split(",");
        for (String fieldDefinition : fieldsDefinition) {

            fields.add(fieldDefinition.trim().split("=")[0]);
        }

        return fields;
    }

    /**
     * Get relations pattern name defined into a relations pattern attribute.
     * 
     * @param relationsAttributes Relations pattern attribute
     * @return Relations pattern name
     */
    protected String getRelationsPatternName(
            StringAttributeValue relationsPattern) {

        // For "PatternName1: field1=tabular, field2=register"
        // Obtains "PatternName1"
        return relationsPattern.getValue().split(":")[0].trim();
    }

    /** {@inheritDoc} */
    public Map<String, String> getRelationsPatternFieldAndRelatedPattern(
            AnnotationAttributeValue<?> relationsPattern) {

        Map<String, String> fieldsPatternIdAndType = new HashMap<String, String>();

        // For { "PatternName1: field1=tabular, field2=register",
        // "PatternName2: field3=tabular_edit_register" }
        // Obtains "field1"="PatternName1=tabular",
        // "field2"="PatternName1=register" and
        // "field3"="PatternName2=tabular_edit_register"

        @SuppressWarnings("unchecked")
        List<StringAttributeValue> relationsPatternList = (List<StringAttributeValue>) relationsPattern
                .getValue();
        if (relationsPatternList != null) {

            for (StringAttributeValue strAttrValue : relationsPatternList) {

                String[] patternsDefinition = strAttrValue.getValue()
                        .split(":");
                String[] fieldsDefinition = patternsDefinition[1].trim().split(
                        ",");
                for (String fieldDefinition : fieldsDefinition) {

                    String[] fieldPatternType = fieldDefinition.trim().split(
                            "=");
                    fieldsPatternIdAndType.put(
                            fieldPatternType[0].trim(),
                            patternsDefinition[0].trim().concat("=")
                                    .concat(fieldPatternType[1].trim()));
                }
            }
        }

        return fieldsPatternIdAndType;
    }

    /** {@inheritDoc} */
    public PhysicalTypeMetadata getEntityController(JavaType entity) {

        // Master controller metadata is the unique controller with a form
        // backing object of master entity type
        PhysicalTypeMetadata controller = null;

        if (entity != null) {
            Set<JavaType> controllers = getTypeLocationService()
                    .findTypesWithAnnotation(
                            new JavaType(RooWebScaffold.class.getName()));
            for (JavaType tmpComtroller : controllers) {
                PhysicalTypeMetadata tmpControllerMetadata = (PhysicalTypeMetadata) getMetadataService()
                        .get(PhysicalTypeIdentifier.createIdentifier(
                                tmpComtroller,
                                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, "")));
                if (entity.equals(new WebScaffoldAnnotationValues(
                        tmpControllerMetadata).getFormBackingObject())) {
                    controller = tmpControllerMetadata;
                }
            }
        }

        return controller;
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on PatternServicesImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public MemberDetailsScanner getMemberDetailsScanner() {
        if (memberDetailsScanner == null) {
            // Get all Services implement MemberDetailsScanner interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MemberDetailsScanner.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MemberDetailsScanner) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MemberDetailsScanner on PatternServicesImpl.");
                return null;
            }
        }
        else {
            return memberDetailsScanner;
        }
    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeLocationService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on PatternServicesImpl.");
                return null;
            }
        }
        else {
            return typeLocationService;
        }
    }

}
