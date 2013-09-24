package ${PACKAGE};

import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson {@link SimpleModule} which registers
 * {@link ConversionServiceBeanSerializerModifier}
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class ConversionServiceModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    private ConversionService conversionService = null;

    public ConversionServiceModule(ConversionService conversionService) {
        super();
        this.conversionService = conversionService;
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanSerializerModifier(new ConversionServiceBeanSerializerModifier(
                conversionService));
    }

}
