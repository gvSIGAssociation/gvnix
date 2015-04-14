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
package org.gvnix.web.json;

/**
 * Store needed variables, i.e. DataBinder, to be used in Jackson 2
 * deserialization process components.
 * <p/>
 * Note another solution would be passing these variables as a parameters to all
 * the Jackson 2 components methods. But this is not a good solution as the code
 * is redundant, unnecessary and some times not possible to customize.
 * <p/>
 * Ref: <a href=
 * "http://www.javacodegeeks.com/2012/05/threading-stories-threadlocal-in-web.html"
 * >ThreadLocal in web applications</a>
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
