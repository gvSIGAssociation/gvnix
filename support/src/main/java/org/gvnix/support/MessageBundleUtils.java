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
import org.springframework.roo.project.ProjectOperations;

/**
 * Utils for work over Messages Bundles.
 * 
 * @author gvNIX Team
 * @since 0.8.0
 */
public class MessageBundleUtils {
    private static final Logger logger = Logger
            .getLogger(MessageBundleUtils.class.getName());

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
        LogicalPath webappPath = WebProjectUtils
                .getWebappPath(projectOperations);
        String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(webappPath, "");

        // Install message bundle
        String messageBundle = targetDirectory.concat("WEB-INF/i18n/messages_")
                .concat(i18n.getLocale().getLanguage()).concat(".properties");

        // Special case for English locale (default)
        if (i18n.getLocale().equals(Locale.ENGLISH)) {
            messageBundle = targetDirectory
                    .concat("WEB-INF/i18n/messages.properties");
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
                        "Error during copying of message bundle MVC JSP addon",
                        e);
            }
            finally {

                IOUtils.closeQuietly(outputStream);
            }
        }
        return;
    }

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
    public static void addPropertiesToMessageBundle(String language,
            Class invokingClass, PropFileOperations propFileOperations,
            ProjectOperations projectOperations, FileManager fileManager) {
        Properties properties = new Properties();
        LogicalPath webappPath = WebProjectUtils
                .getWebappPath(projectOperations);

        String sourcePropertyFile = "/".concat(
                invokingClass.getPackage().getName()).replace('.', '/');

        // Take "en" as default language
        String targetFilePath = "/WEB-INF/i18n/messages.properties";
        String targetFile = projectOperations.getPathResolver().getIdentifier(
                webappPath, targetFilePath);

        try {
            if (language.equals("en")) {
                sourcePropertyFile = sourcePropertyFile
                        .concat("/messages.properties");
                properties.load(invokingClass
                        .getResourceAsStream(sourcePropertyFile));
            }
            else {
                targetFilePath = "/WEB-INF/i18n/messages_".concat(language)
                        .concat(".properties");
                targetFile = projectOperations.getPathResolver().getIdentifier(
                        webappPath, targetFilePath);

                sourcePropertyFile = sourcePropertyFile.concat("/messages_"
                        .concat(language).concat(".properties"));
                properties.load(invokingClass
                        .getResourceAsStream(sourcePropertyFile));
            }

            if (fileManager.exists(targetFile)) {
                propFileOperations.addProperties(webappPath, targetFilePath,
                        new HashMap<String, String>((Map) properties), true,
                        true);
            }
            else {
                logger.warning(targetFile
                        .concat(" file doesn't exist in project."));
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Message properties for language \""
                    .concat(language).concat("\" can't be loaded"));
        }
    }

}
