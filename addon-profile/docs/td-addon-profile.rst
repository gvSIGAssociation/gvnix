================================
 Profile Addon Technical Design
================================

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

Requirements
============

* Independent of the build system (like Maven, Ant, ...)
* Friendly and easy to use
* Faster than write profiles directly in some build system (Maven, Ant, ...) 
* Avoid variables ``${property.name}`` along project files.
  More readable aproximation is store the value of the variable on currently active profile
* Current active profile selection

Proposals
=========

#. Maven profile addon: Modify files managed by other addons replacing some values with variables. Manage the Maven pom.xml profiles section with the values related to variables.
#. Ant profile addon: Modify files managed by other addons replacing some values with variables. Manage the Ant build.xml profiles section with the values related to variables.
#. OSGi profile addon:

 * Define a OSGi component that can be implemented by other addons that would manage their own files with different profile values.
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
* If active profile setted, ¿ whart hapends if other profile is selected from maven command (-p pre) ? 

References
``````````

* `Maven introduction to profiles`_
* `Maven pom.xml profiles`_

Ant profile addon
-----------------

This option is not very interesting because Ant is not the build tool used by default on generated projects.

OSGi profile addon
------------------

Profile selected on project is only available on the gvNIX environment, on generated project is not possible to change selected profile.
A very interesting improvement could be allow the generation of Ant and Maven Profiles on their configuration files and replace on profile files values with variables.
Thus on generated project the profile can be selected too.

Profile information (variables and values by environment) can be stored on same file or on separated files (one by environment).

TODO
````

* Create a OSGi component requires to define an interface that can be implemented by some class with @Component and @Service from org.apache.felix.scr.annotations ?
  Then, ¿ how obtain this list of components ? See Roo shell addon and related commands (CommandMarker interface and @CliCommand annotation).   

Conclusion
----------

Maven and ant profile addon proposals are not desired because is not best than manage the profile section manually in configuration files (pom.xml and build.xml, respectively).
Therefore, OSGi profile addon is a better aproach.

Anyway, is an interesting future option the generation of build systems configuration file (pom.xml, build.xml, ...) to allow profile selection on generated projects. 

Files to include on profiles
============================

There are some important directories on a project:

#. src/main/java: Java files with main source code
#. src/test/java: Java files with test source code
#. src/main/resources: Resources with main configuration
#. src/test/resources: Resources with test configuration
#. src/main/webapp: Web application files

Possible files to include on profiles:

* Xml
 
  * pom.xml
  * build.xml
  * src/main/resources/META-INF/persistence.xml
  * src/main/resources/META-INF/spring/applicationContext.xml
  * src/main/webapp/WEB-INF/urlrewrite.xml
  * src/main/webapp/WEB-INF/web.xml
  * src/main/webapp/WEB-INF/spring/webmvc-config.xml
 
* Properties
 
  * src/main/resources/META-INF/spring/database.properties
  * src/main/resources/log4j.properties

* Java

  * Classes of service layer addon has annotations with attributes values that changed by profile as the imported service URL
  * Java properties

This is a non extensive list, it could not have all interesting files.

TODO
----

* ¿ Include java, resources and/or webapp locations on profiles ?
* ¿ Include main and/or test locations on profiles ?

Environments
============

By default, could have some default environments:

* dev: Development
* pre: Pre-production
* pro: Production

The default can be the development environment.

Commands
========

Next are a first commands proposal:

* environment profile

  * list
  * add
  * delete: 
  * default or set or activate
  * properties or info
  * load: Load all files properties defined and their properties and values
  * save: Save loaded properties and values to a profile
  
* environtment property

  * list
  * add
  * delete: a property deletion of a profile could required to delete same property in all other profiles  
  * update
  * values or info

* environment file

  * list
  * add: File to add to profile system, no included by default 
  * delete
  * properties or info

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
