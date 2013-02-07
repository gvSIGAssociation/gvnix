/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
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
package org.gvnix.dynamic.configuration.roo.addon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.config.DefaultDynamicConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Manage components of dynamic configurations.
 * <ul>
 * <li>TODO Analyze more Roo generated files to be managed</li>
 * <li>TODO What happens when two addons manage the same file ?</li>
 * <li>TODO Revert managed file changes when modification fails</li>
 * </ul>
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
@Reference(name = "components", strategy = ReferenceStrategy.LOOKUP, policy = ReferencePolicy.DYNAMIC, referenceInterface = DefaultDynamicConfiguration.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class ServicesImpl implements Services {

    private static final Logger logger = HandlerUtils
            .getLogger(ServicesImpl.class);

    private ComponentContext context;

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    /**
     * Get all the dynamic configuration components.
     * 
     * @return Dynamic configuration components
     */
    private Set<Object> getComponents() {

        return getSet("components");
    }

    /**
     * Get named configuration components.
     * 
     * @param <T> Components type
     * @param name Components name
     * @return Components
     */
    @SuppressWarnings("unchecked")
    private <T> Set<T> getSet(String name) {

        Set<T> result = new HashSet<T>();
        Object[] objs = context.locateServices(name);
        if (objs != null) {
            for (Object o : objs) {

                result.add((T) o);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration getCurrentConfiguration() {

        // Variable to store active dynamic configuration
        DynConfiguration dynConf = new DynConfiguration();
        dynConf.setActive(Boolean.TRUE);

        // Iterate all dynamic configurations components registered
        for (Object o : getComponents()) {
            try {

                // Invoke the read method of all components to get properties
                Class<? extends Object> c = o.getClass();
                Method m = c.getMethod("read", new Class[0]);
                DynPropertyList dynProps = (DynPropertyList) m.invoke(o,
                        new Object[0]);

                // Get dynamic configuration name
                m = c.getMethod("getName", new Class[0]);
                String name = (String) m.invoke(o, new Object[0]);

                // Create a dynamic configuration with component and properties
                DynComponent dynComp = new DynComponent(c.getName(), name,
                        dynProps);
                dynConf.addComponent(dynComp);
            }
            catch (NoSuchMethodException nsme) {

                logger.log(Level.SEVERE,
                        "No read method on dynamic configuration class", nsme);
            }
            catch (InvocationTargetException ite) {

                logger.log(
                        Level.SEVERE,
                        "Cannot invoke read method on dynamic configuration class",
                        ite);
            }
            catch (IllegalAccessException iae) {

                logger.log(
                        Level.SEVERE,
                        "Cannot access read method on dynamic configuration class",
                        iae);
            }
        }

        return dynConf;
    }

    /**
     * {@inheritDoc}
     */
    public String getFilePath(DynComponent dynComp) {

        String path = "";

        // Find required dynamic configuration component
        for (Object o : getComponents()) {
            if (o.getClass().getName().equals(dynComp.getId())) {

                try {

                    // Invoke the get file path method of component
                    Class<? extends Object> c = o.getClass();
                    Method m = c.getMethod("getFilePath", new Class[0]);
                    path = (String) m.invoke(o, new Object[0]);

                }
                catch (NoSuchMethodException nsme) {

                    logger.log(
                            Level.SEVERE,
                            "No get file path method on dynamic configuration class",
                            nsme);
                }
                catch (InvocationTargetException ite) {

                    logger.log(
                            Level.SEVERE,
                            "Cannot invoke get file path method on dynamic configuration class",
                            ite);
                }
                catch (IllegalAccessException iae) {

                    logger.log(
                            Level.SEVERE,
                            "Cannot access get file path method on dynamic configuration class",
                            iae);
                }

                break;
            }
        }

        return path;
    }

    /**
     * {@inheritDoc}
     */
    public DynComponent getCurrentComponent(String name) {

        // TODO Two properties with same name can exist on different components

        // Get current configuration from disk files
        DynConfiguration dynConf = getCurrentConfiguration();
        for (DynComponent dynComp : dynConf.getComponents()) {

            // Search the property on the configuration components
            for (DynProperty dynProp : dynComp.getProperties()) {
                if (dynProp.getKey().equals(name)) {

                    return dynComp;
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public DynProperty getCurrentProperty(String name) {

        // Get current component from disk files
        DynComponent dynComp = getCurrentComponent(name);

        // Search the property on the component
        if (dynComp != null) {
            for (DynProperty dynProp : dynComp.getProperties()) {
                if (dynProp.getKey().equals(name)) {

                    return dynProp;
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setCurrentConfiguration(DynConfiguration dynConf) {

        // Iterate all dynamic configurations components registered
        for (Object o : getComponents()) {
            try {

                // Invoke the read method of all components to get its
                // properties
                Class<? extends Object> c = o.getClass();
                for (DynComponent dynComp : dynConf.getComponents()) {

                    if (c.getName().equals(dynComp.getId())) {

                        @SuppressWarnings("rawtypes")
                        Class[] t = new Class[1];
                        t[0] = DynPropertyList.class;
                        Method m = c.getMethod("write", t);
                        Object[] args = new Object[1];
                        args[0] = dynComp.getProperties();
                        m.invoke(o, args);
                    }
                }
            }
            catch (NoSuchMethodException nsme) {

                logger.log(Level.SEVERE,
                        "No write method on dynamic configuration class", nsme);
            }
            catch (InvocationTargetException ite) {

                logger.log(
                        Level.SEVERE,
                        "Cannot invoke write method on dynamic configuration class",
                        ite);
            }
            catch (IllegalAccessException iae) {

                logger.log(
                        Level.SEVERE,
                        "Cannot access write method on dynamic configuration class",
                        iae);
            }
        }
    }

}
