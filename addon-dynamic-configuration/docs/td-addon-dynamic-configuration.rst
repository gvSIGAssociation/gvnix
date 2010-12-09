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

Dynamic configuration is a system as Profiles on Maven ant Ant build systems to manage diferent variables values by environment.

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

* Which directories add on resources to do the filtering of the properties defined in the profile ?
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

TODO
````

* A very interesting improvement could be allow the generation of Ant and Maven Profiles on their configuration files (build.xml ant pom.xml respectively) and replace on profile files values with variables.
  Thus on generated project the profile can be selected too.

* By default, create a default or current profile with the currently existing values on the project files ?  

Conclusion
----------

Maven and ant profile addon proposals are not desired because is not best than manage the profile section manually in configuration files (pom.xml and build.xml, respectively).
Therefore, OSGi profile addon is a better aproach.

Files to include on profiles
============================

There are some important directories on a project:

#. src/main/java: Java files with main source code
#. src/test/java: Java files with test source code
#. src/main/resources: Resources with main configuration
#. src/test/resources: Resources with test configuration
#. src/main/webapp: Web application files

Possible files to include on first version
------------------------------------------

* Properties
 
  * src/main/resources/META-INF/spring/database.properties

Possible files to include on second version
-------------------------------------------

* Java

  * Classes of service layer addon has annotations with attributes values that changed by profile as the imported service URL

Possible files to include on future version
-------------------------------------------

* Properties

  * src/main/resources/log4j.properties

* Java

  * Java properties

* Xml
 
  * pom.xml
  * build.xml
  * src/main/resources/META-INF/persistence.xml
  * src/main/resources/META-INF/spring/applicationContext.xml
  * src/main/webapp/WEB-INF/urlrewrite.xml
  * src/main/webapp/WEB-INF/web.xml
  * src/main/webapp/WEB-INF/spring/webmvc-config.xml

This is a non extensive list, it could not have all interesting files.

TODO
----

* ¿ Include java, resources and/or webapp locations on profiles ?
* ¿ Include main and/or test locations on profiles ?

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

  @DynamicConfiguration(file=".../database.properties")
  class DatabaseDynamicConfiguration implements DynamicConfiguration {

    void write(SomeFileFormat file) {
    
      // Update database.properties with values stored on the file in given format 
    }

    SomeFileFormat read() {
    
      // Reads database.properties values and generates a file with given format
    }
  }

TODO
----

* Provide some utils to read/write SomeFileFormat and read/write Properties ?
* If some profile file updated, some monitor is triggered that overwrite changes ?
* Two addons can manage the same profile file ? 
* What happens when a profile monitored file is deleted, renamed or moved ?

Environments
============

By default, could have some default environments:

* dev: Development
* pre: Pre-production
* pro: Production

The default can be the development environment.

Commands
========

First version commands proposal
-------------------------------

* profile

  * save <name>: Save properties and values to a profile with some required name.
    When saved, all property names and values are showed and is not set as the ``Active`` profile.
    The saving action reads the source file performed by its OSGi component and is saved to metadata file on resources. 
  * activate <name>: Set a required profile name as the currently active profile.
    When activated, all property names and values are showed.
    The activate action writes the source file performed by its OSGi component from metadata file on resources.
    If some change is maded on profile files, thereafter active profile will be the ``Modified`` one.

Second version commands proposal
--------------------------------

* profile

  * list: List all previously saved profile names.
    At least, ``Modified`` profile is always present.
    Active profile is marked with the ``Active`` text next to the profile name.
    Active profile is the one whose values are equals to profile files values.
  * delete <name>: Clear a required profile name.

* profile property

  * list <profile>: List all property names and values of a required profile name.
  * value <property>: Show all values of required property on all existing profiles.
  * update <profile> <property> <value>: Actualize a required property of a required profile with some required value.

Properties command info
```````````````````````

Keyword:                   properties list
Description:               Shows the details of a particular properties file

 Keyword:                  name
 
   Help:                   Property file name (including .properties suffix)
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'
   
 Keyword:                  path
 
   Help:                   Source path to property file
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'

Keyword:                   properties remove
Description:               Removes a particular properties file property

 Keyword:                  name
 
   Help:                   Property file name (including .properties suffix)
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'
   
 Keyword:                  path
 
   Help:                   Source path to property file
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'
   
 Keyword:                  ** default **
 Keyword:                  key
 
   Help:                   The property key that should be removed
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'

Keyword:                   properties set
Description:               Changes a particular properties file property

 Keyword:                  name
 
   Help:                   Property file name (including .properties suffix)
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'
   
 Keyword:                  path
 
   Help:                   Source path to property file
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'
   
 Keyword:                  key
 
   Help:                   The property key that should be changed
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'
   
 Keyword:                  value
 
   Help:                   The new vale for this property key
   Mandatory:              true
   Default if specified:   '__NULL__'
   Default if unspecified: '__NULL__'

TODO
````

* ``Modified`` profile always reads from read method of related OSGi component. 
* If one profile is active and something is modified on disk, which profile becomes active ?
* If two profiles has same values, then this profiles will be the ``Active`` profile at same time. 

Future versions commands proposal
---------------------------------
  
* profile property

  * add: Add new property to all profiles.
  * delete: a property deletion of a profile could required to delete same property in all other profiles  

* profile file

  * list: List all files managed by profile addon
  * add: File to add to profile addon, no included by default 
  * delete: Remove a file from profile addon
  * properties or info: Property values of a file

TODO
====

* Analyze Apache Config to load diferent file formats on a same Java configuration object.
* Generate war package with profile name sufix to distinguish it from different profile wars of same project.
* Search Roo information about profiles on forum, documentation, etc.
  If any information exists, to create an entry on Roo forum to comment about the proposal. 
* What happens if one property exists on a profile and non in others ?
* Some files profile configuration can be standar to every projects, like log4j.properties.
  There is a standard file configuration to production environments.
  For example, log4j.properties on production environmente removes the code line of loggin messages by performance.

References
==========

* `Maven introduction to profiles`_ 

.. _Maven introduction to profiles: http://maven.apache.org/guides/introduction/introduction-to-profiles.html

* `Maven pom.xml profiles`_ 

.. _Maven pom.xml profiles: http://maven.apache.org/pom.html#Profiles
