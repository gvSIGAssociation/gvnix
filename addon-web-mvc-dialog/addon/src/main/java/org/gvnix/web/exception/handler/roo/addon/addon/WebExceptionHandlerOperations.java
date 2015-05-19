/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
package org.gvnix.web.exception.handler.roo.addon.addon;

/**
 * Interface for {@link WebExceptionHandlerOperationsImpl}.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public interface WebExceptionHandlerOperations {

    /**
     * Show the list of Handled Exceptions of the
     * SimpleMappingExceptionResolver.
     */
    public String getHandledExceptionList();

    /**
     * Handles the exception with the provided name, title and description.
     * 
     * @param exceptionName Exception Name to Handle.
     * @param exceptionTitle Title of the Exception to show in the view
     * @param exceptionDescription Description of the Exception to show in the
     *        view
     * @param exceptionLanguage Language to set the message.
     */
    public void addNewHandledException(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String exceptionLanguage);

    /**
     * Removes the selected Exception if exists.
     * 
     * @param exceptionName Exception to remove.
     */
    public void removeExceptionHandled(String exceptionName);

    /**
     * Set the title and description of the exception in the selected language
     * 
     * @param exceptionName Exception Name to Handle.
     * @param exceptionTitle Title of the Exception to show in the view
     * @param exceptionDescription Description of the Exception to show in the
     *        view
     * @param exceptionLanguage Language to set the message.
     */
    public void languageExceptionHandled(String exceptionName,
            String exceptionTitle, String exceptionDescription,
            String exceptionLanguage);

    /**
     * Set up gvNIX initial Exceptions to the project.
     */
    public void setUpGvNIXExceptions();

    /**
     * Retrieve the Language FileName selected
     * 
     * @param exceptionLanguage Language in which.
     */
    public String getLanguagePropertiesFile(String exceptionLanguage);

    /**
     * Check if there are exceptions mapped in the SimpleMappingExceptionHanlder
     * bean.
     * 
     * @return {@link Boolean} true if there are exceptions in the mapping.
     */
    public boolean isExceptionMappingAvailable();

    /**
     * Check if there are exceptions mapped in the
     * MessageMappingExceptionHanlder bean.
     * 
     * @return {@link Boolean} true if there are exceptions in the mapping.
     */
    public boolean isMessageMappingAvailable();

    /**
     * Check if project is available and if its a Spring MVC project
     * 
     * @return
     */
    public boolean isProjectAvailable();
}
