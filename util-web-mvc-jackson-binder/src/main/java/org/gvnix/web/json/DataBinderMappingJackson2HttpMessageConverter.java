/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gvnix.web.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Overrides {@link MappingJackson2HttpMessageConverter} to customize the call
 * to {@link ObjectMapper}.
 * <p/>
 * Before call to {@link ObjectMapper#readValue(java.io.InputStream, Class)}
 * this class creates a {@link ServletRequestDataBinder} and put it to current
 * Thread in order to be used by the {@link DataBinderDeserializer}.
 * <p/>
 * Moreover if incoming JSON content is an array, this class creates a
 * {@link DataBinderList} and setup it as DataBinder target object for success
 * deserialization and binding errors storing.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class DataBinderMappingJackson2HttpMessageConverter extends
        MappingJackson2HttpMessageConverter {

    private final ConversionService conversionService;
    private final Validator validator;

    /**
     * Default constructor
     * 
     * @param cs
     * @param validator
     */
    public DataBinderMappingJackson2HttpMessageConverter(ConversionService cs,
            Validator validator) {
        super();
        this.conversionService = cs;
        this.validator = validator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    /**
     * {@inheritDoc}
     */
    public Object read(Type type, Class<?> contextClass,
            HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {

        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    /**
     * Before call to {@link ObjectMapper#readValue(java.io.InputStream, Class)}
     * creates a {@link ServletRequestDataBinder} and put it to current Thread
     * in order to be used by the {@link DataBinderDeserializer}.
     * <p/>
     * Ref: <a href=
     * "http://java.dzone.com/articles/java-thread-local-%E2%80%93-how-use">When
     * to use Thread Local?</a>
     * 
     * @param javaType
     * @param inputMessage
     * @return
     */
    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) {
        try {
            Object target = null;
            String objectName = null;

            // CRear el DataBinder con un target object en funcion del javaType,
            // ponerlo en el thread local
            Class<?> clazz = javaType.getRawClass();
            if (Collection.class.isAssignableFrom(clazz)) {
                Class<?> contentClazz = javaType.getContentType().getRawClass();
                target = new DataBinderList<Object>(contentClazz);
                objectName = "list";
            }
            else if (Map.class.isAssignableFrom(clazz)) {
                // TODO Class<?> contentClazz =
                // javaType.getContentType().getRawClass();
                target = CollectionFactory.createMap(clazz, 0);
                objectName = "map";
            }
            else {
                target = BeanUtils.instantiateClass(clazz);
                objectName = "bean";
            }

            WebDataBinder binder = new ServletRequestDataBinder(target,
                    objectName);
            binder.setConversionService(this.conversionService);
            binder.setAutoGrowNestedPaths(true);
            binder.setValidator(validator);

            ThreadLocalUtil.setThreadVariable(
                    BindingResult.MODEL_KEY_PREFIX.concat("JSON_DataBinder"),
                    binder);

            Object value = getObjectMapper().readValue(inputMessage.getBody(),
                    javaType);

            return value;
        }
        catch (IOException ex) {
            throw new HttpMessageNotReadableException(
                    "Could not read JSON: ".concat(ex.getMessage()), ex);
        }
    }

    /**
     * Bean that enable us to bind and validate a list of objects.
     */
    class DataBinderList<T> implements Collection<T> {

        @Valid
        protected List<T> list = null;
        protected Class<?> contentClass = null;

        public DataBinderList() {
            this.list = new ArrayList<T>();
        }

        public DataBinderList(Class<?> contentClazz) {
            this.list = new ArrayList<T>();
            this.contentClass = contentClazz;
        }

        // Bean interface needed for success data binding ---

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }

        public Class<?> getContentClass() {
            return contentClass;
        }

        public void setContentClass(Class<?> contentClass) {
            this.contentClass = contentClass;
        }

        // Collection interface ---

        @Override
        public boolean add(T obj) {
            return list.add(obj);
        }

        @Override
        public boolean addAll(Collection<? extends T> objects) {
            return list.addAll(objects);
        }

        @Override
        public void clear() {
            list.clear();
        }

        @Override
        public boolean contains(Object obj) {
            return list.contains(obj);
        }

        @Override
        public boolean containsAll(Collection<?> objects) {
            return list.containsAll(objects);
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }

        @Override
        public boolean remove(Object obj) {
            return list.remove(obj);
        }

        @Override
        public boolean removeAll(Collection<?> objects) {
            return list.removeAll(objects);
        }

        @Override
        public boolean retainAll(Collection<?> objects) {
            return list.retainAll(objects);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @SuppressWarnings("hiding")
        @Override
        public <T> T[] toArray(T[] objects) {
            return list.toArray(objects);
        }
    }
}
