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

import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

/**
 * Overrides {@link ServletRequestDataBinderFactory} to use the
 * {@link DataBinder} in current Thread used in deserialization process that
 * contains the {@link BindingResult}.
 * <p/>
 * Note by doing that the {@link BindingResult} of JSON deserialization process
 * will be send to Controller as method parameter.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class Jackson2ServletRequestDataBinderFactory extends
        ServletRequestDataBinderFactory {

    /**
     * Create a new instance.
     * 
     * @param binderMethods one or more {@code @InitBinder} methods
     * @param initializer provides global data binder initialization
     */
    public Jackson2ServletRequestDataBinderFactory(
            List<InvocableHandlerMethod> binderMethods,
            WebBindingInitializer initializer) {
        super(binderMethods, initializer);
    }

    /**
     * Look current Thread for {@link ServletRequestDataBinder} created by
     * {@link DataBinderMappingJackson2HttpMessageConverter}, if found return
     * it, otherwise it delegates on parent method.
     * 
     * @param target
     * @param objectName
     * @param request
     * @return ServletRequestDataBinder
     */
    @Override
    protected ServletRequestDataBinder createBinderInstance(Object target,
            String objectName, NativeWebRequest request) {
        try {
            ServletRequestDataBinder binder = (ServletRequestDataBinder) ThreadLocalUtil
                    .getThreadVariable(BindingResult.MODEL_KEY_PREFIX
                            .concat("JSON_DataBinder"));
            if (binder != null) {
                return binder;
            }
            return super.createBinderInstance(target, objectName, request);
        }
        finally {
            ThreadLocalUtil.destroy();
        }
    }

}
