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
package org.gvnix.addon.jpa.annotations.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Trigger annotation for jpa audit.
 * <p/>
 * For entities annotated with it, gvNIX will generate files to store dates and
 * user which perform changes (create or update) on any entity instance.
 * <p/>
 * To allow automatic fill/update of this fields there must be a class annotated
 * with {@link GvNIXJpaAuditListener} pointing to current entity-class on it
 * {@link GvNIXJpaAuditListener#entity()}
 * 
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXJpaAudit {

    public static enum StoreRevisionLog {
        PROVIDER_DEFAULT, YES, NO
    };

    /**
     * @return Configuration about store revision log in this entity
     */
    StoreRevisionLog storeRevisionLog() default StoreRevisionLog.PROVIDER_DEFAULT;
}
