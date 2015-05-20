/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.datatables.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Web MVC Controllers which list uses JQuery datatables
 * component.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXDatatables {

    String TABLE = "";
    String SHOW = "show";

    /**
     * Defines how datatables gets data (default AJAX)
     */
    boolean ajax() default true;

    /**
     * Specified table view mode:
     * <ul>
     * <li>If <code>null</code> or empty datatables show a standard list</li>
     * <li>Otherwise, datatables will show just-one cell per row and one row per
     * page with specified page (this value) rendered inside of it</li>
     * </ul>
     * <br>
     * Example: <code>show</code> Will display datatables one-row-page one-cell
     * datatables and cell contents will be the <code>show.jspx</code> of
     * current entity rendered with item information
     */
    String mode() default TABLE;

    /**
     * @return an array of relation fields show in datatables
     */
    String[] detailFields() default "";

    /**
     * @return if user could edit elements in-line (inside the table)
     */
    boolean inlineEditing() default false;

    /**
     * 
     * @return string to generate filter
     */
    String baseFilter() default "";
}
