/*
 * Copyright 2015 DISID Corporation S.L. All rights reserved.
 *
 * Project : [PROJECT NAME]
 * SVN Id : $Id$
 */
package org.gvnix.web.exception.handler.roo.addon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * gvNIX ModalDialog annotation
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 0.8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXModalDialogs {
    String[] value() default "";
}
