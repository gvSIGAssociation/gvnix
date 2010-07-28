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
 * Constants for dynamiclist CustomTags.
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public final class TagConstants
{	
	public static final String DYNAMICLIST_CONFIG = "dynamiclistConfig";
	
	
	public static final String BUTTONS_CRUD = "BUTTONS_CRUD";
	public static final String BUTTONS_CRUD_ERROR_MESSAGE = "Error en buttonsTag. El atributo \"crudButtons\" debe tener un valor \"S|N|S|S\" indicando" 
					+ "así si el botón se dibujará (S) o no (N) siguiendo el orden create|read|update|delete";
	
	
	public static final String URL_FORM = "/form.html";	
	public static final String URL_SEARCH = "/search.html";	
	
	public static final String BASE_URLMAPPING_BUTTONS_NOCRUD = "/dynamiclist/";	
	public static final String URL_GROUP = BASE_URLMAPPING_BUTTONS_NOCRUD + "groupBy.html";
	public static final String URL_ORDER = BASE_URLMAPPING_BUTTONS_NOCRUD + "orderBy.html";
	public static final String URL_EXPORT = BASE_URLMAPPING_BUTTONS_NOCRUD + "export.html";
	public static final String URL_COLUMNS = BASE_URLMAPPING_BUTTONS_NOCRUD + "columns.html";
	public static final String URL_UPDATE_COLUMNS = BASE_URLMAPPING_BUTTONS_NOCRUD + "updateColumns.html";
	public static final String URL_SAVE_STATE = BASE_URLMAPPING_BUTTONS_NOCRUD + "saveState.html";
	public static final String URL_DELETE_STATE = BASE_URLMAPPING_BUTTONS_NOCRUD + "deleteState.html";
	
	
	public static final String URL_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "filter.html";
	public static final String URL_DELETE_ACTUAL_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "deleteActualfilter.html";
	public static final String URL_SEARCH_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "searchFilter.html";
	public static final String URL_FILTER_INFO = BASE_URLMAPPING_BUTTONS_NOCRUD + "filterInfo.html";
	public static final String URL_SAVE_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "saveFilter.html";
	public static final String URL_PERSISTENCE_SAVE_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "persistenceSaveFilter.html";
	public static final String URL_DELETE_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "deleteFilter.html";
	public static final String URL_PERSISTENCE_DELETE_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "persistenceDeleteFilter.html";
		public static final String URL_EXECUTE_FILTER = BASE_URLMAPPING_BUTTONS_NOCRUD + "executeFilter.html";
	
	//cooperate labels
	public static final String URL_BASE = "url_base";
	public static final String URL_CONTEXT_BASE = "url_context_base";
	public static final String CLASS_OBJECT = "classObject";
	public static final String IMAGES_PATH= "imagesPath";
	
	
	//constants dynamicService
	public static final String META_FIELDS_NAMES = "metaFieldsNames";
	public static final String META_FIELDS_TYPES = "metaFieldsTypes";
	public static final String FALSE = "false";
	public static final String AUTHENTICATION_USERNAME = "SPRING_SECURITY_LAST_USERNAME";
	
	public static final String LIST = "list";
	
	// constants pagination
	public static final Integer SIZE_PAGE_DEFAULT = 10;
	public static final String PAGE_NAME = "page";
	public static final String SIZE_NAME = "size";
	public static final String MAX_PAGES_NAME = "maxPages";	
	public static final String COUNTLIST_NAME = "countList";
	
	// tag table
	public static final String DATE_FORMAT_DEFAULT = "dd/MM/yyyy";
	public static final String DATE_FORMAT_DEFAULT_EXPORT = "dd/MM/yyyy";
	
	
	//constants buttons
	public static final String GROUPBY = "groupBy";
	
	//type basic constants
	public static final String TYPE_BASIC_NUMBER = "NUMBER";
	public static final String TYPE_BASIC_DATE = "DATE";
	public static final String TYPE_BASIC_TEXT = "TEXT";
	
	public static final String TYPE_FILTER_USER = "U";
	public static final String TYPE_FILTER_GLOBAL = "G";
	
    /**
     * utility class - don't instantiate.
     */
    private TagConstants()
    {
       // unused
    }
}