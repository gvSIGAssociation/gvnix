/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.theme.roo.addon.converters;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.theme.roo.addon.Theme;
import org.gvnix.web.theme.roo.addon.ThemeOperations;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

/**
 * Provides conversion to and from {@link Theme}, with full support for 
 * using IDs to identify a theme.
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @author Enrique Ruiz ( eruiz at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
@Component
@Service
public class ThemeConverter implements Converter {

  /**
   * Use PageOperations to execute operations 
   */
  @Reference private ThemeOperations operations;

  /**
   * Check if given type can be converted by this Converter
   * @param requiredType Can this type be converted?
   * @param optionContext 
   */
  public boolean supports(Class<?> requiredType, String optionContext) {
    return Theme.class.isAssignableFrom(requiredType);
  }

  /** 
   * Convert given ID to {@link Theme}
   * @param value Theme ID
   * @param requiredType [Not used]
   * @param optionContext [Not used]
   */
  public Object convertFromText(String value, Class<?> requiredType,
                                String optionContext) {
    List<Theme> themes = new ArrayList<Theme>();

    // Get all themes for generic use or only installed themes
    // When Commands class set optionContext == "INSTALLED" when it needs
    // only the installed themes, so if it occurs don't add available themes
    if(!"INSTALLED".equals(optionContext)) {
      themes.addAll(operations.getAvailableThemes());
    }
    else {
      themes.addAll(operations.getInstalledThemes());
    }

    // find requested theme
    for (Theme theme : themes) {
      if(value.equals(theme.getId())) {
        return theme;
      }
    }
    return null;
  }

  /**
   * Get available and installed themes.
   * @param completions
   * @param requiredType
   * @param existingData
   * @param optionContext
   * @param target
   * @return
   */
  public boolean getAllPossibleValues(List<String> completions,
                                      Class<?> requiredType,
                                      String existingData,
                                      String optionContext, MethodTarget target) {
    List<Theme> themes = new ArrayList<Theme>();

    // Get all themes for generic use or only installed themes
    // When Commands class set optionContext == "INSTALLED" when it needs
    // only the installed themes, so if it occurs don't add available themes
    if(!"INSTALLED".equals(optionContext)) {
      themes.addAll(operations.getAvailableThemes());
    }
    else {
      themes.addAll(operations.getInstalledThemes());
    }

    if(themes.isEmpty()) {
      return false;
    }

    // get all theme IDs
    for (Theme theme : themes) {
      completions.add(theme.getId());
    }
    return true;
  }
}
