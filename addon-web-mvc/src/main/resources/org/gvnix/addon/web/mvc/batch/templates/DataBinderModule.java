package ${PACKAGE};

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Register {@link DataBinderBeanDeserializerModifier} into 
 * Jackson desarializer context.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class DataBinderModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public DataBinderModule() {
        super();
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new DataBinderBeanDeserializerModifier() );
    }
}
