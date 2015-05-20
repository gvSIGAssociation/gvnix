/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.addon.jpa.addon.geo;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.addon.geo.providers.GeoProviderId;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */

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
