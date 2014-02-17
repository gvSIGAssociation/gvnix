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
import java.util.Set;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * Structure of UserDetail's C.I.T information provided by S.A.F.E. service.
 *
 * @author jmvivo <a href="mailto:jmvivo@disid.com">Jose Manuel Vivó Arnal</a>
 * @author miborra <a href="mailto:miborra@disid.com">Manuel Iborra</a>
 * @author jcgarcia <a href="mailto:jcgarcia@disid.com">Juan Carlos García del Canto</a>
 *
 */
@RooJavaBean
public class WsSafeUser implements UserDetails, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5767016615242591655L;

	// ************** <UserDetails> ****************
	private String username;
	private String password;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private Set<GrantedAuthority> authorities;

	// ************** </UserDetails> ****************

	// ************** <SAFE data> ****************

	private String nombre;
	private String email;
	private String apellido1;
	private String apellido2;
	private String cif;
	private String habilitado;
	private String idHDFI;
	private String iusserDN;
	private String nif;
	private String oid;
	private String razonSocial;
	private String representante;
	private String serialNumber;
	private String subjectDN;
	private String tipoAut;
	private String tipoCertificado;

	// ************** </SAFE data> ****************

}
