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
package org.gvnix.web.report.roo.addon;

public interface ReportConfigService {

    /**
     * Setup all add-on artifacts:
     * <ul>
     * <li>Maven repositories</>
     * <li>Maven properties</>
     * <li>Maven dependencies: JasperReports, the add-on itself</li>
     * <li>JaperReports views resolver in webmvc-config.xml</>
     * <li>copy the jasper-views.xml config file</>
     * </ul>
     *
     */
    void setup();

    /**
     * Add the ViewResolver config to webmvc-config.xml
     */
    public void addJasperReportsViewResolver();

    /**
     * Indicate if is a Spring MVC project
     *
     * @return
     */
    boolean isSpringMvcProject();

    /**
     * Indicate if JasperReports views are already installed
     * 
     * @return
     */
    boolean isJasperViewsProject();

    /**
     * Indicate the project has a web layer based on Spring MVC Tiles.
     * 
     * @return true if the user installed an Spring MVC Tiles web layer,
     *         otherwise returns false.
     */
    boolean isSpringMvcTilesProject();
}
