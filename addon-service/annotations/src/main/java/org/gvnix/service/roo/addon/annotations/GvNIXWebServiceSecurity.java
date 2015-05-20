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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * gvNIX Annotation to configure Security of a gvNIX imported as WebService.
 * </p>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GvNIXWebServiceSecurity {

    /**
     * @return alias name in certificate keystore
     * @deprecated to change this value use related property file or use dynamic
     *             configuration
     */
    String alias() default "";

    /**
     * @return certificate keystore file name. (this file must be in the very
     *         same package of target class)
     * @deprecated to change this value use related property file or use dynamic
     *             configuration
     */
    String certificate() default "";

    /**
     * @return password for certificate keystore and alias
     * @deprecated to change this value use related property file or use dynamic
     *             configuration
     */
    String password() default "";
}
