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
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
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
