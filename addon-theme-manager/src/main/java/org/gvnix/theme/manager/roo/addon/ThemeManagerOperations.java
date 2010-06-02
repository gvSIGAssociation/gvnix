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
package org.gvnix.theme.manager.roo.addon;

import java.util.*;

import org.springframework.roo.project.PathResolver;

/**
 * Interface for {@link ThemeManagerOperationsImpl}.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * 
 */
public interface ThemeManagerOperations {

    /**
     * Installs selected theme coping the files into theme's application folder.
     * Creates the directory themes if not exists.
     * 
     * @param themeName
     *            Theme to install into project.
     */
    public void installTheme(String themeName);

    /**
     * Set to Active the selected theme. Copy the files into project's 'webapp
     * directory' and updates theme.xml with theme name as id tag.
     * 
     * @param themeName
     *            Theme to set active in project.
     */
    public void setThemeActive(String themeName);

    /**
     * Updates theme.xml file with an <id> tag using theme's name.
     * 
     * @param themeName
     *            Theme to set in the theme.xml file.
     */
    public void updateThemeXml(String themeName);

    /**
     * Shows the list of available themes in three categories: <li>
     * gvNIX (gvNIX themes folder)</li> <li>
     * Installed (themes folder in project root)</li> <li>
     * Active (project installed theme)</li>
     * 
     * @return String of available Themes.
     */
    public String showThemesList();

    /**
     * Method to retrieve {@link Theme} {@link List} from a directory path.
     * 
     * @param themeType
     *            to get specific properties.
     * @param themesPath
     *            Directory path to search themes.
     * @return {@link Theme} {@link List} of the themes placed in selected
     *         themesPath.
     */
    public List<Theme> getThemesData(ThemeType themeType, String themesPath);

    /**
     * Method to retrieve Theme object from xml.
     * 
     * @param themeType
     *            to get specific properties from xml.
     * @param themesPath
     *            Path of theme.xml file.
     * @return {@link Theme} Theme entity with the xml attributes.
     */
    public Theme getThemeXmlData(ThemeType themeType, String themeXmlPath);

    public boolean isProjectAvailable();

    /**
     * @return the path resolver or null if there is no user project
     */
    public PathResolver getPathResolver();

    /**
     * gvNIX distribution themes directory.
     * 
     * @return the gvNIX distribution themes path.
     */
    public String getThemesPath();

    public List<Theme> getGvNixDistributionThemes();

    public List<Theme> getInstalledThemes();

    public Theme getActiveTheme();

    /**
     * Sets null value to Distribution theme list.
     */
    public void resetGvNixDistributionThemes();

    /**
     * Reload the value of the theme type list.
     * 
     * @param themeType
     *            Type oif theme.
     * @param themePath
     *            Directory to search.
     */
    public void reloadThemeList(ThemeType themeType, String themePath);

    /**
     * Sets null value to Installed theme list.
     */
    public void resetInstalledThemes();

    /**
     * Sets null value to Active theme.
     */
    public void resetActiveTheme();
}
