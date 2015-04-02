/*
 * Copyright (C) 2009 - CONSELLERIA D'INFRAESTRUCTURES I TRANSPORT GENERALITAT
 * VALENCIANA
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/gpl-2.0.html
 */
package org.gvnix.web.mvc.binding.roo.addon.addon;

import org.springframework.roo.model.JavaType;

/**
 * Operations of Add-on
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
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
