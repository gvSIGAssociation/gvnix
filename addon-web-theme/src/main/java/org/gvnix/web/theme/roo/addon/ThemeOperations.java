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
package org.gvnix.web.theme.roo.addon;

import java.util.List;

/**
 * Operations for theme management:
 * <ul>
 * <li>Find theme repositories</li>
 * <li>Copy themes from repositories to project resources directory<li>
 * </ul>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
public interface ThemeOperations {

	/**
	 * Indicate if the project has been created
	 * 
	 * @return true if the project is available, otherwise false
	 */
	boolean isProjectAvailable();

  /**
   * Indicate if there are themes ready to be installed
   * 
   * @return true if there are themes available, otherwise false
   */
  boolean isThemesAvailable();

  /**
   * Indicate the project has a web layer based on Spring MVC Tiles.
   * 
   * @return true if the user installed an Spring MVC Tiles web layer, otherwise
   *         returns false.
   */
  boolean isSpringMvcTilesProject();

	/**
	 * Install given theme MVC artefacts in current project.
	 * @param id Theme identifier
	 */
	void installThemeArtefacts(String id);

  /**
   * Set the project active theme.
   * @param id Theme identifier
   */
  void setActive(String id);

  /**
   * Get the project active theme.
   * @return Active theme or null if any Theme was activated.
   */
  Theme getActiveTheme();

  /**
   * Returns a list containing the themes ready to be installed, that is, the
   * themes distributed in OSGi bundles (this add-on or any other installed in
   * Shell) and the themes in the local theme repository.
   * <p>
   * Note that to locate themes in any installed bundle will let us to 
   * distribute themes via OBR.
   * 
   * @param themesPath
   * @return
   */
  List<Theme> getAvailableThemes();

  /**
   * Return the themes installed in the project.
   * @return Theme list
   */
  List<Theme> getInstalledThemes();

  /**
   * Return a formated string that shows complete themes info: 
   * <ul>
   * <li>Themes distributed with gvNIX</li>
   * <li>Installed themes</li>
   * <li>Active theme</li>
   * </ul>
   * @return Themes info
   */
  String getThemesInfo();
}