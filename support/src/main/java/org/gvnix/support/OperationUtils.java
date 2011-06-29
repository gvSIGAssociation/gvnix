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
package org.gvnix.support;

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;

/**
 * Utils for Command classes.
 * 
 * @author Mario Martínez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class OperationUtils {

    /**
     * Check if there is a project available.
     * 
     * @param metadataService
     *            Metadata Service component
     * @return Is project available ?
     */
    public static boolean isProjectAvailable(MetadataService metadataService) {
        return getPathResolver(metadataService) != null;
    }

    /**
     * Get the path resolver or null if there is no project available.
     * 
     * @param metadataService
     *            Metadata Service component
     * @return Path resolver or null
     */
    public static PathResolver getPathResolver(MetadataService metadataService) {

        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        if (projectMetadata == null) {

            return null;
        }

        return projectMetadata.getPathResolver();
    }

    /**
     * Check if current project is a Spring MVC one
     * 
     * @param metadataService
     *            Metadata Service component
     * @param fileManager
     *            FileManager component
     * @return
     */
    public static boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager) {
        PathResolver pathResolver = getPathResolver(metadataService);
        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");

        if (!fileManager.exists(webXmlPath)) {
            return false;
        }
        return true;
    }

}
