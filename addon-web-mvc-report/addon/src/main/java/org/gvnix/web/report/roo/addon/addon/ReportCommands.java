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
package org.gvnix.web.report.roo.addon.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * The command class is registered by the Roo shell following an automatic
 * classpath scan. You can provide simple user presentation-related logic in
 * this class. You can return any objects from each method, or use the logger
 * directly if you'd like to emit messages of different severity (and therefore
 * different colours on non-Windows systems).
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
@Component
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class ReportCommands implements CommandMarker { // All command types must
                                                       // implement the
                                                       // CommandMarker
                                                       // interface
    // private static final Logger logger =
    // HandlerUtils.getLogger(ReportCommands.class);

    /**
     * Get a reference to the ReportOperations from the underlying OSGi
     * container
     */
    @Reference
    private ReportOperations operations;

    @Reference
    private ReportConfigService reportConfigService;

    /**
     * This method is optional. It allows automatic command hiding in situations
     * when the command should not be visible. For example the 'entity' command
     * will not be made available before the user has defined his persistence
     * settings in the Roo shell or directly in the project. You can define
     * multiple methods annotated with {@link CliAvailabilityIndicator} if your
     * commands have differing visibility requirements.
     * <p>
     * Setup available if:
     * <ul>
     * <li>Project is created</li>
     * <li>Spring MVC set. Pure Spring MVC web tier or GWT web tier</li>
     * <li>Setup not already done</li>
     * </ul>
     * </p>
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web report setup" })
    public boolean isSetupAvailable() {
        return operations.isProjectAvailable()
                && reportConfigService.isSpringMvcProject()
                && !reportConfigService.isJasperViewsProject();
    }

    @CliAvailabilityIndicator({ "web report add" })
    public boolean isAddAvailable() {
        return operations.isProjectAvailable()
                && reportConfigService.isSpringMvcTilesProject();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web report add",
            help = "Add a new report handled by the given controller")
    public void add(
            @CliOption(key = "controller",
                    mandatory = true,
                    help = "The name of the controller object which handle the report request") JavaType controller,
            @CliOption(key = "reportName",
                    mandatory = true,
                    help = "The name of the new report.") String reportName,
            @CliOption(key = "format",
                    mandatory = false,
                    unspecifiedDefaultValue = "pdf",
                    specifiedDefaultValue = "pdf",
                    help = "The available format for the new report with comma separated list format. By example: pdf,xls,html,csv") String format) {

        if (!reportConfigService.isJasperViewsProject()) {
            reportConfigService.setup();
        }

        operations.annotateType(controller, reportName, format);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     */
    @CliCommand(value = "web report setup",
            help = "Setup JasperReport support in the project. Adds maven dependencies to pom.xlm. Adds JasperReportViewResolver in webmvc-config.xml. Installs jasper-views.xml as config file for JasperReportViewResolver. Installs jasperreports_extension.properties and the FreeSans font family TTF fonts in the webapp classpath.")
    public void setup() {
        reportConfigService.setup();
    }
}
