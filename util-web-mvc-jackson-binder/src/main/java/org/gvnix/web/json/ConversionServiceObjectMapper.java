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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A custom Jackson ObjectMapper that installs JSON
 * serialization/deserialization support which uses {@link ConversionService} to
 * read/write object values.
 * <p/>
 * This registers {@link ConversionServiceModule} and {@link DataBinderModule}
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class ConversionServiceObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 1L;

    /**
     * Register custom serialization and deserialization modules.
     * 
     * @param conversionService
     * @param validator
     */
    @Autowired
    public ConversionServiceObjectMapper(
            final ConversionService conversionService, final Validator validator) {

        // Register a module to add provider for custom bean deserializer
        registerModule(new ConversionServiceModule(conversionService));

        // Register a module to add provider for custom bean serializer
        registerModule(new DataBinderModule());
    }

}
