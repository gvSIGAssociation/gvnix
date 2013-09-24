package ${PACKAGE};

/**
 * Store needed variables, i.e. DataBinder, to be used in Jackson 2 
 * deserialization process components. 
 * <p/>
 * Note another solution would be passing these variables as a parameters to 
 * all the Jackson 2 components methods. But this is not a good solution as 
 * the code is redundant, unnecessary and some times not possible to customize.
 * <p/>
 * Ref: <a href="http://www.javacodegeeks.com/2012/05/threading-stories-threadlocal-in-web.html">ThreadLocal in web applications</a>
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class ThreadLocalUtil {

    private final static ThreadLocal<ThreadVariables> THREAD_VARIABLES = new ThreadLocal<ThreadVariables>() {

        @Override
        protected ThreadVariables initialValue() {
            return new ThreadVariables();
        }
    };

    public static Object getThreadVariable(String name) {
        return THREAD_VARIABLES.get().get(name);
    }

    public static void setThreadVariable(String name, Object value) {
        THREAD_VARIABLES.get().put(name, value);
    }

    public static void destroy() {
        THREAD_VARIABLES.remove();
    }
}

