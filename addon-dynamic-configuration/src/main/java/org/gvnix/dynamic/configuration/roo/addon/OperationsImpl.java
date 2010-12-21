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
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
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
  
	@Reference private MetadataService metadataService;
	@Reference private Services services;
	@Reference private Configurations configurations;

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

    // Get the active configuration and store it
    DynConfiguration dynConfig = services.getActiveConfiguration();
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
      services.setActiveConfiguration(dynConfig);
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
  public DynConfigurationList getProperties(String name) {
    
    DynConfigurationList dynConfs = new DynConfigurationList();
 
    // Find the dom configuration with requested name
    List<Element> confs = configurations.getAllComponents();
    if (confs != null) {
      
      for (Element conf : confs) {
        
        DynConfiguration dynConf = configurations.parseConfiguration(conf, name);
        dynConfs.add(dynConf);
      }
    }
    
    return dynConfs;
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
    
    prop.getChildNodes().item(3).setTextContent(value);
    configurations.saveConfiguration(prop);
    
    return configurations.parseProperty(prop);
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
