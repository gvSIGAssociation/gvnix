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
package org.gvnix.addon.datatables.addon;

import org.gvnix.addon.datatables.annotations.GvNIXDatatables;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface for Datatables operations
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
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
     * Annotate the provided web mvc controller with {@link GvNIXDatatables}
     * 
     * @param controller
     * @param ajax
     * @param mode
     * @param inlineEditing
     * @param baseFilter
     */

    void annotateController(JavaType controller, boolean ajax, String mode,
            boolean inlineEditing, JavaSymbolName baseFilter);

    /**
     * Annotate the provided web mvc controller with {@link GvNIXDatatables}
     * 
     * @param controller of master datatables
     * @param property of controller entity for detail
     */
    void annotateDetailController(JavaType controller, String property);

    /**
     * Annotate all controllers with {@link GvNIXDatatables}
     * 
     * @param ajax
     */
    void annotateAll(boolean ajax);

    /**
     * Setup all artifacts
     * 
     * @param webPackage (optional) controllers package. Only required if no
     *        conversionService registered.
     */
    void setup(JavaPackage webPackage);

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
     * Updates all jspx pages of target controller to use datatables component
     * 
     * @param controller
     * @param datatablesMetadata
     */
    void updateControllerJspPages(JavaType controller,
            DatatablesMetadata datatablesMetadata);

    /**
     * Remove <code>page</code> and <code>size</code> parameters from list menu
     * link for target controller
     * 
     * @param controller
     * @deprecated
     */
    void updateListMenuUrl(JavaType controller);

    /**
     * Add required JS to load-scripts.tagx
     */
    void addJSToLoadScriptsTag();

    /**
     * updates webmvc-config.xml file
     * 
     * @param destinationPackage (optional) only required if no
     *        conversionService found on project
     */
    void updateWebMvcConfigFile(JavaPackage destinationPackage);

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

    /**
     * This method updates Datatable elements to use Bootstrap
     */
    void updateDatatablesAddonToBootstrap();

}