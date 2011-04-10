/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.occ.roo.addon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * <b>Optimistic Concurrency Control of gvNIX</b>
 * </p>
 *
 * <p>
 * This annotation manages the OCC aspect for a Entity that hasn't a persistent
 * {@link javax.persistence.Version} and no way to have it.
 * </p>
 *
 * <p>
 * The control is based on a MD5 value computed form a string generated over all
 * persistent entity properties values.
 * </p>
 *
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXEntityOCCChecksum {

    /**
     * <p>
     * Name for checksum entity field. If dosen't exist the field will be
     * created.
     * </p>
     *
     * <p>
     * <code>occChecksum</code> is the default value.
     * </p>
     *
     *
     * @return the name of the identifier field to use (defaults to "id"; must
     *         be provided)
     */
    String fieldName() default "occChecksum";

    /**
     * <p>
     * Digest Method to use for checksum's compute.
     * </p>
     *
     * <p>
     * <code>md5</code> is the default value.
     * </p>
     *
     * For more information about values see
     * {@link java.security.MessageDigest#getInstance(String)}
     *
     * @return the name of the identifier field to use (defaults to "id"; must
     *         be provided)
     */
    String digestMethod() default "md5";

}
