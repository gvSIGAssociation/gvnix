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

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

/**
 * gvNIX Roo Shell Theme converter
 *
 * @author Ricardo García ( rgarcia at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ThemeManagerConverter implements Converter {

    private String distributionDirectory;
    private String installationDirectory;

    @Reference
    private PathResolver pathResolver;
    @Reference
    private ThemeManagerOperations themeManagerOperations;

    public Object convertFromText(String value, Class<?> requiredType,
	    String optionContext) {

	DistributionTheme distributionTheme;

	if (optionContext.compareTo(ThemeType.DISTRIBUTION.toString()) == 0) {

	    List<Theme> distributionThemeList = themeManagerOperations
		    .getGvNixDistributionThemes();

	    if (distributionThemeList == null) {

		distributionDirectory = themeManagerOperations.getThemesPath();

		themeManagerOperations.reloadThemeList(ThemeType.DISTRIBUTION,
			distributionDirectory);

		distributionThemeList = themeManagerOperations
			.getGvNixDistributionThemes();

	    }

	    if ((distributionThemeList != null)
		    && (distributionThemeList.size() > 0)) {

		for (Theme theme : distributionThemeList) {

		    if (value.compareTo(theme.getName()) == 0) {
			distributionTheme = new DistributionTheme();
			distributionTheme.setName(theme.getName());
			distributionTheme
				.setDescription(theme.getDescription());
			return distributionTheme;
		    }
		}
	    }
	}

	InstalledTheme installedTheme;

	if (optionContext.compareTo(ThemeType.INSTALLED.toString()) == 0) {

	    List<Theme> installedThemeList = themeManagerOperations
		    .getGvNixDistributionThemes();

	    if (installedThemeList == null) {

		installationDirectory = pathResolver.getIdentifier(Path.ROOT,
			"themes/");

		themeManagerOperations.reloadThemeList(ThemeType.INSTALLED,
			installationDirectory);

		installedThemeList = themeManagerOperations
			.getInstalledThemes();

	    }

	    if ((installedThemeList != null) && (installedThemeList.size() > 0)) {

		for (Theme theme : installedThemeList) {

		    if (value.compareTo(theme.getName()) == 0) {
			installedTheme = new InstalledTheme();
			installedTheme.setName(theme.getName());
			installedTheme.setDescription(theme.getDescription());
			return installedTheme;
		    }
		}
	    }
	}

	return null;
    }

    public boolean getAllPossibleValues(List<String> completions,
	    Class<?> requiredType, String existingData, String optionContext,
	    MethodTarget target) {

	String distributionDirectory;
	String installationDirectory;

	// Distribution Themes
	if (optionContext.compareTo(ThemeType.DISTRIBUTION.toString()) == 0) {

	    List<Theme> distributionThemeList = themeManagerOperations
		    .getGvNixDistributionThemes();

	    if (distributionThemeList == null) {

		distributionDirectory = themeManagerOperations.getThemesPath();

		themeManagerOperations.reloadThemeList(ThemeType.DISTRIBUTION,
			distributionDirectory);

		distributionThemeList = themeManagerOperations
			.getGvNixDistributionThemes();
	    }

	    for (Theme theme : distributionThemeList) {

		if (theme.getName().contains(existingData)) {
		    completions.add(theme.getName());
		}

	    }

	    return true;
	}

	// Installed Themes.
	if (optionContext.compareTo(ThemeType.INSTALLED.toString()) == 0) {

	    List<Theme> installedThemeList = themeManagerOperations
		    .getInstalledThemes();

	    if (installedThemeList == null) {

		installationDirectory = pathResolver.getIdentifier(Path.ROOT,
			"themes/");

		themeManagerOperations.reloadThemeList(ThemeType.INSTALLED,
			installationDirectory);

		installedThemeList = themeManagerOperations
			.getInstalledThemes();
	    }

	    for (Theme theme : installedThemeList) {

		if (theme.getName().contains(existingData)) {
		    completions.add(theme.getName());
		}
	    }

	    return true;
	}

	return true;
    }

    public boolean supports(Class<?> requiredType, String optionContext) {
	return Theme.class.isAssignableFrom(requiredType);
    }

}
