package org.gvnix.gva.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.gva.security.providers.SecurityProvider;
import org.gvnix.gva.security.providers.SecurityProviderId;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.Converter;

/**
 * Implementation of {@link SecurityProviderOperations} interface.
 * 
 * @since 1.1.1
 */
@Component
@Service
@References({
        @Reference(name = "provider", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = SecurityProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE),
        @Reference(name = "projectOperations", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = ProjectOperations.class, cardinality = ReferenceCardinality.MANDATORY_UNARY) })
public class SecurityProviderOperationsImpl implements
        SecurityProviderOperations {

    private ProjectOperations projectOperations;

    private List<SecurityProvider> providers = new ArrayList<SecurityProvider>();
    private List<SecurityProviderId> providersId = null;

    /** {@inheritDoc} */
    public boolean checkSecuritySetup() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.SECURITY);
    }

    protected void bindProvider(final SecurityProvider provider) {
        providers.add(provider);
    }

    protected void unbindProvider(final SecurityProvider provider) {
        providers.remove(provider);
    }

    protected void bindProjectOperations(ProjectOperations projectOperations) {
        this.projectOperations = projectOperations;
        providersId = null;
    }

    protected void unbindProjectOperations(ProjectOperations projectOperations) {
        this.projectOperations = null;
        providersId = null;
    }

    @Override
    public List<SecurityProviderId> getProvidersId() {
        if (providersId == null) {
            providersId = new ArrayList<SecurityProviderId>();
            for (SecurityProvider tmpProvider : providers) {
                providersId.add(new SecurityProviderId(tmpProvider));
            }
            providersId = Collections.unmodifiableList(providersId);
        }
        return providersId;
    }

    @Override
    public void installProvider(SecurityProviderId prov,
            JavaPackage targetPackage) {
        SecurityProvider provider = null;
        for (SecurityProvider tmpProvider : providers) {
            if (prov.is(tmpProvider)) {
                provider = tmpProvider;
                break;
            }
        }
        if (provider == null) {
            throw new RuntimeException("Provider '".concat(prov.getId())
                    .concat("' not found'"));
        }
        provider.install(targetPackage);
    }

    @Override
    public SecurityProviderId getProviderIdByName(String name) {
        SecurityProviderId provider = null;
        for (SecurityProvider tmpProvider : providers) {
            if (tmpProvider.getName().equals(name)) {
                provider = new SecurityProviderId(tmpProvider);
            }
        }
        return provider;
    }

}