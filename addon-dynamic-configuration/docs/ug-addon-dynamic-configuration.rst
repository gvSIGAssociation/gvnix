========================================
 Dynamic Configuration Addon User Guide
========================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: DGTI - Generalitat Valenciana
:Author:    DISID Corporation, S.L.
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

Dynamic configuration gives you the ability to change project global settings depending on the environment where it is being built.

Files available in configurations
=================================

* Properties
 
  * src/main/resources/META-INF/spring/database.properties
  * src/main/resources/log4j.properties

* Xml
 
  * src/main/resources/META-INF/persistence.xml

Addon commands
==============

* configuration create - Define a new configuration with a name

 * name: Name for defined configuration

* configuration property add - Make a property available for all configurations

 * name: Name of property to add

* configuration property value - Set new values into a configuration property

 * configuration: Name of configuration to update
 * property: Name of configuration to update
 * value: New value to set
 
* configuration property undefined - Set no value into a configuration property

 * configuration: Name of configuration to update
 * property: ame of property to update

* configuration list - List all created configurations and their properties

* configuration export - Write current configurations into project
