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
package org.gvnix.dynamiclist.service;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.gvnix.dynamiclist.dto.DynamiclistConfig;
import org.gvnix.dynamiclist.exception.DynamiclistException;
import org.gvnix.dynamiclist.jpa.bean.UserConfig;
import org.gvnix.dynamiclist.jpa.bean.UserFilter;
import org.springframework.ui.Model;

/**
 * Dynamiclist service.
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
public interface DynamiclistService {	

	/**
	 * Search MetaFields of a generic object entity
	 * 
	 * @param config
	 * @return
	 * @throws DynamiclistException
	 */
	public Collection<String> searchMetaFields(DynamiclistConfig config) throws DynamiclistException;
	
	
	/**
	 * Search the generic entities with a filter and certain order
	 * 
	 * @param objectClass
	 * @param page
	 * @param size
	 * @param groupBy
	 * @param orderBy
	 * @param orderByColumn
	 * @param request
	 * @param model
	 * @throws DynamiclistException
	 */
	public void search(Class<?> objectClass, Integer page, Integer size, String groupBy, String orderBy, String orderByColumn, 
			HttpServletRequest request, Model model) throws DynamiclistException;
	
	/**
	 * Search the generic entities collection with a filter and certain order to export
	 * 
	 * @param ClassObjectSimpleName
	 * @param config
	 * @return
	 * @throws DynamiclistException
	 */
	public Collection<?> searchExport(String ClassObjectSimpleName, DynamiclistConfig config) throws DynamiclistException;
	
	
	/**
	 * Persist a userFilter entity
	 * 
	 * @param userFilter
	 * @return
	 * @throws DynamiclistException
	 */
	public UserFilter saveUserFilter(UserFilter userFilter) throws DynamiclistException;
	
	/**
	 * Delete a userFilter entity persistent
	 * 
	 * @param id
	 * @throws DynamiclistException
	 */
	public void deleteUserFilter(Integer id) throws Exception, DynamiclistException;
	
	/**
	 * Delete a userConfig entity persistent
	 * 
	 * @param userConfig
	 * @throws DynamiclistException
	 */
	public void deleteUserConfig(UserConfig userConfig) throws DynamiclistException;
	
	/**
	 * Save a userConfig entity persistent
	 * 
	 * @param userConfig
	 * @return
	 * @throws DynamiclistException
	 */
	public UserConfig saveUserConfig(UserConfig userConfig) throws DynamiclistException;
	
	/**
	 * Search user filters of a generic entity and a certain user registered in the application
	 * 
	 * @param entity
	 * @param idUser
	 * @return collection of userFilters
	 * @throws DynamiclistException
	 */
	public Collection<UserFilter> searchUserFilters(String entity, String idUser) throws DynamiclistException;
	
	
	/**
	 * Search width column of  MetaFields collection of a generic object entity
	 * 
	 * @param metaFields
	 * @param className
	 * @return
	 * @throws DynamiclistException
	 */
	public Map<String, Integer> searchMetaFieldsWidth(Collection<String> metaFields, String className) throws DynamiclistException;
}
