============================
 gvNIX Theme Manager add-on
============================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    [ ... ]
:Revision:  $Rev$
:Date:      $Date$

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Requirements
=============

Development mode
-----------------

Set the environment variable ROO_THEMES = "gvNIX/themes" to retrieve the default theme location. Optional value In development mode.
If you want to try new themes copy the theme folder into ``gvNIX/themes`` to be available for the Add-on.

Runtime mode
-------------

gvNIX script sets the variable ``ROO_HOME`` to the working directory then the Add-on search for this variable and adds ``themes`` directory.
Define the variable in roo-dev script for development mode.

Make gvNIX installation
------------------------

Copy the folder ``gvNIX/themes`` to ``roo/trunk``.

Installation Proof
===================

Install the Add-on.
--------------------

Install the Add-on in gvNIX/Roo shell with the required commands for 1.1.0-M1 version.

Create a web application
-------------------------

Create a web application with gvNix/Roo Shell script::

    project --topLevelPackage org.gvnix.test.menu --java 5
    persistence setup --provider HIBERNATE --database HYPERSONIC_IN_MEMORY 
    entity --class ~.domain.Person --testAutomatically 
    field string --fieldName name --notNull 
    dependency add --groupId javax.xml.bind --artifactId jaxb-api --version 2.1
    dependency add --groupId com.sun.xml.bind --artifactId jaxb-impl --version 2.1.3
    controller all --package ~.web

Run the application and see the actual style::

  mvn tomcat:run

Stop tomcat and apply gvNix theme (Install and set gvNix commands) to the project::

  theme install --name theme-gvNIX
  theme set --name theme-gvNIX

Restart the application and see the changes::

  mvn tomcat:run

Stop the application and apply theme-cit using the gvNix commands::

  theme install --name theme-gvNIX
  theme set --name theme-gvNIX

Create another entity and its controllers using Roo commands to see the menu update::

  entity --class ~.domain.Car --testAutomatically 
  field string --fieldName name --notNull 

Run the appliation:

  mvn tomcat:run
  
And the cit menu show the new entries. Roo updated menu.jspx.
