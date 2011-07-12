/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.exception.handler.roo.addon;

/**
 * Interface for {@link SharedOperationsImpl}.
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
public interface SharedOperations {

    /**
     * Installs the Java class given by its className.
     * <p>
     * It expects only "MessageMappingExceptionResolver" or "Dialog" as possible
     * parameter
     * 
     * @param className
     * @return
     */
    public String installWebServletClass(String className);

    /**
     * Installs MVC Artifacts into current project<br/>
     * Artifacts installed:<br/>
     * <ul>
     * <li>message-box.tagx</li>
     * </ul>
     * Modify default.jspx layout adding in the right position the element
     * &lt;util:message-box /&gt;
     * <p>
     * Also adds needed i18n properties to right message_xx.properties files
     */
    public void installMvcArtifacts();

}
