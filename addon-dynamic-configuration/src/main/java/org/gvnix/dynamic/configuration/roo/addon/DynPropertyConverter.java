/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Dynamic property entity converter.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class DynPropertyConverter implements Converter<DynProperty> {

    public static final String SOURCE_FILES = "source";
    public static final String CONFIGURATION_FILE = "config";

    private static final Logger logger = HandlerUtils
            .getLogger(DynPropertyConverter.class);

    @Reference private Services services;
    @Reference private Configurations configurations;

    /**
     * {@inheritDoc}
     */
    public DynProperty convertFromText(String value, Class<?> requiredType,
            String optionContext) {

        // Create a dynamic property with key and without value
        return new DynProperty(value, "");
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAllPossibleValues(List<Completion> completions,
            Class<?> requiredType, String existingData, String optionContext,
            MethodTarget target) {

        // Option context mark property completions origin
        if (CONFIGURATION_FILE.equals(optionContext)) {

            // Find all properties key from configuration file
            DynConfiguration dynConf = configurations.parseConfiguration(
                    configurations.getBaseConfiguration(), null);
            return addPropertyCompletions(completions, dynConf);
        }
        else if (SOURCE_FILES.equals(optionContext)) {

            // Find all properties key from files on disk
            DynConfiguration dynConf = services.getCurrentConfiguration();
            return addPropertyCompletions(completions, dynConf);
        }
        else {

            // No valid state
            logger.log(Level.SEVERE, "Invalid property possible values context");
            return false;
        }
    }

    /**
     * Add a dynamic configuration properties to completions list.
     * 
     * @param completions Completions list
     * @param dynConf Dynamic configuration
     * @return Completions list exists
     */
    private boolean addPropertyCompletions(List<Completion> completions,
            DynConfiguration dynConf) {
        boolean any = false;
        for (DynComponent dynComp : dynConf.getComponents()) {
            for (DynProperty dynProp : dynComp.getProperties()) {

                completions.add(new Completion(dynProp.getKey()));
                any = true;
            }
        }

        return any;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class<?> requiredType, String optionContext) {

        // This converter supports dynamic property
        return DynProperty.class.isAssignableFrom(requiredType);
    }

}
