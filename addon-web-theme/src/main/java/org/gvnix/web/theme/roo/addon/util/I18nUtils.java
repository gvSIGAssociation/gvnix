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
package org.gvnix.web.theme.roo.addon.util;

import java.io.InputStream;
import java.util.Locale;

import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;


/**
 * Utilities for handling {@link I18n} instances.
 * 
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
public abstract class I18nUtils {

  /**
   * Create an I18n instance to contain given data. Result instance won't
   * contain neither message bundle nor flag graphic.
   * 
   * @param language
   * @param label
   * @return
   */
  public static I18n createI18n(final String language, final String label) {

    // Anonymous inner type
    I18n i18n = new I18n() {
      
      public InputStream getMessageBundle() { return null; }
      
      public Locale getLocale() { return new Locale(language); }
      
      public String getLanguage() { return label; }
      
      public InputStream getFlagGraphic() { return null; }
    };

    return i18n;
  }
}
