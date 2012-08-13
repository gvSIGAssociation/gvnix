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
package org.gvnix.web.menu.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.project.Dependency;

/**
 * Dependency listener to be notified of changes to project dependencies.
 * 
 * @author Enrique Ruiz( eruiz at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class DependencyListenerImpl implements MetadataNotificationListener {

  public void notify(String upstreamDependency, String downstreamDependency) {
    // Required when implements change from DependencyListener to MetadataNotificationListener
	// TODO Is required now some extra code here ? 
  }
  
  /**
   * Use PageOperations to execute operations 
   */
  @Reference private MenuEntryOperations operations;

  /**
   * If Spring Security was added manually, update web artifacts to use secure
   * tags.
   * <p>
   * TODO: Unit tests
   * @param d
   */
  public void dependencyAdded(Dependency d) {
    if("spring-security-core".equals(d.getArtifactId())
        && "org.springframework.security".equals(d.getGroupId())) {
      operations.createWebArtefacts("~.web.menu");
    }
  }

  /**
   * If Spring Security was removed manually, update web artifacts to use 
   * unsecure tags.
   * <p>
   * TODO: Unit tests
   * @param d
   */
  public void dependencyRemoved(Dependency d) {
    if("spring-security-core".equals(d.getArtifactId())
        && "org.springframework.security".equals(d.getGroupId())) {
      operations.createWebArtefacts("~.web.menu");
    }
  }
}
