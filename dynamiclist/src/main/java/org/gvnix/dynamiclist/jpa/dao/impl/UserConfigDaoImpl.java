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

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.gvnix.dynamiclist.jpa.bean.UserConfig;
import org.gvnix.dynamiclist.jpa.dao.UserConfigDao;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * UserConfig DAO implementation.
 */
@Configurable
@Repository
@Transactional(readOnly = true)
public class UserConfigDaoImpl implements UserConfigDao {

	
    transient EntityManager entityManager;

    /**
     * Sets the entity manager.
     */
    @PersistenceContext   
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
     * (non-Javadoc)
     * @see org.gvnix.dynamiclist.jpa.dao.UserConfigDao#delete(org.gvnix.dynamiclist.jpa.bean.UserConfig)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete(UserConfig userConfig) {
    	entityManager.remove(entityManager.merge(userConfig));
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserConfigDao#findUserConfigByEntityAndIdUser(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<UserConfig> findUserConfigByEntityAndIdUser(String entity, String idUser) {
		return entityManager.createQuery("select u from UserConfig u where u.entity = :entity and u.idUser = :idUser")
        	.setParameter("entity", entity).setParameter("idUser", idUser).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserConfigDao#findUserConfigById(java.lang.Integer)
	 */
	public UserConfig findUserConfigById(Integer id) {
		return entityManager.find(UserConfig.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserConfigDao#findUserConfigs()
	 */
	@SuppressWarnings("unchecked")
	public Collection<UserConfig> findUserConfigs() {		
		return entityManager.createQuery("select u from UserConfig u order by u.entity, u.idUser").getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserConfigDao#save(org.gvnix.dynamiclist.jpa.bean.UserConfig)
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public UserConfig save(UserConfig userConfig) {
		userConfig = entityManager.merge(userConfig);
		this.entityManager.flush();
		return userConfig;
	}

}
