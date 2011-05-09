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

Dynamic configuration is a profiles management system as Maven or Ant build to manage diferent variable values by environment.

Requirements
============

* Independent of the build system (like Maven, Ant, ...)
* Friendly and easy to use
* Faster than write profiles directly in some build system (Maven, Ant, ...) 
* Avoid variables ``${property.name}`` along project files.
  More readable aproximation is to store the value of variable on currently active profile
* Current active profile selection

Proposals
=========

#. Maven profile addon: Modify files managed by other addons replacing some values with variables. Manage the Maven pom.xml profiles section with the values related to variables.
#. Ant profile addon: Modify files managed by other addons replacing some values with variables. Manage the Ant build.xml profiles section with the values related to variables.
#. OSGi profile addon:

 * Define a OSGi component that can be implemented by other addons to manage their own files with different profile values.
 * Out of the box, has already implemented some OSGi components to profile some files of a project, like database.properties.
 * When a profile property is changed, related OSGi component will be alerted to change the value in corresponding files.
 * The profiles information is stored on a independent and own file on project resources.  

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
     <directory>xxx</directory>
     <excludes>
       <exclude>xxx</exclude>
     </excludes>
     <filtering>xxx</filtering>
    </resource>
   </resources>

  A resource with true filtering value means a location to search and replace property variables with the selected profile value.   

* Property variables format::

   ${property.name}

  The ``property.name`` variable will be replaced with the ``property-value`` if the file location is included in resources.
  
TODO
````

* Which directories to add on resources to do the filtering of the properties defined in the profile ?
* A resources section can be defined on a profile section ?
* Use activation to set the active profile ?
* If active profile setted, ¿ what hapens if other profile is selected from maven command (-p pre) ? 

References
``````````

* `Maven introduction to profiles`_
* `Maven pom.xml profiles`_

Ant profile addon
-----------------

This option is not much interesting because Ant is not the build tool used by default on generated projects.

OSGi profile addon
------------------

Profile selection is only available on the gvNIX environment, on generated project is not possible to change selected profile.

Profile information (variables and values by environment) can be stored on same file or on separated files (one by environment).

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

It will be placed on src/main/resources folder or subfolder and its structure will be:

* OSGi component 1

  * property1 = value1
  * property2 = value2
  * ...

* OSGi component 2

  * property1 = value1
  * property2 = value2
  * ...

OSGi component
==============

Example::

  class DatabaseDynamicConfiguration implements DefaultDynamicConfiguration {

    DynPropertyList read() {
    
      // Reads database.properties values and generates an object with given format
    }
    
    void write(DynPropertyList dynProps) {
    
      // Update database.properties with values stored on the object in given format 
    }
  }

Abstract components
-------------------

There are some OSGi abstract components that can be extended to easy components creation:

* AnnotationClassDynamicConfiguration: Provides management of some annotation attributes
* PropertiesDynamicConfiguration: Provides management of some properties file
* XmlDynamicConfiguration: Provides management of some XML file
* FileDynamicConfiguration: Provides access to some file

TODO
====

* Add an abstract OSGi component to easy component creation to manage Java properties.
* Add command to export dynamic configurations to maven or ant system, Its will allow manage dynamic configurations without Roo console.
  A very interesting improvement could be allow the generation of Ant and Maven Profiles on their configuration files (build.xml ant pom.xml respectively) and replace on profile files values with variables.
  Thus on generated project the profile can be selected too.
* Add component to manage dynamic configuration on java properties
* Some files profile configuration can be standar to every projects, like log4j.properties.
  There is a standard file configuration to production environments.
  For example, log4j.properties on production environmente removes the code line of loggin messages by performance.
* Future versions commands proposal

 * configuration file
 
  * list: List all files managed by profile addon
  * add: File to add to profile addon, no included by default 
  * delete: Remove a file from profile addon
  * properties or info: Property values of a file

* urlrewrite.xml not used from Roo 1.1.0.M3

References
==========

* `Maven introduction to profiles`_ 

.. _Maven introduction to profiles: http://maven.apache.org/guides/introduction/introduction-to-profiles.html

* `Maven pom.xml profiles`_ 

.. _Maven pom.xml profiles: http://maven.apache.org/pom.html#Profiles
