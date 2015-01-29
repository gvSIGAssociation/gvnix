package org.gvnix.addon.jpa.addon.geo;

import java.util.List;

import org.gvnix.addon.jpa.addon.geo.providers.GeoProviderId;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @author gvNIX Team
 * @since 1.4
 */
public interface JpaGeoOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX GEO component has been setup in this
     * project
     */
    static final String FT_NM_GVNIX_GEO_PERS = "gvnix-geo-persistence";

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupCommandAvailable();

    /**
     * 
     * @return
     */
    boolean isFieldCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isFinderGeoAllCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isFinderGeoAddCommandAvailable();

    /**
     * Installs the selected provider
     * 
     * @param provider Provider GeoProviderId
     */
    void installProvider(GeoProviderId provider);

    /**
     * Add new field depending of the selected provider.
     * 
     * @param provider Provider GeoProviderId
     */
    void addFieldByProvider(JavaSymbolName fieldName,
            FieldGeoTypes fieldGeoType, JavaType entity);

    /**
     * Add finders to all Geo Entities
     * 
     */
    void addFinderGeoAllByProvider();

    /**
     * Add finders to specific Entity
     * 
     * @param entity entity
     */
    void addFinderGeoAddByProvider(JavaType entity);

    /**
     * 
     * Get available providers on the system
     * 
     * @return A GeoProviderId List
     */
    List<GeoProviderId> getProvidersId();

    /**
     * Gets the current provider by name
     * 
     * @param name Provider Name
     * @return SecurityProviderId
     */
    GeoProviderId getProviderIdByName(String name);

}