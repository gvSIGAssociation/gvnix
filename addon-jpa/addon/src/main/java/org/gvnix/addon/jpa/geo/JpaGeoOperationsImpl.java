package org.gvnix.addon.jpa.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Implementation of GEO Addon operations
 * 
 * @since 1.4
 */
@Component
@Service
@Reference(name = "provider", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = GeoProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class JpaGeoOperationsImpl implements JpaGeoOperations {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(JpaGeoOperationsImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private List<GeoProvider> providers = new ArrayList<GeoProvider>();
    private List<GeoProviderId> providersId = null;

    private FileManager fileManager;

    private PathResolver pathResolver;

    private ProjectOperations projectOperations;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * If some available provider is setted as persistence provider, the command
     * will be available
     */
    public boolean isSetupCommandAvailable() {
        // Getting all providers
        for (GeoProvider provider : providers) {
            // If some provider says that his needed persistence
            // is installed, command is available
            if (provider.isAvailablePersistence(getFileManager(),
                    getPathResolver())) {
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
        if (!getProjectOperations().isFocusedProjectAvailable()) {
            return false;
        }
        // Getting all providers
        for (GeoProvider provider : providers) {
            // If some provider says that his GEO persistence is installed,
            // field command is available
            if (provider.isGeoPersistenceInstalled(getFileManager(),
                    getPathResolver())) {
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
            if (tmpProvider.isGeoPersistenceInstalled(getFileManager(),
                    getPathResolver())) {
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
            if (tmpProvider.isGeoPersistenceInstalled(getFileManager(),
                    getPathResolver())) {
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
            if (tmpProvider.isGeoPersistenceInstalled(getFileManager(),
                    getPathResolver())) {
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

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on JpaGeoOperationsImpl.");
                return null;
            }
        }
        else {
            return fileManager;
        }

    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (PathResolver) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on JpaGeoOperationsImpl.");
                return null;
            }
        }
        else {
            return pathResolver;
        }

    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on JpaGeoOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }

    }

}