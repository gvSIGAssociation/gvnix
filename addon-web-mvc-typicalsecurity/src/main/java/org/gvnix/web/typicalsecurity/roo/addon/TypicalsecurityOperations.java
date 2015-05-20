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

package org.gvnix.web.typicalsecurity.roo.addon;

/**
 * Interface of commands that are available via the Roo shell.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a> *
 * @since 1.1
 */
public interface TypicalsecurityOperations {

    boolean isCommandAvailable();

    String setup(String entityPackage, String controllerPackage);

    /**
     * This method checks if typical security is installed. If is installed
     * update views to use bootstrap
     */
    void updateTypicalSecurityAddonToBootstrap();

    boolean isTypicalSecurityInstalled();

    boolean isLoginModified();
}