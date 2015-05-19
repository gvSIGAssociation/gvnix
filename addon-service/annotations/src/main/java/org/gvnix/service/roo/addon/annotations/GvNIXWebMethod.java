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
package org.gvnix.service.roo.addon.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * gvNIX Annotation to identify gvNIX created WebMethods to publish as
 * operations in the Web Service.
 * </p>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface GvNIXWebMethod {

    public enum GvNIXWebMethodParameterStyle {
        BARE, WRAPPED
    }

    String operationName();

    Class<?> webResultType();

    String webResultPartName() default "parameters";

    boolean webResultHeader() default false;

    String action() default "";

    String resultName() default "return";

    String resultNamespace() default "";

    String requestWrapperName() default "";

    String requestWrapperNamespace() default "";

    String requestWrapperClassName() default "";

    String responseWrapperName() default "";

    String responseWrapperNamespace() default "";

    String responseWrapperClassName() default "";

    /**
     * SOAPBinding parameter style for Web Service operation.
     * 
     * @return
     */
    GvNIXWebMethodParameterStyle parameterStyle() default GvNIXWebMethodParameterStyle.WRAPPED;

}
