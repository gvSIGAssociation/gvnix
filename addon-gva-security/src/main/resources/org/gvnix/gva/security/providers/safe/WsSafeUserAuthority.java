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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.security.core.GrantedAuthority;

/**
 * Structure of module's C.I.T information provided by S.A.F.E. service.
 *
 * @author jmvivo <a href="mailto:jmvivo@disid.com">Jose Manuel Vivó Arnal</a>
 * @author miborra <a href="mailto:miborra@disid.com">Manuel Iborra</a>
 *
 */
@RooJavaBean
public class WsSafeUserAuthority implements GrantedAuthority, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2443806778851127910L;

	// ************** <GrantedAuthority> ****************

	private String authority;

	// ************** </GrantedAuthority> ****************

	// ************** <SAFE data> ****************
	
	private String nif;
	private String usrtipo;
    protected String idgrupo;
    protected String idrol;
    protected String idaplicacion;

	// ************** <SAFE data> ****************

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((authority == null) ? 0 : authority.hashCode());
		result = prime * result + ((nif == null) ? 0 : nif.hashCode());
		result = prime * result + ((usrtipo == null) ? 0 : usrtipo.hashCode());
		result = prime * result + ((idgrupo == null) ? 0 : idgrupo.hashCode());
		result = prime * result + ((idrol == null) ? 0 : idrol.hashCode());
		result = prime * result + ((idaplicacion == null) ? 0 : idaplicacion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WsSafeUserAuthority other = (WsSafeUserAuthority) obj;
		if (authority == null) {
			if (other.authority != null)
				return false;
		} else if (!authority.equals(other.authority))
			return false;
		if (nif == null) {
			if (other.nif != null)
				return false;
		} else if (!nif.equals(other.nif))
			return false;
		if (usrtipo == null) {
			if (other.usrtipo != null)
				return false;
		} else if (!usrtipo.equals(other.usrtipo))
			return false;
		if (idgrupo == null) {
			if (other.idgrupo != null)
				return false;
		} else if (!idgrupo.equals(other.idgrupo))
			return false;
		if (idrol == null) {
			if (other.idrol != null)
				return false;
		} else if (!idrol.equals(other.idrol))
			return false;
		if (idaplicacion == null) {
			if (other.idaplicacion != null)
				return false;
		} else if (!idaplicacion.equals(other.idaplicacion))
			return false;
		return true;
	}

}
