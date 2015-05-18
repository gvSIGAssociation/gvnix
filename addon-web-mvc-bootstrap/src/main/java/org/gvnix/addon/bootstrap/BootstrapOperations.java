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
package org.gvnix.addon.bootstrap;

import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.1
 */
public interface BootstrapOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX Bootstrap has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_BOOTSTRAP = "gvnix-bootstrap";

    /**
     * Indicate commands should be available
     *
     * @return true if it should be available, otherwise false
     */
    boolean isSetupCommandAvailable();

    /**
     * Indicate commands should be available
     *
     * @return true if it should be available, otherwise false
     */
    boolean isUpdateCommandAvailable();

    /**
     * Setup all Bootstrap artifacts (css, js, etc.. )
     */
    void setup();

    /**
     * Update all Bootstrap tags (css, js, etc.. )
     */
    void updateTags();

    /**
     *
     * This method checks if typical security is installed
     *
     * @return
     */
    boolean isTypicalSecurityInstalled();

    /**
     * This method checks if login was modified before
     *
     * @return
     */
    boolean isLoginModified();

    /**
     * This method updates security addon components to use Bootstrap
     */
    void updateSecurityAddonToBootstrap();

}