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
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
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
