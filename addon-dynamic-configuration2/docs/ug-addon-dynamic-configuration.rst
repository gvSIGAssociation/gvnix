========================================
 Dynamic Configuration Addon User Guide
========================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev$
:Date:      $Date$

.. contents::
   :depth: 3
   :backlinks: none

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 
94105, USA.

Introduction
============

Dynamic configuration allows you to manage diferent values by environment along different project files.

Files available in configurations
=================================

* Properties
 
  * src/main/resources/META-INF/spring/database.properties
  * src/main/resources/log4j.properties

* Java

  * Java annotation attributes of ``GvNIXWebServiceProxy`` annotation on ``Service Layer`` addon

* Xml
 
  * pom.xml
  * src/main/resources/META-INF/persistence.xml
  * src/main/resources/META-INF/spring/applicationContext.xml
  * src/main/webapp/WEB-INF/urlrewrite.xml
  * src/main/webapp/WEB-INF/web.xml
  * src/main/webapp/WEB-INF/spring/webmvc-config.xml

Addon commands
==============

* configuration save: Create a new configuration with a name

  * name (Mandatory): Name of the configuration to be created

  New configuration will store some project file properties.
  When saved, this configuration is not set as ``Active``.

* configuration activate: Update project files with the configuration property values

  * name (Mandatory): Name of the configuration to be activated
  
  Update project file properties with the properties saved in configuration.
  When a configuration is active, configuration property updates will modify project files too.  
  If some project file is modified while a configuration is active, new activation will not be allowed until:
  
  * Undo project files modifications
  * Update previous active configuration properties
  * Save previous active configuration
  * Delete previous active configuration

* configuration unactivate: Unlink the project files from the active configuration

  * name (Mandatory):  Name of the configuration to be unactivated
  
  When a configuration is unactive, configuration property updates will not modify project files.  
  If some project file is modified while a configuration is active, unactivation will not be allowed until:
  
  * Undo project files modifications
  * Update active configuration properties
  * Save active configuration
  * Delete active configuration

* configuration list: List all previously saved configurations

  Active configuration is marked with the ``Active`` text previously to the configuration name.

* configuration delete: Remove a previously saved configuration

  * name (Mandatory): Name of the configuration to be removed

* configuration property list: Show the properties stored in a configuration

  * name (Optional): Name of the configuration to list properties

  If ``name`` command option not specified, list of added configuration properties will be showed without values.

* configuration property values: Show distinct property values along configurations

  * name (Mandatory): Property name
  
* configuration property update: Set a value in a configuration property

  * configuration (Mandatory): Name of the configuration to update
  * property (Mandatory): Name of the property to update
  * value (Mandatory): Value to set
  
  If configuration is active, property will be update on project file too.  

* configuration property add: Make available a property in the configurations

  * name (Mandatory): Name of the property to add

  Property will be added in all configurations with current project file value.

* configuration property delete: Remove a property from all configurations

  * name (Mandatory): Name of the property to delete
