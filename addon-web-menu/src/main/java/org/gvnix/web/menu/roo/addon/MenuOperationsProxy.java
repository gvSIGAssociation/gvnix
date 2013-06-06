/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011, 2012, 2013 CIT -
 * Generalitat Valenciana
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

import static org.gvnix.web.menu.roo.addon.FilterMenuOperationsHook.isGvNIXOperations;
import static org.gvnix.web.menu.roo.addon.FilterMenuOperationsHook.isRooOperations;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * This class is a proxy service for {@link MenuOperations}. <br>
 * This instance delegates on {@link MenuOperationsImpl} or in original Roo's
 * {@link MenuOperations} service depending on runtime parameters. <br>
 * <br>
 * <b>Very Important</b> This service <b>MUST HAS NONE</b> {@link Reference}
 * property to assure this component is really loaded immediately. <br>
 * <br>
 * {@link MenuOperations} services will get manually on the first service
 * request. <br>
 * This service requires {@link FilterMenuOperationsHook} service.
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @see MenuOperations
 * @see FilterMenuOperationsHook
 * @see MenuOperationsImpl
 */
@Component(immediate = true)
@Service
public class MenuOperationsProxy implements MenuOperations {

    private static Logger logger = HandlerUtils
            .getLogger(MenuOperationsProxy.class);

    private static final String MENU_OPERATION_NAME = MenuOperations.class
            .getName();

    /*
     * =========================================================== VERY
     * IMPORTANT: This class **MUST HAS NONE** @Reference property to assure
     * this component is really loaded immediately
     * ===========================================================
     */

    /**
     * Property to identify this service in {@link FilterMenuOperationsHook} and
     * {@link MenuOperationsProxy}
     */
    @Property(boolValue = true)
    public static final String GVNIX_PROXY_COMPONENT = "gvNIXProxyComponent";

    /**
     * gvNIX {@link MenuOperations} implementation
     */
    private MenuOperationsImpl gvNIXOperations = null;

    /**
     * {@link ServiceReference} to gvNIX {@link MenuOperations} implementation
     * (to perform unget on component deactivate)
     */
    private ServiceReference gvNIXOperationsService = null;

    /**
     * Original {@link MenuOperations}
     */
    private MenuOperations rooOperations = null;

    /**
     * {@link ServiceReference} to original {@link MenuOperations}
     * implementation (to perform unget on component deactivate)
     */
    private ServiceReference rooOperationsService = null;

    /**
     * BunleContext
     */
    private BundleContext context;

    /** {@inheritDoc} */
    protected void activate(ComponentContext context) {
        logger.finer("MenuOperation proxy activated");
        this.context = context.getBundleContext();
    }

    /** {@inheritDoc} */
    protected void deactivate(ComponentContext context) {
        // unget from gvNIX menu operation
        gvNIXOperations = null;
        context.getBundleContext().ungetService(gvNIXOperationsService);
        gvNIXOperationsService = null;

        // unget from original menu operation
        rooOperations = null;
        context.getBundleContext().ungetService(rooOperationsService);
        rooOperationsService = null;
    }

    /**
     * Wait until {@link #gvNIXOperations} and {@link #rooOperations} are
     * resolved.
     */
    private void waitToOperations() {
        if (!(gvNIXOperations == null && rooOperations == null)) {
            return;
        }
        else {
            resolveRerences();
            if (!(gvNIXOperations == null && rooOperations == null)) {
                return;
            }
        }

        while (gvNIXOperations == null && rooOperations == null) {
            try {
                Thread.sleep(100);
                resolveRerences();
            }
            catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

    /**
     * Try to initialize {@link #gvNIXOperations} and {@link #rooOperations}.
     * Also store its {@link ServiceReference} so it can perform a unget on
     * component deactivate.
     */
    private void resolveRerences() {
        // logger.info("Revolving ops");
        ServiceReference[] sr;
        try {
            // FilterMenuOperationHook will return ALL services available
            sr = context.getAllServiceReferences(MENU_OPERATION_NAME, null);
        }
        catch (InvalidSyntaxException e) {
            logger.log(
                    Level.SEVERE,
                    "Error getting ".concat(MENU_OPERATION_NAME).concat(
                            " service references"), e);
            return;
        }
        if (gvNIXOperations == null) {
            // locate gvNIX MenuOperations service
            for (int i = 0; i < sr.length; i++) {
                ServiceReference serviceReference = sr[i];
                if (isGvNIXOperations(serviceReference)) {
                    gvNIXOperationsService = serviceReference;
                    gvNIXOperations = (MenuOperationsImpl) context
                            .getService(serviceReference);
                    break;
                }
            }
        }
        if (rooOperations == null) {
            // locate original MenuOperations service
            for (int i = 0; i < sr.length; i++) {
                ServiceReference serviceReference = sr[i];
                if (isRooOperations(serviceReference)) {
                    rooOperationsService = serviceReference;
                    rooOperations = (MenuOperations) context
                            .getService(serviceReference);
                    break;
                }
            }
        }
    }

    /**
     * Gets the {@link MenuOperations} service instance to use
     * 
     * @return
     */
    private MenuOperations getOperations() {
        // Assure than all required services are loaded
        waitToOperations();

        if (gvNIXOperations.isGvNixMenuAvailable()) {
            logger.finest("Using gvNIX Menu ops");
            return gvNIXOperations;
        }
        else {
            logger.finest("Using Roo Menu ops");
            return rooOperations;
        }
    }

    /**
     * ========================================= Delegated
     * {@link MenuOperations} methods This methods use {@link #getOperations()}
     * to decide which service call =========================================
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#addMenuItem
     * (org.springframework.roo.model.JavaSymbolName,
     * org.springframework.roo.model.JavaSymbolName, java.lang.String,
     * java.lang.String, java.lang.String,
     * org.springframework.roo.project.LogicalPath)
     */
    public void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String globalMessageCode, String link,
            String idPrefix, LogicalPath logicalPath) {
        getOperations().addMenuItem(menuCategoryName, menuItemId,
                globalMessageCode, link, idPrefix, logicalPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#addMenuItem
     * (org.springframework.roo.model.JavaSymbolName,
     * org.springframework.roo.model.JavaSymbolName, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String,
     * org.springframework.roo.project.LogicalPath)
     */
    public void addMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemId, String menuItemLabel,
            String globalMessageCode, String link, String idPrefix,
            LogicalPath logicalPath) {
        getOperations().addMenuItem(menuCategoryName, menuItemId,
                menuItemLabel, globalMessageCode, link, idPrefix, logicalPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#
     * cleanUpFinderMenuItems(org.springframework.roo.model.JavaSymbolName,
     * java.util.List, org.springframework.roo.project.LogicalPath)
     */
    public void cleanUpFinderMenuItems(JavaSymbolName menuCategoryName,
            List<String> allowedFinderMenuIds, LogicalPath logicalPath) {
        getOperations().cleanUpFinderMenuItems(menuCategoryName,
                allowedFinderMenuIds, logicalPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations#cleanUpMenuItem
     * (org.springframework.roo.model.JavaSymbolName,
     * org.springframework.roo.model.JavaSymbolName, java.lang.String,
     * org.springframework.roo.project.LogicalPath)
     */
    public void cleanUpMenuItem(JavaSymbolName menuCategoryName,
            JavaSymbolName menuItemName, String idPrefix,
            LogicalPath logicalPath) {
        getOperations().cleanUpMenuItem(menuCategoryName, menuItemName,
                idPrefix, logicalPath);
    }
}
