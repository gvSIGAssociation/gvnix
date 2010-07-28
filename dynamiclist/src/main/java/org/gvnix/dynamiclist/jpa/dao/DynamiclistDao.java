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
package org.gvnix.dynamiclist.jpa.dao;

import java.util.Collection;

/**
 * Dynamiclist DAO interface.
 */
public interface DynamiclistDao {
	
	/**
	 * Find Entities of a persistence generic class.
	 * 
	 * @param entity
	 * @param startIndex
	 * @param maxResults
	 * @param filter
	 * @param orderBy
	 * @return collection of generic entities 
	 */
	public Collection<?> findEntities(String entity, int startIndex, int maxResults, String filter, String orderBy);
	
	/**
	 * Count Entities of a persistence generic class
	 * 
	 * @param entity
	 * @param startIndex
	 * @param maxResults
	 * @param filter
	 * @return number of entities
	 */
	public Integer countEntities(String entity, String filter);
	
	
	/**
	 * Count all Entities of a persistence generic class
	 * 
	 * @param entity
	 * @return number of entities
	 */
	public Integer countAllEntities(String entity);

}
