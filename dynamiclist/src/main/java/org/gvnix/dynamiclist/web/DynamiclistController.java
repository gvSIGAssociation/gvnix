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
package org.gvnix.dynamiclist.web;

import org.gvnix.dynamiclist.exception.DynamiclistException;
import org.gvnix.dynamiclist.service.DynamiclistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Dynamiclist controller
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
@Controller
public class DynamiclistController {
	
	/*@Autowired
    protected DynamiclistService dynamiclistService = null;*/
	
	/**
     * <p>Group form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/groupBy'.</p>
     */
    @RequestMapping(value="/dynamiclist/groupBy", method=RequestMethod.GET)
    public String group() {
    	return "dynamiclist/groupBy";
    }
    
    
    /**
     * <p>Order form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/orderBy'.</p>
     */
    @RequestMapping(value="/dynamiclist/orderBy", method=RequestMethod.GET)
    public String order() {
    	return "dynamiclist/orderBy";
    }
    
    /**
     * <p>Export form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/exportList'.</p>
     */
    @RequestMapping(value="/dynamiclist/export", method=RequestMethod.GET)
    public String export() {
    	return "dynamiclist/exportList";
    }
    
    /**
     * <p>Columns form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/selectProperties'.</p>
     */
    @RequestMapping(value="/dynamiclist/columns", method=RequestMethod.GET)
    public String columns() {
    	return "dynamiclist/selectProperties";
    }
	
    
    
    
    /**
     * <p>Searches for all client and returns them in a 
     * <code>Collection</code>.</p>
     * 
     * <p>Expected HTTP GET and request '/client/search'.</p>
     */
}
