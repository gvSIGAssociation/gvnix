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
package org.gvnix.web.mvc.binding.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Commands of WebBinder Add-on
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public class WebBinderCommands implements CommandMarker {

    /**
     * Get a reference to the WebBinderOperations from the underlying OSGi
     * container
     */
    @Reference
    private WebBinderOperations webBinderOperations;

    /**
     * Check if stringTrimmer command is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc binding stringTrimmer")
    public boolean isSetupAvailable() {
        return webBinderOperations.isStringTrimmerAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc binding stringTrimmer", help = "Declares StringTrimmerEditor on all Web MVC Controller classes")
    public void webBindingStringTrimmer(
            @CliOption(key = { "class", "" }, mandatory = false, help = "The path and name of the controller on which declare de StringTrimmerEditor") JavaType controller,
            @CliOption(key = "emptyAsNull", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "true", help = "Editor that sets Empty String to Null") boolean emptyAsNull) {
        if (controller == null) {
            webBinderOperations.bindStringTrimmerAll(emptyAsNull);
        }
        else {
            webBinderOperations.bindStringTrimmer(controller, emptyAsNull);
        }
    }
}
