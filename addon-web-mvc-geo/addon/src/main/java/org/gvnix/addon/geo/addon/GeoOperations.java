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

package org.gvnix.addon.geo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.4.0
 */
public interface GeoOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX GEO component has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_GEO_WEB_MVC = "gvnix-geo-web-mvc";

    static final String FT_DESC_GVNIX_GEO = "Geo Component";

    /**
     * This method checks if setup command is available
     *
     * @return true if setup command is available
     */
    boolean isSetupCommandAvailable();

    /**
     * This method checks if add map command is available
     *
     * @return true if map command is available
     */
    boolean isMapCommandAvailable();

    /**
     * This method checks if web mvc geo all command is available
     *
     * @return true if web mvc geo all command is available
     */
    boolean isAllCommandAvailable();

    /**
     * This method checks if web mvc geo add command is available
     *
     * @return true if web mvc geo add command is available
     */
    boolean isAddCommandAvailable();

    /**
     * This method checks if web mvc geo field command is available
     *
     * @return true if web mvc geo field command is available
     */
    boolean isFieldCommandAvailable();

    /**
     * This method checks if web mvc geo layers command is available
     *
     * @return true if web mvc geo layers command is available
     */
    boolean isLayerCommandAvailable();

    /**
     * This method checks if web mvc geo tool command is available
     *
     * @return true if web mvc geo tool command is available
     */
    boolean isToolCommandAvailable();

    /**
     * This method imports all necessary element to build a gvNIX GEO
     * application
     */
    void setup();

    /**
     * This method creates new components to visualize a Map component
     *
     * @param controller
     * @param path
     */
    void addMap(JavaType controller, JavaSymbolName path, ProjectionCRSTypes crs);

    /**
     * This method include all GEO entities on specific map
     *
     * @param path
     */
    void all(JavaSymbolName path);

    /**
     * This method include specific GEO entity on specific map
     *
     * @param path
     */
    void add(JavaType controller, JavaSymbolName path);

    /**
     * This method transform an input element to map controller on CRU views
     *
     * @param controller
     * @param fieldName
     * @param color
     * @param weight
     * @param center
     * @param zoom
     * @param maxZoom
     */
    void field(JavaType controller, JavaSymbolName fieldName, String color,
            String weight, String center, String zoom, String maxZoom);

    /**
     * This method add new base tile layers on selected map
     *
     * @param name
     * @param url
     * @param path
     * @param index
     * @param opacity
     */
    void tileLayer(String name, String url, JavaSymbolName path, String index,
            String opacity);

    /**
     *
     * This method add new base wms layers on selected map
     *
     * @param name
     * @param url
     * @param path
     * @param index
     * @param opacity
     * @param layers
     * @param format
     * @param transparent
     * @param styles
     * @param version
     * @param crs
     */
    void wmsLayer(String name, String url, JavaSymbolName path, String index,
            String opacity, String layers, String format, boolean transparent,
            String styles, String version, String crs);

    /**
     *
     * This method add new measure tool on selected map
     *
     * @param name
     * @param path
     * @param preventExitMessageCode
     */
    void addMeasureTool(String name, JavaSymbolName path,
            String preventExitMessageCode);

    /**
     *
     * This method add new measure tool on selected map
     *
     * @param name
     * @param path
     * @param preventExitMessageCode
     */
    void addCustomTool(String name, JavaSymbolName path,
            String preventExitMessageCode, String icon, String iconLibrary,
            boolean actionTool, String activateFunction,
            String deactivateFunction, String cursorIcon);

    /**
     * This method updates geo addon to use Bootstrap components
     */
    void updateGeoAddonToBootstrap();

}