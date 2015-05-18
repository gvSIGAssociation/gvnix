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

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * This class filter the find operation result made to OSGi framework for
 * {@link MenuOperations} service:
 * <ul>
 * <li><i>For request from other bundles</i> Return just one result:
 * {@link MenuOperationsProxy}</li>
 * <li><i>For request from this bundles</i> Return all services</li>
 * </ul>
 * <br>
 * To identify the kind of service without accessing to its class instance, it
 * uses service properties declared on {@link MenuOperationsImpl} and
 * {@link MenuOperationsProxy}. The service reference which has no properties is
 * the original service. <br>
 * <br>
 * <b>Very Important</b> This service <b>MUST HAS NONE</b> {@link Reference}
 * property to assure this component is really loaded immediately. <br>
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 * <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @see MenuOperationsProxy
 */
@Component
@Service
public class FilterMenuOperationsHook implements FindHook {

    private static Logger logger = HandlerUtils
            .getLogger(FilterMenuOperationsHook.class);

    /**
     * Service to manage
     */
    private static final String MENU_OPERATION_NAME = MenuOperations.class
            .getName();

    private BundleContext bundleContext;

    /**
     * Method call when component is activated.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        logger.finer(this.getClass().getName().concat(" activated"));
        // Store bundleContext to use it in find method
        bundleContext = context.getBundleContext();
    }

    /**
     * Filter request returned by framework related to {@link MenuOperations}. <br>
     * This method only manage the {@link MenuOperations} service request,
     * removing from returned collection {@code references} all services except
     * {@link MenuOperationsProxy}. <br>
     * For request inside this bundle all services are returned.
     *
     * @see org.osgi.framework.hooks.service.FindHook#find(org.osgi.framework.BundleContext,
     *      java.lang.String, java.lang.String, boolean, java.util.Collection)
     */
    public void find(BundleContext context, String name, String filter,
            boolean allServices, Collection references) {
        if (!MENU_OPERATION_NAME.equals(name)) {
            // It's not the managed service
            // Do nothing
            return;
        }
        if (context.getBundle().getBundleId() == 0) {
            // Don't hide anything from the system bundle
            return;
        }

        if (references.isEmpty()) {
            // Nothing to do
            return;
        }

        if (bundleContext.equals(context)) {
            // Don't hide anything to myself
            return;
        }

        // Remove all ServiceReferes except MenuOpertationProxy service
        for (Iterator iter = references.iterator(); iter.hasNext();) {
            ServiceReference sr = (ServiceReference) iter.next();
            if (isGvNIXOperations(sr)) {
                // logger.finest("   - gvNIX Menu op ( Removing)");
                iter.remove();
            }
            else if (isProxy(sr)) {
                // Don't remove proxy
                continue;
            }
            else {
                // logger.finest("   - Roo Menu op ( Removing)");
                iter.remove();
            }
        }
    }

    /**
     * Check if service reference is the service proxy <br>
     * Uses {@link MenuOperationsProxy#GVNIX_PROXY_COMPONENT} service property.
     *
     * @param sr
     * @return
     */
    public static boolean isProxy(ServiceReference sr) {
        return sr.getProperty(MenuOperationsProxy.GVNIX_PROXY_COMPONENT) != null;
    }

    /**
     * Check if service reference is gvNIX {@link MenuOperations} implementation <br>
     * Uses {@link MenuOperationsImpl#GVNIX_COMPONENT} service property.
     *
     * @param sr
     * @return
     */
    public static boolean isGvNIXOperations(ServiceReference sr) {
        return sr.getProperty(MenuOperationsImpl.GVNIX_COMPONENT) != null;
    }

    /**
     * Check if service reference is Roo original {@link MenuOperations} <br>
     * Uses {@link #isProxy(ServiceReference)} and
     * {@link #isGvNIXOperations(ServiceReference)}.
     *
     * @param sr
     * @return
     */
    public static boolean isRooOperations(ServiceReference sr) {
        return !(isProxy(sr) || isGvNIXOperations(sr));
    }
}
