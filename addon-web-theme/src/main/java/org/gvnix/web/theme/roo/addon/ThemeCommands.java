/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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
package org.gvnix.web.theme.roo.addon;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Theme management command class. The command class is registered by the Roo
 * shell following an automatic classpath scan.
 * <p>
 * Provide simple user presentation-related logic.
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
@Component
// use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class ThemeCommands implements CommandMarker { // all command types must
                                                      // implement the
                                                      // CommandMarker interface

    private static Logger logger = HandlerUtils.getLogger(ThemeCommands.class);

    /**
     * Use ThemeOperations to install and manage themes
     */
    @Reference
    private ThemeOperations operations;

    /**
     * Hide install command if the project hasn't been created or there aren't
     * any bundle or repository that contain themes to be installed.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "theme install", "theme list" })
    public boolean isCommandAvailable() {
        return operations.isProjectAvailable()
                && operations.isSpringMvcTilesProject()
                && operations.isThemesAvailable();
    }

    /**
     * Hide set command if we haven't installed any theme yet.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("theme set")
    public boolean isSetActiveTheme() {
        return !operations.getInstalledThemes().isEmpty();
    }

    /**
     * TODO javadoc
     * 
     * @param theme
     */
    @CliCommand(value = "theme install", help = "Copy theme artifacts into project's themes directory.")
    public void installTheme(
            @CliOption(key = "id", mandatory = true, help = "Theme ID available to be installed. Use the command 'theme list' to see which are available to install") Theme theme) {

        // Don't use Assert to avoid stack trace in shell, use logger
        if (theme == null) {
            logger.log(
                    Level.SEVERE,
                    "Theme not found. Use the command 'theme list' to see which are available to install.");
            return;
        }
        operations.installThemeArtefacts(theme.getId());
    }

    /**
     * TODO javadoc
     * 
     * @param theme
     */
    @CliCommand(value = "theme set", help = "Set active theme. Copy theme artifacts to webapp directory. By default, overwrite existing files.")
    public void setActiveTheme(
            @CliOption(key = "id", mandatory = true, optionContext = "INSTALLED", help = "Theme's ID installed in theme's project directory. Use the command 'theme list' to see which are installed") Theme theme) {

        // Don't use Assert to avoid stack trace in shell, use logger
        if (theme == null) {
            logger.log(
                    Level.SEVERE,
                    "Theme doesn't exist in project themes folder. Use the command 'theme list' to see which are available to active.");
            return;
        }
        operations.setActive(theme.getId());
        logger.info("Clear your browser's cookies before reload to see active theme settings.");
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    @CliCommand(value = "theme list", help = "Shows themes info: available to install, installed and active.")
    public String showThemesList() {
        return operations.getThemesInfo();
    }

}