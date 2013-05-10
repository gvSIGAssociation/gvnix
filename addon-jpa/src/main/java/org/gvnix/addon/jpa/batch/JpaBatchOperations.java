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

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * <code>jpa batch</code> operations. Offers a API for all supported operations
 * for batch services for JPA Active Records entities
 * 
 * @author gvNIX Team
 * @since 1.1
 */
public interface JpaBatchOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Create a new Spring <code>@Service</code> class with methods to perform
     * batch modification in a JPA Active-Record entity. <br>
     * If <code>target</code> is not defined, the new class will be create from
     * <code>entity</code> name adding <code>BatchService</code> suffix, in the
     * same package of <code>entity</code>.
     * 
     * @param target (optional) class to create
     * @param entity JPA Active-Record entity to manage
     */
    void create(JavaType entity, JavaType target);

    /**
     * Create a new Spring <code>@Service</code> class with methods to perform
     * batch modification for every JPA Active-Record entity in current
     * application. <br>
     * The new classes will be create from <code>entity</code> name adding
     * <code>BatchService</code> suffix. If <code>targetPackage</code> is not
     * defined, generated classes in the same package of <code>entity</code>.
     * 
     * @param targetPackage (optional)
     */
    void createAll(JavaPackage targetPackage);
}