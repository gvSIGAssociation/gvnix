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

import org.gvnix.dynamiclist.jpa.bean.GlobalConfig;
import org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * GlobalConfig DAO implementation.
 */
@Repository
@Transactional(readOnly = true)
public class GlobalConfigDaoImpl implements GlobalConfigDao {

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
     * @see org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao#delete(org.gvnix.dynamiclist.jpa.bean.GlobalConfig)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete(GlobalConfig globalConfig) {
    	 em.remove(em.merge(globalConfig));
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao#findGlobalConfigByEntity(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<GlobalConfig> findGlobalConfigByEntity(String entity) {
		return em.createQuery("select g from GlobalConfig g where g.entity = :entity")
        	.setParameter("entity", entity).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao#findGlobalConfigById(java.lang.Integer)
	 */
	public GlobalConfig findGlobalConfigById(Integer id) {
		return em.find(GlobalConfig.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao#findGlobalConfigs()
	 */
	@SuppressWarnings("unchecked")
	public Collection<GlobalConfig> findGlobalConfigs() {		
		return em.createQuery("select g from GlobalConfig g order by g.entity").getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao#save(org.gvnix.dynamiclist.jpa.bean.GlobalConfig)
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public GlobalConfig save(GlobalConfig globalConfig) {
		return em.merge(globalConfig);
	}

}
