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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Overrides {@link RequestMappingHandlerAdapter} to create custom
 * {@link ServletRequestDataBinderFactory}.
 * <p/>
 * {@link ServletRequestDataBinderFactory} will let us to recover the
 * {@link DataBinder} used in deserialization process and place the
 * {@link BindingResult} as Controller method parameter.
 * <p/>
 * It only handles request which {@link HandlerMethod}'s
 * {@link RequestMapping#consumes()} is equals to "{@code application/json}"
 * (and no more <em>consumes</em> types declared).
 * <p/>
 * To activate this adapter you must declare it on project's webmvc-config.xml.
 * Example:
 * 
 * <pre>
 * {@code
 * 
 *     <!--
 *       Configures JSON content handling:
 *       - Registers custom Jackson2 MessageConverter
 *       - Registers custom Jackson2 ServletRequestDataBinderFactory to take
 *         binding errors in account
 *     -->
 *     <bean id="dataBinderRequestMappingHandlerAdapter" p:order="1"
 *             class="org.gvnix.web.json.Jackson2RequestMappingHandlerAdapter">
 *         <!-- Custom Jackson ObjectMapper delegates object 
 *           serialization/deserialization to Spring ConversionService -->
 *         <property name="objectMapper">
 *             <bean class="org.gvnix.web.json.ConversionServiceObjectMapper" />
 *         </property>
 *     </bean>
 *     
 * }
 * </pre>
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class Jackson2RequestMappingHandlerAdapter extends
        RequestMappingHandlerAdapter {

    private ObjectMapper objectMapper;
    private ConversionService conversionService;
    private Validator validator;

    /**
     * Default constructor.
     */
    @Autowired
    public Jackson2RequestMappingHandlerAdapter(
            ConversionService conversionService, Validator validator) {
        super();
        this.conversionService = conversionService;
        this.validator = validator;
    }

    /**
     * Overrides the default implementation to create a
     * {@link Jackson2ServletRequestDataBinderFactory} instance.
     * 
     * @param binderMethods {@code @InitBinder} methods
     * @return the Jackson2ServletRequestDataBinderFactory instance to use
     * @throws Exception in case of invalid state or arguments
     */
    protected ServletRequestDataBinderFactory createDataBinderFactory(
            List<InvocableHandlerMethod> binderMethods) throws Exception {
        return new Jackson2ServletRequestDataBinderFactory(binderMethods,
                getWebBindingInitializer());
    }

    /**
     * Setup custom {@link DataBinderMappingJackson2HttpMessageConverter}
     */
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        DataBinderMappingJackson2HttpMessageConverter msgConverter = new DataBinderMappingJackson2HttpMessageConverter(
                this.conversionService, this.validator);
        msgConverter.setObjectMapper(this.objectMapper);
        getMessageConverters().add(msgConverter);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Check if handler method consumes json (and just json) messages to handle
     * it
     * 
     * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#supportsInternal(org.springframework.web.method.HandlerMethod)
     */
    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {

        // Get requestMapping annotation
        RequestMapping requestMappingAnnotation = handlerMethod
                .getMethodAnnotation(RequestMapping.class);
        if (requestMappingAnnotation == null) {
            // No annotation: don't handle
            return false;
        }

        // Get consumes configuration
        String[] consumes = requestMappingAnnotation.consumes();
        if (consumes == null || consumes.length != 1) {
            // No consumes configuration or multiple consumes: don't handle
            return false;
        }

        // Check consume value
        // TODO extract a constant
        if (!"application/json".equals(consumes[0])) {
            // Don't consumes json: don't handle
            return false;
        }

        // Delegate on super for additional checks
        return super.supportsInternal(handlerMethod);
    }

}
