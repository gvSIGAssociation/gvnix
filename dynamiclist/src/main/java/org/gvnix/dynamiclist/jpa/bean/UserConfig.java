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
package org.gvnix.dynamiclist.jpa.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Annotation configured userConfig bean.
 * 
 */
@Entity
@Table(name="USER_CONFIG")
public class UserConfig implements Serializable {

    private static final long serialVersionUID = -8712872385957386182L;
  
    private Integer id = null;
    private String idUser = null;
    private String entity = null;
    private String entityProperties = null;
    private String whereFilter = null;
    private String infoFilter = null;
    private String orderBy = null;;	
	private String groupBy = null;;
        
    /**
     * Gets id (primary key).
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    /**
     * Sets id (primary key).
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets id user.
     */
    @Column(name="IDUSER")
    public String getIdUser() {
		return idUser;
	}

    /**
     * Sets id user.
     */
	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}
    
    /**
     * Gets entity.
     */
    @Column(name="ENTITY")
	public String getEntity() {
		return entity;
	}

	/**
     * Sets entity.
     */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	/**
     * Gets entity properties.
     */
    @Column(name="ENTITY_PROPERTIES")
	public String getEntityProperties() {
		return entityProperties;
	}

    /**
     * Sets entity properties
     */
	public void setEntityProperties(String entityProperties) {
		this.entityProperties = entityProperties;
	}
	
	/**
     * Gets where filter.
     */
    @Column(name="WHEREFILTER")
	public String getWhereFilter() {
		return whereFilter;
	}

    /**
     * Sets where filter.
     */
	public void setWhereFilter(String whereFilter) {
		this.whereFilter = whereFilter;
	}

	/**
     * Gets info filter.
     */
    @Column(name="INFOFILTER")
	public String getInfoFilter() {
		return infoFilter;
	}

    /**
     * Sets info filter
     */
	public void setInfoFilter(String infoFilter) {
		this.infoFilter = infoFilter;
	}
	
    /**
     * Gets group by.
     */
    @Column(name="GROUPBY")
	public String getGroupBy() {
		return groupBy;
	}

	/**
	 * @param groupBy the groupBy to set
	 */
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	/**
     * Gets order by.
     */
    @Column(name="ORDERBY")
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	/**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getClass().getName() + "-");
        sb.append("  id=" + id);
        sb.append("  idUser=" + idUser);
        sb.append("  entity=" + entity);
        sb.append("  entityProperties=" + entityProperties);
        sb.append("  whereFilter=" + whereFilter);        
        sb.append("  infoFilter=" + infoFilter);
        sb.append("  groupBy=" + groupBy);
        sb.append("  orderBy=" + orderBy);
        
        return sb.toString();
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Indicates whether some other object is equal to this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserConfig other = (UserConfig) obj;

        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }

        return true;
    }
}
