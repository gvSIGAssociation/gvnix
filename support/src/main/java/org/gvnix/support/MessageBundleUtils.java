/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.support;

import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.ProjectOperations;

/**
 * Utils for work over Messages Bundles.
 * 
 * @author gvNIX Team
 * @since 0.8.0
 */
public interface MessageBundleUtils {

    /**
     * Creates if it doesn't exist the messages_xx.properties file for the given
     * I18n locale.
     * <p>
     * Note that English locale is an especial case where the file is
     * messages.properties
     * 
     * @param i18n
     */
    public void installI18nMessages(I18n i18n,
            ProjectOperations projectOperations, FileManager fileManager);

    /**
     * Copy properties associated with the given class to the message bundle of
     * given language.
     * <p/>
     * Note that properties to add are taken from messages[_xx].properties files
     * and added to messages[_xx].properties in the destination project.
     * <p/>
     * <strong>This method doesn't check if messages[_xx].properties file exist
     * in the add-on invoking it</strong>
     * 
     * @param language Language locale as string (en, es, ca, ...)
     * @param invokingClass Class of the Add-on invoking this method. It's
     *        needed in order to load local resources
     * @param propFileOperations
     * @param projectOperations
     * @param fileManager
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addPropertiesToMessageBundle(String language,
            Class invokingClass, PropFileOperations propFileOperations,
            ProjectOperations projectOperations, FileManager fileManager);

}
