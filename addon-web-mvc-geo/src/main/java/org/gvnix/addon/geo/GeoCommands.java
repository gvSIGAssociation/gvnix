package org.gvnix.addon.geo;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell
 * following an automatic classpath scan. You can provide simple user
 * presentation-related logic in this class. You can return any objects from
 * each method, or use the logger directly if you'd like to emit messages of
 * different severity (and therefore different colours on non-Windows systems).
 * 
 * @since 1.1
 */
@Component
@Service
public class GeoCommands implements CommandMarker {

    /**
     * Get a reference to the GeoOperations from the underlying OSGi container
     */
    @Reference
    private GeoOperations operations;

    @Reference
    private TypeLocationService typeLocationService;

    /**
     * This method checks if the setup method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo setup")
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * This method checks if the map method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo controller")
    public boolean isMapCommandAvailable() {
        return operations.isMapCommandAvailable();
    }

    /**
     * This method checks if web mvc geo all method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo entity all")
    public boolean isAllCommandAvailable() {
        return operations.isAllCommandAvailable();
    }

    /**
     * This method checks if web mvc geo add method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo entity add")
    public boolean isAddCommandAvailable() {
        return operations.isAddCommandAvailable();
    }

    /**
     * This method checks if web mvc geo field method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo field")
    public boolean isFieldCommandAvailable() {
        return operations.isFieldCommandAvailable();
    }

    /**
     * This method checks if web mvc geo tilelayer or wmslayer method is
     * available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc geo tilelayer", "web mvc geo wmslayer" })
    public boolean isLayerCommandAvailable() {
        return operations.isLayerCommandAvailable();
    }

    /**
     * This method checks if web mvc geo tool method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc geo tool" })
    public boolean isToolCommandAvailable() {
        return operations.isToolCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo setup", help = "Setup GEO components in your project.")
    public void setup() {
        operations.setup();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo controller", help = "Add new Map view to your project")
    public void addMap(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the new Map Controller") final JavaType controller,
            @CliOption(key = "preferredMapping", mandatory = true, optionContext = UPDATE_PROJECT, help = "Indicates a specific request mapping path for this map (eg /foo); no default value") final JavaSymbolName path,
            @CliOption(key = "projection", mandatory = false, help = "Indicates which CRS you want to use on current wms layer. DEFAULT: EPSG3857") final ProjectionCRSTypes crs) {
        operations.addMap(controller, path);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo entity all", help = "Run this method to include all GEO entities on specific map or on all available maps")
    public void all(
            @CliOption(key = "map", mandatory = false, help = "Map where you want to add all entities. If blank, adds all GEO entities to all available maps") MapsProperty path) {
        // Checking if path was selected
        if (path != null) {
            operations.all(new JavaSymbolName(path.getValue()));
        }
        else {
            operations.all(null);
        }

    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo entity add", help = "Run this method to include specific GEO entity on all maps or specific map")
    public void add(
            @CliOption(key = "controller", mandatory = true, help = "Indicates which entity controller you want to add to map") final JavaType controller,
            @CliOption(key = "map", mandatory = false, help = "Map where you want to add current entities. If blank, adds current GEO entity to all available maps") final MapsProperty path) {
        // Checking if path was selected
        if (path != null) {
            operations.add(controller, new JavaSymbolName(path.getKey()));
        }
        else {
            operations.add(controller, null);
        }
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo field", help = "Run this method to transform input to map control on entity CRU views.")
    public void field(
            @CliOption(key = "controller", mandatory = true, help = "Indicates which entity controller has field") final JavaType controller,
            @CliOption(key = "field", mandatory = true, help = "Indicates which field you want to implements as map control") final JavaSymbolName fieldName,
            @CliOption(key = "color", mandatory = false, help = "Indicates which color you want to use to draw element") final String color,
            @CliOption(key = "weight", mandatory = false, help = "Indicates which weight you want to use to draw element") final String weight,
            @CliOption(key = "center", mandatory = false, help = "Indicates map center to use as default. FORMAT: 'lat , lng'") final String center,
            @CliOption(key = "zoom", mandatory = false, help = "Indicates which zoom you want to use on map") final String zoom,
            @CliOption(key = "maxZoom", mandatory = false, help = "Indicates which maxZoom you want to use to on map") final String maxZoom) {

        operations.field(controller, fieldName, color, weight, center, zoom,
                maxZoom);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo tilelayer", help = "Run this method to add new base tile layers on your map.")
    public void tileLayer(
            @CliOption(key = "name", mandatory = true, help = "Indicates which name has current base tile layer") final String name,
            @CliOption(key = "url", mandatory = true, help = "Indicates base tile layer URL") final String url,
            @CliOption(key = "map", mandatory = false, help = "Map where you want to add current layer. If blank, adds current layer to all available maps") final MapsProperty path,
            @CliOption(key = "index", mandatory = false, help = "Indicates in which position must be layer displayed. Default: Poisiton of the layer on map view") final String index,
            @CliOption(key = "opacity", mandatory = false, help = "Indicates which opacity has base layer. Number between 0 and 1. DEFAULT: 1") final String opacity) {

        // Checking if path was selected
        if (path != null) {
            operations.tileLayer(name, url, new JavaSymbolName(path.getKey()),
                    index, opacity);
        }
        else {
            operations.tileLayer(name, url, null, index, opacity);
        }
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo wmslayer", help = "Run this method to add new base wms layers on your map.")
    public void wmsLayer(
            @CliOption(key = "name", mandatory = true, help = "Indicates which name has current base wms layer") final String name,
            @CliOption(key = "url", mandatory = true, help = "Indicates base wms layer URL") final String url,
            @CliOption(key = "map", mandatory = false, help = "Map where you want to add current layer. If blank, adds current layer to all available maps") final MapsProperty path,
            @CliOption(key = "index", mandatory = false, help = "Indicates in which position must be layer displayed. Default: Poisiton of the layer on map view") final String index,
            @CliOption(key = "opacity", mandatory = false, help = "Indicates which opacity has base layer. Number between 0 and 1. DEFAULT: 0.5") final String opacity,
            @CliOption(key = "layers", mandatory = false, help = "Indicates which layers you want to load on this wms layer") final String layers,
            @CliOption(key = "format", mandatory = false, help = "Indicates which image format you want to load on this wms layer. EX: image/png") final String format,
            @CliOption(key = "transparent", mandatory = false, help = "Indicates if current layer is transparent or not") final boolean transparent,
            @CliOption(key = "styles", mandatory = false, help = "Indicates which styles you want to use on current wms layer") final String styles,
            @CliOption(key = "version", mandatory = false, help = "Indicates which wms version you want to use on current wms layer") final String version,
            @CliOption(key = "crs", mandatory = false, help = "Indicates which CRS projection you want to use on current wms layer. DEFAULT: EPSG3857") final ProjectionCRSTypes crs) {

        // Checking if path was selected
        if (path != null) {
            operations.wmsLayer(name, url, new JavaSymbolName(path.getKey()),
                    index, opacity, layers, format, transparent, styles,
                    version, crs.toString());
        }
        else {
            operations.wmsLayer(name, url, null, index, opacity, layers,
                    format, transparent, styles, version, crs.toString());
        }
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo tool", help = "Run this method to add new tool on your map.")
    public void tool(
            @CliOption(key = "name", mandatory = true, help = "Indicates which name has current tool") final String name,
            @CliOption(key = "type", mandatory = true, help = "Indicates which type of tool you want to add to your map") final ToolTypes type,
            @CliOption(key = "map", mandatory = false, help = "Map where you want to add current layer. If blank, adds current layer to all available maps") final MapsProperty path,
            @CliOption(key = "preventExitMessageCode", mandatory = false, help = "Indicates which MessageCode you want to use to prevent exit. If blank, not prevent exit. DEFAULT: blank.") final String preventExitMessageCode) {

        // Checking if path was selected
        if (path != null) {
            operations.addTool(name, type, new JavaSymbolName(path.getKey()),
                    preventExitMessageCode);
        }
        else {
            operations.addTool(name, type, null, preventExitMessageCode);
        }
    }

}