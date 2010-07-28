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

import org.gvnix.dynamiclist.jpa.bean.UserFilter;


/**
 * UserFilter DAO interface.
 */
public interface UserFilterDao {

    /**
     * Find UserFilter by id.
     */
    public UserFilter findUserFilterById(Integer id);

    /**
     * Find UserFilters.
     */
    public Collection<UserFilter> findUserFilters();

    /**
     * Find UserFilter by entity and idUser.
     */
    public Collection<UserFilter> findUserFilterByEntityAndIdUser(String entity, String idUser);

    /**
     * Saves UserFilter.
     */
    public UserFilter save(UserFilter userFilter);

    /**
     * Deletes UserFilter.
     */
    public void delete(UserFilter userFilter); 

}

