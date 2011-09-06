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
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

import org.gvnix.dynamiclist.jpa.bean.UserFilter;
import org.gvnix.dynamiclist.jpa.dao.UserFilterDao;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * UserFilter DAO implementation.
 */
@Configurable
@Repository
public class UserFilterDaoImpl implements UserFilterDao {

	@PersistenceContext
	private EntityManager em;

	
	/*@PersistenceUnit(unitName = "springappPU")
	private EntityManagerFactory emf;
	public void setEntityManagerFactory(EntityManagerFactory emf) {
	this.emf = emf;
	}

	private EntityManager getEntityManager() {
	return emf.createEntityManager();
	}

	public Long create(PaymentAmount paymentAmount) {
	EntityManager entityManager = getEntityManager();
	EntityTransaction t = entityManager.getTransaction();
	t.begin();
	try {
	entityManager.persist(paymentAmount);
	}
	catch (Exception up) {
	t.rollback();
	throw up;
	}
	finally{
	try {
	if(t.isActive())
	{
	t.commit();
	}
	} catch (Exception ex) {
	t.rollback();
	}
	}
	return paymentAmount.getId();
	}
	*/
	
   /*
    * (non-Javadoc)
    * @see org.gvnix.dynamiclist.jpa.dao.UserFilterDao#delete(org.gvnix.dynamiclist.jpa.bean.UserFilter)
    */
	@Transactional
	public void delete(UserFilter userFilter) throws Exception{
		em.remove(em.merge(userFilter));		    	
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserFilterDao#findUserFilterByEntityAndIdUser(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<UserFilter> findUserFilterByEntityAndIdUser(String entity, String idUser) {
		return em.createQuery("select u from UserFilter u where u.entity = :entity and u.idUser = :idUser")
        	.setParameter("entity", entity).setParameter("idUser", idUser).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserFilterDao#findUserFilterById(java.lang.Integer)
	 */
	public UserFilter findUserFilterById(Integer id) {
		return em.find(UserFilter.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserFilterDao#findUserFilters()
	 */
	@SuppressWarnings("unchecked")
	public Collection<UserFilter> findUserFilters() {		
		return em.createQuery("select u from UserFilter u order by u.entity, u.idUser").getResultList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.jpa.dao.UserFilterDao#save(org.gvnix.dynamiclist.jpa.bean.UserFilter)
	 */	
	@Transactional
	public UserFilter save(UserFilter userFilter) {
		return em.merge(userFilter);
	}

}
