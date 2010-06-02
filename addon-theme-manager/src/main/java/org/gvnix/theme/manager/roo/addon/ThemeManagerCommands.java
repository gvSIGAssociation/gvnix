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

import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.shell.*;
import org.springframework.roo.support.util.Assert;

/**
 * Addon for Manage Spring Roo themes.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ThemeManagerCommands implements CommandMarker {

    private static Logger logger = Logger.getLogger(ThemeManagerCommands.class
	    .getName());

    @Reference
    private FileManager fileManager;

    @Reference
    private ThemeManagerOperations themeManagerOperations;

    @CliAvailabilityIndicator("theme install")
    public boolean isInstallTheme() {

	return (themeManagerOperations.isProjectAvailable() && (themeManagerOperations
		.getThemesPath() != null));
    }

    @CliCommand(value = "theme install", help = "Copy selected theme into project's themes directory.")
    public void installTheme(
	    @CliOption(key = "name", mandatory = true, optionContext = "DISTRIBUTION", help = "Theme's name available in gvNIX themes directory. Use the command 'theme manager list' to see wich are available to install.") DistributionTheme distributionTheme) {

	Assert.isTrue(distributionTheme != null,
		"Theme doesn't exist in gvNIX distribution themes folder.");
	themeManagerOperations.installTheme(distributionTheme.getName());
    }

    @CliAvailabilityIndicator("theme set")
    public boolean isSetActiveTheme() {

	String themesDirectory = themeManagerOperations.getPathResolver()
		.getIdentifier(Path.ROOT,
			ThemeManagerOperationsImpl.APP_THEME_LOCATION);

	return (themeManagerOperations.isProjectAvailable() && (fileManager
		.exists(themesDirectory)));
    }

    @CliCommand(value = "theme set", help = "Copy selected theme into project's webapp directory and set as active theme for the application.")
    public void setActiveTheme(
	    @CliOption(key = "name", mandatory = true, optionContext = "INSTALLED", help = "Theme's name installed in theme's project directory. Use the command 'theme manager list' to see wich are available to set active.") InstalledTheme installedTheme) {

	Assert.isTrue(installedTheme != null,
		"Theme doesn't exist in project themes folder.");
	themeManagerOperations.setThemeActive(installedTheme.getName());
    }

    @CliAvailabilityIndicator("theme list")
    public boolean iShowThemesList() {
	return themeManagerOperations.isProjectAvailable();
    }

    @CliCommand(value = "theme list", help = "Shows available theme to manage with the Add-on gruped by three categories:\n - gvNIX distribution.\n - Installed themes.\n - Active theme.")
    public String showThemesList() {
	return themeManagerOperations.showThemesList();
    }

}
