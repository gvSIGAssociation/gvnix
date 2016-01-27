/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.support;

import static org.springframework.roo.model.SpringJavaType.REQUEST_MAPPING;
import static org.springframework.roo.model.SpringJavaType.REQUEST_METHOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;

/**
 * Helper which provides utilities for a Web MVC Controllers ITD generator
 * (Metadata)
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public class WebItdBuilderHelper extends ItdBuilderHelper {

    public WebItdBuilderHelper(
            AbstractItdTypeDetailsProvidingMetadataItem metadata,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ImportRegistrationResolver importResolver) {
        super(metadata, governorPhysicalTypeMetadata, importResolver);
    }

    /**
     * Creates a "RequestParam" annotated type
     * 
     * @param paramType
     * @param value (optional) "value" attribute value
     * @param required (optional) attribute value
     * @param defaultValue (optional) attribute value
     * @return
     */
    public AnnotatedJavaType createRequestParam(JavaType paramType,
            String value, Boolean required, String defaultValue) {
        // create annotation values
        final List<AnnotationAttributeValue<?>> annotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        if (StringUtils.isNotBlank(value)) {
            annotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("value"), value));
        }
        if (required != null) {
            annotationAttributes.add(new BooleanAttributeValue(
                    new JavaSymbolName("required"), required.booleanValue()));
        }
        if (defaultValue != null) {
            annotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("defaultValue"), defaultValue));
        }
        AnnotationMetadataBuilder paramAnnotationBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM, annotationAttributes);
        return new AnnotatedJavaType(paramType, paramAnnotationBuilder.build());
    }

    /**
     * Creates a "RequestParam" annotated type including DateTimeFormat
     * annotation for Date fields.
     * 
     * @param paramType
     * @param value (optional) "value" attribute value
     * @param required (optional) attribute value
     * @param defaultValue (optional) attribute value
     * @param dateTimeFormatAnnotation DateTimeFormat annotation metadata from
     *        referred Date field
     * @return
     */
    public AnnotatedJavaType createDateTimeRequestParam(JavaType paramType,
            String value, Boolean required, String defaultValue,
            AnnotationMetadata dateTimeFormatAnnotation) {
        // create annotation values
        final List<AnnotationAttributeValue<?>> annotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        if (StringUtils.isNotBlank(value)) {
            annotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("value"), value));
        }
        if (required != null) {
            annotationAttributes.add(new BooleanAttributeValue(
                    new JavaSymbolName("required"), required.booleanValue()));
        }
        if (defaultValue != null) {
            annotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("defaultValue"), defaultValue));
        }
        AnnotationMetadataBuilder paramAnnotationBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM, annotationAttributes);

        return new AnnotatedJavaType(paramType, paramAnnotationBuilder.build(),
                dateTimeFormatAnnotation);
    }

    /**
     * Create a RequestMapping annotation
     * 
     * @param value (optional) attribute value
     * @param method (optional) attribute value
     * @param consumes (optional) attribute value
     * @param produces (optional) attribute value
     * @param params (optional) attribute value
     * @param headers (optional) attribute value
     * @return
     */
    public AnnotationMetadataBuilder getRequestMappingAnnotation(
            String[] value, String[] method, String[] consumes,
            String[] produces, String[] params, String[] headers) {
        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        if (value != null && value.length > 0) {
            // @RequestMapping(value = {"xx","yyy"})
            methodAnnotation.addAttribute(toAttributeValue("value",
                    Arrays.asList(value)));
        }

        if (produces != null && produces.length > 0) {
            // @RequestMapping(... produces = { "application/json", ... })
            methodAnnotation.addAttribute(toAttributeValue("produces",
                    Arrays.asList(produces)));
        }
        if (consumes != null && consumes.length > 0) {
            // @RequestMapping(... consumes = "application/json")
            methodAnnotation.addAttribute(toAttributeValue("consumes",
                    Arrays.asList(consumes)));
        }
        if (method != null && method.length > 0) {
            // @RequestMapping(... method = { RequestMethod.POST, })

            methodAnnotation.addAttribute(toEnumAttributeValue("method",
                    REQUEST_METHOD, Arrays.asList(method)));
        }
        if (params != null && params.length > 0) {
            // @RequestMapping(... produces = { "application/json" , })
            methodAnnotation.addAttribute(toAttributeValue("params",
                    Arrays.asList(params)));
        }
        if (headers != null && headers.length > 0) {
            // @RequestMapping(... produces = { "application/json" , })
            methodAnnotation.addAttribute(toAttributeValue("headers",
                    Arrays.asList(headers)));
        }

        return methodAnnotation;
    }

    /**
     * Create a RequestMapping annotation
     * 
     * @param value (optional) attribute value
     * @param method (optional) attribute value
     * @param consumes (optional) attribute value
     * @param produces (optional) attribute value
     * @param params (optional) attribute value
     * @param headers (optional) attribute value
     * @return
     */
    public AnnotationMetadataBuilder getRequestMappingAnnotation(String value,
            String method, String consumes, String produces, String params,
            String headers) {
        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        if (value != null) {
            // @RequestMapping(value = "xx")
            methodAnnotation.addStringAttribute("value", value);
        }

        if (produces != null) {
            // @RequestMapping(... produces = "application/json")
            methodAnnotation.addStringAttribute("produces", produces);
        }
        if (consumes != null) {
            // @RequestMapping(... consumes = "application/json")
            methodAnnotation.addStringAttribute("consumes", consumes);
        }
        if (method != null) {
            // @RequestMapping(... method = "POST")
            methodAnnotation.addEnumAttribute("method", REQUEST_METHOD,
                    new JavaSymbolName(method));
        }
        if (params != null) {
            // @RequestMapping(... produces = "application/json")
            methodAnnotation.addStringAttribute("params", params);
        }
        if (headers != null) {
            // @RequestMapping(... produces = "application/json")
            methodAnnotation.addStringAttribute("headers", headers);
        }

        return methodAnnotation;
    }

}
