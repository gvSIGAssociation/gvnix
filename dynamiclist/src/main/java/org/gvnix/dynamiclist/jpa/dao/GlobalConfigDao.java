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

import org.gvnix.dynamiclist.jpa.bean.GlobalConfig;


/**
 * GlobalConfig DAO interface.
 */
public interface GlobalConfigDao {

    /**
     * Find GlobalConfig by id.
     */
    public GlobalConfig findGlobalConfigById(Integer id);

    /**
     * Find globalconfigs.
     */
    public Collection<GlobalConfig> findGlobalConfigs();

    /**
     * Find GlobalConfig by entity.
     */
    public Collection<GlobalConfig> findGlobalConfigByEntity(String entity);

    /**
     * Saves GlobalConfig.
     */
    public GlobalConfig save(GlobalConfig globalConfig);

    /**
     * Deletes GlobalConfig.
     */
    public void delete(GlobalConfig globalConfig); 

}

