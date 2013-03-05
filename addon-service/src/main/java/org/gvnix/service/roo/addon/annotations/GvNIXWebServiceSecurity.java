/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
 * GvNix Annotation to configure Security of a GvNIX imported as WebService.
 * </p>
 * 
 * @author Jose Manuel Vivó Arnal ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
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
