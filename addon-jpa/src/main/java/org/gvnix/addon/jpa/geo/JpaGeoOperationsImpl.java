package org.gvnix.addon.jpa.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.geo.providers.GeoProvider;
import org.gvnix.addon.jpa.geo.providers.GeoProviderId;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of GEO Addon operations
 * 
 * @since 1.4
 */
@Component
@Service
@Reference(name = "provider", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = GeoProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class JpaGeoOperationsImpl implements JpaGeoOperations {

    private List<GeoProvider> providers = new ArrayList<GeoProvider>();
    private List<GeoProviderId> providersId = null;

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private TypeManagementService typeManagementService;

    @Reference
    private ProjectOperations projectOperations;

    /**
     * If some available provider is setted as persistence provider, the command
     * will be available
     */
    public boolean isSetupCommandAvailable() {
        // Getting all providers
        for (GeoProvider provider : providers) {
            // If some provider says that his needed persistence
            // is installed, command is available
            if (provider.isAvailablePersistence(fileManager, pathResolver)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if field geo is available to execute checking all
     * providers
     */
    @Override
    public boolean isFieldCommandAvailable() {
        // If no project, command is not available
        if (!projectOperations.isFocusedProjectAvailable()) {
            return false;
        }
        // Getting all providers
        for (GeoProvider provider : providers) {
            // If some provider says that his GEO persistence is installed,
            // field command is available
            if (provider.isGeoPersistenceInstalled(fileManager, pathResolver)) {
                return true;
            }
        }
        return false;

    }

    /**
     * This method checks if finder geo all is available to execute checking all
     * providers
     */
    @Override
    public boolean isFinderGeoAllCommandAvailable() {
        return isFieldCommandAvailable();
    }

    /**
     * This method checks if finder geo add is available to execute checking all
     * providers
     */
    @Override
    public boolean isFinderGeoAddCommandAvailable() {
        return isFieldCommandAvailable();
    }

    /**
     * This method calls the setup method of the selected provider
     */
    @Override
    public void installProvider(GeoProviderId prov) {
        GeoProvider provider = null;
        for (GeoProvider tmpProvider : providers) {
            if (prov.is(tmpProvider)) {
                provider = tmpProvider;
                break;
            }
        }
        if (provider == null) {
            throw new RuntimeException("Provider '".concat(prov.getId())
                    .concat("' not found'"));
        }
        provider.setup();
    }

    /**
     * This method calls the addField method of the installed provider.
     */
    @Override
    public void addFieldByProvider(JavaSymbolName fieldName,
            FieldGeoTypes fieldGeoType, JavaType entity) {
        GeoProvider provider = null;
        // Getting all providers
        for (GeoProvider tmpProvider : providers) {
            // If some provider says that his GEO persistence is installed
            // execute field geo for this provider
            if (tmpProvider
                    .isGeoPersistenceInstalled(fileManager, pathResolver)) {
                provider = tmpProvider;
                break;
            }
        }

        if (provider == null) {
            throw new RuntimeException(
                    "Error checking which Provider must be used to add new field. ");
        }

        provider.addField(fieldName, fieldGeoType, entity);

    }

    /**
     * This method calls the addFinderGeoAll method of the installed provider.
     */
    @Override
    public void addFinderGeoAllByProvider() {
        GeoProvider provider = null;
        // Getting all providers
        for (GeoProvider tmpProvider : providers) {
            // If some provider says that his GEO persistence is installed
            // execute field geo for this provider
            if (tmpProvider
                    .isGeoPersistenceInstalled(fileManager, pathResolver)) {
                provider = tmpProvider;
                break;
            }
        }

        if (provider == null) {
            throw new RuntimeException(
                    "Error checking which Provider must be used to add new field. ");
        }

        provider.addFinderGeoAll();
    }

    /**
     * This method calls the addFinderGeoAdd method of the installed provider.
     */
    @Override
    public void addFinderGeoAddByProvider(JavaType entity) {
        GeoProvider provider = null;
        // Getting all providers
        for (GeoProvider tmpProvider : providers) {
            // If some provider says that his GEO persistence is installed
            // execute field geo for this provider
            if (tmpProvider
                    .isGeoPersistenceInstalled(fileManager, pathResolver)) {
                provider = tmpProvider;
                break;
            }
        }

        if (provider == null) {
            throw new RuntimeException(
                    "Error checking which Provider must be used to add new field. ");
        }

        provider.addFinderGeoAdd(entity);

    }

    /**
     * This method gets providerId using name
     */
    @Override
    public GeoProviderId getProviderIdByName(String name) {
        GeoProviderId provider = null;
        for (GeoProvider tmpProvider : providers) {
            if (tmpProvider.getName().equals(name)) {
                provider = new GeoProviderId(tmpProvider);
            }
        }
        return provider;
    }

    /**
     * This method load new providers
     * 
     * @param provider
     */
    protected void bindProvider(final GeoProvider provider) {
        providers.add(provider);
    }

    /**
     * This method remove providers
     * 
     * @param provider
     */
    protected void unbindProvider(final GeoProvider provider) {
        providers.remove(provider);
    }

    /**
     * This method gets a List of available providers
     */
    @Override
    public List<GeoProviderId> getProvidersId() {
        if (providersId == null) {
            providersId = new ArrayList<GeoProviderId>();
            for (GeoProvider tmpProvider : providers) {
                providersId.add(new GeoProviderId(tmpProvider));
            }
            providersId = Collections.unmodifiableList(providersId);
        }
        return providersId;
    }

    /**
     * FEATURE METHODS
     */

    @Override
    public String getName() {
        return FT_NM_GVNIX_GEO_PERS;
    }

    @Override
    public boolean isInstalledInModule(String moduleName) {
        // If field command is available, GEO Persistence is installed
        return isFieldCommandAvailable();
    }

}