package ${PACKAGE};

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

/**
 * Jackson {@link BeanDeserializerModifier} which
 * return {@link DataBinderDeserializer}.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class DataBinderBeanDeserializerModifier extends
        BeanDeserializerModifier {

    public DataBinderBeanDeserializerModifier() {
        super();
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
            BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return new DataBinderDeserializer((BeanDeserializerBase) deserializer);
    }

}
