/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
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
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.addon.jpa.batch;

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
 * Sample of a command class. The command class is registered by the Roo shell
 * following an automatic classpath scan. You can provide simple user
 * presentation-related logic in this class. You can return any objects from
 * each method, or use the logger directly if you'd like to emit messages of
 * different severity (and therefore different colours on non-Windows systems).
 * 
 * @since 1.1
 */
@Component
// Use these Apache Felix annotations to register your commands class in the Roo
// container
@Service
public class JpaBatchCommands implements CommandMarker { // All command types
                                                         // must implement the
                                                         // CommandMarker
                                                         // interface

    /**
     * Get a reference to the JpaOperations from the underlying OSGi container
     */
    @Reference private JpaBatchOperations operations;

    /**
     * This method is optional. It allows automatic command hiding in situations
     * when the command should not be visible. For example the 'entity' command
     * will not be made available before the user has defined his persistence
     * settings in the Roo shell or directly in the project. You can define
     * multiple methods annotated with {@link CliAvailabilityIndicator} if your
     * commands have differing visibility requirements.
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "jpa batch create", "jpa batch all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "jpa batch create", help = "Creates a Spring @Service class with methods for batch modification of a JPA Active Record entity")
    public void create(
            @CliOption(key = "entity", mandatory = true, help = "A JPA Active Record Entity") JavaType entity,
            @CliOption(key = "type", mandatory = false, help = "The java type to be created. If not set, class will be create in the same package of managed entity and a name based on entity name") JavaType target) {
        operations.create(entity, target);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     */
    @CliCommand(value = "jpa batch all", help = "Creates a Spring @Service class with methods for batch modification every JPA Active Record entity in application")
    public void all(
            @CliOption(key = "package", mandatory = false, help = "Package for created classes. If not set, classes will be create in the same package of managed entity") JavaPackage targetPackage) {
        operations.createAll(targetPackage);
    }
}