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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;


/**
 * Helper class for message bundle access.
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a> 
 */
public class Messages
{	
    /**
     * Don't instantiate.
     */
    private Messages()
    { }

    /**
     * Reads a message from the resource bundle and format it using Spring MessageSource.
     * @param key Message key.
     * @param parameters Parameters to pass to MessageSource
     * @return message String.
     */    
    public static String getMessage(String key, Object[] parameters, HttpServletRequest request)
    {   
        try
        {
        	Locale locale = RequestContextUtils.getLocale(request);
        	Log log = LogFactory.getLog(Messages.class);
        	if (log.isDebugEnabled()){
        		log.debug("GetMessage [" + key + "] with locale ["+ locale + "]");
        	}
        	
        	WebApplicationContext context = RequestContextUtils.getWebApplicationContext(request);
        	String message = context.getMessage(key, parameters, locale);

        	return message;
        }
        catch (NoSuchMessageException e)
        {
            return key;
         
        }
    }    
    
    /**
     * Reads a message from the resource bundle and format it using Spring MessageSource.
     * @param key Message key.
     * @return message String.
     */
    public static String getMessage(String key, HttpServletRequest request)
    {
    	return getMessage(key, null, request);
    }
    
    /**
     * Reads a message from the resource bundle and format it using java MessageFormat.
     * @param key Message key.
     * @param parameter single parameter to pass to MessageSource()
     * @param request 
     * @return message String.
     */
    public static String getMessage(String key, Object parameter, HttpServletRequest request)
    {
        return getMessage(key, new Object[]{parameter}, request);
    }	
}