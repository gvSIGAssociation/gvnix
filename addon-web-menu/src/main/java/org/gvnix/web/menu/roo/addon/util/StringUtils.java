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
package org.gvnix.web.menu.roo.addon.util;


/**
 * Utilities related to XML management.
 * 
 * @author Enrique Ruiz( eruiz at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
public abstract class StringUtils {

  /**
   * Change to underscore <i>all</i> whitespace from the given String:
   * leading, trailing, and inbetween characters.
   * @param str the String to check
   * @return the new String
   */
  public static String underscoreAllWhitespace(String str) {
    if (!org.springframework.roo.support.util.StringUtils.hasLength(str)) {
      return str;
    }
    StringBuilder sb = new StringBuilder(str);
    int index = 0;
    while (sb.length() > index) {
      if (Character.isWhitespace(sb.charAt(index))) {
        sb.replace(index, index + 1, "_");
      }
      else {
        index++;
      }
    }
    return sb.toString();
  }
}
