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
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.support.OperationUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.w3c.dom.Element;

/**
 * Dynamic configuration operations.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
@Component
@Service
public class OperationsImpl implements Operations {

    @Reference
    private MetadataService metadataService;
    @Reference
    private Services services;
    @Reference
    private Configurations configurations;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private PomManager pomManager;

    private OperationUtils operationUtils;

    private static final Logger LOGGER = HandlerUtils
            .getLogger(OperationsImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    public boolean isProjectAvailable() {

        return getOperationUtils().isProjectAvailable(metadataService,
                projectOperations);
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration saveActiveConfiguration(String name) {

        // Find the configuration with requested name
        Element conf = configurations.findConfiguration(name);
        if (conf != null) {

            // If configuration already exists, delete it
            configurations.deleteConfiguration(conf);
        }

        // Create a configuration with base properties and values
        DynConfiguration dynConfig = configurations.parseConfiguration(
                configurations.getBaseConfiguration(), null);
        dynConfig.setName(name);
        configurations.addConfiguration(dynConfig);

        return dynConfig;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration setActiveConfiguration(String name) {

        DynConfiguration dynConfig = null;

        // Find the dom configuration with requested name
        Element conf = configurations.findConfiguration(name);
        if (conf != null) {

            dynConfig = configurations.parseConfiguration(conf, null);

            // Set current configuration, update active and mark as active
            services.setCurrentConfiguration(dynConfig);
            configurations.setActiveConfiguration(name);
            dynConfig.setActive(true);
        }

        return dynConfig;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfigurationList findConfigurations() {

        DynConfigurationList dynConfs = new DynConfigurationList();

        // Find all the dom configurations
        List<Element> confs = configurations.getAllConfigurations();
        for (Element conf : confs) {

            dynConfs.add(configurations.parseConfiguration(conf, null));
        }

        return dynConfs;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration getConfiguration(String name) {

        DynConfiguration dynConf = null;

        // Find the dom configuration with requested name
        Element conf = configurations.findConfiguration(name);
        if (conf != null) {

            dynConf = configurations.parseConfiguration(conf, null);
        }

        return dynConf;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration getBaseConfiguration() {

        DynConfiguration dynConf = null;

        // Find the dom configuration with requested name
        Element conf = configurations.getBaseConfiguration();
        dynConf = configurations.parseConfiguration(conf, null);

        return dynConf;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration getBaseProperty(String name) {

        // Find the dom configuration with requested name
        Element conf = configurations.getBaseConfiguration();

        return configurations.parseConfiguration(conf, name);
    }

    /**
     * {@inheritDoc}
     */
    public DynProperty updateProperty(String configuration, String property) {

        return (configurations.updateProperty(configuration, property, null));
    }

    /**
     * {@inheritDoc}
     */
    public DynProperty updateProperty(String configuration, String property,
            String value) {

        return (configurations.updateProperty(configuration, property, value));
    }

    /**
     * {@inheritDoc}
     */
    public Boolean addProperty(String name) {

        // If any component contain this property, add it
        if (getBaseProperty(name).getComponents().size() <= 0) {

            // If dynamic component or property null, return null
            DynComponent dynComp = services.getCurrentComponent(name);
            DynProperty dynProp = services.getCurrentProperty(name);
            if (dynComp == null || dynProp == null) {
                return null;
            }

            // Add the property in all configurations and base configuration
            configurations.addProperties(name, dynProp.getValue(),
                    dynComp.getId(), dynComp.getName());
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfigurationList export() {

        // Find all stored dynamic configurations
        DynConfigurationList dynConfs = findConfigurations();
        if (dynConfs.isEmpty()) {

            // If no dynamic configurations, return empty list
            return dynConfs;
        }

        // Iterate exported dynamic configurations for write to pom
        dynConfs = pomManager.export(dynConfs);
        for (DynConfiguration dynConf : dynConfs) {

            // Store created vars name
            services.setCurrentConfiguration(dynConf);
        }

        return dynConfs;
    }

    public OperationUtils getOperationUtils() {
        if (operationUtils == null) {
            // Get all Services implement OperationUtils interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                OperationUtils.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    operationUtils = (OperationUtils) this.context
                            .getService(ref);
                    return operationUtils;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load OperationUtils on Dynamic Configuration OperationsImpl.");
                return null;
            }
        }
        else {
            return operationUtils;
        }

    }

}
