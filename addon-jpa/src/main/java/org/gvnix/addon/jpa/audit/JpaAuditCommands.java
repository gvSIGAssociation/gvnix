/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.audit;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * <code>jpa audit</code> commands class
 * 
 * @author gvNIX Team
 * @since 1.1
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
     * Includes automatically-filled audit information to a JPA Active Record
     * entity (Creation/lastUpdate date and userName)
     * 
     * @param entity a JPA Active Record Entity
     * @param target (optional) the java type to be created. If not set,
     *        entity-listener-class will be create in the same package of
     *        managed entity and a name based on entity name
     */
    @CliCommand(value = "jpa audit add", help = "Adds simple auditory information (creation/lastUpdate user an date) to a entity which is managed automatically.")
    public void create(
            @CliOption(key = "entity", mandatory = true, help = "a JPA Active Record Entity") JavaType entity,
            @CliOption(key = "type", mandatory = false, help = "the java type to be created as entity-listener. If not set, class will be create in the same package of managed entity and a name based on entity name") JavaType target) {
        operations.create(entity, target);
    }

    /**
     * Includes automatically-filled audit information to every JPA Active
     * Record entity in application (Creation/lastUpdate date and userName)
     * 
     * @param targetPackage package for created classes. If not set, classes
     *        will be create in the same package of managed entity
     */
    @CliCommand(value = "jpa audit all", help = "Adds simple auditory information (creation/lastUpdate user an date) to each project entity which is managed automatically.")
    public void all(
            @CliOption(key = "package", mandatory = false, help = "package for created classes. If not set, classes will be create in the same package of managed entity") JavaPackage targetPackage) {
        operations.createAll(targetPackage);
    }
}