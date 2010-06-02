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
package org.gvnix.web.menu.roo.addon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.menu.MenuOperations;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.event.ProcessManagerStatus;
import org.springframework.roo.process.manager.event.ProcessManagerStatusListener;
import org.springframework.roo.process.manager.event.ProcessManagerStatusProvider;
import org.springframework.roo.support.util.Assert;

/**
 * Starndard Roo menu operation service implementation for gvNIX menu
 *
 *
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Service
@Component(immediate = true)
public class MenuOperationsImpl implements MenuOperations {

    private static Logger logger = Logger.getLogger(MenuOperationsImpl.class
	    .getName());
    private ComponentContext context;

    @Reference
    private ProcessManagerStatusProvider processManagerStatus;

    private MenuPageOperations operations;
    private ServiceReference operationsServiceReference;

    private List<DelayedOperation> delayedOperations = new ArrayList<DelayedOperation>();
    private StatusListener statusListener;

    protected void activate(ComponentContext context) {
	this.context = context;
	statusListener = new StatusListener(this);
	processManagerStatus.addProcessManagerStatusListener(statusListener);
	// logger.warning("*** Activated gvNIX MenuOperationsImpl");
    }

    protected void deactivate(ComponentContext context) {
	if (operations != null) {
	    if (operations instanceof MenuPageOperationsImpl) {
		performDelayed((MenuPageOperationsImpl) operations);
	    }

	    context.getBundleContext().ungetService(
		    getOperationsServiceReference());
	}
	processManagerStatus.removeProcessManagerStatusListener(statusListener);
	this.context = null;

    }

    private MenuPageOperations getOperations() {
	if (operations == null) {
	    operations = (MenuPageOperations) context.getBundleContext()
		    .getService(getOperationsServiceReference());
	}
	return operations;
    }

    private ServiceReference getOperationsServiceReference() {
	if (operationsServiceReference == null) {
	    operationsServiceReference = context.getBundleContext()
		    .getServiceReference(MenuPageOperations.class.getName());
	}
	return operationsServiceReference;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.roo.addon.web.menu.MenuOperations#addMenuItem(org
     * .springframework.roo.model.JavaSymbolName,
     * org.springframework.roo.model.JavaSymbolName, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void addMenuItem(JavaSymbolName menuCategoryName,
	    JavaSymbolName menuItemName, String globalMessageCode, String link,
	    String idPrefix) {

	MenuPageOperationsImpl operationsImpl = null;
	Object operations = getOperations();
	if (operations != null) {
	    Assert.isInstanceOf(MenuPageOperationsImpl.class, operations);
	    operationsImpl = (MenuPageOperationsImpl) operations;
	}

	if (operationsImpl == null || !operationsImpl.isCorrectlyInstalled()) {
	    // If we can't perform operation adds it to delayed operations queue
	    delayedOperations.add(new AddMenuItem(menuCategoryName,
		    menuItemName, globalMessageCode, link, idPrefix));
	    return;
	} else {
            // performs delayed operations if any
	    performDelayed(operationsImpl);
	}

	operationsImpl.addOrUpdatePageFromRooMenuItem(menuCategoryName,
		menuItemName, globalMessageCode, link, idPrefix);
    }

    synchronized void performDelayed(MenuPageOperationsImpl operationsImpl) {
	// TODO Implement a way to perform this periodically
	Iterator<DelayedOperation> iter = delayedOperations.iterator();
	if (!iter.hasNext()) {
	    return;
	}
	//logger.warning("*** Delayed operations: "+delayedOperations.size());
	DelayedOperation cur;
	while (iter.hasNext()) {
	    cur = iter.next();
	    cur.execute(operationsImpl);
	    iter.remove();
	}
	//logger.warning("*** Completed Delayed operations: " + delayedOperations.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.roo.addon.web.menu.MenuOperations#cleanUpFinderMenuItems
     * (org.springframework.roo.model.JavaSymbolName, java.util.List)
     */
    public void cleanUpFinderMenuItems(JavaSymbolName menuCategoryName,
	    List<String> allowedFinderMenuIds) {

	MenuPageOperationsImpl operationsImpl = null;
	Object operations = getOperations();
	if (operations != null) {
	    Assert.isInstanceOf(MenuPageOperationsImpl.class, operations);
	    operationsImpl = (MenuPageOperationsImpl) operations;
	}

	if (operationsImpl == null || !operationsImpl.isCorrectlyInstalled()) {
	    // If we can't perform operation adds it to delayed operations queue
	    delayedOperations.add(new CleanUpFinderMenuItems(menuCategoryName,
		    allowedFinderMenuIds));
	    return;
	} else {
            // performs delayed operations if any
	    performDelayed(operationsImpl);
	}

	operationsImpl.cleanUpRooFinderMenuItems(menuCategoryName,
		allowedFinderMenuIds);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.roo.addon.web.menu.MenuOperations#cleanUpMenuItem
     * (org.springframework.roo.model.JavaSymbolName,
     * org.springframework.roo.model.JavaSymbolName, java.lang.String)
     */
    public void cleanUpMenuItem(JavaSymbolName menuCategoryName,
	    JavaSymbolName menuItemName, String idPrefix) {

	MenuPageOperationsImpl operationsImpl = null;
	Object operations = getOperations();
	if (operations != null) {
	    Assert.isInstanceOf(MenuPageOperationsImpl.class, operations);
	    operationsImpl = (MenuPageOperationsImpl) operations;
	}

	if (operationsImpl == null || !operationsImpl.isCorrectlyInstalled()) {
	    // If we can't perform operation adds it to delayed operations queue
	    delayedOperations.add(new CleanUpMenuItem(menuCategoryName,
		    menuItemName, idPrefix));
	    return;
	} else {
            // performs delayed operations if any
	    performDelayed(operationsImpl);
	}

	operationsImpl.cleanUpRooMenuItem(menuCategoryName, menuItemName,
		idPrefix);
    }

    abstract class DelayedOperation {
	abstract void execute(MenuPageOperationsImpl operationsImpl);
    };

    class CleanUpMenuItem extends DelayedOperation {
	public CleanUpMenuItem(JavaSymbolName menuCategoryName,
		JavaSymbolName menuItemName, String idPrefix) {
	    super();
	    this.menuCategoryName = menuCategoryName;
	    this.menuItemName = menuItemName;
	    this.idPrefix = idPrefix;
	}

	JavaSymbolName menuCategoryName;
	JavaSymbolName menuItemName;
	String idPrefix;

	@Override
	void execute(MenuPageOperationsImpl operationsImpl) {
	    operationsImpl.cleanUpRooMenuItem(menuCategoryName, menuItemName,
		    idPrefix);
	}
    }

    class CleanUpFinderMenuItems extends DelayedOperation {
	public CleanUpFinderMenuItems(JavaSymbolName menuCategoryName,
		List<String> allowedFinderMenuIds) {
	    super();
	    this.menuCategoryName = menuCategoryName;
	    this.allowedFinderMenuIds = allowedFinderMenuIds;
	}

	JavaSymbolName menuCategoryName;
	List<String> allowedFinderMenuIds;

	@Override
	void execute(MenuPageOperationsImpl operationsImpl) {
	    operationsImpl.cleanUpRooFinderMenuItems(menuCategoryName,
		    allowedFinderMenuIds);
	}
    }

    class AddMenuItem extends DelayedOperation {
	public AddMenuItem(JavaSymbolName menuCategoryName,
		JavaSymbolName menuItemName, String globalMessageCode,
		String link, String idPrefix) {
	    super();
	    this.menuCategoryName = menuCategoryName;
	    this.menuItemName = menuItemName;
	    this.globalMessageCode = globalMessageCode;
	    this.link = link;
	    this.idPrefix = idPrefix;
	}

	JavaSymbolName menuCategoryName;
	JavaSymbolName menuItemName;
	String globalMessageCode;
	String link;
	String idPrefix;

	@Override
	void execute(MenuPageOperationsImpl operationsImpl) {
	    operationsImpl.addOrUpdatePageFromRooMenuItem(menuCategoryName,
		    menuItemName, globalMessageCode, link, idPrefix);

	}
    }

    class StatusListener implements ProcessManagerStatusListener {

	MenuOperationsImpl menuOperations;

	public StatusListener(MenuOperationsImpl menuOperations) {
	    super();
	    this.menuOperations = menuOperations;
	}

	public void onProcessManagerStatusChange(
		ProcessManagerStatus oldStatus, ProcessManagerStatus newStatus) {
	    if (oldStatus == ProcessManagerStatus.STARTING
		    && newStatus == ProcessManagerStatus.AVAILABLE) {

		MenuPageOperations operations = menuOperations.getOperations();
		if (!(operations instanceof MenuPageOperationsImpl)) {
		    return;
		}
		// If operations are finished perform delayed operations if any
		MenuPageOperationsImpl operationsImpl = (MenuPageOperationsImpl) operations;
		menuOperations.performDelayed(operationsImpl);
	    }

	}

    }
}
