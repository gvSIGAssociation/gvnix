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
package org.gvnix.web.exception.handler.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Addon for Handle Exceptions
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebExceptionHandlerCommands implements CommandMarker {

    private static Logger logger = Logger
            .getLogger(WebExceptionHandlerCommands.class.getName());

    @Reference
    private WebExceptionHandlerOperations exceptionOperations;

    @CliAvailabilityIndicator("exception handler list")
    public boolean isListExceptionHandlerAvailable() {
        return exceptionOperations.isProjectAvailable()
                && exceptionOperations.isMessageMappingAvailable();
    }

    @CliCommand(value = "exception handler list", help = "Obtains the list of the Exceptions that handles the application.")
    public String listExceptionHandler() {
        return exceptionOperations.getHandledExceptionList();
    }

    @CliAvailabilityIndicator("exception handler add")
    public boolean isAddNewHandledException() {
        return exceptionOperations.isProjectAvailable()
                && exceptionOperations.isMessageMappingAvailable();
    }

    @CliCommand(value = "exception handler add", help = "Adds a handler for an Uncaught Exception and creates a view with a specific message.")
    public void addNewHandledException(
            @CliOption(key = "exception", mandatory = true, help = "The name of the Exception you want to handle with the whole package path. e.g. java.lang.Exception") String exceptionName,
            @CliOption(key = "title", mandatory = true, help = "The title of the Exception you want to handle.\nEnter the title between commas if it is composed of more than one word.") String exceptionTitle,
            @CliOption(key = "description", mandatory = true, help = "The description of the Exception you want to handle.\nEnter the description between commas if it is composed of more than one word.") String exceptionDescription,
            @CliOption(key = "language", mandatory = true, help = "The language to create the message fo the exception.\n[es, de, it, nl, sv, en]") String exceptionLanguage) {

        exceptionOperations.addNewHandledException(exceptionName,
                exceptionTitle, exceptionDescription, exceptionLanguage);
    }

    @CliAvailabilityIndicator("exception handler remove")
    public boolean isRemoveExceptionHandledAvailable() {
        return exceptionOperations.isProjectAvailable()
        // && exceptionOperations.isExceptionMappingAvailable();
                && exceptionOperations.isMessageMappingAvailable();
    }

    @CliCommand(value = "exception handler remove", help = "Removes the Exception handler select and her view.")
    public void removeExceptionHandled(
            @CliOption(key = "exception", mandatory = true, help = "The Exception you want to remove.") String exceptionName) {
        exceptionOperations.removeExceptionHandled(exceptionName);
    }

    @CliAvailabilityIndicator("exception handler set language")
    public boolean isSetLanguageExceptionHandledAvailable() {
        return exceptionOperations.isProjectAvailable()
        // && exceptionOperations.isExceptionMappingAvailable();
                && exceptionOperations.isMessageMappingAvailable();
    }

    @CliCommand(value = "exception handler set language", help = "Set the title and description of the exception selected to the choosed language file.")
    public void languageExceptionHandled(
            @CliOption(key = "exception", mandatory = true, help = "The name of the Exception you want to set the message with the whole package path. e.g. java.lang.Exception") String exceptionName,
            @CliOption(key = "title", mandatory = true, help = "The title of the Exception you want to handle.\nEnter the title between commas if it is composed of more than one word.") String exceptionTitle,
            @CliOption(key = "description", mandatory = true, help = "The description of the Exception you want to handle.\nEnter the description between commas if it is composed of more than one word.") String exceptionDescription,
            @CliOption(key = "language", mandatory = true, help = "The language to create the message fo the exception.\n[es, de, it, nl, sv, en]") String exceptionLanguage) {

        exceptionOperations.languageExceptionHandled(exceptionName,
                exceptionTitle, exceptionDescription, exceptionLanguage);
    }

    @CliAvailabilityIndicator("exception handler setup gvnix")
    public boolean isSetUpGvNIXExceptionsAvailable() {
        return exceptionOperations.isProjectAvailable()
                && exceptionOperations.isExceptionMappingAvailable();
    }

    @CliCommand(value = "exception handler setup gvnix", help = "Defines gvNIX predefined Exceptions.")
    public void setUpGvNIXExceptions() {
        exceptionOperations.setUpGvNIXExceptions();
    }

}
