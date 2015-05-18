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

import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson Serializer which uses {@link ConversionService} to transform value
 * before serialize it.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since TODO: Class version
 */
public class ConversionServicePropertySerializer extends StdSerializer<Object> {

    private final ConversionService conversionService;
    private final TypeDescriptor sourceType;
    private final TypeDescriptor targetType;

    public ConversionServicePropertySerializer(
            ConversionService conversionService, TypeDescriptor sourceType,
            TypeDescriptor targetType) {
        super(Object.class);
        this.conversionService = conversionService;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        try {
            jgen.writeObject(this.conversionService.convert(value, sourceType,
                    targetType));
        }
        catch (ConversionException ex) {
            // conversion exception occurred
            throw new JsonGenerationException(ex);
        }
        catch (IllegalArgumentException ex) {
            // targetType is null
            throw new JsonGenerationException(ex);
        }
    }
}
