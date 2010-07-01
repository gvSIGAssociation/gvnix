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
package org.gvnix.dynamiclist.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Helper class for message bundle access.
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a> 
 */
public final class Messages
{

    /**
     * Base name for the bundle.
     */
    private static final String BUNDLE_NAME = "org.gvnix.dynamiclist.messages";

    /**
     * Loaded ResourceBundle.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Don't instantiate.
     */
    private Messages()
    { }

    /**
     * Returns a message from the resource bundle.
     * @param key Message key.
     * @return message String.
     */
    public static String getMessage(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }

    /**
     * Reads a message from the resource bundle and format it using java MessageFormat.
     * @param key Message key.
     * @param parameters Parameters to pass to MessageFormat.format()
     * @return message String.
     */
    public static String getMessage(String key, Object[] parameters)
    {
        String baseMsg;
        try
        {
            baseMsg = RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }

        return MessageFormat.format(baseMsg, parameters);
    }

    /**
     * Reads a message from the resource bundle and format it using java MessageFormat.
     * @param key Message key.
     * @param parameter single parameter to pass to MessageFormat.format()
     * @return message String.
     */
    public static String getMessage(String key, Object parameter)
    {
        return getMessage(key, new Object[]{parameter});
    }
}