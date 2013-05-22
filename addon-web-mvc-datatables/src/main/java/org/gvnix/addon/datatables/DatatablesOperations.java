/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
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
 * this program. If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.datatables;

import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface for Datatables operations
 * 
 * @author gvNIX Team
 * @since 1.1
 */
public interface DatatablesOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX DATATABLES has been setup in this
     * project
     */
    public static final String FEATURE_NAME_GVNIX_DATATABLES = "gvnix-datatables";

    /**
     * Indicate if add commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isAddAvailable();

    /**
     * Annotate the provided web mvc controller with {@link GvNIXDatatables}
     * 
     * @param controller
     * @param ajax
     */
    void annotateController(JavaType controller, boolean ajax);

    /**
     * Annotate all controllers with {@link GvNIXDatatables}
     * 
     * @param ajax
     */
    void annotateAll(boolean ajax);

    /**
     * Setup all artifacts
     */
    void setup();

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
     * Update all datatables artifacts (tags, images, js, etc...)
     */
    void updateTags();

    /**
     * Updates de list.jspx page of target controller to use datatables
     * component
     * 
     * @param controller
     */
    void updateControllerListJsp(JavaType controller);

    /**
     * Remove <code>page</code> and <code>size</code> parameters from list menu
     * link for target controller
     * 
     * @param controller
     */
    void updateListMenuUrl(JavaType controller);

    /**
     * Add required JS to load-scripts.tagx
     */
    void addJSToLoadScriptsTag();

    /**
     * Updates the webmvn-config.xml file
     */
    void updateWebMvcConfigFile();

    /**
     * updates the web.xml file to add filters and servlets definitions. <br>
     * This inserts filter/filter-mapping and servlet/servlet-mapping tags
     * required for datatables.
     */
    void updateWebXmlFile();

    /**
     * Copy base datatables.properties file to resources folder
     */
    void copyPropertiesFile();

    /**
     * Add required i18n keys
     */
    void addI18nKeys();
}