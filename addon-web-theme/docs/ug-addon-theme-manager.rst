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

TODO Validate next subsections are valid already. 

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

Installation
============

Installation
------------

Install gvNIX or the Add-on in Roo shell.

Create a web application
-------------------------

Create a web application with gvNix/Roo Shell script::

    project --topLevelPackage org.gvnix.test.menu
    jpa setup --provider HIBERNATE --database HYPERSONIC_IN_MEMORY 
    entity jpa --class ~.domain.Person --testAutomatically 
    field string --fieldName name --notNull
    web mvc setup 
    web mvc all --package ~.web

Run the application and see the actual style::

  mvn tomcat:run

Stop tomcat and apply gvNix theme (theme install and set commands) to the project::

  theme install --id gvnix
  theme set --id gvnix

Restart the application and see the changes::

  mvn tomcat:run

Stop the application and apply CIT theme using the addon commands::

  theme install --id cit
  theme set --id cit

Run the application and see the actual style::

  mvn tomcat:run
