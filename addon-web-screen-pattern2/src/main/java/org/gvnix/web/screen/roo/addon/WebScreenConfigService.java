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
package org.gvnix.web.screen.roo.addon;

/**
 * <p>
 * Web MVC Pattern configuration service
 * </p>
 * 
 * <p>
 * Contains utility method to configure target project
 * </p>
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public interface WebScreenConfigService {

    /**
     * Setup all add-on artifacts:
     * <ul>
     * <li>Maven repositories</>
     * <li>Maven properties</>
     * <li>Maven dependencies: the add-on itself</li>
     * <li>copy all resources needed</>
     * </ul>
     * 
     */
    void setup();

    /**
     * Indicate if is a Spring MVC project
     * 
     * @return
     */
    boolean isSpringMvcProject();
}
