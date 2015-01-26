package org.gvnix.addon.gva.security;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.gva.security.providers.SecurityProviderId;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

@Component
@Service
public class SecurityProviderIdConverter implements
        Converter<SecurityProviderId> {

    @Reference
    private SecurityProviderOperations operations;

    protected void bindOperations(SecurityProviderOperations operations) {
        this.operations = operations;
    }

    protected void unbindOperations(SecurityProviderOperations operations) {
        this.operations = null;
    }

    @Override
    public SecurityProviderId convertFromText(String value,
            Class<?> targetType, String optionContext) {
        return operations.getProviderIdByName(value);
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions,
            Class<?> targetType, String existingData, String optionContext,
            MethodTarget target) {
        for (final SecurityProviderId id : operations.getProvidersId()) {
            if (existingData.isEmpty() || id.getId().equals(existingData)
                    || id.getId().startsWith(existingData)) {
                completions.add(new Completion(id.getId()));
            }
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return SecurityProviderId.class.isAssignableFrom(type);
    }

}
