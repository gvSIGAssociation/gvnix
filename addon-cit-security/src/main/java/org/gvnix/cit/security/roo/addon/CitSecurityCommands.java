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
package org.gvnix.cit.security.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.shell.*;

/**
 * Clase que declara los comandos del add-on <b>cit securty</b>
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class CitSecurityCommands implements CommandMarker {

    private static Logger logger = Logger.getLogger(CitSecurityCommands.class
	    .getName());

    @Reference
    private CitSecurityOperations citSecurityOperations;

    @CliAvailabilityIndicator("cit security setup")
    public boolean isSetupAvailable() {
	return citSecurityOperations.isSetupAvailable();
    }

    @CliCommand(value = "cit security setup", help = "Configura la autenticación de usuarios con el mecanismo de la Consellería de Infraestructuras y Transportes.")
    public void setup(@CliOption(key="url", mandatory=true, help="URL del servicio de autenticación.") String url,
	    @CliOption(key = "login", mandatory = true, help = "Usuario de acceso al servicio.") String login,
	    @CliOption(key = "password", mandatory = true, help = "Clave de acceso al servicio.") String password,
	    @CliOption(key = "appName", mandatory = true, help = "Nombre de la aplicación para el servicio.") String appName) {
	citSecurityOperations.setup(url, login, password, appName);

    }

}
