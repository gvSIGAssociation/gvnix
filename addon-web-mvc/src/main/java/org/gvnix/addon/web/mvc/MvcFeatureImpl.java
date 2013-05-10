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
package org.gvnix.addon.web.mvc;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.maven.Pom;

/**
 * Implementation of {@link MvcFeature}
 * 
 * @author gvNIX Team
 */
@Component
@Service
public class MvcFeatureImpl implements MvcFeature {

    @Reference private ProjectOperations projectOperations;

    public String getName() {
        return MvcFeature.NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.project.Feature#isInstalledInModule(java.lang
     * .String)
     */
    public boolean isInstalledInModule(final String moduleName) {
        final Pom pom = projectOperations.getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }
        // Look for gvnix web mvc dependency
        for (final Dependency dependency : pom.getDependencies()) {
            if ("org.gvnix.addon.web.mvc".equals(dependency.getArtifactId())) {
                return true;
            }
        }
        return false;
    }
}
