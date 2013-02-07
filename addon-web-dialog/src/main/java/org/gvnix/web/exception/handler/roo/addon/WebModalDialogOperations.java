/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
 * Valenciana
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
package org.gvnix.web.exception.handler.roo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface for {@link WebModalDialogOperationsImpl}.
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WebModalDialogOperations {

    /**
     * Check if project is available and if its a Spring MVC project
     * 
     * @return
     */
    public boolean isProjectAvailable();

    /**
     * Annotates given controllerClass with {@link GvNIXModalDialog} or updates
     * the value of the existing annotation
     * 
     * @param controllerClass
     * @param name
     */
    public void addModalDialogAnnotation(JavaType controllerClass,
            JavaSymbolName name);

    /**
     * Annotates given controllerClass with {@link GvNIXModalDialog} with empty
     * attribute value
     * 
     * @param controllerClass
     */
    public void addDefaultModalDialogAnnotation(JavaType controllerClass);

    /**
     * Setup modal dialogs support for current project
     * <ul>
     * <li>Setup maven dependency</li>
     * <li>Installs bean Dialog.java</li>
     * <li>Installs MVC Artifacts</li>
     * </ul>
     */
    public void setupModalDialogsSupport();

    /**
     * Add addon repository and dependency to get annotations.
     * 
     * @param configuration Configuration element
     */
    public void setupMavenDependency();

    /**
     * Returns true if message-box shows messages as modal dialogs, false
     * otherwise.
     * 
     * @return
     */
    public boolean isMessageBoxOfTypeModal();

}
