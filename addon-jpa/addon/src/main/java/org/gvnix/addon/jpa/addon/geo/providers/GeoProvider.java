package org.gvnix.addon.jpa.addon.geo.providers;

import org.gvnix.addon.jpa.addon.geo.FieldGeoTypes;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;

/**
 * 
 * Interface of GeoProvider
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public interface GeoProvider {

    /**
     * Gets provider name
     * 
     * @return
     */
    String getName();

    /**
     * 
     */
    String getDescription();

    /**
     * This method installs the provider that implements the interface
     * 
     */
    void setup();

    /**
     * This method add a new field on the selected entity
     * 
     */
    void addField(JavaSymbolName fieldName, FieldGeoTypes fieldGeoType,
            JavaType entity);

    /**
     * This method add a new finder in all entities
     * 
     */
    void addFinderGeoAll();

    /**
     * This method add a new finder in the selected entity
     * 
     */
    void addFinderGeoAdd(JavaType entity);

    /**
     * This method checks if his provider persistence is installed
     * 
     * @return true if is installed
     **/
    boolean isAvailablePersistence(FileManager fileManager,
            PathResolver pathResolver);

    /**
     * This method checks if his GEO persistence is installed
     * 
     * @return true if is installed
     **/
    boolean isGeoPersistenceInstalled(FileManager fileManager,
            PathResolver pathResolver);

}
