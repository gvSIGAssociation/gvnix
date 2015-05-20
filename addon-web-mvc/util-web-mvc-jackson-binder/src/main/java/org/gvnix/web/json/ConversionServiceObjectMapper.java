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
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
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
