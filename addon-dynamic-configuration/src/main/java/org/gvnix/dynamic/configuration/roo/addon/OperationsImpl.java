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
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponentList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Dynamic configuration operations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
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
    ProjectOperations projectOperations;
    @Reference
    FileManager fileManager;

    public boolean isProjectAvailable() {
        return getPathResolver() != null;
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

        // Store a configuration with base properties
        DynConfiguration dynConfig = configurations.parseConfiguration(
                configurations.getBaseConfiguration(), null);
        dynConfig.setName(name);

        for (DynComponent dynComp : dynConfig.getComponents()) {
            for (DynProperty dynProp : dynComp.getProperties()) {

                dynProp.setValue(services.getCurrentProperty(dynProp.getKey())
                        .getValue());
            }
        }

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

            // Get current dynamic configuration from configuration file
            DynConfiguration dynConfigActive = configurations
                    .getActiveConfiguration();

            // If no dynamic configuration or is the current, activate it
            if (dynConfigActive == null
                    || dynConfigActive.equals(services
                            .getCurrentConfiguration())) {

                // Set current configuration, update active and mark it as
                // active
                services.setCurrentConfiguration(dynConfig);
                configurations.setActiveConfiguration(name);
                dynConfig.setActive(true);
            } else {

                // This configuration can not be activated
                dynConfig.setActive(false);
            }
        }

        return dynConfig;
    }

    /**
     * {@inheritDoc}
     */
    public DynConfiguration setUnactiveConfiguration(String name) {

        DynConfiguration dynConfig = null;

        // Find the dom configuration with requested name
        Element conf = configurations.findConfiguration(name);
        if (conf != null) {

            dynConfig = configurations.parseConfiguration(conf, null);

            // Get current dynamic configuration from configuration file
            DynConfiguration dynConfigActive = configurations
                    .getActiveConfiguration();

            // If no dynamic configuration or is the current, activate it
            if (dynConfigActive == null
                    || dynConfigActive.equals(services
                            .getCurrentConfiguration())) {

                // Update active and mark it as inactive
                configurations.setActiveConfiguration("");
                dynConfig.setActive(false);
            } else {

                // This configuration can not be unactivated
                dynConfig.setActive(true);
            }
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
    public boolean deleteConfiguration(String name) {

        // Find the dom configuration with requested name
        Element conf = configurations.findConfiguration(name);
        if (conf != null) {

            // If configuration already exists, delete it
            configurations.deleteConfiguration(conf);

            return true;
        }

        return false;
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
    public DynConfigurationList getProperties(String name) {

        DynConfigurationList dynConfs = new DynConfigurationList();

        // Find the dom configuration with requested name
        List<Element> confs = configurations.getAllComponents();
        if (confs != null) {

            for (Element conf : confs) {

                // Add only configurations with some component
                DynConfiguration dynConf = configurations.parseConfiguration(
                        conf, name);
                if (dynConf.getComponents().size() > 0) {
                    dynConfs.add(dynConf);
                }
            }
        }

        return dynConfs;
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
    public DynProperty updateProperty(String configuration, String property,
            String value) {

        // Get the required property element to update
        Element prop = configurations.getProperty(configuration, property);
        if (prop == null) {
            return null;
        }

        // Update property value on configuration
        prop.getChildNodes().item(3).setTextContent(value);
        configurations.saveConfiguration(prop);

        // Update property value on file if configuration is active
        if (configurations.getActiveConfiguration() != null
                && configuration.equals(configurations.getActiveConfiguration()
                        .getName())) {

            DynConfiguration dynConf = getBaseProperty(property);
            dynConf.getComponents().get(0).getProperties().get(0)
                    .setValue(value);
            services.setCurrentConfiguration(dynConf);
        }

        return configurations.parseProperty(prop);
    }

    /**
     * {@inheritDoc}
     */
    public boolean addProperty(String name) {

        // If any component contain this property, add it
        if (getBaseProperty(name).getComponents().size() <= 0) {

            // Add the property in all configurations and base configuration
            configurations.addProperties(name, services
                    .getCurrentProperty(name).getValue(), services
                    .getCurrentComponent(name).getId(), services
                    .getCurrentComponent(name).getName());
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean deleteProperty(String name) {

        // If some components contains this property, delete it
        if (getBaseProperty(name).getComponents().size() > 0) {

            // Remove property from all configurations and base configuration
            configurations.deleteProperties(name,
                    services.getCurrentComponent(name).getId());
            return true;
        }

        return false;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void export() {

        // <project>
        // ...
        // <build>
        // ...
        // <resources>
        // <resource>
        // <directory>src/main/resources</directory>
        // <includes>
        // <include>log4j.properties</include>
        // </includes>
        // <filtering>true</filtering>
        // </resource>
        // <resource>
        // <directory>src/main/resources</directory>
        // <excludes>
        // <exclude>log4j.properties</exclude>
        // </excludes>
        // <filtering>false</filtering>
        // </resource>
        // </resources>
        // ...
        // </build>
        // <profiles>
        // <profile>
        // <id>test</id>
        // <properties>
        // <log4j.rootLogger>INFO, stdout</log4j.rootLogger>
        // </properties>
        // </profile>
        // </profiles>
        // ...
        // </project>

        // Pom root element
        String pom = projectOperations.getPathResolver().getIdentifier(
                Path.ROOT, "pom.xml");
        Document doc = XmlUtils.readXml(fileManager.getInputStream(pom));
        Element root = doc.getDocumentElement();

        // Profiles section: find or create if not exists
        Element profs = XmlUtils.findFirstElement("/project/profiles", root);
        if (profs == null) {

            profs = doc.createElement("profiles");
            root.appendChild(profs);
        }

        // Iterate stored dynamic configurations for export to pom
        DynConfigurationList dynConfs = findConfigurations();
        for (DynConfiguration dynConf : dynConfs) {

            // Create a profile section for dynamic configuration
            Element prof = doc.createElement("profile");
            profs.appendChild(prof);

            // TODO Profile with this id can exist yet
            // Create an identifier for profile
            Element id = doc.createElement("id");
            id.setTextContent(dynConf.getName());
            prof.appendChild(id);

            // Iterate components of dynamic configuration for export to pom
            DynComponentList dynComps = dynConf.getComponents();
            for (DynComponent dynComp : dynComps) {

                // Create a properties section for dynamic configuration
                Element props = doc.createElement("properties");
                prof.appendChild(props);

                // TODO Nothing to do with next values ?
                // dynComp.getId();
                // dynComp.getName();

                // Iterate properties of dynamic configuration for export to pom
                DynPropertyList dynProps = dynComp.getProperties();
                for (DynProperty dynProp : dynProps) {

                    // Create a property element for each dynamic property
                    Element prop = doc.createElement(dynProp.getKey());
                    props.appendChild(prop);

                    // Store dynamic property value in pom and replace dynamic
                    // property value with a var
                    prop.setTextContent(dynProp.getValue());
                    dynProp.setValue("${" + dynProp.getKey() + "}");
                }
            }

            // Store dynamic configuration to replace dynamic property values
            // with created vars name
            services.setCurrentConfiguration(dynConf);
        }

        // TODO Finalize
        // Element build = XmlUtils.findFirstElement("/project/build", root);
        // if (build == null) {
        //
        // build = doc.createElement("build");
        // root.appendChild(build);
        // }
        // Element resos = XmlUtils.findFirstElement("resources", build);
        // if (resos == null) {
        //
        // resos = doc.createElement("resources");
        // build.appendChild(resos);
        // }
        // Element reso = XmlUtils.findFirstElement(
        // "resource/filtering[text()='true']", resos);
        // if (reso == null) {
        //
        // reso = doc.createElement("resource");
        // Element dir = doc.createElement("directory");
        // dir.setTextContent("src/main/resources");
        // reso.appendChild(dir);
        // resos.appendChild(reso);
        // }

        fileManager.createOrUpdateTextFileIfRequired(pom,
                XmlUtils.nodeToString(doc), false);
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {

        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        if (projectMetadata == null) {

            return null;
        }

        return projectMetadata.getPathResolver();
    }

}
