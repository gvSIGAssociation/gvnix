/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.web.mvc.addon.jquery;

import org.gvnix.addon.web.mvc.annotations.jquery.GvNIXWebJQuery;
import org.springframework.roo.addon.web.mvc.controller.addon.finder.WebFinderMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface for JQuery operations
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1
 */
public interface JQueryOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX JQuery has been setup in this project
     */
    public static final String FEATURE_NAME_GVNIX_JQUERY = "gvnix-jquery";

    /**
     * Annotate the provided web mvc controller with {@link GvNIXWebJQuery}
     * 
     * @param controller
     */
    void annotateController(JavaType controller);

    /**
     * Annotate all controllers with {@link GvNIXWebJQuery}
     */
    void annotateAll();

    /**
     * Indicate if add commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isAddAvailable();

    /**
     * Indicate if setup command should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupAvailable();

    /**
     * Indicate if updateTags command should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isUpdateTagsAvailable();

    /**
     * Setup all artifacts
     */
    void setup();

    /**
     * Updates CRUD JSP pages of target controller to use JQuery
     * 
     * @param controller
     * @param jqueryMetadata
     */
    void updateCrudJsp(JavaType controller, JQueryMetadata jqueryMetadata);

    /**
     * Updates find JSP pages of target controller to use JQuery
     * 
     * @param controller
     * @param findMetadata
     */
    void updateFindJsp(JavaType controller, WebFinderMetadata finderMetadata);

    /**
     * Update load-scripts.tagx with required JS, CSS, etc
     */
    void updateLoadScriptsTag();

    /**
     * Update all JQuery artifacts: TAGX, JS, CSS, etc.
     */
    void updateTags();

}