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
package org.gvnix.addon.jpa.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Trigger annotation for jpa audit user data provider.
 * <p/>
 * Annotated class will provider the user to store on audited entities and
 * revision log.
 * <p/>
 * <em>Only one,</em> class by project could be annotated whit this annotation.
 * <p/>
 * To select timestamp format, you can choose two ways:
 * <ul>
 * <li>Set {@link #auditDateTimeFormatPattern()}</li>
 * <li>Set {@link #auditDateTimeFormatStyle()}</li>
 * </ul>
 * By default will be used
 * 
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * @see org.springframework.format.annotation.DateTimeFormat
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXJpaAuditUserService {

    /**
     * @return Return the type to store as <em>user</em> on audit information
     */
    Class<?> userType() default String.class;

    /**
     * @return format pattern to use in audit timestamp fields. If is set
     *         {@link #auditDateFormat()} and {@link #auditTimeFormat()} will be
     *         ignored
     */
    String auditDateTimeFormatPattern() default "";

    /**
     * @return format style of audit timestamp fields. Ignored if
     *         {@link #auditDateTimeFormatPattern()} is specified.
     * @see org.springframework.format.annotation.DateTimeFormat
     */
    String auditDateTimeFormatStyle() default "MM";

}
