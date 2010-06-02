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
package org.gvnix.web.mvc.jsp20.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;

/**
 * pom.xml file changes listener
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ControllerJsp20SupportPomListener implements FileEventListener {

    private String pom;

    @Reference
    private PathResolver pathResolver;
    @Reference
    private ControllerJsp20SupportOperations operations;

    protected void activate(ComponentContext context) {
	this.pom = pathResolver.getIdentifier(Path.ROOT, "/pom.xml");
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent(org.springframework.roo.file.monitor.event.FileEvent)
     */
    public void onFileEvent(FileEvent fileEvent) {
	Assert.notNull(fileEvent, "File event required");

	// If operations is not our implementation do nothing
	if (!(operations instanceof ControllerJsp20SupportOperationsImpl)){
	    return;
	}

	ControllerJsp20SupportOperationsImpl operationsImp = (ControllerJsp20SupportOperationsImpl) operations;

	// If it's not active or operation is working do nothing
	if (!operationsImp.isActivated() || operationsImp.isWorking()){
	    return;
	}

	if (fileEvent.getFileDetails().getCanonicalPath().equals(pom)) {
	    if (fileEvent.getOperation() != FileOperation.UPDATED &&  fileEvent.getOperation() != FileOperation.MONITORING_START){
		return;
	    }
	    operationsImp.projectValuesChanged();
	    operations.activeJSP20Support();
	}
    }

}
