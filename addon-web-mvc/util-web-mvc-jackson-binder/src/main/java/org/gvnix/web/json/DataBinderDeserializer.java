/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.web.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.gvnix.web.json.DataBinderMappingJackson2HttpMessageConverter.DataBinderList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Jackson2 deserializer based on Spring DataBinder.
 * <p/>
 * This deserializer requires a {@link DataBinder} was stored in
 * {@link ThreadLocal} with key "{@link BindingResult#MODEL_KEY_PREFIX}" +
 * {@code "JSON_DataBinder"}
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since TODO: Class version
 */
public class DataBinderDeserializer extends BeanDeserializerBase {

    /**
     *
     */
    private static final long serialVersionUID = -7345091954698956061L;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DataBinderDeserializer.class);

    public DataBinderDeserializer(BeanDeserializerBase source) {
        super(source);
    }

    public DataBinderDeserializer(BeanDeserializerBase source,
            ObjectIdReader objectIdReader) {
        super(source, objectIdReader);
    }

    public DataBinderDeserializer(BeanDeserializerBase source,
            HashSet<String> ignorableProps) {
        super(source, ignorableProps);
    }

    /**
     * {@inheritDoc}
     * 
     * Uses {@link DataBinderDeserializer}
     */
    @Override
    public BeanDeserializerBase withObjectIdReader(ObjectIdReader objectIdReader) {
        return new DataBinderDeserializer(this, objectIdReader);
    }

    /**
     * {@inheritDoc}
     * 
     * Uses {@link DataBinderDeserializer}
     */
    @Override
    public BeanDeserializerBase withIgnorableProperties(
            HashSet<String> ignorableProps) {
        return new DataBinderDeserializer(this, ignorableProps);
    }

    /**
     * Deserializes JSON content into Map<String, String> format and then uses a
     * Spring {@link DataBinder} to bind the data from JSON message to JavaBean
     * objects.
     * <p/>
     * It is a workaround for issue
     * https://jira.springsource.org/browse/SPR-6731 that should be removed from
     * next gvNIX releases when that issue will be resolved.
     * 
     * @param parser Parsed used for reading JSON content
     * @param ctxt Context that can be used to access information about this
     *        deserialization activity.
     * 
     * @return Deserializer value
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonToken t = parser.getCurrentToken();
        MutablePropertyValues propertyValues = new MutablePropertyValues();

        // Get target from DataBinder from local thread. If its a bean
        // collection
        // prepares array index for property names. Otherwise continue.
        DataBinder binder = (DataBinder) ThreadLocalUtil
                .getThreadVariable(BindingResult.MODEL_KEY_PREFIX
                        .concat("JSON_DataBinder"));
        Object target = binder.getTarget();

        // For DstaBinderList instances, contentTarget contains the final bean
        // for binding. DataBinderList is just a simple wrapper to deserialize
        // bean wrapper using DataBinder
        Object contentTarget = null;

        if (t == JsonToken.START_OBJECT) {
            String prefix = null;
            if (target instanceof DataBinderList) {
                prefix = binder.getObjectName().concat("[")
                        .concat(Integer.toString(((Collection) target).size()))
                        .concat("].");

                // BeanWrapperImpl cannot create new instances if generics
                // don't specify content class, so do it by hand
                contentTarget = BeanUtils
                        .instantiateClass(((DataBinderList) target)
                                .getContentClass());
                ((Collection) target).add(contentTarget);
            }
            else if (target instanceof Map) {
                // TODO
                LOGGER.warn("Map deserialization not implemented yet!");
            }
            Map<String, String> obj = readObject(parser, ctxt, prefix);
            propertyValues.addPropertyValues(obj);
        }
        else {
            LOGGER.warn("Deserialization for non-object not implemented yet!");
            return null; // TODO?
        }

        // bind to the target object
        binder.bind(propertyValues);

        // Note there is no need to validate the target object because
        // RequestResponseBodyMethodProcessor.resolveArgument() does it on top
        // of including BindingResult as Model attribute

        // For DAtaBinderList the contentTarget contains the final bean to
        // make the binding, so we must return it
        if (contentTarget != null) {
            return contentTarget;
        }
        return binder.getTarget();
    }

    /**
     * Deserializes JSON object into Map<String, String> format to use it in a
     * Spring {@link DataBinder}.
     * <p/>
     * Iterate over every object's property and delegates on
     * {@link #readField(JsonParser, DeserializationContext, JsonToken, String)}
     * 
     * @param parser JSON parser
     * @param ctxt context
     * @param prefix object DataBinder path
     * @return property values
     * @throws IOException
     * @throws JsonProcessingException
     */
    public Map<String, String> readObject(JsonParser parser,
            DeserializationContext ctxt, String prefix) throws IOException,
            JsonProcessingException {
        JsonToken t = parser.getCurrentToken();

        if (t == JsonToken.START_OBJECT) {
            t = parser.nextToken();
            // Skip it to locate on first object data token
        }

        // Deserialize object properties
        Map<String, String> deserObj = new HashMap<String, String>();
        for (; t != JsonToken.END_OBJECT; t = parser.nextToken()) {
            Map<String, String> field = readField(parser, ctxt, t, prefix);
            deserObj.putAll(field);
        }
        return deserObj;
    }

    /**
     * Deserializes JSON array into Map<String, String> format to use it in a
     * Spring {@link DataBinder}.
     * <p/>
     * Iterate over every array's item to generate a prefix for property names
     * on DataBinder style (
     * <em>{prefix}[{index}].<em>) and delegates on {@link #readField(JsonParser, DeserializationContext, JsonToken, String)}
     * 
     * @param parser JSON parser
     * @param ctxt context
     * @param prefix array dataBinder path
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    protected Map<String, String> readArray(JsonParser parser,
            DeserializationContext ctxt, String prefix) throws IOException,
            JsonProcessingException {
        JsonToken t = parser.getCurrentToken();

        if (t == JsonToken.START_ARRAY) {
            t = parser.nextToken();
            // Skip it to locate on first array data token
        }

        // Deserialize array properties
        int i = 0;
        Map<String, String> deserObj = new HashMap<String, String>();
        for (; t != JsonToken.END_ARRAY; t = parser.nextToken()) {
            // Property name must include prefix this way:
            // degrees[0].description
            Map<String, String> field = readField(parser, ctxt, t, prefix
                    .concat("[").concat(Integer.toString(i++)).concat("]."));
            deserObj.putAll(field);
        }
        return deserObj;
    }

    /**
     * Deserializes JSON property into Map<String, String> format to use it in a
     * Spring {@link DataBinder}.
     * <p/>
     * Check token's type to perform an action:
     * <ul>
     * <li>If it's a property, stores it in map</li>
     * <li>If it's an object, calls to
     * {@link #readObject(JsonParser, DeserializationContext, String)}</li>
     * <li>If it's an array, calls to
     * {@link #readArray(JsonParser, DeserializationContext, String)}</li>
     * </ul>
     * 
     * @param parser
     * @param ctxt
     * @param token current token
     * @param prefix property dataBinder path
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    protected Map<String, String> readField(JsonParser parser,
            DeserializationContext ctxt, JsonToken token, String prefix)
            throws IOException, JsonProcessingException {

        String fieldName = null;
        String fieldValue = null;

        // Read the field name
        fieldName = parser.getCurrentName();

        // If current token contains a field name
        if (!isEmptyString(fieldName)) {

            // Append the prefix if given
            if (isEmptyString(prefix)) {
                fieldName = parser.getCurrentName();
            }
            else {
                fieldName = prefix.concat(parser.getCurrentName());
            }
        }
        // If current token contains mark array or object start markers.
        // Note it cannot be a field value because it will be read below and
        // then the token is advanced to the next
        else {

            // Use the prefix in recursive calls
            if (!isEmptyString(prefix)) {
                fieldName = prefix;
            }
        }

        // If current token has been used to read the field name, advance
        // stream to the next token that contains the field value
        if (token == JsonToken.FIELD_NAME) {
            token = parser.nextToken();
        }

        // Field value
        switch (token) {
        case VALUE_STRING:
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
        case VALUE_EMBEDDED_OBJECT:
        case VALUE_TRUE:
        case VALUE_FALSE:
            // Plain field: Store value
            Map<String, String> field = new HashMap<String, String>();
            fieldValue = parser.getText();
            field.put(fieldName, fieldValue);
            return field;
        case START_ARRAY:
            // Read array items
            return readArray(parser, ctxt, fieldName);
        case START_OBJECT:
            // Read object properties
            return readObject(parser, ctxt, fieldName);
        case END_ARRAY:
        case END_OBJECT:
            // Skip array and object end markers
            parser.nextToken();
            break;
        default:
            throw ctxt.mappingException(getBeanClass());
        }
        return Collections.emptyMap();
    }

    /**
     * @param string
     * @return true if string is null or is empty (ignore spaces)
     */
    private boolean isEmptyString(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * {@inheritDoc}
     * 
     * Not used
     */
    @Override
    public Object deserializeFromObject(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        // Not used
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * Not used
     */
    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        // Not used
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * Not used
     */
    @Override
    protected Object _deserializeUsingPropertyBased(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        // Not used
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * Not used
     */
    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(
            NameTransformer unwrapper) {
        // Not used
        return null;
    }
}
