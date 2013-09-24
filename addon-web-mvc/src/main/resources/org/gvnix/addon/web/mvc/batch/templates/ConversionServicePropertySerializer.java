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
