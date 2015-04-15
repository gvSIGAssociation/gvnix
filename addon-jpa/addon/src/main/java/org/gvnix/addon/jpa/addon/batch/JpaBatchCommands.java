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
package org.gvnix.addon.jpa.addon.batch;

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
 * <code>jpa batch</code> commands class
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public class JpaBatchCommands implements CommandMarker {

    /**
     * Get a reference to the JpaBatchOperations from the underlying OSGi
     * container
     */
    @Reference
    private JpaBatchOperations operations;

    /**
     * Informs if <code>jpa batch</code> commands are available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "jpa batch add", "jpa batch all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * Creates a Spring @Service class with methods for batch modification of a
     * JPA Active Record entity
     * 
     * @param entity a JPA Active Record Entity
     * @param target (optional) the java type to be created. If not set, class
     *        will be create in the same package of managed entity and a name
     *        based on entity name
     */
    @CliCommand(value = "jpa batch add",
            help = "Adds batch support for given entity by creating a Spring @Service class with batch operation methods")
    public void create(
            @CliOption(key = "entity",
                    mandatory = true,
                    help = "a JPA Active Record Entity") JavaType entity,
            @CliOption(key = "type",
                    mandatory = false,
                    help = "the java type to be created. If not set, class will be create in the same package of managed entity and a name based on entity name") JavaType target) {
        operations.create(entity, target);
    }

    /**
     * Creates a Spring @Service class with methods for batch modification every
     * JPA Active Record entity in application
     * 
     * @param targetPackage package for created classes. If not set, classes
     *        will be create in the same package of managed entity
     */
    @CliCommand(value = "jpa batch all",
            help = "Adds batch support for all entities by creating a Spring @Service class with batch operation methods for each JPA Active Record entity")
    public void all(
            @CliOption(key = "package",
                    mandatory = false,
                    help = "package for created classes. If not set, classes will be create in the same package of managed entity") JavaPackage targetPackage) {
        operations.createAll(targetPackage);
    }
}