package ${PACKAGE};


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A custom Jackson ObjectMapper that installs JSON
 * serialization/deserialization support which uses
 * {@link ConversionService} to read/write object
 * values.
 * <p/>
 * This registers {@link ConversionServiceModule} and
 * {@link DataBinderModule}
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
