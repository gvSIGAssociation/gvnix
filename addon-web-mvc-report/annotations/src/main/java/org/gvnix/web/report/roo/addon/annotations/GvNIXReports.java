/*
 * Copyright 2015 DISID Corporation S.L. All rights reserved.
 *
 * Project : [PROJECT NAME]
 * SVN Id : $Id$
 */
package org.gvnix.web.report.roo.addon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * gvNIX Report annotation.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 0.6
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GvNIXReports {
    /**
     * @return an array of strings, with each string being the full name of a
     *         method that should be created as a generateReport
     */
    String[] value() default "";
}
