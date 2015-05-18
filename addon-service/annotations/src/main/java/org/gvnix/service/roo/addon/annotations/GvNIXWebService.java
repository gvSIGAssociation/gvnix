/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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

import java.lang.annotation.*;

/**
 * <p>
 * GvNix Annotation to identify GvNIX publish as WebService.
 * </p>
 * 
* @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GvNIXWebService {

    public enum GvNIXWebServiceParameterStyle {
        BARE, WRAPPED
    }

    /**
     * Web Service portType
     * 
     * @return
     */
    String name();

    /**
     * Web service namespace.
     * 
     * @return
     */
    String targetNamespace();

    /**
     * Web Service Name.
     * 
     * @return
     */
    String serviceName();

    /**
     * Address to publish the service.
     * 
     * @return
     */
    String address();

    /**
     * Java fully qualified name.
     * <p>
     * Package + Class name
     * </p>
     * 
     * @return
     */
    String fullyQualifiedTypeName();

    /**
     * true if Web Services has been created from wsdl using the Add-on.
     * <p>
     * If is false, operation input/output parameters and Exceptions involved
     * have to be checked.
     * </p>
     * 
     * @return
     */
    boolean exported();

    /**
     * SOAPBinding parameter style for Web Service.
     * 
     * @return
     */
    GvNIXWebServiceParameterStyle parameterStyle() default GvNIXWebServiceParameterStyle.WRAPPED;

}
