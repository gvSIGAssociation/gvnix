/*
 * Copyright 2011 DiSiD Technologies S.L.L. All rights reserved.
 *
 * Project  : DiSiD org.gvnix.web.exception.handler.roo.addon
 * SVN Id   : $Id$
 */
package org.gvnix.web.exception.handler.roo.addon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * gvNIX ModalDialog annotation
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GvNIXModalDialogs {
    String[] value() default "";
}
