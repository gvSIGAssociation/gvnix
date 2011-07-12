/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

/**
 * Dynamic configuration entity converter.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class DynConfigurationConverter implements Converter {

    @Reference
    private Operations operations;

    /**
     * {@inheritDoc}
     */
    public Object convertFromText(String value, Class requiredType,
            String optionContext) {

        // Create a dynamic configuration with name only
        return new DynConfiguration(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAllPossibleValues(List completions, Class requiredType,
            String existingData, String optionContext, MethodTarget target) {

        // Find all stored configurations
        DynConfigurationList dynConfs = operations.findConfigurations();

        // No stored configurations
        if (dynConfs.isEmpty()) {

            return false;
        }

        // Add each configuration name to completions
        for (DynConfiguration dynConf : dynConfs) {
            completions.add(dynConf.getName());
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class requiredType, String optionContext) {

        // This converter supports dynamic configuration
        return DynConfiguration.class.isAssignableFrom(requiredType);
    }

}
