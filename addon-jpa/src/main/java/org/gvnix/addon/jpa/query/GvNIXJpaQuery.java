/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.query;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for generate information for create dynamic JPA query.<br>
 * This include information about filter and order properties to use in fields
 * which refer to other entities (one-to-many, many-to-many, one-to-one and
 * many-to-one properties).<br>
 * Entities than use this annotation must add it on entity <em>and</em> in every
 * relation-field to manage.<br>
 * Annotation applied to a non-supported field will be ignored.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Target({ TYPE, FIELD })
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXJpaQuery {

    /**
     * Properties (of related entity) to use in filter by this annotated field. <br>
     * <em>Only for annotation applied to a relation field</em>.
     * 
     * @return
     */
    String[] filterBy() default "";

    /**
     * Properties (of related entity) to use in order by this annotated field. <br>
     * <em>Only for annotation applied to a relation field</em>.
     * 
     * @return
     */
    String[] orderBy() default "";

}
