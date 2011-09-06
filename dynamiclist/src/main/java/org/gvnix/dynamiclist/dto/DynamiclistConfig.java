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
package org.gvnix.dynamiclist.dto;

import java.io.Serializable;
import java.util.Collection;

import org.gvnix.dynamiclist.jpa.bean.GlobalConfig;
import org.gvnix.dynamiclist.jpa.bean.GlobalFilter;
import org.gvnix.dynamiclist.jpa.bean.UserConfig;
import org.gvnix.dynamiclist.jpa.bean.UserFilter;

/**
 * 
 * Dynamiclist Config bean.
 * <p>Bean store the state of dynamiclist in session scope</p>
 *
 */
public class DynamiclistConfig implements Serializable {

	
	private static final long serialVersionUID = 3905769101491567208L;
	
	private String classObjectName = null;
	private String classObjectSimpleName = null;
	
	//
	private GlobalConfig globalConfig;
	private UserConfig userConfig;
	
	private Collection<GlobalFilter> globalFilters;
	private Integer idGlobalFilterDefault;
	
	private Collection<UserFilter> userFilters;
	private Integer idUserFilterDefault;	
		
	private String actualHumanWhereFilter = "";
	private String actualWhereFilter= "";
			
	private Integer idActualFilter = null;
	
	/**
	 * Type actual filter (G=global, U=User)
	 */ 
	private String typeActualFilter = "";
	
	
	//actual QUERY
	private Collection<String> actualFieldsNames;
	private String actualOrderBy = "";
	private String actualOrderByColumn = "";
	private String actualGroupBy = "";
	
	private Integer actualPage = null;
	
	public GlobalConfig getGlobalConfig() {
		return globalConfig;
	}

	public void setGlobalConfig(GlobalConfig globalConfig) {
		this.globalConfig = globalConfig;
	}

	public UserConfig getUserConfig() {
		return userConfig;
	}

	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}

	public Collection<GlobalFilter> getGlobalFilters() {
		return globalFilters;
	}

	public void setGlobalFilters(Collection<GlobalFilter> globalFilters) {
		this.globalFilters = globalFilters;
	}

	public Integer getIdGlobalFilterDefault() {
		return idGlobalFilterDefault;
	}

	public void setIdGlobalFilterDefault(Integer idGlobalFilterDefault) {
		this.idGlobalFilterDefault = idGlobalFilterDefault;
	}

	public Collection<UserFilter> getUserFilters() {
		return userFilters;
	}

	public void setUserFilters(Collection<UserFilter> userFilters) {
		this.userFilters = userFilters;
	}

	public Integer getIdUserFilterDefault() {
		return idUserFilterDefault;
	}

	public void setIdUserFilterDefault(Integer idUserFilterDefault) {
		this.idUserFilterDefault = idUserFilterDefault;
	}	

	public Integer getIdActualFilter() {
		return idActualFilter;
	}

	public void setIdActualFilter(Integer idActualFilter) {
		this.idActualFilter = idActualFilter;
	}

	/**	  
	 * Type actual filter (G=global, U=User)
	 * 	  
	 * @return String "G" or "U"
	 */
	public String getTypeActualFilter() {
		return typeActualFilter;
	}

	public void setTypeActualFilter(String typeActualFilter) {
		this.typeActualFilter = typeActualFilter;
	}

	public Collection<String> getActualFieldsNames() {
		return actualFieldsNames;
	}

	public void setActualFieldsNames(Collection<String> actualFieldsNames) {
		this.actualFieldsNames = actualFieldsNames;
	}

	public String getActualOrderBy() {
		return actualOrderBy;
	}

	public void setActualOrderBy(String actualOrderBy) {
		this.actualOrderBy = actualOrderBy;
	}

	public String getActualGroupBy() {
		return actualGroupBy;
	}

	public void setActualGroupBy(String actualGroupBy) {
		this.actualGroupBy = actualGroupBy;
	}

	public String getActualOrderByColumn() {
		return actualOrderByColumn;
	}

	public void setActualOrderByColumn(String actualOrderByColumn) {
		this.actualOrderByColumn = actualOrderByColumn;
	}

	public String getClassObjectName() {
		return classObjectName;
	}

	public void setClassObjectName(String classObjectName) {
		this.classObjectName = classObjectName;
	}

	public String getClassObjectSimpleName() {
		return classObjectSimpleName;
	}

	public void setClassObjectSimpleName(String classObjectSimpleName) {
		this.classObjectSimpleName = classObjectSimpleName;
	}

	public String getActualHumanWhereFilter() {
		return actualHumanWhereFilter;
	}

	public void setActualHumanWhereFilter(String actualHumanWhereFilter) {
		this.actualHumanWhereFilter = actualHumanWhereFilter;
	}

	public String getActualWhereFilter() {
		return actualWhereFilter;
	}

	public void setActualWhereFilter(String actualWhereFilter) {
		this.actualWhereFilter = actualWhereFilter;
	}

	public Integer getActualPage() {
		return actualPage;
	}

	public void setActualPage(Integer actualPage) {
		this.actualPage = actualPage;
	}	
}
