package org.gvnix.addon.jpa.geo;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.geo.providers.GeoProviderId;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
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
 * @since 1.4
 */
@Component
@Service
public class JpaGeoCommands implements CommandMarker {

    /**
     * Get a reference to the GeoOperations from the underlying OSGi container
     */
    @Reference
    private JpaGeoOperations operations;

    @Reference
    private TypeLocationService typeLocationService;

    /**
     * This method checks if the setup method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("jpa geo setup")
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * This method checks if the method to add new field is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("field geo")
    public boolean isFieldCommandAvailable() {
        return operations.isFieldCommandAvailable();
    }

    /**
     * This method checks if the method to add new finder to all entities is
     * available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("finder geo all")
    public boolean isFinderGeoAllCommandAvailable() {
        return operations.isFinderGeoAllCommandAvailable();
    }

    /**
     * This method checks if the method to add new finder to entity is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("finder geo add")
    public boolean isFinderGeoAddCommandAvailable() {
        return operations.isFinderGeoAddCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "jpa geo setup", help = "Setup GEO persistence in your project.")
    public void setup(
            @CliOption(key = "provider", mandatory = true, help = "Provider's Name") GeoProviderId provider) {
        operations.installProvider(provider);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "field geo", help = "Add GEO field on specified Entity")
    public void addField(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The Java type of the entity") FieldGeoTypes fieldGeoType,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType entity) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(entity);
        Validate.notNull(javaTypeDetails,
                "The entity specified, '%s', doesn't exist", entity);

        operations.addFieldByProvider(fieldName, fieldGeoType, entity);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "finder geo all", help = "Add finders to all Geo Entities")
    public void addFinderGeoAll() {
        operations.addFinderGeoAllByProvider();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "finder geo add", help = "Add finders to selected Geo Entity")
    public void addFinderGeoAdd(
            @CliOption(key = "class", mandatory = true, optionContext = UPDATE_PROJECT, help = "Entity where you want to generate geo finders") final JavaType entity) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(entity);
        Validate.notNull(javaTypeDetails,
                "The entity specified, '%s', doesn't exist", entity);

        operations.addFinderGeoAddByProvider(entity);
    }

}