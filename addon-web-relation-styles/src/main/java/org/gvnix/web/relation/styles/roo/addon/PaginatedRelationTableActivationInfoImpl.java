/*
 * Copyright (C) 2009 - CONSELLERIA D'INFRAESTRUCTURES I TRANSPORT 
 *                      GENERALITAT VALENCIANA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * You may obtain a copy of the License at http://www.gnu.org/licenses/gpl-2.0.html
 */
package org.gvnix.web.relation.styles.roo.addon;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;

@Service
@Component(immediate = true)
public class PaginatedRelationTableActivationInfoImpl implements
	PaginatedRelationTableActivationInfo {

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.relation.styles.roo.addon.PaginatedRelationTableActivationInfo
     * #isActivated()
     */
    public boolean isActivated() {

	if (!fileManager.exists(pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP,
		"WEB-INF/tags/util/gvnixcallfunction.tagx"))) {
	    return false;
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.relation.styles.roo.addon.PaginatedRelationTableActivationInfo
     * #isProjectAvailable()
     */
    public boolean isProjectAvailable() {

	if (getPathResolver() == null) {
	    return false;
	}

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");

	if (!fileManager.exists(webXmlPath)) {
	    return false;
	}
	return true;
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.web.relation.styles.roo.addon.PaginatedRelationTableActivationInfo
     * #isWebScaffoldGenerated()
     */
    public boolean isWebScaffoldGenerated() {

	if (!fileManager.exists(pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, "WEB-INF"))) {
	    return false;
	}
	return true;
    }

}
