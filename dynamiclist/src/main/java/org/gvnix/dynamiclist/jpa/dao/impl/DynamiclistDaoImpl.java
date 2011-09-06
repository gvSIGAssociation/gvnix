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
package org.gvnix.dynamiclist.jpa.dao.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gvnix.dynamiclist.jpa.dao.DynamiclistDao;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * Dynamiclist DAO implementation.
 *
 */
@Configurable
@Repository (value = "dynamiclistDao")
@Transactional(readOnly = true)
public class DynamiclistDaoImpl implements DynamiclistDao{
	
	private Log log = LogFactory.getLog(this.getClass());
	private EntityManager em = null;

    /**
     * Sets the entity manager.
     */
    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    
    /**
     * Find generic entities using a start index and max number of results.
     */    
    public Collection<?> findEntities(String entity, int startIndex, int maxResults, String filter, String orderBy) {
        
    	StringBuffer buffer = new StringBuffer("select e from ");
    	buffer.append(entity);
    	buffer.append(" e");
    	
    	if (StringUtils.isNotEmpty(filter)){
    		buffer.append(" WHERE ");
    		buffer.append(filter);
    	}    	
    	if (StringUtils.isNotEmpty(orderBy)){
    		buffer.append(" ORDER BY ");    		
    		buffer.append(orderBy);
    	}
    	
    	if (log.isDebugEnabled()){
    		log.debug("createQuery qlString ["+ buffer.toString() + "]");
    	}
    	    	
    	Query query = em.createQuery(buffer.toString());
    	// add the pagination
    	query.setFirstResult(startIndex).setMaxResults(maxResults).getResultList();    	
    	return query.getResultList();
    }
    

    /*
     * (non-Javadoc)
     * @see org.gvnix.dynamiclist.jpa.dao.DynamiclistDao#CountAllEntities(java.lang.String)
     */
	public Integer countAllEntities(String entity) {		
		//return em.createQuery("select e from " + entity + " e").getResultList().size();		
		return ((Number)em.createQuery("SELECT COUNT(e) FROM " + entity + " e").getSingleResult()).intValue();
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.DynamiclistDao#countEntities(java.lang.String, java.lang.String)
	 */
	public Integer countEntities(String entity, String filter) {
		StringBuffer buffer = new StringBuffer("SELECT COUNT(e) FROM ");
    	buffer.append(entity);
    	buffer.append(" e");
    	
    	if (StringUtils.isNotEmpty(filter)){
    		buffer.append(" WHERE ");
    		buffer.append(filter);
    	}    	
    	
		return ((Number)em.createQuery(buffer.toString()).getSingleResult()).intValue();
	}
	
}
