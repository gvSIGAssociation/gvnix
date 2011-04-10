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

package org.gvnix.cit.security.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

/**
 * Clase que se encarga de monitorizar los ficheros de nuestra configuración a
 * fin de mantener la integridad de los mismo.
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class CitSecurityFileEventListener implements FileEventListener {

  @Reference
  private CitSecurityOperations operations;

  @Reference
  private PathResolver pathResolver;

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent
   * (org.springframework.roo.file.monitor.event.FileEvent)
   */
  public void onFileEvent(FileEvent fileEvent) {
    // Si no esta instalado no hay nada que hacer
    if (!operations.isAlreadyInstalled()) {
      return;
    }

    if (fileEvent.getOperation() == FileOperation.MONITORING_FINISH) {
      // Nothing to do
      return;
    }

    // TODO No está definida la lógica de esta clase

    String classPath = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
        CitSecurityOperationsImpl.CLASSES_PATH);

    // Changes in security's Classes
    if (fileEvent.getFileDetails().matchesAntPath(classPath + "**")) {

      if (fileEvent.getFileDetails().getFile().getName()
          .equals(CitSecurityOperationsImpl.PROVIDER_CLASS_SHORT_NAME)) {
        // Security provider class

      }

    }

    String appSecurityXMLPath = pathResolver.getIdentifier(
        Path.SPRING_CONFIG_ROOT, "applicationContext-security.xml");
    // Changes in applicationContext-security.xml
    if (fileEvent.getFileDetails().matchesAntPath(classPath + "**")) {

    }

    // No checks needed

  }
}
