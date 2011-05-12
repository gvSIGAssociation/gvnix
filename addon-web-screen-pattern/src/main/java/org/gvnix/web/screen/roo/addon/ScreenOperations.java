/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.screen.roo.addon;

/**
 * Interface of operations this add-on offers. Typically used by a command type 
 * or an external add-on
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @since 0.6
 */
public interface ScreenOperations {

    /**
     * Indicate project should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isProjectAvailable();

    /**
     * Indicate project has a web layer based on Spring MVC.
     * 
     * @return true if the user installed an Spring MVC web layer, otherwise
     *         returns false.
     */
    boolean isSpringMvcProject();

    /**
     * Informs this addon has been installed.
     * 
     * @return
     */
    boolean isActivated();

    /**
     * Create or update web layer artefacts.
     */
   void createWebArtefacts();

    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();
}