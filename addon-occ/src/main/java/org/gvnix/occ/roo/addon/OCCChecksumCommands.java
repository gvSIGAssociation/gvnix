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
package org.gvnix.occ.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * <p>
 * <b>Optimistic Concurrency Control of gvNIX Commands</b> class
 * </p>
 * <p>
 * Commands to manage Optimistic Concurrency Control gvNIX implementation
 * </p>
 * <p>
 * This is based on a checksum computed from values of all local properties of a
 * entity. It's so recommended for legacy databases that hasn't any valid
 * version field and you can't add it.
 * </p>
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class OCCChecksumCommands implements CommandMarker {

    @Reference private OCCChecksumOperations operations;

    @CliAvailabilityIndicator({ "occ checksum set", "occ checksum all" })
    public boolean isOCCChecksumAvailable() {
        return operations.isOCCChecksumAvailable();
    }

    @CliCommand(value = "occ checksum set", help = "Add the gvNIX Optimistic Concurrecy Control Checksum based behaivor to a Entity")
    public void addOCCChecksumToEntity(
            @CliOption(key = "entity", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the entity object to add OCC") JavaType entity,
            @CliOption(key = "fieldName", mandatory = false, help = "The name of the field to use to store de checksum value") String fieldName,
            @CliOption(key = "digestMethod", mandatory = false, help = "The name of the type of digest method to compute the checksum") String digestMethod

    ) {
        operations.addOccToEntity(entity, fieldName, digestMethod);
    }

    @CliCommand(value = "occ checksum all", help = "Add the gvNIX Optimistic Concurrecy Control Checksum based behaivor to all entities in project")
    public void addOCCChecksumAll(
            @CliOption(key = "fieldName", mandatory = false, help = "The name of the field to use to store de checksum value") String fieldName,
            @CliOption(key = "digestMethod", mandatory = false, help = "The name of the type of digest method to compute the checksum") String digestMethod) {
        operations.addOccAll(fieldName, digestMethod);
    }

}
