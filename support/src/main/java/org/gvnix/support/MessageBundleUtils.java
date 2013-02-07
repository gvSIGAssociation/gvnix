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
package org.gvnix.support;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;

/**
 * Utils for work over Messages Bundles.
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8.0
 */
public class MessageBundleUtils {
    private static Logger logger = Logger.getLogger(MessageBundleUtils.class
            .getName());

    /**
     * Creates if it doesn't exist the messages_xx.properties file for the given
     * I18n locale.
     * <p>
     * Note that English locale is an especial case where the file is
     * messages.properties
     * 
     * @param i18n
     */
    public static void installI18nMessages(I18n i18n,
            ProjectOperations projectOperations, FileManager fileManager) {
        Validate.notNull(i18n, "Language choice required");

        if (i18n.getLocale() == null) {
            logger.warning("could not parse language choice");
            return;
        }

        String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), "");

        // Install message bundle
        String messageBundle = targetDirectory + "/WEB-INF/i18n/messages_"
                + i18n.getLocale().getLanguage() + ".properties";
        // Special case for English locale (default)
        if (i18n.getLocale().equals(Locale.ENGLISH)) {
            messageBundle = targetDirectory
                    + "/WEB-INF/i18n/messages.properties";
        }
        if (!fileManager.exists(messageBundle)) {
            OutputStream outputStream = null;
            try {

                outputStream = fileManager.createFile(messageBundle)
                        .getOutputStream();
                IOUtils.copy(i18n.getMessageBundle(), outputStream);

            }
            catch (IOException e) {

                throw new IllegalStateException(
                        "Encountered an error during copying of message bundle MVC JSP addon.",
                        e);
            }
            finally {

                IOUtils.closeQuietly(outputStream);
            }
        }
        return;
    }

    /**
     * Adds I18n properties to the specified message bundle file. The file is
     * given by the language parameter.
     * <p>
     * Note that properties to add are taken from messages[_xx].properties files
     * and added to messages[_xx].properties in the destination project.
     * <p>
     * <strong>This method doesn't check if messages[_xx].properties file exist
     * in the add-on invoking it</strong>
     * 
     * @param language Language locale as string (en, es, ca, ...)
     * @param invokingClass Class of the Add-on invoking this method. It's
     *            needed in order to load local resources
     * @param propFileOperations
     * @param projectOperations
     * @param fileManager
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void addPropertiesToMessageBundle(String language,
            Class invokingClass, PropFileOperations propFileOperations,
            ProjectOperations projectOperations, FileManager fileManager) {
        Properties properties = new Properties();
        String propertiesFolderPath = "/".concat(
                invokingClass.getPackage().getName()).replace('.', '/');
        // Take "en" as default language
        String messageBundleRelativeFilePath = "/WEB-INF/i18n/messages.properties";
        String messageBundle = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        messageBundleRelativeFilePath);
        try {
            if (language == "en") {
                propertiesFolderPath = propertiesFolderPath
                        .concat("/messages.properties");
                properties.load(invokingClass
                        .getResourceAsStream(propertiesFolderPath));
            }
            else {
                messageBundleRelativeFilePath = "/WEB-INF/i18n/messages_"
                        .concat(language).concat(".properties");
                messageBundle = projectOperations.getPathResolver()
                        .getIdentifier(
                                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP,
                                        ""), messageBundleRelativeFilePath);

                propertiesFolderPath = propertiesFolderPath.concat("/messages_"
                        + language + ".properties");
                properties.load(invokingClass
                        .getResourceAsStream(propertiesFolderPath));
            }

            if (fileManager.exists(messageBundle)) {
                propFileOperations.addProperties(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        messageBundleRelativeFilePath,
                        new HashMap<String, String>((Map) properties), true,
                        true);
            }
            else {
                logger.warning(messageBundle
                        .concat(" file doesn't exist in project."));
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Message properties for language \""
                    .concat(language).concat("\" can't be loaded"));
        }
    }

}
