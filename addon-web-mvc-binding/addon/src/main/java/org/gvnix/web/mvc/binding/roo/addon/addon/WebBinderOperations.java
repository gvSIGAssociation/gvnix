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

package org.gvnix.web.mvc.binding.roo.addon.addon;

import org.springframework.roo.model.JavaType;

/**
 * Operations of Add-on
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.8
 */
public interface WebBinderOperations {

    /**
     * Indicate commands should be available
     *
     * @return true if it should be available, otherwise false
     */
    boolean isStringTrimmerAvailable();

    /**
     * Annotate provided Java type with @GvNIXStringTrimmerBinder
     *
     * @param emptyAsNull
     */
    void bindStringTrimmer(JavaType controller, boolean emptyAsNull);

    /**
     * Annotate all Java types annotated with @Controller with @GvNIXStringTrimmerBinder
     *
     * @param emptyAsNull
     */
    void bindStringTrimmerAll(boolean emptyAsNull);

    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();
}
