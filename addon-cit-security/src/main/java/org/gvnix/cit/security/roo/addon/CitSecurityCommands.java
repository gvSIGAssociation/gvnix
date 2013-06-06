/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.cit.security.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Clase que declara los comandos del add-on <b>cit securty</b>
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class CitSecurityCommands implements CommandMarker {

    @Reference
    private CitSecurityOperations citSecurityOperations;

    @CliAvailabilityIndicator("cit security setup")
    public boolean isSetupAvailable() {
        return citSecurityOperations.isSetupAvailable()
                && citSecurityOperations.isSpringMvcTilesProject();
    }

    @CliCommand(value = "cit security setup", help = "Create and configure the security artifacts needed for users authorization and authentication at Conselleria de Infraestructuras y Transporte.")
    public void setup(
            @CliOption(key = "url", mandatory = true, help = "URL del servicio de autenticación.") String url,
            @CliOption(key = "login", mandatory = true, help = "Usuario de acceso al servicio.") String login,
            @CliOption(key = "password", mandatory = true, help = "Clave de acceso al servicio.") String password,
            @CliOption(key = "appName", mandatory = true, help = "Nombre de la aplicación para el servicio.") String appName) {
        citSecurityOperations.setup(url, login, password, appName);

    }

}
