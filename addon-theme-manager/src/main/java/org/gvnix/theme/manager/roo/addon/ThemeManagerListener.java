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

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;

/**
 * gvNIX Theme Mananger file listener
 *
 * @author Ricardo García ( rgarcia at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ThemeManagerListener implements FileEventListener {

    private String distributionDirectory;
    private String installationDirectory;
    private String activeThemeFile;

    @Reference
    private PathResolver pathResolver;
    @Reference
    private ThemeManagerOperations themeManagerOperations;

    protected void activate(ComponentContext context) {

	this.distributionDirectory = themeManagerOperations.getThemesPath();

	this.installationDirectory = pathResolver.getIdentifier(Path.ROOT,
		"themes/");

	this.activeThemeFile = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"/WEB-INF/theme.xml");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent
     * (org.springframework.roo.file.monitor.event.FileEvent)
     */
    public void onFileEvent(FileEvent fileEvent) {

	Assert.notNull(fileEvent, "File event required");

	// If operations is not our implementation do nothing
	if (!(themeManagerOperations instanceof ThemeManagerOperationsImpl)) {
	    return;
	}

	if (fileEvent.getFileDetails().getFile().getAbsolutePath().compareTo(
		distributionDirectory) == 0) {
	    if (fileEvent.getOperation() == FileOperation.UPDATED
		    || fileEvent.getOperation() == FileOperation.CREATED
		    || fileEvent.getOperation() == FileOperation.DELETED) {

		themeManagerOperations.resetGvNixDistributionThemes();
	    }

	} else if (fileEvent.getFileDetails().getFile().getAbsolutePath()
		.compareTo(installationDirectory) == 0) {
	    if (fileEvent.getOperation() == FileOperation.UPDATED
		    || fileEvent.getOperation() == FileOperation.CREATED
		    || fileEvent.getOperation() == FileOperation.DELETED) {

		themeManagerOperations.resetInstalledThemes();
	    }

	} else if (fileEvent.getFileDetails().getFile().getAbsolutePath()
		.compareTo(activeThemeFile) == 0) {
	    if (fileEvent.getOperation() == FileOperation.UPDATED
		    || fileEvent.getOperation() == FileOperation.CREATED
		    || fileEvent.getOperation() == FileOperation.DELETED) {

		themeManagerOperations.resetActiveTheme();
	    }

	}

    }

}
