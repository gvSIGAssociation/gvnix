package org.gvnix.dynamiclist.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Helper class for message bundle access.
 * @author Fabrizio Giustina
 * @version $Revision: 1081 $ ($Author: fgiust $)
 */
public final class Messages
{

    /**
     * Base name for the bundle.
     */
    private static final String BUNDLE_NAME = "org.displaytag.messages"; //$NON-NLS-1$

    /**
     * Loaded ResourceBundle.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Don't instantiate.
     */
    private Messages()
    {
        // unused
    }

    /**
     * Returns a message from the resource bundle.
     * @param key Message key.
     * @return message String.
     */
    public static String getString(String key)
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
    public static String getString(String key, Object[] parameters)
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
    public static String getString(String key, Object parameter)
    {
        return getString(key, new Object[]{parameter});
    }
}