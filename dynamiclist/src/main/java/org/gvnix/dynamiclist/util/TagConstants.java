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

/**
 * Constants for html tags.
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public final class TagConstants
{
	
	
	public static final String BUTTONS_CRUD = "BUTTONS_CRUD";
	public static final String BUTTONS_CRUD_ERROR_MESSAGE = "Error en buttonsTag. El atributo \"crudButtons\" debe tener un valor \"S|N|S|S\" indicando" 
					+ "así si el botón se dibujará (S) o no (N) siguiendo el orden create|read|update|delete";
	
	
	public static final String URL_FORM = "/form.html";	
	public static final String BASE_URLMAPPING_BUTTONS_NOCRUD = "/dynamiclist/";	
	public static final String URL_GROUP = BASE_URLMAPPING_BUTTONS_NOCRUD + "groupBy.html";
	public static final String URL_ORDER = BASE_URLMAPPING_BUTTONS_NOCRUD + "orderBy.html";
	public static final String URL_EXPORT = BASE_URLMAPPING_BUTTONS_NOCRUD + "export.html";
	public static final String URL_COLUMNS = BASE_URLMAPPING_BUTTONS_NOCRUD + "columns.html";
	
	//cooperate labels
	public static final String URL_BASE = "url_base";
	public static final String CLASS_OBJECT = "classObject";
	public static final String IMAGES_PATH= "imagesPath";
	
	
	//constants dynamicService
	public static final String META_FIELDS_NAMES = "metaFieldsNames";
	public static final String META_FIELDS_TYPES = "metaFieldsTypes";
	
	public static final String LIST = "list";
	
    /**
     * utility class - don't instantiate.
     */
    private TagConstants()
    {
       // unused
    }
}