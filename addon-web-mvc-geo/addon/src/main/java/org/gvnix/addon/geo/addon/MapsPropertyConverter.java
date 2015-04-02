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
package org.gvnix.addon.geo.addon;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
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
public class MapsPropertyConverter implements Converter<MapsProperty> {

    private static final Logger logger = HandlerUtils
            .getLogger(MapsPropertyConverter.class);

    @Reference
    private TypeLocationService typeLocationService;

    /**
     * {@inheritDoc}
     */
    public MapsProperty convertFromText(String value, Class<?> requiredType,
            String optionContext) {

        // Create a dynamic property with key and without value
        return new MapsProperty(value, "");
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAllPossibleValues(List<Completion> completions,
            Class<?> requiredType, String existingData, String optionContext,
            MethodTarget target) {

        // Getting all available maps path
        List<String> availableMaps = GeoUtils.getAllMaps(typeLocationService);

        for (String path : availableMaps) {
            MapsProperty mapProperty = new MapsProperty(path, path);
            completions.add(new Completion(mapProperty.getKey()));
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supports(Class<?> requiredType, String optionContext) {

        // This converter supports dynamic property
        return MapsProperty.class.isAssignableFrom(requiredType);
    }

}
