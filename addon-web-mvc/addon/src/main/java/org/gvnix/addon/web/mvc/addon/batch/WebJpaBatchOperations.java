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
package org.gvnix.addon.web.mvc.addon.batch;

import org.gvnix.addon.jpa.annotations.batch.GvNIXJpaBatch;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * <code>web mvc batch</code> operations. Offers a API for all supported
 * operations for batch services for JPA Active Records entities in a
 * controller.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
public interface WebJpaBatchOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX MVC has been setup in this project
     */
    public static final String FEATURE_NAME_GVNIX_MVC_BATCH = "gvnix-mvc-batch";

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Indicate setup should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupAvailable();

    /**
     * Add support for JPA Batch operations in a controller
     * 
     * @param controller class to create
     * @param service (optional) {@link GvNIXJpaBatch} annotated-Spring service
     *        to use. If not set it tries to locate using controller
     *        formBackingObject
     */
    void add(JavaType controller, JavaType service);

    /**
     * Add support for JPA Batch operations in all controllers which
     * formBackingObject has a related JPA Batch service (annotated with
     * {@link GvNIXJpaBatch}
     */
    void addAll();

    /**
     * Initializes/updates required artifacts to allow support of Web jpa batch
     * operations
     * 
     * @param targetPackage
     */
    void setup();
}