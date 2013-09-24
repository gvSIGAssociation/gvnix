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
package ${PACKAGE};

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
 * Jackson Serializer which uses {@link ConversionService} to
 * transform value before serialize it.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class ConversionServicePropertySerializer extends StdSerializer<Object> {

    private ConversionService conversionService;
    private TypeDescriptor sourceType;
    private TypeDescriptor targetType;

    public ConversionServicePropertySerializer(ConversionService conversionService, TypeDescriptor sourceType, TypeDescriptor targetType) {
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
            jgen.writeObject(this.conversionService
                    .convert(value, sourceType, targetType));
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
