/*
 * Copyright 2011 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project : DiSiD org.gvnix.web.report SVN Id : $Id$
 */
package org.gvnix.web.report.roo.addon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * gvNIX Report annotation.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
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
