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
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since TODO: Class version
 */
public class Jackson2RequestMappingHandlerAdapter extends
        RequestMappingHandlerAdapter {

    private ObjectMapper objectMapper;
    private final ConversionService conversionService;
    private final Validator validator;

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
