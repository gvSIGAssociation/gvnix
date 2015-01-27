package org.gvnix.addon.jpa.geo;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.geo.providers.GeoProviderId;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

@Component
@Service
public class ProviderIdConverter implements Converter<GeoProviderId> {

    @Reference
    private JpaGeoOperations operations;

    protected void bindOperations(JpaGeoOperations operations) {
        this.operations = operations;
    }

    protected void unbindOperations(JpaGeoOperations operations) {
        this.operations = null;
    }

    @Override
    public GeoProviderId convertFromText(String value, Class<?> targetType,
            String optionContext) {
        return operations.getProviderIdByName(value);
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions,
            Class<?> targetType, String existingData, String optionContext,
            MethodTarget target) {
        for (final GeoProviderId id : operations.getProvidersId()) {
            if (existingData.isEmpty() || id.getId().equals(existingData)
                    || id.getId().startsWith(existingData)) {
                completions.add(new Completion(id.getId()));
            }
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return GeoProviderId.class.isAssignableFrom(type);
    }

}
