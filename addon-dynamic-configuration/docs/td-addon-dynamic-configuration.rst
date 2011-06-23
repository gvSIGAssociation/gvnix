==============================================
 Dynamic Configuration Addon Technical Design
==============================================

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

Dynamic configuration gives you the ability to change project global settings depending on the environment where it is being built.

Requirements
============

* Independent of the build system (like Maven, Ant, ...)
* Friendly and easy to use
* Faster than write profiles directly in some build system (Maven, Ant, ...)
* Current active profile selection

Proposals
=========

#. Maven profile addon: Modify files managed by other addons replacing some values with variables. Manage the Maven pom.xml profiles section with the values related to variables.
#. Ant profile addon: Modify files managed by other addons replacing some values with variables. Manage the Ant build.xml profiles section with the values related to variables.
#. OSGi profile addon:

 * Define a OSGi component that can be implemented by other addons to manage their own files with different configuration values.
 * Out of the box, has already implemented some OSGi components to configure some files of a project, like database.properties.
 * The configuration information is stored on a independent and own file on project resources.

Maven profile addon
-------------------

This option is interesting because Maven is the build tool used by default on generated projects.

* Use maven profiles section at pom.xml::

   <profiles>
     <profile>
       <id>environment-name</id>
       <activation>...</activation>
       <build>...</build>
       <modules>...</modules>
       <repositories>...</repositories>
       <pluginRepositories>...</pluginRepositories>
       <dependencies>...</dependencies>
       <reporting>...</reporting>
       <dependencyManagement>...</dependencyManagement>
       <distributionManagement>...</distributionManagement>
       <properties>...</properties>
     </profile>
   </profiles>

  This is a non extensive example, it could not have all available profile configurations.

  Multiple environments could be defined, one on each profile section.

  More info at `Maven pom.xml profiles`_

* Use the properties section at pom.xml profile to store different properties and values::

   <properties>
     <property.name>property-value</name>
     ...
   </properties>

* Target locations to search in property variables to be replaces are defined at pom.xml resources section::

   <resources>
   <resource>
     <directory>src/main/resources</directory>
     <filtering>true</filtering>
    </resource>
   </resources>

  A resource with true filtering value means a location to search and replace property variables with the selected profile value.

* Property variables format::

   ${property.name}

  The ``property.name`` variable will be replaced with the ``property-value`` if the file location is included in resources.

References
``````````

* `Maven introduction to profiles`_
* `Maven pom.xml profiles`_

Ant profile addon
-----------------

This option is not much interesting because Ant is not the build tool used by default on generated projects.

OSGi profile addon
------------------

Configuration definition is available on the gvNIX environment, and addon can export configuration to maven profiles. 

Profile information (variables and values by environment) is stored on same file.

OSGi arquitecture
`````````````````

The creation of an OSGi component requires to define an interface that must implement the component classes adding the @Component and @Service annotations from org.apache.felix.scr.annotations package.
To obtain the list of components, use the locateServices("name") method of org.osgi.service.component.ComponentContext OSGi component.

Example:

* Class ``org.springframework.roo.shell.SimpleParser``

  This class manage all available Roo shell commands provided by diferent addons through OSGi components.
  This class has the @org.apache.felix.scr.annotations.Component and @org.apache.felix.scr.annotations.Service annotations.

  * Class annotation ``@org.apache.felix.scr.annotations.Reference(name="commands", strategy=ReferenceStrategy.LOOKUP, policy=ReferencePolicy.DYNAMIC, referenceInterface=CommandMarker.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)``

    Defines references to other services made available to the component.
    Take notice that attribute ``referenceInterface=CommandMarker.class`` is the Java interface implemented by each command class.

  * Property ``org.osgi.service.component.ComponentContext context``

    This property is used by the component instance to interact with its execution context including locating services by reference name.

  * Method ``Object[] objs = context.locateServices("commands");``

    Returns the service objects for the specified reference name.

  * Utility::

  @SuppressWarnings("unchecked")
  private <T> Set<T> getSet(String name) {
    Set<T> result = new HashSet<T>();
    Object[] objs = context.locateServices(name);
    if (objs != null) {
      for (Object o : objs) {
        result.add((T) o);
      }
    }
    if ("commands".equals(name)) {
      result.add((T) this);
    }
    return result;
  }

  * For each Object on Set, get all methods with ``java.lang.reflect.Method[] methods = getClass().getMethods();``

  * To invoke some ``java.lang.reflect.Method``, use reflection with ``invoke`` method

Conclusion
----------

Maven and ant profile addon proposals are not desired because is not best than manage the profile section manually in configuration files (pom.xml and build.xml, respectively).
Therefore, OSGi profile addon is a better aproach.

Metadata
========

It will be placed on src/main/resources/dynamic-configuration.xml and its structure will be::

	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
	<dynamic-configuration>
		<active>dev</active>
		<base>
	        <component id="org.gvnix.dynamic.configuration.roo.addon.config.DatabaseDynamicConfiguration" name="Database Connection Properties">
	            <property>
	                <key>database.url</key>
	                <value>jdbc:hsqldb:mem:petclinic</value>
	            </property>
	        </component>
	    </base>
	<configuration name="dev">
	        <component id="org.gvnix.dynamic.configuration.roo.addon.config.DatabaseDynamicConfiguration" name="Database Connection Properties">
	            <property>
	                <key>database.url</key>
	                <value>jdbc:hsqldb:mem:mydevdb</value>
	            </property>
	        </component>
	    </configuration>
	<configuration name="pro">
	        <component id="org.gvnix.dynamic.configuration.roo.addon.config.DatabaseDynamicConfiguration" name="Database Connection Properties">
	            <property>
	                <key>database.url</key>
	                <value>jdbc:hsqldb:file:myprodb</value>
	            </property>
	        </component>
	    </configuration>
	</dynamic-configuration>

OSGi component
==============

Example::

  @Component
  @Service
  class MyDynamicConfiguration implements DefaultDynamicConfiguration {

    DynPropertyList read() {

      // Reads file values and generates an object with given format
    }

    void write(DynPropertyList dynProps) {

      // Update file with values stored on the object in given format
    }
  }

This OSGi components can be implemented into other addons and will be obtained by OSGi framework by this addon to manage configuration properties defined by them.
By example, gvNIX addon-cit-security and addon-service defines own dynamic configuration OSGi components for their configuration files.

Abstract components
-------------------

There are some OSGi abstract components that can be extended to easy components creation:

* PropertiesDynamicConfiguration: Provides management of some properties file
* PropertiesListDynamicConfiguration: Provides management of a properties file list matching prefix and/or sufix files name
* XmlDynamicConfiguration: Provides management of some XML file
* XpathAttributesDynamicConfiguration: Provides management of some XML attributes defined by a Xpath expression
* XpathElementsDynamicConfiguration: Provides management of some XML elements defined by a Xpath expression

TODO
====

* In export command add a parameter with the target build tool (mvn, ant, ...) because currently, only mvn build tool available.
* Some files profile configuration can be standar to every projects, like log4j.properties.
  There is a standard file configuration to production environments.
  For example, log4j.properties on production environmente removes the code line of loggin messages by performance.
* Future versions commands proposal

 * configuration file

  * add: File to add to configuration management

* What happens if Roo changes some configuration file like persistence.xml or database.properties when this files are already managed by dynamic configuration ?

References
==========

* `Maven introduction to profiles`_

.. _Maven introduction to profiles: http://maven.apache.org/guides/introduction/introduction-to-profiles.html

* `Maven pom.xml profiles`_

.. _Maven pom.xml profiles: http://maven.apache.org/pom.html#Profiles
