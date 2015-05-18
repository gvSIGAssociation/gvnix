/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.web.menu.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.process.manager.FileManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Changes the menu management component of Roo with the gvNIX menu management
 * component.
 * <p>
 * If gvNIX menu is installed, disable OSGI Roo menu component: Then OSGI gvNIX
 * menu component will be used automatically.
 * </p>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.0
 */

// Immediate required to avoid invalid gvNIX menu activation
@Component
@Service
public class MenuEntryFileEventListener implements FileEventListener {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(MenuEntryFileEventListener.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    /**
     * Use addon operations to delegate operations
     */
    private MenuEntryOperations operations;

    /**
     * Use FileManager to modify the underlying disk storage
     */
    private FileManager fileManager;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /*
     * (non-Javadoc)
     * 
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
        if (MenuEntryOperationsImpl.isRooMenuDisabled()) {
            return;
        }

        // If gvNIX menu is installed: disable OSGI Roo menu component
        if (getFileManager().exists(getOperations().getMenuConfigFile())) {

            getOperations().disableRooMenuOperations();
        }
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on MenuEntryFileEventListener.");
                return null;
            }
        }
        else {
            return fileManager;
        }

    }

    public MenuEntryOperations getOperations() {
        if (operations == null) {
            // Get all Services implement MenuEntryOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MenuEntryOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MenuEntryOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MenuEntryOperations on MenuEntryFileEventListener.");
                return null;
            }
        }
        else {
            return operations;
        }

    }

}
