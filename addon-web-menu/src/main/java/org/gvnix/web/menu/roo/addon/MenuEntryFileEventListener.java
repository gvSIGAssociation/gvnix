/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2012 CIT - Generalitat Valenciana
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
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.process.manager.FileManager;

/**
 * Changes the menu management component of Roo with the gvNIX menu management component.
 * 
 * <p>If gvNIX menu is installed, disable OSGI Roo menu component:
 * Then OSGI gvNIX menu component will be used automatically.</p>
 *
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 1.0
 */

// Immediate required to avoid invalid gvNIX menu activation
@Component(immediate=true)
@Service
public class MenuEntryFileEventListener implements FileEventListener {

  /**
   * Use addon operations to delegate operations
   */
  @Reference private MenuEntryOperations operations;

  /**
   * Use FileManager to modify the underlying disk storage
   */
  @Reference
  private FileManager fileManager;

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent
   * (org.springframework.roo.file.monitor.event.FileEvent)
   */
  public void onFileEvent(FileEvent fileEvent) {

	    if (fileEvent.getOperation() == FileOperation.MONITORING_FINISH) {
	      // Nothing to do
	      return;
	    }

		// If OSGI Roo menu component already disabled, nothing to do
		if (MenuEntryOperationsImpl.isRooMenuDisabled) {
			return;
		}

		// If gvNIX menu is installed: disable OSGI Roo menu component 
		if (fileManager.exists(operations.getMenuConfigFile())) {

			operations.disableRooMenuOperations();
		}
  }

}
