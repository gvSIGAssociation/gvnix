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

package org.gvnix.addon.loupefield.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1
 */
public interface LoupefieldOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX Bootstrap has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_LOUPEFIELDS = "gvnix-loupe";

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
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetCommandAvailable();

    /**
     * Setup all add-on artifacts
     */
    void setup();

    /**
     * Update all add-on artifacts
     */
    void update();

    /**
     * Set Controller as Loupe Controller
     */

    void setLoupeController(JavaType controller);

    /**
     * Set Field as Loupe Element
     */
    void setLoupeField(JavaType controller, JavaSymbolName field,
            String additionalFields, String caption, String baseFilter,
            String listPath, String max);

}