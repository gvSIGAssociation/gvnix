/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.jpa.addon.audit;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.addon.audit.providers.RevisionLogProviderId;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * <code>jpa audit</code> commands class
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.3.0
 */
@Component
@Service
public class JpaAuditCommands implements CommandMarker {

    /**
     * Get a reference to the JpaAuditOperations from the underlying OSGi
     * container
     */
    @Reference
    private JpaAuditOperations operations;

    /**
     * Informs if <code>jpa audit setup</code> command is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "jpa audit setup" })
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * Informs if <code>jpa audit</code> commands are available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "jpa audit add", "jpa audit all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * Informs if <code>jpa audit revisionLog</code> commands is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "jpa audit revisionLog" })
    public boolean isProvidersAvailable() {
        return isCommandAvailable() && operations.isProvidersAvailable()
                && operations.getActiveRevisionLogProvider() == null;
    }

    /**
     * Initializes JPA audit support in this project
     * <p/>
     * Creates a class which will provide the user information for audit
     * purpose.
     * 
     * @param service (optional) to create. If not set a AuditClienteService
     *        will be created on lowest package were an entity is found
     * @param userType (optional) to store as user information. If not set uses
     *        {@link String}
     */
    @CliCommand(value = "jpa audit setup",
            help = "Initializes jpa audit support in this project.")
    public void setup(
            @CliOption(key = "service",
                    mandatory = false,
                    help = "Class which will provide the user information for audit purpose") JavaType service,
            @CliOption(key = "userType",
                    mandatory = false,
                    help = "the java type of user information to store in aduit features. If not set, uses String") JavaType userType) {
        operations.setup(service, userType);
    }

    /**
     * Includes automatically-filled audit information to a JPA Active Record
     * entity (Creation/lastUpdate date and userName)
     * 
     * @param entity a JPA Active Record Entity
     * @param target (optional) the java type to be created. If not set,
     *        entity-listener-class will be create in the same package of
     *        managed entity and a name based on entity name
     */
    @CliCommand(value = "jpa audit add",
            help = "Adds simple auditory information (creation/lastUpdate user an date) to a entity which is managed automatically.")
    public void create(
            @CliOption(key = "entity",
                    mandatory = true,
                    help = "a JPA Active Record Entity") JavaType entity,
            @CliOption(key = "type",
                    mandatory = false,
                    help = "the java type to be created as entity-listener. If not set, class will be create in the same package of managed entity and a name based on entity name") JavaType target) {
        operations.create(entity, target);
    }

    /**
     * Includes automatically-filled audit information to every JPA Active
     * Record entity in application (Creation/lastUpdate date and userName)
     * 
     * @param targetPackage package for created classes. If not set, classes
     *        will be create in the same package of managed entity
     */
    @CliCommand(value = "jpa audit all",
            help = "Adds simple auditory information (creation/lastUpdate user an date) to each project entity which is managed automatically.")
    public void all(
            @CliOption(key = "package",
                    mandatory = false,
                    help = "package for created classes. If not set, classes will be create in the same package of managed entity") JavaPackage targetPackage) {
        operations.createAll(targetPackage);
    }

    /**
     * Setup a revision log provider to store all changes of audited entities in
     * DB
     * 
     * @param provider to use to handle revision log information
     */
    @CliCommand(value = "jpa audit revisionLog",
            help = "Enable the revision log provider for audit entity changes.")
    public void revisionLog(
            @CliOption(key = "provider",
                    mandatory = true,
                    help = "Provider to use to handle revision log information") RevisionLogProviderId provider) {
        operations.activeRevisionLog(provider);
    }
}