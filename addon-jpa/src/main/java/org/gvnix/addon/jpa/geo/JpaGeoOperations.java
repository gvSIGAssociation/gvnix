package org.gvnix.addon.jpa.geo;

import java.util.List;

import org.gvnix.addon.jpa.geo.providers.GeoProviderId;
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
    static final String FEATURE_NAME_GVNIX_GEO_PERSISTENCE = "gvnix-geo-persistence";

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