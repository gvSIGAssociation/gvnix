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

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.shell.ShellService;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.logging.LoggingOutputStream;

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
@Component
@Service
public class MenuEntryFileEventListener implements FileEventListener {

  /**
   * Use addon operations to delegate operations
   */
  @Reference private MenuEntryOperations operations;

  /**
   * Use to to interact with Felix to have some sort of interactive shell that
   * allows you to issue commands to the OSGi framework
   */
  @Reference
  private ShellService shellService;
  
  /**
   * Use FileManager to modify the underlying disk storage
   */
  @Reference
  private FileManager fileManager;

  /**
   * Is OSGI Roo menu component already disabled ?
   * In other words, is OSGI gvNIX menu component already activated ?
   */
  private static boolean isRooMenuDisabled = false;

  /**
   * Use to interact with the OSGi execution context including locating
   * and disabling services by reference name.
   */
  private ComponentContext componentContext;

  private static Logger logger = HandlerUtils
          .getLogger(MenuEntryFileEventListener.class);

  /** {@inheritDoc} */
  protected void activate(ComponentContext context) {
      componentContext = context;
  }

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
		if (isRooMenuDisabled) {
			return;
		}

		// If gvNIX menu is installed: disable OSGI Roo menu component 
		if (fileManager.exists(operations.getMenuConfigFile())) {

			disableRooMenuOperations();
			isRooMenuDisabled = true;
		}
  }

  /**
   * {@inheritDoc}
   */
  protected void disableRooMenuOperations() {
      logger.fine("Disable Roo MenuOperationsImpl");

      ServiceReference rooServiceRef = componentContext.getBundleContext()
              .getServiceReference(MenuOperations.class.getName());
      if (rooServiceRef != null) {
      	
	        Long componentId = (Long) rooServiceRef.getProperty("component.id");
	
	        try {
	            executeFelixCommand("scr disable ".concat(componentId.toString()));
	        } catch (Exception e) {
	            throw new IllegalStateException(
	                    "Exception disabling Roo MenuOperationsImpl service", e);
	        }
      }
  }

  /**
   * Execute Felix shell commands
   * 
   * @param commandLine
   * @throws Exception
   */
  private void executeFelixCommand(String commandLine) throws Exception {
      LoggingOutputStream sysOut = new LoggingOutputStream(Level.INFO);
      LoggingOutputStream sysErr = new LoggingOutputStream(Level.SEVERE);
      sysOut.setSourceClassName(MenuEntryOperationsImpl.class.getName());
      sysErr.setSourceClassName(MenuEntryOperationsImpl.class.getName());

      PrintStream printStreamOut = new PrintStream(sysOut);
      PrintStream printErrOut = new PrintStream(sysErr);
      try {
          shellService.executeCommand(commandLine, printStreamOut,
                  printErrOut);
      } finally {
          printStreamOut.close();
          printErrOut.close();
      }
  }

}
