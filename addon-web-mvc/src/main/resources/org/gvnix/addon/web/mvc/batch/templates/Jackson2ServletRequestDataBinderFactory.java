package ${PACKAGE};

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
 * @author gvNIX Team
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
