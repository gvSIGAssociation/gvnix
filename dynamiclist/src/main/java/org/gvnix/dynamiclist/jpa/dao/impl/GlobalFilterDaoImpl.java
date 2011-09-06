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

import org.gvnix.dynamiclist.jpa.bean.GlobalFilter;
import org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * GlobalFilter DAO implementation.
 */
@Configurable
@Repository
@Transactional(readOnly = true)
public class GlobalFilterDaoImpl implements GlobalFilterDao {

    private EntityManager em = null;

    /**
     * Sets the entity manager.
     */
    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    /*
     * (non-Javadoc)
     * @see org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao#delete(org.gvnix.dynamiclist.jpa.bean.GlobalFilter)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete(GlobalFilter globalFilter) {
    	 em.remove(em.merge(globalFilter));
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao#findGlobalFilterByEntity(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<GlobalFilter> findGlobalFilterByEntity(String entity) {
		return em.createQuery("select g from GlobalFilter g where g.entity = :entity")
        	.setParameter("entity", entity).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao#findGlobalFilterById(java.lang.Integer)
	 */
	public GlobalFilter findGlobalFilterById(Integer id) {
		return em.find(GlobalFilter.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao#findGlobalFilters()
	 */
	@SuppressWarnings("unchecked")
	public Collection<GlobalFilter> findGlobalFilters() {		
		return em.createQuery("select g from GlobalFilter g order by g.entity").getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao#save(org.gvnix.dynamiclist.jpa.bean.GlobalFilter)
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public GlobalFilter save(GlobalFilter globalFilter) {
		return em.merge(globalFilter);
	}

}
